package net.survivalboom.sbds.api.messages.template;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

public class EmbedTemplate {

    // AUTHOR //

    @Nullable
    private final String author;

    @Nullable
    private final String authorUrl;

    @Nullable
    private final String authorIconUrl;

    // BODY //

    @Nullable
    private final String title;

    @Nullable
    private final Color color;

    @Nullable
    private final String description;

    @Nullable
    private final String url;

    @Nullable
    private final String thumbnailUrl;

    // FOOTER //

    @Nullable
    private final String footer;

    @Nullable
    private final String footIconUrl;

    @Nullable
    private final String timestamp;

    // FIELDS //

    @NotNull
    private final List<EmbedField> fields = new ArrayList<>();


    private EmbedTemplate(

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

    public record EmbedField(@NotNull String name, @NotNull String value, boolean inline) {}

    //
    // BUILDER
    //

    public static Builder builder() {
        return new Builder();
    }

    private static @NotNull EmbedTemplate fromSection(@NotNull Get section) throws InvalidEmbedException {

        String author = section.getString("author");
        String authorUrl = section.getString("author-url");
        String authorIconUrl = section.getString("author-icon");

        String title = section.getString("title");
        String description = section.getString("description");
        String url = section.getString("url");
        String thumbnailUrl = section.getString("thumbnail");

        String colorRaw = section.getString("color");
        Color color;
        if (colorRaw != null) {
            color = color(colorRaw);
            if (color == null) throw new InvalidEmbedException("Invalid color `" + colorRaw + "`");
        }

        else color = null;

        String footer = section.getString("footer");
        String footIconUrl = section.getString("footer-icon");

        String timestamp = section.getString("timestamp");

        Builder builder = builder();
        builder.setBody(title, description, thumbnailUrl);
        builder.setUrl(url);

        builder.setAuthor(author, authorUrl, authorIconUrl);
        builder.setColor(color);

        builder.setFooter(footer, footIconUrl);
        builder.setTimestamp(timestamp);

        List<Map<?, ?>> fieldList = section.getMapList("fields");
        if (!fieldList.isEmpty()) {
            builder.addField(loadFields(fieldList));
        }

        return builder.build();

    }

    public static @NotNull EmbedTemplate fromSection(@NotNull ConfigurationSection section) throws InvalidEmbedException {

        Get get = new Get() {

            @Override
            public @NotNull List<Map<?, ?>> getMapList(@NotNull String key) {
                return section.getMapList(key);
            }

            @Override
            public @NotNull String getSting(@NotNull String key, @NotNull String v) {
                return section.getString(key, v);
            }

            @Override
            public @Nullable String getString(@NotNull String key) {
                return section.getString(key);
            }

        };

        return fromSection(get);

    }

    public static @NotNull EmbedTemplate fromSection(@NotNull Map<?, ?> map) throws InvalidEmbedException {

        Get get = new Get() {

            @Override
            @SuppressWarnings("unchecked")
            public @NotNull List<Map<?, ?>> getMapList(@NotNull String key) {
                return (List<Map<?, ?>>) map.get("fields");
            }

            @Override
            public @NotNull String getSting(@NotNull String key, @NotNull String v) {
                return (String) Objects.requireNonNullElse(map.get(key), v);
            }

            @Override
            public @Nullable String getString(@NotNull String key) {
                return (String) map.get(key);
            }

        };

        return fromSection(get);

    }

    private static @NotNull List<EmbedField> loadFields(@NotNull List<Map<?, ?>> map) throws InvalidEmbedException {

        List<EmbedField> out = new ArrayList<>();
        for (int i = 0; i < map.size(); i++) {

            Map<?, ?> field  = map.get(i);

            String name = (String) field.get("name");
            String value = (String) field.get("value");

            Boolean inline = (Boolean) field.get("inline");
            if (inline == null) inline = false;

            if (name == null || value == null) throw new InvalidEmbedException("Invalid field `" + i + "`. Field must have string `name`, string `value` and (optional) boolean `inline` keys.");

            out.add(new EmbedField(name, value, inline));

        }

        return out;

    }

    private static @Nullable Color color(@NotNull String hex) {

        int resultRed, resultGreen, resultBlue;
        try {
            resultRed = Integer.valueOf(hex.substring(0, 2), 16);
            resultGreen = Integer.valueOf(hex.substring(2, 4), 16);
            resultBlue = Integer.valueOf(hex.substring(4, 6), 16);
        }

        catch (NumberFormatException e) {
            return null;
        }

        return new Color(resultRed, resultGreen, resultBlue);

    }

    private interface Get {

        @NotNull List<Map<?, ?>> getMapList(@NotNull String key);

        @NotNull String getSting(@NotNull String key, @NotNull String v);

        @Nullable String getString(@NotNull String key);

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

        private @NotNull Builder addField(@NotNull Collection<EmbedField> fields) {
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
