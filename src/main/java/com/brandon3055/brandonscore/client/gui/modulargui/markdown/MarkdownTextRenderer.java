package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 22/01/2022
 */
public class MarkdownTextRenderer {
    private static Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);

    private Style activeStyle = Style.EMPTY;
    private GuiElement<?> guiElement;
    private Font fontRenderer;

    public MarkdownTextRenderer(GuiElement<?> guiElement, Font fontRenderer) {
        this.guiElement = guiElement;
        this.fontRenderer = fontRenderer;
    }

    public int drawFormattedString(String text, float x, float y, int colour, boolean dropShadow) {
        MultiBufferSource.BufferSource getter = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        PoseStack textStack = new PoseStack();
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
                ChatFormatting textformatting = ChatFormatting.getByCode(formatChar);
                if (textformatting != null) {
                    if (textformatting == ChatFormatting.RESET) {
                        activeStyle = Style.EMPTY;
                    } else {
                        switch (textformatting) {
                            case OBFUSCATED:
                                activeStyle = activeStyle.withObfuscated(!activeStyle.isObfuscated());
                                break;
                            case BOLD:
                                activeStyle = activeStyle.withBold(!activeStyle.isBold());
                                break;
                            case STRIKETHROUGH:
                                activeStyle = activeStyle.withStrikethrough(!activeStyle.isStrikethrough());
                                break;
                            case UNDERLINE:
                                activeStyle = activeStyle.withUnderlined(!activeStyle.isUnderlined());
                                break;
                            case ITALIC:
                                activeStyle = activeStyle.withItalic(!activeStyle.isItalic());
                                break;
                            default:
                                TextColor colourFlag = TextColor.fromLegacyFormat(textformatting);
                                if (colourFlag == null) {
                                    activeStyle = activeStyle.withColor((TextColor)null);
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

    protected float drawInternal(String text, float x, float y, int colour, boolean shadow, Matrix4f mat4f, MultiBufferSource getter, boolean boolFalse, int int0, int int1, boolean biDirection) {
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

    protected float renderText(String text, float x, float y, int colour, boolean isShadow, Matrix4f mat4f, MultiBufferSource getter, boolean boolFalse, int int0, int int1) {
        MDCharacterRenderer characterRenderer = new MDCharacterRenderer(getter, x, y, colour, isShadow, mat4f, boolFalse, int1);
        StringDecomposer.iterateFormatted(text, activeStyle, characterRenderer);
        return characterRenderer.finish(int0, x);
    }

    public void reset() {
        activeStyle = Style.EMPTY;
    }

    class MDCharacterRenderer implements FormattedCharSink {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final Font.DisplayMode mode;
        private final int packedLightCoords;
        private float x;
        private float y;
        @Nullable
        private List<BakedGlyph.Effect> effects;

        private void addEffect(BakedGlyph.Effect p_238442_1_) {
            if (this.effects == null) {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(p_238442_1_);
        }

        public MDCharacterRenderer(MultiBufferSource p_92953_, float p_92954_, float p_92955_, int p_92956_, boolean p_92957_, Matrix4f p_92958_, boolean p_92959_, int p_92960_) {
            this(p_92953_, p_92954_, p_92955_, p_92956_, p_92957_, p_92958_, p_92959_ ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, p_92960_);
        }

        public MDCharacterRenderer(MultiBufferSource p_181365_, float p_181366_, float p_181367_, int p_181368_, boolean p_181369_, Matrix4f p_181370_, Font.DisplayMode p_181371_, int p_181372_) {
            this.bufferSource = p_181365_;
            this.x = p_181366_;
            this.y = p_181367_;
            this.dropShadow = p_181369_;
            this.dimFactor = p_181369_ ? 0.25F : 1.0F;
            this.r = (float)(p_181368_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float)(p_181368_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float)(p_181368_ & 255) / 255.0F * this.dimFactor;
            this.a = (float)(p_181368_ >> 24 & 255) / 255.0F;
            this.pose = p_181370_;
            this.mode = p_181371_;
            this.packedLightCoords = p_181372_;
        }



        private FontSet getFontSet(ResourceLocation p_238419_1_) {
            return fontRenderer.fonts.apply(p_238419_1_);
        }

        @Override
        public boolean accept(int p_accept_1_, Style p_accept_2_, int p_accept_3_) {
            FontSet font = getFontSet(p_accept_2_.getFont());
            GlyphInfo iglyph = font.getGlyphInfo(p_accept_3_);
            BakedGlyph texturedglyph = p_accept_2_.isObfuscated() && p_accept_3_ != 32 ? font.getRandomGlyph(iglyph) : font.getGlyph(p_accept_3_);
            boolean flag = p_accept_2_.isBold();
            float f3 = this.a;
            TextColor color = p_accept_2_.getColor();
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
                VertexConsumer ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.mode));
                renderChar(texturedglyph, flag, p_accept_2_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, ivertexbuilder, f, f1, f2, f3, this.packedLightCoords);
            }

            float f6 = iglyph.getAdvance(flag);
            float f7 = this.dropShadow ? 1.0F : 0.0F;
            if (p_accept_2_.isStrikethrough()) {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f, f1, f2, f3));
            }

            if (p_accept_2_.isUnderlined()) {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f, f1, f2, f3));
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
                this.addEffect(new BakedGlyph.Effect(p_238441_2_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null) {
                BakedGlyph texturedglyph = getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer ivertexbuilder = this.bufferSource.getBuffer(texturedglyph.renderType(this.mode));

                for(BakedGlyph.Effect texturedglyph$effect : this.effects) {
                    texturedglyph.renderEffect(texturedglyph$effect, this.pose, ivertexbuilder, this.packedLightCoords);
                }
            }

            return this.x;
        }

        private void renderChar(BakedGlyph p_228077_1_, boolean p_228077_2_, boolean p_228077_3_, float p_228077_4_, float p_228077_5_, float p_228077_6_, Matrix4f p_228077_7_, VertexConsumer p_228077_8_, float p_228077_9_, float p_228077_10_, float p_228077_11_, float p_228077_12_, int p_228077_13_) {
            p_228077_1_.render(p_228077_3_, p_228077_5_, p_228077_6_, p_228077_7_, p_228077_8_, p_228077_9_, p_228077_10_, p_228077_11_, p_228077_12_, p_228077_13_);
            if (p_228077_2_) {
                p_228077_1_.render(p_228077_3_, p_228077_5_ + p_228077_4_, p_228077_6_, p_228077_7_, p_228077_8_, p_228077_9_, p_228077_10_, p_228077_11_, p_228077_12_, p_228077_13_);
            }
        }
    }
}
