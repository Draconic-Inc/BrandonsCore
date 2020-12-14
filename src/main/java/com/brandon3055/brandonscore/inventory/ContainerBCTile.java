package com.brandon3055.brandonscore.inventory;

import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.inventory.ContainerSlotLayout.LayoutFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 28/3/2016.
 * Base class for all containers. Handles syncing on syncable objects inside an attached TileBCBase.
 */
public class ContainerBCTile<T extends TileBCore> extends ContainerBCore<T> {

    /**
     * A reference to the attached tile. This may be null if the container is not attached to a tile
     */
    public T tile;

    public ContainerBCTile(@Nullable ContainerType<?> type, int windowId, PlayerInventory playerInv, PacketBuffer extraData) {
        super(type, windowId, playerInv, extraData);
        this.tile = getClientTile(extraData);
    }

    public ContainerBCTile(@Nullable ContainerType<?> type, int windowId, PlayerInventory player, PacketBuffer extraData, LayoutFactory<T> factory) {
        super(type, windowId, player, extraData, factory);
        this.tile = getClientTile(extraData);
        this.buildSlotLayout();
    }

    public ContainerBCTile(@Nullable ContainerType<?> type, int windowId, PlayerInventory player, T tile) {
        super(type, windowId, player);
        this.tile = tile;
        this.tile.onPlayerOpenContainer(player.player);
    }

    public ContainerBCTile(@Nullable ContainerType<?> type, int windowId, PlayerInventory player, T tile, LayoutFactory<T> factory) {
        super(type, windowId, player, factory);
        this.tile = tile;
        this.tile.onPlayerOpenContainer(player.player);
        this.buildSlotLayout();
    }

    @Override
    protected void buildSlotLayout() {
        if (tile != null){
            this.slotLayout = factory.buildLayout(player, tile).retrieveSlotsForContainer(this::addSlot);
        }
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        tile.onPlayerCloseContainer(playerIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        tile.detectAndSendChangesToListeners(listeners);
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        if (listener instanceof ServerPlayerEntity) {
            tile.getDataManager().forcePlayerSync((ServerPlayerEntity) listener);
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        if (tile.getWorld().getTileEntity(tile.getPos()) != tile) {
            return false;
        } else {
            return player.getDistanceSq((double) tile.getPos().getX() + 0.5D, (double) tile.getPos().getY() + 0.5D, (double) tile.getPos().getZ() + 0.5D) <= tile.getAccessDistanceSq();
        }
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int i) {
        int playerSlots = 36;
        if (slotLayout != null) {
            playerSlots = slotLayout.getPlayerSlotCount();
        }
        LazyOptional<IItemHandler> optional = getItemHandler();
        if (optional.isPresent()) {
            IItemHandler handler = optional.orElse(EmptyHandler.INSTANCE);
            Slot slot = getSlot(i);

            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                ItemStack result = stack.copy();

                //Transferring from tile to player
                if (i >= playerSlots) {
                    if (!mergeItemStack(stack, 0, playerSlots, false)) {
                        return ItemStack.EMPTY; //Return if failed to merge
                    }
                } else {
                    //Transferring from player to tile
                    if (!mergeItemStack(stack, playerSlots, playerSlots + handler.getSlots(), false)) {
                        return ItemStack.EMPTY;  //Return if failed to merge
                    }
                }

                if (stack.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
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
        return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
    }

    public ContainerSlotLayout getSlotLayout() {
        return slotLayout;
    }

    protected static <T extends TileEntity> T getClientTile(PacketBuffer extraData) {
        return (T) Minecraft.getInstance().world.getTileEntity(extraData.readBlockPos());
    }
}
