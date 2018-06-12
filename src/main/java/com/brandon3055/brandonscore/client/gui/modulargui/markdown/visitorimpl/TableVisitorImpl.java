package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementFactory;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.TableElement;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableDefinition;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.VAlign;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public class TableVisitorImpl extends TableVisitor {

    private TableElement element;

    public TableVisitorImpl(TableElement element) {
        this.element = element;
    }

    @Override
    public void visitBorderColour(int argb) {
        element.setColourBorder(argb);
    }

    @Override
    public void visitHeadingColour(int argb) {
        element.headingColour = argb;
        element.colourHeading = true;
    }

    @Override
    public void visitRows(int rows) {
        element.rows = rows;
    }

    @Override
    public void visitColumns(int columns) {
        element.columns = columns;
    }

    @Override
    public void visitWidth(int width, boolean screenRelative) {
        element.width = width;
        element.screenRelativeSize = screenRelative;
    }

    @Override
    public void visitAlignment(HAlign alignment) {
        element.hAlign = alignment;
    }

    @Override
    public void visitVertAlign(VAlign vertAlignment) {
        element.vAlign = vertAlignment;
    }

    @Override
    public void visitRenderCells(boolean renderCells) {
        element.renderCells = renderCells;
    }

    @Override
    public void visitTableRow(String row) {

    }

    @Override
    public void visitTableDefinition(TableDefinition definition) {
        element.definition = definition;
    }

    @Override
    public MarkdownVisitor getCellVisitor(int row, int column) {
        return new MDElementFactory(new MDElementContainer(element, 100));//todo
    }

    @Override
    public void endVisit() {
        element.invalidProps.putAll(invalidCalls);
    }
}
