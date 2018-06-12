package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableDefinition;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class TableElement extends MDElementBase<TableElement> {

    public int headingColour = 0;
    public boolean colourHeading = false;
    public int rows = 0;
    public int columns = 0;
    public boolean renderCells = true;
    public TableDefinition definition = null;

    public TableElement() {
        width = 100;
        screenRelativeSize = true;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        //Umm.... Do stuff?
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        //Render cell colours?
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        //Render cell boxes?
    }
}
