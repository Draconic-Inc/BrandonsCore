package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property;

import java.util.LinkedList;
import java.util.List;

/**
 * Defines the table layout, specifically the number of columns and the width or relative width of each column
 */
public class TableDefinition {
    public boolean hasHeading = false;
    public List<ColumnDef> columns = new LinkedList<>();

    public void addColumn(int width, boolean fixedWidth, HAlign align) {
        columns.add(new ColumnDef(width, fixedWidth, align));
    }

    public static class ColumnDef {
        public int width;
        boolean fixedWidth;
        public HAlign align;

        public ColumnDef(int width, boolean fixedWidth, HAlign align) {
            this.width = width;
            this.fixedWidth = fixedWidth;
            this.align = align;
        }
    }
}