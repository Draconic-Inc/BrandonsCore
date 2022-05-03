package com.brandon3055.brandonscore.client.render;

import com.brandon3055.brandonscore.client.BCSprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 6/5/20.
 */
public class GuiSpriteUploader extends TextureAtlasHolder {

    private final Set<ResourceLocation> registeredSprites;

    public GuiSpriteUploader(Set<ResourceLocation> registeredSprites) {
        super(Minecraft.getInstance().textureManager, BCSprites.LOCATION_GUI_ATLAS, "gui");
        this.registeredSprites = registeredSprites;
        ReloadableResourceManager resourceManager = (ReloadableResourceManager) Minecraft.getInstance().getResourceManager();
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
