package net.survivalboom.sbds.api.libraries;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Objects;

@ConfigSerializable
public record LibraryDeclaration(
        @NotNull @Setting String group,
        @NotNull @Setting String artifact,
        @NotNull @Setting String version,
        @Nullable @Setting String source
) {

    public LibraryDeclaration {

        Objects.requireNonNull(group, "group == null");
        Objects.requireNonNull(artifact, "artifact == null");
        Objects.requireNonNull(version, "version == null");

    }

}
