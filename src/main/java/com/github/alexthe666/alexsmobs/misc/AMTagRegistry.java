package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AMTagRegistry {
    public static final ResourceLocation ANTEATER_BREEDABLES = new ResourceLocation("alexsmobs", "anteater_breedables");
    public static final ResourceLocation ANTEATER_FOODSTUFFS = new ResourceLocation("alexsmobs", "anteater_foodstuffs");
    public static final ResourceLocation INSECT_ITEMS = new ResourceLocation("alexsmobs", "insect_items");
    public static final ResourceLocation FORGE_WOODEN_CHESTS = new ResourceLocation("forge","chests/wooden");
    public static final ResourceLocation GRIZZLY_BEEHIVE = new ResourceLocation("alexsmobs","grizzly_beehive");
    public static final ResourceLocation GRIZZLY_FOODSTUFFS = new ResourceLocation("alexsmobs", "grizzly_foodstuffs");
    public static final ResourceLocation GRIZZLY_HONEY = new ResourceLocation("alexsmobs", "grizzly_honey");
    public static final ResourceLocation FLY_TARGETS = new ResourceLocation("alexsmobs", "fly_hurt_targets");
    public static final ResourceLocation FLY_ANNOY_TARGETS = new ResourceLocation("alexsmobs", "fly_annoy_targets");
    public static final ResourceLocation DROPS_BANANAS = new ResourceLocation("alexsmobs","drops_bananas");
    public static final ResourceLocation DROPS_ACACIA_BLOSSOMS = new ResourceLocation("alexsmobs","drops_acacia_blossoms");
    public static final ResourceLocation ORCA_BREAKABLES = new ResourceLocation("alexsmobs","orca_breakables");
    public static final ResourceLocation SUNBIRD_SCORCH_TARGETS = new ResourceLocation("alexsmobs", "sunbird_scorch_targets");
    public static final ResourceLocation GORILLA_BREAKABLES = new ResourceLocation("alexsmobs","gorilla_breakables");
    public static final ResourceLocation GORILLA_FOODSTUFFS = new ResourceLocation("alexsmobs","gorilla_foodstuffs");
    public static final ResourceLocation GORILLA_TAMEABLES = new ResourceLocation("alexsmobs", "gorilla_tameables");
    public static final ResourceLocation GORILLA_BREEDABLES = new ResourceLocation("alexsmobs", "gorilla_breedables");
    public static final ResourceLocation GORILLA_SPAWNS = new ResourceLocation("alexsmobs", "gorilla_spawns");
    public static final ResourceLocation ORCA_TARGETS = new ResourceLocation("alexsmobs", "orca_targets");
    public static final ResourceLocation CRIMSON_MOSQUITO_TARGETS = new ResourceLocation("alexsmobs", "crimson_mosquito_targets");
    public static final ResourceLocation WARPED_TOAD_TARGETS = new ResourceLocation("alexsmobs", "warped_toad_targets");
    public static final ResourceLocation CROCODILE_TARGETS = new ResourceLocation("alexsmobs", "crocodile_targets");
    public static final ResourceLocation KOMODO_DRAGON_TARGETS = new ResourceLocation("alexsmobs", "komodo_dragon_targets");
    public static final ResourceLocation MANTIS_SHRIMP_TARGETS = new ResourceLocation("alexsmobs", "mantis_shrimp_targets");
    public static final ResourceLocation VOID_PORTAL_IGNORES = new ResourceLocation("alexsmobs", "void_portal_ignores");

    public static final ResourceLocation BANANAS = new ResourceLocation("alexsmobs", "bananas");
    public static final ResourceLocation RACCOON_FOODSTUFFS = new ResourceLocation("alexsmobs", "raccoon_foodstuffs");
    public static final ResourceLocation SEAL_FOODSTUFFS = new ResourceLocation("alexsmobs", "seal_foodstuffs");
    public static final ResourceLocation SEAL_BREEDABLES = new ResourceLocation("alexsmobs", "seal_breedables");
    public static final ResourceLocation SEAL_OFFERINGS = new ResourceLocation("alexsmobs", "seal_offerings");
    public static final ResourceLocation MOOSE_BREEDABLES = new ResourceLocation("alexsmobs", "moose_breedables");
    public static final ResourceLocation CAPUCHIN_MONKEY_TAMEABLES = new ResourceLocation("alexsmobs", "capuchin_monkey_tameables");
    public static final ResourceLocation CAPUCHIN_MONKEY_BREEDABLES = new ResourceLocation("alexsmobs", "capuchin_monkey_breedables");
    public static final ResourceLocation CAPUCHIN_MONKEY_FOODSTUFFS = new ResourceLocation("alexsmobs", "capuchin_monkey_foodstuffs");
    public static final ResourceLocation CAPUCHIN_MONKEY_SPAWNS = new ResourceLocation("alexsmobs", "capuchin_monkey_spawns");
    public static final ResourceLocation SEAL_DIGABLES = new ResourceLocation("alexsmobs", "seal_digables");
    public static final ResourceLocation SHOEBILL_FOODSTUFFS = new ResourceLocation("alexsmobs", "shoebill_foodstuffs");
    public static final ResourceLocation ELEPHANT_FOODBLOCKS = new ResourceLocation("alexsmobs", "elephant_foodblocks");
    public static final ResourceLocation ELEPHANT_FOODSTUFFS = new ResourceLocation("alexsmobs", "elephant_foodstuffs");
    public static final ResourceLocation BISON_BREEDABLES = new ResourceLocation("alexsmobs", "bison_breedables");
    public static final ResourceLocation TUSKLIN_BREEDABLES = new ResourceLocation("alexsmobs", "tusklin_breedables");
    public static final ResourceLocation TUSKLIN_FOODSTUFFS = new ResourceLocation("alexsmobs", "tusklin_foodstuffs");
    public static final ResourceLocation TUSKLIN_SPAWNS = new ResourceLocation("alexsmobs", "tusklin_spawns");
    public static final ResourceLocation BANANA_SLUG_BREEDABLES = new ResourceLocation("alexsmobs", "banana_slug_breedables");
    public static final ResourceLocation ANACONDA_FOODSTUFFS = new ResourceLocation("alexsmobs", "anaconda_foodstuffs");
    public static final ResourceLocation ANACONDA_TARGETS = new ResourceLocation("alexsmobs", "anaconda_targets");
    public static final ResourceLocation ANACONDA_SPAWNS = new ResourceLocation("alexsmobs", "anaconda_spawns");
    public static final ResourceLocation RHINOCEROS_BREEDABLES = new ResourceLocation("alexsmobs", "rhinoceros_breedables");
    public static final ResourceLocation RHINOCEROS_FOODSTUFFS = new ResourceLocation("alexsmobs", "rhinoceros_foodstuffs");
    public static final ResourceLocation SKUNK_BREEDABLES = new ResourceLocation("alexsmobs", "skunk_breedables");
    public static final ResourceLocation SKUNK_FEARS = new ResourceLocation("alexsmobs", "skunk_fears");
    public static final ResourceLocation SOUL_VULTURE_PERCHES = new ResourceLocation("alexsmobs","soul_vulture_perches");
    public static final ResourceLocation SOUL_VULTURE_SPAWNS = new ResourceLocation("alexsmobs","soul_vulture_spawns");
    public static final ResourceLocation SNOW_LEOPARD_TARGETS = new ResourceLocation("alexsmobs", "snow_leopard_targets");
    public static final ResourceLocation SCATTERS_CROWS = new ResourceLocation("alexsmobs", "scatters_crows");
    public static final ResourceLocation CROW_FOODBLOCKS = new ResourceLocation("alexsmobs", "crow_foodblocks");
    public static final ResourceLocation CROW_FOODSTUFFS = new ResourceLocation("alexsmobs", "crow_foodstuffs");
    public static final ResourceLocation BLUE_JAY_FOODSTUFFS = new ResourceLocation("alexsmobs", "blue_jay_foodstuffs");
    public static final ResourceLocation BLUE_JAY_TEAMING_FOODS = new ResourceLocation("alexsmobs", "blue_jay_teaming_foods");
    public static final ResourceLocation BLUE_JAY_BREEDABLES = new ResourceLocation("alexsmobs", "blue_jay_breedables");
    public static final ResourceLocation BLUE_JAY_ALERT_FOODS = new ResourceLocation("alexsmobs", "blue_jay_alert_foods");
    public static final ResourceLocation RACCOON_TEAMING_FOODS = new ResourceLocation("alexsmobs", "raccoon_teaming_foods");
    public static final ResourceLocation MUNGUS_REPLACE_MUSHROOM = new ResourceLocation("alexsmobs", "mungus_replace_mushroom");
    public static final ResourceLocation MUNGUS_REPLACE_NETHER = new ResourceLocation("alexsmobs", "mungus_replace_nether");
    public static final ResourceLocation WARPED_MOSCO_BREAKABLES = new ResourceLocation("alexsmobs", "warped_mosco_breakables");
    public static final ResourceLocation CROW_FEARS = new ResourceLocation("alexsmobs", "crow_fears");
    public static final ResourceLocation PLATYPUS_FOODSTUFFS = new ResourceLocation("alexsmobs", "platypus_foodstuffs");
    public static final ResourceLocation CACHALOT_WHALE_TARGETS = new ResourceLocation("alexsmobs", "cachalot_whale_targets");
    public static final ResourceLocation CACHALOT_WHALE_BREAKABLES = new ResourceLocation("alexsmobs","cachalot_whale_breakables");
    public static final ResourceLocation LEAFCUTTER_ANT_BREAKABLES = new ResourceLocation("alexsmobs","leafcutter_ant_breakables");
    public static final ResourceLocation TIGER_TARGETS = new ResourceLocation("alexsmobs", "tiger_targets");
    public static final ResourceLocation BALD_EAGLE_TARGETS = new ResourceLocation("alexsmobs", "bald_eagle_targets");
    public static final ResourceLocation VOID_WORM_BREAKABLES = new ResourceLocation("alexsmobs", "void_worm_breakables");
    public static final ResourceLocation MIMIC_OCTOPUS_FEARS = new ResourceLocation("alexsmobs", "mimic_octopus_fears");
    public static final ResourceLocation MIMIC_OCTOPUS_CREEPER_ITEMS = new ResourceLocation("alexsmobs", "mimic_octopus_creeper_items");
    public static final ResourceLocation MIMIC_OCTOPUS_GUARDIAN_ITEMS = new ResourceLocation("alexsmobs", "mimic_octopus_guardian_items");
    public static final ResourceLocation MIMIC_OCTOPUS_PUFFERFISH_ITEMS = new ResourceLocation("alexsmobs", "mimic_octopus_pufferfish_items");
    public static final ResourceLocation SHRIMP_RICE_FRYABLES = new ResourceLocation("alexsmobs", "shrimp_rice_fryables");
    public static final ResourceLocation TIGER_BREEDABLES = new ResourceLocation("alexsmobs", "tiger_breedables");
    public static final ResourceLocation BALD_EAGLE_TAMEABLES = new ResourceLocation("alexsmobs", "bald_eagle_tameables");
    public static final ResourceLocation VOID_WORM_DROPS = new ResourceLocation("alexsmobs", "void_worm_drops");
    public static final ResourceLocation GIANT_SQUID_TARGETS = new ResourceLocation("alexsmobs", "giant_squid_targets");
    public static final ResourceLocation LAVIATHAN_BREEDABLES = new ResourceLocation("alexsmobs", "laviathan_breedables");
    public static final ResourceLocation LAVIATHAN_FOODSTUFFS = new ResourceLocation("alexsmobs", "laviathan_foodstuffs");
    public static final ResourceLocation LAVIATHAN_BREAKABLES = new ResourceLocation("alexsmobs", "laviathan_breakables");
    public static final ResourceLocation JERBOA_BREEDABLES = new ResourceLocation("alexsmobs", "jerboa_breedables");
    public static final ResourceLocation JERBOA_BEGS_FOR = new ResourceLocation("alexsmobs", "jerboa_begs_for");
    public static final ResourceLocation RAIN_FROG_BREEDABLES = new ResourceLocation("alexsmobs", "rain_frog_breedables");
    public static final ResourceLocation RAIN_FROG_SPAWNS = new ResourceLocation("alexsmobs", "rain_frog_spawns");
    public static final ResourceLocation TRIOPS_BREEDABLES = new ResourceLocation("alexsmobs", "triops_breedables");
    public static final ResourceLocation PUPFISH_EATABLES = new ResourceLocation("alexsmobs", "pupfish_eatables");
    public static final ResourceLocation CATFISH_ITEM_FASCINATIONS = new ResourceLocation("alexsmobs", "catfish_item_fascinations");
    public static final ResourceLocation CATFISH_BLOCK_FASCINATIONS = new ResourceLocation("alexsmobs", "catfish_block_fascinations");
    public static final ResourceLocation CATFISH_IGNORE_EATING = new ResourceLocation("alexsmobs", "catfish_ignore_eating");
    public static final ResourceLocation TERRAPIN_BREEDABLES = new ResourceLocation("alexsmobs", "terrapin_breedables");
    public static final ResourceLocation GELADA_MONKEY_BREEDABLES = new ResourceLocation("alexsmobs", "gelada_monkey_breedables");
    public static final ResourceLocation GELADA_MONKEY_LAND_CLEARING_FOODS = new ResourceLocation("alexsmobs", "gelada_monkey_land_clearing_foods");
    public static final ResourceLocation GELADA_MONKEY_GRASS = new ResourceLocation("alexsmobs", "gelada_monkey_grass");
    public static final ResourceLocation MANED_WOLF_BREEDABLES = new ResourceLocation("alexsmobs", "maned_wolf_breedables");
    public static final ResourceLocation MANED_WOLF_STENCH_FOODS = new ResourceLocation("alexsmobs", "maned_wolf_stench_foods");
    public static final ResourceLocation CAIMAN_BREEDABLES = new ResourceLocation("alexsmobs", "caiman_breedables");
    public static final ResourceLocation CAIMAN_FOODSTUFFS = new ResourceLocation("alexsmobs", "caiman_foodstuffs");
    public static final ResourceLocation CAIMAN_TARGETS = new ResourceLocation("alexsmobs", "caiman_targets");
    public static final ResourceLocation CAIMAN_SPAWNS = new ResourceLocation("alexsmobs", "caiman_spawns");
    public static final ResourceLocation TOUCAN_BREEDABLES = new ResourceLocation("alexsmobs", "toucan_breedables");
    public static final ResourceLocation TOUCAN_GOLDEN_FOODS = new ResourceLocation("alexsmobs", "toucan_golden_foods");
    public static final ResourceLocation TOUCAN_ENCHANTED_GOLDEN_FOODS = new ResourceLocation("alexsmobs", "toucan_enchanted_golden_foods");
    public static final ResourceLocation POTOO_BREEDABLES = new ResourceLocation("alexsmobs", "potoo_breedables");
    public static final ResourceLocation SUGAR_GLIDER_BREEDABLES = new ResourceLocation("alexsmobs", "sugar_glider_breedables");
    public static final ResourceLocation SUGAR_GLIDER_TAMEABLES = new ResourceLocation("alexsmobs", "sugar_glider_tameables");
    public static final ResourceLocation POTOO_PERCHES = new ResourceLocation("alexsmobs", "potoo_perches");

    public static final ResourceLocation ROADRUNNER_SPAWNS = new ResourceLocation("alexsmobs","roadrunner_spawns");
    public static final ResourceLocation LOBSTER_SPAWNS = new ResourceLocation("alexsmobs","lobster_spawns");
    public static final ResourceLocation MIMIC_OCTOPUS_SPAWNS = new ResourceLocation("alexsmobs","mimic_octopus_spawns");
    public static final ResourceLocation RATTLESNAKE_SPAWNS = new ResourceLocation("alexsmobs","rattlesnake_spawns");
    public static final ResourceLocation KOMODO_DRAGON_SPAWNS = new ResourceLocation("alexsmobs","komodo_dragon_spawns");
    public static final ResourceLocation CROCODILE_SPAWNS = new ResourceLocation("alexsmobs","crocodile_spawns");
    public static final ResourceLocation SEAL_SPAWNS = new ResourceLocation("alexsmobs","seal_spawns");
    public static final ResourceLocation ALLIGATOR_SNAPPING_TURTLE_SPAWNS = new ResourceLocation("alexsmobs","alligator_snapping_turtle_spawns");
    public static final ResourceLocation MANTIS_SHRIMP_SPAWNS = new ResourceLocation("alexsmobs","mantis_shrimp_spawns");
    public static final ResourceLocation EMU_SPAWNS = new ResourceLocation("alexsmobs","emu_spawns");
    public static final ResourceLocation KANGAROO_SPAWNS = new ResourceLocation("alexsmobs","kangaroo_spawns");
    public static final ResourceLocation PLATYPUS_SPAWNS = new ResourceLocation("alexsmobs","platypus_spawns");

    public static final Set<Block> BLOCKS_DROPPING_BANANAS = Sets.newHashSet();
    public static final Set<Block> BLOCKS_DROPPING_ACACIA_BLOSSOMS = Sets.newHashSet();

    /**
     * Block tags loaded from {@code data/alexsmobs/tags/blocks/&lt;path&gt;.json} (1.16 {@code BlockTags} equivalent).
     */
    /** Populated by {@link #loadBlockTagSetFor(ResourceLocation)} from {@code data/alexsmobs/tags/blocks/*.json}. */
    public static final Map<ResourceLocation, Set<Block>> BLOCK_TAG_SETS = new HashMap<ResourceLocation, Set<Block>>();

    /**
     * Item tags loaded from {@code data/alexsmobs/tags/items/&lt;path&gt;.json} (1.16 {@code ItemTags} equivalent).
     */
    public static final Map<ResourceLocation, Set<Item>> ITEM_TAG_SETS = new HashMap<ResourceLocation, Set<Item>>();

    /**
     * Loaded from {@code data/alexsmobs/tags/entity_types/*.json} (same lists as 1.16 {@code ITag<EntityType<?>>}).
     */
    public static final Map<ResourceLocation, Set<ResourceLocation>> ENTITY_TYPE_TAGS = new HashMap<ResourceLocation, Set<ResourceLocation>>();

    public static void loadDataBlockTags() {
        loadBlockTagJson("/data/alexsmobs/tags/blocks/drops_bananas.json", BLOCKS_DROPPING_BANANAS);
        loadBlockTagJson("/data/alexsmobs/tags/blocks/drops_acacia_blossoms.json", BLOCKS_DROPPING_ACACIA_BLOSSOMS);

        loadBlockTagSetFor(ROADRUNNER_SPAWNS);
        loadBlockTagSetFor(LOBSTER_SPAWNS);
        loadBlockTagSetFor(MIMIC_OCTOPUS_SPAWNS);
        loadBlockTagSetFor(RATTLESNAKE_SPAWNS);
        loadBlockTagSetFor(KOMODO_DRAGON_SPAWNS);
        loadBlockTagSetFor(CROCODILE_SPAWNS);
        loadBlockTagSetFor(SEAL_SPAWNS);
        loadBlockTagSetFor(SEAL_DIGABLES);
        loadBlockTagSetFor(ALLIGATOR_SNAPPING_TURTLE_SPAWNS);
        loadBlockTagSetFor(MANTIS_SHRIMP_SPAWNS);
        loadBlockTagSetFor(EMU_SPAWNS);
        loadBlockTagSetFor(KANGAROO_SPAWNS);
        loadBlockTagSetFor(PLATYPUS_SPAWNS);
        loadBlockTagSetFor(SOUL_VULTURE_SPAWNS);
        loadBlockTagSetFor(SOUL_VULTURE_PERCHES);
        loadBlockTagSetFor(LEAFCUTTER_ANT_BREAKABLES);
        loadBlockTagSetFor(VOID_WORM_BREAKABLES);
        loadBlockTagSetFor(ELEPHANT_FOODBLOCKS);
        loadBlockTagSetFor(GRIZZLY_BEEHIVE);
        loadBlockTagSetFor(ORCA_BREAKABLES);
        loadBlockTagSetFor(GORILLA_BREAKABLES);
        loadBlockTagSetFor(GORILLA_SPAWNS);
        loadBlockTagSetFor(WARPED_MOSCO_BREAKABLES);
        loadBlockTagSetFor(CROW_FOODBLOCKS);
        loadBlockTagSetFor(CROW_FEARS);
        loadBlockTagSetFor(MUNGUS_REPLACE_MUSHROOM);
        loadBlockTagSetFor(MUNGUS_REPLACE_NETHER);
        loadBlockTagSetFor(CACHALOT_WHALE_BREAKABLES);
        loadBlockTagSetFor(LAVIATHAN_BREAKABLES);
        loadBlockTagSetFor(TUSKLIN_SPAWNS);
        loadBlockTagSetFor(RAIN_FROG_SPAWNS);
        loadBlockTagSetFor(PUPFISH_EATABLES);
        loadBlockTagSetFor(CATFISH_BLOCK_FASCINATIONS);
        loadBlockTagSetFor(GELADA_MONKEY_GRASS);
        loadBlockTagSetFor(CAIMAN_SPAWNS);
        loadBlockTagSetFor(CAPUCHIN_MONKEY_SPAWNS);
    }

    public static void loadDataItemTags() {
        loadItemTagSetFor(new ResourceLocation("alexsmobs", "animal_dictionary_ingredient"));
        loadItemTagSetFor(BANANAS);
        loadItemTagSetFor(INSECT_ITEMS);
        loadItemTagSetFor(GRIZZLY_FOODSTUFFS);
        loadItemTagSetFor(GRIZZLY_HONEY);
        loadItemTagSetFor(GORILLA_FOODSTUFFS);
        loadItemTagSetFor(GORILLA_TAMEABLES);
        loadItemTagSetFor(GORILLA_BREEDABLES);
        loadItemTagSetFor(RACCOON_FOODSTUFFS);
        loadItemTagSetFor(SEAL_FOODSTUFFS);
        loadItemTagSetFor(SEAL_BREEDABLES);
        loadItemTagSetFor(SEAL_OFFERINGS);
        loadItemTagSetFor(MOOSE_BREEDABLES);
        loadItemTagSetFor(CAPUCHIN_MONKEY_TAMEABLES);
        loadItemTagSetFor(CAPUCHIN_MONKEY_BREEDABLES);
        loadItemTagSetFor(CAPUCHIN_MONKEY_FOODSTUFFS);
        loadItemTagSetFor(SHOEBILL_FOODSTUFFS);
        loadItemTagSetFor(PLATYPUS_FOODSTUFFS);
        loadItemTagSetFor(CROW_FOODSTUFFS);
        loadItemTagSetFor(BLUE_JAY_FOODSTUFFS);
        loadItemTagSetFor(BLUE_JAY_TEAMING_FOODS);
        loadItemTagSetFor(BLUE_JAY_BREEDABLES);
        loadItemTagSetFor(BLUE_JAY_ALERT_FOODS);
        loadItemTagSetFor(RACCOON_TEAMING_FOODS);
        loadItemTagSetFor(ELEPHANT_FOODSTUFFS);
        loadItemTagSetFor(BISON_BREEDABLES);
        loadItemTagSetFor(TUSKLIN_BREEDABLES);
        loadItemTagSetFor(TUSKLIN_FOODSTUFFS);
        loadItemTagSetFor(BANANA_SLUG_BREEDABLES);
        loadItemTagSetFor(ANACONDA_FOODSTUFFS);
        loadBlockTagSetFor(ANACONDA_SPAWNS);
        loadItemTagSetFor(RHINOCEROS_BREEDABLES);
        loadItemTagSetFor(RHINOCEROS_FOODSTUFFS);
        loadItemTagSetFor(SKUNK_BREEDABLES);
        loadItemTagSetFor(VOID_WORM_DROPS);
        loadItemTagSetFor(TIGER_BREEDABLES);
        loadItemTagSetFor(MIMIC_OCTOPUS_CREEPER_ITEMS);
        loadItemTagSetFor(MIMIC_OCTOPUS_GUARDIAN_ITEMS);
        loadItemTagSetFor(MIMIC_OCTOPUS_PUFFERFISH_ITEMS);
        loadItemTagSetFor(SHRIMP_RICE_FRYABLES);
        loadItemTagSetFor(BALD_EAGLE_TAMEABLES);
        loadItemTagSetFor(ANTEATER_BREEDABLES);
        loadItemTagSetFor(ANTEATER_FOODSTUFFS);
        loadItemTagSetFor(LAVIATHAN_BREEDABLES);
        loadItemTagSetFor(LAVIATHAN_FOODSTUFFS);
        loadItemTagSetFor(JERBOA_BREEDABLES);
        loadItemTagSetFor(JERBOA_BEGS_FOR);
        loadItemTagSetFor(RAIN_FROG_BREEDABLES);
        loadItemTagSetFor(TRIOPS_BREEDABLES);
        loadItemTagSetFor(CATFISH_ITEM_FASCINATIONS);
        loadItemTagSetFor(TERRAPIN_BREEDABLES);
        loadItemTagSetFor(GELADA_MONKEY_BREEDABLES);
        loadItemTagSetFor(GELADA_MONKEY_LAND_CLEARING_FOODS);
        loadItemTagSetFor(MANED_WOLF_BREEDABLES);
        loadItemTagSetFor(MANED_WOLF_STENCH_FOODS);
        loadItemTagSetFor(CAIMAN_BREEDABLES);
        loadItemTagSetFor(CAIMAN_FOODSTUFFS);
        loadItemTagSetFor(TOUCAN_BREEDABLES);
        loadItemTagSetFor(TOUCAN_GOLDEN_FOODS);
        loadItemTagSetFor(TOUCAN_ENCHANTED_GOLDEN_FOODS);
        loadItemTagSetFor(POTOO_BREEDABLES);
        loadItemTagSetFor(SUGAR_GLIDER_BREEDABLES);
        loadItemTagSetFor(SUGAR_GLIDER_TAMEABLES);
        loadBlockTagSetFor(POTOO_PERCHES);
        loadForgeWoodenChestTag();
    }

    public static boolean blockInTag(ResourceLocation tagId, Block block) {
        if (tagId == null || block == null) {
            return false;
        }
        Set<Block> set = BLOCK_TAG_SETS.get(tagId);
        return set != null && set.contains(block);
    }

    public static boolean itemInTag(ResourceLocation tagId, Item item) {
        if (tagId == null || item == null) {
            return false;
        }
        Set<Item> set = ITEM_TAG_SETS.get(tagId);
        return set != null && set.contains(item);
    }

    public static boolean isDeadBush(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Item.getItemFromBlock(Blocks.DEADBUSH);
    }

    public static boolean isTallGrassPlant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == Item.getItemFromBlock(Blocks.TALLGRASS)) {
            return true;
        }
        if (item == Item.getItemFromBlock(Blocks.DOUBLE_PLANT)) {
            int meta = stack.getMetadata() & 7;
            return meta == 2 || meta == 3;
        }
        return false;
    }

    public static boolean isPufferfish(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.FISH && stack.getMetadata() == 3;
    }

    public static boolean isRawSalmon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.FISH && stack.getMetadata() == 1;
    }

    public static boolean isClownfish(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.FISH && stack.getMetadata() == 2;
    }

    public static boolean isEgg(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.getItem() == Items.EGG;
    }

    private static void loadBlockTagSetFor(ResourceLocation tagId) {
        if (BLOCK_TAG_SETS.containsKey(tagId)) {
            return;
        }
        String path = "/data/" + tagId.getResourceDomain() + "/tags/blocks/" + tagId.getResourcePath() + ".json";
        Set<Block> into = Sets.newHashSet();
        loadBlockTagJson(path, into, Sets.newHashSet(tagId));
        BLOCK_TAG_SETS.put(tagId, into);
    }

    public static void loadItemTagSetFor(ResourceLocation tagId) {
        if (ITEM_TAG_SETS.containsKey(tagId)) {
            return;
        }
        reloadItemTagSetFor(tagId);
    }

    /** Rebuilds every item tag after the item registry is populated (tags are first loaded in preInit). */
    public static void reloadAllItemTags() {
        ITEM_TAG_SETS.clear();
        loadDataItemTags();
    }

    /** Rebuilds a tag after the item registry is populated (tags are first loaded in preInit). */
    public static void reloadItemTagSetFor(ResourceLocation tagId) {
        String path = "/data/" + tagId.getResourceDomain() + "/tags/items/" + tagId.getResourcePath() + ".json";
        Set<Item> into = Sets.newHashSet();
        loadItemTagJson(path, into, Sets.newHashSet(tagId));
        ITEM_TAG_SETS.put(tagId, into);
    }

    /**
     * 1.16 {@code forge:chests/wooden} tag — 1.12 uses OreDictionary {@code chestWood} plus vanilla chest items.
     */
    private static void loadForgeWoodenChestTag() {
        Set<Item> into = Sets.newHashSet();
        into.add(Item.getItemFromBlock(Blocks.CHEST));
        into.add(Item.getItemFromBlock(Blocks.TRAPPED_CHEST));
        for (ItemStack stack : OreDictionary.getOres("chestWood")) {
            if (!stack.isEmpty()) {
                into.add(stack.getItem());
            }
        }
        ITEM_TAG_SETS.put(FORGE_WOODEN_CHESTS, into);
    }

    public static void loadDataEntityTypeTags() {
        ResourceLocation[] ids = new ResourceLocation[] {
                WARPED_TOAD_TARGETS, VOID_PORTAL_IGNORES, new ResourceLocation("alexsmobs", "villagers"),
                TIGER_TARGETS, SUNBIRD_SCORCH_TARGETS, SNOW_LEOPARD_TARGETS, SCATTERS_CROWS,
                new ResourceLocation("alexsmobs", "passive_land_animals"), new ResourceLocation("alexsmobs", "neutral_land_animals"),
                ORCA_TARGETS, MIMIC_OCTOPUS_FEARS, MANTIS_SHRIMP_TARGETS, KOMODO_DRAGON_TARGETS,
                FLY_TARGETS, FLY_ANNOY_TARGETS, CROCODILE_TARGETS, CRIMSON_MOSQUITO_TARGETS,
                CACHALOT_WHALE_TARGETS, BALD_EAGLE_TARGETS, SKUNK_FEARS, ANACONDA_TARGETS, GIANT_SQUID_TARGETS, CATFISH_IGNORE_EATING, CAIMAN_TARGETS
        };
        for (ResourceLocation tagId : ids) {
            loadEntityTypeTagJson("/data/alexsmobs/tags/entity_types/" + tagId.getResourcePath() + ".json", tagId);
        }
    }

    public static boolean entityMatchesEntityTypeTag(ResourceLocation tagId, Entity entity) {
        if (entity == null || tagId == null) {
            return false;
        }
        Set<ResourceLocation> members = ENTITY_TYPE_TAGS.get(tagId);
        if (members == null || members.isEmpty()) {
            return false;
        }
        ResourceLocation id = registrationNameForEntity(entity);
        return id != null && members.contains(id);
    }

    /**
     * Forge registry id for this entity instance's class (first matching {@link EntityEntry}).
     */
    public static ResourceLocation registrationNameForEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        Class<? extends Entity> clazz = entity.getClass();
        for (EntityEntry ent : ForgeRegistries.ENTITIES.getValues()) {
            if (ent.getEntityClass() == clazz) {
                return ent.getRegistryName();
            }
        }
        return null;
    }

    private static void loadEntityTypeTagJson(String resourcePath, ResourceLocation tagId) {
        InputStream stream = AlexsMobs.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            AlexsMobs.LOGGER.warn("Missing entity type tag resource: {}", resourcePath);
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("values");
            Set<ResourceLocation> out = Sets.newHashSet();
            for (int i = 0; i < values.size(); i++) {
                ResourceLocation resolved = resolveEntityTypeId(values.get(i).getAsString());
                if (resolved != null) {
                    out.add(resolved);
                }
            }
            ENTITY_TYPE_TAGS.put(tagId, out);
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("Failed to load entity type tag {}", resourcePath, e);
        }
    }

    /**
     * Loads item ids from a tag JSON {@code values} array into {@code out}.
     */
    private static void loadItemTagJson(String resourcePath, Set<Item> out, Set<ResourceLocation> loadingTags) {
        InputStream stream = AlexsMobs.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            AlexsMobs.LOGGER.warn("Missing item tag resource: {}", resourcePath);
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("values");
            for (int i = 0; i < values.size(); i++) {
                resolveItemTagEntry(values.get(i).getAsString(), out, loadingTags, resourcePath);
            }
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("Failed to load item tag {}", resourcePath, e);
        }
    }

    /**
     * Loads block names from a tag JSON {@code values} array into {@code out}.
     */
    private static void loadBlockTagJson(String resourcePath, Set<Block> out) {
        loadBlockTagJson(resourcePath, out, Sets.newHashSet());
    }

    private static void loadBlockTagJson(String resourcePath, Set<Block> out, Set<ResourceLocation> loadingTags) {
        InputStream stream = AlexsMobs.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            AlexsMobs.LOGGER.warn("Missing block tag resource: {}", resourcePath);
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            JsonArray values = root.getAsJsonArray("values");
            for (int i = 0; i < values.size(); i++) {
                resolveBlockTagEntry(values.get(i).getAsString(), out, loadingTags, resourcePath);
            }
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("Failed to load block tag {}", resourcePath, e);
        }
    }

    private static void resolveItemTagEntry(String name, Set<Item> out, Set<ResourceLocation> loadingTags, String resourcePath) {
        if ("#minecraft:leaves".equals(name)) {
            addLeafItems(out);
            return;
        }
        if ("#minecraft:saplings".equals(name)) {
            addSaplingItems(out);
            return;
        }
        if ("#forge:seeds".equals(name)) {
            for (ItemStack stack : OreDictionary.getOres("seed")) {
                if (!stack.isEmpty()) {
                    out.add(stack.getItem());
                }
            }
            return;
        }
        if ("#forge:eggs".equals(name) || "#forge:egg".equals(name)) {
            out.add(Items.EGG);
            addOreDictionaryItems(out, "egg");
            addOreDictionaryItems(out, "listAllEgg");
            return;
        }
        if ("#minecraft:flowers".equals(name)) {
            out.add(Item.getItemFromBlock(Blocks.YELLOW_FLOWER));
            out.add(Item.getItemFromBlock(Blocks.RED_FLOWER));
            out.add(Item.getItemFromBlock(Blocks.DOUBLE_PLANT));
            return;
        }
        if (name.startsWith("#forge:crops/")) {
            addOreDictionaryItems(out, "crop" + capitalize(name.substring("#forge:crops/".length())));
            return;
        }
        if (name.startsWith("#forge:fruits/")) {
            addOreDictionaryItems(out, "fruit" + capitalize(name.substring("#forge:fruits/".length())));
            return;
        }
        if ("#minecraft:fishes".equals(name)) {
            out.add(Items.FISH);
            out.add(Items.COOKED_FISH);
            Item flyingFish = ForgeRegistries.ITEMS.getValue(new ResourceLocation("alexsmobs", "flying_fish"));
            if (flyingFish != null) {
                out.add(flyingFish);
            }
            return;
        }
        if (name.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(name.substring(1));
            if (loadingTags.contains(tagId)) {
                AlexsMobs.LOGGER.warn("Circular item tag reference in {}: {}", resourcePath, name);
                return;
            }
            if (!ITEM_TAG_SETS.containsKey(tagId)) {
                loadingTags.add(tagId);
                String path = "/data/" + tagId.getResourceDomain() + "/tags/items/" + tagId.getResourcePath() + ".json";
                Set<Item> nested = Sets.newHashSet();
                loadItemTagJson(path, nested, loadingTags);
                ITEM_TAG_SETS.put(tagId, nested);
                loadingTags.remove(tagId);
            }
            Set<Item> resolved = ITEM_TAG_SETS.get(tagId);
            if (resolved != null) {
                out.addAll(resolved);
            }
            return;
        }
        Item item = resolveItemId(name);
        if (item != null) {
            out.add(item);
        } else if (!isIgnoredMissingItem(name)) {
            AlexsMobs.LOGGER.warn("Unknown item in tag {}: {}", resourcePath, name);
        }
    }

    private static void resolveBlockTagEntry(String name, Set<Block> out, Set<ResourceLocation> loadingTags, String resourcePath) {
        if ("#minecraft:leaves".equals(name)) {
            addLeafBlocks(out);
            return;
        }
        if ("#minecraft:saplings".equals(name)) {
            addSaplingBlocks(out);
            return;
        }
        if (resolveVanillaBlockTag(name, out)) {
            return;
        }
        if (name.startsWith("#")) {
            ResourceLocation tagId = new ResourceLocation(name.substring(1));
            if (loadingTags.contains(tagId)) {
                AlexsMobs.LOGGER.warn("Circular block tag reference in {}: {}", resourcePath, name);
                return;
            }
            if (!BLOCK_TAG_SETS.containsKey(tagId)) {
                loadingTags.add(tagId);
                String path = "/data/" + tagId.getResourceDomain() + "/tags/blocks/" + tagId.getResourcePath() + ".json";
                Set<Block> nested = Sets.newHashSet();
                loadBlockTagJson(path, nested, loadingTags);
                BLOCK_TAG_SETS.put(tagId, nested);
                loadingTags.remove(tagId);
            }
            Set<Block> resolved = BLOCK_TAG_SETS.get(tagId);
            if (resolved != null) {
                out.addAll(resolved);
            }
            return;
        }
        Block block = resolveBlockId(name);
        if (block != null) {
            out.add(block);
        } else if (!isIgnoredMissingBlock(name)) {
            AlexsMobs.LOGGER.warn("Unknown block in tag {}: {}", resourcePath, name);
        }
    }

    private static void addLeafItems(Set<Item> out) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof BlockLeaves) {
                Item item = Item.getItemFromBlock(block);
                if (item != null) {
                    out.add(item);
                }
            }
        }
    }

    private static void addSaplingItems(Set<Item> out) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof BlockSapling) {
                Item item = Item.getItemFromBlock(block);
                if (item != null) {
                    out.add(item);
                }
            }
        }
    }

    private static void addLeafBlocks(Set<Block> out) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof BlockLeaves) {
                out.add(block);
            }
        }
    }

    private static void addSaplingBlocks(Set<Block> out) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof BlockSapling) {
                out.add(block);
            }
        }
    }

    /**
     * Resolves 1.16 {@code #minecraft:*} / {@code #forge:*} block tag ids to 1.12.2 block sets (no tag JSON files in 1.12).
     */
    private static boolean resolveVanillaBlockTag(String name, Set<Block> out) {
        switch (name) {
            case "#minecraft:sand":
                out.add(Blocks.SAND);
                addOreDictionaryBlocks(out, "sand");
                return true;
            case "#forge:gravel":
            case "#minecraft:gravel":
                out.add(Blocks.GRAVEL);
                addOreDictionaryBlocks(out, "gravel");
                return true;
            case "#forge:dirt":
            case "#minecraft:dirt":
                out.add(Blocks.DIRT);
                out.add(Blocks.GRASS);
                out.add(Blocks.FARMLAND);
                out.add(Blocks.MYCELIUM);
                addOreDictionaryBlocks(out, "dirt");
                return true;
            case "#minecraft:ice":
                out.add(Blocks.ICE);
                out.add(Blocks.PACKED_ICE);
                return true;
            case "#minecraft:base_stone_overworld":
                out.add(Blocks.STONE);
                out.add(Blocks.COBBLESTONE);
                return true;
            case "#minecraft:coral_blocks":
                out.add(Blocks.SAND);
                out.add(Blocks.GRAVEL);
                out.add(Blocks.SPONGE);
                addOreDictionaryBlocks(out, "sand");
                addOreDictionaryBlocks(out, "gravel");
                return true;
            case "#minecraft:logs":
                addLogBlocks(out);
                addOreDictionaryBlocks(out, "logWood");
                return true;
            case "#minecraft:soul_fire_base_blocks":
                out.add(Blocks.NETHERRACK);
                out.add(Blocks.SOUL_SAND);
                return true;
            case "#minecraft:crops":
                for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                    if (block instanceof BlockCrops) {
                        out.add(block);
                    }
                }
                return true;
            case "#minecraft:planks":
                addOreDictionaryBlocks(out, "plankWood");
                for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                    if (block instanceof BlockPlanks) {
                        out.add(block);
                    }
                }
                return true;
            case "#minecraft:wooden_stairs":
                for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                    if (block instanceof BlockStairs) {
                        out.add(block);
                    }
                }
                return true;
            case "#minecraft:wooden_slabs":
                for (Block block : ForgeRegistries.BLOCKS.getValues()) {
                    if (block instanceof BlockSlab) {
                        out.add(block);
                    }
                }
                return true;
            case "#minecraft:wool":
                out.add(Blocks.WOOL);
                out.add(Blocks.CARPET);
                return true;
            case "#minecraft:terracotta":
                addTerracottaBlocks(out);
                return true;
            default:
                return false;
        }
    }

    private static void addTerracottaBlocks(Set<Block> out) {
        out.add(Blocks.HARDENED_CLAY);
        out.add(Blocks.STAINED_HARDENED_CLAY);
    }

    private static void addLogBlocks(Set<Block> out) {
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof BlockLog) {
                out.add(block);
            }
        }
    }

    private static void addOreDictionaryItems(Set<Item> out, String oreName) {
        for (ItemStack stack : OreDictionary.getOres(oreName)) {
            if (!stack.isEmpty()) {
                out.add(stack.getItem());
            }
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static void addOreDictionaryBlocks(Set<Block> out, String oreName) {
        for (ItemStack stack : OreDictionary.getOres(oreName)) {
            if (!stack.isEmpty() && stack.getItem() != null) {
                Block block = Block.getBlockFromItem(stack.getItem());
                if (block != null) {
                    out.add(block);
                }
            }
        }
    }

    /**
     * Maps 1.13+ entity type ids from tag JSON to 1.12.2 registry names (or {@code null} when there is no equivalent).
     */
    private static ResourceLocation resolveEntityTypeId(String name) {
        switch (name) {
            case "minecraft:drowned":
                return new ResourceLocation("minecraft", "zombie");
            case "minecraft:cod":
            case "minecraft:salmon":
            case "minecraft:tropical_fish":
            case "minecraft:dolphin":
                return new ResourceLocation("minecraft", "squid");
            case "minecraft:trader_llama":
                return new ResourceLocation("minecraft", "llama");
            case "minecraft:fox":
            case "minecraft:panda":
            case "minecraft:strider":
            case "minecraft:wandering_trader":
            case "minecraft:phantom":
            case "minecraft:frog":
            case "minecraft:turtle":
                return null;
            default:
                break;
        }
        ResourceLocation id = new ResourceLocation(name);
        return ForgeRegistries.ENTITIES.containsKey(id) ? id : null;
    }

    /**
     * Maps 1.13+ item ids from tag JSON to 1.12.2 items. Tags compare {@link Item} only (not stack metadata), so fish
     * variants map to {@link Items#FISH} / {@link Items#COOKED_FISH} as a whole.
     */
    private static Item resolveItemId(String name) {
        switch (name) {
            case "minecraft:cod":
            case "minecraft:salmon":
            case "minecraft:tropical_fish":
            case "minecraft:pufferfish":
                return Items.FISH;
            case "minecraft:cooked_cod":
            case "minecraft:cooked_salmon":
                return Items.COOKED_FISH;
            case "minecraft:pufferfish_bucket":
                return Items.FISH;
            case "minecraft:creeper_head":
                return Items.SKULL;
            case "minecraft:sugar_cane":
            case "minecraft:bamboo":
                return Items.REEDS;
            case "minecraft:sweet_berries":
                return Items.APPLE;
            case "minecraft:melon_slice":
                return Items.MELON;
            case "minecraft:dead_bush":
                return Item.getItemFromBlock(Blocks.DEADBUSH);
            case "minecraft:honeycomb":
            case "minecraft:honey_bottle":
                return Items.GOLDEN_APPLE;
            case "minecraft:honey_block":
                return Item.getItemFromBlock(Blocks.MELON_BLOCK);
            case "minecraft:honeycomb_block":
                return Item.getItemFromBlock(Blocks.HAY_BLOCK);
            case "minecraft:turtle_egg":
                return Items.EGG;
            case "minecraft:seagrass":
                return Item.getItemFromBlock(Blocks.WATERLILY);
            case "minecraft:bee_nest":
                return Item.getItemFromBlock(Blocks.PUMPKIN);
            case "minecraft:yellow_flower":
                return Item.getItemFromBlock(Blocks.YELLOW_FLOWER);
            case "minecraft:grass":
                return Item.getItemFromBlock(Blocks.TALLGRASS);
            default:
                break;
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
        if (item != null) {
            return item;
        }
        Block block = resolveBlockId(name);
        return block != null ? Item.getItemFromBlock(block) : null;
    }

    private static Block resolveBlockId(String name) {
        switch (name) {
            case "minecraft:melon":
                return Blocks.MELON_BLOCK;
            case "minecraft:frosted_ice":
                return Blocks.FROSTED_ICE;
            case "minecraft:lily_pad":
                return Blocks.WATERLILY;
            case "minecraft:grass_block":
            case "minecraft:grass":
                return Blocks.GRASS;
            case "minecraft:tall_grass":
                return Blocks.TALLGRASS;
            case "minecraft:large_fern":
                return Blocks.DOUBLE_PLANT;
            case "minecraft:vine":
            case "minecraft:glow_lichen":
                return Blocks.VINE;
            case "minecraft:bamboo":
                return Blocks.REEDS;
            case "minecraft:bee_nest":
                return Blocks.PUMPKIN;
            case "minecraft:warped_wart_block":
                return Blocks.NETHER_WART_BLOCK;
            case "minecraft:crimson_nylium":
            case "minecraft:warped_nylium":
                return Blocks.NETHERRACK;
            case "minecraft:snow_block":
                return Blocks.SNOW;
            case "minecraft:podzol":
                return Blocks.DIRT;
            case "minecraft:terracotta":
            case "minecraft:white_terracotta":
                return Blocks.HARDENED_CLAY;
            case "minecraft:orange_terracotta":
            case "minecraft:magenta_terracotta":
            case "minecraft:light_blue_terracotta":
            case "minecraft:yellow_terracotta":
            case "minecraft:lime_terracotta":
            case "minecraft:pink_terracotta":
            case "minecraft:gray_terracotta":
            case "minecraft:light_gray_terracotta":
            case "minecraft:cyan_terracotta":
            case "minecraft:purple_terracotta":
            case "minecraft:blue_terracotta":
            case "minecraft:brown_terracotta":
            case "minecraft:green_terracotta":
            case "minecraft:red_terracotta":
            case "minecraft:black_terracotta":
                return Blocks.STAINED_HARDENED_CLAY;
            case "minecraft:sweet_berry_bush":
                return null;
            default:
                break;
        }
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(name));
    }

    private static boolean isIgnoredMissingItem(String name) {
        return false;
    }

    private static boolean isIgnoredMissingBlock(String name) {
        return "minecraft:sweet_berry_bush".equals(name) || "minecraft:water".equals(name);
    }

}
