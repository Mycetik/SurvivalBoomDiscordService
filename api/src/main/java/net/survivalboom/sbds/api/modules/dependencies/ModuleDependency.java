package net.survivalboom.sbds.api.modules.dependencies;

import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static @NotNull List<ModuleDependency> fromMultiSection(@NotNull ConfigurationNode section) {

        if (section.isMap()) {
            return List.of(fromSection(section));
        }

        return section.childrenList().stream().map(ModuleDependency::fromSection).toList();

    }

    public static @NotNull ModuleDependency fromSection(@NotNull ConfigurationNode section) {

        String id = section.node("id").getString();
        if (id == null) {
            throw new IllegalArgumentException("Key `id` not found");
        }

        boolean required = section.node("required").getBoolean(true);
        boolean joinClasspath = section.node("join-classpath").getBoolean(true);

        String orderRaw = section.node("order").getString();

        LoadOrder order;
        if (orderRaw == null) {
            order = LoadOrder.AFTER;
        }

        else {
            order = CommonUtils.getEnumValue(LoadOrder.class, orderRaw);
            if (order == null) {
                throw new IllegalArgumentException("Invalid load order `" + orderRaw + "`");
            }
        }

        return new ModuleDependency(id, required, joinClasspath, order);

    }

}
