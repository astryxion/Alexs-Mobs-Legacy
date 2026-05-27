package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes;
import com.github.alexthe666.alexsmobs.entity.EntityAlligatorSnappingTurtle;
import com.github.alexthe666.alexsmobs.entity.EntityAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityAnteater;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
import com.github.alexthe666.alexsmobs.entity.EntityBison;
import com.github.alexthe666.alexsmobs.entity.EntityBlobfish;
import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeHead;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import com.github.alexthe666.alexsmobs.entity.EntityCrimsonMosquito;
import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.alexsmobs.entity.EntityCrocodile;
import com.github.alexthe666.alexsmobs.entity.EntityGeladaMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityManedWolf;
import com.github.alexthe666.alexsmobs.entity.EntityTerrapin;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.entity.EntityDropBear;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import com.github.alexthe666.alexsmobs.entity.EntityEmu;
import com.github.alexthe666.alexsmobs.entity.EntityFly;
import com.github.alexthe666.alexsmobs.entity.EntityFrilledShark;
import com.github.alexthe666.alexsmobs.entity.EntityGazelle;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import com.github.alexthe666.alexsmobs.entity.EntityRainFrog;
import com.github.alexthe666.alexsmobs.entity.EntityCatfish;
import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import com.github.alexthe666.alexsmobs.entity.EntityFlyingFish;
import com.github.alexthe666.alexsmobs.entity.EntityCosmicCod;
import com.github.alexthe666.alexsmobs.entity.EntityDevilsHolePupfish;
import com.github.alexthe666.alexsmobs.entity.EntityTriops;
import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityGuster;
import com.github.alexthe666.alexsmobs.entity.EntityHammerheadShark;
import com.github.alexthe666.alexsmobs.entity.EntityHummingbird;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.alexsmobs.entity.EntityKomodoDragon;
import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import com.github.alexthe666.alexsmobs.entity.EntityLobster;
import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import com.github.alexthe666.alexsmobs.entity.EntityMimicOctopus;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import com.github.alexthe666.alexsmobs.entity.EntityMungus;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import com.github.alexthe666.alexsmobs.entity.EntityPotoo;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import com.github.alexthe666.alexsmobs.entity.EntityRattlesnake;
import com.github.alexthe666.alexsmobs.entity.EntityRhinoceros;
import com.github.alexthe666.alexsmobs.entity.EntityRoadrunner;
import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import com.github.alexthe666.alexsmobs.entity.EntityShoebill;
import com.github.alexthe666.alexsmobs.entity.EntitySkunk;
import com.github.alexthe666.alexsmobs.entity.EntitySnowLeopard;
import com.github.alexthe666.alexsmobs.entity.EntitySoulVulture;
import com.github.alexthe666.alexsmobs.entity.EntitySpectre;
import com.github.alexthe666.alexsmobs.entity.EntityStraddler;
import com.github.alexthe666.alexsmobs.entity.EntityStradpole;
import com.github.alexthe666.alexsmobs.entity.EntitySugarGlider;
import com.github.alexthe666.alexsmobs.entity.EntitySunbird;
import com.github.alexthe666.alexsmobs.entity.EntityToucan;
import com.github.alexthe666.alexsmobs.entity.EntityTarantulaHawk;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import com.github.alexthe666.alexsmobs.entity.EntityTiger;
import com.github.alexthe666.alexsmobs.entity.EntityTusklin;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWorm;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedMosco;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

/**
 * Familiar Fauna 1.12.2 spawn registration for all Alex's Mobs natural spawns.
 * <p>
 * Uses explicit biome JSON lists ({@link AMSpawnBiomeConfiguration}), FF-style weights/group sizes
 * ({@link AMFaunaSpawnProfile}), and {@link AMSpawnUtil#addSpawn} biome list mutation.
 * {@link AMConfig} spawn weights of {@code 0} disable a mob; positive values enable FF-profile registration
 * with Familiar Fauna-style weights and capped group sizes (no duplicate biome rows).
 */
