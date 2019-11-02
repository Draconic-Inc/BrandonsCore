package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;

import java.util.*;

/**
 * Created by brandon3055 on 5/07/2017.
 * Used to supply hover text for an element. Accepted types for T are String, String[] and List<String>
 */
public interface HoverTextSupplier<T, E extends MGuiElementBase> {

    T getText(E element);

    default List<String> getHoverText(E element) {
        T hoverText = getText(element);
        if (hoverText instanceof String) {
            if (((String) hoverText).contains("\\n")) {
                return Arrays.asList(((String) hoverText).split("\\\\n"));
            }
            return Collections.singletonList((String) hoverText);
        }
        else if (hoverText instanceof String[]) {
            return splitNewLines(Arrays.asList((String[]) hoverText));
        }
        else if (hoverText instanceof List) {
            return splitNewLines((List<String>) hoverText);
        }
        return null;
    }

    static List<String> splitNewLines(Collection<String> collection) {
        List<String> list = new ArrayList<>();
        for (String s : collection) {
            if (s.contains("\\n")) {
                list.addAll(Arrays.asList(s.split("\\\\n")));
            }
            else {
                list.add(s);
            }
        }

        return list;
    }
}
