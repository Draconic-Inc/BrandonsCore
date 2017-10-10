package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders.*;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import net.minecraft.item.ItemStack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Created by brandon3055 on 30/06/2017.
 */
public class GuiMarkdownElement extends MGuiElementBase<GuiMarkdownElement> {

    public static Pattern obf = Pattern.compile("(?<=[^\\\\]|^)(~\\?~.*~\\?~)");
    public static Pattern bold = Pattern.compile("(?<=[^\\\\]|^)(\\*\\*.*\\*\\*)");
    public static Pattern italic = Pattern.compile("(?<=[^\\\\]|^)(\\*.*\\*)");
    public static Pattern strike = Pattern.compile("(?<=[^\\\\]|^)(~~.*~~)");
    public static Pattern tablePat = Pattern.compile("(?<=[^\\\\]|^)(§table\\[[^]]*])");
    public static Pattern underline = Pattern.compile("(?<=[^\\\\]|^)(__.*__)");

    public static Pattern colourPat = Pattern.compile("(?<=[^\\\\]|^)(§colour\\[[^]]*])");
    public static Pattern colourExtractPat = Pattern.compile("(?<=§colour\\[)([^]]*)(?=])");

    protected static LinkedList<IPartBuilder> partBuilders = new LinkedList<>();

    static {
        partBuilders.add(new PartBuilderLink());
        partBuilders.add(new PartBuilderImage());
        partBuilders.add(new PartBuilderRecipe());
        partBuilders.add(new PartBuilderStack());
        partBuilders.add(new PartBuilderEntity());

        //This is the default part builder that will accept anything its given so it must be called last.
        partBuilders.add(new PartBuilderText());
    }

    public String imageDLFailedMessage = "Image Download Failed!";
    private boolean reloadRequired = false;
    private LinkedList<String> mlCache = null;
    private List<PartContainer> containers = new ArrayList<>();
    protected BiConsumer<String, Integer> linkListener = null;
    protected BiConsumer<String, Integer> imageListener = null;
    protected BiConsumer<ItemStack, Integer> recipeListener = null;
    protected GuiAlign currentAlign = GuiAlign.LEFT;

