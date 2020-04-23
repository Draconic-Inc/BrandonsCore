package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.inventory.ContainerSlotLayout.LayoutFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
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
public class ContainerBCore<D> extends Container {

    protected PlayerEntity player;
    protected LayoutFactory<D> factory;
    protected ContainerSlotLayout slotLayout;

    public ContainerBCore(@Nullable ContainerType<?> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(type, windowId);
        this.player = playerInv.player;
    }

    public ContainerBCore(@Nullable ContainerType<?> type, int windowId, PlayerInventory player, PacketBuffer extraData, LayoutFactory<D> factory) {
        this(type, windowId, player, extraData);
        this.factory = factory;
        this.buildSlotLayout();
    }

    public ContainerBCore(@Nullable ContainerType<?> type, int windowId, PlayerInventory player) {
        super(type, windowId);
        this.player = player.player;
    }

    public ContainerBCore(@Nullable ContainerType<?> type, int windowId, PlayerInventory player, LayoutFactory<D> factory) {
        this(type, windowId, player);
        this.player = player.player;
        this.factory = factory;
        this.buildSlotLayout();
    }

    protected void buildSlotLayout() {
        this.slotLayout = factory.buildLayout(player, null).retrieveSlotsForContainer(this::addSlot);
    }

    public ContainerBCore addPlayerSlots(int posX, int posY) {
        return addPlayerSlots(posX, posY, 4);
    }

    public ContainerBCore addPlayerSlots(int posX, int posY, int hotbarSpacing) {
        for (int x = 0; x < 9; x++) {
            addSlot(new SlotCheckValid.IInv(player.inventory, x, posX + 18 * x, posY + 54 + hotbarSpacing));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new SlotCheckValid.IInv(player.inventory, x + y * 9 + 9, posX + 18 * x, posY + y * 18));
            }
        }
        return this;
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int i) {
        LazyOptional<IItemHandler> optional = getItemHandler();
        if (optional.isPresent()) {
            IItemHandler handler = optional.orElse(EmptyHandler.INSTANCE);
            Slot slot = getSlot(i);

            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                ItemStack result = stack.copy();

                //Transferring from tile to player
                if (i >= 36) {
                    if (!mergeItemStack(stack, 0, 36, false)) {
                        return ItemStack.EMPTY; //Return if failed to merge
                    }
                }
                else {
                    //Transferring from player to tile
                    if (!mergeItemStack(stack, 36, 36 + handler.getSlots(), false)) {
                        return ItemStack.EMPTY;  //Return if failed to merge
                    }
                }

                if (stack.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                }
                else {
                    slot.onSlotChanged();
                }

                slot.onTake(player, stack);

                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    //The following are some safety checks to handle conditions vanilla normally does not have to deal with.

    @Override
    public void putStackInSlot(int slotID, ItemStack stack) {
        Slot slot = this.getSlot(slotID);
        if (slot != null) {
            slot.putStack(stack);
        }
    }

    @Override
    public Slot getSlot(int slotId) {
        if (slotId < inventorySlots.size() && slotId >= 0) {
            return inventorySlots.get(slotId);
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setAll(List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); ++i) {
            Slot slot = getSlot(i);
            if (slot != null) {
                slot.putStack(stacks.get(i));
            }
        }
    }

    /**
     * @return the item handler for the tile entity.
     */
    public LazyOptional<IItemHandler> getItemHandler() {
        return LazyOptional.empty();
    }

    public ContainerSlotLayout getSlotLayout() {
        return slotLayout;
    }
}
