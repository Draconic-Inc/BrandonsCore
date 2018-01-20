package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;

public interface IButtonListener {
        /**
         * @param guiButton The gui button that was clicked.
         * @param pressed The mouse button that was pressed.
         */
        void onClick(GuiButton guiButton, int pressed);
    }