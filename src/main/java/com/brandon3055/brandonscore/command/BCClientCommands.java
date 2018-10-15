package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.gui.config.GuiIncompatibleConfig;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiTest;
import com.brandon3055.brandonscore.client.particle.BCEffectHandler;
import com.brandon3055.brandonscore.handlers.HandHelper;
import com.brandon3055.brandonscore.lib.ChatHelper;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.BCProfiler;
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
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
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
            else if (function.equals("testui")) {
                DelayedTask.run(10, () -> Minecraft.getMinecraft().displayGuiScreen(new ModularGuiTest()));
            }
            else if (function.equals("profiler")) {
                BCProfiler.enableProfiler = !BCProfiler.enableProfiler;
            }
            else if (function.equals("dump_event_listeners")) {
                BCUtilCommands.dumpEventListeners(sender);
            }
            else if (function.equals("set_ui_scale")) {
                setUiScale(server, sender, args);
            }
            else if (function.equals("clear_fx")) {
                BCEffectHandler.effectRenderer.clear();
            }
            else {
//                help(sender);
            }

        }
        catch (Throwable e) {
            e.printStackTrace();
            throw new CommandException(e.getMessage());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return getListOfStringsMatchingLastWord(args, "nbt", "profiler", "dump_event_listeners", "set_ui_scale", "clear_fx");
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
        EntityPlayer player = (EntityPlayer) sender;
        ItemStack stack = HandHelper.getMainFirst(player);
        if (stack.isEmpty()) {
            throw new CommandException("You are not holding an item!");
        } else if (!stack.hasTagCompound()) {
            throw new CommandException("That stack has no NBT tag!");
        }

        NBTTagCompound compound = stack.getTagCompound();
        LogHelperBC.logNBT(compound);

        String s = compound+"";
        if (args.length == 2) {
            s = new StackReference(stack).toString();
        }

        LogHelperBC.info(s);
        StringBuilder builder = new StringBuilder();
        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
        String[] lines = builder.toString().split("\n");
        DataUtils.forEach(lines, st -> ChatHelper.message(sender, st, TextFormatting.GOLD));

        if (!StringUtils.isEmpty(compound+"") && !BrandonsCore.proxy.isDedicatedServer())
        {
            try
            {
                StringSelection stringselection = new StringSelection(s);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
                ChatHelper.message(sender, "NBT Copied to clipboard!", TextFormatting.GREEN);
            }
            catch (Exception ignored) {}
        }
    }

    private void setUiScale(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString("Usage: /bcore_client set_ui_scale <0 to 8>"));
            return;
        }

        Minecraft.getMinecraft().gameSettings.guiScale = parseInt(args[1], 0, 8);
        Minecraft.getMinecraft().gameSettings.saveOptions();
        sender.sendMessage(new TextComponentString("Gui Scale Updated!"));
    }

    private void randomFunction5(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
    }
}
