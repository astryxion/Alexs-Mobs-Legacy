package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMBiomes112;
import com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Per-mob biome JSON under {@code config/spawns/}. Lists use real 1.12.2 registry ids from
 * {@link AMBiomes112}; legacy 1.16 names in old files are remapped or replaced with matcher defaults.
 */
public final class AMSpawnBiomeConfiguration {

    private static File spawnConfigDir;
    private static boolean loggedVanillaBiomeCount;

    private AMSpawnBiomeConfiguration() {
    }

    public static void init(File modConfigDirectory) {
        spawnConfigDir = new File(modConfigDirectory, "spawns");
        if (!spawnConfigDir.exists()) {
            spawnConfigDir.mkdirs();
        }
    }

    /** Biomes matching the 1.12.2 rule, using live registry names only. */
    public static List<String> resolveDefaultBiomes(AMNativeSpawnBiomes.Matcher matcher) {
        List<String> biomes = Lists.newArrayList();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (biome.getRegistryName() != null && matcher.test(biome)) {
                biomes.add(biome.getRegistryName().toString());
            }
        }
        Collections.sort(biomes);
        return biomes;
    }

    public static List<String> loadBiomeList(String configBaseName, AMNativeSpawnBiomes.Matcher defaultMatcher) {
        logVanillaBiomeCatalogOnce();
        List<String> defaults = resolveDefaultBiomes(defaultMatcher);
        if (spawnConfigDir == null) {
            return defaults;
        }
        String fileName = configBaseName + "_biomes.json";
        List<String> loaded = AMSpawnJsonUtil.getOrCreateConfigFile(
                spawnConfigDir,
                fileName,
                defaults,
                new TypeToken<List<String>>() {
                }.getType());
        List<String> raw = (loaded == null || loaded.isEmpty()) ? Collections.emptyList() : loaded;
        List<String> normalized = AMBiomes112.normalizeList(raw);

        if (AMBiomes112.shouldReplaceWithDefaults(raw, normalized, defaults)) {
            AlexsMobs.LOGGER.info(
                    "Alex's Mobs: resetting stale spawn biome list '{}' ({} invalid/legacy ids → {} valid 1.12.2 biomes)",
                    fileName, raw.size(), defaults.size());
            AMSpawnJsonUtil.writeConfigFile(new File(spawnConfigDir, fileName), defaults);
            return new ArrayList<>(defaults);
        }

        LinkedHashSet<String> merged = new LinkedHashSet<>(normalized);
        merged.addAll(defaults);
        List<String> result = new ArrayList<>(merged);
        Collections.sort(result);
        return result;
    }

    private static void logVanillaBiomeCatalogOnce() {
        if (loggedVanillaBiomeCount) {
            return;
        }
        loggedVanillaBiomeCount = true;
        AlexsMobs.LOGGER.info("Alex's Mobs: {} vanilla biomes in registry (spawn lists use minecraft:* ids from 1.12.2, not 1.16+ names)",
                AMBiomes112.vanillaIds().size());
    }
}
