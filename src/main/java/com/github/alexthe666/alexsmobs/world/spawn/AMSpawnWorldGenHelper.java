package com.github.alexthe666.alexsmobs.world.spawn;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;
import java.util.Random;

/**
 * Chunk-generation spawn pass (Mo Creatures {@code MoCTools#performCustomWorldGenSpawning}).
 */
public final class AMSpawnWorldGenHelper {

    private AMSpawnWorldGenHelper() {
    }

    public static void performCustomWorldGenSpawning(World world, Biome biome, int centerX, int centerZ,
                                                     int diameterX, int diameterZ, Random random,
                                                     List<Biome.SpawnListEntry> spawnList,
                                                     EntityLiving.SpawnPlacementType placementType) {
        if (spawnList == null || spawnList.isEmpty()) {
            return;
        }
        while (random.nextFloat() < Math.min(biome.getSpawningChance(), 0.5F)) {
            Biome.SpawnListEntry spawnListEntry = WeightedRandom.getRandomItem(random, spawnList);
            if (spawnListEntry == null) {
                continue;
            }
            int minCount = Math.max(spawnListEntry.minGroupCount, 1);
            int maxCount = Math.min(spawnListEntry.maxGroupCount, 6);
            int groupCount = minCount + random.nextInt(1 + maxCount - minCount);
            net.minecraft.entity.IEntityLivingData livingData = null;
            int xPos = centerX + random.nextInt(diameterX);
            int zPos = centerZ + random.nextInt(diameterZ);
            int xPosOrig = xPos;
            int zPosOrig = zPos;
            for (int i = 0; i < groupCount; i++) {
                boolean spawned = false;
                for (int j = 0; !spawned && j < 4; j++) {
                    BlockPos blockPos = world.getTopSolidOrLiquidBlock(new BlockPos(xPos, 0, zPos));
                    if (placementType == EntityLiving.SpawnPlacementType.IN_WATER) {
                        blockPos = blockPos.down();
                    }
                    if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(placementType, world, blockPos)) {
                        xPos += random.nextInt(5) - random.nextInt(5);
                        zPos += random.nextInt(5) - random.nextInt(5);
                        continue;
                    }
                    EntityLiving entityliving;
                    try {
                        entityliving = spawnListEntry.newInstance(world);
                    } catch (Exception exception) {
                        AlexsMobs.LOGGER.warn("Failed to instantiate spawn entry {}", spawnListEntry.entityClass.getSimpleName(), exception);
                        break;
                    }
                    if (ForgeEventFactory.canEntitySpawn(entityliving, world, xPos, blockPos.getY(), zPos, false) == Event.Result.DENY) {
                        continue;
                    }
                    entityliving.setLocationAndAngles(xPos, blockPos.getY(), zPos, random.nextFloat() * 360.0F, 0.0F);
                    if (!entityliving.getCanSpawnHere()) {
                        entityliving.setDead();
                        continue;
                    }
                    if (entityliving.isNotColliding()) {
                        livingData = entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entityliving)), livingData);
                        world.spawnEntity(entityliving);
                        spawned = true;
                    } else {
                        entityliving.setDead();
                    }
                    xPos += random.nextInt(5) - random.nextInt(5);
                    zPos += random.nextInt(5) - random.nextInt(5);
                    while (xPos < centerX || xPos >= centerX + diameterX || zPos < centerZ || zPos >= centerZ + diameterZ) {
                        xPos = xPosOrig + random.nextInt(5) - random.nextInt(5);
                        zPos = zPosOrig + random.nextInt(5) - random.nextInt(5);
                    }
                }
            }
        }
    }
}
