package com.brandon3055.brandonscore.client.gui.modulargui.lib;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.ModuleManager;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 23/10/2016.
 */
public class ModuleBuilder {


    /**
     * Used to easily arrange gui elements into a neat grid pattern.
     *
     * Pattern:
     *
     * <----- Columns ----->
     *
     * |--1--||--2--||--3--|
     * |--4--||--5--||--6--|
     * |--7--||--8--||--9--|
     * |--10-|...
     *
     */
    public static class EqualColumns {

        private final int xPos;
        private final int yPos;
        private final int columns;
        private final int elementWidth;
        private final int elementHeight;
        public int builderEndY;
        private int elementSpacing;
        private int index = 0;
        private LinkedList<MGuiElementBase> elements = new LinkedList<>();

        public EqualColumns(int xPos, int yPos, int columns, int elementWidth, int elementHeight, int elementSpacing){
            this.xPos = xPos;
            this.yPos = yPos;
            this.columns = columns;
            this.elementWidth = elementWidth;
            this.elementHeight = elementHeight;
            this.builderEndY = yPos;
            this.elementSpacing = elementSpacing;
        }

        public void add(MGuiElementBase element) {
            int column = index % columns;
            int row = index / columns;

            element.xSize = elementWidth;
            element.ySize = elementHeight;
            element.xPos = xPos + (column * elementWidth) + (column * elementSpacing);
            element.yPos = yPos + (row * elementHeight) + (row * elementSpacing);
            builderEndY = element.yPos + element.ySize;
            elements.add(element);

            index++;
        }

        public List<MGuiElementBase> finish() {
            return elements;
        }

        public void finish(ModuleManager manager, int level) {
            for (MGuiElementBase elementBase : elements) {
                manager.add(elementBase, level);
            }
        }
    }

    /**
     * Similar to the functionality of the above method except dose not set the size of the elements.
     * elementHeight should be the ySize of the tallest element.
     */
    public static class RawColumns {

        private final int xPos;
        private final int yPos;
        private final int columns;
        public int builderEndY;
        private int elementHeight;
        private int elementSpacing;
        private int index = 0;
        private int lastLevelY = 0;
        private LinkedList<MGuiElementBase> elements = new LinkedList<>();

        public RawColumns(int xPos, int yPos, int columns, int elementHeight, int elementSpacing){
            this.xPos = xPos;
            this.yPos = yPos;
            this.columns = columns;
            this.builderEndY = yPos;
            this.elementHeight = elementHeight;
            this.elementSpacing = elementSpacing;
        }

        public void add(MGuiElementBase element) {
            int column = index % columns;
            int row = index / columns;

            int columnPos = xPos;
            if (column > 0) {
                columnPos = elements.getLast().xPos + elements.getLast().xSize;
            }

            element.moveBy((columnPos + elementSpacing) - element.xPos, (yPos + (row * elementHeight) + (row * elementSpacing)) - element.yPos);

            builderEndY = element.yPos + elementHeight;
            elements.add(element);

            index++;
        }

        public List<MGuiElementBase> finish() {
            return elements;
        }

        public void finish(ModuleManager manager, int level) {
            for (MGuiElementBase elementBase : elements) {
                manager.add(elementBase, level);
            }
        }
    }
}
