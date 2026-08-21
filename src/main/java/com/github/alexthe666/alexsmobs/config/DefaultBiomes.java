package com.github.alexthe666.alexsmobs.config;

import com.github.alexthe666.citadel.config.biome.BiomeEntryType;
import com.github.alexthe666.citadel.config.biome.SpawnBiomeData;

/**
 * Spawn biome lists from Alex's Mobs 1.16.5, plus 1.20.1-only mobs, using 1.12.2 registry ids
 * and dictionary types. 1.16 names that do not exist in 1.12 are replaced with the vanilla
 * equivalent (warm ocean → ocean, bamboo jungle → jungle, crimson forest → hell, etc.).
 */
public class DefaultBiomes {

    public static final SpawnBiomeData EMPTY = new SpawnBiomeData();

    public static final SpawnBiomeData GRIZZLY_BEAR = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:jungle_edge", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:mutated_jungle", 1);

    public static final SpawnBiomeData ROADRUNNER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mesa", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "desert", 1);

    public static final SpawnBiomeData BONE_SERPENT = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "nether", 0);

    public static final SpawnBiomeData GAZELLE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "savanna", 1);

    public static final SpawnBiomeData CROCODILE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "river", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 2)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tropic_beach", 3);

    public static final SpawnBiomeData FLY = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, true, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "ocean", 0);

    public static final SpawnBiomeData HUMMINGBIRD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_forest", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_plains", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 2)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 2);

    public static final SpawnBiomeData ORCA = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "cold", 0);

    public static final SpawnBiomeData SUNBIRD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mountain", 0);

    /** 1.16 jungle minus bamboo jungle. 1.12 has no bamboo jungle, so this is all jungle. */
    public static final SpawnBiomeData GORILLA = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0);

    /** 1.16 crimson forest → 1.12 hell (single nether biome). */
    public static final SpawnBiomeData CRIMSON_MOSQUITO = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:hell", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "byg:crimson_gardens", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:visceral_heap", 2);

    public static final SpawnBiomeData RATTLESNAKE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mesa", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "desert", 1);

    /** 1.16 End minus the_end island. 1.12 only has sky; do not negate the_end or nothing spawns. */
    public static final SpawnBiomeData ENDERGRADE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "end", 0);

    /** 1.16 ocean + hot (warm ocean). 1.12 ocean is not HOT, so ocean alone is the stand-in. */
    public static final SpawnBiomeData HAMMERHEAD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0);

    public static final SpawnBiomeData LOBSTER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "beach", 0);

    public static final SpawnBiomeData KOMODO_DRAGON = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mesa", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tropic_plains", 1);

    public static final SpawnBiomeData CAPUCHIN_MONKEY = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0);

    public static final SpawnBiomeData CENTIPEDE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "mushroom", 0);

    public static final SpawnBiomeData WARPED_TOAD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:hell", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "byg:crimson_gardens", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "byg:warped_desert", 2);

    public static final SpawnBiomeData MOOSE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "snowy", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "wasteland", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "snowy", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 2);

    public static final SpawnBiomeData MIMICUBE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "end", 0);

    public static final SpawnBiomeData RACCOON = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "plains", 1);

    public static final SpawnBiomeData DEEP_SEA = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:deep_ocean", 0);

    public static final SpawnBiomeData SEAL = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "beach", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "snowy", 1);

    public static final SpawnBiomeData COCKROACH = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "mushroom", 0);

    public static final SpawnBiomeData SHOEBILL = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 1);

    public static final SpawnBiomeData ELEPHANT = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "savanna", 1);

    public static final SpawnBiomeData SOUL_VULTURE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:hell", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "byg:warped_desert", 1);

    public static final SpawnBiomeData SPECTRE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "end", 0);

    /**
     * 1.16 required mountain+snowy (ice mountains only on 1.12). 1.20 is all snowy overworld
     * plus peak biomes; 1.12 peaks are the extreme-hills family.
     */
    public static final SpawnBiomeData SNOW_LEOPARD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "snowy", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mountain", 1);

    public static final SpawnBiomeData CROW = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "plains", 1);

    public static final SpawnBiomeData ALLIGATOR_SNAPPING_TURTLE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 1);

    public static final SpawnBiomeData MUNGUS = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mushroom", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "rare", 0);

    public static final SpawnBiomeData MANTIS_SHRIMP = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0);

    public static final SpawnBiomeData GUSTER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "hot", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "dry", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "sandy", 0);

    public static final SpawnBiomeData STRADDLER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:hell", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:withered_abyss", 1);

    public static final SpawnBiomeData SAVANNA_AND_MESA = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mesa", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "savanna", 1);

    public static final SpawnBiomeData PLATYPUS = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "river", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 1);

    public static final SpawnBiomeData DROPBEAR = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:hell", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:crystalline_chasm", 1);

    public static final SpawnBiomeData TASMANIAN_DEVIL = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:jungle_edge", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:mutated_jungle", 0);

    public static final SpawnBiomeData CACHALOT_WHALE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:ocean", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:deep_ocean", 2);

    public static final SpawnBiomeData BEACHED_CACHALOT_WHALE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "beach", 0);

    public static final SpawnBiomeData LEAFCUTTER_ANTHILL = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0);

    public static final SpawnBiomeData ENDERIOPHAGE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "end", 0);

    public static final SpawnBiomeData BALD_EAGLE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "mountain", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0);

    /** 1.16 bamboo jungle only. 1.12 has no bamboo; Mo Creatures tigers use jungle. */
    public static final SpawnBiomeData TIGER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:bamboo_blossom_grove", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:cherry_blossom_grove", 2);

    public static final SpawnBiomeData TARANTULA_HAWK = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "desert", 0);

    public static final SpawnBiomeData MIMIC_OCTOPUS = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0);

    public static final SpawnBiomeData SEAGULL = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "beach", 0);

    public static final SpawnBiomeData ALL_NETHER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "nether", 0);

    public static final SpawnBiomeData ALL_OVERWORLD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0);

    public static final SpawnBiomeData ALL_FOREST = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:jungle_edge", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "coniferous", 1);

    public static final SpawnBiomeData DESERT = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_CATEGORY, false, "desert", 0);

    public static final SpawnBiomeData ICE_FREE_RIVER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "river", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:tundra_bog", 1);

    public static final SpawnBiomeData BANANA_SLUG = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:redwood_taiga", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:redwood_taiga_hills", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "coniferous", 2)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 2)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "dense", 2)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:redwood_forest", 3)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "biomesoplenty:coniferous_forest", 4);

    public static final SpawnBiomeData TUSKLIN = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_ice_flats", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "snowy", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "plains", 1);

    public static final SpawnBiomeData ANACONDA = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0);

    /**
     * 1.20 meadows (plains + plateau). 1.12 savanna plateau is too low/wrong climate;
     * extreme hills are the highland the player actually visits.
     */
    public static final SpawnBiomeData GELADA_MONKEY = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:extreme_hills", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:extreme_hills_with_trees", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:extreme_hills_edge", 2)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_extreme_hills", 3)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_extreme_hills_with_trees", 4)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:savanna_rock", 5)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_savanna_rock", 6);

    public static final SpawnBiomeData TOUCAN = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0);

    public static final SpawnBiomeData MANED_WOLF = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "savanna", 0);

    public static final SpawnBiomeData ANTEATER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "jungle", 0);

    public static final SpawnBiomeData COMB_JELLY = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:frozen_ocean", 0);

    public static final SpawnBiomeData COSMIC_COD = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "end", 0);

    public static final SpawnBiomeData BISON = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "plains", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "hot", 0);

    public static final SpawnBiomeData CATFISH = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "river", 1)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 1);

    public static final SpawnBiomeData FLYING_FISH = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "ocean", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:deep_ocean", 0);

    public static final SpawnBiomeData POTOO = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:roofed_forest", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_roofed_forest", 1);

    public static final SpawnBiomeData MANGROVE = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "swamp", 0);

    public static final SpawnBiomeData RHINOCEROS = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "savanna", 0);

    public static final SpawnBiomeData SUGAR_GLIDER = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:birch_forest", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:birch_forest_hills", 1)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_birch_forest", 2)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, false, "minecraft:mutated_birch_forest_hills", 3);

    public static final SpawnBiomeData SKUNK = new SpawnBiomeData()
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "overworld", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, false, "forest", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "savanna", 0)
            .addBiomeEntry(BiomeEntryType.BIOME_DICT, true, "cold", 0)
            .addBiomeEntry(BiomeEntryType.REGISTRY_NAME, true, "minecraft:jungle_edge", 0);
}
