package com.github.alexthe666.alexsmobs.entity;

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
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntitySugarGlider extends EntityAnimal {

    public float prevGlideProgress;
    public float glideProgress;
    public float prevSitProgress;
    public float sitProgress;
    public float forageProgress;
    public float prevForageProgress;

    public EntitySugarGlider(World world) {
        super(world);
        this.setSize(0.45F, 0.4F);
    }

    public static boolean canSugarGliderSpawnAt(World world, BlockPos pos) {
        return world.getLightFromNeighbors(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(1, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(2, new EntityAILookIdle(this));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!canSugarGliderSpawnAt(this.world, this.getPosition())) {
            return false;
        }
        if (!this.world.isAirBlock(this.getPosition())) {
            return false;
        }
        IBlockState below = this.world.getBlockState(this.getPosition().down());
        Block block = below.getBlock();
        boolean validPerch = block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.GRASS;
        return validPerch && AMEntityRegistry.rollSpawn(AMConfig.sugarGliderSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SUGAR_GLIDER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SUGAR_GLIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SUGAR_GLIDER_HURT;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntitySugarGlider) AMEntityRegistry.SUGAR_GLIDER.newInstance(this.world);
    }
}
