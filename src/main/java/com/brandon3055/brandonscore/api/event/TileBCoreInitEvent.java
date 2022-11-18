package com.brandon3055.brandonscore.api.event;

import com.brandon3055.brandonscore.blocks.TileBCore;
import net.minecraftforge.eventbus.api.Event;

public class TileBCoreInitEvent extends Event {

    private final TileBCore tile;

    public TileBCoreInitEvent(TileBCore tile) {
        this.tile = tile;
    }

    public TileBCore getTile() {
        return tile;
    }
}