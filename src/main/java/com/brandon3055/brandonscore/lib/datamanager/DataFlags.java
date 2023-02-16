package com.brandon3055.brandonscore.lib.datamanager;

import com.brandon3055.brandonscore.inventory.ContainerBCore;
import com.google.common.annotations.Beta;

/**
 * Created by brandon3055 on 7/7/19.
 * I only ever see this system being used on tiles so the documentation will reflect that.
 * But it could theoretically be used in other situations.
 */
public class DataFlags {

    public static DataFlags NONE = new DataFlags(false, false, false, false, false, false, false, false);
    /**
     * Save this data to the tile's NBT.
     */
    public static DataFlags SAVE_NBT = new DataFlags(true, false, false, false, false, false, false, false);

    /**
     * Save this data to dropped item when tile is harvested.
     */
    public static DataFlags SAVE_ITEM = new DataFlags(false, true, false, false, false, false, false, false);

    /**
     * Sync this data via the tile (usually via onUpdate).
     * This should be used for data that needs to always be synced to the client.
     */
    public static DataFlags SYNC_TILE = new DataFlags(false, false, true, false, false, false, false, false);

    /**
     * Sync this data via container.
     * This will only sync the data when the client is accessing a {@link ContainerBCore} linked to this tile.
     * Useful if data is only needed inside a GUI.
     * TODO I think this will have issues if multiple players have a container open. I need to look into this if its still an issue in 1.14+
     */
    public static DataFlags SYNC_CONTAINER = new DataFlags(false, false, false, true, false, false, false, false);

    /**
     * If set a client side block update will be triggered when this data is modified.
     */
    public static DataFlags TRIGGER_UPDATE = new DataFlags(false, false, false, false, true, false, false, false);

    /**
     * If this flag is set then a client sync will be triggered when the data value is set.
     * In most situations this would not be needed but there may be some edge cases where this could be useful.
     */
    public static DataFlags SYNC_ON_SET = new DataFlags(false, false, false, false, false, true, false, false);

    /**
     * If set this flag will allow changes to be pushed from the client to the server. This is useful for things like gui controls <strong>BUT<strong/>
     * It should be used with caution! Do not use this on a field you dont want the player to modify because it would only take a simple client hack to exploit this.
     * This should also add a dataValidator to ensure the server has the last say if the client tries to sent a value outside your defined range.
     *
     * Also note if this is enabled then setting the value client side will not immediately update the client side data. Instead the data is just sent to the server where
     * once validated it should be re synced to the client vie your chosen synchronization method. This is to ensure the client side value always matches the server side value.
     * If you do not want this behavior then call setCCSCS on the data. This will cause the client side value to be set immediately but the value should still be synced
     * from the server if nothing breaks.
     *
     * For Security reasons this will only work on tiles that have an associated container, and that container must be open by the player.
     */
    @Beta //Potentially dangerous! Read documentation
    public static DataFlags CLIENT_CONTROL = new DataFlags(false, false, false, false, false, false, true, false);
    //The following are combinations of the above flags.
    public static DataFlags SAVE_BOTH = new DataFlags(SAVE_NBT, SAVE_ITEM);
    public static DataFlags SAVE_NBT_SYNC_TILE = new DataFlags(SAVE_NBT, SYNC_TILE);
    public static DataFlags SAVE_NBT_SYNC_CONTAINER = new DataFlags(SAVE_NBT, SYNC_CONTAINER);
    public static DataFlags SAVE_BOTH_SYNC_TILE = new DataFlags(SAVE_BOTH, SYNC_TILE);
    public static DataFlags SAVE_BOTH_SYNC_CONTAINER = new DataFlags(SAVE_BOTH, SYNC_CONTAINER);

    /**
     * If this flag is specified the host will not be marked dirty when the data value is changed.
     */
    @Deprecated //Meh changed my mind but this may still be useful
    public static DataFlags DONT_DIRTY = new DataFlags(false, false, false, false, false, false, false, true);


    public final boolean saveNBT;
    public final boolean saveItem;
    public final boolean syncTile;
    public final boolean syncContainer;
    public final boolean triggerUpdate;
    public final boolean syncOnSet;
    public final boolean allowClientControl;
    public final boolean dontMark;

    DataFlags(boolean saveNBT, boolean saveItem, boolean syncTile, boolean syncContainer, boolean triggerUpdate, boolean syncOnSet, boolean allowClientControl, boolean dontMark) {
        this.saveNBT = saveNBT;
        this.saveItem = saveItem;
        this.syncTile = syncTile;
        this.syncContainer = syncContainer;
        this.triggerUpdate = triggerUpdate;
        this.syncOnSet = syncOnSet;
        this.allowClientControl = allowClientControl;
        this.dontMark = dontMark;
    }

    DataFlags(DataFlags[] combine) {
        this(NONE, combine);
    }

    DataFlags(DataFlags base, DataFlags... combine) {
        boolean saveNBT = base.saveNBT;
        boolean saveItem = base.saveItem;
        boolean syncTile = base.syncTile;
        boolean syncContainer = base.syncContainer;
        boolean triggerUpdate = base.triggerUpdate;
        boolean syncOnSet = base.syncOnSet;
        boolean allowClientControl = base.allowClientControl;
        boolean dontMark = base.dontMark;
        for (DataFlags flag : combine) {
            saveNBT |= flag.saveNBT;
            saveItem |= flag.saveItem;
            syncTile |= flag.syncTile;
            syncContainer |= flag.syncContainer;
            triggerUpdate |= flag.triggerUpdate;
            syncOnSet |= flag.syncOnSet;
            allowClientControl |= flag.allowClientControl;
            dontMark |= flag.dontMark;
        }
        this.saveNBT = saveNBT;
        this.saveItem = saveItem;
        this.syncTile = syncTile;
        this.syncContainer = syncContainer;
        this.triggerUpdate = triggerUpdate;
        this.syncOnSet = syncOnSet;
        this.allowClientControl = allowClientControl;
        this.dontMark = dontMark;
    }

    public boolean syncViaPacket() {
        return syncTile || syncContainer;
    }
}
