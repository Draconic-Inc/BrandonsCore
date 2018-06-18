package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class PropertyVisitor {

    public Map<String, String> invalidCalls = new HashMap<>();
    public static final String NOT_SUPPORTED = "Not supported by this tag";

    public void startVisit() {}


    //General
    public void visitSize(int size, boolean screenRelative) {
        invalidCalls.put("size", NOT_SUPPORTED);
    }

    public void visitWidth(int width, boolean screenRelative) {
        invalidCalls.put("width", NOT_SUPPORTED);
    }

    public void visitHeight(int height) {
        invalidCalls.put("height", NOT_SUPPORTED);
    }

    public void visitTooltip(List<String> tooltip) {
        invalidCalls.put("tooltip", NOT_SUPPORTED);
    }

    public void visitEnabledTooltip(boolean enableTooltip) {
        invalidCalls.put("enable_tooltip", NOT_SUPPORTED);
    }

    public void visitAlignment(HAlign alignment) {
        invalidCalls.put("align", NOT_SUPPORTED);
    }

    public void visitVertAlign(VAlign vertAlignment) {
        invalidCalls.put("vert_align", NOT_SUPPORTED);
    }

    public void visitColour(int argb) {
        invalidCalls.put("colour", NOT_SUPPORTED);
    }

    public void visitColourHover(int argb) {
        invalidCalls.put("colour_hover", NOT_SUPPORTED);
    }

    public void visitBorderColour(int argb) {
        invalidCalls.put("border_colour", NOT_SUPPORTED);
    }

    public void visitBorderColourHover(int argb) {
        invalidCalls.put("border_colour_hover", NOT_SUPPORTED);
    }

    public void visitLeftPad(int leftPadding) {
        invalidCalls.put("left_pad", NOT_SUPPORTED);
    }

    public void visitRightPad(int rightPadding) {
        invalidCalls.put("right_pad", NOT_SUPPORTED);
    }

    public void visitTopPad(int topPadding) {
        invalidCalls.put("top_pad", NOT_SUPPORTED);
    }

    public void visitBottomPad(int bottomPadding) {
        invalidCalls.put("bottom_pad", NOT_SUPPORTED);
    }

    public void visitPadding(int padding) {
        visitLeftPad(padding);
        visitRightPad(padding);
        visitTopPad(padding);
        visitBottomPad(padding);
    }

    public final void visitInvalid(String property, String reason) {
        invalidCalls.put(property, reason);
    }


    /*
    * Properties
    *
    * # General
    * size
    * width
    * height
    * tooltip       (will replace hover and alt_hover)
    * enable_tooltip (will replace draw_hover and what not)
    * align
    *
    * border_colour
    * border_colour_hover
    * colour        (will replace fill colour)
    * colour_hover
    *
    * # General padding
    * padding (this can be converted to 4 padding calls locally)
    * left_pad
    * right_pad
    * top_pad
    * bottom_pad
    *
    * # Stack
    * draw_slot
    *
    *
    * # Recipe
    * spacing
    *
    * # Link
    * link_style (the old render is to generic and conflicts with table)
    *
    * # Image
    * link_to
    *
    * # Entity
    * x_offset
    * y_offset
    * rotate_speed
    * rotation
    * scale
    * track_mouse
    * draw_name
    * main_hand (these can be combined into a visitInventory call)
    * off_hand
    * head
    * chest
    * legs
    *
    * # Rule
    *
    * # Table
    * vertical_align
    * render_cells (the old render is to generic and conflicts with link)
    * */


    public void endVisit() {}
}
