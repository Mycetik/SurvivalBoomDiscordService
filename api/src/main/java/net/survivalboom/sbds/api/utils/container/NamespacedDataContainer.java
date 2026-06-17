package net.survivalboom.sbds.api.utils.container;

import net.survivalboom.sbds.api.database.IDatabase;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.jackson.JacksonConfigurationLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NamespacedDataContainer extends Valid implements INamespacedDataContainer {

    private final JacksonConfigurationLoader loader = IDatabase.createConfigurateLoader().build();

    private final Map<NamespacedKey, ConfigurationNode> map = new HashMap<>();

    // CREATE //

    @Override
    public @NotNull ConfigurationNode createNode(@NotNull NamespacedKey key) {

        checkValid();

        if (map.containsKey(key)) {
            throw new IllegalStateException("Data with key `" + key + "` already exists");
        }

        ConfigurationNode node = loader.createNode();

        map.put(key, node);

        return node;

    }

    // REMOVE //

    @Override
    public @Nullable ConfigurationNode removeNode(@NotNull NamespacedKey key) {

        checkValid();

        ConfigurationNode node = getNode(key);
        if (node == null) {
            return null;
        }

        map.remove(key);

        return node;

    }

    // GET //

    @Override
    public @Nullable ConfigurationNode getNode(@NotNull NamespacedKey key) {
        checkValid();
        return map.get(key);
    }

    // OBTAIN //

    @Override
    public @NotNull ConfigurationNode obtainNode(@NotNull NamespacedKey key) {
        checkValid();
        return Objects.requireNonNullElseGet(getNode(key), () -> createNode(key));
    }

    // MISC //

    @Override
    public @NotNull Map<NamespacedKey, ConfigurationNode> getAsMap() {
        return new HashMap<>(map);
    }

}
