package com.github.alexthe668.alexsmobs.block;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe668.alexsmobs.entity.EntityCaiman;
import com.github.alexthe668.alexsmobs.entity.EntityCrocodile;
import com.github.alexthe668.alexsmobs.entity.EntityPlatypus;
import com.github.alexthe668.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe668.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * 1.12 port of {@code BlockReptileEgg}. Crocodile and caiman are separate blocks that share this class.
 */
public class BlockReptileEgg extends Block {

    public static final PropertyInteger HATCH = PropertyInteger.create("hatch", 0, 2);
    public static final PropertyInteger EGGS = PropertyInteger.create("eggs", 1, 4);
    private static final AxisAlignedBB ONE_EGG_AABB = new AxisAlignedBB(3.0D / 16.0D, 0.0D, 3.0D / 16.0D, 12.0D / 16.0D, 7.0D / 16.0D, 12.0D / 16.0D);
    private static final AxisAlignedBB MULTI_EGG_AABB = new AxisAlignedBB(1.0D / 16.0D, 0.0D, 1.0D / 16.0D, 15.0D / 16.0D, 7.0D / 16.0D, 15.0D / 16.0D);

    private final boolean caiman;

    public BlockReptileEgg(boolean caiman) {
        super(Material.GROUND);
        this.caiman = caiman;
        this.setDefaultState(this.blockState.getBaseState().withProperty(HATCH, 0).withProperty(EGGS, 1));
        this.setSoundType(SoundType.METAL);
        this.setHardness(0.5F);
        this.setResistance(0.5F);
        this.setTickRandomly(true);
        this.setLightOpacity(0);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName(caiman ? "alexsmobs:caiman_egg" : "alexsmobs:crocodile_egg");
    }

    public static boolean hasProperHabitat(IBlockAccess world, BlockPos pos) {
        return isProperHabitat(world, pos.down());
    }

    public static boolean isProperHabitat(IBlockAccess world, BlockPos pos) {
        Block ground = world.getBlockState(pos).getBlock();
        return ground == Blocks.SAND || AMTagRegistry.blockInTag(AMTagRegistry.CROCODILE_SPAWNS, ground);
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
        if (!this.canTrample(worldIn, trampler)) {
            return;
        }
        if (!worldIn.isRemote && worldIn.rand.nextInt(chances) == 0) {
            AxisAlignedBB box = new AxisAlignedBB(pos).grow(25.0D);
            if (trampler instanceof EntityLivingBase) {
                Class<?> birth = this.caiman ? EntityCaiman.class : EntityCrocodile.class;
                List<EntityLiving> list = worldIn.getEntitiesWithinAABB(EntityLiving.class, box);
                for (EntityLiving living : list) {
                    if (!living.isEntityAlive() || !birth.isInstance(living)) {
                        continue;
                    }
                    if (living instanceof EntityTameable) {
                        EntityTameable tameable = (EntityTameable) living;
                        if (tameable.isTamed() && tameable.isOwner((EntityLivingBase) trampler)) {
                            continue;
                        }
                    }
                    living.setAttackTarget((EntityLivingBase) trampler);
                }
            }
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
        } else {
            worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            int count = state.getValue(EGGS);
            worldIn.setBlockToAir(pos);
            EntityPlayer closest = worldIn.getClosestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 20.0D, false);
            boolean desert = BiomeDictionary.hasType(worldIn.getBiome(pos), BiomeDictionary.Type.SANDY);
            for (int j = 0; j < count; ++j) {
                worldIn.playEvent(2001, pos, Block.getStateId(state));
                EntityAnimal baby = this.caiman
                        ? (EntityAnimal) AMEntityRegistry.CAIMAN.newInstance(worldIn)
                        : (EntityAnimal) AMEntityRegistry.CROCODILE.newInstance(worldIn);
                if (baby == null) {
                    continue;
                }
                baby.setGrowingAge(-24000);
                baby.setHomePosAndDistance(pos, 20);
                baby.setLocationAndAngles(pos.getX() + 0.3D + j * 0.2D, pos.getY(), pos.getZ() + 0.3D, 0.0F, 0.0F);
                if (closest != null) {
                    if (baby instanceof EntityTameable) {
                        EntityTameable tameable = (EntityTameable) baby;
                        tameable.setTamedBy(closest);
                    }
                    if (baby instanceof EntityCrocodile) {
                        ((EntityCrocodile) baby).setDesert(desert);
                        ((EntityCrocodile) baby).setSitting(true);
                    }
                    if (baby instanceof EntityCaiman) {
                        ((EntityCaiman) baby).setSitting(true);
                    }
                }
                worldIn.spawnEntity(baby);
            }
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
        if (time < 0.8F && time > 0.65F) {
            return true;
        }
        return worldIn.rand.nextInt(15) == 0;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        if (net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel(net.minecraft.init.Enchantments.SILK_TOUCH, stack) > 0) {
            spawnAsEntity(worldIn, pos, new ItemStack(this));
        }
        int eggs = state.getValue(EGGS);
        worldIn.playSound(null, pos, AMSoundRegistry.TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + worldIn.rand.nextFloat() * 0.2F);
        if (eggs > 1) {
            worldIn.setBlockState(pos, state.withProperty(EGGS, eggs - 1), 2);
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
        if (trampler instanceof EntityCrocodile || trampler instanceof EntityCaiman || trampler instanceof EntityPlatypus || trampler instanceof EntityBat) {
            return false;
        }
        if (!(trampler instanceof EntityLivingBase)) {
            return false;
        }
        return trampler instanceof EntityPlayer || ForgeEventFactory.getMobGriefingEvent(worldIn, trampler);
    }
}
