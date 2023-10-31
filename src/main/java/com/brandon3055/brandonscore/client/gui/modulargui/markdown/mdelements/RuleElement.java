//package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;
//
//import codechicken.lib.math.MathHelper;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
//import net.minecraft.client.Minecraft;
//
//import java.util.List;
//
///**
// * Created by brandon3055 on 5/31/2018.
// */
//public class RuleElement extends MDElementBase<RuleElement> {
//
//    public RuleElement() {
//        topPad = 5;
//        bottomPad = 5;
//        height = 3;
//        width = 100;
//    }
//
//    @Override
//    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
//        int w = screenRelativeSize ? (int)(MathHelper.clip(width / 100D, 0, 1) * layout.getWidth()) : MathHelper.clip(width, 0, layout.getWidth());
//        setSize(w, height + topPad + bottomPad);
//        super.layoutElement(layout, lineElement);
//    }
//
//    @Override
//    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        if (hasColourBorder) {
//            drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | colourBorder);
//        }
//        if (hasColour) {
//            drawColouredRect(xPos() + leftPad, yPos() + topPad, xSize() - leftPad - rightPad, ySize() - topPad - bottomPad, 0xFF000000 | colour);
//        }
//        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//    }
//}
