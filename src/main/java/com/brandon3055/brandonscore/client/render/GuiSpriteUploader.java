package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.BCSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 6/5/20.
 */
public class GuiSpriteUploader extends SpriteUploader {

    private final Set<ResourceLocation> registeredSprites;

    public GuiSpriteUploader(Set<ResourceLocation> registeredSprites) {
        super(Minecraft.getInstance().textureManager, BCSprites.LOCATION_GUI_ATLAS, "gui");
        this.registeredSprites = registeredSprites;
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        resourceManager.registerReloadListener(this);
    }

    @Override
    protected Stream<ResourceLocation> getResourcesToLoad() {
        return registeredSprites.stream();
    }

    @Override
    public TextureAtlasSprite getSprite(ResourceLocation locationIn) {
        return super.getSprite(locationIn);
    }
}
