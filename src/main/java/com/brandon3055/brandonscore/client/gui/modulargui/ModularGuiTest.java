//package com.brandon3055.brandonscore.client.gui.modulargui;
//
//import codechicken.lib.render.buffer.TransformingVertexConsumer;
//import com.brandon3055.brandonscore.api.render.GuiHelper;
//import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiButton;
//import com.brandon3055.brandonscore.client.gui.modulargui.baseelements.GuiScrollElement;
//import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiBorderedRect;
//import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiLabel;
//import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTreeElement;
//import com.brandon3055.brandonscore.client.gui.modulargui.guielements.GuiTreeElement.TreeNode;
//import com.brandon3055.brandonscore.client.render.RenderUtils;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.network.chat.Component;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//
//import java.awt.*;
//
//import static com.brandon3055.brandonscore.client.gui.modulargui.lib.GuiAlign.LEFT;
//
///**
// * Created by brandon3055 on 2/07/2017.
// */
//public class ModularGuiTest extends ModularGuiScreen {
//    public ModularGuiTest(Component titleIn) {
//        super(titleIn, 200, 200);
//    }
//
//
//
////    public ModularGuiTest() {
//////        super(550, 300);
////    }
//
//    @Override
//    public void addElements(GuiElementManager manager) {
//
//        //Add a blank grey background to the gui. The reload callback must be used to set the size because this method will not fire again if the gui scale changes.
//        //Alternatively you could store a reference to the element and update its size and pos in the reloadGui method.
//
//        GuiBorderedRect background;
//        manager.addChild(background = new GuiBorderedRect().setFillColour(0xFF303030).addReloadCallback(guiRect -> guiRect.setPos(guiLeft() + 200, guiTop()).setSize(xSize, ySize)));
//        GuiScrollElement scrollElement = new GuiScrollElement().setRelPos(5, 5).setSize(xSize - 10, ySize - 10).setStandardScrollBehavior();
//        background.addChild(scrollElement);
//
//        //Add a white backing for the scroll element
//        scrollElement.applyBackgroundElement(new GuiBorderedRect().setColours(0xFFc0c0c0, 0xFFFFFFFF));
//
//
//        GuiTreeElement tree = (GuiTreeElement) new GuiTreeElement().setRelPos(10, 10).setSize(150, 200);
//        scrollElement.addElement(tree);
//        tree.addBackgroundChild(new GuiBorderedRect().setRelPos(-1, -1).setSize(152, 202).setFillColour(0xFF505050).setBorderColour(0xFF000000));
//
//        for (int i = 0; i < 10; i++) {
//            GuiLabel rootLabel = new GuiLabel("Root Node " + i).setSize(140, 14).setRelPos(10, 0);
//            rootLabel.setAlignment(LEFT);
//            TreeNode rootNode = tree.addRootNode(rootLabel);
//            rootNode.addDefaultExtendButton(-10, 2, 10, 10);
//
//            for (int j = 0; j < 4; j++) {
//                GuiLabel Label = new GuiLabel("Sub " + i + ", Node " + j).setSize(140, 14).setRelPos(15, 0);
//                Label.setAlignment(LEFT);
//                TreeNode subNode = rootNode.addSubNode(Label);
//                subNode.addDefaultExtendButton(-10, 2, 10, 10);
//
//                for (int k = 0; k < 4; k++) {
//                    GuiButton subSubLabel = new GuiButton("Sub " + i + ", Node " + j+" - " + k).setSize(170, 14).setRelPos(15, 0);
//                    subSubLabel.setAlignment(LEFT);
//                    subSubLabel.enableVanillaRender();
//                    TreeNode subSubNode = subNode.addSubNode(subSubLabel);
//                    subSubNode.addDefaultExtendButton(-10, 2, 10, 10);
////                  subSubLabel.enableVanillaRender().setListener(() -> mc.displayGuiScreen(new ModularGuiTest()));
//                }
//            }
//        }
//
//        scrollElement.addElement(new GuiElement().setRelPos(0, 0).setSize(170, 220));
//
//        if (true) return;
//
//        //region Markdown
////        GuiMarkdownElement element = new GuiMarkdownElement();
////        element.setInsets(5, 5, 5, 5);
////        element.setRelPos(scrollElement, 0, 0).setXSize(scrollElement.xSize() - 10);
////        File file = new File("C:\\Users\\brand\\Desktop\\MarkdownDemo.txt");
////
////        try {
////            BufferedReader reader = new BufferedReader(new FileReader(file));
////            LinkedList<String> s = new LinkedList<>();
////
////            String str;
////            while ((str = reader.readLine()) != null) {
////                s.add(str);
////            }
////
////            element.parseMarkdown(s);
////        }
////        catch (Throwable e) {
////            e.printStackTrace();
////        }
//
////        scrollElement.addElement(element);
//        //endregion
//
////        int yOffset = element.maxYPos() + 1600;
////
////        EntityOtherPlayerMP player = new EntityOtherPlayerMP(mc.level, TileEntitySkull.updateGameprofile(new GameProfile(null, "brandon3055"))) {
////            @Override
////            public String getSkinType() {
////                return super.getSkinType();
////            }
////
////            @Override
////            public ResourceLocation getLocationSkin() {
////                ResourceLocation resourcelocation;
////
////                Minecraft minecraft = Minecraft.getInstance();
////                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(getGameProfile());
////
////                if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
////                    resourcelocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
////                }
////                else {
////                    UUID uuid = PlayerEntity.getUUID(getGameProfile());
////                    resourcelocation = DefaultPlayerSkin.getDefaultSkin(uuid);
////                }
////
////                return resourcelocation;
////            }
////
////            @Override
////            public boolean isWearing(EnumPlayerModelParts part) {
////                return true;
////            }
////        };
////
////        player.setCustomNameTag("");
////        player.setAlwaysRenderNameTag(false);
////
////        player.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
////        player.setHeldItem(Hand.OFF_HAND, new ItemStack(Items.SHIELD));
////        ItemStack stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
////        stack.addEnchantment(Enchantments.BLAST_PROTECTION, 1);
////        player.setItemStackToSlot(EquipmentSlotType.CHEST, stack);
////
////        //region thing
////        //All other components will be added to the background component. This is not required but it has the advantage of not needing to use a callback to set the position of these elements.
////        //This is because when the background element's position is updated it will automatically update the position of all of its child elements.
////        //I will also be using the setRelPos method to set the positions of the child elements. This simply sets the position relative to the position of the parent.
////
////        //Note: This is not there recommended method for creating a custom element. I am just using this as a quick and dirty way of showing of the drawCustomString function in MGuiElementBase
////        scrollElement.addElement(new GuiBorderedRect() {
////            @Override
////            public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
////                super.renderElement(minecraft, mouseX, mouseY, partialTicks);
////                drawBorderedRect(xPos() + 10, yPos() + 10, 200, 30, 1, 0xFF00FF00, 0xFF00FF00);
////                drawCustomString(fontRenderer, "Left Aligned String", xPos() + 10, yPos() + 10, 200, 0x000000, LEFT, GuiAlign.TextRotation.NORMAL, false, false, false);
////                drawCustomString(fontRenderer, "Center Aligned String", xPos() + 10, yPos() + 20, 200, 0x000000, CENTER, GuiAlign.TextRotation.NORMAL, false, false, false);
////                drawCustomString(fontRenderer, "Right Aligned String", xPos() + 10, yPos() + 30, 200, 0x000000, GuiAlign.RIGHT, GuiAlign.TextRotation.NORMAL, false, false, false);
////
////                drawCustomString(fontRenderer, "This is a long test string meant to test the left-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 10, yPos() + 45, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.NORMAL, true, false, false);
////                drawCustomString(fontRenderer, "This is a long test string meant to test the center-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 120, yPos() + 45, 100, 0xFF0000, CENTER, GuiAlign.TextRotation.NORMAL, true, false, false);
////                drawCustomString(fontRenderer, "This is a long test string meant to test the right-aligned split string function where a string is split over multiple lines if necessary.", xPos() + 230, yPos() + 45, 100, 0xFF0000, GuiAlign.RIGHT, GuiAlign.TextRotation.NORMAL, true, false, false);
////
////                drawCustomString(fontRenderer, "This is... Upside Down...", xPos() + 230, yPos() + 10, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.ROT_180, true, false, false);
////
////
////                drawCustomString(fontRenderer, "This is a long test string meant to test the CC-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 340, yPos() + 5, 100, 0xFF0000, LEFT, ROT_CC, true, false, false);
////                drawCustomString(fontRenderer, "This is a long test string meant to test the C-Rotated split string function where a string is split over multiple lines if necessary.", xPos() + 480, yPos() + 5, 100, 0xFF0000, LEFT, GuiAlign.TextRotation.ROT_C, true, false, false);
////
////                RenderSystem.color(1, 1, 1, 1);
////                try {
////                    float scale = ySize() / player.height;
////
////                    double zLevel = getRenderZLevel() + 100;
////                    double posX = xPos() + (xSize() / 2D);
////
////
////                    RenderSystem.enableColorMaterial();
////                    RenderSystem.pushMatrix();
////
////                    player.ticksExisted = BCClientEventHandler.elapsedTicks;
////
////
////                    float rotation = 130;//(BCClientEventHandler.elapsedTicks + partialTicks) * -1.2F;
//////                    player.rotationYawHead  = (BCClientEventHandler.elapsedTicks + partialTicks) * -1.2F;
////
////                    RenderSystem.translate((float) posX, (float) yPos() + (player.height * scale) - 100, zLevel);
////                    RenderSystem.scale(-scale, scale, scale);
////                    RenderSystem.rotate(180.0F, 0.0F, 0.0F, 1.0F);
////                    RenderSystem.rotate(rotation, 0.0F, 1.0F, 0.0F);
////                    RenderHelper.enableStandardItemLighting();
////                    RenderSystem.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
//////                    RenderSystem.rotate(-((float) Math.atan((double) (100.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
////                    RenderManager rendermanager = Minecraft.getInstance().getRenderManager();
////                    rendermanager.setPlayerViewY(rotation + 45);
////                    rendermanager.setRenderShadow(false);
////                    rendermanager.renderEntity(player, 0.0D, 0.0D, 0.0D, 0, 1.0F, false);
////                    rendermanager.setRenderShadow(true);
////
////                    RenderSystem.popMatrix();
////                    RenderHelper.disableStandardItemLighting();
////                    RenderSystem.disableRescaleNormal();
////                    RenderSystem.setActiveTexture(OpenGlHelper.lightmapTexUnit);
////                    RenderSystem.disableTexture2D();
////                    RenderSystem.setActiveTexture(OpenGlHelper.defaultTexUnit);
////
////
////                }
////                catch (Throwable e) {
////                    LogHelperBC.error("Failed to build entity in GUI. This is not a bug there are just some entities that can not be rendered like this.");
////                    LogHelperBC.error("Entity: " + player);
////                    e.printStackTrace();
////                }
////
////
////            }
////        }.setColours(0xFFFFFFFF, 0xFF00FF00).setInsetRelPos(0, yOffset).setSize(xSize - 20, 110));
////        //endregion
////
////        //region Buttons
////        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 125 + yOffset).setSize(130, 20).setText("Vanilla Style Button").setVanillaButtonRender(true).setHoverText("With Hover Text!").setHoverTextDelay(10)); //The parent gui is automatically assigned as the event listener because it is an instance of IGuiEventListener
////        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 150 + yOffset).setSize(130, 20).setText("Trim mode enabled on a vanilla button").setVanillaButtonRender(true).setHoverText("With", "A", "Hover", "Text", "Array!", TextFormatting.RED + "[Disclaimer: hover text arrays do not require each word to be on a new line.]"));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(0, 175 + yOffset).setSize(130, 40).setText("A vanilla style button with wrapping enabled and left alignment").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setHoverTextArray(e -> {
////            long seconds = Minecraft.getInstance().world.getWorldTime() / 20;
////            long minutes = (int) Math.floor(seconds / 60D) % 24;
////            return new String[]{"With a hover text supplier that displays world time!", "World Time: " + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60)};
////        }));
////
////        scrollElement.addElement(new GuiButton().setInsetRelPos(135, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(GuiAlign.TextRotation.ROT_CC));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(155, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(CENTER).setRotation(GuiAlign.TextRotation.ROT_CC));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(175, 125 + yOffset).setSize(15, 90).setText("Wat").setVanillaButtonRender(true).setWrap(true).setAlignment(LEFT).setRotation(GuiAlign.TextRotation.ROT_C));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 175 + yOffset).setSize(130, 40).setText("Waaaaaaaat.....\nThis button toggles!").setVanillaButtonRender(true).setWrap(true).setAlignment(CENTER).setRotation(GuiAlign.TextRotation.ROT_180).setToggleMode(true));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(195, 125 + yOffset).setSize(130, 45).setText("Buttons can also use a solid colour bordered rectangle as their background").setFillColour(0xFF000000).setBorderColours(0xFFFF0000, 0xFF00FF00).setWrap(true).setAlignment(LEFT).setToggleMode(true));
////        scrollElement.addElement(new GuiButton().setInsetRelPos(325, 125 + yOffset).setSize(130, 80).setText("Or no background at all!\nYoy can also just apply child elements to buttons to be rendered as the background. e.g. i could use a stack icon element as a button's background").setWrap(true).setAlignment(CENTER));
////        //endregion
////
////        //region Colour Picker
////        GuiButton pickColour = new GuiButton().setRelPos(0, 220 + yOffset).setSize(200, 30).setWrap(true).setText("This button opens a colour picker that sets the colour of this button.");
////        pickColour.setBorderColour(0xFF000000);
////        pickColour.setFillColour(0xFFFFFFFF);
////        pickColour.onPressed(() -> new GuiPickColourDialog(pickColour).setColour(pickColour.getFillColour(false, false)).setColourChangeListener(pickColour::setFillColour).showCenter());
////        scrollElement.addElement(pickColour);
////        //endregion
////
////        //region Effect Renderer
////        scrollElement.addElement(new GuiLabel().setInsetRelPos(210, 230 + yOffset).setSize(300, 12).setAlignment(CENTER).setLabelText("These are particles rendered with a gui effect renderer!"));
////        MGuiEffectRenderer effectRenderer = new MGuiEffectRenderer().setInsetRelPos(210, 230 + +yOffset).setSize(300, 12);
////        scrollElement.addElement(effectRenderer);
////        scrollElement.addElement(new GuiElement() {
////            @Override
////            public boolean onUpdate() {
////                Random rand = mc.level.rand;
////                //Ideally you may want to actually put some effort in to creating a popper custom particle but this will do for this demonstration.
////                //For this demonstration i have just bound the vanilla particle sheet and i am randomly calcining through the SGA particles every tick.
////                GuiEffect effect = new GuiEffect(mc.level,xPos() + rand.nextInt(xSize()), yPos() + rand.nextInt(ySize())) {
////                    @Override
////                    public void onUpdate() {
////                        super.onUpdate();
////                        setParticleTextureIndex(224 + rand.nextInt(26));
////                        setRBGColorF(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
////                    }
////                };
////
////                effect.setParticleTextureIndex(224 + rand.nextInt(26));
////                effectRenderer.setParticleTexture(MGuiEffectRenderer.VANILLA_PARTICLE_TEXTURES);
////                effectRenderer.addEffect(effect);
////                return false;
////            }
////        }.setInsetRelPos(210, 230 + +yOffset).setSize(300, 12));
////        //endregion
////
////        //region Item Stacks and entities
////
////        //This demonstrates one way to easily manage relative element positions.
////        GuiElement lastElement;
////        scrollElement.addElement(lastElement = new GuiLabel().setRelPosBottom(pickColour, 5, 5).setSize(0, 10).setLabelText("These are examples implementations of the item and entity renderers.").setTrim(false).setAlignment(LEFT));//In this case i am using left alignment so i can have an x size of 0
////        scrollElement.addElement(lastElement = new GuiStackIcon(new StackReference("minecraft:stone")).setRelPosBottom(lastElement, 5, 5));
////        scrollElement.addElement(lastElement = new GuiStackIcon(new StackReference("minecraft:enchanted_book")).setRelPosRight(lastElement, 5, 0).setSize(50, 50).setDrawHoverHighlight(true).addSlotBackground());
////        scrollElement.addElement(lastElement = new GuiSlotRender().setRelPosRight(lastElement, 5, 0).setSize(50, 50));
////
////        scrollElement.addElement(lastElement = new GuiEntityRenderer().setRelPos(lastElement, 5, 5).setSize(40, 40).setEntity(new ResourceLocation("minecraft:cow")));
////        scrollElement.addElement(lastElement = new GuiEntityRenderer().setRelPosRight(lastElement, 10, 0).setSize(40, 40).setEntity(new ResourceLocation("minecraft:creeper")).setRotationSpeedMultiplier(10));
////        scrollElement.addElement(new GuiEntityRenderer().setRelPosRight(lastElement, 30, -40).setSize(80, 80).setEntity(new ResourceLocation("minecraft:ender_dragon")).setRotationSpeedMultiplier(-0.5F));
////        //endregion
////
////        //region Text field and selector popups
////        GuiButton tfButton = new GuiButton().setRelPosBottom(lastElement, 0, 10).setXPos(scrollElement.getInsetRect().x).setSize(200, 30).setWrap(true).setText("This button opens a popup text field that allows you to alter its text.");
////        tfButton.setBorderColour(0xFF000000);
////        tfButton.setFillColour(0xFF909090);
////        tfButton.setButtonListener((event, eventSource) -> new GuiTextFieldDialog(tfButton).addTextConfirmCallback(tfButton::setText).setMaxLength(128).setText(tfButton.getDisplayString()).setXSize(400).showCenter(500));
////        scrollElement.addElement(tfButton);
////        lastElement = tfButton;
////
////
////        GuiButton selectButton = new GuiButton().setRelPosRight(lastElement, 5, 0).setSize(200, 30).setWrap(true).setText("This button opens a popup selection dialog that allows you top select a new display string.");
////        selectButton.setBorderColour(0xFF000000);
////        selectButton.setFillColour(0xFF909090);
////
////        scrollElement.addElement(selectButton); //Note its important that the select button is asses before dialog creation because this initializes required fields in GuiButton that need to be passed on to the dialog.
////        GuiSelectDialog<String> selectDialog = new GuiSelectDialog<String>(selectButton).setInsets(2, 2, 2, 2);
////        selectDialog.setSize(100, 200);
////        selectDialog.addChild(new GuiBorderedRect().setPosAndSize(selectDialog).setColours(0xFF000000, 0xFF00FFFF).setBorderWidth(2));
////        selectDialog.setNoScrollBar();
////
////        selectDialog.addItem("This is a string!");
////        selectDialog.addItem("This is a different string!");
////        selectDialog.addItem("This is... another different string!");
////        selectDialog.addItem("Ok this is getting boring");
////        selectDialog.addItem("Im just gonna generate some strings");
////        for (int i = 0; i < 10; i++) selectDialog.addItem("Test String " + i);
////
////        selectDialog.setCloseOnSelection(true);
////        selectDialog.setSelectionListener(selectButton::setText);
////
////        selectButton.setButtonListener((event, eventSource) -> selectDialog.showCenter());
////
////
////        //endregion
////
////        //region Sub Scroll Element
////        scrollElement.addElement(new GuiLabel().setInsetRelPos(5, 850 + yOffset).setSize(300, 12).setAlignment(LEFT).setLabelText("Yes. This is a scroll element inside a scroll element!"));
////        GuiScrollElement scrollElement2 = new GuiScrollElement().setInsetRelPos(5, 865 + yOffset).setSize(510, 200).setStandardScrollBehavior();
////        scrollElement.addElement(scrollElement2);
////        scrollElement2.addElement(new GuiElement().setRelPos(0, 0).setSize(500 + 200, 170 + 200));
////        scrollElement2.applyBackgroundElement(new GuiBorderedRect().setColours(0xFF000000, 0xFFFF00FF));
////        scrollElement2.addBackgroundChild(new GuiBorderedRect().setBorderColour(0xFF707070).setRelPos(-2, -2).setSize(514, 204));
////
////        scrollElement2.addElement(new GuiLabel().setRelPos(5, 2).setSize(505, 10).setAlignment(LEFT).setLabelText("Oh... yea. And there is a scroll list inside this scroll element inside a scroll element..."));
////        GuiScrollElement scrollList = new GuiScrollElement().setRelPos(5, 12).setSize(200, 188).setListMode(VERT_LOCK_POS_WIDTH).setStandardScrollBehavior();
////        scrollElement2.addElement(scrollList);
////        for (int i = 0; i < 50; i++) {
////            scrollList.addElement(new GuiLabel().setSize(0, 15).setLabelText("Random Label In A List " + i));
////        }
////        scrollList.addBackgroundChild(new GuiBorderedRect().setPosAndSize(scrollList.getRect()).setColours(0xFF009090, 0xFF009090));
////        scrollList.getVerticalScrollBar().setHidden(true);
//        //endregion
//
//    }
//
//    @Override
//    public void reloadGui() {
//        super.reloadGui();
//    }
//
//    @Override
//    public void render(PoseStack mStack, int mouseX, int mouseY, float partialTicks) {
//        super.render(mStack, mouseX, mouseY, partialTicks);
//
////        Rectangle rect = new Rectangle(100, 100, 30, 30);
////
////        MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
////        int w = 100;
////
////        GuiHelper.drawHoverRect(getter, mStack, 100, 100, 80, 26);
////
////        ItemStack stack = new ItemStack(Items.CAT_SPAWN_EGG);
////
////        GuiHelper.renderGuiStack(stack, mStack, 104, 105, 16, 16);
////
////        getter.endBatch();
////
////        drawString(mStack, font, "test", 123, 105, 0xFFFFFFFF);
////        drawString(mStack, font, "test", 123, 105+9, 0xFFFFFFFF);
//
//
////        GuiHelper.drawBorderedRect(getter, mStack, rect.x, rect.y, rect.width, rect.height, 1, 0xFFFFFFFF, 0xFFFF0000);
//
//
//
////        VertexConsumer builder = new TransformingVertexConsumer(getter.getBuffer(GuiHelper.transColourType), mStack);
////
////        for (int x = rect.x - w; x < rect.x + rect.width + w; x++) {
////            for (int y = rect.y - w; y < rect.y + rect.height + w; y++) {
//////                if (GuiHelper.isInRect(rect.x, rect.y, rect.width, rect.height, x, y)) continue;
////                double dist = GuiHelper.distToRect(rect.x, rect.y, rect.width, rect.height, x, y);
////                if (dist == 0) continue;
////                int c = (int)((dist / 50) * 255);
////                if (c > 255) c = 255;
//////                drawRect(builder, x, y, 1, 1, 0xFF000000 + c);
////                drawRect(builder, x+0.1, y+0.1, 0.8, 0.8, 0xFF000000 + c);
////            }
////        }
////
////
//
//    }
//
//    public static void drawRect(VertexConsumer builder, double x, double y, double width, double height, int colour) {
//        drawGradientRect(builder, x, y, x + width, y + height, colour, colour);
//    }
//
//    public static void drawGradientRect(VertexConsumer builder, double left, double top, double right, double bottom, int startColor, int endColor) {
//        if (startColor == endColor && endColor == 0) return;
//        //@formatter:off
//        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
//        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
//        float startGreen = (float)(startColor >>  8 & 255) / 255.0F;
//        float startBlue  = (float)(startColor       & 255) / 255.0F;
//        float endAlpha   = (float)(endColor   >> 24 & 255) / 255.0F;
//        float endRed     = (float)(endColor   >> 16 & 255) / 255.0F;
//        float endGreen   = (float)(endColor   >>  8 & 255) / 255.0F;
//        float endBlue    = (float)(endColor         & 255) / 255.0F;
//
//        builder.vertex(right,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
//        builder.vertex( left,    top, 0).color(startRed, startGreen, startBlue, startAlpha).endVertex();
//        builder.vertex( left, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
//        builder.vertex(right, bottom, 0).color(  endRed,   endGreen,   endBlue,   endAlpha).endVertex();
//        //@formatter:on
//    }
//}