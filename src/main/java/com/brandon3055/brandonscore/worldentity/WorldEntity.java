package com.brandon3055.brandonscore.worldentity;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Created by brandon3055 on 15/12/20
 */
public abstract class WorldEntity {
    private WorldEntityType<?> worldEntityType;
    protected final Random rand = new Random();
    private UUID uniqueID = MathHelper.createInsecureUUID(this.rand);
    protected World world;
    protected boolean removed;

    protected WorldEntity(WorldEntityType<?> worldEntityType) {
        this.worldEntityType = worldEntityType;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public WorldEntityType<?> getType() {
        return worldEntityType;
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public void onLoad() {}

    public boolean isRemoved() {
        return removed;
    }

    public void removeEntity() {
        this.removed = true;
        WorldEntityHandler.onEntityRemove(this);
    }

    public void read(CompoundNBT nbt) {
        readInternal(nbt);
    }

    public void write(CompoundNBT nbt) {
        writeInternal(nbt);
    }

    private void writeInternal(CompoundNBT nbt) {
        ResourceLocation resourcelocation = WorldEntityType.getId(this.getType());
        if (resourcelocation == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        } else {
            nbt.putString("id", resourcelocation.toString());
            nbt.putUUID("UUID", this.getUniqueID());
        }
    }

    private void readInternal(CompoundNBT nbt) {
        if (nbt.hasUUID("UUID")) {
            this.uniqueID = nbt.getUUID("UUID");
        }
    }


    @Nullable
    public static WorldEntity readWorldEntity(CompoundNBT nbt) {
        String id = nbt.getString("id");
        return Optional.ofNullable(WorldEntityHandler.REGISTRY.getValue(new ResourceLocation(id)))
                .map(e -> {
                    try {
                        return e.create();
                    }
                    catch (Throwable throwable) {
                        LogHelperBC.error("Failed to create world entity {}", id, throwable);
                        return null;
                    }
                }).map(e -> {
                    try {
                        e.read(nbt);
                        return e;
                    }
                    catch (Throwable throwable) {
                        LogHelperBC.error("Failed to load data for world entity {}", id, throwable);
                        return null;
                    }
                }).orElseGet(() -> {
                    LogHelperBC.warn("Skipping WorldEntity with id {}", (Object) id);
                    return null;
                });
    }
}
