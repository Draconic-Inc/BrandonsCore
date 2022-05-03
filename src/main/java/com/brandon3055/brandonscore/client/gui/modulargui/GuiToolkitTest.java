package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.api.power.OPStorage;
import com.brandon3055.brandonscore.client.gui.GuiToolkit;
import com.brandon3055.brandonscore.client.gui.GuiToolkit.Palette;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
import com.brandon3055.brandonscore.client.gui.modulargui.templates.TBasicMachine;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import static com.brandon3055.brandonscore.client.gui.GuiToolkit.GuiLayout.EXTRA_WIDE_EXTRA_TALL;
import static com.brandon3055.brandonscore.client.gui.GuiToolkit.LayoutPos.BOTTOM_RIGHT;
import static com.brandon3055.brandonscore.client.gui.GuiToolkit.LayoutPos.TOP_LEFT;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GRAY;

/**
 * Created by brandon3055 on 2/07/2017.
 */
public class GuiToolkitTest extends ModularGuiScreen {

    protected GuiToolkit<GuiToolkitTest> toolkit = new GuiToolkit<>(this, EXTRA_WIDE_EXTRA_TALL);

    public GuiToolkitTest(Component titleIn) {
        super(titleIn);
    }

    @Override
    public void addElements(GuiElementManager manager) {
        manager.addChild(new GuiButton("Reload").setSize(50, 16).setVanillaButtonRender(true).onPressed(() -> minecraft.setScreen(new GuiToolkitTest(title))));

        TBasicMachine template = toolkit.loadTemplate(new TBasicMachine(this, null));
        template.title.setLabelText("Colour Palette Test UI");
        GuiElement bg = template.background;

        //Power
        template.addEnergyBar(new OPStorage(100000));
        template.addEnergyItemSlot(false, true);
        template.powerSlot.setMaxYPos(template.playerSlots.maxYPos(), false).setXPos(template.background.maxXPos() - (7 + 18));
        template.energyBar.setYPos(template.playerSlots.yPos() + 10).setMaxYPos(template.powerSlot.yPos() - 12, true).setXPos(template.powerSlot.xPos() + 2);
        template.playerSlots.setXPos(template.background.xPos() + 7);

        //Info Panel
        template.infoPanel.addLabeledValue(GOLD + I18n.get("gui.de.generator.fuel_efficiency"), 6, 11, () -> GRAY + ("--" + "%"), true);
        template.infoPanel.addLabeledValue(GOLD + I18n.get("gui.de.generator.output_power"), 6, 11, () -> GRAY + ("--" + " / " + "--" + " OP/t"), true);
        template.infoPanel.addLabeledValue(GOLD + I18n.get("gui.de.generator.current_fuel_value"), 6, 11, () -> GRAY + ("n/a"), true);

//        GuiEntityFilter filterUI = new GuiEntityFilter(tile.entityFilter, () -> {});
//
//        bg.addChild(filterUI);
//        filterUI.setRelPos(bg, 10, 14).setMaxPos(bg.maxXPos() - 18, template.playerSlots.yPos() - 4, true);
//
        GuiBorderedRect testSlotArea = new GuiBorderedRect();
        testSlotArea.set3DGetters(Palette.Slot::fill, Palette.Slot::accentDark, Palette.Slot::accentLight);
        testSlotArea.setRelPos(bg, 10, 15).setSize(200, 100);
        bg.addChild(testSlotArea);
        testSlotArea.addChild(new GuiLabel("Slot Text").setShadow(false).setRelPos(testSlotArea, 60, 5).setSize(150, 10).setTextColGetter(Palette.Slot::text));

        GuiBorderedRect subItem = new GuiBorderedRect();
        subItem.set3DGetters(Palette.SubItem::fill, Palette.SubItem::accentDark, Palette.SubItem::accentLight);
        subItem.setRelPos(testSlotArea, 5, 5).setSize(50, 14);
        bg.addChild(subItem);
        testSlotArea.addChild(new GuiLabel("SubItem").setShadow(false).setRelPos(subItem, 0, 2).setSize(50, 10).setTextColGetter(Palette.SubItem::text));

        GuiBorderedRect subItem2 = new GuiBorderedRect();
        subItem2.set3DGetters(Palette.SubItem::fill, Palette.SubItem::accentLight, Palette.SubItem::accentDark);
        subItem2.setRelPos(testSlotArea, 5, 25).setSize(80, 60);
        bg.addChild(subItem2);

        GuiBorderedRect subItem3 = new GuiBorderedRect();
        subItem3.set3DGetters(Palette.SubItem::fill, Palette.SubItem::accentDark, Palette.SubItem::accentLight);
        subItem3.setPos(testSlotArea.xPos(), testSlotArea.maxYPos() + 5).setSize(50, 14);
        bg.addChild(subItem3);

        GuiBorderedRect subItem4 = new GuiBorderedRect();
        subItem4.set3DGetters(Palette.SubItem::fill, Palette.SubItem::accentLight, Palette.SubItem::accentDark);
        subItem4.setPos(subItem3.maxXPos() + 5, subItem3.yPos()).setSize(50, 14);
        bg.addChild(subItem4);


        GuiButton button1 = toolkit.createButton("Text", testSlotArea).setSize(50, 14);
        toolkit.placeInside(button1, testSlotArea, BOTTOM_RIGHT, -3, -3);
        GuiButton button12 = toolkit.createVanillaButton("Vanilla!", testSlotArea)
                .setSize(50, 14)
                .setVanillaButtonRender(true);
        toolkit.placeInside(button12, testSlotArea, BOTTOM_RIGHT, -3, -20);
        GuiButton button13 = toolkit.createButton("Text", testSlotArea, true).setSize(50, 14);
        toolkit.placeInside(button13, testSlotArea, BOTTOM_RIGHT, -3, -37);

        GuiButton button2 = toolkit.createButton("Text", testSlotArea).setSize(50, 14);
        toolkit.placeOutside(button2, testSlotArea, BOTTOM_RIGHT, -36, 3);
        GuiButton button22 = toolkit.createVanillaButton("Text", testSlotArea).setSize(50, 14);
        toolkit.placeOutside(button22, testSlotArea, BOTTOM_RIGHT, -36, 20);
        GuiButton button23 = toolkit.createButton("Text", testSlotArea, true).setSize(50, 14);
        toolkit.placeOutside(button23, testSlotArea, BOTTOM_RIGHT, -36, 37);

        GuiButton button3 = toolkit.createButton("Text", subItem2).setSize(50, 14);
        toolkit.placeInside(button3, subItem2, TOP_LEFT, 3, 3);
        GuiButton button32 = toolkit.createVanillaButton("Text", subItem2).setSize(50, 14);
        toolkit.placeInside(button32, subItem2, TOP_LEFT, 3, 20);
        GuiButton button33 = toolkit.createButton("Text", subItem2, true).setSize(50, 14);
        toolkit.placeInside(button33, subItem2, TOP_LEFT, 3, 37);

    }

    @Override
    public void reloadGui() {
        super.reloadGui();
    }
}