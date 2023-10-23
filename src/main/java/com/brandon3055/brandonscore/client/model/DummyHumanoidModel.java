package com.brandon3055.brandonscore.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;

/**
 * Created by brandon3055 on 19/11/2022
 */
public class DummyHumanoidModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final DummyHumanoidModel<?> INSTANCE = new DummyHumanoidModel<>();
    /*
    public static final IItemRenderProperties DUMMY_ITEM_RENDER_PROPS = new IItemRenderProperties() {
        @Override
        public HumanoidModel<?> getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
            return INSTANCE;
        }
    }; //TODO [FoxMcloud5655]: I have no idea what to do with this.
    */

    public DummyHumanoidModel() {
        super(createMesh(new CubeDeformation(1), 0).getRoot().bake(64, 64));
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float r, float g, float b, float a) {}

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of();
    }

    @Override
    public void copyPropertiesTo(EntityModel<T> p_102625_) {}

    @Override
    public void prepareMobModel(T p_102861_, float p_102862_, float p_102863_, float p_102864_) {}

    @Override
    public void setupAnim(T p_102866_, float p_102867_, float p_102868_, float p_102869_, float p_102870_, float p_102871_) {}

    @Override
    protected void setupAttackAnimation(T p_102858_, float p_102859_) {}

    @Override
    public void copyPropertiesTo(HumanoidModel<T> p_102873_) {}

    @Override
    public void setAllVisible(boolean p_102880_) {}

    @Override
    public void translateToHand(HumanoidArm p_102854_, PoseStack p_102855_) {}

    @Override
    protected ModelPart getArm(HumanoidArm p_102852_) {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }

    @Override
    public ModelPart getHead() {
        return new ModelPart(Collections.emptyList(), Collections.emptyMap());
    }
}
