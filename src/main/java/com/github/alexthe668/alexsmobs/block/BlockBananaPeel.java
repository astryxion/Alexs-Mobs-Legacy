package com.github.alexthe668.alexsmobs.block;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.12 port of {@code BlockBananaPeel}. Not a bush: it sits on any solid top face, has a collision
 * box, and is slippery enough to slide entities sideways. In 1.12 friction is read from the block
 * under the feet ({@code floor(minY) - 1}), which misses a 9/16-tall peel, so collision also
 * cancels that friction down to this block's slipperiness.
 */
public class BlockBananaPeel extends Block {

    private static final AxisAlignedBB SHAPE_COLLISION = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 9.0D / 16.0D, 1.0D);
    private static final AxisAlignedBB SHAPE = new AxisAlignedBB(2.0D / 16.0D, 0.0D, 2.0D / 16.0D, 14.0D / 16.0D, 4.0D / 16.0D, 14.0D / 16.0D);

    public BlockBananaPeel() {
        super(Material.PLANTS);
        this.setSoundType(SoundType.GROUND);
        this.setHardness(0.2F);
        this.setResistance(0.2F);
        this.slipperiness = 0.9999999999F;
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:banana_peel");
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn == null || !entityIn.onGround) {
            return;
        }
        if (MathHelper.floor(entityIn.posX) != pos.getX() || MathHelper.floor(entityIn.posZ) != pos.getZ()) {
            return;
        }
        BlockPos underFeet = new BlockPos(entityIn.posX, (double) MathHelper.floor(entityIn.getEntityBoundingBox().minY) - 1.0D, entityIn.posZ);
        float groundSlip = worldIn.getBlockState(underFeet).getBlock().slipperiness;
        float applied = groundSlip * 0.91F;
        float wanted = this.slipperiness * 0.91F;
        if (applied > 1.0E-4F) {
            float scale = wanted / applied;
            entityIn.motionX *= scale;
            entityIn.motionZ *= scale;
        }
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && this.canBlockStay(worldIn, pos);
    }

    public boolean canBlockStay(World worldIn, BlockPos pos) {
        BlockPos below = pos.down();
        IBlockState soil = worldIn.getBlockState(below);
        return soil.isSideSolid(worldIn, below, EnumFacing.UP)
                || soil.getBlockFaceShape(worldIn, below, EnumFacing.UP) == BlockFaceShape.SOLID;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!this.canBlockStay(worldIn, pos)) {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XZ;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return SHAPE.offset(state.getOffset(source, pos));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return SHAPE_COLLISION.offset(blockState.getOffset(worldIn, pos));
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
