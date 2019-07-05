package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.lib.TeleportUtils;
import net.minecraft.command.*;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This command has all of the functionality of the vanilla tp command but with the ability
 * to teleport across dimensions,
 * <p>
 * Created by brandon3055 on 23/12/2017.
 */
public class CommandTPX extends CommandBase
{
    @Override
    public String getName()
    {
        return "tpx";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "bc.commands.tpx.usage";
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    // All possible argument combinations
    // /forge tpx <destination player>                                      1
    // /forge tpx <destination dimension>                                   1
    // /forge tpx [target player] <destination player>                      2
    // /forge tpx [target player] <destination dimension>                   2
    // /forge tpx <x> <y> <z>                                               3
    // /forge tpx <x> <y> <z> [dimension]                                   4
    // /forge tpx [target player] <x> <y> <z>                               4
    // /forge tpx [target player] <x> <y> <z> [dimension]                   5
    // /forge tpx [target player] <x> <y> <z> [dimension] [<yaw> <pitch>]   7

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("bc.commands.tpx.usage");
        }
        else
        {
            Entity entity;
            boolean isFirstPlayer = false;
            try
            {
                getEntity(server, sender, args[0]);
                isFirstPlayer = true;
            }
            catch (EntityNotFoundException ignored) {}

            if (args.length == 1 || args.length == 3 || (args.length == 4 && !isFirstPlayer))
            {
                entity = getCommandSenderAsPlayer(sender);
            }
            else
            {
                entity = getEntity(server, sender, args[0]);
            }

            if (args.length == 1 || args.length == 2)
            {
                String t = args.length == 2 ? args[1] : args[0];
                try
                {
                    Entity target = getEntity(server, sender, t);
                    TeleportUtils.teleportEntity(entity, target.dimension, target.posX, target.posY, target.posZ);
                    notifyCommandListener(sender, this, "commands.tp.success", entity.getName(), target.getName());
                    return;
                }
                catch (EntityNotFoundException ignored) {}

                try
                {
                    int targetDim = parseInt(t);
                    if (!DimensionManager.isDimensionRegistered(targetDim))
                    {
                        throw new WrongUsageException("bc.commands.tpx.invalid_dim");
                    }
                    TeleportUtils.teleportEntity(entity, targetDim, entity.posX, entity.posY, entity.posZ);
                    notifyCommandListener(sender, this, "bc.commands.tpx.success.dim", entity.getName(), targetDim);
                    return;
                }
                catch (NumberInvalidException ignored) {}

                throw new CommandException("bc.commands.tpx.player_or_dim_not_found", t);
            }
            else if (args.length >= 4 || !isFirstPlayer)
            {
                int i = isFirstPlayer ? 1 : 0;
                CommandBase.CoordinateArg xArg = parseCoordinate(entity.posX, args[i++], true);
                CommandBase.CoordinateArg yArg = parseCoordinate(entity.posY, args[i++], -4096, 4096, false);
                CommandBase.CoordinateArg zArg = parseCoordinate(entity.posZ, args[i++], true);
                int dim = entity.dimension;
                if (args.length > i)
                {
                    String arg = args[i++];
                    if (!arg.equals("~"))
                    {
                        dim = parseInt(arg);
                    }
                }
                CommandBase.CoordinateArg yawArg = parseCoordinate((double) entity.rotationYaw, args.length > i ? args[i++] : "~", false);
                CommandBase.CoordinateArg pitchArg = parseCoordinate((double) entity.rotationPitch, args.length > i ? args[i] : "~", false);
                TeleportUtils.teleportEntity(entity, dim, xArg.getResult(), yArg.getResult(), zArg.getResult(), (float) yawArg.getResult(), (float) pitchArg.getResult());
                notifyCommandListener(sender, this, "bc.commands.tpx.success.coordinates", entity.getName(), xArg.getResult(), yArg.getResult(), zArg.getResult(), dim);
            }
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos)
    {
        if (args.length == 1 || args.length == 2)
        {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}