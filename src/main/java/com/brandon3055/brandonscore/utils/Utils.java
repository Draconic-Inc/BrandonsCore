package com.brandon3055.brandonscore.utils;

import com.brandon3055.brandonscore.lib.Vec3D;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.text.DecimalFormat;

/**
 * Created by Brandon on 25/07/2014.
 */
@SuppressWarnings("ALL")
public class Utils {

    public static final String SELECT = "\u00A7";

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
        else if (value < 1000000000000L) return String.valueOf(Math.round(value / 1000000L) / 1000D) + "G";
        else if (value < 1000000000000000L) return String.valueOf(Math.round(value / 1000000000L) / 1000D) + "T";
        else if (value < 1000000000000000000L) return String.valueOf(Math.round(value / 1000000000000L) / 1000D) + "P";
        else if (value <= Long.MAX_VALUE) return String.valueOf(Math.round(value / 1000000000000000L) / 1000D) + "E";
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

        else
            return getDistanceSq(posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ()) <= range * range;
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

//    /**
//     * Update the blocks an all 6 sides of a blocks.
//     */
//    public static void updateNeabourBlocks(World world, BlockPos pos) {
//        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
//    }//Just use the method in world directly

    /**
     * Determine the orientation of a blocks based on the position of the entity that placed it.
     */
    public static int determineOrientation(int x, int y, int z, EntityLivingBase entity) {
        if (MathHelper.abs((float) entity.posX - (float) x) < 2.0F && MathHelper.abs((float) entity.posZ - (float) z) < 2.0F) {
            double d0 = entity.posY + 1.82D - (double) entity.getYOffset();

            if (d0 - (double) y > 2.0D) return 0;

            if ((double) y - d0 > 0.0D) return 1;
        }

        int l = MathHelper.floor((double) (entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? 3 : (l == 1 ? 4 : (l == 2 ? 2 : (l == 3 ? 5 : 0)));
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

    public static double parseDouble(String s, boolean catchException) {
        if (s == null || s.length() == 0) {
            return 0;
        }

        try {
            return Double.parseDouble(s);
        }
        catch (Exception e) {
            if (catchException) {
                return 0;
            }
            throw e;
        }
    }

    public static double parseDouble(String s) {
        return parseDouble(s, true);
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

    public static boolean validInteger(String value) {
        try {
            Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean validDouble(String value) {
        try {
            Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return false;
        }
        return true;
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
    public static EntityPlayer getClosestPlayer(World world, double posX, double posY, double posZ, double distance, boolean includeCreative, boolean includeSpectators) {
        double d0 = -1.0D;
        EntityPlayer closestPlayer = null;

        for (int i = 0; i < world.playerEntities.size(); ++i) {
            EntityPlayer player = world.playerEntities.get(i);

            if ((!player.isCreative() || includeCreative) && (!player.isSpectator() || includeSpectators)) {
                double d1 = player.getDistanceSq(posX, posY, posZ);

                if ((distance < 0.0D || d1 < distance * distance) && (d0 == -1.0D || d1 < d0)) {
                    d0 = d1;
                    closestPlayer = player;
                }
            }
        }

        return closestPlayer;
    }

    /**
     * This is mostly just here as a reminder to myself.<br>
     * This check is the reverse of the usual object instance of TargetClass check so i always get it backwards.<br>
     * This is more like TargetClass instanceof ObjectClass.
     */
    public static boolean checkClassInstanceOf(Class clazz, Class instanceOfThis) {
        if (clazz == null || instanceOfThis == null) {
            return clazz == null && instanceOfThis == null;
        }
        return instanceOfThis.isAssignableFrom(clazz);
    }

    public static String trimString(String input, int length, String trimExtension) {
        if (input.length() <= length) {
            return input;
        }
        else {
            return input.substring(0, length) + trimExtension;
        }
    }

    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        }
        catch (Exception var1) {}

        return "";
    }

    public static void setClipboardString(String copyText) {
        if (!StringUtils.isEmpty(copyText)) {
            try {
                StringSelection stringselection = new StringSelection(copyText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
            }
            catch (Exception var2) {}
        }
    }

    public static void openWebLink(URI url) {
        try {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop").invoke((Object) null);
            oclass.getMethod("browse", URI.class).invoke(object, url);
        }
        catch (Throwable throwable1) {
            Throwable throwable = throwable1.getCause();
            LogHelperBC.error("Couldn't open link: {}", (Object) (throwable == null ? "<UNKNOWN>" : throwable.getMessage()));
        }
    }

    public static int parseColourRGB(String value) {
        if (value.startsWith("0x") || value.startsWith("#")) {
            value = value.replace("0x", "").replace("#", "");
            return parseHex(value, false);
        }
        else if (value.contains(",")) {
            String[] vals = value.split(",");
            if (vals.length != 3)
                throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[1]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[2]);
            return r << 16 | g << 8 | b;
        }
        else {
            throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
        }
    }

    public static int parseColourARGB(String value) {
        if (value.startsWith("0x") || value.startsWith("#")) {
            value = value.replace("0x", "").replace("#", "");
            return parseHex(value, false);
        }
        else if (value.contains(",")) {
            String[] vals = value.split(",");
            if (vals.length < 3 || vals.length > 4){
                throw new NumberFormatException("Number must be a hex using the format 0xAARRGGBB or #AARRGGBB");
            }
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[1]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[2]);
            int a = vals.length == 4 ? vals[3].contains(".") ? (int) (Double.parseDouble(vals[3]) * 255) : Integer.parseInt(vals[3]) : 0xFF;
            return a << 24 | r << 16 | g << 8 | b;
        }
        else {
            throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
        }    }
}

