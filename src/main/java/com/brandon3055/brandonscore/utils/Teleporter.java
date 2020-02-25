package com.brandon3055.brandonscore.utils;

import com.brandon3055.brandonscore.lib.TeleportUtils;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

public class Teleporter
{
    public static class TeleportLocation {
        protected double xCoord;
        protected double yCoord;
        protected double zCoord;
        protected DimensionType dimension;
        protected float pitch;
        protected float yaw;
        protected String name;
        protected String dimensionName = "";
        protected boolean writeProtected = false;

        public TeleportLocation() {

        }

        public TeleportLocation(double x, double y, double z, DimensionType dimension) {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = 0;
            this.yaw = 0;
        }

        public TeleportLocation(double x, double y, double z, DimensionType dimension, float pitch, float yaw) {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = pitch;
            this.yaw = yaw;
        }

        public TeleportLocation(double x, double y, double z, DimensionType dimension, float pitch, float yaw, String name) {
            this.xCoord = x;
            this.yCoord = y;
            this.zCoord = z;
            this.dimension = dimension;
            this.pitch = pitch;
            this.yaw = yaw;
            this.name = name;
        }

        public double getXCoord() {
            return xCoord;
        }

        public double getYCoord() {
            return yCoord;
        }

        public double getZCoord() {
            return zCoord;
        }

        public DimensionType getDimension() {
            return dimension;
        }

        public String getDimensionName() {
            return dimensionName;
        }

        public float getPitch() {
            return pitch;
        }

        public float getYaw() {
            return yaw;
        }

        public String getName() {
            return name;
        }

        public boolean getWriteProtected() {
            return writeProtected;
        }

        public void setXCoord(double x) {
            xCoord = x;
        }

        public void setYCoord(double y) {
            yCoord = y;
        }

        public void setZCoord(double z) {
            zCoord = z;
        }

        public void setDimension(DimensionType d) {
            dimension = d;
        }

        public void setPitch(float p) {
            pitch = p;
        }

        public void setYaw(float y) {
            yaw = y;
        }

        public void setName(String s) {
            name = s;
        }

        public void setWriteProtected(boolean b) {
            writeProtected = b;
        }

        public void writeToNBT(CompoundNBT compound) {
            compound.putDouble("X", xCoord);
            compound.putDouble("Y", yCoord);
            compound.putDouble("Z", zCoord);
            compound.putString("Dimension", dimension.getRegistryName().toString());
            compound.putFloat("Pitch", pitch);
            compound.putFloat("Yaw", yaw);
            compound.putString("Name", name);
            compound.putString("DimentionName", dimensionName);
            compound.putBoolean("WP", writeProtected);
        }

        public void readFromNBT(CompoundNBT compound) {
            xCoord = compound.getDouble("X");
            yCoord = compound.getDouble("Y");
            zCoord = compound.getDouble("Z");
            dimension = DimensionType.byName(new ResourceLocation(compound.getString("Dimension")));
            pitch = compound.getFloat("Pitch");
            yaw = compound.getFloat("Yaw");
            name = compound.getString("Name");
            dimensionName = compound.getString("DimentionName");
            writeProtected = compound.getBoolean("WP");
        }

        public void teleport(Entity entity) {
            TeleportUtils.teleportEntity(entity, dimension, xCoord, yCoord, zCoord, yaw, pitch);
        }

        public void setDimensionName(String dimensionName) {
            this.dimensionName = dimensionName;
        }

        @Override
        public int hashCode() {
            return (xCoord + "-" + yCoord + "-" + zCoord + "-" + name + "-" + dimensionName + "-" + dimension + "-" + yaw + "-" + pitch).hashCode();
        }
    }
}
