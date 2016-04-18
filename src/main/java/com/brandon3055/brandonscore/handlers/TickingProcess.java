package com.brandon3055.brandonscore.handlers;

/**
 * Created by brandon3055 on 14/4/2016.
 */
public class TickingProcess implements IProcess {
    public int tick = 0;

    @Override
    public void updateProcess() {
        tick++;
    }

    @Override
    public boolean isDead() {
        return false;
    }
}
