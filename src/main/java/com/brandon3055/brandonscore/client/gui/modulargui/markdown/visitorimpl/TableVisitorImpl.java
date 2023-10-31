//package com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl;
//
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.MDElementContainer;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.TableElement;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.TableElement.TableCellContainer;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.CellData;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.VAlign;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableVisitor;
//
///**
// * Created by brandon3055 on 5/30/2018.
// */
//public class TableVisitorImpl extends TableVisitor {
//
//    private TableElement element;
//    //In the case of an xml table these are the default values
//    private int cellColour = 0;
//    private boolean hasCellColour = false;
////    private int borderColour = 0;
////    private boolean hasBorderColour = false;
//    private int headingColour = 0;
//    private boolean hasHeadingColour = false;
//    private int leftPadding = 0;
//    private int rightPadding = 0;
//    private int topPadding = 0;
//    private int bottomPadding = 0;
//    private VAlign vertAlignment = VAlign.TOP;
//
//    public TableVisitorImpl(TableElement element) {
//        this.element = element;
//    }
//
//    @Override
//    public void visitColour(int cellColour) {
//        this.cellColour = cellColour;
//        this.hasCellColour = true;
//    }
//
//    @Override
//    public void visitBorderColour(int borderColour) {
//        element.setColourBorder(borderColour);
//    }
//
//    @Override
//    public void visitHeadingColour(int headingColour) {
//        this.headingColour = headingColour;
//        this.hasHeadingColour = true;
//    }
//
//    @Override
//    public void visitWidth(int width, boolean screenRelative) {
//        element.width = width;
//        element.screenRelativeSize = screenRelative;
//    }
//
//    @Override
//    public void visitHeight(int height) {
//        element.height = height;
//    }
//
//    @Override
//    public void visitVertAlign(VAlign vertAlignment) {
//        this.vertAlignment = vertAlignment;
//    }
//
//    @Override
//    public void visitRenderCells(boolean renderCells) {
//        element.renderCells = renderCells;
//    }
//
//    @Override
//    public void visitTableRow(String row) {
//
//    }
//
//    @Override
//    public void visitLeftPad(int leftPadding) {
//        this.leftPadding = leftPadding;
//    }
//
//    @Override
//    public void visitRightPad(int rightPadding) {
//        this.rightPadding = rightPadding;
//    }
//
//    @Override
//    public void visitTopPad(int topPadding) {
//        this.topPadding = topPadding;
//    }
//
//    @Override
//    public void visitBottomPad(int bottomPadding) {
//        this.bottomPadding = bottomPadding;
//    }
//
//    @Override
//    public MarkdownVisitor getCellVisitor(CellData data) {
//        TableCellContainer tableCell = element.getCreateCell(data);
//        MarkdownVisitor visitor = tableCell.createContainer();
//
//        MDElementContainer container = tableCell.container;
//        int lPad = data.lPad != -9999 ? data.lPad : leftPadding;
//        int rPad = data.rPad != -9999 ? data.rPad : rightPadding;
//        int tPad = data.tPad != -9999 ? data.tPad : topPadding;
//        int bPad = data.bPad != -9999 ? data.bPad : bottomPadding;
//        container.setInsets(tPad, lPad, bPad, rPad);
//
//        if (data.colourSet) {
//            tableCell.background.setFillColour(0xFF000000 | data.colour);
//        }
//        else if (hasHeadingColour && data.row == 0) {
//            tableCell.background.setFillColour(0xFF000000 | headingColour);
//        }
//        else if (hasCellColour) {
//            tableCell.background.setFillColour(0xFF000000 | cellColour);
//        }
//
//        if (data.vAlign == null) {
//            data.vAlign = vertAlignment;
//        }
//
//        return visitor;
//    }
//
//    @Override
//    public void endVisit() {
//        element.invalidProps.addAll(invalidCalls.keySet());
//    }
//}
