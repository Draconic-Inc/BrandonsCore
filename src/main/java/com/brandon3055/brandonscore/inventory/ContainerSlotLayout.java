package com.brandon3055.brandonscore.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.brandon3055.brandonscore.inventory.ContainerSlotLayout.SlotType.*;

/**
 * Created by brandon3055 on 5/11/19.
 */
public class ContainerSlotLayout {

    private List<SlotData> slotDataList = new ArrayList<>();
    private Map<SlotType, Map<Integer, SlotData>> slotDataMap = new HashMap<>();

    public ContainerSlotLayout() {}

    protected ContainerSlotLayout retrieveSlotsForContainer(Consumer<Slot> slotConsumer) {
        for (SlotType type : SlotType.values()) {
            slotDataMap.getOrDefault(type, new HashMap<>()).forEach((integer, slotData) -> slotConsumer.accept(slotData.slot));
        }
        return this;
    }

    public ContainerSlotLayout playerMain(PlayerEntity player) {
        if (slotDataMap.keySet().stream().anyMatch(slotType -> !slotType.isPlayer)) {
            throw new IllegalStateException("All player slots must be added before tile slots.");
        }
        LazyOptional<IItemHandler> optionalHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);

        optionalHandler.ifPresent(itemHandler -> {
            for (int x = 0; x < 9; x++) {
                slotDataList.add(new SlotData(this, PLAYER_INV, itemHandler, x));
            }

            for (int y = 0; y < 3; y++) {
                for (int x = 0; x < 9; x++) {
                    slotDataList.add(new SlotData(this, PLAYER_INV, itemHandler, x + y * 9 + 9));
                }
            }
        });

        return this;
    }

    //Armor and off hand
    public ContainerSlotLayout playerEquipSlot(PlayerEntity player, int equipmentSlot) {
        if (slotDataMap.keySet().stream().anyMatch(slotType -> !slotType.isPlayer)) {
            throw new IllegalStateException("All player slots must be added before tile slots.");
        }
        LazyOptional<IItemHandler> optionalHandler  = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.NORTH);
        optionalHandler.ifPresent(itemHandler -> slotDataList.add(new SlotData(this, equipmentSlot < 4 ? PLAYER_ARMOR : PLAYER_OFF_HAND, itemHandler, equipmentSlot)));
        return this;
    }

    public ContainerSlotLayout playerArmor(PlayerEntity player) {
        for (int i = 0; i < 4; i++) {
            playerEquipSlot(player, i);
        }
        return this;
    }

    public ContainerSlotLayout playerOffHand(PlayerEntity player) {
        playerEquipSlot(player, 4);
        return this;
    }

    public ContainerSlotLayout tile(IItemHandler tileItemHandler, int slot) {
        slotDataList.add(new SlotData(this, TILE_INV, tileItemHandler, slot));
        return this;
    }

    public ContainerSlotLayout allTile(IItemHandler tileItemHandler) {
        for (int i = 0; i < tileItemHandler.getSlots(); i++){
            tile(tileItemHandler, i);
        }
        return this;
    }

    public SlotData getSlotData(SlotType type, int index) {
        return slotDataMap.containsKey(type) ? slotDataMap.get(type).get(index) : null;
    }

    public enum SlotType {
        PLAYER_INV(true),
        PLAYER_ARMOR(true),
        PLAYER_OFF_HAND(true),
        TILE_INV(false);

        private boolean isPlayer;

        SlotType(boolean isPlayer) {
            this.isPlayer = isPlayer;
        }
    }

    public static class SlotData {
        protected ContainerSlotLayout layout;
        protected final SlotType type;
        protected IItemHandler itemHandler;
        protected final int index;
        public Slot slot;

        public SlotData(ContainerSlotLayout layout, SlotType type, IItemHandler itemHandler, int index) {
            this.layout = layout;
            this.type = type;
            this.itemHandler = itemHandler;
            this.index = index;
            //'fake' layout just in case the server does not like multiple overlapping slots.
            this.slot = new SlotCheckValid(itemHandler, index, (index % 9) * 18, ((index / 9) + (type.isPlayer ? 0 : 5)) * 18);
            layout.slotDataMap.computeIfAbsent(type, slotType -> new HashMap<>()).put(index, this);
        }

        public void setPos(int xPos, int yPos) {
            slot.xPos = xPos;
            slot.yPos = yPos;
        }
    }

    public interface LayoutFactory<T> {
        ContainerSlotLayout buildLayout(PlayerEntity player, T data);
    }
}
