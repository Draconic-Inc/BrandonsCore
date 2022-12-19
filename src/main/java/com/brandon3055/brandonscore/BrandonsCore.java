package com.brandon3055.brandonscore;

import com.brandon3055.brandonscore.capability.CapabilityOP;
import com.brandon3055.brandonscore.client.ClientProxy;
import com.brandon3055.brandonscore.command.BCCommands;
import com.brandon3055.brandonscore.handlers.BCEventHandler;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.brandon3055.brandonscore.handlers.ProcessHandler;
import com.brandon3055.brandonscore.handlers.contributor.ContributorHandler;
import com.brandon3055.brandonscore.init.ClientInit;
import com.brandon3055.brandonscore.integration.ModHelperBC;
import com.brandon3055.brandonscore.inventory.BlockToStackHelper;
import com.brandon3055.brandonscore.lib.IEquipmentManager;
import com.brandon3055.brandonscore.multiblock.MultiBlockManager;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.brandon3055.brandonscore.worldentity.WorldEntityHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BrandonsCore.MODID)
public class BrandonsCore {
    public static final Logger LOGGER = LogManager.getLogger("BrandonsCore");
    public static final String MODNAME = "Brandon's Core";
    public static final String MODID = "brandonscore";
    public static final String VERSION = "${mod_version}";
    public static CommonProxy proxy;
    public static boolean inDev = false;
    public static IEquipmentManager equipmentManager = null;

    public BrandonsCore() {
        FileHandler.init();
        ModHelperBC.init();
        proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
        inDev = ModHelperBC.getModVersion(MODID).equals("0.0NONE");

        //Knock Knock...
        Logger deLog = LogManager.getLogger("draconicevolution");
        if (ModList.get().isLoaded("draconicevolution")) {
            LOGGER.info("Knock Knock...");
            deLog.log(Level.WARN, "Reactor detonation initiated.");
            LOGGER.info("Wait... NO! What?");
            LOGGER.info("Stop That! That's not how this works!");
            deLog.log(Level.WARN, "Calculating explosion ETA");
            LOGGER.info("Ahh... NO... NONONO! DONT DO THAT!!! STOP THIS NOW!");
            deLog.log(Level.WARN, "**Explosion Imminent!!!**");
            LOGGER.info("Well...... fork...");
        } else {
            LOGGER.info("Hey! Where's DE?");
            LOGGER.info("Oh well.. At least we dont have to worry about getting blown up now...");
        }

        CapabilityOP.init();
        BCoreNetwork.init();
        BCConfig.load();
        ProcessHandler.init();
        MultiBlockManager.init();
        BlockToStackHelper.init();
        WorldEntityHandler.init();
        ContributorHandler.init();
        BCEventHandler.init();
        BCCommands.init();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }
}

