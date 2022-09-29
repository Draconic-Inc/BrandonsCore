package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static com.brandon3055.brandonscore.BrandonsCore.equipmentManager;

/**
 * Created by brandon3055 on 7/06/2016.
 * Used to store a reference to a specific slot in a players inventory.
 * The slot field corresponds to the index of the item within the sub inventory for the given category.
 */
public class PlayerSlot {

    private int slot;
    private EnumInvCategory category;

    public PlayerSlot(int slot, EnumInvCategory category) {
        this.slot = slot;
        this.category = category;
    }

    public PlayerSlot(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            this.slot = 0;
            this.category = EnumInvCategory.OFF_HAND;
        } else {
            this.slot = player.getInventory().selected;
            this.category = EnumInvCategory.MAIN;
        }
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

    public int getSlotIndex() {
        return slot;
    }

    public int getCatIndex() {
        return category.getIndex();
    }

    public static PlayerSlot fromIndexes(int slotIndex, int catIndex) {
        EnumInvCategory category = EnumInvCategory.fromIndex(catIndex);
        return new PlayerSlot(slotIndex, category);
    }

    @Override
    public String toString() {
        return category.getIndex() + ":" + slot;
    }

    public static PlayerSlot fromString(String slot) {
        try {
            return new PlayerSlot(Integer.parseInt(slot.substring(slot.indexOf(":") + 1)), EnumInvCategory.fromIndex(Integer.parseInt(slot.substring(0, slot.indexOf(":")))));
        }
        catch (Exception e) {
            LogHelperBC.error("Error loading slot reference from string! - " + slot);
            LogHelperBC.error("Required format \"inventory:slot\" Where inventory ether 0 (main), 1 (Armor) or 2 (Off Hand) and slot is the index in that inventory.");
            e.printStackTrace();
            return new PlayerSlot(0, EnumInvCategory.MAIN);
        }
    }

    public void setStackInSlot(Player player, ItemStack stack) {
        if (category == EnumInvCategory.ARMOR) {
            if (slot < 0 || slot >= player.getInventory().armor.size()) {
                LogHelperBC.error("PlayerSlot: Could not insert into the specified slot because the specified slot does not exist! Slot: " + slot + ", Inventory: " + category + ", Stack: " + stack);
                return;
            }
            player.getInventory().armor.set(slot, stack);
        } else if (category == EnumInvCategory.MAIN) {
            if (slot < 0 || slot >= player.getInventory().items.size()) {
                LogHelperBC.error("PlayerSlot: Could not insert into the specified slot because the specified slot does not exist! Slot: " + slot + ", Inventory: " + category + ", Stack: " + stack);
                return;
            }
            player.getInventory().items.set(slot, stack);
        } else if (category == EnumInvCategory.OFF_HAND) {
            if (slot < 0 || slot >= player.getInventory().offhand.size()) {
                LogHelperBC.error("PlayerSlot: Could not insert into the specified slot because the specified slot does not exist! Slot: " + slot + ", Inventory: " + category + ", Stack: " + stack);
                return;
            }
            player.getInventory().offhand.set(slot, stack);
        } else if (category == EnumInvCategory.EQUIPMENT && equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(player);
            if (optional.isPresent()) {
                IItemHandlerModifiable handler = optional.orElseThrow(IllegalStateException::new);
                if (slot < 0 || slot >= handler.getSlots()) {
                    LogHelperBC.error("PlayerSlot: Could not insert into the specified slot because the specified slot does not exist! Slot: " + slot + ", Inventory: " + category + ", Stack: " + stack);
                    return;
                }
                handler.setStackInSlot(slot, stack);
            }
        }
    }

    public static PlayerSlot findStack(Inventory inv, Predicate<ItemStack> check) {
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.items.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.MAIN);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack stack = inv.armor.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.ARMOR);
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack stack = inv.offhand.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.OFF_HAND);
            }
        }
        if (equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(inv.player);
            if (optional.isPresent()) {
                IItemHandlerModifiable handler = optional.orElseThrow(IllegalStateException::new);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && check.test(stack)) {
                        return new PlayerSlot(i, EnumInvCategory.EQUIPMENT);
                    }
                }
            }
        }
        return null;
    }

    public static PlayerSlot findStackActiveFirst(Inventory inv, Predicate<ItemStack> check) {
        if (!inv.getSelected().isEmpty() && check.test(inv.getSelected())) {
            return new PlayerSlot(inv.selected, EnumInvCategory.MAIN);
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack stack = inv.offhand.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.OFF_HAND);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            ItemStack stack = inv.armor.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.ARMOR);
            }
        }
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.items.get(i);
            if (!stack.isEmpty() && check.test(stack)) {
                return new PlayerSlot(i, EnumInvCategory.MAIN);
            }
        }
        if (equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(inv.player);
            if (optional.isPresent()) {
                IItemHandlerModifiable handler = optional.orElseThrow(IllegalStateException::new);
                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty() && check.test(stack)) {
                        return new PlayerSlot(i, EnumInvCategory.EQUIPMENT);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public EquipmentSlot getEquipmentSlot() {
        if (category == EnumInvCategory.ARMOR) {
            switch (slot) {
                case 0:
                    return EquipmentSlot.FEET;
                case 1:
                    return EquipmentSlot.LEGS;
                case 2:
                    return EquipmentSlot.CHEST;
                case 3:
                    return EquipmentSlot.HEAD;
            }
        } else if (category == EnumInvCategory.OFF_HAND) {
            return EquipmentSlot.OFFHAND;
        }
        return null;
    }

    public ItemStack getStackInSlot(Player player) {
        if (category == EnumInvCategory.ARMOR) {
            return player.getInventory().armor.get(slot);
        } else if (category == EnumInvCategory.MAIN) {
            return player.getInventory().items.get(slot);
        } else if (category == EnumInvCategory.OFF_HAND) {
            return player.getInventory().offhand.get(slot);
        } else if (category == EnumInvCategory.EQUIPMENT && equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(player);
            if (optional.isPresent()) {
                IItemHandlerModifiable handler = optional.orElseThrow(IllegalStateException::new);
                return handler.getSlots() > slot ? handler.getStackInSlot(slot) : ItemStack.EMPTY;
            }

            return ItemStack.EMPTY;
        } else {
            LogHelperBC.bigError("PlayerSlot#getStackInSlot Invalid or null category! This should not be possible! [%s]... Fix your Shit!", category);
            return ItemStack.EMPTY;
        }
    }

    public enum EnumInvCategory {
        MAIN(0),
        ARMOR(1),
        OFF_HAND(2),
        EQUIPMENT(3);
        private int index;
        private static EnumInvCategory[] indexMap = new EnumInvCategory[4];

        private EnumInvCategory(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static EnumInvCategory fromIndex(int index) {
            if (index > 3 || index < 0) {
                LogHelperBC.bigError("PlayerSlot.EnumInvCategory#fromIndex Attempt to read invalid index! [%s]", index);
                return indexMap[0];
            }
            return indexMap[index];
        }

        static {
            indexMap[0] = MAIN;
            indexMap[1] = ARMOR;
            indexMap[2] = OFF_HAND;
            indexMap[3] = EQUIPMENT;
        }
    }
}
