package com.brandon3055.brandonscore.blocks;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.api.IDataRetainingTile;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.api.power.IOTracker;
import com.brandon3055.brandonscore.api.power.OPStorage;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.inventory.TileItemStackHandler;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.lib.IRSSwitchable.RSMode;
import com.brandon3055.brandonscore.lib.IValueHashable;
import com.brandon3055.brandonscore.lib.datamanager.*;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import com.brandon3055.brandonscore.utils.DataUtils;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.*;

/**
 * Created by brandon3055 on 26/3/2016.
 * Base tile entity class for all tile entities
 */
public class TileBCore extends TileEntity implements IDataManagerProvider, IDataRetainingTile, IWorldNameable {

    protected boolean shouldRefreshOnState = true;
    protected TileDataManager<TileBCore> dataManager = new TileDataManager<>(this);
    private HashMap<EnumFacing, IEnergyStorage> energyCaps = new HashMap<>();
    private HashMap<EnumFacing, IOPStorage> opEnergyCaps = new HashMap<>();
    private HashMap<EnumFacing, IItemHandler> invCaps = new HashMap<>();
    private HashMap<Object, Predicate<EnumFacing>> capSideValidator = new HashMap<>();
    private HashMap<INBTSerializable<NBTTagCompound>, DataHelper> serializableMap = new HashMap<>();
    private List<DataHelper> indexedDataList = new ArrayList<>();
    private List<Runnable> tickables = new ArrayList<>();
    private ManagedEnum<RSMode> rsControlMode = this instanceof IRSSwitchable ? register(new ManagedEnum<>("rs_mode", RSMode.ALWAYS_ACTIVE, SAVE_BOTH_SYNC_TILE, CLIENT_CONTROL)) : null;
    private ManagedBool rsPowered = this instanceof IRSSwitchable ? register(new ManagedBool("rs_powered", false, SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE)) : null;
    private String customName = "";

    //region Data Manager

    @Override
    public TileDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Convenience method for dataManager.register();
     */
    public <M extends IManagedData> M register(M managedData) {
        return dataManager.register(managedData);
    }

    /**
     * super.update() must be called from your update method in order for Data Manager synchronization to work..
     */
    public void update() {
        tickables.forEach(Runnable::run);
        detectAndSendChanges();
    }

    public void detectAndSendChanges() {
        if (!world.isRemote) {
            dataManager.detectAndSendChanges();
            for (int i = 0; i < indexedDataList.size(); i++) {
                DataHelper helper = indexedDataList.get(i);
                if (helper.syncTile && helper.hasChanged(true)) {
                    PacketCustom packet = createCapPacket(helper, i);
                    packet.sendToChunk(this);
                }
            }
        }
    }

    public void detectAndSendChangesToListeners(List<IContainerListener> listeners) {
        if (!world.isRemote) {
            dataManager.detectAndSendChangesToListeners(listeners);
            for (int i = 0; i < indexedDataList.size(); i++) {
                DataHelper helper = indexedDataList.get(i);
                if (helper.syncContainer && helper.hasChanged(true)) {
                    PacketCustom packet = createCapPacket(helper, i);
                    DataUtils.forEachMatch(listeners, p -> p instanceof EntityPlayerMP, p -> packet.sendToPlayer((EntityPlayerMP) p));
                }
            }
        }
    }

    private PacketCustom createCapPacket(DataHelper helper, int index) {
        PacketCustom packet = new PacketCustom(BrandonsCore.NET_CHANNEL, PacketDispatcher.C_TILE_CAP_DATA);
        packet.writePos(pos);
        packet.writeInt(index);
        if (helper.getData() instanceof IMCDataSerializable) {
            ((IMCDataSerializable) helper.getData()).serializeMCD(packet);
        }
        else {
            packet.writeNBTTagCompound((NBTTagCompound) helper.getData().serializeNBT());
        }
        return packet;
    }

    @SuppressWarnings("unchecked")
    public void receiveCapSyncData(MCDataInput input) {
        int index = input.readInt();
        if (index >= 0 && index < indexedDataList.size()) {
            DataHelper helper = indexedDataList.get(index);
            if (helper.getData() instanceof IMCDataSerializable) {
                ((IMCDataSerializable) helper.getData()).deSerializeMCD(input);
            }
            else {
                helper.getData().deserializeNBT(input.readNBTTagCompound());
            }
        }
    }

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

    //endregion

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
    public void receivePacketFromClient(MCDataInput data, EntityPlayerMP client, int id) {
    }


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
    public void receivePacketFromServer(MCDataInput data, int id) {
    }

    //endregion

