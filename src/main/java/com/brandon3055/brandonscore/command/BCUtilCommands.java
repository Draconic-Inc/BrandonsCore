package com.brandon3055.brandonscore.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class AbstractModCommandHandler extends CommandBase {
    @Override
    public String getName() {
        return "bcore";
    }

    

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bcore help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
