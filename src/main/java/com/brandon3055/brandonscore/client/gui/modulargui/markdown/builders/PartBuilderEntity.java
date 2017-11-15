package com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.*;
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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement.profiler;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class PartBuilderEntity extends IPartBuilder {
    public static Map<String, Entity> renderEntityCache = new HashMap<>();
    private static Pattern entityPat = Pattern.compile("(?<=[^\\\\]|^)(§entity\\[[^§]*]\\{[^§]*})|(?<=[^\\\\]|^)(§entity\\[[^§ ]*])");
    private static Pattern entityString = Pattern.compile("(?<=§entity\\[)(.*)(?=][{])|(?<=§entity\\[)(.*)(?=])");
    private static Pattern entityOPS = Pattern.compile("(?<=]\\{)(.*)(?=})");

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    @Override
    public int matches(String test) {
        Matcher matcher = entityPat.matcher(test);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    /**
     * size
     * hover
     * living
     * track_mouse
     */
    @Override
    public String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel) {
        profiler.startSection("Build Entity");
        //region Extract Entity Data

        Matcher entityPatMatch = entityPat.matcher(markdown);
        String match;

        if (entityPatMatch.find(0)) {
            match = entityPatMatch.group();
        }
        else {
            LogHelperBC.error("Failed to build " + getClass().getSimpleName() + " This suggests a false match occurred which should not be possible!");
            return "";
        }

        Matcher entityStringMatch = entityString.matcher(match);
        String entityString;
        if (!entityStringMatch.find() || (entityString = entityStringMatch.group()).isEmpty()) {
            return entityPatMatch.replaceFirst("[Broken Entity. No entity string Found]");
        }

        Matcher opsMatch = entityOPS.matcher(match);
        String ops = opsMatch.find() ? opsMatch.group() : "";

        int size;
        int xOffset;
        int yOffset;
        try {
            size = Integer.parseInt(Part.readOption(ops, "size", "64"));
            xOffset = Integer.parseInt(Part.readOption(ops, "x_offset", "0"));
            yOffset = Integer.parseInt(Part.readOption(ops, "y_offset", "0"));
        }
        catch (NumberFormatException e) {
            return entityPatMatch.replaceFirst("[Broken Entity. Invalid size or offset value! Must be an integer number]");
        }

        double rotationSpeed;
        double rotation;
        double eScale;
        try {
            rotationSpeed = Double.parseDouble(Part.readOption(ops, "rotate_speed", "1"));
            rotation = Double.parseDouble(Part.readOption(ops, "rotation", "0"));
            eScale = Double.parseDouble(Part.readOption(ops, "scale", "1"));
        }
        catch (NumberFormatException e) {
            return entityPatMatch.replaceFirst("[Broken Entity. Invalid rotation or scale value! Must be a number]");
        }

        String hover = Part.readOption(ops, "hover", "");
        boolean trackMouse = Part.readOption(ops, "track_mouse", "false").equals("true");
        boolean drawName = Part.readOption(ops, "draw_name", "false").equals("true");

        //endregion

        MouseIntractable mi = new MouseIntractable();
        if (!hover.isEmpty()) {
            if (hover.contains("\\n")) {
                mi.hoverText.addAll(Arrays.asList(hover.split("(\\\\n)")));
            }
            else {
                mi.hoverText.add(hover);
            }
        }

        container.mouseIntractables.add(mi);

        if (finalXPos + size > elementRight) {
            finalXPos = elementLeft;
            finalYPos = nextYLevel;
        }

        Entity renderEntity;
        try {
            renderEntity = getRenderEntity(container.mc.world, entityString, ops);
        }
        catch (IllegalArgumentException e) {
            return entityPatMatch.replaceFirst("[Broken Entity. " + e.getMessage() + "]");
        }

        if (trackMouse && !(renderEntity instanceof EntityLivingBase)) {
            return entityPatMatch.replaceFirst("[Broken Entity. track_mouse is only supported with living entities]");
        }

        if (drawName && !(renderEntity instanceof EntityPlayer)) {
            return entityPatMatch.replaceFirst("[Broken Entity. draw_name is only supported by player's]");
        }

        Part part = new Part(container) {
            @Override
            public void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks) {
                if (errored) return;
                xPos += xOffset;
                yPos += yOffset;

//                container.drawBorderedRect(xPos, yPos, width, height, 1, 0, 0xFF0000FF);

                try {
                    int scale = (int) ((size / Math.max(renderEntity.height, renderEntity.width)) * eScale);
                    double posX = xPos + (width / 2D);
                    double entityRotation = rotation + ((BCClientEventHandler.elapsedTicks + partialTicks) * rotationSpeed);

                    renderEntity.ticksExisted = BCClientEventHandler.elapsedTicks;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(0, 0, 25 + scale);
                    GlStateManager.color(1, 1, 1, 1);

                    int eyeOffset = (int) ((renderEntity.height - renderEntity.getEyeHeight()) * scale);
                    if (renderEntity instanceof EntityLivingBase) {
                        drawEntityOnScreen((int) posX, yPos + height, scale, (int) posX - mouseX, yPos - mouseY + eyeOffset, (EntityLivingBase) renderEntity, trackMouse, entityRotation, drawName);
                    }
                    else {
                        drawEntityOnScreen((int) posX, yPos + height, scale, renderEntity, entityRotation);
                    }

                    GlStateManager.popMatrix();
                }
                catch (Throwable e) {
                    LogHelperBC.error("Something went wrong while attempting to render an entity on the screen!");
                    LogHelperBC.error("Entity: " + renderEntity);
                    e.printStackTrace();
                    errored = true;
                }
            }
        };
        part.width = size;
        part.height = size;
        finalXPos += part.width;

        parts.add(part);
        mi.parts.add(part);

        builtHeight = (finalYPos - yPos) + part.height;

        return entityPatMatch.replaceFirst("");
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
        rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
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
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F + rotation + (drawName ? 0 : 180));
        rendermanager.setRenderShadow(false);
        rendermanager.doRenderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
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

    public static Entity getRenderEntity(World world, String entityString, String options) {
        if (renderEntityCache.containsKey(entityString + "|" + options)) {
            return renderEntityCache.get(entityString + "|" + options);
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

        EquipmentHelper helper = EquipmentHelper.create(options);

        if (entity instanceof EntityLivingBase && helper.hasEquipment) {
            helper.apply((EntityLivingBase) entity);
        }
        else if (helper.hasEquipment) {
            throw new IllegalArgumentException("The specified entity does not allow equipment!");
        }

        return entity;
    }

    public static EntityPlayer createRenderPlayer(World world, String username) {
        EntityOtherPlayerMP player = new EntityOtherPlayerMP(world, TileEntitySkull.updateGameprofile(new GameProfile(null, username))) {
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

        return player;
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

        public static EquipmentHelper create(String options) {
            EquipmentHelper helper = new EquipmentHelper();

            String mainHand = Part.readOption(options, "main_hand", "");
            String offHand = Part.readOption(options, "off_hand", "");
            String head = Part.readOption(options, "head", "");
            String chest = Part.readOption(options, "chest", "");
            String legs = Part.readOption(options, "legs", "");
            String boots = Part.readOption(options, "boots", "");

            if (!mainHand.isEmpty()) {
                StackReference stackRef = StackReference.fromString(mainHand);
                if (stackRef == null || (helper.mainHand = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + mainHand);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!offHand.isEmpty()) {
                StackReference stackRef = StackReference.fromString(offHand);
                if (stackRef == null || (helper.offHand = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + offHand);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!head.isEmpty()) {
                StackReference stackRef = StackReference.fromString(head);
                if (stackRef == null || (helper.head = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + head);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!chest.isEmpty()) {
                StackReference stackRef = StackReference.fromString(chest);
                if (stackRef == null || (helper.chest = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + chest);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!legs.isEmpty()) {
                StackReference stackRef = StackReference.fromString(legs);
                if (stackRef == null || (helper.legs = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + legs);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            if (!boots.isEmpty()) {
                StackReference stackRef = StackReference.fromString(boots);
                if (stackRef == null || (helper.boots = stackRef.createStack()) == null) {
                    LogHelperBC.warn("[MarkdownParser]: No matching item found for stack string: " + boots);
                }
                else {
                    helper.hasEquipment = true;
                }
            }

            return helper;
        }
    }
}
