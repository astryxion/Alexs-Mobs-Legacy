package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.reflect.Type;

/**
 * Familiar Fauna-style JSON biome list I/O ({@code config/alexsmobs/spawns/*_biomes.json}).
 */
public final class AMSpawnJsonUtil {

    public static final Gson SERIALIZER = new GsonBuilder().setPrettyPrinting().create();

    private AMSpawnJsonUtil() {
    }

    public static <T> T getOrCreateConfigFile(File configDir, String configName, T defaults, Type type) {
        File configFile = new File(configDir, configName);
        if (!configFile.exists()) {
            writeFile(configFile, defaults);
        }
        try {
            return SERIALIZER.fromJson(FileUtils.readFileToString(configFile), type);
        } catch (Exception e) {
            AlexsMobs.LOGGER.error("Error parsing spawn biome config from json: {}", configFile, e);
        }
        return defaults;
    }

    public static void writeConfigFile(File outputFile, Object obj) {
        writeFile(outputFile, obj);
    }

    private static boolean writeFile(File outputFile, Object obj) {
        try {
            FileUtils.write(outputFile, SERIALIZER.toJson(obj));
            return true;
        } catch (Exception e) {
            AlexsMobs.LOGGER.error("Error writing spawn biome config {}: {}", outputFile.getAbsolutePath(), e.getMessage());
            return false;
        }
    }
}
