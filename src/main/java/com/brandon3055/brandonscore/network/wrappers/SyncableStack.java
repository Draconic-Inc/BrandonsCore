package com.brandon3055.brandonscore.network.wrappers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Created by brandon3055 on 26/3/2016.
 * Will finish this class if i ever find a use for it
 */
public class SyncableStack extends SyncableObject {

	public ItemStack value;
	private ItemStack lastTickValue;

    public SyncableStack(ItemStack stack, boolean syncInTile, boolean syncInContainer) {
        super(syncInTile, syncInContainer);
        this.value = this.lastTickValue = stack;
    }

    @Override
    public void detectAndSendChanges(TileBCBase tile, EntityPlayer player, boolean forceSync) {
        //TODO check each tag and only sent tags that have changed.
		if (!ItemStack.areItemStacksEqual(value, lastTickValue)) {
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
		if (packet.dataType == PacketSyncableObject.STACK_INDEX){
			value = lastTickValue = packet.stack;
		}
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        if (value != null) {
            NBTTagCompound stackCompound = new NBTTagCompound();
            value.writeToNBT(stackCompound);
            compound.setTag("SyncableStack" + index, stackCompound);
        }
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        if (compound.hasKey("SyncableStack" + index)) {
            value = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("SyncableStack" + index));
        }
    }



}
