package com.brandon3055.brandonscore.handlers.contributor;

import com.brandon3055.brandonscore.api.TechLevel;
import com.brandon3055.brandonscore.api.TimeKeeper;
import com.brandon3055.brandonscore.handlers.contributor.ContributorConfig.Badge;
import com.brandon3055.brandonscore.init.ClientInit;
import com.brandon3055.brandonscore.network.BCoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by brandon3055 on 21/11/2022
 * <pre>
 * Contributor Flags:
 * "dev":""                      User is a DE developer. Enable all the things!
 * "shield_rgb":""               Enables armor shield colour customisation. (Not sure if I am going to add this yet)
 * "wings_rgb":""                Enables wings colour customisation. (Not sure if I am going to add this yet)
 * "lolnet":""                   Enables the legacy Lolnet contributor badge.
 * "cr":""                       Possible future Creation Reborn badge?
 * "wings":"tier" (empty=all)    Enables wings of the specified tier or all tiers if no tier is specified
 * "patreon":"badge" (empty=all) Enables badge of the specified tier or all tiers if no tier is specified (OG badge is always included if patreon flag is set)
 * "og_veteran":""               For those who, prior to this overhaul have already supported me for a year or more.
 * "tier+1":"day/month/year"     Everyone who was already a patreon at the time of the overhaul gets a +1 tier bonus. The duration is 1:1 with how long they have been a patreon.
 * </pre>
 */
public class ContributorProperties {
    private static final Logger LOGGER = LogManager.getLogger(ContributorProperties.class);
    private final UUID userID;
    private boolean isContributor = false;
    private boolean isDev = false;
    private boolean shieldRGB = false;
    private boolean wingsRGB = false;
    private boolean isPatron = false;
    private List<Badge> badges = new ArrayList<>();
    private List<TechLevel> wings = new ArrayList<>();
    private Calendar plusOneEnd = null;

    private ContributorConfig config = null;
    private Animations anim = null;
    private boolean loadComplete = false;
    private List<Consumer<ContributorProperties>> loadWaitList = new ArrayList<>();

    @Deprecated //Used for testing purposes
    public ContributorProperties() {
        this.userID = UUID.randomUUID();
        isContributor = isDev = shieldRGB = wingsRGB = true;
        badges.addAll(Arrays.asList(Badge.values()));
        wings.addAll(Arrays.asList(TechLevel.values()));
        loadComplete = true;
    }

    public ContributorProperties(Player player) {
        ContributorFetcher.getContributorStatus(player, this::fetchComplete);
        this.userID = player.getUUID();
    }

