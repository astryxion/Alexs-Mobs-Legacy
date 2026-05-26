package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityGust;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Random;

public class BlockGustmaker extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    public static final PropertyBool TRIGGERED = PropertyBool.create("triggered");

    public BlockGustmaker() {
        super(Material.ROCK);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TRIGGERED, false));
        this.setSoundType(SoundType.STONE);
        this.setHardness(4.5F);
        this.setResistance(4.5F);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:gustmaker");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, TRIGGERED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = state.getValue(FACING).ordinal();
        if (state.getValue(TRIGGERED)) {
            i |= 8;
        }
        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int fi = meta & 7;
        if (fi >= EnumFacing.values().length) {
            fi = 0;
        }
        EnumFacing facing = EnumFacing.values()[fi];
        boolean trig = (meta & 8) > 0;
        return this.getDefaultState().withProperty(FACING, facing).withProperty(TRIGGERED, trig);
    }

    public static Vec3d getDispensePosition(BlockPos coords, EnumFacing dir) {
        double d0 = coords.getX() + 0.5D + 0.7D * (double) dir.getFrontOffsetX();
        double d1 = coords.getY() + 0.15D + 0.7D * (double) dir.getFrontOffsetY();
        double d2 = coords.getZ() + 0.5D + 0.7D * (double) dir.getFrontOffsetZ();
        return new Vec3d(d0, d1, d2);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        tickGustmaker(state, worldIn, pos, false);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        tickGustmaker(state, worldIn, pos, true);
    }

    public void tickGustmaker(IBlockState state, World worldIn, BlockPos pos, boolean tickOff) {
        boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.down()) || worldIn.isBlockPowered(pos.up());
        boolean flag1 = state.getValue(TRIGGERED);
        if (flag && !flag1) {
            EnumFacing facing = state.getValue(FACING);
            Vec3d dispensePosition = getDispensePosition(pos, facing);
            Vec3i dv = facing.getDirectionVec();
            double sx = dv.getX() * 0.1D;
            double sy = dv.getY() * 0.1D;
            double sz = dv.getZ() * 0.1D;
            EntityGust gust = new EntityGust(worldIn);
            gust.setGustDir((float) sx, (float) sy, (float) sz);
            gust.setPosition(dispensePosition.x, dispensePosition.y, dispensePosition.z);
            if (facing.getAxis() == EnumFacing.Axis.Y) {
                gust.setVertical(true);
            }
            if (!worldIn.isRemote) {
                worldIn.spawnEntity(gust);
            }
            worldIn.setBlockState(pos, state.withProperty(TRIGGERED, true), 2);
            worldIn.scheduleUpdate(pos, this, 20);
        } else if (flag1) {
            if (tickOff) {
                worldIn.scheduleUpdate(pos, this, 20);
                worldIn.setBlockState(pos, state.withProperty(TRIGGERED, false), 2);
            }
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing.getOpposite());
    }

    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
}
