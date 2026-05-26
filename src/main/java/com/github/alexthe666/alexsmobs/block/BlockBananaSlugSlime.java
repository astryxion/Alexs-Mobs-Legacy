package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Queue;

/**
 * 1.12 port of 1.20 {@code BlockBananaSlugSlime}.
 */
public class BlockBananaSlugSlime extends BlockSlime {

    protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0625D, 0.0625D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
    private static final int MAXIMUM_BLOCKS_DRAINED = 64;
    public static final int MAX_FLUID_SPREAD = 6;

    public BlockBananaSlugSlime() {
        super();
        this.setHardness(0.0F);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:banana_slug_slime_block");
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public boolean isStickyBlock(IBlockState state) {
        return true;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            this.tryAbsorbWater(worldIn, pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            this.tryAbsorbWater(worldIn, pos);
        }
    }

    protected void tryAbsorbWater(World level, BlockPos pos) {
        if (this.removeWaterBreadthFirstSearch(level, pos)) {
            level.playSound(null, pos, AMSoundRegistry.BANANA_SLUG_SLIME_EXPAND, net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }

    private boolean removeWaterBreadthFirstSearch(World level, BlockPos pos) {
        Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
        queue.add(new Tuple<>(pos, 0));
        int i = 0;
        int fullBlocks = 0;
        Material lastFluidMaterial = null;
        while (!queue.isEmpty()) {
            Tuple<BlockPos, Integer> tuple = queue.poll();
            BlockPos blockpos = tuple.getFirst();
            IBlockState state = level.getBlockState(blockpos);
            int j = tuple.getSecond();
            Material mat = state.getMaterial();
            if (mat == Material.WATER) {
                fullBlocks++;
                level.setBlockState(blockpos, AMBlockRegistry.CRYSTALIZED_BANANA_SLUG_MUCUS.getDefaultState(), 2);
            }
            for (EnumFacing direction : EnumFacing.values()) {
                BlockPos blockpos1 = blockpos.offset(direction);
                IBlockState blockstate = level.getBlockState(blockpos1);
                Material fluidMat = blockstate.getMaterial();
                if (lastFluidMaterial != null && fluidMat == Material.WATER && lastFluidMaterial != fluidMat) {
                    continue;
                }
                if (blockstate.getBlock() instanceof BlockLiquid) {
                    if (fluidMat == Material.WATER) {
                        lastFluidMaterial = fluidMat;
                    }
                    ++i;
                    fullBlocks++;
                    level.setBlockState(blockpos1, AMBlockRegistry.CRYSTALIZED_BANANA_SLUG_MUCUS.getDefaultState(), 2);
                    if (j < MAX_FLUID_SPREAD) {
                        queue.add(new Tuple<>(blockpos1, j + 1));
                    }
                }
            }
            if (i > MAXIMUM_BLOCKS_DRAINED) {
                break;
            }
        }
        return fullBlocks > 0;
    }
}
