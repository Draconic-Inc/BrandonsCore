package com.brandon3055.brandonscore.client.gui.modulargui.guielements;


import codechicken.lib.render.buffer.TransformingVertexConsumer;
import com.brandon3055.brandonscore.client.BCGuiSprites;
import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.brandon3055.brandonscore.client.render.RenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.Material;

import java.util.function.Supplier;

/**
 * Created by brandon3055 on 1/10/2016.
 */
public class GuiTexture extends GuiElement<GuiTexture> {
    public int texU;
    public int texV;
    public int textSheetSizeX = 256;
    public int textSheetSizeY = 256;
    private int texUSize = 0;
    private int texVSize = 0;
    private boolean texSizeOverride = false;
    private Supplier<Integer> texXGetter = null;
    private Supplier<Integer> texYGetter = null;
    private float rotation = 0;
    private boolean flipX = false;
    private boolean flipY = false;

    public Material material;
    public Supplier<Material> materialSupplier;

    public GuiTexture(int xPos, int yPos, int xSize, int ySize, Material material) {
        super(xPos, yPos, xSize, ySize);
        this.material = material;
    }

    public GuiTexture(int xPos, int yPos, int xSize, int ySize, Supplier<Material> materialSupplier) {
        super(xPos, yPos, xSize, ySize);
        this.materialSupplier = materialSupplier;
    }

    public GuiTexture(int xSize, int ySize, Material material) {
        super(0, 0, xSize, ySize);
        this.material = material;
    }

    public GuiTexture(int xSize, int ySize, Supplier<Material> materialSupplier) {
        super(0, 0, xSize, ySize);
        this.materialSupplier = materialSupplier;
    }

    public GuiTexture(Supplier<Material> materialSupplier) {
        super(0, 0);
        this.materialSupplier = materialSupplier;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        Material mat = getMaterial();
        if (mat != null) {
            MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
            if (rotation != 0 || flipX || flipY) {
                PoseStack mStack = new PoseStack();
                mStack.translate(xPos(), yPos(), 0);
                mStack.translate(xSize() / 2D, ySize() / 2D, 0);
                if (flipX || flipY) {
                    mStack.scale(flipX ? -1 : 1, flipY ? -1 : 1, 1);
                }
                if (rotation != 0) {
                    mStack.mulPose(new Quaternion(0, 0, rotation, true));
                }
                mStack.translate(-(xSize() / 2D), -(ySize() / 2D), 0);
                VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(mat.renderType(BCGuiSprites::makeType)), mStack);
                drawSprite(builder, 0, 0, xSize(), ySize(), mat.sprite());
            } else {
                drawSprite(getter.getBuffer(mat.renderType(BCGuiSprites::makeType)), xPos(), yPos(), xSize(), ySize(), mat.sprite());
            }
            getter.endBatch();
        }

        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
    }

    public GuiTexture setRotation(float rotation) {
        this.rotation = rotation;
        return this;
    }

    public GuiTexture flipX() {
        this.flipX = true;
        return this;
    }

    public GuiTexture flipY() {
        this.flipY = true;
        return this;
    }

    /**
     * Set the x and y (u/v) position of the texture on the provided texture sheet.
     * By default the width and height if the texture will be set from the element width and height but
     * this can be overridden using {@link #setTexSizeOverride(int, int)}
     */
    @Deprecated
    public GuiTexture setTexturePos(int textureX, int textureY) {
        this.texU = textureX;
        this.texV = textureY;
        return this;
    }

    public GuiTexture setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public Material getMaterial() {
        return materialSupplier == null ? material : materialSupplier.get();
    }

    public GuiTexture setMaterialSupplier(Supplier<Material> materialSupplier) {
        this.materialSupplier = materialSupplier;
        return this;
    }

    /**
     * Allows you to override the texture size. When set the texture will be scaled up or down as needed to fit
     * the bounds of this element.
     */
    @Deprecated
    public GuiTexture setTexSizeOverride(int texXOverride, int texYOverride) {
        this.texUSize = texXOverride;
        this.texVSize = texYOverride;
        texSizeOverride = true;
        return this;
    }

    /**
     * Set the size of the texture sheet being used. Default is the standard 256x256
     */
    @Deprecated
    public GuiTexture setTexSheetSize(int textureSizeX, int textureSizeY) {
        this.textSheetSizeX = textureSizeX;
        this.textSheetSizeY = textureSizeY;
        return this;
    }

    /**
     * @see #setTexSheetSize(int, int)
     */
    @Deprecated
    public GuiTexture setTexSheetSize(int textureSize) {
        return setTexSheetSize(textureSize, textureSize);
    }

    @Deprecated
    public GuiTexture setTexXGetter(Supplier<Integer> texXGetter) {
        this.texXGetter = texXGetter;
        return this;
    }

    @Deprecated
    public GuiTexture setTexYGetter(Supplier<Integer> texYGetter) {
        this.texYGetter = texYGetter;
        return this;
    }

    @Deprecated
    public int getTexU() {
        return texXGetter == null ? texU : texXGetter.get();
    }

    @Deprecated
    public int getTexV() {
        return texYGetter == null ? texV : texYGetter.get();
    }

    /**
     * @return a new themed slot texture
     */
    public static GuiTexture newSlot() {
        return new GuiTexture(18, 18, BCGuiSprites.themedGetter("slot"));
    }

    public static GuiTexture newDynamicTexture(int xSize, int ySize, Supplier<Material> materialSupplier) {
        return newDynamicTexture(materialSupplier).setSize(xSize, ySize);
    }

    public static GuiTexture newDynamicTexture(Supplier<Material> materialSupplier) {
        return new GuiTexture(null) {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
                drawDynamicSprite(getter.getBuffer(BCGuiSprites.GUI_TYPE), getMaterial().sprite(), xPos(), yPos(), xSize(), ySize(), getInsets().top, getInsets().left, getInsets().bottom, getInsets().right);
                getter.endBatch();

                for (GuiElement element : childElements) {
                    if (element.isEnabled()) {
                        element.renderElement(minecraft, mouseX, mouseY, partialTicks);
                    }
                }

            }
        }.setMaterialSupplier(materialSupplier).setInsets(4, 4, 4, 4);
    }

    public GuiTexture centerOnParent() {
        if (getParent() != null) {
            setXPos(getParent().xPos() + (getParent().xSize() / 2) - (xSize() / 2));
            setYPos(getParent().yPos() + (getParent().ySize() / 2) - (ySize() / 2));
        }
        return this;
    }
}
