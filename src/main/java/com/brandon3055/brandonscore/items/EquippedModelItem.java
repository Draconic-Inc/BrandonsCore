package com.brandon3055.brandonscore.items;

import com.brandon3055.brandonscore.client.render.EquippedItemModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;


/**
 * For items that render custom models on the player when equipped.
 * E.g. custom armor models such as the DE Chestpiece model.
 * Note: This currently only supports entities with that use a {@link HumanoidModel}
 *
 * Created by brandon3055 on 19/11/2022
 */
public interface EquippedModelItem {

    /**
     * @param entity The entity on which this model will be rendered.
     * @param stack The item stack instance.
     * @param slot The equipment slot the stack is in, or null if in a modded equipment slot (e.g. curios).
     * @param parentModel The model of the {@link LivingEntity}. You can copy the properties from this model to yours using {@link net.minecraftforge.client.ForgeHooksClient#copyModelProperties(HumanoidModel, HumanoidModel)} (If applicable)
     * @param slimModel This will be true in the event the entity is a player with the slim model enabled.
     * @return a model implementing {@link EquippedItemModel}
     */
    @OnlyIn(Dist.CLIENT)
    EquippedItemModel getExtendedModel(LivingEntity entity, ItemStack stack, @Nullable EquipmentSlot slot, HumanoidModel<?> parentModel, boolean slimModel);

    /**
     * @param entity The entity on which this model will be rendered.
     * @param stack The item stack instance.
     * @param slot The equipment slot the stack is in, or null if in a modded equipment slot (e.g. curios).
     * @return true if the model should be rendered in the specified slot
     */
    default boolean shouldRenderInSlot(LivingEntity entity, ItemStack stack, @Nullable EquipmentSlot slot) {
        return true;
    }

    /**
     * This can be used in your {@link #getExtendedModel(LivingEntity, ItemStack, EquipmentSlot, HumanoidModel, boolean)} implementation to set up part visibility based on slot.
     * If slot is null all parts will be visible.
     *
     * @param model The model
     * @param slot The slot
     */
    static void setPartVisibility(HumanoidModel<?> model, @Nullable EquipmentSlot slot) {
        if (slot == null) {
            model.setAllVisible(true);
        } else {
            model.setAllVisible(false);
            switch (slot) {
                case HEAD:
                    model.head.visible = true;
                    model.hat.visible = true;
                    break;
                case CHEST:
                    model.body.visible = true;
                    model.rightArm.visible = true;
                    model.leftArm.visible = true;
                    break;
                case LEGS:
                    model.body.visible = true;
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
                    break;
                case FEET:
                    model.rightLeg.visible = true;
                    model.leftLeg.visible = true;
            }
        }
    }

}
