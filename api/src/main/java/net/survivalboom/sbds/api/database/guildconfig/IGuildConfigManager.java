package net.survivalboom.sbds.api.database.guildconfig;

import net.dv8tion.jda.api.entities.Guild;
import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.utils.valid.IManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface IGuildConfigManager extends IManager {

    //
    // GUILD CONFIG TEMPLATE
    //

    // REG //

    @NotNull IGuildConfigTemplate registerTemplate(@NotNull IModule module, @NotNull Collection<GuildConfigField> fields);

    @NotNull IGuildConfigTemplate registerTemplate(@NotNull IModule module, @NotNull Consumer<IGuildConfigBuilder> builder);

    void unregisterGuildConfig(@NotNull IGuildConfigTemplate template);

    // GET //

    @Nullable IGuildConfigTemplate getTemplate(@Nullable IModule module);

    @Nullable IGuildConfigTemplate getTemplate(@NotNull String key);

    @NotNull List<IGuildConfigTemplate> getTemplates();

    //
    // GUILD CONFIG
    //

    @NotNull IGuildConfig getGuildConfig(@NotNull IGuildConfigTemplate template, long guildId);

    default @Nullable IGuildConfig getGuildConfig(@NotNull IGuildConfigTemplate template, @NotNull Guild guild) {
        return getGuildConfig(template, guild.getIdLong());
    }

    @NotNull List<IGuildConfig> getGuildConfigs(long guildId);

    default @NotNull List<IGuildConfig> getGuildConfigs(@NotNull Guild guild) {
        return getGuildConfigs(guild.getIdLong());
    }


    interface IGuildConfigBuilder {

        <T> IGuildConfigManager.@NotNull IGuildConfigBuilder addField(@NotNull String key, @NotNull String translationKey, @NotNull Class<T> type, @Nullable T defaultValue);

    }

}
