package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;

public class EntityCrocodile extends EntityTameable implements IAnimatedEntity, ISemiAquatic {

    public static final Animation ANIMATION_LUNGE = Animation.create(23);
    public static final Animation ANIMATION_DEATHROLL = Animation.create(40);
    public static final Predicate<EntityLivingBase> NOT_CREEPER = entity -> entity.isEntityAlive() && !(entity instanceof EntityCreeper);
    private static final DataParameter<Byte> CLIMBING = EntityDataManager.createKey(EntityCrocodile.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityCrocodile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DESERT = EntityDataManager.createKey(EntityCrocodile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> STUN_TICKS = EntityDataManager.createKey(EntityCrocodile.class, DataSerializers.VARINT);
    public float groundProgress = 0;
    public float prevGroundProgress = 0;
    public float swimProgress = 0;
    public float prevSwimProgress = 0;
    public float baskingProgress = 0;
    public float prevBaskingProgress = 0;
    public float grabProgress = 0;
    public float prevGrabProgress = 0;
    public int baskingType = 0;
    public boolean forcedSit = false;
    private int baskingTimer = 0;
    private int swimTimer = -1000;
    private int ticksSinceInWater = 0;
    private int passengerTimer = 0;
    private boolean isLandNavigator;
    private boolean hasSpedUp = false;
    private int animationTick;
    private Animation currentAnimation;
    /** 1.16 {@code setInLove(int)} after egg lay — blocks re-breeding until ticks elapse. */
    private int loveCooldownTicks = 0;

    public EntityCrocodile(World worldIn) {
        super(worldIn);
        this.setSize(2.15F, 0.75F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, 0.0F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, 0.0F);
        switchNavigator(false);
        this.baskingType = rand.nextInt(1);
    }

    public void setLoveCooldownTicks(int ticks) {
        this.loveCooldownTicks = ticks;
    }

    @Override
    public boolean isInLove() {
        return this.loveCooldownTicks <= 0 && super.isInLove();
    }

    public static boolean canCrocodileSpawn(World worldIn, BlockPos pos) {
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.CROCODILE_SPAWNS, worldIn.getBlockState(pos.down()).getBlock());
        return spawnBlock && pos.getY() < worldIn.getSeaLevel() + 4;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.4D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.crocSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected void onGrowingAdult() {
        super.onGrowingAdult();
        if (!this.isChild() && this.world.getGameRules().getBoolean("doMobLoot")) {
            this.entityDropItem(new ItemStack(AMItemRegistry.CROCODILE_SCUTE, rand.nextInt(1) + 1), 1.0F);
        }
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setDesert(this.isBiomeDesert(this.world, this.getPosition()));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    private boolean isBiomeDesert(World worldIn, BlockPos position) {
        return BiomeDictionary.hasType(worldIn.getBiome(position), BiomeDictionary.Type.SANDY);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isChild() ? AMSoundRegistry.CROCODILE_BABY : AMSoundRegistry.CROCODILE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CROCODILE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CROCODILE_HURT;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("CrocodileSitting", this.isSitting());
        compound.setBoolean("Desert", this.isDesert());
        compound.setBoolean("ForcedToSit", this.forcedSit);
        compound.setInteger("BaskingStyle", this.baskingType);
        compound.setInteger("BaskingTimer", this.baskingTimer);
        compound.setInteger("SwimTimer", this.swimTimer);
        compound.setInteger("StunTimer", this.getStunTicks());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("CrocodileSitting"));
        this.setDesert(compound.getBoolean("Desert"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.baskingType = compound.getInteger("BaskingStyle");
        this.baskingTimer = compound.getInteger("BaskingTimer");
        this.swimTimer = compound.getInteger("SwimTimer");
        this.setStunTicks(compound.getInteger("StunTimer"));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AquaticMoveController(this, 1F);
            this.navigator = new SemiAquaticPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(DESERT, Boolean.FALSE);
        this.dataManager.register(CLIMBING, (byte) 0);
        this.dataManager.register(STUN_TICKS, 0);
    }

    public boolean isBesideClimbableBlock() {
        return (this.dataManager.get(CLIMBING) & 1) != 0;
    }

    public void setBesideClimbableBlock(boolean climbing) {
        byte b0 = this.dataManager.get(CLIMBING);
        if (climbing) {
            b0 = (byte) (b0 | 1);
        } else {
            b0 = (byte) (b0 & -2);
        }
        this.dataManager.set(CLIMBING, b0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevGroundProgress = groundProgress;
        this.prevSwimProgress = swimProgress;
        this.prevBaskingProgress = baskingProgress;
        this.prevGrabProgress = grabProgress;
        boolean ground = !this.isInWater();
        boolean groundAnimate = !this.isInWater();
        boolean basking = groundAnimate && this.isSitting();
        boolean grabbing = !this.getPassengers().isEmpty();
        if (!ground && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (ground && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (groundAnimate && this.groundProgress < 10F) {
            this.groundProgress++;
        }
        if (!groundAnimate && this.groundProgress > 0F) {
            this.groundProgress--;
        }
        if (!groundAnimate && this.swimProgress < 10F) {
            this.swimProgress++;
        }
        if (groundAnimate && this.swimProgress > 0F) {
            this.swimProgress--;
        }
        if (basking && this.baskingProgress < 10F) {
            this.baskingProgress++;
        }
        if (!basking && this.baskingProgress > 0F) {
            this.baskingProgress--;
        }
        if (grabbing && this.grabProgress < 10F) {
            this.grabProgress++;
        }
        if (!grabbing && this.grabProgress > 0F) {
            this.grabProgress--;
        }
        if (this.getAttackTarget() != null && !hasSpedUp) {
            hasSpedUp = true;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
        }
        if (this.getAttackTarget() == null && hasSpedUp) {
            hasSpedUp = false;
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        }
        if (!this.world.isRemote) {
            this.setBesideClimbableBlock(this.collidedHorizontally);
        }
        if (baskingTimer < 0) {
            baskingTimer++;
        }
        if (passengerTimer > 0 && this.getPassengers().isEmpty()) {
            passengerTimer = 0;
        }
        if (!world.isRemote) {
            if (isInWater()) {
                swimTimer++;
                ticksSinceInWater = 0;
            } else {
                ticksSinceInWater++;
                swimTimer--;
            }
        }
        if (!world.isRemote && !this.isInWater() && this.onGround) {
            if (!this.isTamed() && !this.isChild()) {
                if (!this.isSitting() && baskingTimer == 0 && this.getAttackTarget() == null && this.getNavigator().noPath()) {
                    this.setSitting(true);
                    this.baskingTimer = 1000 + rand.nextInt(750);
                }
                if (this.isSitting() && (baskingTimer <= 0 || this.getAttackTarget() != null || swimTimer < -1000)) {
                    this.setSitting(false);
                    this.baskingTimer = -2000 - rand.nextInt(750);
                }
                if (this.isSitting() && baskingTimer > 0) {
                    baskingTimer--;
                }
            }
        }
        if (!world.isRemote && this.getStunTicks() == 0 && this.isEntityAlive() && this.getAttackTarget() != null && this.getAnimation() == ANIMATION_LUNGE && (world.getDifficulty() != EnumDifficulty.PEACEFUL || !(this.getAttackTarget() instanceof EntityPlayer)) && this.getAnimationTick() > 5 && this.getAnimationTick() < 9) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * 0.02F;
            this.motionZ += MathHelper.cos(f1) * 0.02F;
            if (this.getDistance(this.getAttackTarget()) < 3.5F && this.canEntityBeSeen(this.getAttackTarget())) {
                boolean flag = this.getAttackTarget() instanceof EntityPlayer && ((EntityPlayer) this.getAttackTarget()).isActiveItemStackBlocking();
                if (!flag) {
                    if (this.getAttackTarget().width < this.width && this.getPassengers().isEmpty() && !this.getAttackTarget().isSneaking()) {
                        this.getAttackTarget().startRiding(this, true);
                    }
                }
                if (flag) {
                    if (this.getAttackTarget() instanceof EntityPlayer) {
                        this.damageShieldFor((EntityPlayer) this.getAttackTarget(), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
                    }
                    if (this.getStunTicks() == 0) {
                        this.setStunTicks(25 + rand.nextInt(20));
                    }
                } else {
                    this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
                }
                this.playSound(AMSoundRegistry.CROCODILE_BITE, this.getSoundVolume(), this.getSoundPitch());
            }
        }
        if (!world.isRemote && this.isEntityAlive() && this.getAttackTarget() != null && this.isInWater() && (world.getDifficulty() != EnumDifficulty.PEACEFUL || !(this.getAttackTarget() instanceof EntityPlayer))) {
            if (this.getAttackTarget().getRidingEntity() != null && this.getAttackTarget().getRidingEntity() == this) {
                if (this.getAnimation() == NO_ANIMATION) {
                    this.setAnimation(ANIMATION_DEATHROLL);
                }
                if (this.getAnimation() == ANIMATION_DEATHROLL && this.getAnimationTick() % 10 == 0 && this.getDistance(this.getAttackTarget()) < 5D) {
                    this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 2);
                }
            }
        }
        if (this.getAnimation() == ANIMATION_DEATHROLL) {
            this.getNavigator().clearPath();
        }
        if (this.loveCooldownTicks > 0) {
            --this.loveCooldownTicks;
        }
        if (this.isInLove() && this.getAttackTarget() != null) {
            this.setAttackTarget(null);
        }
        if (this.getStunTicks() > 0) {
            this.setStunTicks(this.getStunTicks() - 1);
            if (world.isRemote) {
                float angle = (0.01745329251F * this.renderYawOffset);
                double headX = 1.5F * getRenderScale() * MathHelper.sin((float) (Math.PI + angle));
                double headZ = 1.5F * getRenderScale() * MathHelper.cos(angle);
                for (int i = 0; i < 5; i++) {
                    float innerAngle = (0.01745329251F * (this.renderYawOffset + ticksExisted * 5) * (i + 1));
                    double extraX = 0.5F * MathHelper.sin((float) (Math.PI + innerAngle));
                    double extraZ = 0.5F * MathHelper.cos(innerAngle);
                    world.spawnParticle(EnumParticleTypes.CRIT, this.posX + headX + extraX, this.posY + (double) this.getEyeHeight() + 0.5F, this.posZ + headZ + extraZ, 0, 0, 0);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
    }

    protected void damageShieldFor(EntityPlayer holder, float damage) {
        if (holder.isActiveItemStackBlocking()) {
            if (!this.world.isRemote) {
                holder.addStat(StatList.getObjectUseStats(holder.getActiveItemStack().getItem()));
            }

            if (damage >= 3.0F) {
                int i = 1 + MathHelper.floor(damage);
                EnumHand hand = holder.getActiveHand();
                holder.getActiveItemStack().damageItem(i, holder);
                if (holder.getActiveItemStack().isEmpty()) {
                    if (hand == EnumHand.MAIN_HAND) {
                        holder.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        holder.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }
                    holder.playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.8F, 0.8F + this.world.rand.nextFloat() * 0.4F);
                }
            }
        }
    }

    @Override
    protected boolean isMovementBlocked() {
        return super.isMovementBlocked() || this.getStunTicks() > 0;
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase owner = this.getOwner();
            if (entityIn == owner) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(owner);
            }
            if (owner != null) {
                return owner.isOnSameTeam(entityIn);
            }
        }

        return super.isOnSameTeam(entityIn);
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (!this.getPassengers().isEmpty()) {
            this.renderYawOffset = MathHelper.wrapDegrees(this.rotationYaw - 180F);
        }
        if (this.getPassengers().contains(passenger)) {
            float radius = 2F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + 0.1F, this.posZ + extraZ);
            passengerTimer++;
            if (this.isEntityAlive() && passengerTimer > 0 && passengerTimer % 40 == 0) {
                passenger.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
            }
        }
    }

    @Override
    public boolean isOnLadder() {
        return isInWater() && this.isBesideClimbableBlock();
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION && this.getPassengers().isEmpty() && this.getStunTicks() == 0) {
            this.setAnimation(ANIMATION_LUNGE);
        }
        return true;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.DROWN || source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    public boolean shouldLeaveWater() {
        if (!this.getPassengers().isEmpty()) {
            return false;
        }
        if (this.getAttackTarget() != null && !this.getAttackTarget().isInWater()) {
            return true;
        }
        return swimTimer > 600;
    }

    @Override
    public boolean shouldStopMoving() {
        return this.getAnimation() == ANIMATION_DEATHROLL;
    }

    @Override
    public int getWaterSearchRange() {
        return this.getPassengers().isEmpty() ? 15 : 45;
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isDesert() {
        return this.dataManager.get(DESERT);
    }

    public void setDesert(boolean desert) {
        this.dataManager.set(DESERT, desert);
    }

    public int getStunTicks() {
        return this.dataManager.get(STUN_TICKS);
    }

    private void setStunTicks(int stun) {
        this.dataManager.set(STUN_TICKS, stun);
    }

    public float getRenderScale() {
        return this.isChild() ? 0.5F : 1.0F;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISit(this));
        this.tasks.addTask(1, new AnimalAIMate(this, 1.0D) {
            @Override
            protected void spawnBaby() {
                super.spawnBaby();
                EntityCrocodile.this.setLoveCooldownTicks(600);
            }
        });
        this.tasks.addTask(2, new BreatheAirGoal(this));
        this.tasks.addTask(2, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new AnimalAILeaveWater(this));
        this.tasks.addTask(4, new CrocodileAIMelee(this, 1, true));
        this.tasks.addTask(5, new CrocodileAIRandomSwimming(this, 1.0D, 7));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this, true));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntityPlayer.class, 80, false, true, null) {
            @Override
            public boolean shouldExecute() {
                return !isChild() && !isTamed() && world.getDifficulty() != EnumDifficulty.PEACEFUL && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(5, new EntityAINearestTarget3D(this, EntityLivingBase.class, 180, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.CROCODILE_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return !isChild() && !isTamed() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(6, new EntityAINearestTarget3D(this, EntityMob.class, 180, false, true, NOT_CREEPER) {
            @Override
            public boolean shouldExecute() {
                return !isChild() && isTamed() && super.shouldExecute();
            }
        });
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            this.setSitting(false);
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 3.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityCrocodile) AMEntityRegistry.CROCODILE.newInstance(this.world);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (itemstack.getItem() == Items.NAME_TAG) {
            return super.processInteract(player, hand);
        }
        if (isTamed() && this.isBreedingItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.heal(10);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        boolean type = super.processInteract(player, hand);
        if (!type && isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            if (this.isSitting()) {
                this.forcedSit = false;
                this.setSitting(false);
                return true;
            } else {
                this.forcedSit = true;
                this.setSitting(true);
                return true;
            }
        }
        return type;
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (!this.isChild()) {
            super.setAttackTarget(entitylivingbaseIn);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.ROTTEN_FLESH;
    }

    @Override
    public boolean shouldEnterWater() {
        if (!this.getPassengers().isEmpty()) {
            return true;
        }
        return this.getAttackTarget() == null && !this.isSitting() && this.baskingTimer <= 0 && !shouldLeaveWater() && swimTimer <= -1000;
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
        return new Animation[]{ANIMATION_LUNGE, ANIMATION_DEATHROLL};
    }
}
