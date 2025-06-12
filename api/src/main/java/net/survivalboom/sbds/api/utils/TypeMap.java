package net.survivalboom.sbds.api.utils;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypeMap implements Map<String, Object> {

    private final List<Class<?>> allowedTypes;

    private final Map<String, Object> map;

    private final boolean allowModification;

    private final Callback callback;

    private TypeMap(@Nullable List<Class<?>> allowedTypes, @NotNull Map<String, Object> map, @Nullable Callback callback, boolean allowModification) {
        this.allowedTypes = allowedTypes;
        this.map = map;
        this.callback = callback;
        this.allowModification = allowModification;
    }

    public @Nullable Object get(@NotNull String name) {
        Objects.requireNonNull(name, "name == null");
        return map.get(name);
    }

    public @Nullable <T> T get(@NotNull String name, @NotNull Class<T> type) {

        Objects.requireNonNull(type, "type == null");

        Object object = get(name);
        if (object == null) return null;

        return type.cast(object);

    }

    public @NotNull <T> T get(@NotNull String name, @NotNull T orElse) {

        Objects.requireNonNull(orElse, "orElse == null");

        Object object = get(name);
        if (object == null) {
            return orElse;
        }

        try {
            return (T) object;
        }

        catch (ClassCastException e) {
            return orElse;
        }

    }

    public @Nullable <T> T getCastOrNull(@NotNull String name, @NotNull Class<T> clazz) {

        try {
            return get(name, clazz);
        }

        catch (ClassCastException ignored) {
            return null;
        }

    }

    public @NotNull <T> T getCastOrDefault(@NotNull String name, @NotNull Class<T> clazz, @NotNull T defaultValue) {
        Objects.requireNonNull(name, "name == null");
        Objects.requireNonNull(clazz, "clazz == null");
        Objects.requireNonNull(defaultValue, "defaultValue == null");

        Object value = get(name);
        if (value == null) return defaultValue;

        try {
            return clazz.cast(value);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    /*
        CALLBACK
     */

    public interface Callback {

        boolean putCallback(@NotNull TypeMap map, @NotNull String key, @Nullable Object value);

    }

    /*
        STATIC
     */

    @SuppressWarnings("unchecked")
    public static @NotNull List<TypeMap> ofMapList(@NotNull List<Map<?, ?>> mapList) {
        return mapList.stream().map(l -> TypeMap.ofMap((Map<String, Object>) l, false)).toList();
    }

    public static @NotNull TypeMap ofMappings(@NotNull List<OptionMapping> mappings) {

        Map<String, Object> map = new ConcurrentHashMap<>();

        for (OptionMapping mapping : mappings) {
            map.put(mapping.getName(), CommonUtils.getValueFromMapping(mapping));
        }

        return new TypeMap(null, map, null, false);

    }

    public static @NotNull TypeMap copyMap(@NotNull Map<String, Object> map, boolean allowModification) {
        Objects.requireNonNull(map, "map == null");
        return new TypeMap(null, new ConcurrentHashMap<>(map), null, allowModification);
    }

    public static @NotNull TypeMap copyMap(@NotNull Map<String, Object> map, @NotNull Callback callback, boolean allowModification) {
        return new TypeMap(null, map, callback, allowModification);
    }

    public static @NotNull TypeMap ofMap(@NotNull Map<String, Object> map, @NotNull Callback callback, boolean allowModification) {
        return new TypeMap(null, map, callback, allowModification);
    }

    public static @NotNull TypeMap ofMap(@NotNull Map<String, Object> map, boolean allowModification) {
        Objects.requireNonNull(map, "map == null");
        return new TypeMap(null, map, null, allowModification);
    }

    public static @NotNull TypeMap empty(boolean allowModification) {
        return new TypeMap(null, new ConcurrentHashMap<>(), null, allowModification);
    }

    public static @NotNull TypeMap empty(@NotNull Callback callback, boolean allowModification) {
        return new TypeMap(null, new ConcurrentHashMap<>(), callback, allowModification);
    }

    public static @NotNull TypeMap empty(@NotNull Callback callback, boolean allowModification, Class<?> @NotNull... allowedValues) {
        return new TypeMap(List.of(allowedValues), new ConcurrentHashMap<>(), callback, allowModification);
    }

    public static @NotNull TypeMap emptyValuesArray(@Nullable Callback callback, boolean allowModification, @Nullable Class<?>[] allowedValues) {

        @SuppressWarnings("NullableProblems")
        List<Class<?>> classes = allowedValues != null ? List.of(allowedValues) : null;

        return new TypeMap(classes, new ConcurrentHashMap<>(), callback, allowModification);

    }


    /*
        MAP
     */

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    @Override
    public Object get(Object o) {
        return map.get(o);
    }

    @Override
    public @Nullable Object put(@NotNull String s, Object o) {

        if (!allowModification) throw new UnsupportedOperationException("Modification of this TypeMap is not allowed");
        Objects.requireNonNull(s, "key == null");

        if (allowedTypes != null && !allowedTypes.contains(o.getClass())) {
            throw new IllegalArgumentException("Unsupported type `" + o.getClass().getName() + "`");
        }

        if (callback != null && !callback.putCallback(this, s, o)) {
            return o;
        }

        return map.put(s, o);

    }

    @Override
    public Object remove(Object o) {
        if (!allowModification) throw new UnsupportedOperationException("Modification of this TypeMap is not allowed");
        return map.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {
        if (!allowModification) throw new UnsupportedOperationException("Modification of this TypeMap is not allowed");
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        if (!allowModification) throw new UnsupportedOperationException("Modification of this TypeMap is not allowed");
        map.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return new HashSet<>(map.keySet());
    }

    @Override
    public @NotNull Collection<Object> values() {
        return new ArrayList<>(map.values());
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        return new HashSet<>(map.entrySet());
    }

    public @NotNull Map<String, Object> map() {
        return new HashMap<>(map);
    }


    @Override
    public String toString() {
        return "TypeMap" + this.map;
    }

}
