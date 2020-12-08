package com.brandon3055.brandonscore.blocks;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.api.IDataRetainingTile;
import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.api.power.IOTracker;
import com.brandon3055.brandonscore.api.power.OPStorage;
import com.brandon3055.brandonscore.inventory.ContainerBCTile;
import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.brandon3055.brandonscore.inventory.TileItemStackHandler;
import com.brandon3055.brandonscore.lib.IRSSwitchable;
import com.brandon3055.brandonscore.lib.IRSSwitchable.RSMode;
import com.brandon3055.brandonscore.lib.datamanager.*;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.INameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.brandon3055.brandonscore.lib.datamanager.DataFlags.*;

/**
 * Created by brandon3055 on 26/3/2016.
 * Base tile entity class for all tile entities
 */
public class TileBCore extends TileEntity implements IDataManagerProvider, IDataRetainingTile, INameable {

    protected boolean playerAccessTracking = false;
    protected TileCapabilityManager capManager = new TileCapabilityManager(this);
    protected TileDataManager<TileBCore> dataManager = new TileDataManager<>(this);
    private Map<Integer, BiConsumer<MCDataInput, ServerPlayerEntity>> serverPacketHandlers = new HashMap<>();
    protected Map<String, INBTSerializable<CompoundNBT>> savedItemDataObjects = new HashMap<>();
    protected Map<String, INBTSerializable<CompoundNBT>> savedDataObjects = new HashMap<>();
    private Map<Integer, Consumer<MCDataInput>> clientPacketHandlers = new HashMap<>();

    private List<Runnable> tickables = new ArrayList<>();
    private ManagedEnum<RSMode> rsControlMode = this instanceof IRSSwitchable ? register(new ManagedEnum<>("rs_mode", RSMode.ALWAYS_ACTIVE, SAVE_BOTH_SYNC_TILE, CLIENT_CONTROL)) : null;
    private ManagedBool rsPowered = this instanceof IRSSwitchable ? register(new ManagedBool("rs_powered", false, SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE)) : null;
    private String customName = "";
    private Set<PlayerEntity> accessingPlayers = new HashSet<>();
    private int tick = 0;

    public TileBCore(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    //region Data Manager

    @Override
    public TileDataManager getDataManager() {
        return dataManager;
    }

    public TileCapabilityManager getCapManager() {
        return capManager;
    }

    /**
     * Convenience method for dataManager.register();
     */
    public <M extends IManagedData> M register(M managedData) {
        return dataManager.register(managedData);
    }

    /**
     * super.tick() must be called from your update method in order for Data Manager synchronization to work..
     */
    public void tick() {
        tickables.forEach(Runnable::run);
        detectAndSendChanges();
        tick++;
    }

    public void detectAndSendChanges() {
        if (world != null && !world.isRemote) {
            dataManager.detectAndSendChanges();
            capManager.detectAndSendChanges();
        }
    }

    public void detectAndSendChangesToListeners(List<IContainerListener> listeners) {
        if (world != null && !world.isRemote) {
            dataManager.detectAndSendChangesToListeners(listeners);
            capManager.detectAndSendChangesToListeners(listeners);
        }
    }

    public int getAccessDistanceSq() {
        return 64;
    }

    //endregion

    //region Packets
    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT compound = new CompoundNBT();
        dataManager.writeSyncNBT(compound);
        writeExtraNBT(compound);
        return new SUpdateTileEntityPacket(this.pos, 0, compound);
    }

    //Used when initially sending chunks to the client... I think
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT compound = super.getUpdateTag();
        dataManager.writeSyncNBT(compound);
        writeExtraNBT(compound);
        return compound;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        dataManager.readSyncNBT(tag);
        readExtraNBT(tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        dataManager.readSyncNBT(pkt.getNbtCompound());
        readExtraNBT(pkt.getNbtCompound());
    }

