package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by brandon3055 on 5/07/2017.
 * Used to supply hover text for an element. Accepted types for T are String, String[] and List<String>
 */
public interface HoverTextSupplier<T, E extends MGuiElementBase> {

    T getText(E element);

    default List<String> getHoverText(E element) {
        T hoverText = getText(element);
        if (hoverText instanceof String) {
            return Collections.singletonList((String) hoverText);
        }
        else if (hoverText instanceof String[]) {
            return Arrays.asList((String[]) hoverText);
        }
        else if (hoverText instanceof List) {
            return (List<String>) hoverText;
        }
        return null;
    }
}
