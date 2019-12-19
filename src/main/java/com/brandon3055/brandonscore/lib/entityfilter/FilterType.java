package com.brandon3055.brandonscore.lib.entityfilter;

import java.util.function.Function;

/**
 * Created by brandon3055 on 7/11/19.
 * Docs indicate what configurable options each type has.
 */
public enum FilterType {
    /**
     * Include / Exclude Hostile Mods           (Hostiles Only | Non Hostiles Only)
     */
    HOSTILE(0, FilterHostile::new),
    /**
     * Include / Exclude Tamed                  (Tamed Only | Non Tamed Only)
     * - Include Tamable Mobs                   (Check Tamable) "When enabled will also check if the entity is tamable when applying the filter"
     */
    TAMED(1, FilterTamed::new),
    /**
     * Include / Exclude Adults                 (Adult Only | Child Entities Only)
     * Include / Exclude Non-Ageable Mobs       (Accept Non-Ageable Entities) "Should non-ageable entities be accepted or rejected by this filter."
     */
    ADULTS(2, FilterAdults::new),
    /**
     * Include / Exclude Players                (Players Only | Ignore Players)
     * - Player Filter                          "Apply this filter to a specific player"
     */
    //There is no reason for ether of these to have a list because the user can simply add multiple player or entity_type filters.
    PLAYER(3, FilterPlayer::new),
    /**
     * Include / Exclude Filtered Entities      (Filtered Entities Only | Ignore Filtered Entities)
     * - Entity Filter                          "Specify an entity type"
     */
    ENTITY_TYPE(4, FilterEntity::new),
//    /**
//     * Include / Exclude ItemStacks             (Item Stacks Only | Ignore Item Stacks)
//     * - Item Filter                            "Apply this filter to specific items. Applies to all item stack entities if list is empty"
//     */
//    ITEM_ENTITY(5, () -> null),
//   I would have to add things like damage and possibly ore dict to make this useful. At that point why not just let the user specify a bunch of ItemFilter's instead. Its essentially the same thing.
    /**
     * Include / Exclude Filtered Stacks        (Include Matching Stacks | Ignore Matching Stacks)
     * - ItemType or Ore Dictionary
     * - Count (With Wiled card option)
     * - Item Damage (With Wiled card option)
     *   - Does not apply to ore dict items
     * - NBT (WIll have an enable option Once enabled the current stack nbt will be loaded into a text field where it can be edited by the player)
     *   - Does not apply to ore dict items
     * Include / Exclude Blocks                 (Include Stacks Containing Blocks | Exclude Stacks Containing Blocks)
     *   - Option is disabled if any other options are set.
     */
    ITEM_FILTER(5, FilterItem::new),
    /**
     * This does not actually filter anything but instead gives a way to group a bunch of filters and an 'AND' or 'OR' configuration
     */
    FILTER_GROUP(6, FilterGroup::new);

    public final int index;
    private final Function<EntityFilter, FilterBase> nodeBuilder;
    public static final FilterType[] filterTypeMap = new FilterType[FilterType.values().length];

    FilterType(int index, Function<EntityFilter, FilterBase> nodeSupplier) {
        this.index = index;
        this.nodeBuilder = nodeSupplier;
    }

    public FilterBase createNode(EntityFilter filter) {
        return nodeBuilder.apply(filter);
    }

    static {
        for (FilterType type : FilterType.values()) {
            filterTypeMap[type.index] = type;
        }
    }
}
