package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.utils.BCProfiler;
import net.covers1624.quack.util.CrashLock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is a class used to run processes that implement IProcess.
 * Processes are similar to tile entities except that they are not bound to anything and they are not currently
 * persistent (they will be deleted when the world closes)
 * <p/>
 * Created by brandon3055 on 12/8/2015.
 */

public class ProcessHandlerClient {
    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static List<IProcess> processes = new ArrayList<IProcess>();
    private static List<IProcess> newProcesses = new ArrayList<IProcess>();

    private static List<IProcess> persistentProcesses = new ArrayList<IProcess>();
    private static List<IProcess> newPersistentProcesses = new ArrayList<IProcess>();

    public static void init() {
        LOCK.lock();
        MinecraftForge.EVENT_BUS.register(new ProcessHandlerClient());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            BCProfiler.TICK.start("process_handler");
            while (!syncTasks.isEmpty()) {
                syncTasks.poll().run();
            }

            Iterator<IProcess> i = processes.iterator();
            while (i.hasNext()) {
                IProcess process = i.next();
                if (process.isDead()) {
                    i.remove();
                }
                else {
                    process.updateProcess();
                }
            }

            if (!newProcesses.isEmpty()) {
                processes.addAll(newProcesses);
                newProcesses.clear();
            }


            i = persistentProcesses.iterator();
            while (i.hasNext()) {
                IProcess process = i.next();
                if (process.isDead()) {
                    i.remove();
                }
                else {
                    process.updateProcess();
                }
            }

            if (!newPersistentProcesses.isEmpty()) {
                persistentProcesses.addAll(newPersistentProcesses);
                newPersistentProcesses.clear();
            }
            BCProfiler.TICK.stop();
        }
    }

    @SubscribeEvent
    public void onWorldClose(WorldEvent.Unload event) {
        processes.clear();
        newProcesses.clear();
    }

    public static void addProcess(IProcess process) {
        newProcesses.add(process);
    }

    /**
     * Adds a new process that will not be removed when the world is closed.
     */
    public static void addPersistentProcess(IProcess process) {
        newPersistentProcesses.add(process);
    }

    private static final Queue<Runnable> syncTasks = new ConcurrentLinkedQueue<>();

    public static void syncTask(Runnable task) {
        syncTasks.add(task);
    }
}
