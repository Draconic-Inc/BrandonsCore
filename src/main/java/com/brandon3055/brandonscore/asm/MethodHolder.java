package com.brandon3055.brandonscore.asm;

/**
 * Created by Brandon on 30/5/2015.
 */
public class MethodHolder {
//	public static List<Potion> detectedPotions = new ArrayList<Potion>();

//	public static void addPotionForSorting(Potion potion, String name){
//		if (potion.id <= 23) {
//			return;
//		}
//		detectedPotions.add(potion);
//	}
//
//	public static NBTTagCompound writeCustomPotionEffectToNBT(PotionEffect effect, NBTTagCompound compound)
//	{
//		compound.setByte("Id", (byte) 0);
//		compound.setShort("ExtendedID", (short) effect.getPotionID());
//		compound.setByte("Amplifier", (byte) effect.getAmplifier());
//		compound.setInteger("Duration", effect.getDuration());
//		compound.setBoolean("Ambient", effect.getIsAmbient());
//		return compound;
//	}
//
//	public static PotionEffect readCustomPotionEffectFromNBT(NBTTagCompound compound)
//	{
//		if (compound.hasKey("ExtendedID"))
//		{
//			int shortIDid = compound.getShort("ExtendedID");
//			if ((shortIDid >= 0) && (shortIDid < Potion.potionTypes.length) && (Potion.potionTypes[shortIDid] != null))
//			{
//				byte amplifier = compound.getByte("Amplifier");
//				int duration = compound.getInteger("Duration");
//				boolean isAmbient = compound.getBoolean("Ambient");
//
//				return new PotionEffect(shortIDid, duration, amplifier, isAmbient);
//			}
//			return null;
//		}
//		byte byteID = compound.getByte("Id");
//		if ((byteID >= 0) && (byteID < Potion.potionTypes.length) && (Potion.potionTypes[byteID] != null))
//		{
//			byte am = compound.getByte("Amplifier");
//			int duration = compound.getInteger("Duration");
//			boolean isAmbient = compound.getBoolean("Ambient");
//			return new PotionEffect(byteID, duration, am, isAmbient);
//		}
//		return null;
//	}
}
