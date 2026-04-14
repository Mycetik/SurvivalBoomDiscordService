package net.survivalboom.sbds.api.utils.typemap;

import net.survivalboom.sbds.api.utils.CommonUtils;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface TypeMap extends Map<String, Object> {

    default @NotNull Optional<?> getOptional(@NotNull String key) {
        Objects.requireNonNull(key, "key == null");
        return Optional.ofNullable(get(key));
    }

    default <T> @NotNull Optional<T> getCast(@NotNull String key, @NotNull Class<T> cast) {

        Objects.requireNonNull(key, "key == null");
        
        var obj = get(key);
        if (obj == null) {
            return Optional.empty();
        }

        T t;
        try {
            t = cast.cast(obj);
        }

        catch (ClassCastException e) {
            return Optional.empty();
        }

        return Optional.of(t);

    }

    @SuppressWarnings("unchecked")
    default <T> @NotNull Optional<List<T>> getCastList(@NotNull String section, @NotNull Class<T> cast) {

        Objects.requireNonNull(section, "section == null");

        Object undefined = getMap().get(section);
        if (undefined == null) {
            return Optional.empty();
        }

        List<T> list;
        try {
            list = (List<T>) undefined;
        }

        catch (ClassCastException e) {
            return Optional.empty();
        }

        return Optional.of(list);

    }

    default @NotNull Optional<TypeMap> getSection(@NotNull String key) {

        Map<String, Object> map = new HashMap<>();
        for (var entry : this.getMap().entrySet()) {

            if (!entry.getKey().startsWith(key)) {
                continue;
            }

            map.put(entry.getKey(), entry.getValue());

        }

        if (map.isEmpty()) {
            return Optional.empty();
        }

        TypeMap typeMap = UnmodifiableTypeMap.ofMap(map);

        return Optional.of(typeMap);

    }

    default @NotNull Optional<List<TypeMap>> getSectionList(@NotNull String key) {

        TypeMap section = getSection(key).orElse(null);
        if (section == null) {
            return Optional.empty();
        }

        List<TypeMap> out = new ArrayList<>();

        JSONObject

    }


    @NotNull Map<String, Object> getMap();


    //
    // STATIC
    //

    static @NotNull TypeMap ofMap(@NotNull Map<String, Object> map, @NotNull Function<Map<String, Object>, TypeMap> function) {
        Map<String, Object> newMap = CommonUtils.compressDeepMap(map);
        return function.apply(newMap);
    }

    static @NotNull List<TypeMap> ofMapList(@NotNull Collection<Map<String, Object>> collection, @NotNull Function<Map<String, Object>, TypeMap> function) {
        return collection.stream().map(function).collect(Collectors.toList());
    }

    // SECTION //

    static @NotNull TypeMap ofSection(@NotNull ConfigurationSection section, @NotNull Function<Map<String, Object>, TypeMap> function) {
        Map<String, Object> map = mapFromSection(section, function);
        return function.apply(map);
    }

    @SuppressWarnings("unchecked")
    private static @NotNull Map<String, Object> mapFromSection(@NotNull ConfigurationSection section, @NotNull Function<Map<String, Object>, TypeMap> function) {

        Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {

            ConfigurationSection sect = section.getConfigurationSection(key);
            if (sect != null) {
                map.put(key, mapFromSection(sect, function));
                continue;
            }

            var m = section.getMapList(key);
            if (!m.isEmpty()) {

                for (var mm : m) {
                    map.put(key, function.apply((Map<String, Object>) mm));
                }

                continue;

            }

            Object obj = section.get(key);
            if (obj == null) {
                continue;
            }

            map.put(key, obj);

        }

        return map;

    }

}
