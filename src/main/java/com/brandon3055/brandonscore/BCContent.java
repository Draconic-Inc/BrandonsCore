package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 23/12/19.
 */
@ObjectHolder(MODID)
@Mod.EventBusSubscriber (modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BCContent {

    //region Container Types.
    @ObjectHolder ("player_access")
    public static MenuType<ContainerPlayerAccess> containerPlayerAccess;
    //endregion

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
    }

    @SubscribeEvent
    public static void onRegisterTiles(RegistryEvent.Register<BlockEntityType<?>> event) {
        IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
    }

    @SubscribeEvent
    public static void onRegisterContainers(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();
        registry.register(new MenuType<>(ContainerPlayerAccess::new).setRegistryName("player_access"));
    }

    @SubscribeEvent
    public static void onRegisterRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        IForgeRegistry<RecipeSerializer<?>> registry = event.getRegistry();
    }

}
