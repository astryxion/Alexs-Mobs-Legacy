package com.github.alexthe666.alexsmobs.world.spawn;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Direct {@link Biome.SpawnListEntry} mutation — same approach as Familiar Fauna {@code ModEntities.addSpawn},
 * with duplicate removal so reloads and client/server init cannot stack the same mob.
 */
public final class AMSpawnUtil {

    private AMSpawnUtil() {
    }

    /**
     * Removes every spawn list row for {@code entityClass} on {@code typeOfCreature} in the given biomes.
     */
    public static void removeSpawn(Class<? extends EntityLiving> entityClass, EnumCreatureType typeOfCreature, List<String> biomes) {
        if (biomes == null) {
            return;
        }
        for (String biomeName : biomes) {
            ResourceLocation loc = new ResourceLocation(biomeName);
            if (!ForgeRegistries.BIOMES.containsKey(loc)) {
                continue;
            }
            Biome biome = ForgeRegistries.BIOMES.getValue(loc);
            removeAllEntries(biome.getSpawnableList(typeOfCreature), entityClass);
        }
    }

    /**
     * Strips all Alex's Mobs spawn rows from every biome before a fresh registration pass.
     */
    public static void purgeSpawns(Collection<Class<? extends EntityLiving>> entityClasses) {
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            for (EnumCreatureType creatureType : EnumCreatureType.values()) {
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

    /**
     * Ensures exactly one spawn row per biome: clears duplicates, then sets weight and group size.
     */
    public static int addSpawn(Class<? extends EntityLiving> entityClass, int weightedProb, int min, int max,
                              EnumCreatureType typeOfCreature, List<String> biomes) {
        if (biomes == null || biomes.isEmpty() || weightedProb <= 0) {
            return 0;
        }
        int minGroup = Math.max(1, Math.min(min, max));
        int maxGroup = Math.max(minGroup, max);
        int added = 0;
        for (String biomeName : biomes) {
            ResourceLocation loc = new ResourceLocation(biomeName);
            if (!ForgeRegistries.BIOMES.containsKey(loc)) {
                continue;
            }
            Biome biome = ForgeRegistries.BIOMES.getValue(loc);
            List<Biome.SpawnListEntry> spawns = biome.getSpawnableList(typeOfCreature);
            removeAllEntries(spawns, entityClass);
            spawns.add(new Biome.SpawnListEntry(entityClass, weightedProb, minGroup, maxGroup));
            added++;
        }
        return added;
    }

    private static void removeAllEntries(List<Biome.SpawnListEntry> spawns, Class<? extends EntityLiving> entityClass) {
        Iterator<Biome.SpawnListEntry> iterator = spawns.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().entityClass == entityClass) {
                iterator.remove();
            }
        }
    }
}
