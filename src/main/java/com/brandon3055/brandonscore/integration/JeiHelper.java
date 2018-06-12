package com.brandon3055.brandonscore.integration;

import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.config.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 21/09/2016.
 */
public class JeiHelper {

    //region JEI Checks

    public static boolean jeiAvailable() {
        if (!ModHelperBC.isJEIInstalled) {
            return false;
        }

        return checkJEIRuntime();
    }

    @Optional.Method(modid = "jei")
    public static boolean checkJEIRuntime() {
        return BCJEIPlugin.jeiRuntime != null;
    }

    //endregion

    //region Get Renderers

    public static List<IRecipeRenderer> getRecipeRenderers(ItemStack result) {
        if (!jeiAvailable()) {
            return null;
        }

        return getRenderers(result);
    }

    @Optional.Method(modid = "jei")
    private static List<IRecipeRenderer> getRenderers(ItemStack result) {
        List<IRecipeRenderer> renderers = new ArrayList<>();

        IRecipeRegistry registry = BCJEIPlugin.jeiRuntime.getRecipeRegistry();
        List<IRecipeCategory> categories = new LinkedList<>();
        categories.addAll(registry.getRecipeCategories(registry.createFocus(IFocus.Mode.OUTPUT, result)));

        for (IRecipeCategory category : categories) {
            List wrappers = registry.getRecipeWrappers(category, registry.createFocus(IFocus.Mode.OUTPUT, result));
            for (Object wrapper : wrappers) {
                try {
                    renderers.add(new RecipeRenderer(category, (IRecipeWrapper) wrapper, result));
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }

        return renderers;
    }

    public static void openJEIRecipe(ItemStack stack, boolean usage) {
        if (jeiAvailable()) {
            openJEIRecipeInternal(stack, usage);
        }
    }

    @Optional.Method(modid = "jei")
    private static void openJEIRecipeInternal(ItemStack stack, boolean usage) {
        if (checkJEIRuntime()) {
            IFocus f = BCJEIPlugin.jeiRuntime.getRecipeRegistry().createFocus(usage ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, stack);
            BCJEIPlugin.jeiRuntime.getRecipesGui().show(f);
        }
    }


    public static int getRecipeKey(boolean usage) {
        if (jeiAvailable()) {
            return getRecipeKeyInternal(usage);
        }
        return -1;
    }

    @Optional.Method(modid = "jei")
    private static int getRecipeKeyInternal(boolean usage) {
        try {
            return usage ? KeyBindings.showUses.getKeyCode() : KeyBindings.showRecipe.getKeyCode();
        }
        catch (Throwable e) {
            e.printStackTrace();
            return 01;
        }
    }




    //endregion

    public static ItemStack getPanelItemUnderMouse() {
        if (!jeiAvailable()) {
            return null;
        }

        Object ingredient = BCJEIPlugin.jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

        if (ingredient == null) {
            return null;
        }

        IIngredientHelper helper = BCJEIPlugin.ingredientRegistry.getIngredientHelper(ingredient);

        return helper.cheatIngredient(ingredient, false);
    }

    //region IRecipeRenderer

    private static class RecipeRenderer implements IRecipeRenderer {

        private IRecipeLayoutDrawable recipeLayout;
        private int width;
        private int height;
        private int xPos = 0;
        private int yPos = 0;
        private String title;

        public RecipeRenderer(IRecipeCategory category, IRecipeWrapper wrapper, ItemStack result) {
            IFocus<?> f = BCJEIPlugin.jeiRuntime.getRecipeRegistry().createFocus(IFocus.Mode.OUTPUT, result);//new Focus<Object>(result);
            this.recipeLayout = BCJEIPlugin.jeiRuntime.getRecipeRegistry().createRecipeLayoutDrawable(category, wrapper, f);
            this.width = category.getBackground().getWidth();
            this.height = category.getBackground().getHeight();
            this.title = category.getTitle();
            if (StringUtils.isNullOrEmpty(this.title)) {
                this.title = "[Unknown Crafting Type]";
            }
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

            recipeLayout.drawRecipe(mc, mouseX, mouseY);
        }

        @Override
        public void renderOverlay(Minecraft mc, int mouseX, int mouseY) {
            recipeLayout.drawOverlays(mc, mouseX, mouseY);
        }

        @Override
        public boolean handleRecipeClick(Minecraft minecraft, int mouseX, int mouseY, boolean usage) {
            Object clicked = recipeLayout.getIngredientUnderMouse(mouseX, mouseY);

            if (clicked != null) {
                IFocus f = BCJEIPlugin.jeiRuntime.getRecipeRegistry().createFocus(usage ? IFocus.Mode.INPUT : IFocus.Mode.OUTPUT, clicked);
                BCJEIPlugin.jeiRuntime.getRecipesGui().show(f);
            }

            return false;//layout.handleRecipeClick(minecraft, mouseX, mouseY, mouseButton);
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Nullable
        @Override
        public Object getIngredientUnderMouse(int mouseX, int mouseY) {
            return recipeLayout.getIngredientUnderMouse(mouseX, mouseY);
        }
    }

    //endregion

}
