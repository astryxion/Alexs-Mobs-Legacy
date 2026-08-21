package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.citadel.config.biome.SpawnBiomeData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mo Creatures-style natural spawn registration for 1.12.2:
 * collect biomes from Citadel spawn JSON, then {@link EntityRegistry#addSpawn}.
 * Weights and group sizes match Alex's Mobs 1.16.5 / 1.20.1 {@code AMWorldRegistry}.
 * 1.16 {@code WATER_AMBIENT} is {@link EnumCreatureType#WATER_CREATURE} here (no ambient-water type in 1.12).
 */
public final class AMSpawnRegistry {

    private AMSpawnRegistry() {
    }

    public static void register() {
        purgeAlexsMobsSpawns();
        registerCreature(AMEntityRegistry.GRIZZLY_BEAR, AMConfig.grizzlyBearSpawnWeight, 2, 3, BiomeConfig.grizzlyBear);
        registerCreature(AMEntityRegistry.ROADRUNNER, AMConfig.roadrunnerSpawnWeight, 2, 2, BiomeConfig.roadrunner);
        registerMonster(AMEntityRegistry.BONE_SERPENT, AMConfig.boneSerpentSpawnWeight, 1, 1, BiomeConfig.boneSerpent);
        registerCreature(AMEntityRegistry.GAZELLE, AMConfig.gazelleSpawnWeight, 7, 7, BiomeConfig.gazelle);
        registerCreature(AMEntityRegistry.CROCODILE, AMConfig.crocodileSpawnWeight, 1, 2, BiomeConfig.crocodile);
        registerAmbient(AMEntityRegistry.FLY, AMConfig.flySpawnWeight, 2, 3, BiomeConfig.fly);
        registerCreature(AMEntityRegistry.HUMMINGBIRD, AMConfig.hummingbirdSpawnWeight, 7, 7, BiomeConfig.hummingbird);
        registerWater(AMEntityRegistry.ORCA, AMConfig.orcaSpawnWeight, 3, 4, BiomeConfig.orca);
        registerCreature(AMEntityRegistry.SUNBIRD, AMConfig.sunbirdSpawnWeight, 1, 1, BiomeConfig.sunbird);
        registerCreature(AMEntityRegistry.GORILLA, AMConfig.gorillaSpawnWeight, 7, 7, BiomeConfig.gorilla);
        registerMonster(AMEntityRegistry.CRIMSON_MOSQUITO, AMConfig.crimsonMosquitoSpawnWeight, 4, 4, BiomeConfig.crimsonMosquito);
        registerCreature(AMEntityRegistry.RATTLESNAKE, AMConfig.rattlesnakeSpawnWeight, 1, 2, BiomeConfig.rattlesnake);
        registerCreature(AMEntityRegistry.ENDERGRADE, AMConfig.endergradeSpawnWeight, 2, 6, BiomeConfig.endergrade);
        registerWater(AMEntityRegistry.HAMMERHEAD_SHARK, AMConfig.hammerheadSharkSpawnWeight, 2, 3, BiomeConfig.hammerheadShark);
        registerWater(AMEntityRegistry.LOBSTER, AMConfig.lobsterSpawnWeight, 3, 5, BiomeConfig.lobster);
        registerCreature(AMEntityRegistry.KOMODO_DRAGON, AMConfig.komodoDragonSpawnWeight, 1, 2, BiomeConfig.komodoDragon);
        registerCreature(AMEntityRegistry.CAPUCHIN_MONKEY, AMConfig.capuchinMonkeySpawnWeight, 9, 16, BiomeConfig.capuchinMonkey);
        registerMonster(AMEntityRegistry.CENTIPEDE_HEAD, AMConfig.caveCentipedeSpawnWeight, 1, 1, BiomeConfig.caveCentipede);
        registerCreature(AMEntityRegistry.WARPED_TOAD, AMConfig.warpedToadSpawnWeight, 5, 5, BiomeConfig.warpedToad);
        registerCreature(AMEntityRegistry.MOOSE, AMConfig.mooseSpawnWeight, 3, 4, BiomeConfig.moose);
        registerMonster(AMEntityRegistry.MIMICUBE, AMConfig.mimicubeSpawnWeight, 1, 3, BiomeConfig.mimicube);
        registerCreature(AMEntityRegistry.RACCOON, AMConfig.raccoonSpawnWeight, 2, 4, BiomeConfig.raccoon);
        registerWater(AMEntityRegistry.BLOBFISH, AMConfig.blobfishSpawnWeight, 2, 2, BiomeConfig.blobfish);
        registerCreature(AMEntityRegistry.SEAL, AMConfig.sealSpawnWeight, 3, 8, BiomeConfig.seal);
        registerAmbient(AMEntityRegistry.COCKROACH, AMConfig.cockroachSpawnWeight, 5, 5, BiomeConfig.cockroach);
        registerCreature(AMEntityRegistry.SHOEBILL, AMConfig.shoebillSpawnWeight, 1, 2, BiomeConfig.shoebill);
        registerCreature(AMEntityRegistry.ELEPHANT, AMConfig.elephantSpawnWeight, 3, 5, BiomeConfig.elephant);
        if (!AMConfig.soulVultureSpawnOnFossil) {
            registerMonster(AMEntityRegistry.SOUL_VULTURE, AMConfig.soulVultureSpawnWeight, 2, 3, BiomeConfig.soulVulture);
        }
        registerCreature(AMEntityRegistry.SNOW_LEOPARD, AMConfig.snowLeopardSpawnWeight, 1, 2, BiomeConfig.snowLeopard);
        registerCreature(AMEntityRegistry.SPECTRE, AMConfig.spectreSpawnWeight, 1, 2, BiomeConfig.spectre);
        registerCreature(AMEntityRegistry.CROW, AMConfig.crowSpawnWeight, 3, 5, BiomeConfig.crow);
        registerCreature(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE, AMConfig.alligatorSnappingTurtleSpawnWeight, 1, 2, BiomeConfig.alligatorSnappingTurtle);
        registerCreature(AMEntityRegistry.MUNGUS, AMConfig.mungusSpawnWeight, 3, 5, BiomeConfig.mungus);
        registerWater(AMEntityRegistry.MANTIS_SHRIMP, AMConfig.mantisShrimpSpawnWeight, 1, 4, BiomeConfig.mantisShrimp);
        registerMonster(AMEntityRegistry.GUSTER, AMConfig.gusterSpawnWeight, 1, 2, BiomeConfig.guster);
        registerMonster(AMEntityRegistry.WARPED_MOSCO, AMConfig.warpedMoscoSpawnWeight, 1, 1, BiomeConfig.warpedMosco);
        registerMonster(AMEntityRegistry.STRADDLER, AMConfig.straddlerSpawnWeight, 1, 3, BiomeConfig.straddler);
        registerWater(AMEntityRegistry.STRADPOLE, AMConfig.stradpoleSpawnWeight, 1, 1, BiomeConfig.stradpole);
        registerCreature(AMEntityRegistry.EMU, AMConfig.emuSpawnWeight, 2, 5, BiomeConfig.emu);
        registerCreature(AMEntityRegistry.PLATYPUS, AMConfig.platypusSpawnWeight, 1, 2, BiomeConfig.platypus);
        registerMonster(AMEntityRegistry.DROPBEAR, AMConfig.dropbearSpawnWeight, 1, 1, BiomeConfig.dropbear);
        registerCreature(AMEntityRegistry.TASMANIAN_DEVIL, AMConfig.tasmanianDevilSpawnWeight, 1, 2, BiomeConfig.tasmanianDevil);
        registerCreature(AMEntityRegistry.KANGAROO, AMConfig.kangarooSpawnWeight, 3, 5, BiomeConfig.kangaroo);
        registerWater(AMEntityRegistry.CACHALOT_WHALE, AMConfig.cachalotWhaleSpawnWeight, 1, 2, BiomeConfig.cachalot_whale_spawns);
        registerCreature(AMEntityRegistry.ENDERIOPHAGE, AMConfig.enderiophageSpawnWeight, 2, 2, BiomeConfig.enderiophage_spawns);
        registerCreature(AMEntityRegistry.BALD_EAGLE, AMConfig.baldEagleSpawnWeight, 2, 4, BiomeConfig.baldEagle);
        registerCreature(AMEntityRegistry.TIGER, AMConfig.tigerSpawnWeight, 1, 3, BiomeConfig.tiger);
        registerCreature(AMEntityRegistry.TARANTULA_HAWK, AMConfig.tarantulaHawkSpawnWeight, 1, 1, BiomeConfig.tarantula_hawk);
        registerMonster(AMEntityRegistry.VOID_WORM, AMConfig.voidWormSpawnWeight, 1, 1, BiomeConfig.void_worm);
        registerWater(AMEntityRegistry.FRILLED_SHARK, AMConfig.frilledSharkSpawnWeight, 1, 1, BiomeConfig.frilled_shark);
        registerWater(AMEntityRegistry.MIMIC_OCTOPUS, AMConfig.mimicOctopusSpawnWeight, 1, 2, BiomeConfig.mimic_octopus);
        registerCreature(AMEntityRegistry.SEAGULL, AMConfig.seagullSpawnWeight, 3, 6, BiomeConfig.seagull);
        registerCreature(AMEntityRegistry.TUSKLIN, AMConfig.tusklinSpawnWeight, 3, 5, BiomeConfig.tusklin);
        registerCreature(AMEntityRegistry.LAVIATHAN, AMConfig.laviathanSpawnWeight, 1, 1, BiomeConfig.laviathan);
        registerCreature(AMEntityRegistry.TOUCAN, AMConfig.toucanSpawnWeight, 5, 5, BiomeConfig.toucan);
        registerCreature(AMEntityRegistry.MANED_WOLF, AMConfig.manedWolfSpawnWeight, 1, 1, BiomeConfig.maned_wolf);
        registerCreature(AMEntityRegistry.ANACONDA, AMConfig.anacondaSpawnWeight, 1, 1, BiomeConfig.anaconda);
        registerCreature(AMEntityRegistry.ANTEATER, AMConfig.anteaterSpawnWeight, 1, 3, BiomeConfig.anteater);
        registerCreature(AMEntityRegistry.GELADA_MONKEY, AMConfig.geladaMonkeySpawnWeight, 9, 16, BiomeConfig.gelada_monkey);
        registerAmbient(AMEntityRegistry.JERBOA, AMConfig.jerboaSpawnWeight, 1, 3, BiomeConfig.jerboa);
        registerWater(AMEntityRegistry.TERRAPIN, AMConfig.terrapinSpawnWeight, 1, 2, BiomeConfig.terrapin);
        registerWater(AMEntityRegistry.COMB_JELLY, AMConfig.combJellySpawnWeight, 2, 3, BiomeConfig.comb_jelly);
        registerAmbient(AMEntityRegistry.COSMIC_COD, AMConfig.cosmicCodSpawnWeight, 9, 13, BiomeConfig.cosmic_cod);
        registerCreature(AMEntityRegistry.BISON, AMConfig.bisonSpawnWeight, 6, 10, BiomeConfig.bison);
        registerWater(AMEntityRegistry.GIANT_SQUID, AMConfig.giantSquidSpawnWeight, 1, 2, BiomeConfig.giant_squid);
        registerWater(AMEntityRegistry.DEVILS_HOLE_PUPFISH, AMConfig.pupfishSpawnWeight, 5, 12, BiomeConfig.devils_hole_pupfish);
        registerWater(AMEntityRegistry.CATFISH, AMConfig.catfishSpawnWeight, 1, 3, BiomeConfig.catfish);
        registerWater(AMEntityRegistry.FLYING_FISH, AMConfig.flyingFishSpawnWeight, 3, 6, BiomeConfig.flying_fish);
        registerAmbient(AMEntityRegistry.RAIN_FROG, AMConfig.rainFrogSpawnWeight, 1, 3, BiomeConfig.rain_frog);
        registerCreature(AMEntityRegistry.POTOO, AMConfig.potooSpawnWeight, 1, 1, BiomeConfig.potoo);
        registerCreature(AMEntityRegistry.RHINOCEROS, AMConfig.rhinocerosSpawnWeight, 3, 5, BiomeConfig.rhinoceros);
        registerCreature(AMEntityRegistry.SUGAR_GLIDER, AMConfig.sugarGliderSpawnWeight, 2, 4, BiomeConfig.sugar_glider);
        registerCreature(AMEntityRegistry.SKUNK, AMConfig.skunkSpawnWeight, 1, 2, BiomeConfig.skunk);
        registerCreature(AMEntityRegistry.BANANA_SLUG, AMConfig.bananaSlugSpawnWeight, 2, 3, BiomeConfig.banana_slug);
        registerCreature(AMEntityRegistry.BLUE_JAY, AMConfig.blueJaySpawnWeight, 2, 4, BiomeConfig.blue_jay);
        registerCreature(AMEntityRegistry.CAIMAN, AMConfig.caimanSpawnWeight, 2, 4, BiomeConfig.caiman);
        registerWater(AMEntityRegistry.TRIOPS, AMConfig.triopsSpawnWeight, 2, 6, BiomeConfig.triops);
        AlexsMobs.LOGGER.info("Registered Alex's Mobs biome spawns");
    }

    private static void registerCreature(EntityEntry entry, int weight, int min, int max, Pair<String, SpawnBiomeData> biomes) {
        add(entry, weight, min, max, EnumCreatureType.CREATURE, biomes);
    }

    private static void registerMonster(EntityEntry entry, int weight, int min, int max, Pair<String, SpawnBiomeData> biomes) {
        add(entry, weight, min, max, EnumCreatureType.MONSTER, biomes);
    }

    private static void registerAmbient(EntityEntry entry, int weight, int min, int max, Pair<String, SpawnBiomeData> biomes) {
        add(entry, weight, min, max, EnumCreatureType.AMBIENT, biomes);
    }

    private static void registerWater(EntityEntry entry, int weight, int min, int max, Pair<String, SpawnBiomeData> biomes) {
        add(entry, weight, min, max, EnumCreatureType.WATER_CREATURE, biomes);
    }

    @SuppressWarnings("unchecked")
    private static void add(EntityEntry entry, int weight, int min, int max, EnumCreatureType type, Pair<String, SpawnBiomeData> biomeConfig) {
        if (entry == null || weight <= 0 || biomeConfig == null) {
            return;
        }
        Class<? extends EntityLiving> clazz;
        try {
            clazz = (Class<? extends EntityLiving>) entry.getEntityClass();
        } catch (ClassCastException e) {
            AlexsMobs.LOGGER.warn("Skipped spawn registration for {}", entry.getRegistryName());
            return;
        }
        List<Biome> biomes = new ArrayList<>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (testBiome(biomeConfig, biome)) {
                biomes.add(biome);
            }
        }
        if (biomes.isEmpty()) {
            return;
        }
        EntityRegistry.addSpawn(clazz, weight, min, max, type, biomes.toArray(new Biome[0]));
    }

    private static boolean testBiome(Pair<String, SpawnBiomeData> entry, Biome biome) {
        try {
            return BiomeConfig.test(entry, biome);
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("could not test biome config for {}, defaulting to no spawns for mob", entry.getLeft());
            return false;
        }
    }

    private static void purgeAlexsMobsSpawns() {
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            for (EnumCreatureType type : EnumCreatureType.values()) {
                List<Biome.SpawnListEntry> list = biome.getSpawnableList(type);
                Iterator<Biome.SpawnListEntry> it = list.iterator();
                while (it.hasNext()) {
                    Biome.SpawnListEntry spawn = it.next();
                    if (spawn.entityClass != null && spawn.entityClass.getName().startsWith("com.github.alexthe666.alexsmobs.entity.")) {
                        it.remove();
                    }
                }
            }
        }
    }
}
