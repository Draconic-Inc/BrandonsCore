package com.brandon3055.brandonscore.client.gui.modulargui.markdown;

import com.brandon3055.brandonscore.client.gui.modulargui.MGuiElementBase;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiColourProvider;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.builders.*;
import com.brandon3055.brandonscore.utils.Profiler;
import com.brandon3055.brandonscore.utils.Utils;
import net.minecraft.item.ItemStack;

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
    public static Pattern tablePat = Pattern.compile("(?<=[^\\\\]|^)(" + Utils.SELECT + "table\\[[^]]*])");
    public static Pattern underline = Pattern.compile("(?<=[^\\\\]|^)(__.*__)");

    public static Pattern colourPat = Pattern.compile("(?<=[^\\\\]|^)(" + Utils.SELECT + "colour\\[[^]]*])");
    public static Pattern colourExtractPat = Pattern.compile("(?<=" + Utils.SELECT + "colour\\[)([^]]*)(?=])");
    public static Profiler profiler = new Profiler();

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
    protected GuiColourProvider<Integer> colourProvider = () -> 0;

    public GuiMarkdownElement() {
        reportYSizeChange = true;
        profiler.enabled = false;
//
//        setLinkListener((link, integer) -> {
//            File file = new File("C:\\Users\\brand\\Desktop\\MarkdownDemo.txt");
//
//            toRemove.addAll(containers);
//            containers.clear();
//
//            try {
//                BufferedReader reader = new BufferedReader(new FileReader(file));
//                LinkedList<String> s = new LinkedList<>();
//
//                String str;
//                while ((str = reader.readLine()) != null) {
//                    s.add(str);
//                }
//
////                LogHelper.startTimer("Markdown Read");
//                parseMarkdown(s);
////                LogHelper.stopTimer();
//            }
//            catch (Throwable e) {
//                e.printStackTrace();
//            }
//        });
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
     * " + Utils.SELECT + "link[http://www.google.com]
     * " + Utils.SELECT + "link[http://www.google.com](Alternate Link Text)
     * " + Utils.SELECT + "link[http://www.google.com "Hover text for the link"](Alternate Link Text)
     * Also supports branch:branchId in place of link
     *
     * Images:
     * " + Utils.SELECT + "img[http://url.png]
     * " + Utils.SELECT + "img[http://url.png "Hover text for the image"]
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
     * " + Utils.SELECT + "colour[RGB]               Takes an integer RGB colour value
     * " + Utils.SELECT + "colour[red,green,blue]  Takes separate red, green and blue values (0-255)
     *
     * " + Utils.SELECT + "shadow
     *
     * Recipe: (Uses StackReference string format)
     * " + Utils.SELECT + "recipe[minecraft:furnace,0,1,{}]
     *
     * ItemStack: (Uses StackReference string format)
     * " + Utils.SELECT + "stack[minecraft:furnace,0,1,{}]
     * " + Utils.SELECT + "stack[minecraft:furnace,0,1,{}]{renderSize:32,toolTip:false,renderSlot:true,hoverText:"Custom hover text"}
     * Parameters can be specified in any order and all parameters are optional.
     *
     * Entity: (Rendered an entity on the screen)
     * " + Utils.SELECT + "entity[entity:registryName]
     * " + Utils.SELECT + "entity[entity:registryName]{renderSize:32,hoverText:"Hover text",rotate:false,rotateSpeed:1,rotation:180}
     *
     * Text align flag can be placed above a block of text or at the start of a block of text.
     * " + Utils.SELECT + "align:center
     * " + Utils.SELECT + "align:right
     *
     * Horizontal Rule:
     * //Rule can be used to draw a horizontal line across the page or as a precise spacer bu setting height to 0.
     * //Parameters can be specified in any order and all parameters are optional.
     * //Width parameter accepts both a % value and a px value where percent is the percent of the page width and px is a precise
     * //number of pixels. Defaults to 100%
     * " + Utils.SELECT + "rule[colour:#700090,height:5,topPadding:20,bottomPadding:20,width:70%,align:center]
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
        currentAlign = GuiAlign.LEFT;

        profiler.startSection("Parsing Markdown");
//        LogHelperBC.startTimer("Parsing Markdown " + markdownLines.size() + " Lines");
        this.mlCache = new LinkedList<>(markdownLines);
        markdownLines = new LinkedList<>(markdownLines);
        int yPos = getInsetRect().y;
        while (markdownLines.size() > 0) {
            profiler.startSection("Create Container");
            PartContainer container = createContainer(markdownLines);
            profiler.endStartSection("Populate Container");
            container.setPos(getInsetRect().x, yPos).setXSize(getInsetRect().width);
            container.parseMarkdown(markdownLines);
            yPos = container.maxYPos();
            addChild(container);

            containers.add(container);
            profiler.endSection();
        }

        setYSize((yPos - yPos()) + getInsets().bottom);
//        LogHelperBC.stopTimer();
        profiler.endSection();
        profiler.finish();

        return this;
    }

    private PartContainer createContainer(LinkedList<String> markdownLines) {
        PartContainer container;

        profiler.startSection("Get Line");
        String line = markdownLines.getFirst();
        profiler.endSection();

        if (tablePat.matcher(line).find()) {
            profiler.startSection("Create Table Container");
            container = new PartContainerTable(this);
        }
        else {
            profiler.startSection("Create Normal Container");
            container = new PartContainer(this);
        }

        profiler.endSection();

        return container;
    }

    public void reload() {
        reloadRequired = true;
    }

    @Override
    public void reloadElement() {
        super.reloadElement();
        reload();
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

    public int getBaseTextColour() {
        return colourProvider.getColour();
    }

    public void setColourProvider(GuiColourProvider<Integer> colourProvider) {
        this.colourProvider = colourProvider;
    }

    public void clear() {
        toRemove.addAll(containers);
        containers.clear();
    }
}