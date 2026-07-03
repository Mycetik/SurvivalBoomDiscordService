package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.*;

public record LibraryDeclaration(ArtifactAddress address, @Nullable String source) {

    public LibraryDeclaration {
        Objects.requireNonNull(address, "address == null");
    }


    public static @NotNull LibraryDeclaration fromSection(@NotNull ConfigurationNode section) {

        String artifactStr = section.node("artifact").getString();
        String source = section.node("source").getString();

        if (artifactStr == null) {
            throw new IllegalArgumentException("Invalid LibraryDeclaration section `" + section.key() + "`. Key `artifact` not found");
        }

        ArtifactAddress address = ArtifactAddress.fromGradleString(artifactStr);

        return new LibraryDeclaration(address, source);

    }

    public static @NotNull MassLoadResult fromMultiSection(@NotNull ConfigurationNode section) {

        MassLoadResult result = new MassLoadResult(new ArrayList<>(), new HashMap<>());

        var list = section.childrenList();
        if (list.isEmpty()) {
            return result;
        }

        if (!list.getFirst().isMap()) {

            for (var node : list) {

                try {

                    String gradleString = node.getString();
                    Objects.requireNonNull(gradleString, "gradleString == null");

                    ArtifactAddress address = ArtifactAddress.fromGradleString(gradleString);
                    LibraryDeclaration declaration = new LibraryDeclaration(address, null);

                    result.loaded.add(declaration);

                }

                catch (Exception e) {
                    result.failed.put(String.valueOf(node.key()), e);
                }

            }

            return result;

        }

        for (ConfigurationNode node : section.childrenList()) {

            try {
                result.loaded.add(fromSection(node));
            }

            catch (IllegalArgumentException e) {
                result.failed.put(String.valueOf(node.key()), e);
            }

        }

        return result;

    }

    @Override
    public String toString() {
        return address.toString();
    }

    public record MassLoadResult(@NotNull List<LibraryDeclaration> loaded, @NotNull Map<String, Exception> failed) {}


}
