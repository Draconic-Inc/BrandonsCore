package com.brandon3055.brandonscore.asm;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Created by Brandon on 27/5/2015.
 */
public class ClassTransformer implements IClassTransformer {
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes) {
//		if (transformedName.equals("net.minecraft.potion.Potion")) return patchPotionClass(bytes);
//		else if (transformedName.equals("net.minecraft.network.play.server.S1DPacketEntityEffect")) return patchEffectPacket(bytes);
//		else if (transformedName.equals("net.minecraft.network.play.server.S1EPacketRemoveEntityEffect")) return patchRemoveEffectPacket(bytes);
//		else if (transformedName.equals("net.minecraft.client.network.NetHandlerPlayClient")) return patchClientNetHandlerClass(bytes);
//		else if (transformedName.equals("net.minecraft.potion.PotionEffect")) return patchPotionEffect(bytes);

		return bytes;
	}

//	private byte[] patchPotionClass(byte[] basicClass) {
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(basicClass);
//		classReader.accept(classNode, 0);
//		LogHelper.info("[ASM]: Found Class: " + classNode.name);
//
//		MethodNode setPotionName = null;
//
//		for (MethodNode mn : classNode.methods) if (mn.name.equals("setPotionName") || mn.name.equals("func_76390_b")) setPotionName = mn;
//
//
//		if (setPotionName != null)
//		{
//			InsnList toInsert = new InsnList();
//
//			toInsert.add(new VarInsnNode(ALOAD, 0));
//			toInsert.add(new VarInsnNode(ALOAD, 1));
//			toInsert.add(new MethodInsnNode(INVOKESTATIC, "com/brandon3055/brandonscore/asm/MethodHolder", "addPotionForSorting", "(Lnet/minecraft/potion/Potion;Ljava/lang/String;)V", false));
//
//			setPotionName.instructions.insert(toInsert);
//		}
//
//		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//		classNode.accept(writer);
//		return writer.toByteArray();
//	}
//
//	private byte[] patchPotionEffect(byte[] basicClass) {
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(basicClass);
//		classReader.accept(classNode, 0);
//		LogHelper.info("[ASM]: Found Class: " + classNode.name);
//
//		MethodNode readCustomPotionEffectFromNBT = null;
//		MethodNode writeCustomPotionEffectToNBT = null;
//		for (MethodNode mn : classNode.methods) {
//			if (mn.name.equals("func_82722_b") || mn.name.equals("readCustomPotionEffectFromNBT")) 	readCustomPotionEffectFromNBT = mn;
//			else if (mn.name.equals("func_82719_a") || mn.name.equals("writeCustomPotionEffectToNBT")) writeCustomPotionEffectToNBT = mn;
//		}
//		if (readCustomPotionEffectFromNBT != null)
//		{
//			InsnList toInsert = new InsnList();
//
//			toInsert.add(new VarInsnNode(ALOAD, 0));
//			toInsert.add(new MethodInsnNode(INVOKESTATIC, "com/brandon3055/brandonscore/asm/MethodHolder", "readCustomPotionEffectFromNBT", "(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/potion/PotionEffect;", false));
//			toInsert.add(new InsnNode(ARETURN));
//
//			readCustomPotionEffectFromNBT.instructions.insert(toInsert);
//		}
//		if (writeCustomPotionEffectToNBT != null)
//		{
//			InsnList toInsert = new InsnList();
//
//			toInsert.add(new VarInsnNode(ALOAD, 0));
//			toInsert.add(new VarInsnNode(ALOAD, 1));
//			toInsert.add(new MethodInsnNode(INVOKESTATIC, "com/brandon3055/brandonscore/asm/MethodHolder", "writeCustomPotionEffectToNBT", "(Lnet/minecraft/potion/PotionEffect;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", false));
//			toInsert.add(new InsnNode(ARETURN));
//
//			writeCustomPotionEffectToNBT.instructions.insert(toInsert);
//		}
//		ClassWriter writer = new ClassWriter(3);
//		classNode.accept(writer);
//
//		return writer.toByteArray();
//	}
//
//	private byte[] patchClientNetHandlerClass(byte[] basicClass) {
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(basicClass);
//		classReader.accept(classNode, 0);
//		LogHelper.info("[ASM]: Found Class: " + classNode.name);
//
//		MethodNode handleEffect = null;
//		for (MethodNode mn : classNode.methods) if (mn.name.equals("func_147260_a") || mn.name.equals("handleEntityEffect")) handleEffect = mn;
//
//		if (handleEffect != null)
//		{
//			for (int i = 0; i < handleEffect.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = handleEffect.instructions.get(i);
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if ((min.owner.equals("net/minecraft/network/play/server/S1DPacketEntityEffect")) && (min.name.equals("func_149427_e")))
//					{
//						min.desc = "()S";
//					}
//				}
//			}
//		}
//		ClassWriter writer = new ClassWriter(3);
//		classNode.accept(writer);
//
//		return writer.toByteArray();
//	}
//
//	private byte[] patchRemoveEffectPacket(byte[] basicClass) {
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(basicClass);
//		classReader.accept(classNode, 0);
//		LogHelper.info("[ASM]: Found Class: " + classNode.name);
//
//		MethodNode readPacketData = null;
//		MethodNode writePacketData = null;
//		for (MethodNode mn : classNode.methods) {
//			if (mn.name.equals("func_148837_a") || mn.name.equals("readPacketData")) readPacketData = mn;
//			else if (mn.name.equals("func_148840_b") || mn.name.equals("writePacketData")) writePacketData = mn;
//		}
//		if (readPacketData != null)
//		{
//			for (int i = 0; i < readPacketData.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = readPacketData.instructions.get(i);
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if (min.owner.equals("net/minecraft/network/PacketBuffer")) {
//						if ((min.name.equals("readUnsignedByte")) && ((readPacketData.instructions.get(i + 1) instanceof FieldInsnNode)))
//						{
//							FieldInsnNode fin = (FieldInsnNode)readPacketData.instructions.get(i + 1);
//							if (fin.name.equals("field_149078_b"))
//							{
//								MethodInsnNode newMethod = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "readShort", "()S", false);
//								readPacketData.instructions.insert(min, newMethod);
//								readPacketData.instructions.remove(min);
//							}
//						}
//					}
//				}
//			}
//		}
//		if (writePacketData != null)
//		{
//			for (int i = 0; i < writePacketData.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = writePacketData.instructions.get(i);
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if (min.owner.equals("net/minecraft/network/PacketBuffer")) {
//						if ((min.name.equals("writeByte")) && ((writePacketData.instructions.get(i - 1) instanceof FieldInsnNode)))
//						{
//							FieldInsnNode fin = (FieldInsnNode)writePacketData.instructions.get(i - 1);
//							if (fin.name.equals("field_149078_b"))
//							{
//								MethodInsnNode newMethod = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "writeShort", "(I)Lio/netty/buffer/ByteBuf;", false);
//								writePacketData.instructions.insert(min, newMethod);
//								writePacketData.instructions.remove(min);
//							}
//						}
//					}
//				}
//			}
//		}
//		ClassWriter writer = new ClassWriter(3);
//		classNode.accept(writer);
//
//		return writer.toByteArray();
//	}
//
//	private byte[] patchEffectPacket(byte[] basicClass) {
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(basicClass);
//		classReader.accept(classNode, 0);
//		LogHelper.info("[ASM]: Found Class: " + classNode.name);
//
//		for (FieldNode fn : classNode.fields) if (fn.name.equals("field_149432_b")) fn.desc = "S";
//
//		MethodNode constructor = null;
//		MethodNode getId = null;
//
//		MethodNode readPacketData = null;
//		MethodNode writePacketData = null;
//		for (MethodNode mn : classNode.methods)
//		{
//			if ((mn.name.equals("<init>")) && (mn.desc.equals("(ILnet/minecraft/potion/PotionEffect;)V"))) constructor = mn;
//			else if (mn.name.equals("func_149427_e")) getId = mn;
//			else if (mn.name.equals("func_148837_a") || mn.name.equals("readPacketData")) readPacketData = mn;
//			else if (mn.name.equals("func_148840_b") || mn.name.equals("writePacketData")) writePacketData = mn;
//		}
//		if (getId != null)
//		{
//			getId.desc = "()S";
//			for (AbstractInsnNode ain : getId.instructions.toArray()) {
//				if ((ain instanceof FieldInsnNode))
//				{
//					FieldInsnNode fin = (FieldInsnNode)ain;
//					fin.desc = "S";
//				}
//			}
//		}
//		if (constructor != null) {
//			for (int i = 0; i < constructor.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = constructor.instructions.get(i);
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if (min.name.equals("func_76456_a") || min.name.equals("getPotionID"))
//					{
//						for (int c = 0; c < 3; c++)
//						{
//							AbstractInsnNode n = constructor.instructions.get(i + 1);
//							constructor.instructions.remove(n);
//						}
//					}
//				}
//				else if ((ain instanceof FieldInsnNode))
//				{
//					FieldInsnNode fin = (FieldInsnNode)ain;
//					if (fin.name.equals("field_149432_b")) {
//						fin.desc = "S";
//					}
//				}
//			}
//		}
//		if (readPacketData != null)
//		{
//			for (int i = 0; i < readPacketData.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = readPacketData.instructions.get(i);
//				if ((ain instanceof FieldInsnNode))
//				{
//					FieldInsnNode fin = (FieldInsnNode)ain;
//					if (fin.name.equals("field_149432_b")) fin.desc = "S";
//				}
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if (min.owner.equals("net/minecraft/network/PacketBuffer")) {
//						if ((min.name.equals("readByte")) && ((readPacketData.instructions.get(i + 1) instanceof FieldInsnNode)))
//						{
//							FieldInsnNode fin = (FieldInsnNode)readPacketData.instructions.get(i + 1);
//							if (fin.name.equals("field_149432_b"))
//							{
//								MethodInsnNode newMethod = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "readShort", "()S", false);
//								readPacketData.instructions.insert(min, newMethod);
//								readPacketData.instructions.remove(min);
//							}
//						}
//					}
//				}
//			}
//		}
//		if (writePacketData != null)
//		{
//			for (int i = 0; i < writePacketData.instructions.size(); i++)
//			{
//				AbstractInsnNode ain = writePacketData.instructions.get(i);
//				if ((ain instanceof FieldInsnNode))
//				{
//					FieldInsnNode fin = (FieldInsnNode)ain;
//					if (fin.name.equals("field_149432_b")) fin.desc = "S";
//				}
//				if ((ain instanceof MethodInsnNode))
//				{
//					MethodInsnNode min = (MethodInsnNode)ain;
//					if (min.owner.equals("net/minecraft/network/PacketBuffer")) {
//						if ((min.name.equals("writeByte")) && ((writePacketData.instructions.get(i - 1) instanceof FieldInsnNode)))
//						{
//							FieldInsnNode fin = (FieldInsnNode)writePacketData.instructions.get(i - 1);
//							if (fin.name.equals("field_149432_b"))
//							{
//								MethodInsnNode newMethod = new MethodInsnNode(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "writeShort", "(I)Lio/netty/buffer/ByteBuf;", false);
//
//								writePacketData.instructions.insert(min, newMethod);
//								writePacketData.instructions.remove(min);
//							}
//						}
//					}
//				}
//			}
//		}
//		ClassWriter writer = new ClassWriter(3);
//		classNode.accept(writer);
//
//		return writer.toByteArray();
//	}
}
