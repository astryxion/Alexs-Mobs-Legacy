package com.github.alexthe666.alexsmobs.world;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.world.spawn.AMSpawnRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Random;

/**
 * World features (leafcutter anthill generator). Natural mob spawning is handled by
 * {@link AMSpawnRegistry} using Mo Creatures-style {@code EntityRegistry.addSpawn}.
 */
public final class AMWorldRegistry {

    private static final FeatureLeafcutterAnthill LEAFCUTTER_FEATURE = new FeatureLeafcutterAnthill();
    private static boolean leafcutterGeneratorRegistered;

    private AMWorldRegistry() {
    }

    public static void register() {
        AMSpawnRegistry.register();
        registerLeafcutterAnthillGeneratorIfNeeded();
    }

    private static void registerLeafcutterAnthillGeneratorIfNeeded() {
        if (leafcutterGeneratorRegistered || AMConfig.leafcutterAnthillSpawnChance <= 0D) {
            return;
        }
        leafcutterGeneratorRegistered = true;
        GameRegistry.registerWorldGenerator(new IWorldGenerator() {
            @Override
            public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
                if (world.isRemote || world.provider.getDimension() != 0) {
                    return;
                }
                BlockPos chunkStart = new BlockPos(chunkX << 4, 0, chunkZ << 4);
                LEAFCUTTER_FEATURE.generate(world, random, chunkStart);
            }
        }, 0);
    }
}
