package com.brandon3055.brandonscore.utils;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
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
    private double x;
    private double y;
    private double z;
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
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.pitch = 0;
        this.yaw = 0;
    }

    public TargetPos(double x, double y, double z, RegistryKey<World> dimension, float pitch, float yaw) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public void update(Entity player) {
        x = player.getX();
        y = player.getY();
        z = player.getZ();
        dimension = player.level.dimension();
        pitch = player.xRot;
        yaw = player.yRot;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
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
        return "X: " + (int) Math.floor(x) +
                ", Y: " + (int) Math.floor(y) +
                ", Z: " + (int) Math.floor(z) +
                ", " + (fullDim ? dimension.location() : dimension.location().getPath());
    }

    public TargetPos setIncludeHeading(boolean includeHeading) {
        this.includeHeading = includeHeading;
        return this;
    }

    public TargetPos setX(double x) {
        this.x = x;
        return this;
    }

    public TargetPos setY(double y) {
        this.y = y;
        return this;
    }

    public TargetPos setZ(double z) {
        this.z = z;
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
        nbt.putDouble("x", x);
        nbt.putDouble("y", y);
        nbt.putDouble("z", z);
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
        x = nbt.getDouble("x");
        y = nbt.getDouble("y");
        z = nbt.getDouble("z");
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("dim")));
        includeHeading = nbt.getBoolean("heading");
        if (includeHeading) {
            pitch = nbt.getFloat("pitch");
            yaw = nbt.getFloat("yaw");
        }
    }

    public void write(MCDataOutput output) {
        output.writeDouble(x);
        output.writeDouble(y);
        output.writeDouble(z);
        output.writeResourceLocation(dimension.location());
        output.writeBoolean(includeHeading);
        if (includeHeading) {
            output.writeFloat(pitch);
            output.writeFloat(yaw);
        }
    }

    public void read(MCDataInput input) {
        x = input.readDouble();
        y = input.readDouble();
        z = input.readDouble();
        dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, input.readResourceLocation());
        includeHeading = input.readBoolean();
        if (includeHeading) {
            pitch = input.readFloat();
            yaw = input.readFloat();
        }
    }

    public Entity teleport(Entity entity) {
        if (includeHeading) {
            return TeleportUtils.teleportEntity(entity, dimension, x, y, z, yaw, pitch);
        }
        return TeleportUtils.teleportEntity(entity, dimension, x, y, z);
    }
}
