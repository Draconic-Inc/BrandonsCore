package com.brandon3055.brandonscore.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 24/4/2016.
 * Just some useful facing related stuff
 */
public class FacingUtils {
    /**
     * Offsets for all 8 blocks around X Axis
     */
    public static final BlockPos[] AROUND_X;
    /**
     * Offsets for all 8 blocks around Y Axis
     */
    public static final BlockPos[] AROUND_Y;
    /**
     * Offsets for all 8 blocks around Z Axis
     */
    public static final BlockPos[] AROUND_Z;
    /**
     * A combination of the 3.
     */
    public static final BlockPos[] AROUND_ALL;

    /**
     * Facings for the 4 faces around X Axis
     */
    public static final EnumFacing[] FACES_AROUND_X;
    /**
     * Facings for the 4 faces around Y Axis
     */
    public static final EnumFacing[] FACES_AROUND_Y;
    /**
     * Facings for the 4 faces around Z Axis
     */
    public static final EnumFacing[] FACES_AROUND_Z;

    static {
        AROUND_X = new BlockPos[]{new BlockPos(0, 1, -1), new BlockPos(0, 1, 0), new BlockPos(0, 1, 1), new BlockPos(0, 0, -1), new BlockPos(0, 0, 1), new BlockPos(0, -1, -1), new BlockPos(0, -1, 0), new BlockPos(0, -1, 1)};
        AROUND_Y = new BlockPos[]{new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1)};
        AROUND_Z = new BlockPos[]{new BlockPos(1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(-1, 1, 0), new BlockPos(1, 0, 0), new BlockPos(-1, 0, 0), new BlockPos(1, -1, 0), new BlockPos(0, -1, 0), new BlockPos(-1, -1, 0)};

        List<BlockPos> list = new ArrayList<BlockPos>();

        for (BlockPos pos : AROUND_X) {
            if (!list.contains(pos)) {
                list.add(pos);
            }
        }

        for (BlockPos pos : AROUND_Y) {
            if (!list.contains(pos)) {
                list.add(pos);
            }
        }

        for (BlockPos pos : AROUND_Z) {
            if (!list.contains(pos)) {
                list.add(pos);
            }
        }

        AROUND_ALL = list.toArray(new BlockPos[0]);

        List<EnumFacing> x = new ArrayList<EnumFacing>();
        List<EnumFacing> y = new ArrayList<EnumFacing>();
        List<EnumFacing> z = new ArrayList<EnumFacing>();

        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing.getAxis() != EnumFacing.Axis.X) {
                x.add(facing);
            }
            if (facing.getAxis() != EnumFacing.Axis.Y) {
                y.add(facing);
            }
            if (facing.getAxis() != EnumFacing.Axis.Z) {
                z.add(facing);
            }
        }

        FACES_AROUND_X = x.toArray(new EnumFacing[1]);
        FACES_AROUND_Y = y.toArray(new EnumFacing[1]);
        FACES_AROUND_Z = z.toArray(new EnumFacing[1]);
    }

    /**
     * Get the offset for the 8 blocks around the given axis
     *
     * @param axis Axis.X, Axis.Y or Axis.Z
     * @return a BlockPos[] containing 8 positions which are the offset value for each of the 8 blocks around the axis
     */
    public static BlockPos[] getAroundAxis(EnumFacing.Axis axis) {
        if (axis == EnumFacing.Axis.X) {
            return AROUND_X;
        }
        else if (axis == EnumFacing.Axis.Y) {
            return AROUND_Y;
        }
        else {
            return AROUND_Z;
        }
    }

    public static EnumFacing[] getAxisFaces(EnumFacing.Axis axis) {
        switch (axis) {
            case X: return new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST};
            case Y: return new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN};
            case Z: return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH};
            default: return new EnumFacing[0];
        }
    }

    public static EnumFacing[] getFacingsAroundAxis(EnumFacing.Axis axis) {
        if (axis == EnumFacing.Axis.X) {
            return FACES_AROUND_X;
        }
        else if (axis == EnumFacing.Axis.Y) {
            return FACES_AROUND_Y;
        }
        else {
            return FACES_AROUND_Z;
        }
    }

    public static EnumFacing rotateAround(EnumFacing facing, EnumFacing.Axis axis, boolean reverse) {
        switch (axis) {
            case X:

                if (facing != EnumFacing.WEST && facing != EnumFacing.EAST) {
                    return rotateX(facing, reverse);
                }

                return facing;
            case Y:

                if (facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                    return rotateY(facing, reverse);
                }

                return facing;
            case Z:

                if (facing != EnumFacing.NORTH && facing != EnumFacing.SOUTH) {
                    return rotateZ(facing, reverse);
                }

                return facing;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    private static EnumFacing rotateY(EnumFacing facing, boolean reverse) {
        switch (facing) {
            case NORTH:
                return reverse ? EnumFacing.WEST : EnumFacing.EAST;
            case EAST:
                return reverse ? EnumFacing.NORTH : EnumFacing.SOUTH;
            case SOUTH:
                return reverse ? EnumFacing.EAST : EnumFacing.WEST;
            case WEST:
                return reverse ? EnumFacing.SOUTH : EnumFacing.NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + facing);
        }
    }

    private static EnumFacing rotateX(EnumFacing facing, boolean reverse) {
        switch (facing) {
            case NORTH:
                return reverse ? EnumFacing.UP : EnumFacing.DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + facing);
            case SOUTH:
                return reverse ? EnumFacing.DOWN : EnumFacing.UP;
            case UP:
                return reverse ? EnumFacing.SOUTH : EnumFacing.NORTH;
            case DOWN:
                return reverse ? EnumFacing.NORTH : EnumFacing.SOUTH;
        }
    }

    private static EnumFacing rotateZ(EnumFacing facing, boolean reverse) {
        switch (facing) {
            case EAST:
                return reverse ? EnumFacing.UP : EnumFacing.DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + facing);
            case WEST:
                return reverse ? EnumFacing.DOWN : EnumFacing.UP;
            case UP:
                return reverse ? EnumFacing.WEST : EnumFacing.EAST;
            case DOWN:
                return reverse ? EnumFacing.EAST : EnumFacing.WEST;
        }
    }

    public static int destanceInDirection(BlockPos fromPos, BlockPos toPos, EnumFacing direction) {
        switch (direction) {
            case DOWN:
                return fromPos.getY() - toPos.getY();
            case UP:
                return toPos.getY() - fromPos.getY();
            case NORTH:
                return fromPos.getZ() - toPos.getZ();
            case SOUTH:
                return toPos.getZ() - fromPos.getZ();
            case WEST:
                return fromPos.getX() - toPos.getX();
            case EAST:
                return toPos.getX() - fromPos.getX();
        }
        return 0;
    }
}
