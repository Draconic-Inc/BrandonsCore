package com.brandon3055.brandonscore.lib.datamanager;

public class TileDataOptions<D> {
    public boolean saveToNBT = false;
    public boolean saveToItem = false;
    public boolean syncViaTile = false;
    public boolean syncViaContainer = false;
    public boolean triggerUpdate = false;
    public D managedData;

    public TileDataOptions(D managedData) {
        this.managedData = managedData;
    }

    /**
     * Call to have the data saved to the tile's nbt.
     */
    public TileDataOptions<D> saveToNBT() {
        saveToNBT = true;
        return this;
    }

    /**
     * Call to have the data saved to the item when the tile is broken.
     */
    public TileDataOptions<D> saveToItem() {
        saveToItem = true;
        return this;
    }

    /**
     * Call to have the data automatically synchronized with all clients in range of the tile.
     */
    public TileDataOptions<D> syncViaTile() {
        syncViaTile = true;
        return this;
    }

    /**
     * Call to have the data synchronized by {@link com.brandon3055.brandonscore.inventory.ContainerBCBase} that is attacked to this tile.
     * if you are using syncViaTile this is redundant and should not be called.
     */
    public TileDataOptions<D> syncViaContainer() {
        syncViaContainer = true;
        return this;
    }

    /**
     * Call to have an block update triggered when received by the client.
     */
    public TileDataOptions<D> trigerUpdate() {
        this.triggerUpdate = true;
        return this;
    }

    /**
     * Call after setting flags to return the ManagedData object.
     */
    public D finish() {
        return managedData;
    }
}