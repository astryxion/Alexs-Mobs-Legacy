package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.entity.EntityAlligatorSnappingTurtle;
import com.github.alexthe666.alexsmobs.entity.EntityAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityAnteater;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.entity.EntityBananaSlug;
import com.github.alexthe666.alexsmobs.entity.EntityBison;
import com.github.alexthe666.alexsmobs.entity.EntityBlobfish;
import com.github.alexthe666.alexsmobs.entity.EntityBlueJay;
import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpent;
import com.github.alexthe666.alexsmobs.entity.EntityCachalotWhale;
import com.github.alexthe666.alexsmobs.entity.EntityCaiman;
import com.github.alexthe666.alexsmobs.entity.EntityCapuchinMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityCatfish;
import com.github.alexthe666.alexsmobs.entity.EntityCentipedeHead;
import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import com.github.alexthe666.alexsmobs.entity.EntityCockroach;
import com.github.alexthe666.alexsmobs.entity.EntityCosmicCod;
import com.github.alexthe666.alexsmobs.entity.EntityCrimsonMosquito;
import com.github.alexthe666.alexsmobs.entity.EntityCrocodile;
import com.github.alexthe666.alexsmobs.entity.EntityCrow;
import com.github.alexthe666.alexsmobs.entity.EntityDevilsHolePupfish;
import com.github.alexthe666.alexsmobs.entity.EntityDropBear;
import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import com.github.alexthe666.alexsmobs.entity.EntityEndergrade;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import com.github.alexthe666.alexsmobs.entity.EntityEmu;
import com.github.alexthe666.alexsmobs.entity.EntityFly;
import com.github.alexthe666.alexsmobs.entity.EntityFlyingFish;
import com.github.alexthe666.alexsmobs.entity.EntityFrilledShark;
import com.github.alexthe666.alexsmobs.entity.EntityGazelle;
import com.github.alexthe666.alexsmobs.entity.EntityGeladaMonkey;
import com.github.alexthe666.alexsmobs.entity.EntityGiantSquid;
import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import com.github.alexthe666.alexsmobs.entity.EntityGrizzlyBear;
import com.github.alexthe666.alexsmobs.entity.EntityGuster;
import com.github.alexthe666.alexsmobs.entity.EntityHammerheadShark;
import com.github.alexthe666.alexsmobs.entity.EntityHummingbird;
import com.github.alexthe666.alexsmobs.entity.EntityJerboa;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.alexsmobs.entity.EntityKomodoDragon;
import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import com.github.alexthe666.alexsmobs.entity.EntityLobster;
import com.github.alexthe666.alexsmobs.entity.EntityManedWolf;
import com.github.alexthe666.alexsmobs.entity.EntityMantisShrimp;
import com.github.alexthe666.alexsmobs.entity.EntityMimicOctopus;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import com.github.alexthe666.alexsmobs.entity.EntityMungus;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import com.github.alexthe666.alexsmobs.entity.EntityPotoo;
import com.github.alexthe666.alexsmobs.entity.EntityRaccoon;
import com.github.alexthe666.alexsmobs.entity.EntityRainFrog;
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
import com.github.alexthe666.alexsmobs.entity.EntityTarantulaHawk;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import com.github.alexthe666.alexsmobs.entity.EntityTerrapin;
import com.github.alexthe666.alexsmobs.entity.EntityTiger;
import com.github.alexthe666.alexsmobs.entity.EntityToucan;
import com.github.alexthe666.alexsmobs.entity.EntityTriops;
import com.github.alexthe666.alexsmobs.entity.EntityTusklin;
import com.github.alexthe666.alexsmobs.entity.EntityVoidWorm;
import com.github.alexthe666.alexsmobs.entity.EntityWarpedToad;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;

/**
 * Natural spawn table: 1.16/1.20 creature types, weights ({@link AMConfig}), and group sizes.
 */
public final class AMSpawnDefinitions {

    private static final int[] OVERWORLD = {0};
    private static final int[] NETHER = {-1};
    private static final int[] END = {1};

    private AMSpawnDefinitions() {
    }

