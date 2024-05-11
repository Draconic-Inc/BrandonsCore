package com.brandon3055.brandonscore.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 21/5/20.
 */
//@formatter:off
public class LangGenerator extends LanguageProvider {
    public LangGenerator(DataGenerator gen) {
        super(gen.getPackOutput(), MODID, "en_us");
    }

    private void addModularGui(PrefixHelper helper) {
        helper.setPrefix("mod_gui.brandonscore.energy_bar");
        helper.add("operational_potential",                  "Operational Potential");
        helper.add("op",                                     "OP");
        helper.add("rf",                                     "RF");
        helper.add("capacity",                               "Capacity");
        helper.add("stored",                                 "Stored");
        helper.add("input",                                  "Input");
        helper.add("output",                                 "Output");
        helper.add("io",                                     "I/O");

        helper.setPrefix("mod_gui.brandonscore.entity_filter");
        add("mod_gui.brandonscore.entity_filter",           "Entity Filter");
        helper.add("hostile",                               "Filter Hostility");
        helper.add("hostile.true",                          "Hostile Mobs Only");
        helper.add("hostile.false",                         "Passive Mobs Only");
        helper.add("tamed",                                 "Filter Tamed");
        helper.add("tamed.true",                            "Tamed Only");
        helper.add("tamed.false",                           "Untamable Only");
        helper.add("tamable.true",                          "Include Tamable");
        helper.add("tamable.false",                         "Exclude Tamable");
        helper.add("tamable.info",                          "Applies to tamable mobs that are not owned by a player.");
        helper.add("adults",                                "Filter Adults");
        helper.add("adults.true",                           "Adults Only");
        helper.add("adults.false",                          "Babies Only");
        helper.add("non_ageable.true",                      "Include Non-Ageable");
        helper.add("non_ageable.false",                     "Exclude Non-Ageable");
        helper.add("non_ageable.info",                      "'Include Non-Ageable' will treat any 'non-ageable' mob as an adult.");
        helper.add("player",                                "Filter Players");
        helper.add("player.true",                           "Include Player(s)");
        helper.add("player.false",                          "Exclude Player(s)");
        helper.add("player.name",                           "Name:");
        helper.add("player.info",                           "This filter applies to all players.\nUnless a specific player name is entered.");
        helper.add("player.suggestion",                     "(Enter player name here)");
        helper.add("entity_type",                           "Filter Entity Type");
        helper.add("entity_type.true",                      "Include Entity");
        helper.add("entity_type.false",                     "Exclude Entity");
        helper.add("entity_type.name",                      "Name:");
        helper.add("entity_type.find",                      "Select from entity list.");
        helper.add("entity_type.unknown",                   "Unknown / Invalid entity name");
        helper.add("entity_type.suggestion",                "(Enter entity registry name here)");
        helper.add("item_filter",                           "Filter Item");
        helper.add("set_stack",                             "Drop stack here to set\nOr click to clear");
        helper.add("item.true",                             "Include Matching Stacks");
        helper.add("item.false",                            "Exclude Matching Stacks");
        helper.add("item.tag.true",                         "Mode: Item Tag");
        helper.add("item.tag.false",                        "Mode: Item Stack");
        helper.add("item.match_count.true",                 "Stack Size: Must match");
        helper.add("item.match_count.false",                "Stack Size: Ignored");
        helper.add("item.fuzzy.true",                       "Match Mode: Fuzzy match");
        helper.add("item.fuzzy.false",                      "Match Mode: Exact match");
        helper.add("item.fuzzy.info",                       "Fuzzy match will ignore damage and NBT.\nExact match will only detect an identical item stack.");
        helper.add("item.tag_matches",                      "Found %s matching items!");
        helper.add("item.invalid_tag",                      "Unknown / Invalid tag");
        helper.add("item.find_tag",                         "Search available item tags.");
//        helper.add("item.count",                            "Stack Size:");
//        helper.add("item.count.info",                       "Search for a specific stack size\nOr leave empty to ignore stack size.");
//        helper.add("item.damage",                           "Meta Data:");
//        helper.add("item.damage.info",                      "Search for a specific damage value\nOr leave empty to ignore meta data.");
//        helper.add("item.nbt",                              "NBT:");
//        helper.add("item.nbt.info",                         "Search for a stack with specific NBT data\nOr leave empty to ignore NBT.");
//        helper.add("item.nbt.bad",                          "Invalid NBT string.\nMust be in standard JSON NBT format.");
        helper.add("item.blocks_only",                      "Match: Blocks Only");
        helper.add("item.items_only",                       "Match: Items Only");
        helper.add("item.items_or_blocks",                  "Match: Items or Blocks");
        helper.add("item.items_blocks.info",                "Match mode when no stack is specified.");
        helper.add("item.tag.suggestion",                   "(Enter tag name here)");
        helper.add("filter_group",                          "Filter Group");
        helper.add("add_filter",                            "Add Filter");
        helper.add("and_group.button.true",                 "Match All");
        helper.add("and_group.button.false",                "Match Any");
        helper.add("and_group.true",                        "An entity will be accepted if it matches ALL of the filters in this group.");
        helper.add("and_group.false",                       "An entity will be accepted if it matches ANY of the filters in this group.");
        helper.add("delete.node",                           "Double-Click to delete filter node");
        helper.add("delete.all",                            "Triple-Click to clear all nodes");
        helper.add("search",                                "Search...");

        helper.setPrefix("mod_gui.brandonscore.button");
        helper.add("yes",                                   "Yes");
        helper.add("no",                                    "No");
        helper.add("ok",                                    "Ok");
        helper.add("cancel",                                "Cancel");

        helper.setPrefix("mod_gui.brandonscore.list_dialog");
        helper.add("search",                                "Search");

    }

