package net.survivalboom.sbds.api.utils.typemap;

import net.survivalboom.sbds.api.utils.CommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifiableCallbackTypeMap extends ModifiableTypeMap {

    private final Callback callback;


    protected ModifiableCallbackTypeMap(
            @NotNull Map<String, Object> map,
            @Nullable Callback callback,
            boolean allowNull,
            @Nullable Collection<Class<?>> allowedTypes
    ) {
        super(map, allowNull, allowedTypes);
        this.callback = callback;
    }

    @Override
    public Object put(@NotNull String key, Object object) {

        if (callback != null) {
            boolean result = callback.onMapAdd(this, key, object);
            if (!result) {
                return object;
            }
        }

        return super.put(key, object);

    }

    @Override
    public Object remove(Object key) {

        Object obj = get(key);
        if (obj == null) {
            return null;
        }

        if (callback != null) {
            boolean result = callback.onMapRemove(this, (String) key, obj);
            if (!result) {
                return obj;
            }
        }

        this.map.remove(key);

        return obj;

    }

    //
    // CALLBACK
    //

    public interface Callback {

        boolean onMapAdd(@NotNull ModifiableTypeMap map, @NotNull String key, @Nullable Object obj);

        boolean onMapRemove(@NotNull ModifiableTypeMap map, @NotNull String key, @Nullable Object obj);

    }


    //
    // STATIC
    //

    // mutable map //

    public static @NotNull ModifiableCallbackTypeMap ofMap(
            @NotNull Map<String, Object> map,
            @NotNull Callback callback,
            boolean allowNull,
            @Nullable Collection<Class<?>> allowedTypes
    ) {
        return new ModifiableCallbackTypeMap(map, callback, allowNull, allowedTypes);
    }

    public static @NotNull ModifiableCallbackTypeMap ofMap(
            @NotNull Map<String, Object> map,
            @NotNull Callback callback,
            boolean allowNull,
            Class<?>... allowedTypes
    ) {
        return new ModifiableCallbackTypeMap(map, callback, allowNull, List.of(allowedTypes));
    }

    // immutable map //

    public static @NotNull ModifiableCallbackTypeMap copyMap(
            @NotNull Map<String, Object> map,
            @NotNull Callback callback,
            boolean allowNull,
            @Nullable Collection<Class<?>> allowedTypes
    ) {
        Map<String, Object> copy = CommonUtils.deepCopy(map);
        return new ModifiableCallbackTypeMap(copy, callback, allowNull, allowedTypes);
    }

    public static @NotNull ModifiableCallbackTypeMap copyMap(
            @NotNull Map<String, Object> map,
            @NotNull Callback callback,
            boolean allowNull,
            Class<?>... allowedTypes
    ) {
        Map<String, Object> copy = CommonUtils.deepCopy(map);
        return new ModifiableCallbackTypeMap(new HashMap<>(copy), callback, allowNull, List.of(allowedTypes));
    }

    // new fresh map //

    public static @NotNull ModifiableCallbackTypeMap empty(
            @NotNull Callback callback,
            boolean allowNull,
            @Nullable Collection<Class<?>> allowedTypes
    ) {
        return new ModifiableCallbackTypeMap(new HashMap<>(), callback, allowNull, allowedTypes);
    }

    public static @NotNull ModifiableCallbackTypeMap empty(
            @NotNull Callback callback,
            boolean allowNull,
            Class<?>... allowedTypes
    ) {
        return new ModifiableCallbackTypeMap(new HashMap<>(), callback, allowNull, List.of(allowedTypes));
    }

}
