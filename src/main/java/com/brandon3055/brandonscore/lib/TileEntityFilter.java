package com.brandon3055.brandonscore.lib;

import com.brandon3055.brandonscore.blocks.TileBCore;
import net.minecraft.nbt.CompoundTag;

/**
 * Created by brandon3055 on 23/10/2016.
 * A simple implementation of EntityFilter for use by tile entities.
 * Remember to call EntityFilter.receiveConfigFromClient on the filter packetID in TileBCBase.receivePacketFromClient
 */
public class TileEntityFilter extends EntityFilter {

    public byte packetID;
    private TileBCore tile;
    public boolean isListEnabled;
    public boolean isTypeSelectionEnabled;
    public boolean isOtherSelectorEnabled;

    public TileEntityFilter(TileBCore tile, byte packetID) {
        this.tile = tile;
        this.packetID = packetID;
    }

    @Override
    public void sendConfigToServer(CompoundTag compound) {
        tile.sendPacketToServer(output -> output.writeCompoundNBT(compound), packetID);
    }

    @Override
    public void receiveConfigFromClient(CompoundTag compound) {
        super.receiveConfigFromClient(compound);
        tile.updateBlock();
        tile.setChanged();
    }

    @Override
    public boolean isListEnabled() {
        return isListEnabled;
    }

    @Override
    public boolean isTypeSelectionEnabled() {
        return isTypeSelectionEnabled;
    }

    @Override
    public boolean isOtherSelectorEnabled() {
        return isOtherSelectorEnabled;
    }
}
