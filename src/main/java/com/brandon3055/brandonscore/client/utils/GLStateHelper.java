package com.brandon3055.brandonscore.client.utils;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * Created by brandon3055 on 4/07/2016.
 */
public class GLStateHelper {

    private static int lastBlendState = -1;
    public static void pushBlend(boolean enable) {
        if (lastBlendState != -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to push twice");
        }
        else {
            lastBlendState = GlStateManager.glGetInteger(GL11.GL_BLEND);
            if (enable) {
                GlStateManager.enableBlend();
            }
            else {
                GlStateManager.disableBlend();
            }
        }
    }

    public static void popBlend() {
        if (lastBlendState == -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to pop before pushing");
        }
        else {
            if (lastBlendState == 1) {
                GlStateManager.enableBlend();
            }
            else {
                GlStateManager.disableBlend();
            }
            lastBlendState = -1;
        }
    }

    private static int lastAlphaState = -1;
    public static void pushAlpha(boolean enable) {
        if (lastAlphaState != -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to push twice");
        }
        else {
            lastAlphaState = GlStateManager.glGetInteger(GL11.GL_ALPHA);
            if (enable) {
                GlStateManager.enableAlpha();
            }
            else {
                GlStateManager.disableAlpha();
            }
        }
    }

    public static void popAlpha() {
        if (lastAlphaState == -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to pop before pushing");
        }
        else {
            if (lastAlphaState == 1) {
                GlStateManager.enableAlpha();
            }
            else {
                GlStateManager.disableAlpha();
            }
            lastAlphaState = -1;
        }
    }

    private static int lastDepthState = -1;
    public static void pushDepth(boolean enable) {
        if (lastDepthState != -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to push twice");
        }
        else {
            lastDepthState = GlStateManager.glGetInteger(GL11.GL_DEPTH);
            if (enable) {
                GlStateManager.enableDepth();
            }
            else {
                GlStateManager.disableDepth();
            }
        }
    }

    public static void popDepth() {
        if (lastDepthState == -1){
            LogHelperBC.bigError("[GLStateHelper] Attempt to pop before pushing");
        }
        else {
            if (lastDepthState == 1) {
                GlStateManager.enableDepth();
            }
            else {
                GlStateManager.disableDepth();
            }
            lastDepthState = -1;
        }
    }
}
