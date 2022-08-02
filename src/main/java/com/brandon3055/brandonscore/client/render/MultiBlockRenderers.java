package com.brandon3055.brandonscore.client.render;

import codechicken.lib.render.RenderUtils;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.vec.Cuboid6;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.multiblock.MultiBlockDefinition;
import com.brandon3055.brandonscore.multiblock.MultiBlockPart;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.*;

/**
 * Created by brandon3055 on 25/07/2022
 */
public class MultiBlockRenderers {
    private static final RenderType outlineType = RenderType.create("invalid_outline", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder()
            .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(4.0)))
            .createCompositeState(false)
    );

    public static void renderBuildGuide(Level level, BlockPos inWorldOrigin, PoseStack poseStack, MultiBufferSource getter, MultiBlockDefinition structure, int packedLight, float partialTicks) {
        Map<BlockPos, MultiBlockPart> blocks = structure.getBlocks();
        Map<MultiBlockPart, BlockState> stateMap = new IdentityHashMap<>();
        Player player = Minecraft.getInstance().player;

        int time = TimeKeeper.getClientTick() % 80;
        int anim = 5;
        if (time >= 70) {
            time -= 70;
            anim += (int) (Math.sin((time / 10F) * Math.PI) * 5);
        }

        List<BlockPos> invalidBlocks = new ArrayList<>();
        for (BlockPos pos : blocks.keySet()) {
            MultiBlockPart part = blocks.get(pos);
            if (part.validBlocks().isEmpty()) continue;
            BlockPos worldPos = inWorldOrigin.offset(pos);

            if (level.isEmptyBlock(worldPos)) {
                if (player != null && worldPos.distToCenterSqr(player.getEyePosition()) < (4*4)) continue;
                BlockState state = stateMap.computeIfAbsent(part, e -> List.copyOf(e.validBlocks()).get((TimeKeeper.getClientTick() / 40) % e.validBlocks().size()).defaultBlockState());
                if (state.isAir()) continue;
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.1, pos.getY() + 0.1, pos.getZ() + 0.1);
                poseStack.scale(0.8F, 0.8F, 0.8F);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, poseStack, getter, packedLight, OverlayTexture.pack(anim, 10), EmptyModelData.INSTANCE);
                poseStack.popPose();
            } else if (!part.isMatch(level, worldPos)) {
                invalidBlocks.add(pos);
            }
        }

        com.brandon3055.brandonscore.client.render.RenderUtils.endBatch(getter); //Needed to ensure invalid blocks are rendered last.

        if (!invalidBlocks.isEmpty()) {
            for (BlockPos pos : invalidBlocks) {
                poseStack.pushPose();
                poseStack.translate(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(outlineType), poseStack);
                double invalidAnim = Math.sin((TimeKeeper.getClientTick() + partialTicks) / 25D * 3.141593);
                invalidAnim = Math.max(0.1, Math.abs(invalidAnim));
                Cuboid6 box = new Cuboid6().expand(invalidAnim / 2);
                RenderUtils.bufferCuboidOutline(builder, box, 1F, 0F, 0F, 1F);
                poseStack.popPose();
            }
        }
    }

}
