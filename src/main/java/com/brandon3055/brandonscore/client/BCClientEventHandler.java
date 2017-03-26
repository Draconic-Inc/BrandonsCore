package com.brandon3055.brandonscore.client;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IFOVModifierItem;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.network.PacketTickTime;
import com.brandon3055.brandonscore.network.PacketUpdateMount;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.LinkedHashList;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DimensionType;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class BCClientEventHandler {

    private static int remountTicksRemaining = 0;
    private static int remountEntityID = 0;
    private static int debugTimeout = 0;
    private static Map<Integer, Integer[]> dimTickTimes = new HashMap<Integer, Integer[]>();
    private static Integer[] overallTickTime = new Integer[200];
    private static int renderIndex = 0;
    private static LinkedList<Integer> sortingOrder = new LinkedList<Integer>();
    public static int elapsedTicks = 0;

    //region sorter

    private static Comparator<Integer> sorter = new Comparator<Integer>() {
        @Override
        public int compare(Integer value, Integer compare) {
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

            return totalValue > totalCompare ? -1 : totalValue < totalCompare ? 1 : 0;
        }
    };

    //endregion

    //region Event Hooks

    @SubscribeEvent
    public void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        elapsedTicks++;
        if (Minecraft.getMinecraft().isGamePaused()) {
            return;
        }

        if (debugTimeout > 0) {
            debugTimeout--;
        }

        if (elapsedTicks % 100 == 0 && debugTimeout > 0) {
            sortingOrder.clear();
            sortingOrder.addAll(dimTickTimes.keySet());
            Collections.sort(sortingOrder, sorter);
        }
    }

    @SubscribeEvent
    public void joinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayerSP) {
            BrandonsCore.network.sendToServer(new PacketUpdateMount(0));
        }
    }

    @SubscribeEvent
    public void renderScreen(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL || debugTimeout <= 0 ||  Minecraft.getMinecraft().currentScreen instanceof GuiChat) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 600);

        renderGraph(220, 0, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight(), overallTickTime, "Overall");

        int i = 0;
        for (Integer dim : sortingOrder) {
            if (dimTickTimes.get(dim) == null || !DimensionManager.isDimensionRegistered(dim)) {
                continue;
            }

            DimensionType dimensionType = DimensionManager.getProviderType(dim);
            renderGraph(0, i, event.getResolution().getScaledWidth(), event.getResolution().getScaledHeight(), dimTickTimes.get(dim), dimensionType == null ? dim.toString() : dimensionType.getName());
            i++;
        }

        if (debugTimeout < 190) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            fontRenderer.drawString("Server Stopped Sending Updates!", 0, event.getResolution().getScaledHeight() - 21, 0xFF0000, true);
            fontRenderer.drawString("Display will time out in " + Utils.round((debugTimeout / 20D), 10), 0, event.getResolution().getScaledHeight() - 11, 0xFF0000, true);
        }

        GlStateManager.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fovUpdate(FOVUpdateEvent event) {
        EntityPlayer player = event.getEntity();
        float originalFOV = event.getFov();
        float newFOV = originalFOV;

        int slotIndex = 2;
        for (ItemStack stack : player.inventory.armorInventory) {
            if (stack != null && stack.getItem() instanceof IFOVModifierItem) {
                newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EntityEquipmentSlot.values()[slotIndex]);
            }
            slotIndex++;
        }

        ItemStack stack = player.getHeldItemOffhand();
        if (stack != null && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EntityEquipmentSlot.OFFHAND);
        }
        stack = player.getHeldItemMainhand();
        if (stack != null && stack.getItem() instanceof IFOVModifierItem) {
            newFOV = ((IFOVModifierItem) stack.getItem()).getNewFOV(player, stack, newFOV, originalFOV, EntityEquipmentSlot.MAINHAND);
        }

        if (newFOV != originalFOV) {
            event.setNewfov(newFOV);
        }
    }

