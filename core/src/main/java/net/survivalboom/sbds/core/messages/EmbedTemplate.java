package net.survivalboom.sbds.core.messages;

import net.dv8tion.jda.api.EmbedBuilder;
import net.survivalboom.sbds.api.messages.InvalidEmbedException;
import net.survivalboom.sbds.api.utils.Placeholders;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.*;

public class EmbedTemplate {

    // AUTHOR //

    @Nullable private final String author;

    @Nullable private final String authorUrl;

    @Nullable private final String authorIconUrl;

    // BODY //

    @Nullable private final String title;

    @Nullable private final String description;

    @Nullable private final String url;

    @Nullable private final String thumbnailUrl;

    // FOOTER //

    @Nullable private final String footer;

    @Nullable private final String footIconUrl;

    @Nullable private final String timestamp;

    // FIELDS //

    @NotNull private final List<EmbedField> fields = new ArrayList<>();



    public EmbedTemplate(@NotNull ConfigurationSection section) throws InvalidEmbedException {

        Objects.requireNonNull(section, "section == null");

        author = section.getString("author");
        authorUrl = section.getString("author-url");
        authorIconUrl = section.getString("author-icon");

        title = section.getString("title");
        description = section.getString("description");
        url = section.getString("url");
        thumbnailUrl = section.getString("thumbnail");

        footer = section.getString("footer");
        footIconUrl = section.getString("footer-icon");

        timestamp = section.getString("timestamp");

        List<Map<?, ?>> fieldList = section.getMapList("fields");
        if (fieldList.isEmpty()) return;

        fields.addAll(loadFields(fieldList));

    }

    @SuppressWarnings("unchecked")
    public EmbedTemplate(@NotNull Map<?, ?> section) throws InvalidEmbedException {

        Objects.requireNonNull(section, "section == null");

        author = toString(section.get("author"));
        authorUrl = toString(section.get("author-url"));
        authorIconUrl = toString(section.get("author-icon"));

        title = toString(section.get("title"));
        description = toString(section.get("description"));
        url = toString(section.get("url"));
        thumbnailUrl = toString(section.get("thumbnail"));

        footer = toString(section.get("footer"));
        footIconUrl = toString(section.get("footer-icon"));

        timestamp = toString(section.get("timestamp"));

        List<Map<?, ?>> fieldList;
        try {
            fieldList = (List<Map<?, ?>>) section.get("fields");
        }

        catch (ClassCastException e) {
            throw new InvalidEmbedException("Invalid embed fields");
        }

        if (fieldList == null) return;

        fields.addAll(loadFields(fieldList));

    }


    public @NotNull Map<String, String> dump() {

        Map<String, String> out = new HashMap<>();

        out.put("author", author);
        out.put("author-url", authorUrl);
        out.put("author-icon", authorIconUrl);

        out.put("title", title);
        out.put("description", description);
        out.put("url", url);
        out.put("thumbnail", thumbnailUrl);

        out.put("footer", footer);
        out.put("footer-icon", footIconUrl);

        out.put("timestamp", timestamp);

        return out;

    }

    public @NotNull EmbedBuilder build(@Nullable Placeholders pl) {

        Placeholders placeholders = new Placeholders();
        placeholders.add("{TIME}", LocalDateTime.now().toString());
        placeholders.addAll(pl);

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(author, authorUrl, authorIconUrl);

        builder.setTitle(title, url);
        builder.setDescription(description);
        builder.setThumbnail(thumbnailUrl);

        builder.setFooter(footer, footIconUrl);

        if (timestamp != null) builder.setTimestamp(LocalDateTime.parse(timestamp));

        fields.forEach(f -> builder.addField(f.name(), f.value(), f.inline()));

        return builder;

    }

    private @NotNull List<EmbedField> loadFields(@NotNull List<Map<?, ?>> map) throws InvalidEmbedException {

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

    private @Nullable String toString(@Nullable Object o) {

        if (o == null) return null;

        if (o instanceof String s) return s;

        return o.toString();

    }


    record EmbedField(@NotNull String name, @NotNull String value, boolean inline) {}

}

