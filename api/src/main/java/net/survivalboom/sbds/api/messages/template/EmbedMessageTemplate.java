package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.MessageInteractableComponentTemplate;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.components.templates.ButtonTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.*;
import java.util.function.Function;

@ConfigSerializable
public class EmbedMessageTemplate implements IMessageTemplate {

    private final List<EmbedTemplate> embeds = new ArrayList<>();

    private final List<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> components = new ArrayList<>();

    @Nullable
    private String content;


    public EmbedMessageTemplate(
            @Nullable String content,
            @Nullable Collection<EmbedTemplate> embeds,
            @Nullable Collection<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> components
    ) {

        this.content = content;

        if (embeds != null) {
            this.embeds.addAll(embeds);
        }

        if (components != null) {
            this.components.addAll(components);
        }

        if ((content == null || content.isBlank()) && this.embeds.isEmpty() && this.components.isEmpty()) {
            throw new IllegalArgumentException("Тобі блять робить нєхуй, чи що? Розкажи мені будь ласка.");
        }

        // Перевірка на дебіла. В одному рядку можуть бути або кнопки, або Dropdown.
        for (int i = 1; i < 6; i++) {

            int finalI = i;
            var templates = this.components.stream()
                    .filter(t -> t.getRow() == finalI)
                    .toList();

            boolean hasDropdown = templates.stream().anyMatch(t -> !(t instanceof ButtonTemplate));
            boolean hasButtons = templates.stream().anyMatch(t -> t instanceof ButtonTemplate);

            boolean isUserDumbFucker = hasDropdown && hasButtons;
            if (isUserDumbFucker) {
                throw new IllegalArgumentException("Invalid set of components on row " + i + ". Buttons and Dropdowns cannot be on the same row. Got: `" + templates + "`");
            }

            if (hasDropdown && templates.size() > 1) {
                throw new IllegalArgumentException("One row can only contain one dropdown. Got: `" + templates + "`");
            }

        }

    }

    @Override
    public @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        if ((content == null || content.isBlank()) && this.embeds.isEmpty() && this.components.isEmpty()) {
            throw new IllegalArgumentException("content is empty && embeds are empty && component are empty; you fucked up! congratulations!");
        }

        MessageCreateBuilder builder = new MessageCreateBuilder();

        builder.setContent(StringParser.stParseNullable(parser, content));

        // embeds //

        for (EmbedTemplate embed : embeds) {
            builder.addEmbeds(embed.build(parser));
        }

        // components //

        // Проходимось по кожній з 5 рядків та шукаємо компоненти, що знаходяться на такому рядку.
        for (int i = 0; i < 5; i++) {

            final int index = i;

            var components = this.components.stream()
                    .filter(component -> component.getRow() == index)
                    .toList();

            // Ця лінія не має компонентів, пропускаємо.
            if (components.isEmpty()) {
                continue;
            }

            // Це Dropdown, ніяких кнопок бути не може (якщо звісно якийсь дебіл якимсь чином не обійшов конструктор...), додаємо просто в ActionRow.
            if (!(components.getFirst() instanceof ButtonTemplate)) {

                ActionRowChildComponent component = components.getFirst().build(parser, linker);
                ActionRow row = ActionRow.of(component);

                builder.addComponents(row);

                continue;

            }

            // КНОПАЧКІ!!!! // Робимо кнопочкі. Логічно? Думаю да. А якщо ні - вийди в вікно.
            List<Button> buttons = new ArrayList<>();
            for (var template : components) {

                // Нєхуй Reflection бавитись, понятно блять? Нєхуй перевірку в конструкторі скіпать. Совсєм абнаглєлі вайбкодері.
                if (!(template instanceof ButtonTemplate buttonTemplate)) {
                    throw new RuntimeException("А схуялі блять, скажи мені будь ласка, га? Пизди давно тобі не давали, чи що? Хуль тут `" + template.getClass() + "` замість `ButtonTemplate? А блять?");
                }

                Button button = buttonTemplate.build(parser, linker);
                buttons.add(button);

            }

            ActionRow row = ActionRow.of(buttons);
            builder.addComponents(row);

        }

