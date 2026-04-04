package net.survivalboom.sbds.api.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ITranslation {

    void update() throws IOException, InvalidConfigurationException, MessageLoadException;

    void save() throws IOException;


    @NotNull File getFile();

    @NotNull String getName();


    @Nullable String displayName();

    void displayName(@Nullable String displayName);


    @NotNull DiscordLocale discordLocale();

    void discordLocale(@NotNull DiscordLocale locale);


    @Nullable String icon();

    void icon(@Nullable String icon);


    @Nullable IMessage getMessage(@NotNull String name);

    @NotNull List<IMessage> getMessages();

}
