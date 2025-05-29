package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Placeholders {

    private final Map<String, String> placeholders = new HashMap<>();

    private final boolean allowNullValues;

    public Placeholders() {
        this.allowNullValues = true;
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
    public List<String> get() {
        return new ArrayList<>(placeholders.keySet());
    }

    @Nullable
    public String get(@NotNull String placeholder) {
        return placeholders.get(placeholder);
    }

    public boolean contains(@NotNull String name) {
        return placeholders.containsKey(name);
    }

    //
    // ADD
    //

    @NotNull
    public Placeholders add(@NotNull String placeholder, @Nullable String value) {

        if (!allowNullValues) {
            if (value == null) return this;
            placeholders.put(placeholder, value);
            return this;
        }

        placeholders.put(placeholder, value != null ? value : "null");

        return this;
    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, int value) {
        placeholders.put(placeholder, String.valueOf(value));
        return this;
    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, @Nullable Object value) {

        if (!allowNullValues) {
            if (value == null) return this;
            placeholders.put(placeholder, value.toString());
            return this;
        }

        placeholders.put(placeholder, value != null ? value.toString() : "null");

        return this;

    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, @NotNull AtomicInteger integer) {

        Objects.requireNonNull(integer);

        add(placeholder, integer.get());

        return this;

    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, @NotNull AtomicBoolean atomicBoolean) {

        Objects.requireNonNull(atomicBoolean);

        add(placeholder, atomicBoolean.get());

        return this;

    }

    @NotNull
    public Placeholders add(@NotNull String placeholder, @NotNull AtomicReference<?> reference) {

        Objects.requireNonNull(reference);

        add(placeholder, reference.get());

        return this;

    }

    @NotNull
    public Placeholders addAll(@Nullable Placeholders placeholders) {
        if (placeholders != null) this.placeholders.putAll(placeholders.placeholders);
        return this;
    }

    @NotNull
    public Placeholders putAll(@Nullable Map<String, String> map) {
        if (map != null) this.placeholders.putAll(map);
        return this;
    }

    //
    // PARSE
    //

    @NotNull
    public String parse(@NotNull String text) {

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {

            if (entry.getKey() == null) throw new RuntimeException(String.format("Placeholder key is null! [null = %s] Placeholders obj content: %s", entry.getValue(), placeholders));
            if (entry.getValue() == null) throw new RuntimeException(String.format("Placeholder value is null! [%s = null] Placeholders obj content: %s", entry.getKey(), placeholders));

            text = text.replace(entry.getKey(), entry.getValue());

        }

        return text;

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

    public Placeholders copy() {
        return new Placeholders(this);
    }


    public Placeholders selfParseValues() {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {

            String value = entry.getValue();
            String parsedValue = parse(value);

            entry.setValue(parsedValue);

        }

        return this;

    }


    //
    // STATIC
    //

    public static Placeholders of(Object... args) {

        Placeholders placeholders = new Placeholders();

        int index = 0;
        while (args.length > index + 1) {

            Object key = args[index];
            Object value = args[index + 1];

            if (!(key instanceof String string)) throw new IllegalArgumentException(String.format("Object %s is not a string", key));

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
