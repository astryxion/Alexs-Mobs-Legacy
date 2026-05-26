package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * 1.12 port of 1.16 lantern-style feeder. Waterlogging is stored as a boolean state (no shared water+block voxel in 1.12);
 * placement sets it when the replaced cell was water, matching 1.16 intent.
 */
public class BlockHummingbirdFeeder extends Block {

    public static final PropertyInteger CONTENTS = PropertyInteger.create("contents", 0, 3);
    public static final PropertyBool HANGING = PropertyBool.create("hanging");
    public static final PropertyBool WATERLOGGED = PropertyBool.create("waterlogged");

    private static final AxisAlignedBB AABB = new AxisAlignedBB(4.0D / 16.0D, 0.0D, 4.0D / 16.0D, 12.0D / 16.0D, 12.0D / 16.0D, 12.0D / 16.0D);
    private static final AxisAlignedBB AABB_HANGING = new AxisAlignedBB(4.0D / 16.0D, 0.0D, 4.0D / 16.0D, 12.0D / 16.0D, 16.0D / 16.0D, 12.0D / 16.0D);

    public BlockHummingbirdFeeder() {
        super(Material.IRON);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CONTENTS, 0).withProperty(HANGING, false).withProperty(WATERLOGGED, false));
        this.setSoundType(SoundType.METAL);
        this.setHardness(3.0F);
        this.setResistance(3.0F);
        this.setHarvestLevel("pickaxe", 0);
        this.setTickRandomly(true);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:hummingbird_feeder");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CONTENTS, HANGING, WATERLOGGED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int c = state.getValue(CONTENTS);
        if (state.getValue(HANGING)) {
            c |= 4;
        }
        if (state.getValue(WATERLOGGED)) {
            c |= 8;
        }
        return c;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(CONTENTS, meta & 3)
                .withProperty(HANGING, (meta & 4) != 0)
                .withProperty(WATERLOGGED, (meta & 8) != 0);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HANGING) ? AABB_HANGING : AABB;
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing clickedSide, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        IBlockState here = world.getBlockState(pos);
        boolean waterHere = here.getBlock() == Blocks.WATER || here.getBlock() == Blocks.FLOWING_WATER || here.getMaterial() == Material.WATER;
        java.util.List<EnumFacing> order = new java.util.ArrayList<EnumFacing>();
        if (placer != null && placer.rotationPitch < 0.0F) {
            order.add(EnumFacing.UP);
            order.add(EnumFacing.DOWN);
        } else {
            order.add(EnumFacing.DOWN);
            order.add(EnumFacing.UP);
        }
        for (EnumFacing direction : order) {
            if (direction.getAxis() != EnumFacing.Axis.Y) {
                continue;
            }
            IBlockState blockstate = this.getDefaultState().withProperty(HANGING, direction == EnumFacing.UP).withProperty(WATERLOGGED, waterHere).withProperty(CONTENTS, 0);
            if (this.validatePlacement(world, pos, blockstate)) {
                return blockstate;
            }
        }
        return null;
    }

    /**
     * Same attachment semantics as 1.16 {@code HANGING}: support above when hanging; support below when on ground.
     */
    protected static EnumFacing getBlockConnected(IBlockState state) {
        return state.getValue(HANGING) ? EnumFacing.DOWN : EnumFacing.UP;
    }

    private boolean validatePlacement(World world, BlockPos pos, IBlockState state) {
        return this.isValidPosition(state, world, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int contents = state.getValue(CONTENTS);
        ItemStack waterBottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
        ItemStack itemStack = playerIn.getHeldItem(hand);
        int setContent = -1;
        if (contents == 0) {
            if (itemStack.getItem() == Items.SUGAR) {
                setContent = 2;
                useItem(playerIn, itemStack);
            } else if (itemStack.getItem() == waterBottle.getItem() && ItemStack.areItemStackTagsEqual(waterBottle, itemStack)) {
                setContent = 1;
                useItem(playerIn, itemStack);
            }
        } else if (contents == 1) {
            if (itemStack.getItem() == Items.SUGAR) {
                setContent = 3;
                useItem(playerIn, itemStack);
            }
        } else if (contents == 2) {
            if (itemStack.getItem() == waterBottle.getItem() && ItemStack.areItemStackTagsEqual(waterBottle, itemStack)) {
                setContent = 3;
                useItem(playerIn, itemStack);
            }
        }
        if (setContent >= 0) {
            worldIn.setBlockState(pos, state.withProperty(CONTENTS, setContent), 2);
            return true;
        }
        return false;
    }

    public void useItem(EntityPlayer playerEntity, ItemStack stack) {
        if (!playerEntity.capabilities.isCreativeMode) {
            if (stack.getItem().hasContainerItem()) {
                ItemStack container = new ItemStack(stack.getItem().getContainerItem());
                playerEntity.inventory.addItemStackToInventory(container);
            }
            stack.shrink(1);
        }
    }

    public boolean isValidPosition(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        EnumFacing towardSupport = getBlockConnected(state).getOpposite();
        BlockPos supportPos = pos.offset(towardSupport);
        IBlockState supportState = worldIn.getBlockState(supportPos);
        return supportState.isSideSolid(worldIn, supportPos, towardSupport.getOpposite());
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.DESTROY;
    }

    @Override
    public void neighborChanged(IBlockState stateIn, World worldIn, BlockPos currentPos, Block blockIn, BlockPos fromPos) {
        EnumFacing towardSupport = getBlockConnected(stateIn).getOpposite();
        BlockPos supportPos = currentPos.offset(towardSupport);
        if (fromPos.equals(supportPos) && !this.isValidPosition(stateIn, worldIn, currentPos)) {
            worldIn.destroyBlock(currentPos, true);
            return;
        }
        super.neighborChanged(stateIn, worldIn, currentPos, blockIn, fromPos);
    }

    @Override
    @Nullable
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
        return PathNodeType.BLOCKED;
    }
}
