package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.utils.BCLogHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Created by brandon3055 on 7/06/2016.
 * Used to store a reference to a specific slot in a players inventory.
 * The field for the inventory which the item is located in should be set to the slot index.
 * The other two fields should be set to -1.
 */
public class PlayerSlot {//todo Maby move to API? Depends if this gets used in anything API related

    private int slot;
    private EnumInvCategory category;

    public PlayerSlot(int slot, EnumInvCategory category) {
        this.slot = slot;
        this.category = category;
    }

    public void toBuff(ByteBuf buf) {
        buf.writeByte(category.getIndex());
        buf.writeByte(slot);
    }

    public static PlayerSlot fromBuff(ByteBuf buf) {
        EnumInvCategory category = EnumInvCategory.fromIndex(buf.readByte());
        int slot = buf.readByte();
        return new PlayerSlot(slot, category);
    }

    public ItemStack getStackInSlot(EntityPlayer player) {
        ItemStack[] stacks;

        if (category == EnumInvCategory.ARMOR){
            stacks = player.inventory.armorInventory;
        }
        else if (category == EnumInvCategory.MAIN){
            stacks = player.inventory.mainInventory;
        }
        else if (category == EnumInvCategory.OFF_HAND){
            stacks = player.inventory.offHandInventory;
        }
        else {
            BCLogHelper.bigError("PlayerSlot#getStackInSlot Invalid or null category! This should not be possible! [%s]... Fix your Shit!", category);
            return null;
        }

        if (slot < 0 || slot >= stacks.length){
            BCLogHelper.bigError("PlayerSlot#getStackInSlot Hay! I just saved you an index out of bounds! Be grateful and fix your shit!");
            return null;
        }

        return stacks[slot];
    }

    public enum EnumInvCategory {
        MAIN(0),
        ARMOR(1),
        OFF_HAND(2);
        private int index;
        private static EnumInvCategory[] indexMap = new EnumInvCategory[3];

        private EnumInvCategory(int index){
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static EnumInvCategory fromIndex(int index){
            if (index > 2 || index < 0){
                BCLogHelper.bigError("PlayerSlot.EnumInvCategory#fromIndex Attempt to read invalid index! [%s]", index);
                return indexMap[0];
            }
            return indexMap[index];
        }

        static {
            indexMap[0] = MAIN;
            indexMap[1] = ARMOR;
            indexMap[2] = OFF_HAND;
        }
    }
}
