package net.survivalboom.sbds.core.libraries;

import net.survivalboom.sbds.api.libraries.*;
import net.survivalboom.sbds.api.utils.placeholders.Placeholders;
import net.survivalboom.sbds.api.utils.valid.Manager;
import net.survivalboom.sbds.core.libraries.simple.SimpleLibrariesDownloader;
import net.survivalboom.sbds.core.libraries.simple.SimpleLibrary;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.xml.XmlConfigurationLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.*;

public class LibrariesManager extends Manager implements ILibrariesManager {


    private static final Logger log = LoggerFactory.getLogger(LibrariesManager.class.getSimpleName());

    private final DynamicClassLoader rootClassLoader;

    private final File librariesDir;

    private final HttpClient httpClient;


    private final Map<ArtifactAddress, PomData> cachedPoms = new HashMap<>();

    private final Map<ArtifactAddress, ILibrary> libraryMap = new HashMap<>();


    public LibrariesManager(
            @NotNull File librariesDir,
            @NotNull DynamicClassLoader rootClassLoader
    ) {

        this.librariesDir = librariesDir;
        this.rootClassLoader = rootClassLoader;

        this.httpClient = HttpClient.newBuilder().build();

    }

    //
    // MANAGER
    //

    @Override
    protected void init0() {

    }

    @Override
    protected void shutdown0() {
        libraryMap.clear();
        cachedPoms.clear();
    }

    //
    // IMPORT LIBRARIES FROM SimpleLibrariesManager //
    //

    public void importFromSimpleLibrariesDownloader(@NotNull SimpleLibrariesDownloader downloader) {

        checkValid();
        Objects.requireNonNull(downloader, "downloader == null");

        for (var lib : downloader.getLibrariesInstalled()) {
            importSimpleLibrary(lib);
        }

    }

    private @NotNull ILibrary importSimpleLibrary(@NotNull SimpleLibrary simpleLibrary) {

        List<ILibrary> dependencies = simpleLibrary.dependencies().stream()
                .map(this::importSimpleLibrary)
                .toList();

        String group = simpleLibrary.group();
        String artifact = simpleLibrary.artifact();
        String version = simpleLibrary.version();

        ArtifactAddress address = ArtifactAddress.create(group, artifact, version);

        ILibrary library = getLoadedLibrary(address);
        if (library != null) {
            return library;
        }

        File file = simpleLibrary.file();
        DynamicClassLoader classLoader = simpleLibrary.classLoader();

        String repository = simpleLibrary.repository();

        IPomData pom;
        try {
            pom = retrievePom(repository, address);
        }

        catch (PomResolutionException e) {
            throw new RuntimeException(e);
        }

        classLoader.addClassSupplier(file.getName(), name -> rootClassLoader.getClass(name, false, true));

        library = new Library(pom, file, classLoader, dependencies);
        libraryMap.put(address, library);

        return library;

    }

    //
    // LIBRARIES
    //

    // DOWNLOAD //

    @Override
    public @NotNull MassDownloadResult downloadLibraries(@NotNull ConfigurationNode node) {

        Objects.requireNonNull(node, "node == null");
        checkValid();

        MassDownloadResult result = new MassDownloadResult(new ArrayList<>(), new ArrayList<>(), new HashMap<>());

        List<LibraryDeclaration> declarations = new ArrayList<>();
        for (var section : node.childrenList()) {

            LibraryDeclaration declaration;
            try {
                declaration = section.get(LibraryDeclaration.class);
            }

            catch (SerializationException e) {
                String key = (String) section.key();
                result.failed().put(key, e);
                continue;
            }

            declarations.add(declaration);

        }

        List<IPomData> poms = new ArrayList<>();
        for (var declaration : declarations) {

            ArtifactAddress address = ArtifactAddress.fromDeclaration(declaration);
            String repository = declaration.source();
            if (repository == null) {
                repository = MAVEN_CENTRAL_URL;
            }

            IPomData pom;
            try {
                pom = retrievePom(repository, address);
            }

            catch (PomResolutionException e) {
                result.failed().put(address, e);
                continue;
            }

            poms.add(pom);

        }

        for (var pom : poms) {

            ILibrary library = getLoadedLibrary(pom);
            if (library != null) {
                result.skipped().add(library);
                continue;
            }

            try {
                library = downloadLibrary(pom);
            }

            catch (LibraryDownloadException e) {
                result.failed().put(pom.getAddress(), e);
                continue;
            }

            result.downloaded().add(library);

        }

        return result;

    }