    public PacketCustom createServerBoundPacket(int id) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.S_TILE_MESSAGE);
        packet.writePos(pos);
        packet.writeByte((byte) id);
        return packet;
    }

    /**
     * Send a data packet to the server, Supply a consumer to write the data you want to send
     */
    public void sendPacketToServer(Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = createServerBoundPacket(id);
        writer.accept(packet);
        packet.sendToServer();
    }

    /**
     * Override this method to receive data from the server via sendPacketToServer
     */
    public void receivePacketFromClient(MCDataInput data, ServerPlayerEntity client, int id) {
        if (serverPacketHandlers.containsKey(id)) {
            serverPacketHandlers.get(id).accept(data, client);
        }
    }

    public PacketCustom createClientBoundPacket(int id) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_MESSAGE);
        packet.writePos(pos);
        packet.writeByte((byte) id);
        return packet;
    }

    /**
     * Create a packet to send to the client
     */
    public PacketCustom sendPacketToClient(Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = createClientBoundPacket(id);
        writer.accept(packet);
        return packet;
    }

    public void sendPacketToClient(ServerPlayerEntity player, Consumer<MCDataOutput> writer, int id) {
        sendPacketToClient(writer, id).sendToPlayer(player);
    }

    public void sendPacketToClients(Collection<PlayerEntity> players, Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = createClientBoundPacket(id);
        writer.accept(packet);
        sendPacketToClients(players, packet);
    }

    public void sendPacketToClients(Collection<PlayerEntity> players, PacketCustom packet) {
        players.stream().filter(e -> e instanceof ServerPlayerEntity).map(e -> (ServerPlayerEntity) e).forEach(packet::sendToPlayer);
    }

    public void sendPacketToChunk(Consumer<MCDataOutput> writer, int id) {
        sendPacketToClient(writer, id).sendToChunk(this);
    }

//    public void sendPacketToClient(NetworkRegistry.TargetPoint tp, Consumer<MCDataOutput> writer, int id) {
//        sendPacketToClient(writer, id).sendPacketToAllAround(tp.x, tp.y, tp.z, tp.range, tp.dimension);
//    }

    /**
     * Override this method to receive data from the client via sendPacketToClient
     */
    public void receivePacketFromServer(MCDataInput data, int id) {
        if (clientPacketHandlers.containsKey(id)) {
            clientPacketHandlers.get(id).accept(data);
        }
    }

    /**
     * Sets a client side packet handler to handle packets sent from the server with the specified id
     *
     * @param packetId packet id
     * @param handler  the handler for this packet
     */
    public void setClientSidePacketHandler(int packetId, Consumer<MCDataInput> handler) {
        this.clientPacketHandlers.put(packetId, handler);
    }

    /**
     * Sets a server side packet handler to handle packets sent from the client with the specified id
     *
     * @param packetId packet id
     * @param handler  the handler for this packet
     */
    public void setServerSidePacketHandler(int packetId, BiConsumer<MCDataInput, ServerPlayerEntity> handler) {
        this.serverPacketHandlers.put(packetId, handler);
    }

    //endregion

    //region Helper Functions.

    public void updateBlock() {
        BlockState state = world.getBlockState(getPos());
        world.notifyBlockUpdate(getPos(), state, state, 3);
    }

    public void dirtyBlock() {
        Chunk chunk = world.getChunkAt(getPos());
        chunk.setModified(true);
    }

//    /**
//     * Calling this in the constructor will force the tile to only refresh when the block changes rather then when the state changes.
//     * Note that this should NOT be used in cases where the block has a different tile depending on its state.
//     */
//    public void setShouldRefreshOnBlockChange() {
//        shouldRefreshOnState = false;
//    }

