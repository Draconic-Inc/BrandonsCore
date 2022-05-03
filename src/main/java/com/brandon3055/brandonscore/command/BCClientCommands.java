package com.brandon3055.brandonscore.command;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiToolkitTest;
import com.brandon3055.brandonscore.client.gui.modulargui.ModularGuiTest;
import com.brandon3055.brandonscore.lib.DelayedTask;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

/**
 * Created by brandon3055 on 23/06/2017.
 */
public class BCClientCommands {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("bcore_client")
                        .then(testGui())
//                        .then(registerStackString())
//                        .then(registerRegenChunk())
//                        .then(registerNoClip())
//                        .then(registerUUID())
//                        .then(registerPlayerAccess())
//                        .then(registerDumpEvents())
//                        .then(registerEggify())
        );
    }


    private static ArgumentBuilder<CommandSourceStack, ?> testGui() {
        return Commands.literal("testui")
                .requires(cs -> cs.hasPermission(0))
                .then(Commands.literal("mgui")
                        .executes(context -> {
                            DelayedTask.run(10, () -> Minecraft.getInstance().setScreen(new ModularGuiTest(new TextComponent("Test"))));
                            return 0;
                        }))
                .then(Commands.literal("toolkit")
                        .executes(context -> {
                            DelayedTask.run(10, () -> Minecraft.getInstance().setScreen(new GuiToolkitTest(new TextComponent("Test"))));
                            return 0;
                        }));
    }



