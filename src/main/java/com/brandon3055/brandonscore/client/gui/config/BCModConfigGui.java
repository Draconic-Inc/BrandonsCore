//package com.brandon3055.brandonscore.client.gui.config;
//
//import com.brandon3055.brandonscore.registry.ModConfigParser;
//import com.brandon3055.brandonscore.utils.LogHelperBC;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.resources.I18n;
//import net.minecraftforge.common.config.ConfigCategory;
//import net.minecraftforge.common.config.ConfigElement;
//import net.minecraftforge.common.config.Property;
//import net.minecraftforge.fml.client.config.*;
//import net.minecraftforge.fml.common.ModContainer;
//import org.lwjgl.input.Keyboard;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import static net.minecraftforge.fml.client.config.GuiUtils.RESET_CHAR;
//import static net.minecraftforge.fml.client.config.GuiUtils.UNDO_CHAR;
//
//public class BCModConfigGui extends GuiConfig {
//
//    public BCModConfigGui(Screen parent, ModContainer mod) {
//        super(parent, getConfigElements(mod.getModId()), mod.getModId(), false, false, mod.getName() + " Configuration");
//
//        entryList = new BCGuiConfigEntries(this, Minecraft.getMinecraft());
//        initEntries.clear();
//        initEntries.addAll(new ArrayList<>(entryList.listEntries));
//    }
//
//    public BCModConfigGui(Screen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2)
//    {
//        super(parentScreen, configElements, modID, null, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
//    }
//
//    @Override
//    public void initGui() {
//        Keyboard.enableRepeatEvents(true);
//
//        if (this.entryList == null || this.needsRefresh) {
//            this.entryList = new BCGuiConfigEntries(this, mc);
//            this.needsRefresh = false;
//        }
//
//        int undoGlyphWidth = mc.fontRenderer.getStringWidth(UNDO_CHAR) * 2;
//        int resetGlyphWidth = mc.fontRenderer.getStringWidth(RESET_CHAR) * 2;
//        int doneWidth = Math.max(mc.fontRenderer.getStringWidth(I18n.format("gui.done")) + 20, 100);
//        int undoWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.undoChanges")) + undoGlyphWidth + 20;
//        int resetWidth = mc.fontRenderer.getStringWidth(" " + I18n.format("fml.configgui.tooltip.resetToDefault")) + resetGlyphWidth + 20;
//        int checkWidth = mc.fontRenderer.getStringWidth(I18n.format("fml.configgui.applyGlobally")) + 13;
//        int buttonWidthHalf = (doneWidth + 5 + undoWidth + 5 + resetWidth + 5 + checkWidth) / 2;
//        this.buttonList.add(new GuiButtonExt(2000, this.width / 2 - buttonWidthHalf, this.height - 29, doneWidth, 20, I18n.format("gui.done")));
//        this.buttonList.add(this.btnDefaultAll = new GuiUnicodeGlyphButton(2001, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5, this.height - 29, resetWidth, 20, " " + I18n.format("fml.configgui.tooltip.resetToDefault"), RESET_CHAR, 2.0F));
//        this.buttonList.add(btnUndoAll = new GuiUnicodeGlyphButton(2002, this.width / 2 - buttonWidthHalf + doneWidth + 5, this.height - 29, undoWidth, 20, " " + I18n.format("fml.configgui.tooltip.undoChanges"), UNDO_CHAR, 2.0F));
//        this.buttonList.add(chkApplyGlobally = new GuiCheckBox(2003, this.width / 2 - buttonWidthHalf + doneWidth + 5 + undoWidth + 5 + resetWidth + 5, this.height - 24, I18n.format("fml.configgui.applyGlobally"), false));
//
//        this.undoHoverChecker = new HoverChecker(this.btnUndoAll, 800);
//        this.resetHoverChecker = new HoverChecker(this.btnDefaultAll, 800);
//        this.checkBoxHoverChecker = new HoverChecker(chkApplyGlobally, 800);
//
//        ((BCGuiConfigEntries) entryList).initOverride();
//    }
//
//    private static List<IConfigElement> getConfigElements(String modid) {
//        List<IConfigElement> configElements = new ArrayList<IConfigElement>();
//
//        if (ModConfigParser.getModCategories(modid).size() <= 1) {
//            LogHelperBC.dev("Single Cat Mode: " + ModConfigParser.getModCategories(modid));
//            ModConfigParser.getModProperties(modid).forEach(property -> configElements.add(new BCConfigElement(property)));
//        }
//        else {
//            LogHelperBC.dev("Detected Multi-Cat");
//            ModConfigParser.getModCategories(modid).forEach(category -> configElements.add(new BCConfigElement(category)));
//        }
////		if (Minecraft.getInstance().world != null) {
////			Configuration worldConfig = Config.getWorldConfig();
////			if (worldConfig != null) {
////				ConfigCategory categoryWorldConfig = worldConfig.getCategory(SessionData.getWorldUid());
////				configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
////			}
////		}
////
////		//So basically you just give it a list of IConfigElement or more specifically ConfigElement.
////		//If you give that element a Property it will be a single property in the gui.
////		//you give it a config category it will open a sub gui with all items in that category.
////		LocalizedConfiguration config = Config.getConfig();
////		if (config != null) {
////			ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_ADVANCED);
////			configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());
////
////			ConfigCategory categorySearch = config.getCategory(Config.CATEGORY_SEARCH);
////			configElements.add(new ConfigElement(categorySearch));
////		}
//
//        return configElements;
//    }
//
//    public static class BCConfigElement extends ConfigElement {
//
//        private Property theProp = null;
//        private ConfigCategory category = null;
//        private boolean categoriesFirst = true;
//
//        public BCConfigElement(ConfigCategory category) {
//            super(category);
//            this.category = category;
//        }
//
//        public BCConfigElement(Property prop) {
//            super(prop);
//            this.theProp = prop;
//        }
//
//        public Property getProp() {
//            return theProp;
//        }
//
//        @Override
//        public ConfigElement listCategoriesFirst(boolean categoriesFirst) {
//            this.categoriesFirst = categoriesFirst;
//            return super.listCategoriesFirst(categoriesFirst);
//        }
//
//        @Override
//        public List<IConfigElement> getChildElements()
//        {
//            if (!isProperty())
//            {
//                List<IConfigElement> elements = new ArrayList<IConfigElement>();
//                Iterator<ConfigCategory> ccI = category.getChildren().iterator();
//                Iterator<Property> pI = category.getOrderedValues().iterator();
//                @SuppressWarnings("unused")
//                int index = 0;
//
//                if (categoriesFirst)
//                    while (ccI.hasNext())
//                    {
//                        ConfigElement temp = new BCConfigElement(ccI.next());
//                        if (temp.showInGui()) // don't bother adding elements that shouldn't show
//                            elements.add(temp);
//                    }
//
//                while (pI.hasNext())
//                {
//                    ConfigElement temp = new BCConfigElement(pI.next());
//                    if (temp.showInGui())
//                        elements.add(temp);
//                }
//
//                if (!categoriesFirst)
//                    while (ccI.hasNext())
//                    {
//                        ConfigElement temp = new BCConfigElement(ccI.next());
//                        if (temp.showInGui())
//                            elements.add(temp);
//                    }
//
//                return elements;
//            }
//            return null;
//        }
//    }
//}
