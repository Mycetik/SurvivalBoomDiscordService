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

    private final Map<ArtifactAddress, Library> libraryMap = new HashMap<>();


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

    }

    // IMPORT LIBRARIES FROM SimpleLibrariesManager //

    public void importFromSimpleLibrariesDownloader(@NotNull SimpleLibrariesDownloader downloader) {

        checkValid();
        Objects.requireNonNull(downloader, "downloader == null");

        List<SimpleLibrary> toImport = downloader.getLibrariesInstalled();
        for (var lib : List.copyOf(toImport)) {

            boolean remove = toImport.stream().anyMatch(l -> l.dependencies().contains(lib));
            if (remove) {
                toImport.remove(lib);
            }

        }

        for (var lib : toImport) {
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

        ArtifactAddress address = new ArtifactAddress(group, artifact, version);
        String repository = simpleLibrary.repository();

        if (cachedPoms.containsKey(address)) {
            throw new IllegalStateException("Library with address `" + address + "` already exists");
        }

        File file = simpleLibrary.file();
        DynamicClassLoader classLoader = simpleLibrary.classLoader();

        IPomData pom;
        try {
            pom = retrievePom(repository, address);
        }

        catch (PomResolutionException e) {
            throw new RuntimeException(e);
        }

        classLoader.addClassSupplier(file.getName(), name -> rootClassLoader.getClass(name, false, true));

        Library library = new Library(pom, file, classLoader, dependencies);
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

            ILibrary library = getLibrary(pom);
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

        String repository = pom.getRepository();
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
    public @Nullable ILibrary getLibrary(@NotNull ArtifactAddress address) {
        checkValid();
        return libraryMap.get(address);
    }

    @Override
    public @NotNull Map<ArtifactAddress, ILibrary> getInstalledLibraries() {
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

        IPomData pom = getPom(address);
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
            data = createPomFromBytes(repository, address, string);
        }

        catch (PomResolutionException e) {

            if (e.getMessage().contains("Invalid POM file")) {
                log.error("Received an invalid POM file. Printing contents below: \n{}", string);
            }

            throw e;

        }

        cachedPoms.put(address, data);

        return data;

    }

    @Override
    public @Nullable IPomData getPom(@NotNull ArtifactAddress address) {
        checkValid();
        return cachedPoms.get(address);
    }

    @Override
    public @NotNull Map<ArtifactAddress, IPomData> getCachedPoms() {
        checkValid();
        return new HashMap<>(cachedPoms);
    }


    //
    // INTERNAL
    //

    private @NotNull PomData createPomFromBytes(@NotNull String repository, @NotNull ArtifactAddress address, @NotNull String string) throws PomResolutionException {

        ConfigurationNode pomData;
        try {
            pomData = XmlConfigurationLoader.builder().buildAndLoadString(string);
        }

        catch (ConfigurateException e) {
            throw new PomResolutionException("Invalid POM file. Could not parse XML", e);
        }

        ConfigurationNode projectSection = pomData; // Я не знаю чому, але configurate ігнорує project секцію та одразу надає її вміст X_X
//        ConfigurationNode projectSection = pomData.node("project");
//        if (projectSection.virtual()) {
//            throw new PomResolutionException("Invalid POM file. Section `project` not found");
//        }

        // Завантажуємо properties у поточному pom.xml //

        Map<String, String> localProperties = new HashMap<>();
        localProperties.put("project.version", address.version()); // Додаємо деякі системні properties що існують в maven.

        for (var section : projectSection.node("properties").childrenList()) {

            String key = (String) section.key();
            String value = section.getString();

            localProperties.put(key, value);

        }

        Placeholders propertiesAsPlaceholders = Placeholders.of(localProperties);

        // Дістаємо перелік репозиторіїв //

        List<String> repositories = new ArrayList<>();
        repositories.add(MAVEN_CENTRAL_URL);
        for (var section : projectSection.node("repositories").childrenList()) {

            String url = section.node("url").getString();
            if (url == null) {
                continue;
            }

            repositories.add(url);

        }

        // Визначаємо parent цього pom //

        ConfigurationNode parentSection = projectSection.node("parent");
        IPomData parent;
        if (!parentSection.virtual()) {

            ArtifactAddress artifactAddress = artifactAddressFromSection(parentSection, address, propertiesAsPlaceholders);

            try {
                parent = retrievePom(repositories, artifactAddress);
            }

            catch (Exception e) {
                throw new PomResolutionException("Failed to resolve parent POM `" + address + "`. Searched in " + repositories.size() + " repositories", e);
            }

        }

        else {
            parent = null;
        }

        // Якщо parent існує, значить десь там є properties...
        // Використовуємо рекурсивний піздець із parent'ами та доповнюємо наші properties.
        if (parent != null) {
            createFullParentRecursivePlaceholders(parent, propertiesAsPlaceholders);
        }

        propertiesAsPlaceholders.selfParse(); // Перепарсимо properties на самих себе.

        List<IPomData> bombs = new ArrayList<>(); // ХА-ХА! Не BOM, а BOMBs ГІ ГІ ГІ, ГУ ГУ ГУ!!!
        for (var section : projectSection.node("dependencyManagement").childrenList()) {

            ArtifactAddress bomAddress = artifactAddressFromSection(section, address, propertiesAsPlaceholders);

            IPomData bom;

            try {
                bom = retrievePom(repositories, bomAddress);
            }

            catch (Exception e) {
                throw new PomResolutionException("Failed to retrieve BOM `" + bomAddress + "`");
            }

            bombs.add(bom);

        }

        List<IPomData> dependencies = new ArrayList<>();
        for (var section : projectSection.node("dependencies").childrenList()) {

            String scope = section.node("scope").getString();
            if (scope != null && !scope.equals("compile") && !scope.equals("runtime")) {
                continue;
            }

            boolean optional = section.node("optional").getBoolean();
            if (optional) {
                continue;
            }

            ArtifactAddress dependencyAddress = artifactAddressFromSection(section, address, propertiesAsPlaceholders);

            IPomData dependency;
            try {
                dependency = retrievePom(repository, dependencyAddress);
            }

            catch (Exception e) {
                throw new PomResolutionException("Failed to resolve dependency `" + dependencyAddress + "` of `" + address + "`");
            }

            dependencies.add(dependency);

        }

        return new PomData(repository, address, pomData, repositories, parent, localProperties, bombs, dependencies);

    }

    private @NotNull ArtifactAddress artifactAddressFromSection(
            @NotNull ConfigurationNode section,
            @NotNull ArtifactAddress address,
            @NotNull Placeholders properties
    ) throws PomResolutionException {

        String group = section.node("groupId").getString();
        String artifact = section.node("artifactId").getString();
        String version = section.node("version").getString();

        if (group == null || artifact == null || version == null) {
            throw new PomResolutionException("Artifact `" + address + "` has an invalid dependency");
        }

        group = properties.parse(group, MAVEN_PROPERTIES_LAYOUT);
        artifact = properties.parse(artifact, MAVEN_PROPERTIES_LAYOUT);
        version = properties.parse(version, MAVEN_PROPERTIES_LAYOUT);

        return new ArtifactAddress(group, artifact, version);

    }

    private void createFullParentRecursivePlaceholders(@NotNull IPomData pom, @NotNull Placeholders placeholders) {

        placeholders.add("project.version", pom.getAddress().version());

        IPomData parent = pom.getParent();
        while (parent != null) {

            for (var entry : parent.getProperties().entrySet()) {

                String key = entry.getKey();
                String value = entry.getValue();

                if (!placeholders.contains(key)) {
                    continue;
                }

                placeholders.add(key, value);

            }

            parent = parent.getParent();

        }

    }


    //
    // MISC
    //

    @Override
    public @NotNull ClassLoader getClassLoader() {
        return rootClassLoader;
    }

}