    @Override
    public @NotNull ILibrary downloadLibrary(@NotNull IPomData pom) throws LibraryDownloadException {

        Objects.requireNonNull(pom, "pom == null");
        checkValid();

        String repository = pom.getSourceRepository();
        ArtifactAddress address = pom.getAddress();

        if (libraryMap.containsKey(address)) {
            throw new IllegalStateException("Library `" + address + "` already exists");
        }

        String url = address.createRepositoryAddress(repository, "jar");
        File file = new File(librariesDir, address.toGradleString(ArtifactAddress.DEFAULT_FILESYSTEM_SEPARATOR) + ".jar");

        log.info("Downloading library from {}", url);

        List<ILibrary> dependencies = new ArrayList<>();
        for (IPomData dependencyPom : pom.getDependencies()) {

            ILibrary dependency;

            try {
                dependency = downloadLibrary(dependencyPom);
            }

            catch (LibraryDownloadException e) {
                throw new LibraryDownloadException("Failed to download dependency `" + pom.getAddress() + "`");
            }

            dependencies.add(dependency);

        }

        try (InputStream in = new URI(url).toURL().openStream()) {
            Files.copy(in, file.toPath());
        }

        catch (Exception e) {
            throw new LibraryDownloadException("Failed to download library `" + address + "`", e);
        }

        DynamicClassLoader classLoader = new DynamicClassLoader(pom.getAddress().toGradleString(), null);
        classLoader.addClassSupplier(file.getName(), name -> rootClassLoader.getClass(name, false, true));


        Library library = new Library(pom, file, classLoader, dependencies);

        libraryMap.put(address, library);

        return library;

    }

    // GETTERS //

    @Override
    public @Nullable ILibrary getLoadedLibrary(@NotNull ArtifactAddress address) {
        checkValid();
        return libraryMap.get(address);
    }

    @Override
    public @NotNull Map<ArtifactAddress, ILibrary> getLoadedLibraries() {
        checkValid();
        return new HashMap<>(libraryMap);
    }

    //
    // POM
    //

    @Override
    @Blocking
    public @NotNull IPomData retrievePom(@NotNull String repository, @NotNull ArtifactAddress address) throws PomResolutionException {

        Objects.requireNonNull(repository, "repository == null");
        Objects.requireNonNull(address, "address == null");
        checkValid();

        IPomData pom = getLoadedPom(address);
        if (pom != null) {
            return pom;
        }

        String url = address.createRepositoryAddress(repository, "pom");

        log.info("Retrieving POM from {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        catch (Exception e) {
            throw new PomResolutionException("POM resolution failed. Failed to retrieve POM from repository. " + url);
        }

        int status = response.statusCode();
        if (status != 200) {
            throw new PomResolutionException("POM resolution failed. Server responded with code " + status + " " + url);
        }

        String string = response.body();

        PomData data;
        try {
            data = createPomFromString(repository, address, string);
        }

        catch (PomResolutionException e) {

            if (e.getMessage().contains("Invalid POM file")) {
                log.error("Received an invalid POM file. Printing contents below: \n{}", string);
            }

            throw new PomResolutionException("Failed to resolve `" + address + "`", e);

        }

        catch (Exception e) {
            throw new PomResolutionException("An internal error occurred! Maybe a bug?", e);
        }

        cachedPoms.put(address, data);

        return data;

    }

    @Override
    public @Nullable IPomData getLoadedPom(@NotNull ArtifactAddress address) {

        checkValid();

        if (!address.isComplete()) {
            throw new IllegalArgumentException("ArtifactAddress `" + address + "` is not complete");
        }

        return cachedPoms.get(address);

    }

