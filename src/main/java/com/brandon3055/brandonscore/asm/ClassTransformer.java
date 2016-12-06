package com.brandon3055.brandonscore.asm;

import codechicken.lib.asm.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.Map;

/**
 * Created by Brandon on 27/5/2015.
 */
public class ClassTransformer implements IClassTransformer {

    private ModularASMTransformer transformer = new ModularASMTransformer();

    public ClassTransformer() {
        Map<String, ASMBlock> blocks = ASMReader.loadResource("/assets/brandonscore/asm/hooks.asm");
        transformer.add(new ModularASMTransformer.MethodInjector(new ObfMapping("net/minecraft/enchantment/EnumEnchantmentType", "func_77557_a", "(Lnet/minecraft/item/Item;)Z"), blocks.get("i_EnchantmetTypeCheck"), true));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        return transformer.transform(name, bytes);
    }

        //if (transformedName.equals("net.minecraft.enchantment.EnumEnchantmentType")) {
            //return transformer.transform(name, bytes);
        //}

        //return bytes;

//        if (transformedName.equals("net.minecraft.enchantment.EnumEnchantmentType")) {
//            //debug("net.minecraft.enchantment.EnumEnchantmentType", bytes);
//
//            return transformer.transform(name, bytes);
////
////            ClassNode classNode = ASMHelper.createClassNode(bytes);
////
////            MethodNode method = ASMHelper.findMethod(new ObfMapping("net/minecraft/enchantment/EnumEnchantmentType", "canEnchantItem", "(Lnet/minecraft/item/Item;)Z"), classNode);
////
////
////            InsnList list = new InsnList();
////
////            list.add(new VarInsnNode(ALOAD, 1));
////            list.add(new TypeInsnNode(INSTANCEOF, "com/brandon3055/brandonscore/asm/IEnchantmentOverride"));
////            list.add(new JumpInsnNode(IFEQ, getFirstLabel(method.instructions)));
////            //list.add(new JumpInsnNode(IFEQ, new LabelNode()));
////            //list.add(new VarInsnNode(ALOAD, 1));
////            //list.add(new TypeInsnNode(CHECKCAST, "com/brandon3055/brandonscore/asm/IEnchantmentOverride"));
////            //list.add(new VarInsnNode(ALOAD, 0));
////            //list.add(new MethodInsnNode(INVOKESTATIC, "com/brandon3055/brandonscore/asm/IEnchantmentOverride", "checkEnchantTypeValid", "(Lnet/minecraft/enchantment/EnumEnchantmentType;)Z", true));
////            list.add(new InsnNode(IRETURN));
////
////            list.add(method.instructions);
////
////            method.instructions = list;
////            //method.instructions.add(list);
////
////            debug(ASMHelper.createBytes(classNode, 0));
////
////            return ASMHelper.createBytes(classNode, 0);
//
////            ALOAD 1
////            INSTANCEOF com/brandon3055/brandonscore/asm/IEnchantmentOverride
////            IFEQ L1
//
////            ALOAD 1
////            CHECKCAST com/brandon3055/brandonscore/asm/IEnchantmentOverride
////            ALOAD 0
////            INVOKEINTERFACE com/brandon3055/brandonscore/asm/IEnchantmentOverride.checkEnchantTypeValid (Lnet/minecraft/enchantment/EnumEnchantmentType;)Z
////            IRETURN
//        }
//
  //      return bytes;
//    }

    public static LabelNode getFirstLabel(InsnList instructions){
        boolean flag = false;
        for (AbstractInsnNode instruction : instructions.toArray()){
            if (instruction instanceof LabelNode){
                return (LabelNode) instruction;
            }
        }
        return null;
    }

    private static void debug(byte[] bytes) {
        ObfMapping mapping = new ObfMapping("net/minecraft/enchantment/EnumEnchantmentType", "canEnchantItem", "(Lnet/minecraft/item/Item;)Z");
        System.out.println("\n");
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 8);
        for (MethodNode node : classNode.methods) {
            System.out.println(String.format("Name: [%s], Desc: [%s].", node.name, node.desc));
        }
        MethodNode methodNode = ASMHelper.findMethod(mapping, classNode);
        if (methodNode == null) {
            System.out.println("Unable to find method!");
        } else {
            System.out.println("\n Instructions: \n");
            System.out.println(ASMHelper.toString(methodNode.instructions));
        }
        System.out.println("\n");

    }
}
