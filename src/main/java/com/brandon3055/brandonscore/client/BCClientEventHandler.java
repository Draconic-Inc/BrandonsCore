package com.brandon3055.brandonscore.client;

import codechicken.lib.CodeChickenLib;
import codechicken.lib.colour.EnumColour;
import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IFOVModifierItem;
import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.brandonscore.client.render.BlockEntityRendererTransparent;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.covers1624.quack.util.CrashLock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static net.minecraft.client.renderer.RenderStateShard.*;
import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_PARTICLES;
import static net.minecraftforge.client.event.RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS;

/**
 * Created by brandon3055 on 17/07/2016.
 */
public class BCClientEventHandler {
    private static final CrashLock LOCK = new CrashLock("Already Initialized");

    private static int remountTicksRemaining = 0;
    private static int remountEntityID = 0;
    private static int debugTimeout = 0;
    private static Map<ResourceKey<Level>, Integer[]> dimTickTimes = new HashMap<>();
    private static Integer[] overallTickTime = new Integer[200];
    private static int renderIndex = 0;
    private static LinkedList<ResourceKey<Level>> sortingOrder = new LinkedList<>();
    public static int elapsedTicks = 0;

    private static final Set<BlockEntity> translucentBlockEntities = new ObjectOpenHashSet<>();

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

    public static void init() {
        LOCK.lock();

        MinecraftForge.EVENT_BUS.register(new BCClientEventHandler());
    }

    @SubscribeEvent
    public static void disconnectEvent(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            BCEventHandler.noClipPlayers.remove(mc.player.getUUID());
        }
    }

    public static <T extends BlockEntity> void addTranslucentBlockEntity(T blockEntity) {
        translucentBlockEntities.add(blockEntity);
    }

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
    public void drawSelectionEvent(RenderHighlightEvent.Block event) {
        Level level = Minecraft.getInstance().level;
        if (event.getTarget().getType() == HitResult.Type.MISS || level == null) return;
        BlockState state = level.getBlockState(event.getTarget().getBlockPos());
        if (state.getBlock() instanceof BlockBCore block) {
            if (!block.renderSelectionBox(event, level)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void fovUpdate(ComputeFovModifierEvent event) {
        Player player = event.getPlayer();
        float originalFOV = event.getFovModifier();
        float newFOV = originalFOV;

        int slotIndex = 2;
        for (ItemStack stack : player.getInventory().armor) {
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
            event.setNewFovModifier(newFOV);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void renderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == AFTER_SOLID_BLOCKS) doDebugRendering(event);
        if (event.getStage() != AFTER_PARTICLES) return;

        BlockEntityRenderDispatcher tileRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        MultiBufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        Vec3 vec3 = event.getCamera().getPosition();
        double camX = vec3.x();
        double camY = vec3.y();
        double camZ = vec3.z();

        for (BlockEntity tile : translucentBlockEntities) {
            BlockPos pos = tile.getBlockPos();
            poseStack.pushPose();
            poseStack.translate((double) pos.getX() - camX, (double) pos.getY() - camY, (double) pos.getZ() - camZ);
            renderTransparent(camera, (BlockEntityRendererTransparent<BlockEntity>) tileRenderDispatcher.getRenderer(tile), tile, event.getPartialTick(), poseStack, buffers);
            poseStack.popPose();
        }

        translucentBlockEntities.clear();
    }

    public <E extends BlockEntity> void renderTransparent(Camera camera, BlockEntityRendererTransparent<E> renderer, E tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers) {
        if (!tile.hasLevel() || !tile.getType().isValid(tile.getBlockState())) return;
        if (!renderer.shouldRender(tile, camera.getPosition())) return;
        int packedLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos());
        try {
            renderer.renderTransparent(tile, partialTicks, poseStack, buffers, packedLight, OverlayTexture.NO_OVERLAY);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static final DepthTestStateShard DISABLE_DEPTH = new DepthTestStateShard("none", 519) {
        @Override
        public void setupRenderState() {
            RenderSystem.disableDepthTest();
        }
    };

    private static final RenderType boxNoDepth = RenderType.create("ccl:box_no_depth", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true, RenderType.CompositeState.builder()
            .setShaderState(POSITION_COLOR_SHADER)
            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
            .setWriteMaskState(COLOR_WRITE)
            .setDepthTestState(DISABLE_DEPTH)
            .createCompositeState(false)
    );

    private static final Cuboid6 BOX = Cuboid6.full.copy().expand(0.02);
    private static final float[] RED = EnumColour.RED.getColour(128).packArray();

    public static List<Vector3> debugBlockList = null;
    int i = 0;
    public void doDebugRendering(RenderLevelStageEvent event) {
        if (debugBlockList == null) return;

        MultiBufferSource.BufferSource source = Minecraft.getInstance().renderBuffers().bufferSource();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack pStack = event.getPoseStack();
        pStack.pushPose();

        if (i++ % 100 == 0) {
            debugBlockList.sort(Comparator.comparingDouble(value -> value.distanceSquared(Vector3.fromEntity(Minecraft.getInstance().player))));
        }

        pStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        TransformingVertexConsumer consumer = new TransformingVertexConsumer(source.getBuffer(boxNoDepth), pStack);

        int i = 0;
        for (Vector3 pos : debugBlockList) {
            i++;
            RenderUtils.bufferCuboidSolid(
                    consumer,
                    BOX.copy().add(pos),
                    RED[0], RED[1], RED[2], RED[3]
            );
            if (i > 1000) break;;
        }


        source.endBatch();
        pStack.popPose();

    }
}
