package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;

public class EntityRattlesnake extends EntityAnimal implements IAnimatedEntity {

    public static final Animation ANIMATION_BITE = Animation.create(20);
    private static final DataParameter<Boolean> RATTLING = EntityDataManager.createKey(EntityRattlesnake.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CURLED = EntityDataManager.createKey(EntityRattlesnake.class, DataSerializers.BOOLEAN);
    private static final Predicate<EntityLivingBase> WARNABLE_PREDICATE = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase mob) {
            return mob != null && (mob instanceof EntityPlayer && !((EntityPlayer) mob).capabilities.isCreativeMode && !(mob instanceof EntityPlayerMP && ((EntityPlayerMP) mob).isSpectator())
                    || mob instanceof EntityRoadrunner);
        }
    };
    private static final Predicate<EntityLivingBase> TARGETABLE_PREDICATE = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase mob) {
            return mob != null && (mob instanceof EntityPlayer && !((EntityPlayer) mob).capabilities.isCreativeMode && !(mob instanceof EntityPlayerMP && ((EntityPlayerMP) mob).isSpectator())
                    || mob instanceof EntityRoadrunner);
        }
    };
    public float prevCurlProgress;
    public float curlProgress;
    public int randomToungeTick = 0;
    public int maxCurlTime = 75;
    private int curlTime = 0;
    private int animationTick;
    private Animation currentAnimation;
    private int loopSoundTick = 0;

    public EntityRattlesnake(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 0.4F);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.2D, true));
        this.tasks.addTask(2, new WarnPredatorsGoal());
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new AnimalAIWanderRanged(this, 60, 1.0D, 7, 7));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityRabbit.class, 15, true, false, null));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new ShortDistanceTarget());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.RATTLESNAKE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.RATTLESNAKE_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.RATTLESNAKE;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.rattlesnakeSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.setAnimation(ANIMATION_BITE);
        return true;
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potioneffectIn) {
        if (potioneffectIn.getPotion() == MobEffects.POISON) {
            return false;
        }
        return super.isPotionApplicable(potioneffectIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CURLED, Boolean.FALSE);
        this.dataManager.register(RATTLING, Boolean.FALSE);
    }

    public boolean isCurled() {
        return this.dataManager.get(CURLED);
    }

    public void setCurled(boolean curled) {
        this.dataManager.set(CURLED, curled);
    }

    public boolean isRattling() {
        return this.dataManager.get(RATTLING);
    }

    public void setRattling(boolean rattling) {
        this.dataManager.set(RATTLING, rattling);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevCurlProgress = curlProgress;
        if (this.isCurled() && curlProgress < 5) {
            curlProgress += 0.5F;
        }
        if (!this.isCurled() && curlProgress > 0) {
            curlProgress -= 1;
        }
        if (rand.nextInt(15) == 0 && randomToungeTick == 0) {
            randomToungeTick = 10 + rand.nextInt(20);
        }
        if (randomToungeTick > 0) {
            randomToungeTick--;
        }
        if (isCurled() && !isRattling() && ++curlTime > maxCurlTime) {
            this.setCurled(false);
            curlTime = 0;
            maxCurlTime = 75 + rand.nextInt(50);
        }
        if (!world.isRemote && this.isCurled() && (this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive())) {
            this.setCurled(false);
        }
        if (!world.isRemote && this.isRattling() && this.getAttackTarget() == null) {
            this.setCurled(true);
        }
        if (!world.isRemote && !this.isCurled() && this.getAttackTarget() == null && rand.nextInt(500) == 0) {
            maxCurlTime = 300 + rand.nextInt(250);
            this.setCurled(true);
        }
        if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 4) {
            this.playSound(AMSoundRegistry.RATTLESNAKE_ATTACK, this.getSoundVolume(), this.getSoundPitch());
        }
        EntityLivingBase target = this.getAttackTarget();
        if (this.getAnimation() == ANIMATION_BITE && this.getAnimationTick() == 8 && target != null && this.getDistance(target) < 2D) {
            boolean meepMeep = target instanceof EntityRoadrunner;
            target.attackEntityFrom(DamageSource.causeMobDamage(this), meepMeep ? 1.0F : (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            if (!meepMeep) {
                target.addPotionEffect(new PotionEffect(MobEffects.POISON, 300, 2));
            }
        }
        if (isRattling()) {
            if (loopSoundTick == 0) {
                this.playSound(AMSoundRegistry.RATTLESNAKE_LOOP, this.getSoundVolume() * 0.5F, this.getSoundPitch());
            }
            loopSoundTick++;
            if (loopSoundTick > 50) {
                loopSoundTick = 0;
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.onGround && this.isCurled()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() instanceof ItemFood && ((ItemFood) stack.getItem()).isWolfsFavoriteMeat();
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityRattlesnake) AMEntityRegistry.RATTLESNAKE.newInstance(this.world);
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
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_BITE};
    }

    private class WarnPredatorsGoal extends EntityAIBase {
        int executionChance = 20;
        Entity target = null;

        WarnPredatorsGoal() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityRattlesnake.this.getRNG().nextInt(executionChance) == 0) {
                double dist = 5D;
                List<EntityLivingBase> list = EntityRattlesnake.this.world.getEntitiesWithinAABB(EntityLivingBase.class,
                        EntityRattlesnake.this.getEntityBoundingBox().grow(dist, dist, dist), WARNABLE_PREDICATE);
                double d0 = Double.MAX_VALUE;
                Entity possibleTarget = null;
                for (Entity entity : list) {
                    double d1 = EntityRattlesnake.this.getDistanceSq(entity);
                    if (!(d1 > d0)) {
                        d0 = d1;
                        possibleTarget = entity;
                    }
                }
                target = possibleTarget;
                return !list.isEmpty();
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && EntityRattlesnake.this.getDistance(target) < 5D && EntityRattlesnake.this.getAttackTarget() == null;
        }

        @Override
        public void resetTask() {
            target = null;
            EntityRattlesnake.this.setRattling(false);
        }

        @Override
        public void updateTask() {
            EntityRattlesnake.this.setRattling(true);
            EntityRattlesnake.this.setCurled(true);
            EntityRattlesnake.this.curlTime = 0;
            EntityRattlesnake.this.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
        }
    }

    private class ShortDistanceTarget extends EntityAINearestAttackableTarget<EntityPlayer> {

        ShortDistanceTarget() {
            super(EntityRattlesnake.this, EntityPlayer.class, 3, true, false, TARGETABLE_PREDICATE);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityRattlesnake.this.isChild()) {
                return false;
            } else {
                return super.shouldExecute();
            }
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            EntityRattlesnake.this.setCurled(false);
            EntityRattlesnake.this.setRattling(true);
        }

        @Override
        protected double getTargetDistance() {
            return 2D;
        }
    }
}
