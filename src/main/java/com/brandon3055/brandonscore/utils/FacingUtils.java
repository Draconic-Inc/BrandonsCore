package com.brandon3055.brandonscore.utils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Created by brandon3055 on 24/4/2016.
 */
public class FacingUtils {
    /**Offsets for all 8 blocks around X Axis*/
    public static final BlockPos[] AROUND_X;
    /**Offsets for all 8 blocks around Y Axis*/
    public static final BlockPos[] AROUND_Y;
    /**Offsets for all 8 blocks around Z Axis*/
    public static final BlockPos[] AROUND_Z;

    static {
        AROUND_X = new BlockPos[] {
                new BlockPos(0, 1, -1), new BlockPos(0, 1, 0), new BlockPos(0, 1, 1),
                new BlockPos(0, 0, -1),                        new BlockPos(0, 0, 1),
                new BlockPos(0, -1, -1), new BlockPos(0, -1, 0), new BlockPos(0, -1, 1)
        };

        AROUND_Y = new BlockPos[] {
                new BlockPos(-1, 0, 1), new BlockPos(0, 0, 1), new BlockPos(1, 0, 1),
                new BlockPos(-1, 0, 0),                        new BlockPos(1, 0, 0),
                new BlockPos(-1, 0, -1), new BlockPos(0, 0, -1), new BlockPos(1, 0, -1)
        };

        AROUND_Z = new BlockPos[] {
                new BlockPos(1, 1, 0), new BlockPos(0, 1, 0), new BlockPos(-1, 1, 0),
                new BlockPos(1, 0, 0),                        new BlockPos(-1, 0, 0),
                new BlockPos(1, -1, 0), new BlockPos(0, -1, 0), new BlockPos(-1, -1, 0)
        };
    }

    /**
     * Get the offset for the 8 blocks around the given axis
     * @param axis Axis.X, Axis.Y or Axis.Z
     * @return a BlockPos[] containing 8 positions which are the offset value for each of the 8 blocks around the axis
     * */
    public static BlockPos[] getAroundAxis(EnumFacing.Axis axis) {
        if (axis == EnumFacing.Axis.X){
            return AROUND_X;
        }
        else if (axis == EnumFacing.Axis.Y) {
            return AROUND_Y;
        }
        else {
            return AROUND_Z;
        }
    }
}
