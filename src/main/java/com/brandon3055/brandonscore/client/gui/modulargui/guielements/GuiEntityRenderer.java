package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        this.entityName = EntityList.getKey(entity);

        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    public GuiEntityRenderer setEntity(ResourceLocation entity) {
        this.entityName = entity;
        this.entity = entityCache.computeIfAbsent(entity, resourceLocation -> EntityList.createEntityByIDFromName(entity, mc.world));

        if (invalidEntities.contains(entityName)) {
            invalidEntity = true;
        }

        return this;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        mouseX = mouseY = 0;

        if (invalidEntity) {
            return;
        }

        try {
            if (entity != null) {
                float scale = ySize() / entity.height;

                double zLevel = getRenderZLevel() + 100;
                double posX = xPos() + (xSize() / 2D);

                if (entity instanceof EntityLivingBase || entity instanceof EntityMinecart) {
                    Entity ent = entity;
                    GlStateManager.enableColorMaterial();
                    GlStateManager.pushMatrix();

                    float rotation = isRotationLocked() ? getLockedRotation() : (BCClientEventHandler.elapsedTicks + partialTicks) * getRotationSpeedMultiplier();

                    GlStateManager.translate((float) posX, (float) yPos() + (entity.height * scale), zLevel);
                    GlStateManager.scale(-scale, scale, scale);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
                    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
                    rendermanager.setPlayerViewY(180.0F);
                    rendermanager.setRenderShadow(false);
                    rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
                    rendermanager.setRenderShadow(true);

                    GlStateManager.popMatrix();
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                    GlStateManager.disableTexture2D();
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
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

    public GuiEntityRenderer setRotationSpeedMultiplier(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
        return this;
    }

    public float getRotationSpeedMultiplier() {
        return rotationSpeed;
    }
}
