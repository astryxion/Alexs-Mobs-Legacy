package com.github.alexthe666.alexsmobs.config;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

/**
 * 1.12.2-native spawn biome rules derived from the 1.16.5 {@code DefaultBiomes} pools.
 * Uses vanilla / Forge {@link BiomeDictionary} types and explicit 1.12.2 registry ids only.
 */
public final class AMNativeSpawnBiomes {

    private AMNativeSpawnBiomes() {
    }

    @FunctionalInterface
    public interface Matcher {
        boolean test(Biome biome);
    }

    private static Matcher overworld() {
        return b -> !BiomeDictionary.hasType(b, BiomeDictionary.Type.NETHER)
                && !BiomeDictionary.hasType(b, BiomeDictionary.Type.END);
    }

    private static Matcher nether() {
        return b -> b == Biomes.HELL || BiomeDictionary.hasType(b, BiomeDictionary.Type.NETHER);
    }

    private static Matcher end() {
        return b -> BiomeDictionary.hasType(b, BiomeDictionary.Type.END);
    }

    private static Matcher dict(BiomeDictionary.Type type) {
        return b -> BiomeDictionary.hasType(b, type);
    }

    private static Matcher not(Matcher inner) {
        return b -> !inner.test(b);
    }

    private static Matcher all(Matcher... parts) {
        return b -> {
            for (Matcher part : parts) {
                if (!part.test(b)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Matcher any(Matcher... parts) {
        return b -> {
            for (Matcher part : parts) {
                if (part.test(b)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Matcher is(Biome... biomes) {
        return b -> {
            for (Biome candidate : biomes) {
                if (b == candidate) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Matcher id(String... registryPaths) {
        return b -> {
            ResourceLocation rl = b.getRegistryName();
            if (rl == null) {
                return false;
            }
            String full = rl.toString();
            for (String path : registryPaths) {
                if (full.equals(path)) {
                    return true;
                }
            }
            return false;
        };
    }

    /** 1.16 {@code Biome.Category.FOREST} — temperate forest biomes, not jungle or taiga. */
    private static Matcher forestCategory() {
        return any(
                is(Biomes.FOREST, Biomes.FOREST_HILLS, Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS,
                        Biomes.MUTATED_FOREST, Biomes.MUTATED_BIRCH_FOREST, Biomes.MUTATED_BIRCH_FOREST_HILLS,
                        Biomes.ROOFED_FOREST, Biomes.MUTATED_ROOFED_FOREST)
        );
    }

    /** 1.16 {@code Biome.Category.DESERT}. */
    private static Matcher desertCategory() {
        return any(is(Biomes.DESERT, Biomes.DESERT_HILLS, Biomes.MUTATED_DESERT));
    }

    /** 1.16 {@code Biome.Category.SAVANNA}. */
    private static Matcher savannaCategory() {
        return any(is(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.MUTATED_SAVANNA, Biomes.MUTATED_SAVANNA_ROCK));
    }

    /** Shallow overworld oceans (1.16 lukewarm/warm/cold/frozen ocean → 1.12 ocean + frozen_ocean). */
    private static Matcher shallowOcean() {
        return is(Biomes.OCEAN, Biomes.FROZEN_OCEAN);
    }

    /** All deep variants from 1.16.5 map to {@code minecraft:deep_ocean} in 1.12.2. */
    private static Matcher deepOcean() {
        return is(Biomes.DEEP_OCEAN);
    }

    /** 1.16 warm/lukewarm ocean biomes — 1.12 has only {@code ocean} (no HOT dict type on oceans). */
    private static Matcher warmOcean() {
        return is(Biomes.OCEAN);
    }

    /** 1.16 cold/frozen ocean biomes — {@code frozen_ocean} plus {@code deep_ocean} as deep-cold stand-in. */
    private static Matcher coldOcean() {
        return is(Biomes.FROZEN_OCEAN, Biomes.DEEP_OCEAN);
    }

  /** Main End island (1.16 {@code minecraft:the_end}). */
    private static Matcher mainEndIsland() {
        return is(Biomes.SKY);
    }

    public static boolean grizzlyBear(Biome biome) {
        return any(
                all(overworld(), forestCategory()),
                all(overworld(), dict(BiomeDictionary.Type.FOREST), not(is(Biomes.JUNGLE_EDGE, Biomes.MUTATED_JUNGLE)))
        ).test(biome);
    }

    public static boolean roadrunner(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.MESA)),
                all(overworld(), desertCategory())
        ).test(biome);
    }

    public static boolean boneSerpent(Biome biome) {
        return nether().test(biome);
    }

    public static boolean gazelle(Biome biome) {
        return all(overworld(), savannaCategory()).test(biome);
    }

    public static boolean crocodile(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.SWAMP)),
                all(overworld(), dict(BiomeDictionary.Type.RIVER), not(dict(BiomeDictionary.Type.COLD)))
        ).test(biome);
    }

    public static boolean fly(Biome biome) {
        return all(overworld(), not(dict(BiomeDictionary.Type.OCEAN))).test(biome);
    }

    public static boolean hummingbird(Biome biome) {
        return any(
                is(Biomes.MUTATED_FOREST),
                is(Biomes.MUTATED_PLAINS),
                all(overworld(), dict(BiomeDictionary.Type.JUNGLE))
        ).test(biome);
    }

    public static boolean orca(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.OCEAN), dict(BiomeDictionary.Type.COLD)).test(biome);
    }

    public static boolean sunbird(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.MOUNTAIN)).test(biome);
    }

    public static boolean gorilla(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.JUNGLE)).test(biome);
    }

    public static boolean anteater(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.JUNGLE)).test(biome);
    }

    public static boolean crimsonMosquito(Biome biome) {
        return nether().test(biome);
    }

    public static boolean rattlesnake(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.MESA)),
                all(overworld(), desertCategory())
        ).test(biome);
    }

    public static boolean endergrade(Biome biome) {
        return all(end(), not(mainEndIsland())).test(biome);
    }

    public static boolean hammerheadShark(Biome biome) {
        return warmOcean().test(biome);
    }

    public static boolean lobster(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.BEACH)).test(biome);
    }

    public static boolean komodoDragon(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.MESA)).test(biome);
    }

    public static boolean capuchinMonkey(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.JUNGLE)).test(biome);
    }

    public static boolean caveCentipede(Biome biome) {
        return all(overworld(), not(dict(BiomeDictionary.Type.OCEAN)), not(dict(BiomeDictionary.Type.MUSHROOM))).test(biome);
    }

    public static boolean warpedToad(Biome biome) {
        return nether().test(biome);
    }

    public static boolean moose(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.SNOWY), dict(BiomeDictionary.Type.WASTELAND)),
                all(overworld(), dict(BiomeDictionary.Type.SNOWY), dict(BiomeDictionary.Type.FOREST)),
                all(overworld(), dict(BiomeDictionary.Type.SNOWY), dict(BiomeDictionary.Type.CONIFEROUS))
        ).test(biome);
    }

    public static boolean mimicube(Biome biome) {
        return all(end(), not(mainEndIsland())).test(biome);
    }

    public static boolean raccoon(Biome biome) {
        return any(
                all(overworld(), not(dict(BiomeDictionary.Type.SAVANNA)), dict(BiomeDictionary.Type.FOREST)),
                all(overworld(), not(dict(BiomeDictionary.Type.SAVANNA)), dict(BiomeDictionary.Type.PLAINS))
        ).test(biome);
    }

    public static boolean blobfish(Biome biome) {
        return deepOcean().test(biome);
    }

    public static boolean frilledShark(Biome biome) {
        return deepOcean().test(biome);
    }

  /** 1.16 frozen-ocean coastal spawns → cold beach + frozen ocean in 1.12.2. */
    public static boolean seal(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.BEACH)),
                is(Biomes.COLD_BEACH, Biomes.FROZEN_OCEAN),
                all(overworld(), dict(BiomeDictionary.Type.OCEAN), dict(BiomeDictionary.Type.SNOWY))
        ).test(biome);
    }

    public static boolean cockroach(Biome biome) {
        return all(overworld(), not(dict(BiomeDictionary.Type.OCEAN)), not(dict(BiomeDictionary.Type.MUSHROOM))).test(biome);
    }

    public static boolean shoebill(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.SWAMP)).test(biome);
    }

    public static boolean elephant(Biome biome) {
        return all(overworld(), savannaCategory()).test(biome);
    }

    /** Overworld plains, not savanna, not hot; cold plains; snowy plains ({@link Biomes#ICE_PLAINS}). */
    public static boolean bison(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.PLAINS), not(dict(BiomeDictionary.Type.SAVANNA)), not(dict(BiomeDictionary.Type.HOT))),
                all(overworld(), dict(BiomeDictionary.Type.PLAINS), dict(BiomeDictionary.Type.COLD)),
                all(overworld(), dict(BiomeDictionary.Type.SNOWY), dict(BiomeDictionary.Type.WASTELAND))
        ).test(biome);
    }

    /** 1.16 {@code DefaultBiomes.TUSKLIN} — {@link Biomes#ICE_PLAINS} and {@link Biomes#MUTATED_ICE_FLATS} (ice spikes). */
    public static boolean tusklin(Biome biome) {
        return any(
                is(Biomes.ICE_PLAINS, Biomes.MUTATED_ICE_FLATS),
                all(overworld(), dict(BiomeDictionary.Type.SNOWY), dict(BiomeDictionary.Type.WASTELAND)),
                id("terralith:snowy_badlands"),
                id("terralith:gravel_desert"),
                id("biomesoplenty:snowblossom_grove")
        ).test(biome);
    }

    public static boolean rhinoceros(Biome biome) {
        return all(overworld(), savannaCategory()).test(biome);
    }

    /** 1.16 {@code DefaultBiomes.SKUNK}: forest overworld, not savanna/cold/sparse jungle. */
    public static boolean skunk(Biome biome) {
        return all(
                overworld(),
                any(forestCategory(), dict(BiomeDictionary.Type.FOREST)),
                not(savannaCategory()),
                not(dict(BiomeDictionary.Type.COLD)),
                not(is(Biomes.MUTATED_JUNGLE_EDGE))
        ).test(biome);
    }

    public static boolean soulVulture(Biome biome) {
        return nether().test(biome);
    }

    public static boolean snowLeopard(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.MOUNTAIN), dict(BiomeDictionary.Type.SNOWY)).test(biome);
    }

    public static boolean spectre(Biome biome) {
        return all(end(), not(mainEndIsland())).test(biome);
    }

    public static boolean crow(Biome biome) {
        return raccoon(biome);
    }

    /** 1.20 {@code DefaultBiomes.ALL_FOREST} temperate forest spawns. */
    public static boolean blueJay(Biome biome) {
        return all(overworld(), forestCategory()).test(biome);
    }

    public static boolean alligatorSnappingTurtle(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.SWAMP)).test(biome);
    }

    public static boolean mungus(Biome biome) {
        return all(overworld(), any(dict(BiomeDictionary.Type.MUSHROOM), dict(BiomeDictionary.Type.RARE))).test(biome);
    }

    public static boolean mantisShrimp(Biome biome) {
        return warmOcean().test(biome);
    }

    public static boolean guster(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.HOT), dict(BiomeDictionary.Type.DRY), dict(BiomeDictionary.Type.SANDY)).test(biome);
    }

    public static boolean warpedMosco(Biome biome) {
        return false;
    }

    public static boolean straddler(Biome biome) {
        return nether().test(biome);
    }

    public static boolean stradpole(Biome biome) {
        return straddler(biome);
    }

    public static boolean savannaAndMesa(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.MESA)),
                all(overworld(), dict(BiomeDictionary.Type.SAVANNA))
        ).test(biome);
    }

    public static boolean platypus(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.RIVER), not(dict(BiomeDictionary.Type.COLD))).test(biome);
    }

    public static boolean dropbear(Biome biome) {
        return nether().test(biome);
    }

    public static boolean tasmanianDevil(Biome biome) {
        return all(
                overworld(),
                not(dict(BiomeDictionary.Type.SAVANNA)),
                dict(BiomeDictionary.Type.FOREST),
                not(dict(BiomeDictionary.Type.COLD)),
                not(is(Biomes.JUNGLE_EDGE, Biomes.MUTATED_JUNGLE))
        ).test(biome);
    }

    public static boolean cachalotWhale(Biome biome) {
        return any(
                all(overworld(), dict(BiomeDictionary.Type.OCEAN), dict(BiomeDictionary.Type.COLD)),
                shallowOcean(),
                deepOcean()
        ).test(biome);
    }

    public static boolean beachedCachalotWhale(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.BEACH)).test(biome);
    }

    public static boolean leafcutterAnthill(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.JUNGLE)).test(biome);
    }

    public static boolean enderiophage(Biome biome) {
        return all(end(), not(mainEndIsland())).test(biome);
    }

    public static boolean baldEagle(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.MOUNTAIN), dict(BiomeDictionary.Type.FOREST)).test(biome);
    }

    public static boolean tiger(Biome biome) {
        // 1.16 bamboo_jungle only — 1.12.2 has no bamboo biomes; use all jungle variants.
        return all(overworld(), dict(BiomeDictionary.Type.JUNGLE)).test(biome);
    }

    public static boolean tarantulaHawk(Biome biome) {
        return all(overworld(), desertCategory()).test(biome);
    }

    public static boolean voidWorm(Biome biome) {
        return false;
    }

    public static boolean mimicOctopus(Biome biome) {
        return warmOcean().test(biome);
    }

    public static boolean seagull(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.BEACH)).test(biome);
    }

    public static boolean bananaSlug(Biome biome) {
        return any(
                is(Biomes.TAIGA, Biomes.TAIGA_HILLS, Biomes.COLD_TAIGA, Biomes.COLD_TAIGA_HILLS,
                        Biomes.MUTATED_TAIGA, Biomes.MUTATED_TAIGA_COLD),
                all(overworld(), dict(BiomeDictionary.Type.CONIFEROUS), dict(BiomeDictionary.Type.FOREST)),
                all(overworld(), is(Biomes.ROOFED_FOREST, Biomes.MUTATED_ROOFED_FOREST)),
                id("biomesoplenty:redwood_forest", "biomesoplenty:coniferous_forest", "biomesoplenty:fir_clearing")
        ).test(biome);
    }

    public static boolean anaconda(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.SWAMP)).test(biome);
    }

    public static boolean laviathan(Biome biome) {
        return nether().test(biome);
    }

    public static boolean giantSquid(Biome biome) {
        return deepOcean().test(biome);
    }

    public static boolean jerboa(Biome biome) {
        return desertCategory().test(biome);
    }

    public static boolean triops(Biome biome) {
        return desertCategory().test(biome);
    }

    public static boolean rainFrog(Biome biome) {
        return desertCategory().test(biome);
    }

    public static boolean toucan(Biome biome) {
        return tiger(biome);
    }

    public static boolean potoo(Biome biome) {
        return is(Biomes.ROOFED_FOREST, Biomes.MUTATED_ROOFED_FOREST).test(biome);
    }

    public static boolean sugarGlider(Biome biome) {
        return all(
                overworld(),
                any(
                        is(Biomes.BIRCH_FOREST, Biomes.BIRCH_FOREST_HILLS, Biomes.MUTATED_BIRCH_FOREST),
                        all(dict(BiomeDictionary.Type.FOREST), not(dict(BiomeDictionary.Type.JUNGLE)), not(dict(BiomeDictionary.Type.SAVANNA)))
                )
        ).test(biome);
    }

    public static boolean flyingFish(Biome biome) {
        return all(
                overworld(),
                dict(BiomeDictionary.Type.OCEAN),
                not(dict(BiomeDictionary.Type.COLD)),
                not(is(Biomes.DEEP_OCEAN))
        ).test(biome);
    }

    public static boolean combJelly(Biome biome) {
        return all(overworld(), is(Biomes.FROZEN_OCEAN)).test(biome);
    }

    public static boolean cosmicCod(Biome biome) {
        return end().test(biome);
    }

    public static boolean devilsHolePupfish(Biome biome) {
        return platypus(biome);
    }

    public static boolean catfish(Biome biome) {
        return crocodile(biome) || platypus(biome);
    }

    public static boolean terrapin(Biome biome) {
        return crocodile(biome) || platypus(biome);
    }

    public static boolean geladaMonkey(Biome biome) {
        return all(
                overworld(),
                any(
                        all(dict(BiomeDictionary.Type.MOUNTAIN), dict(BiomeDictionary.Type.PLAINS)),
                        is(Biomes.EXTREME_HILLS, Biomes.EXTREME_HILLS_WITH_TREES,
                                Biomes.MUTATED_EXTREME_HILLS, Biomes.MUTATED_EXTREME_HILLS_WITH_TREES)
                )
        ).test(biome);
    }

    public static boolean manedWolf(Biome biome) {
        return all(overworld(), dict(BiomeDictionary.Type.SAVANNA)).test(biome);
    }

    public static boolean caiman(Biome biome) {
        return crocodile(biome);
    }
}
