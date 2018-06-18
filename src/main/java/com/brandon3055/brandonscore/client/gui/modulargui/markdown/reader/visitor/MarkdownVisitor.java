package com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor;

import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.HAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.lib.TableDefinition;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.TableVisitor;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.reader.visitor.property.*;

/**
 * Created by brandon3055 on 5/30/2018.
 */
public abstract class MarkdownVisitor {

    public abstract void startVisit();

    public abstract void startLine();

    public abstract StackVisitor visitStack(String stackString);

    public abstract RecipeVisitor visitRecipe(String stackString);

    public abstract ImageVisitor visitImage(String imageURL);

    public abstract LinkVisitor visitLink(String linkTarget);

    public abstract EntityVisitor visitEntity(String regName);

    public abstract RuleVisitor visitRule();

    public abstract TableVisitor visitTable(TableDefinition definition);

    public abstract void visitHeading(String headingText, int heading, boolean underlineDefinition);

    public abstract void visitText(String text);

    public abstract void visitAlignment(HAlign alignment);

    public abstract void visitShadow(boolean enable);

    public abstract void visitColour(int argb);

    public abstract void visitComment(String comment);

    //Because some blank space may be skipped when reading formatting flags.
    public abstract void visitSkipped(String skipped);

    //This only exists to display an error to the user
    public void visitError(String error) {}

    public abstract void endLine();

    public abstract void endVisit();
}
