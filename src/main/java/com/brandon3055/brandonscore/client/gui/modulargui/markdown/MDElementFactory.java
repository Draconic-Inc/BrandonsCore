package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.TableDefinition;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.MarkdownVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.*;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.visitorimpl.*;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.utils.Utils;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements.MarkerElement.Type.NEW_LINE;

/**
 * Created by brandon3055 on 5/31/2018.
 */
public class MDElementFactory extends MarkdownVisitor {

    private static Pattern obf = Pattern.compile("(?<=[^\\\\]|^)(~\\?~.*~\\?~)");
    private static Pattern bold = Pattern.compile("(?<=[^\\\\]|^)(\\*\\*.*\\*\\*)");
    private static Pattern italic = Pattern.compile("(?<=[^\\\\]|^)(\\*.*\\*)");
    private static Pattern strike = Pattern.compile("(?<=[^\\\\]|^)(~~.*~~)");
    private static Pattern tablePat = Pattern.compile("(?<=[^\\\\]|^)(" + Utils.SELECT + "table\\[[^]]*])");
    private static Pattern underline = Pattern.compile("(?<=[^\\\\]|^)(__.*__)");

    private MDElementContainer container;

    private boolean colourOverridden = false;
    private Supplier<Integer> colourSupplier = () -> 0x000000;
    private boolean shadow = false;
    private int colourOverride = 0;
    private int lineElements = 0;
    private boolean formatLine = false;

    /**
     * Will only allow a maximum of 1 blank line between elements
     */
    public boolean truncateNewLines = true;

    public MDElementFactory(MDElementContainer container) {
        this.container = container;
        container.lastFactory = this;
    }

    @Override
    public void startVisit() {

    }

    @Override
    public void startLine() {

    }

    @Override
    public StackVisitor visitStack(String stackString) {
        StackElement element = new StackElement(stackString);
        StackVisitor visitor = new StackVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public RecipeVisitor visitRecipe(String stackString) {
        RecipeElement element = new RecipeElement(stackString);
        RecipeVisitor visitor = new RecipeVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public ImageVisitor visitImage(String imageURL) {
        ImageElement element = new ImageElement(container, imageURL);
        ImageVisitor visitor = new ImageVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public LinkVisitor visitLink(String linkTarget) {
        LinkElement element = new LinkElement(container, linkTarget);
        LinkVisitor visitor = new LinkVisitorImpl(element);
        element.shadow = shadow;
        element.defaultColour = colourOverridden ? () -> colourOverride : colourSupplier;
        addElement(element);
        return visitor;
    }

    @Override
    public EntityVisitor visitEntity(String regName) {
        EntityElement element = new EntityElement(regName);
        EntityVisitor visitor = new EntityVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public RuleVisitor visitRule() {
        RuleElement element = new RuleElement();
        RuleVisitor visitor = new RuleVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public TableVisitor visitTable(TableDefinition definition) {
        TableElement element = new TableElement(container, definition);
        TableVisitor visitor = new TableVisitorImpl(element);
        addElement(element);
        return visitor;
    }

    @Override
    public void visitHeading(String text, int heading, boolean underlineDefinition) { //First Pass
        text = applyTextFormatting(text);
        TextElement element = new TextElement(text, heading);
        element.colour = colourOverridden ? () -> colourOverride : colourSupplier;
        element.shadow = shadow;
        addElement(element);
    }

    @Override
    public void visitText(String text) { //First Pass
        text = applyTextFormatting(text);
        TextElement element = new TextElement(text, 0);
        element.colour = colourOverridden ? () -> colourOverride : colourSupplier;
        element.shadow = shadow;
        addElement(element);
    }

    @Override
    public void visitAlignment(HAlign alignment) {
        addElement(MarkerElement.forAlignment(alignment));
    }

    @Override
    public void visitShadow(boolean enable) {
        shadow = enable;
        formatLine = true;
    }

    @Override
    public void visitColour(int argb) {
        colourOverride = argb;
        colourOverridden = true;
        formatLine = true;
    }

    @Override
    public void visitComment(String comment) {

    }

    @Override
    public void visitSkipped(String skipped) {

    }

    @Override
    public void endLine() {
        if (lineElements > 0) {
            colourOverridden = false;
            formatLine = false;
            lineElements = 0;
        }
        else if (formatLine) {
            return;
        }

        List<MDElementBase> eList = container.getElements();
        if (!truncateNewLines || eList.size() < 2 || !(MarkerElement.isNewLine(eList.get(eList.size() - 1)) && MarkerElement.isNewLine(eList.get(eList.size() - 2)))) {
            addElement(new MarkerElement(NEW_LINE));
        }
    }

    @Override
    public void endVisit() {

    }

    private void addElement(MDElementBase element) {
        if (!(element instanceof MarkerElement)) {
            lineElements++;
        }
        if (!element.getError().isEmpty()) {
            addErrorElement(element);
        }
        else {
            container.addElement(element);
        }
    }

    private void addErrorElement(MDElementBase erredElement) {
        TextElement error = new TextElement(erredElement.getError(), 0);
        error.colour = () -> 0xFF0000;
        addElement(error);
    }

    @Override
    public void visitError(String errorMessage) {
        TextElement error = new TextElement("\n" + errorMessage + "\n", 0);
        error.colour = () -> 0xFF0000;
        addElement(error);
    }

    public void setColourSupplier(Supplier<Integer> colourSupplier) {
        this.colourSupplier = colourSupplier;
    }

    public void inherit(MDElementFactory parentFactory) {
        this.colourSupplier = parentFactory.colourSupplier;
    }

    private static String applyTextFormatting(String input) {
        input = input.replaceAll("\t", "");
        int escape = 0;
        while (bold.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l").replaceFirst("(\\*\\*)", "" + Utils.SELECT + "l");
        }

        while (italic.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(\\*)", "" + Utils.SELECT + "o").replaceFirst("(\\*)", "" + Utils.SELECT + "o");
        }

        while (underline.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(__)", "" + Utils.SELECT + "n").replaceFirst("(__)", "" + Utils.SELECT + "n");
        }

        while (strike.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~~)", "" + Utils.SELECT + "m").replaceFirst("(~~)", "" + Utils.SELECT + "m");
        }

        while (obf.matcher(input).find() && escape++ < 1000) {
            input = input.replaceFirst("(~\\?~)", "" + Utils.SELECT + "k").replaceFirst("(~\\?~)", "" + Utils.SELECT + "k");
        }

        if (escape >= 1000) {
            LogHelperBC.dev("Escape!");
        }
        return input;
    }
}
