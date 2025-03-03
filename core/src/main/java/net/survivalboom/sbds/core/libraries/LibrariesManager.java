package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.*;
import net.survivalboom.sbds.api.modules.IModule;
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

            catch (UnknownDependencyException e) {
                log.error("Could not find a dependency `{}` from the library `{}`.", e.searchInfo().gradle(), s);
                return false;
            }

            catch (UnknownLibraryException e) {
                log.error("Library `{}` not found in repositories.", s);
                return false;
            }


            try {
                download(library);
            }

            catch (LibraryDownloadException e) {
                log.info("Failed to download library `{}`.", s);
                return false;
            }

        }

        return true;

    }


    private void download(@NotNull Library library) throws LibraryDownloadException {

        for (Library dependency : library.getDependencies()) {
            download(dependency);
        }

        LibrarySearchInfo info = library.getInfo();

        File jarFile = new File(dir, info.jarFileName());
        File pomFile = new File(dir, info.pomFileName());

        if (jarFile.exists() && pomFile.exists()) {
            jarLoader.mountJar(jarFile);
            return;
        }

        if (pomFile.exists() && !jarFile.exists() && library.getUrl() == null) {
            log.error("ERR!");
            return;
        }

        String url = library.getUrl();
        Objects.requireNonNull(url);

        log.info("~ Downloading `{}` from {}", library.getName(), url);

        HttpFileDownloader jarDownloader = new HttpFileDownloader(info.urlJar(url), jarFile);
        HttpFileDownloader pomDownloader = new HttpFileDownloader(info.urlPom(url), pomFile);

        try {
            pomDownloader.download();
            jarDownloader.download();
        }

        catch (IOException | URISyntaxException e) {
            throw new LibraryDownloadException(e);
        }

        jarLoader.mountJar(jarFile);
        log.info("> Mounted `{}`.", jarFile.getName());

    }


    private @NotNull Library findLibrary(@NotNull LibrarySearchInfo info) throws UnknownLibraryException, UnknownDependencyException {

        String gradleString = info.gradle();
        if (cachedLibraries.containsKey(gradleString)) {
            return cachedLibraries.get(gradleString);
        }

        ConfigurationSection pom = findPom(info);
        String url = pom.getString("url");

        List<Library> dependencies = resolveDependencies(pom);

        Library library = new Library(info, url, dependencies, Objects.requireNonNull(pom.getConfigurationSection("project")));

        cachedLibraries.put(gradleString, library);

        return library;

    }

    //
    // resolveDependencies();
    //

    private @NotNull List<Library> resolveDependencies(@NotNull ConfigurationSection pom) throws UnknownDependencyException {

        List<String> repositories = findRepositories(pom);

        List<Map<?, ?>> section = pom.getMapList("project.dependencies.dependency");

        List<Library> out = new ArrayList<>();
        for (Map<?, ?> map : section) {

            String group = (String) map.get("groupId");
            String artifact = (String) map.get("artifactId");
            String version = map.get("version").toString();

            String scope = (String) map.get("scope");
            if (scope != null && !scope.equals("compile") && !scope.equals("runtime")) {
                continue;
            }

            LibrarySearchInfo info = new LibrarySearchInfo(group, artifact, version, repositories);

            Library library;
            try {
                library = findLibrary(info);
            }

            catch (UnknownLibraryException e) {
                throw new UnknownDependencyException("Could not find library `" + info.gradle() + "`.", info);
            }

            out.add(library);

        }

        return out;

    }

    private @NotNull List<String> findRepositories(@NotNull ConfigurationSection pom) {

        List<Map<?, ?>> section = pom.getMapList("project.repositories.repository");
        if (section.isEmpty()) return List.of(ILibrariesManager.MAVEN_CENTRAL_URL);

        List<String> out = new ArrayList<>();
        for (Map<?, ?> map : section) {
            out.add((String) map.get("url"));
        }

        return out;

    }

    //
    // findPom();
    //

    private @NotNull ConfigurationSection findPom(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        File file = new File(dir, info.pomFileName());
        if (file.exists()) {

            try {

                ConfigurationSection section = loadPomFromLocalFile(file);

                File jarFile = new File(dir, info.jarFileName());
                if (jarFile.exists()) return section;

                log.warn("Library `{}` pom file was found locally, but jar file does not exist. Downloading pom from repositories.", info.gradle());

            }

            catch (IOException | LibraryPomParseException e) {
                log.error("Invalid local `{}` file. Trying to download it from repositories...", info.pomFileName(), e);
            }

        }

        return findPomInRepositories(info);


    }

    private @NotNull ConfigurationSection findPomInRepositories(@NotNull LibrarySearchInfo info) throws UnknownLibraryException {

        List<String> repositories = info.repositories();
        log.info("Searching `{}` on {} repositories...", info.pomFileName(), repositories.size());

        for (String repository : repositories) {

            String url = info.urlPom(repository);

            try {

                ConfigurationSection section = loadPomFromRepository(url);
                section.set("url", repository);

                return section;

            }

            catch (FileNotFoundException e) {
                log.warn("Library `{}` not found on {}", info.pomFileName(), url);
            }

            catch (URISyntaxException e) {
                log.warn("Invalid library URL: `{}`", url);
                break;
            }

            catch (IOException e) {
                log.warn("Failed to retrieve pom from `{}`.", url, e);
            }

            catch (LibraryPomParseException e) {
                log.warn("Repository returned invalid pom file: `{}`.", url, e);
            }

        }

        throw new UnknownLibraryException("Library `" + info.gradle() + "` was not found in " + repositories.size() + " repositories.");

    }

    private @NotNull ConfigurationSection loadPomFromRepository(@NotNull String url) throws URISyntaxException, IOException, LibraryPomParseException {

        byte[] bytes;
        try (InputStream in = new URI(url).toURL().openStream()) {
            bytes = in.readAllBytes();
        }

        return loadPomFromBytes(bytes);

    }

    private @NotNull ConfigurationSection loadPomFromLocalFile(@NotNull File file) throws IOException, LibraryPomParseException {

        byte[] bytes;
        try (FileInputStream in = new FileInputStream(file)) {
            bytes = in.readAllBytes();
        }

        return loadPomFromBytes(bytes);

    }

    private @NotNull ConfigurationSection loadPomFromBytes(byte[] bytes) throws LibraryPomParseException {

        JsonConfiguration jsonConfiguration = new JsonConfiguration();

        JSONObject json;
        try {
            json = XML.toJSONObject(new String(bytes, StandardCharsets.UTF_8));
        }

        catch (JSONException e) {
            throw new LibraryPomParseException(e);
        }

        jsonConfiguration.loadFromJson(json);

        return jsonConfiguration;

    }





}
