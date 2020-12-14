package com.brandon3055.brandonscore.datagen;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 21/5/20.
 */
//@formatter:off
public class LangGenerator extends LanguageProvider {
    public LangGenerator(DataGenerator gen) {
        super(gen, MODID, "en_us");
    }

    private void addModularGui(PrefixHelper helper) {
        helper.setPrefix("mod_gui.brandonscore.energy_bar");
        helper.add("operational_potential"                  ,"Operational Potential");
        helper.add("op"                                     ,"OP");
        helper.add("rf"                                     ,"RF");
        helper.add("capacity"                               ,"Capacity");
        helper.add("stored"                                 ,"Stored");
        helper.add("input"                                  ,"Input");
        helper.add("output"                                 ,"Output");
        helper.add("io"                                     ,"I/O");
    }

    private void addGuiToolkit(PrefixHelper helper) {
        helper.setPrefix("gui_tkt.brandonscore");
        helper.add("theme.light"                                   ,"Light Theme");
        helper.add("theme.dark"                                    ,"Dark Theme");
        helper.add("info_panel"                                    ,"Display additional information");
        helper.add("rs_mode.always_active"                         ,"Always active");
        helper.add("rs_mode.active_high"                           ,"Active with redstone signal");
        helper.add("rs_mode.active_low"                            ,"Active without redstone signal");
        helper.add("rs_mode.never_active"                          ,"Never active");
        helper.add("large_view"                                    ,"Large View");
        helper.add("large_view.close"                              ,"Click outside or press Esc to close");
        helper.add("your_inventory"                                ,"Inventory");
    }

    private void addMisc(PrefixHelper helper) {
        helper.setPrefix("op.brandonscore");
        helper.add("operational_potential"                  ,"Operational Potential");
        helper.add("op"                                     ,"OP");
        helper.add("charge"                                 ,"Charge");
        helper.add("op_capacity"                            ,"OP Capacity");
        helper.add("op_stored"                              ,"OP Stored");
//        helper.add("op_max_receive"                         ,"Draconic Evolution Blocks");
//        helper.add("op_max_extract"                         ,"Draconic Evolution Blocks");
        helper.add("op_transfer"                            ,"OP Transfer");
    }


    @Override
    protected void addTranslations() {
        PrefixHelper helper = new PrefixHelper(this);
        addModularGui(helper);
        addGuiToolkit(helper);
        addMisc(helper);
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
