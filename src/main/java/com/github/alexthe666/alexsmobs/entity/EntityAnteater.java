package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.Locale;
import java.util.UUID;

public class EntityAnteater extends EntityAnimal implements IAngerable, IAnimatedEntity, ITargetsDroppedItems {

    public static final Animation ANIMATION_SLASH_R = Animation.create(20);
    public static final Animation ANIMATION_TOUNGE_IDLE = Animation.create(10);
    public static final Animation ANIMATION_SLASH_L = Animation.create(20);

    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityAnteater.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LEANING_DOWN = EntityDataManager.createKey(EntityAnteater.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ANT_ON_TONGUE = EntityDataManager.createKey(EntityAnteater.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ANGER_TIME = EntityDataManager.createKey(EntityAnteater.class, DataSerializers.VARINT);

    public float prevStandProgress;
    public float standProgress;
    public float prevTongueProgress;
    public float tongueProgress;
    public float prevLeaningProgress;
    public float leaningProgress;
    public int eatAntCooldown = 0;
    public int ticksAntOnTongue = 0;
    private int animationTick;
    private Animation currentAnimation;
    private int maxStandTime = 75;
    private int standingTime = 0;
    private int antsEatenRecently = 0;
    private int heldItemTime;
    @Nullable
    private UUID lastHurtBy;
    private long lastPlayerAttackGameTime = -1000000L;

    public EntityAnteater(World world) {
        super(world);
        this.stepHeight = 1.0F;
        this.setSize(0.9F, 0.9F);
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.ANTEATER;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.anteaterSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new AIMelee());
        this.tasks.addTask(3, new AnteaterAIRaidNest(this));
        this.tasks.addTask(4, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(5, new AnimalAIRideParent(this, 1.25D));
        this.tasks.addTask(6, new EntityAITempt(this, 1.2D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.ANTEATER_FOODSTUFFS, stack.getItem());
            }
        });
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 110, 1.0D, 10, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, false, 25, 16));
        this.targetTasks.addTask(2, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(3, new AITargetAnts());
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return super.isEntityInvulnerable(source) || source.getTrueSource() != null && source.getTrueSource() instanceof EntityLeafcutterAnt;
    }

    @Override
    protected net.minecraft.util.SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ANTEATER_HURT;
    }

    @Override
    protected net.minecraft.util.SoundEvent getDeathSound() {
        return AMSoundRegistry.ANTEATER_HURT;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Standing", this.isStanding());
        compound.setInteger("AntCooldown", this.eatAntCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setStanding(compound.getBoolean("Standing"));
        this.eatAntCooldown = compound.getInteger("AntCooldown");
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.ANTEATER_BREEDABLES, stack.getItem());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STANDING, Boolean.FALSE);
        this.dataManager.register(ANT_ON_TONGUE, Boolean.FALSE);
        this.dataManager.register(LEANING_DOWN, Boolean.FALSE);
        this.dataManager.register(ANGER_TIME, 0);
    }

    @Override
    public int getAngerTime() {
        return this.dataManager.get(ANGER_TIME);
    }

    @Override
    public void setAngerTime(int time) {
        this.dataManager.set(ANGER_TIME, time);
    }

    @Override
    public UUID getAngerTarget() {
        return this.lastHurtBy;
    }

    @Override
    public void setAngerTarget(@Nullable UUID target) {
        this.lastHurtBy = target;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(600 + this.rand.nextInt(601));
    }

    @Override
    public void resetTargets() {
        this.setAngerTime(0);
        this.setAngerTarget(null);
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
    }

    @Override
    public boolean wasHurtByPlayerRecently() {
        return world.getTotalWorldTime() - this.lastPlayerAttackGameTime <= 100L;
    }

    public boolean isStanding() {
        return this.dataManager.get(STANDING);
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, standing);
    }

    public boolean hasAntOnTongue() {
        return this.dataManager.get(ANT_ON_TONGUE);
    }

    public void setAntOnTongue(boolean standing) {
        this.dataManager.set(ANT_ON_TONGUE, standing);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (!(entity instanceof EntityLeafcutterAnt)) {
            super.applyEntityCollision(entity);
        }
    }

    public boolean isLeaning() {
        return this.dataManager.get(LEANING_DOWN);
    }

    public void setLeaning(boolean leaning) {
        this.dataManager.set(LEANING_DOWN, leaning);
    }

    @Override
  protected void updateAITasks() {
        super.updateAITasks();
        if (!this.world.isRemote) {
            tickIAngerable();
        }
    }

    private void tickIAngerable() {
        if (this.getAngerTime() > 0) {
            this.setAngerTime(this.getAngerTime() - 1);
        }
        UUID id = this.getAngerTarget();
        if (id != null) {
            Entity e = null;
            if (this.world instanceof WorldServer) {
                e = ((WorldServer) this.world).getEntityFromUuid(id);
            }
            if (e instanceof EntityLivingBase) {
                if (this.getAngerTime() > 0 && this.getAttackTarget() != e) {
                    this.setAttackTarget((EntityLivingBase) e);
                }
            } else if (this.getAngerTime() <= 0) {
                this.setAngerTarget(null);
                this.setAttackTarget(null);
            }
        } else if (this.getAngerTime() <= 0) {
            this.setAttackTarget(null);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        final ItemStack itemstack = player.getHeldItem(hand);
        final boolean type = super.processInteract(player, hand);
        final boolean isFoodstuff = AMTagRegistry.itemInTag(AMTagRegistry.ANTEATER_FOODSTUFFS, itemstack.getItem());
        if (isFoodstuff && !itemstack.isEmpty()) {
            final ItemStack rippedStack = itemstack.copy();
            rippedStack.setCount(1);
            this.resetTargets();
            this.heal(4.0F);
            this.setHeldItem(EnumHand.MAIN_HAND, rippedStack);
            if (AMTagRegistry.itemInTag(AMTagRegistry.ANTEATER_BREEDABLES, itemstack.getItem())) {
                return type;
            }
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            return true;
        }
        return type;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevStandProgress = standProgress;
        prevTongueProgress = tongueProgress;
        prevLeaningProgress = leaningProgress;

        if (isStanding()) {
            if (standProgress < 5F) {
                standProgress++;
            }
        } else {
            if (standProgress > 0F) {
                standProgress--;
            }
        }

        final boolean isTongueOut = this.getAnimation() == ANIMATION_TOUNGE_IDLE;
        if (isTongueOut) {
            if (tongueProgress < 5F) {
                tongueProgress++;
            }
        } else {
            if (tongueProgress > 0F) {
                tongueProgress--;
            }
        }

        if (isLeaning()) {
            if (leaningProgress < 5F) {
                leaningProgress++;
            }
        } else {
            if (leaningProgress > 0F) {
                leaningProgress--;
            }
        }

        if (isStanding() && ++standingTime > maxStandTime) {
            this.setStanding(false);
            standingTime = 0;
            maxStandTime = 75 + rand.nextInt(50);
        }

        if (this.isRiding() && this.getRidingEntity() instanceof EntityAnteater) {
            EntityAnteater mount = (EntityAnteater) this.getRidingEntity();
            if (this.isChild()) {
                this.rotationYaw = mount.renderYawOffset;
                this.rotationYawHead = mount.renderYawOffset;
                this.renderYawOffset = mount.renderYawOffset;
            } else {
                this.dismountRidingEntity();
            }
        }

        if (eatAntCooldown > 0) {
            eatAntCooldown--;
        }
        if (antsEatenRecently >= 3 && eatAntCooldown <= 0) {
            this.resetAntCooldown();
        }
        if (ticksAntOnTongue > 10 && this.hasAntOnTongue()) {
            this.heal(6.0F);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            this.setAntOnTongue(false);
        }
        if (this.hasAntOnTongue()) {
            ticksAntOnTongue++;
        } else {
            ticksAntOnTongue = 0;
        }
        if (!this.world.isRemote && getTongueStickOut() > 0.6F && !this.hasAntOnTongue() && antsEatenRecently < 3) {
            EntityLeafcutterAnt closestAnt = null;
            for (EntityLeafcutterAnt entity : this.world.getEntitiesWithinAABB(EntityLeafcutterAnt.class, this.getEntityBoundingBox().grow(2.6D))) {
                if (closestAnt == null || entity.getDistance(this) < closestAnt.getDistance(this) && this.canEntityBeSeen(entity)) {
                    closestAnt = entity;
                }
            }
            if (closestAnt != null) {
                closestAnt.setDead();
                ticksAntOnTongue = 0;
                this.setAntOnTongue(true);
                antsEatenRecently++;
            }
        }
        if (!this.getHeldItemMainhand().isEmpty()) {
            heldItemTime++;
            if (heldItemTime > 10 && getTongueStickOut() < 0.3F && canTargetItem(this.getHeldItemMainhand())) {
                heldItemTime = 0;
                this.heal(4.0F);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                ItemStack held = this.getHeldItemMainhand();
                ItemStack container = held.getItem().getContainerItem(held);
                if (!container.isEmpty()) {
                    this.entityDropItem(container, 0.0F);
                }
                this.resetTargets();
                held.shrink(1);
            }
        } else {
            heldItemTime = 0;
        }

        if (!this.world.isRemote) {
            if (getRNG().nextInt(300) == 0) {
                this.setAnimation(ANIMATION_TOUNGE_IDLE);
            }

            final EntityLivingBase attackTarget = this.getAttackTarget();
            if (attackTarget != null) {
                if (this.getDistance(attackTarget) < attackTarget.width + this.width + 2) {
                    if (this.getAnimationTick() == 7) {
                        if (this.getAnimation() == ANIMATION_SLASH_L) {
                            this.attackEntityAsMob(attackTarget);
                            final float rot = rotationYaw + 90;
                            attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * 0.017453292F), -MathHelper.cos(rot * 0.017453292F));
                        } else if (this.getAnimation() == ANIMATION_SLASH_R) {
                            this.attackEntityAsMob(attackTarget);
                            final float rot = rotationYaw - 90;
                            attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * 0.017453292F), -MathHelper.cos(rot * 0.017453292F));
                        }
                    }
                }
            }
        }

        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean ok = super.attackEntityFrom(source, amount);
        if (ok && source.getTrueSource() instanceof EntityPlayer) {
            this.lastPlayerAttackGameTime = world.getTotalWorldTime();
        }
        return ok;
    }

    public void resetAntCooldown() {
        this.eatAntCooldown = 600 + rand.nextInt(1000);
        this.antsEatenRecently = 0;
    }

    public void standFor(int time) {
        this.setStanding(true);
        this.maxStandTime = time;
    }

    public float getTongueStickOut() {
        if (this.tongueProgress > 0F) {
            final double tongueM = Math.min(Math.sin(this.ticksExisted * 0.15F), 0);
            return (float) -tongueM * (this.tongueProgress * 0.2F);
        }
        return 0.0F;
    }

    @Nullable
    @Override
    public EntityAnteater createChild(EntityAgeable ageable) {
        return new EntityAnteater(this.world);
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
    public boolean canTargetItem(ItemStack stack) {
        return !this.hasAntOnTongue() && AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        final ItemStack duplicate = e.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItemMainhand().isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
        }
        this.setAnimation(ANIMATION_TOUNGE_IDLE);
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_SLASH_L, ANIMATION_SLASH_R, ANIMATION_TOUNGE_IDLE};
    }

    private boolean shouldTargetAnts() {
        return !this.isAngry();
    }

    public boolean isPeter() {
        final String name = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        if (name == null) {
            return false;
        }
        final String lowercaseName = name.toLowerCase(Locale.ROOT);
        return lowercaseName.contains("peter") || lowercaseName.contains("petr") || lowercaseName.contains("zot");
    }

    private static final Predicate<EntityLeafcutterAnt> ANTEATER_QUEEN_ANT = entity -> !entity.isQueen();

    private class AITargetAnts extends EntityAINearestAttackableTarget<EntityLeafcutterAnt> {

        public AITargetAnts() {
            super(EntityAnteater.this, EntityLeafcutterAnt.class, 30, true, false, ANTEATER_QUEEN_ANT);
        }

        @Override
        public boolean shouldExecute() {
            return EntityAnteater.this.shouldTargetAnts() && !EntityAnteater.this.isChild() && !EntityAnteater.this.hasAntOnTongue() && !EntityAnteater.this.isStanding() && super.shouldExecute();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return EntityAnteater.this.shouldTargetAnts() && !EntityAnteater.this.hasAntOnTongue() && !EntityAnteater.this.isStanding() && super.shouldContinueExecuting();
        }
    }

    private class AIMelee extends EntityAIBase {
        public AIMelee() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return EntityAnteater.this.getAttackTarget() != null && EntityAnteater.this.getAttackTarget().isEntityAlive() && !EntityAnteater.this.isChild();
        }

        @Override
        public void updateTask() {
            final EntityLivingBase enemy = EntityAnteater.this.getAttackTarget();
            if (enemy != null) {
                final double attackReachSqr = this.getAttackReachSqr(enemy);
                final double distToEnemySqr = EntityAnteater.this.getDistanceSq(enemy);
                EntityAnteater.this.getLookHelper().setLookPositionWithEntity(enemy, 100.0F, 5.0F);
                if (enemy instanceof EntityLeafcutterAnt) {
                    if (distToEnemySqr <= attackReachSqr + 1.5F) {
                        EntityAnteater.this.setAnimation(ANIMATION_TOUNGE_IDLE);
                    } else {
                        EntityAnteater.this.getLookHelper().setLookPositionWithEntity(enemy, 5.0F, 5.0F);
                    }
                    EntityAnteater.this.getNavigator().tryMoveToEntityLiving(enemy, 1.0D);
                } else {
                    if (distToEnemySqr <= attackReachSqr) {
                        EntityAnteater.this.getNavigator().tryMoveToEntityLiving(enemy, 1.0D);
                        EntityAnteater.this.setAnimation(EntityAnteater.this.getRNG().nextBoolean() ? ANIMATION_SLASH_L : ANIMATION_SLASH_R);
                    }
                    final double x = enemy.posX - EntityAnteater.this.posX;
                    final double z = enemy.posZ - EntityAnteater.this.posZ;
                    final float f = (float) (MathHelper.atan2(z, x) * (180D / Math.PI)) - 90.0F;
                    EntityAnteater.this.rotationYaw = f;
                    EntityAnteater.this.renderYawOffset = f;
                    EntityAnteater.this.setStanding(true);
                }
            }
        }

        @Override
        public void resetTask() {
            EntityAnteater.this.setStanding(false);
            super.resetTask();
        }

        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            return 2.0F + attackTarget.width;
        }
    }
}