    @Override
    public @NotNull Map<ArtifactAddress, IPomData> getLoadedPoms() {
        checkValid();
        return new HashMap<>(cachedPoms);
    }


    //
    // INTERNAL
    //

    private @NotNull PomData createPomFromString(@NotNull String repository, @NotNull ArtifactAddress address, @NotNull String string) throws PomResolutionException {

        ConfigurationNode pomData;
        try {
            pomData = XmlConfigurationLoader.builder().buildAndLoadString(string); // Я не знаю чому, але configurate ігнорує project секцію та одразу надає її вміст X_X
        } catch (ConfigurateException e) {
            throw new PomResolutionException("Invalid POM file. Could not parse XML", e);
        }

        //
        // Завантажуємо properties з поточного pom.xml //
        //

        Placeholders properties = new Placeholders();

        // Додаємо деякі системні змінні.
        String version = address.version().orElseThrow();
        properties.add("project.version", version);
        properties.add("version", version);

        for (var entry : pomData.node("properties").childrenMap().entrySet()) {

            String key = (String) entry.getKey();
            String value = entry.getValue().getString();

            properties.add(key, value);

        }

        Map<String, String> propertiesAsMap = new HashMap<>();
        properties.getAsMap().forEach((key, value) -> propertiesAsMap.put(key, (String) value));

        //
        // Дістаємо перелік репозиторіїв //
        //

        List<String> repositories = new ArrayList<>();
        repositories.add(MAVEN_CENTRAL_URL);

        for (var section : pomData.node("repositories").childrenList()) {

            String url = section.node("url").getString();
            if (url == null) {
                continue;
            }

            repositories.add(url);

        }

        //
        // Визначаємо parent цього POM //
        //

        ConfigurationNode parentSection = pomData.node("parent");
        IPomData parent = null;
        if (!parentSection.virtual()) {

            ArtifactAddress parentAddress;
            try {

                parentAddress = ArtifactAddress.fromPom(parentSection, properties);

                if (!parentAddress.isComplete()) {
                    throw new IllegalArgumentException("Invalid POM section, version not found");
                }

            }

            catch (IllegalArgumentException e) {
                throw new PomResolutionException("Invalid POM parent: `" + e.getMessage() + "`");
            }

            try {
                parent = retrievePom(repositories, parentAddress);
            }

            catch (PomResolutionException e) {
                throw new PomResolutionException("Failed to resolve parent `" + parentAddress + "`", e);
            }

        }

        // Додаємо деякі системні змінні.
        if (parent != null) {
            String parentVersion = parent.getAddress().version().orElseThrow();
            properties.add("parent.version", parentVersion);
            properties.add("project.parent.version", parentVersion);
        }

        //
        // Створюємо глобально properties рекурсивно з усіх parent //
        //

        Placeholders globalProperties = parent != null ? createGlobalPlaceholdersRecursive(parent) : new Placeholders();
        globalProperties.addAll(properties);

        //
        // Завантажуємо BOM //
        //

        List<IPomData> bombSources = new ArrayList<>();
        List<ArtifactAddress> bombArtifacts = new ArrayList<>();

        for (var section : pomData.node("dependencyManagement", "dependencies").childrenList()) {

            ArtifactAddress bomAddress;
            try {
                bomAddress = ArtifactAddress.fromPom(section, globalProperties);
            }

            catch (IllegalArgumentException e) {
                throw new PomResolutionException("An invalid BOM at index " + section.key() + " -> " + e.getMessage());
            }

            boolean isSource = Objects.equals(section.node("scope").getString(), "import");

            if (isSource) {
                IPomData bom;

                try {
                    bom = retrievePom(repositories, bomAddress);
                }

                catch (PomResolutionException e) {
                    throw new PomResolutionException("Failed to resolve BOM artifact`" + bomAddress + "`", e);
                }

                bombSources.add(bom);

            }

            else {

                if (!bomAddress.isComplete()) {
                    throw new PomResolutionException("Invalid BOM `" + bomAddress + "`. No version is present and no import scope is defined");
                }

                bombArtifacts.add(bomAddress);

            }

        }

        //
        // Нарешті, завантажуємо залежності //
        //

        List<IPomData> dependencies = new ArrayList<>();
        for (var section : pomData.node("dependencies").childrenList()) {

            String scope = section.node("scope").getString();
            if (scope != null && !scope.equals("compile") && !scope.equals("runtime")) {
                continue;
            }

            ArtifactAddress dependencyAddress;
            try {
                dependencyAddress = ArtifactAddress.fromPom(section, globalProperties);
            }

            catch (IllegalArgumentException e) {
                throw new PomResolutionException("An invalid dependency at index " + section.key() + " -> " + e.getMessage());
            }

            ArtifactAddress completeDependencyAddress = findCompleteArtifact(dependencyAddress, parent, bombSources, bombArtifacts, properties);
            if (completeDependencyAddress == null) {
                throw new PomResolutionException("Dependency `" + dependencyAddress + "` has no version defined. Tried to find version in BOM, no results found");
            }

            IPomData dependency;
            try {
                dependency = retrievePom(repositories, completeDependencyAddress);
            }

            catch (PomResolutionException e) {
                throw new PomResolutionException("Failed to resolve dependency `" + completeDependencyAddress + "`", e);
            }

            dependencies.add(dependency);

        }

        return new PomData(repository, address, pomData, repositories, propertiesAsMap, parent, bombSources, bombArtifacts, dependencies);

    }

