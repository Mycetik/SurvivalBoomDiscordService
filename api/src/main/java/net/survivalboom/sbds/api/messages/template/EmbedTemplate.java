package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class EmbedTemplate {

    // AUTHOR //

    private final @Nullable String author;
    private final @Nullable String authorUrl;
    private final @Nullable String authorIconUrl;

    // BODY //

    private final @Nullable String title;
    private final @Nullable String description;

    private final @Nullable String url;
    private final @Nullable String thumbnailUrl;

    private final @Nullable Color color;

    // FOOTER //

    private final @Nullable String footer;
    private final @Nullable String footIconUrl;

    private final @Nullable String timestamp;

    // FIELDS //

    private final @NotNull List<EmbedField> fields = new ArrayList<>();


    public EmbedTemplate(

            // AUTHOR //
            @Nullable String author,
            @Nullable String authorUrl,
            @Nullable String authorIconUrl,

            // BODY  //
            @Nullable String title,
            @Nullable Color color,
            @Nullable String description,
            @Nullable String url,
            @Nullable String thumbnailUrl,

            // FOOTER //
            @Nullable String footer,
            @Nullable String footIconUrl,
            @Nullable String timestamp,

            // FIELDS //
            @Nullable Collection<EmbedField> fields

    ) {

        this.author = author;
        this.authorUrl = authorUrl;
        this.authorIconUrl = authorIconUrl;

        this.title = title;
        this.color = color;
        this.description = description;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;

        this.footer = footer;
        this.footIconUrl = footIconUrl;
        this.timestamp = timestamp;

        if (fields != null) {
            this.fields.addAll(fields);
        }

    }

    // BUILD //

    public @NotNull EmbedBuilder build(@Nullable StringParser parser) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(
                StringParser.stParseNullable(parser, author),
                StringParser.stParseNullable(parser, authorUrl),
                StringParser.stParseNullable(parser, authorIconUrl)
        );

        builder.setTitle(
                StringParser.stParseNullable(parser, title),
                StringParser.stParseNullable(parser, url)
        );

        builder.setThumbnail(StringParser.stParseNullable(parser, thumbnailUrl));

        builder.setDescription(StringParser.stParseNullable(parser, description));

        builder.setColor(color);

        builder.setFooter(
                StringParser.stParseNullable(parser, footer),
                StringParser.stParseNullable(parser, footIconUrl)
        );

        if (timestamp != null) {
            builder.setTimestamp(LocalDateTime.parse(timestamp));
        }

        for (EmbedField field : fields) {

            String name = StringParser.stParse(parser, field.name);
            String value = StringParser.stParse(parser, field.value);
            boolean inline = field.inline;

            builder.addField(name, value, inline);

        }

        return builder;

    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // FIELDS
    //

    public record EmbedField(
            @NotNull String name,
            @Nullable String value,
            boolean inline
    ) {}

    //
    // BUILDER
    //

    public static @NotNull Builder fromSection(@NotNull ConfigurationNode section) {

        String author = section.node("author").getString();
        String authorUrl = section.node("author-url").getString();
        String authorIconUrl = section.node("author-icon").getString();

        String title = section.node("title").getString();
        String description = section.node("description").getString();
        String colorRaw = section.node("color").getString();
        String url = section.node("url").getString();
        String thumbnailUrl = section.node("thumbnail").getString();

        String footer = section.node("footer").getString();
        String footerIconUrl = section.node("footer-url").getString();
        String timestamp = section.node("timestamp").getString();

        Color color = CommonUtils.parseColor(colorRaw);

        ConfigurationNode fieldsSection = section.node("fields");
        List<EmbedField> fields = new ArrayList<>();
        for (ConfigurationNode node : fieldsSection.childrenList()) {

            String name = node.node("name").getString();
            String value = node.node("value").getString();
            boolean inline = node.node("inline").getBoolean(false);

            EmbedField field = new EmbedField(name, value, inline);
            fields.add(field);

        }

        return builder()
                .setAuthor(author, authorUrl, authorIconUrl)
                .setBody(title, description, thumbnailUrl)
                .setUrl(url)
                .setColor(color)
                .setFooter(footer, footerIconUrl)
                .setTimestamp(timestamp)
                .setFields(fields);

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        // AUTHOR //

        @Nullable
        private String author;

        @Nullable
        private String authorUrl;

        @Nullable
        private String authorIconUrl;

        // BODY //

        @Nullable
        private String title;

        @Nullable
        private Color color;

        @Nullable
        private String description;

        @Nullable
        private String url;

        @Nullable
        private String thumbnailUrl;

        // FOOTER //

        @Nullable
        private String footer;

        @Nullable
        private String footIconUrl;

        @Nullable
        private String timestamp;

        // FIELDS //

        @NotNull
        private final List<EmbedField> fields = new ArrayList<>();

        private Builder() {}

        private Builder(@NotNull Builder builder) {

            this.author = builder.author;
            this.authorUrl = builder.authorUrl;
            this.authorIconUrl = builder.authorIconUrl;

            this.title = builder.title;
            this.color = builder.color;
            this.description = builder.description;
            this.url = builder.url;
            this.thumbnailUrl = builder.thumbnailUrl;

            this.footer = builder.footer;
            this.footIconUrl = builder.footIconUrl;
            this.timestamp = builder.timestamp;

            this.fields.addAll(builder.fields);

        }

        public Builder(@NotNull EmbedTemplate embed) {

            this.author = embed.author;
            this.authorUrl = embed.authorUrl;
            this.authorIconUrl = embed.authorIconUrl;

            this.title = embed.title;
            this.color = embed.color;
            this.description = embed.description;
            this.url = embed.url;
            this.thumbnailUrl = embed.thumbnailUrl;

            this.footer = embed.footer;
            this.footIconUrl = embed.footIconUrl;
            this.timestamp = embed.timestamp;

            this.fields.addAll(embed.fields);

        }

        // AUTHOR //
        public @NotNull Builder setAuthor(@Nullable String author, @Nullable String authorUrl, @Nullable String authorIconUrl) {

            this.author = author;
            this.authorUrl = authorUrl;
            this.authorIconUrl = authorIconUrl;

            return this;

        }

        // FIELDS //

        public @NotNull Builder setFields(@Nullable Collection<EmbedField> fields) {

            this.fields.clear();

            if (fields != null) {
                this.fields.addAll(fields);
            }

            return this;

        }

        public @NotNull Builder addField(@NotNull String name, @NotNull String value, boolean inline) {

            Objects.requireNonNull(name, "name == null");
            Objects.requireNonNull(value, "value == null");

            this.fields.add(new EmbedField(name, value, inline));

            return this;

        }

        private @NotNull Builder addFields(@NotNull Collection<EmbedField> fields) {
            this.fields.addAll(fields);
            return this;
        }

        // BODY //

        public @NotNull Builder setBody(@Nullable String title, @Nullable String description, @Nullable String thumbnailUrl) {

            this.title = title;
            this.description = description;
            this.thumbnailUrl = thumbnailUrl;

            return this;

        }

        public @NotNull Builder setColor(@Nullable Color color) {
            this.color = color;
            return this;
        }

        public @NotNull Builder setUrl(@Nullable String url) {
            this.url = url;
            return this;
        }

        public @NotNull Builder setTimestamp(@Nullable String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        // FOOTER //
        public @NotNull Builder setFooter(@Nullable String footer, @Nullable String footIconUrl) {
            this.footer = footer;
            this.footIconUrl = footIconUrl;
            return this;
        }

        // BUILD //

        public @NotNull EmbedTemplate build() {
            return new EmbedTemplate(
                    author, 
                    authorUrl, 
                    authorIconUrl, 
                    title, 
                    color, 
                    description, 
                    url, 
                    thumbnailUrl, 
                    footer, 
                    footIconUrl, 
                    timestamp, 
                    fields
            );
        }

        public @NotNull Builder copy() {
            return new Builder(this);
        }

    }

}
