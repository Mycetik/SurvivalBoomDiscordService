package net.survivalboom.sbds.api.modules.dependencies;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@ConfigSerializable
public record ModuleDependency(
        @NotNull String id,
        boolean required,
        boolean joinClasspath,
        @NotNull LoadOrder order
) {

    public ModuleDependency {

        Objects.requireNonNull(id, "id == null");
        Objects.requireNonNull(order, "order == null");

    }

}
