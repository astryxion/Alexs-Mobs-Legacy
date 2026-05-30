package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Dimension filtering and optional chunk-gen spawn pass (Mo Creatures event hooks).
 */
@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public final class AMSpawnEvents {

    private static final Map<Biome, List<Biome.SpawnListEntry>> CREATURE_SPAWN_MAP = new HashMap<>();
    private static final Map<Biome, List<Biome.SpawnListEntry>> WATER_CREATURE_SPAWN_MAP = new HashMap<>();

    private AMSpawnEvents() {
    }

    public static void buildWorldGenSpawnLists() {
        CREATURE_SPAWN_MAP.clear();
        WATER_CREATURE_SPAWN_MAP.clear();
        if (!AMSpawnRegistry.isRegistered()) {
            return;
        }
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            CREATURE_SPAWN_MAP.put(biome, filterAlexsMobsEntries(biome.getSpawnableList(EnumCreatureType.CREATURE)));
            WATER_CREATURE_SPAWN_MAP.put(biome, filterAlexsMobsEntries(biome.getSpawnableList(EnumCreatureType.WATER_CREATURE)));
        }
    }

    private static List<Biome.SpawnListEntry> filterAlexsMobsEntries(List<Biome.SpawnListEntry> source) {
        List<Biome.SpawnListEntry> filtered = new ArrayList<>();
        for (Biome.SpawnListEntry entry : source) {
            if (entry.itemWeight > 0 && AMSpawnRegistry.getSpawnData(entry.entityClass) != null) {
                filtered.add(entry);
            }
        }
        return filtered;
    }

    @SubscribeEvent
    public static void onLivingSpawn(LivingSpawnEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityLiving)) {
            return;
        }
        EntityLiving living = (EntityLiving) entity;
        AMSpawnData data = AMSpawnRegistry.getSpawnData(living.getClass());
        if (data == null) {
            return;
        }
        World world = event.getWorld();
        int dimension = world.provider.getDimension();
        boolean allowed = false;
        for (int dim : data.dimensions) {
            if (dim == dimension) {
                allowed = true;
                break;
            }
        }
        if (!allowed || data.weight.getAsInt() <= 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onPopulateChunk(PopulateChunkEvent.Populate event) {
        if (event.getWorld().isRemote || event.getType() != PopulateChunkEvent.Populate.EventType.ANIMALS) {
            return;
        }
        if (CREATURE_SPAWN_MAP.isEmpty() && WATER_CREATURE_SPAWN_MAP.isEmpty()) {
            return;
        }
        int chunkX = event.getChunkX() * 16;
        int chunkZ = event.getChunkZ() * 16;
        int centerX = chunkX + 8;
        int centerZ = chunkZ + 8;
        World world = event.getWorld();
        Random rand = event.getRand();
        BlockPos sample = new BlockPos(chunkX, 0, chunkZ);
        Biome biome = world.getBiome(sample.add(16, 0, 16));

        List<Biome.SpawnListEntry> creatures = CREATURE_SPAWN_MAP.get(biome);
        if (creatures != null && !creatures.isEmpty()) {
            AMSpawnWorldGenHelper.performCustomWorldGenSpawning(world, biome, centerX, centerZ, 16, 16, rand, creatures,
                    EntityLiving.SpawnPlacementType.ON_GROUND);
        }
        List<Biome.SpawnListEntry> waterCreatures = WATER_CREATURE_SPAWN_MAP.get(biome);
        if (waterCreatures != null && !waterCreatures.isEmpty()) {
            AMSpawnWorldGenHelper.performCustomWorldGenSpawning(world, biome, centerX, centerZ, 16, 16, rand, waterCreatures,
                    EntityLiving.SpawnPlacementType.IN_WATER);
        }
    }
}
