package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.entity.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraftforge.oredict.OreDictionary;
import com.github.alexthe666.citadel.server.item.CustomArmorMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundEvent;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMItemRegistry {
    public static CustomArmorMaterial ROADRUNNER_ARMOR_MATERIAL = new AMArmorMaterial("roadrunner", 18, new int[]{3, 3, 3, 3}, 20, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0);
    public static CustomArmorMaterial CROCODILE_ARMOR_MATERIAL = new AMArmorMaterial("crocodile", 22, new int[]{2, 5, 7, 3}, 25, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1);
    public static CustomArmorMaterial CENTIPEDE_ARMOR_MATERIAL = new AMArmorMaterial("centipede", 20, new int[]{6, 6, 6, 6}, 22, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.5F);
    public static CustomArmorMaterial MOOSE_ARMOR_MATERIAL = new AMArmorMaterial("moose", 19, new int[]{5, 5, 5, 5}, 21, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.5F);
    public static CustomArmorMaterial RACCOON_ARMOR_MATERIAL = new AMArmorMaterial("raccoon", 17, new int[]{3, 3, 3, 3}, 21, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2.5F);
    public static CustomArmorMaterial SOMBRERO_ARMOR_MATERIAL = new AMArmorMaterial("sombrero", 14, new int[]{2, 2, 2, 2}, 30, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static CustomArmorMaterial SPIKED_TURTLE_SHELL_ARMOR_MATERIAL = new AMArmorMaterial("spiked_turtle_shell", 35, new int[]{3, 3, 3, 3}, 30, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1F, 0.2F);
    public static CustomArmorMaterial FEDORA_ARMOR_MATERIAL = new AMArmorMaterial("fedora", 10, new int[]{2, 2, 2, 2}, 30, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static CustomArmorMaterial EMU_ARMOR_MATERIAL = new AMArmorMaterial("emu", 9, new int[]{4, 4, 4, 4}, 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static final Item TARANTULA_HAWK_ELYTRA = new ItemTarantulaHawkElytra().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:tarantula_hawk_elytra");

    /** 1.12 has no composter block; chances preserved for mod-compat / future blocks (same values as 1.16 {@code ComposterBlock#CHANCES}). */
    public static final Map<Item, Float> COMPOST_CHANCES = new HashMap<>();

    public static final Item TAB_ICON = AlexsMobs.PROXY.setupISTER(new ItemTabIcon()).setRegistryName("alexsmobs:tab_icon");
    public static final Item ANIMAL_DICTIONARY = new ItemAnimalDictionary().setRegistryName("alexsmobs:animal_dictionary");
    public static final Item BEAR_FUR = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:bear_fur");
    public static final Item ROADRUNNER_FEATHER = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:roadrunner_feather");
    public static final Item ROADDRUNNER_BOOTS = new ItemModArmor(ROADRUNNER_ARMOR_MATERIAL, EntityEquipmentSlot.FEET).setRegistryName("alexsmobs:roadrunner_boots");
    public static final Item LAVA_BOTTLE = new Item().setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:lava_bottle");
    public static final Item BONE_SERPENT_TOOTH = fireImmuneItem().setRegistryName("alexsmobs:bone_serpent_tooth");
    public static final Item GAZELLE_HORN = fireImmuneItem().setRegistryName("alexsmobs:gazelle_horn");
    public static final Item CROCODILE_SCUTE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:crocodile_scute");
    public static final Item CROCODILE_CHESTPLATE = new ItemModArmor(CROCODILE_ARMOR_MATERIAL, EntityEquipmentSlot.CHEST).setRegistryName("alexsmobs:crocodile_chestplate");
    public static final Item MAGGOT = new ItemFood(1, 0.2F, false).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:maggot");
    public static final Item BANANA = new ItemFood(4, 0.3F, false).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:banana");
    public static final Item ANCIENT_DART = new Item() {
        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.UNCOMMON;
        }
    }.setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:ancient_dart");
    public static final Item HALO = new ItemAMInternal().setRegistryName("alexsmobs:halo");
    public static final Item BLOOD_SAC = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:blood_sac");
    public static final Item MOSQUITO_PROBOSCIS = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:mosquito_proboscis");
    public static final Item BLOOD_SPRAYER = new ItemBloodSprayer().setRegistryName("alexsmobs:blood_sprayer");
    public static final Item RATTLESNAKE_RATTLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:rattlesnake_rattle");
    public static final Item CHORUS_ON_A_STICK = new Item().setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:chorus_on_a_stick");
    public static final Item SHARK_TOOTH = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:shark_tooth");
    public static final Item SHARK_TOOTH_ARROW = new ItemModArrow().setRegistryName("alexsmobs:shark_tooth_arrow");
    public static final Item LOBSTER_TAIL = new ItemFood(2, 0.4F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:lobster_tail");
    public static final Item COOKED_LOBSTER_TAIL = new ItemFood(6, 0.65F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cooked_lobster_tail");
    public static final Item LOBSTER_BUCKET = new ItemModFishBucket(AMEntityRegistry.LOBSTER, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:lobster_bucket");
    public static final Item TRIOPS_BUCKET = new ItemModFishBucket(AMEntityRegistry.TRIOPS, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:triops_bucket");
    public static final Item TERRAPIN_BUCKET = new ItemModFishBucket(AMEntityRegistry.TERRAPIN, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:terrapin_bucket");
    public static final Item COMB_JELLY_BUCKET = new ItemModFishBucket(AMEntityRegistry.COMB_JELLY, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:comb_jelly_bucket");
    public static final Item COSMIC_COD_BUCKET = new ItemModFishBucket(AMEntityRegistry.COSMIC_COD, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:cosmic_cod_bucket");
    public static final Item PUPFISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.DEVILS_HOLE_PUPFISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:devils_hole_pupfish_bucket");
    public static final Item SMALL_CATFISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.CATFISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:small_catfish_bucket");
    public static final Item MEDIUM_CATFISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.CATFISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:medium_catfish_bucket");
    public static final Item LARGE_CATFISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.CATFISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:large_catfish_bucket");
    public static final Item RAW_CATFISH = new ItemFood(2, 0.4F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:raw_catfish");
    public static final Item COOKED_CATFISH = new ItemFood(6, 0.65F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cooked_catfish");
    public static final Item COSMIC_COD = new ItemFood(3, 0.4F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cosmic_cod");
    public static final Item KOMODO_SPIT = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:komodo_spit");
    public static final Item KOMODO_SPIT_BOTTLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:komodo_spit_bottle");
    public static final Item POISON_BOTTLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:poison_bottle");
    public static final Item SOPA_DE_MACACO = new ItemSoup(5).setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:sopa_de_macaco");
    public static final Item CENTIPEDE_LEG = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:centipede_leg");
    public static final Item CENTIPEDE_LEGGINGS = new ItemModArmor(CENTIPEDE_ARMOR_MATERIAL, EntityEquipmentSlot.LEGS).setRegistryName("alexsmobs:centipede_leggings");
    public static final Item MOSQUITO_LARVA = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:mosquito_larva");
    public static final Item MOOSE_ANTLER = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:moose_antler");
    public static final Item BISON_FUR = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:bison_fur");
    public static final Item STINK_BOTTLE = new ItemStinkBottle(AMBlockRegistry.SKUNK_SPRAY).setRegistryName("alexsmobs:stink_bottle");
    public static final Item STINK_RAY = new ItemStinkRay().setRegistryName("alexsmobs:stink_ray");
    public static final Item PIGSHOES = new ItemPigshoes().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:pigshoes");
    public static final Item MOOSE_HEADGEAR = new ItemModArmor(MOOSE_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD).setRegistryName("alexsmobs:moose_headgear");
    public static final Item MOOSE_RIBS = new ItemFood(3, 0.6F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:moose_ribs");
    public static final Item COOKED_MOOSE_RIBS = new ItemFood(7, 0.85F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cooked_moose_ribs");
    public static final Item MIMICREAM = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:mimicream");
    public static final Item RACCOON_TAIL = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:raccoon_tail");
    public static final Item FRONTIER_CAP = new ItemModArmor(RACCOON_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD).setRegistryName("alexsmobs:frontier_cap");
    public static final Item BLOBFISH = new ItemFood(3, 0.4F, true).setPotionEffect(new PotionEffect(MobEffects.POISON, 120, 0), 1.0F).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:blobfish");
    public static final Item BLOBFISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.BLOBFISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:blobfish_bucket");
    public static final Item FISH_OIL = new ItemFishOil().setRegistryName("alexsmobs:fish_oil");
    public static final Item MARACA = new ItemMaraca().setRegistryName("alexsmobs:maraca");
    public static final Item SOMBRERO = new ItemModArmor(SOMBRERO_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD).setRegistryName("alexsmobs:sombrero");
    public static final Item COCKROACH_WING_FRAGMENT = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cockroach_wing_fragment");
    public static final Item COCKROACH_WING = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cockroach_wing");
    public static final Item COCKROACH_OOTHECA = new ItemAnimalEgg().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cockroach_ootheca");
    public static final Item ACACIA_BLOSSOM = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:acacia_blossom");
    public static final Item SOUL_HEART = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:soul_heart");
    public static final Item SPIKED_SCUTE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:spiked_scute");
    public static final Item SPIKED_TURTLE_SHELL = new ItemModArmor(SPIKED_TURTLE_SHELL_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD).setRegistryName("alexsmobs:spiked_turtle_shell");
    public static final Item SHRIMP_FRIED_RICE = new ItemFood(12, 1.0F, false).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:shrimp_fried_rice");
    public static final Item GUSTER_EYE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:guster_eye");
    public static final Item POCKET_SAND = new ItemPocketSand().setRegistryName("alexsmobs:pocket_sand");
    public static final Item WARPED_MUSCLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:warped_muscle");
    public static final Item HEMOLYMPH_SAC = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:hemolymph_sac");
    public static final Item HEMOLYMPH_BLASTER = new ItemHemolymphBlaster().setRegistryName("alexsmobs:hemolymph_blaster");
    public static final Item WARPED_MIXTURE = new Item() {
        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.RARE;
        }
    }.setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:warped_mixture");
    public static final Item STRADDLITE = fireImmuneItem().setRegistryName("alexsmobs:straddlite");
    public static final Item STRADPOLE_BUCKET = new ItemModFishBucket(AMEntityRegistry.STRADPOLE, ItemModFishBucket.FluidKind.LAVA).setRegistryName("alexsmobs:stradpole_bucket");
    public static final Item STRADDLEBOARD = new ItemStraddleboard().setRegistryName("alexsmobs:straddleboard");
    public static final Item STRADDLE_HELMET = fireImmuneItem().setRegistryName("alexsmobs:straddle_helmet");
    public static final Item STRADDLE_SADDLE = fireImmuneItem().setRegistryName("alexsmobs:straddle_saddle");
    public static final Item EMU_EGG = new ItemAnimalEgg().setCreativeTab(AlexsMobs.TAB).setMaxStackSize(8).setRegistryName("alexsmobs:emu_egg");
    public static final Item BOILED_EMU_EGG = new ItemFood(4, 1.0F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:boiled_emu_egg");
    public static final Item EMU_FEATHER = fireImmuneItem().setRegistryName("alexsmobs:emu_feather");
    public static final Item EMU_LEGGINGS = new ItemModArmor(EMU_ARMOR_MATERIAL, EntityEquipmentSlot.LEGS).setRegistryName("alexsmobs:emu_leggings");
    public static final Item PLATYPUS_BUCKET = new ItemModFishBucket(AMEntityRegistry.PLATYPUS, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:platypus_bucket");
    public static final Item FEDORA = new ItemModArmor(FEDORA_ARMOR_MATERIAL, EntityEquipmentSlot.HEAD).setRegistryName("alexsmobs:fedora");
    public static final Item DROPBEAR_CLAW = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:dropbear_claw");
    public static final Item KANGAROO_MEAT = new ItemFood(4, 0.6F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:kangaroo_meat");
    public static final Item COOKED_KANGAROO_MEAT = new ItemFood(8, 0.85F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cooked_kangaroo_meat");
    public static final Item KANGAROO_HIDE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:kangaroo_hide");
    public static final Item KANGAROO_BURGER = new ItemFood(12, 1.0F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:kangaroo_burger");
    public static final Item AMBERGRIS = new ItemFuel(12800).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:ambergris");
    public static final Item CACHALOT_WHALE_TOOTH = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cachalot_whale_tooth");
    public static final Item ECHOLOCATOR = new ItemEcholocator(false).setRegistryName("alexsmobs:echolocator");
    public static final Item ENDOLOCATOR = new ItemEcholocator(true).setRegistryName("alexsmobs:endolocator");
    public static final Item GONGYLIDIA = new ItemFood(3, 1.2F, false).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:gongylidia");
    public static final Item LEAFCUTTER_ANT_PUPA = new ItemLeafcutterPupa().setRegistryName("alexsmobs:leafcutter_ant_pupa");
    public static final Item ENDERIOPHAGE_ROCKET = new ItemEnderiophageRocket().setRegistryName("alexsmobs:enderiophage_rocket");
    public static final Item FALCONRY_GLOVE_INVENTORY = new ItemAMInternal().setRegistryName("alexsmobs:falconry_glove_inventory");
    public static final Item FALCONRY_GLOVE_HAND = new ItemAMInternal().setRegistryName("alexsmobs:falconry_glove_hand");
    public static final Item FALCONRY_GLOVE = new ItemFalconryGlove().setRegistryName("alexsmobs:falconry_glove");
    public static final Item FALCONRY_HOOD = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:falconry_hood");
    public static final Item TARANTULA_HAWK_WING_FRAGMENT = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:tarantula_hawk_wing_fragment");
    public static final Item TARANTULA_HAWK_WING = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:tarantula_hawk_wing");
    public static final Item MYSTERIOUS_WORM = AlexsMobs.PROXY.setupISTER(new ItemMysteriousWorm()).setRegistryName("alexsmobs:mysterious_worm");
    public static final Item VOID_WORM_MANDIBLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:void_worm_mandible");
    public static final Item VOID_WORM_EYE = new Item() {
        @Override
        public EnumRarity getRarity(ItemStack stack) {
            return EnumRarity.RARE;
        }
    }.setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:void_worm_eye");
    public static final Item DIMENSIONAL_CARVER = new ItemDimensionalCarver().setRegistryName("alexsmobs:dimensional_carver");
    public static final Item SERRATED_SHARK_TOOTH = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:serrated_shark_tooth");
    public static final Item FRILLED_SHARK_BUCKET = new ItemModFishBucket(AMEntityRegistry.FRILLED_SHARK, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:frilled_shark_bucket");
    public static final Item SHIELD_OF_THE_DEEP = AlexsMobs.PROXY.setupISTER(new ItemShieldOfTheDeep() {
        @Override
        public net.minecraft.item.EnumRarity getRarity(ItemStack stack) {
            return net.minecraft.item.EnumRarity.UNCOMMON;
        }
    }).setRegistryName("alexsmobs:shield_of_the_deep");
    public static final Item MIMIC_OCTOPUS_BUCKET = new ItemModFishBucket(AMEntityRegistry.MIMIC_OCTOPUS, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:mimic_octopus_bucket");
    public static final Item BANANA_SLUG_SLIME = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:banana_slug_slime");
    public static final Item SHED_SNAKE_SKIN = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:shed_snake_skin");
    public static final Item VINE_LASSO_INVENTORY = new ItemAMInternal().setRegistryName("alexsmobs:vine_lasso_inventory");
    public static final Item VINE_LASSO_HAND = new ItemAMInternal().setRegistryName("alexsmobs:vine_lasso_hand");
    public static final Item VINE_LASSO = new ItemVineLasso().setRegistryName("alexsmobs:vine_lasso");
    public static final Item LOST_TENTACLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:lost_tentacle");
    public static final Item SQUID_GRAPPLE = new ItemSquidGrapple().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:squid_grapple");

    public static final Item MUSIC_DISC_THIME = registerDisc("music_disc_thime", AMSoundRegistry.MUSIC_DISC_THIME);
    public static final Item MUSIC_DISC_DAZE = registerDisc("music_disc_daze", AMSoundRegistry.MUSIC_DISC_DAZE);

    private static Item fireImmuneItem() {
        return new Item().setCreativeTab(AlexsMobs.TAB);
    }

    private static Item registerDisc(String name, SoundEvent sound) {
        // ItemRecord tooltip uses item.record.<this string>.desc (1.12), not the namespaced sound id.
        return new ItemRecord(name, sound) {
            @Override
            public EnumRarity getRarity(ItemStack stack) {
                return EnumRarity.RARE;
            }
        }.setCreativeTab(AlexsMobs.TAB).setMaxStackSize(1).setRegistryName("alexsmobs:" + name);
    }

    private static void registerItem(RegistryEvent.Register<Item> event, Item item) {
        event.getRegistry().register(item);
        AlexsMobs.applyUnlocalizedNameFromRegistry(item);
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.GRIZZLY_BEAR, 0X693A2C, 0X976144).setRegistryName("alexsmobs:spawn_egg_grizzly_bear"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ROADRUNNER, 0X3A2E26, 0XFBE9CE).setRegistryName("alexsmobs:spawn_egg_roadrunner"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BONE_SERPENT, 0XE5D9C4, 0XFF6038).setRegistryName("alexsmobs:spawn_egg_bone_serpent"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.GAZELLE, 0XDDA675, 0X2C2925).setRegistryName("alexsmobs:spawn_egg_gazelle"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CROCODILE, 0X738940, 0XA6A15E).setRegistryName("alexsmobs:spawn_egg_crocodile"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.FLY, 0X464241, 0X892E2E).setRegistryName("alexsmobs:spawn_egg_fly"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.HUMMINGBIRD, 0X325E7F, 0X44A75F).setRegistryName("alexsmobs:spawn_egg_hummingbird"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ORCA, 0X2C2C2C, 0XD6D8E4).setRegistryName("alexsmobs:spawn_egg_orca"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SUNBIRD, 0XF6694F, 0XFFDDA0).setRegistryName("alexsmobs:spawn_egg_sunbird"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.GORILLA, 0X595B5D, 0X1C1C21).setRegistryName("alexsmobs:spawn_egg_gorilla"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CRIMSON_MOSQUITO, 0X53403F, 0XC11A1A).setRegistryName("alexsmobs:spawn_egg_crimson_mosquito"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.RATTLESNAKE, 0XCEB994, 0X937A5B).setRegistryName("alexsmobs:spawn_egg_rattlesnake"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ENDERGRADE, 0XE6E6A4, 0XB29BDD).setRegistryName("alexsmobs:spawn_egg_endergrade"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.HAMMERHEAD_SHARK, 0X8A92B5, 0XB9BED8).setRegistryName("alexsmobs:spawn_egg_hammerhead_shark"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.LOBSTER, 0XC43123, 0XDD5F38).setRegistryName("alexsmobs:spawn_egg_lobster"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.KOMODO_DRAGON, 0X746C4F, 0X564231).setRegistryName("alexsmobs:spawn_egg_komodo_dragon"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CAPUCHIN_MONKEY, 0X25211F, 0XF1DAB3).setRegistryName("alexsmobs:spawn_egg_capuchin_monkey"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CENTIPEDE_HEAD, 0X342B2E, 0X733449).setRegistryName("alexsmobs:spawn_egg_centipede"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.WARPED_TOAD, 0X1F968E, 0XFEAC6D).setRegistryName("alexsmobs:spawn_egg_warped_toad"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.MOOSE, 0X36302A, 0XD4B183).setRegistryName("alexsmobs:spawn_egg_moose"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.MIMICUBE, 0X8A80C1, 0X5E4F6F).setRegistryName("alexsmobs:spawn_egg_mimicube"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.RACCOON, 0X85827E, 0X2A2726).setRegistryName("alexsmobs:spawn_egg_raccoon"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BLOBFISH, 0XDBC6BD, 0X9E7A7F).setRegistryName("alexsmobs:spawn_egg_blobfish"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SEAL, 0X483C32, 0X66594C).setRegistryName("alexsmobs:spawn_egg_seal"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.COCKROACH, 0X0D0909, 0X42241E).setRegistryName("alexsmobs:spawn_egg_cockroach"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SHOEBILL, 0X828282, 0XD5B48A).setRegistryName("alexsmobs:spawn_egg_shoebill"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ELEPHANT, 0X8D8987, 0XEDE5D1).setRegistryName("alexsmobs:spawn_egg_elephant"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SOUL_VULTURE, 0X23262D, 0X57F4FF).setRegistryName("alexsmobs:spawn_egg_soul_vulture"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SNOW_LEOPARD, 0XACA293, 0X26201D).setRegistryName("alexsmobs:spawn_egg_snow_leopard"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SPECTRE, 0XC8D0EF, 0X8791EF).setRegistryName("alexsmobs:spawn_egg_spectre"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CROW, 0X0D111C, 0X1C2030).setRegistryName("alexsmobs:spawn_egg_crow"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BLUE_JAY, 0X5FB7FE, 0X293B42).setRegistryName("alexsmobs:spawn_egg_blue_jay"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE, 0X6C5C52, 0X456926).setRegistryName("alexsmobs:spawn_egg_alligator_snapping_turtle"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.MUNGUS, 0X836A8D, 0X45454C).setRegistryName("alexsmobs:spawn_egg_mungus"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.MANTIS_SHRIMP, 0XDB4858, 0X15991E).setRegistryName("alexsmobs:spawn_egg_mantis_shrimp"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.GUSTER, 0XF8D49A, 0XFF720A).setRegistryName("alexsmobs:spawn_egg_guster"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.WARPED_MOSCO, 0X322F58, 0X5B5EF1).setRegistryName("alexsmobs:spawn_egg_warped_mosco"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.STRADDLER, 0X5D5F6E, 0XCDA886).setRegistryName("alexsmobs:spawn_egg_straddler"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.STRADPOLE, 0X5D5F6E, 0X576A8B).setRegistryName("alexsmobs:spawn_egg_stradpole"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.EMU, 0X665346, 0X3B3938).setRegistryName("alexsmobs:spawn_egg_emu"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.PLATYPUS, 0X7D503E, 0X363B43).setRegistryName("alexsmobs:spawn_egg_platypus"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.DROPBEAR, 0X8A2D35, 0X60A3A3).setRegistryName("alexsmobs:spawn_egg_dropbear"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.TASMANIAN_DEVIL, 0X252426, 0XA8B4BF).setRegistryName("alexsmobs:spawn_egg_tasmanian_devil"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.KANGAROO, 0XCE9D65, 0XDEBDA0).setRegistryName("alexsmobs:spawn_egg_kangaroo"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.CACHALOT_WHALE, 0X949899, 0X5F666E).setRegistryName("alexsmobs:spawn_egg_cachalot_whale"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.LEAFCUTTER_ANT, 0X964023, 0XA65930).setRegistryName("alexsmobs:spawn_egg_leafcutter_ant"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.ENDERIOPHAGE, 0X872D83, 0XF6E2CD).setRegistryName("alexsmobs:spawn_egg_enderiophage"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BALD_EAGLE, 0X321F18, 0XF4F4F4).setRegistryName("alexsmobs:spawn_egg_bald_eagle"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.TIGER, 0XC7612E, 0X2A3233).setRegistryName("alexsmobs:spawn_egg_tiger"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.TARANTULA_HAWK, 0X234763, 0XE37B38).setRegistryName("alexsmobs:spawn_egg_tarantula_hawk"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.VOID_WORM, 0X0F1026, 0X1699AB).setRegistryName("alexsmobs:spawn_egg_void_worm"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.FRILLED_SHARK, 0X726B6B, 0X873D3D).setRegistryName("alexsmobs:spawn_egg_frilled_shark"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.MIMIC_OCTOPUS, 0XFFEBDC, 0X1D1C1F).setRegistryName("alexsmobs:spawn_egg_mimic_octopus"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SEAGULL, 0XC9D2DC, 0XFFD850).setRegistryName("alexsmobs:spawn_egg_seagull"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BISON, 0X4C3A2E, 0X7A6546).setRegistryName("alexsmobs:spawn_egg_bison"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.RHINOCEROS, 0XA19594, 0X827474).setRegistryName("alexsmobs:spawn_egg_rhinoceros"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.SKUNK, 0X222D36, 0XE4E5F2).setRegistryName("alexsmobs:spawn_egg_skunk"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.TUSKLIN, 0X735841, 0XE8E2D5).setRegistryName("alexsmobs:spawn_egg_tusklin"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.ANTEATER, 0X4C3F3A, 0XCCBCB4).setRegistryName("alexsmobs:spawn_egg_anteater"));
        registerItem(event,new ItemAMSpawnEgg(AMEntityRegistry.BANANA_SLUG, 0XFFD045, 0XFFF173).setRegistryName("alexsmobs:spawn_egg_banana_slug"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.ANACONDA, 0X565C22, 0XD3763F).setRegistryName("alexsmobs:spawn_egg_anaconda"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.LAVIATHAN, 0XD68356, 0X3C3947).setRegistryName("alexsmobs:spawn_egg_laviathan"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.GIANT_SQUID, 0X1F1F1F, 0XAC5843).setRegistryName("alexsmobs:spawn_egg_giant_squid"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.JERBOA, 0XDEC58A, 0XDE9D90).setRegistryName("alexsmobs:spawn_egg_jerboa"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.RAIN_FROG, 0XC0B59B, 0X7B654F).setRegistryName("alexsmobs:spawn_egg_rain_frog"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.TRIOPS, 0X967954, 0XCA7150).setRegistryName("alexsmobs:spawn_egg_triops"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.FLYING_FISH, 0X7BBCED, 0X6881B3).setRegistryName("alexsmobs:spawn_egg_flying_fish"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.COMB_JELLY, 0X4A90D9, 0X9B59B6).setRegistryName("alexsmobs:spawn_egg_comb_jelly"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.TERRAPIN, 0X6E6E30, 0X929647).setRegistryName("alexsmobs:spawn_egg_terrapin"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.GELADA_MONKEY, 0XB08C64, 0XFF4F53).setRegistryName("alexsmobs:spawn_egg_gelada_monkey"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.MANED_WOLF, 0XBB7A47, 0X40271A).setRegistryName("alexsmobs:spawn_egg_maned_wolf"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.CAIMAN, 0X5C5631, 0XBBC45C).setRegistryName("alexsmobs:spawn_egg_caiman"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.COSMIC_COD, 0X3B2E7E, 0X8E44AD).setRegistryName("alexsmobs:spawn_egg_cosmic_cod"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.DEVILS_HOLE_PUPFISH, 0X6B8E9F, 0X4A7C59).setRegistryName("alexsmobs:spawn_egg_devils_hole_pupfish"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.CATFISH, 0X5A5A5A, 0X3D3D3D).setRegistryName("alexsmobs:spawn_egg_catfish"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.TOUCAN, 0XF58F33, 0X1E2133).setRegistryName("alexsmobs:spawn_egg_toucan"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.POTOO, 0X8C7753, 0XFFC042).setRegistryName("alexsmobs:spawn_egg_potoo"));
        registerItem(event, new ItemAMSpawnEgg(AMEntityRegistry.SUGAR_GLIDER, 0X868181, 0XEBEBE0).setRegistryName("alexsmobs:spawn_egg_sugar_glider"));
        try {
            for (Field f : AMItemRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Item) {
                    Item item = (Item) obj;
                    if (item.getCreativeTab() == null && !(item instanceof ItemAMInternal)) {
                        item.setCreativeTab(AlexsMobs.TAB);
                    }
                    registerItem(event, item);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        OreDictionary.registerOre("cropBanana", BANANA);
        OreDictionary.registerOre("fruitBanana", BANANA);
        AMTagRegistry.reloadItemTagSetFor(AMTagRegistry.BANANAS);
        AMTagRegistry.reloadItemTagSetFor(AMTagRegistry.CAPUCHIN_MONKEY_TAMEABLES);
        AMTagRegistry.reloadItemTagSetFor(new net.minecraft.util.ResourceLocation("alexsmobs", "animal_dictionary_ingredient"));
        AMTagRegistry.reloadItemTagSetFor(AMTagRegistry.GORILLA_TAMEABLES);
        AMTagRegistry.reloadItemTagSetFor(AMTagRegistry.GORILLA_BREEDABLES);
        AMTagRegistry.reloadItemTagSetFor(AMTagRegistry.GORILLA_FOODSTUFFS);
        try {
            for (Field f : AMBlockRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Block) {
                    if (obj == AMBlockRegistry.SKUNK_SPRAY) {
                        continue;
                    }
                    ItemBlock itemBlock = new ItemBlock((Block) obj);
                    itemBlock.setCreativeTab(AlexsMobs.TAB);
                    itemBlock.setRegistryName(((Block) obj).getRegistryName());
                    registerItem(event,itemBlock);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        CROCODILE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(CROCODILE_SCUTE));
        ROADRUNNER_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(ROADRUNNER_FEATHER));
        CENTIPEDE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(CENTIPEDE_LEG));
        MOOSE_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(MOOSE_ANTLER));
        RACCOON_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(RACCOON_TAIL));
        SOMBRERO_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(Item.getItemFromBlock(Blocks.HAY_BLOCK)));
        SPIKED_TURTLE_SHELL_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(SPIKED_SCUTE));
        FEDORA_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(Items.LEATHER));
        EMU_ARMOR_MATERIAL.setRepairMaterial(Ingredient.fromItems(EMU_FEATHER));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(SHARK_TOOTH_ARROW, new BehaviorProjectileDispense() {
            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                EntitySharkToothArrow entityarrow = new EntitySharkToothArrow(worldIn, position.getX(), position.getY(), position.getZ());
                entityarrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
                entityarrow.setPotionEffect(stackIn);
                return entityarrow;
            }
        });
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ANCIENT_DART, new BehaviorProjectileDispense() {
            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                EntityTossedItem tossedItem = new EntityTossedItem(worldIn, position.getX(), position.getY(), position.getZ());
                tossedItem.setDart(true);
                return tossedItem;
            }
        });
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(COCKROACH_OOTHECA, new BehaviorProjectileDispense() {
            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                return new EntityCockroachEgg(worldIn, position.getX(), position.getY(), position.getZ());
            }
        });
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(EMU_EGG, new BehaviorProjectileDispense() {
            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                return new EntityEmuEgg(worldIn, position.getX(), position.getY(), position.getZ());
            }
        });
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ENDERIOPHAGE_ROCKET, new BehaviorDefaultDispenseItem() {
            @Override
            public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                EnumFacing facing = source.getBlockState().getValue(net.minecraft.block.BlockDispenser.FACING);
                double x = source.getX() + facing.getFrontOffsetX();
                double y = source.getY() + facing.getFrontOffsetY();
                double z = source.getZ() + facing.getFrontOffsetZ();
                EntityEnderiophageRocket rocket = new EntityEnderiophageRocket(source.getWorld(), x, y, z, stack);
                rocket.motionX = facing.getFrontOffsetX();
                rocket.motionY = facing.getFrontOffsetY() + 0.1F;
                rocket.motionZ = facing.getFrontOffsetZ();
                source.getWorld().spawnEntity(rocket);
                stack.shrink(1);
                return stack;
            }
        });
        COMPOST_CHANCES.put(BANANA, 0.65F);
        COMPOST_CHANCES.put(Item.getItemFromBlock(AMBlockRegistry.BANANA_PEEL), 1.0F);
        COMPOST_CHANCES.put(ACACIA_BLOSSOM, 0.65F);
        COMPOST_CHANCES.put(GONGYLIDIA, 0.9F);
    }
}
