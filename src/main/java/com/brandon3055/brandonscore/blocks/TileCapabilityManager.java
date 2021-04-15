package com.brandon3055.brandonscore.blocks;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Created by brandon3055 on 18/12/19.
 */
public class TileCapabilityManager implements ICapabilityProvider {

    private Map<Capability<?>, Map<Direction, LazyOptional<?>>> capabilityMap = new HashMap<>();
    private Map<Object, Predicate<Direction>> capSideValidator = new HashMap<>();

    private Map<INBTSerializable<CompoundNBT>, SerializationFlags<?>> serializableMap = new HashMap<>();
    private List<SerializationFlags<?>> indexedDataList = new ArrayList<>();
    private TileBCore tile;

    public TileCapabilityManager(TileBCore tile) {
        this.tile = tile;
    }

    /**
     * Bind the given capability instance to the specified sides while also invalidating and replacing
     * any existing capabilities of this type on the specified side.
     *
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param sides       The sides to bind to. (Leave empty to bind to all sides including null)
     */
    public <T> void set(@Nonnull Capability<?> cap, @Nonnull T capInstance, Direction... sides) {
        if (sides == null) {
            return;
        }
        if (sides.length == 0) {
            sides = Direction.values();
            setSide(cap, capInstance, null);
        }

        for (Direction dir : sides) {
            setSide(cap, capInstance, dir);
        }
    }

    /**
     * This method has the same functionality as {@link #set(Capability, Object, Direction...)}.
     * However it also adds capability to the management system that can automatically save, load and synchronize
     * the capability. This should only be used on static capability instances that dont need to be replaced runtime.
     *
     * @param tagName     The NBT name for this capability.
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param sides       The sides to bind to. (Leave empty to bind to all sides including null)
     * @return The modifiable serialization flags. By default set to 'save to tile' and 'save to item'
     * @see #set(Capability, Object, Direction...)
     */
    public <T extends INBTSerializable<CompoundNBT>> SerializationFlags<T> setManaged(String tagName, @Nonnull Capability<?> cap, @Nonnull T capInstance, Direction... sides) {
        set(cap, capInstance, sides);
        SerializationFlags<T> flags = new SerializationFlags<>(tagName, capInstance);
        serializableMap.put(capInstance, flags);
        indexedDataList.add(flags);
        return flags;
    }

    /**
     * The same as setManaged except the capability will not be exposes at all via getCapability. Used in cases where you need a "private" internal capability
     * @see #setManaged(String, Capability, INBTSerializable, Direction...)
     */
    public <T extends INBTSerializable<CompoundNBT>> SerializationFlags<T> setInternalManaged(String tagName, @Nonnull Capability<?> cap, @Nonnull T capInstance) {
        SerializationFlags<T> flags = new SerializationFlags<>(tagName, capInstance);
        serializableMap.put(capInstance, flags);
        indexedDataList.add(flags);
        return flags;
    }

    /**
     * Invalidate and remove the specified capability type from the specified sides.
     *
     * @param cap   The capability type to remove.
     * @param sides The sides to remove from. (Leave empty to bind to all sides including null)
     */
    public <T> void remove(@Nonnull Capability<T> cap, Direction... sides) {
        if (sides.length == 0) {
            sides = Direction.values();
            clearSide(cap, null);
        }

        for (Direction dir : sides) {
            clearSide(cap, dir);
        }
    }

    /**
     * Bind the given capability instance to the specified side while also invalidating and replacing
     * any existing capability of this type.
     *
     * @param cap         The capability type.
     * @param capInstance The capability instance.
     * @param side        The side to bind to. (can be null)
     */
    public <T> void setSide(@Nonnull Capability<?> cap, @Nonnull T capInstance, @Nullable Direction side) {
        Map<Direction, LazyOptional<?>> map = capabilityMap.computeIfAbsent(cap, c -> new HashMap<>());
        LazyOptional<?> previous = map.get(side);
        map.put(side, LazyOptional.of(() -> capInstance));

        if (previous != null) {
            previous.invalidate();
        }
    }

