package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public class EntityToucan extends EntityAnimal implements ITargetsDroppedItems {

    public float prevFlyProgress;
    public float flyProgress;
    public float prevPeckProgress;
    public float peckProgress;
    public boolean aiItemFlag;

    public EntityToucan(World world) {
        super(world);
        this.setSize(0.45F, 0.45F);
    }

    public static boolean canToucanSpawnAt(World world, BlockPos pos) {
        return true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(2, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(3, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, false, 15, 16));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!world.isAirBlock(this.getPosition())) {
            return false;
        }
        if (this.getPosition().getY() < this.world.getSeaLevel()) {
            return false;
        }
        IBlockState below = world.getBlockState(this.getPosition().down());
        Block block = below.getBlock();
        boolean validGround = block == Blocks.GRASS || block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2;
        if (!validGround) {
            return false;
        }
        Biome biome = this.world.getBiome(this.getPosition());
        return (biome.getTempCategory() == Biome.TempCategory.WARM || biome.getTempCategory() == Biome.TempCategory.MEDIUM)
                && AMEntityRegistry.rollSpawn(AMConfig.toucanSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.TOUCAN_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TOUCAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TOUCAN_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.TOUCAN;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.isEgg(stack) || AMTagRegistry.itemInTag(AMTagRegistry.TOUCAN_BREEDABLES, stack.getItem());
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (this.isBreedingItem(itemstack)) {
            if (!super.processInteract(player, hand) && !player.capabilities.isCreativeMode) {
                return true;
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return (stack.getItem() instanceof ItemFood || AMTagRegistry.itemInTag(AMTagRegistry.TOUCAN_BREEDABLES, stack.getItem())
                || AMTagRegistry.itemInTag(AMTagRegistry.TOUCAN_GOLDEN_FOODS, stack.getItem())
                || AMTagRegistry.itemInTag(AMTagRegistry.TOUCAN_ENCHANTED_GOLDEN_FOODS, stack.getItem())) && !this.isChild();
    }

    @Override
    public void onGetItem(EntityItem e) {
        ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItem(EnumHand.MAIN_HAND).isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItem(EnumHand.MAIN_HAND), 0.0F);
        }
        if (!this.world.isRemote) {
            this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
            e.setDead();
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityToucan) AMEntityRegistry.TOUCAN.newInstance(this.world);
    }

    public int getVariant() {
        return 0;
    }

    public boolean isSam() {
        return false;
    }

    public boolean isGolden() {
        return false;
    }

    public boolean isEnchanted() {
        return false;
    }
}
