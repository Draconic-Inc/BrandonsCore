package com.brandon3055.brandonscore.utils;

import com.brandon3055.brandonscore.lib.Vec3D;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.function.Consumer;

/**
 * Created by Brandon on 25/07/2014.
 */
@SuppressWarnings("ALL")
public class Utils {

    public static final String SELECT = "\u00A7";
    private static DecimalFormat energyValue = new DecimalFormat("###,###,###,###,###");

    @Deprecated
    public static String formatNumber(double value) {
        if (Math.abs(value) < 1000D) return String.valueOf(value);
        else if (Math.abs(value) < 1000000D) return addCommas((int) value); //I mean whats the ploint of displaying 1.235K instead of 1,235?
        else if (Math.abs(value) < 1000000000D) return String.valueOf(Math.round(value / 1000D) / 1000D) + "M";
        else if (Math.abs(value) < 1000000000000D) return String.valueOf(Math.round(value / 1000000D) / 1000D) + "B";
        else return String.valueOf(Math.round(value / 1000000000D) / 1000D) + "T";
    }

    @Deprecated //TODO Switch to localizations used by DE
    public static String formatNumber(long value) {
        if (value == Long.MIN_VALUE) value = Long.MAX_VALUE;
        if (Math.abs(value) < 1000L) return String.valueOf(value);
        else if (Math.abs(value) < 1000000L) return Utils.addCommas(value); //I mean whats the ploint of displaying 1.235K instead of 1,235?
        else if (Math.abs(value) < 1000000000L) return String.valueOf(Math.round(value / 100000L) / 10D) + "M";
        else if (Math.abs(value) < 1000000000000L) return String.valueOf(Math.round(value / 100000000L) / 10D) + "G";
        else if (Math.abs(value) < 1000000000000000L) return String.valueOf(Math.round(value / 1000000000L) / 1000D) + "T";
        else if (Math.abs(value) < 1000000000000000000L) return String.valueOf(Math.round(value / 1000000000000L) / 1000D) + "P";
        else if (Math.abs(value) <= Long.MAX_VALUE) return String.valueOf(Math.round(value / 1000000000000000L) / 1000D) + "E";
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
    public static double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt((dx * dx + dy * dy + dz * dz));
    }

    @Deprecated
    public static double getDistanceAtoB(double x1, double y1, double z1, double x2, double y2, double z2) {
        return getDistance(x1, y1, z1, x2, y2, z2);
    }