    /**
     * Invalidate and remove the specified capability type from the specified side.
     *
     * @param cap  The capability type to remove.
     * @param side The side to remove from. (can be null)
     */
    public <T> void clearSide(@Nonnull Capability<?> cap, @Nullable Direction side) {
        Map<Direction, LazyOptional<?>> map = capabilityMap.get(cap);
        if (map != null) {
            LazyOptional<?> previous = map.get(side);
            map.remove(side);

            if (previous != null) {
                previous.invalidate();
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        Map<Direction, LazyOptional<?>> map = capabilityMap.get(cap);
        if (map == null && cap == CapabilityEnergy.ENERGY) {
            map = capabilityMap.get(CapabilityOP.OP);
        }

        if (map != null && map.containsKey(side)) {
            LazyOptional<T> optional = (LazyOptional<T>) map.get(side);
            if (optional.filter(o -> capSideValidator.getOrDefault(o, d -> true).test(side)).isPresent()) {
                return optional;
            }
        }

        return LazyOptional.empty();
    }

    //Should be called by the tile's remove method to invalidate capabilities when the tile is removed.
    public void invalidate() {
        capabilityMap.values().forEach(map -> map.values().forEach(LazyOptional::invalidate));
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
    public void setCapSideValidator(Object capabilityInstance, Predicate<Direction> predicate) {
        capSideValidator.put(capabilityInstance, predicate);
    }

    //Serialization

    public CompoundNBT serialize(boolean forItem) {
        CompoundNBT compound = new CompoundNBT();
        for (SerializationFlags<?> helper : serializableMap.values()) {
            if ((forItem && helper.saveItem) || (!forItem && helper.saveTile)) {
                compound.put(helper.tagName, helper.getData().serializeNBT());
            }
        }
        return compound;
    }

    @SuppressWarnings("unchecked")
    public void deserialize(CompoundNBT compound) {
        for (SerializationFlags<?> helper : serializableMap.values()) {
            if (compound.contains(helper.tagName)) {
                helper.getData().deserializeNBT(compound.getCompound(helper.tagName));
            }
        }
    }

    //Synchronization

    public void detectAndSendChanges() {
        for (int i = 0; i < indexedDataList.size(); i++) {
            SerializationFlags<?> helper = indexedDataList.get(i);
            if (helper.syncTile && helper.hasChanged(true)) {
                PacketCustom packet = createCapPacket(helper, i);
                packet.sendToChunk(tile);
            }
        }
    }

    public void detectAndSendChangesToListeners(List<IContainerListener> listeners) {
        for (int i = 0; i < indexedDataList.size(); i++) {
            SerializationFlags<?> helper = indexedDataList.get(i);
            if (helper.syncContainer && helper.hasChanged(true)) {
                PacketCustom packet = createCapPacket(helper, i);
                DataUtils.forEachMatch(listeners, p -> p instanceof ServerPlayerEntity, p -> packet.sendToPlayer((ServerPlayerEntity) p));
            }
        }
    }

    private PacketCustom createCapPacket(SerializationFlags<?> helper, int index) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, BCoreNetwork.C_TILE_CAP_DATA);
        packet.writePos(tile.getBlockPos());
        packet.writeInt(index);
        if (helper.getData() instanceof IMCDataSerializable) {
            ((IMCDataSerializable) helper.getData()).serializeMCD(packet);
        } else {
            packet.writeCompoundNBT((CompoundNBT) helper.getData().serializeNBT());
        }
        return packet;
    }

    @SuppressWarnings("unchecked")
    public void receiveCapSyncData(MCDataInput input) {
        int index = input.readInt();
        if (index >= 0 && index < indexedDataList.size()) {
            SerializationFlags<?> helper = indexedDataList.get(index);
            if (helper.getData() instanceof IMCDataSerializable) {
                ((IMCDataSerializable) helper.getData()).deSerializeMCD(input);
            } else {
                helper.getData().deserializeNBT(input.readCompoundNBT());
            }
        }
    }
}
