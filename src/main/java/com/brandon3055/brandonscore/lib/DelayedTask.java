package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.ProcessHandlerClient;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Created by brandon3055 on 4/01/2018.
 */
public class DelayedTask {

    @Deprecated //There are issues with then when for example i need to do server world stuff in single player. May need to switch to using effective side.
    public static void run(int delay, Runnable task) {
        BrandonsCore.proxy.runSidedProcess(new Task(delay, task));
    }

    public static void sided(int delay, Runnable task) {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ProcessHandlerClient.addProcess(new Task(delay, task)));
        DistExecutor.safeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> ProcessHandler.addProcess(new Task(delay, task)));
    }

    public static void server(int delay, Runnable task) {
        ProcessHandler.addProcess(new Task(delay, task));
    }

    public static void client(int delay, Runnable task) {
        ProcessHandlerClient.addProcess(new Task(delay, task));
    }


    public static class Task implements IProcess {
        private int delay;
        private Runnable task;
        private boolean hasExecuted = false;

        public Task(int delay, Runnable task) {
            this.delay = delay;
            this.task = task;
        }

        @Override
        public final void updateProcess() {
            if (delay <= 0 && !hasExecuted) {
                task.run();
                hasExecuted = true;
            }
            delay--;
        }

        @Override
        public boolean isDead() {
            return hasExecuted;
        }
    }

}
