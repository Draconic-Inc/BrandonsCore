package com.brandon3055.brandonscore.lib.datamanager;

/**
 * Created by brandon3055 on 12/06/2017.
 * <p>
 * wow so much less clutter than the old SyncableObject!
 */
public abstract class AbstractManagedData implements IManagedData {
    private boolean isDirty = true;

    protected int index = 0;
    protected String name = "";
    protected DataFlags flags = DataFlags.NONE;
    protected IDataManager dataManager;
    protected boolean ccscsFlag = false; //clientControlSetClientSide

    public AbstractManagedData(String name, /*T defaultValue,*/ DataFlags... flags) {
        this.name = name;
        if (flags.length > 0) {
            this.flags = flags.length == 1 ? flags[0] : new DataFlags(flags);
        }
    }

    @Override
    public void init(IDataManager dataManager, int index) {
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
        isDirty = true;
        dataManager.markDirty();
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

    public void setCCSCS() {
        this.ccscsFlag = true;
    }
}
