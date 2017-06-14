package com.brandon3055.brandonscore.lib.datamanager;

/**
 * Created by brandon3055 on 12/06/2017.
 * <p>
 * wow so much less clutter than the old SyncableObject!
 */
public abstract class AbstractManagedData implements IManagedData {

    protected String name = "";
    protected int index = 0;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
