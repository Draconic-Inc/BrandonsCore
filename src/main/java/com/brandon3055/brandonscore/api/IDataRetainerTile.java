package com.brandon3055.brandonscore.api;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 26/3/2016.
 * Implemented by the TileEntity of blocks that need to retain custom data when harvested.
 *
 * Note: If you want to save SyncableObjects to the item set the objects to NOT save when you register them.
 * Then save them manually vial this interface. See TileEntityDetector in Draconic Evolution form an example
 *
 * //TODO Possible make this function via the block break event so it can be implemented on any tile entity for better mod support.
 * //Or add the event on top of the current logic. So if block broken is not instance of BlockBCore then save via event?
 */
public interface IDataRetainerTile {

    /**
     * Used to write custom tile specific data to NBT.
     * Data saved in this method will be synced with the client via description packets.
     * Data saved in this method will also be saved to the ItemBlock when the tile is harvested so it can be restored
     * when the tile is placed.
     */
    void writeRetainedData(NBTTagCompound dataCompound);

    /**
     * This is where any data saved in writeRetainedData should be loaded from NBT.
     */
    void readRetainedData(NBTTagCompound dataCompound);
}
