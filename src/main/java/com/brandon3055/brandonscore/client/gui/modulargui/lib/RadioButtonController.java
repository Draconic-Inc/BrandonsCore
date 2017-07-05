package com.brandon3055.brandonscore.client.gui.modulargui.lib;

//Discovered some potential downfall's that make this kinda tricky to implement. Mainly the fact that in most cases
//the server will need to be responsible for ensuring only 1 function is active at a time.
//I may come back to this if i find a client side use for it or a way around this issue.

///**
// * Created by brandon3055 on 2/07/2017.
// * This is a helper class for implementing simple radio buttons.
// */
//public class RadioButtonController implements IGuiEventListener {
//
//    private IGuiEventListener listener;
//
//    /**
//     * @param forceDefault If true there will always need to be at least 1 button active.
//     *                     This is only enforced by preventing the current active button from being pressed again.
//     *                     You must ensure that one of the supplied buttons is active by default.
//     * @param simulateClicks If true buttons will be disabled by simulating a click as opposed to just setting their active state.
//     * @param listener This class needs to capture events from the buttons meaning you cant listen to the buttons directly.
//     *                 The button events will be passed though to the listener provided here.
//     */
//    public RadioButtonController(boolean forceDefault, boolean simulateClicks, IGuiEventListener listener) {}
//
//    @Override
//    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventElement) {
//
//    }
//}
