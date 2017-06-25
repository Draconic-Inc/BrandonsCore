package com.brandon3055.brandonscore.client.gui.config;

import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

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
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		ModContainer container = DataUtils.firstMatch(Loader.instance().getActiveModList(), mod -> mod.getModId().equals(getModID()));
		if (container != null && ModConfigParser.hasConfig(getModID())) {
			return new BCModConfigGui(parentScreen, container);
		}
		return null;
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return BCModConfigGui.class;
	}

	@Nullable
	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Nullable
	@Override
	public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
		return null;
	}
}
