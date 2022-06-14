package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import net.minecraft.nbt.CompoundNBT;

/**
 * Created by brandon3055 on 12/06/2017.
 *
 */
public interface IDataManager {

    /**
     * Use this to detect and send changes to the client via your own sync packet. See {@link TileDataManager} for an example
     * This should be called by your tile, container, etc every tick.
     */
    void detectAndSendChanges();

    /**
     * If you wish to create your own data manager your mod will need to have a {@link codechicken.lib.packet.ICustomPacketHandler} registered with {@link PacketCustom}
     * In this method you need to return a new PacketCustom with your chanel key and a type index of your choosing.
     * Data to be synchronized will be whiten to this packet and the packet will then be sent to the client.
     *
     * You are then responsible for making sure the packet gets back to receiveSyncData via your PacketHandler.
     * For example say you were using this for tile synchronization. When you create the packet you would
     * write the tile position to the packet before returning it. Then when received by the client you can read that position and get the client side tile.
     * From the tile you should be able to get the data manager and from there you just pass the packet to receiveSyncData
     *
     * @return a new {@link PacketCustom} with your chanel key and any additional data required to get it to the data manager client side.
     */
    PacketCustom createSyncPacket(); //TODO Not sure if i really need to define this in the interface...

    void receiveSyncData(MCDataInput input);

    /**
     * @param name the name of the data to get.
     * @return return the managed object with the given name or null if there is no object registered with this name.
     */
    IManagedData getDataByName(String name);

    /**
     * This is only used for data synchronization to avoid sending the name over the network.
     * @param index the index of the object to get.
     * @return the object with the given index.
     */
    IManagedData getDataByIndex(int index);

    /**
     * Write all managed data to NBT
     * It is recommended to create a new tag compound to which you can save oll data,
     * Then write that compound to the given compound.
     */
    void writeToNBT(CompoundNBT compound);

    /**
     * Load all data from NBT
     */
    void readFromNBT(CompoundNBT compound);

    /**
     * Called by a data object when its value is updated.
     * Primarily used to mark the base tile as dirty.
     * Also handles {@link DataFlags#CLIENT_CONTROL} flag.
     */
    void markDirty();

    /**
     * @return true if called client side.
     */
    boolean isClientSide();

    /**
     * This is used by data objects with the {@link DataFlags#CLIENT_CONTROL} flag enabled.
     * This is responsible for sending the data to the server.
     * Upon receival of the data the server must first confirm that client control is enabled for this data
     * then validate the received value before applying it.
     *
     * Note: Depending on use case you my need to consider security. Do you need to verify that the player has permission to send this packet?
     * {@link TileDataManager} does this by ensuring the player has the tile's container open.
     * @param data The data to be send to the server.
     */
    void sendToServer(IManagedData data);
}
