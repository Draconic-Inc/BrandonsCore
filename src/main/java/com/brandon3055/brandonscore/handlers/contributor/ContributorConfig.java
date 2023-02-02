package com.brandon3055.brandonscore.handlers.contributor;

import codechicken.lib.colour.Colour;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.handlers.FileHandler;
import com.google.gson.Gson;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * These are user (contributor) controlled properties that are
 * saved locally and synced to all other players via the server.
 * <p>
 * Created by brandon3055 on 21/11/2022
 */
public class ContributorConfig {
    private static final Gson GSON = new Gson();
    private boolean overrideShield = false;
    private int shieldOverride = 0;
    private boolean overrideBoneColour = false;
    private boolean overrideWebColour = false;
    private int boneColourOverride = 0;
    private int webColourOverride = 0;
    private boolean wingBoneShader = true;
    private boolean wingWebShader = true;
    private boolean wingBoneRGB = false;
    private boolean wingWebRGB = false;
    private boolean shieldRGB = false;
    private TechLevel wingsTier = TechLevel.CHAOTIC;
    private Badge chestBadge = null; //Null means use the last badge added to the list.
    private Badge backBadge = null;  //Ensures the highest tier badge the player has is used by default
    private WingBehavior wingsGround = WingBehavior.RETRACT;
    private WingBehavior wingsCreative = WingBehavior.RETRACT;
    private WingElytraCompat wingsElytra = WingElytraCompat.HIDE_ELYTRA;

    private boolean showWelcome = true;

    private transient boolean requiresSync = false;
    private transient TechLevel lastWingsTier = null;
    protected transient ContributorProperties props;

    public ContributorConfig() {}

    public ContributorConfig(ContributorProperties props) {
        this.props = props;
    }

    //@formatter:off
    // ## Shield ##
    public boolean overrideShield() { return overrideShield && props.hasShieldRGB(); }
    public void setOverrideShield(boolean overrideShield) {
        this.overrideShield = overrideShield;
        markDirty();
    }

    public int getShieldOverride() { return shieldOverride; }
    public void setShieldOverride(int shieldOverride) {
        this.shieldOverride = shieldOverride;
        markDirty();
    }

    public boolean getShieldRGB() { return shieldRGB && props.hasShieldRGB(); }
    public void setShieldRGB(boolean shieldRGB) {
        this.shieldRGB = shieldRGB;
        markDirty();
    }

    // ## Wings ##
    //Tier
    @Nullable
    public TechLevel getWingsTier() {
        List<TechLevel> tiers = props.getWingTiers();
        return wingsTier == null || tiers.isEmpty() ? null : tiers.contains(wingsTier) ? wingsTier : tiers.get(tiers.size() - 1);
    }

    public TechLevel getWingRenderTier() {
        TechLevel tier = getWingsTier();
        return tier == null ? lastWingsTier : tier;
    }

    public void setWingsTier(@Nullable TechLevel wingsTier) {
        if (wingsTier == null) {
            lastWingsTier = this.wingsTier;
        }
        this.wingsTier = wingsTier;
        markDirty();
    }

    //Wing Bones
    public boolean overrideWingBoneColour() { return overrideBoneColour && props.hasWingsRGB(); }
    public void setOverrideWingBoneColour(boolean overrideBoneColour) {
        this.overrideBoneColour = overrideBoneColour;
        markDirty();
    }

    public int getWingsOverrideBoneColour() { return boneColourOverride; }
    public void setWingsOverrideBoneColour(int boneColourOverride) {
        this.boneColourOverride = boneColourOverride;
        markDirty();
    }

    public boolean getWingRGBBoneColour() { return wingBoneRGB && props.hasWingsRGB(); }
    public void setWingRGBBoneColour(boolean wingBoneRGB) {
        this.wingBoneRGB = wingBoneRGB;
        markDirty();
    }

