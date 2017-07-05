package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.client.gui.modulargui.oldelements.MGuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiLabel;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;

/**
 * Created by brandon3055 on 2/07/2017.
 */
public class ModularGuiExample extends ModularGuiScreen implements IGuiEventListener {

    public ModularGuiExample() {
        super(550, 300);
    }

    @Override
    public void addElements(ModuleManager manager) {

        //Add a blank grey background to the gui. The reload callback must be used to set the size because this method will not fire again if the gui scale changes.
        //Alternatively you could store a reference to the element and update its size and pos in the reloadGui method.

        MGuiBorderedRect background;
        manager.add(background = new MGuiBorderedRect().setFillColour(0xFF303030).addReloadCallback(guiRect -> guiRect.setPos(guiLeft(), guiTop()).setSize(xSize, ySize)));

        GuiScrollElement scrollElement = new GuiScrollElement().setRelPos(5, 5).setSize(xSize - 10, ySize - 10).setStandardScrollBehavior();
        background.addChild(scrollElement);

        //Add a white backing for the scroll element
        scrollElement.applyBackgroundElement(new MGuiBorderedRect().setColours(0xFFc0c0c0, 0xFFFFFFFF));
//        scrollElement.setInsets(10, 10, 0, 0);

//        MGuiBorderedRect canvas = new MGuiBorderedRect().setFillColour(0xFFc0c0c0).setRelPos(-10, -10).setSize(xSize + 10, ySize + 10);
//        scrollElement.addElement(canvas);

        //All other components will be added to the background component. This is not required but it has the advantage of not needing to use a callback to set the position of these elements.
        //This is because when the background element's position is updated it will automatically update the position of all of its child elements.
        //I will also be using the setRelPos method to set the positions of the child elements. This simply sets the position relative to the position of the parent.

        //Note: This is not there recommended method for creating a custom element. I am just using this as a quick and dirty way of showing of the drawCustomString function in MGuiElementBase
        scrollElement.addElement(new MGuiBorderedRect() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                drawBorderedRect(xPos() + 10, yPos() + 10, 200, 30, 1, 0xFF00FF00, 0xFF00FF00);
                drawCustomString(fontRenderer, "Left Aligned String", xPos() + 10, yPos() + 10, 200, 0x000000, LEFT, TextRotation.NORMAL, false, false, false);
                drawCustomString(fontRenderer, "Center Aligned String", xPos() + 10, yPos() + 20, 200, 0x000000, GuiAlign.CENTER, TextRotation.NORMAL, false, false, false);
                drawCustomString(fontRenderer, "Right Aligned String", xPos() + 10, yPos() + 30, 200, 0x000000, GuiAlign.RIGHT, TextRotation.NORMAL, false, false, false);

                drawCustomString(fontRenderer, "This is a long test string meant to test the left-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 10, yPos() + 45, 100, 0xFF0000, LEFT, TextRotation.NORMAL, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the center-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 120, yPos() + 45, 100, 0xFF0000, GuiAlign.CENTER, TextRotation.NORMAL, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the right-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 230, yPos() + 45, 100, 0xFF0000, GuiAlign.RIGHT, TextRotation.NORMAL, true, false, false);

                drawCustomString(fontRenderer, "This is... Upside Down...", xPos() + 230, yPos() + 10, 100, 0xFF0000, LEFT, TextRotation.ROT_180, true, false, false);


                drawCustomString(fontRenderer, "This is a long test string meant to test the CC-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 340, yPos() + 5, 100, 0xFF0000, LEFT, TextRotation.ROT_CC, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the C-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 480, yPos() + 5, 100, 0xFF0000, LEFT, TextRotation.ROT_C, true, false, false);

            }
        }.setColours(0xFFFFFFFF, 0xFF00FF00).setInsetRelPos(0, 0).setSize(xSize - 10 + 500, 110));

        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 125).setSize(130, 20).setText("Vanilla Style Button").setVanillaButtonRender(true).setHoverText("With Hover Text!").setHoverTextDelay(10)); //The parent gui is automatically assigned as the event listener because it is an instance of IGuiEventListener
        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 150).setSize(130, 20).setText("Trim mode enabled on a vanilla button").setVanillaButtonRender(true).setHoverText("With", "A", "Hover", "Text", "Array!", TextFormatting.RED+"[Disclaimer: hover text arrays do not require each word to be on a new line.]"));
        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 175).setSize(130, 40).setText("A vanilla style button with wrapping enabled and left alignment").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setHoverTextArray(element -> {
            long seconds = Minecraft.getMinecraft().world.getWorldTime() / 20;
            long minutes = (int) Math.floor(seconds / 60D);
            return new String[] {"With a hover text supplier that displays world time!", "World Time: " + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60)};
        }));

        scrollElement.addElement(new GuiButton().setInsetRelPos(135, 125).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(TextRotation.ROT_CC));
        scrollElement.addElement(new GuiButton().setInsetRelPos(155, 125).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(GuiAlign.CENTER).setRotation(TextRotation.ROT_CC));
        scrollElement.addElement(new GuiButton().setInsetRelPos(175, 125).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(TextRotation.ROT_C));

        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 175).setSize(130, 40).setText("Waaaaaaaat.....\nThis button toggles!").setVanillaButtonRender(true).setWrap(true).setAlignment(GuiAlign.CENTER).setRotation(TextRotation.ROT_180).setToggleMode(true));

        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 125).setSize(130, 45).setText("Buttons can also use a solid colour bordered rectangle as their background").setFillColour(0xFF000000).setBorderColours(0xFFFF0000, 0xFF00FF00).setWrap(true).setAlignment(LEFT).setToggleMode(true));
        scrollElement.addElement(new GuiButton().setInsetRelPos(325, 125).setSize(130, 80).setText("Or no background at all!\nYoy can also just apply child elements to buttons to be rendered as the background. e.g. i could use a stack icon element as a button's background").setWrap(true).setAlignment(GuiAlign.CENTER));


        scrollElement.addElement(new GuiLabel().setInsetRelPos(0, 250).setSize(300, 12).setAlignment(LEFT).setDisplayString("Yes. This is a scroll element inside a scroll element!"));

        GuiScrollElement scrollElement2 = new GuiScrollElement().setInsetRelPos(5, 265).setSize(510, 200).setStandardScrollBehavior();
        scrollElement.addElement(scrollElement2);
        scrollElement2.addElement(new MGuiElementBase().setRelPos(0, 0).setSize(500, 170));
        scrollElement2.applyBackgroundElement(new MGuiBorderedRect().setColours(0xFF000000, 0xFFFF00FF));
        scrollElement2.addBackgroundChild(new MGuiBorderedRect().setBorderColour(0xFF707070).setRelPos(-2, -2).setSize(514, 204));

        scrollElement2.addElement(new GuiLabel().setRelPos(5, 2).setSize(505, 10).setAlignment(LEFT).setDisplayString("Oh... yea. And there is a scroll list inside this scroll element inside a scroll element..."));
        GuiScrollElement scrollList = new GuiScrollElement().setRelPos(5, 12).setSize(200, 188).setListMode(VERT_LOCK_POS_WIDTH).setStandardScrollBehavior();
        scrollElement2.addElement(scrollList);
        for (int i = 0; i < 50; i++) {
            scrollList.addElement(new GuiLabel().setSize(0, 15).setDisplayString("Random Label In A List " + i));
        }
        scrollList.addBackgroundChild(new MGuiBorderedRect().setPosAndSize(scrollList.getRect()).setColours(0xFF009090, 0xFF009090));
        scrollList.getVerticalScrollBar().setHidden(true);

    }

    @Override
    public void reloadGui() {
        super.reloadGui();
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventSource) {
        if (event.isButton() && !event.asButton().getElement().getToggleMode()) {
            mc.displayGuiScreen(new ModularGuiExample());
        }
    }
}