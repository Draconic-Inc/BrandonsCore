package com.brandon3055.brandonscore.lib;

/**
 * Created by brandon3055 on 18/9/19.
 *
 * Not sure if 'hash' is the correct word...
 * But this interface is meant for situations where you need to know if some value in a data object changes.
 * Implement this on the data object's class then use getValueHash to return an object that represents the current value(s)
 * stored in the data object.
 */
public interface IValueHashable<HASH_TYPE> {

    /**
     * @return a new object instance that represents the value(s) stored in this data object.
     */
    HASH_TYPE getValueHash();

    /**
     * Should return true if the given value hash matches the data stored in this data object.
     */
    boolean checkValueHash(Object valueHash);
}
