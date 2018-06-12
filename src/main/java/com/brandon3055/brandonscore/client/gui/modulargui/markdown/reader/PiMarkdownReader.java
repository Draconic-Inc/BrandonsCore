package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.*;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.HAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.HAlign.LEFT;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.HAlign.RIGHT;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class PiMarkdownReader {

    private static String S = "\u00A7";
    private static String tagMatchReg = "(?<=[^\\\\]|^)(" + S + "%1$s\\[[^" + S + "]*]\\{[^" + S + "]*})|(?<=[^\\\\]|^)(" + S + "%1$s\\[[^" + S + " ]*])|(?<=[^\\\\]|^)(" + S + "%1$s\\{[^" + S + " ]*})";
    private static String constructorMatchReg = "(?<=" + S + "%1$s\\[)(.*)(?=][{])|(?<=" + S + "%1$s\\[)(.*)(?=])";
    private static String propMatchReg = "(?<=]\\{)(.*)(?=})|(?<=" + S + "%1$s\\{)(.*)(?=})";
    private static String propSplit = "(?!\\B\"[^\"]*),(?![^\"]*\"\\B)";
    private static String alignReg = "(?<=[^\\\\]|^)(" + S + "align:%1$s)";
//    private static final Pattern shadowOnPat = Pattern.compile("(?<=[^\\\\]|^)(" + S + "shadow:on)");
//    private static final Pattern shadowOffPat = Pattern.compile("(?<=[^\\\\]|^)(" + S + "shadow:off)");

    private static Pattern obf = Pattern.compile("(?<=[^\\\\]|^)(~\\?~.*~\\?~)");
    private static Pattern bold = Pattern.compile("(?<=[^\\\\]|^)(\\*\\*.*\\*\\*)");
    private static Pattern italic = Pattern.compile("(?<=[^\\\\]|^)(\\*.*\\*)");
    private static Pattern strike = Pattern.compile("(?<=[^\\\\]|^)(~~.*~~)");
    private static Pattern tablePat = Pattern.compile("(?<=[^\\\\]|^)(" + Utils.SELECT + "table\\[[^]]*])");
    private static Pattern underline = Pattern.compile("(?<=[^\\\\]|^)(__.*__)");

    private static final String LINK = "link";
    private static final String IMG = "img";
    private static final String RECIPE = "recipe";
    private static final String STACK = "stack";
    private static final String ENTITY = "entity";
    private static final String TABLE = "table";
    private static final String RULE = "rule";
    private static final String COLOUR = "colour";

    private final List<String> markdown;

    public PiMarkdownReader(byte[] bytes) {
        this(new String(bytes).split("\n"));
    }

    public PiMarkdownReader(String[] lines) {
        this(Arrays.asList(lines));
    }

    public PiMarkdownReader(List<String> lines) {
        markdown = ImmutableList.copyOf(lines);
    }

    public void accept(MarkdownVisitor visitor) {
        LinkedList<String> markdownLines = new LinkedList<>(markdown);
        visitor.startVisit();

        while (!markdownLines.isEmpty()) {
            String currentLine = markdownLines.removeFirst();
            visitor.startLine();

            if (currentLine.startsWith("\\//")) {
                currentLine = currentLine.replaceFirst("\\\\", "");
            }
            else if (currentLine.startsWith("//")) {
                visitor.visitComment(currentLine.replaceFirst("//", ""));
                continue;
            }

            Matcher matcher;

            //All that needs to happen here for each thing.
            //-Finds a match for the tag
            //-Extracts the tag's constructor parameter if it has one
            //-Calls the visit method for that tag
            //-Reads all properties and passes them to the property visitor

            while (currentLine.length() > 0) {
                int nextPart = currentLine.length();

                if ((matcher = getTagMatcher(LINK, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptLink(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(IMG, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptImg(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(RECIPE, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptRecipe(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(STACK, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptStack(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(ENTITY, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptEntity(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(RULE, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        acceptRule(visitor, matcher.group());
                        currentLine = matcher.replaceFirst("");
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if ((matcher = getTagMatcher(TABLE, currentLine)).find()) { //Done? Maybe?
                    int start = matcher.start();
                    if (start == 0) {
                        String tagString = matcher.group();
                        TableVisitor tableVisitor = visitor.visitTable();
                        String[] props = extractProps(TABLE, tagString);
                        acceptProps(tableVisitor, props, false);
                        currentLine = matcher.replaceFirst("");

                        if (!currentLine.isEmpty()) { //There shouldn't be anything after the table tag but if there is then skip it.
                            visitor.visitSkipped(currentLine);
                            currentLine = "";
                        }

                        if (markdownLines.size() < 2) {
                            continue;
                        }

                        String nextLine = markdownLines.getFirst();
                        TableDefinition definition = checkDelimiter(nextLine);
                        if (definition != null) {
                            markdownLines.removeFirst();
                        }
                        else if ((definition = checkDelimiter(markdownLines.get(1))) != null) {
                            definition.hasHeading = true;
                            markdownLines.remove(1);
                        }
                        else {
                            continue;//Do deliminator found so table is invalid
                        }

                        tableVisitor.visitTableDefinition(definition);

                        int row = 0;
                        while ((nextLine = markdownLines.getFirst()).startsWith("|") && nextLine.length() > 2 && nextLine.substring(1).contains("|")) {
                            tableVisitor.visitTableRow(nextLine);
                            nextLine = nextLine.trim();

                            if (nextLine.startsWith("|") && nextLine.length() > 1) {
                                nextLine = nextLine.substring(1);
                            }
                            if (nextLine.endsWith("|") && nextLine.length() > 1) {
                                nextLine = nextLine.substring(0, nextLine.length() - 1);
                            }
                            if (!nextLine.contains("|")) {
                                break; //We have found the end of the table..
                            }

                            String[] columns = nextLine.split("\\|");
                            for (int column = 0; column < columns.length; column++) {
                                String cell = columns[column];
                                if (cell.startsWith(" ") && cell.length() > 1) cell = cell.substring(1);
                                if (cell.endsWith(" ") && cell.length() > 1) cell = cell.substring(0, cell.length() - 1);

                                MarkdownVisitor cellVisitor = tableVisitor.getCellVisitor(row, column);
                                new PiMarkdownReader(cell.getBytes()).accept(cellVisitor);
                            }

                            markdownLines.removeFirst();
                            row ++;
                        }

                        tableVisitor.endVisit();
                        continue;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                //Check Heading
                String nextLine = markdownLines.size() > 1 ? markdownLines.get(1) : "";
                if (currentLine.startsWith("#") || isAllChar(nextLine, '=') || isAllChar(nextLine, '-')) {
                    int headingType = 0;
                    while (headingType < currentLine.length() && currentLine.charAt(headingType) == '#') headingType++;

                    if (headingType == 0) {
                        headingType = isAllChar(nextLine, '=') ? 1 : 2;
                        visitor.visitHeading(applyTextFormatting(currentLine), headingType, true);
                        markdownLines.removeFirst();
                        currentLine = "";
                        continue;
                    }
                    else {
                        visitor.visitHeading(trim(applyTextFormatting(currentLine), '#'), headingType, true);
                        currentLine = "";
                        continue;
                    }
                }

                //Check for alignment tag
                boolean formatLine = false;
                if ((matcher = getTagMatcher(COLOUR, currentLine)).find()) { //Done
                    int start = matcher.start();
                    if (start == 0) {
                        String tagString = matcher.group();
                        try {
                            acceptColour(visitor, tagString);
                        }
                        catch (Exception e) {
                            visitor.visitError("[An error occurred while reading colour tag]: " + e.toString());
                            visitor.visitSkipped(tagString);
                        }
                        currentLine = matcher.replaceFirst("");
                        if (currentLine.trim().isEmpty()) {
                            visitor.visitSkipped(currentLine);
                            currentLine = "";
                        }
                        formatLine = true;
                    }
                    else if (start > 0 && start < nextPart) {
                        nextPart = start;
                    }
                }
                if (getMatcher("left", alignReg, currentLine).find()) {
                    visitor.visitAlignment(LEFT);
                    currentLine = currentLine.replace("" + S + "align:left", "");
                    formatLine = true;
                }
                else if (getMatcher("center", alignReg, currentLine).find()) {
                    visitor.visitAlignment(CENTER);
                    currentLine = currentLine.replace("" + S + "align:center", "");
                    formatLine = true;
                }
                else if (getMatcher("right", alignReg, currentLine).find()) {
                    visitor.visitAlignment(RIGHT);
                    currentLine = currentLine.replace("" + S + "align:right", "");
                    formatLine = true;
                }
//                if (shadowOnPat.matcher(currentLine).find()) {
//                    visitor.visitShadow(true);
//                    currentLine = currentLine.replace("" + S + "shadow:on", "");
//                    formatLine = true;
//                }
//                if (shadowOffPat.matcher(currentLine).find()) {
//                    visitor.visitShadow(false);
//                    currentLine = currentLine.replace("" + S + "shadow:off", "");
//                    formatLine = true;
//                }

                if (formatLine) {
                    if (!currentLine.isEmpty()) {
                        while (currentLine.startsWith(" ") && currentLine.length() > 1) {
                            currentLine = currentLine.substring(1);
                            visitor.visitSkipped(" ");
                        }
                    }

                    if (currentLine.trim().isEmpty()) {
                        visitor.visitSkipped(currentLine);
                        currentLine = "";
                    }
                    continue;
                }

                String text = currentLine.substring(0, nextPart);
                currentLine = currentLine.substring(nextPart);
                visitor.visitText(applyTextFormatting(text));

            }
            visitor.endLine();
        }

        visitor.endVisit();
    }

    private void acceptLink(MarkdownVisitor visitor, String tagString) {
        String constructor = getConstructor(LINK, tagString);
        LinkVisitor propVisitor = visitor.visitLink(constructor);
        String[] props = extractProps(LINK, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptImg(MarkdownVisitor visitor, String tagString) {
        String constructor = getConstructor(IMG, tagString);
        ImageVisitor propVisitor = visitor.visitImage(constructor);
        String[] props = extractProps(IMG, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptRecipe(MarkdownVisitor visitor, String tagString) {
        String constructor = getConstructor(RECIPE, tagString);
        RecipeVisitor propVisitor = visitor.visitRecipe(constructor);
        String[] props = extractProps(RECIPE, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptStack(MarkdownVisitor visitor, String tagString) {
        String constructor = getConstructor(STACK, tagString);
        StackVisitor propVisitor = visitor.visitStack(constructor);
        String[] props = extractProps(STACK, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptEntity(MarkdownVisitor visitor, String tagString) {
        String constructor = getConstructor(ENTITY, tagString);
        EntityVisitor propVisitor = visitor.visitEntity(constructor);
        String[] props = extractProps(ENTITY, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptRule(MarkdownVisitor visitor, String tagString) {
        RuleVisitor propVisitor = visitor.visitRule();
        String[] props = extractProps(RULE, tagString);
        acceptProps(propVisitor, props, true);
    }

    private void acceptColour(MarkdownVisitor visitor, String tagString) throws Exception {
        String constructor = getConstructor(COLOUR, tagString);
        visitor.visitColour(readColour(constructor));
    }

    private void acceptProps(PropertyVisitor visitor, String[] props, boolean endVisit) {
        visitor.startVisit();
        for (String prop : props) {
            if (!prop.contains(":")) {
                visitor.visitInvalid(prop, "Invalid property");
                continue;
            }

            String name = prop.substring(0, prop.indexOf(":"));
            String value = prop.substring(prop.indexOf(":") + 1);
            if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 2) {
                value = value.substring(1).substring(0, value.length() - 2);
            }

            boolean isr = value.endsWith("%");

            try {
                switch (name) {
                    case "size":
                        visitor.visitSize(Integer.parseInt(isr ? value.replace("%", "") : value), isr);
                        break;
                    case "width":
                        visitor.visitWidth(Integer.parseInt(isr ? value.replace("%", "") : value), isr);
                        break;
                    case "height":
                        visitor.visitHeight(Integer.parseInt(value));
                        break;
                    case "tooltip":
                        List<String> toolTip = new ArrayList<>();
                        if (!value.isEmpty()) {
                            if (value.contains("\\n")) {
                                toolTip.addAll(Arrays.asList(value.split("(\\\\n)")));
                            }
                            else {
                                toolTip.add(value);
                            }
                            visitor.visitTooltip(toolTip);
                        }
                        break;
                    case "enable_tooltip":
                        visitor.visitEnabledTooltip(parseBoolean(value));
                        break;
                    case "align":
                        switch (value.toLowerCase()) {
                            case "left":
                                visitor.visitAlignment(LEFT);
                                break;
                            case "center":
                                visitor.visitAlignment(CENTER);
                                break;
                            case "right":
                                visitor.visitAlignment(RIGHT);
                                break;
                            default:
                                throw new Exception("Invalid alignment! expected left, center or right but got " + value);
                        }
                        break;
                    case "vert_align":
                        switch (value.toLowerCase()) {
                            case "top":
                                visitor.visitVertAlign(VAlign.TOP);
                                break;
                            case "middle":
                                visitor.visitVertAlign(VAlign.MIDDLE);
                                break;
                            case "bottom":
                                visitor.visitVertAlign(VAlign.BOTTOM);
                                break;
                            default:
                                throw new Exception("Invalid vertical alignment! expected top, middle or bottom but got " + value);
                        }
                        break;
                    case "colour":
                        visitor.visitColour(readColour(value));
                        break;
                    case "colour_hover":
                        visitor.visitColourHover(readColour(value));
                        break;
                    case "border_colour":
                        visitor.visitBorderColour(readColour(value));
                        break;
                    case "border_colour_hover":
                        visitor.visitBorderColourHover(readColour(value));
                        break;
                    case "padding":
                        visitor.visitPadding(Integer.parseInt(value));
                        break;
                    case "left_pad":
                        visitor.visitLeftPad(Integer.parseInt(value));
                        break;
                    case "right_pad":
                        visitor.visitRightPad(Integer.parseInt(value));
                        break;
                    case "top_pad":
                        visitor.visitTopPad(Integer.parseInt(value));
                        break;
                    case "bottom_pad":
                        visitor.visitBottomPad(Integer.parseInt(value));
                        break;
                    //link
                    case "link_style":
                        ((LinkVisitor) visitor).visitLinkStyle(value);
                        break;
                    case "alt_text":
                        ((LinkVisitor) visitor).visitAltText(value);
                        break;
                    //image
                    case "link_to":
                        ((ImageVisitor) visitor).visitLinkTo(value);
                        break;
                    //recipe
                    case "spacing":
                        ((RecipeVisitor) visitor).visitSpacing(Integer.parseInt(value));
                        break;
                    //stack
                    case "draw_slot":
                        ((StackVisitor) visitor).visitDrawSlot(parseBoolean(value));
                        break;
                    //entity
                    case "x_offset":
                        ((EntityVisitor) visitor).visitXOffset(Integer.parseInt(value));
                        break;
                    case "y_offset":
                        ((EntityVisitor) visitor).visitYOffset(Integer.parseInt(value));
                        break;
                    case "rotate_speed":
                        ((EntityVisitor) visitor).visitRotateSpeed(Double.parseDouble(value));
                        break;
                    case "rotation":
                        ((EntityVisitor) visitor).visitRotation(Double.parseDouble(value));
                        break;
                    case "scale":
                        ((EntityVisitor) visitor).visitScale(Double.parseDouble(value));
                        break;
                    case "track_mouse":
                        ((EntityVisitor) visitor).visitTrackMouse(parseBoolean(value));
                        break;
                    case "draw_name":
                        ((EntityVisitor) visitor).visitDrawName(parseBoolean(value));
                        break;
                    case "main_hand":
                        ((EntityVisitor) visitor).visitMainHand(value);
                        break;
                    case "off_hand":
                        ((EntityVisitor) visitor).visitOffHand(value);
                        break;
                    case "head":
                        ((EntityVisitor) visitor).visitHead(value);
                        break;
                    case "chest":
                        ((EntityVisitor) visitor).visitChest(value);
                        break;
                    case "legs":
                        ((EntityVisitor) visitor).visitLegs(value);
                        break;
                    case "boots":
                        ((EntityVisitor) visitor).visitBoots(value);
                        break;
                    //table
                    case "heading_colour":
                        ((TableVisitor) visitor).visitHeadingColour(readColour(value));
                        break;
                    case "rows":
                        ((TableVisitor) visitor).visitRows(Integer.parseInt(value));
                        break;
                    case "columns":
                        ((TableVisitor) visitor).visitColumns(Integer.parseInt(value));
                        break;
                    case "render_cells":
                        ((TableVisitor) visitor).visitRenderCells(parseBoolean(value));
                        break;
                    default:
                        visitor.visitInvalid(prop, "Unknown property");
                }
            }
            catch (ClassCastException e) {
                visitor.visitInvalid(prop, PropertyVisitor.NOT_SUPPORTED);
            }
            catch (Exception e) {
                visitor.visitInvalid(prop, e.getMessage());
            }
        }
        if (endVisit) {
            visitor.endVisit();
        }
    }


//    public Pattern getTagPat(String tag) {
//        return Pattern.compile(String.format(tagMatchReg, tag));
//    }

//    public boolean hasTag(String tag, String line) {
//        Pattern pattern = Pattern.compile(String.format(tagMatchReg, tag));
//        Matcher matcher = pattern.matcher(line);
//        if (matcher.find()) {
//            return matcher.start() == 0;
//        }
//        return false;
//    }

    public Matcher getTagMatcher(String tag, String line) {
        Pattern pattern = Pattern.compile(String.format(tagMatchReg, tag));
        return pattern.matcher(line);
    }

    public Matcher getMatcher(String tag, String regex, String line) {
        Pattern pattern = Pattern.compile(String.format(regex, tag));
        return pattern.matcher(line);
    }

    public Matcher getConstructorMatcher(String tag, String tagString) {
        Pattern pattern = Pattern.compile(String.format(constructorMatchReg, tag));
        return pattern.matcher(tagString);
    }

    public String getConstructor(String tag, String tagString) {
        Matcher constructorMatch = getConstructorMatcher(tag, tagString);
        return constructorMatch.find() ? constructorMatch.group() : "";
    }

    public String[] extractProps(String tag, String tagString) {
        Pattern pattern = Pattern.compile(String.format(propMatchReg, tag));
        Matcher matcher = pattern.matcher(tagString);

        if (matcher.find()) {
            String rawProps = matcher.group();
            return rawProps.split(propSplit);
        }

        return new String[0];
    }

    //Because i want to throw an exception if the value is not true or false!
    public boolean parseBoolean(String value) throws Exception {
        if (value.equals("true")) return true;
        else if (value.equals("false")) return false;
        else throw new Exception("Expected boolean got " + value);
    }

    public static int readColour(String input) throws Exception {
        if (input.startsWith("0x") || input.startsWith("#")) {
            input = input.replace("0x", "").replace("#", "");
            return parseHex(input, false);
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

    public static int parseHex(String s, boolean catchException) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        return (int) Long.parseLong(s, 16);
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

    /**
     * Trims the specified character from the start of the string.
     */
    public static String trim(String input, char target) {
        if (input.length() == 0) {
            return input;
        }

        int start = 0;
        while (start + 1 < input.length() && input.charAt(start) == target) start++;
        if (input.charAt(start) == ' ' && start + 1 < input.length()) start++;

        return input.substring(start);
    }

    private TableDefinition checkDelimiter(String line) {
        TableDefinition definition = new TableDefinition();
        line = line.trim();

        if (line.length() < 2) {
            return null;
        }

        if (line.startsWith("|")) {
            line = line.substring(1);
        }

        String[] divs = line.split("\\|");

        for (String div : divs) {
            boolean leftColon = false;
            div = div.trim();
            if (div.length() == 0) {
                return null;
            }

            if (div.startsWith(":")) {
                leftColon = true;
                div = div.substring(1);
                if (div.length() == 0) {
                    return null;
                }
            }

            if (div.startsWith("n")) {
                boolean rightBinding = false;
                if (div.endsWith(":")) {
                    div = div.substring(0, div.length() - 1);
                    rightBinding = true;
                }

                try {
                    definition.addColumn(Integer.parseInt(div.substring(1)), true, getColumnAlign(leftColon, rightBinding));
//                    awMaps.add(new ColumnData(GuiAlign.fromBindings(leftColon, rightBinding), Integer.parseInt(div.substring(1)), true));
                }
                catch (Exception e) {
//                    LogHelperBC.error("Error reading Delimiter with fixed column width. " + e.getMessage());
                    return null;
                }
            }
            else {
                int index = 0;
                while (index < div.length()) {
                    char charAt = div.charAt(index);
                    if (charAt != '-' && charAt != ' ' && charAt != ':') {
                        return null;
                    }
                    if (charAt == ' ' && index + 1 < div.length()) {
                        return null;
                    }
                    if (charAt == ' ' || charAt == ':' || index + 1 == div.length()) {
                        definition.addColumn(Math.max(index, 1), false, getColumnAlign(leftColon, charAt == ':'));
//                        awMaps.add(new ColumnData(GuiAlign.fromBindings(leftColon, charAt == ':'), Math.max(index, 1)));
                        break;
                    }
                    index++;
                }
            }
        }

        return definition;
    }

    public static HAlign getColumnAlign(boolean leftBind, boolean rightBind) {
        if (leftBind) {
            return rightBind ? CENTER : LEFT;
        }

        return rightBind ? RIGHT : CENTER;
    }

    private static String applyTextFormatting(String input) {
        int escape = 0;
        while (bold.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l").replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l");
        }

        while (italic.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*)", "" + Utils.SELECT + "o").replaceFirst("(\\*)", "" + Utils.SELECT + "o");
        }

        while (underline.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(__)", "" + Utils.SELECT + "n").replaceFirst("(__)", "" + Utils.SELECT + "n");
        }

        while (strike.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~~)", "" + Utils.SELECT + "m").replaceFirst("(~~)", "" + Utils.SELECT + "m");
        }

        while (obf.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~\\?~)", "" + Utils.SELECT + "k").replaceFirst("(~\\?~)", "" + Utils.SELECT + "k");
        }

        if (escape >= 1000) {
            LogHelperBC.dev("Escape!");
        }
        return input;
    }
}