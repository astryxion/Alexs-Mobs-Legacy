package com.github.alexthe666.alexsmobs.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 1.12.2 biome id mapping for spawn configs that still contain 1.16+ registry names.
 * One-to-one renames apply on match and negate. Stand-ins (missing 1.16 biomes) apply only
 * on positive registry entries so a bamboo-jungle negate cannot wipe all jungle.
 */
public final class AMBiomes112 {

    /** Same biome, renamed. Safe on negate (flower forest is mutated_forest, not every forest). */
    static final Map<String, String> RENAMES = ImmutableMap.<String, String>builder()
            .put("minecraft:beaches", "minecraft:beach")
            .put("minecraft:ice_flats", "minecraft:ice_plains")
            .put("minecraft:snowy_plains", "minecraft:ice_plains")
            .put("minecraft:frozen_peaks", "minecraft:ice_mountains")
            .put("minecraft:snowy_slopes", "minecraft:ice_mountains")
            .put("minecraft:taiga_cold", "minecraft:cold_taiga")
            .put("minecraft:taiga_cold_hills", "minecraft:cold_taiga_hills")
            .put("minecraft:old_growth_pine_taiga", "minecraft:redwood_taiga")
            .put("minecraft:old_growth_spruce_taiga", "minecraft:redwood_taiga")
            .put("minecraft:smaller_extreme_hills", "minecraft:extreme_hills_edge")
            .put("minecraft:windswept_hills", "minecraft:extreme_hills")
            .put("minecraft:windswept_forest", "minecraft:extreme_hills_with_trees")
            .put("minecraft:windswept_gravelly_hills", "minecraft:mutated_extreme_hills")
            .put("minecraft:savanna_plateau", "minecraft:savanna_rock")
            .put("minecraft:windswept_savanna", "minecraft:mutated_savanna")
            .put("minecraft:swamp", "minecraft:swampland")
            .put("minecraft:dark_forest", "minecraft:roofed_forest")
            .put("minecraft:flower_forest", "minecraft:mutated_forest")
            .put("minecraft:sunflower_plains", "minecraft:mutated_plains")
            .put("minecraft:sparse_jungle", "minecraft:jungle_edge")
            .put("minecraft:old_growth_birch_forest", "minecraft:mutated_birch_forest")
            .put("minecraft:ice_spikes", "minecraft:mutated_ice_flats")
            .put("minecraft:mushroom_fields", "minecraft:mushroom_island")
            .put("minecraft:modified_jungle_edge", "minecraft:jungle_edge")
            .put("minecraft:jungle_egde", "minecraft:jungle_edge")
            .build();

    /** Missing 1.16 biomes → closest 1.12 biome(s). Positive matches only. */
    static final Map<String, Set<String>> STAND_INS = ImmutableMap.<String, Set<String>>builder()
            .put("minecraft:warm_ocean", ImmutableSet.of("minecraft:ocean"))
            .put("minecraft:lukewarm_ocean", ImmutableSet.of("minecraft:ocean"))
            .put("minecraft:cold_ocean", ImmutableSet.of("minecraft:frozen_ocean"))
            .put("minecraft:deep_lukewarm_ocean", ImmutableSet.of("minecraft:deep_ocean"))
            .put("minecraft:deep_warm_ocean", ImmutableSet.of("minecraft:deep_ocean"))
            .put("minecraft:deep_cold_ocean", ImmutableSet.of("minecraft:deep_ocean"))
            .put("minecraft:deep_frozen_ocean", ImmutableSet.of("minecraft:deep_ocean"))
            .put("minecraft:bamboo_jungle", ImmutableSet.of("minecraft:jungle", "minecraft:jungle_hills"))
            .put("minecraft:bamboo_jungle_hills", ImmutableSet.of("minecraft:jungle_hills"))
            .put("minecraft:crimson_forest", ImmutableSet.of("minecraft:hell"))
            .put("minecraft:warped_forest", ImmutableSet.of("minecraft:hell"))
            .put("minecraft:soul_sand_valley", ImmutableSet.of("minecraft:hell"))
            .put("minecraft:basalt_deltas", ImmutableSet.of("minecraft:hell"))
            .put("minecraft:nether_wastes", ImmutableSet.of("minecraft:hell"))
            .put("minecraft:the_end", ImmutableSet.of("minecraft:sky"))
            .put("minecraft:end_barrens", ImmutableSet.of("minecraft:sky"))
            .put("minecraft:end_highlands", ImmutableSet.of("minecraft:sky"))
            .put("minecraft:small_end_islands", ImmutableSet.of("minecraft:sky"))
            .put("minecraft:mangrove_swamp", ImmutableSet.of("minecraft:swampland"))
            .put("minecraft:meadow", ImmutableSet.of("minecraft:extreme_hills", "minecraft:savanna_rock"))
            .build();

    private AMBiomes112() {
    }

    public static String rename(String biomeId) {
        if (biomeId == null || biomeId.isEmpty()) {
            return biomeId;
        }
        String key = biomeId.toLowerCase(Locale.ROOT);
        String mapped = RENAMES.get(key);
        return mapped != null ? mapped : key;
    }

    public static Set<String> standIns(String biomeId) {
        if (biomeId == null) {
            return Collections.emptySet();
        }
        Set<String> ids = STAND_INS.get(biomeId.toLowerCase(Locale.ROOT));
        return ids != null ? ids : Collections.emptySet();
    }

    public static boolean isRegistered(String biomeId) {
        if (biomeId == null || biomeId.isEmpty()) {
            return false;
        }
        return ForgeRegistries.BIOMES.containsKey(new ResourceLocation(biomeId));
    }
}
