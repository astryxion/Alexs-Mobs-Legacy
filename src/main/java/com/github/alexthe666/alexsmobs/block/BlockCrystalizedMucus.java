package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/**
 * 1.12 port of 1.20 {@code BlockCrystalizedMucus}.
 */
public class BlockCrystalizedMucus extends BlockBreakable {

    public static final int DECAY_DISTANCE = 7;
    public static final PropertyInteger DISTANCE = PropertyInteger.create("distance", 0, 7);
    public static final PropertyBool PERSISTENT = PropertyBool.create("persistent");

    public BlockCrystalizedMucus() {
        super(Material.GLASS, false);
        this.setHardness(0.1F);
        this.setSoundType(SoundType.GLASS);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setTickRandomly(true);
        this.setRegistryName("alexsmobs:crystalized_banana_slug_mucus");
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(DISTANCE, 7)
                .withProperty(PERSISTENT, false));
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 1;
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
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote && this.decaying(state)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        } else if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, updateDistance(state, worldIn, pos), 2);
        }
    }

    protected boolean decaying(IBlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(DISTANCE) >= 7;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            int i = getDistanceAt(worldIn.getBlockState(fromPos)) + 1;
            if (i != 1 || state.getValue(DISTANCE) != i) {
                worldIn.scheduleUpdate(pos, this, 1);
            }
        }
    }

    private static IBlockState updateDistance(IBlockState state, IBlockAccess world, BlockPos pos) {
        int i = 7;
        for (EnumFacing direction : EnumFacing.values()) {
            BlockPos offset = pos.offset(direction);
            i = Math.min(i, getDistanceAt(world.getBlockState(offset)) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.withProperty(DISTANCE, i);
    }

    private static int getDistanceAt(IBlockState state) {
        if (state.getBlock() == AMBlockRegistry.BANANA_SLUG_SLIME_BLOCK) {
            return 0;
        }
        if (state.getBlock() instanceof BlockCrystalizedMucus) {
            return state.getValue(DISTANCE);
        }
        return 7;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, DISTANCE, PERSISTENT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(DISTANCE, meta & 7)
                .withProperty(PERSISTENT, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(DISTANCE);
        if (state.getValue(PERSISTENT)) {
            i |= 8;
        }
        return i;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, updateDistance(state.withProperty(PERSISTENT, true), worldIn, pos), 2);
        }
    }
}
