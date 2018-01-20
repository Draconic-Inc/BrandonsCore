package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.utils.GuiHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public class MouseIntractable {
    public boolean isMouseOver = false;
    public List<Part> parts = new ArrayList<>();
    public List<String> hoverText = new LinkedList<>();
    public String errorText = null;
    public ItemStack hoverStack = null;

    public boolean onClick(int mouseX, int mouseY, int button) {
        return false;
    }

    public boolean updateMouseOver(int mouseX, int mouseY) {
        isMouseOver = false;

        for (Part part : parts) {
            if (part.lastXPos >= 0 && GuiHelper.isInRect(part.lastXPos, part.lastYPos, part.width, part.height, mouseX, mouseY)) {
                isMouseOver = true;
                return true;
            }
        }

        return false;
    }
}
