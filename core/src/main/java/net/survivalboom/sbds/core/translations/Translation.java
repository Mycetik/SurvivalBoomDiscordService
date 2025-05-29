package net.survivalboom.sbds.core.translations;

import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.InvalidComponentException;
import net.survivalboom.sbds.api.messages.MessageTemplate;
import net.survivalboom.sbds.api.translations.ITranslation;
import net.survivalboom.sbds.api.translations.InvalidTranslationException;
import net.survivalboom.sbds.api.translations.MessageLoadException;
import net.survivalboom.sbds.core.messages.Message;
import net.survivalboom.sbds.api.utils.Valid;
import net.survivalboom.sbds.api.messages.InvalidEmbedException;
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

    private final String name;

    private final File file;

    private String displayName;

    private String icon;


    private final Map<String, Message> messages = new HashMap<>();

    private final Map<Module, Map<String, Message>> moduleMessages = new HashMap<>();


    public Translation(@NotNull File file) throws IOException, InvalidConfigurationException, MessageLoadException, InvalidTranslationException {

        Objects.requireNonNull(file, "file == null");
        if (!file.exists() || !file.isFile()) throw new InvalidTranslationException("Invalid file `" + file.getPath() + "`");

        this.file = file;

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.load(file);

        this.name = yamlConfiguration.getString("$name");
        if (this.name == null) throw new InvalidTranslationException("Invalid translation. Key `$name` not found");

        load(yamlConfiguration);

    }


    private void load(@NotNull YamlConfiguration yamlConfiguration) throws MessageLoadException {

        checkValid();

        this.displayName = yamlConfiguration.getString("$display-name");
        this.icon = yamlConfiguration.getString("$icon");

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

            if (section.contains("$embed") || section.contains("$embeds")) {

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
    public @Nullable String icon() {
        return icon;
    }

    @Override
    public void icon(@Nullable String icon) {
        this.icon = icon;
    }


    public void invalid() {
        valid(false);
    }

    @Override
    public String toString() {
        return "Translation{name=" + name + ", display-name=" + displayName + ", messages=" + messages.size() + ", icon=" + icon + ", file=" + file.getName() + "}";
    }

}
