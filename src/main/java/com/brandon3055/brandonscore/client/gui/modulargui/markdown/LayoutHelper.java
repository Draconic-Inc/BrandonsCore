package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import java.awt.*;

/**
 * Created by brandon3055 on 5/31/2018.
 *
 * This class defines the pos and width of an MDElementContainer as well as the caret position.
 * The caret position refers to the position where the next element will be placed or where the last element ends.
 * This is used when arranging elements in an element container.
 *
 * TODO want to investigate using the layout helper to manage alignment as well.
 * Because there is currently the issue that right aligned text does not remove trailing spaces.
 * This would also make layout a lot smoother.
 * May be able to use some sort of "space marker" that would be stripped if it ends up at the start or end of the line
 * This marker would be added by the text element when it does a new line split.
 */
public class LayoutHelper {
    private final int xPos;
    private final int yPos;
    private final int width;
    private int caretX;
    private int caretY;
    private int currentLineHeight;

    public LayoutHelper(int xPos, int yPos, int width) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        caretX = xPos;
        caretY = yPos;
        currentLineHeight = 0;
    }

    public Point nextElementPos(int elementWidth, int elementHeight) {
        if (getCaretXOffset() + elementWidth > width && getCaretXOffset() > 0) {
            caretX = xPos;
            caretY += currentLineHeight;
            currentLineHeight = 0;
        }

        Point pos = new Point(caretX, caretY);

        caretX += elementWidth;
        currentLineHeight = Math.max(currentLineHeight, elementHeight);
        return pos;
    }

    public int getContainerHeight() {
        return (caretY + currentLineHeight) - yPos;
    }

    /**
     * @param minLineHeight Sets the minimum height for the previous line (not the new line)
     */
    public void newLine(int minLineHeight) {
        currentLineHeight = Math.max(currentLineHeight, minLineHeight);
        caretX = xPos;
        caretY += currentLineHeight;
        currentLineHeight = 0;
    }

    public void newLine(int minLineHeight, int nextLineOffset) {
        currentLineHeight = Math.max(currentLineHeight, minLineHeight);
        currentLineHeight += nextLineOffset;
        caretX = xPos;
        caretY += currentLineHeight;
        currentLineHeight = 0;
    }

    /**
     * @return the caret offset from the left edge of the container
     */
    public int getCaretXOffset() {
        return caretX - xPos;
    }

    public int getWidth() {
        return width;
    }

    public void addCaretOffset(int xOffset) {
        caretX += xOffset;
    }

    public int getCaretX() {
        return caretX;
    }

    public int getCaretY() {
        return caretY;
    }
}
