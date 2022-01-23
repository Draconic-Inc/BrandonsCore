package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.fonts.EmptyGlyph;
import net.minecraft.client.gui.fonts.Font;
import net.minecraft.client.gui.fonts.IGlyph;
import net.minecraft.client.gui.fonts.TexturedGlyph;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ICharacterConsumer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TextProcessing;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 22/01/2022
 */
public class MarkdownTextRenderer {
    private static Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);

    private Style activeStyle = Style.EMPTY;
    private GuiElement<?> guiElement;
    private FontRenderer fontRenderer;

    public MarkdownTextRenderer(GuiElement<?> guiElement, FontRenderer fontRenderer) {
        this.guiElement = guiElement;
        this.fontRenderer = fontRenderer;
    }

    public int drawFormattedString(String text, float x, float y, int colour, boolean dropShadow) {
        IRenderTypeBuffer.Impl getter = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
        MatrixStack textStack = new MatrixStack();
        textStack.translate(0.0D, 0.0D, guiElement.getRenderZLevel());
        Matrix4f textLocation = textStack.last().pose();

        float charX = x;
        int texLen = text.length();
        for (int i = 0; i < texLen; i++) {
            char character = text.charAt(i);
            if (character == 167) { //§
                if (i + 1 >= texLen) {
                    break;
                }

                char formatChar = text.charAt(i + 1);
                TextFormatting textformatting = TextFormatting.getByCode(formatChar);
                if (textformatting != null) {
                    if (textformatting == TextFormatting.RESET) {
                        activeStyle = Style.EMPTY;
                    } else {
                        switch (textformatting) {
                            case OBFUSCATED:
                                activeStyle = activeStyle.setObfuscated(!activeStyle.isObfuscated());
                                break;
                            case BOLD:
                                activeStyle = activeStyle.withBold(!activeStyle.isBold());
                                break;
                            case STRIKETHROUGH:
                                activeStyle = activeStyle.setStrikethrough(!activeStyle.isStrikethrough());
                                break;
                            case UNDERLINE:
                                activeStyle = activeStyle.setUnderlined(!activeStyle.isUnderlined());
                                break;
                            case ITALIC:
                                activeStyle = activeStyle.withItalic(!activeStyle.isItalic());
                                break;
                            default:
                                Color colourFlag = Color.fromLegacyFormat(textformatting);
                                if (colourFlag == null) {
                                    activeStyle = activeStyle.withColor((Color)null);
                                } else {
                                    activeStyle = activeStyle.withColor(activeStyle.getColor() == colourFlag ? null : colourFlag);
                                }
                        }
                    }
                }
                ++i;
            }
            else {
                charX = drawInternal(String.valueOf(character), charX, y, colour, dropShadow, textLocation, getter, false, 0, 15728880, fontRenderer.isBidirectional());
            }
        }

        getter.endBatch();
        return 0;
    }

    protected static int adjustColor(int colour) {
        return (colour & -67108864) == 0 ? colour | -16777216 : colour;
    }

    protected float drawInternal(String text, float x, float y, int colour, boolean shadow, Matrix4f mat4f, IRenderTypeBuffer getter, boolean boolFalse, int int0, int int1, boolean biDirection) {
        SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
        if (biDirection) {
            text = fontRenderer.bidirectionalShaping(text);
        }

        colour = adjustColor(colour);
        Matrix4f matrix4f = mat4f.copy();
        if (shadow) {
            renderText(text, x, y, colour, true, mat4f, getter, boolFalse, int0, int1);
            matrix4f.translate(SHADOW_OFFSET);
        }

        x = renderText(text, x, y, colour, false, matrix4f, getter, boolFalse, int0, int1);
        return (int)x + (shadow ? 1 : 0);
    }

    protected float renderText(String text, float x, float y, int colour, boolean isShadow, Matrix4f mat4f, IRenderTypeBuffer getter, boolean boolFalse, int int0, int int1) {
        MDCharacterRenderer characterRenderer = new MDCharacterRenderer(getter, x, y, colour, isShadow, mat4f, boolFalse, int1);
        TextProcessing.iterateFormatted(text, activeStyle, characterRenderer);
        return characterRenderer.finish(int0, x);
    }

    public void reset() {
        activeStyle = Style.EMPTY;
    }

    class MDCharacterRenderer implements ICharacterConsumer {
        final IRenderTypeBuffer bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final boolean seeThrough;
        private final int packedLightCoords;
        private float x;
        private float y;
        @Nullable
        private List<TexturedGlyph.Effect> effects;

        private void addEffect(TexturedGlyph.Effect p_238442_1_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_238442_1_);
        }

        public MDCharacterRenderer(IRenderTypeBuffer typeBuffer, float p_i232250_3_, float p_i232250_4_, int p_i232250_5_, boolean p_i232250_6_, Matrix4f p_i232250_7_, boolean p_i232250_8_, int p_i232250_9_) {
            this.bufferSource = typeBuffer;
            this.x = p_i232250_3_;
            this.y = p_i232250_4_;
            this.dropShadow = p_i232250_6_;
            this.dimFactor = p_i232250_6_ ? 0.25F : 1.0F;
            this.r = (float) (p_i232250_5_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float) (p_i232250_5_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float) (p_i232250_5_ & 255) / 255.0F * this.dimFactor;
            this.a = (float) (p_i232250_5_ >> 24 & 255) / 255.0F;
            this.pose = p_i232250_7_;
            this.seeThrough = p_i232250_8_;
            this.packedLightCoords = p_i232250_9_;
        }

        private Font getFontSet(ResourceLocation p_238419_1_) {
            return fontRenderer.fonts.apply(p_238419_1_);
        }

        @Override
        public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
            Font font = getFontSet(p_accept_2_.getFont());
            IGlyph iglyph = font.getGlyphInfo(p_accept_3_);
            TexturedGlyph texturedglyph = p_accept_2_.isObfuscated() && p_accept_3_ != 32 ? font.getRandomGlyph(iglyph) : font.getGlyph(p_accept_3_);
            boolean flag = p_accept_2_.isBold();
            float f3 = this.a;
            Color color = p_accept_2_.getColor();
            float f;
            float f1;
            float f2;
            if (color != null) {
                int i = color.getValue();
                f = (float) (i >> 16 & 255) / 255.0F * this.dimFactor;
                f1 = (float) (i >> 8 & 255) / 255.0F * this.dimFactor;
                f2 = (float) (i & 255) / 255.0F * this.dimFactor;
            } else {
                f = this.r;
                f1 = this.g;
                f2 = this.b;
            }

            if (!(texturedglyph instanceof EmptyGlyph)) {
                float f5 = flag ? iglyph.getBoldOffset() : 0.0F;
                float f4 = this.dropShadow ? iglyph.getShadowOffset() : 0.0F;
                IVertexBuilder ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.seeThrough));
                renderChar(texturedglyph, flag, p_accept_2_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, ivertexbuilder, f, f1, f2, f3, this.packedLightCoords);
            }

            float f6 = iglyph.getAdvance(flag);
            float f7 = this.dropShadow ? 1.0F : 0.0F;
            if (p_accept_2_.isStrikethrough()) {
                this.addEffect(new TexturedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
            }

            if (p_accept_2_.isUnderlined()) {
                this.addEffect(new TexturedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
            }

            this.x += f6;
            return true;
        }

        public float finish(int p_238441_1_, float p_238441_2_) {
            if (p_238441_1_ != 0) {
                float f = (float)(p_238441_1_ >> 24 & 255) / 255.0F;
                float f1 = (float)(p_238441_1_ >> 16 & 255) / 255.0F;
                float f2 = (float)(p_238441_1_ >> 8 & 255) / 255.0F;
                float f3 = (float)(p_238441_1_ & 255) / 255.0F;
                this.addEffect(new TexturedGlyph.Effect(p_238441_2_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null) {
                TexturedGlyph texturedglyph = getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                IVertexBuilder ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.seeThrough));

                for(TexturedGlyph.Effect texturedglyph$effect : this.effects) {
                    texturedglyph.renderEffect(texturedglyph$effect, this.pose, ivertexbuilder, this.packedLightCoords);
                }
            }

            return this.x;
        }

        private void renderChar(TexturedGlyph p_228077_1_, boolean p_228077_2_, boolean p_228077_3_, float p_228077_4_, float p_228077_5_, float p_228077_6_, Matrix4f p_228077_7_, IVertexBuilder p_228077_8_, float p_228077_9_, float p_228077_10_, float p_228077_11_, float p_228077_12_, int p_228077_13_) {
            p_228077_1_.render(p_228077_3_, p_228077_5_, p_228077_6_, p_228077_7_, p_228077_8_, p_228077_9_, p_228077_10_, p_228077_11_, p_228077_12_, p_228077_13_);
            if (p_228077_2_) {
                p_228077_1_.render(p_228077_3_, p_228077_5_ + p_228077_4_, p_228077_6_, p_228077_7_, p_228077_8_, p_228077_9_, p_228077_10_, p_228077_11_, p_228077_12_, p_228077_13_);
            }
        }
    }
}
