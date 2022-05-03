package com.brandon3055.brandonscore.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.brandon3055.brandonscore.BrandonsCore.equipmentManager;
import static com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotType.*;

/**
 * Created by brandon3055 on 5/11/19.
 */
public class ContainerSlotLayout {

    private List<SlotMover> slotMoverList = new ArrayList<>();
    private Map<SlotType, Map<Integer, SlotMover>> slotDataMap = new HashMap<>();

    public ContainerSlotLayout() {}

    protected ContainerSlotLayout retrieveSlotsForContainer(Consumer<Slot> slotConsumer) {
        for (SlotType type : SlotType.values()) {
            slotDataMap.getOrDefault(type, new HashMap<>()).forEach((integer, slotMover) -> slotConsumer.accept(slotMover.slot));
        }
        return this;
    }

    public ContainerSlotLayout playerMain(Player player) {
        if (slotDataMap.keySet().stream().anyMatch(slotType -> !slotType.isPlayer)) {
            throw new IllegalStateException("All player slots must be added before tile slots.");
        }
        LazyOptional<IItemHandler> optionalHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);

        optionalHandler.ifPresent(itemHandler -> {
            for (int x = 0; x < 9; x++) {
                slotMoverList.add(new LayoutSlotData(this, PLAYER_INV, itemHandler, x));
            }

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    slotMoverList.add(new LayoutSlotData(this, PLAYER_INV, itemHandler, x + y * 9 + 9));
                }
            }
        });

        return this;
    }

    //Armor and off hand
    public ContainerSlotLayout playerEquipSlot(Player player, int equipmentSlot) {
        if (slotDataMap.keySet().stream().anyMatch(slotType -> !slotType.isPlayer)) {
            throw new IllegalStateException("All player slots must be added before tile slots.");
        }
        LazyOptional<IItemHandler> optionalHandler  = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH);
        optionalHandler.ifPresent(itemHandler -> slotMoverList.add(new LayoutSlotData(this, equipmentSlot < 4 ? PLAYER_ARMOR : PLAYER_OFF_HAND, itemHandler, equipmentSlot)));
        return this;
    }

    public ContainerSlotLayout playerArmor(Player player) {
        for (int i = 0; i < 4; i++) {
            playerEquipSlot(player, i);
        }
        return this;
    }

    public ContainerSlotLayout playerEquipMod(Player player) {
        if (slotDataMap.keySet().stream().anyMatch(slotType -> !slotType.isPlayer)) {
            throw new IllegalStateException("All player slots must be added before tile slots.");
        }
        if (equipmentManager != null) {
            LazyOptional<IItemHandlerModifiable> optional = equipmentManager.getInventory(player);
            optional.ifPresent(handler -> {
                for (int i = 0; i < handler.getSlots(); i++) {
                    slotMoverList.add(new LayoutSlotData(this, PLAYER_EQUIPMENT, handler, i));
                }
            });
        }
        return this;
    }

    public ContainerSlotLayout playerOffHand(Player player) {
        playerEquipSlot(player, 4);
        return this;
    }

    public ContainerSlotLayout tile(IItemHandler tileItemHandler, int slot) {
        slotMoverList.add(new LayoutSlotData(this, TILE_INV, tileItemHandler, slot));
        return this;
    }

    public ContainerSlotLayout allTile(IItemHandler tileItemHandler) {
        for (int i = 0; i < tileItemHandler.getSlots(); i++){
            tile(tileItemHandler, i);
        }
        return this;
    }

    public SlotMover getSlotData(SlotType type, int index) {
        return slotDataMap.containsKey(type) ? slotDataMap.get(type).get(index) : null;
    }

    public int getPlayerSlotCount() {
        return (int) slotMoverList.stream().filter(e -> e instanceof LayoutSlotData && ((LayoutSlotData) e).type.isPlayer).count();
    }

    public enum SlotType {
        PLAYER_INV(true),
        PLAYER_ARMOR(true),
        PLAYER_OFF_HAND(true),
        PLAYER_EQUIPMENT(true),
        TILE_INV(false);

        private boolean isPlayer;

        SlotType(boolean isPlayer) {
            this.isPlayer = isPlayer;
        }
    }

    private static class LayoutSlotData extends SlotMover {
        protected ContainerSlotLayout layout;
        protected final SlotType type;
        protected IItemHandler itemHandler;

        public LayoutSlotData(ContainerSlotLayout layout, SlotType type, IItemHandler itemHandler, int index) {
            this.layout = layout;
            this.type = type;
            //'fake' layout just in case the server does not like multiple overlapping slots.
            this.slot = new SlotCheckValid(itemHandler, index, (index % 9) * 18, ((index / 9) + (type.isPlayer ? 0 : 5)) * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return super.mayPlace(stack) && type != PLAYER_EQUIPMENT;
                }
            };
            layout.slotDataMap.computeIfAbsent(type, slotType -> new HashMap<>()).put(index, this);
        }
    }

    public interface LayoutFactory<T> {
        ContainerSlotLayout buildLayout(Player player, T data);
    }
}
