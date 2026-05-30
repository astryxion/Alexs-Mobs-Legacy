package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHerdPanic;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;

public class EntityEmu extends EntityAnimal implements IAnimatedEntity, IHerdPanic {

    public static final Animation ANIMATION_DODGE_LEFT = Animation.create(10);
    public static final Animation ANIMATION_DODGE_RIGHT = Animation.create(10);
    public static final Animation ANIMATION_PECK_GROUND = Animation.create(25);
    public static final Animation ANIMATION_SCRATCH = Animation.create(20);
    public static final Animation ANIMATION_PUZZLED = Animation.create(30);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityEmu.class, DataSerializers.VARINT);
    private int animationTick;
    private Animation currentAnimation;
    private int revengeCooldown = 0;
    private boolean emuAttackedDirectly = false;
    public int timeUntilNextEgg = this.rand.nextInt(6000) + 6000;

    public EntityEmu(World world) {
        super(world);
        this.setSize(0.9F, 1.5F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.emuSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.EMU_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.EMU_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.EMU_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.EMU;
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityEmuAttackMelee(this, 1.3D, true));
        this.tasks.addTask(2, new AnimalAIHerdPanic(this, 1.5D));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.1D, Items.WHEAT, false));
        this.tasks.addTask(5, new AnimalAIWanderRanged(this, 110, 1.0D, 10, 7));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EmuAIHurtByTarget(this));
        if (AMConfig.emuTargetSkeletons) {
            this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntitySkeleton.class, false));
            this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVindicator.class, false));
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            double range = 15;
            int fleeTime = 100 + getRNG().nextInt(5);
            this.revengeCooldown = fleeTime;
            List<EntityEmu> list = this.world.getEntitiesWithinAABB(EntityEmu.class, this.getEntityBoundingBox().grow(range, range / 2, range));
            for (EntityEmu emu : list) {
                emu.revengeCooldown = fleeTime;
                if (emu.isChild() && rand.nextInt(2) == 0) {
                    emu.emuAttackedDirectly = this.getRevengeTarget() != null;
                    emu.revengeCooldown = emu.emuAttackedDirectly ? 10 + getRNG().nextInt(30) : fleeTime;
                }
            }
            emuAttackedDirectly = this.getRevengeTarget() != null;
            this.revengeCooldown = emuAttackedDirectly ? 10 + getRNG().nextInt(30) : revengeCooldown;
        }
        return prev;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        float speedMul = (this.getAnimation() == ANIMATION_PECK_GROUND || this.getAnimation() == ANIMATION_PUZZLED ? 0.15F : 1F) * (this.isInLava() ? 0.2F : 1F);
        this.setAIMoveSpeed((float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * speedMul);
        super.travel(strafe, vertical, forward);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!world.isRemote) {
            if (this.getRevengeTarget() == null && this.getAttackTarget() == null) {
                double motionSq = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
                if (motionSq < 0.03D && this.getRNG().nextInt(190) == 0 && this.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    if (getRNG().nextInt(3) == 0) {
                        this.setAnimation(ANIMATION_PUZZLED);
                    } else if (this.onGround) {
                        this.setAnimation(ANIMATION_PECK_GROUND);
                    }
                }
            }
            if (revengeCooldown > 0) {
                revengeCooldown--;
            }
            if (revengeCooldown <= 0 && this.getRevengeTarget() != null && !emuAttackedDirectly) {
                this.setRevengeTarget(null);
                revengeCooldown = 0;
            }
            if (this.getAttackTarget() != null && this.getAnimation() == ANIMATION_SCRATCH && this.getDistance(this.getAttackTarget()) < 4F
                    && (this.getAnimationTick() == 8 || this.getAnimationTick() == 15)) {
                float f1 = this.rotationYaw * ((float) Math.PI / 180F);
                this.motionX += -MathHelper.sin(f1) * 0.02F;
                this.motionZ += MathHelper.cos(f1) * 0.02F;
                this.getAttackTarget().knockBack(this, 0.4F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
        }
        if (!this.world.isRemote && this.isEntityAlive() && !this.isChild() && --this.timeUntilNextEgg <= 0) {
            this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.entityDropItem(new ItemStack(AMItemRegistry.EMU_EGG), 0.0F);
            this.timeUntilNextEgg = this.rand.nextInt(6000) + 6000;
        }
        AMEntityRegistry.updateAnimations(this);
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
        return new Animation[]{ANIMATION_DODGE_LEFT, ANIMATION_DODGE_RIGHT, ANIMATION_PECK_GROUND, ANIMATION_SCRATCH, ANIMATION_PUZZLED};
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
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityEmu emu = (EntityEmu) AMEntityRegistry.EMU.newInstance(this.world);
        emu.setVariant(this.getVariant());
        return emu;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
            this.setAnimation(ANIMATION_SCRATCH);
        }
        return true;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
        if (compound.hasKey("EggLayTime")) {
            this.timeUntilNextEgg = compound.getInteger("EggLayTime");
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
        compound.setInteger("EggLayTime", this.timeUntilNextEgg);
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        if (this.rand.nextInt(200) == 0) {
            this.setVariant(2);
        } else if (rand.nextInt(3) == 0) {
            this.setVariant(1);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onPanic() {
    }

    @Override
    public boolean canPanic() {
        return true;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.WHEAT;
    }

    private static class EntityEmuAttackMelee extends EntityAIAttackMelee {
        private final EntityEmu emu;

        EntityEmuAttackMelee(EntityEmu emu, double speedIn, boolean useLongMemory) {
            super(emu, speedIn, useLongMemory);
            this.emu = emu;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && emu.revengeCooldown <= 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && emu.revengeCooldown <= 0;
        }
    }

    private class EmuAIHurtByTarget extends EntityAIHurtByTarget {

        EmuAIHurtByTarget(EntityEmu emu) {
            super(emu, false);
        }

        @Override
        public void startExecuting() {
            if (EntityEmu.this.isChild() || !emuAttackedDirectly) {
                this.alertOthers();
                this.resetTask();
            } else {
                super.startExecuting();
            }
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature mobIn, EntityLivingBase target) {
            if (mobIn instanceof EntityEmu && !mobIn.isChild() && !emuAttackedDirectly && ((EntityEmu) mobIn).revengeCooldown <= 0) {
                super.setEntityAttackTarget(mobIn, target);
            }
        }
    }
}
