package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.api.IFOVModifierItem;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class BCClientEventHandler {

    private static int remountTicksRemaining = 0;
    private static int remountEntityID = 0;
    private static int debugTimeout = 0;
    private static Map<ResourceKey<Level>, Integer[]> dimTickTimes = new HashMap<>();
    private static Integer[] overallTickTime = new Integer[200];
    private static int renderIndex = 0;
    private static LinkedList<ResourceKey<Level>> sortingOrder = new LinkedList<>();
    public static int elapsedTicks = 0;

    //region sorter

    private static Comparator<ResourceKey<Level>> sorter = (value, compare) -> {
        long totalValue = 0;
        for (Integer time : dimTickTimes.get(value)) {
            totalValue += time;
        }
        totalValue /= 200;

        long totalCompare = 0;
        for (Integer time : dimTickTimes.get(compare)) {
            totalCompare += time;
        }
        totalCompare /= 200;

        return Long.compare(totalCompare, totalValue);
    };

    //endregion

    //region Event Hooks

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        elapsedTicks++;
        if (Minecraft.getInstance().isPaused()) {
            return;
        }

        if (debugTimeout > 0) {
            debugTimeout--;
        }

        if (elapsedTicks % 100 == 0 && debugTimeout > 0) {
            sortingOrder.clear();
            sortingOrder.addAll(dimTickTimes.keySet());
            sortingOrder.sort(sorter);
        }
    }

    @SubscribeEvent
    public void joinWorld(EntityJoinWorldEvent event) {
//        if (event.getEntity() instanceof ServerPlayerEntity) {
//TODO            BrandonsCore.network.sendToServer(new PacketUpdateMount(0));
//        }
    }

    @SubscribeEvent
    public void renderScreen(RenderGameOverlayEvent.Post event) {
//        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || debugTimeout <= 0 ||  Minecraft.getInstance().screen instanceof ChatScreen) {
//            return;
//        }
//
//        GlStateManager._pushMatrix();
//        GlStateManager._translated(0, 0, 600);
//
//        renderGraph(event.getMatrixStack(), 220, 0, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), overallTickTime, "Overall");
//
//        int i = 0;
//        for (ResourceKey<Level> dim : sortingOrder) {
//            if (dimTickTimes.get(dim) == null || dim == null) {
//                continue;
//            }
//
//            renderGraph(event.getMatrixStack(), 0, i, event.getWindow().getGuiScaledWidth(), event.getWindow().getGuiScaledHeight(), dimTickTimes.get(dim), dim.location().toString());
//            i++;
//        }
//
//        if (debugTimeout < 190) {
//            Font fontRenderer = Minecraft.getInstance().font;
//            fontRenderer.drawShadow(event.getMatrixStack(), "Server Stopped Sending Updates!", 0, event.getWindow().getGuiScaledHeight() - 21, 0xFF0000);
//            fontRenderer.drawShadow(event.getMatrixStack(), "Display will time out in " + MathUtils.round((debugTimeout / 20D), 10), 0, event.getWindow().getGuiScaledHeight() - 11, 0xFF0000);
//        }
//
//        GlStateManager._popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fovUpdate(FOVModifierEvent event) {
        Player player = event.getEntity();
        float originalFOV = event.getFov();
        float newFOV = originalFOV;

        int slotIndex = 2;
        for (ItemStack stack : player.inventory.armor) {
            if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
                newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EquipmentSlot.values()[slotIndex]);
            }
            slotIndex++;
        }

        ItemStack stack = player.getOffhandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EquipmentSlot.OFFHAND);
        }
        stack = player.getMainHandItem();
        if (!stack.isEmpty() && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EquipmentSlot.MAINHAND);
        }

        if (newFOV != originalFOV) {
            event.setNewfov(newFOV);
        }
    }

//    @SubscribeEvent
//    public void guiInit(GuiScreenEvent.InitGuiEvent event) {
//        for (int i = 0; i < event.getButtonList().size(); i++) {
//            GuiButton button = event.getButtonList().get(i);
//            String resource = button instanceof GuiButtonImage ? ((GuiButtonImage) button).resourceLocation.getResourcePath() : "";
//            if (resource.equals("textures/gui/container/crafting_table.png") || resource.equals("textures/gui/container/inventory.png")) {
//                event.getButtonList().set(i, new GuiButton(button.id, 0, 0, ""){
//                    @Override
//                    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {}
//
//                    @Override
//                    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
//                        return false;
//                    }
//                });
//            }
//        }
//    }

