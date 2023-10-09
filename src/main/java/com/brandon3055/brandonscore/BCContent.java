package com.brandon3055.brandonscore;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

import com.brandon3055.brandonscore.inventory.ContainerPlayerAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

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
    public static void onRegisterContainers(RegistryEvent.Register<MenuType<?>> event) {
        IForgeRegistry<MenuType<?>> registry = event.getRegistry();
        registry.register(new MenuType<>(ContainerPlayerAccess::new).setRegistryName("player_access"));
    }
}
