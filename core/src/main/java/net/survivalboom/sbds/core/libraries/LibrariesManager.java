package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.*;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.http.HttpFileDownloader;
import net.survivalboom.sbds.api.utils.json.JsonConfiguration;
import net.survivalboom.sbds.core.SBDS;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LibrariesManager  implements ILibrariesManager {

    private static final Logger log = LoggerFactory.getLogger("LibrariesManager");


    private final File dir;

    private final JarLoader jarLoader;

    private SBDS sbds = null;


    private final Map<String, Library> cachedLibraries = new HashMap<>();


    public LibrariesManager(@NotNull File file, @NotNull JarLoader jarLoader) {

        Objects.requireNonNull(file, "file == null");
        if (file.isFile()) throw new IllegalArgumentException("File object represents an existing file, not a folder. Do you want to fuck yourself?");

        this.jarLoader = jarLoader;
        this.dir = file;

        //noinspection ResultOfMethodCallIgnored
        file.mkdirs();

    }


    public void configure(@NotNull SBDS sbds) {
        if (this.sbds != null) throw new RuntimeException("Пошел нахуй, чурка!");
        this.sbds = sbds;
    }

    //
    //
    //

    @Override
    public boolean satisfy(@NotNull IModule module, @NotNull ConfigurationSection section) {
        Objects.requireNonNull(module, "module == null");
        return satisfy0(module, section, false);
    }


    public synchronized boolean satisfy0(@Nullable IModule module, @NotNull ConfigurationSection librariesSection, boolean ignoreState) {

        Objects.requireNonNull(librariesSection, "section == null");

        if (module != null && !ignoreState) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module attempted to download libraries.");
        }

        for (String s : librariesSection.getKeys(false)) {

            ConfigurationSection section = librariesSection.getConfigurationSection(s);
            if (section == null) continue;

            LibrarySearchInfo searchInfo;
            try {
                searchInfo = LibrarySearchInfo.create(section);
            }

            catch (LibrarySectionParseException e) {
                log.warn("Invalid library `{}` section. Skipping...", s, e);
                return false;
            }

            Library library;
            try {
                library = findLibrary(searchInfo);
            }

            catch (UnknownLibraryException e) {
                log.error("Library `{}` not found in repositories.", s);
                return false;
            }

            catch (UnknownDependencyException e) {
                log.error("Could not find a dependency `{}` from the library `{}`.", e.searchInfo().gradle(), s);
                return false;
            }

            log.info("Successfully found `{}`!", library);


        }

        return true;

    }


    public @NotNull Library findLibrary(@NotNull LibrarySearchInfo info) throws UnknownLibraryException, UnknownDependencyException {

        String gradleString = info.gradle();

        if (cachedLibraries.containsKey(gradleString)) {
            System.out.println("Loaded library `" + gradleString + "` from cache!");
            return cachedLibraries.get(gradleString);
        }

        ConfigurationSection pom = findPom(info);

        List<String> repositories = findRepositories(pom);

        Library library = new Library(info, repositories, pom);

        Library parent = findParent(library);
        if (parent != null) library.setParent(parent);

        Placeholders properties = findProperties(library);
        library.setProperties(properties);

        Map<String, String> bomDependenciesVersions = findBomDependenciesVersions(library);
        library.setBomDependenciesVersions(bomDependenciesVersions);

        List<Library> bomDependencyProviders = findBomProviders(library);
        library.setDependencyProviders(bomDependencyProviders);

        List<Library> dependencies = findDependencies(library);
        library.setDependencies(dependencies);

        cachedLibraries.put(gradleString, library);

        return library;

    }

    //
    // findRepositories();
    //

    private @NotNull List<String> findRepositories(@NotNull ConfigurationSection pom) {

        List<Map<?, ?>> section = pom.getMapList("project.repositories.repository");
        if (section.isEmpty()) return List.of(ILibrariesManager.MAVEN_CENTRAL_URL);

        List<String> out = new ArrayList<>();
        out.add(ILibrariesManager.MAVEN_CENTRAL_URL);

        for (Map<?, ?> map : section) {
            out.add((String) map.get("url"));
        }

        return out;

    }

    //
    // findParent();
    //

    private @Nullable Library findParent(@NotNull Library library) throws UnknownLibraryException, UnknownDependencyException {

        ConfigurationSection parentSection = library.getPom().getConfigurationSection("project.parent");
        if (parentSection == null) return null;

        String group = parentSection.getString("groupId");
        String artifact = parentSection.getString("artifactId");
        String version = parentSection.getString("version");

        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");

        LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, library.getRepositories());

        return findLibrary(info);

    }

    //
    // findProperties();
    //

    private @NotNull Placeholders findProperties(@NotNull Library library) {

        Placeholders placeholders = new Placeholders();

        Library parent = library.getParent();
        if (parent != null) placeholders.addAll(findProperties(parent));

        ConfigurationSection propertiesSection = library.getPom().getConfigurationSection("project.properties");
        if (propertiesSection == null) return placeholders;

        return placeholders.addAll(getAllProperties(propertiesSection, null))
                .add("${project.version}", library.getInfo().version())
                .add("${project.name}", library.getName())
                .add("${project.description}", library.getDescription())
                .add("${project.groupId}", library.getInfo().group())
                .add("${project.artifactId}", library.getInfo().artifact())
                .selfParseValues();

    }

    private @NotNull Placeholders getAllProperties(@NotNull ConfigurationSection properties, @Nullable String path) {

        Placeholders out = new Placeholders();

        ConfigurationSection section = path == null ? properties : properties.getConfigurationSection(path);
        Objects.requireNonNull(section);

        for (String s : section.getKeys(false)) {

            String key = path == null ? s : path + "." + s;

            ConfigurationSection sect = properties.getConfigurationSection(key);
            if (sect == null) {
                String pKey = "${" + key.replace("!", "") + "}";
                String value = properties.getString(key);
                out.add(pKey, value);
            }

            else out.addAll(getAllProperties(properties, key));

        }

        out.selfParseValues();

        return out;

    }

    //
    // findBom()
    //
    private @NotNull Map<String, String> findBomDependenciesVersions(@NotNull Library library) {

        Map<String, String> bom = new HashMap<>();

        List<Map<?, ?>> section = library.getPom().getMapList("project.dependencyManagement.dependencies.dependency");
        if (section.isEmpty()) return bom;

        Placeholders placeholders = library.getProperties();

        for (Map<?, ?> map : section) {

            String group = placeholders.parse((String) map.get("groupId"));
            String artifact = placeholders.parse((String) map.get("artifactId"));
            String version = placeholders.parse(map.get("version").toString());

            bom.put(group + ":" + artifact, version);

        }

        return bom;

    }

    private @NotNull List<Library> findBomProviders(@NotNull Library library) throws UnknownDependencyException, UnknownLibraryException {

        List<Library> out = new ArrayList<>();

        List<Map<?, ?>> section = library.getPom().getMapList("project.dependencyManagement.dependencies.dependency");
        if (section.isEmpty()) return out;

        Placeholders placeholders = library.getProperties();

        for (Map<?, ?> map : section) {

            if (!map.containsKey("type") || !map.containsKey("scope")) continue;

            String group = placeholders.parse((String) map.get("groupId"));
            String artifact = placeholders.parse((String) map.get("artifactId"));
            String version = placeholders.parse(map.get("version").toString());

            LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, library.getRepositories());

            Library provider = findLibrary(info);

            out.add(provider);

        }

        return out;

    }

    //
    // findDependencies()
    //

    private @NotNull List<Library> findDependencies(@NotNull Library library) throws UnknownDependencyException {

        List<String> repositories = library.getRepositories();
        Placeholders properties = library.getProperties();

        List<Map<?, ?>> section = library.getPom().getMapList("project.dependencies.dependency");

        List<Library> out = new ArrayList<>();
        for (Map<?, ?> map : section) {

            String group = properties.parse((String) map.get("groupId"));
            String artifact = properties.parse((String) map.get("artifactId"));

            String scope = (String) map.get("scope");
            if (scope != null && !scope.equals("compile") && !scope.equals("runtime")) {
                continue;
            }

            String version = map.containsKey("version") ? map.get("version").toString() : library.getBomVersion(group + ":" + artifact);
            Objects.requireNonNull(version, "No bom version available for `" + group + ":" + artifact + "`");

            version = properties.parse(version);

            LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, repositories);

            Library dependency;
            try {
                dependency = findLibrary(info);
            }

            catch (UnknownLibraryException e) {
                log.error("Could not find dependency `{}` of `{}`", info.gradle(), library.getInfo().gradle(), e);
                throw new UnknownDependencyException("Could not find library `" + info.gradle() + "`.", info);
            }

            out.add(dependency);

        }

        return out;


    }


    //
    // findPom();
    //

    private @NotNull ConfigurationSection findPom(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        if (isLocalPomExists(info)) {
            try {
                return loadLocalPom(info);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> repositories = info.repositories();
        if (repositories.size() == 1) {
            return findPomInRepository(repositories.getFirst(), info);
        }

        return findPomInRepositories(info);

    }

    private boolean isLocalPomExists(@NotNull LibrarySearchInfo info) {

        File pomFile = info.pomFile(dir);
        File pomUrlFile = info.pomUrlFile(dir);

        return pomFile.exists() && pomUrlFile.exists();

    }


    private @NotNull ConfigurationSection loadLocalPom(@NotNull LibrarySearchInfo info) throws IOException {

        File pomFile = info.pomFile(dir);
        File pomUrlFile = info.pomUrlFile(dir);

        String url;
        try (InputStream in = new FileInputStream(pomUrlFile)) {
            url = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        byte[] bytes;
        try (InputStream in = new FileInputStream(pomFile)) {
            bytes = in.readAllBytes();
        }

        return loadPomFile(bytes, url);

    }

    private @NotNull ConfigurationSection findPomInRepositories(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        List<String> repositories = info.repositories();
        for (String repo : repositories) {

            try {

                return findPomInRepository(repo, info);

            }

            catch (UnknownLibraryException e) {
                log.warn("Failed to retrieve pom from `{}`.", repo, e);
            }

        }

        throw new UnknownLibraryException("Library `" + info.gradle() + "` was not found in " + repositories.size() + " repositories.");

    }

    private @NotNull ConfigurationSection findPomInRepository(@NotNull String repo, @NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        String url = info.urlPom(repo);

        log.info("Searching `{}` in `{}`", info.gradle(), url);

        byte[] bytes;

        try {

            try (InputStream in = new URI(url).toURL().openStream()) {
                bytes = in.readAllBytes();
            }

        }

        catch (Exception e) {
            throw new UnknownLibraryException("Library `" + info.gradle() + "` not found in repository `" + repo + "`. " + e);
        }

        return loadPomFile(bytes, repo);

    }

    private @NotNull ConfigurationSection loadPomFile(byte[] bytes, @NotNull String repo) {

        String string = new String(bytes, StandardCharsets.UTF_8);

        JSONObject json = XML.toJSONObject(string);
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.loadFromJson(json);

        jsonConfiguration.set("url", repo);

        return jsonConfiguration;

    }




}
