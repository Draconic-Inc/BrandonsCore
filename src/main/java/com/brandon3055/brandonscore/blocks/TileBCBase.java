package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IDataRetainerTile;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.network.PacketTileMessage;
import com.brandon3055.brandonscore.network.wrappers.SyncableObject;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 26/3/2016.
 * Base tile entity class for all tile entities
 */
public class TileBCBase extends TileEntity {

    protected Map<Byte, SyncableObject> syncableObjectMap = new HashMap<Byte, SyncableObject>();
    protected int objIndexCount = 0;
    protected int viewRange = -1;
    protected boolean shouldRefreshOnState = true;

    //region Sync
    public void detectAndSendChanges() {
        detectAndSendChanges(false);
    }

    public void detectAndSendChanges(boolean forceSync) {
        if (worldObj.isRemote) return;
        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.syncInTile) {
                syncableObject.detectAndSendChanges(this, null, forceSync);
            }
        }
    }

    public void detectAndSendChangesToPlayer(boolean forceSync, EntityPlayerMP playerMP) {
        if (worldObj.isRemote) return;
        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.syncInContainer) {
                syncableObject.detectAndSendChanges(this, playerMP, forceSync);
            }
        }
    }

    public void registerSyncableObject(SyncableObject object) {
        registerSyncableObject(object, true);
    }

    public void registerSyncableObject(SyncableObject object, boolean saveToNBT) {
        registerSyncableObject(object, saveToNBT, false);
    }

    /**
     * Registers a syncable object. These objects will automatically be synchronized with the client.
     * Note: you are required to call detectAndSendChanges server side in order for objects to detect and send changes to the client.
     * @param object The object.
     * @param saveToNBT If true the object will ba saved and loaded from NBT.
     * @param saveToItem If true and the tile is an instance of {@link IDataRetainerTile} the object will be saved and loaded from the item when the tile is broken.
     */
    public void registerSyncableObject(SyncableObject object, boolean saveToNBT, boolean saveToItem) {
        if (objIndexCount > Byte.MAX_VALUE) {
            throw new RuntimeException("TileBCBase#registerSyncableObject To many objects registered!");
        }
        syncableObjectMap.put((byte) objIndexCount, object.setIndex(objIndexCount));
        object.setSaveMode(saveToNBT, saveToItem);

        objIndexCount++;
    }

    public void receiveSyncPacketFromServer(PacketSyncableObject packet) {
        if (syncableObjectMap.containsKey(packet.index)) {
            SyncableObject object = syncableObjectMap.get(packet.index);
            object.updateReceived(packet);

            if (object.updateOnReceived) {
                updateBlock();
            }
        }
    }

    public NetworkRegistry.TargetPoint syncRange() {
        if (viewRange == -1 && !worldObj.isRemote) {
            Field f = ReflectionHelper.findField(PlayerChunkMap.class, "playerViewRadius", "field_72698_e");
            f.setAccessible(true);
            try {
                viewRange = f.getInt(((WorldServer) worldObj).getPlayerChunkMap());
            }
            catch (IllegalAccessException e) {
                LogHelperBC.error("A THING BROKE!!!!!!!");
                e.printStackTrace();
            }
        } else if (worldObj.isRemote) {
            LogHelperBC.error("Hay! Someone is doing a bad thing!!! Check your side!!!!!!!");
        }
        return new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), viewRange * 16);
    }

    //Used to sync data to the client
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).writeRetainedData(nbttagcompound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            syncableObject.toNBT(nbttagcompound);
        }

        writeExtraNBT(nbttagcompound);

        return new SPacketUpdateTileEntity(this.pos, 0, nbttagcompound);
    }

    //Used when initially sending chunks to the client... I think
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();

        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).writeRetainedData(compound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSaveToNBT) {
                syncableObject.toNBT(compound);
            }
        }

        writeExtraNBT(compound);
        return compound;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);//todo?
        readExtraNBT(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).readRetainedData(pkt.getNbtCompound());
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            syncableObject.fromNBT(pkt.getNbtCompound());
        }

        readExtraNBT(pkt.getNbtCompound());
    }

    //endregion

    //region Packets

    /**
     * Send a message to the server side tile
     */
    public void sendPacketToServer(PacketTileMessage packet) {
        BrandonsCore.network.sendToServer(packet);
    }

    public void sendPacketToClients(PacketTileMessage packet, NetworkRegistry.TargetPoint targetPoint) {
        BrandonsCore.network.sendToAllAround(packet, targetPoint);
    }

    /**
     * Receive a message from the client side tile. Override this to receive messages.
     */
    public void receivePacketFromClient(PacketTileMessage packet, EntityPlayerMP client) {

    }

    public void receivePacketFromServer(PacketTileMessage packet) {

    }

    //endregion

    //region Helper Functions.

    public void updateBlock() {
        IBlockState state = worldObj.getBlockState(getPos());
        worldObj.notifyBlockUpdate(getPos(), state, state, 3);
    }

    public void dirtyBlock() {
        Chunk chunk = worldObj.getChunkFromBlockCoords(getPos());
        chunk.setChunkModified();
    }

    /**
     * Calling this in the constructor will force the tile to only refresh when the block changes rather then when the state changes.
     * Note that this should NOT be used in cases where the block has a different tile depending on its state.
     */
    public void setShouldRefreshOnBlockChange() {
        shouldRefreshOnState = false;
    }

    public IBlockState getState(Block expectedBlock) {
        IBlockState state = worldObj.getBlockState(pos);
        return state.getBlock() == expectedBlock ? state : expectedBlock.getDefaultState();
    }


    /**
     * Is minecraft seriously so screwed up that i have to resort to things like this?
     */
    public Block getBlockTypeSafe(Block defaultBlock)
    {
        if (getBlockType() != null) {
            return getBlockType();
        }
        else {
            return defaultBlock;
        }
    }

    //endregion

    //region Save/Load

    /**
     * Part of the new method of saving Syncable Objects to item.
     * When implementing IDataRetainerTile
     */
    public void writeRetainedData(NBTTagCompound dataCompound) {
        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSaveToItem) {
                syncableObject.toNBT(dataCompound);
            }
        }
    }

    public void readRetainedData(NBTTagCompound dataCompound) {
        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSaveToItem) {
                syncableObject.fromNBT(dataCompound);
            }
        }
    }

    /**
     * Write any extra data that needs to be saved to NBT that is not saved via a syncable field.
     * This data is also synced to the client!.
     * */
    public void writeExtraNBT(NBTTagCompound compound){}

    public void readExtraNBT(NBTTagCompound compound){}

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).writeRetainedData(compound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSaveToNBT && (!syncableObject.shouldSaveToItem || !(this instanceof IDataRetainerTile))) {
                syncableObject.toNBT(compound);
            }
        }

        writeExtraNBT(compound);

        return compound;
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).readRetainedData(compound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSaveToNBT) {
                syncableObject.fromNBT(compound);
            }
        }

        readExtraNBT(compound);

        onTileLoaded();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return shouldRefreshOnState ? oldState != newSate : (oldState.getBlock() != newSate.getBlock());
    }

    /**
     * Called immediately after all NBT is loaded. World may be null at this point
     */
    public void onTileLoaded() {}

    //endregion
}
