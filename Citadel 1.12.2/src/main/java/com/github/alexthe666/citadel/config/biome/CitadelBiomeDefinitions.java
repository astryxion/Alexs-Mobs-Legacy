package com.github.alexthe666.citadel.config.biome;

public class CitadelBiomeDefinitions {
   public static final SpawnBiomeData TEST;

   static {
      TEST = (new SpawnBiomeData()).addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0).addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "water", 0).addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:nether_wastes", 1);
   }
}
