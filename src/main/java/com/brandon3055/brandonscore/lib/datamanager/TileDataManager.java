package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.blocks.BlockBCore;
import com.brandon3055.brandonscore.inventory.ContainerBCTile;
import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 12/06/2017.
 * My implementation if IDataManager for tile {@link com.brandon3055.brandonscore.blocks.TileBCore}
 */
@SuppressWarnings("DuplicatedCode")
public class TileDataManager<T extends BlockEntity & IDataManagerProvider> implements IDataManager {

    protected LinkedList<IManagedData> managedDataList = new LinkedList<>();
    public final T tile;
    private int lastDirty = -9999;
    private int maxSaveInterval = 0;

    public TileDataManager(T tile) {
        this.tile = tile;
    }

    public void setMaxSaveInterval(int maxSaveInterval) {
        this.maxSaveInterval = maxSaveInterval;
    }

    /**
     * Use this to create, Configure and register your Managed Data Objects<br>
     * Example Registration:<br><br>
     * <p>
     * public ManagedInt anInt = register(new ManagedInt("anInt", 0, {@link DataFlags#SAVE_BOTH_SYNC_TILE}));
     *
     * @param managedData A new instance of this managed data type.
     * @return Returns a generified data options class. Set the flags you need then call finish to get your shiny new ManagedData object!
     */
    public <M extends IManagedData> M register(M managedData) {
        managedData.init(this, managedDataList.size());
        managedDataList.add(managedData);
        return managedData;
    }

    /**
     * Use this to detect and send changes to the client via your own sync packet. See {@link TileDataManager} for an example
     * This should be called by your tile, container, etc every tick.
     */
    @Override
    public void detectAndSendChanges() {
        for (IManagedData data : managedDataList) {
            if (data.flags().syncTile && data.isDirty(true)) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                syncPacket.sendToChunk(tile);
            }
        }
    }

    /**
     * This method is called each tick by {@link ContainerBCore} to sent updates to container listeners.
     *
     * @param listeners The list of container listeners.
     */
    public void detectAndSendChangesToListeners(Collection<Player> listeners) {
        for (IManagedData data : managedDataList) {
            if (data.flags().syncContainer && data.isDirty(true)) {
                PacketCustom syncPacket = createSyncPacket();
                syncPacket.writeByte((byte) data.getIndex());
                data.toBytes(syncPacket);
                DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayer, p -> syncPacket.sendToPlayer((ServerPlayer) p));
            }
        }
    }

    /**
     * You may want to call this when the player opens a container is you have data that is only synced by the container and does not update often.
     * This may be required because normally data only syncs when it changes so if your container data isnt constantly changing the client
     * will see incorrect values until the next sync.
     */
    public void forceContainerSync(List<ContainerListener> listeners) {
        if (!tile.getLevel().isClientSide) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncContainer) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayer, p -> syncPacket.sendToPlayer((ServerPlayer) p));
                }
            }
        }
    }

    public void forceSync() {
        if (!tile.getLevel().isClientSide) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncTile) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToChunk(tile);
                }
            }
        }
    }

    public void forcePlayerSync(ServerPlayer player) {
        if (!tile.getLevel().isClientSide) {
            for (IManagedData data : managedDataList) {
                if (data.flags().syncContainer) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToPlayer(player);
                }
            }
        }
    }

    public void forceSync(IManagedData data) {
        if (!tile.getLevel().isClientSide) {
            PacketCustom syncPacket = createSyncPacket();
            syncPacket.writeByte((byte) data.getIndex());
            data.toBytes(syncPacket);
            syncPacket.sendToChunk(tile);
        }
    }

    @Override
    public PacketCustom createSyncPacket() {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_DATA_MANAGER);
        packet.writePos(tile.getBlockPos());
        return packet;
    }

    @Override
    public void receiveSyncData(MCDataInput input) {
        int index = input.readByte() & 0xFF;
        IManagedData data = getDataByIndex(index);
        if (data != null) {
            data.fromBytes(input);
            if (data.flags().triggerUpdate) {
                BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
                tile.getLevel().sendBlockUpdated(tile.getBlockPos(), state, state, 3);
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
    public void writeToNBT(CompoundTag compound) {
        CompoundTag dataTag = new CompoundTag();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().syncViaPacket(), data -> data.toNBT(dataTag));
        compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundTag dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().syncViaPacket(), data -> data.fromNBT(dataTag));
        }
    }

    @Override
    public void markDirty() {
        if (tile.getLevel() != null && !tile.getLevel().isClientSide) {
            maxSaveInterval = 10;
            if (maxSaveInterval == 0){
                tile.setChanged();
            } else if (TimeKeeper.getServerTick() > lastDirty + maxSaveInterval) {
                lastDirty = TimeKeeper.getServerTick();
                tile.setChanged();
            }

            for (IManagedData data : managedDataList) {
                if (data.flags().syncOnSet && data.isDirty(true)) {
                    PacketCustom syncPacket = createSyncPacket();
                    syncPacket.writeByte((byte) data.getIndex());
                    data.toBytes(syncPacket);
                    syncPacket.sendToChunk(tile);
                }
            }
        }
    }

    @Override
    public boolean isClientSide() {
        return tile.hasLevel() && tile.getLevel().isClientSide;
    }

    @Override
    public void sendToServer(IManagedData data) {
        if (tile.getLevel().isClientSide && data.flags().allowClientControl) {
            Player player = BrandonsCore.proxy.getClientPlayer();
            if (player != null) {
                AbstractContainerMenu container = player.containerMenu;
                if (container instanceof ContainerBCTile<?> && ((ContainerBCTile<?>) container).tile == tile) {
                    PacketCustom packet = ((ContainerBCTile<?>) container).createServerBoundPacket(BCoreNetwork.S_TILE_DATA_MANAGER);
                    packet.writeByte((byte) data.getIndex());
                    data.toBytes(packet);
                    packet.sendToServer();
                }
            }
        }
    }

    public void receiveDataFromClient(MCDataInput input) {
        int index = input.readByte() & 0xFF;
        IManagedData data = getDataByIndex(index);
        if (data != null && data.flags().allowClientControl) {
            data.fromBytes(input);
            data.validate(); //This is very important! Assuming this data has a validator this prevents a malicious or bugged client from sending an invalid value.
            data.markDirty();
        }
    }

    /**
     * Used to sync data via getUpdatePacket and getUpdateTag in TileEntity
     */
    public void writeSyncNBT(CompoundTag compound) {
        CompoundTag dataTag = new CompoundTag();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().syncViaPacket(), data -> data.toNBT(dataTag));
        compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
    }

    public void readSyncNBT(CompoundTag compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundTag dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().syncViaPacket(), data -> data.fromNBT(dataTag));
        }
    }

    /**
     * Used to save data to the itemstack when the tile is broken.
     */
    public void writeToStackNBT(CompoundTag compound) {
        CompoundTag dataTag = new CompoundTag();
        DataUtils.forEachMatch(managedDataList, data -> data.flags().saveItem, data -> data.toNBT(dataTag));
        if (!dataTag.isEmpty()) {
            compound.put(BlockBCore.BC_MANAGED_DATA_FLAG, dataTag);
        }
    }

    public void readFromStackNBT(CompoundTag compound) {
        if (compound.contains(BlockBCore.BC_MANAGED_DATA_FLAG, 10)) {
            CompoundTag dataTag = compound.getCompound(BlockBCore.BC_MANAGED_DATA_FLAG);
            DataUtils.forEachMatch(managedDataList, data -> data.flags().saveItem, data -> data.fromNBT(dataTag));
        }
    }
}
