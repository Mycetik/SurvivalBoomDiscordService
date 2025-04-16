package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NamespacedContainer {

    private final Class<?>[] allowedTypes;

    private final Callback callback;

    private final Map<NamespacedKey, TypeMap> namespacedMap;

    private NamespacedContainer(@NotNull Map<NamespacedKey, TypeMap> map, @Nullable Callback callback, @Nullable Class<?>[] allowedTypes) {
        this.namespacedMap = map;
        this.allowedTypes = allowedTypes;
        this.callback = callback;
    }

    public @NotNull TypeMap create(@NotNull NamespacedKey key) {

        Objects.requireNonNull(key, "key == null");

        if (namespacedMap.containsKey(key)) throw new IllegalStateException("NamespacedContainer already has a TypeMap for `" + key + "`");

        TypeMap typeMap = createTypeMap(key);

        namespacedMap.put(key, typeMap);

        return typeMap;

    }

    public @Nullable TypeMap get(@NotNull String keyRaw) {
        Objects.requireNonNull(keyRaw, "key == null");
        return get(NamespacedKey.fromString(keyRaw));
    }

    public @Nullable TypeMap get(@NotNull NamespacedKey key) {
        Objects.requireNonNull(key, "key == null");
        return namespacedMap.get(key);
    }

    public @NotNull TypeMap getOrCreate(@NotNull NamespacedKey key) {
        Objects.requireNonNull(key, "key == null");
        if (namespacedMap.containsKey(key)) return namespacedMap.get(key);
        return create(key);
    }


    public @Nullable Map<NamespacedKey, TypeMap> map() {
        return new HashMap<>(namespacedMap);
    }


    private @NotNull TypeMap createTypeMap(@NotNull NamespacedKey namespacedKey) {
        TypeMap.Callback typeMapCallback = callback == null ? null : (map, key, value) -> callback.putCallback(namespacedKey, map, key, value);
        return TypeMap.emptyValuesArray(typeMapCallback, true, allowedTypes);
    }



    public interface Callback {

        boolean putCallback(@NotNull NamespacedKey namespace, @NotNull TypeMap map, @NotNull String key, @Nullable Object value);

    }


    public static @NotNull NamespacedContainer empty(@Nullable Callback callback, @Nullable Class<?> @NotNull... allowedTypes) {
        return new NamespacedContainer(new ConcurrentHashMap<>(), callback, allowedTypes);
    }

    public static @NotNull NamespacedContainer empty() {
        return new NamespacedContainer(new ConcurrentHashMap<>(), null, null);
    }


    @Override
    public String toString() {
        return "NamespacedContainer{" + namespacedMap + "}";
    }

}
