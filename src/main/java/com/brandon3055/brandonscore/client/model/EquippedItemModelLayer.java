package com.brandon3055.brandonscore.client.model;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.render.EquippedItemModel;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.brandon3055.brandonscore.items.EquippedModelItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 30/6/20
 */
public class EquippedItemModelLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private boolean slimModel;
    private ContributorModel<?> contributorModel = new ContributorModel<>();

    public EquippedItemModelLayer(LivingEntityRenderer<T, M> renderer, boolean slimModel) {
        super(renderer);
        this.slimModel = slimModel;
    }

    @Override
    public void render(PoseStack mStack, MultiBufferSource getter, int packedLightIn, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderItemModel(mStack, getter, livingEntity, livingEntity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST, packedLightIn, partialTicks);
        this.renderItemModel(mStack, getter, livingEntity, livingEntity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS, packedLightIn, partialTicks);
        this.renderItemModel(mStack, getter, livingEntity, livingEntity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET, packedLightIn, partialTicks);
        this.renderItemModel(mStack, getter, livingEntity, livingEntity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD, packedLightIn, partialTicks);

        if (BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e -> e.getItem() instanceof EquippedModelItem, livingEntity);
            if (!stack.isEmpty()) {
                this.renderItemModel(mStack, getter, livingEntity, stack, null, packedLightIn, partialTicks);
            }
        }

        if (livingEntity instanceof Player player) {
            ContributorProperties props = ContributorHandler.getProps(player);
            if (props.isContributor()) {
                ForgeHooksClient.copyModelProperties(getParentModel(), contributorModel);
                contributorModel.props = props;
                contributorModel.render(livingEntity, mStack, getter, ItemStack.EMPTY, packedLightIn, OverlayTexture.NO_OVERLAY, partialTicks);
            }
        }
    }

    private void renderItemModel(PoseStack poseStack, MultiBufferSource source, T entity, ItemStack stack, @Nullable EquipmentSlot slot, int packedlight, float partialTicks) {
        if (stack.getItem() instanceof EquippedModelItem modelItem) {
            if (slot != null && stack.getItem() instanceof ArmorItem armorItem && armorItem.getSlot() != slot) {
                return;
            }

            EquippedItemModel model = modelItem.getExtendedModel(entity, stack, slot, this.getParentModel(), slimModel);
            model.render(entity, poseStack, source, stack, packedlight, OverlayTexture.NO_OVERLAY, partialTicks);
        }
    }
}
