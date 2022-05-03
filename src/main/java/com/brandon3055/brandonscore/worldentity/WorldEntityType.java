package com.brandon3055.brandonscore.worldentity;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Created by brandon3055 on 15/12/20
 */
public class WorldEntityType<T extends WorldEntity> extends net.minecraftforge.registries.ForgeRegistryEntry<WorldEntityType<?>> {
    private Supplier<? extends T> factory;

    public WorldEntityType(Supplier<? extends T> factory) {
        this.factory = factory;
    }

    public T create() {
        return this.factory.get();
    }

    @Nullable
    public static ResourceLocation getId(WorldEntityType<?> tileEntityTypeIn) {
        return WorldEntityHandler.REGISTRY.getKey(tileEntityTypeIn);
    }
}