    /**
     * Called when the ContributorFetcher has finished with this user.
     * This should always get called but the flags will be null if the user is not a contributor.
     */
    @SuppressWarnings("MagicConstant")
    protected void fetchComplete(@Nullable Map<String, String> flags) {
        if (flags == null || flags.isEmpty()) {
            confirmationFailed();
            return;
        }
        config = new ContributorConfig(this);
        isContributor = true;
        isDev = flags.containsKey("dev");

        if (isDev) {
            shieldRGB = wingsRGB = true;
            badges.addAll(Arrays.asList(Badge.values()));
            wings.addAll(Arrays.asList(TechLevel.values()));
        } else {
            badges.add(Badge.DISABLED);
            shieldRGB = flags.containsKey("shield_rgb");
            wingsRGB = flags.containsKey("wings_rgb");
            if (flags.containsKey("lolnet")) badges.add(Badge.LOLNET);
            if (flags.containsKey("cr")) badges.add(Badge.CR);
            if (flags.containsKey("og_veteran")) badges.add(Badge.OG_VETERAN);
            //I think i'm just going to handle applying this on the API side.
            //It makes more sense considering the one-off nature of this
            if (flags.containsKey("tier+1")) {
                try {
                    String[] date = flags.get("tier+1").split("/");
                    Calendar end = Calendar.getInstance();
                    end.clear();
                    end.set(Integer.parseInt(date[0]), Integer.parseInt(date[1]) + 1, Integer.parseInt(date[2]));
                    plusOneEnd = end;
                } catch (Throwable ignored) {}
            }

            if (flags.containsKey("wings")) {
                String tier = flags.get("wings");
                if (tier.isEmpty()) {
                    wings.addAll(Arrays.asList(TechLevel.values()));
                } else {
                    try {
                        wings.add(TechLevel.valueOf(tier.toUpperCase(Locale.ENGLISH)));
                    } catch (Throwable e) {
                        LOGGER.error("Unable to parse wings tier {}", tier, e);
                    }
                }
            }

            if (flags.containsKey("patreon")) {
                isPatron = true;
                String tier = flags.get("patreon");
                badges.add(Badge.PATREON_OG);
                if (tier.isEmpty()) {
                    badges.addAll(Arrays.asList(Badge.PATREON_DRACONIUM, Badge.PATREON_WYVERN, Badge.PATREON_DRACONIC, Badge.PATREON_CHAOTIC));
                } else {
                    try {
                        badges.add(Badge.valueOf(tier.toUpperCase(Locale.ENGLISH)));
                    } catch (Throwable e) {
                        LOGGER.error("Unable to parse patreon tier {}", tier, e);
                    }
                }
            }
        }

        if (badges.size() == 1 && badges.contains(Badge.DISABLED)) {
            badges.clear();
        }

        Player client = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> ClientInit::getClientPlayer);
        if (client != null && client.getUUID().equals(userID)) {
            config = ContributorConfig.load();
            config.props = this;
        }
        loadComplete = true;
        loadWaitList.forEach(e -> e.accept(this));
        loadWaitList.clear();
    }

    private void confirmationFailed() {
        loadComplete = true;
        isContributor = false;
        loadWaitList.clear();
    }

    public boolean isLoadComplete() {
        return loadComplete;
    }

    /**
     * Allows you to get a callback if and when this player is confirmed to be a contributor.
     * Will be called immediately if already confirmed.
     */
    public void onContributorLoaded(Consumer<ContributorProperties> callBack) {
        if (loadComplete && isContributor()) {
            callBack.accept(this);
        } else if (!loadComplete) {
            loadWaitList.add(callBack);
        }
    }

    public void clientTick() {
        if (!isContributor() || Minecraft.getInstance().isPaused()) return;
        if (TimeKeeper.getClientTick() % 20 == 0 && getConfig().getResetSyncRequired()) {
            BCoreNetwork.sendContributorConfigToServer(this);
        }
        getAnim().tick();
        //Will do wing animation updates here
    }

    public UUID getUserID() {
        return userID;
    }

    public boolean isDev() {
        return isDev;
    }

    public boolean isContributor() {
        return isContributor;
    }

    public boolean isPatron() {
        return isPatron;
    }

    public boolean hasShieldRGB() {
        return shieldRGB;
    }

    public boolean hasWingsRGB() {
        return wingsRGB;
    }

    public List<TechLevel> getWingTiers() {
        return wings;
    }

    public boolean hasWings() {
        return !wings.isEmpty();
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public ContributorConfig getConfig() {
        if (config == null) {
            setConfig(new ContributorConfig());
        }
        return config;
    }

    public Animations getAnim() {
        if (anim == null) {
            anim = new Animations(this);
        }
        return anim;
    }

    public void setConfig(ContributorConfig config) {
        this.config = config;
        this.config.props = this;
    }

    @Override
    public String toString() {
        return "ContributorProperties{" +
                "userID=" + userID +
                ", isContributor=" + isContributor +
                ", isDev=" + isDev +
                ", shieldRGB=" + shieldRGB +
                ", wingsRGB=" + wingsRGB +
                ", badges=" + badges +
                ", wings=" + wings +
                ", plusOneEnd=" + plusOneEnd +
                ", config=" + config +
                ", anim=" + anim +
                ", loadComplete=" + loadComplete +
                ", loadWaitList=" + loadWaitList +
                '}';
    }
}
