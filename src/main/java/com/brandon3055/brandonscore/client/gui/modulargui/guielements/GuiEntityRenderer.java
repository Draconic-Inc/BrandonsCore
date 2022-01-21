package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.List;
import java.util.*;

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
                    RenderSystem.translated(0, 0, zLevel);
                    renderEntityOnScreen((int) posX, yPos, (int) scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (LivingEntity) entity, rotation, trackMouse, drawName);
                    RenderSystem.translated(0, 0, -zLevel);
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

    public static void renderEntityOnScreen(int xPos, int yPos, int scale, float mouseX, float mouseY, LivingEntity entity, double rotation, boolean trackMouse, boolean drawName) {
        float lookX = trackMouse ? (float) Math.atan((double) (mouseX / 40.0F)) : 0;
        float lookY = trackMouse ? (float) Math.atan((double) (mouseY / 40.0F)) : 0;
        if (drawName && entity instanceof RemoteClientPlayerEntity && Minecraft.getInstance().player != null) {
            entity.setPos(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ());
        } else if (entity instanceof RemoteClientPlayerEntity) {
            entity.setPos(0, -1000, 0);
        }

        RenderSystem.pushMatrix();
        RenderSystem.translatef((float) xPos, (float) yPos, 1050.0F);
        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
        MatrixStack matrixstack = new MatrixStack();
        matrixstack.translate(0.0D, 0.0D, 1000.0D);
        matrixstack.scale((float) scale, (float) scale, (float) scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(lookY * 20.0F);
        quaternion.mul(quaternion1);
        matrixstack.mulPose(quaternion);
        matrixstack.mulPose(Vector3f.YP.rotationDegrees((float) rotation));
        float f2 = entity.yBodyRot;
        float f3 = entity.yRot;
        float f4 = entity.xRot;
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + lookX * 20.0F;
        entity.yRot = 180.0F + lookX * 40.0F;
        entity.xRot = -lookY * 20.0F;
        entity.yHeadRot = entity.yRot;
        entity.yHeadRotO = entity.yRot;
        EntityRendererManager rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        rendererManager.overrideCameraOrientation(quaternion1);
        rendererManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            rendererManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
        });
        irendertypebuffer$impl.endBatch();
        rendererManager.setRenderShadow(true);
        entity.yBodyRot = f2;
        entity.yRot = f3;
        entity.xRot = f4;
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        RenderSystem.popMatrix();
    }

    public static PlayerEntity createRenderPlayer(ClientWorld world, String username) {
        return new RemoteClientPlayerEntity(world, SkullTileEntity.updateGameprofile(new GameProfile(null, username))) {
            @Override
            public String getModelName() {
                return super.getModelName();
            }

            @Override
            public ResourceLocation getSkinTextureLocation() {
                ResourceLocation resourcelocation;

                Minecraft minecraft = Minecraft.getInstance();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(getGameProfile());

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    resourcelocation = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                } else {
                    UUID uuid = PlayerEntity.createPlayerUUID(getGameProfile());
                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                }

                return resourcelocation;
            }

            @Override
            public boolean isModelPartShown(PlayerModelPart part) {
                return true;
            }
        };
    }
}
