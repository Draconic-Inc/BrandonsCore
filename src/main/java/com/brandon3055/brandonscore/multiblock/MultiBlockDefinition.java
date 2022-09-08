package com.brandon3055.brandonscore.multiblock;

import codechicken.lib.vec.Rotation;
import codechicken.lib.vec.Transformation;
import codechicken.lib.vec.Vector3;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

/**
 * Created by brandon3055 on 26/06/2022
 */
public class MultiBlockDefinition {
    private final ResourceLocation id;
    // Holding onto this, so I can yeet it at the client because I'm to lazy to write dedicated network serialization when I already have a convenient json and a way to read it.
    private final JsonElement json;

    /**
     * The structure origin offset.
     * This shouldn't be needed outside this class because this offset is already applied when the structure is loaded.
     * Meaning the block at 0, 0, 0 within the structure is the origin.
     */
    private BlockPos origin = BlockPos.ZERO;
    //Position the position of each block relative to origin
    private Map<BlockPos, MultiBlockPart> blockMap = new HashMap<>();

    public MultiBlockDefinition(ResourceLocation id, JsonElement json) {
        this.id = id;
        this.json = json;
        loadFromJson();
    }

    public ResourceLocation getId() {
        return id;
    }

    public JsonElement getJson() {
        return json;
    }

    // Structure map getters

    /**
     * @return The block map for this structure.
     */
    public Map<BlockPos, MultiBlockPart> getBlocks() {
        return ImmutableMap.copyOf(blockMap);
    }

    /**
     * Returns the structures block map translated so that the structure's origin is at the given position.
     *
     * @param worldOrigin A position in the world.
     * @return The block map.
     */
    public Map<BlockPos, MultiBlockPart> getBlocksAt(BlockPos worldOrigin) {
        Map<BlockPos, MultiBlockPart> translated = new HashMap<>();
        blockMap.forEach((pos, part) -> translated.put(worldOrigin.offset(pos), part));
        return ImmutableMap.copyOf(translated);
    }

    /**
     * Applies a rotation to the multi block structure. This rotation is applied relative to the structure's origin.
     * For obvious reasons the rotation angle must be a multiple of 90 degrees.
     *
     * @param rotation Rotation to apply to the structure.
     * @return The block map with the applied rotation.
     */
    public Map<BlockPos, MultiBlockPart> getBlocks(Rotation rotation) {
        Map<BlockPos, MultiBlockPart> transformed = new HashMap<>();
        Transformation transform = rotation.at(Vector3.CENTER).inverse();
        blockMap.forEach((pos, part) -> {
            Vector3 vec = Vector3.fromBlockPosCenter(pos);
            vec.apply(transform);
            transformed.put(vec.pos(), part);
        });
        return ImmutableMap.copyOf(transformed);
    }

    /**
     * Combination of {@link #getBlocksAt(BlockPos)} and {@link #getBlocks(Rotation)}
     *
     * @see #getBlocksAt(BlockPos)
     * @see #getBlocks(Rotation)
     */
    public Map<BlockPos, MultiBlockPart> getBlocksAt(BlockPos worldOrigin, Rotation rotation) {
        Map<BlockPos, MultiBlockPart> transformed = new HashMap<>();
        Transformation transform = rotation.at(Vector3.CENTER).inverse();
        blockMap.forEach((pos, part) -> {
            Vector3 vec = Vector3.fromBlockPosCenter(pos);
            vec.apply(transform);
            transformed.put(vec.add(worldOrigin).pos(), part);
        });
        return ImmutableMap.copyOf(transformed);
    }

    // Structure validation

    /**
     * Test if this structure matches a structure at the given position.
     *
     * @param level The world
     * @param originPos The in world position that corresponds to the structure's origin.
     * @return A list containing any blocks that do not match this structure. Empty list means the structure is valid.
     */
    public List<InvalidPart> test(Level level, BlockPos originPos) {
        List<InvalidPart> result = new ArrayList<>();
        getBlocksAt(originPos).forEach((pos, part) -> {
            if (!part.isMatch(level, pos)) result.add(new InvalidPart(pos, part));
        });
        return result;
    }

    /**
     * Test if this structure matches a structure at the given position.
     *
     * @param level The world
     * @param originPos The in world position that corresponds to the structure's origin.
     * @param rotation Rotation to apply to the structure.
     * @return A list containing any blocks that do not match this structure. Empty list means the structure is valid.
     * @see #getBlocks(Rotation)
     */
    public List<InvalidPart> test(Level level, BlockPos originPos, Rotation rotation) {
        List<InvalidPart> result = new ArrayList<>();
        getBlocksAt(originPos, rotation).forEach((pos, part) -> {
            if (!part.isMatch(level, pos)) result.add(new InvalidPart(pos, part));
        });
        return result;
    }


    private void loadFromJson() {
        JsonObject obj = json.getAsJsonObject();
        if (obj.has("origin")) {
            JsonObject originObj = obj.getAsJsonObject("origin");
            this.origin = new BlockPos(originObj.get("x").getAsInt(), originObj.get("y").getAsInt(), originObj.get("z").getAsInt());
        }

        Map<String, MultiBlockPart> keyMap = new HashMap<>();
        JsonObject keysObj = obj.getAsJsonObject("keys");
        for (Map.Entry<String, JsonElement> entry : keysObj.entrySet()) {
            String key = entry.getKey();
            if (keyMap.containsKey(key)) {
                throw new IllegalStateException("Duplicate key detected!, " + id);
            }

            JsonObject keyVal = entry.getValue().getAsJsonObject();
            if (keyVal.has("tag")) {
                ResourceLocation resourcelocation = new ResourceLocation(keyVal.get("tag").getAsString());
                TagKey<Block> tagkey = TagKey.create(Registry.BLOCK_REGISTRY, resourcelocation);
                keyMap.put(key, new TagPart(tagkey));
            } else if (keyVal.has("block")) {
                ResourceLocation resourcelocation = new ResourceLocation(keyVal.get("block").getAsString());
                if (Blocks.AIR.getRegistryName().equals(resourcelocation)) {
                    keyMap.put(key, new EmptyPart());
                } else {
                    keyMap.put(key, new BlockPart(resourcelocation));
                }
            } else {
                throw new IllegalArgumentException("Invalid block key detected!, " + keyVal + ", " + id);
            }
        }

        JsonArray structure = obj.getAsJsonArray("structure");
        int layer = 0; //Y Pos
        for (JsonElement layerElement : structure) {
            JsonArray layerArray = layerElement.getAsJsonArray();
            int row = 0; //Z Pos
            for (JsonElement rowElement : layerArray) {
                String rowString = rowElement.getAsString();
                for (int i = 0; i < rowString.length(); i++) {
                    String key = String.valueOf(rowString.charAt(i));
                    BlockPos pos = new BlockPos(i, layer, row).subtract(origin);
                    if (blockMap.containsKey(pos)) {
                        throw new IllegalStateException("Duplicate Position Detected"); //<- Should be impossible but just in case.
                    }
                    if (!key.equals(" ")) {
                        if (!keyMap.containsKey(key)) {
                            throw new IllegalArgumentException("Undefined key in multiblock definition: " + id + ", Key: " + key);
                        }
                        blockMap.put(pos, keyMap.get(key));
                    }
                }
                row++;
            }
            layer++;
        }
    }
}
