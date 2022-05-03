package com.brandon3055.brandonscore.client.hud;

import com.brandon3055.brandonscore.api.hud.AbstractHudElement;
import com.brandon3055.brandonscore.api.hud.IHudBlock;
import com.brandon3055.brandonscore.api.hud.IHudDisplay;
import com.brandon3055.brandonscore.api.hud.IHudItem;
import com.brandon3055.brandonscore.api.math.Vector2;
import com.brandon3055.brandonscore.api.render.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 19/8/21
 */
public class HudDataElement extends AbstractHudElement {
    private final boolean iHudItem;
    private final boolean iHudBlock;

    private IHudDisplay activeHud = null;
    private List<Component> displayList = new ArrayList<>();

    public HudDataElement(Vector2 defaultPos, boolean iHudItem, boolean iHudBlock) {
        super(defaultPos);
        this.iHudItem = iHudItem;
        this.iHudBlock = iHudBlock;
    }

    @Override
    public void tick(boolean configuring) {
        activeHud = null;
        displayList.clear();
        width = 145;
        height = 30;
        if (!enabled) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (iHudBlock) {
            HitResult traceResult = Minecraft.getInstance().player.pick(5, 0, false);
            if (traceResult instanceof BlockHitResult && traceResult.getType() != HitResult.Type.MISS) {
                BlockPos pos = ((BlockHitResult) traceResult).getBlockPos();
                BlockState state = player.level.getBlockState(pos);
                BlockEntity tile = player.level.getBlockEntity(pos);
                if (state.getBlock() instanceof IHudBlock) {
                    activeHud = (IHudBlock) state.getBlock();
                } else if (tile instanceof IHudBlock) {
                    activeHud = (IHudBlock) tile;
                }
                if (activeHud != null && ((IHudBlock) activeHud).shouldDisplayHudText(player.level, pos, player)) {
                    ((IHudBlock) activeHud).generateHudText(player.level, pos, player, displayList);
                    if (displayList.isEmpty()) {
                        activeHud = null;
                    }
                } else {
                    activeHud = null;
                }
            }
        }

        if (iHudItem && activeHud == null) {
            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof IHudItem) || !((IHudItem) stack.getItem()).shouldDisplayHudText(stack, player)) {
                stack = player.getOffhandItem();
            }
            if (!stack.isEmpty() && stack.getItem() instanceof IHudItem) {
                activeHud = (IHudItem) stack.getItem();
                if (((IHudItem) activeHud).shouldDisplayHudText(stack, player)) {
                    ((IHudItem) activeHud).generateHudText(stack, player, displayList);
                    if (displayList.isEmpty()) {
                        activeHud = null;
                    }
                } else {
                    activeHud = null;
                }
            }
        }

        if (activeHud != null) {
            Minecraft mc = Minecraft.getInstance();
            width = activeHud.computeHudWidth(mc, displayList);
            height = activeHud.computeHudHeight(mc, displayList);
        }
    }

    @Override
    public void render(PoseStack mStack, float partialTicks, boolean configuring) {
        if (!enabled || (activeHud == null && !configuring)) return;
        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        mStack.translate(xPos(), yPos(), 0);
        if (activeHud == null) {
            GuiHelper.drawHoverRect(getter, mStack, 0, 0, width(), height());
            getter.endBatch();
            return;
        }
        activeHud.renderHudBackground(getter, mStack, width(), height(), displayList);
        getter.endBatch();
        activeHud.renderHudContent(Minecraft.getInstance().font, mStack, width(), height(), displayList);
    }
}
