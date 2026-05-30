package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public class EntityPotoo extends EntityAnimal {

    public float prevFlyProgress;
    public float flyProgress;
    public float mouthProgress;
    public float prevMouthProgress;
    public float prevPerchProgress;
    public float perchProgress;

    public EntityPotoo(World world) {
        super(world);
        this.setSize(0.5F, 0.55F);
    }

    public static boolean canPotooSpawnAt(World world, BlockPos pos) {
        return world.getLightFromNeighbors(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(2, new EntityAILookIdle(this));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!canPotooSpawnAt(this.world, this.getPosition())) {
            return false;
        }
        if (!this.world.isAirBlock(this.getPosition())) {
            return false;
        }
        IBlockState below = this.world.getBlockState(this.getPosition().down());
        Block block = below.getBlock();
        boolean validPerch = block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2;
        return validPerch && AMEntityRegistry.rollSpawn(AMConfig.potooSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.POTOO_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.POTOO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.POTOO_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.POTOO;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityPotoo) AMEntityRegistry.POTOO.newInstance(this.world);
    }

    public boolean isSleeping() {
        return false;
    }

    public float getEyeScale(int ticks, float partialTick) {
        return 15F;
    }
}
