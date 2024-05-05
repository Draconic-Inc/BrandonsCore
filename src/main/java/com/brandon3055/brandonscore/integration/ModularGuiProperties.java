package com.brandon3055.brandonscore.integration;

import codechicken.lib.gui.modular.ModularGui;
import com.google.common.base.Preconditions;
import mezz.jei.api.gui.handlers.IGuiProperties;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ModularGuiProperties implements IGuiProperties {
	private final Class<? extends Screen> screenClass;
	private final int guiLeft;
	private final int guiTop;
	private final int guiXSize;
	private final int guiYSize;
	private final int screenWidth;
	private final int screenHeight;

	@Nullable
	public static ModularGuiProperties create(ModularGui modularGui) {
		Screen screen = modularGui.getScreen();
		if (screen.width <= 0 || screen.height <= 0) {
			return null;
		}
		int x = (int) modularGui.xMin();
		int y = (int) modularGui.yMin();
		int width = (int) modularGui.xSize();
		int height = (int) modularGui.ySize();

		if (x < 0) {
			width -= x;
			x = 0;
		}
		if (y < 0) {
			height -= y;
			y = 0;
		}
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new ModularGuiProperties(
			screen.getClass(),
			x,
			y,
			width,
			height,
			screen.width,
			screen.height
		);
	}

	private ModularGuiProperties(Class<? extends Screen> screenClass, int guiLeft, int guiTop, int guiXSize, int guiYSize, int screenWidth, int screenHeight) {
		Preconditions.checkArgument(guiLeft >= 0, "guiLeft must be >= 0");
		Preconditions.checkArgument(guiTop >= 0, "guiTop must be >= 0");
		Preconditions.checkArgument(guiXSize > 0, "guiXSize must be > 0");
		Preconditions.checkArgument(guiYSize > 0, "guiYSize must be > 0");
		Preconditions.checkArgument(screenWidth > 0, "screenWidth must be > 0");
		Preconditions.checkArgument(screenHeight > 0, "screenHeight must be > 0");
		this.screenClass = screenClass;
		this.guiLeft = guiLeft;
		this.guiTop = guiTop;
		this.guiXSize = guiXSize;
		this.guiYSize = guiYSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
	}

	@Override
	public Class<? extends Screen> getScreenClass() {
		return screenClass;
	}

	@Override
	public int getGuiLeft() {
		return guiLeft;
	}

	@Override
	public int getGuiTop() {
		return guiTop;
	}

	@Override
	public int getGuiXSize() {
		return guiXSize;
	}

	@Override
	public int getGuiYSize() {
		return guiYSize;
	}

	@Override
	public int getScreenWidth() {
		return screenWidth;
	}

	@Override
	public int getScreenHeight() {
		return screenHeight;
	}
}
