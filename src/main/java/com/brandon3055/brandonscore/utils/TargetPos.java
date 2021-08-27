package com.brandon3055.brandonscore.utils;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import com.brandon3055.brandonscore.lib.TeleportUtils;
import com.google.common.base.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class TargetPos {
    private Vector3 pos;
    private float yaw;
    private float pitch;
    private boolean includeHeading = true;
    private RegistryKey<World> dimension;

    public TargetPos() {}

    public TargetPos(Entity entity) {
        this(entity, true);
    }

    public TargetPos(CompoundNBT nbt) {
        readFromNBT(nbt);
    }

    public TargetPos(Entity entity, boolean includeHeading) {
        update(entity);
        this.includeHeading = includeHeading;
    }

    public TargetPos(double x, double y, double z, RegistryKey<World> dimension) {
        this(new Vector3(x, y, z), dimension);
    }

    public TargetPos(double x, double y, double z, RegistryKey<World> dimension, float pitch, float yaw) {
        this(new Vector3(x, y, z), dimension, pitch, yaw);
    }

    public TargetPos(Vector3 pos, RegistryKey<World> dimension) {
        this(pos, dimension, 0, 0);
    }

    public TargetPos(Vector3 pos, RegistryKey<World> dimension, float pitch, float yaw) {
        this.pos = pos;
        this.dimension = dimension;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void update(Entity player) {
        pos = Vector3.fromEntity(player);
        dimension = player.level.dimension();
        pitch = player.xRot;
        yaw = player.yRot;
    }

    public double getX() {
        return pos.x;
    }

    public double getY() {
        return pos.y;
    }

    public double getZ() {
        return pos.z;
    }

    public Vector3 getPos() {
        return pos;
    }

    public RegistryKey<World> getDimension() {
        return dimension;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public String getReadableName(boolean fullDim) {
        return "X: " + (int) Math.floor(pos.x) +
                ", Y: " + (int) Math.floor(pos.y) +
                ", Z: " + (int) Math.floor(pos.z) +
                ", " + (fullDim ? dimension.location() : dimension.location().getPath());
    }

    public TargetPos setIncludeHeading(boolean includeHeading) {
        this.includeHeading = includeHeading;
        return this;
    }

    public TargetPos setX(double x) {
        pos.x = x;
        return this;
    }

    public TargetPos setY(double y) {
        pos.y = y;
        return this;
    }

    public TargetPos setZ(double z) {
        pos.z = z;
        return this;
    }

    public TargetPos setPos(Vector3 pos) {
        this.pos = pos;
        return this;
    }

    public TargetPos setDimension(RegistryKey<World> d) {
        dimension = d;
        return this;
    }

    public TargetPos setPitch(float p) {
        pitch = p;
        return this;
    }

    public TargetPos setYaw(float y) {
        yaw = y;
        return this;
    }

    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        pos.writeToNBT(nbt);
        nbt.putString("dim", dimension.location().toString());
        nbt.putBoolean("heading", includeHeading);
        if (includeHeading) {
            nbt.putFloat("pitch", pitch);
            nbt.putFloat("yaw", yaw);
        }
        return nbt;
    }

    public CompoundNBT writeToNBT() {
        return writeToNBT(new CompoundNBT());
    }

    public void readFromNBT(CompoundNBT nbt) {
        pos = Vector3.fromNBT(nbt);
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("dim")));
        includeHeading = nbt.getBoolean("heading");
        if (includeHeading) {
            pitch = nbt.getFloat("pitch");
            yaw = nbt.getFloat("yaw");
        }
    }

    public void write(MCDataOutput output) {
        output.writeVector(pos);
        output.writeResourceLocation(dimension.location());
        output.writeBoolean(includeHeading);
        if (includeHeading) {
            output.writeFloat(pitch);
            output.writeFloat(yaw);
        }
    }

    public void read(MCDataInput input) {
        pos = input.readVector();
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, input.readResourceLocation());
        includeHeading = input.readBoolean();
        if (includeHeading) {
            pitch = input.readFloat();
            yaw = input.readFloat();
        }
    }

    public Entity teleport(Entity entity) {
        if (includeHeading) {
            return TeleportUtils.teleportEntity(entity, dimension, pos, yaw, pitch);
        }
        return TeleportUtils.teleportEntity(entity, dimension, pos);
    }
}
