package net.survivalboom.sbds.api.utils.typemap;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class AbstractTypeMap implements TypeMap {

    protected final Map<String, Object> map;


    public AbstractTypeMap(@NotNull Map<String, Object> map) {
        this.map = map;
    }


    @Override
    public @NotNull Map<String, Object> getMap() {
        return map;
    }

    //
    // MAP
    //

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
    public Object put(@NotNull String key, Object object) {
        Objects.requireNonNull(key, "key == null");
        return map.put(key, object);
    }

    @Override
    public Object remove(Object o) {
        return map.remove(o);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> map) {

        if (map.containsKey(null)) {
            throw new NullPointerException("map contains null key");
        }

        this.map.putAll(map);

    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public @NotNull Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public @NotNull Collection<Object> values() {
        return map.values();
    }

    @Override
    public @NotNull Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }


}
