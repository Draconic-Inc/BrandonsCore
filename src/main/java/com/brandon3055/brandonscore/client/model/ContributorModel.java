package com.brandon3055.brandonscore.client.model;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.buffer.TransformingVertexConsumer;
import codechicken.lib.render.buffer.VBORenderType;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.client.render.EquippedItemModel;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.brandon3055.brandonscore.client.shader.BCShader;
import com.brandon3055.brandonscore.client.shader.BCShaders;
import com.brandon3055.brandonscore.client.shader.ContribShader;
import com.brandon3055.brandonscore.handlers.contributor.Animations;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig;
import com.brandon3055.brandonscore.handlers.contributor.ContributorProperties;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.Map;

import static codechicken.lib.math.MathHelper.torad;
import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 13/11/2022
 */
public class ContributorModel<T extends LivingEntity> extends HumanoidModel<T> implements EquippedItemModel {
    public ContributorProperties props = new ContributorProperties();

    //Wing Types
    private static final RenderType BONE_TYPE = createBoneType("bone", BCShaders.CONTRIB_BASE_SHADER);
    private static final RenderType BONE_CHAOS_TYPE = createChaosType("bone_chaos", BCShaders.CHAOS_ENTITY_SHADER);
    private static final RenderType BONE_SHADER_TYPE = createBoneType("bone_shader", BCShaders.WINGS_BONE_SHADER);
    private static final RenderType WEB_TYPE = createWebType("web", BCShaders.CONTRIB_BASE_SHADER);
    private static final RenderType WEB_SHADER_TYPE = createWebType("web_shader", BCShaders.WINGS_WEB_SHADER);

    //Basic badge types
    private static final RenderType LOLNET_TYPE = createBasicBadgeType(new ResourceLocation(MODID, "textures/contributor/badges/lolnet.png"), false);
    private static final RenderType CR_TYPE = createBasicBadgeType(new ResourceLocation(MODID, "textures/contributor/badges/cr.png"), false);
    private static final RenderType OG_TYPE = createBasicBadgeType(new ResourceLocation(MODID, "textures/contributor/badges/og_patreon.png"), false);

    //Fancy badge types
    private static final RenderType PATREON_LOGO_TYPE = createBadgeType("badge", BCShaders.BADGE_FOIL_SHADER, new ResourceLocation(MODID, "textures/contributor/badges/patreon_base.png"), true);
    private static final RenderType PATREON_LOGO_OUTLINE_TYPE = createBadgeType("badge", BCShaders.BADGE_OUTLINE_SHADER, new ResourceLocation(MODID, "textures/contributor/badges/patreon_base.png"), true);
    private static final RenderType PATREON_CORE_TYPE = createBadgeType("badge", BCShaders.BADGE_CORE_SHADER, new ResourceLocation(MODID, "textures/contributor/badges/patreon_core.png"), true);

    private static final RenderType BADGE_VET_TYPE = createBadgeType("vet", BCShaders.VET_BADGE_SHADER, new ResourceLocation(MODID, "textures/contributor/badges/veteran.png"), false);
    private static final RenderType BADGE_VET_CORE_TYPE = createBadgeType("vet_over", BCShaders.VET_BADGE_SHADER, new ResourceLocation(MODID, "textures/contributor/badges/veteran_overlay.png"), false);

    private final WingBoneRenderer humBone;
    private final WingBoneRenderer radBone;
    private final WingBoneRenderer f1Bone;
    private final WingBoneRenderer f2Bone;
    private final WingBoneRenderer f3Bone;
    private final Item chestpiece1;
    private final Item chestpiece2;
    private final Item chestpiece3;