    public static List<AMSpawnData> all() {
        List<AMSpawnData> list = new ArrayList<>();
        // Overworld land & ambient
        list.add(entry(EntityGrizzlyBear.class, AMSpawnBiomeFilters.config(BiomeConfig.grizzlyBear), EnumCreatureType.CREATURE, () -> AMConfig.grizzlyBearSpawnWeight, 2, 3, OVERWORLD));
        list.add(entry(EntityRoadrunner.class, AMSpawnBiomeFilters.config(BiomeConfig.roadrunner), EnumCreatureType.CREATURE, () -> AMConfig.roadrunnerSpawnWeight, 2, 2, OVERWORLD));
        list.add(entry(EntityGazelle.class, AMSpawnBiomeFilters.config(BiomeConfig.gazelle), EnumCreatureType.CREATURE, () -> AMConfig.gazelleSpawnWeight, 7, 7, OVERWORLD));
        list.add(entry(EntityCrocodile.class, AMSpawnBiomeFilters.config(BiomeConfig.crocodile), EnumCreatureType.CREATURE, () -> AMConfig.crocodileSpawnWeight, 1, 2, OVERWORLD));
        // 1.12 has no AMBIENT daytime pool like 1.16+; CREATURE so flies compete with land animals.
        list.add(entry(EntityFly.class, AMSpawnBiomeFilters.config(BiomeConfig.fly), EnumCreatureType.CREATURE, () -> AMConfig.flySpawnWeight, 2, 3, OVERWORLD));
        list.add(entry(EntityHummingbird.class, AMSpawnBiomeFilters.config(BiomeConfig.hummingbird), EnumCreatureType.CREATURE, () -> AMConfig.hummingbirdSpawnWeight, 7, 7, OVERWORLD));
        list.add(entry(EntitySunbird.class, AMSpawnBiomeFilters.config(BiomeConfig.sunbird), EnumCreatureType.CREATURE, () -> AMConfig.sunbirdSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntityGorilla.class, AMSpawnBiomeFilters.config(BiomeConfig.gorilla), EnumCreatureType.CREATURE, () -> AMConfig.gorillaSpawnWeight, 7, 7, OVERWORLD));
        list.add(entry(EntityRattlesnake.class, AMSpawnBiomeFilters.config(BiomeConfig.rattlesnake), EnumCreatureType.CREATURE, () -> AMConfig.rattlesnakeSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityKomodoDragon.class, AMSpawnBiomeFilters.config(BiomeConfig.komodoDragon), EnumCreatureType.CREATURE, () -> AMConfig.komodoDragonSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityCapuchinMonkey.class, AMSpawnBiomeFilters.config(BiomeConfig.capuchinMonkey), EnumCreatureType.CREATURE, () -> AMConfig.capuchinMonkeySpawnWeight, 9, 16, OVERWORLD));
        list.add(entry(EntityMoose.class, AMSpawnBiomeFilters.config(BiomeConfig.moose), EnumCreatureType.CREATURE, () -> AMConfig.mooseSpawnWeight, 3, 4, OVERWORLD));
        list.add(entry(EntityRaccoon.class, AMSpawnBiomeFilters.config(BiomeConfig.raccoon), EnumCreatureType.CREATURE, () -> AMConfig.raccoonSpawnWeight, 2, 4, OVERWORLD));
        list.add(entry(EntitySeal.class, AMSpawnBiomeFilters.config(BiomeConfig.seal), EnumCreatureType.CREATURE, () -> AMConfig.sealSpawnWeight, 3, 8, OVERWORLD));
        list.add(entry(EntityCockroach.class, AMSpawnBiomeFilters.config(BiomeConfig.cockroach), EnumCreatureType.AMBIENT, () -> AMConfig.cockroachSpawnWeight, 5, 5, OVERWORLD));
        list.add(entry(EntityShoebill.class, AMSpawnBiomeFilters.config(BiomeConfig.shoebill), EnumCreatureType.CREATURE, () -> AMConfig.shoebillSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityElephant.class, AMSpawnBiomeFilters.config(BiomeConfig.elephant), EnumCreatureType.CREATURE, () -> AMConfig.elephantSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntitySnowLeopard.class, AMSpawnBiomeFilters.config(BiomeConfig.snowLeopard), EnumCreatureType.CREATURE, () -> AMConfig.snowLeopardSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityCrow.class, AMSpawnBiomeFilters.config(BiomeConfig.crow), EnumCreatureType.CREATURE, () -> AMConfig.crowSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntityAlligatorSnappingTurtle.class, AMSpawnBiomeFilters.config(BiomeConfig.alligatorSnappingTurtle), EnumCreatureType.CREATURE, () -> AMConfig.alligatorSnappingTurtleSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityMungus.class, AMSpawnBiomeFilters.config(BiomeConfig.mungus), EnumCreatureType.CREATURE, () -> AMConfig.mungusSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntityEmu.class, AMSpawnBiomeFilters.config(BiomeConfig.emu), EnumCreatureType.CREATURE, () -> AMConfig.emuSpawnWeight, 2, 5, OVERWORLD));
        list.add(entry(EntityPlatypus.class, AMSpawnBiomeFilters.config(BiomeConfig.platypus), EnumCreatureType.CREATURE, () -> AMConfig.platypusSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityTasmanianDevil.class, AMSpawnBiomeFilters.config(BiomeConfig.tasmanianDevil), EnumCreatureType.CREATURE, () -> AMConfig.tasmanianDevilSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityKangaroo.class, AMSpawnBiomeFilters.config(BiomeConfig.kangaroo), EnumCreatureType.CREATURE, () -> AMConfig.kangarooSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntityBaldEagle.class, AMSpawnBiomeFilters.config(BiomeConfig.baldEagle), EnumCreatureType.CREATURE, () -> AMConfig.baldEagleSpawnWeight, 2, 4, OVERWORLD));
        list.add(entry(EntityTiger.class, AMSpawnBiomeFilters.config(BiomeConfig.tiger), EnumCreatureType.CREATURE, () -> AMConfig.tigerSpawnWeight, 1, 3, OVERWORLD));
        list.add(entry(EntityTarantulaHawk.class, AMSpawnBiomeFilters.config(BiomeConfig.tarantula_hawk), EnumCreatureType.CREATURE, () -> AMConfig.tarantulaHawkSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntitySeagull.class, AMSpawnBiomeFilters.config(BiomeConfig.seagull), EnumCreatureType.CREATURE, () -> AMConfig.seagullSpawnWeight, 3, 6, OVERWORLD));
        list.add(entry(EntityTusklin.class, AMSpawnBiomeFilters.config(BiomeConfig.tusklin), EnumCreatureType.CREATURE, () -> AMConfig.tusklinSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntityBananaSlug.class, AMSpawnBiomeFilters.config(BiomeConfig.banana_slug), EnumCreatureType.CREATURE, () -> AMConfig.bananaSlugSpawnWeight, 2, 3, OVERWORLD));
        list.add(entry(EntityAnaconda.class, AMSpawnBiomeFilters.config(BiomeConfig.anaconda), EnumCreatureType.CREATURE, () -> AMConfig.anacondaSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntityAnteater.class, AMSpawnBiomeFilters.config(BiomeConfig.anteater), EnumCreatureType.CREATURE, () -> AMConfig.anteaterSpawnWeight, 1, 3, OVERWORLD));
        list.add(entry(EntityJerboa.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::jerboa), EnumCreatureType.AMBIENT, () -> AMConfig.jerboaSpawnWeight, 1, 3, OVERWORLD));
        list.add(entry(EntityRainFrog.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::rainFrog), EnumCreatureType.AMBIENT, () -> AMConfig.rainFrogSpawnWeight, 1, 3, OVERWORLD));
        list.add(entry(EntityBison.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::bison), EnumCreatureType.CREATURE, () -> AMConfig.bisonSpawnWeight, 6, 10, OVERWORLD));
        list.add(entry(EntityRhinoceros.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::rhinoceros), EnumCreatureType.CREATURE, () -> AMConfig.rhinocerosSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntitySkunk.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::skunk), EnumCreatureType.CREATURE, () -> AMConfig.skunkSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityBlueJay.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::blueJay), EnumCreatureType.CREATURE, () -> AMConfig.blueJaySpawnWeight, 2, 4, OVERWORLD));
        list.add(entry(EntityGeladaMonkey.class, AMSpawnBiomeFilters.config(BiomeConfig.geladaMonkey), EnumCreatureType.CREATURE, () -> AMConfig.geladaMonkeySpawnWeight, 9, 16, OVERWORLD));
        list.add(entry(EntityManedWolf.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::manedWolf), EnumCreatureType.CREATURE, () -> AMConfig.manedWolfSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntityCaiman.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::caiman), EnumCreatureType.CREATURE, () -> AMConfig.caimanSpawnWeight, 2, 4, OVERWORLD));
        list.add(entry(EntityToucan.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::toucan), EnumCreatureType.CREATURE, () -> AMConfig.toucanSpawnWeight, 5, 5, OVERWORLD));
        list.add(entry(EntityPotoo.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::potoo), EnumCreatureType.CREATURE, () -> AMConfig.potooSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntitySugarGlider.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::sugarGlider), EnumCreatureType.CREATURE, () -> AMConfig.sugarGliderSpawnWeight, 2, 4, OVERWORLD));
        list.add(entry(EntityGuster.class, AMSpawnBiomeFilters.config(BiomeConfig.guster), EnumCreatureType.MONSTER, () -> AMConfig.gusterSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityCentipedeHead.class, AMSpawnBiomeFilters.config(BiomeConfig.caveCentipede), EnumCreatureType.MONSTER, () -> AMConfig.caveCentipedeSpawnWeight, 1, 1, OVERWORLD));
        // Water overworld
        list.add(entry(EntityOrca.class, AMSpawnBiomeFilters.config(BiomeConfig.orca), EnumCreatureType.WATER_CREATURE, () -> AMConfig.orcaSpawnWeight, 3, 4, OVERWORLD));
        list.add(entry(EntityHammerheadShark.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::hammerheadShark), EnumCreatureType.WATER_CREATURE, () -> AMConfig.hammerheadSharkSpawnWeight, 2, 3, OVERWORLD));
        list.add(entry(EntityLobster.class, AMSpawnBiomeFilters.config(BiomeConfig.lobster), EnumCreatureType.WATER_CREATURE, () -> AMConfig.lobsterSpawnWeight, 3, 5, OVERWORLD));
        list.add(entry(EntityBlobfish.class, AMSpawnBiomeFilters.config(BiomeConfig.blobfish), EnumCreatureType.WATER_CREATURE, () -> AMConfig.blobfishSpawnWeight, 2, 2, OVERWORLD));
        list.add(entry(EntityMantisShrimp.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::mantisShrimp), EnumCreatureType.WATER_CREATURE, () -> AMConfig.mantisShrimpSpawnWeight, 1, 4, OVERWORLD));
        list.add(entry(EntityCachalotWhale.class, AMSpawnBiomeFilters.config(BiomeConfig.cachalot_whale_spawns), EnumCreatureType.WATER_CREATURE, () -> AMConfig.cachalotWhaleSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityFrilledShark.class, AMSpawnBiomeFilters.config(BiomeConfig.frilled_shark), EnumCreatureType.WATER_CREATURE, () -> AMConfig.frilledSharkSpawnWeight, 1, 1, OVERWORLD));
        list.add(entry(EntityMimicOctopus.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::mimicOctopus), EnumCreatureType.WATER_CREATURE, () -> AMConfig.mimicOctopusSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityGiantSquid.class, AMSpawnBiomeFilters.config(BiomeConfig.giant_squid), EnumCreatureType.WATER_CREATURE, () -> AMConfig.giantSquidSpawnWeight, 1, 2, OVERWORLD));
        list.add(entry(EntityTriops.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::triops), EnumCreatureType.WATER_CREATURE, () -> AMConfig.triopsSpawnWeight, 2, 6, OVERWORLD));
        list.add(entry(EntityFlyingFish.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::flyingFish), EnumCreatureType.WATER_CREATURE, () -> AMConfig.flyingFishSpawnWeight, 3, 6, OVERWORLD));
        list.add(entry(EntityCombJelly.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::combJelly), EnumCreatureType.WATER_CREATURE, () -> AMConfig.combJellySpawnWeight, 2, 3, OVERWORLD));
        list.add(entry(EntityDevilsHolePupfish.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::devilsHolePupfish), EnumCreatureType.WATER_CREATURE, () -> AMConfig.pupfishSpawnWeight, 5, 12, OVERWORLD));
        list.add(entry(EntityCatfish.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::catfish), EnumCreatureType.WATER_CREATURE, () -> AMConfig.catfishSpawnWeight, 1, 3, OVERWORLD));
        list.add(entry(EntityTerrapin.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::terrapin), EnumCreatureType.WATER_CREATURE, () -> AMConfig.terrapinSpawnWeight, 1, 2, OVERWORLD));
        // Nether
        list.add(entry(EntityBoneSerpent.class, AMSpawnBiomeFilters.config(BiomeConfig.boneSerpent), EnumCreatureType.MONSTER, () -> AMConfig.boneSerpentSpawnWeight, 1, 1, NETHER));
        list.add(entry(EntityCrimsonMosquito.class, AMSpawnBiomeFilters.config(BiomeConfig.crimsonMosquito), EnumCreatureType.MONSTER, () -> AMConfig.crimsonMosquitoSpawnWeight, 4, 4, NETHER));
        list.add(entry(EntityWarpedToad.class, AMSpawnBiomeFilters.config(BiomeConfig.warpedToad), EnumCreatureType.CREATURE, () -> AMConfig.warpedToadSpawnWeight, 5, 5, NETHER));
        list.add(entry(EntitySoulVulture.class, AMSpawnBiomeFilters.config(BiomeConfig.soulVulture), EnumCreatureType.MONSTER, () -> AMConfig.soulVultureSpawnWeight, 2, 3, NETHER, () -> !AMConfig.soulVultureSpawnOnFossil));
        // Warped Mosco: no natural biomes in 1.16/1.20 (boss via mosquito transformation only).
        list.add(entry(EntityStraddler.class, AMSpawnBiomeFilters.config(BiomeConfig.straddler), EnumCreatureType.MONSTER, () -> AMConfig.straddlerSpawnWeight, 1, 3, NETHER));
        list.add(entry(EntityStradpole.class, AMSpawnBiomeFilters.config(BiomeConfig.stradpole), EnumCreatureType.WATER_CREATURE, () -> AMConfig.stradpoleSpawnWeight, 1, 1, NETHER));
        list.add(entry(EntityDropBear.class, AMSpawnBiomeFilters.config(BiomeConfig.dropbear), EnumCreatureType.MONSTER, () -> AMConfig.dropbearSpawnWeight, 1, 1, NETHER));
        list.add(entry(EntityLaviathan.class, AMSpawnBiomeFilters.config(BiomeConfig.laviathan), EnumCreatureType.CREATURE, () -> AMConfig.laviathanSpawnWeight, 1, 1, NETHER));
        // End
        list.add(entry(EntityEndergrade.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::endergrade), EnumCreatureType.CREATURE, () -> AMConfig.endergradeSpawnWeight, 2, 6, END));
        list.add(entry(EntitySpectre.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::spectre), EnumCreatureType.CREATURE, () -> AMConfig.spectreSpawnWeight, 1, 2, END));
        list.add(entry(EntityEnderiophage.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::enderiophage), EnumCreatureType.CREATURE, () -> AMConfig.enderiophageSpawnWeight, 2, 2, END));
        list.add(entry(EntityMimicube.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::mimicube), EnumCreatureType.MONSTER, () -> AMConfig.mimicubeSpawnWeight, 1, 3, END, () -> !AMConfig.mimicubeSpawnInEndCity));
        list.add(entry(EntityVoidWorm.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::voidWorm), EnumCreatureType.MONSTER, () -> AMConfig.voidWormSpawnWeight, 1, 1, END));
        list.add(entry(EntityCosmicCod.class, AMSpawnBiomeFilters.nativeMatcher(AMNativeSpawnBiomes::cosmicCod), EnumCreatureType.AMBIENT, () -> AMConfig.cosmicCodSpawnWeight, 9, 13, END));
        // Structure-restricted variants (1.20 structure spawn overrides)
        list.add(entry(EntityMimicube.class, biome -> biome == Biomes.SKY, EnumCreatureType.MONSTER, () -> AMConfig.mimicubeSpawnWeight, 1, 3, END, () -> AMConfig.mimicubeSpawnInEndCity));
        list.add(entry(EntitySoulVulture.class, biome -> biome == Biomes.HELL && AMNativeSpawnBiomes.soulVulture(biome), EnumCreatureType.MONSTER, () -> AMConfig.soulVultureSpawnWeight, 1, 1, NETHER, () -> AMConfig.soulVultureSpawnOnFossil));
        return Collections.unmodifiableList(list);
    }

    private static AMSpawnData entry(Class<? extends EntityLiving> entityClass, AMSpawnData.BiomeFilter biomeFilter,
                                     EnumCreatureType creatureType, IntSupplier weight, int minGroup, int maxGroup,
                                     int[] dimensions) {
        return new AMSpawnData(entityClass, biomeFilter, creatureType, weight, minGroup, maxGroup, dimensions);
    }

    private static AMSpawnData entry(Class<? extends EntityLiving> entityClass, AMSpawnData.BiomeFilter biomeFilter,
                                     EnumCreatureType creatureType, IntSupplier weight, int minGroup, int maxGroup,
                                     int[] dimensions, java.util.function.BooleanSupplier condition) {
        return new AMSpawnData(entityClass, biomeFilter, creatureType, weight, minGroup, maxGroup, dimensions, condition);
    }
}
