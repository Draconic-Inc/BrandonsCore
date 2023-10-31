//package com.brandon3055.brandonscore.client.gui.modulargui.lib;
//
//import com.brandon3055.brandonscore.utils.Utils;
//import com.mojang.math.Matrix4f;
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.font.FontSet;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.FormattedCharSequence;
//
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//
///**
// * Created by brandon3055 on 10/07/2017.
// */
//@Deprecated //TODO Investigate changes to the vanilla font renderer and ether remove or update this
//public class BCFontRenderer extends Font {
//    private static boolean styleToggleMode = false;
//    private static boolean colourSet = false;
//    private static int prevFormat = -1;
//    private static boolean colourFormatSet = false;
//    private static Map<Font, BCFontRenderer> cashedRenderers = new HashMap<>();
//
//    public BCFontRenderer(Function<ResourceLocation, FontSet> font) {
//        super(font);
//    }
//
//
//    @Override
//    protected int drawInternal(String text, float x, float y, int colour, boolean shadow, Matrix4f mat4f, MultiBufferSource getter, boolean boolFalse, int int0, int int1, boolean biDirection) {
//        if (biDirection) {
//            text = this.bidirectionalShaping(text);
//        }
//
//        colour = adjustColor(colour);
//        Matrix4f matrix4f = mat4f.copy();
//        if (shadow) {
//            this.renderText(text, x, y, colour, true, mat4f, getter, boolFalse, int0, int1);
////            matrix4f.translate(FONT_OFFSET);
//        }
//
//        x = this.renderText(text, x, y, colour, false, matrix4f, getter, boolFalse, int0, int1);
//        return (int)x + (shadow ? 1 : 0);
//    }
//
//    @Override
//    protected int drawInternal(FormattedCharSequence p_238424_1_, float x, float y, int color, boolean p_238424_5_, Matrix4f matrix, MultiBufferSource buffer, boolean p_238424_8_, int p_238424_9_, int p_238424_10_) {
//        color = adjustColor(color);
//        Matrix4f matrix4f = matrix.copy();
//        if (p_238424_5_) {
//            this.renderText(p_238424_1_, x, y, color, true, matrix, buffer, p_238424_8_, p_238424_9_, p_238424_10_);
////            matrix4f.translate(FONT_OFFSET);
//        }
//
//        x = this.renderText(p_238424_1_, x, y, color, false, matrix4f, buffer, p_238424_8_, p_238424_9_, p_238424_10_);
//        return (int)x + (p_238424_5_ ? 1 : 0);
//    }
//
//
//    public static List<String> listFormattedStringToWidth(String str, int wrapWidth) {
//        return Arrays.asList(wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
//    }
//
//    public static String wrapFormattedStringToWidth(String str, int wrapWidth) {
//        String s;
//        String s1;
//        for(s = ""; !str.isEmpty(); s = s + s1 + "\n") {
//            int i = sizeStringToWidth(str, wrapWidth);
//            if (str.length() <= i) {
//                return s + str;
//            }
//
//            s1 = str.substring(0, i);
//            char c0 = str.charAt(i);
//            boolean flag = c0 == ' ' || c0 == '\n';
//            str = Utils.getTextFormatString(s1) + str.substring(i + (flag ? 1 : 0));
//        }
//
//        return s;
//    }
//
//    public static int sizeStringToWidth(String str, int wrapWidth) {
//        int i = Math.max(1, wrapWidth);
//        int j = str.length();
//        float f = 0.0F;
//        int k = 0;
//        int l = -1;
//        boolean flag = false;
//
//        for(boolean flag1 = true; k < j; ++k) {
//            char c0 = str.charAt(k);
//            switch(c0) {
//                case '\n':
//                    --k;
//                    break;
//                case ' ':
//                    l = k;
//                default:
//                    if (f != 0.0F) {
//                        flag1 = false;
//                    }
//
//                    f += Minecraft.getInstance().font.getSplitter().stringWidth(c0+"");
//                    if (flag) {
//                        ++f;
//                    }
//                    break;
//                case '\u00a7':
//                    if (k < j - 1) {
//                        ++k;
//                        ChatFormatting textformatting = ChatFormatting.getByCode(str.charAt(k));
//                        if (textformatting == ChatFormatting.BOLD) {
//                            flag = true;
//                        } else if (textformatting != null && !textformatting.isFormat()) {
//                            flag = false;
//                        }
//                    }
//            }
//
//            if (c0 == '\n') {
//                ++k;
//                l = k;
//                break;
//            }
//
//            if (f > (float)i) {
//                if (flag1) {
//                    ++k;
//                }
//                break;
//            }
//        }
//
//        return k != j && l != -1 && l < k ? l : k;
//    }
//
//
//    //This is a temporary hack that wont be needed once i re write my gui system
////    @Override
////    public int drawInternal(String text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, IRenderTypeBuffer buffer, boolean transparentIn, int colorBackgroundIn, int packedLight) {
////        if (this.bidiFlag) {
////            text = this.bidiReorder(text);
////        }
////
////        if ((color & -67108864) == 0) {
////            color |= -16777216;
////        }
////
////        if (dropShadow) {
////            this.renderStringAtPos(text, x, y, color, true, matrix, buffer, transparentIn, colorBackgroundIn, packedLight);
////        }
////
////        Matrix4f matrix4f = matrix.copy();
//////        matrix4f.translate(new Vector3f(0.0F, 0.0F, 0.001F));
////        x = this.renderStringAtPos(text, x, y, color, false, matrix4f, buffer, transparentIn, colorBackgroundIn, packedLight);
////        return (int)x + (dropShadow ? 1 : 0);
////    }
//
////    @Override
////    public String wrapFormattedStringToWidth(String str, int wrapWidth) {
////        int i = this.sizeStringToWidth(str, wrapWidth);
////
////        if (i <= 0) i = 1;
////
////        if (i <= 2 && str.startsWith("\u00a7")) {
////            i = 3;
////        }
////
////        if (str.length() <= i) {
////            recurs = 0;
////            return str;
////        } else {
////            recurs++;
////            String s = str.substring(0, i);
////            char c0 = str.charAt(i);
////            boolean flag = c0 == 32 || c0 == 10;
////            String s1 = TextFormatting.getFormatString(s) + str.substring(i + (flag ? 1 : 0));
////            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
////        }
////    }
//
////    @Override
////    public void renderStringAtPos(String text, boolean shadow) {
////        for (int index = 0; index < text.length(); ++index) {
////            char c0 = text.charAt(index);
////
////            if (c0 == '\\' && index + 1 < text.length() && text.charAt(index + 1) == 167) {
////                continue;
////            }
////            if (c0 == 167 && index + 1 < text.length() && (index == 0 || text.charAt(index - 1) != '\\')) {
////                int i1 = "0123456789abcdefklmnor".indexOf(String.valueOf(text.charAt(index + 1)).toLowerCase(Locale.ROOT).charAt(0));
////
////                if (i1 < 16) //Set Colour
////                {
////                    if (!styleToggleMode) {
////                        this.randomStyle = false;
////                        this.boldStyle = false;
////                        this.strikethroughStyle = false;
////                        this.underlineStyle = false;
////                        this.italicStyle = false;
////                    }
////
////                    if (i1 < 0 || i1 > 15) {
////                        i1 = 15;
////                    }
////
////                    if (shadow) {
////                        i1 += 16;
////                    }
////
////                    int j1 = this.colorCode[i1];
////
////                    if (styleToggleMode) {
////                        if (colourFormatSet && prevFormat == j1) {
////                            setColor(this.red, this.blue, this.green, this.alpha);
////                            colourFormatSet = false;
////                        }
////                        else {
////                            colourFormatSet = true;
////                            prevFormat = j1;
////                            this.textColor = j1;
////                            setColor((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
////                        }
////                    }
////                    else {
////                        this.textColor = j1;
////                        setColor((float) (j1 >> 16) / 255.0F, (float) (j1 >> 8 & 255) / 255.0F, (float) (j1 & 255) / 255.0F, this.alpha);
////                    }
////                }
////                else if (i1 == 16) {
////                    randomStyle = !styleToggleMode || !randomStyle;
////                }
////                else if (i1 == 17) {
////                    boldStyle = !styleToggleMode || !boldStyle;
////                }
////                else if (i1 == 18) {
////                    strikethroughStyle = !styleToggleMode || !strikethroughStyle;
////                }
////                else if (i1 == 19) {
////                    underlineStyle = !styleToggleMode || !underlineStyle;
////                }
////                else if (i1 == 20) {
////                    italicStyle = !styleToggleMode || !italicStyle;
////                }
////                else if (i1 == 21) {
////                    this.randomStyle = false;
////                    this.boldStyle = false;
////                    this.strikethroughStyle = false;
////                    this.underlineStyle = false;
////                    this.italicStyle = false;
////                    setColor(this.red, this.blue, this.green, this.alpha);
////                }
////
////                ++index;
////            }
////            else {
////                int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(c0);
////
////                if (this.randomStyle && j != -1) {
////                    int k = this.getCharWidth(c0);
////                    char c1;
////
////                    while (true) {
////                        j = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
////                        c1 = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);
////
////                        if (k == this.getCharWidth(c1)) {
////                            break;
////                        }
////                    }
////
////                    c0 = c1;
////                }
////
////                float f1 = j == -1 || this.unicodeFlag ? 0.5f : 1f;
////                boolean flag = (c0 == 0 || j == -1 || this.unicodeFlag) && shadow;
////
////                if (flag) {
////                    this.posX -= f1;
////                    this.posY -= f1;
////                }
////
////                float f = this.renderChar(c0, this.italicStyle);
////
////                if (flag) {
////                    this.posX += f1;
////                    this.posY += f1;
////                }
////
////                if (this.boldStyle) {
////                    this.posX += f1;
////
////                    if (flag) {
////                        this.posX -= f1;
////                        this.posY -= f1;
////                    }
////
////                    this.renderChar(c0, this.italicStyle);
////                    this.posX -= f1;
////
////                    if (flag) {
////                        this.posX += f1;
////                        this.posY += f1;
////                    }
////
////                    ++f;
////                }
////                doDraw(f);
////            }
////        }
////    }
//
//    public static BCFontRenderer convert(Font fontRenderer) {
//        if (!cashedRenderers.containsKey(fontRenderer)) {
//            BCFontRenderer fr = new BCFontRenderer(fontRenderer.fonts);
////                        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(fr);
//            cashedRenderers.put(fontRenderer, fr);
//        }
//
//        return cashedRenderers.get(fontRenderer);
//    }
//
////    @Override
////    public void resetStyles() {
////        if (styleToggleMode) return;
////        colourFormatSet = false;
////        super.resetStyles();
////    }
//
////    @Override
////    public int renderString(String text, float x, float y, int color, boolean dropShadow) {
////        if (text == null) {
////            return 0;
////        } else {
////            if (this.bidiFlag) {
////                text = this.bidiReorder(text);
////            }
////
////            if ((color & -67108864) == 0) {
////                color |= -16777216;
////            }
////
////            if (dropShadow) {
////                color = (color & 16579836) >> 2 | color & -16777216;
////            }
////
////            if ((!colourSet && !dropShadow) || !styleToggleMode) {
////                this.red = (float) (color >> 16 & 255) / 255.0F;
////                this.blue = (float) (color >> 8 & 255) / 255.0F;
////                this.green = (float) (color & 255) / 255.0F;
////                this.alpha = (float) (color >> 24 & 255) / 255.0F;
////                setColor(this.red, this.blue, this.green, this.alpha);
////                colourSet = true;
////            } else if (dropShadow) {
////                color = (color & 16579836) >> 2 | color & -16777216;
////                float red = (float) (color >> 16 & 255) / 255.0F;
////                float blue = (float) (color >> 8 & 255) / 255.0F;
////                float green = (float) (color & 255) / 255.0F;
////                float alpha = (float) (color >> 24 & 255) / 255.0F;
////                setColor(red, blue, green, alpha);
////            }
////
////
////            this.posX = x;
////            this.posY = y;
////            this.renderStringAtPos(text, dropShadow);
////
////            if (dropShadow && styleToggleMode) {
////                setColor(red, blue, green, alpha);
////            }
////
////            return (int) this.posX;
////        }
////    }
//
////    //Modified to accurately return the length of bold text when style toggle mode is enabled
////    @Override
////    public int getStringWidth(String text) {
////        if (text == null) {
////            return 0;
////        } else {
////            int width = 0;
////            boolean bold = boldStyle;
////
////            for (int index = 0; index < text.length(); ++index) {
////                char charAtIndex = text.charAt(index);
////                int charWidth = this.getCharWidth(charAtIndex);
////                if (charAtIndex == 167 && index > 0 && text.charAt(index - 1) == '\\') {
////                    charWidth = 6;
////                }
////                if (text.charAt(index) == '\\' && index + 1 < text.length() && text.charAt(index + 1) == 167) {
////                    continue;
////                }
////
////                //If char is " + Utils.SELECT + "
////                if (charWidth < 0 && index < text.length() - 1) {
////                    ++index;
////                    charAtIndex = text.charAt(index);
////
////                    //l L (Not bold)?
////                    if (charAtIndex != 108 && charAtIndex != 76) {
////                        //r R (Reset)
////                        if (charAtIndex == 114 || charAtIndex == 82) {
////                            bold = boldStyle = false;
////                        }
////                    } else {
////                        bold = boldStyle = !styleToggleMode || !boldStyle;
////                    }
////
////                    charWidth = 0;
////                }
////
////                width += charWidth;
////
////                if (bold && charWidth > 0) {
////                    ++width;
////                }
////            }
////
////            return width;
////        }
////    }
////
////    //Modified to accurately return the length of bold text when style toggle mode is enabled
////    @Override
////    public int sizeStringToWidth(String str, int wrapWidth) {
////        int totalLength = str.length();
////        int currentWidth = 0;
////        int index = 0;
////        int lastWord = -1;
////
////        for (boolean bold = boldStyle; index < totalLength; ++index) {
////            char charAtIndex = str.charAt(index);
////
////            switch (charAtIndex) {
////                case '\n':
////                    --index;
////                    break;
////                case ' ':
////                    lastWord = index;
////                default:
////                    currentWidth += this.getCharWidth(charAtIndex);
////
////                    if (bold) {
////                        ++currentWidth;
////                    }
////
////                    break;
////                case '\u00a7':
////
////                    if (index < totalLength - 1) {
////                        ++index;
////                        char c1 = str.charAt(index);
////
////                        //l L (Not bold)?
////                        if (c1 != 108 && c1 != 76) {
////                            if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
////                                bold = false;
////                            }
////                        } else {
////                            bold = true;
////                        }
////                    }
////            }
////
////            if (charAtIndex == 10) {
////                ++index;
////                lastWord = index;
////                break;
////            }
////
////            if (currentWidth > wrapWidth) {
////                break;
////            }
////        }
////
////        return index != totalLength && lastWord != -1 && lastWord < index ? lastWord : index;
////    }
//
////    /**
////     * This should be enabled before you start drawing and disabled immediately after.
////     */
////    public static void setStileToggleMode(boolean enabled) {
////        styleToggleMode = enabled;
////        colourSet = false;
////    }
////
////    public FontState getState() {
////        return new FontState(this);
////    }
////
////    public void loadState(FontState state) {
////        state.apply(this);
////    }
////
////    public static class FontState {
////        public boolean unicodeFlag;
////        public boolean bidiFlag;
////        public float red;
////        public float blue;
////        public float green;
////        public float alpha;
////        public int textColor;
////        public boolean randomStyle;
////        public boolean boldStyle;
////        public boolean italicStyle;
////        public boolean underlineStyle;
////        public boolean strikethroughStyle;
////
////        protected FontState(BCFontRenderer font) {
//////            unicodeFlag = font.unicodeFlag;
//////            bidiFlag = font.bidiFlag;
//////            red = font.red;
//////            blue = font.blue;
//////            green = font.green;
//////            alpha = font.alpha;
//////            textColor = font.textColor;
//////            boldStyle = font.boldStyle;
//////            italicStyle = font.italicStyle;
//////            underlineStyle = font.underlineStyle;
//////            strikethroughStyle = font.strikethroughStyle;
////        }
////
////        private void apply(BCFontRenderer font) {
//////            font.unicodeFlag = unicodeFlag;
//////            font.bidiFlag = bidiFlag;
//////            font.red = red;
//////            font.blue = blue;
//////            font.green = green;
//////            font.alpha = alpha;
//////            font.textColor = textColor;
//////            font.boldStyle = boldStyle;
//////            font.italicStyle = italicStyle;
//////            font.underlineStyle = underlineStyle;
//////            font.strikethroughStyle = strikethroughStyle;
//////            font.setColor(red, green, blue, alpha);
////        }
////
////    }
//}
