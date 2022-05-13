package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Locale;

/**
 * Created by brandon3055 on 7/11/19.
 */
public abstract class FilterBase implements INBTSerializable<CompoundTag>, IMCDataSerializable {
    private final EntityFilter filter;
    protected int nodeID = -1;
    private FilterGroup parent;

    public FilterBase(EntityFilter filter) {
        this.filter = filter;
    }

    /**
     * If this node's condition is met it will then and only then check its child nodes.
     * This means the match type only applies when testing child nodes.
     *
     * @param entity The entity to apply this filter to.
     * @return true if the entity matches this filter and its sub nodes.
     * True meaning this entity can be collected, killed etc.
     */
    public abstract boolean test(Entity entity);

    /**
     * This is the internal test method that each filter node type must implement.
     */

    public int getNodeId() {
        return nodeID;
    }

    protected void initNode(FilterGroup parent) {
        this.parent = parent;
        this.nodeID = getFilter().getNextNodeID();
        getFilter().trackNode(this);
    }

    public void onLoaded(FilterGroup parent) {
        this.parent = parent;
    }

    public FilterGroup getParent() {
        return parent;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("node_id", nodeID);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag compound) {
        nodeID = compound.getInt("node_id");
        getFilter().trackNode(this);
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        output.writeVarInt(nodeID);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        nodeID = input.readVarInt();
        getFilter().trackNode(this);
    }

    public abstract FilterType getType();

    public EntityFilter getFilter() {
        return filter;
    }

    public String getTranslationKey() {
        return "mod_gui.brandonscore.entity_filter." + getType().name().toLowerCase(Locale.ENGLISH);
    }
}