//    @SubscribeEvent
//    public void mouseClickEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
////        Screen screen = event.getGui();
////        int button = Mouse.getEventButton();
////
////        if (screen instanceof GuiChat && button == 0) {
////
////            ITextComponent itextcomponent = Minecraft.getInstance().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
////            BCLogHelper.info(itextcomponent);
////            if (itextcomponent != null) {
////                BCLogHelper.info(itextcomponent.getStyle().getClickEvent());
////                event.setCanceled(true);
////            }
////        }
//    }
    //endregion

    //region misc methods

    private void searchForPlayerMount() {
        if (remountTicksRemaining > 0) {
            Entity e = Minecraft.getInstance().level.getEntity(remountEntityID);
            if (e != null) {
                Minecraft.getInstance().player.startRiding(e);
                LogHelperBC.info("Successfully placed player on mount after " + (500 - remountTicksRemaining) + " ticks");
                remountTicksRemaining = 0;
                return;
            }
            remountTicksRemaining--;
            if (remountTicksRemaining == 0) {
                LogHelperBC.error("Unable to locate player mount after 500 ticks! Aborting");
//TODO                BrandonsCore.network.sendToServer(new PacketUpdateMount(-1));
            }
        }
    }

    public static void tryRepositionPlayerOnMount(int id) {
        if (remountTicksRemaining == 500) return;
        remountTicksRemaining = 500;
        remountEntityID = id;
        LogHelperBC.info("Started checking for player mount"); //Todo move to core as this is part of the teleporter
    }

//    private void renderGraph(PoseStack matrix, int x, int y, int screenWidth, int screenHeight, Integer[] times, String name) {
//        int yHeight = screenHeight - 23 - (y * 45);
//
//        GuiHelperOld.drawColouredRect(x, yHeight - 34, 202, 32, 0xAA000000);
//        Font fontRenderer = Minecraft.getInstance().font;
//        fontRenderer.drawShadow(matrix, name, x + 2, yHeight - 43, 0xFFFFFF);
//        GuiHelperOld.drawBorderedRect(x, yHeight - 34, 202, 17, 1, 0x44AA0000, 0xAACCCCCC);
//        GuiHelperOld.drawBorderedRect(x, yHeight - 18, 202, 17, 1, 0x4400AA00, 0xAACCCCCC);
//        fontRenderer.draw(matrix, "50ms", x + 2, yHeight - 16, 0xFFFFFF);
//        fontRenderer.draw(matrix, "100ms", x + 2, yHeight - 32, 0xFFFFFF);
//
//        for (int i = 0; i < 200; i++) {
//            int time = times[i] == null ? 0 : times[i];
//            int height = (int) (((time / 100D) / 100D) * 30D);
//            int j1 = getFrameColor(Mth.clamp(height, 0, 30), 0, 15, 30);
//            GuiHelperOld.drawColouredRect(x + ((i - renderIndex) % 200) + 200, yHeight - 2 - height, 1, height, j1);
//        }
//    }

//TODO    public static void handleTickPacket(PacketTickTime packet) {
//        debugTimeout = 200;
//        renderIndex++;
//
//        overallTickTime[renderIndex % 200] = packet.overall;
//
//        LinkedList<Integer> dims = new LinkedList<>();
//        dims.addAll(dimTickTimes.keySet());
//
//        for (Integer dim : packet.tickTimes.keySet()) {
//            if (!dimTickTimes.containsKey(dim)) {
//                Integer[] ints = new Integer[200];
//                for (int i = 0; i < ints.length; i++) {
//                    ints[i] = 0;
//                }
//                dimTickTimes.put(dim, ints);
//            }
//
//            dimTickTimes.get(dim)[renderIndex % 200] = packet.tickTimes.get(dim);
//            if (dims.contains(dim)) {
//                dims.remove(dim);
//            }
//        }
//
//        if (!dims.isEmpty()) {
//            while (dims.size() > 0) {
//                for (Integer i : dimTickTimes.keySet()) {
//                    if (dims.get(0).equals(i)) {
//                        dimTickTimes.remove(i);
//                        dims.remove(0);
//                        break;
//                    }
//                }
//            }
//
//            sortingOrder.clear();
//            sortingOrder.addAll(dimTickTimes.keySet());
//            Collections.sort(sortingOrder, sorter);
//        }
//    }

    public static int getFrameColor(int input, int min, int mid, int max) {
        return input < mid ? blendColors(-16711936, -256, (float) input / (float) mid) : blendColors(-256, -65536, (float) (input - mid) / (float) (max - mid));
    }

    public static int blendColors(int p_181553_1_, int p_181553_2_, float p_181553_3_) {
        int i = p_181553_1_ >> 24 & 255;
        int j = p_181553_1_ >> 16 & 255;
        int k = p_181553_1_ >> 8 & 255;
        int l = p_181553_1_ & 255;
        int i1 = p_181553_2_ >> 24 & 255;
        int j1 = p_181553_2_ >> 16 & 255;
        int k1 = p_181553_2_ >> 8 & 255;
        int l1 = p_181553_2_ & 255;
        int i2 = Mth.clamp((int) ((float) i + (float) (i1 - i) * p_181553_3_), 0, 255);
        int j2 = Mth.clamp((int) ((float) j + (float) (j1 - j) * p_181553_3_), 0, 255);
        int k2 = Mth.clamp((int) ((float) k + (float) (k1 - k) * p_181553_3_), 0, 255);
        int l2 = Mth.clamp((int) ((float) l + (float) (l1 - l) * p_181553_3_), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }

    //endregion
}
