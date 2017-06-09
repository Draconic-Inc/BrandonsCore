package com.brandon3055.brandonscore.network.wrappers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 26/3/2016.
 * Will finish this class if i ever find a use for it
 */
public class SyncableNBT extends SyncableObject {

	public NBTTagCompound value;
	private NBTTagCompound lastTickValue;

    public SyncableNBT(NBTTagCompound value, boolean syncInTile, boolean syncInContainer) {
        super(syncInTile, syncInContainer);
        this.value = this.lastTickValue = value;
    }

    @Override
    public void detectAndSendChanges(TileBCBase tile, EntityPlayer player, boolean forceSync) {
        //TODO check each tag and only sent tags that have changed.
		if (!lastTickValue.equals(value)) {
			lastTickValue = value;
			if (tile != null) {
				BrandonsCore.network.sendToAllAround(new PacketSyncableObject(tile, index, value, updateOnReceived), tile.syncRange());
			}
			else if (player instanceof EntityPlayerMP) {
				BrandonsCore.network.sendTo(new PacketSyncableObject(null, index, value, updateOnReceived), (EntityPlayerMP)player);
			}
			else LogHelperBC.error("SyncableNBT#detectAndSendChanges No valid destination for sync packet!");
		}
    }

    @Override
    public void updateReceived(PacketSyncableObject packet) {
		if (packet.dataType == PacketSyncableObject.TAG_INDEX){
			value = packet.compound;
		}
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        compound.setTag("SyncableNBT" + index, value);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        if (compound.hasKey("SyncableNBT" + index)) {
            value = compound.getCompoundTag("SyncableNBT" + index);
        }
    }



}
