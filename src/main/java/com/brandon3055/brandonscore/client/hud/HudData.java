package com.brandon3055.brandonscore.client.hud;

import com.brandon3055.brandonscore.BrandonsCore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.brandon3055.brandonscore.client.hud.HudManager.hudElements;

/**
 * Created by brandon3055 on 1/8/21
 */
public class HudData {
    private static Path settingsPath = Paths.get("./config/brandon3055/hud_settings.json");
    private static Gson gson = new Gson();
    private static int settingsDirty = 0;

    protected static void clientTick() {
        if (settingsDirty > 0 && --settingsDirty <= 0) {
            saveSettings();
        }
    }

    public static void dirtySettings() {
        settingsDirty = 600; //Settings will be saved 30 seconds after the last edit (Or when the settings gui is closed)
    }

    public static void saveIfDirty() {
        if (settingsDirty > 0) {
            settingsDirty = 0;
            saveSettings();
        }
    }

    private static void saveSettings() {
        JsonObject storage = new JsonObject();

        try (JsonWriter fileWriter = new JsonWriter(new FileWriter(settingsPath.toFile()))) {
            fileWriter.setIndent("    ");
            for (ResourceLocation key : hudElements.keySet()) {
                CompoundTag nbt = new CompoundTag();
                hudElements.get(key).writeNBT(nbt);
                storage.addProperty(key.toString(), nbt.toString());
            }
            Streams.write(storage, fileWriter);
            fileWriter.flush();
        }
        catch (Throwable e) {
            BrandonsCore.LOGGER.error("An error occurred while saving hud settings to disk!");
            e.printStackTrace();
            //Just in case we managed write some corrupt data.
            settingsPath.toFile().delete();
        }
    }

    protected static void loadSettings() {
        if (!settingsPath.toFile().exists()) {
            saveSettings();
        }

        try (JsonReader reader = new JsonReader(new FileReader(settingsPath.toFile()))) {
            JsonParser parser = new JsonParser();
            reader.setLenient(true);
            JsonObject element = (JsonObject) parser.parse(reader);

            for (Map.Entry<String, JsonElement> entry : element.entrySet()) {
                ResourceLocation key = new ResourceLocation(entry.getKey());
                if (hudElements.containsKey(key)) {
                    CompoundTag nbt = TagParser.parseTag(entry.getValue().getAsString());
                    hudElements.get(key).readNBT(nbt);
                }
            }
        }
        catch (Throwable e) {
            BrandonsCore.LOGGER.error("An error occurred while loading hud settings from disk!");
            e.printStackTrace();
        }
        hudElements.values().forEach(e -> e.setChangeListener(HudData::dirtySettings));
    }
}
