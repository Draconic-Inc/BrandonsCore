package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.blocks.TileBCore;

import java.util.function.Predicate;

/**
 * Created by brandon3055 on 31/10/19.
 *
 * All functionality for this interface is already built into {@link com.brandon3055.brandonscore.blocks.TileBCore}
 * This means all you have to do is implement this on a tile that extends TileBCore.
 * you can then use {@link TileBCore#isTileEnabled()} to check if the tile is currently allowed to run.
 */
public interface IRSSwitchable extends IChangeListener {

    RSMode getRSMode();

    void setRSMode(RSMode mode);

    enum RSMode {
        ALWAYS_ACTIVE(0, signal -> true),
        ACTIVE_HIGH(1, signal -> signal),
        ACTIVE_LOW(2, signal -> !signal),
        NEVER_ACTIVE(3, signal -> false);

        public int index;
        private Predicate<Boolean> canRun;

        RSMode(int index, Predicate<Boolean> canRun) {
            this.index = index;
            this.canRun = canRun;
        }

        public RSMode next(boolean prev) {
            if (prev) {
                return values()[index - 1 < 0 ? values().length - 1 : index - 1];
            }
            return values()[index + 1 == values().length ? 0 : index + 1];
        }

        public boolean canRun(boolean signal) {
            return canRun.test(signal);
        }
    }

}
