package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.GuiElement;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by brandon3055 on 5/07/2017.
 * Used to supply hover text for an element. Accepted types for T are String, String[] and List<String>
 */
@Deprecated //I need to figure out a better solution to this that better handles Components
public interface HoverTextSupplier<E extends GuiElement<?>> {

    Object getText(E element);

    default List<Component> getHoverText(E element) {
        Object hoverText = getText(element);
        if (hoverText instanceof String) {
            String text = (String) hoverText;
            if (text.isEmpty()) {
                return Collections.emptyList();
            } else if (text.contains("\n")) {
                return Arrays.stream(text.split("\n")).map(net.minecraft.network.chat.Component::literal).collect(Collectors.toList());
            }
            return Collections.singletonList(Component.literal(text));
        }
        else if (hoverText instanceof String[]) {
            List<String> lines = splitNewLines(Arrays.asList((String[]) hoverText));
            if (lines.isEmpty() || (lines.size() == 1 && lines.get(0).isEmpty())) return Collections.emptyList();
            return lines.stream().map(net.minecraft.network.chat.Component::literal).collect(Collectors.toList());
        }
        else if (hoverText instanceof List) {
            return splitNewLines((List<String>) hoverText).stream().map(net.minecraft.network.chat.Component::literal).collect(Collectors.toList());
        }
        else if (hoverText instanceof Component) {
            return Collections.singletonList(((Component) hoverText));
        }
        return Collections.emptyList();
    }

    static List<String> splitNewLines(Collection<String> collection) {
        List<String> list = new ArrayList<>();
        for (String s : collection) {
            if (s.contains("\n")) {
                list.addAll(Arrays.asList(s.split("\n")));
            }
            else {
                list.add(s);
            }
        }

        return list;
    }
}
