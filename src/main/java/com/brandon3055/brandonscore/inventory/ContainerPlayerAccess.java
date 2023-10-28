package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventorySimple;
import com.brandon3055.brandonscore.BCContent;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import static net.minecraft.world.entity.EquipmentSlot.*;

/**
 * Created by brandon3055 on 6/23/2018.
 */
public class ContainerPlayerAccess extends AbstractContainerMenu {

    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[]{HEAD, CHEST, LEGS, FEET};
    public Player player;
    public Player playerAccess;
    private Container targetInventory;
    private MinecraftServer server;
    private int tick = 0;

    //Client Side Constructor
    public ContainerPlayerAccess(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(BCContent.containerPlayerAccess, windowId);
        this.player = playerInv.player;
        playerAccess = null;
        targetInventory = new InventorySimple(41);
        layoutSlots();
    }

    public ContainerPlayerAccess(int id, Inventory playerInv, Player playerAccess, MinecraftServer server) {
        super(BCContent.containerPlayerAccess, id);
        this.player = playerInv.player;
        this.playerAccess = playerAccess;
        targetInventory = playerAccess.getInventory();
        this.server = server;
        layoutSlots();
    }

    //    @OnlyIn(Dist.DEDICATED_SERVER)
    @Override
    public void broadcastChanges() {
//        if (playerAccess != null && !(playerAccess instanceof OfflinePlayer)) {
//            if (!playerAccess.isAlive()) {
//                player.closeScreen();
//                player.sendSystemMessage(new StringTextComponent("Target player died or disconnected.").setStyle(new Style().setColor(RED)).appendSibling(new StringTextComponent("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
//                return;
//            }
//        } else if (playerAccess != null) {
//            //noinspection ConstantConditions
//            if (server.getPlayerList().getPlayerByUUID(playerAccess.getGameProfile().getId()) != null) {
//                player.closeScreen();
//                player.sendSystemMessage(new StringTextComponent("Target player is now online.").setStyle(new Style().setColor(RED)).appendSibling(new StringTextComponent("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
//                return;
//            }
//        }

        if (tick++ % 10 == 0 && player instanceof ServerPlayer && playerAccess != null) {
            BCoreNetwork.sendPlayerAccessUIUpdate((ServerPlayer) player, playerAccess);
        }

        super.broadcastChanges();
    }

    public void layoutSlots() {
        int xPos = 9;
        int yPos = 21;

        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(targetInventory, x, xPos + 21 + 18 * x, yPos + 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(targetInventory, x + y * 9 + 9, xPos + 21 + 18 * x, yPos + y * 18));
            }
        }

        for (int i = 0; i < 4; i++) {
            this.addSlot(new ArmorSlot(targetInventory, 36 + (3 - i), xPos, yPos + i * 19, VALID_EQUIPMENT_SLOTS[i], player));
        }

        this.addSlot(new OffhandSlot(targetInventory, 40, xPos + 186, yPos + 54 + 3));

        xPos = 9;
        yPos = 168;
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(player.getInventory(), x, xPos + 21 + 18 * x, yPos + 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(player.getInventory(), x + y * 9 + 9, xPos + 21 + 18 * x, yPos + y * 18));
            }
        }

        for (int i = 0; i < 4; i++) {
            this.addSlot(new ArmorSlot(player.getInventory(), 36 + (3 - i), xPos, yPos + i * 19, VALID_EQUIPMENT_SLOTS[i], player));
        }

        this.addSlot(new OffhandSlot(player.getInventory(), 40, xPos + 186, yPos + 54 + 3));
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    private static class ArmorSlot extends Slot {
        private final EquipmentSlot eSlot;
        private final Player aPlayer;

        public ArmorSlot(Container inventoryIn, int index, int xPosition, int yPosition, EquipmentSlot eSlot, Player aPlayer) {
            super(inventoryIn, index, xPosition, yPosition);
            this.eSlot = eSlot;
            this.aPlayer = aPlayer;
        }

        public int getMaxStackSize() {
            return 64;
        }

        public boolean mayPlace(ItemStack stack) {
            return true;//stack.getItem().isValidArmor(stack, eSlot, aPlayer);
        }

        public boolean mayPickup(Player playerIn) {
            ItemStack itemstack = this.getItem();
            return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.mayPickup(playerIn);
        }

        @Nullable
        @OnlyIn(Dist.CLIENT)
        public String getSlotTexture() {
//            return PlayerContainer.ARMOR_SLOT_TEXTURES[eSlot.getIndex()];
            return InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS.toString();//TODO ARMOR_SLOT_TEXTURES[eSlot.getIndex()];
        }
    }

    private static class OffhandSlot extends Slot {
        public OffhandSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Nullable
        @OnlyIn(Dist.CLIENT)
        public String getSlotTexture() {
            return "minecraft:items/empty_armor_slot_shield";
        }
    }
}
