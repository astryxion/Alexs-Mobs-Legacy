package com.github.alexthe668.alexsmobs.block;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe668.alexsmobs.entity.EntityTriops;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * 1.12 port of {@code BlockTriopsEggs}. Sits on water and hatches on a scheduled tick, the way frogspawn does.
 */
public class BlockTriopsEggs extends Block {

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D / 16.0D, 1.0D);

    public BlockTriopsEggs() {
        super(Material.PLANTS);
        this.setSoundType(SoundType.SLIME);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:triops_eggs");
    }

    public static boolean canPlaceOn(IBlockAccess world, BlockPos waterPos) {
        return world.getBlockState(waterPos).getMaterial() == Material.WATER;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock().isReplaceable(worldIn, pos) && canPlaceOn(worldIn, pos.down());
    }

    private boolean canSurvive(World world, BlockPos pos) {
        return world.isAirBlock(pos) || world.getBlockState(pos).getBlock() == this;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        worldIn.scheduleUpdate(pos, this, 3600 + worldIn.rand.nextInt(8400));
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!canPlaceOn(worldIn, pos.down())) {
            worldIn.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (worldIn.getBlockState(pos).getBlock() != this) {
            return;
        }
        if (!canPlaceOn(worldIn, pos.down()) || !this.canSurvive(worldIn, pos)) {
            worldIn.setBlockToAir(pos);
            return;
        }
        worldIn.setBlockToAir(pos);
        int count = 2 + rand.nextInt(2);
        for (int j = 1; j <= count; ++j) {
            EntityTriops baby = (EntityTriops) AMEntityRegistry.TRIOPS.newInstance(worldIn);
            if (baby == null) {
                continue;
            }
            int yaw = rand.nextInt(360);
            baby.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() - 0.5D, pos.getZ() + 0.5D, yaw, 0.0F);
            baby.enablePersistence();
            baby.setBabyAge(-12000);
            worldIn.spawnEntity(baby);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB.offset(state.getOffset(source, pos));
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public EnumOffsetType getOffsetType() {
        return EnumOffsetType.XZ;
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
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
