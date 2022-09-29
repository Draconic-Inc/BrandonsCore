package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by brandon3055 on 12/06/2017.
 */
public class ManagedPos extends AbstractManagedData<BlockPos> {

    private BlockPos value;
    private BlockPos defaultValue;
    protected Function<BlockPos, BlockPos> validator = null;

    public ManagedPos(String name, @Nullable BlockPos defaultValue, DataFlags... flags) {
        super(name, flags);
        this.value = defaultValue;
        this.defaultValue = defaultValue == null ? null : new BlockPos(defaultValue);
    }

    /**
     * Default 0, 0, 0
     */
    public ManagedPos(String name, DataFlags... flags) {
        this(name, BlockPos.ZERO, flags);
    }

    public BlockPos set(BlockPos value) {
        if (!Objects.equals(this.value, value)) {
            boolean set = true;
            BlockPos prev = this.value;
            this.value = value;

            if (dataManager.isClientSide() && flags.allowClientControl) {
                dataManager.sendToServer(this);
                set = ccscsFlag;
            }

            if (set) {
                markDirty();
                notifyListeners(value);
            } else {
                this.value = prev;
            }
        }

        return this.value;
    }

    @Nullable
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
    public void toBytes(MCDataOutput output) {
        output.writeBoolean(value != null);
        if (value != null) {
            output.writePos(value);
        }
    }

    @Override
    public void fromBytes(MCDataInput input) {
        if (input.readBoolean()) {
            value = input.readPos();
        } else {
            value = null;
        }
        notifyListeners(value);
    }

    @Override
    public void toNBT(CompoundTag compound) {
        CompoundTag nbt = value == null ? new CompoundTag() : NbtUtils.writeBlockPos(value);
        if (value == null) {
            nbt.putBoolean("null", true);
        }
        compound.put(name, nbt);
    }

    @Override
    public void fromNBT(CompoundTag compound) {
        if (!compound.contains(name, 10)) {
            value = defaultValue == null ? null : new BlockPos(defaultValue);
        }else {
            CompoundTag nbt = compound.getCompound(name);
            if (nbt.contains("null")) {
                value = null;
            } else {
                value = NbtUtils.readBlockPos(nbt);
            }
        }
        notifyListeners(value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "=" + value + "]";
    }

    public boolean isNull() {
        return value == null;
    }

    public boolean notNull() {
        return value != null;
    }
}
