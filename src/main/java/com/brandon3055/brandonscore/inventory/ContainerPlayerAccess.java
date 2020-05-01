package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventorySimple;
import com.brandon3055.brandonscore.BCContent;
import com.brandon3055.brandonscore.command.BCUtilCommands.OfflinePlayer;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

import static net.minecraft.inventory.EquipmentSlotType.*;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.WHITE;

/**
 * Created by brandon3055 on 6/23/2018.
 */
public class ContainerPlayerAccess extends Container {

    private static final EquipmentSlotType[] VALID_EQUIPMENT_SLOTS = new EquipmentSlotType[]{HEAD, CHEST, LEGS, FEET};
    public PlayerEntity player;
    public PlayerEntity playerAccess;
    private IInventory targetInventory;
    private MinecraftServer server;
    private int tick = 0;

    //Client Side Constructor
    public ContainerPlayerAccess(int id, PlayerInventory playerInv) {
        super(BCContent.containerPlayerAccess, id);
        this.player = playerInv.player;
        playerAccess = null;
        targetInventory = new InventorySimple(41);
        layoutSlots();
    }

    public ContainerPlayerAccess(int id, PlayerInventory playerInv, PlayerEntity playerAccess, MinecraftServer server) {
        super(BCContent.containerPlayerAccess, id);
        this.player = playerInv.player;
        this.playerAccess = playerAccess;
        targetInventory = playerAccess.inventory;
        this.server = server;
        layoutSlots();
    }

    //    @OnlyIn(Dist.DEDICATED_SERVER)
    @Override
    public void detectAndSendChanges() {
        if (playerAccess != null && !(playerAccess instanceof OfflinePlayer)) {
            if (!playerAccess.isAlive()) {
                player.closeScreen();
                player.sendMessage(new StringTextComponent("Target player died or disconnected.").setStyle(new Style().setColor(RED)).appendSibling(new StringTextComponent("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
                return;
            }
        } else if (playerAccess != null) {
            //noinspection ConstantConditions
            if (server.getPlayerList().getPlayerByUUID(playerAccess.getGameProfile().getId()) != null) {
                player.closeScreen();
                player.sendMessage(new StringTextComponent("Target player is now online.").setStyle(new Style().setColor(RED)).appendSibling(new StringTextComponent("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
                return;
            }
        }

        if (tick++ % 10 == 0 && player instanceof ServerPlayerEntity && playerAccess != null) {
            BCoreNetwork.sendPlayerAccessUIUpdate((ServerPlayerEntity) player, playerAccess);
        }

        super.detectAndSendChanges();
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
            addSlot(new Slot(player.inventory, x, xPos + 21 + 18 * x, yPos + 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(player.inventory, x + y * 9 + 9, xPos + 21 + 18 * x, yPos + y * 18));
            }
        }

        for (int i = 0; i < 4; i++) {
            this.addSlot(new ArmorSlot(player.inventory, 36 + (3 - i), xPos, yPos + i * 19, VALID_EQUIPMENT_SLOTS[i], player));
        }

        this.addSlot(new OffhandSlot(player.inventory, 40, xPos + 186, yPos + 54 + 3));
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }

    private static class ArmorSlot extends Slot {
        private final EquipmentSlotType eSlot;
        private final PlayerEntity aPlayer;

        public ArmorSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, EquipmentSlotType eSlot, PlayerEntity aPlayer) {
            super(inventoryIn, index, xPosition, yPosition);
            this.eSlot = eSlot;
            this.aPlayer = aPlayer;
        }

        public int getSlotStackLimit() {
            return 64;
        }

        public boolean isItemValid(ItemStack stack) {
            return true;//stack.getItem().isValidArmor(stack, eSlot, aPlayer);
        }

        public boolean canTakeStack(PlayerEntity playerIn) {
            ItemStack itemstack = this.getStack();
            return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
        }

        @Nullable
        @OnlyIn(Dist.CLIENT)
        public String getSlotTexture() {
//            return PlayerContainer.ARMOR_SLOT_TEXTURES[eSlot.getIndex()];
            return PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS.toString();//TODO ARMOR_SLOT_TEXTURES[eSlot.getIndex()];
        }
    }

    private static class OffhandSlot extends Slot {
        public OffhandSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Nullable
        @OnlyIn(Dist.CLIENT)
        public String getSlotTexture() {
            return "minecraft:items/empty_armor_slot_shield";
        }
    }
}
