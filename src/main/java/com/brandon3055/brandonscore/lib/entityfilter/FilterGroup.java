package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.Map;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterGroup extends FilterBase {
    protected Int2ObjectMap<FilterBase> subNodeMap = new Int2ObjectLinkedOpenHashMap<>(); //because i want these to keep their order of addition.
    /**
     * If true than an entity must match all of the filters in this group.
     * If false than an entity only needs to match one or more filters in this group.
     */
    protected boolean andGroup = false;

    public FilterGroup(EntityFilter filter) {
        super(filter);
    }

    protected void addNode(FilterBase node) {
        if (EffectiveSide.get().isClient()) {
            throw new IllegalStateException("Filter notes can be edited client side but must be added and removed server side!");
        }

        if (getFilter().isFilterAllowed(node.getType())) {
            node.initNode(this);
            subNodeMap.put(node.nodeID, node);
            getFilter().filterChanged();
        }
    }

    protected void removeNode(FilterBase node) {
        if (EffectiveSide.get().isClient()) {
            throw new IllegalStateException("Filter notes can be edited client side but must be added and removed server side!");
        }

        if (node instanceof FilterGroup) {
            ((FilterGroup) node).getSubNodeMap().values().forEach(((FilterGroup) node)::removeNode);
        }

        subNodeMap.remove(node.nodeID);
        getFilter().dropNode(node);
        getFilter().filterChanged();
    }

    public void setAndGroup(boolean andGroup) {
        boolean prev = this.andGroup;
        this.andGroup = andGroup;
        getFilter().nodeModified(this);
        this.andGroup = prev;
    }

    public boolean isAndGroup() {
        return andGroup;
    }

    public Map<Integer, FilterBase> getSubNodeMap() {
        return subNodeMap;
    }

    @Override
    public boolean test(Entity entity) {
        if (andGroup) {
            return subNodeMap.isEmpty() || subNodeMap.values().stream().allMatch(node -> node.test(entity));
        } else {
            return subNodeMap.isEmpty() || subNodeMap.values().stream().anyMatch(node -> node.test(entity));
        }
    }

    @Override
    public FilterType getType() {
        return FilterType.FILTER_GROUP;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();
        compound.putBoolean("and_group", andGroup);
        if (!subNodeMap.isEmpty()) {
            ListTag subs = new ListTag();
            subNodeMap.values().forEach(node -> {
                CompoundTag tag = node.serializeNBT();
                tag.putByte("filter_type", (byte) node.getType().index);
                subs.add(tag);
            });
            compound.put("sub_nodes", subs);
        }
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        andGroup = nbt.getBoolean("and_group");
        subNodeMap.clear();
        if (nbt.contains("sub_nodes")) {
            ListTag tagList = nbt.getList("sub_nodes", 10);
            for (Tag tag : tagList) {
                FilterType type = FilterType.filterTypeMap[((CompoundTag) tag).getByte("filter_type")];
                FilterBase node = type.createNode(getFilter());
                node.onLoaded(this);
                node.deserializeNBT((CompoundTag) tag);
                if (node.nodeID == 0 && !(node instanceof EntityFilter)) {
                    LogHelperBC.warn("EntityFilter: Skipping broken filter node");
                    continue; //Something broke and this node lost its id so it has to go!
                }
                subNodeMap.put(node.nodeID, node);
            }
        }
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(andGroup);
        output.writeVarInt(subNodeMap.size());
        if (!subNodeMap.isEmpty()) {
            subNodeMap.values().forEach(node -> {
                output.writeByte(node.getType().index);
                node.serializeMCD(output);
            });
        }
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        andGroup = input.readBoolean();
        subNodeMap.clear();
        int subCount = input.readVarInt();
        for (int i = 0; i < subCount; i++) {
            FilterType type = FilterType.filterTypeMap[input.readByte()];
            if (getFilter().isFilterAllowed(type)) {
                FilterBase node = type.createNode(getFilter());
                node.onLoaded(this);
                node.deSerializeMCD(input);
                if (node.nodeID == 0 && !(node instanceof EntityFilter)) {
                    continue; //Something broke and this node lost its id so it has to go!
                }
                subNodeMap.put(node.nodeID, node);
            }
        }
    }
}