    public boolean getWingsBoneShader() { return wingBoneShader; }
    public void setWingsBoneShader(boolean wingShaderA) {
        this.wingBoneShader = wingShaderA;
        markDirty();
    }

    //Wing Webs
    public boolean overrideWingWebColour() { return overrideWebColour && props.hasWingsRGB(); }
    public void setOverrideWingsWebColour(boolean overrideWebColour) {
        this.overrideWebColour = overrideWebColour;
        markDirty();
    }

    public int getWingsOverrideWebColour() { return webColourOverride; }
    public void setWingsOverrideWebColour(int webColourOverride) {
        this.webColourOverride = webColourOverride;
        markDirty();
    }

    public boolean getWingRGBWebColour() { return wingWebRGB && props.hasWingsRGB(); }
    public void setWingRGBWebColour(boolean wingWebRGB) {
        this.wingWebRGB = wingWebRGB;
        markDirty();
    }

    public boolean getWingsWebShader() { return wingWebShader; }
    public void setWingsWebShader(boolean wingWebShader) {
        this.wingWebShader = wingWebShader;
        markDirty();
    }

    //Wing behavior
    public WingBehavior getWingsGround() { return wingsGround; }
    public void setWingsGround(WingBehavior wingsGround) {
        this.wingsGround = wingsGround;
        markDirty();
    }

    public WingBehavior getWingsCreative() { return wingsCreative; }
    public void setWingsCreative(WingBehavior wingsCreative) {
        this.wingsCreative = wingsCreative;
        markDirty();
    }

    public WingElytraCompat getWingsElytra() { return wingsElytra; }
    public void setWingsElytra(WingElytraCompat wingsElytra) {
        this.wingsElytra = wingsElytra;
        markDirty();
    }

    // ## Badges ##
    public Badge getChestBadge() {
        List<Badge> badges = props.getBadges();
        if (chestBadge == null && !badges.isEmpty()) return badges.get(badges.size() - 1);
        return badges.isEmpty() ? Badge.DISABLED : badges.contains(chestBadge) ? chestBadge : badges.get(badges.size() - 1);
    }

    public void setChestBadge(@Nullable Badge chestBadge) {
        this.chestBadge = chestBadge;
        markDirty();
    }

    public Badge getBackBadge() {
        List<Badge> badges = props.getBadges();
        if (backBadge == null && !badges.isEmpty()) return badges.get(badges.size() - 1);
        return badges.isEmpty() ? Badge.DISABLED : badges.contains(backBadge) ? backBadge : badges.get(badges.size() - 1);
    }

    public void setBackBadge(@Nullable Badge backBadge) {
        this.backBadge = backBadge;
        markDirty();
    }

    // ## Other ##
    public boolean showWelcome() { return showWelcome; }
    public void setShowWelcome(boolean showWelcome) {
        this.showWelcome = showWelcome;
        markDirty();
    }

    //@formatter:on

    public void markDirty() {
        requiresSync = true;
        save();
    }

    public boolean getResetSyncRequired() {
        if (requiresSync) {
            requiresSync = false;
            return true;
        }
        return false;
    }

