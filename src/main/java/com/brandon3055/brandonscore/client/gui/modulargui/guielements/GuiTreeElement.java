package com.brandon3055.brandonscore.client.gui.modulargui.guielements;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;

import java.util.LinkedList;

/**
 * Created by brandon3055 on 1/23/2018.
 */
public class GuiTreeElement extends GuiScrollElement {
    private LinkedList<TreeNode> rootElements = new LinkedList<>();
    private int nodeSpacing = 1;

    public GuiTreeElement() {}

    public GuiTreeElement(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public GuiTreeElement(int xPos, int yPos, int xSize, int ySize) {
        super(xPos, yPos, xSize, ySize);
    }

    @Override
    public void addChildElements() {
        super.addChildElements();
        setListMode(ListMode.VERTICAL);
        setStandardScrollBehavior();
        useAbsoluteElementSize(true);
        getVerticalScrollBar().setHidden(true);
    }

    public LinkedList<TreeNode> getRootElements() {
        return rootElements;
    }

    public GuiTreeElement setNodeSpacing(int nodeSpacing) {
        this.nodeSpacing = nodeSpacing;
        return this;
    }

    public void updateTree() {
        rootElements.forEach(TreeNode::updateNode);
    }

    /**
     * Adds a root element to this map and returns the node representing that element.
     * The elements width and x position must be handles my the implementor.
     * The element can be any height and its y position will be handled automatically.
     *
     * @param element the element to add.
     * @return the node representing the element that was added.
     */
    public TreeNode addRootNode(MGuiElementBase element) {
        TreeNode node = new TreeNode(element, this);
        rootElements.add(node);
        addElement(element);
        updateTree();
        return node;
    }

    public void removeRootNode(TreeNode node) {
        if (rootElements.contains(node)) {
            rootElements.remove(node);
            removeElement(node.element);
        }
    }

    public static class TreeNode {
        public MGuiElementBase element;
        private GuiTreeElement guiElement;
        public LinkedList<TreeNode> branches = new LinkedList<>();
        public boolean extended = false;

        public TreeNode(MGuiElementBase element, GuiTreeElement guiElement) {
            this.element = element;
            this.guiElement = guiElement;
            this.element.reportYSizeChange = true;
        }

        public boolean isExtended() {
            return extended;
        }

        public void setExtended(boolean extended) {
            setExtended(extended, true);
        }

        public void setExtended(boolean extended, boolean updateTree) {
            this.extended = extended;
            updateNode();
            if (updateTree) {
                guiElement.updateTree();
            }
            element.ySizeChanged(element);
        }

        public TreeNode addSubNode(MGuiElementBase element) {
            TreeNode node = new TreeNode(element, guiElement);
            branches.add(node);
            this.element.addChild(element);
            updateNode();
            guiElement.updateTree();
            return node;
        }

        public void removeNode(TreeNode node) {
            branches.remove(node);
            element.removeChild(node.element);
        }

        public void updateNode() {
            int yPos = element.maxYPos() + guiElement.nodeSpacing;

            for (TreeNode node : branches) {
                MGuiElementBase nodeElement = node.element;
                nodeElement.setEnabled(extended);
                nodeElement.setYPos(yPos);
                yPos = yPos + nodeElement.getEnclosingRect().height + guiElement.nodeSpacing;
            }

            branches.forEach(TreeNode::updateNode);
        }

        //xOffset is relative to the left of the element so should be a negative value
        //to put it to the left of the element.
        public GuiButton addDefaultExtendButton(int xOffset, int yOffset, int xSize, int ySize) {
            GuiButton button = new GuiButton();
            button.setTrim(false).setInsets(0, 0, 0, 0);
            button.setDisplaySupplier(() -> extended ? "\u25BC" : "\u25B6");
            button.setTextColour(0);
            button.setButtonListener(() -> setExtended(!extended));
            button.setSize(xSize, ySize).setRelPos(element, xOffset, yOffset);
            button.setEnabledCallback(() -> !branches.isEmpty());
            element.addChild(button);
            return button;
        }
    }
}
