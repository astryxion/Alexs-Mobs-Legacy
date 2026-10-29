package com.github.alexthe668.alexsmobs.block;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe668.alexsmobs.entity.EntityTerrapin;
import com.github.alexthe668.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe668.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe668.alexsmobs.tileentity.TileEntityTerrapinEgg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Random;

/**
 * 1.12 port of {@code BlockTerrapinEgg}. Parent shell data lives on {@link TileEntityTerrapinEgg}.
 */
public class BlockTerrapinEgg extends BlockContainer {

    public static final PropertyInteger HATCH = PropertyInteger.create("hatch", 0, 2);
    public static final PropertyInteger EGGS = PropertyInteger.create("eggs", 1, 4);
    private static final AxisAlignedBB ONE_EGG_AABB = new AxisAlignedBB(3.0D / 16.0D, 0.0D, 3.0D / 16.0D, 12.0D / 16.0D, 7.0D / 16.0D, 12.0D / 16.0D);
    private static final AxisAlignedBB MULTI_EGG_AABB = new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 7.0D / 16.0D, 15.0D / 16.0D);

    public BlockTerrapinEgg() {
        super(Material.GROUND);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HATCH, 0).withProperty(EGGS, 1));
        this.setSoundType(SoundType.METAL);
        this.setHardness(0.5F);
        this.setResistance(0.5F);
        this.setTickRandomly(true);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:terrapin_egg");
    }

    public static boolean hasProperHabitat(IBlockAccess world, BlockPos pos) {
        return isProperHabitat(world, pos.down());
    }

    public static boolean isProperHabitat(IBlockAccess world, BlockPos pos) {
        Block ground = world.getBlockState(pos).getBlock();
        return ground == Blocks.SAND || AMTagRegistry.blockInTag(AMTagRegistry.CROCODILE_SPAWNS, ground);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityTerrapinEgg();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        this.tryTrample(worldIn, pos, entityIn, 100);
        super.onEntityWalk(worldIn, pos, entityIn);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if (!(entityIn instanceof EntityZombie)) {
            this.tryTrample(worldIn, pos, entityIn, 3);
        }
        super.onFallenUpon(worldIn, pos, entityIn, fallDistance);
    }

    private void tryTrample(World worldIn, BlockPos pos, Entity trampler, int chances) {
        if (this.canTrample(worldIn, trampler) && !worldIn.isRemote && worldIn.rand.nextInt(chances) == 0) {
            this.removeOneEgg(worldIn, pos, worldIn.getBlockState(pos));
        }
    }

    private void removeOneEgg(World worldIn, BlockPos pos, IBlockState state) {
        if (state.getBlock() != this) {
            return;
        }
        worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + worldIn.rand.nextFloat() * 0.2F);
        int eggs = state.getValue(EGGS);
        if (eggs <= 1) {
            worldIn.setBlockToAir(pos);
        } else {
            worldIn.setBlockState(pos, state.withProperty(EGGS, eggs - 1), 2);
            worldIn.playEvent(2001, pos, Block.getStateId(state));
        }
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (!this.canGrow(worldIn) || !hasProperHabitat(worldIn, pos)) {
            return;
        }
        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            worldIn.setBlockState(pos, state.withProperty(HATCH, hatch + 1), 2);
            return;
        }
        worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
        TileEntityTerrapinEgg eggTe = null;
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityTerrapinEgg) {
            eggTe = (TileEntityTerrapinEgg) te;
        }
        int count = state.getValue(EGGS);
        worldIn.setBlockToAir(pos);
        for (int j = 0; j < count; ++j) {
            worldIn.playEvent(2001, pos, Block.getStateId(state));
            EntityTerrapin baby = (EntityTerrapin) AMEntityRegistry.TERRAPIN.newInstance(worldIn);
            if (baby == null) {
                continue;
            }
            baby.setGrowingAge(-24000);
            if (eggTe != null) {
                eggTe.addAttributesToOffspring(baby, random);
            }
            baby.setFromBucket(true);
            baby.setLocationAndAngles(pos.getX() + 0.3D + j * 0.2D, pos.getY(), pos.getZ() + 0.3D, 0.0F, 0.0F);
            worldIn.spawnEntity(baby);
        }
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        if (hasProperHabitat(worldIn, pos) && !worldIn.isRemote) {
            worldIn.playEvent(2005, pos, 0);
        }
    }

    private boolean canGrow(World worldIn) {
        float time = worldIn.getCelestialAngle(1.0F);
        if (time < 0.69F && time > 0.65F) {
            return true;
        }
        return worldIn.rand.nextInt(15) == 0;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("BlockEntityTag", 10)) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityTerrapinEgg) {
                TileEntityTerrapinEgg eggTe = (TileEntityTerrapinEgg) te;
                eggTe.readParents(tag.getCompoundTag("BlockEntityTag"));
                eggTe.markDirty();
            }
        }
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        EntityTerrapin.ParentData parent1 = null;
        EntityTerrapin.ParentData parent2 = null;
        if (te instanceof TileEntityTerrapinEgg) {
            parent1 = ((TileEntityTerrapinEgg) te).parent1;
            parent2 = ((TileEntityTerrapinEgg) te).parent2;
        }
        if (net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.SILK_TOUCH, stack) > 0) {
            ItemStack drop = new ItemStack(this);
            if (parent1 != null || parent2 != null) {
                NBTTagCompound blockTag = new NBTTagCompound();
                if (parent1 != null) {
                    NBTTagCompound p1 = new NBTTagCompound();
                    parent1.writeToNBT(p1);
                    blockTag.setTag("Parent1Data", p1);
                }
                if (parent2 != null) {
                    NBTTagCompound p2 = new NBTTagCompound();
                    parent2.writeToNBT(p2);
                    blockTag.setTag("Parent2Data", p2);
                }
                NBTTagCompound root = new NBTTagCompound();
                root.setTag("BlockEntityTag", blockTag);
                drop.setTagCompound(root);
            }
            spawnAsEntity(worldIn, pos, drop);
        }
        int eggs = state.getValue(EGGS);
        worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + worldIn.rand.nextFloat() * 0.2F);
        if (eggs > 1) {
            worldIn.setBlockState(pos, state.withProperty(EGGS, eggs - 1), 2);
            TileEntity replaced = worldIn.getTileEntity(pos);
            if (replaced instanceof TileEntityTerrapinEgg) {
                ((TileEntityTerrapinEgg) replaced).parent1 = parent1;
                ((TileEntityTerrapinEgg) replaced).parent2 = parent2;
            }
            worldIn.playEvent(2001, pos, Block.getStateId(state));
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack held = playerIn.getHeldItem(hand);
        if (held.getItem() != Item.getItemFromBlock(this) || state.getValue(EGGS) >= 4) {
            return false;
        }
        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos, state.withProperty(EGGS, Math.min(4, state.getValue(EGGS) + 1)), 2);
        }
        if (!playerIn.capabilities.isCreativeMode) {
            held.shrink(1);
        }
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(EGGS) > 1 ? MULTI_EGG_AABB : ONE_EGG_AABB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return this.getBoundingBox(state, worldIn, pos);
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
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HATCH, EGGS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(EGGS) - 1) | (state.getValue(HATCH) << 2);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int eggs = (meta & 3) + 1;
        int hatch = (meta >> 2) & 3;
        if (hatch > 2) {
            hatch = 2;
        }
        return this.getDefaultState().withProperty(EGGS, eggs).withProperty(HATCH, hatch);
    }

    private boolean canTrample(World worldIn, Entity trampler) {
        if (trampler instanceof EntityTerrapin || trampler instanceof EntityBat) {
            return false;
        }
        if (!(trampler instanceof EntityLivingBase)) {
            return false;
        }
        return trampler instanceof EntityPlayer || ForgeEventFactory.getMobGriefingEvent(worldIn, trampler);
    }
}
