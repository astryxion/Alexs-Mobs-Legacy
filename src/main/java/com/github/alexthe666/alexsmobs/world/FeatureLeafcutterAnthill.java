package com.github.alexthe666.alexsmobs.world;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.BiomeConfig;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Iterator;
import java.util.Random;

/**
 * 1.12 port of the 1.16 {@code Feature}. Mounds coarse dirt / dirt, places anthill + chambers, and seeds ants via {@link AMEntityRegistry#LEAFCUTTER_ANT}.
 */
public class FeatureLeafcutterAnthill extends WorldGenerator {

    private static long chunkDecorationSeed(BlockPos chunkStart) {
        return ((long) chunkStart.getX() << 32) | (chunkStart.getZ() & 0xffffffffL);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos pos) {
        Biome biome = worldIn.getBiome(pos.add(8, 0, 8));
        if (!BiomeConfig.test(BiomeConfig.leafcutter_anthill_spawns, biome)) {
            return false;
        }
        if (AMConfig.leafcutterAnthillSpawnChance <= 0D) {
            return false;
        }
        if (rand.nextFloat() > (float) AMConfig.leafcutterAnthillSpawnChance) {
            return false;
        }
        BlockPos heightPos = pickSurfaceInChunk(worldIn, rand, pos);
        if (heightPos == null) {
            return false;
        }
        IBlockState coarse = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
        IBlockState plainDirt = Blocks.DIRT.getDefaultState();
        int outOfGround = 2 + rand.nextInt(2);
        for (int i = 0; i < outOfGround; i++) {
            float size = outOfGround - i;
            int lvt_8_1_ = (int) (Math.floor(size) * rand.nextFloat()) + 2;
            int lvt_10_1_ = (int) (Math.floor(size) * rand.nextFloat()) + 2;
            float radius = (float) (lvt_8_1_ + lvt_10_1_) * 0.333F;
            Iterator<BlockPos> it = BlockPos.getAllInBox(heightPos.add(-lvt_8_1_, 0, -lvt_10_1_), heightPos.add(lvt_8_1_, 3, lvt_10_1_)).iterator();
            while (it.hasNext()) {
                BlockPos p = it.next();
                if (p.distanceSq(heightPos) <= (double) (radius * radius)) {
                    IBlockState block = coarse;
                    if (rand.nextFloat() < 0.2F) {
                        block = plainDirt;
                    }
                    worldIn.setBlockState(p, block, 4);
                }
            }
        }
        Random chunkSeedRandom = new Random(chunkDecorationSeed(pos));
        outOfGround -= chunkSeedRandom.nextInt(1) + 1;
        heightPos = heightPos.add(-chunkSeedRandom.nextInt(2), 0, -chunkSeedRandom.nextInt(2));
        if (worldIn.getBlockState(heightPos.up(outOfGround + 1)).getBlock() != AMBlockRegistry.LEAFCUTTER_ANTHILL
                && worldIn.getBlockState(heightPos.up(outOfGround - 1)).getBlock() != AMBlockRegistry.LEAFCUTTER_ANTHILL) {
            worldIn.setBlockState(heightPos.up(outOfGround), AMBlockRegistry.LEAFCUTTER_ANTHILL.getDefaultState(), 4);
            TileEntity tileentity = worldIn.getTileEntity(heightPos.up(outOfGround));
            if (tileentity instanceof TileEntityLeafcutterAnthill) {
                TileEntityLeafcutterAnthill beehivetileentity = (TileEntityLeafcutterAnthill) tileentity;
                int j = 3 + chunkSeedRandom.nextInt(3);
                if (beehivetileentity.hasNoAnts()) {
                    for (int k = 0; k < j; ++k) {
                        Entity spawned = AMEntityRegistry.LEAFCUTTER_ANT.newInstance(worldIn);
                        if (spawned instanceof EntityLeafcutterAnt) {
                            EntityLeafcutterAnt beeentity = (EntityLeafcutterAnt) spawned;
                            beeentity.setQueen(k == 0);
                            beehivetileentity.tryEnterHive(beeentity, false, rand.nextInt(599));
                        }
                    }
                }
            }

            if (rand.nextBoolean()) {
                worldIn.setBlockState(heightPos.up(outOfGround).north(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 1).north(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 2).north(), coarse, 4);
            }
            if (rand.nextBoolean()) {
                worldIn.setBlockState(heightPos.up(outOfGround).east(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 1).east(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 2).east(), coarse, 4);
            }
            if (rand.nextBoolean()) {
                worldIn.setBlockState(heightPos.up(outOfGround).south(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 1).south(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 2).south(), coarse, 4);
            }
            if (rand.nextBoolean()) {
                worldIn.setBlockState(heightPos.up(outOfGround).west(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 1).west(), coarse, 4);
                worldIn.setBlockState(heightPos.up(outOfGround - 2).west(), coarse, 4);
            }
            // 1.16 places this feature before trees. 1.12 runs after, so punch a small
            // foliage hole or the dirt mound is invisible under the jungle canopy.
            clearFoliageAbove(worldIn, heightPos.up(outOfGround), 4);
        }
        int i = outOfGround;
        int down = rand.nextInt(2) + 1;
        while (i > -down) {
            i--;
            worldIn.setBlockState(heightPos.up(i), AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState(), 4);
        }
        float size = chunkSeedRandom.nextInt(1) + 1;
        int lvt_8_1_ = (int) (Math.floor(size) * rand.nextFloat()) + 1;
        int lvt_9_1_ = (int) (Math.floor(size) * rand.nextFloat()) + 1;
        int lvt_10_1_ = (int) (Math.floor(size) * rand.nextFloat()) + 1;
        float radius = (float) (lvt_8_1_ + lvt_9_1_ + lvt_10_1_) * 0.333F + 0.5F;
        heightPos = heightPos.down(down + lvt_9_1_).add(chunkSeedRandom.nextInt(2), 0, chunkSeedRandom.nextInt(2));
        Iterator<BlockPos> it2 = BlockPos.getAllInBox(heightPos.add(-lvt_8_1_, -lvt_9_1_, -lvt_10_1_), heightPos.add(lvt_8_1_, lvt_9_1_, lvt_10_1_)).iterator();
        while (it2.hasNext()) {
            BlockPos p = it2.next();
            if (p.distanceSq(heightPos) < (double) (radius * radius)) {
                IBlockState block = AMBlockRegistry.LEAFCUTTER_ANT_CHAMBER.getDefaultState();
                worldIn.setBlockState(p, block, 4);
            }
        }
        return true;
    }

