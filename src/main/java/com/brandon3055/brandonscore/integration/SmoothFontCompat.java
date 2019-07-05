package com.brandon3055.brandonscore.integration;

import bre.smoothfont.FontRendererHook;
import bre.smoothfont.FontRendererHookList;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraftforge.fml.common.Optional;

/**
 * Created by brandon3055 on 5/7/19.
 */
public class SmoothFontCompat {

    @Optional.Method(modid = "smoothfont")
    public static void renderStringAtPosEnterHook(BCFontRenderer fr, String text, boolean unicodeFlag, boolean shadow) {
        try {
            if (!check(fr)) {
                return;
            }
            ((FontRendererHook) fr.sfh).renderStringAtPosEnterHook(text, unicodeFlag, shadow);
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook renderStringAtPosEnterHook");
            e.printStackTrace();
            fr.sfhb = true;
        }
    }

    @Optional.Method(modid = "smoothfont")
    public static void renderStringAtPosExitHook(BCFontRenderer fr, boolean unicodeFlag) {
        try {
            if (!check(fr)) {
                return;
            }
            ((FontRendererHook) fr.sfh).renderStringAtPosExitHook(unicodeFlag);
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook renderStringAtPosExitHook");
            e.printStackTrace();
            fr.sfhb = true;
        }
    }

    @Optional.Method(modid = "smoothfont")
    public static Result renderStringHook(BCFontRenderer fr, String text, int color, boolean dropShadow, boolean unicodeFlag) {
        try {
            if (!check(fr)) {
                return null;
            }
            return new Result(((FontRendererHook) fr.sfh).renderStringHook(text, color, dropShadow, unicodeFlag));
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook renderStringHook");
            e.printStackTrace();
            fr.sfhb = true;
        }

        return null;
    }

    @Optional.Method(modid = "smoothfont")
    public static void renderStringExitHook(BCFontRenderer fr, String text, boolean unicodeFlag) {
        try {
            if (!check(fr)) {
                return;
            }
            ((FontRendererHook) fr.sfh).renderStringExitHook(text, unicodeFlag);
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook renderStringExitHook");
            e.printStackTrace();
            fr.sfhb = true;
        }
    }

    @Optional.Method(modid = "smoothfont")
    public static Result getStringWidthFloatHook(BCFontRenderer fr, String text) {
        try {
            if (!check(fr)) {
                return null;
            }
            return new Result(((FontRendererHook) fr.sfh).getStringWidthFloatHook(text));
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook getStringWidthFloatHook");
            e.printStackTrace();
            fr.sfhb = true;
        }

        return null;
    }

    @Optional.Method(modid = "smoothfont")
    public static Result sizeStringToWidthFloatHook(BCFontRenderer fr, String str, int wrapWidth) {
        try {
            if (!check(fr)) {
                return null;
            }
            return new Result(((FontRendererHook) fr.sfh).sizeStringToWidthFloatHook(str, wrapWidth));
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to insert hook sizeStringToWidthFloatHook");
            e.printStackTrace();
            fr.sfhb = true;
        }

        return null;
    }

    @Optional.Method(modid = "smoothfont")
    private static boolean check(BCFontRenderer fr) {
        try {
            if (!fr.sfhb && fr.sfh == null) {
                fr.sfh = FontRendererHookList.getFontRendererHook(fr);
                return fr.sfh != null;
            }

            return !fr.sfhb;
        }
        catch (Throwable e) {
            LogHelperBC.error("Smooth Font integration is broken. Failed to retrieve hook");
            e.printStackTrace();
            fr.sfhb = true;
            return false;
        }
    }

    public static class Result {
        public int result;
        public Result(int result) {
            this.result = result;
        }
    }
}