    private void addGuiToolkit(PrefixHelper helper) {
        helper.setPrefix("gui_tkt.brandonscore");
        helper.add("theme.light",                           "Light Theme");
        helper.add("theme.dark",                            "Dark Theme");
        helper.add("info_panel.closed",                     "Display additional information");
        helper.add("info_panel.open",                       "Hide additional information");
        helper.add("rs_mode.always_active",                 "Always active");
        helper.add("rs_mode.active_high",                   "Active with redstone signal");
        helper.add("rs_mode.active_low",                    "Active without redstone signal");
        helper.add("rs_mode.never_active",                  "Never active");
        helper.add("large_view",                            "Large View");
        helper.add("large_view.close",                      "Click outside or press Esc to close");
        helper.add("your_inventory",                        "Inventory");
        helper.add("click_out_close",                       "Click outside to close");
    }

    private void addMisc(PrefixHelper helper) {
        helper.setPrefix("op.brandonscore");
        helper.add("operational_potential",                 "Operational Potential");
        helper.add("op",                                    "OP");
        helper.add("charge",                                "Charge");
        helper.add("op_capacity",                           "OP Capacity");
        helper.add("op_stored",                             "OP Stored");
        helper.add("op_transfer",                           "OP Transfer");

        helper.setPrefix("item_info.brandonscore");
        helper.add("shift_for_details",                     "Hold %sShift%s for details");

        add("info.brandonscore.block_has_saved_data",       "Contents Saved.");

        add("hud.brandonscore.item_hud.name",               "Item info HUD");
        add("hud.brandonscore.item_hud.info",               "Displays information about certain draconic items when you are holding them.\nDisabled by default");

        add("hud.brandonscore.block_hud.name",              "Block info HUD");
        add("hud.brandonscore.block_hud.info",              "Displays information about certain draconic blocks when you are looking at them.\nDisabled by default");

        add("hud.brandonscore.block_item_hud.name",         "Combined Block & Item HUD");
        add("hud.brandonscore.block_item_hud.info",         "Combines item info hud and block info hud into one for convenience.");

        //Structure Builder
        helper.setPrefix("struct_build.brandonscore");
        helper.add("found_invalid",                         "Build Error: Found invalid block \"%s\" at %s.");
        helper.add("missing_required",                      "Build Error: Unable to complete structure! Missing required block(s) \"%s\".");
    }

    private void addGui(PrefixHelper helper) {
        //Hud Config
        helper.setPrefix("gui.brandonscore.hud_config");
        helper.add("name",                                  "Draconic HUD Configuration");
        helper.add("settings",                              "Open Settings");
        helper.add("enabled.true",                          "Enabled");
        helper.add("enabled.false",                         "Disabled");
    }



        @Override
    protected void addTranslations() {
        PrefixHelper helper = new PrefixHelper(this);
        addModularGui(helper);
        addGuiToolkit(helper);
        addMisc(helper);
            addGui(helper);
    }

    @Override
    public void add(Block key, String name) {
        if (key != null)super.add(key, name);
    }

    @Override
    public void add(Item key, String name) {
        if (key != null)super.add(key, name);
    }

    public static class PrefixHelper {
        private LangGenerator generator;
        private String prefix;

        public PrefixHelper(LangGenerator generator) {
            this.generator = generator;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix + ".";
        }

        public void add(String translationKey, String translation) {
            generator.add(prefix + translationKey, translation);
        }

        public void add(Block key, String name) {
            if (key != null) generator.add(key, name);
        }

        public void add(Item key, String name) {
            if (key != null) generator.add(key, name);
        }
    }
}
