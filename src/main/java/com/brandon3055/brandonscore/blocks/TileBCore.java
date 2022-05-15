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
import com.brandon3055.brandonscore.network.ServerPacketHandler;
import com.brandon3055.brandonscore.utils.EnergyUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.thread.EffectiveSide;
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
public class TileBCore extends BlockEntity implements IDataManagerProvider, IDataRetainingTile, Nameable {

    protected boolean playerAccessTracking = false;
    protected TileCapabilityManager capManager = new TileCapabilityManager(this);
    protected TileDataManager<TileBCore> dataManager = new TileDataManager<>(this);
    private Map<Integer, BiConsumer<MCDataInput, ServerPlayer>> serverPacketHandlers = new HashMap<>();
    protected Map<String, INBTSerializable<CompoundTag>> savedItemDataObjects = new HashMap<>();
    protected Map<String, INBTSerializable<CompoundTag>> savedDataObjects = new HashMap<>();
    private Map<Integer, Consumer<MCDataInput>> clientPacketHandlers = new HashMap<>();

    private List<Runnable> tickables = new ArrayList<>();
    private ManagedEnum<RSMode> rsControlMode = this instanceof IRSSwitchable ? register(new ManagedEnum<>("rs_mode", RSMode.ALWAYS_ACTIVE, SAVE_BOTH_SYNC_TILE, CLIENT_CONTROL)) : null;
    private ManagedBool rsPowered = this instanceof IRSSwitchable ? register(new ManagedBool("rs_powered", false, SAVE_NBT_SYNC_TILE, TRIGGER_UPDATE)) : null;
    private String customName = "";
    private Set<Player> accessingPlayers = new HashSet<>();
    private int tick = 0;

    public TileBCore(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
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
        detectAndSendChanges(false);
        tick++;
    }

    public void detectAndSendChanges(boolean containerListeners) {
        if (level != null && !level.isClientSide) {
            if (containerListeners) {
                dataManager.detectAndSendChangesToListeners(getAccessingPlayers());
                capManager.detectAndSendChangesToListeners(getAccessingPlayers());
            } else {
                dataManager.detectAndSendChanges();
                capManager.detectAndSendChanges();
            }
        }
    }

    public int getAccessDistanceSq() {
        return 64;
    }

    //endregion

