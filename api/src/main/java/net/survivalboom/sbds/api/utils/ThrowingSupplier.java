package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ThrowingSupplier<T> extends Supplier<T> {

    @Override
    default T get() {

        try {
            return getThrowing();
        }

        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    T getThrowing() throws Exception;



    static @NotNull <V> ThrowingSupplier<V> create(@NotNull ThrowingSupplier<V> supplier) {
        return supplier;
    }

}
