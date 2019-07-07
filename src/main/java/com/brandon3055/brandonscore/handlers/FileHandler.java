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

import javax.annotation.Nullable;
import java.io.*;
import java.net.*;
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

        if (!brandon3055Folder.exists() && !brandon3055Folder.mkdirs()) {
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
        if (!output.exists() && !output.createNewFile()) {
            throw new IOException("Could not create file, Reason unknown refresh ");
        }

        InputStream is = openURLStream(sourceUrl);
        OutputStream os = new FileOutputStream(output);
        IOUtils.copy(is, os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }

    public static InputStream openURLStream(URL baseUrl) throws IOException {
        return openURLStream(baseUrl.toString());
    }

    public static InputStream openURLStream(String url) throws IOException {
        return openConnection(url, null).getInputStream();
    }

    public static HttpURLConnection openConnection(String url, @Nullable Proxy proxy) throws IOException {
        URL resourceUrl, base, next;
        HttpURLConnection conn = null;
        String location;

        for (int i = 0; i < 5; i++) {
            url = url.replaceAll(" ", "%20");
            resourceUrl = new URL(url);
//            URL url1 = new URL(url);
//            URI uri;
//            try {
//                uri = new URI(url1.getProtocol(), url1.getUserInfo(), url1.getHost(), url1.getPort(), url1.getPath(), url1.getQuery(), url1.getRef());
//                resourceUrl = new URL(uri.toASCIIString());
//            }
//            catch (URISyntaxException e) {
//                resourceUrl = new URL(url);
//            }
            if (proxy != null) {
                conn = (HttpURLConnection) resourceUrl.openConnection(proxy);
            }
            else {
                conn = (HttpURLConnection) resourceUrl.openConnection();
            }

            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");

            switch (conn.getResponseCode()) {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    base = new URL(url);
                    next = new URL(base, location);  // Deal with relative URLs
                    url = next.toExternalForm();
                    continue;
            }

            break;
        }

        return conn;
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
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            JsonParser parser = new JsonParser();
            reader.setLenient(true);
            JsonElement element = parser.parse(reader);
            return element.getAsJsonObject();
        }
    }

    public static void writeJson(JsonObject obj, File file) throws IOException {
        try(JsonWriter writer = new JsonWriter(new FileWriter(file))) {
            writer.setIndent("  ");
            Streams.write(obj, writer);
            writer.flush();
        }
    }
}
