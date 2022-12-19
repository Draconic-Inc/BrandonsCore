package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class GuiEntityRenderer extends GuiElement<GuiEntityRenderer> {
    private static Map<ResourceLocation, Entity> entityCache = new HashMap<>();
    private static List<ResourceLocation> invalidEntities = new ArrayList<>();

    private float rotationSpeed = 1;
    private float lockedRotation = 0;
    private Entity entity;
    private ResourceLocation entityName;
    private boolean invalidEntity = false;
    private boolean rotationLocked = false;
    private boolean trackMouse = false;
    private boolean drawName = false;
    public boolean silentErrors = false;
    public boolean force2dSize = false;

    public GuiEntityRenderer() {
    }

    public GuiEntityRenderer(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiEntityRenderer(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    public GuiEntityRenderer setEntity(Entity entity) {
        this.entity = entity;
        if (this.entity == null) {
            if (!silentErrors) {
                LogHelperBC.dev("GuiEntityRenderer#setEntity: Invalid Entity - " + entityName);
            }
            invalidEntity = true;
            return this;
        }

        this.entityName = entity.getType().getRegistryName();
        invalidEntity = false;

        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    public GuiEntityRenderer setEntity(ResourceLocation entity) {
        this.entityName = entity;
        this.entity = entityCache.computeIfAbsent(entity, resourceLocation -> {
            EntityType type = ForgeRegistries.ENTITIES.getValue(entity);
            return type == null ? null : type.create(mc.level);
        });
        invalidEntity = false;

        if (this.entity == null) {
            if (!silentErrors) {
                LogHelperBC.dev("GuiEntityRenderer#setEntity: Invalid Entity - " + entityName);
            }
            invalidEntity = true;
        }

        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    public GuiEntityRenderer setSilentErrors(boolean silentErrors) {
        this.silentErrors = silentErrors;
        return this;
    }

    public GuiEntityRenderer setForce2dSize(boolean force2dSize) {
        this.force2dSize = force2dSize;
        return this;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);

        if (invalidEntity && !silentErrors) {
            LogHelperBC.dev("GuiEntityRenderer: Invalid Entity - " + entityName);
            return;
        }

        try {
            if (entity != null) {
                Rectangle rect = getInsetRect();
                float scale = (float) (force2dSize ? (Math.min(rect.height / entity.getBbHeight(), rect.width / entity.getBbWidth())) : rect.height / entity.getBbHeight());

                double zLevel = getRenderZLevel() + 100;
                double posX = rect.x + (rect.width / 2D);
                int yPos = (yPos() + (ySize() / 2)) + (rect.height / 2);

                float rotation = isRotationLocked() ? getLockedRotation() : (BCClientEventHandler.elapsedTicks + partialTicks) * getRotationSpeedMultiplier();
                if (entity instanceof LivingEntity) {
                    int eyeOffset = (int) ((entity.getBbHeight() - entity.getEyeHeight()) * scale);
                    renderEntityOnScreen((int) posX, yPos, (int) scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (LivingEntity) entity, rotation, trackMouse, drawName, zLevel);
                } else {
//                    drawEntityOnScreen(posX, rect.y, scale, entity, rotation, zLevel);
                }
            }
        }
        catch (Throwable e) {
            invalidEntity = true;
            invalidEntities.add(entityName);
            LogHelperBC.error("Failed to build entity in GUI. This is not a bug there are just some entities that can not be rendered like this.");
            LogHelperBC.error("Entity: " + entity);
            e.printStackTrace();
        }
    }

    public boolean isRotationLocked() {
        return rotationLocked;
    }

    public GuiEntityRenderer rotationLocked(boolean rotationLocked) {
        this.rotationLocked = rotationLocked;
        return this;
    }

    public GuiEntityRenderer setLockedRotation(float lockedRotation) {
        this.lockedRotation = lockedRotation;
        rotationLocked(true);
        return this;
    }

    public float getLockedRotation() {
        return lockedRotation;
    }

    public boolean isInvalidEntity() {
        return invalidEntity;
    }

    public GuiEntityRenderer setRotationSpeedMultiplier(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public GuiEntityRenderer setTrackMouse(boolean trackMouse) {
        this.trackMouse = trackMouse;
        return this;
    }

    public GuiEntityRenderer setDrawName(boolean drawName) {
        this.drawName = drawName;
        return this;
    }

    public float getRotationSpeedMultiplier() {
        return rotationSpeed;
    }

    public static void renderEntityOnScreen(int xPos, int yPos, int scale, float mouseX, float mouseY, LivingEntity entity, double rotation, boolean trackMouse, boolean drawName, double zOffset) {
        float lookX = trackMouse ? (float) Math.atan((double) (mouseX / 40.0F)) : 0;
        float lookY = trackMouse ? (float) Math.atan((double) (mouseY / 40.0F)) : 0;
        if (drawName && entity instanceof RemotePlayer && Minecraft.getInstance().player != null) {
            entity.setPos(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ());
        } else if (entity instanceof RemotePlayer) {
            entity.setPos(0, -1000, 0);
        }

        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate((double)xPos, (double)yPos, 1050.0D + zOffset);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float) scale, (float) scale, (float) scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(lookY * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        posestack1.mulPose(Vector3f.YP.rotationDegrees((float) rotation));

        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + lookX * 20.0F;
        entity.setYRot(180.0F + lookX * 40.0F);
        entity.setXRot(-lookY * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }

//    public static Player createRenderPlayer(ClientLevel world, String username) {
//        return new RemotePlayer(world, SkullBlockEntity.updateGameprofile(new GameProfile(null, username))) {
//            @Override
//            public String getModelName() {
//                return super.getModelName();
//            }
//
//            @Override
//            public ResourceLocation getSkinTextureLocation() {
//                ResourceLocation resourcelocation;
//
//                Minecraft minecraft = Minecraft.getInstance();
//                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(getGameProfile());
//
//                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
//                    resourcelocation = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
//                } else {
//                    UUID uuid = Player.createPlayerUUID(getGameProfile());
//                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
//                }
//
//                return resourcelocation;
//            }
//
//            @Override
//            public boolean isModelPartShown(PlayerModelPart part) {
//                return true;
//            }
//        };
//    }
}