    public ContributorModel() {
        super(createMesh(new CubeDeformation(1), 0).getRoot().bake(64, 64));
        Map<String, CCModel> model = new OBJParser(new ResourceLocation(MODID, "models/entity/contributor_wings.obj")).ignoreMtl().parse();
        CCModel hum = model.get("hum_bone").backfacedCopy();
        CCModel humShell = model.get("hum_bone_shell").backfacedCopy();
        CCModel rad = model.get("rad_bone").backfacedCopy();
        CCModel radShell = model.get("rad_bone_shell").backfacedCopy();
        CCModel f1 = model.get("f1_bone").backfacedCopy();
        CCModel f1Shell = model.get("f1_bone_shell").backfacedCopy();
        CCModel f2 = model.get("f2_bone").backfacedCopy();
        CCModel f2Shell = model.get("f2_bone_shell").backfacedCopy();
        CCModel f3 = model.get("f3_bone").backfacedCopy();
        CCModel f3Shell = model.get("f3_bone_shell").backfacedCopy();

        humBone = new WingBoneRenderer(hum, new Vector3(-1.5, 22.5, 2), true);
        humBone.shell = new WingBoneRenderer(humShell, Vector3.ZERO, false);
        radBone = new WingBoneRenderer(rad, new Vector3(-1.5, 15, 2.8), true);
        radBone.shell = new WingBoneRenderer(radShell, Vector3.ZERO, false);
        f1Bone = new WingBoneRenderer(f1, new Vector3(-1.5, 31, 2.8), true);
        f1Bone.shell = new WingBoneRenderer(f1Shell, Vector3.ZERO, false);
        f2Bone = new WingBoneRenderer(f2, new Vector3(-1.5, 31, 2.8), true);
        f2Bone.shell = new WingBoneRenderer(f2Shell, Vector3.ZERO, false);
        f3Bone = new WingBoneRenderer(f3, new Vector3(-1.5, 31, 2.8), true);
        f3Bone.shell = new WingBoneRenderer(f3Shell, Vector3.ZERO, false);

        chestpiece1 = ForgeRegistries.ITEMS.getValue(new ResourceLocation("draconicevolution:wyvern_chestpiece"));
        chestpiece2 = ForgeRegistries.ITEMS.getValue(new ResourceLocation("draconicevolution:draconic_chestpiece"));
        chestpiece3 = ForgeRegistries.ITEMS.getValue(new ResourceLocation("draconicevolution:chaotic_chestpiece"));
    }

    @Override
    public void render(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, ItemStack stack, int packedLight, int packedOverlay, float partialTicks) {
        if (!props.isContributor()) return;
        BCShaders.CONTRIB_BASE_SHADER.getUv1OverrideUniform().glUniform2i(packedOverlay & 0xFFFF, (packedOverlay >> 16) & 0xFFFF);
        BCShaders.CONTRIB_BASE_SHADER.getUv2OverrideUniform().glUniform2i(packedLight & 0xFFFF, (packedLight >> 16) & 0xFFFF);
        ContributorConfig config = props.getConfig();
        if (this.young) {
            poseStack.pushPose();
            float f1 = 1.0F / this.babyBodyScale;
            poseStack.scale(f1, f1, f1);
            poseStack.translate(0.0D, this.bodyYOffset / 16.0F, 0.0D);
            renderBadges(config, entity, poseStack, buffers, packedLight, packedOverlay, partialTicks);
            renderWings(config, entity, poseStack, buffers, packedLight, packedOverlay, partialTicks);
            poseStack.popPose();
        } else {
            renderBadges(config, entity, poseStack, buffers, packedLight, packedOverlay, partialTicks);
            renderWings(config, entity, poseStack, buffers, packedLight, packedOverlay, partialTicks);
        }
    }

    private void renderBadges(ContributorConfig config, LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        BCShaders.CONTRIB_BASE_SHADER.getDecayUniform().glUniform1f(0);
        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        Matrix4 mat = new Matrix4(poseStack);
        poseStack.popPose();

        ItemStack armorStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        boolean armor = armorStack.getItem() instanceof ArmorItem;
        boolean hasChestpiece = !armorStack.isEmpty() && isChestpiece(armorStack.getItem());
        if (hasChestpiece) armor = false;
        if (!hasChestpiece && BrandonsCore.equipmentManager != null) {
            ItemStack stack = BrandonsCore.equipmentManager.findMatchingItem(e -> isChestpiece(e.getItem()), entity);
            hasChestpiece = !stack.isEmpty();
        }

        drawBadge(ccrs, mat.copy(), buffers, config.getChestBadge(), config, false, armor, hasChestpiece, partialTicks);
        drawBadge(ccrs, mat.copy(), buffers, config.getBackBadge(), config, true, armor, hasChestpiece, partialTicks);
    }

