package com.brandon3055.brandonscore.client.gui.config;

import com.brandon3055.brandonscore.registry.ModConfigParser;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import mezz.jei.config.JEIModConfigGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import javax.annotation.Nullable;
import java.util.Set;

public class BCModGuiFactory implements IModGuiFactory {
    @Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		ModContainer mod = Loader.instance().activeModContainer();
		LogHelperBC.dev(mod);
		if (mod != null && ModConfigParser.hasConfig(mod.getModId())) {
			return new BCModConfigGui(parentScreen, mod);
		}
		return null;
	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return JEIModConfigGui.class;
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
