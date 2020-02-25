package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.utils.GuiHelper;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

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

        if (trackMouse && !(renderEntity instanceof LivingEntity)) {
            error("[Broken Entity. track_mouse is only supported with living entities]");
        }

        if (drawName && !(renderEntity instanceof PlayerEntity)) {
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
            int scale = (int) ((size / Math.max(renderEntity.getHeight(), renderEntity.getWidth())) * drawScale);
            double posX = xPos + (size / 2D);
            double entityRotation = rotation + ((BCClientEventHandler.elapsedTicks + partialTicks) * rotateSpeed);

            renderEntity.ticksExisted = BCClientEventHandler.elapsedTicks;

            GlStateManager.pushMatrix();
            GlStateManager.translated(0, 0, 25 + getRenderZLevel() + scale);
            GlStateManager.color4f(1, 1, 1, 1);

            int eyeOffset = (int) ((renderEntity.getHeight() - renderEntity.getEyeHeight()) * scale);
            if (renderEntity instanceof LivingEntity) {
                drawEntityOnScreen((int) posX, yPos + ySize(), scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (LivingEntity) renderEntity, trackMouse, entityRotation, drawName);
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
            double ar = renderEntity.getHeight() / renderEntity.getWidth();
            double width = size * drawScale;
            double height = size * drawScale;
            if (ar > 1) {
                width *= renderEntity.getWidth() / renderEntity.getHeight();
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
        GlStateManager.translated((float) posX, (float) posY, 50.0F);
        GlStateManager.scalef((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        GlStateManager.rotatef(135.0F + (float) rotation, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotatef(-135.0F, 0.0F, 1.0F, 0.0F);
//        GlStateManager.rotate(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
//        ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
//        ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        GlStateManager.translatef(0.0F, 0.0F, 0.0F);
        EntityRendererManager rendermanager = Minecraft.getInstance().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, LivingEntity ent, boolean trackMouse, double noTrackRotation, boolean drawName) {
        float rotation = trackMouse ? 0 : (float) noTrackRotation;
        if (!trackMouse) {
            mouseX = 0;
            mouseY = 0;
        }

        if (ent instanceof EnderDragonEntity && trackMouse) {
            mouseY += scale * 16;
        }

        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) posX, (float) posY, 50.0F);
        GlStateManager.scalef((float) (-scale), (float) scale, (float) scale);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotatef(135.0F + rotation, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotatef(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(-((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float) Math.atan((double) (mouseX / 40.0F)) * 20.0F;
        ent.rotationYaw = (float) Math.atan((double) (mouseX / 40.0F)) * 40.0F;
        ent.rotationPitch = -((float) Math.atan((double) (mouseY / 40.0F))) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        if (ent instanceof EnderDragonEntity) {
            GlStateManager.rotated(ent.rotationPitch, 1, 0, 0);
            GlStateManager.rotated(-ent.rotationYawHead + 180, 0, 1, 0);
        }

        GlStateManager.translatef(0.0F, 0.0F, 0.0F);
        EntityRendererManager rendermanager = Minecraft.getInstance().getRenderManager();
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
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    public Entity getRenderEntity(ClientWorld world, String entityString, EquipmentHelper helper) {
        String ident = String.format("%s|mh:%s|oh:%s|h:%s|ch:%s|le:%s|bo:%s", entityString, mainHand, offHand, head, chest, legs, boots);
        if (renderEntityCache.containsKey(ident) && !animate) {
            return renderEntityCache.get(ident);
        }

        if (!entityString.contains(":")) {
            throw new IllegalArgumentException("Invalid entity string! Must be ether modid:entityName or player:username");
        }

        Entity entity = null;
        if (entityString.startsWith("player:")) {
            entity = createRenderPlayer(world, entityString.replaceFirst("player:", ""));
        }
        else {
            EntityType type = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityString));
            if (type != null) {
                entity = type.create(world);
            }
        }

        if (entity == null) {
            throw new IllegalArgumentException("No matching entity found for string: " + entityString);
        }

        if (entity instanceof LivingEntity && helper.hasEquipment) {
            helper.apply((LivingEntity) entity);
        }
        else if (helper.hasEquipment) {
            throw new IllegalArgumentException("The specified entity does not allow equipment!");
        }

        if (!animate) {
            renderEntityCache.put(ident, entity);
        }
        return entity;
    }

    public static PlayerEntity createRenderPlayer(ClientWorld world, String username) {
        return new RemoteClientPlayerEntity(world, SkullTileEntity.updateGameProfile(new GameProfile(null, username))) {
            @Override
            public String getSkinType() {
                return super.getSkinType();
            }

            @Override
            public ResourceLocation getLocationSkin() {
                ResourceLocation resourcelocation;

                Minecraft minecraft = Minecraft.getInstance();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(getGameProfile());

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    resourcelocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
                else {
                    UUID uuid = PlayerEntity.getUUID(getGameProfile());
                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                }

                return resourcelocation;
            }

            @Override
            public boolean isWearing(PlayerModelPart part) {
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
                renderEntity.tick();
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

        public void apply(LivingEntity entity) {
            if (!mainHand.isEmpty()) entity.setHeldItem(Hand.MAIN_HAND, mainHand);
            if (!offHand.isEmpty()) entity.setHeldItem(Hand.OFF_HAND, offHand);
            if (!head.isEmpty()) entity.setItemStackToSlot(EquipmentSlotType.HEAD, head);
            if (!chest.isEmpty()) entity.setItemStackToSlot(EquipmentSlotType.CHEST, chest);
            if (!legs.isEmpty()) entity.setItemStackToSlot(EquipmentSlotType.LEGS, legs);
            if (!boots.isEmpty()) entity.setItemStackToSlot(EquipmentSlotType.FEET, boots);
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
