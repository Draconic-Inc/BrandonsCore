package com.brandon3055.brandonscore.network.wrappers;

import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 26/3/2016.
 * The base class for syncable objects
 */
public abstract class SyncableObject {
    public final boolean syncInTile;
    public final boolean syncInContainer;
    public boolean shouldSaveToNBT = false;
    public boolean shouldSaveToItem = false;
    public boolean updateOnReceived;
    protected byte index = -1;

    public SyncableObject(boolean syncInTile, boolean syncInContainer) {
        this(syncInTile, syncInContainer, false);
    }

    public SyncableObject(boolean syncInTile, boolean syncInContainer, boolean updateOnReceived) {
        this.syncInTile = syncInTile;
        this.syncInContainer = syncInContainer;
        this.updateOnReceived = updateOnReceived;
    }

    public abstract void detectAndSendChanges(TileBCBase tile, EntityPlayer player, boolean forceSync);

    public abstract void updateReceived(PacketSyncableObject packet);

    public SyncableObject setIndex(int index) {
        this.index = (byte) index;
        return this;
    }

    public abstract void toNBT(NBTTagCompound compound);

    public abstract void fromNBT(NBTTagCompound compound);

    public SyncableObject setSaveMode(boolean nbt, boolean item) {
        shouldSaveToNBT = nbt;
        shouldSaveToItem = item;
        return this;
    }
}
