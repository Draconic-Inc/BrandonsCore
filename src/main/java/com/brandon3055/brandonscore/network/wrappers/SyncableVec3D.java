package com.brandon3055.brandonscore.network.wrappers;

import com.brandon3055.brandonscore.BrandonsCore;
import com.brandon3055.brandonscore.blocks.TileBCBase;
import com.brandon3055.brandonscore.lib.Vec3D;
import com.brandon3055.brandonscore.network.PacketSyncableObject;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;

/**
 * Created by brandon3055 on 26/3/2016.
 */
public class SyncableVec3D extends SyncableObject {

    public Vec3D vec;
    private Vec3D lastTickVec;


    public SyncableVec3D(Vec3D vec, boolean syncInTile, boolean syncInContainer, boolean updateOnReceived) {
        super(syncInTile, syncInContainer, updateOnReceived);
        this.vec = vec;
        this.lastTickVec = vec;
    }

    public SyncableVec3D(Vec3D vec, boolean syncInTile, boolean syncInContainer) {
        super(syncInTile, syncInContainer);
        this.vec = vec;
        this.lastTickVec = vec;
    }

    @Override
    public void detectAndSendChanges(TileBCBase tile, EntityPlayer player, boolean forceSync) {
        if (!vec.equals(lastTickVec) || forceSync) {
            lastTickVec = vec.copy();
            tile.dirtyBlock();
            if (player == null) {
                BrandonsCore.network.sendToAllAround(new PacketSyncableObject(tile, index, vec, updateOnReceived), tile.syncRange());
            } else if (player instanceof EntityPlayerMP) {
                BrandonsCore.network.sendTo(new PacketSyncableObject(tile, index, vec, updateOnReceived), (EntityPlayerMP) player);
            } else LogHelperBC.error("SyncableInt#detectAndSendChanges No valid destination for sync packet!");
        }
    }

    @Override
    public void updateReceived(PacketSyncableObject packet) {
        if (packet.dataType == PacketSyncableObject.VEC3D_INDEX) {
            vec = packet.vec3D.copy();
        }
    }

    @Override
    public void toNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagDouble(vec.x));
        list.appendTag(new NBTTagDouble(vec.y));
        list.appendTag(new NBTTagDouble(vec.z));

        compound.setTag("SyncableVec3D" + index, list);
    }

    @Override
    public void fromNBT(NBTTagCompound compound) {
        if (compound.hasKey("SyncableVec3D" + index)) {
            NBTTagList list = compound.getTagList("SyncableVec3D" + index, 6);
            if (list.tagCount() == 3) {
                vec.set(list.getDoubleAt(0), list.getDoubleAt(1), list.getDoubleAt(2));
            }
        }
    }

    @Override
    public String toString() {
        return vec.toString();
    }
}
