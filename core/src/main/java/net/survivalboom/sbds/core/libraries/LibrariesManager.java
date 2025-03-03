package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.ILibrariesManager;
import net.survivalboom.sbds.api.libraries.LibraryDownloadException;
import net.survivalboom.sbds.api.libraries.LibraryParseException;
import net.survivalboom.sbds.api.libraries.RepositoryConnectionException;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.http.HttpFileDownloader;
import net.survivalboom.sbds.core.SBDS;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    // DOWNLOADING
    //

    @Override
    public void download(@NotNull IModule module, @NotNull ConfigurationSection section) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(section, "section == null");

        download0(module, section, false);

    }

    @Override
    public void download(@NotNull IModule module, @NotNull File file) throws IOException, InvalidConfigurationException {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(file, "file == null");

        download0(module, file, false);

    }

    public void download0(@Nullable IModule module, @NotNull File file, boolean ignoreState) throws IOException, InvalidConfigurationException {

        Objects.requireNonNull(file, "file == null");

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        ConfigurationSection section = yamlConfiguration.getConfigurationSection("libraries");
        if (section == null) throw new IllegalArgumentException("Section does not contain 'libraries' section");

        download0(module, section, ignoreState);

    }

    public synchronized void download0(@Nullable IModule module, @NotNull ConfigurationSection section, boolean ignoreState) {

        Objects.requireNonNull(section, "section == null");

        if (module != null && !ignoreState) {
            sbds.getModuleManager().checkModuleEnabled(module, "Disabled module tried to download libraries.");
        }

        for (String key : section.getKeys(false)) {

            ConfigurationSection libSection = section.getConfigurationSection(key);
            if (libSection == null) continue;

            try {

                downloadSection(libSection, key);

            }

            catch (LibraryParseException e) {
                log.warn("{} Skipping...", e.getMessage());
            }

            catch (RepositoryConnectionException e) {
                log.error("Unable to get information about the library `{}`.", key, e);
            }

            catch (LibraryDownloadException e) {
                log.error("Failed to download library `{}`.", key, e);
            }



        }

    }

    private void downloadSection(@NotNull ConfigurationSection libSection, @NotNull String key) throws LibraryParseException, RepositoryConnectionException, LibraryDownloadException {

        String group = libSection.getString("group");
        String artifact = libSection.getString("artifact");
        String version = libSection.getString("version");

        if (group == null) throw new LibraryParseException(String.format("Library section `%s` does not contain `group` key.", key));
        if (artifact == null) throw new LibraryParseException(String.format("Library section `%s` does not contain `artifact` key.", key));
        if (version == null) throw new LibraryParseException(String.format("Library section `%s` does not contain `version` key.", key));

        List<String> repositories = libSection.getStringList("repositories");
        if (repositories.isEmpty()) repositories.add(ILibrariesManager.MAVEN_CENTRAL_URL);

        String fileName = ILibrariesManager.generateJarFileName(group, artifact, version, "jar");
        File file = new File(dir, fileName);

        if (file.exists()) {
            jarLoader.mountJar(file);
            return;
        }

        log.info("Searching `{}` library information on {} repositories...", key, repositories.size());

        Library library = findLibrary(repositories, group, artifact, version);
        library.download();

    }

    public void downloadJar(@NotNull Library library) throws LibraryDownloadException {

        if (library.installed()) return;

        String url = library.getUrl() + ".jar";

        log.info("~ Downloading `{}` from {}", library.getName(), url);

        File file = library.getFile();
        HttpFileDownloader downloader = new HttpFileDownloader(url, file);

        try {
            downloader.download();
        }

        catch (IOException | URISyntaxException e) {
            throw new LibraryDownloadException(e);
        }

        jarLoader.mountJar(file);
        log.info("+ Mounted {}", file.getName());

    }


    //
    // RESOLVING
    //

    public @NotNull Library findLibrary(@NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {
        return findLibrary(ILibrariesManager.MAVEN_CENTRAL_URL, group, artifact, version);
    }

    public @NotNull Library findLibrary(@NotNull String repo, @NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {
        return findLibrary(List.of(repo), group, artifact, version);
    }

    public @NotNull Library findLibrary(@NotNull String[] repos, @NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {
        return findLibrary(List.of(repos), group, artifact,version);
    }

    public @NotNull Library findLibrary(@NotNull List<String> repos, @NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {

        String shortString = group + ":" + artifact + ":" + version;
        if (cachedLibraries.containsKey(shortString)) {
            return cachedLibraries.get(shortString);
        }

        JSONObject json = findPom(repos, group, artifact, version);
        JSONObject pomJson = json.getJSONObject("project");

        List<Library> dependencies = resolveDependencies(pomJson);

        String url = json.getString("url");
        Library library = new Library(this, new File(dir, ILibrariesManager.generateJarFileName(group, artifact, version, "jar")), url, group, artifact, version, dependencies, pomJson);

        cachedLibraries.put(shortString, library);

        return library;

    }

    private @NotNull List<Library> resolveDependencies(@NotNull JSONObject json) throws RepositoryConnectionException {

        if (!json.has("dependencies")) {
            return new ArrayList<>();
        }

        JSONObject dependenciesSection = json.getJSONObject("dependencies");
        if (!dependenciesSection.has("dependency")) {
            return new ArrayList<>();
        }

        Object dependenciesRaw = dependenciesSection.get("dependency");
        JSONArray dependencies = dependenciesRaw instanceof JSONArray jsonArray ? jsonArray : new JSONArray().put(dependenciesRaw);

        List<String> repositories = readRepositories(json);

        List<Library> out = new ArrayList<>();
        for (Object obj : dependencies) {

            JSONObject dependencyInfo = (JSONObject) obj;

            String scope = dependencyInfo.has("scope") ? dependencyInfo.getString("scope") : null;
            if (scope != null && !scope.equals("compile")) {
                continue;
            }

            String dGroup = dependencyInfo.getString("groupId");
            String dVersion = dependencyInfo.get("version").toString();
            String dArtifact = dependencyInfo.getString("artifactId");

            out.add(findLibrary(repositories, dGroup, dArtifact, dVersion));

        }

        return out;

    }

    private @NotNull List<String> readRepositories(@NotNull JSONObject obj) {

        if (!obj.has("repositories")) {
            return List.of(ILibrariesManager.MAVEN_CENTRAL_URL);
        }

        JSONObject repositoriesSection = obj.getJSONObject("repositories");
        if (!repositoriesSection.has("repository")) {
            return List.of(ILibrariesManager.MAVEN_CENTRAL_URL);
        }

        JSONArray repositories = obj.getJSONArray("repository");

        List<String> out = new ArrayList<>();
        for (Object repository : repositories) {

            JSONObject repositoryInfo = (JSONObject) repository;
            String url = repositoryInfo.getString("url");

            out.add(url);

        }

        return out;


    }

    public @NotNull JSONObject findPom(@NotNull List<String> repos, @NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {

        if (repos.isEmpty()) throw new IllegalArgumentException("No repositories provided. (repos.size() == 0)");

        if (repos.size() == 1) {
            return findPom(repos.getFirst(), group, artifact, version);
        }

        Throwable lastError = null;
        for (String r : repos) {

            try {
                return findPom(r, group, artifact, version);
            }

            catch (RepositoryConnectionException e) {
                lastError = e;
            }

        }

        throw new RepositoryConnectionException("The library pom file could not be retrieved. All attempts to connect to any of the " + repos.size() + " repositories were failed.", lastError);

    }

    public @NotNull JSONObject findPom(@NotNull String repository, @NotNull String group, @NotNull String artifact, @NotNull String version) throws RepositoryConnectionException {

        String url = ILibrariesManager.generateArtifactUrl(repository, group, artifact, version);
        String pom = url + ".pom";

        String xml;
        try {
            try (InputStream in = new URI(pom).toURL().openStream()) {
                xml = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        catch (IOException | URISyntaxException e) {
            throw new RepositoryConnectionException(e);
        }

        return XML.toJSONObject(xml).put("url", url);

    }


}
