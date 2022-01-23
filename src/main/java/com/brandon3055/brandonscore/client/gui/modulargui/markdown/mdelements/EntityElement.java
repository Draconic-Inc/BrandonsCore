package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.matrix.MatrixStack;
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
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
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
            renderEntity = getRenderEntity(mc.level, entityName, EquipmentHelper.create(this));
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
        if (errored || renderEntity == null) {
            return;
        }
        int xPos = xPos() + xOffset;
        int yPos = yPos() + yOffset;

        int scale = (int) ((size / Math.max(renderEntity.getBbHeight(), renderEntity.getBbWidth())) * drawScale);
        double posX = xPos + (size / 2D);
        double entityRotation = rotation + ((BCClientEventHandler.elapsedTicks + partialTicks) * rotateSpeed);
        renderEntity.tickCount = BCClientEventHandler.elapsedTicks;
        double zLevel = 25 + getRenderZLevel() + scale;

        try {
            if (renderEntity instanceof LivingEntity) {
                int eyeOffset = (int) ((renderEntity.getBbHeight() - renderEntity.getEyeHeight()) * scale);
                RenderSystem.translated(0, 0, zLevel);
                renderEntityOnScreen((int) posX, yPos + ySize(), (int) scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (LivingEntity) renderEntity, entityRotation, trackMouse, drawName);
                RenderSystem.translated(0, 0, -zLevel);
            }
        }
        catch (Throwable e) {
            BrandonsCore.LOGGER.error("Something went wrong while attempting to render an entity on the screen!");
            BrandonsCore.LOGGER.error("Entity: " + renderEntity);
            e.printStackTrace();
            errored = true;
        }
    }

    @Override
    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        if (!tooltip.isEmpty() && !errored && renderEntity != null) {
            double ar = renderEntity.getBbHeight() / renderEntity.getBbWidth();
            double width = size * drawScale;
            double height = size * drawScale;
            if (ar > 1) {
                width *= renderEntity.getBbWidth() / renderEntity.getBbHeight();
            } else {
                height *= ar;
            }
            int x = (int) (xPos() + xOffset + ((xSize() - width) / 2));
            int y = (int) (yPos() + (ySize() - height) + yOffset);
            if (GuiHelperOld.isInRect(x, y, (int) width, (int) height, mouseX, mouseY)) {
                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
                return true;
            }
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
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
        } else {
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
        } else if (helper.hasEquipment) {
            throw new IllegalArgumentException("The specified entity does not allow equipment!");
        }

        if (!animate) {
            renderEntityCache.put(ident, entity);
        }
        return entity;
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

    boolean animateBroken = false;
    int animTick = 0;

    @Override
    public boolean onUpdate() {
        if (animate && renderEntity != null && !errored && !animateBroken && animTick != BCClientEventHandler.elapsedTicks) {
            try {
                renderEntity.tickCount = animTick;
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
            if (!mainHand.isEmpty()) entity.setItemInHand(Hand.MAIN_HAND, mainHand);
            if (!offHand.isEmpty()) entity.setItemInHand(Hand.OFF_HAND, offHand);
            if (!head.isEmpty()) entity.setItemSlot(EquipmentSlotType.HEAD, head);
            if (!chest.isEmpty()) entity.setItemSlot(EquipmentSlotType.CHEST, chest);
            if (!legs.isEmpty()) entity.setItemSlot(EquipmentSlotType.LEGS, legs);
            if (!boots.isEmpty()) entity.setItemSlot(EquipmentSlotType.FEET, boots);
        }

        public static EquipmentHelper create(EntityElement element) {
            EquipmentHelper helper = new EquipmentHelper();

            try {
                if (!element.mainHand.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.mainHand);
                    if (stackRef == null || (helper.mainHand = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.mainHand);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.offHand.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.offHand);
                    if (stackRef == null || (helper.offHand = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.offHand);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.head.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.head);
                    if (stackRef == null || (helper.head = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.head);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.chest.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.chest);
                    if (stackRef == null || (helper.chest = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.chest);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.legs.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.legs);
                    if (stackRef == null || (helper.legs = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.legs);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.boots.isEmpty()) {
                    StackReference stackRef = StackReference.fromString(element.boots);
                    if (stackRef == null || (helper.boots = stackRef.createStack()) == null) {
                        LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + element.boots);
                    } else {
                        helper.hasEquipment = true;
                    }
                }
            }
            catch (Throwable e) {
                BrandonsCore.LOGGER.warn("[Entity Element] An error occurred while parsing stack string.");
                e.printStackTrace();
            }

            return helper;
        }
    }
}