    //region Helper Functions.

    public void updateBlock() {
        IBlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    public void dirtyBlock() {
        Chunk chunk = world.getChunkFromBlockCoords(getPos());
        chunk.setModified(true);
    }

    /**
     * Calling this in the constructor will force the tile to only refresh when the block changes rather then when the state changes.
     * Note that this should NOT be used in cases where the block has a different tile depending on its state.
     */
    public void setShouldRefreshOnBlockChange() {
        shouldRefreshOnState = false;
    }

    @Deprecated //I want to store everything on the tile in 1.13. I'm done dealing with these bullshit blockstate crashes.
    public IBlockState getState(Block expectedBlock) {
        if (world == null) {
            return expectedBlock.getDefaultState();//Because apparently this is a thing........
        }
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == expectedBlock ? state : expectedBlock.getDefaultState();
    }

    /**
     * Is minecraft seriously so screwed up that i have to resort to things like this?
     */
    public Block getBlockTypeSafe(Block defaultBlock) {
        if (getBlockType() != Blocks.AIR) {
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
    @Deprecated
    //Just a reminder that i nolonger need to do this in the tile because its handled via the packet handler.
    public boolean verifyPlayerPermission(EntityPlayer player) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, EnumHand.MAIN_HAND, pos, EnumFacing.UP, player.getLookVec());
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    /**
     * Adds an item to the 'tickables' list. Every item in this list will be called every tick via the tiles update method.
     * Note: in order for this to work the tile must implement ITickable and call super in {@link ITickable#update()}
     *
     * @param runnable The runnable to add
     */
    public <T extends Runnable> T addTickable(T runnable) {
        tickables.add(runnable);
        return runnable;
    }

    public boolean removeTickable(Runnable runnable) {
        return tickables.remove(runnable);
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
    public void writeToItemStack(NBTTagCompound compound, boolean willHarvest) {
        dataManager.writeToStackNBT(compound);
        NBTTagCompound capTags = serializeCapabilities(true);
        if (!capTags.hasNoTags()) {
            compound.setTag("bc_caps", capTags);
        }
    }

    /**
     * @return the tile data compound so that tiles that override this method can easily read extra data from it.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void readFromItemStack(NBTTagCompound compound) {
        dataManager.readFromStackNBT(compound);
        if (compound.hasKey("bc_caps")) {
            deserializeCapabilities(compound.getCompoundTag("bc_caps"));
        }
    }

    /**
     * Write any extra data that needs to be saved to NBT that is not saved via a syncable field.
     * This data is also synced to the client via getUpdateTag and getUpdatePacket.
     * Note: This will not save data to the item when the block is harvested.<br>
     * For that you need to override read and writeToStack just be sure to pay attention to the doc for those.
     */
    public void writeExtraNBT(NBTTagCompound compound) {
        NBTTagCompound capTags = serializeCapabilities(false);
        if (!capTags.hasNoTags()) {
            compound.setTag("bc_caps", capTags);
        }

        if (!customName.isEmpty()) {
            compound.setString("custom_name", customName);
        }
    }

    public void readExtraNBT(NBTTagCompound compound) {
        if (compound.hasKey("bc_caps")) {
            deserializeCapabilities(compound.getCompoundTag("bc_caps"));
        }

        if (compound.hasKey("custom_name", 8)) {
            customName = compound.getString("custom_name");
        }
    }

    protected NBTTagCompound serializeCapabilities(boolean forItem) {
        NBTTagCompound compound = new NBTTagCompound();
        for (DataHelper helper : serializableMap.values()) {
            if ((forItem && helper.saveItem) || (!forItem && helper.saveTile)) {
                compound.setTag(helper.tagName, helper.getData().serializeNBT());
            }
        }
        return compound;
    }

    @SuppressWarnings("unchecked")
    protected void deserializeCapabilities(NBTTagCompound compound) {
        for (DataHelper helper : serializableMap.values()) {
            if (compound.hasKey(helper.tagName)) {
                helper.getData().deserializeNBT(compound.getCompoundTag(helper.tagName));
            }
        }
    }

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
    public void onTileLoaded() {
    }

    //endregion

    //region Capabilities

    /**
     * Adds an forge energy storage capability to the tile.
     * This should be used in the tile's constructor to apply capabilities.
     * Capability data will be automatically saved and loaded from tile NBT.
     *
     * @param tagName The name used to save this cap instance to NBT. FE, OP and Item Handler caps are all stored to the same NBT Compound so this
     *                name must be unique. Pass null to prevent this cap from being saved to NBT.
     * @param storage The energy storage to add.
     * @param sides   The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
     * @return the {@link DataHelper} instance assigned to this data. Can be used to toggle save and sync flags. Or null if tagName is null.
     */
    protected <T extends IEnergyStorage & INBTSerializable<NBTTagCompound>> DataHelper<T> addEnergyCap(@Nullable String tagName, T storage, EnumFacing... sides) {
        mapCapToSides(storage, energyCaps, sides);
        if (storage instanceof IOPStorage) {
            mapCapToSides((IOPStorage) storage, opEnergyCaps, sides);
        }
        if (tagName != null) {
            return addInternalCap(tagName, storage);
        }
        return null;
    }

    /**
     * Convenience method for adding a single FE cap with the tag name "energy_storage"
     *
     * @param storage The energy storage to add.
     * @param sides   The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
     * @return the {@link DataHelper} instance assigned to this data. Can be used to toggle save and sync flags.
     */
    protected <T extends IEnergyStorage & INBTSerializable<NBTTagCompound>> DataHelper<T> addEnergyCap(T storage, EnumFacing... sides) {
        return addEnergyCap("energy_storage", storage, sides);
    }

    /**
     * Adds this cap strait to the capability maps bypassing all save and synchronization functionality.
     */
    protected <T extends IEnergyStorage> T addRawEnergyCap(T storage, EnumFacing... sides) {
        mapCapToSides(storage, energyCaps, sides);

        if (storage instanceof IOPStorage) {
            mapCapToSides((IOPStorage) storage, opEnergyCaps, sides);
        }
        return storage;
    }

    /**
     * Adds an IItemHandler capability to the tile.
     * This should be used in the tile's constructor to apply capabilities.
     * Capability data will be automatically saved and loaded from tile NBT.
     *
     * @param tagName     The name used to save this cap instance to NBT. FE, OP and Item Handler caps are all stored to the same NBT Compound so this
     *                    name must be unique. Pass null to prevent this cap from being saved to NBT.
     * @param itemHandler The item handler to add.
     * @param sides       The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
     * @return the {@link DataHelper} instance assigned to this data. Can be used to toggle save and sync flags.
     */
    protected <T extends ItemStackHandler> DataHelper<T> addItemHandlerCap(@Nullable String tagName, T itemHandler, EnumFacing... sides) {
        mapCapToSides(itemHandler, invCaps, sides);
        if (tagName != null) {
            return addInternalCap(tagName, itemHandler);
        }
        return null;
    }

    /**
     * Convenience method for adding a single item handler cap with the tag name "item_handler"
     *
     * @param itemHandler The item handler to add.
     * @param sides       The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
     * @return the {@link DataHelper} instance assigned to this data. Can be used to toggle save and sync flags.
     */
    protected <T extends ItemStackHandler> DataHelper<T> addItemHandlerCap(T itemHandler, EnumFacing... sides) {
        return addItemHandlerCap("item_handler", itemHandler, sides);
    }

    protected <T extends ItemStackHandler> DataHelper<T> addInternalItemHandlerCap(String tagName, T itemHandler) {
        return addInternalCap(tagName, itemHandler);
    }

    protected <T extends ItemStackHandler> DataHelper<T> addInternalItemHandlerCap(T itemHandler) {
        return addInternalItemHandlerCap("item_handler", itemHandler);
    }

    /**
     * Adds this cap strait to the capability map bypassing all save and synchronization functionality.
     */
    protected <T extends IItemHandler> T addRawItemCap(T handler, EnumFacing... sides) {
        mapCapToSides(handler, invCaps, sides);
        return handler;
    }

    /**
     * Allows you to add a capability to be saved and synchronized without actually exposing via has/getCapability.
     * Technically this can be used to save and sync any {@link INBTSerializable<NBTTagCompound>}
     *
     * @param tagName  The tag name that will be used to save this capability to nbt.
     * @param instance The INBTSerializable instance.
     * @return the {@link DataHelper} instance assigned to this data. Can be used to toggle save and sync flags.
     */
    protected <T extends INBTSerializable<NBTTagCompound>> DataHelper<T> addInternalCap(String tagName, T instance) {
        DataHelper<T> helper = new DataHelper<>(tagName, instance);
        serializableMap.put(instance, helper);
        indexedDataList.add(helper);
        return helper;
    }

    private <C> void mapCapToSides(C cap, Map<EnumFacing, C> map, EnumFacing... sides) {
        if (sides == null || sides.length == 0) {
            sides = EnumFacing.values();
            map.put(null, cap);
        }

        for (EnumFacing facing : sides) {
            map.put(facing, cap);
        }
    }

    /**
     * This can give you more dynamic control over capability sidedness.
     * To use this you must first apply the capability to all sides via the standard add method.
     * Or at least all the sides you want to be able to give it access to.
     * Then this predicate can be used to dynamically block or allows access to the configured sides.
     *
     * @param capabilityInstance the capability instance (the same instance provided to the add method)
     * @param predicate          the predicate that determines whether or not the capability can be accessed from a given side.
     */
    public void setCapSideValidator(Object capabilityInstance, Predicate<EnumFacing> predicate) {
        capSideValidator.put(capabilityInstance, predicate);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && energyCaps.containsKey(facing)) {
            IEnergyStorage cap = energyCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return true;
            }
        }
        if (capability == CapabilityOP.OP && opEnergyCaps.containsKey(facing)) {
            IOPStorage cap = opEnergyCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return true;
            }
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && invCaps.containsKey(facing)) {
            IItemHandler cap = invCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return true;
            }
        }
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && energyCaps.containsKey(facing)) {
            IEnergyStorage cap = energyCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return CapabilityEnergy.ENERGY.cast(cap);
            }
        }
        if (capability == CapabilityOP.OP && opEnergyCaps.containsKey(facing)) {
            IOPStorage cap = opEnergyCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return CapabilityOP.OP.cast(cap);
            }
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && invCaps.containsKey(facing)) {
            IItemHandler cap = invCaps.get(facing);
            if (!capSideValidator.containsKey(cap) || capSideValidator.get(cap).test(facing)) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(cap);
            }
        }
        return super.getCapability(capability, facing);
    }

    //endregion

    //region EnergyHelpers

    public long sendEnergyToAll(long maxPerTarget, long maxAvailable) {
        long i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(Math.min(maxPerTarget, maxAvailable - i), direction);
        }
        return i;
    }

    public long sendEnergyTo(long maxSend, EnumFacing side) {
        if (maxSend == 0) {
            return 0;
        }

        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyTo(IBlockAccess world, BlockPos pos, long maxSend, EnumFacing side) {
        if (maxSend == 0) {
            return 0;
        }

        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyToAll(IBlockAccess world, BlockPos pos, long maxPerTarget, long maxAvailable) {
        long i = 0;
        for (EnumFacing direction : EnumFacing.VALUES) {
            i += sendEnergyTo(world, pos, Math.min(maxPerTarget, maxAvailable - i), direction);
        }
        return i;
    }

//    /**
//     * Convenience method for getting the tiles primary energy handler.
//     * Will only return if the tile has a single energy storage. OOtherwisewill return null.
//     */
//    @Nullable
//    public IOPStorage getPrimaryOPStorage() {
//        return primaryOPStorage;
//    }
//
//    /**
//     * Sets the "primary" op storage. This is meant to be used as a simple internal method for retrieving the op storage
//     * from any {@link TileBCore}.
//     * This is used for things like the default energy bar implementation.
//     *
//     * @param primaryOPStorage An {@link IOPStorage} object or null.
//     */
//    public void setPrimaryOPStorage(@Nullable IOPStorage primaryOPStorage) {
//        this.primaryOPStorage = primaryOPStorage;
//    }

    /**
     * Adds an io tracker to the specified storage and ensures the tracker is updated every tick.
     * Note: for updating to work this tile must implement {@link ITickable} and call super in {@link ITickable#update()}
     *
     * @param storage The storage to add an IO tracker to.
     */
    public void installIOTracker(OPStorage storage) {
        storage.setIOTracker(addTickable(new IOTracker()));
    }

    /**
     * This method configures the specified slot in the specified item handler as an energy item slot.
     * The item in this slot will be automatically charged or discharged depending on whether chargeItem is true or false.
     * <p>
     * If the IItemHandler is an instance of {@link TileItemStackHandler} then this will automatically add a slot validator limiting
     * the slot to items that can receive/provide energy.
     *
     * @param itemHandler The item handler.
     * @param slot        The slot in the item handler.
     * @param storage     The storage to transfer energy into or out of.
     * @param chargeItem  A managed boolean that controls whether the item is being charged or discharged.
     */
    public void setupPowerSlot(IItemHandler itemHandler, int slot, IOPStorage storage, ManagedBool chargeItem) {
        setupPowerSlot(itemHandler, slot, storage, chargeItem::get);
    }

    /**
     * This method configures the specified slot in the specified item handler as an energy item slot.
     * The item in this slot will be automatically charged or discharged depending on whether chargeItem is true or false.
     * <p>
     * If the IItemHandler is an instance of {@link TileItemStackHandler} then this will automatically add a slot validator limiting
     * the slot to items that can receive/provide energy.
     *
     * @param itemHandler The item handler.
     * @param slot        The slot in the item handler.
     * @param storage     The storage to transfer energy into or out of.
     * @param chargeItem  If True the item will be charged otherwise it will be discharged.
     */
    public void setupPowerSlot(IItemHandler itemHandler, int slot, IOPStorage storage, boolean chargeItem) {
        setupPowerSlot(itemHandler, slot, storage, () -> chargeItem);
    }

    private void setupPowerSlot(IItemHandler itemHandler, int slot, IOPStorage storage, Supplier<Boolean> chargeItem) {
        if (itemHandler instanceof TileItemStackHandler) {
            ((TileItemStackHandler) itemHandler).setSlotValidator(slot, stack -> (chargeItem.get() ? EnergyUtils.canReceiveEnergy(stack) : EnergyUtils.canExtractEnergy(stack)));
        }
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            addTickable(() -> {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    if (chargeItem.get()) {
                        EnergyUtils.transferEnergy(storage, stack);
                    }
                    else {
                        EnergyUtils.transferEnergy(stack, storage);
                    }
                }
            });
        }
    }


    //endregion

    //Other

    public RSMode getRSMode() {
        if (!(this instanceof IRSSwitchable)) {
            throw new IllegalStateException("Tile does not implement IRSSwitchable");
        }
        return rsControlMode.get();
    }

    public void setRSMode(RSMode mode) {
        if (!(this instanceof IRSSwitchable)) {
            throw new IllegalStateException("Tile does not implement IRSSwitchable");
        }
        rsControlMode.set(mode);
    }

    public void cycleRSMode(boolean reverse) {
        rsControlMode.set(rsControlMode.get().next(reverse));
    }

    public void onNeighborChange(BlockPos neighbor) {
        if (this instanceof IRSSwitchable) {
            rsPowered.set(world.isBlockPowered(pos));
        }
    }

    /**
     * If this tile implements {@link IRSSwitchable} this method can be used to check if the tile is currently allowed to run.
     * This takes the current redstone state as well as the current control mode into consideration.
     *
     * @return true if the current RS control mode allows the tile to run given its current redstone state.
     */
    public boolean isTileEnabled() {
        if (this instanceof IRSSwitchable) {
            return rsControlMode.get().canRun(rsPowered.get());
        }

        return true;
    }

    @Override
    public String getName() {
        if (hasCustomName()) {
            return customName;
        }

        if (getBlockType() != null) {
            return getBlockType().getUnlocalizedName() + ".gui.title";
        }

        return "[Failed to compute name]";
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TextComponentString(getName()) : new TextComponentTranslation(getName());
    }

    protected static class DataHelper<T extends INBTSerializable<NBTTagCompound>> {
        private final String tagName;
        private final T serializableInstance;
        private Object lastData;

        private boolean saveTile = true;
        private boolean saveItem = true;
        private boolean syncTile = false;
        private boolean syncContainer = false;

        public DataHelper(String tagName, T serializableInstance) {
            this.tagName = tagName;
            this.serializableInstance = serializableInstance;

            if (serializableInstance instanceof IValueHashable) {
                lastData = ((IValueHashable) serializableInstance).getValueHash();
                syncContainer = true;
            }
            else {
                lastData = serializableInstance.serializeNBT();
            }
        }

        public T getData() {
            return serializableInstance;
        }

        /**
         * Default: true
         */
        public DataHelper<T> saveItem(boolean saveItem) {
            this.saveItem = saveItem;
            return this;
        }

        /**
         * Default: true
         */
        public DataHelper<T> saveTile(boolean saveTile) {
            this.saveTile = saveTile;
            return this;
        }

        /**
         * Default: false
         * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
         */
        public DataHelper<T> syncTile(boolean syncTile) {
            this.syncTile = syncTile;
            return this;
        }

        /**
         * Defaults to true for any serializable that also implements {@link IValueHashable}
         * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
         */
        public DataHelper<T> syncContainer(boolean syncContainer) {
            this.syncContainer = syncContainer;
            return this;
        }

        protected boolean hasChanged(boolean reset) {
            if (serializableInstance instanceof IValueHashable) {
                if (!((IValueHashable) serializableInstance).checkValueHash(lastData)) {
                    if (reset) {
                        lastData = ((IValueHashable) serializableInstance).getValueHash();
                    }
                    return true;
                }
            }
            else {
                if (!serializableInstance.serializeNBT().equals(lastData)) {
                    if (reset) {
                        lastData = serializableInstance.serializeNBT();
                    }
                    return true;
                }
            }

            return false;
        }
    }
}
