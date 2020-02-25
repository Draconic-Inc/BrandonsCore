package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.lib.IMCDataSerializable;
import com.brandon3055.brandonscore.lib.IValueHashable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by brandon3055 on 18/12/19.
 */
public class SerializationFlags<D extends INBTSerializable<CompoundNBT>> {
    protected final String tagName;
    protected final D serializableInstance;
    protected Object lastData;

    protected boolean saveTile = false;
    protected boolean saveItem = false;
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
     * Enable saving to item when broken.
     */
    public SerializationFlags<D> saveItem() {
        this.saveItem = true;
        return this;
    }

    /**
     * Enable saving to tile data.
     */
    public SerializationFlags<D> saveTile() {
        this.saveTile = true;
        return this;
    }

    /**
     * Enable saving to both item and tile data.
     * */
    public SerializationFlags<D> saveBoth() {
        saveItem();
        saveTile();
        return this;
    }

    /**
     * Enable syncing via tile entity.
     * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
     */
    public SerializationFlags<D> syncTile() {
        this.syncTile = true;
        return this;
    }

    /**
     * Enable syncing via container.
     * Is automatically enabled for any serializable that also implements {@link IValueHashable}
     * You can also implement {@link IMCDataSerializable} on your data instance to improve sync efficiency.
     */
    public SerializationFlags<D> syncContainer() {
        this.syncContainer = true;
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
