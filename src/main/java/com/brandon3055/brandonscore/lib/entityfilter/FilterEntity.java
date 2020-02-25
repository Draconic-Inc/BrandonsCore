package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterEntity extends FilterBase {

    protected boolean whitelistEntity = true;
    protected String entityName = "";

    public FilterEntity(EntityFilter filter) {
        super(filter);
    }

    public void setWhitelistEntity(boolean whitelistEntity) {
        boolean prev = this.whitelistEntity;
        this.whitelistEntity = whitelistEntity;
        getFilter().nodeModified(this);
        this.whitelistEntity = prev;
    }

    public void setEntityName(String entityName) {
//        String prev = this.entityName;//This needs to be set real time to avoid breaking the text field
        this.entityName = entityName;
        getFilter().nodeModified(this);
//        this.entityName = prev;
    }

    public boolean isWhitelistEntity() {
        return whitelistEntity;
    }

    public String getEntityName() {
        return entityName;
    }

    @Override
    public boolean test(Entity entity) {
        ResourceLocation res = entity.getType().getRegistryName();
        if (entityName.isEmpty() || res == null) {
            return !whitelistEntity;
        }
        String name = entityName.contains(":") ? entityName : "minecraft:" + entityName;
        return res.toString().equals(name) == whitelistEntity;
    }

    @Override
    public FilterType getType() {
        return FilterType.ENTITY_TYPE;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.putBoolean("include", whitelistEntity);
        compound.putString("name", entityName);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        whitelistEntity = nbt.getBoolean("include");
        entityName = nbt.getString("name");
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistEntity);
        output.writeString(entityName);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistEntity = input.readBoolean();
        entityName = input.readString();
    }
}
