package net.survivalboom.sbds.api.libraries;

import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface ILibrariesManager extends IManager {

    String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

    String MAVEN_PROPERTIES_LAYOUT = "${$p$}";

    //
    // CLASS
    //

    @NotNull ClassLoader getClassLoader();

    //
    // LIBRARIES
    //

    @NotNull MassDownloadResult downloadLibraries(@NotNull ConfigurationNode node);

    @NotNull ILibrary downloadLibrary(@NotNull IPomData pom) throws LibraryDownloadException;


    @Nullable ILibrary getLibrary(@NotNull ArtifactAddress address);

    default @Nullable ILibrary getLibrary(@NotNull IPomData pom) {
        return getLibrary(pom.getAddress());
    }

    default @Nullable ILibrary getLibrary(@NotNull String gradleString) {
        return getLibrary(ArtifactAddress.fromGradleString(gradleString));
    }

    @NotNull Map<ArtifactAddress, ILibrary> getInstalledLibraries();

    //
    // POM
    //

    @NotNull IPomData retrievePom(@NotNull String repository, @NotNull ArtifactAddress address) throws PomResolutionException;

    default @NotNull IPomData retrievePom(@NotNull Collection<String> repositories, @NotNull ArtifactAddress address) throws PomResolutionException {

        if (repositories.isEmpty()) {
            throw new IllegalArgumentException("repositories are empty");
        }

        PomResolutionException lastException = null;
        for (String repo : repositories) {

            IPomData pom;

            try {
                pom = retrievePom(repo, address);
            }

            catch (PomResolutionException e) {
                lastException = e;
                continue;
            }

            return pom;

        }

        throw lastException;

    }

    default @NotNull IPomData retrievePom(@NotNull LibraryDeclaration declaration) throws PomResolutionException {

        Objects.requireNonNull(declaration, "declaration == null");

        ArtifactAddress address = ArtifactAddress.fromDeclaration(declaration);
        String repository = declaration.source();

        if (repository == null) {
            repository = MAVEN_CENTRAL_URL;
        }

        return retrievePom(repository, address);

    }

    default @NotNull IPomData retrievePom(@NotNull String repository, @NotNull String gradleString) throws PomResolutionException {
        return retrievePom(repository, ArtifactAddress.fromGradleString(gradleString));
    }


    @Nullable IPomData getPom(@NotNull ArtifactAddress address);

    default @Nullable IPomData getPom(@NotNull String gradleString) {
        return getPom(ArtifactAddress.fromGradleString(gradleString));
    }

    @NotNull Map<ArtifactAddress, IPomData> getCachedPoms();


    record MassDownloadResult(
            @NotNull List<ILibrary> downloaded,
            @NotNull List<ILibrary> skipped,
            @NotNull Map<Object, Exception> failed
    ) {}

}
