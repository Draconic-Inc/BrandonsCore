package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.utils.GuiHelperOld;
import com.brandon3055.brandonscore.lib.StringyStacks;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class EntityElement extends MDElementBase<EntityElement> {
    private static final Logger LOGGER = LogManager.getLogger(EntityElement.class);

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

        if (drawName && !(renderEntity instanceof Player)) {
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
//                RenderSystem.translated(0, 0, zLevel);
//                renderEntityOnScreen((int) posX, yPos + ySize(), (int) scale, (int) posX - mouseX, yPos() - mouseY + eyeOffset, (LivingEntity) renderEntity, entityRotation, trackMouse, drawName);
//                RenderSystem.translated(0, 0, -zLevel);
            }
        }
        catch (Throwable e) {
            LOGGER.error("Something went wrong while attempting to render an entity on the screen!");
            LOGGER.error("Entity: " + renderEntity);
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
                PoseStack poseStack = new PoseStack();
                poseStack.translate(0, 0, getRenderZLevel());
                renderTooltip(poseStack, tooltip.stream().map(TextComponent::new).collect(Collectors.toList()), mouseX, mouseY);
                return true;
            }
        }

        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
    }

    public static void renderEntityOnScreen(int xPos, int yPos, int scale, float mouseX, float mouseY, LivingEntity entity, double rotation, boolean trackMouse, boolean drawName) {
//        float lookX = trackMouse ? (float) Math.atan((double) (mouseX / 40.0F)) : 0;
//        float lookY = trackMouse ? (float) Math.atan((double) (mouseY / 40.0F)) : 0;
//        if (drawName && entity instanceof RemotePlayer && Minecraft.getInstance().player != null) {
//            entity.setPos(Minecraft.getInstance().player.getX(), Minecraft.getInstance().player.getY(), Minecraft.getInstance().player.getZ());
//        } else if (entity instanceof RemotePlayer) {
//            entity.setPos(0, -1000, 0);
//        }
//
//        RenderSystem.pushMatrix();
//        RenderSystem.translatef((float) xPos, (float) yPos, 1050.0F);
//        RenderSystem.scalef(1.0F, 1.0F, -1.0F);
//        PoseStack matrixstack = new PoseStack();
//        matrixstack.translate(0.0D, 0.0D, 1000.0D);
//        matrixstack.scale((float) scale, (float) scale, (float) scale);
//        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
//        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(lookY * 20.0F);
//        quaternion.mul(quaternion1);
//        matrixstack.mulPose(quaternion);
//        matrixstack.mulPose(Vector3f.YP.rotationDegrees((float) rotation));
//        float f2 = entity.yBodyRot;
//        float f3 = entity.yRot;
//        float f4 = entity.xRot;
//        float f5 = entity.yHeadRotO;
//        float f6 = entity.yHeadRot;
//        entity.yBodyRot = 180.0F + lookX * 20.0F;
//        entity.yRot = 180.0F + lookX * 40.0F;
//        entity.xRot = -lookY * 20.0F;
//        entity.yHeadRot = entity.yRot;
//        entity.yHeadRotO = entity.yRot;
//        EntityRenderDispatcher rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
//        quaternion1.conj();
//        rendererManager.overrideCameraOrientation(quaternion1);
//        rendererManager.setRenderShadow(false);
//        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
//        RenderSystem.runAsFancy(() -> {
//            rendererManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixstack, irendertypebuffer$impl, 15728880);
//        });
//        irendertypebuffer$impl.endBatch();
//        rendererManager.setRenderShadow(true);
//        entity.yBodyRot = f2;
//        entity.setYRot(f3);
//        entity.setXRot(f4);
//        entity.yHeadRotO = f5;
//        entity.yHeadRot = f6;
//        RenderSystem.popMatrix();
    }

    public Entity getRenderEntity(ClientLevel world, String entityString, EquipmentHelper helper) {
        String ident = String.format("%s|mh:%s|oh:%s|h:%s|ch:%s|le:%s|bo:%s", entityString, mainHand, offHand, head, chest, legs, boots);
        if (renderEntityCache.containsKey(ident) && !animate) {
            return renderEntityCache.get(ident);
        }

        if (!entityString.contains(":")) {
            throw new IllegalArgumentException("Invalid entity string! Must be ether modid:entityName or player:username");
        }

        Entity entity = null;
        if (entityString.startsWith("player:")) {
//            entity = createRenderPlayer(world, entityString.replaceFirst("player:", ""));
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
            if (!mainHand.isEmpty()) entity.setItemInHand(InteractionHand.MAIN_HAND, mainHand);
            if (!offHand.isEmpty()) entity.setItemInHand(InteractionHand.OFF_HAND, offHand);
            if (!head.isEmpty()) entity.setItemSlot(EquipmentSlot.HEAD, head);
            if (!chest.isEmpty()) entity.setItemSlot(EquipmentSlot.CHEST, chest);
            if (!legs.isEmpty()) entity.setItemSlot(EquipmentSlot.LEGS, legs);
            if (!boots.isEmpty()) entity.setItemSlot(EquipmentSlot.FEET, boots);
        }

        public static EquipmentHelper create(EntityElement element) {
            EquipmentHelper helper = new EquipmentHelper();

            try {
                if (!element.mainHand.isEmpty()) {
                    helper.mainHand = StringyStacks.fromString(element.mainHand);
                    if (helper.mainHand.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.mainHand);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.offHand.isEmpty()) {
                    helper.offHand = StringyStacks.fromString(element.offHand);
                    if (helper.offHand.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.offHand);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.head.isEmpty()) {
                    helper.head = StringyStacks.fromString(element.head);
                    if (helper.head.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.head);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.chest.isEmpty()) {
                    helper.chest = StringyStacks.fromString(element.chest);
                    if (helper.chest.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.chest);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.legs.isEmpty()) {
                    helper.legs = StringyStacks.fromString(element.legs);
                    if (helper.legs.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.legs);
                    } else {
                        helper.hasEquipment = true;
                    }
                }

                if (!element.boots.isEmpty()) {
                    helper.boots = StringyStacks.fromString(element.boots);
                    if (helper.boots.isEmpty()) {
                        LOGGER.warn("[MarkdownParser]: No matching item found for stack string: " + element.boots);
                    } else {
                        helper.hasEquipment = true;
                    }
                }
            }
            catch (Throwable e) {
                LOGGER.warn("[Entity Element] An error occurred while parsing stack string.");
                e.printStackTrace();
            }

            return helper;
        }
    }
}
