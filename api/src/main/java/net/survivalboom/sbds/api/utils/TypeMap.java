package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TypeMap implements Map<String, Object> {

    private final Map<String, Object> map;

    private final boolean allowModification;

    private TypeMap(@NotNull Map<String, Object> map, boolean allowModification) {
        this.map = map;
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
        return get(name, clazz);
    }

    /*
        STATIC
     */

    public static @NotNull TypeMap copyMap(@NotNull Map<String, Object> map, boolean allowModification) {
        Objects.requireNonNull(map, "map == null");
        return new TypeMap(new ConcurrentHashMap<>(map), allowModification);
    }

    public static @NotNull TypeMap ofMap(@NotNull Map<String, Object> map, boolean allowModification) {
        Objects.requireNonNull(map, "map == null");
        return new TypeMap(map, allowModification);
    }

    public static @NotNull TypeMap empty(boolean allowModification) {
        return new TypeMap(new ConcurrentHashMap<>(), allowModification);
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
    public @Nullable Object put(String s, Object o) {
        if (!allowModification) throw new UnsupportedOperationException("Modification of this TypeMap is not allowed");
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
