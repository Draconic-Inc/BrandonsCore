package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.utils.MultiBlockHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by brandon3055 on 1/4/2016.
 * This class is going to be used to define and hold a 3 dimensional array of blocks that makes up a structure.
 */
public class MultiBlockStorage {

    private MultiBlockHelper helper;
    private String[][][] blockStorage;
    private int xPos = 0;
    private int yPos = 0;


    public MultiBlockStorage(int size, MultiBlockHelper helper) {
        blockStorage = new String[size][size][size];
        this.helper = helper;
    }

    public void addRow(String... zRow) {
        if (zRow.length > blockStorage.length || zRow.length < blockStorage.length) {
            throw new RuntimeException("[MultiBlockStorage] Attempt to add zRow larger or smaller then defined structure size");
        } else if (xPos >= blockStorage.length) {
            throw new RuntimeException("[MultiBlockStorage] Attempt to add too many zRow's to layer");
        }
        blockStorage[xPos][yPos] = zRow;
        xPos++;
    }

    public void newLayer() {
        xPos = 0;
        yPos++;

        if (yPos >= blockStorage.length) {
            throw new RuntimeException("[MultiBlockStorage] Attempt to add too many layers to structure");
        }
    }

    /**
     * Checks the given position for a structure that matches this storage
     *
     * @param startPos refers to position x=0, y=0, z=0 in the storage array.
     */
    public boolean checkStructure(World world, BlockPos startPos) {
        for (int x = 0; x < blockStorage.length; x++) {
            for (int y = 0; y < blockStorage[0].length; y++) {
                for (int z = 0; z < blockStorage[0][0].length; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!helper.checkBlock(blockStorage[x][y][z], world, pos.add(startPos))) {
                        helper.invalidBlock = startPos.add(pos);
                        helper.expectedBlock = blockStorage[x][y][z];
                        return false;
                    }
                }
            }
        }

        helper.invalidBlock = null;
        return true;
    }

    /**
     * This method should only be used ether for testing or for use by a creative only method of spawning the structure.
     * If used for some sort of survival mode builder the MultiBlockHelper#setBlock method will need to be modified.
     */
    public void placeStructure(World world, BlockPos startPos) {
        for (int x = 0; x < blockStorage.length; x++) {
            for (int y = 0; y < blockStorage[0].length; y++) {
                for (int z = 0; z < blockStorage[0][0].length; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    helper.setBlock(blockStorage[x][y][z], world, pos.add(startPos));
                }
            }
        }
    }

    /**
     * When called MultiBlockHelper#forBlock will be called for every block in the structure.
     */
    public void forEachInStructure(World world, BlockPos startPos, int flag) {
        for (int x = 0; x < blockStorage.length; x++) {
            for (int y = 0; y < blockStorage[0].length; y++) {
                for (int z = 0; z < blockStorage[0][0].length; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    helper.forBlock(blockStorage[x][y][z], world, pos.add(startPos), startPos, flag);
                }
            }
        }
    }
}
