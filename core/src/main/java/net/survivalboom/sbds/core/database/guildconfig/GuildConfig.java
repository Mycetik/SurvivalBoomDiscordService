package net.survivalboom.sbds.core.database.guildconfig;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.database.guildconfig.GuildConfigField;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfig;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigManager;
import net.survivalboom.sbds.api.database.guildconfig.IGuildConfigTemplate;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.NamespacedKey;
import net.survivalboom.sbds.api.utils.valid.Valid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GuildConfig extends Valid implements IGuildConfig {

    private final GuildConfigManager manager;

    private final GuildConfigTemplate template;

    private final long guildId;


    private final NamespacedKey key;


    public GuildConfig(long guildId, @NotNull GuildConfigTemplate template, @NotNull GuildConfigManager manager) {

        this.guildId = guildId;
        this.template = template;
        this.manager = manager;

        IModule module = template.getRegistration().module();
        this.key = module != null ? NamespacedKey.fromModule(module, "config") : NamespacedKey.sbds("config");

    }

    @Override
    public @NotNull IGuildConfigManager getManager() {
        return manager;
    }

    @Override
    public long getGuildId() {
        return guildId;
    }

    @Override
    public @NotNull Guild getGuild() {
        return Objects.requireNonNull(manager.sbds.getBot().getGuildById(guildId), "guild == null");
    }

    @Override
    public @NotNull IGuildConfigTemplate getTemplate() {
        return template;
    }

    @Override
    public @NotNull String getKey() {
        return template.getKey();
    }

    // FIELDS //

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <T> CompletableFuture<Optional<T>> get(@NotNull String key, @NotNull Class<T> type, boolean defaultValue) {

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(type, "type == null");
        checkValid();

        GuildConfigField field = template.getField(key);
        if (field == null) {
            throw new IllegalArgumentException("No config value by path `" + key + "` exists");
        }

        if (!type.isAssignableFrom(field.type())) {
            throw new IllegalArgumentException("Cannot cast `" + type + "` to field `" + field.type() + "`");
        }

        String[] path0 = CommonUtils.splitString(key, "\\.");

        return manager.guildDataManager.get(guildId).thenApply(guildData -> {

            if (guildData == null) {
                return Optional.empty();
            }

            ConfigurationNode rootNode = guildData.container().getNode(this.key);
            if (rootNode == null) {
                return Optional.empty();
            }

            ConfigurationNode node = rootNode.node((Object[]) path0);
            if (node.virtual()) {

                if (defaultValue) {
                    return Optional.ofNullable((T) field.defaultValue());
                }

                return Optional.empty();

            }

            T value;
            try {
                value = node.get(type);
            }
            catch (SerializationException e) {
                throw new RuntimeException("Failed to load `" + key + "` from database. Invalid object.", e);
            }

            if (value == null && defaultValue) {
                return Optional.ofNullable((T) field.defaultValue());
            }

            return Optional.ofNullable(value);

        });

    }

    @Override
    public @NotNull CompletableFuture<Map<GuildConfigField, Object>> getValues(boolean defaultValue) {

        checkValid();

        Map<GuildConfigField, CompletableFuture<Optional<Object>>> futures = new HashMap<>();
        for (var field : template.getFields().values()) {
            var future = get(field.key(), Object.class, defaultValue);
            futures.put(field, future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[0])
        );

        return allFutures.thenApply(v -> {

            Map<GuildConfigField, Object> map = new HashMap<>();
            for (var entry : futures.entrySet()) {
                map.put(entry.getKey(), entry.getValue().join().orElse(null));
            }

            return map;

        });

    }

    @Override
    public void set(@NotNull String key, @Nullable Object obj) {

        Objects.requireNonNull(key, "key == null");
        checkValid();

        GuildConfigField field = template.getField(key);
        if (field == null) {
            throw new IllegalArgumentException("No config value by path `" + key + "` exists");
        }

        if (obj != null && !obj.getClass().isAssignableFrom(field.type())) {
            throw new IllegalArgumentException("Cannot cast `" + obj.getClass() + "` to field `" + field.type() + "`");
        }

        String[] path0 = CommonUtils.splitString(key, "\\.");

        manager.guildDataManager.obtain(guildId).thenAccept(guildData -> {

            ConfigurationNode rootNode = guildData.container().obtainNode(this.key);
            ConfigurationNode node = rootNode.node((Object[]) path0);

            try {
                node.set(obj);
            }
            catch (SerializationException e) {
                throw new RuntimeException("Failed to save `" + key + "` to database. Invalid object.", e);
            }

            guildData.save();

        });

    }

}
