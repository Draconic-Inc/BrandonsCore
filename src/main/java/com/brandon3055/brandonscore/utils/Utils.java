package com.brandon3055.brandonscore.utils;

import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

/**
 * Created by Brandon on 25/07/2014.
 */
public class Utils {

    private static DecimalFormat energyValue = new DecimalFormat("###,###,###,###,###");

    public static String formatNumber(double value) {
        if (value < 1000D) return String.valueOf(value);
        else if (value < 1000000D) return String.valueOf(Math.round(value) / 1000D) + "K";
        else if (value < 1000000000D) return String.valueOf(Math.round(value / 1000D) / 1000D) + "M";
        else if (value < 1000000000000D) return String.valueOf(Math.round(value / 1000000D) / 1000D) + "B";
        else return String.valueOf(Math.round(value / 1000000000D) / 1000D) + "T";
    }

    public static String formatNumber(long value) {
        if (value < 1000L) return String.valueOf(value);
        else if (value < 1000000L) return String.valueOf(Math.round(value) / 1000D) + "K";
        else if (value < 1000000000L) return String.valueOf(Math.round(value / 1000L) / 1000D) + "M";
        else if (value < 1000000000000L) return String.valueOf(Math.round(value / 1000000L) / 1000D) + "B";
        else if (value < 1000000000000000L) return String.valueOf(Math.round(value / 1000000000L) / 1000D) + "T";
        else if (value < 1000000000000000000L) return String.valueOf(Math.round(value / 1000000000000L) / 1000D) + "Quad";
        else if (value <= Long.MAX_VALUE) return String.valueOf(Math.round(value / 1000000000000000L) / 1000D) + "Quin";
        else return "Something is very broken!!!!";
    }

    /**
     * Add commas to a number e.g. 161253126 > 161,253,126
     */
    public static String addCommas(int value) {
        return energyValue.format(value);
    }

    /**
     * Add commas to a number e.g. 161253126 > 161,253,126
     */
    public static String addCommas(long value) {
        return energyValue.format(value);
    }

    /**
     * Calculates the exact distance between two points in 3D space
     *
     * @param x1 point A x
     * @param y1 point A y
     * @param z1 point A z
     * @param x2 point B x
     * @param y2 point B y
     * @param z2 point B z
     * @return The distance between point A and point B
     */
    public static double getDistanceAtoB(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt((dx * dx + dy * dy + dz * dz));
    }

    public static double getDistanceAtoB(Vec3D pos1, Vec3D pos2) {
        return getDistanceAtoB(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

    public static boolean inRangeSphere(BlockPos posA, BlockPos posB, int range) {
        if (Math.abs(posA.getX() - posB.getX()) > range || Math.abs(posA.getY() - posB.getY()) > range || Math.abs(posA.getZ() - posB.getZ()) > range) {
            return false;
        }

        else return getDistanceSq(posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ()) <= range * range;
    }

    /**
     * Calculates the exact distance between two points in 2D space
     *
     * @param x1 point A x
     * @param z1 point A z
     * @param x2 point B x
     * @param z2 point B z
     * @return The distance between point A and point B
     */
    public static double getDistanceAtoB(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return Math.sqrt((dx * dx + dz * dz));
    }

    public static double getDistanceSq(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double getDistanceSq(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    /**
     * Returns true if this is a client connected to a remote server.
     */
    public static boolean isConnectedToDedicatedServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance() == null;
    }

    /**
     * Update the blocks an all 6 sides of a blocks.
     */
    public static void updateNeabourBlocks(World world, BlockPos pos) {
        world.notifyBlockOfStateChange(pos, world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(-1, 0, 0), world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(1, 0, 0), world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(0, -1, 0), world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(0, 1, 0), world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(0, 0, -1), world.getBlockState(pos).getBlock());
        world.notifyBlockOfStateChange(pos.add(0, 0, 1), world.getBlockState(pos).getBlock());


//		world.notifyBlocksOfNeighborChange(x, y, z, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x - 1, y, z, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x + 1, y, z, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x, y - 1, z, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x, y + 1, z, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x, y, z - 1, world.getBlock(x, y, z));
//		world.notifyBlocksOfNeighborChange(x, y, z + 1, world.getBlock(x, y, z));
    }

    /**
     * Determine the orientation of a blocks based on the position of the entity that placed it.
     */
    public static int determineOrientation(int x, int y, int z, EntityLivingBase entity) {
        if (MathHelper.abs((float) entity.posX - (float) x) < 2.0F && MathHelper.abs((float) entity.posZ - (float) z) < 2.0F) {
            double d0 = entity.posY + 1.82D - (double) entity.getYOffset();

            if (d0 - (double) y > 2.0D) return 0;

            if ((double) y - d0 > 0.0D) return 1;
        }

        int l = MathHelper.floor_double((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? 3 : (l == 1 ? 4 : (l == 2 ? 2 : (l == 3 ? 5 : 0)));
    }

    /**
     * Rounds the number of decimal places based on the given multiplier.<br>
     * e.g.<br>
     * Input: 17.5245743<br>
     * multiplier: 1000<br>
     * Output: 17.534<br>
     * multiplier: 10<br>
     * Output 17.5<br><br>
     *
     * @param number The input value.
     * @param multiplier The multiplier.
     * @return The input rounded to a number of decimal places based on the multiplier.
     */
    public static double round(double number, double multiplier) {
        return Math.round(number * multiplier) / multiplier;
    }

    public static int getNearestMultiple(int number, int multiple) {
        int result = number;

        if (number < 0) result *= -1;

        if (result % multiple == 0) return number;
        else if (result % multiple < multiple / 2) result = result - result % multiple;
        else result = result + (multiple - result % multiple);

        if (number < 0) result *= -1;

        return result;
    }

    /**
     * Simple method to convert a Double object to a primitive int
     */
    public static int toInt(double d) {
        return (int) d;
    }

    public static int parseInt(String s) {
        return parseInt(s, true);
    }

    public static int parseInt(String s, boolean catchException) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            if (catchException) {
                return 0;
            }
            throw e;
        }
    }

    public static int parseHex(String s) {
        return parseHex(s, true);
    }

    public static int parseHex(String s, boolean catchException) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        if (catchException) {
            try {
                return (int) Long.parseLong(s, 16);
            }
            catch (Exception e) {
                return 0;
            }
        }
        else {
            return (int) Long.parseLong(s, 16);
        }
    }

    public static double map(double valueIn, double inMin, double inMax, double outMin, double outMax) {
        return (valueIn - inMin) * (outMax - outMin) / (inMax - inMin) + outMin;
    }


    @Nullable
    public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double distance) {
        return getClosestPlayer(world, posX, posY, posZ, distance, true);
    }

    @Nullable
    public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double distance, boolean includeCreative) {
        return getClosestPlayer(world, posX, posY, posZ, distance, includeCreative, false);
    }


    @Nullable
    public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double distance, boolean includeCreative, boolean includeSpectators)
    {
        double d0 = -1.0D;
        EntityPlayer closestPlayer = null;

        for (int i = 0; i < world.playerEntities.size(); ++i)
        {
            EntityPlayer player = world.playerEntities.get(i);

            if ((!player.isCreative() || includeCreative) && (!player.isSpectator() || includeSpectators))
            {
                double d1 = player.getDistanceSq(posX, posY, posZ);

                if ((distance < 0.0D || d1 < distance * distance) && (d0 == -1.0D || d1 < d0))
                {
                    d0 = d1;
                    closestPlayer = player;
                }
            }
        }

        return closestPlayer;
    }

}
