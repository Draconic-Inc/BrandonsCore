package com.brandon3055.brandonscore.client.gui.modulargui_old;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

/**
 * Created by brandon3055 on 30/08/2016.
 */
@Deprecated
public interface IModularGui<T extends GuiScreen> {

    T getScreen();

    int xSize();

    int ySize();

    int guiLeft();

    int guiTop();

    int screenWidth();

    int screenHeight();

    Minecraft getMinecraft();

    ModuleManager getManager();

    void setZLevel(int zLevel);

    int getZLevel();
}
