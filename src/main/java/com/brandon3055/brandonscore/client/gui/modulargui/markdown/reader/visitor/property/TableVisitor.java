package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.PiMarkdownReader.XMLTableElement.Row;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.CellData;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class TableVisitor extends PropertyVisitor {

    @Override
    public abstract void visitColour(int argb);

    @Override
    public abstract void visitBorderColour(int argb);

    public abstract void visitHeadingColour(int argb);

    @Override
    public abstract void visitWidth(int width, boolean screenRelative);

    @Override
    public abstract void visitHeight(int height);

    @Override
    public abstract void visitVertAlign(VAlign vertAlignment);

    public abstract void visitRenderCells(boolean renderCells);

    //Called for each row with the raw row string (just in case its needed)
    public abstract void visitTableRow(String row);

    public void visitXMLTableRow(int row, Row rowData) {}

    @Override
    public abstract void visitLeftPad(int leftPadding);

    @Override
    public abstract void visitRightPad(int rightPadding);

    @Override
    public abstract void visitTopPad(int topPadding);

    @Override
    public abstract void visitBottomPad(int bottomPadding);

    public abstract MarkdownVisitor getCellVisitor(CellData cell);
}
