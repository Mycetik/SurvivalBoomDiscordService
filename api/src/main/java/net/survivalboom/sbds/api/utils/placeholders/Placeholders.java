package net.survivalboom.sbds.api.utils.placeholders;

import net.survivalboom.sbds.api.messages.parsers.StringParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

public class Placeholders implements StringParser {

    public static final String DEFAULT_LAYOUT = "{$p$}";


    private final Map<String, Object> placeholders = new HashMap<>();

    private final boolean allowNullValues;


    public Placeholders() {
        this.allowNullValues = true;
    }

    public Placeholders(@NotNull Map<String, Object> map) {

        this.allowNullValues = true;

        for (var entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }

    }

    public Placeholders(@Nullable Placeholders oldPlaceholders) {

        if (oldPlaceholders == null) {
            this.allowNullValues = true;
            return;
        }

        this.allowNullValues = oldPlaceholders.allowNullValues;
        placeholders.putAll(oldPlaceholders.placeholders);

    }

    public Placeholders(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }


    //
    // GET
    //

    @NotNull
    public Map<String, Object> getAsMap() {
        return new HashMap<>(placeholders);
    }

    @Nullable
    public Object get(@NotNull String placeholder) {
        return placeholders.get(placeholder);
    }

    public boolean contains(@NotNull String name) {
        return placeholders.containsKey(name);
    }

    //
    // ADD
    //

    @NotNull
    public Placeholders add(@NotNull String placeholder, @Nullable Object value) {

        Objects.requireNonNull(placeholder, "placeholder == null");

        if (!allowNullValues) {
            Objects.requireNonNull(value, "value == null");
        }

        placeholders.put(placeholder, value);

        return this;

    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, @Nullable Supplier<?> supplier) {

        Objects.requireNonNull(placeholder, "placeholder == null");

        if (!allowNullValues) {
            Objects.requireNonNull(supplier, "supplier == null");
        }

        placeholders.put(placeholder, supplier);

        return this;

    }

    @NotNull
    public Placeholders addAll(@Nullable Placeholders placeholders) {

        if (placeholders == null) {
            return this;
        }

        for (var entry : placeholders.placeholders.entrySet()) {

            String key = entry.getKey();
            Object obj = entry.getValue();

            if (!allowNullValues) {
                Objects.requireNonNull(obj, "value == null");
            }

            this.placeholders.put(key, obj);

        }

        return this;

    }

    @NotNull
    public Placeholders addAll(@Nullable Map<String, String> map) {
        if (map != null) this.placeholders.putAll(map);
        return this;
    }

    //
    // REMOVE
    //

    public @NotNull Placeholders remove(@NotNull String key) {
        this.placeholders.remove(key);
        return this;
    }

    public @NotNull Placeholders removeAll(@NotNull Collection<String> keys) {
        keys.forEach(placeholders::remove);
        return this;
    }

    public @NotNull Placeholders removeAll(@NotNull String... keys) {

        for (String key : keys) {
            this.placeholders.remove(key);
        }

        return this;

    }

    //
    // PARSE
    //

    @Override
    public @NotNull String parse(@NotNull String string) {
        return parse(string, DEFAULT_LAYOUT);
    }

    public @NotNull String parse(@NotNull String text, @Nullable String layout) {

        var map = prepareRecursivePlaceholders(this.placeholders, "");

        for (var entry : map.entrySet()) {

            String key = entry.getKey();
            Object object = entry.getValue();

            if (key == null) {
                throw new NullPointerException("Key is null! " + map);
            }

            String placeholder = layout.replace("$p$", key);

            if (object == null) {

                if (!allowNullValues) {
                    throw new NullPointerException("Value of `" + key + "` is null");
                }

                text = text.replace(placeholder, "null");
                continue;

            }

            String objectToString = object.toString();
            text = text.replace(placeholder, objectToString);

        }

        return text;

    }

    private Map<String, Object> prepareRecursivePlaceholders(@NotNull Map<String, Object> map, String prefix) {

        Map<String, Object> out = new HashMap<>();
        for (var entry : map.entrySet()) {

            String key = entry.getKey();
            Object object = entry.getValue();

            if (object instanceof Supplier<?> supplier) {
                object = supplier.get();
            }

            if (object instanceof IPlaceholders ip) {
                var pl = ip.placeholders().placeholders;
                out.putAll(prepareRecursivePlaceholders(pl, prefix + key + "."));
                continue;
            }

            out.put(prefix + key, object);

        }

        return out;

    }

    @NotNull
    public List<String> parseAll(@NotNull List<String> texts) {

        List<String> out = new ArrayList<>();

        for (String s : texts) {
            out.add(parse(s));
        }

        return out;

    }

    //
    // MISC
    //

    public void clear() {
        placeholders.clear();
    }

    @Override
    public String toString() {
        return String.format("Placeholders%s", placeholders);
    }

    public @NotNull Placeholders copy() {
        return new Placeholders(this);
    }


    public @NotNull Placeholders selfParse() {
        return selfParse(100);
    }

    public @NotNull Placeholders selfParse(int attempts) {

        for (var entry : placeholders.entrySet()) {

            String lastParsedAttempt = null;
            int attempt = 0;
            while (true) {

                Object value = entry.getValue();

                if (!(value instanceof String string)) {
                    continue;
                }

                String parsedValue = parse(string);
                if (parsedValue.equals(lastParsedAttempt)) {
                    break;
                }

                if (attempt >= attempts) {
                    throw new IllegalStateException("attempt " + attempt + " >= max_attempts " + attempts);
                }

                lastParsedAttempt = parsedValue;

                entry.setValue(value);

            }

        }

        return this;

    }


    //
    // STATIC
    //

    public static @NotNull Placeholders of(@NotNull Map<String, String> map) {

        Placeholders placeholders = new Placeholders();

        for (var entry : map.entrySet()) {
            placeholders.add(entry.getKey(), entry.getValue());
        }

        return placeholders;

    }

    public static @NotNull Placeholders of(Object... args) {

        Placeholders placeholders = new Placeholders();

        int index = 0;
        while (args.length > index + 1) {

            Object key = args[index];
            Object value = args[index + 1];

            if (!(key instanceof String string)) {
                throw new IllegalArgumentException(String.format("Key %s is not a string", key));
            }

            placeholders.add(string, value);

            index += 2;

        }

        return placeholders;

    }


    public static @NotNull String parse(@NotNull String s, @Nullable Placeholders placeholders) {
        Objects.requireNonNull(s, "string == null");
        if (placeholders == null) return s;
        return placeholders.parse(s);
    }



}
