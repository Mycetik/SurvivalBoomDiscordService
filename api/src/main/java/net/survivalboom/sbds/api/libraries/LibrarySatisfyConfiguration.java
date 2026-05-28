package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public class LibrarySatisfyConfiguration {

    private final Set<LibraryDeclaration> libraries = new HashSet<>();

    private final Set<LibraryDeclaration> pinnedLibraries = new HashSet<>();

    public LibrarySatisfyConfiguration(
            @NotNull Collection<LibraryDeclaration> libraries,
            @Nullable Collection<LibraryDeclaration> pinnedLibraries
    ) {

        Objects.requireNonNull(libraries, "libraries == null");
        this.libraries.addAll(libraries);

        if (pinnedLibraries != null) {
            this.pinnedLibraries.addAll(pinnedLibraries);
        }

    }


    public @NotNull List<LibraryDeclaration> getLibraries() {
        return new ArrayList<>(libraries);
    }

    public @NotNull List<LibraryDeclaration> getPinnedLibraries() {
        return new ArrayList<>(pinnedLibraries);
    }

    public boolean isEmpty() {
        return libraries.isEmpty();
    }

    //
    // STATIC
    //

    public static @NotNull MassLoadResult fromSection(@NotNull ConfigurationNode section) {

        if (section.isList()) {

            var result = LibraryDeclaration.fromMultiSection(section);

            LibrarySatisfyConfiguration configuration = new LibrarySatisfyConfiguration(
                    result.loaded(),
                    null
            );

            return new MassLoadResult(configuration, result.failed(), new HashMap<>());

        }

        ConfigurationNode pinnedSection = section.node("pinned");
        var pinnedResult = LibraryDeclaration.fromMultiSection(pinnedSection);

        ConfigurationNode declarationsSection = section.node("dependencies");
        var declarationsResult = LibraryDeclaration.fromMultiSection(declarationsSection);

        LibrarySatisfyConfiguration configuration = new LibrarySatisfyConfiguration(
                declarationsResult.loaded(),
                pinnedResult.loaded()
        );

        return new MassLoadResult(configuration, declarationsResult.failed(), pinnedResult.failed());

    }

    public record MassLoadResult(
            @NotNull LibrarySatisfyConfiguration result,
            @NotNull Map<String, Exception> declarationsFailed,
            @NotNull Map<String, Exception> pinnedFailed
    ) {}


}
