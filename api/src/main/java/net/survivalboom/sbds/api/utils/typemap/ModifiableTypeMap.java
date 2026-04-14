package net.survivalboom.sbds.api.utils.typemap;

import net.survivalboom.sbds.api.utils.CommonUtils;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ModifiableTypeMap extends AbstractTypeMap {

    private final boolean allowNull;

    @Nullable
    private final List<Class<?>> allowedTypes;

    public ModifiableTypeMap(
            @NotNull Map<String, Object> map,
            boolean allowNull,
            @Nullable Collection<Class<?>> allowedTypes
    ) {
        super(map);

        this.allowNull = allowNull;

        this.allowedTypes = allowedTypes != null ? new ArrayList<>(allowedTypes) : null;

    }

    @Override
    public Object put(@NotNull String key, Object object) {

        Objects.requireNonNull(key, "key == null");

        if (!allowNull && object != null) {
            throw new NullPointerException();
        }

        if (object != null && allowedTypes != null && !allowedTypes.contains(object.getClass())) {
            throw new IllegalArgumentException("Object `" + object.getClass().getName() + "` is not allowed in this TypeMap");
        }

        this.map.put(key, object);

        return object;

    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {

        if (map.containsKey(null)) {
            throw new NullPointerException("map contains null key");
        }

        for (var entry : new ArrayList<>(map.entrySet())) {
            put(entry.getKey(), entry.getValue());
        }

    }

    @Override
    public void clear() {

        for (var entry : new ArrayList<>(map.entrySet())) {
            remove(entry.getKey());
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(Object o) {

        Object obj = super.get(o);

        try {
            Map<String, Object> map = (Map<String, Object>) obj;
            return ModifiableTypeMap.ofMap(map, allowNull, allowedTypes);
        }

        catch (ClassCastException ignored) {}

        return obj;

    }


    //
    // STATIC
    //

    // mutable map //

    public static @NotNull ModifiableTypeMap ofMap(@NotNull Map<String, Object> map,  boolean allowNull, @Nullable Collection<Class<?>> allowedTypes) {
        return new ModifiableTypeMap(map, allowNull, allowedTypes);
    }

    public static @NotNull ModifiableTypeMap ofMap(@NotNull Map<String, Object> map, boolean allowNull, Class<?>... allowedTypes) {
        return new ModifiableTypeMap(map, allowNull, List.of(allowedTypes));
    }

    // immutable map //

    public static @NotNull ModifiableTypeMap copyMap(@NotNull Map<String, Object> map, boolean allowNull, @Nullable Collection<Class<?>> allowedTypes) {
        Map<String, Object> copy = CommonUtils.deepCopy(map);
        return new ModifiableTypeMap(copy, allowNull, allowedTypes);
    }

    public static @NotNull ModifiableTypeMap copyMap(@NotNull Map<String, Object> map, boolean allowNull, Class<?>... allowedTypes) {
        Map<String, Object> copy = CommonUtils.deepCopy(map);
        return new ModifiableTypeMap(new HashMap<>(copy), allowNull, List.of(allowedTypes));
    }

    // new fresh map //

    public static @NotNull ModifiableTypeMap empty(boolean allowNull, @Nullable Collection<Class<?>> allowedTypes) {
        return new ModifiableTypeMap(new HashMap<>(), allowNull, allowedTypes);
    }

    public static @NotNull ModifiableTypeMap empty(boolean allowNull, Class<?>... allowedTypes) {
        return new ModifiableTypeMap(new HashMap<>(), allowNull, List.of(allowedTypes));
    }

    // of configuration section //

    public static @NotNull ModifiableTypeMap fromSection(@NotNull ConfigurationSection section, boolean allowNull) {
        var map = CommonUtils.mapFromSection(section);
        return ofMap(map, allowNull);
    }

}
