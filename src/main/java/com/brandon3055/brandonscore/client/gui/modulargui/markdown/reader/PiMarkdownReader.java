package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.CellData;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.TableDefinition;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.*;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign.*;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign.*;

/**
 * Created by brandon3055 on 5/30/2018.
 *
 * This is a reader for the modified markdown language used by {@link com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory}
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

            final String thisLine = currentLine;
            try {
                while (currentLine.length() > 0) {
                    int nextPart = currentLine.length();

                    //region Basic Tags
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
                    //endregion

                    //region Table Tags
                    if ((matcher = getTagMatcher(TABLE, currentLine)).find()) { //Done? Maybe?
                        int start = matcher.start();
                        if (start == 0) {
                            String tagString = matcher.group();
                            currentLine = matcher.replaceFirst("");

                            if (markdownLines.size() < 2) {
                                continue;
                            }

                            //Extract the table definition
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
                                //Check / parse XML table
                                if (!nextLine.trim().startsWith("<table")) {
                                    continue;
                                }

                                //Create a copy so an un-closed table wont eat the entire page.
                                LinkedList<String> linesCopy = new LinkedList<>(markdownLines);
                                TableHelper helper = new TableHelper();
                                String tableXML = helper.parseXML(linesCopy, true);

                                if (!helper.tableClosed) {
                                    visitor.visitError("XML table is missing its closing tag! \"</table>\" (must be at the start of the line)");
                                    continue;
                                }

                                readXMLTableRows(visitor, tableXML, tagString, currentLine);
                                currentLine = "";

                                //Strip out the table xml
                                new TableHelper().parseXML(markdownLines, true);

                                continue;
                            }

                            //Parse normal table

                            TableVisitor tableVisitor = visitor.visitTable(definition);
                            String[] props = extractProps(TABLE, tagString);
                            acceptProps(tableVisitor, props, false);

                            if (!currentLine.isEmpty()) { //There shouldn't be anything after the table tag but if there is then skip it.
                                visitor.visitSkipped(currentLine);
                                currentLine = "";
                            }

                            readMDTableRows(tableVisitor, markdownLines, definition);
                            tableVisitor.endVisit();
                            continue;
                        }
                        else if (start > 0 && start < nextPart) {
                            nextPart = start;
                        }
                    }
                    //endregion

                    //region Formatting and text
                    String nextLine = markdownLines.size() > 1 ? markdownLines.getFirst() : "";
                    if (currentLine.startsWith("#") || isAllChar(nextLine, '=') || isAllChar(nextLine, '-')) {
                        int headingType = 0;
                        while (headingType < currentLine.length() && currentLine.charAt(headingType) == '#')
                            headingType++;

                        if (headingType == 0) {
                            headingType = isAllChar(nextLine, '=') ? 1 : 2;
                            visitor.visitHeading(currentLine, headingType, true);
                            markdownLines.removeFirst();
                            currentLine = "";
                            continue;
                        }
                        else {
                            visitor.visitHeading(trim(currentLine, '#'), headingType, false);
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
                    visitor.visitText(text);
                    //endregion
                }
                visitor.endLine();
            }
            catch (Throwable e) {
                e.printStackTrace();
                visitor.visitError("En exception was thrown while reading line: " + thisLine);
            }
        }


        visitor.endVisit();
    }

    //Table Parsing

    private void readMDTableRows(TableVisitor tableVisitor, LinkedList<String> markdownLines, TableDefinition definition) {
        int row = 0;
        String nextLine;
        while (!markdownLines.isEmpty() && (nextLine = markdownLines.getFirst()).startsWith("|") && nextLine.length() > 2 && nextLine.substring(1).contains("|")) {
            tableVisitor.visitTableRow(nextLine);
            nextLine = nextLine.trim();

            if (!nextLine.contains("|")) {
                break; //We have found the end of the table..
            }
            if (nextLine.startsWith("|") && nextLine.length() > 1) {
                nextLine = nextLine.substring(1);
            }
            if (nextLine.endsWith("|") && nextLine.length() > 1) {
                nextLine = nextLine.substring(0, nextLine.length() - 1);
            }

            String[] cells = nextLine.split("\\|");
            for (int cellIndex = 0; cellIndex < cells.length; cellIndex++) {
                String cell = cells[cellIndex];
                if (cell.startsWith(" ") && cell.length() > 1) cell = cell.substring(1);
                if (cell.endsWith(" ") && cell.length() > 1) cell = cell.substring(0, cell.length() - 1);

                CellData data = new CellData(cellIndex, row);

                if (cellIndex < definition.columns.size()) {
                    data.hAlign = definition.columns.get(cellIndex).align;
                }

                MarkdownVisitor cellVisitor = tableVisitor.getCellVisitor(data);
                new PiMarkdownReader(cell.getBytes()).accept(cellVisitor);
            }

            markdownLines.removeFirst();
            row++;
        }
    }

    private void readXMLTableRows(MarkdownVisitor visitor, String rawXML, String tagString, String currentLine) {
        rawXML = rawXML.substring(0, rawXML.length() - 1);
        TableVisitor tableVisitor = null;
        try {
            TableDefinition definition = new TableDefinition(true);
            List<XMLTableElement.Row> rows = readTableXML(rawXML, definition);
            tableVisitor = visitor.visitTable(definition);
            List<CellData> cellDataList = new ArrayList<>();
            int maxColumn = 0;

            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                XMLTableElement.Row row = rows.get(rowIndex);
                tableVisitor.visitXMLTableRow(rowIndex, row);
                for (int columnIndex = 0; columnIndex < row.cells.size(); columnIndex++) {
                    XMLTableElement.Cell cell = row.cells.get(columnIndex);
                    maxColumn = Math.max(maxColumn, columnIndex);
                    cellDataList.add(cell.getCellData(columnIndex, rowIndex));
                }
            }

            if (maxColumn >= definition.columns.size()) {
                throw new TableReadException("Layout Error! The table has " + (maxColumn + 1) + " columns\n" + "but you have only defined " + definition.columns.size() + " in the column_layout attribute!");
            }

            String[] props = extractProps(TABLE, tagString);
            acceptProps(tableVisitor, props, false);

            if (!currentLine.isEmpty()) { //There shouldn't be anything after the table tag but if there is then skip it.
                visitor.visitSkipped(currentLine);
            }

            for (CellData data: cellDataList) {
                MarkdownVisitor cellVisitor = tableVisitor.getCellVisitor(data);
                new PiMarkdownReader(data.readerCellContent.getBytes()).accept(cellVisitor);
            }
        }
        catch (TableReadException e) {
            visitor.visitError("An error occurred while reading XML table!");
            visitor.visitError(e.getMessage() == null ? e.toString() : e.getMessage());
        }
        catch (Throwable e) {
            visitor.visitError("An error occurred while reading XML table!");
            visitor.visitError(e.toString());
        }

        if (tableVisitor != null) {
            tableVisitor.endVisit();
        }
    }

    private List<XMLTableElement.Row> readTableXML(String rawXML, TableDefinition definition) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        ByteArrayInputStream is = new ByteArrayInputStream(rawXML.getBytes());
        Document document = builder.parse(new InputSource(new InputStreamReader(is))); //because pare(is) will try to read the is as UTF-8 but the bytes were already decoded when read from disk so this just breaks (but only on windows)
        Element tableE = document.getDocumentElement();
        tableE.getTagName();

        boolean layoutSet = false;
        if (tableE.hasAttribute("column_layout")) {
            String layoutString = tableE.getAttribute("column_layout");
            String[] columns = layoutString.split(",");
            for (String column: columns) {
                boolean dynamic = column.endsWith("*");
                column = column.replace("*", "");
                int width;
                try { width = Integer.parseInt(column); }
                catch (NumberFormatException e) {
                    throw new TableReadException("Invalid column layout specified! \"" + layoutString + "\"\n" + //
                            "A valid layout should look something like this\n" + //
                            "\"50,1*,2*\" * indicates the width of the column is dynamic relative to the other dynamic columns\n" + //
                            "The number of column width's specified must match the number of columns in the table.");
                }
                definition.addColumn(width, !dynamic, LEFT);
            }
            layoutSet = true;
        }

        List<XMLTableElement.Row> rows = new ArrayList<>();
        NodeList rowNodes = tableE.getChildNodes();
        for (int row = 0; row < rowNodes.getLength(); row++) {
            Node node = rowNodes.item(row);
            if (node instanceof Element) {
                rows.add(new XMLTableElement.Row((Element) node));
            }
        }

        if (!layoutSet) {
            int columns = 0;
            for (XMLTableElement.Row row: rows) {
                columns = Math.max(columns, row.cells.size());
            }
            for (int i = 0; i < columns; i++) {
                definition.addColumn(1, true, LEFT);
            }
        }

        return rows;
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
        for (String prop: props) {
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
                                visitor.visitVertAlign(TOP);
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
                    case "animate":
                        ((EntityVisitor) visitor).visitAnimate(parseBoolean(value));
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
            if (vals.length != 3)
                throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
            int r = vals[0].contains(".") ? (int) (Double.parseDouble(vals[0]) * 255) : Integer.parseInt(vals[0]);
            int g = vals[1].contains(".") ? (int) (Double.parseDouble(vals[1]) * 255) : Integer.parseInt(vals[0]);
            int b = vals[2].contains(".") ? (int) (Double.parseDouble(vals[2]) * 255) : Integer.parseInt(vals[0]);
            return r << 16 | g << 8 | b;
        }
        else {
            throw new NumberFormatException("Number must be a hex using the format 0xRRGGBB or #RRGGBB");
        }
    }

    public static int parseHex(String s, boolean catchException) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        return (int) Long.parseLong(s, 16);
    }

    public static boolean isAllChar(String input, char target) {
        if (input.length() == 0) {
            return false;
        }
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

    private TableDefinition checkDelimiter(String input) {
        TableDefinition definition = new TableDefinition(false);
        String line = input.trim();

        if (line.length() < 2) {
            return null;
        }

        if (line.startsWith("|")) {
            line = line.substring(1);
        }

        String[] divs = line.split("\\|");

        for (String div: divs) {
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
                }
                catch (Exception e) {
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
                        break;
                    }
                    index++;
                }
            }
        }

        return definition.setRawMDDeliminator(input);
    }

    public static HAlign getColumnAlign(boolean leftBind, boolean rightBind) {
        if (leftBind) {
            return rightBind ? CENTER : LEFT;
        }

        return rightBind ? RIGHT : CENTER;
    }

    //For internal xml parsing use only
    public static class XMLTableElement {
        public int colour = 0;
        public boolean colourSet = false;
        public int lPad = -9999;
        public int rPad = -9999;
        public int tPad = -9999;
        public int bPad = -9999;
        public HAlign hAlign = null;
        public VAlign vAlign = null;

        protected void readAttributes(Element element, XMLTableElement parent) throws Exception {
            if (parent != null) {
                this.colour = parent.colour;
                this.colourSet = parent.colourSet;
                this.lPad = parent.lPad;
                this.rPad = parent.rPad;
                this.tPad = parent.tPad;
                this.bPad = parent.bPad;
                this.hAlign = parent.hAlign;
                this.vAlign = parent.vAlign;
            }

            if (element.hasAttribute("colour")) {
                colourSet = true;
                colour = readColour(element.getAttribute("colour"));
            }
            if (element.hasAttribute("padding")) {
                String paddingAttrib = element.getAttribute("padding");
                String[] sValues = paddingAttrib.split(",");
                int[] values = new int[sValues.length];
                for (int i = 0; i < sValues.length; i++) {
                    values[i] = Integer.parseInt(sValues[i]);
                }

                try {
                    tPad = values[0];
                    rPad = values.length > 1 ? values[1] : values[0];
                    bPad = values.length > 2 ? values[2] : values[0];
                    lPad = values.length == 4 ? values[3] : values.length > 1 ? values[1] : values[0];
                }
                catch (Throwable e) {
                    throw new TableReadException("Invalid padding attribute: \"" + paddingAttrib + "\"\n" + //
                            "Valid formats are \"n\", \"n,n\", \"n,n,n\" or \"n,n,n,n\" Where n is the padding value as an integer.\n" +
                            "The values specified apply to:\n" +
                            "\"<all sides>\", \"<top & bottom>,<right & left>\",\n" +
                            "\"<top>,<right & left>,<bottom>\" or \"<top>,<right>,<bottom>,<left>\"");
                }
            }
            if (element.hasAttribute("align")) {
                String alignString = element.getAttribute("align");
                if (alignString.contains("left")) {
                    hAlign = LEFT;
                    alignString = alignString.replace("left", "");
                }
                else if (alignString.contains("center")) {
                    hAlign = CENTER;
                    alignString = alignString.replace("center", "");
                }
                else if (alignString.contains("right")) {
                    hAlign = RIGHT;
                    alignString = alignString.replace("right", "");
                }

                if (alignString.contains("top")) {
                    vAlign = TOP;
                    alignString = alignString.replace("top", "");
                }
                else if (alignString.contains("middle")) {
                    vAlign = MIDDLE;
                    alignString = alignString.replace("middle", "");
                }
                else if (alignString.contains("bottom")) {
                    vAlign = BOTTOM;
                    alignString = alignString.replace("bottom", "");
                }

                alignString = alignString.replaceAll(" ", "").replaceAll("-", "");
                if (!alignString.isEmpty()) {
                    throw new TableReadException("Detected invalid characters in alignment attribute: " + alignString + //
                            "\nAnd example of a valid alignment is \"top\" or \"left\" or \"top left\" or \"bottom-right\"\n" + //
                            "Valid horizontal alignments are left, center and right\n" + //
                            "Valid vertical alignments are tob, middle and bottom");
                }
            }
        }

        public static class Row extends XMLTableElement {
            public List<Cell> cells = new ArrayList<>();

            public Row(Element rowE) throws Exception {
                String tag = rowE.getTagName();
                if (!tag.equals("tr")) {
                    throw new TableReadException("Found <" + tag + "> Tag where <tr> tag was expected!");
                }
                readAttributes(rowE, null);

                NodeList cellNodes = rowE.getChildNodes();
                for (int cell = 0; cell < cellNodes.getLength(); cell++) {
                    Node node = cellNodes.item(cell);
                    if (node instanceof Element) {
                        cells.add(new XMLTableElement.Cell(this, (Element) node));
                    }
                }
            }
        }

        private static class Cell extends XMLTableElement {
            public String cellContent;

            public Cell(Row row, Element cellE) throws Exception {
                String tag = cellE.getTagName();
                if (!tag.equals("td")) {
                    if (tag.equals("th")) {
                        throw new TableReadException("<th> tag is not supported as it is not needed. If you want to format this as a heading cell then just do so via cell/row colour");
                    }
                    else {
                        throw new TableReadException("Found <" + tag + "> Tag where <td> tag was expected!");
                    }
                }
                readAttributes(cellE, row);

                cellContent = cellE.getTextContent();
                if (cellContent.startsWith("\n") && cellContent.length() > 1) {
                    cellContent = cellContent.substring(1);
                }
                while (cellContent.endsWith("\t") && cellContent.length() > 1) {
                    cellContent = cellContent.substring(0, cellContent.length() - 1);
                }
                if (cellContent.endsWith("\n") && cellContent.length() > 1) {
                    cellContent = cellContent.substring(0, cellContent.length() - 1);
                }
            }

            public CellData getCellData(int column, int row) {
                CellData data = new CellData(column, row);
                data.colour = colour;
                data.colourSet = colourSet;
                data.lPad = lPad;
                data.rPad = rPad;
                data.tPad = tPad;
                data.bPad = bPad;
                if (hAlign != null) {
                    data.hAlign = hAlign;
                }
                if (vAlign != null){
                    data.vAlign = vAlign;
                }
                data.readerCellContent = cellContent;
                return data;
            }
        }

    }

    protected static class TableReadException extends Exception {
        public TableReadException(String message) {
            super(message);
        }
    }

    private static class TableHelper {
        public boolean tableClosed = false;

        public String parseXML(LinkedList<String> markdownLines, boolean isRoot) {
            StringBuilder xmlBuilder = new StringBuilder();

            //Need to do this to avoid a stack overflow because the first line will always be an opening table element.
            String next = markdownLines.removeFirst();
            xmlBuilder.append(next).append("\n");

            do {
                if (markdownLines.isEmpty()) {
                    return xmlBuilder.toString();
                }
                next = markdownLines.removeFirst();
                if (next.trim().startsWith("<table")) {
                    markdownLines.addFirst(next);
                    next = new TableHelper().parseXML(markdownLines, false);
                }
                xmlBuilder.append(next).append("\n");
            }
            while (!next.trim().endsWith("</table>"));
            tableClosed = true;
            String xml= xmlBuilder.toString();

            if (!isRoot) {
                xml = StringEscapeUtils.escapeXml11(xml);
            }

            return xml;
        }

    }
}