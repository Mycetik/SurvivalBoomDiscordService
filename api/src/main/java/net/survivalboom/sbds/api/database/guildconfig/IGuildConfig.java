package net.survivalboom.sbds.api.database.guildconfig;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.valid.IValid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IGuildConfig extends IValid {

    @NotNull IGuildConfigManager getManager();

    long getGuildId();

    @NotNull Guild getGuild();

    @NotNull IGuildConfigTemplate getTemplate();

    @NotNull Registration<IGuildConfigTemplate> getRegistration();

    @NotNull String getKey();

    // FIELDS //

    default @Nullable GuildConfigField getField(@NotNull String key) {
        return getTemplate().getField(key);
    }

    default @NotNull Map<String, GuildConfigField> getFields() {
        return getTemplate().getFields();
    }

    // CONFIG //

    <T> @NotNull CompletableFuture<Optional<T>> get(@NotNull String key, @NotNull Class<T> type, boolean defaultValue);

    default  <T> @NotNull CompletableFuture<Optional<T>> get(@NotNull String key, @NotNull Class<T> type) {
        return get(key, type, true);
    }


    @NotNull CompletableFuture<Map<GuildConfigField, Object>> getValues(boolean defaultValue);

    default @NotNull CompletableFuture<Map<GuildConfigField, Object>> getValues() {
        return getValues(true);
    }

    @NotNull CompletableFuture<Void> set(@NotNull String key, @Nullable Object obj);

}
