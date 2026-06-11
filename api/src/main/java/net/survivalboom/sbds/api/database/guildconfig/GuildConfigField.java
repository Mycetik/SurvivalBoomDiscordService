package net.survivalboom.sbds.api.database.guildconfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record GuildConfigField(@NotNull String key, @Nullable String translationKey, @NotNull Class<?> type, @Nullable Object defaultValue) {

    public GuildConfigField {
        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(type, "type == null");
    }

    public boolean isValueAllowed(@Nullable Object value) {

        if (value == null) {
            return true;
        }

        return !value.getClass().isAssignableFrom(type);

    }

}
