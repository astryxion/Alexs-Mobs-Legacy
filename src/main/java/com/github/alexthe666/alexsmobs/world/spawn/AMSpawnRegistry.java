package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mo Creatures-style spawn registration: scan biomes, {@link EntityRegistry#addSpawn}, dimension map for events.
 */
public final class AMSpawnRegistry {

    private static final Map<Class<? extends EntityLiving>, AMSpawnData> ENTITY_SPAWN_MAP = new HashMap<>();
    private static boolean registered;

    private AMSpawnRegistry() {
    }

    public static Map<Class<? extends EntityLiving>, AMSpawnData> getEntitySpawnMap() {
        return Collections.unmodifiableMap(ENTITY_SPAWN_MAP);
    }

    public static AMSpawnData getSpawnData(Class<? extends EntityLiving> entityClass) {
        return ENTITY_SPAWN_MAP.get(entityClass);
    }

    /**
     * Registers all natural spawns on the logical server. Purges prior Alex's Mobs rows first.
     */
    public static void register() {
        if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER) {
            return;
        }
        purgeAlexsMobsSpawns(collectEntityClasses());
        ENTITY_SPAWN_MAP.clear();

        int mobsEnabled = 0;
        int biomeRows = 0;
        for (AMSpawnData data : AMSpawnDefinitions.all()) {
            if (!data.shouldRegister()) {
                continue;
            }
            List<Biome> spawnBiomes = resolveBiomes(data);
            if (spawnBiomes.isEmpty()) {
                AlexsMobs.LOGGER.warn("Alex's Mobs: no valid 1.12.2 biomes for {} — check biome config",
                        data.entityClass.getSimpleName());
                continue;
            }
            int weight = data.weight.getAsInt();
            int minGroup = Math.max(1, Math.min(data.minGroup, data.maxGroup));
            int maxGroup = Math.max(minGroup, data.maxGroup);
            EntityRegistry.addSpawn(data.entityClass, weight, minGroup, maxGroup, data.creatureType,
                    spawnBiomes.toArray(new Biome[0]));
            ENTITY_SPAWN_MAP.putIfAbsent(data.entityClass, data);
            mobsEnabled++;
            biomeRows += spawnBiomes.size();
        }
        registered = true;
        AlexsMobs.LOGGER.info("Alex's Mobs: spawn registry applied ({} mobs, {} biome rows)", mobsEnabled, biomeRows);
    }

    public static boolean isRegistered() {
        return registered;
    }

    private static List<Biome> resolveBiomes(AMSpawnData data) {
        List<Biome> spawnBiomes = new ArrayList<>();
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            if (data.biomeFilter.test(biome)) {
                spawnBiomes.add(biome);
            }
        }
        return spawnBiomes;
    }

    private static Set<Class<? extends EntityLiving>> collectEntityClasses() {
        Set<Class<? extends EntityLiving>> classes = new HashSet<>();
        for (AMSpawnData data : AMSpawnDefinitions.all()) {
            classes.add(data.entityClass);
        }
        return classes;
    }

    static void purgeAlexsMobsSpawns(Set<Class<? extends EntityLiving>> entityClasses) {
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            for (net.minecraft.entity.EnumCreatureType creatureType : net.minecraft.entity.EnumCreatureType.values()) {
                List<Biome.SpawnListEntry> spawns = biome.getSpawnableList(creatureType);
                Iterator<Biome.SpawnListEntry> iterator = spawns.iterator();
                while (iterator.hasNext()) {
                    if (entityClasses.contains(iterator.next().entityClass)) {
                        iterator.remove();
                    }
                }
            }
        }
    }
}
