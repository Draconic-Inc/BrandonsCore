package com.brandon3055.brandonscore.inventory;

import codechicken.lib.data.MCDataInput;
import codechicken.lib.inventory.container.modular.ModularGuiContainerMenu;
import codechicken.lib.packet.PacketCustom;
import com.brandon3055.brandonscore.blocks.TileBCore;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * Created by brandon3055 on 28/3/2016.
 * Base class for all containers. Handles syncing on syncable objects inside an attached TileBCBase.
 */
public abstract class ContainerBCTile<T extends TileBCore> extends ModularGuiContainerMenu {

    /**
     * A reference to the attached tile. This may be null if the container is not attached to a tile
     */
    public T tile;
    public Player player;

    public ContainerBCTile(@Nullable MenuType<?> type, int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(type, windowId, playerInv);
        this.player = playerInv.player;
        this.tile = getClientTile(playerInv, extraData);
    }

    public ContainerBCTile(@Nullable MenuType<?> type, int windowId, Inventory playerInv, T tile) {
        super(type, windowId, playerInv);
        this.player = playerInv.player;
        this.tile = tile;
        this.tile.onPlayerOpenContainer(playerInv.player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        tile.onPlayerCloseContainer(player);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        tile.detectAndSendChanges(true);
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        if (listener instanceof ServerPlayer) {
            tile.getDataManager().forcePlayerSync((ServerPlayer) listener);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (tile.getLevel().getBlockEntity(tile.getBlockPos()) != tile) {
            return false;
        } else {
            return player.distanceToSqr((double) tile.getBlockPos().getX() + 0.5D, (double) tile.getBlockPos().getY() + 0.5D, (double) tile.getBlockPos().getZ() + 0.5D) <= tile.getAccessDistanceSq();
        }
    }

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

    //The following are some safety checks to handle conditions vanilla normally does not have to deal with.

    @Override
    public Slot getSlot(int slotId) {
        if (slotId < slots.size() && slotId >= 0) {
            return slots.get(slotId);
        }
        return null;
    }

    /**
     * @return the item handler for the tile entity.
     */
    @Deprecated
    public LazyOptional<IItemHandler> getItemHandler() {
        return tile.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
    }

    protected static <T extends BlockEntity> T getClientTile(Inventory playerInv, FriendlyByteBuf extraData) {
        return (T) playerInv.player.level().getBlockEntity(extraData.readBlockPos());
    }

    public PacketCustom createServerBoundPacket(int packetType) {
        PacketCustom packet = new PacketCustom(BCoreNetwork.CHANNEL, packetType);
        packet.writeInt(containerId);
        return packet;
    }

    public void handleContainerMessage(PacketCustom packet, ServerPlayer player) {
        int containerId = packet.readInt();
        if (containerId != this.containerId) return;
        int packetID = packet.readByte();
        tile.receivePacketFromClient(packet, player, packetID);
    }

    public void handleTileDataPacket(PacketCustom packet, ServerPlayer player) {
        int containerId = packet.readInt();
        if (containerId != this.containerId) return;
        tile.getDataManager().receiveDataFromClient(packet);
    }
}
