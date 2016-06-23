package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.util.math.BlockPos;

/**
 * Created by brandon3055 on 9/4/2016.
 * A class that holds 3 doubles
 */
public class Vec3D {

    public double x;
    public double y;
    public double z;

    public Vec3D() {
    }

    public Vec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D(Vec3D vec3I) {
        this.x = vec3I.x;
        this.y = vec3I.y;
        this.z = vec3I.z;
    }

    public Vec3D(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public Vec3D set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3D set(Vec3D vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
        return this;
    }

    public Vec3D set(BlockPos pos) {
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
        return this;
    }

    public Vec3D add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3D add(Vec3D vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
        return this;
    }

    public Vec3D add(BlockPos pos) {
        this.x += pos.getX();
        this.y += pos.getY();
        this.z += pos.getZ();
        return this;
    }

    public Vec3D subtract(BlockPos pos) {
        this.x -= pos.getX();
        this.y -= pos.getY();
        this.z -= pos.getZ();
        return this;
    }

    public Vec3D subtract(Vec3D vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
        return this;
    }

    public Vec3D subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3D multiply(Vec3D vec) {
        this.x *= vec.x;
        this.y *= vec.y;
        this.z *= vec.z;
        return this;
    }

    public Vec3D multiply(double x, double y, double z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vec3D copy() {
        return new Vec3D(this);
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
        Vec3D other = (Vec3D) obj;

        if (x == other.x && y == other.y && z == other.z) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return ((int) y + (int) z * 31) * 31 + (int) x;
    }

    /**
     * Calculates a directional vector between the two given points
     * This can be used for example if you have an entity at pos1 and you want to
     * apply motion so hat is moves towards pos2
     */
    public static Vec3D getDirectionVec(Vec3D vecFrom, Vec3D vecTo) {
        double distance = Utils.getDistanceAtoB(vecFrom, vecTo);
        if (distance == 0) {
            distance = 0.1;
        }
        Vec3D offset = vecTo.copy();
        offset.subtract(vecFrom);
        return new Vec3D(offset.x / distance, offset.y / distance, offset.z / distance);
    }
}
