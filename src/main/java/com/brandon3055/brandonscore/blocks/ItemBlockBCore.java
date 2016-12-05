package com.brandon3055.brandonscore.blocks;

import com.brandon3055.brandonscore.config.FeatureWrapper;
import com.brandon3055.brandonscore.lib.ITilePlaceListener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Created by brandon3055 on 18/3/2016.
 * This is the base item block for all custom item blocks.
 */
public class ItemBlockBCore extends ItemBlock {

    private FeatureWrapper feature;
    private String registryDomain = null;

    public ItemBlockBCore(Block block, FeatureWrapper feature) {
        super(block);
        this.feature = feature;
        this.setHasSubtypes(feature.variantMap().length > 0 || (block instanceof BlockBCore && ((BlockBCore) block).nameOverrides.size() > 1));
    }

    public ItemBlockBCore(Block block) {
        super(block);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, playerIn, tooltip, advanced);
//        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(BlockBCore.TILE_DATA_TAG)) {
//            //tooltip.add(I18n.format("info.de.hasSavedData.txt"));
//        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (block instanceof BlockBCore && ((BlockBCore) block).nameOverrides.containsKey(stack.getItemDamage())) {
            return "tile." + getRegistryDomain() + ":" +((BlockBCore) block).nameOverrides.get(stack.getItemDamage());
        }

        return super.getUnlocalizedName(stack);
    }

    @Override
    public int getMetadata(int damage) {
        return getHasSubtypes() ? damage : 0;
    }

    public String getRegistryDomain() {
        if (registryDomain == null) {
            if (getRegistryName() == null) {
                return "null";
            }
            else {
                registryDomain = getRegistryName().getResourceDomain();
            }
        }

        return registryDomain;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean placed = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);

        TileEntity tile = world.getTileEntity(pos);
        if (placed && tile instanceof ITilePlaceListener) {
            ((ITilePlaceListener) tile).onTilePlaced(world, pos, side, hitX, hitY, hitZ, player, stack);
        }

        return placed;
    }
}
