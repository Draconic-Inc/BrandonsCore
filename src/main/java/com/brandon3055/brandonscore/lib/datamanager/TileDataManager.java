package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 12/06/2017.
 * My implementation if IDataManager for tile {@link TileBCBase}
 */
public class TileDataManager<T extends TileEntity & IDataManagerProvider> implements IDataManager {

    protected LinkedList<IManagedData> managedDataList = new LinkedList<>();
    protected Map<IManagedData, TileDataOptions> dataOptions = new HashMap<>();
    public final T tile;

    public TileDataManager(T tile) {
        this.tile = tile;
    }

    /**
     * Use this to create, Configure and register your Managed Data Objects<br>
     * Example Registration:<br><br>
     * <p>
     * public ManagedInt anInt = register("anInt", new ManagedInt(0)).saveToTile().saveToItem().syncViaTile().finish();
     *
     * @param name        The name to register the managed data as. This will be sued to sage the data to NBT.
     * @param managedData A new instance of this managed data type.
     * @return Returns a generified data options class. Set the flags you need then call finish to get your shiny new ManagedData object!
     */
    public <M extends IManagedData> TileDataOptions<M> register(String name, M managedData) {
        managedData.setName(name);
        managedData.setIndex(managedDataList.size());
        managedDataList.add(managedData);
        TileDataOptions<M> ops = new TileDataOptions<>(managedData);
        dataOptions.put(managedData, ops);
        return ops;
    }
//    public ManagedInt anInt = register("anInt", new ManagedInt(0)).saveToTile().saveToItem().syncViaTile().finish();

    /**
     * You can store IManagedData instances however you want in your manager implementation
     * As long as you are able to retrieve them as a collection and supply them via this method.
     *
     * @return All data objects stored in the manager as a collection.
     */
    @Override
    public void detectAndSendChanges() {
        if (tile.getWorld().isRemote) {
            return;
        }
        for (IManagedData data : managedDataList) {
            if (dataOptions.get(data).syncViaTile && data.detectChanges()) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToChunk(tile);
            }
        }
    }

    /**
     * This method is called each tick by {@link com.brandon3055.brandonscore.inventory.ContainerBCBase} to sent updates to container listeners.
     *
     * @param listeners The list of container listeners.
     */
    public void detectAndSendChangesToListeners(List<IContainerListener> listeners) {
        if (tile.getWorld().isRemote) {
            return;
        }
        for (IManagedData data : managedDataList) {
            if (dataOptions.get(data).syncViaContainer && data.detectChanges()) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToChunk(tile);
                DataUtils.forEachMatch(listeners, p -> p instanceof EntityPlayerMP, p -> syncPacket.sendToPlayer((EntityPlayerMP) p));
            }
        }
    }

    /**
     * You may want to call this when the player opens a container is you have data that is only synced by the container and does not update often.
     * This may be required because normally data only syncs when it changes so if your container data isnt constantly changing the client
     * will see incorrect values until the next sync.
     */
    public void forceContainerSync(List<IContainerListener> listeners) {
        if (tile.getWorld().isRemote) {
            return;
        }
        for (IManagedData data : managedDataList) {
            if (dataOptions.get(data).syncViaContainer) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                DataUtils.forEachMatch(listeners, p -> p instanceof EntityPlayerMP, p -> syncPacket.sendToPlayer((EntityPlayerMP) p));
            }
        }
    }

    public void forceSync() {
        if (tile.getWorld().isRemote) {
            return;
        }
        for (IManagedData data : managedDataList) {
            if (dataOptions.get(data).syncViaTile) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToChunk(tile);
            }
        }
    }

    public void forcePlayerSync(EntityPlayerMP player) {
        if (tile.getWorld().isRemote) {
            return;
        }
        for (IManagedData data : managedDataList) {
            if (dataOptions.get(data).syncViaContainer) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToPlayer(player);
            }
        }
    }

    public void forceSync(IManagedData data) {
        if (tile.getWorld().isRemote) {
            return;
        }
        PacketCustom syncPacket = createSyncPacket();
        syncPacket.writeByte((byte) data.getIndex());
        data.toBytes(syncPacket);
        syncPacket.sendToChunk(tile);
    }

    @Override
    public PacketCustom createSyncPacket() {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, PacketDispatcher.C_TILE_DATA_MANAGER);
        packet.writePos(tile.getPos());
        return packet;
    }

    @Override
    public void receiveSyncData(MCDataInput input) {
        int index = input.readByte() & 0xFF;
        IManagedData data = getDataByIndex(index);
        if (data != null) {
            data.fromBytes(input);
            if (dataOptions.get(data).triggerUpdate) {
                IBlockState state = tile.getWorld().getBlockState(tile.getPos());
                tile.getWorld().notifyBlockUpdate(tile.getPos(), state, state, 3);
            }
        }
    }

    @Override
    public IManagedData getDataByName(String name) {
        return DataUtils.firstMatch(managedDataList, data -> data.getName().equals(name));
    }

    @Override
    public IManagedData getDataByIndex(int index) {
        return DataUtils.firstMatch(managedDataList, data -> data.getIndex() == index);
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagCompound dataTag = new NBTTagCompound();
        DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).saveToNBT, p -> p.toNBT(dataTag));
        compound.setTag("BCManagedData", dataTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("BCManagedData", 10)) {
            NBTTagCompound dataTag = compound.getCompoundTag("BCManagedData");
            DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).saveToNBT, p -> p.fromNBT(dataTag));
        }
    }

    /**
     * Used to sync data via getUpdatePacket and getUpdateTag in TileEntity
     */
    public void writeSyncNBT(NBTTagCompound compound) {
        NBTTagCompound dataTag = new NBTTagCompound();
        DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).syncViaTile, p -> p.toNBT(dataTag));
        compound.setTag("BCManagedData", dataTag);
    }

    public void readSyncNBT(NBTTagCompound compound) {
        if (compound.hasKey("BCManagedData", 10)) {
            NBTTagCompound dataTag = compound.getCompoundTag("BCManagedData");
            DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).syncViaTile, p -> p.fromNBT(dataTag));
        }
    }

    /**
     * Used to save data to the itemstack when the tile is broken.
     */
    public void writeToStackNBT(NBTTagCompound compound) {
        NBTTagCompound dataTag = new NBTTagCompound();
        DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).saveToItem, p -> p.toNBT(dataTag));
        compound.setTag("BCManagedData", dataTag);
    }

    public void readFromStackNBT(NBTTagCompound compound) {
        if (compound.hasKey("BCManagedData", 10)) {
            NBTTagCompound dataTag = compound.getCompoundTag("BCManagedData");
            DataUtils.forEachMatch(managedDataList, p -> dataOptions.get(p).saveToItem, p -> p.fromNBT(dataTag));
        }
    }
}
