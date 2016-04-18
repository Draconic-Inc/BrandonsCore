package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.network.PacketTileMessage;
import com.brandon3055.brandonscore.network.wrappers.SyncableObject;
import com.brandon3055.brandonscore.utills.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 26/3/2016.
 * Base tile entity class for all tile entities
 */
public class TileBCBase extends TileEntity {

    private Map<Byte, SyncableObject> syncableObjectMap = new HashMap<Byte, SyncableObject>();
    private int objIndexCount = 0;
    private int viewRange = -1;
    private boolean shouldRefreshOnState = true;

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

    public void registerSyncableObject(SyncableObject object, boolean saveToNBT) {
        if (objIndexCount > Byte.MAX_VALUE) {
            throw new RuntimeException("TileBCBase#registerSyncableObject To many objects registered!");
        }
        syncableObjectMap.put((byte) objIndexCount, object.setIndex(objIndexCount));
        if (saveToNBT) {
            object.setSaved();
        }
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
            Field f = ReflectionHelper.findField(PlayerManager.class, "playerViewRadius", "field_72698_e");
            f.setAccessible(true);
            try {
                viewRange = f.getInt(((WorldServer) worldObj).getPlayerChunkManager());
            }
            catch (IllegalAccessException e) {
                LogHelper.error("A THING BROKE!!!!!!!");
                e.printStackTrace();
            }
        } else if (worldObj.isRemote) {
            LogHelper.error("Hay! Someone is doing a bad thing!!! Check your side!!!!!!!");
        }
        return new NetworkRegistry.TargetPoint(worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), viewRange * 16);
    }

    @Override
    public Packet<?> getDescriptionPacket() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).writeDataToNBT(nbttagcompound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            syncableObject.toNBT(nbttagcompound);
        }

        return new SPacketUpdateTileEntity(this.pos, 0, nbttagcompound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).readDataFromNBT(pkt.getNbtCompound());
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            syncableObject.fromNBT(pkt.getNbtCompound());
        }
    }

    //endregion

    //region Packets

    /**
     * Send a message to the server side tile
     */
    public void sendPacketToServer(PacketTileMessage packet) {
        BrandonsCore.network.sendToServer(packet);
    }

    /**
     * Receive a message from the client side tile. Override this to receive messages.
     */
    public void receivePacketFromClient(PacketTileMessage packet, EntityPlayerMP client) {

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

    public static <T> T getCastTileAt(IBlockAccess world, BlockPos posAt, Class<T> clazz) {
        TileEntity tile = world.getTileEntity(posAt);
        return (tile != null && clazz.isAssignableFrom(tile.getClass())) ? clazz.cast(tile) : null;
    }

    /**
     * Calling this in the constructor will force the tile to only refresh when the block changes rather then when the state changes.
     * Note that this should NOT be used in cases where the block has a different tile depending on its state.
     * */
    public void setShouldRefreshOnBlockChange(){
        shouldRefreshOnState = false;
    }
    //endregion

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).writeDataToNBT(compound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSave) {
                syncableObject.toNBT(compound);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (this instanceof IDataRetainerTile) {
            ((IDataRetainerTile) this).readDataFromNBT(compound);
        }

        for (SyncableObject syncableObject : syncableObjectMap.values()) {
            if (syncableObject.shouldSave) {
                syncableObject.fromNBT(compound);
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return shouldRefreshOnState ? oldState != newSate : (oldState.getBlock() != newSate.getBlock());
    }
}
