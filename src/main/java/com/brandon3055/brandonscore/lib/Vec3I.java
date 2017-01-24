package com.brandon3055.brandonscore.lib;

import net.minecraft.util.math.BlockPos;

/**
 * Created by brandon3055 on 9/4/2016.
 * A class that holds 3 integers
 */
public class Vec3I {

    public int x;
    public int y;
    public int z;

    public Vec3I() {
    }

    public Vec3I(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3I(Vec3I vec3I) {
        this.x = vec3I.x;
        this.y = vec3I.y;
        this.z = vec3I.z;
    }

    public Vec3I(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3I vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public void set(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public void add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void add(Vec3I vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    public void add(BlockPos pos) {
        this.x += pos.getX();
        this.y += pos.getY();
        this.z += pos.getZ();
    }

    public void subtract(BlockPos pos) {
        this.x -= pos.getX();
        this.y -= pos.getY();
        this.z -= pos.getZ();
    }

    public void subtract(Vec3I vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
    }

    public void subtract(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public Vec3I copy() {
        return new Vec3I(this);
    }

    public BlockPos getPos() {
        return new BlockPos(x, y, z);
    }

    @Override
    public String toString() {
        return String.format("Vec3I: [x: %s, y: %s, z: %s]", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vec3I other = (Vec3I) obj;

        if (x == other.x && y == other.y && z == other.z) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return (y + z * 31) * 31 + x;
    }

    public int sum() {return Math.abs(x) + Math.abs(y) + Math.abs(z); }
}