//    @SubscribeEvent
//    public void mouseClickEvent(GuiScreenEvent.MouseInputEvent.Pre event) {
////        GuiScreen screen = event.getGui();
////        int button = Mouse.getEventButton();
////
////        if (screen instanceof GuiChat && button == 0) {
////
////            ITextComponent itextcomponent = Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());
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
            Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(remountEntityID);
            if (e != null) {
                Minecraft.getMinecraft().thePlayer.startRiding(e);
                LogHelperBC.info("Successfully placed player on mount after " + (500 - remountTicksRemaining) + " ticks");
                remountTicksRemaining = 0;
                return;
            }
            remountTicksRemaining--;
            if (remountTicksRemaining == 0) {
                LogHelperBC.error("Unable to locate player mount after 500 ticks! Aborting");
                BrandonsCore.network.sendToServer(new PacketUpdateMount(-1));
            }
        }
    }

    public static void tryRepositionPlayerOnMount(int id) {
        if (remountTicksRemaining == 500) return;
        remountTicksRemaining = 500;
        remountEntityID = id;
        LogHelperBC.info("Started checking for player mount"); //Todo move to core as this is part of the teleporter
    }

    private void renderGraph(int x, int y, int screenWidth, int screenHeight, Integer[] times, String name) {
        int yHeight = screenHeight - 23 - (y * 45);

        GuiHelper.drawColouredRect(x, yHeight - 34, 202, 32, 0xAA000000);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString(name, x + 2, yHeight - 43, 0xFFFFFF, true);
        GuiHelper.drawBorderedRect(x, yHeight - 34, 202, 17, 1, 0x44AA0000, 0xAACCCCCC);
        GuiHelper.drawBorderedRect(x, yHeight - 18, 202, 17, 1, 0x4400AA00, 0xAACCCCCC);
        fontRenderer.drawString("50ms", x + 2, yHeight - 16, 0xFFFFFF);
        fontRenderer.drawString("100ms", x + 2, yHeight - 32, 0xFFFFFF);

        for (int i = 0; i < 200; i++) {
            int time = times[i] == null ? 0 : times[i];
            int height = (int) (((time / 100D) / 100D) * 30D);
            int j1 = getFrameColor(MathHelper.clamp_int(height, 0, 30), 0, 15, 30);
            GuiHelper.drawColouredRect(x + ((i - renderIndex) % 200) + 200, yHeight - 2 - height, 1, height, j1);
        }
    }

    public static void handleTickPacket(PacketTickTime packet) {
        debugTimeout = 200;
        renderIndex++;

        overallTickTime[renderIndex % 200] = packet.overall;

        LinkedHashList<Integer> dims = new LinkedHashList<Integer>();
        dims.addAll(dimTickTimes.keySet());

        for (Integer dim : packet.tickTimes.keySet()) {
            if (!dimTickTimes.containsKey(dim)) {
                Integer[] ints = new Integer[200];
                for (int i = 0; i < ints.length; i++) {
                    ints[i] = 0;
                }
                dimTickTimes.put(dim, ints);
            }

            dimTickTimes.get(dim)[renderIndex % 200] = packet.tickTimes.get(dim);
            if (dims.contains(dim)) {
                dims.remove(dim);
            }
        }

        if (!dims.isEmpty()) {
            while (dims.size() > 0) {
                for (Integer i : dimTickTimes.keySet()) {
                    if (dims.get(0).equals(i)) {
                        dimTickTimes.remove(i);
                        dims.remove(0);
                        break;
                    }
                }
            }

            sortingOrder.clear();
            sortingOrder.addAll(dimTickTimes.keySet());
            Collections.sort(sortingOrder, sorter);
        }
    }

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
        int i2 = MathHelper.clamp_int((int) ((float) i + (float) (i1 - i) * p_181553_3_), 0, 255);
        int j2 = MathHelper.clamp_int((int) ((float) j + (float) (j1 - j) * p_181553_3_), 0, 255);
        int k2 = MathHelper.clamp_int((int) ((float) k + (float) (k1 - k) * p_181553_3_), 0, 255);
        int l2 = MathHelper.clamp_int((int) ((float) l + (float) (l1 - l) * p_181553_3_), 0, 255);
        return i2 << 24 | j2 << 16 | k2 << 8 | l2;
    }

    //endregion
}
