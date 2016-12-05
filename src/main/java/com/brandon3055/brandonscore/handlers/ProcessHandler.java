package com.brandon3055.brandonscore.handlers;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is a class used to run processes that implement IProcess.
 * Processes are similar to tile entities except that they are not bound to anything and they are not currently
 * persistent (they will be deleted when the world closes)
 * <p/>
 * Created by brandon3055 on 12/8/2015.
 */

public class ProcessHandler {

    private static List<IProcess> processes = new ArrayList<IProcess>();
    private static List<IProcess> newProcesses = new ArrayList<IProcess>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new ProcessHandler());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
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
        }
    }

//    @SubscribeEvent
//    public void onWorldClose(WorldEvent.Unload event) {
//        if (event.getWorld().isRemote) {
//            return;
//        }
//        processes.clear();
//        newProcesses.clear();
//        BCLogHelper.info("Clear Handler " + event.getWorld());
//    }

    public static void clearHandler() {
        processes.clear();
        newProcesses.clear();
    }

    public static void addProcess(IProcess process) {
        newProcesses.add(process);
    }

}