    private void drawBadge(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, ContributorConfig.Badge badge, ContributorConfig config, boolean onBack, boolean onArmor, boolean hasChestpiece, float partialTicks) {
        switch (badge) {
            case DISABLED -> {}
            case PATREON_OG -> drawBadgeBasic(ccrs, mat.copy(), buffers, OG_TYPE, onBack, onArmor, hasChestpiece, 0, true, 0);
            case PATREON_DRACONIUM -> drawDEBadge(ccrs, mat, buffers, TechLevel.DRACONIUM, config, onBack, onArmor, hasChestpiece);
            case PATREON_WYVERN -> drawDEBadge(ccrs, mat, buffers, TechLevel.WYVERN, config, onBack, onArmor, hasChestpiece);
            case PATREON_DRACONIC -> drawDEBadge(ccrs, mat, buffers, TechLevel.DRACONIC, config, onBack, onArmor, hasChestpiece);
            case PATREON_CHAOTIC -> drawDEBadge(ccrs, mat, buffers, TechLevel.CHAOTIC, config, onBack, onArmor, hasChestpiece);
            case OG_VETERAN -> {
                float[] colour = ContributorConfig.unpack(config.getBaseColourI(TechLevel.DRACONIC));
                BCShaders.VET_BADGE_SHADER.getBaseColorUniform().glUniform4f(colour[0], colour[1], colour[2], 1);
                BCShaders.VET_BADGE_SHADER.getDecayUniform().glUniform1f(0.01F);
                drawBadge(ccrs, mat.copy(), buffers, BCShaders.VET_BADGE_SHADER, BADGE_VET_TYPE, onBack, onArmor, hasChestpiece, 0, false, ((TimeKeeper.getClientTick() + partialTicks) / 2F) % 360);
                BCShaders.VET_BADGE_SHADER.getDecayUniform().glUniform1f(0.1F);
                BCShaders.VET_BADGE_SHADER.getBaseColorUniform().glUniform4f(0.75F, 0.75F, 0.75F, 0F);
                drawBadge(ccrs, mat.copy(), buffers, BCShaders.VET_BADGE_SHADER, BADGE_VET_CORE_TYPE, onBack, onArmor, hasChestpiece, 0.001, false, 0);
            }
            case LOLNET -> drawBadgeBasic(ccrs, mat.copy(), buffers, LOLNET_TYPE, onBack, onArmor, hasChestpiece, 0, true, 0);
            case CR -> drawBadgeBasic(ccrs, mat.copy(), buffers, CR_TYPE, onBack, onArmor, hasChestpiece, 0, true, 0);
        }
    }

