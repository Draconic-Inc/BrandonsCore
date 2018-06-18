package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib;

/**
 * Created by brandon3055 on 6/16/2018.
 */
public class CellData {
    public final int column;
    public final int row;
    public int colour = 0;
    public boolean colourSet = false;
    public int lPad = -9999;
    public int rPad = -9999;
    public int tPad = -9999;
    public int bPad = -9999;
    public HAlign hAlign = HAlign.LEFT;
    public VAlign vAlign = null;
    //For internal use by the reader
    public String readerCellContent;

    public CellData(int column, int row) {
        this.column = column;
        this.row = row;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CellData && (obj == this || (((CellData) obj).column == column && ((CellData) obj).row == row));
    }
}
