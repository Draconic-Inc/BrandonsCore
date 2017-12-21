package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.client.gui.config.GuiIncompatibleConfig;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class BCClientCommands extends CommandBase {

    @Override
    public String getName() {
        return "bcore_client";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/bcore_client help";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender = Minecraft.getMinecraft().player;
        if (args.length == 0) {
            help(sender);
            return;
        }

        try {

            String function = args[0];

            if (function.equals("config_sync_gui")) {
                configSync(server, sender, args);
            }
            else if (function.equals("nbt")) {
                functionNBT(server, sender, args);
            }
            else {
                help(sender);
            }

        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return new ArrayList<>();//getListOfStringsMatchingLastWord(args, "nbt", "regenchunk");
    }

    private void help(ICommandSender sender) {
        ChatHelper.message(sender, "NO!", TextFormatting.RED);
//        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_util", TextFormatting.BLUE);
//        ChatHelper.message(sender, "-", TextFormatting.GRAY);
    }

    private void configSync(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!ModConfigParser.propsRequireRestart.isEmpty()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiIncompatibleConfig(ModConfigParser.propsRequireRestart));
        }
    }

    private void functionNBT(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = Minecraft.getMinecraft().player;
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandException("You are not holding an item!");
        }
        else if (!stack.hasTagCompound()) {
            throw new CommandException("That stack has no NBT tag!");
        }

        NBTTagCompound compound = stack.getTagCompound();
        LogHelperBC.logNBT(compound);
        StringBuilder builder = new StringBuilder();
        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
        String[] lines = builder.toString().split("\n");
        DataUtils.forEach(lines, s -> ChatHelper.message(sender, s, TextFormatting.GOLD));
    }

    private void randomFunction4(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }

    private void randomFunction5(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }
}