public final class AMFaunaSpawnRegistry {

    private AMFaunaSpawnRegistry() {
    }

    private static final class SpawnEntry {
        private final String configName;
        private final Class<? extends EntityLiving> entityClass;
        private final AMNativeSpawnBiomes.Matcher biomeMatcher;
        private final AMFaunaSpawnProfile profile;
        private final IntSupplier configWeight;

        private SpawnEntry(String configName, Class<? extends EntityLiving> entityClass,
                           AMNativeSpawnBiomes.Matcher biomeMatcher, AMFaunaSpawnProfile profile,
                           IntSupplier configWeight) {
            this.configName = configName;
            this.entityClass = entityClass;
            this.biomeMatcher = biomeMatcher;
            this.profile = profile;
            this.configWeight = configWeight;
        }
    }

    private static SpawnEntry entry(String configName, Class<? extends EntityLiving> entityClass,
                                      AMNativeSpawnBiomes.Matcher matcher, AMFaunaSpawnProfile profile,
                                      IntSupplier weight) {
        return new SpawnEntry(configName, entityClass, matcher, profile, weight);
    }

    /** All natural-spawning Alex's Mobs with Familiar Fauna weight/group profiles. */
    private static final SpawnEntry[] ENTRIES = {
            entry("grizzly_bear", EntityGrizzlyBear.class, AMNativeSpawnBiomes::grizzlyBear, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.grizzlyBearSpawnWeight),
            entry("roadrunner", EntityRoadrunner.class, AMNativeSpawnBiomes::roadrunner, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.roadrunnerSpawnWeight),
            entry("bone_serpent", EntityBoneSerpent.class, AMNativeSpawnBiomes::boneSerpent, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.boneSerpentSpawnWeight),
            entry("gazelle", EntityGazelle.class, AMNativeSpawnBiomes::gazelle, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.gazelleSpawnWeight),
            entry("crocodile", EntityCrocodile.class, AMNativeSpawnBiomes::crocodile, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.crocodileSpawnWeight),
            entry("fly", EntityFly.class, AMNativeSpawnBiomes::fly, AMFaunaSpawnProfile.AMBIENT_BUTTERFLY, () -> AMConfig.flySpawnWeight),
            entry("hummingbird", EntityHummingbird.class, AMNativeSpawnBiomes::hummingbird, AMFaunaSpawnProfile.AMBIENT_DRAGONFLY, () -> AMConfig.hummingbirdSpawnWeight),
            entry("orca", EntityOrca.class, AMNativeSpawnBiomes::orca, AMFaunaSpawnProfile.WATER_PIXIE, () -> AMConfig.orcaSpawnWeight),
            entry("sunbird", EntitySunbird.class, AMNativeSpawnBiomes::sunbird, AMFaunaSpawnProfile.CREATURE_PIXIE, () -> AMConfig.sunbirdSpawnWeight),
            entry("gorilla", EntityGorilla.class, AMNativeSpawnBiomes::gorilla, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.gorillaSpawnWeight),
            entry("crimson_mosquito", EntityCrimsonMosquito.class, AMNativeSpawnBiomes::crimsonMosquito, AMFaunaSpawnProfile.MONSTER_DEER, () -> AMConfig.crimsonMosquitoSpawnWeight),
            entry("rattlesnake", EntityRattlesnake.class, AMNativeSpawnBiomes::rattlesnake, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.rattlesnakeSpawnWeight),
            entry("endergrade", EntityEndergrade.class, AMNativeSpawnBiomes::endergrade, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.endergradeSpawnWeight),
            entry("hammerhead_shark", EntityHammerheadShark.class, AMNativeSpawnBiomes::hammerheadShark, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.hammerheadSharkSpawnWeight),
            entry("lobster", EntityLobster.class, AMNativeSpawnBiomes::lobster, AMFaunaSpawnProfile.WATER_SNAIL, () -> AMConfig.lobsterSpawnWeight),
            entry("komodo_dragon", EntityKomodoDragon.class, AMNativeSpawnBiomes::komodoDragon, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.komodoDragonSpawnWeight),
            entry("capuchin_monkey", EntityCapuchinMonkey.class, AMNativeSpawnBiomes::capuchinMonkey, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.capuchinMonkeySpawnWeight),
            entry("cave_centipede", EntityCentipedeHead.class, AMNativeSpawnBiomes::caveCentipede, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.caveCentipedeSpawnWeight),
            entry("warped_toad", EntityWarpedToad.class, AMNativeSpawnBiomes::warpedToad, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.warpedToadSpawnWeight),
            entry("moose", EntityMoose.class, AMNativeSpawnBiomes::moose, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.mooseSpawnWeight),
            entry("mimicube", EntityMimicube.class, AMNativeSpawnBiomes::mimicube, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.mimicubeSpawnWeight),
            entry("raccoon", EntityRaccoon.class, AMNativeSpawnBiomes::raccoon, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.raccoonSpawnWeight),
            entry("blobfish", EntityBlobfish.class, AMNativeSpawnBiomes::blobfish, AMFaunaSpawnProfile.WATER_SNAIL, () -> AMConfig.blobfishSpawnWeight),
            entry("seal", EntitySeal.class, AMNativeSpawnBiomes::seal, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.sealSpawnWeight),
            entry("cockroach", EntityCockroach.class, AMNativeSpawnBiomes::cockroach, AMFaunaSpawnProfile.AMBIENT_SNAIL, () -> AMConfig.cockroachSpawnWeight),
            entry("shoebill", EntityShoebill.class, AMNativeSpawnBiomes::shoebill, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.shoebillSpawnWeight),
            entry("elephant", EntityElephant.class, AMNativeSpawnBiomes::elephant, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.elephantSpawnWeight),
            entry("bison", EntityBison.class, AMNativeSpawnBiomes::bison, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.bisonSpawnWeight),
            entry("tusklin", EntityTusklin.class, AMNativeSpawnBiomes::tusklin, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.tusklinSpawnWeight),
            entry("rhinoceros", EntityRhinoceros.class, AMNativeSpawnBiomes::rhinoceros, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.rhinocerosSpawnWeight),
            entry("skunk", EntitySkunk.class, AMNativeSpawnBiomes::skunk, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.skunkSpawnWeight),
            entry("anteater", EntityAnteater.class, AMNativeSpawnBiomes::anteater, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.anteaterSpawnWeight),
            entry("soul_vulture", EntitySoulVulture.class, AMNativeSpawnBiomes::soulVulture, AMFaunaSpawnProfile.MONSTER_DEER, () -> AMConfig.soulVultureSpawnWeight),
            entry("snow_leopard", EntitySnowLeopard.class, AMNativeSpawnBiomes::snowLeopard, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.snowLeopardSpawnWeight),
            entry("spectre", EntitySpectre.class, AMNativeSpawnBiomes::spectre, AMFaunaSpawnProfile.CREATURE_PIXIE, () -> AMConfig.spectreSpawnWeight),
            entry("crow", EntityCrow.class, AMNativeSpawnBiomes::crow, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.crowSpawnWeight),
            entry("alligator_snapping_turtle", EntityAlligatorSnappingTurtle.class, AMNativeSpawnBiomes::alligatorSnappingTurtle, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.alligatorSnappingTurtleSpawnWeight),
            entry("mungus", EntityMungus.class, AMNativeSpawnBiomes::mungus, AMFaunaSpawnProfile.CREATURE_PIXIE, () -> AMConfig.mungusSpawnWeight),
            entry("mantis_shrimp", EntityMantisShrimp.class, AMNativeSpawnBiomes::mantisShrimp, AMFaunaSpawnProfile.WATER_SNAIL, () -> AMConfig.mantisShrimpSpawnWeight),
            entry("guster", EntityGuster.class, AMNativeSpawnBiomes::guster, AMFaunaSpawnProfile.MONSTER_DEER, () -> AMConfig.gusterSpawnWeight),
            entry("warped_mosco", EntityWarpedMosco.class, AMNativeSpawnBiomes::warpedMosco, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.warpedMoscoSpawnWeight),
            entry("straddler", EntityStraddler.class, AMNativeSpawnBiomes::straddler, AMFaunaSpawnProfile.MONSTER_DEER, () -> AMConfig.straddlerSpawnWeight),
            entry("stradpole", EntityStradpole.class, AMNativeSpawnBiomes::stradpole, AMFaunaSpawnProfile.WATER_SNAIL, () -> AMConfig.stradpoleSpawnWeight),
            entry("emu", EntityEmu.class, AMNativeSpawnBiomes::savannaAndMesa, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.emuSpawnWeight),
            entry("platypus", EntityPlatypus.class, AMNativeSpawnBiomes::platypus, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.platypusSpawnWeight),
            entry("dropbear", EntityDropBear.class, AMNativeSpawnBiomes::dropbear, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.dropbearSpawnWeight),
            entry("tasmanian_devil", EntityTasmanianDevil.class, AMNativeSpawnBiomes::tasmanianDevil, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.tasmanianDevilSpawnWeight),
            entry("kangaroo", EntityKangaroo.class, AMNativeSpawnBiomes::savannaAndMesa, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.kangarooSpawnWeight),
            entry("cachalot_whale", EntityCachalotWhale.class, AMNativeSpawnBiomes::cachalotWhale, AMFaunaSpawnProfile.WATER_PIXIE, () -> AMConfig.cachalotWhaleSpawnWeight),
            entry("enderiophage", EntityEnderiophage.class, AMNativeSpawnBiomes::enderiophage, AMFaunaSpawnProfile.CREATURE_PIXIE, () -> AMConfig.enderiophageSpawnWeight),
            entry("bald_eagle", EntityBaldEagle.class, AMNativeSpawnBiomes::baldEagle, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.baldEagleSpawnWeight),
            entry("tiger", EntityTiger.class, AMNativeSpawnBiomes::tiger, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.tigerSpawnWeight),
            entry("tarantula_hawk", EntityTarantulaHawk.class, AMNativeSpawnBiomes::tarantulaHawk, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.tarantulaHawkSpawnWeight),
            entry("void_worm", EntityVoidWorm.class, AMNativeSpawnBiomes::voidWorm, AMFaunaSpawnProfile.MONSTER_PIXIE, () -> AMConfig.voidWormSpawnWeight),
            entry("frilled_shark", EntityFrilledShark.class, AMNativeSpawnBiomes::frilledShark, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.frilledSharkSpawnWeight),
            entry("mimic_octopus", EntityMimicOctopus.class, AMNativeSpawnBiomes::mimicOctopus, AMFaunaSpawnProfile.WATER_PIXIE, () -> AMConfig.mimicOctopusSpawnWeight),
            entry("seagull", EntitySeagull.class, AMNativeSpawnBiomes::seagull, AMFaunaSpawnProfile.CREATURE_TURKEY, () -> AMConfig.seagullSpawnWeight),
            entry("blue_jay", EntityBlueJay.class, AMNativeSpawnBiomes::blueJay, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.blueJaySpawnWeight),
            entry("banana_slug", EntityBananaSlug.class, AMNativeSpawnBiomes::bananaSlug, AMFaunaSpawnProfile.AMBIENT_SNAIL, () -> AMConfig.bananaSlugSpawnWeight),
            entry("anaconda", EntityAnaconda.class, AMNativeSpawnBiomes::anaconda, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.anacondaSpawnWeight),
            entry("laviathan", EntityLaviathan.class, AMNativeSpawnBiomes::laviathan, AMFaunaSpawnProfile.MONSTER_DEER, () -> AMConfig.laviathanSpawnWeight),
            entry("giant_squid", EntityGiantSquid.class, AMNativeSpawnBiomes::giantSquid, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.giantSquidSpawnWeight),
            entry("jerboa", EntityJerboa.class, AMNativeSpawnBiomes::jerboa, AMFaunaSpawnProfile.AMBIENT_JERBOA, () -> AMConfig.jerboaSpawnWeight),
            entry("rain_frog", EntityRainFrog.class, AMNativeSpawnBiomes::rainFrog, AMFaunaSpawnProfile.AMBIENT_RAIN_FROG, () -> AMConfig.rainFrogSpawnWeight),
            entry("triops", EntityTriops.class, AMNativeSpawnBiomes::triops, AMFaunaSpawnProfile.WATER_TRIOPS, () -> AMConfig.triopsSpawnWeight),
            entry("flying_fish", EntityFlyingFish.class, AMNativeSpawnBiomes::flyingFish, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.flyingFishSpawnWeight),
            entry("comb_jelly", EntityCombJelly.class, AMNativeSpawnBiomes::combJelly, AMFaunaSpawnProfile.WATER_SNAIL, () -> AMConfig.combJellySpawnWeight),
            entry("cosmic_cod", EntityCosmicCod.class, AMNativeSpawnBiomes::cosmicCod, AMFaunaSpawnProfile.AMBIENT_DRAGONFLY, () -> AMConfig.cosmicCodSpawnWeight),
            entry("devils_hole_pupfish", EntityDevilsHolePupfish.class, AMNativeSpawnBiomes::devilsHolePupfish, AMFaunaSpawnProfile.WATER_PIXIE, () -> AMConfig.pupfishSpawnWeight),
            entry("catfish", EntityCatfish.class, AMNativeSpawnBiomes::catfish, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.catfishSpawnWeight),
            entry("terrapin", EntityTerrapin.class, AMNativeSpawnBiomes::terrapin, AMFaunaSpawnProfile.WATER_DRAGONFLY, () -> AMConfig.terrapinSpawnWeight),
            entry("gelada_monkey", EntityGeladaMonkey.class, AMNativeSpawnBiomes::geladaMonkey, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.geladaMonkeySpawnWeight),
            entry("maned_wolf", EntityManedWolf.class, AMNativeSpawnBiomes::manedWolf, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.manedWolfSpawnWeight),
            entry("caiman", EntityCaiman.class, AMNativeSpawnBiomes::caiman, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.caimanSpawnWeight),
            entry("toucan", EntityToucan.class, AMNativeSpawnBiomes::toucan, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.toucanSpawnWeight),
            entry("potoo", EntityPotoo.class, AMNativeSpawnBiomes::potoo, AMFaunaSpawnProfile.CREATURE_PIXIE, () -> AMConfig.potooSpawnWeight),
            entry("sugar_glider", EntitySugarGlider.class, AMNativeSpawnBiomes::sugarGlider, AMFaunaSpawnProfile.CREATURE_DEER, () -> AMConfig.sugarGliderSpawnWeight),
    };

