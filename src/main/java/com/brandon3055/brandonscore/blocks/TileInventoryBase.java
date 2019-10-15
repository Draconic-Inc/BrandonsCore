package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

/**
 * Created by brandon3055 on 26/3/2016.
 * The base class for all inventory tiles
 */
@Deprecated
public class TileInventoryBase extends TileBCBase implements IInventory {

    protected NonNullList<ItemStack> inventoryStacks = NonNullList.create();
    protected int stackLimit = 64;

    protected IItemHandler itemHandler;
    {
        itemHandler = new InvWrapper(this);
    }

    protected SidedInvWrapper[] sidedHandlers = new SidedInvWrapper[6];

    public TileInventoryBase() {
        if (this instanceof ISidedInventory) {
            for (EnumFacing facing : EnumFacing.values()) {
                sidedHandlers[facing.getIndex()] = new SidedInvWrapper((ISidedInventory) this, facing);
            }
        }
    }

    protected void setInventorySize(int size) {
        inventoryStacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    //region IInventory

    @Override
    public int getSizeInventory() {
        return inventoryStacks.size();
    }

    @Override
    public boolean isEmpty() {
        return DataUtils.count(inventoryStacks, (stack -> !stack.isEmpty())) <= 0;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryStacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack;

        if (index >= 0 && index < inventoryStacks.size() && !inventoryStacks.get(index).isEmpty() && count > 0) {
            stack = inventoryStacks.get(index).splitStack(count);
        }
        else {
            stack = ItemStack.EMPTY;
        }

        markDirty();

        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack;
        if (index >= 0 && index < inventoryStacks.size()) {
            stack = inventoryStacks.set(index, ItemStack.EMPTY);
        }
        else {
            stack = ItemStack.EMPTY;
        }

        markDirty();

        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryStacks.set(index, stack);

        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }

        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return stackLimit;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (world == null) {
            return false;
        }
        else if (world.getTileEntity(pos) != this) {
            return false;
        }
        return player.getDistanceSq(pos.add(0.5, 0.5, 0.5)) < 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventoryStacks.clear();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    //endregion

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    protected void writeInventoryToNBT(NBTTagCompound compound) {
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < inventoryStacks.size(); ++i) {
            ItemStack itemstack = inventoryStacks.get(i);

            if (!itemstack.isEmpty()) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                if (inventoryStacks.size() > 255) {
                    nbttagcompound.setShort("Slot", (short) i);
                }
                else {
                    nbttagcompound.setByte("Slot", (byte) i);
                }
                itemstack.writeToNBT(nbttagcompound);
                nbttaglist.appendTag(nbttagcompound);
            }
        }

        if (!nbttaglist.hasNoTags()) {
            compound.setTag("Items", nbttaglist);
        }
    }

    protected void readInventoryFromNBT(NBTTagCompound compound) {
        inventoryStacks.clear();
        NBTTagList nbttaglist = compound.getTagList("Items", 10);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            int j;
            if (inventoryStacks.size() > 255) {
                j = nbttagcompound.getShort("Slot");
            }
            else {
                j = nbttagcompound.getByte("Slot") & 255;
            }

            if (j >= 0 && j < inventoryStacks.size()) {
                inventoryStacks.set(j, new ItemStack(nbttagcompound));
            }
        }
    }

    @Override
    public void writeExtraNBT(NBTTagCompound compound) {
        super.writeExtraNBT(compound);
        writeInventoryToNBT(compound);
    }

    @Override
    public void readExtraNBT(NBTTagCompound compound) {
        super.readExtraNBT(compound);
        readInventoryFromNBT(compound);
    }

    @Override
    public void writeToItemStack(NBTTagCompound compound, boolean willHarvest) {
        super.writeToItemStack(compound, willHarvest);
        if (!isEmpty()){
            writeInventoryToNBT(compound);
        }
    }

    @Override
    public void readFromItemStack(NBTTagCompound compound) {
        super.readFromItemStack(compound);
        readInventoryFromNBT(compound);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return getItemHandler(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    protected <T> T getItemHandler(Capability<T> capability, EnumFacing facing) {
        if (this instanceof ISidedInventory && facing != null) { //TODO Not sure if i want to return a handler for null face?
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(sidedHandlers[facing.getIndex()]);
        }
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
    }
}
