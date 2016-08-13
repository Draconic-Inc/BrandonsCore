package com.brandon3055.brandonscore.lib;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Created by brandon3055 on 4/08/2016.
 */
public class FullAtlasSprite extends TextureAtlasSprite {


    public FullAtlasSprite() {
        super("");
    }

    public FullAtlasSprite(String spriteName) {
        super(spriteName);
    }

    @Override
    public float getMinU() {
        return 0;
    }

    @Override
    public float getMaxU() {
        return 1;
    }

    @Override
    public float getMinV() {
        return 0;
    }

    @Override
    public float getMaxV() {
        return 1;
    }
}
