package com.brandon3055.brandonscore.inventory;

import net.minecraft.world.inventory.Slot;

/**
 * Created by brandon3055 on 19/04/2022
 */
public class SlotMover {

    public Slot slot;

    public SlotMover() {}

    public SlotMover(Slot slot) {
        this.slot = slot;
    }

    public void setPos(int xPos, int yPos) {
        slot.x = xPos;
        slot.y = yPos;
    }
}
