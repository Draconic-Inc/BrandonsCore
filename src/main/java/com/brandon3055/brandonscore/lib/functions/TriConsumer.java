package com.brandon3055.brandonscore.lib.functions;

@FunctionalInterface
public interface TriConsumer<T, U, V> {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the second input argument
     */
    void accept(T t, U u, V v);
}