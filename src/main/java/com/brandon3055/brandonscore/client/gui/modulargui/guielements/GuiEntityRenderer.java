package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class GuiEntityRenderer extends MGuiElementBase<GuiEntityRenderer> {
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

        this.entityName = EntityList.getKey(entity);
        invalidEntity = false;

        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    public GuiEntityRenderer setEntity(ResourceLocation entity) {
        this.entityName = entity;
        this.entity = entityCache.computeIfAbsent(entity, resourceLocation -> EntityList.createEntityByIDFromName(entity, mc.world));
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
                float scale = (float) (force2dSize ? (Math.min(rect.height / entity.height, rect.width / entity.width)) : rect.height / entity.height);

                double zLevel = getRenderZLevel() + 100;
                double posX = rect.x + (rect.width/ 2D);

                float rotation = isRotationLocked() ? getLockedRotation() : (BCClientEventHandler.elapsedTicks + partialTicks) * getRotationSpeedMultiplier();
                if (entity instanceof EntityLivingBase) {
                    int eyeOffset = (int) ((entity.height - entity.getEyeHeight()) * scale);
                    drawEntityOnScreen(posX, rect.y, scale, (int) posX - mouseX, rect.y - mouseY + eyeOffset, (EntityLivingBase) entity, trackMouse, rotation, drawName, zLevel);
                }
                else {
                    drawEntityOnScreen(posX, rect.y, scale, entity, rotation, zLevel);
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

    public static void drawEntityOnScreen(double posX, double posY, double scale, Entity ent, double rotation, double zOffset) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();

//        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.translate((float) posX, (float) posY + (ent.height * scale), zOffset);

        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        GlStateManager.rotate(135.0F + (float) rotation, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
//        ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
//        ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void drawEntityOnScreen(double posX, double posY, double scale, double mouseX, double mouseY, EntityLivingBase ent, boolean trackMouse, double noTrackRotation, boolean drawName, double zOffset) {
        float rotation = trackMouse ? 0 : (float) noTrackRotation;
        if (!trackMouse) {
            mouseX = 0;
            mouseY = 0;
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
//        GlStateManager.translate((float) posX, (float) posY, 50.0F);
        GlStateManager.translate((float) posX, (float) posY + (ent.height * scale), zOffset);

        GlStateManager.scale((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F + rotation, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float) Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float) Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float) Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float) Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F + rotation + (drawName ? 0 : 180));
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static EntityPlayer createRenderPlayer(World world, String username) {
        return new EntityOtherPlayerMP(world, TileEntitySkull.updateGameprofile(new GameProfile(null, username))) {
            @Override
            public String getSkinType() {
                return super.getSkinType();
            }

            @Override
            public ResourceLocation getLocationSkin() {
                ResourceLocation resourcelocation;

                Minecraft minecraft = Minecraft.getMinecraft();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(getGameProfile());

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    resourcelocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
                else {
                    UUID uuid = EntityPlayer.getUUID(getGameProfile());
                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                }

                return resourcelocation;
            }

            @Override
            public boolean isWearing(EnumPlayerModelParts part) {
                return true;
            }
        };
    }
}