        return builder.build();

    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // BUILDER
    //

    public static @NotNull Builder fromSection(@NotNull ConfigurationNode section) {

        String content = section.node("$content").getString();

        var builder = builder();

        builder.setContent(content);

        ConfigurationNode componentsSection = section.node("$components");
        for (ConfigurationNode node : componentsSection.childrenList()) {
            MessageInteractableComponentTemplate<?> template = MessageInteractableComponentTemplate.fromSection(node);
            builder.addComponent(template);
        }

        ConfigurationNode embedSection = section.node("$embed");
        if (!embedSection.virtual()) {
            EmbedTemplate template = EmbedTemplate.fromSection(embedSection).build();
            return builder.addEmbed(template);
        }

        ConfigurationNode embedsSection = section.node("$embeds");
        for (ConfigurationNode node : embedsSection.childrenList()) {
            EmbedTemplate template = EmbedTemplate.fromSection(node).build();
            builder.addEmbed(template);
        }

        return builder;

    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder ofString(@NotNull String content) {
        return builder().setContent(content);
    }

    public static class Builder {

        @Nullable
        private String content;

        private final List<EmbedTemplate> embeds = new ArrayList<>();

        private final List<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> components = new ArrayList<>();


        private Builder() {}

        private Builder(@NotNull Builder builder) {

            this.content = builder.content;

            this.embeds.addAll(builder.embeds);
            this.components.addAll(builder.components);

        }

        private Builder(@NotNull EmbedMessageTemplate template) {

            this.content = template.content;

            this.embeds.addAll(template.embeds);
            this.components.addAll(template.components);

        }

        // CONTENT //

        public @NotNull Builder setContent(@Nullable String content) {
            this.content = content;
            return this;
        }

        public @Nullable String getContent() {
            return content;
        }

        // EMBEDS //

        public @NotNull Builder setEmbeds(@Nullable Collection<EmbedTemplate> embeds) {

            this.embeds.clear();

            if (embeds != null) {
                this.embeds.addAll(embeds);
            }

            return this;

        }

        public @NotNull Builder addEmbed(@NotNull EmbedTemplate embed) {
            this.embeds.add(embed);
            return this;
        }

        public @NotNull Builder addEmbed(@NotNull Function<EmbedTemplate.Builder, EmbedTemplate> function) {

            var builder = EmbedTemplate.builder();

            EmbedTemplate embed = function.apply(builder);
            this.embeds.add(embed);

            return this;

        }

        public @NotNull Builder addEmbeds(@NotNull Collection<EmbedTemplate> embeds) {
            this.embeds.addAll(embeds);
            return this;
        }

        public @NotNull List<EmbedTemplate> getEmbeds() {
            return new ArrayList<>(this.embeds);
        }

        // COMPONENTS //

        public @NotNull Builder setComponents(@Nullable Collection<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> components) {

            this.components.clear();

            if (components != null) {
                this.components.addAll(components);
            }

            return this;

        }

        public @NotNull Builder addComponent(@NotNull MessageInteractableComponentTemplate<? extends ActionRowChildComponent> component) {
            this.components.add(component);
            return this;
        }

        @SuppressWarnings("unchecked")
        public @NotNull Builder addComponent(@NotNull ComponentTemplate<?> component) {

            MessageInteractableComponentTemplate<? extends ActionRowChildComponent> sex;

            try {
                sex = (MessageInteractableComponentTemplate<? extends ActionRowChildComponent>) component;
            }

            catch (ClassCastException e) {
                throw new RuntimeException("Нєт. Ну просто блять нєт. Ну сука, ну просто ні. Ти розумієш блять? Воно так не буде сука працювати.", e);
            }

            return addComponent(sex);

        }

        public @NotNull Builder addComponents(@NotNull Collection<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> components) {
            this.components.addAll(components);
            return this;
        }

        public @NotNull List<MessageInteractableComponentTemplate<? extends ActionRowChildComponent>> getComponents() {
            return new ArrayList<>(this.components);
        }

        // BUILD //

        public @NotNull EmbedMessageTemplate build() {
            return new EmbedMessageTemplate(content, embeds, components);
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
