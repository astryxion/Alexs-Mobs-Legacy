package com.github.alexthe666.alexsmobs.world;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.config.AMNativeSpawnBiomes;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.BlockDirt;
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
        if (!AMNativeSpawnBiomes.leafcutterAnthill(biome)) {
            return false;
        }
        if (AMConfig.leafcutterAnthillSpawnChance <= 0D) {
            return false;
        }
        if (rand.nextFloat() > (float) AMConfig.leafcutterAnthillSpawnChance) {
            return false;
        }
        int z = 8;
        int x = 8;
        BlockPos heightProbe = new BlockPos(pos.getX() + x, 0, pos.getZ() + z);
        int y = worldIn.getPrecipitationHeight(heightProbe).getY();
        BlockPos heightPos = new BlockPos(pos.getX() + x, y, pos.getZ() + z);
        if (worldIn.getBlockState(heightPos.down()).getMaterial().isLiquid()) {
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
}
