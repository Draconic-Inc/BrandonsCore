package com.brandon3055.brandonscore.lib.datamanager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 12/06/2017.
 * <p>
 * wow so much less clutter than the old SyncableObject!
 */
public abstract class AbstractManagedData<T> implements IManagedData {
    private boolean isDirty = true;

    protected int index = 0;
    protected String name = "";
    protected DataFlags flags = DataFlags.NONE;
    protected IDataManager dataManager;
    /**
     * @see #setCCSCS()
     * */
    protected boolean ccscsFlag = false; //clientControlSetClientSide
    protected List<Consumer<T>> valueListeners = new ArrayList<>();

    public AbstractManagedData(String name, /*T defaultValue,*/ DataFlags... flags) {
        this.name = name;
        if (flags.length > 0) {
            this.flags = flags.length == 1 ? flags[0] : new DataFlags(flags);
        }
    }

    @Override
    public void init(IDataManager dataManager, int index) {
        if (dataManager.getDataByName(getName()) != null) {
            throw new IllegalStateException("Attempted to register managed data object with duplicate name!");
        }
        this.dataManager = dataManager;
        this.index = index;
    }

    public void addFlags(DataFlags... newFlags) {
        flags = new DataFlags(flags, newFlags);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void markDirty() {
        validate();
        isDirty = true;
        if (flags.saveNBT && !flags.dontMark){
            dataManager.markDirty();
        }
    }

    @Override
    public boolean isDirty(boolean reset) {
        boolean ret = isDirty;
        if (reset) {
            isDirty = false;
        }
        return ret;
    }

    @Override
    public IDataManager getDataManager() {
        return dataManager;
    }

    @Override
    public DataFlags flags() {
        return flags;
    }

    /**
     * Applies when using the client control flag to set the value from the client side.
     * By default, the client sends a packet to the server, then once the new value is set server side
     * that new value is sent back to the client where it is then applied.
     * This is the most reliable method as it means the server is still driving the client side value but it can introduce a delay client side.
     *
     * Setting this flag to true will force the client to apply the new value immediately before waiting for confirmation from the server.
     * */
    public void setCCSCS() {
        this.ccscsFlag = true;
    }

    /**
     * No matter what a value listener will always have the current value stored in this object.
     * The value listener is updated when any of the following actions occur.
     *
     * Client Side: when value syncs from client to server.
     * Server Side: when value syncs from server to client.
     * Server side: when value is loaded from nbt.
     *
     * @param listener A value listener
     */
    public void addValueListener(Consumer<T> listener) {
        valueListeners.add(listener);
    }

    public void removeValueListener(Consumer<T> listener) {
        valueListeners.remove(listener);
    }

    protected void notifyListeners(T newValue) {
        valueListeners.forEach(listener -> listener.accept(newValue));
    }
}
