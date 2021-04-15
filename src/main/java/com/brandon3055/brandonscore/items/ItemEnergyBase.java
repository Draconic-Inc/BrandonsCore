package com.brandon3055.brandonscore.items;


import com.brandon3055.brandonscore.api.power.IOPStorage;
import com.brandon3055.brandonscore.capability.OPMultiProvider;
import com.brandon3055.brandonscore.utils.InfoHelper;
import com.brandon3055.brandonscore.utils.ItemNBTHelper;
import com.brandon3055.brandonscore.utils.MathUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 31/05/2016.
 * The base RF item for Brandon's Core
 */
@Deprecated
public class ItemEnergyBase extends ItemBCore {

    private long capacity;
    private long receive;
    private long extract;

    public ItemEnergyBase(Properties properties) {
        super(properties);
    }

    //region Item

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            items.add(new ItemStack(this));
            ItemStack stack = new ItemStack(this);
            setEnergy(stack, getCapacity(stack));
            items.add(stack);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

//endregion

    //region Energy

    /**
     * Set the Capacity, Receive and Extract stats for the item
     * */
    public void setEnergyStats(long capacity, long receive, long extract) {
        this.capacity = capacity;
        this.receive = receive;
        this.extract = extract;
    }

    /**
     * Returns the items RF capacity.
     * Overriding will override the capacity set by setEnergyStats
     * */
    protected long getCapacity(ItemStack stack) {
        return capacity;
    }

    /**
     * Returns the items max receive.
     * Overriding will override the receive set by setEnergyStats
     * */
    protected long getMaxReceive(ItemStack stack) {
        return receive;
    }

    /**
     * Returns the items max extract.
     * Overriding will override the extract set by setEnergyStats
     * */
    protected long getMaxExtract(ItemStack stack) {
        return extract;
    }

    protected long receiveEnergy(ItemStack stack, long maxReceive, boolean simulate) {
        if (getMaxReceive(stack) > 0) {
            long energy = ItemNBTHelper.getLong(stack, "energy", 0);
            long energyReceived = Math.min(getCapacity(stack) - energy, Math.min(getMaxReceive(stack), maxReceive));

            if (!simulate) {
                energy += energyReceived;
                ItemNBTHelper.setLong(stack, "energy", energy);
            }
            return energyReceived;
        }

        return 0;
    }

    protected long extractEnergy(ItemStack stack, long maxExtract, boolean simulate) {
        if (getMaxExtract(stack) > 0) {
            long energy = ItemNBTHelper.getLong(stack, "energy", 0);
            long energyExtracted = Math.min(energy, Math.min(getMaxExtract(stack), maxExtract));

            if (!simulate) {
                energy -= energyExtracted;
                ItemNBTHelper.setLong(stack, "energy", energy);
            }
            return energyExtracted;
        }

        return 0;
    }

    public long getEnergyStored(ItemStack stack) {
        return ItemNBTHelper.getLong(stack, "energy", 0);
    }

    protected long getEnergyStored(ItemStack stack, boolean isOPAsking) {
        return getEnergyStored(stack);
    }

    public void setEnergy(ItemStack stack, long energy) {
        ItemNBTHelper.setLong(stack, "energy", MathUtils.clamp(energy, 0, getCapacity(stack)));
    }

    public void modifyEnergy(ItemStack stack, long modify) {
        long energy = ItemNBTHelper.getLong(stack, "energy", 0);
        ItemNBTHelper.setLong(stack, "energy", MathUtils.clamp(energy + modify, 0, getCapacity(stack)));
    }

    //endregion

    //region Display

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !(getEnergyStored(stack) == getCapacity(stack));
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        long es = getEnergyStored(stack, true);
        long mes = getCapacity(stack);
        return 1D - ((double)es / (double)mes);
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        InfoHelper.addEnergyInfo(stack, tooltip);
    }

    //endregion

    //region Capabilities

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, CompoundNBT nbt) {
        //TODO Check this out
        return new OPMultiProvider(LazyOptional.of(() -> new OPStorageItem(stack)), null);
    }

    private class OPStorageItem implements IOPStorage {
        private ItemStack stack;

        public OPStorageItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public long receiveOP(long maxReceive, boolean simulate) {
            return ItemEnergyBase.this.receiveEnergy(stack, maxReceive, simulate);
        }

        @Override
        public long extractOP(long maxExtract, boolean simulate) {
            return ItemEnergyBase.this.extractEnergy(stack, maxExtract, simulate);
        }

        @Override
        public long getOPStored() {
            return ItemEnergyBase.this.getEnergyStored(stack, true);
        }

        @Override
        public long getMaxOPStored() {
            return ItemEnergyBase.this.getCapacity(stack);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int) receiveOP(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return (int) extractOP(maxExtract, simulate);
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, ItemEnergyBase.this.getEnergyStored(stack, false));
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(Integer.MAX_VALUE, getMaxOPStored());
        }

        @Override
        public boolean canExtract() {
            return getMaxExtract(stack) > 0;
        }

        @Override
        public boolean canReceive() {
            return getMaxReceive(stack) > 0;
        }
    }

    //endregion
}
