package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.nbt.CompoundNBT;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterHostile extends FilterBase {

    protected boolean whitelistHostile = true;

    public FilterHostile(EntityFilter filter) {
        super(filter);
    }

    public void setWhitelistHostile(boolean whitelistHostile) {
        boolean prev = this.whitelistHostile;
        this.whitelistHostile = whitelistHostile;
        getFilter().nodeModified(this);
        this.whitelistHostile = prev;
    }

    public boolean isWhitelistHostile() {
        return whitelistHostile;
    }

    @Override
    public boolean test(Entity entity) {
        boolean isHostile = entity instanceof IMob;
        return isHostile == whitelistHostile;
    }

    @Override
    public FilterType getType() {
        return FilterType.HOSTILE;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = super.serializeNBT();
        compound.putBoolean("include", whitelistHostile);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        whitelistHostile = nbt.getBoolean("include");
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistHostile);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistHostile = input.readBoolean();
    }
}
