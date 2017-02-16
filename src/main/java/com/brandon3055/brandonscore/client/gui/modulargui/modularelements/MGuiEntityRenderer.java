package com.brandon3055.brandonscore.client.gui.modulargui.modularelements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.IModularGui;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class MGuiEntityRenderer extends MGuiElementBase {
    private static Map<String, Entity> entityCache = new HashMap<>();
    private static List<String> invalidEntities = new ArrayList<>();

    private Entity entity;
    private String eString;
    private boolean invalidEntitie = false;

    public MGuiEntityRenderer(IModularGui modularGui) {
        super(modularGui);
    }

    public MGuiEntityRenderer(IModularGui modularGui, int xPos, int yPos) {
        super(modularGui, xPos, yPos);
    }

    public MGuiEntityRenderer(IModularGui modularGui, int xPos, int yPos, int xSize, int ySize) {
        super(modularGui, xPos, yPos, xSize, ySize);
    }

    public MGuiEntityRenderer setEntity(Entity entity) {
        this.entity = entity;
        this.eString = EntityList.getEntityString(entity);

        if (invalidEntities.contains(eString)) {
            invalidEntitie = true;
        }

        return this;
    }

    public MGuiEntityRenderer setEntity(String entity) {
        if (!entityCache.containsKey(entity)) {
            entityCache.put(entity, EntityList.createEntityByName(entity, modularGui.getMinecraft().theWorld));
        }
        this.eString = entity;
        this.entity = entityCache.get(entity);

        if (invalidEntities.contains(eString)) {
            invalidEntitie = true;
        }

        return this;
    }

    @Override
    public void renderBackgroundLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        mouseX = mouseY = 0;
        super.renderBackgroundLayer(minecraft, mouseX, mouseY, partialTicks);

        if (invalidEntitie) {
            return;
        }

        try {
            if (entity != null) {
                float scale = ySize / entity.height;

                double zLevel = getRenderZLevel() + 100;
                double posX = xPos + (xSize / 2D);

                if (entity instanceof EntityLivingBase || entity instanceof EntityMinecart) {
                    Entity ent = entity;
                    GlStateManager.enableColorMaterial();
                    GlStateManager.pushMatrix();

                    GlStateManager.translate((float) posX, (float) yPos + (entity.height * scale), zLevel);
                    GlStateManager.scale(-scale, scale, scale);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(BCClientEventHandler.elapsedTicks, 0.0F, 1.0F, 0.0F);
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
            invalidEntitie = true;
            invalidEntities.add(eString);
            LogHelperBC.error("Failed to render entity in GUI. This is not a bug there are just some entities that can not be rendered like this.");
            LogHelperBC.error("Entity: " + entity);
            e.printStackTrace();
        }
    }
}
