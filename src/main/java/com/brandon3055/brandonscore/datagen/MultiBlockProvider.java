package com.brandon3055.brandonscore.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brandon3055 on 26/06/2022
 */
public abstract class MultiBlockProvider implements DataProvider {
    public static final Logger LOGGER = LogManager.getLogger(MultiBlockProvider.class);

    private final DataGenerator gen;
    private final String modid;
    private Map<String, JsonObject> builtMultiBlocks = new HashMap<>();

    public MultiBlockProvider(DataGenerator gen, String modid) {
        this.gen = gen;
        this.modid = modid;
    }

    @Override
    public void run(CachedOutput cache) throws IOException {
        builtMultiBlocks.clear();
        buildMultiBlocks();
        for (String name : builtMultiBlocks.keySet()) {
            saveMultiBlock(cache, new ResourceLocation(modid, name), builtMultiBlocks.get(name));
        }
    }

    private void saveMultiBlock(CachedOutput cache, ResourceLocation id, JsonObject multiBlockJson) {
        Path mainOutput = gen.getOutputFolder();
        String pathSuffix = "data/" + id.getNamespace() + "/multiblocks/" + id.getPath() + ".json";
        Path outputPath = mainOutput.resolve(pathSuffix);
        try {
            DataProvider.saveStable(cache, multiBlockJson, outputPath);
        } catch (IOException e) {
            LOGGER.error("Couldn't save multiblock structure to {}", outputPath, e);
        }
    }

    protected abstract void buildMultiBlocks();

    public Builder builder(String id) {
        return new Builder(id);
    }

    @Override
    public String getName() {
        return "Multi-Blocks: " + modid;
    }

    /**
     * Multiblock definitions are built up row by row, layer by layer.
     * A multiblock is assembled as follows.
     * Initial starting pos of the builder is 0, 0, 0
     * Calling addRow adds a new row of blocks along the xAxis starting at X=0
     * It then increments Z by one and resets X to zero.
     * This allows you to fill out a layer of blocks.
     * Once the layer is complete calling newLayer will reset the builder position to Z=0, Y+1, Z=00
     */
    protected class Builder {
        private JsonObject keys = new JsonObject();
        private JsonArray layerArray = new JsonArray();
        private JsonArray currentLayer = new JsonArray();
        private String id;
        private BlockPos originPos = BlockPos.ZERO;

        private Builder(String id) {
            this.id = id;
        }

        private Builder key(char key, String type, String value) {
            if (keys.has(String.valueOf(key))) {
                throw new RuntimeException("MultiBlockProvider: Attempted to add the same key twice for multiblock: " + id + ", Key: " + key);
            }
            JsonObject keyObj = new JsonObject();
            keyObj.addProperty(type, value);
            keys.add(String.valueOf(key), keyObj);
            return this;
        }

        public Builder key(char key, TagKey<Block> tag) {
            return key(key, "tag", tag.location().toString());
        }

        public Builder key(char key, Block block) {
            return key(key, "block", ForgeRegistries.BLOCKS.getKey(block).toString());
        }

        /**
         * This is a position within the multiblock that is the starting point for multiblock validation and placement.
         * By default, this is 0, 0, 0 (Corresponding to XMin,YMin,ZMin) but in most cases this will be the position of the "controller" block within the structure.
         * For example if your multiblock bounds are 5x5x5 and your controller is in the dead center of the structure,
         * the root pos would be 2, 2, 2
         */
        public Builder setOrigin(BlockPos originPos) {
            this.originPos = originPos;
            return this;
        }

        /**
         * Adds a row of blocks along the xAxis
         * Then increments the builder's z position by 1 and resets the x position to zero.
         * <p>
         * Note: Use a blank space " " To mark a position as ignored, meaning it's not a part of the structure, and you don't care what block is in that position.
         * If a position must be air then simply define a kay for Blocks.AIR (This will automatically account for things like fake air blocks)
         */
        public Builder addRow(String rowOfkeys) {
            for (char keyChar : rowOfkeys.toCharArray()) {
                String key = String.valueOf(keyChar);
                if (!keys.has(key) && !key.equals(" ")) {
                    throw new RuntimeException("MultiBlockProvider: Attempted to add undefined key for multiblock: " + id + ", Key: " + key);
                }
            }
            currentLayer.add(rowOfkeys);
            return this;
        }

        /**
         * Resets builder to XMin,ZMin and increments the builders Y position by one.
         */
        public Builder newLayer() {
            layerArray.add(currentLayer);
            currentLayer = new JsonArray();
            return this;
        }

        public void build() {
            if (!currentLayer.isEmpty()) {
                newLayer();
            }
            JsonObject json = new JsonObject();
            json.add("keys", keys);
            if (!originPos.equals(BlockPos.ZERO)) {
                JsonObject origin = new JsonObject();
                origin.addProperty("x", originPos.getX());
                origin.addProperty("y", originPos.getY());
                origin.addProperty("z", originPos.getZ());
                json.add("origin", origin);
            }
            json.add("structure", layerArray);
            builtMultiBlocks.put(id, json);
        }
    }
}
