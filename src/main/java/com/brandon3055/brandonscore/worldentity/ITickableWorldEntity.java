package com.brandon3055.brandonscore.worldentity;

import net.minecraftforge.event.TickEvent;

/**
 * Created by brandon3055 on 15/12/20
 */
public interface ITickableWorldEntity {
    void tick();

    /**
     * Allows you to choose weather this entity ticks at the start or the end of the world tick. Default is start.
     * @return TickEvent.Phase.START or TickEvent.Phase.END
     */
    default TickEvent.Phase getPhase() {
        return TickEvent.Phase.START;
    }
}
