package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.Vertical;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.RIGHT;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.Vertical.BOTTOM;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.Vertical.MIDDLE;
import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement.profiler;

/**
 * Created by brandon3055 on 20/07/2017.
 * §table[]
 * Parameters:
 * width                    - Total width of the table
 * render:true/false        - Enable/disable the table renderer to just display the content
 * vertical_align           - Vertical alignment for rows where the row height it higher than the content height
 * cell_colour              - The colour of the cells when render is enabled
 * <p>
 * <p>
 * §table[width:100%]
 * | Heading 1 | Heading 2 | Heading 3 |       //Heading line is optional
 * | :n64 | :---------: | ---------: |         //Sets the relative width of each column
 * | Content | Content | Content |             //First and last pipe are going to be enforced
 * | Content | Content | Content |             //Always splits on pipe no exceptions
 * | Content | Content | Content |             //Maby allow the addition of extra tags at the end?
 * | Content | Content | Content |
 */
public class PartContainerTable extends PartContainer {
    private static Pattern optionExtract = Pattern.compile("(?<=§table\\[)([^]]*)(?=])");

    //List of Row<Column<Part>>
    public LinkedList<LinkedList<LinkedList<Part>>> partsRows = new LinkedList<>();
    public LinkedList<LinkedList<Integer>> cellHeights = new LinkedList<>();

    //List of Column<ColumnData>
    public LinkedList<ColumnData> columnData = new LinkedList<>();
    public LinkedList<Integer> rowHeights = new LinkedList<>();
    boolean headers = false;

    public int tableWidth = 0;
    //    public int divSum = 0;
    public boolean renderCells = false;
    public int cellColour = 0xFF000000;
    public int headerColour = 0xFF00FFFF;
    public Vertical vertAlign = Vertical.TOP;

    public PartContainerTable(GuiMarkdownElement element) {
        super(element);
    }

    @Override
    public void parseMarkdown(LinkedList<String> markdownLines) {
        if (markdownLines.size() < 2) return;
        String tag = markdownLines.removeFirst();
        Matcher optionMatcher = optionExtract.matcher(tag);

        if (!optionMatcher.find()) {
            markdownLines.addFirst("[Broken Table. Invalid table tag]");
            return;
        }

        String options = optionMatcher.group();

        String parseError = readOptions(options);
        if (parseError != null) {
            markdownLines.addFirst("[Broken Table. " + parseError + "]");
            return;
        }

        if (checkDelimiter(markdownLines.getFirst())) {
            markdownLines.removeFirst();
        }
        else if (checkDelimiter(markdownLines.get(1))) {
            markdownLines.remove(1);
            headers = true;
        }
        else {
            markdownLines.addFirst("[Broken Table. Invalid table no Delimiter found!]");
            return;
        }

//        if (render) {
//            tableWidth -= 4;
//        }

        int divSum = 0;
        int availableWidth = tableWidth;
        boolean dynamic = false;
        for (ColumnData data : columnData) {
            if (data.fixedWidth) {
                availableWidth -= data.colWidth;
            }
            else {
                divSum += data.divN;
                dynamic = true;
            }
        }

        if (!dynamic) tableWidth -= availableWidth - 1;

        int offset = 0;
        for (ColumnData data : columnData) {
            if (data.fixedWidth) {
                offset += data.colWidth;
                if (renderCells) data.colWidth -= 4;
                data.colOffset = offset;
                continue;
            }
            int width = (int) (((double) data.divN / (double) divSum) * availableWidth);
            data.colOffset = offset;
            data.colWidth = width;
            if (renderCells) data.colWidth -= 4;
            offset += width;
        }

        while (markdownLines.size() > 0 && parseNextTableRow(markdownLines)) {
            markdownLines.removeFirst();
        }
    }

