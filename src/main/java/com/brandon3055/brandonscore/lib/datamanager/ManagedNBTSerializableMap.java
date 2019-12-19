package com.brandon3055.brandonscore.lib.datamanager;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 08/10/2019.
 * This is only used for synchronization purposes though it could theoretically be used to save values as well.
 */
@Deprecated //Avoid if possible
public class ManagedNBTSerializableMap extends AbstractManagedData {

    private Map<String, INBTSerializable<CompoundNBT>> valueMap;
    private Map<String, INBT> lastValueMap;

    public ManagedNBTSerializableMap(String name, Map<String, INBTSerializable<CompoundNBT>> serializableMap, DataFlags... flags) {
        super(name, flags);
        this.valueMap = serializableMap;
        lastValueMap = new HashMap<>();
        serializableMap.forEach((key, value) -> lastValueMap.put(key, value.serializeNBT()));
    }

    public Map<String, INBTSerializable<CompoundNBT>> get() {
        return valueMap;
    }

    @Override
    public void validate() {}

    @Override
    public boolean isDirty(boolean reset) {
        if (lastValueMap != null && (lastValueMap.size() != valueMap.size() || (valueMap.entrySet().stream().anyMatch(entry -> {
            INBT base = lastValueMap.get(entry.getKey());
            return base == null || !(base.equals(entry.getValue().serializeNBT()));
        })))) {
            if (reset) {
                lastValueMap.clear();
                valueMap.forEach((key, value) -> lastValueMap.put(key, value.serializeNBT()));
            }
            return true;
        }

        return super.isDirty(reset);
    }

    @Override
    public void toBytes(MCDataOutput output) {
        output.writeVarInt(valueMap.size());
        valueMap.forEach((name, serializable) -> output.writeString(name).writeNBTTagCompound(serializable.serializeNBT()));
    }

    @Override
    public void fromBytes(MCDataInput input) {
        int c = input.readVarInt();
        for (int i = 0; i < c; i++) {
            String name = input.readString();
            CompoundNBT nbt = input.readNBTTagCompound();
            if (valueMap.containsKey(name)) {
                valueMap.get(name).deserializeNBT(nbt);
            }
        }
        lastValueMap.clear();
        valueMap.forEach((key, value) -> lastValueMap.put(key, value.serializeNBT()));
    }

    @Override
    public void toNBT(CompoundNBT compound) {
        CompoundNBT tags = new CompoundNBT();
        valueMap.forEach((name, serializable) -> tags.setTag(name, serializable.serializeNBT()));
        compound.setTag(name, tags);
    }

    @Override
    public void fromNBT(CompoundNBT compound) {
        CompoundNBT tags = compound.getCompoundTag(name);
        for (String name : new ArrayList<>(valueMap.keySet())) {
            if (tags.hasKey(name)) {
                valueMap.get(name).deserializeNBT(tags.getCompoundTag(name));
            }
        }
        lastValueMap.clear();
        valueMap.forEach((key, value) -> lastValueMap.put(key, value.serializeNBT()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":[" + getName() + "=" + valueMap + "]";
    }
}
