package com.brandon3055.brandonscore.inventory;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.container.modular.ModularGuiContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 28/3/2016.
 * Base class for all containers. Handles syncing on syncable objects inside an attached TileBCBase.
 */
@Deprecated //Probably dont need this. Can just use ModularGuiContainerMenu
public class ContainerBCore extends ModularGuiContainerMenu {

    protected Player player;

    public ContainerBCore(@Nullable MenuType<?> type, int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(type, containerId, playerInv);
        this.player = playerInv.player;
    }

    public ContainerBCore(@Nullable MenuType<?> type, int containerId, Inventory playerInv) {
        super(type, containerId, playerInv);
        this.player = playerInv.player;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

//    protected void buildSlotLayout() {
////        this.slotLayout = factory.buildLayout(player, null).retrieveSlotsForContainer(this::addSlot);
//    }
//
//    public ContainerBCore addPlayerSlots(int posX, int posY) {
//        return addPlayerSlots(posX, posY, 4);
//    }
//
//    public ContainerBCore addPlayerSlots(int posX, int posY, int hotbarSpacing) {
//        for (int x = 0; x < 9; x++) {
//            addSlot(new SlotCheckValid.IInv(player.getInventory(), x, posX + 18 * x, posY + 54 + hotbarSpacing));
//        }
//
//        for (int y = 0; y < 3; y++) {
//            for (int x = 0; x < 9; x++) {
//                addSlot(new SlotCheckValid.IInv(player.getInventory(), x + y * 9 + 9, posX + 18 * x, posY + y * 18));
//            }
//        }
//        return this;
//    }
//
//    @Override
//    public void addSlotListener(ContainerListener listener) {
//        super.addSlotListener(listener);
//    }
//
//    //Note to self: This is called from a loop. As long as this does not return an empty stack the loop will continue.
//    //Returning an empty stack essentially indicates that no more items can be transferred.
//    @Override
//    public ItemStack quickMoveStack(Player player, int i) {
//        int playerSlots = 36;
////        if (slotLayout != null) {
////            playerSlots = slotLayout.getPlayerSlotCount();
////        }
//        LazyOptional<IItemHandler> optional = getItemHandler();
//        if (optional.isPresent()) {
//            IItemHandler handler = optional.orElse(EmptyHandler.INSTANCE);
//            Slot slot = getSlot(i);
//
//            if (slot != null && slot.hasItem()) {
//                ItemStack stack = slot.getItem();
//                ItemStack result = stack.copy();
//
//                //Transferring from tile to player
//                if (i >= playerSlots) {
//                    if (!moveItemStackTo(stack, 0, playerSlots, false)) {
//                        return ItemStack.EMPTY; //Return if failed to merge
//                    }
//                } else {
//                    //Transferring from player to tile
//                    if (!moveItemStackTo(stack, playerSlots, playerSlots + handler.getSlots(), false)) {
//                        return ItemStack.EMPTY;  //Return if failed to merge
//                    }
//                }
//
//                if (stack.getCount() == 0) {
//                    slot.set(ItemStack.EMPTY);
//                } else {
//                    slot.setChanged();
//                }
//
//                slot.onTake(player, stack);
//
//                return result;
//            }
//        }
//        return ItemStack.EMPTY;
//    }
//
//    //The following are some safety checks to handle conditions vanilla normally does not have to deal with.
//
//    @Override
//    public void setItem(int slotID, int stateId, ItemStack stack) {
//        Slot slot = this.getSlot(slotID);
//        if (slot != null) {
//            slot.set(stack);
//        }
//        this.stateId = stateId;
//    }
//
//    @Override
//    public Slot getSlot(int slotId) {
//        if (slotId < slots.size() && slotId >= 0) {
//            return slots.get(slotId);
//        }
//        return null;
//    }
//
//    @Override
//    public void initializeContents(int stateId, List<ItemStack> stacks, ItemStack carried) {
//        for (int i = 0; i < stacks.size(); ++i) {
//            Slot slot = getSlot(i);
//            if (slot != null) {
//                slot.set(stacks.get(i));
//            }
//        }
//
//        this.carried = carried;
//        this.stateId = stateId;
//    }
//
//    /**
//     * @return the item handler for the tile entity.
//     */
//    public LazyOptional<IItemHandler> getItemHandler() {
//        return LazyOptional.empty();
//    }
//
////    public ContainerSlotLayout getSlotLayout() {
////        return slotLayout;
////    }
//
//    @OnlyIn(Dist.CLIENT)
//    public void clientTick() {}
}