    @Deprecated
    public static double getDistanceAtoB(Vec3D pos1, Vec3D pos2) {
        return getDistance(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

    public static double getDistance(Vec3D pos1, Vec3D pos2) {
        return getDistance(pos1.x, pos1.y, pos1.z, pos2.x, pos2.y, pos2.z);
    }

    public static int getCardinalDistance(BlockPos pos1, BlockPos pos2) {
        int x = Math.abs(pos2.getX() - pos1.getX());
        int y = Math.abs(pos2.getY() - pos1.getY());
        int z = Math.abs(pos2.getZ() - pos1.getZ());
        return Math.max(Math.max(x, y), z);
    }

    public static boolean inRangeSphere(BlockPos posA, BlockPos posB, int range) {
        if (Math.abs(posA.getX() - posB.getX()) > range || Math.abs(posA.getY() - posB.getY()) > range || Math.abs(posA.getZ() - posB.getZ()) > range) {
            return false;
        } else
            return getDistanceSq(posA.getX(), posA.getY(), posA.getZ(), posB.getX(), posB.getY(), posB.getZ()) <= range * range;
    }


    @Deprecated
    public static double getDistanceAtoB(double x1, double z1, double x2, double z2) {
        double dx = x1 - x2;
        double dz = z1 - z2;
        return Math.sqrt((dx * dx + dz * dz));
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
    public static double getDistance(double x1, double z1, double x2, double z2) {
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

//    /**
//     * Update the blocks an all 6 sides of a blocks.
//     */
//    public static void updateNeabourBlocks(World world, BlockPos pos) {
//        world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
//    }//Just use the method in world directly

    /**
     * Determine the orientation of a blocks based on the position of the entity that placed it.
     */
    public static int determineOrientation(int x, int y, int z, LivingEntity entity) {
        if (Mth.abs((float) entity.getX() - (float) x) < 2.0F && Mth.abs((float) entity.getZ() - (float) z) < 2.0F) {
            double d0 = entity.getY() + 1.82D - (double) entity.getMyRidingOffset();

            if (d0 - (double) y > 2.0D) return 0;

            if ((double) y - d0 > 0.0D) return 1;
        }

        int l = Mth.floor((double) (entity.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;
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
        } catch (Exception e) {
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
        } catch (Exception e) {
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
            } catch (Exception e) {
                return 0;
            }
        } else {
            return (int) Long.parseLong(s, 16);
        }
    }

    public static boolean validInteger(String value) {
        try {
            Long.parseLong(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static boolean validDouble(String value) {
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Nullable
    public static Player getClosestPlayer(Level world, double posX, double posY, double posZ, double distance) {
        return getClosestPlayer(world, posX, posY, posZ, distance, true);
    }

    @Nullable
    public static Player getClosestPlayer(Level world, double posX, double posY, double posZ, double distance, boolean includeCreative) {
        return getClosestPlayer(world, posX, posY, posZ, distance, includeCreative, false);
    }


    @Nullable
    @Deprecated //Check the world method
    public static Player getClosestPlayer(Level world, double posX, double posY, double posZ, double distance, boolean includeCreative, boolean includeSpectators) {
        double d0 = -1.0D;
        Player closestPlayer = null;

        for (int i = 0; i < world.players().size(); ++i) {
            Player player = world.players().get(i);

            if ((!player.isCreative() || includeCreative) && (!player.isSpectator() || includeSpectators)) {
                double d1 = player.distanceToSqr(posX, posY, posZ);

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
        } else {
            return input.substring(0, length) + trimExtension;
        }
    }

    public static String getClipboardString() {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception var1) {}

        return "";
    }

    public static void setClipboardString(String copyText) {
        if (!StringUtils.isEmpty(copyText)) {
            try {
                StringSelection stringselection = new StringSelection(copyText);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringselection, null);
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
    }

    public static void openWebLink(URI url) {
        Util.getPlatform().openUri(url);
    }

    public static int parseColourRGB(String value) {
        if (value.startsWith("0x") || value.startsWith("#")) {
            value = value.replace("0x", "").replace("#", "");
            return parseHex(value, false);
        } else if (value.contains(",")) {
            String[] vals = value.split(",");
            if (vals.length != 3)
                throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[1]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[2]);
            return r << 16 | g << 8 | b;
        } else {
            throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
        }
    }

    public static int parseColourARGB(String value) {
        if (value.startsWith("0x") || value.startsWith("#")) {
            value = value.replace("0x", "").replace("#", "");
            return parseHex(value, false);
        } else if (value.contains(",")) {
            String[] vals = value.split(",");
            if (vals.length < 3 || vals.length > 4) {
                throw new NumberFormatException("Number must be a hex using the format 0xAARRGGBB or #AARRGGBB");
            }
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[1]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[2]);
            int a = vals.length == 4 ? vals[3].contains(".") ? (int) (Double.parseDouble(vals[3]) * 255) : Integer.parseInt(vals[3]) : 0xFF;
            return a << 24 | r << 16 | g << 8 | b;
        } else {
            throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
        }
    }

    private static BiMap<Block, Fluid> fluidBlocks = null;

    public static Fluid lookupFluidForBlock(Block block) {
        if (fluidBlocks == null) {
            BiMap<Block, Fluid> tmp = HashBiMap.create();
            for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
                Block fluidBlock = fluid.defaultFluidState().createLegacyBlock().getBlock();
                if (fluidBlock != Blocks.AIR) {
                    tmp.put(fluidBlock, fluid);
                }
            }
            fluidBlocks = tmp;
        }
        return fluidBlocks.get(block);
    }

    public static String getTextFormatString(String stringIn) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = -1;
        int j = stringIn.length();

        while ((i = stringIn.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                ChatFormatting textformatting = ChatFormatting.getByCode(stringIn.charAt(i + 1));
                if (textformatting != null) {
                    if (!textformatting.isFormat()) {
                        stringbuilder.setLength(0);
                    }

                    if (textformatting != ChatFormatting.RESET) {
                        stringbuilder.append((Object) textformatting);
                    }
                }
            }
        }

        return stringbuilder.toString();
    }

    @Deprecated //Use world.isLoaded or world this.getChunkSource().hasChunk
    public static boolean isAreaLoaded(Level world, BlockPos pos, ChunkHolder.FullChunkStatus minimum) {
        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkAccess ichunk = world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
        if (!(ichunk instanceof LevelChunk)) {
            return false;
        }

        ChunkHolder.FullChunkStatus locationType = ((LevelChunk) ichunk).getFullStatus();
        return locationType.isOrAfter(minimum);
    }

    public static long safeAdd(long x, long y) {
        long r = x + y;
        if (((x ^ r) & (y ^ r)) < 0) {
            return Long.MAX_VALUE;
        }
        return r;
    }

    public static int scaleToTPS(Level world, int min, int max) {
        if (!(world instanceof ServerLevel)) return max;
        long[] times = world.getServer().getTickTime(world.dimension());
        if (times == null) return max;

        double worldTickTime = mean(times) * 0.000001;
        double worldTPS = Math.min(1000.0 / worldTickTime, 20);

        return codechicken.lib.math.MathHelper.clip((int) MathUtils.map(worldTPS, 5, 20, min, max), min, max);
    }

    public static long mean(long[] values) {
        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    public static void hollowCube(BlockPos min, BlockPos max, Consumer<BlockPos> callback) {
        hollowCube(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), callback);
    }

    public static void hollowCube(int xmin, int ymin, int zmin, int xmax, int ymax, int zmax, Consumer<BlockPos> callback) {
        if (xmax - xmin < 2 || ymax - ymin < 2 || zmax - zmin < 2) {
            betweenClosed(xmin, ymin, zmin, xmax, ymax, zmax, callback);
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = xmin; x <= xmax; x++) {
            for (int y = ymin + 1; y <= ymax - 1; y++) {
                for (int z = zmin; z <= zmax; z += zmax - zmin) {
                    callback.accept(cursor.set(x, y, z));
                }
            }

            for (int z = zmin; z <= zmax; z++) {
                for (int y = ymin; y <= ymax; y += ymax - ymin) {
                    callback.accept(cursor.set(x, y, z));
                }
            }
        }


        for (int z = zmin + 1; z <= zmax - 1; z++) {
            for (int y = ymin + 1; y <= ymax - 1; y++) {
                for (int x = xmin; x <= xmax; x += xmax - xmin) {
                    callback.accept(cursor.set(x, y, z));
                }
            }
        }
    }

    public static void betweenClosed(BlockPos min, BlockPos max, Consumer<BlockPos> callback) {
        betweenClosed(Math.min(min.getX(), max.getX()), Math.min(min.getY(), max.getY()), Math.min(min.getZ(), max.getZ()), Math.max(min.getX(), max.getX()), Math.max(min.getY(), max.getY()), Math.max(min.getZ(), max.getZ()), callback);
    }

    public static void betweenClosed(int xmin, int ymin, int zmin, int xmax, int ymax, int zmax, Consumer<BlockPos> callback) {
        int width = xmax - xmin + 1;
        int height = ymax - ymin + 1;
        int depth = zmax - zmin + 1;
        int total = width * height * depth;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int index = 0; index < total; index++) {
            int x = index % width;
            int j1 = index / width;
            int y = j1 % height;
            int z = j1 / height;
            callback.accept(cursor.set(xmin + x, ymin + y, zmin + z));
        }
    }
}

