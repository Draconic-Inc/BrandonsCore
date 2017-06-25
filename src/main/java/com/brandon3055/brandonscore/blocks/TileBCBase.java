package com.brandon3055.brandonscore.blocks;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IDataRetainingTile;
import com.brandon3055.brandonscore.lib.datamanager.IDataManagerProvider;
import com.brandon3055.brandonscore.lib.datamanager.IManagedData;
import com.brandon3055.brandonscore.lib.datamanager.TileDataManager;
import com.brandon3055.brandonscore.lib.datamanager.TileDataOptions;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 26/3/2016.
 * Base tile entity class for all tile entities
 */
public class TileBCBase extends TileEntity implements IDataManagerProvider, IDataRetainingTile {

    public static final String TILE_DATA_TAG = "BCTileData";
    protected boolean shouldRefreshOnState = true;
    protected TileDataManager<TileBCBase> dataManager = new TileDataManager<>(this);

    //region Data Manager

    @Override
    public TileDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Convenience method for dataManager.register();
     */
    public <M extends IManagedData> TileDataOptions<M> register(String name, M managedData) {
        return dataManager.register(name, managedData);
    }

    /**
     * super.update() must be called from your update method in order for Data Manager synchronization to work..
     */
    public void update() {
        if (!world.isRemote) {
            dataManager.detectAndSendChanges();
        }
    }

    //vanilla

    //Used to sync data to the client
    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        dataManager.writeSyncNBT(compound);
        writeExtraNBT(compound);
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    //Used when initially sending chunks to the client... I think
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = super.getUpdateTag();
        dataManager.writeSyncNBT(compound);
        writeExtraNBT(compound);
        return compound;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        dataManager.readSyncNBT(pkt.getNbtCompound());
        readExtraNBT(pkt.getNbtCompound());
    }

    //    //endregion

    //region Packets

    /**
     * Send a data packet to the server, Supply a consumer to write the data you want to send
     */
    public void sendPacketToServer(Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, PacketDispatcher.S_TILE_MESSAGE);
        packet.writePos(pos);
        packet.writeByte((byte) id);
        writer.accept(packet);
        packet.sendToServer();
    }

    /**
     * Override this method to receive data from the server via sendPacketToServer
     */
    public void receivePacketFromClient(MCDataInput data, EntityPlayerMP client, int id) {}


    /**
     * Create a packet to send to the client
     */
    public PacketCustom sendPacketToClient(Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, PacketDispatcher.C_TILE_MESSAGE);
        packet.writePos(pos);
        packet.writeByte((byte) id);
        writer.accept(packet);
        return packet;
    }

    public void sendPacketToClient(EntityPlayerMP player, Consumer<MCDataOutput> writer, int id) {
        sendPacketToClient(writer, id).sendToPlayer(player);
    }

    public void sendPacketToClient(NetworkRegistry.TargetPoint tp, Consumer<MCDataOutput> writer, int id) {
        sendPacketToClient(writer, id).sendPacketToAllAround(tp.x, tp.y, tp.z, tp.range, tp.dimension);
    }

    /**
     * Override this method to receive data from the client via sendPacketToClient
     */
    public void receivePacketFromServer(MCDataInput data, int id) {}

    //endregion

    //region Helper Functions.

    public void updateBlock() {
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    public void dirtyBlock() {
        Chunk chunk = world.getChunkFromBlockCoords(getPos());
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
        IBlockState state = world.getBlockState(pos);
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

    /**
     * checks that the player is allowed to interact with this tile bu firing the RightClickBlock.
     * If the event is canceled bu another mod such as a permissions mod this will return false.
     */
    @Deprecated //Just a reminder that i nolonger need to do this in the tile because its handled via the packet handler.
    public boolean verifyPlayerPermission(EntityPlayer player) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, EnumHand.MAIN_HAND, pos, EnumFacing.UP, player.getLookVec());
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    //endregion

    //region Save/Load

    /**
     * These methods replace the methods from IDataRetainerTile but they are
     * now ONLY used to read and write data to and from an itemstack<br>
     * Note: if you wish to add additional data to the item then override this, Call super to get a data tag,
     * Write your data to said tag and finally return said tag.
     *
     * @return the tile data compound so that tiles that override this method can easily write extra data to it.
     */
    @Override
    public NBTTagCompound writeToItemStack(ItemStack stack, boolean willHarvest) {
        NBTTagCompound dataTag = new NBTTagCompound();
        dataManager.writeToStackNBT(dataTag);
        ItemNBTHelper.getCompound(stack).setTag(TILE_DATA_TAG, dataTag);
        return dataTag;
    }

    /**
     * @return the tile data compound so that tiles that override this method can easily read extra data from it.
     */
    @Nullable
    @Override
    public NBTTagCompound readFromItemStack(ItemStack stack) {
        NBTTagCompound dataTag = stack.getOrCreateSubCompound(TILE_DATA_TAG);
        dataManager.readFromStackNBT(dataTag);
        return dataTag;
    }

    /**
     * Write any extra data that needs to be saved to NBT that is not saved via a syncable field.
     * This data is also synced to the client via getUpdateTag and getUpdatePacket.
     * Note: This will not save data to the item when the block is harvested.<br>
     * For that you need to override read and writeToStack just be sure to pay attention to the doc for those.
     * */
    public void writeExtraNBT(NBTTagCompound compound){}

    public void readExtraNBT(NBTTagCompound compound){}

    @Override
    public final NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        dataManager.writeToNBT(compound);
        writeExtraNBT(compound);

        return compound;
    }

    @Override
    public final void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        dataManager.readFromNBT(compound);
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
