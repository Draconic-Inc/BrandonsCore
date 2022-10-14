package com.brandon3055.brandonscore.inventory;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 13/10/2022
 */
public interface SlotDisableable {

    void setEnabled(Supplier<Boolean> enabled);

    default void setEnabled(boolean enabled) {
        setEnabled(() -> enabled);
    }
}
