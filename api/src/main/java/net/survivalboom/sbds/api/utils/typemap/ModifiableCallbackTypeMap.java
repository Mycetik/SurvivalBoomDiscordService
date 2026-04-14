package net.survivalboom.sbds.api.utils.typemap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class ModifiableCallbackTypeMap extends ModifiableTypeMap {

    private final Callback callback;


    public ModifiableCallbackTypeMap(
            @NotNull Map<String, Object> map,
            boolean allowNull,
            @Nullable Callback callback,
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

}
