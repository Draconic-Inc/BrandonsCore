package com.brandon3055.brandonscore.handlers;

import com.brandon3055.brandonscore.utils.LogHelperBC;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by Brandon on 7/6/2015.
 */
public class FileHandler {
    public static File rootConfigFolder;
    public static File brandon3055Folder;
    public static File mcDirectory;

    public static void init(FMLPreInitializationEvent event) {
        rootConfigFolder = event.getModConfigurationDirectory();
        brandon3055Folder = new File(rootConfigFolder, "brandon3055");

        if (!brandon3055Folder.exists() && brandon3055Folder.mkdirs()) {
            LogHelperBC.error("Could not create config directory! Things are probably going to break!");
        }

        mcDirectory = rootConfigFolder.getParentFile();
    }

    /**
     * This is a simple helper method that downloads a file from the specified url to the specified output file.
     * This only supports http connections and will not work with https.
     * This is a blocking method meaning it will hang the thread until the download is complete or an exception is thrown.
     *
     * @param sourceUrl Source URL
     * @param output    Target File
     * @throws IOException
     */
    public static void downloadFile(String sourceUrl, File output) throws IOException {
        URL url = new URL(sourceUrl);

        if (!output.exists() && !output.createNewFile()) {
            throw new IOException("Could not create file, Reason unknown");
        }

        InputStream is = openURLStream(url);
        OutputStream os = new FileOutputStream(output);
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }

    public static InputStream openURLStream(URL url) throws IOException {
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        return urlConnection.getInputStream();
    }

    private static final Set<Character> ILLEGAL_CHARACTERS = Sets.newHashSet('/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':');

    public static final Predicate<String> FILE_NAME_VALIDATOR = s -> {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) {
            if (ILLEGAL_CHARACTERS.contains(c)) return false;
        }
        return true;
    };

    public static JsonObject readObj(File file) throws IOException {
        JsonReader reader = new JsonReader(new FileReader(file));
        JsonParser parser = new JsonParser();
        reader.setLenient(true);
        JsonElement element = parser.parse(reader);
        org.apache.commons.io.IOUtils.closeQuietly(reader);
        return element.getAsJsonObject();
    }

    public static void writeJson(JsonObject obj, File file) throws IOException {
        JsonWriter writer = new JsonWriter(new FileWriter(file));
        writer.setIndent("  ");
        Streams.write(obj, writer);
        writer.flush();
        org.apache.commons.io.IOUtils.closeQuietly(writer);
    }
}
