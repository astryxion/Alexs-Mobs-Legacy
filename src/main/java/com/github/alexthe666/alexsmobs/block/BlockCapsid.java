package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityCapsid;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * 1.12 port of 1.16 {@code ContainerBlock} capsid: horizontal facing TE, same interaction and drops.
 */
public class BlockCapsid extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockCapsid() {
        super(Material.GLASS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setSoundType(SoundType.GLASS);
        this.setHardness(4.5F);
        this.setResistance(4.5F);
        this.setLightOpacity(0);
        this.setLightLevel(5F / 15F);
        this.setHarvestLevel("pickaxe", 0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:capsid");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, net.minecraft.entity.EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
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
        return BlockRenderLayer.TRANSLUCENT;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState neighbor = blockAccess.getBlockState(pos.offset(side));
        return neighbor.getBlock() == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityCapsid && !playerIn.isSneaking() && heldItem.getItem() != net.minecraft.item.Item.getItemFromBlock(this)) {
            TileEntityCapsid capsid = (TileEntityCapsid) te;
            ItemStack copy = heldItem.copy();
            copy.setCount(1);
            if (capsid.getStackInSlot(0).isEmpty()) {
                capsid.setInventorySlotContents(0, copy);
                if (!playerIn.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                }
                return true;
            } else if (capsid.getStackInSlot(0).isItemEqual(copy) && capsid.getStackInSlot(0).getMaxStackSize() > capsid.getStackInSlot(0).getCount() + copy.getCount()) {
                capsid.getStackInSlot(0).grow(1);
                if (!playerIn.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                }
                return true;
            } else {
                if (!worldIn.isRemote) {
                    net.minecraft.entity.item.EntityItem entityitem = new net.minecraft.entity.item.EntityItem(worldIn, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, capsid.getStackInSlot(0).copy());
                    worldIn.spawnEntity(entityitem);
                }
                capsid.setInventorySlotContents(0, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntityCapsid) {
            InventoryHelper.dropInventoryItems(worldIn, pos, (TileEntityCapsid) tileentity);
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public net.minecraft.util.EnumBlockRenderType getRenderType(IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCapsid();
    }
}
