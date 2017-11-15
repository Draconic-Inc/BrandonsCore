package com.brandon3055.brandonscore.client.gui.modulargui;

import com.brandon3055.brandonscore.client.BCClientEventHandler;
import com.brandon3055.brandonscore.client.gui.effects.GuiEffect;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
import com.brandon3055.brandonscore.client.gui.modulargui.guielements.*;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiEvent;
import com.brandon3055.brandonscore.client.gui.modulargui.lib.IGuiEventListener;
import com.brandon3055.brandonscore.lib.StackReference;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.client.gui.modulargui.markdown.GuiMarkdownElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement.ListMode.VERT_LOCK_POS_WIDTH;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.CENTER;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;
import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.TextRotation.ROT_CC;

/**
 * Created by brandon3055 on 2/07/2017.
 */
public class ModularGuiExample extends ModularGuiScreen implements IGuiEventListener {

    public ModularGuiExample() {
        super(550, 300);
    }

    @Override
    public void addElements(GuiElementManager manager) {

        //Add a blank grey background to the gui. The reload callback must be used to set the size because this method will not fire again if the gui scale changes.
        //Alternatively you could store a reference to the element and update its size and pos in the reloadGui method.

        GuiBorderedRect background;
        manager.add(background = new GuiBorderedRect().setFillColour(0xFF303030).addReloadCallback(guiRect -> guiRect.setPos(guiLeft(), guiTop()).setSize(xSize, ySize)));
        GuiScrollElement scrollElement = new GuiScrollElement().setRelPos(5, 5).setSize(xSize - 10, ySize - 10).setStandardScrollBehavior();
        background.addChild(scrollElement);

        //Add a white backing for the scroll element
        scrollElement.applyBackgroundElement(new GuiBorderedRect().setColours(0xFFc0c0c0, 0xFFFFFFFF));


//        String test = "option1:option,option2:op2,op3:\"Comma, Seperated,Option ,String\",Op4:op4";
//        String[] ops = test.split("(?!\\B\"[^\"]*),(?![^\"]*\"\\B)");
//        for (String s : ops) LogHelper.dev(s);

//        Pattern test = Pattern.compile("(?<=.:\")(.*)(\"=\")(.*)(?=\")");
//        Matcher testMatch = test.matcher("S:\"invalid=\"Data == t be in quotes. Things \"CAN\" be escaped \\n to allow for stuff like this \\t.\"");
//        if (testMatch.find()) {
//            LogHelper.dev("Group1: " + testMatch.group(1) + ", Group2: " + testMatch.group(2) + ", Group3: " + testMatch.group(3));
//        }
//        else { LogHelper.dev("No Match"); }


//        String test1 = " :------: | -----: | ------- |";
//        String test2 = " :------: | -----: | ------- ";
//
//        LogHelper.dev(Arrays.asList(test1.split("\\|")));
//        LogHelper.dev(Arrays.asList(test2.split("(?<=[^\\n])(\\|)")));

//        LogHelper.dev((testMatch.find() ? testMatch.group(2) : "No Match") + " " + testMatch.matches());

//        testMatch = test.matcher("S:invalid=\"Data == t be in quotes. Things \"CAN\" be escaped \\n to allow for stuff like this \\t.\"");
//        LogHelper.dev((testMatch.find() ? testMatch.group(2) : "No Match") + " " + testMatch.matches());
//
//        Pattern linkPat = Pattern.compile("(?<=[^\\\\]|^)(§link\\[[^]]*]\\{[^§]*})|(?<=[^\\\\]|^)(§link\\[[^]]*])");
//        String test1 = "§link[httpBla] Rand Text";
//        String test2 = "§link[httpBla] Rand Text §link[httpBla2]";
//        String test3 = "Rand Text §link[httpBla]{ops} Rand Text §link[httpBla2]";
//        String test4 = "Rand Text §link[httpBla] Rand Text §link[httpBla2]{ops}";
//        Matcher mat = linkPat.matcher(test1);
//        if (mat.find()) LogHelper.dev(mat.group());
//        mat = linkPat.matcher(test2);
//        if (mat.find()) LogHelper.dev(mat.group());
//        mat = linkPat.matcher(test3);
//        if (mat.find()) LogHelper.dev(mat.group());
//        mat = linkPat.matcher(test4);
//        if (mat.find()) LogHelper.dev(mat.group());


        GuiMarkdownElement element = new GuiMarkdownElement();
        element.setInsets(5, 5, 5, 5);
        element.setRelPos(scrollElement, 0, 0).setXSize(scrollElement.xSize() - 10);

        String markdown = "This is test string to test the functionality of the Markdown element.\n\n" + //
                "Random Text §link[http://www.google.com] Random Text\n" + //
                "Random Text §link[http://www.google.com]{altText:\"Alternate Link Text!\"} Random Text\n" + //
                "Random Text §link[http://www.google.com]{altText:\"Alternate Link Text!\",hover:\"This link has hover text!\"} Random Text\n" + //
                "Random Text §link[http://www.google.com]{altText:\"Alternate Link Text!\",hover:\"This link has\\nMulti-Line\\nhover text!\"} Random Text\n\n" + //
                "Random Text: §stack[minecraft:gold_block]§stack[minecraft:beacon,10]{drawSlot:true,size:64}§stack[minecraft:gold_block] §stack[minecraft:gold_block]Text\n" + //
                "§stack[minecraft:iron_sword,1,0,{ench:[0:{lvl:3s,id:21s},1:{lvl:1s,id:6s},2:{lvl:3s,id:22s},3:{lvl:5s,id:48s},4:{lvl:5s,id:16s}],RepairCost:63}]{drawSlot:true}\n" + //
                "§stack[minecraft:stone]\n\n" + //
                "§entity[player:brandon3055]{size:128,track_mouse:true,hover:\"This is the creator of this mod!\",main_hand:\"minecraft:diamond_sword,1,0,{ench:[0:{lvl:1s,id:18s}]}\",off_hand:\"minecraft:shield,1,0,{ench:[0:{lvl:1s,id:70s}]}\",chest:\"minecraft:diamond_chestplate,1,0,{ench:[0:{lvl:1s,id:3s}]}\"}" + "§entity[minecraft:ender_crystal]\n" + //
                "§entity[player:brandon3055] §entity[player:covers1624] §entity[minecraft:ender_dragon] §entity[minecraft:Pig] §entity[player:brandon3055]{size:128} §entity[player:brandon3055]{size:200} §entity[player:brandon3055]{size:256,track_mouse:true}\n" + //
                "§recipe[minecraft:gold_ingot]\n\n" + //
                "\\§align:centre \\§recipe[minecraft:iron_ingot]{padding:2,borderColour:#00ff00,spacing:10}\n" + //
                "§align:centre §recipe[minecraft:iron_ingot]{padding:2,borderColour:#00ff00,spacing:10}\n\n" + //
                "§link[http://www.google.com]{altText:\"This is a button style link!\",render:vanilla,padding:5}\n\n" +//
                "§colour[#00ff00]§link[http://www.google.com]{altText:\"This is a solid button style link!\",render:solid,borderColourHover:#00FFFF,padding:5}\n\n" + //
                "§img[http://ss.brandon3055.com/nr36p5s.png]{width:50%}§img[http://ss.brandon3055.com/wq6lvf9.png]{width:50%,borderColour:#FF0000,hover:\"Yay! Hover Text!\"}\n" +//
                "This is a new paragraph\n" + //
                "This is a new line\n\n" + //
                "This is a new paragraph.\n\n" +//
                "This is a very long single line of text. This §3line§3 is so \\§2ling\\§2 that it \\§4will not be able §7to fit §lwithin the §4screen width §8however this should§8 not be a problem because it should be automatically wrapped to the next line. The purpose of this string is to test this functionality.\n\n" + //
                "§align:centre§colour[0xff0000]\n" +//
                "Headings:\n" +//
                "§align:centre §colour[#00ff00] # Heading 1\n" + //
                "§shadow\n" + //
                "§align:centre ## Heading 2\n" + //
                "§align:centre§colour[#00ff00]### Heading 3\n" +//
                "§align:centre §colour[0.0,1.0,0.5] #### Heading 4\n" +//
                "§align:centre §colour[255,255,0] ##### Heading 5\n" +//
                "§align:centre §colour[10,2,0.5] ###### Heading 6\n" + //
                "§align:centre Plain Text\n" +//
                "Rules\n" + //
                "§rule[]\n" +//
                "§rule[colour:#700090,height:3,topPadding:2,bottomPadding:2]\n" + //
                "§rule[colour:#ff0000,height:3,topPadding:2,bottomPadding:2,width:70%,align:left]\n" +//
                "§rule[colour:#00ff00,height:3,topPadding:2,bottomPadding:2,width:100px,align:center]\n" + //
                "§rule[colour:#0000ff,height:3,topPadding:2,bottomPadding:2,width:100px,align:right]\n" + //
                "§rule[colour:#FFFFFF,height:3,topPadding:2,bottomPadding:2,width:20px,align:left]\n" + //
                "Text Formatting:\n" + //
                "*Italic*\n" + //
                "**Bold**\n" +//
                "~~Strikethrough~~\n" +//
                "~?~Obfustated~?~\n" +//
                "__Underline__\n" +//
                "__Underline *Italic*__\n" +//
                "__Underline **Bold**__\n" + //
                "__Underline **Bold *Italic***__\n" + //
                "__Underline ~~*Italic*~~__\n" + //
                "This **is** __*a* ~~test__ string **containing *a* combination~~ *of** various* different format flags\n\n" +//
                "§link[http://www.google.com] A link\n" + //
                "§link[http://www.google.com](Alternate Link Text) A link with alt text\n" +//
                "§img[http://url.png] An image\n" + //
                "§img[http://url.png] An image with options\n" + //
                "§recipe[minecraft:furnace,0,1,{}] A Recipe\n" +//
                "§entity[entity:registryName] An entity\n" +//
                "§entity[entity:registryName]{renderSize:32,hoverText:\"Hover text\",rotate:false,rotateSpeed:1,rotation:180} An entity with options\n" +//
                "§link[http://www.google.com] A link §img[http://url.png] An image §recipe[minecraft:furnace,0,1,{}] And a Recipe §link[http://www.google.com] t §recipe[minecraft:furnace,0,1,{}] §link[http://www.googlee.com] t §recipe[minecraft:furnace,0,1,{}] §link[http://www.google.com] t §link[http://www.google.com] t §entity[http://www.google.com]";


        File file = new File("C:\\Users\\brand\\Desktop\\MarkdownDemo.txt");

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            LinkedList<String> s = new LinkedList<>();

            String str;
            while ((str = reader.readLine()) != null) {
                s.add(str);
            }

            element.parseMarkdown(s);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }


