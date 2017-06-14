package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 12/06/2017.
 *
 * The idea behind managed data objects is to make saving, loading and synchronizing data as simple as possible.
 * For example you can create a ManagedInt in a tile that implements {@link IDataManager} then simply register that int and... that is.
 * The int value stored in that int object will be saved, loaded and synchronized automatically.
 *
 * Note: depending on the the manager implementation you can create an object that only saves or only syncs you dont have to do both.
 */
public interface IManagedData {

    /**
     * Set the unique name for this object.
     * Note this name only has to be unique within the DataManager its registered to.
     *
     * @param name the new unique id.
     */
    void setName(String name);

    /**
     * @return A unique (within the manager its registered to) name for the purpose of identifying and saving this data.
     */
    String getName();

    /**
     * This index is used for sending identifying this data when it is sent over the network.
     * This index should be set by the manager and must be the same both server and client side.
     * How you assign this index is up to you but a safe bet would be to just start at 0 ind increment
     * every time a new data object is registered.
     */
    void setIndex(int index);

    /**
     * @return the index of this managed data.
     */
    int getIndex();


    /**
     * When this is called compare the stored data to whatever data cache you use to check for changes.
     * If the data has changed return true and reset the cache so that the next time this is called it will return false
     * Unless the data has changed again of course.
     *
     * @return true if the data has changed since the last time detectChanges was called.
     */
    boolean detectChanges();

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
    void toNBT(NBTTagCompound compound);

    /**
     * Load this data from nbt.
     */
    void fromNBT(NBTTagCompound compound);
}
