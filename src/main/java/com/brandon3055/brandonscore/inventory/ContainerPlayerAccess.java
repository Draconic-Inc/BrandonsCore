package com.brandon3055.brandonscore.inventory;

import codechicken.lib.inventory.InventorySimple;
import com.brandon3055.brandonscore.command.BCUtilCommands.OfflinePlayer;
import com.brandon3055.brandonscore.network.PacketDispatcher;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static net.minecraft.inventory.EntityEquipmentSlot.*;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.WHITE;

/**
 * Created by brandon3055 on 6/23/2018.
 */
public class ContainerPlayerAccess extends Container {

    private static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] {HEAD, CHEST, LEGS, FEET};
    public EntityPlayer player;
    public EntityPlayer playerAccess;
    private IInventory targetInventory;
    private MinecraftServer server;
    private int tick = 0;

    //Client Side Constructor
    public ContainerPlayerAccess(EntityPlayer player) {
        this.player = player;
        playerAccess = null;
        targetInventory = new InventorySimple(41);
        layoutSlots();
    }

    public ContainerPlayerAccess(EntityPlayer player, EntityPlayer playerAccess, MinecraftServer server) {
        this.player = player;
        this.playerAccess = playerAccess;
        targetInventory = playerAccess.inventory;
        this.server = server;
        layoutSlots();
    }

    @SideOnly(Side.SERVER)
    @Override
    public void detectAndSendChanges() {
        if (playerAccess != null && !(playerAccess instanceof OfflinePlayer)) {
            if (playerAccess.isDead) {
                player.closeScreen();
                player.sendMessage(new TextComponentString("Target player died or disconnected.").setStyle(new Style().setColor(RED)).appendSibling(new TextComponentString("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
                return;
            }
        }
        else if (playerAccess != null) {
            //noinspection ConstantConditions
            if (server.getPlayerList().getPlayerByUUID(playerAccess.getGameProfile().getId()) != null) {
                player.closeScreen();
                player.sendMessage(new TextComponentString("Target player is now online.").setStyle(new Style().setColor(RED)).appendSibling(new TextComponentString("\n(run command again to re-establish inventory connection)").setStyle(new Style().setColor(WHITE))));
                return;
            }
        }

        if (tick++ % 10 == 0 && player instanceof EntityPlayerMP && playerAccess != null) {
            PacketDispatcher.sendPlayerAccessUIUpdate((EntityPlayerMP) player, playerAccess);
        }

        super.detectAndSendChanges();
    }

    public void layoutSlots() {
        int xPos = 9;
        int yPos = 21;

        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(targetInventory, x, xPos + 21 + 18 * x, yPos + 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(targetInventory, x + y * 9 + 9, xPos + 21 + 18 * x, yPos + y * 18));
            }
        }

        for (int i = 0; i < 4; i++) {
            this.addSlotToContainer(new ArmorSlot(targetInventory, 36 + (3 - i), xPos, yPos + i * 19, VALID_EQUIPMENT_SLOTS[i], player));
        }

        this.addSlotToContainer(new OffhandSlot(targetInventory, 40, xPos + 186, yPos + 54 + 3));

        xPos = 9;
        yPos = 168;
        for (int x = 0; x < 9; x++) {
            addSlotToContainer(new Slot(player.inventory, x, xPos + 21 + 18 * x, yPos + 54 + 3));
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, xPos + 21 + 18 * x, yPos + y * 18));
            }
        }

        for (int i = 0; i < 4; i++) {
            this.addSlotToContainer(new ArmorSlot(player.inventory, 36 + (3 - i), xPos, yPos + i * 19, VALID_EQUIPMENT_SLOTS[i], player));
        }

        this.addSlotToContainer(new OffhandSlot(player.inventory, 40, xPos + 186, yPos + 54 + 3));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

    private static class ArmorSlot extends Slot {
        private final EntityEquipmentSlot eSlot;
        private final EntityPlayer aPlayer;

        public ArmorSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, EntityEquipmentSlot eSlot, EntityPlayer aPlayer) {
            super(inventoryIn, index, xPosition, yPosition);
            this.eSlot = eSlot;
            this.aPlayer = aPlayer;
        }

        public int getSlotStackLimit()
        {
            return 64;
        }
        public boolean isItemValid(ItemStack stack)
        {
            return true;//stack.getItem().isValidArmor(stack, eSlot, aPlayer);
        }
        public boolean canTakeStack(EntityPlayer playerIn)
        {
            ItemStack itemstack = this.getStack();
            return (itemstack.isEmpty() || playerIn.isCreative() || !EnchantmentHelper.hasBindingCurse(itemstack)) && super.canTakeStack(playerIn);
        }
        @Nullable
        @SideOnly(Side.CLIENT)
        public String getSlotTexture()
        {
            return ItemArmor.EMPTY_SLOT_NAMES[eSlot.getIndex()];
        }
    }

    private static class OffhandSlot extends Slot {
        public OffhandSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Nullable
        @SideOnly(Side.CLIENT)
        public String getSlotTexture()
        {
            return "minecraft:items/empty_armor_slot_shield";
        }
    }
}
