package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityVoidWormBeak;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * 1.12 port of 1.16 {@code ContainerBlock}: all six facings, redstone {@code powered}, axis-aligned hit boxes,
 * no solid collision (matches {@code doesNotBlockMovement()} + {@code notSolid()}), random + neighbor updates.
 */
public class BlockVoidWormBeak extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.25D, 0.0D, 1.0D, 0.75D, 1.0D);
    private static final AxisAlignedBB AABB_VERTICAL = new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D);

    public BlockVoidWormBeak() {
        super(Material.DRAGON_EGG);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, Boolean.FALSE));
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setResistance(3.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.setLightOpacity(0);
        this.setTickRandomly(true);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:void_worm_beak");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(FACING).getAxis() == EnumFacing.Axis.Y ? AABB_VERTICAL : AABB;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            this.updateState(state, worldIn, pos, blockIn);
        }
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (!worldIn.isRemote) {
            this.updateState(state, worldIn, pos, state.getBlock());
        }
    }

    public void updateState(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        boolean flag = state.getValue(POWERED);
        boolean flag1 = worldIn.isBlockPowered(pos);

        if (flag1 != flag) {
            worldIn.setBlockState(pos, state.withProperty(POWERED, flag1), 3);
            worldIn.notifyNeighborsOfStateChange(pos.down(), this, false);
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityVoidWormBeak();
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing).withProperty(POWERED, world.isBlockPowered(pos));
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, POWERED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int facing = state.getValue(FACING).ordinal();
        if (state.getValue(POWERED)) {
            facing |= 8;
        }
        return facing;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        boolean powered = (meta & 8) != 0;
        int ord = meta & 7;
        if (ord < 0 || ord >= EnumFacing.values().length) {
            ord = EnumFacing.NORTH.ordinal();
        }
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[ord]).withProperty(POWERED, powered);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