    public static ContributorConfig load() {
        File file = new File(FileHandler.brandon3055Folder, "contributor_settings.json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                ContributorConfig config = GSON.fromJson(reader, ContributorConfig.class);
                config.requiresSync = true;
                return config;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        ContributorConfig config = new ContributorConfig();
        config.requiresSync = true;
        config.save();
        return config;
    }

    public void save() {
        File file = new File(FileHandler.brandon3055Folder, "contributor_settings.json");
        try (Writer writer = new FileWriter(file)) {
            GSON.toJson(this, writer);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void serialize(MCDataOutput output) {
        //Shield
        output.writeBoolean(overrideShield);
        output.writeInt(shieldOverride);
        //Wings Enabled
        output.writeBoolean(wingsTier != null);
        if (wingsTier != null) {
            output.writeEnum(wingsTier);
            output.writeBoolean(overrideBoneColour);
            output.writeInt(boneColourOverride);
            output.writeBoolean(wingBoneShader);
            output.writeBoolean(overrideWebColour);
            output.writeInt(webColourOverride);
            output.writeBoolean(wingWebShader);

            output.writeBoolean(wingBoneRGB);
            output.writeBoolean(wingWebRGB);
            output.writeBoolean(shieldRGB);
            //Wing Behavior
            output.writeEnum(wingsGround);
            output.writeEnum(wingsCreative);
            output.writeEnum(wingsElytra);
        }
        //Chest Badge
        output.writeBoolean(chestBadge != null);
        if (chestBadge != null) {
            output.writeEnum(chestBadge);
        }
        //Back Badge
        output.writeBoolean(backBadge != null);
        if (backBadge != null) {
            output.writeEnum(backBadge);
        }
        //Other
        output.writeBoolean(showWelcome);
    }

    public static ContributorConfig deSerialize(MCDataInput input) {
        ContributorConfig config = new ContributorConfig();
        //Shield                                                          // //Shield
        config.overrideShield = input.readBoolean();                      // output.writeBoolean(overrideShield);
        config.shieldOverride = input.readInt();                          // output.writeInt(shieldOverride);
        //Wings                                                           // //Wings Enabled
        boolean enabled = input.readBoolean();                            // output.writeBoolean(wingsTier != null);
        if (enabled) {                                                    // if (wingsTier != null) {
            config.wingsTier = input.readEnum(TechLevel.class);           //     output.writeEnum(wingsTier);
            config.overrideBoneColour = input.readBoolean();              //     output.writeBoolean(overrideBoneColour);
            config.boneColourOverride = input.readInt();                  //     output.writeInt(boneColourOverride);
            config.wingBoneShader = input.readBoolean();                  //     output.writeBoolean(wingBoneShader);
            config.overrideWebColour = input.readBoolean();               //     output.writeBoolean(overrideWebColour);
            config.webColourOverride = input.readInt();                   //     output.writeInt(webColourOverride);
            config.wingWebShader = input.readBoolean();                   //     output.writeBoolean(wingWebShader);

            config.wingBoneRGB = input.readBoolean();
            config.wingWebRGB = input.readBoolean();
            config.shieldRGB = input.readBoolean();
            //Wing Behavior                                               //     //Wing Behavior
            config.wingsGround = input.readEnum(WingBehavior.class);      //     output.writeEnum(wingsGround);
            config.wingsCreative = input.readEnum(WingBehavior.class);    //     output.writeEnum(wingsCreative);
            config.wingsElytra = input.readEnum(WingElytraCompat.class);  //     output.writeEnum(wingsElytra);
        }                                                                 // }
        //Chest Badge                                                     // //Chest Badge
        boolean cbe = input.readBoolean();                                // output.writeBoolean(chestBadge != null);
        if (cbe) {                                                        // if (chestBadge != null) {
            config.chestBadge = input.readEnum(Badge.class);              //     output.writeEnum(chestBadge);
        }                                                                 // }
        //Back Badge                                                      // //Back Badge
        boolean bbe = input.readBoolean();                                // output.writeBoolean(backBadge != null);
        if (bbe) {                                                        // if (backBadge != null) {
            config.backBadge = input.readEnum(Badge.class);               //     output.writeEnum(backBadge);
        }                                                                 // }
        //Other                                                           // //Other
        config.showWelcome = input.readBoolean();                         // output.writeBoolean(showWelcome);
        return config;
    }

    private static float[][] TIER_COLOURS = {
            {0.0F, 0.5F, 0.8F, 1F},
            {0.55F, 0.0F, 0.65F, 1F},
            {0.8F, 0.5F, 0.1F, 1F},
            {0.75F, 0.05F, 0.05F, 1F}
    };

    public int getBaseColourI(TechLevel techLevel) {
        if (techLevel == null) techLevel = TechLevel.DRACONIUM;
        return Colour.packARGB(TIER_COLOURS[techLevel.index]);
    }

    public float[] getWingBoneColour(float partialTick) {
        return getColour(getWingRenderTier(), overrideWingBoneColour(), getWingsOverrideBoneColour(), getWingRGBBoneColour(), props.getAnim().getWingBoneCol(partialTick));
    }

    public float[] getWingWebColour(float partialTick) {
        return getColour(getWingRenderTier(), overrideWingWebColour(), getWingsOverrideWebColour(), getWingRGBWebColour(), props.getAnim().getWingWebCol(partialTick));
    }

    public int getShieldColour(float partialTick) {
        if (getShieldRGB()) {
            float[] colours = unpack(getShieldOverride());
            return Color.HSBtoRGB(props.getAnim().getShieldCol(partialTick), colours[1], colours[2]);
        }
        return getShieldOverride();
    }

    private static float[] getColour(TechLevel level, boolean useOverride, int override, boolean rgbEffect, float anim) {
        if (useOverride) {
            float[] colours = unpack(override);
            if (rgbEffect) {
                colours = unpack(Color.HSBtoRGB(anim, colours[1], colours[2]));
            }
            return colours;
        }
        return level == null ? TIER_COLOURS[0] : TIER_COLOURS[level.index];
    }

    public static float[] unpack(int colour) {
        return new float[]{((colour >> 16) & 0xFF) / 255F, ((colour >> 8) & 0xFF) / 255F, (colour & 0xFF) / 255F, 1F};
    }

    public enum WingBehavior implements HoverText {
        HIDE("Hide", ""),
        RETRACT("Retract", ""),
        EXTEND("Extend", ""),
        EXTEND_AND_FLAP("Extend and flap", "");

        private final String name;
        private final String[] hover;

        WingBehavior(String name, String... hover) {
            this.name = name;
            this.hover = hover;
        }

        @Override
        public String[] getHoverText() {
            return hover;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum WingElytraCompat implements HoverText {
        HIDE_WINGS("Hide wings", "Wings will be hidden while wearing Elytra."),
        HIDE_ELYTRA("Hide Elytra", "The Elytra model will be disabled in favour of the wings."),
        SHOW_BOTH("Show Both", "Both the wings and Elytra will render on top of each other.", "Not sure why you would want this but its an option."),
        REPLACE("Elytra Only", "The wings will replace the Elytra model.", "And " + ChatFormatting.RED + "they will only be visible while wearing Elytra.");

        private final String name;
        private final String[] hover;

        WingElytraCompat(String name, String... hover) {
            this.name = name;
            this.hover = hover;
        }

        @Override
        public String[] getHoverText() {
            return hover;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    //Maybe include some info for some of these? Like the veteran badge?
    public enum Badge implements HoverText {
        DISABLED("Disabled", ""),
        PATREON_OG("OG Patreon", "The original patreon badge"),
        PATREON_DRACONIUM("Draconium", "Draconium Patreon Badge"), //These names can never be changed as they are set in the API
        PATREON_WYVERN("Wyvern", "Wyvern Patreon Badge"),    //These names can never be changed as they are set in the API
        PATREON_DRACONIC("Draconic", "Draconic Patreon Badge"),  //These names can never be changed as they are set in the API
        PATREON_CHAOTIC("Chaotic", "Chaotic Patreon Badge"),   //These names can never be changed as they are set in the API
        OG_VETERAN("OG Veteran", "This badge is only given to long term supporters", "who joined before the Patreon overhaul in December 2022.", "This is a permanent badge."),
        LOLNET("Lolnet", "Legacy Lolnet Badge"),
        CR("CR", "Creation Reborn");

        private final String name;
        private final String[] hover;

        Badge(String name, String... hover) {
            this.name = name;
            this.hover = hover;
        }

        @Override
        public String[] getHoverText() {
            return hover;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public interface HoverText {
        String[] getHoverText();
    }


}
