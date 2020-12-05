package com.brandon3055.brandonscore.datagen;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

import static com.brandon3055.brandonscore.BrandonsCore.MODID;

/**
 * Created by brandon3055 on 21/5/20.
 */
public class LangGenerator extends LanguageProvider {
    public LangGenerator(DataGenerator gen) {
        super(gen, MODID, "en_us");
    }

    @Override
    public void add(Block key, String name) {
        if (key != null)super.add(key, name);
    }

    @Override
    public void add(Item key, String name) {
        if (key != null)super.add(key, name);
    }

    @Override
    protected void addTranslations() {
        //@formatter:off

        //region # Gui's and related translations
//        add("gui.draconicevolution.item_config.show_unavailable"                            ,"Show unavailable");


        //endregion




        //region general energy
        add("op.brandonscore.operational_potential"                                             ,"Operational Potential");
        add("op.brandonscore.op"                                                                ,"OP");
        add("op.brandonscore.charge"                                                            ,"Charge");
        add("op.brandonscore.op_capacity"                                                       ,"OP Capacity");
        add("op.brandonscore.op_stored"                                                         ,"OP Stored");
//        add("op.brandonscore.op_max_receive"                                                    ,"Draconic Evolution Blocks");
//        add("op.brandonscore.op_max_extract"                                                    ,"Draconic Evolution Blocks");
        add("op.brandonscore.op_transfer"                                                       ,"OP Transfer");




        //endergion

        //region # Misc
//        add("itemGroup.draconicevolution.blocks"                                            ,"Draconic Evolution Blocks");
//        add("itemGroup.draconicevolution.items"                                             ,"Draconic Evolution Items");
//        add("itemGroup.draconicevolution.modules"                                           ,"Draconic Evolution Modules");



        //endregion

        //# Gui Toolkit
        add("gui.brandonscore.theme.light",                                                     "Light Theme");
        add("gui.brandonscore.theme.dark",                                                      "Dark Theme");
        add("gui.brandonscore.info_panel",                                                      "Display additional information");
        add("gui.brandonscore.rs_mode.always_active",                                           "Always active");
        add("gui.brandonscore.rs_mode.active_high",                                             "Active with redstone signal");
        add("gui.brandonscore.rs_mode.active_low",                                              "Active without redstone signal");
        add("gui.brandonscore.rs_mode.never_active",                                            "Never active");
        add("gui.brandonscore.large_view",                                                      "Large View");
        add("gui.brandonscore.large_view.close",                                                "Click outside or press Esc to close");
        add("gui.brandonscore.your_inventory",                                                  "Your Inventory");


        //temp

        //@formatter:on
    }
}
