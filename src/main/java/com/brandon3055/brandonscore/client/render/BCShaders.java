package com.brandon3055.brandonscore.client.render;

import codechicken.lib.render.shader.ShaderObject;
import com.brandon3055.brandonscore.BCConfig;

import java.io.IOException;

//import static codechicken.lib.render.shader.ShaderHelper.getStream;
//import static codechicken.lib.render.shader.ShaderHelper.readShader;
//import static codechicken.lib.render.shader.ShaderObject.ShaderType.FRAGMENT;
//import static codechicken.lib.render.shader.ShaderObject.ShaderType.VERTEX;

/**
 * Created by brandon3055 on 6/11/2016.
 */
public class BCShaders {

    public static ShaderObject energyBar;
    public static ShaderObject energyBarH;
    public static ShaderObject commonVert;

    static {
        if (useShaders()) {
            try {
                initShaders();
            } catch (Throwable e) {
                throw new RuntimeException("Unable to initialize BCShaders.", e);
            }
        }
    }

    //Used in dev to reload shaders runtime
    public static void initShaders() throws IOException {
//        dispose(energyBar);
//        dispose(energyBarH);
        dispose(commonVert);

//        energyBar = new ShaderObject(FRAGMENT, readShader(getStream("/assets/brandonscore/shaders/power_bar.frag")));
//        energyBarH = new ShaderObject(FRAGMENT, readShader(getStream("/assets/brandonscore/shaders/power_bar_horizontal.frag")));
//        commonVert = new ShaderObject(VERTEX, readShader(getStream("/assets/brandonscore/shaders/common.vert")));
    }

    public static boolean useShaders() {
        return /*OpenGlHelper.shadersSupported && TODO */BCConfig.useShaders;
    }

    private static void dispose(ShaderObject object) {
        if (object != null) {
//            object.disposeObject();
        }
    }
}
