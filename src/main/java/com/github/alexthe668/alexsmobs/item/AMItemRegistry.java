package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe668.alexsmobs.entity.*;
import com.github.alexthe668.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe668.alexsmobs.misc.AMTagRegistry;
import net.minecraftforge.oredict.OreDictionary;
import com.github.alexthe668.citadel.server.item.CustomArmorMaterial;
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
    public static CustomArmorMaterial MOOSE_ARMOR_MATERIAL = new AMArmorMaterial("moose", 19, new int[]{3, 3, 3, 3}, 21, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.5F);
    public static CustomArmorMaterial RACCOON_ARMOR_MATERIAL = new AMArmorMaterial("raccoon", 17, new int[]{3, 3, 3, 3}, 21, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 2.5F);
    public static CustomArmorMaterial SOMBRERO_ARMOR_MATERIAL = new AMArmorMaterial("sombrero", 14, new int[]{2, 2, 2, 2}, 30, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static CustomArmorMaterial SPIKED_TURTLE_SHELL_ARMOR_MATERIAL = new AMArmorMaterial("spiked_turtle_shell", 35, new int[]{3, 3, 3, 3}, 30, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1F, 0.2F);
    public static CustomArmorMaterial FEDORA_ARMOR_MATERIAL = new AMArmorMaterial("fedora", 10, new int[]{2, 2, 2, 2}, 30, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static CustomArmorMaterial EMU_ARMOR_MATERIAL = new AMArmorMaterial("emu", 9, new int[]{4, 4, 4, 4}, 20, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F);
    public static CustomArmorMaterial FLYING_FISH_MATERIAL = new AMArmorMaterial("flying_fish", 9, new int[]{1, 1, 1, 1}, 8, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0F);
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
    public static final Item FLYING_FISH = new ItemFood(3, 0.4F, true).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:flying_fish");
    public static final Item FLYING_FISH_BOOTS = new ItemModArmor(FLYING_FISH_MATERIAL, EntityEquipmentSlot.FEET).setRegistryName("alexsmobs:flying_fish_boots");
    public static final Item FLYING_FISH_BUCKET = new ItemModFishBucket(AMEntityRegistry.FLYING_FISH, ItemModFishBucket.FluidKind.WATER).setRegistryName("alexsmobs:flying_fish_bucket");
    public static final Item COSMIC_COD = new ItemFood(6, 0.3F, false) {
        @Override
        public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, net.minecraft.entity.player.EntityPlayer player, net.minecraft.util.EnumHand hand) {
            // Creative sets disableDamage, and ItemFood.canEat then refuses every food.
            player.setActiveHand(hand);
            return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
    }.setAlwaysEdible().setPotionEffect(new net.minecraft.potion.PotionEffect(com.github.alexthe668.alexsmobs.effect.AMEffectRegistry.ENDER_FLU, 12000, 0), 0.15F).setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:cosmic_cod");
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
    public static final Item MOSQUITO_REPELLENT_STEW = new ItemMosquitoRepellentStew();
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
    public static final Item VINE_LASSO = AlexsMobs.PROXY.setupISTER(new ItemVineLasso()).setRegistryName("alexsmobs:vine_lasso");
    public static final Item LOST_TENTACLE = new Item().setCreativeTab(AlexsMobs.TAB).setRegistryName("alexsmobs:lost_tentacle");
    public static final Item SQUID_GRAPPLE = AlexsMobs.PROXY.setupISTER(new ItemSquidGrapple().setCreativeTab(AlexsMobs.TAB)).setRegistryName("alexsmobs:squid_grapple");
    public static final Item RAINBOW_JELLY = new ItemRainbowJelly();

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
        OreDictionary.registerOre("listAllfishraw", FLYING_FISH);
        OreDictionary.registerOre("listAllfishfresh", FLYING_FISH);
        OreDictionary.registerOre("foodFishraw", FLYING_FISH);
        AMTagRegistry.reloadAllItemTags();
        try {
            for (Field f : AMBlockRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Block) {
                    if (obj == AMBlockRegistry.SKUNK_SPRAY) {
                        continue;
                    }
                    ItemBlock itemBlock;
                    if (obj == AMBlockRegistry.TRIOPS_EGGS) {
                        itemBlock = new ItemTriopsEggs((Block) obj);
                    } else if (obj == AMBlockRegistry.TERRAPIN_EGG) {
                        itemBlock = new ItemTerrapinEgg((Block) obj);
                    } else {
                        itemBlock = new ItemBlock((Block) obj);
                    }
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
        FLYING_FISH_MATERIAL.setRepairMaterial(Ingredient.fromItems(FLYING_FISH));
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