    private @Nullable ArtifactAddress findCompleteArtifact(
            @NotNull ArtifactAddress address,
            @Nullable IPomData parent,
            @Nullable List<IPomData> bombSources,
            @Nullable List<ArtifactAddress> bombArtifacts,
            @NotNull Placeholders properties
    ) {

        if (address.isComplete()) {
            return address;
        }

        ArtifactAddress result = null;

        // Спочатку шукаємо артефакт у деклараціях артефактів BOM.
        if (bombArtifacts != null) {
            result = bombArtifacts.stream()
                    .filter(artifact -> artifact.group().equals(address.group()) && artifact.artifact().equals(address.artifact()))
                    .map(artifact -> artifact.applyProperties(properties))
                    .findAny()
                    .orElse(null);
        }

        // Якщо не знайшли, рекурсивно пробігаємось по BOMb sources.
        if (result == null && bombSources != null) {

            for (IPomData source : bombSources) {

                result = findCompleteArtifact(address, null, source.getBOMbSources(), source.getBOMbArtifacts(), properties);
                if (result != null) {
                    break;
                }

            }

        }

        // Якщо все ще не знайшли, дивимось у parent
        if (result == null && parent != null) {
            result = findCompleteArtifact(address, parent.getParent(), parent.getBOMbSources(), parent.getBOMbArtifacts(), properties);
        }

        return result;

    }

    private @NotNull Placeholders createGlobalPlaceholdersRecursive(@NotNull IPomData pom) {

        Placeholders result = new Placeholders();
        IPomData current = pom;

        while (current != null) {

            Placeholders placeholders = createPomPlaceholders(current);

            for (var entry : placeholders.getAsMap().entrySet()) {

                String key = entry.getKey();
                String value = (String) entry.getValue();

                if (!result.contains(key)) {
                    continue;
                }

                result.add(key, value);

            }

            current = current.getParent();

        }

        return result;

    }

    private @NotNull Placeholders createPomPlaceholders(@NotNull IPomData pom) {

        Placeholders placeholders = new Placeholders();

        String version = pom.getAddress().version().orElseThrow();
        placeholders.add("project.version", version);
        placeholders.add("version", version);

        IPomData parent = pom.getParent();
        if (parent != null) {
            String parentVersion = parent.getAddress().version().orElseThrow();
            placeholders.add("parent.version", parentVersion);
            placeholders.add("project.parent.version", parentVersion);
        }

        placeholders.addAll(pom.getProperties());

        placeholders.selfParse();

        placeholders.removeAll(
                "project.version",
                "version",
                "parent.version",
                "project.parent.version"
        );

        return placeholders;

    }


    //
    // MISC
    //

    @Override
    public @NotNull ClassLoader getClassLoader() {
        return rootClassLoader;
    }

}
