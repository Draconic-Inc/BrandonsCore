package com.brandon3055.brandonscore.lib.entityfilter;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

/**
 * Created by brandon3055 on 7/11/19.
 */
public class FilterTamed extends FilterBase {

    protected boolean whitelistTamed = true;
    protected boolean includeTamable = true;

    public FilterTamed(EntityFilter filter) {
        super(filter);
    }

    public void setIncludeTamable(boolean includeTamable) {
        boolean prev = this.includeTamable;
        this.includeTamable = includeTamable;
        getFilter().nodeModified(this);
        this.includeTamable = prev;
    }

    public void setWhitelistTamed(boolean whitelistTamed) {
        boolean prev = this.whitelistTamed;
        this.whitelistTamed = whitelistTamed;
        getFilter().nodeModified(this);
        this.whitelistTamed = prev;
    }

    public boolean isIncludeTamable() {
        return includeTamable;
    }

    public boolean isWhitelistTamed() {
        return whitelistTamed;
    }

    @Override
    public boolean test(Entity entity) {
        if (entity instanceof Player) return false;
        boolean isTamable = entity instanceof TamableAnimal;
        if (isTamable) {
            TamableAnimal ownable = (TamableAnimal) entity;
            if (ownable.getOwnerUUID() != null) {
                return whitelistTamed;
            }
            return includeTamable;
        }
        return !whitelistTamed;
    }

    @Override
    public FilterType getType() {
        return FilterType.TAMED;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag compound = super.serializeNBT();
        compound.putBoolean("include", whitelistTamed);
        compound.putBoolean("tamable", includeTamable);
        return compound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        whitelistTamed = nbt.getBoolean("include");
        includeTamable = nbt.getBoolean("tamable");
    }

    @Override
    public void serializeMCD(MCDataOutput output) {
        super.serializeMCD(output);
        output.writeBoolean(whitelistTamed);
        output.writeBoolean(includeTamable);
    }

    @Override
    public void deSerializeMCD(MCDataInput input) {
        super.deSerializeMCD(input);
        whitelistTamed = input.readBoolean();
        includeTamable = input.readBoolean();
    }
}