//    @Deprecated //I want to store everything on the tile in 1.13. I'm done dealing with these bullshit blockstate crashes.
//    public BlockState getState(Block expectedBlock) {
//        if (world == null) {
//            return expectedBlock.getDefaultState();//Because apparently this is a thing........
//        }
//        BlockState state = world.getBlockState(pos);
//        return state.getBlock() == expectedBlock ? state : expectedBlock.getDefaultState();
//    }
//
//    /**
//     * Is minecraft seriously so screwed up that i have to resort to things like this?
//     */
//    public Block getBlockTypeSafe(Block defaultBlock) {
//        if (getBlockState().isAir(world, pos)) {
//            return getBlockType();
//        } else {
//            return defaultBlock;
//        }
//    }

    /**
     * checks that the player is allowed to interact with this tile bu firing the RightClickBlock.
     * If the event is canceled bu another mod such as a permissions mod this will return false.
     * <br/>
     * Note: Packets from client to server do not need to be verified because that is already handled by the packet handler.
     */
    public boolean verifyPlayerPermission(PlayerEntity player) {
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(player, Hand.MAIN_HAND, pos, Direction.UP);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    /**
     * Adds an item to the 'tickables' list. Every item in this list will be called every tick via the tiles update method.
     * Note: in order for this to work the tile must implement ITickable and call super in {@link ITickableTileEntity#tick()} ()}
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
     */
    @Override
    public void writeToItemStack(CompoundNBT compound, boolean willHarvest) {
        dataManager.writeToStackNBT(compound);
        savedItemDataObjects.forEach((tagName, serializable) -> compound.put(tagName, serializable.serializeNBT()));
        CompoundNBT capTags = capManager.serialize(true);
        if (!capTags.isEmpty()) {
            compound.put("bc_caps", capTags);
        }
    }


    @Override
    public void readFromItemStack(CompoundNBT compound) {
        dataManager.readFromStackNBT(compound);
        savedItemDataObjects.forEach((tagName, serializable) -> serializable.deserializeNBT(compound.getCompound(tagName)));
        if (compound.contains("bc_caps")) {
            capManager.deserialize(compound.getCompound("bc_caps"));
        }
    }

    /**
     * Write any extra data that needs to be saved to NBT that is not saved via a syncable field.
     * This data is also synced to the client via getUpdateTag and getUpdatePacket.
     * Note: This will not save data to the item when the block is harvested.<br>
     * For that you need to override read and writeToStack just be sure to pay attention to the doc for those.
     */
    public void writeExtraNBT(CompoundNBT compound) {
        CompoundNBT capTags = capManager.serialize(false);
        if (!capTags.isEmpty()) {
            compound.put("bc_caps", capTags);
        }

        if (!customName.isEmpty()) {
            compound.putString("custom_name", customName);
        }

        savedDataObjects.forEach((tagName, serializable) -> compound.put(tagName, serializable.serializeNBT()));
    }

    public void readExtraNBT(CompoundNBT compound) {
        if (compound.contains("bc_caps")) {
            capManager.deserialize(compound.getCompound("bc_caps"));
        }

        if (compound.contains("custom_name", 8)) {
            customName = compound.getString("custom_name");
        }

        savedDataObjects.forEach((tagName, serializable) -> serializable.deserializeNBT(compound.getCompound(tagName)));
    }

    @Override
    public final CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        dataManager.writeToNBT(compound);
        writeExtraNBT(compound);

        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        dataManager.readFromNBT(nbt);
        readExtraNBT(nbt);

        onTileLoaded();
    }


//    @Override
//    public boolean shouldRefresh(World world, BlockPos pos, BlockState oldState, BlockState newSate) {
//        return shouldRefreshOnState ? oldState != newSate : (oldState.getBlock() != newSate.getBlock());
//    }

    /**
     * Called immediately after all NBT is loaded. World may be null at this point
     */
    public void onTileLoaded() {
    }

    /**
     * Allows you to add raw INBTSerializable data objects too be saved and loaded directly from the tile's NBT.
     * This saves and loads the data server side. Its also sent in the default chunk sync packet.
     *
     * @param tagName    the name to use when saving this object to the tile's NBT
     * @param dataObject the serializable data object.
     */
    public void setSavedDataObject(String tagName, INBTSerializable<CompoundNBT> dataObject) {
        this.savedDataObjects.put(tagName, dataObject);
    }

    public void setItemSavedDataObject(String tagName, INBTSerializable<CompoundNBT> dataObject) {
        this.savedItemDataObjects.put(tagName, dataObject);
    }

    //endregion

