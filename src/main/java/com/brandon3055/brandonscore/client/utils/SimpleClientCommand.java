package com.brandon3055.brandonscore.client.utils;

import com.brandon3055.brandonscore.command.ISimpleClientCommand;
import com.brandon3055.brandonscore.lib.ScheduledTask;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

/**
 * Created by brandon3055 on 8/07/2017.
 */
public class SimpleClientCommand extends CommandBase implements IClientCommand {

    private String name;
    private ISimpleClientCommand simpleCommand;

    public SimpleClientCommand(String name, ISimpleClientCommand simpleCommand) {
        this.name = name;
        this.simpleCommand = simpleCommand;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/" + name;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        new ScheduledTask(0) {
            @Override
            public void execute(Object[] a) {
                try {
                    simpleCommand.execute(sender, args);
                }
                catch (CommandException e) {
                    e.printStackTrace();
                    sender.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "An error occurred while executing the command! " + e.getMessage()));
                }
            }
        }.scheduleClient();
    }

    /**
     * Determine whether this command can be used without the "/" prefix. By default this is true.
     *
     * @param sender  the command sender
     * @param message the message, without potential prefix
     * @return true to allow the usage of this command without the prefix
     */
    @Override
    public boolean allowUsageWithoutPrefix(ICommandSender sender, String message) {
        return false;
    }
}
