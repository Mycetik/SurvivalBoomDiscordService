package net.survivalboom.sbds.api.utils.typemap;

import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;

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

    default @NotNull Optional<TypeMap> getTypeMap(@NotNull String key) {

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

}
