package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityBlobfish;
import com.github.alexthe666.alexsmobs.entity.EntityCatfish;
import com.github.alexthe666.alexsmobs.entity.EntityCombJelly;
import com.github.alexthe666.alexsmobs.entity.EntityCosmicCod;
import com.github.alexthe666.alexsmobs.entity.EntityDevilsHolePupfish;
import com.github.alexthe666.alexsmobs.entity.EntityFlyingFish;
import com.github.alexthe666.alexsmobs.entity.EntityFrilledShark;
import com.github.alexthe666.alexsmobs.entity.EntityLobster;
import com.github.alexthe666.alexsmobs.entity.EntityMimicOctopus;
import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import com.github.alexthe666.alexsmobs.entity.EntityStradpole;
import com.github.alexthe666.alexsmobs.entity.EntityTriops;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 1.16 used {@link net.minecraft.item.BucketItem} + {@code EntityType#spawn}; 1.12 uses a plain {@link Item},
 * {@link EntityEntry#newInstance}, and material checks for water vs lava (stradpole).
 */
public class ItemModFishBucket extends Item {

    public enum FluidKind {
        WATER,
        LAVA
    }

    private final EntityEntry fishEntry;
    private final FluidKind fluidKind;

    public ItemModFishBucket(EntityEntry fishEntry, FluidKind fluidKind) {
        this.fishEntry = fishEntry;
        this.fluidKind = fluidKind;
        this.setMaxStackSize(1);
        this.setCreativeTab(AlexsMobs.TAB);
    }

    public EntityEntry getFishEntry() {
        return fishEntry;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isBlockModifiable(player, pos)) {
            return EnumActionResult.PASS;
        }
        BlockPos target = resolveEmptyTarget(world, pos, facing);
        if (target == null) {
            return EnumActionResult.PASS;
        }
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        return this.emptyBucketAt(player, world, hand, player.getHeldItem(hand), target) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        RayTraceResult ray = player.rayTrace(5.0D, 1.0F);
        if (ray == null || ray.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        BlockPos pos = ray.getBlockPos();
        if (!world.isBlockModifiable(player, pos)) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        BlockPos target = resolveEmptyTarget(world, pos, ray.sideHit);
        if (target == null) {
            return new ActionResult<>(EnumActionResult.PASS, held);
        }
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.SUCCESS, held);
        }
        return this.emptyBucketAt(player, world, hand, held, target) ? new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand)) : new ActionResult<>(EnumActionResult.PASS, held);
    }

    /**
     * 1.16 {@link net.minecraft.item.BucketItem} places fluid in the adjacent air block (or uses an existing source);
     * it does not require clicking an existing fluid block.
     */
    @Nullable
    private BlockPos resolveEmptyTarget(World world, BlockPos clicked, EnumFacing facing) {
        IBlockState clickedState = world.getBlockState(clicked);
        if (matchesBucketFluid(clickedState)) {
            return clicked;
        }
        if (facing != EnumFacing.DOWN) {
            BlockPos offset = clicked.offset(facing);
            IBlockState offsetState = world.getBlockState(offset);
            if (matchesBucketFluid(offsetState) || canPlaceFluidAt(world, offset)) {
                return offset;
            }
        }
        if (canPlaceFluidAt(world, clicked)) {
            return clicked;
        }
        return null;
    }

    private boolean canPlaceFluidAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material mat = state.getMaterial();
        if (mat.isReplaceable()) {
            return true;
        }
        return matchesBucketFluid(state);
    }

    private boolean placeFluid(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (matchesBucketFluid(state)) {
            return true;
        }
        if (fluidKind == FluidKind.LAVA) {
            if (!world.setBlockState(pos, Blocks.FLOWING_LAVA.getDefaultState(), 11)) {
                return false;
            }
            ((net.minecraft.block.BlockLiquid) Blocks.FLOWING_LAVA).checkForMixing(world, pos, world.getBlockState(pos));
            return true;
        }
        if (!world.setBlockState(pos, Blocks.FLOWING_WATER.getDefaultState(), 11)) {
            return false;
        }
        ((net.minecraft.block.BlockLiquid) Blocks.FLOWING_WATER).checkForMixing(world, pos, world.getBlockState(pos));
        return true;
    }

    private boolean emptyBucketAt(EntityPlayer player, World world, EnumHand hand, ItemStack held, BlockPos target) {
        if (!this.placeFluid(world, target)) {
            return false;
        }
        Entity entity = this.fishEntry.newInstance(world);
        if (entity == null) {
            return false;
        }
        double x = target.getX() + 0.5D;
        double y = target.getY() + 0.5D;
        double z = target.getZ() + 0.5D;
        entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
        this.applyPostSpawnFromStack(held, entity);
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = living.rotationYaw;
            living.renderYawOffset = living.rotationYaw;
            if (ForgeEventFactory.doSpecialSpawn(living, world, (float) x, (float) y, (float) z, null)) {
                entity.setDead();
                return false;
            }
            if (!(entity instanceof EntityCatfish && ((EntityCatfish) entity).isFromBucket())) {
                living.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(living)), (IEntityLivingData) null);
            }
        }
        if (!world.spawnEntity(entity)) {
            entity.setDead();
            return false;
        }
        SoundEvent play = fluidKind == FluidKind.LAVA ? SoundEvents.ITEM_BUCKET_EMPTY_LAVA : SoundEvents.ITEM_BUCKET_EMPTY;
        world.playSound(null, target, play, SoundCategory.NEUTRAL, 1.0F, 1.0F);
        if (!player.capabilities.isCreativeMode) {
            consumeBucket(player, hand, held);
        }
        return true;
    }

    private ItemStack consumeBucket(EntityPlayer player, EnumHand hand, ItemStack held) {
        held.shrink(1);
        ItemStack empty = new ItemStack(Items.BUCKET);
        if (held.isEmpty()) {
            player.setHeldItem(hand, empty);
            return empty;
        }
        if (!player.inventory.addItemStackToInventory(empty)) {
            player.dropItem(empty, false);
        }
        return held;
    }

    private boolean matchesBucketFluid(IBlockState state) {
        Material mat = state.getMaterial();
        if (fluidKind == FluidKind.LAVA) {
            return mat == Material.LAVA;
        }
        return mat == Material.WATER;
    }

    /**
     * Equivalent to 1.16 {@code BucketItem} empty callback + {@code EntityType#spawn} follow-up (NBT, bucket flags).
     */
    private void applyPostSpawnFromStack(ItemStack stack, Entity entity) {
        NBTTagCompound tag = stack.getTagCompound();
        if (entity instanceof EntityLobster) {
            ((EntityLobster) entity).setFromBucket(true);
            if (tag != null && tag.hasKey("BucketVariantTag", 3)) {
                ((EntityLobster) entity).setVariant(tag.getInteger("BucketVariantTag"));
            }
        }
        if (entity instanceof EntityBlobfish) {
            ((EntityBlobfish) entity).setFromBucket(true);
            if (tag != null) {
                if (tag.hasKey("BucketScale")) {
                    ((EntityBlobfish) entity).setBlobfishScale(tag.getFloat("BucketScale"));
                }
                if (tag.hasKey("Slimed")) {
                    ((EntityBlobfish) entity).setSlimed(tag.getBoolean("Slimed"));
                }
            }
        }
        if (entity instanceof EntityStradpole) {
            ((EntityStradpole) entity).setFromBucket(true);
        }
        if (entity instanceof EntityPlatypus && tag != null && tag.hasKey("PlatypusData")) {
            ((EntityPlatypus) entity).readAdditional(tag.getCompoundTag("PlatypusData"));
        }
        if (entity instanceof EntityFrilledShark && tag != null && tag.hasKey("FrilledSharkData")) {
            ((EntityFrilledShark) entity).readAdditional(tag.getCompoundTag("FrilledSharkData"));
        }
        if (entity instanceof EntityMimicOctopus && tag != null && tag.hasKey("MimicOctopusData")) {
            ((EntityMimicOctopus) entity).readAdditional(tag.getCompoundTag("MimicOctopusData"));
            ((EntityMimicOctopus) entity).setMoistness(60000);
        }
        if (entity instanceof EntityTriops && tag != null && tag.hasKey("TriopsTag")) {
            ((EntityTriops) entity).readEntityFromNBT(tag.getCompoundTag("TriopsTag"));
            ((EntityTriops) entity).setFromBucket(true);
            ((EntityTriops) entity).setAir(2000);
        }
        if (entity instanceof EntityCombJelly) {
            ((EntityCombJelly) entity).setFromBucket(true);
            if (tag != null) {
                if (tag.hasKey("BucketScale")) {
                    ((EntityCombJelly) entity).setJellyScale(tag.getFloat("BucketScale"));
                }
                if (tag.hasKey("BucketVariantTag")) {
                    ((EntityCombJelly) entity).setVariant(tag.getInteger("BucketVariantTag"));
                }
            }
        }
        if (entity instanceof EntityCosmicCod) {
            ((EntityCosmicCod) entity).setFromBucket(true);
            if (tag != null) {
                ((EntityCosmicCod) entity).loadFromBucketTag(tag);
            }
        }
        if (entity instanceof EntityDevilsHolePupfish) {
            ((EntityDevilsHolePupfish) entity).setFromBucket(true);
            if (tag != null) {
                if (tag.hasKey("BucketScale")) {
                    ((EntityDevilsHolePupfish) entity).setPupfishScale(tag.getFloat("BucketScale"));
                }
                if (tag.hasKey("BabyAge")) {
                    ((EntityDevilsHolePupfish) entity).setBabyAge(tag.getInteger("BabyAge"));
                }
            }
        }
        if (entity instanceof EntityCatfish) {
            EntityCatfish catfish = (EntityCatfish) entity;
            catfish.setFromBucket(true);
            if (tag != null && tag.hasKey("CatfishSize")) {
                catfish.readEntityFromNBT(tag);
            } else {
                catfish.setCatfishSize(catfishSizeFromBucketItem(stack));
            }
        }
        if (entity instanceof EntityFlyingFish) {
            EntityFlyingFish fish = (EntityFlyingFish) entity;
            fish.setFromBucket(true);
            if (tag != null && tag.hasKey("Variant")) {
                fish.setVariant(tag.getInteger("Variant"));
            }
        }
    }

    private static int catfishSizeFromBucketItem(ItemStack stack) {
        if (stack.getItem() == AMItemRegistry.MEDIUM_CATFISH_BUCKET) {
            return 1;
        }
        if (stack.getItem() == AMItemRegistry.LARGE_CATFISH_BUCKET) {
            return 2;
        }
        return 0;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (this.fishEntry == AMEntityRegistry.LOBSTER) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null && tag.hasKey("BucketVariantTag", 3)) {
                int i = tag.getInteger("BucketVariantTag");
                String s = "entity.alexsmobs.lobster.variant_" + EntityLobster.getVariantName(i);
                tooltip.add(TextFormatting.GRAY.toString() + TextFormatting.ITALIC + I18n.translateToLocal(s));
            }
        }
    }
}