//    //region Capabilities
//
//    /**
//     * Adds an forge energy storage capability to the tile.
//     * This should be used in the tile's constructor to apply capabilities.
//     * Capability data will be automatically saved and loaded from tile NBT.
//     *
//     * @param tagName The name used to save this cap instance to NBT. FE, OP and Item Handler caps are all stored to the same NBT Compound so this
//     *                name must be unique. Pass null to prevent this cap from being saved to NBT.
//     * @param storage The energy storage to add.
//     * @param sides   The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
//     * @return the {@link SerializationFlags} instance assigned to this data. Can be used to toggle save and sync flags. Or null if tagName is null.
//     */
//    protected <T extends IEnergyStorage & INBTSerializable<CompoundNBT>> SerializationFlags<T> addEnergyCap(@Nullable String tagName, T storage, Direction... sides) {
//        mapCapToSides(storage, energyCaps, sides);
//        if (storage instanceof IOPStorage) {
//            mapCapToSides((IOPStorage) storage, opEnergyCaps, sides);
//        }
//        if (tagName != null) {
//            return addInternalCap(tagName, storage);
//        }
//        return null;
//    }
//
//    /**
//     * Convenience method for adding a single FE cap with the tag name "energy_storage"
//     *
//     * @param storage The energy storage to add.
//     * @param sides   The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
//     * @return the {@link SerializationFlags} instance assigned to this data. Can be used to toggle save and sync flags.
//     */
//    protected <T extends IEnergyStorage & INBTSerializable<CompoundNBT>> SerializationFlags<T> addEnergyCap(T storage, Direction... sides) {
//        return addEnergyCap("energy_storage", storage, sides);
//    }
//
//    /**
//     * Adds this cap strait to the capability maps bypassing all save and synchronization functionality.
//     */
//    protected <T extends IEnergyStorage> T addRawEnergyCap(T storage, Direction... sides) {
//        mapCapToSides(storage, energyCaps, sides);
//
//        if (storage instanceof IOPStorage) {
//            mapCapToSides((IOPStorage) storage, opEnergyCaps, sides);
//        }
//        return storage;
//    }
//
//    /**
//     * Adds an IItemHandler capability to the tile.
//     * This should be used in the tile's constructor to apply capabilities.
//     * Capability data will be automatically saved and loaded from tile NBT.
//     *
//     * @param tagName     The name used to save this cap instance to NBT. FE, OP and Item Handler caps are all stored to the same NBT Compound so this
//     *                    name must be unique. Pass null to prevent this cap from being saved to NBT.
//     * @param itemHandler The item handler to add.
//     * @param sides       The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
//     * @return the {@link SerializationFlags} instance assigned to this data. Can be used to toggle save and sync flags.
//     */
//    protected <T extends ItemStackHandler> SerializationFlags<T> addItemHandlerCap(@Nullable String tagName, T itemHandler, Direction... sides) {
//        mapCapToSides(itemHandler, invCaps, sides);
//        if (tagName != null) {
//            return addInternalCap(tagName, itemHandler);
//        }
//        return null;
//    }
//
//    /**
//     * Convenience method for adding a single item handler cap with the tag name "item_handler"
//     *
//     * @param itemHandler The item handler to add.
//     * @param sides       The side or sides (including null) that this storage should be exposed to. If nothing is specified this storage will be available to all sides.
//     * @return the {@link SerializationFlags} instance assigned to this data. Can be used to toggle save and sync flags.
//     */
//    protected <T extends ItemStackHandler> SerializationFlags<T> addItemHandlerCap(T itemHandler, Direction... sides) {
//        return addItemHandlerCap("item_handler", itemHandler, sides);
//    }
//
//    protected <T extends ItemStackHandler> SerializationFlags<T> addInternalItemHandlerCap(String tagName, T itemHandler) {
//        return addInternalCap(tagName, itemHandler);
//    }
//
//    protected <T extends ItemStackHandler> SerializationFlags<T> addInternalItemHandlerCap(T itemHandler) {
//        return addInternalItemHandlerCap("item_handler", itemHandler);
//    }
//
//    /**
//     * Adds this cap strait to the capability map bypassing all save and synchronization functionality.
//     */
//    protected <T extends IItemHandler> T addRawItemCap(T handler, Direction... sides) {
//        mapCapToSides(handler, invCaps, sides);
//        return handler;
//    }
//
//    /**
//     * Allows you to add a capability to be saved and synchronized without actually exposing via has/getCapability.
//     * Technically this can be used to save and sync any {@link INBTSerializable<CompoundNBT>}
//     *
//     * @param tagName  The tag name that will be used to save this capability to nbt.
//     * @param instance The INBTSerializable instance.
//     * @return the {@link SerializationFlags} instance assigned to this data. Can be used to toggle save and sync flags.
//     */
//    protected <T extends INBTSerializable<CompoundNBT>> SerializationFlags<T> addInternalCap(String tagName, T instance) {
//        SerializationFlags<T> helper = new SerializationFlags<>(tagName, instance);
//        serializableMap.put(instance, helper);
//        indexedDataList.add(helper);
//        return helper;
//    }
//
//    private <C> void mapCapToSides(C cap, Map<Direction, C> map, Direction... sides) {
//        if (sides == null || sides.length == 0) {
//            sides = Direction.values();
//            map.put(null, cap);
//        }
//
//        for (Direction facing : sides) {
//            map.put(facing, cap);
//        }
//    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        LazyOptional<T> ret = capManager.getCapability(capability, side);
        return ret.isPresent() ? ret : super.getCapability(capability, side);
    }

    @Override
    protected void invalidateCaps() {
        super.invalidateCaps();
        capManager.invalidate();
    }

    //endregion

    //region EnergyHelpers

    public long sendEnergyToAll(long maxPerTarget, long maxAvailable) {
        long i = 0;
        for (Direction direction : Direction.values()) {
            i += sendEnergyTo(Math.min(maxPerTarget, maxAvailable - i), direction);
        }
        return i;
    }

    public long sendEnergyTo(long maxSend, Direction side) {
        if (maxSend == 0) {
            return 0;
        }

        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyTo(IWorldReader world, BlockPos pos, long maxSend, Direction side) {
        if (maxSend == 0) {
            return 0;
        }

        TileEntity tile = world.getTileEntity(pos.offset(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyToAll(IWorldReader world, BlockPos pos, long maxPerTarget, long maxAvailable) {
        long i = 0;
        for (Direction direction : Direction.values()) {
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
     * Note: for updating to work this tile must implement {@link ITickableTileEntity} and call super in {@link ITickableTileEntity#tick()} ()}
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
        if (EffectiveSide.get().isServer()) {
            addTickable(() -> {
                ItemStack stack = itemHandler.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    if (chargeItem.get()) {
                        EnergyUtils.transferEnergy(storage, stack);
                    } else {
                        EnergyUtils.transferEnergy(stack, storage);
                    }
                }
            });
        }
    }


    //endregion

    //Other

    /**
     * Only works on {@link ITickableTileEntity} tiles that call super.update()
     *
     * @return an internal tick timer specific to this tile
     */
    public int getTime() {
        return tick;
    }

    /**
     * Only works on {@link ITickableTileEntity} tiles that call super.update()
     *
     * @return true once every 'tickInterval' based on the tiles internal timer.
     */
    public boolean onInterval(int tickInterval) {
        return tick % tickInterval == 0;
    }

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
    public ITextComponent getName() {
        if (hasCustomName()) {
            return new StringTextComponent(customName);
        }

        return getBlockState().getBlock().getTranslatedName();
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    @Nullable
    @Override
    public ITextComponent getCustomName() {
        return customName.isEmpty() ? null : getName();
    }

    public ITextComponent getDisplayName() {
        return getName();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

//    @Nullable
//    @Override
//    public ITextComponent getDisplayName() {
//        return hasCustomName() ? new StringTextComponent(getName()) : new TextComponentTranslation(getName());
//    }

    /**
     * If enabled a list of players currently accessing this tiles container will be available via {@link #getAccessingPlayers()}
     *
     * @param playerAccessTracking enable tracking
     */
    public void enablePlayerAccessTracking(boolean playerAccessTracking) {
        this.playerAccessTracking = playerAccessTracking;
    }

    /**
     * @return a list of players currently accessing this tile's container.
     * playerAccessTracking must be enabled in this tile's constructor in order for this to work.
     */
    public Set<PlayerEntity> getAccessingPlayers() {
        accessingPlayers.removeIf(e -> !(e.openContainer instanceof ContainerBCore) || ((ContainerBCTile) e.openContainer).tile != this); //Clean up set
        return accessingPlayers;
    }

    public void onPlayerOpenContainer(PlayerEntity player) {
        accessingPlayers.add(player);
    }

    public void onPlayerCloseContainer(PlayerEntity player) {
        accessingPlayers.remove(player);
        accessingPlayers.removeIf(e -> !(e.openContainer instanceof ContainerBCore) || ((ContainerBCTile) e.openContainer).tile != this); //Clean up set
    }

}
