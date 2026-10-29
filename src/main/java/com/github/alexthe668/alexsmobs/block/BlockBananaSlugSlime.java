package com.github.alexthe668.alexsmobs.block;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.misc.AMSoundRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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
 * 1.12 port of 1.20 {@code BlockBananaSlugSlime}. Sticky like honey: slows and grips entities,
 * does not bounce. Collision is open on the top (honey shape) so a stack does not leave a pocket
 * that traps the player inside the block above.
 */
public class BlockBananaSlugSlime extends Block {

    protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.9375D, 0.9375D);
    private static final int MAXIMUM_BLOCKS_DRAINED = 64;
    public static final int MAX_FLUID_SPREAD = 6;

    public BlockBananaSlugSlime() {
        super(Material.CLAY);
        this.setSoundType(SoundType.SLIME);
        this.setHardness(0.0F);
        this.slipperiness = 0.8F;
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:banana_slug_slime_block");
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn == null) {
            return;
        }
        double feet = entityIn.getEntityBoundingBox().minY;
        boolean standingOnThis = feet >= pos.getY() + 0.75D && feet <= pos.getY() + 1.01D;
        boolean bodyExtendsUpFromBelow = feet < pos.getY() + 0.05D;
        if (bodyExtendsUpFromBelow && !standingOnThis) {
            return;
        }
        entityIn.motionX *= 0.8D;
        entityIn.motionY *= 0.8D;
        entityIn.motionZ *= 0.8D;
        entityIn.fallDistance = 0.0F;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
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
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
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