    private void drawBadge(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, BCShader<?> shader, RenderType type, boolean onBack, boolean onArmor, boolean hasChestpiece, double zOffset, boolean glint, float rotation) {
        float p = 1 / 16F;
        float scale = onBack ? p * 6 : p * 3F;
        if (onBack) mat.rotate(180 * torad, Vector3.Y_NEG);
        mat.translate(onBack ? 0 : p * 2, onBack ? p * 4 : p * 2, (p * (onArmor ? -3.1 : -2.275)) - zOffset);
        if (hasChestpiece) {
            if (onBack && !onArmor) {
                scale = p * 2.5F;
            } else if (!onBack) {
                scale = p * 2F;
                mat.translate(p * -2, p * 2, p * (onArmor ? -1.1 : -1.4));
            }
        }

        mat.scale(scale, scale, scale);
        if (rotation != 0) mat.rotate(rotation * torad, Vector3.Z_NEG);
        mat.translate(-0.5, -0.5, 0);
        shader.getModelMatUniform().glUniformMatrix4f(mat);
        VertexConsumer consumer = buffers.getBuffer(type);
        ccrs.bind(consumer, DefaultVertexFormat.NEW_ENTITY);
        ccrs.startDrawing(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        addVertex(consumer, 0, 0, 0, 0, 0);
        addVertex(consumer, 0, 1, 0, 0, 1);
        addVertex(consumer, 1, 1, 0, 1, 1);
        addVertex(consumer, 1, 0, 0, 1, 0);
        ccrs.draw();
        RenderUtils.endBatch(buffers);
    }

    private void drawBadgeBasic(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, RenderType type, boolean onBack, boolean onArmor, boolean hasChestpiece, double zOffset, boolean glint, float rotation) {
        float p = 1 / 16F;
        float scale = onBack ? p * 6 : p * 3F;
        if (onBack) mat.rotate(180 * torad, Vector3.Y_NEG);
        mat.translate(onBack ? 0 : p * 2, onBack ? p * 4 : p * 2, (p * (onArmor ? -3.1 : -2.275)) - zOffset);
        if (hasChestpiece) {
            if (onBack && !onArmor) {
                scale = p * 2.5F;
            } else if (!onBack) {
                scale = p * 2F;
                mat.translate(p * -2, p * 2, p * (onArmor ? -2.225 : -1.4));
            }
        }

        mat.scale(scale, scale, scale);
        if (rotation != 0) mat.rotate(rotation * torad, Vector3.Z_NEG);
        mat.translate(-0.5, -0.5, 0);
        VertexConsumer consumer = new TransformingVertexConsumer(buffers.getBuffer(type), mat);
        ccrs.bind(consumer, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        ccrs.startDrawing(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        addBasicVertex(consumer, 0, 0, 0, 0, 0);
        addBasicVertex(consumer, 0, 1, 0, 0, 1);
        addBasicVertex(consumer, 1, 1, 0, 1, 1);
        addBasicVertex(consumer, 1, 0, 0, 1, 0);
        ccrs.draw();
        if (glint) {
            consumer = new TransformingVertexConsumer(buffers.getBuffer(RenderType.armorGlint()), mat);
            ccrs.bind(consumer, DefaultVertexFormat.NEW_ENTITY);
            ccrs.startDrawing(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
            addVertex(consumer, 0, 0, 0, 0, 0);
            addVertex(consumer, 0, 1, 0, 0, p / 4);
            addVertex(consumer, 1, 1, 0, p / 4, p / 4);
            addVertex(consumer, 1, 0, 0, p / 4, 0);
            ccrs.draw();
        }
        RenderUtils.endBatch(buffers);
    }

    private void drawDEBadge(CCRenderState ccrs, Matrix4 mat, MultiBufferSource buffers, TechLevel tier, ContributorConfig config, boolean onBack, boolean onArmor, boolean hasChestpiece) {
        float[] colour = ContributorConfig.unpack(config.getBaseColourI(tier));
        //Patreon Logo
        BCShaders.BADGE_FOIL_SHADER.getBaseColorUniform().glUniform4f(1F, 0.259F, 0.302F, 1F); //<= Patreon logo colour
        drawBadge(ccrs, mat.copy(), buffers, BCShaders.BADGE_FOIL_SHADER, PATREON_LOGO_TYPE, onBack, onArmor, hasChestpiece, 0, false, 0);
        //Patreon Logo Outline
        drawBadge(ccrs, mat.copy(), buffers, BCShaders.BADGE_OUTLINE_SHADER, PATREON_LOGO_OUTLINE_TYPE, onBack, onArmor, hasChestpiece, 0, false, 0);
        //Core
        BCShaders.BADGE_CORE_SHADER.getBaseColorUniform().glUniform4f(colour[0], colour[1], colour[2], colour[3]);
        drawBadge(ccrs, mat.copy(), buffers, BCShaders.BADGE_CORE_SHADER, PATREON_CORE_TYPE, onBack, onArmor, hasChestpiece, 0, false, 0);
    }

    private void renderWings(ContributorConfig config, LivingEntity entity, PoseStack poseStack, MultiBufferSource buffers, int packedLight, int packedOverlay, float partialTicks) {
        if (!props.hasWings() || !(entity instanceof Player player)) return;
        Animations anim = props.getAnim();
        anim.setPlayer(player);
        if (anim.hideDecay() == 1) return;

        //Colours
        float[] boneColour = config.getWingBoneColour(partialTicks);
        float[] webColour = config.getWingWebColour(partialTicks);
        boolean chaos = config.getWingRenderTier() == TechLevel.CHAOTIC && !config.overrideWingBoneColour();
        float rm = chaos && anim.hideDecay() != 0 ? 0.35F : 1F;
        chaos = chaos && anim.hideDecay() == 0;

        BCShaders.WINGS_BONE_SHADER.getBaseColorUniform().glUniform4f(boneColour[0] * rm, boneColour[1], boneColour[2], boneColour[3]);
        BCShaders.WINGS_WEB_SHADER.getBaseColorUniform().glUniform4f(webColour[0], webColour[1], webColour[2], webColour[3]);
        BCShaders.CONTRIB_BASE_SHADER.getDecayUniform().glUniform1f(anim.hideDecay());
        BCShaders.WINGS_BONE_SHADER.getDecayUniform().glUniform1f(anim.hideDecay());
        BCShaders.WINGS_WEB_SHADER.getDecayUniform().glUniform1f(anim.hideDecay());

        //Shader Toggles
        boolean fancyBones = config.getWingsBoneShader();
        boolean fancyWebs = config.getWingsWebShader();
        ContribShader webShader = fancyWebs ? BCShaders.WINGS_WEB_SHADER : BCShaders.CONTRIB_BASE_SHADER;
        RenderType webType = fancyWebs ? WEB_SHADER_TYPE : WEB_TYPE;

        //Render Config
        double yComp = config.getWingsWebShader() ? 0.1 : 0;
        double extension = anim.getWingExt(partialTicks);
        double fold = anim.getWingFold(partialTicks);
        double flap = anim.getWingFlap(partialTicks);
        double flap2 = anim.getWingFlap2(partialTicks);
        double pitch = anim.getWingPitch(partialTicks);
        float livingAnim = entity.tickCount + partialTicks;
        extension += (Mth.cos(livingAnim * 0.029F) * 0.05F + 0.05F);
        fold += Mth.sin(livingAnim * 0.067F) * 0.05F;
        Vector3 webOrigin = new Vector3(-1.5, 31, 2.8).multiply(1 / 16D);

        //Initial Matrix Setup
        poseStack.pushPose();
        body.translateAndRotate(poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(0, -1.5, 0);
        Matrix4 mat = new Matrix4(poseStack);
        poseStack.popPose();

        //Render Left
        Matrix4 lMat = mat.copy();
        humBone.xRot = Mth.lerp(pitch, 0, 15);
        humBone.yRot = Mth.lerp(fold, 45, 10) + Mth.lerp(flap, 0, 35);
        humBone.zRot = Mth.lerp(extension, -15, -80);
        humBone.render(lMat, buffers, fancyBones, chaos);
        radBone.zRot = Mth.lerp(extension, 30, 130);
        radBone.render(lMat, buffers, fancyBones, chaos);
        f1Bone.zRot = Mth.lerp(extension, 157.5, 40);
        f1Bone.xRot = Mth.lerp(flap2, 0, -25);
        f1Bone.render(lMat.copy(), buffers, fancyBones, chaos);
        f2Bone.zRot = Mth.lerp(extension, 165, 80);
        f2Bone.yRot = Mth.lerp(flap2, 0, -10);
        f2Bone.xRot = f2Bone.yRot;
        f2Bone.render(lMat.copy(), buffers, fancyBones, chaos);
        f3Bone.zRot = Mth.lerp(extension, 172.5, 140);
        f3Bone.xRot = Mth.lerp(flap2, 0, 5);
        f3Bone.yRot = f3Bone.xRot * -0.5F;
        f3Bone.render(lMat.copy(), buffers, fancyBones, chaos);

        renderWingWeb(webShader, webType, webOrigin, lMat.copy(), buffers, 24 + yComp, f1Bone.getRotation(), 17 + yComp, f2Bone.getRotation());
        renderWingWeb(webShader, webType, webOrigin, lMat.copy(), buffers, 17 + yComp, f2Bone.getRotation(), 14 + yComp, f3Bone.getRotation());
        renderWingWeb(webShader, webType, webOrigin, lMat.copy(), buffers, 14 + yComp, f3Bone.getRotation(), 17 + yComp, new Vector3(0, 0, 180 * torad)); //<--16

        //Render Right
        Matrix4 rMat = mat.copy();
        rMat.translate(3 * (1 / 16D), 0, 0);
        humBone.yRot *= -1;
        humBone.zRot *= -1;
        humBone.render(rMat, buffers, fancyBones, chaos);
        radBone.zRot *= -1;
        radBone.render(rMat, buffers, fancyBones, chaos);
        f1Bone.zRot *= -1;
        f1Bone.render(rMat.copy(), buffers, fancyBones, chaos);
        f2Bone.zRot *= -1;
        f2Bone.yRot *= -1;
        f2Bone.render(rMat.copy(), buffers, fancyBones, chaos);
        f3Bone.zRot *= -1;
        f3Bone.yRot *= -1;
        f3Bone.render(rMat.copy(), buffers, fancyBones, chaos);

        renderWingWeb(webShader, webType, webOrigin, rMat.copy(), buffers, 24 + yComp, f1Bone.getRotation(), 17 + yComp, f2Bone.getRotation());
        renderWingWeb(webShader, webType, webOrigin, rMat.copy(), buffers, 17 + yComp, f2Bone.getRotation(), 14 + yComp, f3Bone.getRotation());
        renderWingWeb(webShader, webType, webOrigin, rMat.copy(), buffers, 14 + yComp, f3Bone.getRotation(), 17 + yComp, new Vector3(0, 0, 180 * torad)); //<--16
    }

    public void renderWingWeb(BCShader<?> shader, RenderType renderType, Vector3 origin, Matrix4 mat, MultiBufferSource buffers, double len1, Vector3 rot1, double len2, Vector3 rot2) {
        mat.translate(origin);
        shader.getModelMatUniform().glUniformMatrix4f(mat);
        len1 *= 1 / 16D;
        len2 *= 1 / 16D;

        Vector3 v0 = Vector3.ZERO;
        Vector3 v1 = new Vector3(0, len1, 0).apply(new Rotation(rot1.z, Vector3.Z_POS)).apply(new Rotation(rot1.y, Vector3.Y_POS)).apply(new Rotation(rot1.x, Vector3.X_POS));
        Vector3 v2 = new Vector3(0, len2, 0).apply(new Rotation(rot2.z, Vector3.Z_POS)).apply(new Rotation(rot2.y, Vector3.Y_POS)).apply(new Rotation(rot2.x, Vector3.X_POS));

        CCRenderState ccrs = CCRenderState.instance();
        ccrs.reset();
        VertexConsumer consumer = buffers.getBuffer(renderType);
        ccrs.bind(consumer, DefaultVertexFormat.NEW_ENTITY);
        ccrs.startDrawing(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        addVertex(consumer, v1, 0, 1);
        addVertex(consumer, v0, 0.5F, 0);
        addVertex(consumer, v2, 1, 1);
        addVertex(consumer, v2, 1, 1);
        ccrs.draw();
        RenderUtils.endBatch(buffers);
    }

    private void addVertex(VertexConsumer consumer, Vector3 vert, float u, float v) {
        consumer.vertex(vert.x, vert.y, vert.z).color(0xFFFFFFFF).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(0.5F, 0.5F, 0.5F).endVertex();
    }

    private void addVertex(VertexConsumer consumer, double x, double y, double z, float u, float v) {
        consumer.vertex(x, y, z).color(0xFFFFFFFF).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).normal(0, 1, 0).endVertex();
    }

    private void addBasicVertex(VertexConsumer consumer, double x, double y, double z, float u, float v) {
        consumer.vertex(x, y, z).color(0xFFFFFFFF).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(240).endVertex();
    }

    private boolean isChestpiece(Item item) {
        return chestpiece1 != null && (item == chestpiece1 || item == chestpiece2 || item == chestpiece3);
    }

    //@formatter:off
    @Override public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float r, float g, float b, float a) {}
    @Override protected Iterable<ModelPart> headParts() { return ImmutableList.of(); }
    @Override protected Iterable<ModelPart> bodyParts() { return ImmutableList.of(); }
    //@formatter:on

    public static class WingBoneRenderer {
        private WingBoneRenderer shell = null;
        private final VBORenderType baseType;
        private VBORenderType fancyType;
        private VBORenderType chaosType;
        private final Vector3 pivot;
        public double xRot = 0;
        public double yRot = 0;
        public double zRot = 0;

        public WingBoneRenderer(CCModel model, Vector3 pivot, boolean core) {
            this.pivot = pivot.multiply(1 / 16D);
            this.baseType = new VBORenderType(BONE_TYPE, (format, builder) -> {
                CCRenderState ccrs = CCRenderState.instance();
                ccrs.reset();
                ccrs.bind(builder, format);
                model.render(ccrs);
            });
            if (core) {
                this.fancyType = new VBORenderType(BONE_SHADER_TYPE, (format, builder) -> {
                    CCRenderState ccrs = CCRenderState.instance();
                    ccrs.reset();
                    ccrs.bind(builder, format);
                    model.render(ccrs);
                });
                this.chaosType = new VBORenderType(BONE_CHAOS_TYPE, (format, builder) -> {
                    CCRenderState ccrs = CCRenderState.instance();
                    ccrs.reset();
                    ccrs.bind(builder, format);
                    model.render(ccrs);
                });
            }
        }

        public void render(Matrix4 mat, MultiBufferSource buffers) {
            render(mat, buffers, false, false);
        }

        public void render(Matrix4 mat, MultiBufferSource buffers, boolean fancy, boolean chaos) {
            if (xRot != 0) mat.apply(new Rotation(xRot * torad, Vector3.X_POS).at(pivot));
            if (yRot != 0) mat.apply(new Rotation(yRot * torad, Vector3.Y_POS).at(pivot));
            if (zRot != 0) mat.apply(new Rotation(zRot * torad, Vector3.Z_POS).at(pivot));
            VBORenderType type = fancy ? chaos ? chaosType : fancyType : baseType;
            BCShader<?> shader = fancy ? chaos ? BCShaders.CHAOS_ENTITY_SHADER : BCShaders.WINGS_BONE_SHADER : BCShaders.CONTRIB_BASE_SHADER;
            buffers.getBuffer(type.withCallback(() -> shader.getModelMatUniform().glUniformMatrix4f(mat)));
            RenderUtils.endBatch(buffers);

            if (shell != null && fancy) {
                shell.render(mat, buffers);
            }
        }

        public Vector3 getRotation() {
            return new Vector3(xRot, yRot, zRot).multiply(torad);
        }
    }

    private static RenderType createBoneType(String name, ContribShader shader) {
        return RenderType.create(MODID + ":" + name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, false, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(shader::getShaderInstance))
                .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(MODID, "textures/contributor/contributor_wings_bones.png"), false, false))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)
        );
    }

