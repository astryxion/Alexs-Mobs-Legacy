package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntitySkunkSpray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 1.12 port of 1.20 {@link com.github.alexthe666.alexsmobs.block.BlockSkunkSpray} (MultifaceBlock).
 * Multiface data is stored in {@link TileEntitySkunkSpray}; blockstate properties drive multipart models via {@link #getActualState}.
 */
public class BlockSkunkSpray extends BlockContainer {

    private static final double FACE_THICKNESS = 0.0625D;

    public static final PropertyBool NORTH = PropertyBool.create("north");
    public static final PropertyBool EAST = PropertyBool.create("east");
    public static final PropertyBool SOUTH = PropertyBool.create("south");
    public static final PropertyBool WEST = PropertyBool.create("west");
    public static final PropertyBool UP = PropertyBool.create("up");
    public static final PropertyBool DOWN = PropertyBool.create("down");
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    public BlockSkunkSpray() {
        super(Material.VINE);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false)
                .withProperty(WEST, false).withProperty(UP, false).withProperty(DOWN, false)
                .withProperty(AGE, 0).withProperty(WATERLOGGED, false));
        this.setSoundType(SoundType.SLIME);
        this.setHardness(0.0F);
        this.setResistance(0.0F);
        this.setLightOpacity(0);
        this.setTickRandomly(true);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:skunk_spray");
    }

    public static PropertyBool getFaceProperty(EnumFacing face) {
        switch (face) {
            case NORTH: return NORTH;
            case SOUTH: return SOUTH;
            case EAST: return EAST;
            case WEST: return WEST;
            case UP: return UP;
            case DOWN: return DOWN;
            default: return NORTH;
        }
    }

    public static boolean hasFace(IBlockState state, EnumFacing face) {
        return state.getValue(getFaceProperty(face));
    }

    public static boolean hasAnyFace(IBlockState state) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (hasFace(state, facing)) {
                return true;
            }
        }
        return false;
    }

    public static List<EnumFacing> availableFaces(IBlockState state) {
        List<EnumFacing> list = new ArrayList<>();
        for (EnumFacing facing : EnumFacing.values()) {
            if (hasFace(state, facing)) {
                list.add(facing);
            }
        }
        return list;
    }

    private static AxisAlignedBB getFaceShape(EnumFacing face) {
        switch (face) {
            case DOWN:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, FACE_THICKNESS, 1.0D);
            case UP:
                return new AxisAlignedBB(0.0D, 1.0D - FACE_THICKNESS, 0.0D, 1.0D, 1.0D, 1.0D);
            case NORTH:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, FACE_THICKNESS);
            case SOUTH:
                return new AxisAlignedBB(0.0D, 0.0D, 1.0D - FACE_THICKNESS, 1.0D, 1.0D, 1.0D);
            case WEST:
                return new AxisAlignedBB(0.0D, 0.0D, 0.0D, FACE_THICKNESS, 1.0D, 1.0D);
            case EAST:
                return new AxisAlignedBB(1.0D - FACE_THICKNESS, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
            default:
                return FULL_BLOCK_AABB;
        }
    }

    private static AxisAlignedBB computeBounds(IBlockState state) {
        if (!hasAnyFace(state)) {
            return NULL_AABB;
        }
        double minX = 1.0D;
        double minY = 1.0D;
        double minZ = 1.0D;
        double maxX = 0.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;
        for (EnumFacing facing : EnumFacing.values()) {
            if (hasFace(state, facing)) {
                AxisAlignedBB face = getFaceShape(facing);
                minX = Math.min(minX, face.minX);
                minY = Math.min(minY, face.minY);
                minZ = Math.min(minZ, face.minZ);
                maxX = Math.max(maxX, face.maxX);
                maxY = Math.max(maxY, face.maxY);
                maxZ = Math.max(maxZ, face.maxZ);
            }
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static IBlockState removeStinkFace(IBlockState state, EnumFacing face) {
        IBlockState next = state.withProperty(getFaceProperty(face), false);
        return hasAnyFace(next) ? next : Blocks.AIR.getDefaultState();
    }

    @Nullable
    public static IBlockState getStateForPlacement(IBlockState existing, World world, BlockPos pos, EnumFacing face) {
        IBlockState here = world.getBlockState(pos);
        if (here.getBlock() == AMBlockRegistry.SKUNK_SPRAY) {
            IBlockState actual = here;
            TileEntitySkunkSpray te = getTile(world, pos);
            if (te != null) {
                actual = te.toBlockState(here);
            } else if (existing.getBlock() == AMBlockRegistry.SKUNK_SPRAY) {
                actual = existing;
            }
            return actual.withProperty(getFaceProperty(face), true);
        }
        if (here.getBlock().isAir(here, world, pos) || here.getBlock().isReplaceable(world, pos)) {
            IBlockState result = AMBlockRegistry.SKUNK_SPRAY.getDefaultState().withProperty(getFaceProperty(face), true);
            if (here.getMaterial() == Material.WATER) {
                result = result.withProperty(WATERLOGGED, true);
            }
            return result;
        }
        return null;
    }

    public static void applyPlacementState(World world, BlockPos pos, IBlockState sprayState) {
        IBlockState here = world.getBlockState(pos);
        if (here.getBlock() != AMBlockRegistry.SKUNK_SPRAY) {
            world.setBlockState(pos, AMBlockRegistry.SKUNK_SPRAY.getDefaultState(), 2);
        }
        TileEntitySkunkSpray te = getOrCreateTile(world, pos);
        if (te != null) {
            for (EnumFacing facing : EnumFacing.values()) {
                if (hasFace(sprayState, facing)) {
                    te.setFace(facing, true);
                }
            }
            te.setAge(sprayState.getValue(AGE));
            te.setWaterlogged(sprayState.getValue(WATERLOGGED));
            world.notifyBlockUpdate(pos, here, world.getBlockState(pos), 3);
        }
    }

    @Nullable
    private static TileEntitySkunkSpray getTile(IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntitySkunkSpray ? (TileEntitySkunkSpray) te : null;
    }

    @Nullable
    private static TileEntitySkunkSpray getOrCreateTile(World world, BlockPos pos) {
        TileEntitySkunkSpray te = getTile(world, pos);
        if (te == null && !world.isRemote) {
            te = new TileEntitySkunkSpray();
            world.setTileEntity(pos, te);
        }
        return te;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, NORTH, EAST, SOUTH, WEST, UP, DOWN, AGE, WATERLOGGED);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntitySkunkSpray te = getTile(world, pos);
        if (te == null) {
            return state;
        }
        return te.toBlockState(state);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta & 3);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        if (!world.isRemote && getTile(world, pos) == null) {
            world.setTileEntity(pos, new TileEntitySkunkSpray());
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return computeBounds(this.getActualState(state, source, pos));
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        IBlockState actual = this.getActualState(blockState, worldIn, pos);
        RayTraceResult closest = null;
        double closestDist = 0.0D;
        for (EnumFacing facing : EnumFacing.values()) {
            if (hasFace(actual, facing)) {
                RayTraceResult result = getFaceShape(facing).offset(pos).calculateIntercept(start, end);
                if (result != null) {
                    double dist = start.squareDistanceTo(result.hitVec);
                    if (closest == null || dist < closestDist) {
                        closest = new RayTraceResult(result.hitVec, facing, pos);
                        closestDist = dist;
                    }
                }
            }
        }
        return closest;
    }

    public boolean isReplaceable(World world, BlockPos pos, ItemStack stack) {
        return stack.getItem() != com.github.alexthe666.alexsmobs.item.AMItemRegistry.STINK_BOTTLE;
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(8) == 0) {
            for (EnumFacing direction : EnumFacing.values()) {
                BlockPos offset = pos.offset(direction);
                IBlockState neighbor = world.getBlockState(offset);
                if (neighbor.getBlock() == this) {
                    TileEntitySkunkSpray te = getTile(world, offset);
                    if (te != null && !incrementAge(world, offset, te)) {
                        world.scheduleUpdate(offset, this, rand.nextInt(50) + 50);
                    }
                }
            }
            TileEntitySkunkSpray te = getTile(world, pos);
            if (te != null) {
                incrementAge(world, pos, te);
            }
        } else {
            world.scheduleUpdate(pos, this, rand.nextInt(50) + 50);
        }
    }

    private boolean incrementAge(World world, BlockPos pos, TileEntitySkunkSpray te) {
        int age = te.getAge();
        if (age < 3) {
            te.setAge(age + 1);
            world.notifyBlockUpdate(pos, AMBlockRegistry.SKUNK_SPRAY.getDefaultState(), AMBlockRegistry.SKUNK_SPRAY.getDefaultState(), 3);
            return false;
        } else {
            world.setBlockToAir(pos);
            return true;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.GLASS_BOTTLE) {
            EnumFacing face = facing.getOpposite();
            IBlockState actual = getActualState(state, world, pos);
            if (hasFace(actual, face)) {
                IBlockState next = BlockSkunkSpray.removeStinkFace(actual, face);
                if (next.getBlock() == Blocks.AIR) {
                    world.setBlockToAir(pos);
                } else {
                    applyPlacementState(world, pos, next);
                }
                ItemStack bottle = new ItemStack(com.github.alexthe666.alexsmobs.item.AMItemRegistry.STINK_BOTTLE);
                if (!player.addItemStackToInventory(bottle)) {
                    player.dropItem(bottle, false);
                }
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                return true;
            }
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        IBlockState actual = getActualState(state, world, pos);
        if (rand.nextInt(2) == 0) {
            List<EnumFacing> faces = availableFaces(actual);
            EnumFacing direction = null;
            if (faces.size() == 1) {
                direction = faces.get(0);
            } else if (faces.size() > 1) {
                direction = faces.get(rand.nextInt(faces.size()));
            }
            if (direction != null) {
                double d0 = direction.getFrontOffsetX() == 0 ? rand.nextDouble() : 0.5D + (double) direction.getFrontOffsetX() * 0.8D;
                double d1 = direction.getFrontOffsetY() == 0 ? rand.nextDouble() : 0.5D + (double) direction.getFrontOffsetY() * 0.8D;
                double d2 = direction.getFrontOffsetZ() == 0 ? rand.nextDouble() : 0.5D + (double) direction.getFrontOffsetZ() * 0.8D;
                AMParticleRegistry.spawnParticle(world, AMParticleRegistry.SMELLY, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySkunkSpray();
    }
}