//    private static Map<String, PairKV<Integer, Integer>> RESOLUTIONS = new HashMap<>();
//
//    static {
//        RESOLUTIONS.put("240p", new PairKV<>(352, 240));
//        RESOLUTIONS.put("360p", new PairKV<>(480, 360));
//        RESOLUTIONS.put("480p", new PairKV<>(858, 480));
//        RESOLUTIONS.put("576p", new PairKV<>(1024, 576));
//        RESOLUTIONS.put("648p", new PairKV<>(1152, 648));
//        RESOLUTIONS.put("720p", new PairKV<>(1280, 720));
//        RESOLUTIONS.put("768p", new PairKV<>(1366, 768));
//        RESOLUTIONS.put("900p", new PairKV<>(1600, 900));
//        RESOLUTIONS.put("1080p", new PairKV<>(1920, 1080));
//        RESOLUTIONS.put("1440p", new PairKV<>(2560, 1440));
//        RESOLUTIONS.put("4k", new PairKV<>(3840, 2160));
//        RESOLUTIONS.put("8k", new PairKV<>(7680, 4320));
//    }
//
//    @Override
//    public String getName() {
//        return "bcore_client";
//    }
//
//    @Override
//    public String getUsage(ICommandSource sender) {
//        return "/bcore_client help";
//    }
//
//    @Override
//    public int getRequiredPermissionLevel() {
//        return 0;
//    }
//
//    @Override
//    public void execute(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
//        sender = Minecraft.getInstance().player;
//        if (args.length == 0) {
//            help(sender);
//            return;
//        }
//
//        try {
//            String function = args[0];
//
//            if (function.equals("config_sync_gui")) {
//                configSync(server, sender, args);
//            }
//            else if (function.equals("nbt")) {
//                functionNBT(server, sender, args);
//            }
//            else if (function.equals("testui")) {
//
//            }
//            else if (function.equals("testui2")) {
//
//            }
//            else if (function.equals("profiler")) {
//                BCProfiler.enableProfiler = !BCProfiler.enableProfiler;
//            }
//            else if (function.equals("dump_event_listeners")) {
//                BCUtilCommands.dumpEventListeners(sender);
//            }
//            else if (function.equals("set_ui_scale")) {
//                setUiScale(server, sender, args);
//            }
//            else if (function.equals("set_ui_size")) {
//                setUISize(server, sender, args);
//            }
//            else if (function.equals("clear_fx")) {
//                BCEffectHandler.effectRenderer.clear();
//            }
//            else {
//                help(sender);
//            }
//
//        }
//        catch (Throwable e) {
//            e.printStackTrace();
//            throw new CommandException(e.getMessage());
//        }
//    }
//
//    @Override
//    public List<String> getTabCompletions(MinecraftServer server, ICommandSource sender, String[] args, @Nullable BlockPos targetPos) {
//        if (args.length == 2 && args[0].equals("set_ui_size")) {
//            return getListOfStringsMatchingLastWord(args, RESOLUTIONS.keySet());
//        }
//
//        return getListOfStringsMatchingLastWord(args, "nbt", "profiler", "dump_event_listeners", "set_ui_scale", "clear_fx", "set_ui_size");
//    }
//
//    private void help(ICommandSource sender) {
////        ChatHelper.message(sender, "NO!", TextFormatting.RED);
//        ChatHelper.message(sender, "/bcore_client nbt", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_client dump_event_listeners", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_client set_ui_scale", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_client set_ui_size", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//        ChatHelper.message(sender, "/bcore_client clear_fx", TextFormatting.BLUE);
////        ChatHelper.message(sender, "-", TextFormatting.GRAY);
//    }
//
//    private void configSync(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
//        if (!ModConfigParser.propsRequireRestart.isEmpty()) {
//            Minecraft.getInstance().displayGuiScreen(new GuiIncompatibleConfig(ModConfigParser.propsRequireRestart));
//        }
//    }
//
//    private void functionNBT(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
//        PlayerEntity player = (PlayerEntity) sender;
//        ItemStack stack = HandHelper.getMainFirst(player);
//        if (stack.isEmpty()) {
//            throw new CommandException("You are not holding an item!");
//        }
//        else if (!stack.hasTagCompound()) {
//            throw new CommandException("That stack has no NBT tag!");
//        }
//
//        CompoundNBT compound = stack.getTag();
//        LogHelperBC.logNBT(compound);
//
//        String s = compound + "";
//        if (args.length == 2) {
//            s = new StackReference(stack).toString();
//        }
//
//        LogHelperBC.info(s);
//        StringBuilder builder = new StringBuilder();
//        LogHelperBC.buildNBT(builder, compound, "", "Tag", false);
//        String[] lines = builder.toString().split("\n");
//        DataUtils.forEach(lines, st -> ChatHelper.message(sender, st, TextFormatting.GOLD));
//
//        if (!StringUtils.isEmpty(compound + "") && !BrandonsCore.proxy.isDedicatedServer()) {
//            try {
//                StringSelection stringselection = new StringSelection(s);
//                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
//                ChatHelper.message(sender, "NBT Copied to clipboard!", TextFormatting.GREEN);
//            }
//            catch (Exception ignored) {
//            }
//        }
//    }
//
//    private void setUiScale(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
//        if (args.length < 2) {
//            sender.sendMessage(new StringTextComponent("Usage: /bcore_client set_ui_scale <0 to 8>"));
//            return;
//        }
//
//        Minecraft.getInstance().gameSettings.guiScale = parseInt(args[1], 0, 8);
//        Minecraft.getInstance().gameSettings.saveOptions();
//        sender.sendMessage(new StringTextComponent("Gui Scale Updated!"));
//    }
//
//    //Sometimes i just have too much free time on my hands xD
//    private void setUISize(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
//        if ((args.length > 3 || args.length < 2) && !RESOLUTIONS.containsKey(args[1])) {
//            sender.sendMessage(new StringTextComponent("Usage: /set_ui_size (<width> <height> or <res name>)"));
//            return;
//        }
//
//        int width;
//        int height;
//
//        if (args.length == 2) {
//            width = RESOLUTIONS.get(args[1]).getKey();
//            height = RESOLUTIONS.get(args[1]).getValue();
//        }
//        else {
//            width = parseInt(args[1], 64, 7680);
//            height = parseInt(args[2], 64, 4320);
//        }
//
//
//        Minecraft mc = Minecraft.getInstance();
//        mc.resize(width, height);
//        try {
//            Display.setDisplayMode(new DisplayMode(mc.displayWidth, mc.displayHeight));
//        }
//        catch (LWJGLException e) {
//            e.printStackTrace();
//            throw new CommandException(e.getMessage());
//        }
//    }
}