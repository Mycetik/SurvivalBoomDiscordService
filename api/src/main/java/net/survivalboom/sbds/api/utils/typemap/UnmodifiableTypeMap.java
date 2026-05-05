package net.survivalboom.sbds.api.utils.typemap;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class UnmodifiableTypeMap extends AbstractTypeMap {

    public UnmodifiableTypeMap(@NotNull Map<String, Object> map) {
        super(Map.copyOf(map));
    }

    @Override
    public @NotNull Object put(@NotNull String string, @NotNull Object object) {
        throw new IllegalStateException("This is an UnmodifiableTypeMap!");
    }

    @Override
    public Object remove(Object o) {
        throw new IllegalStateException("This is an UnmodifiableTypeMap!");
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {
        throw new IllegalStateException("This is an UnmodifiableTypeMap!");
    }

    @Override
    public void clear() {
        throw new IllegalStateException("This is an UnmodifiableTypeMap!");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(Object o) {

        Object obj = super.get(o);

        try {
            Map<String, Object> map = (Map<String, Object>) obj;
            return UnmodifiableTypeMap.ofMap(map);
        }

        catch (ClassCastException ignored) {}

        return obj;

    }

    //
    // STATIC
    //

    public static @NotNull UnmodifiableTypeMap ofMap(@NotNull Map<String, Object> map) {
        return new UnmodifiableTypeMap(map);
    }

    public static final @NotNull UnmodifiableTypeMap EMPTY = new UnmodifiableTypeMap(new HashMap<>());


}
