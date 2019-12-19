package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.lib.IValueHashable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by brandon3055 on 18/12/19.
 */
class SerializationFlags<D extends INBTSerializable<CompoundNBT>> {
    protected final String tagName;
    protected final D serializableInstance;
    protected Object lastData;

    protected boolean saveTile = true;
    protected boolean saveItem = true;
    protected boolean syncTile = false;
    protected boolean syncContainer = false;

    public SerializationFlags(String tagName, D serializableInstance) {
        this.tagName = tagName;
        this.serializableInstance = serializableInstance;

        if (serializableInstance instanceof IValueHashable) {
            lastData = ((IValueHashable) serializableInstance).getValueHash();
            syncContainer = true;
        } else {
            lastData = serializableInstance.serializeNBT();
        }
    }

    public D getData() {
        return serializableInstance;
    }

    /**
     * Default: true
     */
    public SerializationFlags<D> saveItem(boolean saveItem) {
        this.saveItem = saveItem;
        return this;
    }

    /**
     * Default: true
     */
    public SerializationFlags<D> saveTile(boolean saveTile) {
        this.saveTile = saveTile;
        return this;
    }

    /**
     * Default: false
     * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
     */
    public SerializationFlags<D> syncTile(boolean syncTile) {
        this.syncTile = syncTile;
        return this;
    }

    /**
     * Defaults to true for any serializable that also implements {@link IValueHashable}
     * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
     */
    public SerializationFlags<D> syncContainer(boolean syncContainer) {
        this.syncContainer = syncContainer;
        return this;
    }

    protected boolean hasChanged(boolean reset) {
        if (serializableInstance instanceof IValueHashable) {
            if (!((IValueHashable) serializableInstance).checkValueHash(lastData)) {
                if (reset) {
                    lastData = ((IValueHashable) serializableInstance).getValueHash();
                }
                return true;
            }
        } else {
            if (!serializableInstance.serializeNBT().equals(lastData)) {
                if (reset) {
                    lastData = serializableInstance.serializeNBT();
                }
                return true;
            }
        }

        return false;
    }
}