        scrollElement.addElement(element);


        int yOffset = element.maxYPos() + 1600;

        EntityOtherPlayerMP player = new EntityOtherPlayerMP(mc.world, TileEntitySkull.updateGameprofile(new GameProfile(null, "brandon3055"))) {
            @Override
            public String getSkinType() {
                return super.getSkinType();
            }

            @Override
            public ResourceLocation getLocationSkin() {
                ResourceLocation resourcelocation;

                Minecraft minecraft = Minecraft.getMinecraft();
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(getGameProfile());

                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                    resourcelocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                }
                else {
                    UUID uuid = EntityPlayer.getUUID(getGameProfile());
                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
                }

                return resourcelocation;
            }

            @Override
            public boolean isWearing(EnumPlayerModelParts part) {
                return true;
            }
        };

        player.setCustomNameTag("");
        player.setAlwaysRenderNameTag(false);

        player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        player.setHeldItem(EnumHand.OFF_HAND, new ItemStack(Items.SHIELD));
        ItemStack stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
        stack.addEnchantment(Enchantments.BLAST_PROTECTION, 1);
        player.setItemStackToSlot(EntityEquipmentSlot.CHEST, stack);


        //All other components will be added to the background component. This is not required but it has the advantage of not needing to use a callback to set the position of these elements.
        //This is because when the background element's position is updated it will automatically update the position of all of its child elements.
        //I will also be using the setRelPos method to set the positions of the child elements. This simply sets the position relative to the position of the parent.

        //Note: This is not there recommended method for creating a custom element. I am just using this as a quick and dirty way of showing of the drawCustomString function in MGuiElementBase
        scrollElement.addElement(new GuiBorderedRect() {
            @Override
            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
                drawBorderedRect(xPos() + 10, yPos() + 10, 200, 30, 1, 0xFF00FF00, 0xFF00FF00);
                drawCustomString(fontRenderer, "Left Aligned String", xPos() + 10, yPos() + 10, 200, 0x000000, LEFT, GuiAlign.TextRotation.NORMAL, false, false, false);
                drawCustomString(fontRenderer, "Center Aligned String", xPos() + 10, yPos() + 20, 200, 0x000000, CENTER, GuiAlign.TextRotation.NORMAL, false, false, false);
                drawCustomString(fontRenderer, "Right Aligned String", xPos() + 10, yPos() + 30, 200, 0x000000, GuiAlign.RIGHT, GuiAlign.TextRotation.NORMAL, false, false, false);

                drawCustomString(fontRenderer, "This is a long test string meant to test the left-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 10, yPos() + 45, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.NORMAL, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the center-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 120, yPos() + 45, 100, 0xFF0000, CENTER, GuiAlign.TextRotation.NORMAL, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the right-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 230, yPos() + 45, 100, 0xFF0000, GuiAlign.RIGHT, GuiAlign.TextRotation.NORMAL, true, false, false);

                drawCustomString(fontRenderer, "This is... Upside Down...", xPos() + 230, yPos() + 10, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.ROT_180, true, false, false);


                drawCustomString(fontRenderer, "This is a long test string meant to test the CC-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 340, yPos() + 5, 100, 0xFF0000, LEFT, ROT_CC, true, false, false);
                drawCustomString(fontRenderer, "This is a long test string meant to test the C-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 480, yPos() + 5, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.ROT_C, true, false, false);

                GlStateManager.color(1, 1, 1, 1);
                try {
                    float scale = ySize() / player.height;

                    double zLevel = getRenderZLevel() + 100;
                    double posX = xPos() + (xSize() / 2D);


                    GlStateManager.enableColorMaterial();
                    GlStateManager.pushMatrix();

                    player.ticksExisted = BCClientEventHandler.elapsedTicks;


                    float rotation = 130;//(BCClientEventHandler.elapsedTicks + partialTicks) * -1.2F;
//                    player.rotationYawHead  = (BCClientEventHandler.elapsedTicks + partialTicks) * -1.2F;

                    GlStateManager.translate((float) posX, (float) yPos() + (player.height * scale) - 100, zLevel);
                    GlStateManager.scale(-scale, scale, scale);
                    GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
//                    GlStateManager.rotate(-((float) Math.atan((double) (100.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
                    RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
                    rendermanager.setPlayerViewY(rotation + 45);
                    rendermanager.setRenderShadow(false);
                    rendermanager.doRenderEntity(player, 0.0D, 0.0D, 0.0D, 0, 1.0F, false);
                    rendermanager.setRenderShadow(true);

                    GlStateManager.popMatrix();
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.disableRescaleNormal();
                    GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                    GlStateManager.disableTexture2D();
                    GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);


                }
                catch (Throwable e) {
                    LogHelperBC.error("Failed to build entity in GUI. This is not a bug there are just some entities that can not be rendered like this.");
                    LogHelperBC.error("Entity: " + player);
                    e.printStackTrace();
                }


            }
        }.setColours(0xFFFFFFFF, 0xFF00FF00).setInsetRelPos(0, yOffset).setSize(xSize - 20, 110));

        //region Buttons
        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 125 + yOffset).setSize(130, 20).setText("Vanilla Style Button").setVanillaButtonRender(true).setHoverText("With Hover Text!").setHoverTextDelay(10)); //The parent gui is automatically assigned as the event listener because it is an instance of IGuiEventListener
        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 150 + yOffset).setSize(130, 20).setText("Trim mode enabled on a vanilla button").setVanillaButtonRender(true).setHoverText("With", "A", "Hover", "Text", "Array!", TextFormatting.RED + "[Disclaimer: hover text arrays do not require each word to be on a new line.]"));
        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 175 + yOffset).setSize(130, 40).setText("A vanilla style button with wrapping enabled and left alignment").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setHoverTextArray(e -> {
            long seconds = Minecraft.getMinecraft().world.getWorldTime() / 20;
            long minutes = (int) Math.floor(seconds / 60D) % 24;
            return new String[]{"With a hover text supplier that displays world time!", "World Time: " + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60)};
        }));

        scrollElement.addElement(new GuiButton().setInsetRelPos(135, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(GuiAlign.TextRotation.ROT_CC));
        scrollElement.addElement(new GuiButton().setInsetRelPos(155, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(CENTER).setRotation(GuiAlign.TextRotation.ROT_CC));
        scrollElement.addElement(new GuiButton().setInsetRelPos(175, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(GuiAlign.TextRotation.ROT_C));
        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 175 + yOffset).setSize(130, 40).setText("Waaaaaaaat.....\nThis button toggles!").setVanillaButtonRender(true).setWrap(true).setAlignment(CENTER).setRotation(GuiAlign.TextRotation.ROT_180).setToggleMode(true));
        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 125 + yOffset).setSize(130, 45).setText("Buttons can also use a solid colour bordered rectangle as their background").setFillColour(0xFF000000).setBorderColours(0xFFFF0000, 0xFF00FF00).setWrap(true).setAlignment(LEFT).setToggleMode(true));
        scrollElement.addElement(new GuiButton().setInsetRelPos(325, 125 + yOffset).setSize(130, 80).setText("Or no background at all!\nYoy can also just apply child elements to buttons to be rendered as the background. e.g. i could use a stack icon element as a button's background").setWrap(true).setAlignment(CENTER));
        //endregion

        //region Colour Picker
        GuiButton pickColour = new GuiButton().setRelPos(0, 220 + yOffset).setSize(200, 30).setWrap(true).setText("This button opens a colour picker that sets the colour of this button.");
        pickColour.setBorderColour(0xFF000000);
        pickColour.setFillColour(0xFFFFFFFF);
        pickColour.setListener((event, eventSource) -> new GuiPickColourDialog(pickColour).setColour(pickColour.getFillColour(false, false)).setColourChangeListener(pickColour::setFillColour).showCenter());
        scrollElement.addElement(pickColour);
        //endregion

        //region Effect Renderer
        scrollElement.addElement(new GuiLabel().setInsetRelPos(210, 230 + yOffset).setSize(300, 12).setAlignment(CENTER).setLabelText("These are particles rendered with a gui effect renderer!"));
        MGuiEffectRenderer effectRenderer = new MGuiEffectRenderer().setInsetRelPos(210, 230 + +yOffset).setSize(300, 12);
        scrollElement.addElement(effectRenderer);
        scrollElement.addElement(new MGuiElementBase() {
            @Override
            public boolean onUpdate() {
                Random rand = mc.world.rand;
                //Ideally you may want to actually put some effort in to creating a popper custom particle but this will do for this demonstration.
                //For this demonstration i have just bound the vanilla particle sheet and i am randomly calcining through the SGA particles every tick.
                GuiEffect effect = new GuiEffect(xPos() + rand.nextInt(xSize()), yPos() + rand.nextInt(ySize())) {
                    @Override
                    public void onUpdate() {
                        super.onUpdate();
                        setParticleTextureIndex(224 + rand.nextInt(26));
                        setRBGColorF(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                    }
                };

                effect.setParticleTextureIndex(224 + rand.nextInt(26));
                effect.setActiveTecture(GuiEffect.VANILLA_PARTICLE_TEXTURES);
                effectRenderer.addEffect(effect);
                return false;
            }
        }.setInsetRelPos(210, 230 + +yOffset).setSize(300, 12));
        //endregion

        //region Item Stacks and entities

        //This demonstrates one way to easily manage relative element positions.
        MGuiElementBase lastElement;
        scrollElement.addElement(lastElement = new GuiLabel().setRelPosBottom(pickColour, 5, 5).setSize(0, 10).setLabelText("These are examples implementations of the item and entity renderers.").setTrim(false).setAlignment(LEFT));//In this case i am using left alignment so i can have an x size of 0
        scrollElement.addElement(lastElement = new GuiStackIcon(new StackReference("minecraft:stone")).setRelPosBottom(lastElement, 5, 5));
        scrollElement.addElement(lastElement = new GuiStackIcon(new StackReference("minecraft:enchanted_book")).setRelPosRight(lastElement, 5, 0).setSize(50, 50).setDrawHoverHighlight(true).addSlotBackground());
        scrollElement.addElement(lastElement = new GuiSlotRender().setRelPosRight(lastElement, 5, 0).setSize(50, 50));

        scrollElement.addElement(lastElement = new GuiEntityRenderer().setRelPos(lastElement, 5, 5).setSize(40, 40).setEntity(new ResourceLocation("minecraft:cow")));
        scrollElement.addElement(lastElement = new GuiEntityRenderer().setRelPosRight(lastElement, 10, 0).setSize(40, 40).setEntity(new ResourceLocation("minecraft:creeper")).setRotationSpeedMultiplier(10));
        scrollElement.addElement(new GuiEntityRenderer().setRelPosRight(lastElement, 30, -40).setSize(80, 80).setEntity(new ResourceLocation("minecraft:ender_dragon")).setRotationSpeedMultiplier(-0.5F));
        //endregion

        //region Text field and selector popups
        GuiButton tfButton = new GuiButton().setRelPosBottom(lastElement, 0, 10).setXPos(scrollElement.getInsetRect().x).setSize(200, 30).setWrap(true).setText("This button opens a popup text field that allows you to alter its text.");
        tfButton.setBorderColour(0xFF000000);
        tfButton.setFillColour(0xFF909090);
        tfButton.setListener((event, eventSource) -> new GuiTextFieldDialog(tfButton).addTextConfirmCallback(tfButton::setText).setMaxLength(128).setText(tfButton.getDisplayString()).setXSize(400).showCenter(500));
        scrollElement.addElement(tfButton);
        lastElement = tfButton;


        GuiButton selectButton = new GuiButton().setRelPosRight(lastElement, 5, 0).setSize(200, 30).setWrap(true).setText("This button opens a popup selection dialog that allows you top select a new display string.");
        selectButton.setBorderColour(0xFF000000);
        selectButton.setFillColour(0xFF909090);

        scrollElement.addElement(selectButton); //Note its important that the select button is asses before dialog creation because this initializes required fields in GuiButton that need to be passed on to the dialog.
        GuiSelectDialog<String> selectDialog = new GuiSelectDialog<String>(selectButton).setInsets(2, 2, 2, 2);
        selectDialog.setSize(100, 200);
        selectDialog.addChild(new GuiBorderedRect().setPosAndSize(selectDialog).setColours(0xFF000000, 0xFF00FFFF).setBorderWidth(2));
        selectDialog.setNoScrollBar();

        selectDialog.addItem("This is a string!");
        selectDialog.addItem("This is a different string!");
        selectDialog.addItem("This is... another different string!");
        selectDialog.addItem("Ok this is getting boring");
        selectDialog.addItem("Im just gonna generate some strings");
        for (int i = 0; i < 10; i++) selectDialog.addItem("Test String " + i);

        selectDialog.setCloseOnSelection(true);
        selectDialog.setSelectionListener(selectButton::setText);

        selectButton.setListener((event, eventSource) -> selectDialog.showCenter());


        //endregion


        //region Sub Scroll Element
        scrollElement.addElement(new GuiLabel().setInsetRelPos(5, 850 + yOffset).setSize(300, 12).setAlignment(LEFT).setLabelText("Yes. This is a scroll element inside a scroll element!"));
        GuiScrollElement scrollElement2 = new GuiScrollElement().setInsetRelPos(5, 865 + yOffset).setSize(510, 200).setStandardScrollBehavior();
        scrollElement.addElement(scrollElement2);
        scrollElement2.addElement(new MGuiElementBase().setRelPos(0, 0).setSize(500 + 200, 170 + 200));
        scrollElement2.applyBackgroundElement(new GuiBorderedRect().setColours(0xFF000000, 0xFFFF00FF));
        scrollElement2.addBackgroundChild(new GuiBorderedRect().setBorderColour(0xFF707070).setRelPos(-2, -2).setSize(514, 204));

        scrollElement2.addElement(new GuiLabel().setRelPos(5, 2).setSize(505, 10).setAlignment(LEFT).setLabelText("Oh... yea. And there is a scroll list inside this scroll element inside a scroll element..."));
        GuiScrollElement scrollList = new GuiScrollElement().setRelPos(5, 12).setSize(200, 188).setListMode(VERT_LOCK_POS_WIDTH).setStandardScrollBehavior();
        scrollElement2.addElement(scrollList);
        for (int i = 0; i < 50; i++) {
            scrollList.addElement(new GuiLabel().setSize(0, 15).setLabelText("Random Label In A List " + i));
        }
        scrollList.addBackgroundChild(new GuiBorderedRect().setPosAndSize(scrollList.getRect()).setColours(0xFF009090, 0xFF009090));
        scrollList.getVerticalScrollBar().setHidden(true);
        //endregion

    }

    @Override
    public void reloadGui() {
        super.reloadGui();
    }

    @Override
    public void onMGuiEvent(GuiEvent event, MGuiElementBase eventSource) {
        if (event.isButton() && !event.asButton().getElement().getToggleMode()) {
            mc.displayGuiScreen(new ModularGuiExample());
        }
    }
}