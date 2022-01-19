package org.neo4j.mapper.core.support;

import java.util.function.Supplier;

/**
 * Tiny wrapper around supplier.
 * @param <T>
 */
public class Lazy<T> {

    private final Supplier<? extends T> supplier;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new Lazy<T>(supplier);
    }

    public T get() {
        return supplier.get();
    }

}
