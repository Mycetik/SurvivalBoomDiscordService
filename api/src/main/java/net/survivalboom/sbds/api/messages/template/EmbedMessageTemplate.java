package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.components.Component;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.survivalboom.sbds.api.messages.components.InvalidComponentException;
import net.survivalboom.sbds.api.messages.components.ComponentLinker;
import net.survivalboom.sbds.api.messages.components.ComponentTemplate;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.typemap.ModifiableTypeMap;
import net.survivalboom.sbds.api.utils.typemap.TypeMap;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

public class EmbedMessageTemplate implements IMessageTemplate {

    private static final Logger log = LoggerFactory.getLogger(EmbedMessageTemplate.class);
    @Nullable
    private final String content;

    private final List<EmbedTemplate> embeds = new ArrayList<>();

    private final List<ComponentTemplate> components = new ArrayList<>();


    public EmbedMessageTemplate(
            @Nullable String content,
            @Nullable Collection<EmbedTemplate> embeds,
            @Nullable Collection<ComponentTemplate> components
    ) {

        this.content = content;

        if (embeds != null) {
            this.embeds.addAll(embeds);
        }

        if (components != null) {
            this.components.addAll(components);
        }

    }

    @Override
    public @NotNull MessageCreateData createMessageData(@Nullable StringParser parser, @Nullable ComponentLinker linker) {

        MessageCreateBuilder builder = new MessageCreateBuilder();

        builder.setContent(StringParser.stParseNullable(parser, content));

        // embeds //

        for (EmbedTemplate embed : embeds) {
            builder.addEmbeds(embed.build(parser));
        }

        // components //

        for (int i = 1; i < 6; i++) {

            final int index = i;

            List<Component> components = this.components.stream()
                    .filter(component -> component.getRow() == index)
                    .sorted(Comparator.comparing(ComponentTemplate::getPriority))
                    .map(component -> component.build(parser, linker))
                    .toList();

            List<ActionRowChildComponent> rowChildComponents = new ArrayList<>();
            for (Component component : components) {

                if (!(component instanceof ActionRowChildComponent actionRowChildComponent)) {
                    log.warn("Tried to add incompatible with ActionRow component `{}`.", component.getType());
                    continue;
                }

                rowChildComponents.add(actionRowChildComponent);

            }

            ActionRow row = ActionRow.of(rowChildComponents);
            builder.addComponents(row);

        }

        return builder.build();

    }

    @Override
    public void dump(@NotNull ModifiableTypeMap map) {
        
    }

    //
    // BUILDER
    //

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static @NotNull Builder ofString(@NotNull String content) {
        return builder().setContent(content);
    }

    public static @NotNull Builder ofMap(@NotNull TypeMap map) throws InvalidEmbedException, InvalidComponentException {

        String content = map.getCast("$content", String.class).orElse(null);
        List<EmbedTemplate> embeds = createEmbeds(map);

        map.getCastList()

        List<ComponentTemplate> components = ComponentTemplate.createComponents(TypeMap.ofMapList(section.getMapList("$components")));

        return builder()
                .setContent(content)
                .setEmbeds(embeds)
                .setComponents(components);

    }

    private static @NotNull List<EmbedTemplate> createEmbeds(@NotNull ConfigurationSection section) throws InvalidEmbedException {

        Objects.requireNonNull(section, "section == null");

        List<EmbedTemplate> out = new ArrayList<>();

        if (!section.contains("$embed") && !section.contains("$embeds")) return new ArrayList<>();

        if (!section.contains("$embeds")) {

            ConfigurationSection embedSection = section.getConfigurationSection("$embed");
            Objects.requireNonNull(embedSection);

            EmbedTemplate embed = EmbedTemplate.fromSection(embedSection);

            return new ArrayList<>(List.of(embed));

        }


        List<Map<?, ?>> map = section.getMapList("$embeds");
        for (Map<?, ?> m : map) {
            out.add(EmbedTemplate.fromSection(m));
        }

        return out;

    }

    public static class Builder {

        @Nullable
        private String content;

        private final List<EmbedTemplate> embeds = new ArrayList<>();

        private final List<ComponentTemplate> components = new ArrayList<>();


        private Builder() {}

        private Builder(@NotNull Builder builder) {

            this.content = builder.content;

            this.embeds.addAll(builder.embeds);
            this.components.addAll(builder.components);

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

        public @NotNull Builder setComponents(@Nullable Collection<ComponentTemplate> components) {

            this.components.clear();

            if (components != null) {
                this.components.addAll(components);
            }

            return this;

        }

        public @NotNull Builder addComponent(@NotNull ComponentTemplate component) {
            this.components.add(component);
            return this;
        }

        public @NotNull Builder addComponents(@NotNull Collection<ComponentTemplate> components) {
            this.components.addAll(components);
            return this;
        }

        public @NotNull List<ComponentTemplate> getComponents() {
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
