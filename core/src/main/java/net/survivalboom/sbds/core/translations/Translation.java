package net.survivalboom.sbds.core.translations;

import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.survivalboom.sbds.api.translations.*;
import net.survivalboom.sbds.api.messages.components.InvalidComponentException;
import net.survivalboom.sbds.api.registrations.Registration;
import net.survivalboom.sbds.api.utils.CommonUtils;
import net.survivalboom.sbds.api.utils.valid.Valid;
import net.survivalboom.sbds.api.messages.template.InvalidEmbedException;
import net.survivalboom.sbds.core.modules.Module;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.configuration.InvalidConfigurationException;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Translation extends Valid implements ITranslation {


    private final ITranslationManager manager;


    private Registration<ITranslation> registration;


    private DiscordLocale discordLocale;

    private String displayName;

    private String icon;


    private final Map<String, IMessage> messages = new HashMap<>();


    public Translation() {}


    private void load(@NotNull YamlConfiguration yamlConfiguration) throws MessageLoadException {

        checkValid();

        this.displayName = yamlConfiguration.getString("$display-name");
        this.icon = yamlConfiguration.getString("$icon");
        this.discordLocale = CommonUtils.getEnumValue(DiscordLocale.class, yamlConfiguration.getString("$discord-locale"));

        Set<String> keys = yamlConfiguration.getKeys(false);
        keys.remove("$name");
        keys.remove("$display-name");
        keys.remove("$icon");

        this.messages.clear();

        Map<String, Message> map = new HashMap<>();
        for (String s : keys) {
            load(yamlConfiguration, map, s);
        }

        this.messages.putAll(map);

    }

    @Override
    public synchronized void update() throws IOException, InvalidConfigurationException, MessageLoadException {

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        load(yamlConfiguration);

    }

    @Override
    public synchronized void save() throws IOException {

        checkValid();

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.mkdirs();
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }

        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        yamlConfiguration.set("$description", displayName);

        messages.values().forEach(message -> message.dump(yamlConfiguration));

        yamlConfiguration.save(file);

    }



    @Override
    public @Nullable Message getMessage(@NotNull String name) {
        checkValid();

        Message message = messages.get(name);
        if (message != null) return message;

        return getModuleMessage(name);

    }

    private @Nullable Message getModuleMessage(@NotNull String name) {
        return moduleMessages.values().stream().map(map -> map.get(name)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    @Override
    public @NotNull List<IMessage> getMessages() {
        return new ArrayList<>(messages.values());
    }



    public void addMessage(@NotNull String key, @NotNull Message message) {

        checkValid();

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(message, "message == null");

        messages.put(key, message);

    }

    public @NotNull List<Message> getMessages0() {
        return new ArrayList<>(messages.values());
    }



    private void load(@NotNull YamlConfiguration configuration, @NotNull Map<String, Message> map, @NotNull String path) throws MessageLoadException {

        ConfigurationSection section = configuration.getConfigurationSection(path);

        if (section != null) {

            if (section.contains("$embed") || section.contains("$embeds") || section.contains("$content") || section.contains("$components")) {

                try {
                    map.put(path, new Message(path, this, MessageTemplate.fromSection(section)));
                }

                catch (InvalidEmbedException | InvalidComponentException e) {
                    throw new MessageLoadException(path, e);
                }

                return;
            }

            for (String s : section.getKeys(false)) {
                load(configuration, map, path + "." + s);
            }

            return;

        }

        map.put(path, new Message(path, this, MessageTemplate.fromContent(configuration.getString(path, "Value of `" + path + "` is null"))));

    }



    @Override
    public @NotNull File getFile() {
        return file;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }


    public void addModuleTranslation(@NotNull Module module, @NotNull YamlConfiguration yamlConfiguration) throws MessageLoadException {
        checkValid();

        Set<String> keys = yamlConfiguration.getKeys(false);
        keys.remove("$name");

        Map<String, Message> map = new HashMap<>();
        for (String s : keys) {
            load(yamlConfiguration, map, s);
        }

        moduleMessages.put(module, map);

    }

    public void removeModuleTranslation(@NotNull Module module) {
        moduleMessages.remove(module);
    }


    @Override
    public @Nullable String displayName() {
        return displayName;
    }

    @Override
    public void displayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Override
    public @NotNull DiscordLocale discordLocale() {
        return discordLocale;
    }

    @Override
    public void discordLocale(@NotNull DiscordLocale locale) {
        this.discordLocale = locale;
    }


    @Override
    public @Nullable String icon() {
        return icon;
    }

    @Override
    public void icon(@Nullable String icon) {
        this.icon = icon;
    }


    public void invalid() {
        setValid(false);
    }

    @Override
    public String toString() {
        return "Translation{name=" + name + ", display-name=" + displayName + ", messages=" + messages.size() + ", icon=" + icon + ", file=" + file.getName() + "}";
    }

}
