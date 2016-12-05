package com.brandon3055.brandonscore.lib;

import net.minecraft.util.math.BlockPos;

/**
 * Created by brandon3055 on 9/4/2016.
 * A class that holds 3 doubles
 */
public class Vec3B {

    public byte x;
    public byte y;
    public byte z;

    public Vec3B() {
    }

    public Vec3B(Vec3B vec3B) {
        this.x = vec3B.x;
        this.y = vec3B.y;
        this.z = vec3B.z;
    }

    public Vec3B(BlockPos pos) {
        this.x = (byte) pos.getX();
        this.y = (byte) pos.getY();
        this.z = (byte) pos.getZ();
    }

    public Vec3B(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3B set(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public BlockPos toPos() {
        return new BlockPos(x, y, z);
    }

    public Vec3B set(Vec3B vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public Vec3B add(byte x, byte y, byte z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3B add(Vec3B vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3B add(BlockPos pos) {
        this.x += pos.getX();
        this.y += pos.getY();
        this.z += pos.getZ();
        return this;
    }

    public Vec3B subtract(BlockPos pos) {
        this.x -= pos.getX();
        this.y -= pos.getY();
        this.z -= pos.getZ();
        return this;
    }

    public Vec3B subtract(Vec3B vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3B subtract(byte x, byte y, byte z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3B copy() {
        return new Vec3B(this);
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vec3D: [x: %s, y: %s, z: %s]", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vec3B other = (Vec3B) obj;

        if (x == other.x && y == other.y && z == other.z) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return ((int) y + (int) z * 31) * 31 + (int) x;
    }
}
