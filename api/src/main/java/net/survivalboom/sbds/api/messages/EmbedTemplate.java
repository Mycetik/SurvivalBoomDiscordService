package net.survivalboom.sbds.api.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;


// TODO: Make dumb-checks.
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
    private final List<EmbedField> fields;


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
            @NotNull List<EmbedField> fields

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

        this.fields = fields;

    }


    public @NotNull MessageEmbed build(@Nullable Function<String, String> parser) {

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(author != null ? parse(author, parser) : null, authorUrl != null ? parse(authorUrl, parser) : null, authorIconUrl != null ? parse(authorIconUrl, parser) : null);

        builder.setTitle(title != null ? parse(title, parser) : null, url != null ? parse(url, parser) : null);
        builder.setColor(color);
        builder.setDescription(description != null ? parse(description, parser) : null);
        builder.setThumbnail(thumbnailUrl != null ? parse(thumbnailUrl, parser) : null);

        builder.setFooter(footer != null ? parse(footer, parser) : null, footIconUrl != null ? parse(footIconUrl, parser) : null);

        if (timestamp != null) builder.setTimestamp(LocalDateTime.parse(timestamp));

        fields.forEach(f -> builder.addField(parse(f.name(), parser), parse(f.value(), parser), f.inline()));

        return builder.build();

    }

    public @NotNull Builder toBuilder() {
        return new Builder(author, authorUrl, authorIconUrl, title, color, description, url, thumbnailUrl, footer, footIconUrl, timestamp, new ArrayList<>(fields));
    }
    
    
    private @NotNull String parse(@NotNull String s, @Nullable Function<String, String> parser) {
        if (parser != null) s = parser.apply(s);
        return s.replace("{TIME}", LocalDateTime.now().toString());
    }


    private record EmbedField(@NotNull String name, @NotNull String value, boolean inline) {}

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
        private final List<EmbedField> fields;


        private Builder(

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
                @NotNull List<EmbedField> fields

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

            this.fields = fields;

        }

        // AUTHOR //
        public @NotNull Builder setAuthor(@Nullable String author, @Nullable String authorUrl, @Nullable String authorIconUrl) {

            this.author = author;
            this.authorUrl = authorUrl;
            this.authorIconUrl = authorIconUrl;

            return this;

        }

        // FIELDS //
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
            return new EmbedTemplate(author, authorUrl, authorIconUrl, title, color, description, url, thumbnailUrl, footer, footIconUrl, timestamp, new ArrayList<>(fields));
        }

        public @NotNull Builder copy() {
            return new Builder(author, authorUrl, authorIconUrl, title, color, description, url, thumbnailUrl, footer, footIconUrl, timestamp, new ArrayList<>(fields));
        }

    }


    public static @NotNull Builder builder() {
        return new Builder(null, null, null, null, null, null, null, null, null, null, null, new ArrayList<>());
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

}
