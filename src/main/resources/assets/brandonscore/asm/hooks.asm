list i_EnchantmetTypeCheck
ALOAD 1
INSTANCEOF com/brandon3055/brandonscore/asm/IEnchantmentOverride
IFEQ LELSE
ALOAD 1
ALOAD 0
INVOKEINTERFACE com/brandon3055/brandonscore/asm/IEnchantmentOverride.checkEnchantTypeValid (Lnet/minecraft/enchantment/EnumEnchantmentType;)Z
IRETURN
LELSE