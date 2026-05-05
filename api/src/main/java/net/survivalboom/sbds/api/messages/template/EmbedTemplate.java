package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.PostProcess;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@ConfigSerializable
public class EmbedTemplate {

    // AUTHOR //

    private @Setting("author") @Nullable String author;
    private @Setting("author-url") @Nullable String authorUrl;
    private @Setting("author-icon") @Nullable String authorIconUrl;

    // BODY //

    private @Setting("title") @Nullable String title;
    private @Setting("description") @Nullable String description;

    private @Setting("url") @Nullable String url;
    private @Setting("thumbnail") @Nullable String thumbnailUrl;

    private @Setting("color") @Nullable Color color;

    // FOOTER //

    private @Setting("footer") @Nullable String footer;
    private @Setting("footer-icon") @Nullable String footIconUrl;

    private @Setting("timestamp") @Nullable String timestamp;

    // FIELDS //

    @Setting("fields")
    private @NotNull List<EmbedField> fields = new ArrayList<>();


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

    @ApiStatus.Internal
    public EmbedTemplate() {}

    @PostProcess
    private void validate() throws SerializationException {

    }

    // BUILD //

    public @NotNull MessageEmbed build(@Nullable StringParser parser) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(
                StringParser.stParseNullable(parser, author),
                StringParser.stParseNullable(parser, authorUrl),
                StringParser.stParseNullable(parser, authorIconUrl)
        );

        builder.setTitle(
                StringParser.stParseNullable(parser, title),
                StringParser.stParseNullable(parser, title)
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

        return builder.build();

    }

    public @NotNull Builder copy() {
        return new Builder(this);
    }

    //
    // FIELDS
    //

    @ConfigSerializable
    public record EmbedField(
            @Setting("name") @NotNull String name,
            @Setting("value") @NotNull String value,
            @Setting("inline") boolean inline
    ) {}

    //
    // BUILDER
    //

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
