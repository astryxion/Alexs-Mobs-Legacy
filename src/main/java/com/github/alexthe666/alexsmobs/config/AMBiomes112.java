package com.github.alexthe666.alexsmobs.config;

import com.google.common.collect.ImmutableMap;
import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Authoritative 1.12.2 biome registry ids for Alex's Mobs spawning.
 * <p>
 * Vanilla overworld biomes use {@code minecraft:<name>} from {@link Biomes} registration
 * (e.g. {@code ice_plains}, not 1.16 {@code snowy_plains} or legacy {@code ice_flats}).
 * Modded biomes pass through when present in {@link ForgeRegistries#BIOMES}.
 */
public final class AMBiomes112 {

    /**
     * Legacy / 1.16+ config ids and old JSON typos mapped to real 1.12.2 registry names.
     */
    private static final Map<String, String> LEGACY_TO_112 = ImmutableMap.<String, String>builder()
            // Beaches
            .put("minecraft:beaches", "minecraft:beach")
            // Snow / ice
            .put("minecraft:ice_flats", "minecraft:ice_plains")
            .put("minecraft:ice_spikes", "minecraft:mutated_ice_flats")
            .put("minecraft:snowy_plains", "minecraft:ice_plains")
            .put("minecraft:frozen_peaks", "minecraft:ice_mountains")
            .put("minecraft:snowy_slopes", "minecraft:ice_mountains")
            // Taiga naming (1.12 uses cold_taiga, not taiga_cold)
            .put("minecraft:taiga_cold", "minecraft:cold_taiga")
            .put("minecraft:taiga_cold_hills", "minecraft:cold_taiga_hills")
            .put("minecraft:old_growth_pine_taiga", "minecraft:redwood_taiga")
            .put("minecraft:old_growth_spruce_taiga", "minecraft:redwood_taiga")
            // Hills / mountains
            .put("minecraft:smaller_extreme_hills", "minecraft:extreme_hills_edge")
            .put("minecraft:windswept_hills", "minecraft:extreme_hills")
            .put("minecraft:windswept_forest", "minecraft:extreme_hills_with_trees")
            .put("minecraft:windswept_gravelly_hills", "minecraft:mutated_extreme_hills")
            // Savanna (1.12: savanna_plateau + mutated_savanna_rock; no savanna_rock)
            .put("minecraft:savanna_rock", "minecraft:savanna_plateau")
            .put("minecraft:windswept_savanna", "minecraft:mutated_savanna")
            // Swamp / jungle / forest (1.16 names)
            .put("minecraft:swamp", "minecraft:swampland")
            .put("minecraft:mangrove_swamp", "minecraft:swampland")
            .put("minecraft:dark_forest", "minecraft:roofed_forest")
            .put("minecraft:flower_forest", "minecraft:mutated_forest")
            .put("minecraft:sunflower_plains", "minecraft:mutated_plains")
            .put("minecraft:bamboo_jungle", "minecraft:jungle")
            .put("minecraft:bamboo_jungle_hills", "minecraft:jungle_hills")
            .put("minecraft:sparse_jungle", "minecraft:jungle_edge")
            .put("minecraft:old_growth_birch_forest", "minecraft:mutated_birch_forest")
            // Oceans (1.16 → 1.12)
            .put("minecraft:lukewarm_ocean", "minecraft:ocean")
            .put("minecraft:warm_ocean", "minecraft:ocean")
            .put("minecraft:deep_lukewarm_ocean", "minecraft:deep_ocean")
            .put("minecraft:deep_warm_ocean", "minecraft:deep_ocean")
            .put("minecraft:cold_ocean", "minecraft:frozen_ocean")
            .put("minecraft:deep_cold_ocean", "minecraft:deep_ocean")
            .put("minecraft:deep_frozen_ocean", "minecraft:deep_ocean")
            // Nether / End (1.16+)
            .put("minecraft:the_end", "minecraft:sky")
            .put("minecraft:end_barrens", "minecraft:sky")
            .put("minecraft:end_highlands", "minecraft:sky")
            .put("minecraft:small_end_islands", "minecraft:sky")
            .put("minecraft:crimson_forest", "minecraft:hell")
            .put("minecraft:warped_forest", "minecraft:hell")
            .put("minecraft:soul_sand_valley", "minecraft:hell")
            .put("minecraft:basalt_deltas", "minecraft:hell")
            .put("minecraft:nether_wastes", "minecraft:hell")
            .put("minecraft:mushroom_fields", "minecraft:mushroom_island")
            .put("minecraft:modified_jungle_edge", "minecraft:jungle_edge")
            .put("minecraft:jungle_egde", "minecraft:jungle_edge")
            .build();

    /** Every vanilla biome id registered at runtime (populated on first use). */
    private static Set<String> vanillaRegistryIds;

    private AMBiomes112() {
    }

    public static Set<String> vanillaIds() {
        ensureVanillaCache();
        return vanillaRegistryIds;
    }

    public static boolean isRegistered(String biomeId) {
        if (biomeId == null || biomeId.isEmpty()) {
            return false;
        }
        ResourceLocation loc = new ResourceLocation(biomeId);
        return ForgeRegistries.BIOMES.containsKey(loc);
    }

    /**
     * Resolves a config id to the 1.12.2 registry id if registered; otherwise null.
     */
    public static String resolve(String biomeId) {
        if (biomeId == null || biomeId.isEmpty()) {
            return null;
        }
        String key = biomeId.toLowerCase(Locale.ROOT);
        String mapped = LEGACY_TO_112.get(key);
        if (mapped != null) {
            key = mapped;
        }
        if (isRegistered(key)) {
            return key;
        }
        return null;
    }

    /**
     * Normalizes a biome list to registered 1.12.2 ids (deduped, sorted).
     */
    public static List<String> normalizeList(List<String> biomeIds) {
        if (biomeIds == null || biomeIds.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String raw : biomeIds) {
            String resolved = resolve(raw);
            if (resolved != null) {
                out.add(resolved);
            }
        }
        List<String> result = new ArrayList<>(out);
        Collections.sort(result);
        return result;
    }

    /**
     * True when a saved JSON list is mostly legacy/wrong ids and should be replaced with matcher defaults.
     */
    public static boolean shouldReplaceWithDefaults(List<String> rawFromFile, List<String> normalized, List<String> matcherDefaults) {
        if (rawFromFile == null || rawFromFile.isEmpty()) {
            return true;
        }
        if (normalized.isEmpty()) {
            return true;
        }
        int rawSize = rawFromFile.size();
        int normSize = normalized.size();
        if (normSize < rawSize / 2) {
            return true;
        }
        if (matcherDefaults != null && !matcherDefaults.isEmpty() && normSize < matcherDefaults.size() / 3) {
            return true;
        }
        return false;
    }

    /**
     * Acceptable registry ids for Citadel {@link com.github.alexthe666.citadel.config.biome.SpawnBiomeData} matching.
     */
    public static Set<String> acceptableIdsForConfigValue(String configValue) {
        Set<String> acceptable = new LinkedHashSet<>();
        String resolved = resolve(configValue);
        if (resolved != null) {
            acceptable.add(resolved);
        }
        acceptable.add(configValue);
        String legacy = LEGACY_TO_112.get(configValue.toLowerCase(Locale.ROOT));
        if (legacy != null) {
            acceptable.add(legacy);
        }
        return acceptable;
    }

    private static void ensureVanillaCache() {
        if (vanillaRegistryIds != null) {
            return;
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            ResourceLocation rl = biome.getRegistryName();
            if (rl != null && "minecraft".equals(rl.getResourceDomain())) {
                ids.add(rl.toString());
            }
        }
        vanillaRegistryIds = Collections.unmodifiableSet(ids);
    }
}
