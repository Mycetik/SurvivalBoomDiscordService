package net.survivalboom.sbds.api.translations;

import net.survivalboom.sbds.api.messages.IMessage;
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


    @Nullable String icon();

    void icon(@Nullable String icon);


    @Nullable IMessage getMessage(@NotNull String name);

    @NotNull List<IMessage> getMessages();

}
