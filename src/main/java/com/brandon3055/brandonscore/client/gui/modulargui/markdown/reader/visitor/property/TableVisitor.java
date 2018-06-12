package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class TableVisitor extends PropertyVisitor {

    @Override
    public abstract void visitBorderColour(int argb);

    public abstract void visitHeadingColour(int argb);

    public abstract void visitRows(int rows);

    public abstract void visitColumns(int columns);

    @Override
    public abstract void visitWidth(int width, boolean screenRelative);

    @Override
    public abstract void visitAlignment(HAlign alignment);

    @Override
    public abstract void visitVertAlign(VAlign vertAlignment);

    public abstract void visitRenderCells(boolean renderCells);

    //Called for each row with the raw row string (just in case its needed)
    public abstract void visitTableRow(String row);

    public abstract void visitTableDefinition(TableDefinition definition);

    //Override this if you need to know when the table being read is an xmtable
    public void visitXMTable() {}

    public abstract MarkdownVisitor getCellVisitor(int row, int column);
}
