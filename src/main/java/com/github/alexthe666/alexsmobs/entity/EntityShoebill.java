package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;

public class EntityShoebill extends EntityAnimal implements IAnimatedEntity, ITargetsDroppedItems {

    public static final Animation ANIMATION_FISH = Animation.create(40);
    public static final Animation ANIMATION_BEAKSHAKE = Animation.create(20);
    public static final Animation ANIMATION_ATTACK = Animation.create(20);
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityShoebill.class, DataSerializers.BOOLEAN);
    public static final Predicate<EntityLivingBase> TARGET_BABY = entity -> entity.isChild();
    public float prevFlyProgress;
    public float flyProgress;
    public int revengeCooldown = 0;
    public int fishingCooldown = 1200 + rand.nextInt(1200);
    public int lureLevel = 0;
    public int luckLevel = 0;
    private int animationTick;
    private Animation currentAnimation;
    private boolean isLandNavigator;

    public EntityShoebill(World world) {
        super(world);
        this.setSize(0.9F, 1.5F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(false);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.shoebillSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SHOEBILL_HURT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SHOEBILL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SHOEBILL_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.SHOEBILL;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getTrueSource() != null && !(source.getTrueSource() instanceof EntityBlobfish)) {
            double range = 15;
            int fleeTime = 100 + getRNG().nextInt(150);
            this.revengeCooldown = fleeTime;
            List<EntityShoebill> list = this.world.getEntitiesWithinAABB(EntityShoebill.class, this.getEntityBoundingBox().grow(range, range / 2, range));
            for (EntityShoebill gaz : list) {
                gaz.revengeCooldown = fleeTime;
            }
        }
        return prev;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.7F);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, Boolean.FALSE);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AnimalAIWadeSwimming(this));
        this.tasks.addTask(1, new ShoebillAIFish(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, true));
        this.tasks.addTask(4, new ShoebillAIFlightFlee(this));
        this.tasks.addTask(5, new ShoebillAITempt(this, 1.1D));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 1400, 1.0D, 14, 7));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAINearestTarget3D(this, EntityBlobfish.class, 30, false, true, null));
        this.targetTasks.addTask(2, new CreatureAITargetItems(this, false, 10));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, EntityPlayer.class));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityAlligatorSnappingTurtle.class, 40, false, false, TARGET_BABY));
        this.targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntityCrocodile.class, 40, false, false, TARGET_BABY));
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isInWater()) {
            this.stepHeight = 1.2F;
        } else {
            this.stepHeight = 0.6F;
        }
        prevFlyProgress = flyProgress;
        if (isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (revengeCooldown > 0) {
            revengeCooldown--;
        }
        if (revengeCooldown == 0 && this.getRevengeTarget() != null) {
            this.setRevengeTarget(null);
        }
        if (!world.isRemote) {
            if (fishingCooldown > 0) {
                fishingCooldown--;
            }
            if (this.getAnimation() == NO_ANIMATION && this.getRNG().nextInt(700) == 0) {
                this.setAnimation(ANIMATION_BEAKSHAKE);
            }
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (this.revengeCooldown > 0 && !this.isFlying()) {
                if (this.onGround || this.isInWater()) {
                    this.setFlying(false);
                }
            }
            if (isFlying()) {
                this.setNoGravity(true);
            } else {
                this.setNoGravity(false);
            }
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.canEntityBeSeen(this.getAttackTarget())
                && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 9) {
            this.getAttackTarget().knockBack(this, 0.3F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
            this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setInteger("FishingTimer", this.fishingCooldown);
        compound.setInteger("FishingLuck", this.luckLevel);
        compound.setInteger("FishingLure", this.lureLevel);
        compound.setInteger("RevengeCooldownTimer", this.revengeCooldown);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.fishingCooldown = compound.getInteger("FishingTimer");
        this.luckLevel = compound.getInteger("FishingLuck");
        this.lureLevel = compound.getInteger("FishingLure");
        this.revengeCooldown = compound.getInteger("RevengeCooldownTimer");
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
        }
        return true;
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int i) {
        animationTick = i;
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
        return new Animation[]{ANIMATION_FISH, ANIMATION_BEAKSHAKE, ANIMATION_ATTACK};
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == AMItemRegistry.BLOBFISH && this.isEntityAlive()) {
            if (this.luckLevel < 10) {
                luckLevel = MathHelper.clamp(luckLevel + 1, 0, 10);
                for (int i = 0; i < 6 + rand.nextInt(3); i++) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(stack.getItem()), stack.getMetadata());
                }
                this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
                stack.shrink(1);
                return true;
            } else {
                if (this.getAnimation() == NO_ANIMATION) {
                    this.setAnimation(ANIMATION_BEAKSHAKE);
                }
                return true;
            }
        } else {
            return super.processInteract(player, hand);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityShoebill) AMEntityRegistry.SHOEBILL.newInstance(this.world);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.SHOEBILL_FOODSTUFFS, stack.getItem())
                || stack.getItem() == AMItemRegistry.BLOBFISH && luckLevel < 10;
    }

    public void resetFishingCooldown() {
        fishingCooldown = Math.max(1200 + rand.nextInt(1200) - lureLevel * 120, 200);
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
        if (e.getItem().getItem() == AMItemRegistry.BLOBFISH) {
            luckLevel = MathHelper.clamp(luckLevel + 1, 0, 10);
        }
        this.heal(5);
    }

    private static class ShoebillAITempt extends EntityAITempt {

        ShoebillAITempt(EntityShoebill shoebill, double speed) {
            super(shoebill, speed, Items.FISH, false);
        }

        @Override
        protected boolean isTempting(ItemStack stack) {
            return AMTagRegistry.itemInTag(AMTagRegistry.SHOEBILL_FOODSTUFFS, stack.getItem());
        }
    }

}
