package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.citadel.config.biome.SpawnBiomeData;
import net.minecraft.world.biome.Biome;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Resolves spawn biomes via {@link BiomeConfig#test} (1.16 parity) or {@link AMNativeSpawnBiomes} fallbacks.
 */
public final class AMSpawnBiomeFilters {

    private AMSpawnBiomeFilters() {
    }

    public static AMSpawnData.BiomeFilter config(Pair<String, SpawnBiomeData> entry) {
        return biome -> BiomeConfig.test(entry, biome);
    }

    public static AMSpawnData.BiomeFilter nativeMatcher(AMNativeSpawnBiomes.Matcher matcher) {
        return matcher::test;
    }
}
