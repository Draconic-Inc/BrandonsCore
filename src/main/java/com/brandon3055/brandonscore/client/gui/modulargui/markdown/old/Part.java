package com.brandon3055.brandonscore.client.gui.modulargui.markdown.old;

import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 20/07/2017.
 */
public abstract class Part {
    public int width;
    public int height;
    public int lastXPos = -1;
    public int lastYPos = -1;
    public PartContainer container;
    public boolean errored = false;

    public Part(PartContainer container) {
        this.container = container;
    }

    /**
     * Extracts the specified option from the given options string.
     *
     * @param optionsString Example Input colour:0xFF0000,width:10,name:Some Name
     * @param target        Example target width
     * @return 10 or defaultOpt if the target does not exist.
     */
    public static String readOption(String optionsString, String target, String defaultOpt) {
        if (optionsString.isEmpty()) {
            return defaultOpt;
        }

        String[] ops = optionsString.split("(?!\\B\"[^\"]*),(?![^\"]*\"\\B)");

        for (String op : ops) {
            if (!op.contains(":")) {
                LogHelperBC.error("GuiMarkdownElement: Invalid option detected! Option: " + op + " Required Format: optionName:Option Value");
                return defaultOpt;
            }
            if (op.toLowerCase().startsWith(target + ":")) {
                String found = op.substring(target.length() + 1);
                if (found.startsWith("\"") && found.length() > 1) found = found.substring(1);
                if (found.endsWith("\"") && found.length() > 1) found = found.substring(0, found.length() - 1);
                return found;
            }
        }
        return defaultOpt;
    }

    public static int readColour(String input) throws NumberFormatException {
        if (input.startsWith("0x") || input.startsWith("#")) {
            input = input.replace("0x", "").replace("#", "");
            return Utils.parseHex(input, false);
        }
        else if (input.contains(",")) {
            String[] vals = input.split(",");
            if (vals.length != 3) throw new NumberFormatException();
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[0]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[0]);
            return r << 16 | g << 8 | b;
        }
        else {
            throw new NumberFormatException();
        }
    }

    public static LinkedList<String> splitOnSpace(String input) {
        LinkedList<String> subs = new LinkedList<>();

        if (input.length() == 0) {
            return subs;
        }

        int prev = 0; //Index of previous space
        int pos = 0; //The current search index;

        while (pos < input.length()) {
            char c = input.charAt(pos);

            if (c == 32 || c == 10) {
                subs.add(input.substring(prev, pos));
                prev = pos;
            }
            pos++;
        }

        subs.add(input.substring(prev, pos));

        return subs;
    }

    public static String applyTextFormatting(String input) {
        int escape = 0;
        while (GuiMarkdownElement.bold.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l").replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l");
        }

        while (GuiMarkdownElement.italic.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*)", "" + Utils.SELECT + "o").replaceFirst("(\\*)", "" + Utils.SELECT + "o");
        }

        while (GuiMarkdownElement.underline.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(__)", "" + Utils.SELECT + "n").replaceFirst("(__)", "" + Utils.SELECT + "n");
        }

        while (GuiMarkdownElement.strike.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~~)", "" + Utils.SELECT + "m").replaceFirst("(~~)", "" + Utils.SELECT + "m");
        }

        while (GuiMarkdownElement.obf.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~\\?~)", "" + Utils.SELECT + "k").replaceFirst("(~\\?~)", "" + Utils.SELECT + "k");
        }

        if (escape >= 1000) {
            LogHelperBC.dev("Escape!");
        }
        return input;
    }

    /**
     * Trims the specified character from the start and end of the string.
     */
    public static String trim(String input, char target) {
        if (input.length() == 0) {
            return input;
        }

        int start = 0;
        int end = input.length() - 1;

        while (start + 1 < input.length() && input.charAt(start) == target) start++;
        if (input.charAt(start) == ' ' && start + 1 < input.length()) start++;
        while (end >= start && input.charAt(end) == target) end--;

        return input.substring(start, end + 1);
    }

    public static boolean isAllChar(String input, char target) {
        if (input.length() == 0) return false;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) != target) {
                return false;
            }
        }
        return true;
    }

    public static void addError(LinkedList<String> markdownLines, String message, String errorValue) {
        markdownLines.removeFirst();
        markdownLines.addFirst("" + Utils.SELECT + "4Error: " + message + "" + Utils.SELECT + "4 " + errorValue);
    }

    public abstract void render(BCFontRenderer font, int xPos, int yPos, int mouseX, int mouseY, int colour, boolean shadow, float partialTicks);

    public static int parseSize(int maxWidth, String value) throws NumberFormatException {
        if (value.endsWith("%")) {
            return (int) ((Double.parseDouble(value.replace("%", "")) / 100D) * maxWidth);
        }
        else if (value.endsWith("px") || Utils.validInteger(value)) {
            return Integer.parseInt(value.replace("px", ""));
        }
        else { throw new NumberFormatException(); }
    }
}
