package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.HAlign;

import java.awt.*;
import java.util.List;

/**
 * Created by brandon3055 on 5/31/2018.
 * This element only exists to serve as a marker element for things like new lines (currently only new lines).
 */
public class MarkerElement extends MDElementBase<MarkerElement> {
    private Type type;

    public MarkerElement(Type type) {
        this.type = type;
    }

    public MarkerElement(int spacerWidth, int spacerHeight) {
        this.type = Type.SPACER;
        setSize(spacerWidth, spacerHeight);
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        switch (type) {
            case NEW_LINE:
                layout.newLine(lineElement.isEmpty() ? 8 : 0);
                break;
            case SPACER:
                Point pos = layout.nextElementPos(xSize(), ySize());
                setPos(pos.x, pos.y);
                break;
        }
    }

    public static boolean isNewLine(MDElementBase element) {
        return element instanceof MarkerElement && ((MarkerElement) element).type == Type.NEW_LINE;
    }

    public boolean isAlign() {
        return type == Type.ALIGN_LEFT || type == Type.ALIGN_CENTER || type == Type.ALIGN_RIGHT;
    }

    public HAlign getAlign() {
        return type == Type.ALIGN_LEFT ? HAlign.LEFT : type == Type.ALIGN_CENTER ? HAlign.CENTER : HAlign.RIGHT;
    }

    public Type getType() {
        return type;
    }

    public static MarkerElement forAlignment(HAlign align) {
        return new MarkerElement(align == HAlign.LEFT ? Type.ALIGN_LEFT : align == HAlign.CENTER ? Type.ALIGN_CENTER : Type.ALIGN_RIGHT);
    }

    public enum Type {
        NEW_LINE,
        ALIGN_LEFT,
        ALIGN_CENTER,
        ALIGN_RIGHT,
        SPACER,
    }
}