    private String readOptions(String options) {
        tableWidth = xSize();

        if (options.toLowerCase().contains("width")) {
            try {
                String widthString = Part.readOption(options, "width", "-1px");
                int w = Part.parseSize(xSize(), widthString);
                tableWidth = MathHelper.clip(w, 64, xSize());
            }
            catch (NumberFormatException e) {
                return "Invalid width value! Must be a number";
            }
        }

        String align = Part.readOption(options, "align", "left");
        if (align.toLowerCase().equals("center")) {
            this.align = CENTER;
        }
        else if (align.toLowerCase().equals("right")) {
            this.align = RIGHT;
        }

        String vertAlign = Part.readOption(options, "vertical_align", "top");
        if (vertAlign.toLowerCase().equals("middle")) {
            this.vertAlign = MIDDLE;
        }
        else if (vertAlign.toLowerCase().equals("bottom")) {
            this.vertAlign = BOTTOM;
        }

        renderCells = Part.readOption(options, "render", "true").equals("true");

        try {
            cellColour = 0xFF000000 | Part.readColour(Part.readOption(options, "cell_colour", "0x000000"));
        }
        catch (NumberFormatException e) {
            return  "Invalid cell colour Value! Valid formats are 0xRRGGBB or #RRGGBB (hex) or Red,Green,Blue (RGB)";
        }

        try {
            headerColour = 0xFF000000 | Part.readColour(Part.readOption(options, "heading_colour", "0x00FFFF"));
        }
        catch (NumberFormatException e) {
            return  "Invalid heading colour Value! Valid formats are 0xRRGGBB or #RRGGBB (hex) or Red,Green,Blue (RGB)";
        }

        return null;
    }

    private boolean checkDelimiter(String line) {
        LinkedList<ColumnData> awMaps = new LinkedList<>();
        line = line.trim();

        if (line.length() < 2) {
            return false;
        }

        if (line.startsWith("|")) {
            line = line.substring(1);
        }

        String[] divs = line.split("\\|");

//        LogHelperBC.dev(line + " Divs: " + divs.length);
        for (String div : divs) {
//            LogHelperBC.dev("D: " + div);
            boolean leftColon = false;
            div = div.trim();
            if (div.length() == 0) {
                return false;
            }

            if (div.startsWith(":")) {
                leftColon = true;
                div = div.substring(1);
                if (div.length() == 0) {
                    return false;
                }
            }

            if (div.startsWith("n")) {
//                LogHelperBC.dev("Fixed: "+div);
                boolean rightBinding = false;
                if (div.endsWith(":")) {
                    div = div.substring(0, div.length() - 1);
                    rightBinding = true;
                }

                try {
                    awMaps.add(new ColumnData(GuiAlign.fromBindings(leftColon, rightBinding), Integer.parseInt(div.substring(1)), true));
                }
                catch (Exception e) {
                    LogHelperBC.error("Error reading Delimiter with fixed column width. " + e.getMessage());
                    return false;
                }
            }
            else {
//                LogHelperBC.dev(div);
                int index = 0;
                while (index < div.length()) {
                    char charAt = div.charAt(index);
                    if (charAt != '-' && charAt != ' ' && charAt != ':') {
                        return false;
                    }
                    if (charAt == ' ' && index + 1 < div.length()) {
                        return false;
                    }
                    if (charAt == ' ' || charAt == ':' || index + 1 == div.length()) {
                        awMaps.add(new ColumnData(GuiAlign.fromBindings(leftColon, charAt == ':'), Math.max(index, 1)));
                        break;
                    }
                    index++;
                }
            }
        }

//
//        int index = 0;
//        boolean inDiv = false;
//        boolean leftColon = false;
//
//        int currentDiv = 0;
//
//        while (index < line.length()) {
//            char charAt = line.charAt(index);
//
//            if (inDiv) {
//                if (charAt == '-' ) {
//                    currentDiv++;
//                }
//                else if (charAt == ':' | charAt == ' ' || charAt == '|') {
//                    awMaps.add(new PairKV<>(GuiAlign.fromBindings(leftColon, charAt == ':'), currentDiv));
//                    currentDiv = 0;
//                }
//                else {
//                    return false;
//                }
//            }
//            else {
//                if (charAt == ':' || charAt == '-') {
//                    inDiv = true;
//                    leftColon = charAt == ':';
//                }
//                else if (charAt != '|' && charAt != ' '){
//                    return false;
//                }
//            }
//
//            index++;
//        }
//
//        if (inDiv) {
//            awMaps.add(new PairKV<>(GuiAlign.fromBindings(leftColon, false), currentDiv));
//        }
//
        columnData = awMaps;

        return true;
    }

