package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.handlers.IProcess;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.network.PacketTickTime;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextComponentString;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 27/07/2016.
 */
public class CommandTickTime extends CommandBase {

    private Map<String, TickSenderProcess> listeners = new HashMap<String, TickSenderProcess>();

    @Override
    public String getCommandName() {
        return "bcore_ticktime";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/bcore_ticktime";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String name = sender.getName();

        if (listeners.containsKey(name)) {
            if (!listeners.get(name).isDead()){
                listeners.get(name).setDead();
                listeners.remove(name);
                sender.addChatMessage(new TextComponentString("Stopped sending tick time to client. (Display will go away after a few seconds)"));
                return;
            }
            else {
                listeners.remove(name);
            }
        }


        PlayerList list = server.getPlayerList();
        TickSenderProcess process = new TickSenderProcess(sender.getName(), list);
        listeners.put(sender.getName(), process);
        ProcessHandler.addProcess(process);
        sender.addChatMessage(new TextComponentString("Started sending tick time to client."));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    private class TickSenderProcess implements IProcess {

        private boolean isDead = false;
        private String username;
        private PlayerList playerList;
        private MinecraftServer server;
        private int timeout = 0;

        public TickSenderProcess(String listener, PlayerList playerList) {
            this.username = listener;
            this.playerList = playerList;
            this.server = playerList.getServerInstance();
        }

        @Override
        public void updateProcess() {
            EntityPlayerMP player = playerList.getPlayerByUsername(username);
            if (player == null) {
                timeout++;
                if (timeout > 100) {
                    setDead();
                }
                return;
            }

            timeout = 0;

            int overallTick = (int)(server.tickTimeArray[server.getTickCounter() % 100] / 10000L);
            Map<Integer, Integer> dimTimes = new HashMap<Integer, Integer>();

            java.util.Hashtable<Integer, long[]> worldTickTimes = server.worldTickTimes;
            for (Integer dim : worldTickTimes.keySet()) {
                dimTimes.put(dim, (int) (worldTickTimes.get(dim)[server.getTickCounter() % 100] / 10000L));
            }

            BrandonsCore.network.sendTo(new PacketTickTime(dimTimes, overallTick), player);
        }

        @Override
        public boolean isDead() {
            return isDead;
        }

        public void setDead() {
            isDead = true;
        }
    }
}
