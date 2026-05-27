package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.Citadel;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.AnimationEvent;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.server.message.AnimationMessage;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.ArrayUtils;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

import java.lang.reflect.Field;
import java.util.Random;
import com.google.common.base.Predicate;

import java.util.function.BiPredicate;

/**
 * 1.16 used {@link net.minecraft.entity.EntityType}; 1.12 uses {@link EntityEntry} + {@link EntityEntryBuilder}.
 * <p>
 * Default attribute maps from 1.16 {@code EntityAttributeCreationEvent} must be reproduced in each
 * {@link net.minecraft.entity.EntityLiving#applyEntityAttributes()} when that entity is ported — see
 * {@code initializeAttributes} in the 1.16 sources.
 */
@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMEntityRegistry {

    /**
     * Replaces 1.16 {@code net.minecraft.entity.SpawnReason} for rolls that depended only on {@code SPAWNER} vs everything else.
     */
    public enum AMSpawnReason {
        MOB_SPAWNER,
        OTHER
    }

    private static int nextNetworkId;

    private static int nextNetworkId() {
        return nextNetworkId++;
    }

    /**
     * 1.12: {@link net.minecraft.entity.monster.EntityMob} and other parents already register some attributes in
     * {@code applyEntityAttributes}; calling {@code registerAttribute} twice throws and spams the log every spawn/GUI preview.
     */
    public static void registerAttributeIfAbsent(EntityLivingBase entity, IAttribute attribute) {
        if (entity.getEntityAttribute(attribute) == null) {
            entity.getAttributeMap().registerAttribute(attribute);
        }
    }

    private static EntityEntry reg(Class<? extends Entity> clazz, String name) {
        return reg(clazz, name, 80, 3, false);
    }

    private static EntityEntry reg(Class<? extends Entity> clazz, String name, int trackingRangeBlocks, int updateFrequency, boolean velocityUpdates) {
        return EntityEntryBuilder.create()
                .entity(clazz)
                .id(new ResourceLocation(AlexsMobs.MODID, name), nextNetworkId())
                .name(AlexsMobs.MODID + "." + name)
                .tracker(trackingRangeBlocks, updateFrequency, velocityUpdates)
                .build();
    }

    public static final EntityEntry GRIZZLY_BEAR = reg(EntityGrizzlyBear.class, "grizzly_bear");
    public static final EntityEntry ROADRUNNER = reg(EntityRoadrunner.class, "roadrunner");
    public static final EntityEntry BONE_SERPENT = reg(EntityBoneSerpent.class, "bone_serpent");
    public static final EntityEntry BONE_SERPENT_PART = reg(EntityBoneSerpentPart.class, "bone_serpent_part");
    public static final EntityEntry GAZELLE = reg(EntityGazelle.class, "gazelle");
    public static final EntityEntry CROCODILE = reg(EntityCrocodile.class, "crocodile");
    public static final EntityEntry FLY = reg(EntityFly.class, "fly");
    public static final EntityEntry HUMMINGBIRD = reg(EntityHummingbird.class, "hummingbird");
    public static final EntityEntry ORCA = reg(EntityOrca.class, "orca");
    public static final EntityEntry SUNBIRD = reg(EntitySunbird.class, "sunbird", 160, 1, true);
    public static final EntityEntry GORILLA = reg(EntityGorilla.class, "gorilla");
    public static final EntityEntry CRIMSON_MOSQUITO = reg(EntityCrimsonMosquito.class, "crimson_mosquito");
    public static final EntityEntry MOSQUITO_SPIT = reg(EntityMosquitoSpit.class, "mosquito_spit", 64, 2, true);
    public static final EntityEntry RATTLESNAKE = reg(EntityRattlesnake.class, "rattlesnake");
    public static final EntityEntry ENDERGRADE = reg(EntityEndergrade.class, "endergrade");
    public static final EntityEntry HAMMERHEAD_SHARK = reg(EntityHammerheadShark.class, "hammerhead_shark");
    public static final EntityEntry SHARK_TOOTH_ARROW = reg(EntitySharkToothArrow.class, "shark_tooth_arrow");
    public static final EntityEntry LOBSTER = reg(EntityLobster.class, "lobster");
    public static final EntityEntry KOMODO_DRAGON = reg(EntityKomodoDragon.class, "komodo_dragon");
    public static final EntityEntry CAPUCHIN_MONKEY = reg(EntityCapuchinMonkey.class, "capuchin_monkey");
    public static final EntityEntry TOSSED_ITEM = reg(EntityTossedItem.class, "tossed_item", 64, 2, true);
    public static final EntityEntry CENTIPEDE_HEAD = reg(EntityCentipedeHead.class, "centipede_head");
    public static final EntityEntry CENTIPEDE_BODY = reg(EntityCentipedeBody.class, "centipede_body");
    public static final EntityEntry CENTIPEDE_TAIL = reg(EntityCentipedeTail.class, "centipede_tail");
    public static final EntityEntry WARPED_TOAD = reg(EntityWarpedToad.class, "warped_toad", 80, 1, true);
    public static final EntityEntry MOOSE = reg(EntityMoose.class, "moose");
    public static final EntityEntry MIMICUBE = reg(EntityMimicube.class, "mimicube");
    public static final EntityEntry RACCOON = reg(EntityRaccoon.class, "raccoon");
    public static final EntityEntry BLOBFISH = reg(EntityBlobfish.class, "blobfish");
    public static final EntityEntry SEAL = reg(EntitySeal.class, "seal");
    public static final EntityEntry COCKROACH = reg(EntityCockroach.class, "cockroach");
    public static final EntityEntry COCKROACH_EGG = reg(EntityCockroachEgg.class, "cockroach_egg");
    public static final EntityEntry SHOEBILL = reg(EntityShoebill.class, "shoebill", 80, 1, false);
    public static final EntityEntry ELEPHANT = reg(EntityElephant.class, "elephant", 80, 1, false);
    public static final EntityEntry SOUL_VULTURE = reg(EntitySoulVulture.class, "soul_vulture", 80, 1, false);
    public static final EntityEntry SNOW_LEOPARD = reg(EntitySnowLeopard.class, "snow_leopard");
    public static final EntityEntry SPECTRE = reg(EntitySpectre.class, "spectre", 160, 1, true);
    public static final EntityEntry CROW = reg(EntityCrow.class, "crow");
    public static final EntityEntry ALLIGATOR_SNAPPING_TURTLE = reg(EntityAlligatorSnappingTurtle.class, "alligator_snapping_turtle");
    public static final EntityEntry MUNGUS = reg(EntityMungus.class, "mungus");
    public static final EntityEntry MANTIS_SHRIMP = reg(EntityMantisShrimp.class, "mantis_shrimp");
    public static final EntityEntry GUSTER = reg(EntityGuster.class, "guster");
    public static final EntityEntry SAND_SHOT = reg(EntitySandShot.class, "sand_shot", 64, 2, true);
    public static final EntityEntry GUST = reg(EntityGust.class, "gust");
    public static final EntityEntry WARPED_MOSCO = reg(EntityWarpedMosco.class, "warped_mosco");
    public static final EntityEntry HEMOLYMPH = reg(EntityHemolymph.class, "hemolymph", 64, 2, true);
    public static final EntityEntry STRADDLER = reg(EntityStraddler.class, "straddler");
    public static final EntityEntry STRADPOLE = reg(EntityStradpole.class, "stradpole");
    public static final EntityEntry STRADDLEBOARD = reg(EntityStraddleboard.class, "straddleboard");
    public static final EntityEntry EMU = reg(EntityEmu.class, "emu");
    public static final EntityEntry EMU_EGG = reg(EntityEmuEgg.class, "emu_egg");
    public static final EntityEntry PLATYPUS = reg(EntityPlatypus.class, "platypus");
    public static final EntityEntry DROPBEAR = reg(EntityDropBear.class, "dropbear");
    public static final EntityEntry TASMANIAN_DEVIL = reg(EntityTasmanianDevil.class, "tasmanian_devil");
    public static final EntityEntry KANGAROO = reg(EntityKangaroo.class, "kangaroo");
    public static final EntityEntry CACHALOT_WHALE = reg(EntityCachalotWhale.class, "cachalot_whale");
    public static final EntityEntry CACHALOT_PART = reg(EntityCachalotPart.class, "cachalot_part");
    public static final EntityEntry CACHALOT_ECHO = reg(EntityCachalotEcho.class, "cachalot_echo");
    public static final EntityEntry LEAFCUTTER_ANT = reg(EntityLeafcutterAnt.class, "leafcutter_ant");
    public static final EntityEntry ENDERIOPHAGE = reg(EntityEnderiophage.class, "enderiophage", 80, 1, false);
    public static final EntityEntry ENDERIOPHAGE_ROCKET = reg(EntityEnderiophageRocket.class, "enderiophage_rocket");
    public static final EntityEntry BALD_EAGLE = reg(EntityBaldEagle.class, "bald_eagle", 224, 1, false);
    public static final EntityEntry TIGER = reg(EntityTiger.class, "tiger");
    public static final EntityEntry TARANTULA_HAWK = reg(EntityTarantulaHawk.class, "tarantula_hawk");
    public static final EntityEntry VOID_WORM = reg(EntityVoidWorm.class, "void_worm", 320, 1, true);
    public static final EntityEntry VOID_WORM_PART = reg(EntityVoidWormPart.class, "void_worm_part", 320, 1, true);
    public static final EntityEntry VOID_WORM_SHOT = reg(EntityVoidWormShot.class, "void_worm_shot");
    public static final EntityEntry VOID_PORTAL = reg(EntityVoidPortal.class, "void_portal");
    public static final EntityEntry FRILLED_SHARK = reg(EntityFrilledShark.class, "frilled_shark");
    public static final EntityEntry MIMIC_OCTOPUS = reg(EntityMimicOctopus.class, "mimic_octopus");
    public static final EntityEntry SEAGULL = reg(EntitySeagull.class, "seagull");
    public static final EntityEntry BISON = reg(EntityBison.class, "bison");
    public static final EntityEntry RHINOCEROS = reg(EntityRhinoceros.class, "rhinoceros");
    public static final EntityEntry SKUNK = reg(EntitySkunk.class, "skunk");
    public static final EntityEntry BLUE_JAY = reg(EntityBlueJay.class, "blue_jay");
    public static final EntityEntry TUSKLIN = reg(EntityTusklin.class, "tusklin");
    public static final EntityEntry ANTEATER = reg(EntityAnteater.class, "anteater");
    public static final EntityEntry BANANA_SLUG = reg(EntityBananaSlug.class, "banana_slug");
    public static final EntityEntry LAVIATHAN = reg(EntityLaviathan.class, "laviathan");
    public static final EntityEntry LAVIATHAN_PART = reg(EntityLaviathanPart.class, "laviathan_part");
    public static final EntityEntry ANACONDA = reg(EntityAnaconda.class, "anaconda");
    public static final EntityEntry ANACONDA_PART = reg(EntityAnacondaPart.class, "anaconda_part", 80, 1, true);
    public static final EntityEntry VINE_LASSO = reg(EntityVineLasso.class, "vine_lasso", 80, 2, true);
    public static final EntityEntry GIANT_SQUID = reg(EntityGiantSquid.class, "giant_squid");
    public static final EntityEntry GIANT_SQUID_PART = reg(EntityGiantSquidPart.class, "giant_squid_part", 80, 1, true);
    public static final EntityEntry SQUID_GRAPPLE = reg(EntitySquidGrapple.class, "squid_grapple");
    public static final EntityEntry FART = reg(EntityFart.class, "fart", 64, 2, true);
    public static final EntityEntry JERBOA = reg(EntityJerboa.class, "jerboa");
    public static final EntityEntry RAIN_FROG = reg(EntityRainFrog.class, "rain_frog");
    public static final EntityEntry TRIOPS = reg(EntityTriops.class, "triops");
    public static final EntityEntry FLYING_FISH = reg(EntityFlyingFish.class, "flying_fish");
    public static final EntityEntry COMB_JELLY = reg(EntityCombJelly.class, "comb_jelly");
    public static final EntityEntry COSMIC_COD = reg(EntityCosmicCod.class, "cosmic_cod");
    public static final EntityEntry DEVILS_HOLE_PUPFISH = reg(EntityDevilsHolePupfish.class, "devils_hole_pupfish");
    public static final EntityEntry CATFISH = reg(EntityCatfish.class, "catfish");
    public static final EntityEntry TERRAPIN = reg(EntityTerrapin.class, "terrapin");
    public static final EntityEntry GELADA_MONKEY = reg(EntityGeladaMonkey.class, "gelada_monkey");
    public static final EntityEntry MANED_WOLF = reg(EntityManedWolf.class, "maned_wolf");
    public static final EntityEntry CAIMAN = reg(EntityCaiman.class, "caiman");
    public static final EntityEntry TOUCAN = reg(EntityToucan.class, "toucan");
    public static final EntityEntry POTOO = reg(EntityPotoo.class, "potoo");
    public static final EntityEntry SUGAR_GLIDER = reg(EntitySugarGlider.class, "sugar_glider");

    private static final EntityLiving.SpawnPlacementType AM_IN_LAVA_BONE_SERPENT = EnumHelper.addSpawnPlacementType(
            "AMALEX_BONE_SERPENT_LAVA", SpawnPredicateLogic.boneSerpentLava());
    private static final EntityLiving.SpawnPlacementType AM_IN_LAVA_STRADPOLE = EnumHelper.addSpawnPlacementType(
            "AMALEX_STRADPOLE_LAVA", SpawnPredicateLogic.stradpoleLava());
    private static final EntityLiving.SpawnPlacementType AM_NO_RESTRICTIONS_ALWAYS_TRUE = EnumHelper.addSpawnPlacementType(
            "AMALEX_NO_RESTRICTIONS_TRUE", SpawnPredicateLogic.alwaysTrue());

    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_ORCA = SpawnPredicateLogic.inWater("AMALEX_ORCA", SpawnPredicateLogic.orca());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_HHAMMER = SpawnPredicateLogic.inWater("AMALEX_HHAMMER", SpawnPredicateLogic.hammerheadShark());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_LOBSTER = SpawnPredicateLogic.inWater("AMALEX_LOBSTER", SpawnPredicateLogic.lobster());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_BLOBFISH = SpawnPredicateLogic.inWater("AMALEX_BLOBFISH", SpawnPredicateLogic.blobfish());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_MSHRIMP = SpawnPredicateLogic.inWater("AMALEX_MSHRIMP", SpawnPredicateLogic.mantisShrimp());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_CWHALE = SpawnPredicateLogic.inWater("AMALEX_CWHALE", SpawnPredicateLogic.cachalotWhale());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_FRILLED = SpawnPredicateLogic.inWater("AMALEX_FRILLED", SpawnPredicateLogic.frilledShark());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_GSQUID = SpawnPredicateLogic.inWater("AMALEX_GSQUID", SpawnPredicateLogic.giantSquid());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_MIMICO = SpawnPredicateLogic.inWater("AMALEX_MIMICO", SpawnPredicateLogic.mimicOctopus());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_TRIOPS = SpawnPredicateLogic.inWater("AMALEX_TRIOPS", SpawnPredicateLogic.triops());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_FLYING_FISH = SpawnPredicateLogic.inWater("AMALEX_FLYING_FISH", SpawnPredicateLogic.flyingFish());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_COMB_JELLY = SpawnPredicateLogic.inWater("AMALEX_COMB_JELLY", SpawnPredicateLogic.combJelly());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_PUPFISH = SpawnPredicateLogic.inWater("AMALEX_PUPFISH", SpawnPredicateLogic.devilsHolePupfish());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_CATFISH = SpawnPredicateLogic.inWater("AMALEX_CATFISH", SpawnPredicateLogic.catfish());
    private static final EntityLiving.SpawnPlacementType AM_IN_WATER_TERRAPIN = SpawnPredicateLogic.inWater("AMALEX_TERRAPIN", SpawnPredicateLogic.terrapin());
    private static final EntityLiving.SpawnPlacementType AM_COSMIC_COD = EnumHelper.addSpawnPlacementType("AMALEX_COSMIC_COD", SpawnPredicateLogic.cosmicCod());

    static {
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGrizzlyBear.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityRoadrunner.class, EnumHelper.addSpawnPlacementType("AMALEX_ROADRUNNER", SpawnPredicateLogic.roadrunner()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBoneSerpent.class, AM_IN_LAVA_BONE_SERPENT);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityLaviathan.class, AM_IN_LAVA_BONE_SERPENT);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGazelle.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCrocodile.class, EnumHelper.addSpawnPlacementType("AMALEX_CROC", SpawnPredicateLogic.crocodile()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityFly.class, EnumHelper.addSpawnPlacementType("AMALEX_FLY", SpawnPredicateLogic.fly()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityHummingbird.class, AM_NO_RESTRICTIONS_ALWAYS_TRUE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityOrca.class, AM_IN_WATER_ORCA);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySunbird.class, AM_NO_RESTRICTIONS_ALWAYS_TRUE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGorilla.class, EnumHelper.addSpawnPlacementType("AMALEX_GORILLA", SpawnPredicateLogic.gorilla()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCrimsonMosquito.class, EnumHelper.addSpawnPlacementType("AMALEX_CMOSQUITO", SpawnPredicateLogic.crimsonMosquito()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityRattlesnake.class, EnumHelper.addSpawnPlacementType("AMALEX_RATTLESNAKE", SpawnPredicateLogic.rattlesnake()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityEndergrade.class, EnumHelper.addSpawnPlacementType("AMALEX_ENDERGRADE", SpawnPredicateLogic.endergrade()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityHammerheadShark.class, AM_IN_WATER_HHAMMER);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityLobster.class, AM_IN_WATER_LOBSTER);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityKomodoDragon.class, EnumHelper.addSpawnPlacementType("AMALEX_KOMODO", SpawnPredicateLogic.komodoDragon()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCapuchinMonkey.class, EnumHelper.addSpawnPlacementType("AMALEX_CAPUCHIN", SpawnPredicateLogic.capuchinMonkey()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCentipedeHead.class, EnumHelper.addSpawnPlacementType("AMALEX_CENTIPEDE", SpawnPredicateLogic.centipedeHead()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityWarpedToad.class, EnumHelper.addSpawnPlacementType("AMALEX_WTOAD", SpawnPredicateLogic.warpedToad()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityMoose.class, EnumHelper.addSpawnPlacementType("AMALEX_MOOSE", SpawnPredicateLogic.moose()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityMimicube.class, EnumHelper.addSpawnPlacementType("AMALEX_MIMICUBE", SpawnPredicateLogic.mimicube()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityRaccoon.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBlobfish.class, AM_IN_WATER_BLOBFISH);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySeal.class, EnumHelper.addSpawnPlacementType("AMALEX_SEAL", SpawnPredicateLogic.seal()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCockroach.class, EnumHelper.addSpawnPlacementType("AMALEX_ROACH", SpawnPredicateLogic.cockroach()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityShoebill.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityElephant.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySoulVulture.class, EnumHelper.addSpawnPlacementType("AMALEX_SVULTURE", SpawnPredicateLogic.soulVulture()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySnowLeopard.class, EnumHelper.addSpawnPlacementType("AMALEX_SNOW_LEOPARD", SpawnPredicateLogic.snowLeopard()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySpectre.class, AM_NO_RESTRICTIONS_ALWAYS_TRUE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCrow.class, EnumHelper.addSpawnPlacementType("AMALEX_CROW", SpawnPredicateLogic.crow()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityAlligatorSnappingTurtle.class, EnumHelper.addSpawnPlacementType("AMALEX_ASTURTLE", SpawnPredicateLogic.alligatorSnappingTurtle()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityMungus.class, EnumHelper.addSpawnPlacementType("AMALEX_MUNGUS", SpawnPredicateLogic.mungus()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityMantisShrimp.class, AM_IN_WATER_MSHRIMP);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGuster.class, EnumHelper.addSpawnPlacementType("AMALEX_GUSTER", SpawnPredicateLogic.guster()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityWarpedMosco.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityStraddler.class, EnumHelper.addSpawnPlacementType("AMALEX_STRADDLER", SpawnPredicateLogic.straddler()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityStradpole.class, AM_IN_LAVA_STRADPOLE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityEmu.class, EnumHelper.addSpawnPlacementType("AMALEX_EMU", SpawnPredicateLogic.emu()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityPlatypus.class, EnumHelper.addSpawnPlacementType("AMALEX_PLATYPUS", SpawnPredicateLogic.platypus()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityDropBear.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTasmanianDevil.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityKangaroo.class, EnumHelper.addSpawnPlacementType("AMALEX_KANGAROO", SpawnPredicateLogic.kangaroo()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCachalotWhale.class, AM_IN_WATER_CWHALE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityLeafcutterAnt.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityEnderiophage.class, AM_NO_RESTRICTIONS_ALWAYS_TRUE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBaldEagle.class, EnumHelper.addSpawnPlacementType("AMALEX_EAGLE", SpawnPredicateLogic.baldEagle()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTiger.class, EnumHelper.addSpawnPlacementType("AMALEX_TIGER", SpawnPredicateLogic.tiger()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTarantulaHawk.class, EnumHelper.addSpawnPlacementType("AMALEX_THAWK", SpawnPredicateLogic.tarantulaHawk()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityVoidWorm.class, AM_NO_RESTRICTIONS_ALWAYS_TRUE);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityFrilledShark.class, AM_IN_WATER_FRILLED);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityMimicOctopus.class, AM_IN_WATER_MIMICO);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySeagull.class, EnumHelper.addSpawnPlacementType("AMALEX_SEAGULL", SpawnPredicateLogic.seagull()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBison.class, EnumHelper.addSpawnPlacementType("AMALEX_BISON", SpawnPredicateLogic.bison()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityRhinoceros.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBlueJay.class, EnumHelper.addSpawnPlacementType("AMALEX_BLUE_JAY", SpawnPredicateLogic.blueJay()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTusklin.class, EnumHelper.addSpawnPlacementType("AMALEX_TUSKLIN", SpawnPredicateLogic.tusklin()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityAnteater.class, EnumHelper.addSpawnPlacementType("AMALEX_ANTEATER", SpawnPredicateLogic.anteater()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityBananaSlug.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityAnaconda.class, EnumHelper.addSpawnPlacementType("AMALEX_ANACONDA", SpawnPredicateLogic.anaconda()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGiantSquid.class, AM_IN_WATER_GSQUID);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySkunk.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityJerboa.class, EnumHelper.addSpawnPlacementType("AMALEX_JERBOA", SpawnPredicateLogic.jerboa()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityRainFrog.class, EnumHelper.addSpawnPlacementType("AMALEX_RAIN_FROG", SpawnPredicateLogic.rainFrog()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTriops.class, AM_IN_WATER_TRIOPS);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityFlyingFish.class, AM_IN_WATER_FLYING_FISH);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCombJelly.class, AM_IN_WATER_COMB_JELLY);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCosmicCod.class, AM_COSMIC_COD);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityDevilsHolePupfish.class, AM_IN_WATER_PUPFISH);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCatfish.class, AM_IN_WATER_CATFISH);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityTerrapin.class, AM_IN_WATER_TERRAPIN);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityGeladaMonkey.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityManedWolf.class, EntityLiving.SpawnPlacementType.ON_GROUND);
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityCaiman.class, EnumHelper.addSpawnPlacementType("AMALEX_CAIMAN", SpawnPredicateLogic.caiman()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityToucan.class, EnumHelper.addSpawnPlacementType("AMALEX_TOUCAN", SpawnPredicateLogic.toucan()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntityPotoo.class, EnumHelper.addSpawnPlacementType("AMALEX_POTOO", SpawnPredicateLogic.potoo()));
        net.minecraft.entity.EntitySpawnPlacementRegistry.setPlacementType(EntitySugarGlider.class, EnumHelper.addSpawnPlacementType("AMALEX_SUGAR_GLIDER", SpawnPredicateLogic.sugarGlider()));
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityEntry> event) throws IllegalAccessException {
        for (Field f : AMEntityRegistry.class.getDeclaredFields()) {
            Object obj = f.get(null);
            if (obj instanceof EntityEntry) {
                event.getRegistry().register((EntityEntry) obj);
            }
        }
    }

    /**
     * 1.16 used {@code ITag<EntityType<?>>}; 1.12 uses the same JSON lists loaded into {@link AMTagRegistry#ENTITY_TYPE_TAGS}.
     */
    public static Predicate<EntityLivingBase> buildPredicateFromTag(ResourceLocation entityTagId) {
        if (entityTagId == null) {
            return Predicates.alwaysFalse();
        }
        return e -> e.isEntityAlive() && AMTagRegistry.entityMatchesEntityTypeTag(entityTagId, e);
    }

    public static Predicate<EntityLivingBase> buildPredicateFromTagTameable(ResourceLocation entityTagId, EntityLivingBase owner) {
        if (entityTagId == null) {
            return Predicates.alwaysFalse();
        }
        return e -> e.isEntityAlive() && AMTagRegistry.entityMatchesEntityTypeTag(entityTagId, e) && !owner.isOnSameTeam(e);
    }

    public static boolean rollSpawn(int rolls, Random random, AMSpawnReason reason) {
        if (reason == AMSpawnReason.MOB_SPAWNER) {
            return true;
        }
        return rolls <= 0 || random.nextInt(rolls) == 0;
    }

    /**
     * 1.16 {@code IWorldReader.checkNoEntityCollision(entity)}. Vanilla {@link EntityLiving#isNotColliding()}
     * rejects liquids and prevents all {@link net.minecraft.entity.EnumCreatureType#WATER_CREATURE} natural spawns.
     */
    public static boolean aquaticNoEntityCollision(Entity entity) {
        return entity.world.checkNoEntityCollision(entity.getEntityBoundingBox(), entity);
    }

    /**
     * 1:1 with Citadel {@code AnimationHandler#updateAnimations} — inlined so the integrated server never
     * hard-depends on {@code AnimationHandler} being resolvable from the Citadel jar classloader.
     */
    public static <T extends Entity & IAnimatedEntity> void updateAnimations(T entity) {
        com.github.alexthe666.citadel.animation.AnimationHandler.INSTANCE.updateAnimations(entity);
    }

    /**
     * Spawn placement predicates: 1.12 only exposes {@link BiPredicate}{@code <IBlockAccess, BlockPos>} (see {@link EntityLiving.SpawnPlacementType} / {@link EnumHelper#addSpawnPlacementType}).
     * Logic matches the 1.16 static {@code canXSpawn} helpers where possible without {@code SpawnReason}; spawner-only branches belong to mob processing / rolls instead.
     */
    private static final class SpawnPredicateLogic {

        private SpawnPredicateLogic() {
        }

        private static BiPredicate<IBlockAccess, BlockPos> alwaysTrue() {
            return (w, p) -> true;
        }

        private static World castWorld(IBlockAccess w) {
            return w instanceof World ? (World) w : null;
        }

        /** 1.12.2 {@link EntityLiving.SpawnPlacementType#IN_WATER} plus extra rules (1.16 {@code PlacementType.IN_WATER}). */
        private static EntityLiving.SpawnPlacementType inWater(String id, BiPredicate<IBlockAccess, BlockPos> extra) {
            return EnumHelper.addSpawnPlacementType(id, (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                if (!EntityLiving.SpawnPlacementType.IN_WATER.canSpawnAt(w, pos)) {
                    return false;
                }
                return extra.test(wa, pos);
            });
        }

        private static BiPredicate<IBlockAccess, BlockPos> roadrunner() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Block b = w.getBlockState(pos.down()).getBlock();
                return AMTagRegistry.blockInTag(AMTagRegistry.ROADRUNNER_SPAWNS, b) && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> boneSerpentLava() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
                while (w.getBlockState(m).getMaterial() == Material.LAVA) {
                    m.setPos(m.getX(), m.getY() + 1, m.getZ());
                }
                return w.getBlockState(m).getMaterial() == Material.AIR;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> crocodile() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Block b = w.getBlockState(pos.down()).getBlock();
                boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.CROCODILE_SPAWNS, b);
                return spawnBlock && pos.getY() < w.getSeaLevel() + 4;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> fly() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                if (!EntityLiving.SpawnPlacementType.ON_GROUND.canSpawnAt(w, pos)) {
                    return false;
                }
                Block down = w.getBlockState(pos.down()).getBlock();
                boolean sandOrDirt = down == Blocks.SAND || down == Blocks.DIRT || down == Blocks.GRASS;
                return pos.getY() > 63 && w.getLight(pos) > 8
                        && w.getLightFor(EnumSkyBlock.BLOCK, pos) == 0 && sandOrDirt;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> hummingbird() {
            return (wa, pos) -> jungleGroundOrTree(castWorld(wa), pos);
        }

        private static boolean shallowOpenWaterLayer(World w, BlockPos pos) {
            if (w == null || pos.getY() <= 45 || pos.getY() >= w.getSeaLevel()) {
                return false;
            }
            return w.getBlockState(pos).getMaterial() == Material.WATER;
        }

        private static BiPredicate<IBlockAccess, BlockPos> orca() {
            return (wa, pos) -> shallowOpenWaterLayer(castWorld(wa), pos);
        }

        private static BiPredicate<IBlockAccess, BlockPos> gorilla() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos down = pos.down();
                net.minecraft.block.Block block = w.getBlockState(down).getBlock();
                return (AMTagRegistry.blockInTag(AMTagRegistry.GORILLA_SPAWNS, block) || block == Blocks.AIR) && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> crimsonMosquito() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos down = pos.down();
                return w.getBlockState(down).isNormalCube() && w.getLight(pos) < 8;
            };
        }

        /**
         * Matches Gorilla / Capuchin / Hummingbird 1.16 spawn material checks (leaves, grass, logs, air) + light.
         */
        private static boolean jungleGroundOrTree(World w, BlockPos pos) {
            if (w == null) {
                return false;
            }
            net.minecraft.block.state.IBlockState st = w.getBlockState(pos.down());
            Material mat = st.getMaterial();
            Block b = st.getBlock();
            boolean ok = mat == Material.LEAVES || b == Blocks.GRASS || mat == Material.WOOD || b == Blocks.AIR;
            return ok && w.getLight(pos) > 8;
        }

        private static BiPredicate<IBlockAccess, BlockPos> rattlesnake() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                return AMTagRegistry.blockInTag(AMTagRegistry.RATTLESNAKE_SPAWNS, w.getBlockState(pos.down()).getBlock());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> endergrade() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && !w.getBlockState(pos.down()).getBlock().isAir(w.getBlockState(pos.down()), w, pos.down());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> hammerheadShark() {
            return (wa, pos) -> shallowOpenWaterLayer(castWorld(wa), pos);
        }

        private static BiPredicate<IBlockAccess, BlockPos> lobster() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && AMTagRegistry.blockInTag(AMTagRegistry.LOBSTER_SPAWNS, w.getBlockState(pos.down()).getBlock());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> komodoDragon() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && AMTagRegistry.blockInTag(AMTagRegistry.KOMODO_DRAGON_SPAWNS, w.getBlockState(pos.down()).getBlock());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> capuchinMonkey() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                net.minecraft.block.state.IBlockState st = w.getBlockState(pos.down());
                return (AMTagRegistry.blockInTag(AMTagRegistry.CAPUCHIN_MONKEY_SPAWNS, st.getBlock()) || st.getBlock() == Blocks.AIR) && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> anteater() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> centipedeHead() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && !w.canSeeSky(pos) && pos.getY() <= AMConfig.caveCentipedeSpawnHeight && w.getLight(pos) < 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> warpedToad() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos feet = pos.down();
                net.minecraft.block.state.IBlockState below = w.getBlockState(feet);
                return below.getMaterial() == Material.LAVA || below.isNormalCube();
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> moose() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                net.minecraft.block.state.IBlockState st = w.getBlockState(pos.down());
                Block b = st.getBlock();
                return (b == Blocks.GRASS || b == Blocks.SNOW || b == Blocks.SNOW_LAYER)
                        || b == Blocks.SNOW && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> mimicube() {
            return (wa, pos) -> EntityLiving.SpawnPlacementType.ON_GROUND.canSpawnAt(castWorld(wa), pos);
        }

        private static BiPredicate<IBlockAccess, BlockPos> blobfish() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                return pos.getY() <= AMConfig.blobfishSpawnHeight
                        && w.getBlockState(pos).getMaterial() == Material.WATER
                        && w.getBlockState(pos.up()).getMaterial() == Material.WATER;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> seal() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Biome biome = w.getBiome(pos);
                ResourceLocation id = biome.getRegistryName();
                boolean frozenOcean = id != null && "minecraft:frozen_ocean".equals(id.toString());
                if (!frozenOcean) {
                    boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.SEAL_SPAWNS, w.getBlockState(pos.down()).getBlock());
                    return spawnBlock && w.getLight(pos) > 8;
                }
                return w.getLight(pos) > 8 && w.getBlockState(pos.down()).getBlock() == Blocks.ICE;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> cockroach() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                return EntityCockroach.canCockroachSpawn(w, pos, w.rand);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> soulVulture() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && AMTagRegistry.blockInTag(AMTagRegistry.SOUL_VULTURE_SPAWNS, w.getBlockState(pos.down()).getBlock());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> alligatorSnappingTurtle() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null
                        && AMTagRegistry.blockInTag(AMTagRegistry.ALLIGATOR_SNAPPING_TURTLE_SPAWNS, w.getBlockState(pos.down()).getBlock())
                        && pos.getY() < w.getSeaLevel() + 4;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> mungus() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getBlockState(pos.down()).isNormalCube();
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> mantisShrimp() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos downPos = new BlockPos(pos);
                while (downPos.getY() > 1 && w.getBlockState(downPos).getMaterial() == Material.WATER) {
                    downPos = downPos.down();
                }
                boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.MANTIS_SHRIMP_SPAWNS, w.getBlockState(downPos).getBlock());
                return spawnBlock && downPos.getY() < w.getSeaLevel() + 1;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> guster() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Block down = w.getBlockState(pos.down()).getBlock();
                boolean sandOrSoul = down == Blocks.SAND || down == Blocks.SOUL_SAND;
                boolean weatherOk = !AMConfig.limitGusterSpawnsToWeather || w.getWorldInfo().isThundering() || w.getWorldInfo().isRaining() || w.provider.getDimension() == -1;
                return sandOrSoul && weatherOk;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> straddler() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getBlockState(pos.down()).getBlock() == Blocks.NETHERRACK;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> stradpoleLava() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                if (w.getBlockState(pos).getMaterial() == Material.LAVA && w.getBlockState(pos.down()).getMaterial() != Material.LAVA) {
                    return w.getBlockState(pos.up()).getMaterial() == Material.AIR;
                }
                return false;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> emu() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && AMTagRegistry.blockInTag(AMTagRegistry.EMU_SPAWNS, w.getBlockState(pos.down()).getBlock())
                        && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> platypus() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.PLATYPUS_SPAWNS, w.getBlockState(pos.down()).getBlock());
                java.util.Set<Block> tagSet = AMTagRegistry.BLOCK_TAG_SETS.get(AMTagRegistry.PLATYPUS_SPAWNS);
                boolean tagUnsetOrEmpty = tagSet == null || tagSet.isEmpty();
                boolean dirtOrTag = (tagUnsetOrEmpty && w.getBlockState(pos.down()).getBlock() == Blocks.DIRT) || spawnBlock;
                return dirtOrTag && pos.getY() < w.getSeaLevel() + 4;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> kangaroo() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && AMTagRegistry.blockInTag(AMTagRegistry.KANGAROO_SPAWNS, w.getBlockState(pos.down()).getBlock());
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> cachalotWhale() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getBlockState(pos).getMaterial() == Material.WATER;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> baldEagle() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> tiger() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && w.getLight(pos) > 8;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> tarantulaHawk() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                boolean sand = w.getBlockState(pos.down()).getBlock() == Blocks.SAND;
                boolean sandAndLight = sand && w.getLight(pos) > 8;
                boolean nether = w.provider.getDimension() == -1;
                return sandAndLight || nether || AMConfig.fireproofTarantulaHawk;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> frilledShark() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null
                        && w.getBlockState(pos).getMaterial() == Material.WATER
                        && w.getBlockState(pos.up()).getMaterial() == Material.WATER;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> mimicOctopus() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                BlockPos downPos = new BlockPos(pos);
                while (downPos.getY() > 1 && w.getBlockState(downPos).getMaterial() == Material.WATER) {
                    downPos = downPos.down();
                }
                boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.MIMIC_OCTOPUS_SPAWNS, w.getBlockState(downPos).getBlock());
                return spawnBlock && downPos.getY() < w.getSeaLevel() + 1;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> giantSquid() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                return w.getBlockState(pos).getMaterial() == Material.WATER && w.getBlockState(pos.up()).getMaterial() == Material.WATER;
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> anaconda() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityAnaconda.canAnacondaSpawn(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> seagull() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Material below = w.getBlockState(pos.down()).getMaterial();
                boolean fluidEmptyBelow = below != Material.WATER && below != Material.LAVA;
                return w.getLight(pos) > 8 && fluidEmptyBelow;
            };
        }

        /** Matches {@link EntitySnowLeopard#getCanSpawnHere()} / 1.16 {@code canSnowLeopardSpawn}. */
        private static BiPredicate<IBlockAccess, BlockPos> snowLeopard() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Block block = w.getBlockState(pos.down()).getBlock();
                boolean validGround = block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS;
                return validGround && w.getLight(pos) > 8;
            };
        }

        /** Matches 1.16 {@code EntityCrow#canCrowSpawn}. */
        private static BiPredicate<IBlockAccess, BlockPos> crow() {
            return (wa, pos) -> jungleGroundOrTree(castWorld(wa), pos);
        }

        /** Matches 1.20 {@code EntityBlueJay#checkSpawnObstruction} on leaves/logs/grass. */
        private static BiPredicate<IBlockAccess, BlockPos> blueJay() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityBlueJay.canBlueJaySpawnAt(w, pos);
            };
        }

        /** Matches 1.16 {@code EntityBison#checkAnimalSpawnRules} / plains ground. */
        private static BiPredicate<IBlockAccess, BlockPos> bison() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                Block b = w.getBlockState(pos.down()).getBlock();
                return (b == Blocks.GRASS || b == Blocks.DIRT) && w.getLight(pos) > 8;
            };
        }

        /** Matches 1.16 {@code EntityTusklin#canTusklinSpawn}. */
        private static BiPredicate<IBlockAccess, BlockPos> tusklin() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null) {
                    return false;
                }
                return EntityTusklin.canTusklinSpawn(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> jerboa() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityJerboa.canJerboaSpawn(w, pos, w.rand);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> rainFrog() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityRainFrog.canRainFrogSpawn(w, pos, w.rand);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> triops() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null || !EntityLiving.SpawnPlacementType.IN_WATER.canSpawnAt(w, pos)) {
                    return false;
                }
                return com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes.triops(w.getBiome(pos));
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> terrapin() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null || !EntityLiving.SpawnPlacementType.IN_WATER.canSpawnAt(w, pos)) {
                    return false;
                }
                return com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes.terrapin(w.getBiome(pos));
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> caiman() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityCaiman.canCaimanSpawn(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> combJelly() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null || !EntityLiving.SpawnPlacementType.IN_WATER.canSpawnAt(w, pos)) {
                    return false;
                }
                return com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes.combJelly(w.getBiome(pos))
                        && EntityCombJelly.canCombJellySpawn(w, pos, w.rand);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> flyingFish() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                if (w == null || !EntityLiving.SpawnPlacementType.IN_WATER.canSpawnAt(w, pos)) {
                    return false;
                }
                return com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes.flyingFish(w.getBiome(pos));
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> toucan() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityToucan.canToucanSpawnAt(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> potoo() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityPotoo.canPotooSpawnAt(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> sugarGlider() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntitySugarGlider.canSugarGliderSpawnAt(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> cosmicCod() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityCosmicCod.canSpawnAt(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> devilsHolePupfish() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityDevilsHolePupfish.canPupfishSpawn(w, pos);
            };
        }

        private static BiPredicate<IBlockAccess, BlockPos> catfish() {
            return (wa, pos) -> {
                World w = castWorld(wa);
                return w != null && EntityCatfish.canCatfishSpawn(w, pos);
            };
        }
    }
}
