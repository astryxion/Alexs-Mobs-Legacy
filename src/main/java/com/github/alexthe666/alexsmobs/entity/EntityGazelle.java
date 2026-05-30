package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHerdPanic;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;

public class EntityGazelle extends EntityAnimal implements IAnimatedEntity, IHerdPanic {

    public static final Animation ANIMATION_FLICK_EARS = Animation.create(20);
    public static final Animation ANIMATION_FLICK_TAIL = Animation.create(14);
    public static final Animation ANIMATION_EAT_GRASS = Animation.create(30);
    private static final DataParameter<Boolean> RUNNING = EntityDataManager.createKey(EntityGazelle.class, DataSerializers.BOOLEAN);
    private int animationTick;
    private Animation currentAnimation;
    private boolean hasSpedUp = false;
    private int revengeCooldown = 0;

    public EntityGazelle(World worldIn) {
        super(worldIn);
        this.setSize(0.85F, 1.3F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.gazelleSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 8;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new AnimalAIHerdPanic(this, 1.1D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.1D, Items.WHEAT, false));
        this.tasks.addTask(5, new AnimalAIWanderRanged(this, 100, 1.0D, 25, 7));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GAZELLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GAZELLE_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.GAZELLE;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            double range = 15;
            int fleeTime = 100 + getRNG().nextInt(150);
            this.revengeCooldown = fleeTime;
            List<EntityGazelle> list = this.world.getEntitiesWithinAABB(EntityGazelle.class, this.getEntityBoundingBox().grow(range, range / 2, range));
            for (EntityGazelle gaz : list) {
                gaz.revengeCooldown = fleeTime;
            }
        }
        return prev;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RUNNING, Boolean.FALSE);
    }

    public boolean isRunning() {
        return this.dataManager.get(RUNNING);
    }

    public void setRunning(boolean running) {
        this.dataManager.set(RUNNING, running);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.WHEAT || stack.getItem() == AMItemRegistry.ACACIA_BLOSSOM;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote && this.getAnimation() == IAnimatedEntity.NO_ANIMATION && getRNG().nextInt(70) == 0
                && (this.getRevengeTarget() == null || this.getDistance(this.getRevengeTarget()) > 30)) {
            if (world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS && getRNG().nextInt(3) == 0) {
                this.setAnimation(ANIMATION_EAT_GRASS);
            } else {
                this.setAnimation(getRNG().nextBoolean() ? ANIMATION_FLICK_EARS : ANIMATION_FLICK_TAIL);
            }
        }
        if (!this.world.isRemote) {
            if (revengeCooldown >= 0) {
                revengeCooldown--;
            }
            if (revengeCooldown == 0 && this.getRevengeTarget() != null) {
                this.setRevengeTarget(null);
            }
            this.setRunning(revengeCooldown > 0);
            if (isRunning() && !hasSpedUp) {
                hasSpedUp = true;
                this.setSprinting(true);
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.475D);
            }
            if (!isRunning() && hasSpedUp) {
                hasSpedUp = false;
                this.setSprinting(false);
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("GazelleRunning", this.isRunning());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setRunning(compound.getBoolean("GazelleRunning"));
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_FLICK_EARS, ANIMATION_FLICK_TAIL, ANIMATION_EAT_GRASS};
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityGazelle) AMEntityRegistry.GAZELLE.newInstance(this.world);
    }

    @Override
    public void onPanic() {
    }

    @Override
    public boolean canPanic() {
        return true;
    }
}
