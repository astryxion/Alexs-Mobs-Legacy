package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.SnowLeopardAIMelee;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntitySnowLeopard extends EntityAnimal implements IAnimatedEntity, ITargetsDroppedItems {

    public static final Animation ANIMATION_ATTACK_R = Animation.create(13);
    public static final Animation ANIMATION_ATTACK_L = Animation.create(13);
    private int animationTick;
    private Animation currentAnimation;
    public float prevSneakProgress;
    public float sneakProgress;
    public float prevTackleProgress;
    public float tackleProgress;
    public float prevSitProgress;
    public float sitProgress;
    private static final DataParameter<Boolean> TACKLING = EntityDataManager.createKey(EntitySnowLeopard.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntitySnowLeopard.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntitySnowLeopard.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SL_SNEAKING = EntityDataManager.createKey(EntitySnowLeopard.class, DataSerializers.BOOLEAN);
    private boolean hasSlowedDown = false;
    private boolean pounceAttacking = false;
    private int sittingTime = 0;
    private int maxSitTime = 75;
    public float prevSleepProgress;
    public float sleepProgress;

    public EntitySnowLeopard(World worldIn) {
        super(worldIn);
        this.setSize(1.2F, 1.3F);
        this.stepHeight = 2F;
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos down = new BlockPos(this.posX, this.posY - 1, this.posZ);
        IBlockState blockstate = this.world.getBlockState(down);
        Block block = blockstate.getBlock();
        boolean validGround = block == Blocks.STONE || block == Blocks.DIRT || block == Blocks.GRASS
                || block == Blocks.SNOW || block == Blocks.SNOW_LAYER;
        return validGround && this.world.getLight(down) > 8
                && AMEntityRegistry.rollSpawn(AMConfig.snowLeopardSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == AMItemRegistry.MOOSE_RIBS || stack.getItem() == AMItemRegistry.COOKED_MOOSE_RIBS;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(3, new SnowLeopardAIMelee(this));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 70, 1.0D, 14, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 10, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.SNOW_LEOPARD_TARGETS)));
        this.targetTasks.addTask(3, new CreatureAITargetItems(this, false, 30));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SNOW_LEOPARD_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SNOW_LEOPARD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SNOW_LEOPARD_HURT;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(SLEEPING, Boolean.FALSE);
        this.dataManager.register(SL_SNEAKING, Boolean.FALSE);
        this.dataManager.register(TACKLING, Boolean.FALSE);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean bar) {
        this.dataManager.set(SITTING, bar);
    }

    public boolean isTackling() {
        return this.dataManager.get(TACKLING);
    }

    public void setTackling(boolean bar) {
        this.dataManager.set(TACKLING, bar);
    }

    public boolean isSLSneaking() {
        return this.dataManager.get(SL_SNEAKING);
    }

    public void setSlSneaking(boolean bar) {
        this.dataManager.set(SL_SNEAKING, bar);
    }

    public boolean isPounceAttacking() {
        return pounceAttacking;
    }

    public void setPounceAttacking(boolean pounceAttacking) {
        this.pounceAttacking = pounceAttacking;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntitySnowLeopard) AMEntityRegistry.SNOW_LEOPARD.newInstance(this.world);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevSitProgress = sitProgress;
        this.prevSneakProgress = sneakProgress;
        this.prevTackleProgress = tackleProgress;
        this.prevSleepProgress = sleepProgress;
        if (this.isSitting() && sitProgress < 5F) {
            sitProgress += 0.5F;
        }
        if (!isSitting() && sitProgress > 0F) {
            sitProgress -= 0.5F;
        }
        if (this.isSLSneaking() && sneakProgress < 5F) {
            sneakProgress++;
        }
        if (!isSLSneaking() && sneakProgress > 0F) {
            sneakProgress--;
        }
        if (this.isTackling() && tackleProgress < 3F) {
            tackleProgress++;
        }
        if (!isTackling() && tackleProgress > 0F) {
            tackleProgress--;
        }
        if (this.isSleeping() && sleepProgress < 5F) {
            sleepProgress += 0.5F;
        }
        if (!isSleeping() && sleepProgress > 0F) {
            sleepProgress -= 0.5F;
        }
        if (isSLSneaking() && !hasSlowedDown) {
            hasSlowedDown = true;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        }
        if (!isSLSneaking() && hasSlowedDown) {
            hasSlowedDown = false;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        }
        if (isTackling()) {
            this.renderYawOffset = this.rotationYaw;
        }
        EntityLivingBase attackTarget = this.getAttackTarget();
        if (attackTarget == this) {
            this.setAttackTarget(null);
            attackTarget = null;
        }
        if (attackTarget != null) {
            if (this.getDistance(attackTarget) < attackTarget.width + this.width + 0.6F && this.canEntityBeSeen(attackTarget)) {
                if (this.getAnimation() == ANIMATION_ATTACK_L && this.getAnimationTick() == 7) {
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    float rot = this.rotationYaw + 90.0F;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * 0.017453292F), -MathHelper.cos(rot * 0.017453292F));
                }
                if (this.getAnimation() == ANIMATION_ATTACK_R && this.getAnimationTick() == 7) {
                    attackTarget.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    float rot = this.rotationYaw - 90.0F;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * 0.017453292F), -MathHelper.cos(rot * 0.017453292F));
                }
            }
        }
        if (!world.isRemote) {
            if (this.getAttackTarget() != null && (this.isSitting() || this.isSleeping())) {
                this.setSitting(false);
                this.setSleeping(false);
            }
            if ((isSitting() || isSleeping()) && (++sittingTime > maxSitTime || this.getAttackTarget() != null || this.isInLove() || this.isInWater())) {
                this.setSitting(false);
                this.setSleeping(false);
                sittingTime = 0;
                maxSitTime = 100 + rand.nextInt(50);
            }
            double motionSq = this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ;
            if (this.getAttackTarget() == null && motionSq < 0.03D && this.getAnimation() == NO_ANIMATION && !this.isSleeping() && !this.isSitting() && !this.isInWater() && rand.nextInt(340) == 0) {
                sittingTime = 0;
                if (this.getRNG().nextInt(2) != 0) {
                    maxSitTime = 200 + rand.nextInt(800);
                    this.setSitting(true);
                    this.setSleeping(false);
                } else {
                    maxSitTime = 2000 + rand.nextInt(2600);
                    this.setSitting(false);
                    this.setSleeping(true);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() == this || source.getImmediateSource() == this) {
            return false;
        }
        if (this.pounceAttacking && (source == DamageSource.FALL || source == DamageSource.FLY_INTO_WALL || source == DamageSource.IN_WALL)) {
            return false;
        }
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            if (!this.world.isRemote && source.getTrueSource() instanceof EntityLivingBase && source.getTrueSource() != this) {
                EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
                this.setRevengeTarget(attacker);
                this.setAttackTarget(attacker);
            }
            sittingTime = 0;
            this.setSleeping(false);
            this.setSitting(false);
        }
        return prev;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        if (source == DamageSource.IN_WALL) {
            return true;
        }
        if ((this.isTackling() || this.pounceAttacking)
                && (source == DamageSource.IN_WALL || source == DamageSource.FLY_INTO_WALL || source == DamageSource.FALL)) {
            return true;
        }
        return super.isEntityInvulnerable(source);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting() || this.isSleeping()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, sleeping);
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
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_ATTACK_L, ANIMATION_ATTACK_R};
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        ItemFood food = stack.getItem() instanceof ItemFood ? (ItemFood) stack.getItem() : null;
        return food != null && food.isWolfsFavoriteMeat();
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.heal(5);
    }
}
