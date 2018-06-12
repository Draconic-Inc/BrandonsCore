package com.brandon3055.brandonscore.client.gui.modulargui.markdown.old;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public abstract class IPartBuilder {

    public int finalXPos = 0;
    public int finalYPos = 0;
    public int builtHeight = 0;

    /**
     * Checks if the given string contains this part.
     * If true returns the index of the part otherwise returns -1.
     */
    public abstract int matches(String test);

    /**
     * @param markdown  The markdown string
     * @param nextPart  The index of the next part
     * @param container
     * @return the remaining markdown string
     */
    public abstract String build(BCFontRenderer font, String markdown, int nextPart, BCFontRenderer fr, PartContainer container, LinkedList<Part> parts, int elementLeft, int elementRight, int xPos, int yPos, int nextYLevel);
}