    private static RenderType createChaosType(String name, BCShader<?> shader) {
        return RenderType.create(MODID + ":" + name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(shader::getShaderInstance))
                .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(MODID, "textures/chaos_shader.png"), true, false))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)
        );
    }

    private static RenderType createWebType(String name, ContribShader shader) {
        return RenderType.create(MODID + ":" + name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(shader::getShaderInstance))
                .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation(MODID, "textures/contributor/contributor_wings_web.png"), false, false))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false)
        );
    }

    private static RenderType createBadgeType(String name, BCShader<?> shader, ResourceLocation texture, boolean blur) {
        return RenderType.create(MODID + ":badge" + name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(shader::getShaderInstance))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, blur, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)
        );
    }

    //    private static RenderType createBasicBadgeType(ResourceLocation texture, boolean blur) {
//        return RenderType.create(MODID + ":basic_badge", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, RenderType.CompositeState.builder()
//                .setShaderState(RenderType.RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
//                .setTextureState(new RenderStateShard.TextureStateShard(texture, blur, false))
//                .setTransparencyState(RenderType.NO_TRANSPARENCY)
//                .setCullState(RenderType.NO_CULL)
//                .setLightmapState(RenderType.LIGHTMAP)
//                .setOverlayState(RenderType.OVERLAY)
//                .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
//                .createCompositeState(true));
//    }

    private static RenderType createBasicBadgeType(ResourceLocation texture, boolean blur) {
        return RenderType.create(MODID + ":basic_badge", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, blur, false))
                .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderType.NO_CULL)
                .setLightmapState(RenderType.LIGHTMAP)
                .setOverlayState(RenderType.OVERLAY)
                .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true));
    }
}