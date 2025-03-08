package net.survivalboom.sbds.core.messages;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.IEmbedTemplate;
import net.survivalboom.sbds.api.messages.IMessage;
import net.survivalboom.sbds.api.messages.InvalidEmbedException;
import net.survivalboom.sbds.api.utils.Placeholders;
import net.survivalboom.sbds.core.translations.Translation;
import net.survivalboom.sbds.api.utils.Valid;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Message extends Valid implements IMessage {

    private final Translation translation;

    private final String key;

    private final String text;

    private final List<EmbedTemplate> embeds;


    public Message(@NotNull Translation translation, @NotNull String key, @NotNull String text) {

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(text, "text == null");

        this.key = key;
        this.text = text;
        this.embeds = null;

        this.translation = translation;

    }

    public Message(@NotNull Translation translation, @NotNull String key, @NotNull ConfigurationSection section) throws InvalidEmbedException {

        Objects.requireNonNull(key, "key == null");
        Objects.requireNonNull(section, "section == null");

        this.key = key;
        this.embeds = createEmbeds(section);

        if (embeds.isEmpty()) throw new InvalidEmbedException("Message looks like an embed, but `createEmbeds` returned empty List. Check message syntax.");

        this.text = null;

        this.translation = translation;

    }

    public @Nullable String key() {
        return key;
    }

    public @Nullable String text() {
        return text;
    }


    public @Nullable List<IEmbedTemplate> embeds() {
        if (embeds == null) return null;
        return new ArrayList<>(embeds);
    }

    public @Nullable List<EmbedTemplate> embeds0() {
        return embeds;
    }


    public @Nullable Translation translation() {
        return translation;
    }

    @Override
    public @NotNull MessageCreateData messageData(@Nullable Placeholders placeholders) {

        if (embeds == null) {
            return MessageCreateData.fromContent(placeholders.parse(text));
        }

        List<MessageEmbed> messageEmbeds = new ArrayList<>();
        embeds.forEach(e -> messageEmbeds.add(e.build(placeholders).build()));

        return MessageCreateData.fromEmbeds(messageEmbeds);

    }


    public void dump(@NotNull ConfigurationSection cfg) {

        if (text != null) {
            cfg.set(key, text);
            return;
        }

        Objects.requireNonNull(embeds, "embeds == null, while text == null. Congratulations! You broke everything!");

        List<Map<String, String>> mapList = new ArrayList<>();
        embeds.forEach(embed -> mapList.add(embed.dump()));

        if (mapList.size() > 1) cfg.set(key + ".$embeds", mapList);
        else cfg.set(key + ".$embed", mapList.getFirst());

    }


    private @NotNull List<EmbedTemplate> createEmbeds(@NotNull ConfigurationSection section) throws InvalidEmbedException {

        Objects.requireNonNull(section, "section == null");

        List<EmbedTemplate> out = new ArrayList<>();

        if (!section.contains("$embed") && !section.contains("$embeds")) return new ArrayList<>();

        if (!section.contains("$embeds")) {

            ConfigurationSection embedSection = section.getConfigurationSection("$embed");
            Objects.requireNonNull(embedSection);

            EmbedTemplate embed = new EmbedTemplate(embedSection);

            return new ArrayList<>(List.of(embed));
        }


        List<Map<?, ?>> map = section.getMapList("$embeds");
        for (Map<?, ?> m : map) {
            out.add(new EmbedTemplate(m));
        }

        return out;

    }


}
