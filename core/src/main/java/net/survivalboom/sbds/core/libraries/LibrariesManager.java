package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.*;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.api.utils.http.HttpFileDownloader;
import net.survivalboom.sbds.api.utils.json.JsonConfiguration;
import net.survivalboom.sbds.core.SBDS;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
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
import java.nio.file.Files;
import java.util.*;

public class LibrariesManager  implements ILibrariesManager {

    private static final Logger log = LoggerFactory.getLogger("LibrariesManager");


    private final File dir;

    private final JarLoader jarLoader;

    private SBDS sbds = null;


    private final Map<String, Library> cachedLibraries = new HashMap<>();

    private final List<PomFile> cachedPoms = new ArrayList<>();


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

    public @NotNull List<Library> getLibraries() {
        return new ArrayList<>(cachedLibraries.values());
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
                library = findLibrary(searchInfo, true);
            }

            catch (UnknownLibraryException e) {
                log.error("Library `{}` not found in repositories.", s);
                return false;
            }

            catch (UnknownDependencyException e) {
                log.error("Could not find a dependency `{}` from the library `{}`.", e.searchInfo().gradle(), s);
                return false;
            }

            try {
                downloadLibrary(library);
            }

            catch (LibraryDownloadException e) {
                log.error("Failed to download `{}`. An exception occurred.", searchInfo);
                return false;
            }


        }

        downloadPoms();

        return true;

    }


    public @NotNull Library findLibrary(@NotNull LibrarySearchInfo info, boolean resolveDependencies) throws UnknownLibraryException, UnknownDependencyException {

        String gradleString = info.gradle();

        if (cachedLibraries.containsKey(gradleString)) {
            return cachedLibraries.get(gradleString);
        }

        PomFile pom = findPom(info);

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

        if (resolveDependencies) {
            List<Library> dependencies = findDependencies(library);
            library.setDependencies(dependencies);
        }

        else library.setDependencies(new ArrayList<>());

        cachedLibraries.put(gradleString, library);

        return library;

    }

    //
    // downloadLibraries()
    //
    public void downloadLibrary(@NotNull Library library) throws LibraryDownloadException {

        Objects.requireNonNull(library);

        for (Library dependency : library.getDependencies()) downloadLibrary(dependency);

        LibrarySearchInfo info = library.getInfo();
        File file = info.jarFile(dir);

        if (file.exists()) {
            jarLoader.mountJar(file);
            return;
        }

        String url = info.urlJar(library.getPom().url());

        try {

            log.info("Downloading `{}` from `{}`...", file.getName(), url);
            try (InputStream in = new URI(url).toURL().openStream()) {
                Files.copy(in, file.toPath());
            }

            jarLoader.mountJar(file);

        }

        catch (IOException | URISyntaxException e) {
            throw new LibraryDownloadException(e);
        }

    }

    public void downloadPoms() {

        for (PomFile pom : cachedPoms) {

            if (checkLocalPom(pom.info())) continue;

            File pomFile = pom.info().pomFile(dir);
            File urlFile = pom.info().pomUrlFile(dir);

            log.info("Caching `{}` on disk...", pomFile.getName());

            try {

                write(pomFile, pom.original());
                write(urlFile, pom.url());

            }

            catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    private void write(@NotNull File file, @NotNull String string) throws IOException {

        file.createNewFile();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(string.getBytes(StandardCharsets.UTF_8));
        }

    }

    // TODO Зробити перевірку на вже встановлену нову версію, якщо є, не шукати стару версію.
    private boolean isNewerVersion(String newVersion, String oldVersion) {
        if (newVersion == null || oldVersion == null) throw new IllegalArgumentException("Version cannot be null");

        String[] newParts = newVersion.split("[-.]");
        String[] oldParts = oldVersion.split("[-.]");

        int length = Math.max(newParts.length, oldParts.length);

        for (int i = 0; i < length; i++) {
            String newPart = i < newParts.length ? newParts[i] : "0";
            String oldPart = i < oldParts.length ? oldParts[i] : "0";

            // Проверяем, число это или нет
            boolean isNewNumeric = newPart.matches("\\d+");
            boolean isOldNumeric = oldPart.matches("\\d+");

            if (isNewNumeric && isOldNumeric) {
                // Сравниваем числа
                int newNum = Integer.parseInt(newPart);
                int oldNum = Integer.parseInt(oldPart);
                if (newNum > oldNum) return true;
                if (newNum < oldNum) return false;
            } else {
                // Если одно число, а другое строка → число "старше"
                if (isNewNumeric) return true;
                if (isOldNumeric) return false;

                // Сравниваем строки (beta, RC, SNAPSHOT)
                int cmp = newPart.compareTo(oldPart);
                if (cmp > 0) return true;
                if (cmp < 0) return false;
            }
        }

        return false; // Версии равны
    }


    //
    // findRepositories();
    //

    private @NotNull List<String> findRepositories(@NotNull PomFile pom) {

        List<Map<?, ?>> section = pom.pom().getMapList("project.repositories.repository");
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

        ConfigurationSection parentSection = library.getPom().pom().getConfigurationSection("project.parent");
        if (parentSection == null) return null;

        String group = parentSection.getString("groupId");
        String artifact = parentSection.getString("artifactId");
        String version = parentSection.getString("version");

        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");

        LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, library.getRepositories());

        return findLibrary(info, false);

    }

    //
    // findProperties();
    //

    private @NotNull Placeholders findProperties(@NotNull Library library) {

        Placeholders placeholders = new Placeholders();

        Library parent = library.getParent();
        if (parent != null) placeholders.addAll(findProperties(parent));

        ConfigurationSection propertiesSection = library.getPom().pom().getConfigurationSection("project.properties");
        if (propertiesSection != null) placeholders.addAll(getAllProperties(propertiesSection, null));

        return placeholders
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

        List<Map<?, ?>> section = library.getPom().pom().getMapList("project.dependencyManagement.dependencies.dependency");
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

        List<Map<?, ?>> section = library.getPom().pom().getMapList("project.dependencyManagement.dependencies.dependency");
        if (section.isEmpty()) return out;

        Placeholders placeholders = library.getProperties();

        for (Map<?, ?> map : section) {

            String scope = (String) map.get("scope");
            String type = (String) map.get("type");

            if (scope == null || !scope.equals("import") || type == null || !type.equals("pom")) continue;

            String group = placeholders.parse((String) map.get("groupId"));
            String artifact = placeholders.parse((String) map.get("artifactId"));
            String version = placeholders.parse(map.get("version").toString());

            LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, library.getRepositories());

            Library provider = findLibrary(info, false);

            out.add(provider);

        }

        return out;

    }

    //
    // findDependencies()
    //

    private @NotNull List<Library> findDependencies(@NotNull Library library) throws UnknownDependencyException {

        List<Library> out = new ArrayList<>();

        List<String> repositories = library.getRepositories();
        Placeholders properties = library.getProperties();

        List<Map<?, ?>> section = library.getPom().pom().getMapList("project.dependencies.dependency");

        if (section.isEmpty()) {

            ConfigurationSection dSect = library.getPom().pom().getConfigurationSection("project.dependencies.dependency");
            if (dSect == null) return out;

            Map<String, String> m = new HashMap<>();
            for (String key : dSect.getKeys(false)) {
                m.put(key, dSect.getString(key));
            }

            section.add(m);

        }

        for (Map<?, ?> map : section) {

            String group = properties.parse((String) map.get("groupId"));
            String artifact = properties.parse((String) map.get("artifactId"));

            String scope = (String) map.get("scope");
            if (scope != null && !scope.equals("compile") && !scope.equals("runtime")) {
                continue;
            }

            Boolean optional = (Boolean) map.get("optional");
            if (optional != null && optional) {
                continue;
            }

            String version = map.containsKey("version") ? map.get("version").toString() : library.getBomVersion(group + ":" + artifact);
            if (version == null) {
                log.warn("No BOM version found for dependency `{}`. Required by `{}`. Trying to use latest version...", group + ":" + artifact, library);
                version = findLatestVersion(group, artifact);
                Objects.requireNonNull(version, "CACUS!");
            }

            version = properties.parse(version);

            LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, repositories);

            Library dependency;
            try {
                dependency = findLibrary(info, true);
            }

            catch (UnknownLibraryException e) {
                log.error("Could not find dependency `{}` of `{}`", info.gradle(), library.getInfo().gradle(), e);
                throw new UnknownDependencyException("Could not find library `" + info.gradle() + "`.", info);
            }

            out.add(dependency);

        }

        return out;


    }



    private @Nullable String findLatestVersion(@NotNull String group, @NotNull String artifact) {

        String url = ILibrariesManager.MAVEN_CENTRAL_URL + group.replace(".", "/") + "/" + artifact + "/maven-metadata.xml";

        log.info("Searching for latest version `{}` on `{}`...", group + ":" + artifact, url);

        byte[] bytes;
        try {

            try (InputStream in = new URI(url).toURL().openStream()) {
                bytes = in.readAllBytes();
            }

        }

        catch (IOException | URISyntaxException e) {
            log.warn("Error!", e);
            return null;
        }

        String string = new String(bytes, StandardCharsets.UTF_8);

        JSONObject json = XML.toJSONObject(string);

        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.loadFromJson(json);

        String ver = jsonConfiguration.getString("metadata.versioning.release");
        if (ver == null) ver = jsonConfiguration.getString("metadata.version");

        return ver;

    }


    //
    // findPom();
    //

    private @NotNull PomFile findPom(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        PomFile cachedPomFile = cachedPoms.stream().filter(p -> p.info().equals(info)).findAny().orElse(null);

        if (cachedPomFile != null) {
            return cachedPomFile;
        }

        PomFile pom;

        if (checkLocalPom(info)) {
            try {
                pom = loadLocalPom(info);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        else {

            List<String> repositories = info.repositories();
            if (repositories.size() == 1) {
                pom = findPomInRepository(repositories.getFirst(), info);
            }

            else {

                pom = findPomInRepositories(info);

            }

        }

        cachedPoms.add(pom);

        return pom;

    }

    private boolean checkLocalPom(@NotNull LibrarySearchInfo info) {

        File pomFile = info.pomFile(dir);
        File urlFile = info.pomUrlFile(dir);

        return pomFile.exists() && urlFile.exists();

    }


    private @NotNull PomFile loadLocalPom(@NotNull LibrarySearchInfo info) throws IOException {

        File pomFile = info.pomFile(dir);
        File urlFile = info.pomUrlFile(dir);

        byte[] bytes;
        try (InputStream in = new FileInputStream(pomFile)) {
            bytes = in.readAllBytes();
        }

        String url;
        try (FileInputStream in = new FileInputStream(urlFile)) {
            url = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        return loadPomFile(info, url, bytes);

    }

    private @NotNull PomFile findPomInRepositories(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

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

    private @NotNull PomFile findPomInRepository(@NotNull String repo, @NotNull LibrarySearchInfo info) throws UnknownLibraryException {

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

        return loadPomFile(info, repo, bytes);

    }

    private @NotNull PomFile loadPomFile(@NotNull LibrarySearchInfo info, @NotNull String url, byte[] bytes) {

        String string = new String(bytes, StandardCharsets.UTF_8);

        JSONObject json = XML.toJSONObject(string);
        JsonConfiguration jsonConfiguration = new JsonConfiguration();
        jsonConfiguration.loadFromJson(json);

        return new PomFile(info, url, jsonConfiguration, string);

    }




}
