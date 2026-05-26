package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.EnumBlockRenderType;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Anthill tile block; 1.12 {@link BlockContainer} port of the 1.16 {@code ContainerBlock} implementation.
 */
public class BlockLeafcutterAnthill extends BlockContainer {

    public BlockLeafcutterAnthill() {
        super(Material.GROUND);
        this.setSoundType(SoundType.GROUND);
        this.setHardness(2.5F);
        this.setResistance(2.5F);
        this.setHarvestLevel("shovel", 0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:leafcutter_anthill");
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityLeafcutterAnthill) {
            TileEntityLeafcutterAnthill hill = (TileEntityLeafcutterAnthill) worldIn.getTileEntity(pos);
            ItemStack heldItem = player.getHeldItem(hand);
            if (heldItem.getItem() == AMItemRegistry.GONGYLIDIA && hill.hasQueen()) {
                hill.releaseQueens();
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!worldIn.isRemote && player.capabilities.isCreativeMode && worldIn.getGameRules().getBoolean("doTileDrops")) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof TileEntityLeafcutterAnthill) {
                TileEntityLeafcutterAnthill anthill = (TileEntityLeafcutterAnthill) tileentity;
                ItemStack itemstack = new ItemStack(this);
                boolean hasAnts = !anthill.hasNoAnts();
                if (!hasAnts) {
                    return;
                }
                NBTTagCompound blockEntityTag = new NBTTagCompound();
                blockEntityTag.setTag("Ants", anthill.getAnts());
                NBTTagCompound stackRoot = itemstack.getTagCompound();
                if (stackRoot == null) {
                    stackRoot = new NBTTagCompound();
                    itemstack.setTagCompound(stackRoot);
                }
                stackRoot.setTag("BlockEntityTag", blockEntityTag);
                stackRoot.setTag("BlockStateTag", new NBTTagCompound());
                EntityItem drop = new EntityItem(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                drop.setDefaultPickupDelay();
                worldIn.spawnEntity(drop);
            }
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (entityIn instanceof EntityLivingBase) {
            this.angerNearbyAnts(worldIn, (EntityLivingBase) entityIn, pos);
            if (!worldIn.isRemote && worldIn.getTileEntity(pos) instanceof TileEntityLeafcutterAnthill) {
                TileEntityLeafcutterAnthill hill = (TileEntityLeafcutterAnthill) worldIn.getTileEntity(pos);
                hill.angerAnts((EntityLivingBase) entityIn, worldIn.getBlockState(pos), TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
                if (entityIn instanceof EntityPlayerMP) {
                    AMAdvancementTriggerRegistry.STOMP_LEAFCUTTER_ANTHILL.trigger((EntityPlayerMP) entityIn);
                }
            }
        }
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        if (!worldIn.isRemote && te instanceof TileEntityLeafcutterAnthill) {
            TileEntityLeafcutterAnthill hill = (TileEntityLeafcutterAnthill) te;
            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) == 0) {
                hill.angerAnts(player, state, TileEntityLeafcutterAnthill.AnthillReleaseState.EMERGENCY);
                worldIn.updateComparatorOutputLevel(pos, this);
                this.angerNearbyAnts(worldIn, pos);
            }
        }
    }

    private void angerNearbyAnts(World worldIn, BlockPos pos) {
        List<EntityLeafcutterAnt> list = worldIn.getEntitiesWithinAABB(EntityLeafcutterAnt.class, new AxisAlignedBB(pos).grow(20D, 6.0D, 20D));
        if (!list.isEmpty()) {
            List<EntityPlayer> players = worldIn.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(20D, 6.0D, 20D));
            if (players.isEmpty()) {
                return;
            }
            int n = players.size();
            for (EntityLeafcutterAnt ant : list) {
                if (ant.getAttackTarget() == null) {
                    ant.setAttackTarget(players.get(worldIn.rand.nextInt(n)));
                }
            }
        }
    }

    private void angerNearbyAnts(World worldIn, EntityLivingBase aggressor, BlockPos pos) {
        List<EntityLeafcutterAnt> list = worldIn.getEntitiesWithinAABB(EntityLeafcutterAnt.class, new AxisAlignedBB(pos).grow(20D, 6.0D, 20D));
        if (!list.isEmpty()) {
            for (EntityLeafcutterAnt ant : list) {
                if (ant.getAttackTarget() == null) {
                    ant.setAttackTarget(aggressor);
                }
            }
        }
    }

    @Override
    @Nullable
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLeafcutterAnthill();
    }
}