    //region Packets
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = super.getUpdateTag();
        dataManager.writeSyncNBT(compound);
        writeExtraNBT(compound);
        return compound;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        dataManager.readSyncNBT(pkt.getTag());
        readExtraNBT(pkt.getTag());
    }

    public PacketCustom createServerBoundPacket(int id) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.S_TILE_MESSAGE);
        packet.writePos(worldPosition);
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
    public void receivePacketFromClient(MCDataInput data, ServerPlayer client, int id) {
        if (serverPacketHandlers.containsKey(id)) {
            serverPacketHandlers.get(id).accept(data, client);
        }
    }

    public PacketCustom createClientBoundPacket(int id) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_MESSAGE);
        packet.writePos(worldPosition);
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

    public void sendPacketToClient(ServerPlayer player, Consumer<MCDataOutput> writer, int id) {
        sendPacketToClient(writer, id).sendToPlayer(player);
    }

    public void sendPacketToClients(Collection<Player> players, Consumer<MCDataOutput> writer, int id) {
        PacketCustom packet = createClientBoundPacket(id);
        writer.accept(packet);
        sendPacketToClients(players, packet);
    }

    public void sendPacketToClients(Collection<Player> players, PacketCustom packet) {
        players.stream().filter(e -> e instanceof ServerPlayer).map(e -> (ServerPlayer) e).forEach(packet::sendToPlayer);
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
    public void setServerSidePacketHandler(int packetId, BiConsumer<MCDataInput, ServerPlayer> handler) {
        this.serverPacketHandlers.put(packetId, handler);
    }

    //endregion

    //region Helper Functions.

    public void updateBlock() {
        BlockState state = level.getBlockState(getBlockPos());
        level.sendBlockUpdated(getBlockPos(), state, state, 3);
    }

    public void dirtyBlock() {
        LevelChunk chunk = level.getChunkAt(getBlockPos());
        chunk.setUnsaved(true);
    }

    /**
     * checks that the player is allowed to interact with this tile bu firing the RightClickBlock.
     * If the event is canceled bu another mod such as a permissions mod this will return false.
     * <br/>
     * Note: Packets from client to server do not need to be verified because that is already handled by the packet handler.
     */
    public boolean verifyPlayerPermission(Player player) {
        return ServerPacketHandler.verifyPlayerPermission(player, getBlockPos());
    }

    /**
     * Adds an item to the 'tickables' list. Every item in this list will be called every tick via the tiles update method.
     * Note: in order for this to work the tile must be ticking and call super in {@link #tick()}
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
    public void writeToItemStack(CompoundTag nbt, boolean willHarvest) {
        dataManager.writeToStackNBT(nbt);
        savedItemDataObjects.forEach((tagName, serializable) -> nbt.put(tagName, serializable.serializeNBT()));
        CompoundTag capTags = capManager.serialize(true);
        if (!capTags.isEmpty()) {
            nbt.put("bc_caps", capTags);
        }
        writeExtraTileAndStack(nbt);
    }


    @Override
    public void readFromItemStack(CompoundTag nbt) {
        dataManager.readFromStackNBT(nbt);
        savedItemDataObjects.forEach((tagName, serializable) -> serializable.deserializeNBT(nbt.getCompound(tagName)));
        if (nbt.contains("bc_caps")) {
            capManager.deserialize(nbt.getCompound("bc_caps"));
        }
        readExtraTileAndStack(nbt);
    }

    /**
     * Write any extra data that needs to be saved to NBT that is not saved via a syncable field.
     * This data is also synced to the client via getUpdateTag and getUpdatePacket.
     * Note: This will not save data to the item when the block is harvested.<br>
     * For that you need to override read and writeToStack just be sure to pay attention to the doc for those.
     */
    public void writeExtraNBT(CompoundTag nbt) {
        CompoundTag capTags = capManager.serialize(false);
        if (!capTags.isEmpty()) {
            nbt.put("bc_caps", capTags);
        }

        if (!customName.isEmpty()) {
            nbt.putString("custom_name", customName);
        }

        savedDataObjects.forEach((tagName, serializable) -> nbt.put(tagName, serializable.serializeNBT()));
        writeExtraTileAndStack(nbt);
    }

    public void readExtraNBT(CompoundTag nbt) {
        if (nbt.contains("bc_caps")) {
            capManager.deserialize(nbt.getCompound("bc_caps"));
        }

        if (nbt.contains("custom_name", 8)) {
            customName = nbt.getString("custom_name");
        }

        savedDataObjects.forEach((tagName, serializable) -> serializable.deserializeNBT(nbt.getCompound(tagName)));
        readExtraTileAndStack(nbt);
    }

    /**
     * Convenience method that is called by both
     * {@link #writeExtraNBT(CompoundTag)} and
     * {@link #writeToItemStack(CompoundTag, boolean)}
     */
    public void writeExtraTileAndStack(CompoundTag nbt) {}

    /**
     * Convenience method that is called by both
     * {@link #readExtraNBT(CompoundTag)} and
     * {@link #readFromItemStack(CompoundTag)}
     */
    public void readExtraTileAndStack(CompoundTag nbt) {}

    @Override
    protected final void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        dataManager.writeToNBT(nbt);
        writeExtraNBT(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        dataManager.readFromNBT(nbt);
        readExtraNBT(nbt);
        onTileLoaded();
    }

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
    public void setSavedDataObject(String tagName, INBTSerializable<CompoundTag> dataObject) {
        this.savedDataObjects.put(tagName, dataObject);
    }

    public void setItemSavedDataObject(String tagName, INBTSerializable<CompoundTag> dataObject) {
        this.savedItemDataObjects.put(tagName, dataObject);
    }

    //endregion

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        LazyOptional<T> ret = capManager.getCapability(capability, side);
        return ret.isPresent() ? ret : super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        capManager.invalidate();
    }

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

        BlockEntity tile = level.getBlockEntity(worldPosition.relative(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyTo(LevelReader world, BlockPos pos, long maxSend, Direction side) {
        if (maxSend == 0) {
            return 0;
        }

        BlockEntity tile = world.getBlockEntity(pos.relative(side));
        if (tile != null) {
            return EnergyUtils.insertEnergy(tile, maxSend, side.getOpposite(), false);
        }
        return 0;
    }

    public static long sendEnergyToAll(LevelReader world, BlockPos pos, long maxPerTarget, long maxAvailable) {
        long i = 0;
        for (Direction direction : Direction.values()) {
            i += sendEnergyTo(world, pos, Math.min(maxPerTarget, maxAvailable - i), direction);
        }
        return i;
    }

    /**
     * Adds an io tracker to the specified storage and ensures the tracker is updated every tick.
     * Note: for updating to work this tile must be ticking and call super in {@link #tick()}
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
     * Only works on ticking tiles that call super.update()
     *
     * @return an internal tick timer specific to this tile
     */
    public int getTime() {
        return tick;
    }

    /**
     * Only works on ticking tiles that call super.update()
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
            boolean lastSignal = rsPowered.get();
            rsPowered.set(level.hasNeighborSignal(worldPosition));
            if (rsPowered.get() != lastSignal) {
                onSignalChange(rsPowered.get());
            }
        }
    }

    public void onSignalChange(boolean newSignal) {

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

    public boolean hasRSSignal() {
        return level.hasNeighborSignal(getBlockPos());
    }

    public int getRSSignal() {
        return level.getBestNeighborSignal(getBlockPos());
    }


    @Override
    public Component getName() {
        if (hasCustomName()) {
            return new TextComponent(customName);
        }

        return new TranslatableComponent(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return customName.isEmpty() ? null : getName();
    }

    public Component getDisplayName() {
        return getName();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

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
    public Set<Player> getAccessingPlayers() {
        accessingPlayers.removeIf(e -> !(e.containerMenu instanceof ContainerBCore) || ((ContainerBCTile) e.containerMenu).tile != this); //Clean up set
        return accessingPlayers;
    }

    public void onPlayerOpenContainer(Player player) {
        accessingPlayers.add(player);
    }

    public void onPlayerCloseContainer(Player player) {
        accessingPlayers.remove(player);
        accessingPlayers.removeIf(e -> !(e.containerMenu instanceof ContainerBCore) || ((ContainerBCTile) e.containerMenu).tile != this); //Clean up set
    }

    public int posSeed() {
        return (int) worldPosition.asLong();
    }

}
