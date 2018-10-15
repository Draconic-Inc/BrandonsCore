package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.lib.StackReference;
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
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class EntityElement extends MDElementBase<EntityElement> {

    private static Map<String, Entity> renderEntityCache = new HashMap<>();
    public int xOffset = 0;
    public int yOffset = 0;
    public double rotateSpeed = 0;
    public double rotation = 0;
    public double drawScale = 1;
    public boolean animate = false;
    public boolean trackMouse = false;
    public boolean drawName = false;
    public String mainHand = "";
    public String offHand = "";
    public String head = "";
    public String chest = "";
    public String legs = "";
    public String boots = "";
    private Entity renderEntity = null;
    private String entityName;
    private boolean errored = false;

    public EntityElement(String entityName) {
        this.entityName = entityName;
        this.size = 64;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        setSize(size, size);

        try {
            renderEntity = getRenderEntity(mc.world, entityName, EquipmentHelper.create(this));
        }
        catch (IllegalArgumentException e) {
            error("[Broken Entity. " + e.getMessage() + "]");
        }

        if (trackMouse && !(renderEntity instanceof EntityLivingBase)) {
            error("[Broken Entity. track_mouse is only supported with living entities]");
        }

        if (drawName && !(renderEntity instanceof EntityPlayer)) {
            error("[Broken Entity. draw_name is only supported by player's]");
        }

        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (errored) return;
        int xPos = xPos() + xOffset;
        int yPos = yPos() + yOffset;

        try {
            int scale = (int) ((size / Math.max(renderEntity.height, renderEntity.width)) * drawScale);
            double posX = xPos + (size / 2D);
            double entityRotation = rotation + ((BCClientEventHandler.elapsedTicks + partialTicks) * rotateSpeed);

            renderEntity.ticksExisted = BCClientEventHandler.elapsedTicks;

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0, 25 + getRenderZLevel() + scale);
            GlStateManager.color(1, 1, 1, 1);

            int eyeOffset = (int) ((renderEntity.height - renderEntity.getEyeHeight()) * scale);
            if (renderEntity instanceof EntityLivingBase) {
                drawEntityOnScreen((int) posX, yPos + ySize(), scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (EntityLivingBase) renderEntity, trackMouse, entityRotation, drawName);
            }
            else {
                drawEntityOnScreen((int) posX, yPos + ySize(), scale, renderEntity, entityRotation);
            }

            GlStateManager.popMatrix();
        }
        catch (Throwable e) {
            LogHelperBC.error("Something went wrong while attempting to render an entity on the screen!");
            LogHelperBC.error("Entity: " + renderEntity);
            e.printStackTrace();
            errored = true;
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFF00FF00);
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!tooltip.isEmpty() && !errored && renderEntity != null) {
            double ar = renderEntity.height / renderEntity.width;
            double width = size * drawScale;
            double height = size * drawScale;
            if (ar > 1) {
                width *= renderEntity.width / renderEntity.height;
            }
            else {
                height *= ar;
            }
            int x = (int) (xPos() + xOffset + ((xSize() - width) / 2));
            int y = (int) (yPos() + (ySize() - height) + yOffset);
            if (GuiHelper.isInRect(x, y, (int) width, (int) height, mouseX, mouseY)) {
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, Entity ent, double rotation) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
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

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent, boolean trackMouse, double noTrackRotation, boolean drawName) {
        float rotation = trackMouse ? 0 : (float) noTrackRotation;
        if (!trackMouse) {
            mouseX = 0;
            mouseY = 0;
        }

        if (ent instanceof EntityDragon && trackMouse) {
            mouseY += scale * 16;
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX, (float) posY, 50.0F);
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
        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float) Math.atan((double) (mouseX / 40.0F)) * 20.0F;
        ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
        ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        if (ent instanceof EntityDragon) {
            GlStateManager.rotate(ent.rotationPitch, 1, 0, 0);
            GlStateManager.rotate(-ent.rotationYawHead + 180, 0, 1, 0);
        }

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

    public Entity getRenderEntity(World world, String entityString, EquipmentHelper helper) {
        String ident = String.format("%s|mh:%s|oh:%s|h:%s|ch:%s|le:%s|bo:%s", entityString, mainHand, offHand, head, chest, legs, boots);
        if (renderEntityCache.containsKey(ident) && !animate) {
            return renderEntityCache.get(ident);
        }

        if (!entityString.contains(":")) {
            throw new IllegalArgumentException("Invalid entity string! Must be ether modid:entityName or player:username");
        }

        Entity entity;
        if (entityString.startsWith("player:")) {
            entity = createRenderPlayer(world, entityString.replaceFirst("player:", ""));
        }
        else {
            entity = EntityList.createEntityByIDFromName(new ResourceLocation(entityString), world);
        }

        if (entity == null) {
            throw new IllegalArgumentException("No matching entity found for string: " + entityString);
        }

        if (entity instanceof EntityLivingBase && helper.hasEquipment) {
            helper.apply((EntityLivingBase) entity);
        }
        else if (helper.hasEquipment) {
            throw new IllegalArgumentException("The specified entity does not allow equipment!");
        }

        if (!animate) {
            renderEntityCache.put(ident, entity);
        }
        return entity;
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

    boolean animateBroken = false;
    int animTick = 0;

    @Override
    public boolean onUpdate() {
        if (animate && renderEntity != null && !errored && !animateBroken && animTick != BCClientEventHandler.elapsedTicks) {
            try {
                renderEntity.ticksExisted = animTick;
                animTick = BCClientEventHandler.elapsedTicks;
                renderEntity.onUpdate();
            }
            catch (Throwable e) {
                animateBroken = true;
            }
        }
        return super.onUpdate();
    }

    static class EquipmentHelper {
        private ItemStack mainHand = ItemStack.EMPTY;
        private ItemStack offHand = ItemStack.EMPTY;
        private ItemStack head = ItemStack.EMPTY;
        private ItemStack chest = ItemStack.EMPTY;
        private ItemStack legs = ItemStack.EMPTY;
        private ItemStack boots = ItemStack.EMPTY;
        public boolean hasEquipment = false;

        public void apply(EntityLivingBase entity) {
            if (!mainHand.isEmpty()) entity.setHeldItem(EnumHand.MAIN_HAND, mainHand);
            if (!offHand.isEmpty()) entity.setHeldItem(EnumHand.OFF_HAND, offHand);
            if (!head.isEmpty()) entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, head);
            if (!chest.isEmpty()) entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
            if (!legs.isEmpty()) entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
            if (!boots.isEmpty()) entity.setItemStackToSlot(EntityEquipmentSlot.FEET, boots);
        }

        public static EquipmentHelper create(EntityElement element) {
            EquipmentHelper helper = new EquipmentHelper();

            if (!element.mainHand.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.mainHand);
                if (stackRef == null || (helper.mainHand = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.mainHand);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!element.offHand.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.offHand);
                if (stackRef == null || (helper.offHand = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.offHand);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!element.head.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.head);
                if (stackRef == null || (helper.head = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.head);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!element.chest.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.chest);
                if (stackRef == null || (helper.chest = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.chest);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!element.legs.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.legs);
                if (stackRef == null || (helper.legs = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.legs);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!element.boots.isEmpty()) {
                StackReference stackRef = StackReference.fromString(element.boots);
                if (stackRef == null || (helper.boots = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.boots);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            return helper;
        }
    }
}
