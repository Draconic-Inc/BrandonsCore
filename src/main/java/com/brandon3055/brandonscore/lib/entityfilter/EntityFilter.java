package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.packet.PacketCustom;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class EntityFilter extends FilterGroup {
    private boolean livingOnly = false;
    private List<FilterType> allowedFilters = new ArrayList<>();
    private List<FilterBase> fixedFilters = new ArrayList<>();
    public Int2ObjectMap<FilterBase> nodeMap = new Int2ObjectOpenHashMap<>();
    private Predicate<FilterType> typePredicate;
    private int nextNodeID = 1;
    private Supplier<? extends MCDataOutput> serverPacketProvider;
    private Consumer<PacketCustom> serverPacketSender;
    private Supplier<? extends MCDataOutput> clientPacketProvider;
    private Consumer<PacketCustom> clientPacketSender;
    private Runnable dirtyHandler;
    private com.google.common.base.Predicate<Entity> filterPredicate = this::test;
    public boolean fixedAndLogic = false;
    public int maxFilters = 256;

    /**
     * @param livingOnly If true this filter will always return false for any entity that does is not an instance of {@link net.minecraft.entity.LivingEntity}
     */
    public EntityFilter(boolean livingOnly, FilterType... allowedFilters) {
        super(null);
        this.livingOnly = livingOnly;
        this.allowedFilters.addAll(Arrays.asList(allowedFilters));
        this.andGroup = true;
        this.nodeID = 0;
        this.trackNode(this);
    }

    public boolean isLivingOnly() {
        return livingOnly;
    }

    //region Packet Handling / User Interaction
    //Server > Client

    /**
     * @param player Send full synchronization data package to the specified client.
     */
    public void syncClient(ServerPlayerEntity player) {
        PacketCustom output = (PacketCustom) serverPacketProvider.get();
        output.writeByte(0);
        serializeMCD(output);
        output.sendToPlayer(player);
    }

    /**
     * Sets up packet handling that allows this entity filter to send sync packets from the server to clients.
     *
     * @param serverPacketProvider must provide a packet to write the sync data to.
     * @param serverPacketSender   must sent the previously provided packet to all players accessing this filter.
     */
    public <T extends MCDataOutput> void setupServerPacketHandling(Supplier<T> serverPacketProvider, Consumer<PacketCustom> serverPacketSender) {
        this.serverPacketProvider = serverPacketProvider;
        this.serverPacketSender = serverPacketSender;
    }

    /**
     * This is where packets sent by {@link #setupServerPacketHandling(Supplier, Consumer)} must end up. (Client side)
     */
    public void receivePacketFromServer(MCDataInput input) {
        int id = input.readByte();
        if (id == 0) {
            deSerializeMCD(input);
        } else if (id == 1) {
            int nodeID = input.readVarInt();
            FilterBase node = nodeMap.get(nodeID);
            if (node != null) {
                node.deSerializeMCD(input);
            }
        }
    }

    protected void filterChanged() {
        if (serverPacketProvider != null && EffectiveSide.get().isServer()) {
            MCDataOutput output = serverPacketProvider.get();
            output.writeByte(0);
            serializeMCD(output);
            serverPacketSender.accept((PacketCustom) output);
        }
    }

    private void serverModifiedNode(FilterBase node) {
        if (serverPacketProvider != null && EffectiveSide.get().isServer()) {
            MCDataOutput output = serverPacketProvider.get();
            output.writeByte(1);
            output.writeVarInt(node.nodeID);
            node.serializeMCD(output);
            serverPacketSender.accept((PacketCustom) output);
        }
    }

    //Client > Server

    /**
     * Sets up packet handling that allows this entity filter to send packets from a client to the server.
     *
     * @param clientPacketProvider must provide a packet to write the sync data to.
     * @param clientPacketSender   must sent the previously provided packet to the server.
     */
    public <T extends MCDataOutput> void setupClientPacketHandling(Supplier<T> clientPacketProvider, Consumer<PacketCustom> clientPacketSender) {
        this.clientPacketProvider = clientPacketProvider;
        this.clientPacketSender = clientPacketSender;
    }

    /**
     * This is where packets sent by {@link #setupClientPacketHandling(Supplier, Consumer)} must end up. (Server side)
     */
    public void receivePacketFromClient(MCDataInput input) {
        int id = input.readByte();
        if (id == 0) {
            FilterType type = input.readEnum(FilterType.class);
            int parentID = input.readVarInt();
            FilterBase parent = nodeMap.get(parentID);
            if (parent instanceof FilterGroup && nodeMap.size() <= maxFilters) {
                ((FilterGroup) parent).addNode(type.createNode(this));
            }
        } else if (id == 1) {
            int nodeID = input.readVarInt();
            FilterBase node = nodeMap.get(nodeID);
            if (node != null && node != this && node.getParent() != null) {
                node.getParent().removeNode(node);
            }
        } else if (id == 2) {
            int nodeID = input.readVarInt();
            FilterBase node = nodeMap.get(nodeID);
            if (node != null) {
                node.deSerializeMCD(input);
                serverModifiedNode(node);
            }
        } else if (id == 3) {
            nodeMap.clear();
            subNodeMap.clear();
            trackNode(this);
            filterChanged();
        }
        markDirty();
    }

    /**
     * Called by the client to add a node to the specified group.
     */
    public void clientAddNode(FilterType type, FilterGroup parentGroup) {
        if (parentGroup == null) return;
        MCDataOutput output = clientPacketProvider.get();
        output.writeByte(0);
        output.writeEnum(type);
        output.writeVarInt(parentGroup.nodeID);
        clientPacketSender.accept((PacketCustom) output);
    }

    /**
     * Called by the client to remove the specified node
     */
    public void clientRemoveNode(int nodeID) {
        MCDataOutput output = clientPacketProvider.get();
        output.writeByte(1);
        output.writeVarInt(nodeID);
        clientPacketSender.accept((PacketCustom) output);
    }

    public void clientClearFilter() {
        MCDataOutput output = clientPacketProvider.get();
        output.writeByte(3);
        clientPacketSender.accept((PacketCustom) output);
    }

    private void clientModifiedNode(FilterBase node) {
        if (clientPacketProvider != null) {
            MCDataOutput output = clientPacketProvider.get();
            output.writeByte(2);
            output.writeVarInt(node.nodeID);
            node.serializeMCD(output);
            clientPacketSender.accept((PacketCustom) output);
        }
    }

    //endregion

    /**
     * Node modified by unknown side
     */
    public void nodeModified(FilterBase node) {
        if (EffectiveSide.get().isClient()) {
            clientModifiedNode(node);
        } else {
            serverModifiedNode(node);
        }
    }

    /**
     * Designed to work in conjunction with the supplied {@link FilterType} list.
     * This can be used to dynamically disable any of the allowed filters.
     */
    public void setTypePredicate(Predicate<FilterType> typePredicate) {
        this.typePredicate = typePredicate;
    }

    public boolean isFilterAllowed(FilterType type) {
        return allowedFilters.contains(type) && (typePredicate == null || typePredicate.test(type));
    }

    /**
     * Allows you to retrieve the modifiable fixedFilters list.
     * Fixed filters are applied in addition to user filters and can not be modified by the user.
     *
     * @return the modifiable fixedFilters list.
     */
    public List<FilterBase> getFixedFilters() {
        return fixedFilters;
    }

    //region Filtering

    public List<Entity> filterEntities(Collection<Entity> entities) {
        List<Entity> list = new ArrayList<>();
        entities.stream().filter(this::test).forEach(list::add);
        return list;
    }

    public void filterEntityCollection(Collection<Entity> entities) {
        entities.removeIf(entity -> !test(entity));
    }

    public Stream<Entity> toFilteredStream(Collection<Entity> entities) {
        return entities.stream().filter(this::test);
    }

    @Override
    public boolean test(Entity entity) {
        if (livingOnly && !(entity instanceof LivingEntity)) {
            return false;
        }

        boolean fixedPass;
        if (fixedAndLogic) {
            fixedPass = fixedFilters.isEmpty() || fixedFilters.parallelStream().allMatch(node -> node.test(entity));
        } else {
            fixedPass = fixedFilters.isEmpty() || fixedFilters.parallelStream().anyMatch(node -> node.test(entity));
        }
        return super.test(entity) && fixedPass;
    }
    //endregion
//    /**
//     * This has no type because it is the main parent filter object not a filter node.
//     */
//    @Override
//    public FilterType getType() {

//        return null;

//    }


    public com.google.common.base.Predicate<Entity> predicate() {
        return filterPredicate;
    }


    @Override
    public EntityFilter getFilter() {
        return this;
    }

    protected int getNextNodeID() {
        return nextNodeID++;
    }

    public void trackNode(FilterBase node) {
        if (node.nodeID == 0 && node != this) {
            return;
        }
        nodeMap.put(node.nodeID, node);
    }

    public void dropNode(FilterBase node) {
        nodeMap.remove(node.nodeID);
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        output.writeBoolean(fixedAndLogic);
        output.writeVarInt(allowedFilters.size());
        if (!allowedFilters.isEmpty()) {
            allowedFilters.forEach(type -> {
                output.writeByte(type.index);
            });
        }
        // Not sure if i need this yet.
//        output.writeVarShort(fixedFilters.size());
//        if (!fixedFilters.isEmpty()) {
//            fixedFilters.forEach(node -> {
//                output.writeByte(node.getType().index);
//                node.serializeMCD(output);
//            });
//        }

//        Will need to serialize the types and maybe fixed nodes.
//        Bur bees to avoid sending them back from the client to the server...
        super.serializeMCD(output);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        fixedAndLogic = input.readBoolean();
        nodeMap.clear();
        trackNode(this);

        boolean isClient = EffectiveSide.get().isClient();
        if (isClient) {
            allowedFilters.clear();
//            fixedFilters.clear();
        }

        int allowedCount = input.readVarInt();
        for (int i = 0; i < allowedCount; i++) {
            FilterType type = FilterType.filterTypeMap[input.readByte()];
            //Because the client is not allowed to modify the allowed filters.
            if (isClient) {
                allowedFilters.add(type);
            }
        }

//        int fixedCount = input.readVarShort();
//        for (int i = 0; i < fixedCount; i++) {
//            FilterType type = FilterType.filterTypeMap[input.readByte()];
//            if (isClient) {
//                FilterNodeBase filterNode = type.createNode(getFilter());
//                filterNode.deSerializeMCD(input);
//                fixedFilters.add(filterNode);
//            }
//        }
        super.deSerializeMCD(input);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        nodeMap.clear();
        super.deserializeNBT(nbt);
        trackNode(this);
        nextNodeID = nodeMap.keySet().stream().mapToInt(value -> value).max().orElse(0) + 1;
    }

    protected void markDirty() {
        if (dirtyHandler != null) {
            dirtyHandler.run();
        }
    }

    public void setDirtyHandler(Runnable dirtyHandler) {
        this.dirtyHandler = dirtyHandler;
    }

    @Override
    public String getTranslationKey() {
        return "gui.bc.entity_filter";
    }

    public FilterBase getNode(int nodeID) {
        return nodeMap.get(nodeID);
    }
}
