package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * 1.12 replacement for 1.16 {@link net.minecraft.block.BushBlock}: same collision/selection bounds and slipperiness.
 */
public class BlockBananaPeel extends BlockBush {

    private static final AxisAlignedBB SHAPE_COLLISION = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 9.0D / 16.0D, 1.0D);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 4.0D / 16.0D, 14.0D / 16.0D);

    public BlockBananaPeel() {
        super();
        this.setSoundType(SoundType.PLANT);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.slipperiness = 0.9999999999F;
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:banana_peel");
    }

    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        IBlockState below = worldIn.getBlockState(pos.down());
        return below.isSideSolid(worldIn, pos.down(), EnumFacing.UP) && super.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return SHAPE_COLLISION.offset(pos);
    }
}