    private boolean parseNextTableRow(LinkedList<String> markdownLines) {
        String line = markdownLines.getFirst();

        //region Strip out leading and trailing pipes, Split and Align.
        if (line.startsWith("|") && line.length() > 1) {
            line = line.substring(1);
        }
        if (!line.contains("|")) {
            return false;
        }

        String[] cols = line.split("\\|");

        int colLeft = xPos();
        if (align == GuiAlign.CENTER) {
            colLeft += (xSize() / 2) - (tableWidth / 2);
        }
        else if (align == GuiAlign.RIGHT) {
            colLeft += xSize() - tableWidth;
        }
        //endregion

        int builderYPos = yPos() + ySize();
        int rowYPos = builderYPos;
//        int rowHeight = 0;
        int tableHeight = 0;
        int totalCols = columnData.size();

        LinkedList<LinkedList<Part>> row = new LinkedList<>();
        LinkedList<Integer> rowCellHeights = new LinkedList<>();
        partsRows.add(row);

        for (int colIndex = 0; colIndex < cols.length && colIndex < totalCols; colIndex++) {
            String col = cols[colIndex];
            ColumnData data = columnData.get(colIndex);
            if (col.startsWith(" ") && col.length() > 1) col = col.substring(1);
            if (col.endsWith(" ") && col.length() > 1) col = col.substring(0, col.length() - 1);

            LinkedList<Part> cellParts = new LinkedList<>();
            row.add(cellParts);

            fontRenderer.resetStyles();
            BCFontRenderer.setStileToggleMode(true);

            int nextPart = col.length();
            int xPos = colLeft;
            int maxYPos = builderYPos;
            while (col.length() > 0) {
                for (IPartBuilder builder : GuiMarkdownElement.partBuilders) {
                    int i = builder.matches(col);
                    if (i == 0) {
                        builder.finalXPos = xPos;
                        builder.finalYPos = builderYPos;
                        int builderStartY = builderYPos;
                        col = builder.build(fontRenderer, col, nextPart, fontRenderer, this, cellParts, colLeft, colLeft + data.colWidth, xPos, builderYPos, maxYPos);
                        profiler.endSection();
                        nextPart = col.length();
                        xPos = builder.finalXPos;
                        builderYPos = builder.finalYPos;
                        if (builderStartY + builder.builtHeight > maxYPos) {
                            maxYPos = builderStartY + builder.builtHeight;
                        }
                        break;
                    }
                    else if (i > 0 && i < nextPart) {
                        nextPart = i;
                    }
                }
            }

            rowCellHeights.add(maxYPos - rowYPos);

            if (maxYPos > tableHeight) {
                tableHeight = maxYPos;
            }

//            if (empty) {
//                rowHeight = Math.max(rowHeight, fontRenderer.FONT_HEIGHT);
//            }
//            else {
//                rowHeight = Math.max(rowHeight, maxYPos - yPos());
//            }

            BCFontRenderer.setStileToggleMode(false);

            colLeft += data.colWidth;
        }

        if (renderCells) {
            tableHeight += 2;
        }

        rowHeights.add(tableHeight - rowYPos);
        cellHeights.add(rowCellHeights);

        if (tableHeight > maxYPos()) {
            setYSize(tableHeight - yPos());
        }

        return true;
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
        int tableXPos = xPos();
        if (align == GuiAlign.CENTER) {
            tableXPos += (xSize() / 2) - (tableWidth / 2);
        }
        else if (align == GuiAlign.RIGHT) {
            tableXPos += xSize() - tableWidth;
        }

        int renderYPos = yPos();
        int rowYPos = yPos();
        if (renderCells) {
            drawColouredRect(tableXPos, yPos(), tableWidth, 1, cellColour);
            renderYPos += 2;
            rowYPos = renderYPos;
        }

        for (int rowIndex = 0; rowIndex < partsRows.size(); rowIndex++) {
            LinkedList<LinkedList<Part>> rowCells = partsRows.get(rowIndex);
            int prevHeight = 0;
            int colLeft = tableXPos;
            int rowHeight = rowHeights.get(rowIndex);
            if (renderCells) {
                if (rowIndex == 0 && headers) {
                    drawColouredRect(colLeft + 1, yPos() + 1, tableWidth - 1, rowHeight - 1, headerColour);
                }

                drawColouredRect(colLeft, yPos(), 1, ySize(), cellColour);
                colLeft += 2;
            }

            for (int colIndex = 0; colIndex < rowCells.size(); colIndex++) {
                LinkedList<Part> cellParts = rowCells.get(colIndex);
                ColumnData data = columnData.get(colIndex);

                int cellHeight = cellHeights.get(rowIndex).get(colIndex);
                if (renderCells) cellHeight += 2;
                int yOffset = vertAlign == MIDDLE ? (rowHeight - cellHeight) / 2 : vertAlign == BOTTOM ? rowHeight - cellHeight : 0;

                int xPos = colLeft;
                fontRenderer.resetStyles();
                BCFontRenderer.setStileToggleMode(true);

                int maxWidth = data.colWidth;
                int alignOffset = 0;

                for (int i = 0; i < cellParts.size(); i++) {
                    Part part = cellParts.get(i);
                    if (xPos + part.width > colLeft + maxWidth) {
                        xPos = colLeft;
                        renderYPos += prevHeight;
                        prevHeight = 0;
                    }

                    //Calculate offset for alignment
                    if (xPos == colLeft && data.align != GuiAlign.LEFT) {
                        int width = 0;
                        for (int j = i; j < cellParts.size(); j++) {
                            Part p = cellParts.get(j);
                            if (width + p.width > maxWidth) {
                                if (width == 0) width = p.width;
                                break;
                            }
                            width += p.width;
                        }

                        alignOffset = data.align == CENTER ? (maxWidth - width) / 2 : (maxWidth - width);
                    }

                    part.lastXPos = xPos + alignOffset;
                    part.lastYPos = renderYPos;
                    part.render(fontRenderer, xPos + alignOffset, renderYPos + yOffset, mouseX, mouseY, colourProvider.getColour(), shadow, partialTicks);
//                    drawBorderedRect(xPos + alignOffset, renderYPos, part.width, part.height, 1, 0, 0xFF00FFFF);
                    prevHeight = Math.max(part.height, prevHeight);
                    xPos += part.width;
                }

                GlStateManager.color(1, 1, 1, 1);
                BCFontRenderer.setStileToggleMode(false);

                colLeft += data.colWidth;
                if (renderCells) {
                    colLeft += 4;
                    if (colIndex + 1 == rowCells.size()) {
                        drawColouredRect(tableXPos + tableWidth - 1, yPos(), 1, ySize(), cellColour);
                    }
                    else {
                        drawColouredRect(colLeft - 2, yPos(), 1, ySize(), cellColour);
                    }
                }

                renderYPos = rowYPos;
            }

            rowYPos += rowHeight;
            renderYPos = rowYPos;
            if (renderCells) {
                drawColouredRect(tableXPos, rowYPos - 2, tableWidth, 1, cellColour);
            }
        }
    }

    public static class ColumnData {
        public int colOffset = 0;
        public int colWidth = 0;
        public int divN = 0;
        boolean fixedWidth = false;
        public GuiAlign align;

        public ColumnData(GuiAlign align, int divN) {
            this.divN = divN;
            this.align = align;
        }

        public ColumnData(GuiAlign align, int colWidth, boolean fixedWidth) {
            this.colWidth = colWidth;
            this.align = align;
            this.fixedWidth = fixedWidth;
        }
    }
}
