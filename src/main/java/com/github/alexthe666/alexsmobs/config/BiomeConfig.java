package com.github.alexthe666.alexsmobs.config;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.citadel.config.biome.BiomeEntryType;
import com.github.alexthe666.citadel.config.biome.SpawnBiomeConfig;
import com.github.alexthe666.citadel.config.biome.SpawnBiomeData;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BiomeConfig {
	public static Pair<String, SpawnBiomeData> grizzlyBear = Pair.of("alexsmobs:grizzly_bear_spawns", DefaultBiomes.GRIZZLY_BEAR);
	public static Pair<String, SpawnBiomeData> roadrunner = Pair.of("alexsmobs:roadrunner_spawns", DefaultBiomes.ROADRUNNER);
	public static Pair<String, SpawnBiomeData> boneSerpent = Pair.of("alexsmobs:bone_serpent_spawns", DefaultBiomes.BONE_SERPENT);
	public static Pair<String, SpawnBiomeData> gazelle = Pair.of("alexsmobs:gazelle_spawns", DefaultBiomes.GAZELLE);
	public static Pair<String, SpawnBiomeData> crocodile = Pair.of("alexsmobs:crocodile_spawns", DefaultBiomes.CROCODILE);
	public static Pair<String, SpawnBiomeData> fly = Pair.of("alexsmobs:fly_spawns", DefaultBiomes.FLY);
	public static Pair<String, SpawnBiomeData> hummingbird = Pair.of("alexsmobs:hummingbird_spawns", DefaultBiomes.HUMMINGBIRD);
	public static Pair<String, SpawnBiomeData> orca = Pair.of("alexsmobs:orca_spawns", DefaultBiomes.ORCA);
	public static Pair<String, SpawnBiomeData> sunbird = Pair.of("alexsmobs:sunbird_spawns", DefaultBiomes.SUNBIRD);
	public static Pair<String, SpawnBiomeData> gorilla = Pair.of("alexsmobs:gorilla_spawns", DefaultBiomes.GORILLA);
	public static Pair<String, SpawnBiomeData> anteater = Pair.of("alexsmobs:anteater_spawns", DefaultBiomes.ANTEATER);
	public static Pair<String, SpawnBiomeData> crimsonMosquito = Pair.of("alexsmobs:crimson_mosquito_spawns", DefaultBiomes.CRIMSON_MOSQUITO);
	public static Pair<String, SpawnBiomeData> rattlesnake = Pair.of("alexsmobs:rattlesnake_spawns", DefaultBiomes.RATTLESNAKE);
	public static Pair<String, SpawnBiomeData> endergrade = Pair.of("alexsmobs:endergrade_spawns", DefaultBiomes.ENDERGRADE);
	public static Pair<String, SpawnBiomeData> hammerheadShark = Pair.of("alexsmobs:hammerhead_shark_spawns", DefaultBiomes.HAMMERHEAD);
	public static Pair<String, SpawnBiomeData> lobster = Pair.of("alexsmobs:lobster_spawns", DefaultBiomes.LOBSTER);
	public static Pair<String, SpawnBiomeData> komodoDragon = Pair.of("alexsmobs:komodo_dragon_spawns", DefaultBiomes.KOMODO_DRAGON);
	public static Pair<String, SpawnBiomeData> capuchinMonkey = Pair.of("alexsmobs:capuchin_monkey_spawns", DefaultBiomes.CAPUCHIN_MONKEY);
	public static Pair<String, SpawnBiomeData> caveCentipede = Pair.of("alexsmobs:cave_centipede_spawns", DefaultBiomes.CENTIPEDE);
	public static Pair<String, SpawnBiomeData> warpedToad = Pair.of("alexsmobs:warped_toad_spawns", DefaultBiomes.WARPED_TOAD);
	public static Pair<String, SpawnBiomeData> moose = Pair.of("alexsmobs:moose_spawns", DefaultBiomes.MOOSE);
	public static Pair<String, SpawnBiomeData> mimicube = Pair.of("alexsmobs:mimicube_spawns", DefaultBiomes.MIMICUBE);
	public static Pair<String, SpawnBiomeData> raccoon = Pair.of("alexsmobs:raccoon_spawns", DefaultBiomes.RACCOON);
	public static Pair<String, SpawnBiomeData> blobfish = Pair.of("alexsmobs:blobfish_spawns", DefaultBiomes.DEEP_SEA);
	public static Pair<String, SpawnBiomeData> seal = Pair.of("alexsmobs:seal_spawns", DefaultBiomes.SEAL);
	public static Pair<String, SpawnBiomeData> cockroach = Pair.of("alexsmobs:cockroach_spawns", DefaultBiomes.COCKROACH);
	public static Pair<String, SpawnBiomeData> shoebill = Pair.of("alexsmobs:shoebill_spawns", DefaultBiomes.SHOEBILL);
	public static Pair<String, SpawnBiomeData> elephant = Pair.of("alexsmobs:elephant_spawns", DefaultBiomes.ELEPHANT);
	public static Pair<String, SpawnBiomeData> soulVulture = Pair.of("alexsmobs:soul_vulture_spawns", DefaultBiomes.SOUL_VULTURE);
	public static Pair<String, SpawnBiomeData> snowLeopard = Pair.of("alexsmobs:snow_leopard_spawns", DefaultBiomes.SNOW_LEOPARD);
	public static Pair<String, SpawnBiomeData> spectre = Pair.of("alexsmobs:spectre_spawns", DefaultBiomes.SPECTRE);
	public static Pair<String, SpawnBiomeData> crow = Pair.of("alexsmobs:crow_spawns", DefaultBiomes.CROW);
	public static Pair<String, SpawnBiomeData> alligatorSnappingTurtle = Pair.of("alexsmobs:alligator_snapping_turtle_spawns", DefaultBiomes.ALLIGATOR_SNAPPING_TURTLE);
	public static Pair<String, SpawnBiomeData> mungus = Pair.of("alexsmobs:mungus_spawns", DefaultBiomes.MUNGUS);
	public static Pair<String, SpawnBiomeData> mantisShrimp = Pair.of("alexsmobs:mantis_shrimp_spawns", DefaultBiomes.MANTIS_SHRIMP);
	public static Pair<String, SpawnBiomeData> guster = Pair.of("alexsmobs:guster_spawns", DefaultBiomes.GUSTER);
	public static Pair<String, SpawnBiomeData> warpedMosco = Pair.of("alexsmobs:warped_mosco_spawns", DefaultBiomes.EMPTY);
	public static Pair<String, SpawnBiomeData> straddler = Pair.of("alexsmobs:straddler_spawns", DefaultBiomes.STRADDLER);
	public static Pair<String, SpawnBiomeData> stradpole = Pair.of("alexsmobs:stradpole_spawns", DefaultBiomes.STRADDLER);
	public static Pair<String, SpawnBiomeData> emu = Pair.of("alexsmobs:emu_spawns", DefaultBiomes.SAVANNA_AND_MESA);
	public static Pair<String, SpawnBiomeData> platypus = Pair.of("alexsmobs:platypus_spawns", DefaultBiomes.PLATYPUS);
	public static Pair<String, SpawnBiomeData> dropbear = Pair.of("alexsmobs:dropbear_spawns", DefaultBiomes.DROPBEAR);
	public static Pair<String, SpawnBiomeData> tasmanianDevil = Pair.of("alexsmobs:tasmanian_devil_spawns", DefaultBiomes.TASMANIAN_DEVIL);
	public static Pair<String, SpawnBiomeData> kangaroo = Pair.of("alexsmobs:kangaroo_spawns", DefaultBiomes.SAVANNA_AND_MESA);
	public static Pair<String, SpawnBiomeData> cachalot_whale_spawns = Pair.of("alexsmobs:cachalot_whale_spawns", DefaultBiomes.CACHALOT_WHALE);
	public static Pair<String, SpawnBiomeData> cachalot_whale_beached_spawns = Pair.of("alexsmobs:cachalot_whale_beached_spawns", DefaultBiomes.BEACHED_CACHALOT_WHALE);
	public static Pair<String, SpawnBiomeData> leafcutter_anthill_spawns = Pair.of("alexsmobs:leafcutter_anthill_spawns", DefaultBiomes.LEAFCUTTER_ANTHILL);
	public static Pair<String, SpawnBiomeData> enderiophage_spawns = Pair.of("alexsmobs:enderiophage_spawns", DefaultBiomes.ENDERIOPHAGE);
	public static Pair<String, SpawnBiomeData> baldEagle = Pair.of("alexsmobs:bald_eagle_spawns", DefaultBiomes.BALD_EAGLE);
	public static Pair<String, SpawnBiomeData> tiger = Pair.of("alexsmobs:tiger_spawns", DefaultBiomes.TIGER);
	public static Pair<String, SpawnBiomeData> tarantula_hawk = Pair.of("alexsmobs:tarantula_hawk_spawns", DefaultBiomes.TARANTULA_HAWK);
	public static Pair<String, SpawnBiomeData> void_worm = Pair.of("alexsmobs:void_worm_spawns", DefaultBiomes.EMPTY);
	public static Pair<String, SpawnBiomeData> frilled_shark = Pair.of("alexsmobs:frilled_shark_spawns", DefaultBiomes.DEEP_SEA);
	public static Pair<String, SpawnBiomeData> mimic_octopus = Pair.of("alexsmobs:mimic_octopus_spawns", DefaultBiomes.MIMIC_OCTOPUS);
	public static Pair<String, SpawnBiomeData> seagull = Pair.of("alexsmobs:seagull_spawns", DefaultBiomes.SEAGULL);
	public static Pair<String, SpawnBiomeData> tusklin = Pair.of("alexsmobs:tusklin_spawns", DefaultBiomes.TUSKLIN);
	public static Pair<String, SpawnBiomeData> banana_slug = Pair.of("alexsmobs:banana_slug_spawns", DefaultBiomes.BANANA_SLUG);
	public static Pair<String, SpawnBiomeData> anaconda = Pair.of("alexsmobs:anaconda_spawns", DefaultBiomes.ANACONDA);
	public static Pair<String, SpawnBiomeData> laviathan = Pair.of("alexsmobs:laviathan_spawns", DefaultBiomes.ALL_NETHER);
	public static Pair<String, SpawnBiomeData> giant_squid = Pair.of("alexsmobs:giant_squid_spawns", DefaultBiomes.DEEP_SEA);
	public static Pair<String, SpawnBiomeData> geladaMonkey = Pair.of("alexsmobs:gelada_monkey_spawns", DefaultBiomes.GELADA_MONKEY);

	private static boolean init = false;
	private static Map<String, SpawnBiomeData> biomeConfigValues = new HashMap<>();

    public static void init() {
        try {
            for (Field f : BiomeConfig.class.getDeclaredFields()) {
                Object obj = f.get(null);
               if(obj instanceof Pair){
				   String id = (String)((Pair) obj).getLeft();
				   SpawnBiomeData data = (SpawnBiomeData)((Pair) obj).getRight();
				   biomeConfigValues.put(id, SpawnBiomeConfig.create(new ResourceLocation(id), data));
               }
            }
        }catch (Exception e){
            AlexsMobs.LOGGER.warn("Encountered error building alexsmobs biome config .json files");
            e.printStackTrace();
        }
		init = true;
    }

    public static boolean test(Pair<String, SpawnBiomeData> entry, Biome biome){
    	if(!init){
    		return false;
		}
		SpawnBiomeData data = biomeConfigValues.get(entry.getKey());
		if (data == null) {
			return false;
		}
		return SpawnBiomeMatcher112.matches(data, biome);
	}

	/**
	 * 1.12.2 spawn biome matching: Citadel's {@link SpawnBiomeData#matches} compares {@code BIOME_CATEGORY} to Java
	 * class simple names and does not map 1.16 registry ids to 1.12 ids. This matcher preserves JSON/config semantics
	 * while resolving both issues for existing and default spawn configs.
	 */
	private static final class SpawnBiomeMatcher112 {

		private static Field biomesField;
		private static Field entryTypeField;
		private static Field entryNegateField;
		private static Field entryValueField;

		private static final Map<String, Set<String>> REGISTRY_ALIASES = ImmutableMap.<String, Set<String>>builder()
				.put("minecraft:the_end", ImmutableSet.of("minecraft:sky"))
				.put("minecraft:end_barrens", ImmutableSet.of("minecraft:sky"))
				.put("minecraft:end_highlands", ImmutableSet.of("minecraft:sky"))
				.put("minecraft:small_end_islands", ImmutableSet.of("minecraft:sky"))
				.put("minecraft:flower_forest", ImmutableSet.of("minecraft:mutated_forest"))
				.put("minecraft:sunflower_plains", ImmutableSet.of("minecraft:mutated_plains"))
				.put("minecraft:bamboo_jungle", ImmutableSet.of("minecraft:jungle", "minecraft:jungle_hills"))
				.put("minecraft:bamboo_jungle_hills", ImmutableSet.of("minecraft:jungle_hills"))
				.put("minecraft:crimson_forest", ImmutableSet.of("minecraft:hell"))
				.put("minecraft:warped_forest", ImmutableSet.of("minecraft:hell"))
				.put("minecraft:soul_sand_valley", ImmutableSet.of("minecraft:hell"))
				.put("minecraft:basalt_deltas", ImmutableSet.of("minecraft:hell"))
				.put("minecraft:nether_wastes", ImmutableSet.of("minecraft:hell"))
				.put("minecraft:mushroom_fields", ImmutableSet.of("minecraft:mushroom_island"))
				.put("minecraft:deep_lukewarm_ocean", ImmutableSet.of("minecraft:deep_ocean"))
				.put("minecraft:lukewarm_ocean", ImmutableSet.of("minecraft:ocean"))
				.put("minecraft:deep_warm_ocean", ImmutableSet.of("minecraft:deep_ocean"))
				.put("minecraft:deep_cold_ocean", ImmutableSet.of("minecraft:deep_ocean"))
				.put("minecraft:deep_frozen_ocean", ImmutableSet.of("minecraft:deep_ocean"))
				.put("minecraft:warm_ocean", ImmutableSet.of("minecraft:ocean"))
				.put("minecraft:cold_ocean", ImmutableSet.of("minecraft:frozen_ocean"))
				.put("minecraft:ice_spikes", ImmutableSet.of("minecraft:mutated_ice_flats"))
				.put("minecraft:snowy_plains", ImmutableSet.of("minecraft:ice_plains"))
				.put("minecraft:dark_forest", ImmutableSet.of("minecraft:roofed_forest", "minecraft:mutated_roofed_forest"))
				.put("minecraft:birch_forest", ImmutableSet.of("minecraft:birch_forest", "minecraft:birch_forest_hills", "minecraft:mutated_birch_forest"))
				.put("minecraft:old_growth_birch_forest", ImmutableSet.of("minecraft:mutated_birch_forest", "minecraft:mutated_birch_forest_hills"))
				.put("minecraft:modified_jungle_edge", ImmutableSet.of("minecraft:jungle_edge", "minecraft:mutated_jungle"))
				.put("minecraft:jungle_egde", ImmutableSet.of("minecraft:jungle_edge"))
				.build();

		static {
			try {
				biomesField = SpawnBiomeData.class.getDeclaredField("biomes");
				biomesField.setAccessible(true);
				Class<?> entryClass = Class.forName("com.github.alexthe666.citadel.config.biome.SpawnBiomeData$SpawnBiomeEntry");
				entryTypeField = entryClass.getDeclaredField("type");
				entryTypeField.setAccessible(true);
				entryNegateField = entryClass.getDeclaredField("negate");
				entryNegateField.setAccessible(true);
				entryValueField = entryClass.getDeclaredField("value");
				entryValueField.setAccessible(true);
			} catch (Exception e) {
				throw new RuntimeException("Failed to initialize 1.12.2 spawn biome matcher reflection", e);
			}
		}

		private SpawnBiomeMatcher112() {
		}

		static boolean matches(SpawnBiomeData data, Biome biomeIn) {
			if (biomeIn == null || biomeIn.getRegistryName() == null) {
				return false;
			}
			try {
				@SuppressWarnings("unchecked")
				List<List<Object>> pools = (List<List<Object>>) biomesField.get(data);
				if (pools == null || pools.isEmpty()) {
					return false;
				}
				for (List<Object> pool : pools) {
					boolean overall = true;
					for (Object entry : pool) {
						if (!matchesEntry(entry, biomeIn)) {
							overall = false;
						}
					}
					if (overall) {
						return true;
					}
				}
			} catch (Exception e) {
				AlexsMobs.LOGGER.warn("Failed to evaluate spawn biome data for {}", biomeIn.getRegistryName(), e);
			}
			return false;
		}

		private static boolean matchesEntry(Object entry, Biome biomeIn) throws IllegalAccessException {
			BiomeEntryType type = (BiomeEntryType) entryTypeField.get(entry);
			boolean negate = (Boolean) entryNegateField.get(entry);
			String value = (String) entryValueField.get(entry);
			boolean matched;
			if (type == BiomeEntryType.BIOME_DICT) {
				matched = hasBiomeDictType(biomeIn, value);
			} else if (type == BiomeEntryType.BIOME_CATEGORY) {
				matched = matchesBiomeCategory(biomeIn, value);
			} else {
				matched = matchesRegistryName(biomeIn, value, negate);
			}
			return negate ? !matched : matched;
		}

		private static boolean hasBiomeDictType(Biome biomeIn, String value) {
			String key = value.toLowerCase(Locale.ROOT);
			if ("overworld".equals(key)) {
				return !BiomeDictionary.hasType(biomeIn, BiomeDictionary.Type.NETHER)
						&& !BiomeDictionary.hasType(biomeIn, BiomeDictionary.Type.END);
			}
			if ("nether".equals(key)) {
				return BiomeDictionary.hasType(biomeIn, BiomeDictionary.Type.NETHER);
			}
			if ("end".equals(key)) {
				return BiomeDictionary.hasType(biomeIn, BiomeDictionary.Type.END);
			}
			List<String> biomeTypes = BiomeDictionary.getTypes(biomeIn).stream()
					.map(t -> t.getName().toLowerCase(Locale.ROOT))
					.collect(Collectors.toList());
			return biomeTypes.contains(key);
		}

		private static boolean matchesBiomeCategory(Biome biomeIn, String category) {
			String key = category.toLowerCase(Locale.ROOT);
			switch (key) {
				case "forest":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.FOREST);
				case "desert":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.DRY, BiomeDictionary.Type.HOT);
				case "savanna":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.SAVANNA);
				case "ocean":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.OCEAN);
				case "jungle":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.JUNGLE);
				case "swamp":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.SWAMP);
				case "mountain":
				case "extreme_hills":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.MOUNTAIN);
				case "plains":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.PLAINS);
				case "mesa":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.MESA);
				case "icy":
				case "snowy":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.COLD);
				case "river":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.RIVER);
				case "beach":
					return hasAnyDict(biomeIn, BiomeDictionary.Type.BEACH);
				default:
					return biomeIn.getBiomeClass().getSimpleName().toLowerCase(Locale.ROOT).contains(key);
			}
		}

		private static boolean hasAnyDict(Biome biomeIn, BiomeDictionary.Type... types) {
			for (BiomeDictionary.Type type : types) {
				if (BiomeDictionary.hasType(biomeIn, type)) {
					return true;
				}
			}
			return false;
		}

		private static boolean matchesRegistryName(Biome biomeIn, String configValue, boolean negate) {
			String biomeId = biomeIn.getRegistryName().toString();
			if (negate) {
				return biomeId.equals(configValue) || AMBiomes112.resolve(configValue) != null && biomeId.equals(AMBiomes112.resolve(configValue));
			}
			Set<String> acceptable = new HashSet<>(AMBiomes112.acceptableIdsForConfigValue(configValue));
			Set<String> aliases = REGISTRY_ALIASES.get(configValue);
			if (aliases != null) {
				for (String alias : aliases) {
					String resolved = AMBiomes112.resolve(alias);
					if (resolved != null) {
						acceptable.add(resolved);
					}
					acceptable.add(alias);
				}
			}
			return acceptable.contains(biomeId);
		}
	}
}
