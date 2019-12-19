package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundNBT;

/**
 * Created by brandon3055 on 12/06/2017.
 * <p>
 * The idea behind managed data objects is to make saving, loading and synchronizing data as simple as possible.
 * For example you can create a ManagedInt in a tile that implements {@link IDataManager} then simply register that int and... that is.
 * The int value stored in that int object will be saved, loaded and synchronized automatically.
 * <p>
 * Note: depending on the the manager implementation you can create an object that only saves or only syncs you dont have to do both.
 */
public interface IManagedData {

    /**
     * @return A unique (within the manager its registered to) name for the purpose of identifying and saving this data.
     */
    String getName();

    /**
     * This index is used for sending identifying this data when it is sent over the network.
     * This index should be set by the manager and must be the same both server and client side.
     * How you assign this index is up to you but a safe bet would be to just start at 0 ind increment
     * every time a new data object is registered.
     *
     * @return the index of this managed data.
     */
    int getIndex();

    /**
     * Serialize this object to bytes for the purpose of sending it over the network.
     * Note do not serialize the data name ir index as the manager will have already added the
     * index of this data for the purpose of identification at the other end.
     * And sending the name would very inefficient as its not needed and should always be the same
     * on client and server.
     */
    void toBytes(MCDataOutput output);

    /**
     * De serialize this object from bytes.
     */
    void fromBytes(MCDataInput input);

    /**
     * Save this data to nbt using its name to identify it.
     * The data manager should create a dedicated compound to save all data to so
     * so you should not have to worry about the tag mame conflicting with some other random tag on the tile/item etc
     */
    void toNBT(CompoundNBT compound);

    /**
     * Load this data from nbt.
     */
    void fromNBT(CompoundNBT compound);

    /**
     * Marks this data as 'dirty' meaning it needs to be saved/synchronized
     */
    void markDirty();

    /**
     * Checks if this data is dirty. Meaning it needs to be saved/synced.
     * @param reset If true the dirty status will be reset to false (Will not affect the returned value).
     * @return true if this data is dirty.
     */
    boolean isDirty(boolean reset);

    void init(IDataManager dataManager, int index);

    /**
     * @return the data manager this is registered to.
     */
    IDataManager getDataManager();

    /**
     * @return this data's flags object.
     */
    DataFlags flags();

    /**
     * When called this data object should validate its current value and ensure it is within any restrictions that may have been set.
     * This is called whenever the data value is set and more importantly when the data value is set by the client if {@link DataFlags#CLIENT_CONTROL} is enabled.
     */
    void validate();
}