    /**
     * 1.16 places this feature in {@code SURFACE_STRUCTURES} using {@code WORLD_SURFACE_WG}
     * (terrain height before trees). 1.12 {@code IWorldGenerator} runs after biome decoration,
     * and {@link World#getPrecipitationHeight} includes leaves, so jungles would spawn the
     * mound on the canopy. Walk down through air/foliage/logs to the real ground.
     * Try several columns: jungle chunk centers are often trees over water.
     */
    private static BlockPos pickSurfaceInChunk(World world, Random rand, BlockPos chunkStart) {
        BlockPos found = findSurfaceAboveGround(world, chunkStart.getX() + 8, chunkStart.getZ() + 8);
        if (found != null) {
            return found;
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            found = findSurfaceAboveGround(world, chunkStart.getX() + rand.nextInt(16), chunkStart.getZ() + rand.nextInt(16));
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static BlockPos findSurfaceAboveGround(World world, int x, int z) {
        int y = world.getPrecipitationHeight(new BlockPos(x, 0, z)).getY();
        if (y <= 1) {
            y = world.getHeight();
        }
        if (y > 255) {
            y = 255;
        }
        for (int scanY = y; scanY > 1; scanY--) {
            BlockPos ground = new BlockPos(x, scanY, z);
            IBlockState state = world.getBlockState(ground);
            if (isAirOrFoliage(world, ground, state)) {
                continue;
            }
            if (state.getMaterial().isLiquid()) {
                return null;
            }
            if (!state.getMaterial().blocksMovement() || !isTerrain(state)) {
                continue;
            }
            return ground.up();
        }
        return null;
    }

    private static boolean isTerrain(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.SAND || block == Blocks.GRAVEL
                || block == Blocks.CLAY || block == Blocks.STONE || material == Material.GRASS
                || material == Material.GROUND || material == Material.SAND || material == Material.ROCK;
    }

    private static void clearFoliageAbove(World world, BlockPos anthill, int radius) {
        int top = world.getPrecipitationHeight(anthill).getY() + 1;
        if (top < anthill.getY() + 2) {
            top = anthill.getY() + 4;
        }
        Iterator<BlockPos> it = BlockPos.getAllInBox(anthill.add(-radius, 1, -radius), new BlockPos(anthill.getX() + radius, top, anthill.getZ() + radius)).iterator();
        while (it.hasNext()) {
            BlockPos p = it.next();
            if (p.distanceSq(anthill) > (double) (radius * radius)) {
                continue;
            }
            IBlockState state = world.getBlockState(p);
            if (isCanopyFoliage(world, p, state)) {
                world.setBlockState(p, Blocks.AIR.getDefaultState(), 4);
            }
        }
    }

    private static boolean isAirOrFoliage(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block.isAir(state, world, pos)) {
            return true;
        }
        if (block.isLeaves(state, world, pos) || block.isWood(world, pos) || block.isFoliage(world, pos)) {
            return true;
        }
        if (material == Material.LEAVES || material == Material.VINE || material == Material.PLANTS || material == Material.WOOD) {
            return true;
        }
        return block == Blocks.COCOA || block == Blocks.SNOW_LAYER || block == Blocks.VINE;
    }

    private static boolean isCanopyFoliage(World world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (block.isLeaves(state, world, pos) || block.isFoliage(world, pos)) {
            return true;
        }
        if (material == Material.LEAVES || material == Material.VINE || material == Material.PLANTS) {
            return true;
        }
        return block == Blocks.COCOA || block == Blocks.VINE;
    }
}
