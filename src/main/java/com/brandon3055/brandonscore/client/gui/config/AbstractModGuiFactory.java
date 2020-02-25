package com.brandon3055.brandonscore.client.gui.config;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.IModGuiFactory;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * To use the ingame config gui with the BrandonsCore config system simply extend this class, return your mod id in getModID and set that class as your mods gui factory.
 */
public abstract class AbstractModGuiFactory implements IModGuiFactory {

    public abstract String getModID();

    @Override
    public void initialize(Minecraft minecraftInstance) {

    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public Screen createConfigGui(Screen parentScreen) {
//        ModContainer container = DataUtils.firstMatch(ModList.get().getMods(), mod -> mod.getModId().equals(getModID()));
//        if (container != null && ModConfigParser.hasConfig(getModID())) {
//            return new BCModConfigGui(parentScreen, container);
//        }
        return null;
    }

//	@Override
//	public Class<? extends Screen> mainConfigGuiClass() {
//		return BCModConfigGui.class;
//	}

    @Nullable
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
}