    public GuiMarkdownElement() {
        reportYSizeChange = true;

        setLinkListener((link, integer) -> {
            File file = new File("C:\\Users\\brand\\Desktop\\MarkdownDemo.txt");

            toRemove.addAll(containers);
            containers.clear();

            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                LinkedList<String> s = new LinkedList<>();

                String str;
                while ((str = reader.readLine()) != null) {
                    s.add(str);
                }

//                LogHelper.startTimer("Markdown Read");
                parseMarkdown(s);
//                LogHelper.stopTimer();
            }
            catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * <pre>
     * ########### Markdown Keys ###########
     * Headings:
     * # H1              OR ====== (under heading text)
     * ## H2             OR ------ (under heading text)
     * ### H3
     * #### H4
     * ##### H5
     * ###### H6
     *
     * Emphasis:
     * *text*       Italic (Think i will just use *)
     * **text**     Bold   (Think i will just use *)
     * ~~text~~     Strike through
     * ~?~text~?~   Obfuscated
     * __text__     underline
     *
     * Lists:
     * Todo
     *
     * Links:
     * §link[http://www.google.com]
     * §link[http://www.google.com](Alternate Link Text)
     * §link[http://www.google.com "Hover text for the link"](Alternate Link Text)
     * Also supports branch:branchId in place of link
     *
     * Images:
     * §img[http://url.png]
     * §img[http://url.png "Hover text for the image"]
     *
     * Code:
     * Todo is this needed?
     *
     * Tables:
     * https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet#tables
     *
     * #### Custom markdown ####
     *
     * Text Colour: (Changes the colour of all following text)
     * §colour[RGB]               Takes an integer RGB colour value
     * §colour[red,green,blue]  Takes separate red, green and blue values (0-255)
     *
     * §shadow
     *
     * Recipe: (Uses StackReference string format)
     * §recipe[minecraft:furnace,0,1,{}]
     *
     * ItemStack: (Uses StackReference string format)
     * §stack[minecraft:furnace,0,1,{}]
     * §stack[minecraft:furnace,0,1,{}]{renderSize:32,toolTip:false,renderSlot:true,hoverText:"Custom hover text"}
     * Parameters can be specified in any order and all parameters are optional.
     *
     * Entity: (Rendered an entity on the screen)
     * §entity[entity:registryName]
     * §entity[entity:registryName]{renderSize:32,hoverText:"Hover text",rotate:false,rotateSpeed:1,rotation:180}
     *
     * Text align flag can be placed above a block of text or at the start of a block of text.
     * §align:center
     * §align:right
     *
     * Horizontal Rule:
     * //Rule can be used to draw a horizontal line across the page or as a precise spacer bu setting height to 0.
     * //Parameters can be specified in any order and all parameters are optional.
     * //Width parameter accepts both a % value and a px value where percent is the percent of the page width and px is a precise
     * //number of pixels. Defaults to 100%
     * §rule[colour:#700090,height:5,topPadding:20,bottomPadding:20,width:70%,align:center]
     *
     * ###################################
     */
    public GuiMarkdownElement parseMarkdown(String markdownSting) {
        String[] lines = markdownSting.split("\n");
        return parseMarkdown(lines);
    }

    public GuiMarkdownElement parseMarkdown(String[] markdownLines) {
        return parseMarkdown(new LinkedList<>(Arrays.asList(markdownLines)));
    }

    public GuiMarkdownElement parseMarkdown(LinkedList<String> markdownLines) {
        LogHelperBC.startTimer("Parsing Markdown " + markdownLines.size() + " Lines");
        this.mlCache = new LinkedList<>(markdownLines);
        int yPos = getInsetRect().y;
        while (markdownLines.size() > 0) {
            PartContainer container = createContainer(markdownLines);
            container.setPos(getInsetRect().x, yPos).setXSize(getInsetRect().width);
            container.parseMarkdown(markdownLines);
            yPos = container.maxYPos();
            addChild(container);

            containers.add(container);
        }

        setYSize(yPos - yPos());
        LogHelperBC.stopTimer();

        return this;
    }

    private PartContainer createContainer(LinkedList<String> markdownLines) {
        String line = markdownLines.getFirst();
        if (tablePat.matcher(line).find()) {
            return new PartContainerTable(this);
        }

        return new PartContainer(this);
    }

    /*

    You have a conversion modifier for every energy type.
    This modifier will be a value between 0 and 1.
    All conversion modifiers will start at 0.

    When energy is fed into the system from energy type X this will increase that energy type's conversion modifier.
    The rate at which the conversion modifier increases involves a bit of math...
    First of all there will be a base increment value that is calculated using the transfer rate and the total capacity of the network.
    The exact calculation is TBD.

    That is then multiplied by the (1 - the current conversion modifier) and added to the conversion modifier.
    So what this means is the higher the conversion modifier the more energy it takes to push it even higher.
    This is a similar mechanic to the DE reactor shield. You can pump infinite power into it but it will never actually hit 100%.

    Finally the conversion modifier for all other energy types is decremented via a calculation that is TBD

    The result of all this is you now have a list of conversion modifiers that represent the ratio of power input from each energy type.
    To calculate the efficiency for a specific energy type you would add all of the conversion ratios and divide that number by the conversion modifier for the target energy type.
    The result will be a value between 0 and 1 where 1 is 100% efficient.

    */

    public void reload() {
        reloadRequired = true;
    }

    @Override
    public boolean onUpdate() {
        if (reloadRequired) {
            reloadRequired = false;
            if (mlCache != null) {
                toRemove.addAll(containers);
                containers.clear();
                parseMarkdown(mlCache);
            }
            return true;
        }

        return super.onUpdate();
    }

    /**
     * Sets the listener that will be called when a link is clicked.
     * Values provided given to the consumer are the link URL and the mouse button pressed.
     */
    public GuiMarkdownElement setLinkListener(BiConsumer<String, Integer> linkListener) {
        this.linkListener = linkListener;
        return this;
    }

    /**
     * Sets the listener that will be called when an image is clicked.
     * Values provided given to the consumer are the image URL and the mouse button pressed.
     */
    public GuiMarkdownElement setImageListener(BiConsumer<String, Integer> imageListener) {
        this.imageListener = imageListener;
        return this;
    }

    public GuiMarkdownElement setRecipeListener(BiConsumer<ItemStack, Integer> recipeListener) {
        this.recipeListener = recipeListener;
        return this;
    }
}