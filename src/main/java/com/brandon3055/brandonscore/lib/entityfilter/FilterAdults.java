package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.nbt.CompoundNBT;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterAdults extends FilterBase {

    protected boolean whitelistAdults = true;
    protected boolean includeNonAgeable = true;

    public FilterAdults(EntityFilter filter) {
        super(filter);
    }

    public void setWhitelistAdults(boolean whitelistAdults) {
        boolean prev = this.whitelistAdults;
        this.whitelistAdults = whitelistAdults;
        getFilter().nodeModified(this);
        this.whitelistAdults = prev;
    }

    public void setIncludeNonAgeable(boolean includeNonAgeable) {
        boolean prev = this.includeNonAgeable;
        this.includeNonAgeable = includeNonAgeable;
        getFilter().nodeModified(this);
        this.includeNonAgeable = prev;
    }

    public boolean isWhitelistAdults() {
        return whitelistAdults;
    }

    public boolean isIncludeNonAgeable() {
        return includeNonAgeable;
    }

    @Override
    public boolean test(Entity entity) {
        boolean isAgeable = entity instanceof EntityAgeable;
        if (isAgeable) {
            EntityAgeable ageable = (EntityAgeable) entity;
            return whitelistAdults == !ageable.isChild();
        }
        return includeNonAgeable;
    }

    @Override
    public FilterType getType() {
        return FilterType.ADULTS;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.setBoolean("include", whitelistAdults);
        compound.setBoolean("tamable", includeNonAgeable);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        whitelistAdults = nbt.getBoolean("include");
        includeNonAgeable = nbt.getBoolean("tamable");
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistAdults);
        output.writeBoolean(includeNonAgeable);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistAdults = input.readBoolean();
        includeNonAgeable = input.readBoolean();
    }
}
