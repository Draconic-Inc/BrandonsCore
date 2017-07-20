package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;

/**
 * Created by brandon3055 on 7/12/2016.
 */
public abstract class ScheduledTask implements IProcess {

    private int delay;
    private Object[] args;
    private boolean hasExecuted = false;

    public ScheduledTask(int delay, Object... args) {
        this.delay = delay;
        this.args = args;
    }

    public abstract void execute(Object[] args);

    @Override
    public final void updateProcess() {
        if (delay <= 0 && !hasExecuted) {
            execute(args);
            hasExecuted = true;
        }

        delay--;
    }

    @Override
    public boolean isDead() {
        return hasExecuted;
    }

    public void schedule() {
        ProcessHandler.addProcess(this);
    }

    public void scheduleClient() {
        ProcessHandlerClient.addProcess(this);
    }
}
