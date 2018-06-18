package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;

import codechicken.lib.math.MathHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.CellData;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.TableDefinition;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.TableDefinition.ColumnDef;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
import com.brandon3055.brandonscore.utils.DataUtils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class TableElement extends MDElementBase<TableElement> {

    public int rows = 0;
    public int columns = 0;
    public boolean renderCells = true;
    private MDElementContainer container;
    public TableDefinition definition;
    private List<TableCellContainer> tableCells = new ArrayList<>();
    private int[] rowLines = new int[0];
    private int[] columnLines = new int[0];

    public TableElement(MDElementContainer container, TableDefinition definition) {
        bottomPad = leftPad = rightPad = topPad = 1;
        this.container = container;
        this.definition = definition;
        this.width = 100;
        screenRelativeSize = true;
    }

    @Override
    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
        int width = screenRelativeSize ? (int) (MathHelper.clip(this.width / 100D, 0, 1) * layout.getWidth()) : MathHelper.clip(this.width, 0, layout.getWidth());
        rowLines = new int[rows + 1];
        columnLines = new int[columns + 1];

        int cbw = renderCells ? 1 : 0; //Cell border width
        width -= cbw * (columns + 1);

        double dynamicWidth = width;
        double dynamicRange = 0;
        boolean dynamic = false;
        for (ColumnDef data: definition.columns) {
            if (data.fixedWidth) {
                dynamicWidth -= data.width;
            }
            else {
                dynamicRange += data.width;
                dynamic = true;
            }
        }

        int[] xOffsets = new int[definition.columns.size()];
        int currentXOffset = 0;
        for (int i = 0; i < columns; i++) {
            if (i >= definition.columns.size()) {
                continue;
            }
            columnLines[i] = currentXOffset;
            currentXOffset += cbw;
            ColumnDef data = definition.columns.get(i);
            xOffsets[i] = currentXOffset;
            currentXOffset += data.fixedWidth ? data.width : (int) ((data.width / dynamicRange) * dynamicWidth);
        }
        columnLines[columnLines.length - 1] = currentXOffset;

        if (!dynamic) {
            width -= dynamicWidth;
        }

        int rowYPos = yPos() + cbw;
        int totalXSize = 0;
        int row;
        for (row = 0; row < rows; row++) {
            rowLines[row] = (rowYPos - cbw) - yPos();
            int rowHeight = height;
            //Layout Row Cells
            //List<MGuiElementBase> rowContainers = new ArrayList<>();
            for (TableCellContainer cell: tableCells) {
                if (cell.data.row != row || cell.data.column < 0 || cell.data.column >= xOffsets.length) {
                    continue;
                }

                ColumnDef column = cell.columnDef;
                int cellWidth = column.fixedWidth ? column.width : (int) ((column.width / dynamicRange) * dynamicWidth);
                cell.container.setXSize(cellWidth);
                cell.container.setPos(xPos() + xOffsets[cell.data.column], rowYPos);
                cell.container.layoutMarkdownElements();
                totalXSize = Math.max(totalXSize, cell.container.maxXPos() - xPos());
                rowHeight = Math.max(rowHeight, cell.container.ySize());
                if (!childElements.contains(cell.container)) {
                    addChild(cell.container);
                }
            }

            for (TableCellContainer cell: tableCells) {
                if (cell.data.row != row || cell.data.column < 0 || cell.data.column >= xOffsets.length) {
                    continue;
                }

                if (cell.data.vAlign != VAlign.TOP) {
                    cell.container.translate(0, (rowHeight - cell.container.ySize()) / (cell.data.vAlign == VAlign.MIDDLE ? 2 : 1));
                }

                cell.background.setYSize(rowHeight);
                cell.background.setYPos(rowYPos);
            }


            rowYPos += rowHeight + cbw;
        }
        rowLines[row] = (rowYPos - cbw) - yPos();

        width = (totalXSize) + cbw;
        setSize(width, rowYPos - yPos());
        super.layoutElement(layout, lineElement);
    }

    @Override
    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
//        if ((colourHeading || hasColour) && rowLines.length > 1) {
//            if (hasColour) {
//                drawColouredRect(xPos(), yPos(), xSize(), ySize(), 0xFF000000 | colour);
//            }
//            if (colourHeading) {
//                drawColouredRect(xPos(), yPos(), xSize(), rowLines[1], 0xFF000000 | headingColour);
//            }
//        }
        if (renderCells) {
            for (int row: rowLines) {
                drawColouredRect(xPos(), yPos() + row, xSize(), 1, 0xFF000000 | colourBorder);
            }
            for (int column: columnLines) {
                drawColouredRect(xPos() + column, yPos(), 1, ySize(), 0xFF000000 | colourBorder);
            }
        }

        //Render cell colours?
        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
        //Render cell boxes?
//        drawBorderedRect(xPos(), yPos(), xSize(), ySize(), 1, 0, 0xFF00FF00);
    }

    public TableCellContainer getCreateCell(CellData data) {
        TableCellContainer cell = DataUtils.firstMatch(tableCells, c -> c.data.equals(data));
        if (cell == null) {
            if (data.column >= 0 && data.column < definition.columns.size()) {
                cell = new TableCellContainer(data, container, definition.columns.get(data.column), definition);
            }
            else {
                cell = new TableCellContainer(data, container, new ColumnDef(10, true, HAlign.LEFT), definition);
                error("Invalid table layout! Attempt to assign cell in non-existent column");
            }

            tableCells.add(cell);

            this.columns = Math.max(this.columns, data.column + 1); //Add 1 to convert array index to count
            this.rows = Math.max(this.rows, data.row + 1);
        }
        return cell;
    }

    public static class TableCellContainer {
        public MDElementContainer container = null;
        private CellData data;
        private MDElementContainer parentContainer;
        private ColumnDef columnDef;
        public TableDefinition tableDefinition;
        public GuiBorderedRect background;

        public TableCellContainer(CellData data, MDElementContainer parentContainer, ColumnDef columnDef, TableDefinition tableDefinition) {
            this.data = data;
            this.parentContainer = parentContainer;
            this.columnDef = columnDef;
            this.tableDefinition = tableDefinition;
        }

        public MarkdownVisitor createContainer() {
            container = new MDElementContainer(parentContainer);
            container.inherit(parentContainer);
            container.defaultAlignment = data.hAlign;
            background = new GuiBorderedRect();
            background.setFillColour(0x0);
            background.setPos(container);
            background.setXSizeMod((guiBorderedRect, integer) -> container.xSize());
            container.addChild(background);

            MDElementFactory factory = new MDElementFactory(container);
            if (parentContainer.lastFactory != null) {
                factory.inherit(parentContainer.lastFactory);
            }
            return factory;
        }
    }
}
