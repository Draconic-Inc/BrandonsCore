package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines the table layout, specifically the number of columns and the width or relative width of each column
 */
public class TableDefinition {
    /**
     * Used by the basic markdown type table
     * Indicates that the first line of the table is a heading line
     * */
    public boolean hasHeading = false;
    public List<ColumnDef> columns = new LinkedList<>();
    public boolean isXML;

    public TableDefinition(boolean isXML) {
        this.isXML = isXML;
    }

    public void addColumn(int width, boolean fixedWidth, HAlign align) {
        columns.add(new ColumnDef(width, fixedWidth, align));
    }

    public static class ColumnDef {
        public int width;
        public boolean fixedWidth;
        public HAlign align;

        public ColumnDef(int width, boolean fixedWidth, HAlign align) {
            this.width = width;
            this.fixedWidth = fixedWidth;
            this.align = align;
        }
    }
}