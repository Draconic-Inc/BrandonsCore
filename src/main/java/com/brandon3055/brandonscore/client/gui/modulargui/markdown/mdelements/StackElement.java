//package com.brandon3055.brandonscore.client.gui.modulargui.markdown.mdelements;
//
//import com.brandon3055.brandonscore.client.BCClientEventHandler;
//import com.brandon3055.brandonscore.client.gui.modulargui.markdown.LayoutHelper;
//import com.brandon3055.brandonscore.integration.JeiHelper;
//import com.brandon3055.brandonscore.lib.StringyStacks;
//import net.minecraft.client.Minecraft;
//import net.minecraft.core.NonNullList;
//import net.minecraft.world.item.ItemStack;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by brandon3055 on 5/31/2018.
// */
//public class StackElement extends MDElementBase<StackElement> {
//
//    private ItemStack[] stacks;
//    public boolean drawSlot = false;
//
//    public StackElement(String stackString) {
//        this.enableTooltip = true;
//        this.size = 16;
//
//        //TODO Fix ore dictionary stuff
////        May need to fix the PI editor first...
////        SetTag<Item> tag = null;////ItemTags.getCollection().get(ResourceHelperBC.getResourceRAW(stackString));
////        boolean isOre = tag != null;
//
//        List<ItemStack> baseStacks = new ArrayList<>();
//
////        if (isOre) {
////            baseStacks.addAll(tag.getValues().stream().map(ItemStack::new).collect(Collectors.toList()));
////        } else {
//        ItemStack stack = StringyStacks.fromString(stackString, null);
//        if (stack == null) {
//            error("[Broken Stack. Specified Item or Block could not be found!]");
//            return;
//        }
//        baseStacks.add(stack);
////        }
//
//        NonNullList<ItemStack> finalStacks = NonNullList.create();
////        for (ItemStack stack : baseStacks) {
////            if (stack.getDamage() == OreDictionary.WILDCARD_VALUE && stack.getHasSubtypes()) {
////                stack.getItem().getSubItems(CreativeTabs.SEARCH, finalStacks);
////            }
////            else {
////                finalStacks.add(stack);
////            }
////        }
//        finalStacks.addAll(baseStacks);
//
//        stacks = finalStacks.toArray(new ItemStack[0]);
//    }
//
//    @Override
//    public void layoutElement(LayoutHelper layout, List<MDElementBase> lineElement) {
//        setSize(size, size);
//        super.layoutElement(layout, lineElement);
//    }
//
//    @Override
//    public void renderElement(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
////        if (stacks.length == 0) return;
////
////        RenderSystem.pushMatrix();
////
////        if (drawSlot) {
////            Material mat = BCSprites.get("light/slot");
////            bindTexture(mat.atlasLocation());
////            RenderSystem.color4f(1F, 1F, 1F, 1F);
////            MultiBufferSource.BufferSource getter = RenderUtils.getGuiBuffers();
////            drawSprite(getter.getBuffer(mat.renderType(BCSprites::makeType)), xPos(), yPos(), xSize(), ySize(), mat.sprite());
////            getter.endBatch();
////        }
////
////        double scale = size / 18D;
////        ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
////
////        Lighting.turnBackOn();
////        RenderSystem.translated(xPos() + scale, yPos() + scale, getRenderZLevel() - 80);
////        RenderSystem.scaled(scale, scale, 1);
////        minecraft.getItemRenderer().renderGuiItem(stack, 0, 0);
////
////        if (stack.getCount() > 1) {
////            String s = "" + Utils.SELECT + "f" + stack.getCount() + "" + Utils.SELECT + "f";
////            RenderSystem.translated(0, 0, -(getRenderZLevel() - 80));
////            zOffset += 45;
////            drawString(fontRenderer, s, 18 - (fontRenderer.width(s)) - 1, fontRenderer.lineHeight, 0xFFFFFF, true);
////            zOffset -= 45;
////        }
////
////        //TODO com.brandon3055.brandonscore.client.gui.modulargui.lib.BCFontRenderer
//////        RenderSystem.color4f(fontRenderer.red, fontRenderer.blue, fontRenderer.green, 1);
////        Lighting.turnOff();
////        RenderSystem.popMatrix();
////        super.renderElement(minecraft, mouseX, mouseY, partialTicks);
//    }
//
//    @Override
//    public boolean renderOverlayLayer(Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
////        if (enableTooltip && isMouseOver(mouseX, mouseY)) {
////            if (tooltip.isEmpty()) {
////                ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
////                List<Component> list = getTooltipFromItem(stack);
////
////                for (int i = 0; i < list.size(); ++i) {
////                    if (i == 0) {
//////                        list.set(i, stack.getRarity().color + list.get(i));
////                    } else {
//////                        list.set(i, TextFormatting.GRAY + list.get(i));
////                    }
////                }
////
////                ScreenUtils.preItemToolTip(stack);
////                this.drawHoveringText(stack, list, mouseX, mouseY, screenWidth, screenHeight, -1, fontRenderer);
////                ScreenUtils.postItemToolTip();
////            } else {
////                drawHoveringText(tooltip, mouseX, mouseY, fontRenderer, screenWidth, screenHeight);
////            }
////            return true;
////        }
//        return super.renderOverlayLayer(minecraft, mouseX, mouseY, partialTicks);
//    }
//
//    @Override
//    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//        if (isMouseOver(mouseX, mouseY) && (mouseButton == 0 || mouseButton == 1)) {
//            ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
//            JeiHelper.openJEIRecipe(stack, mouseButton == 1);
//            return true;
//        }
//
//        return super.mouseClicked(mouseX, mouseY, mouseButton);
//    }
//
//    @Override
//    protected boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//        double mouseX = getMouseX();
//        double mouseY = getMouseY();
//
//        if (isMouseOver(mouseX, mouseY)) {
//            ItemStack stack = stacks[(BCClientEventHandler.elapsedTicks / 40) % stacks.length];
//            if (keyCode == JeiHelper.getRecipeKey(false)) {
//                JeiHelper.openJEIRecipe(stack, false);
//                return true;
//            } else if (keyCode == JeiHelper.getRecipeKey(true)) {
//                JeiHelper.openJEIRecipe(stack, true);
//                return true;
//            }
//
//            //TODO When PI is back!! And Test
////            else if (PIHelper.isInstalled() && PIHelper.getETGuiKey().matches(keyCode, scanCode)) {
////                List<String> pages = PIHelper.getRelatedPages(stack);
////                if (!pages.isEmpty()) {
////                    PIHelper.openGui(modularGui.getScreen(), pages);
////                    return true;
////                }
////            }
//        }
//
//        return super.keyPressed(keyCode, scanCode, modifiers);
//    }
//}