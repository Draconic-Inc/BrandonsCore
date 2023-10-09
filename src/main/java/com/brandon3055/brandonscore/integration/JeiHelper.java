package com.brandon3055.brandonscore.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

/**
 * Created by brandon3055 on 21/09/2016.
 */
public class JeiHelper {

    public static boolean jeiAvailable() {
        if (!ModHelperBC.isJEIInstalled) {
            return false;
        }

        return checkJEIRuntime();
    }

    public static boolean checkJEIRuntime() {
        return BCJEIPlugin.jeiRuntime != null;
    }

    public static List<IRecipeRenderer> getRecipeRenderers(ItemStack result) {
        if (!jeiAvailable()) {
            return null;
        }

        return getRenderers(result);
    }

    private static List<IRecipeRenderer> getRenderers(ItemStack result) {
        List<IRecipeRenderer> renderers = new ArrayList<>();

        IRecipeManager registry = BCJEIPlugin.jeiRuntime.getRecipeManager();
        IFocus<ItemStack> focus = BCJEIPlugin.jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(OUTPUT, VanillaTypes.ITEM_STACK, result);
        registry.createRecipeCategoryLookup()
                .limitFocus(Collections.singleton(focus))
                .get()
                .forEach(category -> registry.createRecipeLookup(category.getRecipeType())
                        .limitFocus(Collections.singleton(focus))
                        .get()
                        .forEach(recipe -> {
                            try {
                                renderers.add(new RecipeRenderer(category, recipe, result));
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        })
                );

        return renderers;
    }

    public static void openJEIRecipe(ItemStack stack, boolean usage) {
        if (jeiAvailable()) {
            openJEIRecipeInternal(stack, usage);
        }
    }

    private static void openJEIRecipeInternal(ItemStack stack, boolean usage) {
        if (checkJEIRuntime()) {
            IFocus<ItemStack> focus = BCJEIPlugin.jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(usage ? INPUT : OUTPUT, VanillaTypes.ITEM_STACK, stack);
            BCJEIPlugin.jeiRuntime.getRecipesGui().show(focus);
        }
    }

    public static int getRecipeKey(boolean usage) {
//        if (jeiAvailable()) {
//            return getRecipeKeyInternal(usage);
//        }
        return -1;
    }

//    private static int getRecipeKeyInternal(boolean usage) {
//        try {
//            return usage ? KeyBindings.showUses.get(0).getKey().getValue() : KeyBindings.showRecipe.get(0).getKey().getValue();
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return 01;
//        }
//    }

    @Nullable //Because reasons!
    public static ItemStack getPanelItemUnderMouse() {
        if (!jeiAvailable()) {
            return null;
        }

        Object ingredient = BCJEIPlugin.jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

        if (ingredient == null) {
            return null;
        }

        IIngredientHelper helper = BCJEIPlugin.jeiRuntime.getIngredientManager().getIngredientHelper(ingredient);

        return helper.getCheatItemStack(ingredient);
    }

    //region IRecipeRenderer

    private static class RecipeRenderer implements IRecipeRenderer {

        private IRecipeLayoutDrawable recipeLayout;
        private int width;
        private int height;
        private int xPos = 0;
        private int yPos = 0;
        private Component title;

        public RecipeRenderer(IRecipeCategory category, Object wrapper, ItemStack result) {
            IFocus<ItemStack> focus = BCJEIPlugin.jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(OUTPUT, VanillaTypes.ITEM_STACK, result);
            this.recipeLayout = BCJEIPlugin.jeiRuntime.getRecipeManager().createRecipeLayoutDrawable(category, wrapper, focus);
            this.width = category.getBackground().getWidth();
            this.height = category.getBackground().getHeight();
            this.title = category.getTitle();
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void render(Minecraft mc, int xPos, int yPos, int mouseX, int mouseY) {
            if (this.xPos != xPos || this.yPos != yPos) {
                this.xPos = xPos;
                this.yPos = yPos;
                recipeLayout.setPosition(xPos, yPos);
            }

            recipeLayout.drawRecipe(new PoseStack(), mouseX, mouseY);
        }

        @Override
        public void renderOverlay(Minecraft mc, int mouseX, int mouseY) {
            recipeLayout.drawOverlays(new PoseStack(), mouseX, mouseY);
        }

        @Override
        public boolean handleRecipeClick(Minecraft minecraft, double mouseX, double mouseY, boolean usage) {
            Optional<ItemStack> clicked = recipeLayout.getIngredientUnderMouse((int) mouseX, (int) mouseY, VanillaTypes.ITEM_STACK);

            if (clicked != null) {
                IFocus<ItemStack> focus = BCJEIPlugin.jeiRuntime.getJeiHelpers().getFocusFactory().createFocus(usage ? INPUT : OUTPUT, VanillaTypes.ITEM_STACK, clicked.get());
                BCJEIPlugin.jeiRuntime.getRecipesGui().show(focus);
            }

            return false;//layout.handleRecipeClick(minecraft, mouseX, mouseY, mouseButton);
        }

        @Nullable
        @Override
        public Object getIngredientUnderMouse(double mouseX, double mouseY) {
            return recipeLayout.getIngredientUnderMouse((int) mouseX, (int) mouseY, () -> Object.class);
        }

        @Override
        public Component getTitle() {
            return title;
        }
    }

    //endregion

}
