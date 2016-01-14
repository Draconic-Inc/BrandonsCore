package com.brandon3055.brandonscore.common.utills;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * Created by Brandon on 25/07/2014.
 */
public class Utills {

	public static IEntitySelector selectLivingBase = new IEntitySelector() {
		@Override
		public boolean isEntityApplicable(Entity entity) {
			return entity instanceof EntityLivingBase;
		}
	};

	public static IEntitySelector selectPlayer = new IEntitySelector() {
		@Override
		public boolean isEntityApplicable(Entity entity) {
			return entity instanceof EntityPlayer;
		}
	};

	public static String formatNumber(double value){
		if (value < 1000D)
			return String.valueOf(value);
		else if (value < 1000000D)
			return String.valueOf(Math.round(value)/1000D) + "K";
		else if (value < 1000000000D)
			return String.valueOf(Math.round(value/1000D)/1000D) + "M";
		else if (value < 1000000000000D)
			return String.valueOf(Math.round(value/1000000D)/1000D) + "B";
		else
			return String.valueOf(Math.round(value/1000000000D)/1000D) + "T";
	}

	public static String formatNumber(long value){
		if (value < 1000L)
			return String.valueOf(value);
		else if (value < 1000000L)
			return String.valueOf(Math.round(value)/1000D) + "K";
		else if (value < 1000000000L)
			return String.valueOf(Math.round(value/1000L)/1000D) + "M";
		else if (value < 1000000000000L)
			return String.valueOf(Math.round(value/1000000L)/1000D) + "B";
		else
			return String.valueOf(Math.round(value/1000000000L)/1000D) + "T";
	}

	/**
	 * Add commas to a number e.g. 161253126 > 161,253,126
	 */
	public static String addCommas(int value){
		String rawNumber = String.valueOf(value);
		String formattedNumber = "";
		do {
			int end = rawNumber.length();
			int start = Math.max(0, end - 3);
			String part = rawNumber.substring(start, end);
			rawNumber = rawNumber.substring(0, start);
			formattedNumber = part + (formattedNumber.length() > 0 ? "," : "") + formattedNumber;

		}while (rawNumber.length() > 0);
		return formattedNumber;
	}

	/**
	 * Add commas to a number e.g. 161253126 > 161,253,126
	 */
	public static String addCommas(long value){
		String rawNumber = String.valueOf(value);
		String formattedNumber = "";
		do {
			int end = rawNumber.length();
			int start = Math.max(0, end - 3);
			String part = rawNumber.substring(start, end);
			rawNumber = rawNumber.substring(0, start);
			formattedNumber = part + (formattedNumber.length() > 0 ? "," : "") + formattedNumber;

		}while (rawNumber.length() > 0);
		return formattedNumber;
	}

	/**
	 * Calculates the exact distance between two points in 3D space
	 * @param x1 point A x
	 * @param y1 point A y
	 * @param z1 point A z
	 * @param x2 point B x
	 * @param y2 point B y
	 * @param z2 point B z
	 * @return The distance between point A and point B
	 */
	public static double getDistanceAtoB(double x1, double y1, double z1, double x2, double y2, double z2){
		double dx = x1-x2;
		double dy = y1-y2;
		double dz = z1-z2;
		return Math.sqrt((dx*dx + dy*dy + dz*dz));
	}

	/**
	 * Calculates the exact distance between two points in 2D space
	 * @param x1 point A x
	 * @param z1 point A z
	 * @param x2 point B x
	 * @param z2 point B z
	 * @return The distance between point A and point B
	 */
	public static double getDistanceAtoB(double x1, double z1, double x2, double z2){
		double dx = x1-x2;
		double dz = z1-z2;
		return Math.sqrt((dx*dx + dz*dz ));
	}

	public static double getDistanceSq(double x1, double y1, double z1, double x2, double y2, double z2){
		double dx = x1-x2;
		double dy = y1-y2;
		double dz = z1-z2;
		return dx*dx + dy*dy + dz*dz;
	}

	public static double getDistanceSq(double x1, double z1, double x2, double z2){
		double dx = x1-x2;
		double dz = z1-z2;
		return dx*dx + dz*dz;
	}

	/**
	 * Returns true if this is a client connected to a remote server.
	 */
	public static boolean isConnectedToDedicatedServer()
	{
		return FMLCommonHandler.instance().getMinecraftServerInstance() == null;
	}

	/**
	 * Get itemstack from name of item or block name.
	 */
	public static ItemStack getStackFromName(String name, int meta)
	{
		if (name.contains("tile."))
		{
			name = name.replace("draconicevolution", "DraconicEvolution").replace("tile.", "");
			if (GameData.getBlockRegistry().getObject(name) != null) return new ItemStack(GameData.getBlockRegistry().getObject(name), 1, meta);
		}
		if (name.contains("item."))
		{
			name = name.replace("draconicevolution", "DraconicEvolution").replace("item.", "");
			if (GameData.getItemRegistry().getObject(name) != null) return new ItemStack(GameData.getItemRegistry().getObject(name), 1, meta);
		}
		return null;
	}

	/**
	 * Update the blocks an all 6 sides of a block.
	 */
	public static void updateNeabourBlocks(World world, int x, int y, int z)
	{
		world.notifyBlocksOfNeighborChange(x, y, z, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x - 1, y, z, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x + 1, y, z, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x, y - 1, z, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x, y + 1, z, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x, y, z - 1, world.getBlock(x, y, z));
		world.notifyBlocksOfNeighborChange(x, y, z + 1, world.getBlock(x, y, z));
	}

	/**
	 * Determine the orientation of a block based on the position of the entity that placed it.
	 */
	public static int determineOrientation(int x, int y, int z, EntityLivingBase entity)
	{
		if (MathHelper.abs((float) entity.posX - (float) x) < 2.0F && MathHelper.abs((float)entity.posZ - (float)z) < 2.0F)
		{
			double d0 = entity.posY + 1.82D - (double)entity.yOffset;

			if (d0 - (double)y > 2.0D) return 0;

			if ((double)y - d0 > 0.0D) return 1;
		}

		int l = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		return l == 0 ? 3 : (l == 1 ? 4 : (l == 2 ? 2 : (l == 3 ? 5 : 0)));
	}

	public static double round(double number, double multiplier){
		return Math.round(number * multiplier) / multiplier;
	}

	public static int getNearestMultiple(int number, int multiple){
		int result = number;

		if (number < 0) result *= -1;

		if (result % multiple == 0) return number;
		else if (result % multiple < multiple/2) result = result - result % multiple;
		else result = result + (multiple - result % multiple);

		if (number < 0) result *= -1;

		return result;
	}

	/**Simple method to convert a Double object to a primitive int*/
	public static int toInt(double d) { return (int)d; }
}