    /**
     * Applies all natural spawns on the logical server. Safe to call each time a world loads;
     * strips prior Alex's Mobs rows first so nothing stacks into mega-packs.
     */
    public static void register() {
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            return;
        }
        AMSpawnUtil.purgeSpawns(collectSpawnEntityClasses());
        int biomeRows = 0;
        int mobsEnabled = 0;
        for (SpawnEntry spawn : ENTRIES) {
            if (spawn.configWeight.getAsInt() <= 0) {
                continue;
            }
            if (spawn.entityClass == EntityMimicube.class && AMConfig.mimicubeSpawnInEndCity) {
                continue;
            }
            if (spawn.entityClass == EntitySoulVulture.class && AMConfig.soulVultureSpawnOnFossil) {
                continue;
            }
            List<String> biomes = AMSpawnBiomeConfiguration.loadBiomeList(spawn.configName, spawn.biomeMatcher);
            if (biomes.isEmpty()) {
                AlexsMobs.LOGGER.warn("Alex's Mobs: no valid 1.12.2 biomes for '{}' ({}) — check config/spawns/{}_biomes.json",
                        spawn.configName, spawn.entityClass.getSimpleName(), spawn.configName);
                continue;
            }
            biomeRows += AMSpawnUtil.addSpawn(
                    spawn.entityClass,
                    spawn.profile.weight,
                    cappedMinGroup(spawn.profile),
                    cappedMaxGroup(spawn.profile),
                    spawn.profile.creatureType,
                    biomes);
            mobsEnabled++;
        }
        biomeRows += registerStructureSpawns();
        AlexsMobs.LOGGER.info("Alex's Mobs: spawn registry applied ({} mobs, {} biome rows)", mobsEnabled, biomeRows);
    }

    private static Set<Class<? extends EntityLiving>> collectSpawnEntityClasses() {
        Set<Class<? extends EntityLiving>> classes = new HashSet<>();
        for (SpawnEntry spawn : ENTRIES) {
            classes.add(spawn.entityClass);
        }
        classes.add(EntityMimicube.class);
        classes.add(EntitySoulVulture.class);
        return classes;
    }

    /** 1.12-era caps: deer/turkey-sized herds at most 4; ambient/water/rare stay smaller. */
    private static int cappedMaxGroup(AMFaunaSpawnProfile profile) {
        int max = profile.maxGroup;
        if (profile.creatureType == EnumCreatureType.AMBIENT) {
            return Math.min(max, 4);
        }
        if (profile.creatureType == EnumCreatureType.WATER_CREATURE) {
            return Math.min(max, 2);
        }
        if (profile.creatureType == EnumCreatureType.MONSTER) {
            return Math.min(max, 2);
        }
        return Math.min(max, 4);
    }

    private static int cappedMinGroup(AMFaunaSpawnProfile profile) {
        return Math.max(1, Math.min(profile.minGroup, cappedMaxGroup(profile)));
    }

    private static int registerStructureSpawns() {
        int rows = 0;
        if (AMConfig.mimicubeSpawnInEndCity && AMConfig.mimicubeSpawnWeight > 0) {
            List<String> endBiomes = AMSpawnBiomeConfiguration.loadBiomeList("mimicube_end_city",
                    b -> b == Biomes.SKY || AMNativeSpawnBiomes.mimicube(b));
            rows += AMSpawnUtil.addSpawn(EntityMimicube.class,
                    AMFaunaSpawnProfile.MONSTER_PIXIE.weight,
                    AMFaunaSpawnProfile.MONSTER_PIXIE.minGroup,
                    AMFaunaSpawnProfile.MONSTER_PIXIE.maxGroup,
                    AMFaunaSpawnProfile.MONSTER_PIXIE.creatureType,
                    endBiomes);
        }
        if (AMConfig.soulVultureSpawnOnFossil && AMConfig.soulVultureSpawnWeight > 0) {
            List<String> netherBiomes = AMSpawnBiomeConfiguration.loadBiomeList("soul_vulture_fossil",
                    b -> b == Biomes.HELL && AMNativeSpawnBiomes.soulVulture(b));
            rows += AMSpawnUtil.addSpawn(EntitySoulVulture.class,
                    AMFaunaSpawnProfile.MONSTER_DEER.weight,
                    AMFaunaSpawnProfile.MONSTER_DEER.minGroup,
                    AMFaunaSpawnProfile.MONSTER_DEER.maxGroup,
                    AMFaunaSpawnProfile.MONSTER_DEER.creatureType,
                    netherBiomes);
        }
        return rows;
    }
}
