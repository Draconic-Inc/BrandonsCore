package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.lib.Vec3I;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedPos extends AbstractManagedData<BlockPos> {

    private BlockPos value;
    private BlockPos lastValue;
    protected Function<BlockPos, BlockPos> validator = null;

    public ManagedPos(String name, BlockPos defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.lastValue = new BlockPos(value);
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedPos(String name, DataFlags... flags) {
        this(name, BlockPos.ZERO, flags);
    }

    public BlockPos set(BlockPos value) {
        if (!this.value.equals(value)) {
            boolean set = true;
            BlockPos prev = this.value;
            this.value = value;

            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                lastValue = new BlockPos(prev);
                markDirty();
                notifyListeners(value);
            }
            else {
                this.value = prev;
            }
        }

        return this.value;
    }

    public BlockPos get() {
        return value;
    }

    /**
     * Use to validate new values. Use this to enforce any restrictions such as min/max then return the corrected value.
     *
     * @param validator a validator function that takes an input, applies restrictions if needed then returns the updated value.
     * @return
     */
    public ManagedPos setValidator(Function<BlockPos, BlockPos> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public void validate() {
        if (validator != null) {
            value = validator.apply(value);
        }
    }

    @Override
    public boolean isDirty(boolean reset) {
        if (lastValue != null && !lastValue.equals(value)) {
            if (reset) {
                lastValue = new BlockPos(value);
            }
            return true;
        }

        return super.isDirty(reset);
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writePos(value);

    }

    @Override
    public void fromBytes(MCDataInput input) {
        value = input.readPos();
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundNBT compound) {
        compound.put(name, NBTUtil.writeBlockPos(value));
    }

    @Override
    public void fromNBT(CompoundNBT compound) {
        value = NBTUtil.readBlockPos(compound.getCompound(name));
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "="+ value + "]";
    }
}
