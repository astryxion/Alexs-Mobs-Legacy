package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoDismount;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoMountPlayer;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

public class EntityEnderiophage extends EntityCreature {

    private static final DataParameter<Float> PHAGE_PITCH = EntityDataManager.createKey(EntityEnderiophage.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityEnderiophage.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> MISSING_EYE = EntityDataManager.createKey(EntityEnderiophage.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> PHAGE_SCALE = EntityDataManager.createKey(EntityEnderiophage.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityEnderiophage.class, DataSerializers.VARINT);
    private static final Predicate<EntityLivingBase> ENDERGRADE_OR_INFECTED = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase entity) {
            return entity != null && (entity instanceof EntityEndergrade || entity.isPotionActive(AMEffectRegistry.ENDER_FLU));
        }
    };
    public float prevPhagePitch;
    public float tentacleAngle;
    public float lastTentacleAngle;
    public float phageRotation;
    public float prevFlyProgress;
    public float flyProgress;
    public int passengerIndex = 0;
    public float prevEnderiophageScale = 1F;
    private float rotationVelocity;
    private int slowDownTicks = 0;
    private float randomMotionSpeed;
    private boolean isLandNavigator;
    private int timeFlying = 0;
    private int fleeAfterStealTime = 0;
    private int attachTime = 0;
    private int dismountCooldown = 0;
    private int squishCooldown = 0;
    @Nullable
    private EntityEnderman angryEnderman = null;

    public EntityEnderiophage(World worldIn) {
        super(worldIn);
        this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
        switchNavigator(false);
        this.experienceValue = 5;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.enderiophageSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    private void doInitialPosing() {
        BlockPos down = this.getPhageGround(this.getPosition());
        this.setPosition(down.getX() + 0.5F, down.getY() + 1, down.getZ() + 0.5F);
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            doInitialPosing();
        }
        setSkinForDimension();
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public int getMaxSpawnedInChunk() {
        return 2;
    }

    public float getPhageScale() {
        return this.dataManager.get(PHAGE_SCALE);
    }

    public void setPhageScale(float scale) {
        this.dataManager.set(PHAGE_SCALE, scale);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT).intValue();
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, Integer.valueOf(variant));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new FlyTowardsTarget(this));
        this.tasks.addTask(2, new AIWalkIdle());
        this.targetTasks.addTask(1, new EntityAINearestTarget3D(this, EntityEnderman.class, 15, true, true, null) {
            @Override
            public boolean shouldExecute() {
                return EntityEnderiophage.this.isMissingEye() && super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                return EntityEnderiophage.this.isMissingEye() && super.shouldContinueExecuting();
            }
        });
        this.targetTasks.addTask(1, new EntityAINearestTarget3D(this, EntityLivingBase.class, 15, true, true, ENDERGRADE_OR_INFECTED) {
            @Override
            public boolean shouldExecute() {
                return !EntityEnderiophage.this.isMissingEye() && EntityEnderiophage.this.fleeAfterStealTime == 0 && super.shouldExecute();
            }

            @Override
            public boolean shouldContinueExecuting() {
                return !EntityEnderiophage.this.isMissingEye() && super.shouldContinueExecuting();
            }
        });
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, EntityEnderman.class));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 1F, false, true);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(PHAGE_PITCH, 0F);
        this.dataManager.register(PHAGE_SCALE, 1F);
        this.dataManager.register(FLYING, false);
        this.dataManager.register(MISSING_EYE, false);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    public boolean isInOverworld() {
        return this.world.provider.getDimension() == 0 && !this.isAIDisabled();
    }

    public boolean isInNether() {
        return this.world.provider.getDimension() == -1 && !this.isAIDisabled();
    }

    public void setStandardFleeTime() {
        this.fleeAfterStealTime = 20;
    }

    @Override
    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && !entity.isEntityAlive()) {
            this.dismountRidingEntity();
        } else {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onLivingUpdate();
            if (this.isRiding()) {
                attachTime++;
                Entity mount = this.getRidingEntity();
                if (mount instanceof EntityLivingBase) {
                    EntityLivingBase livingMount = (EntityLivingBase) mount;
                    passengerIndex = mount.getPassengers().indexOf(this);
                    this.renderYawOffset = livingMount.renderYawOffset;
                    this.rotationYaw = livingMount.rotationYaw;
                    this.rotationYawHead = livingMount.rotationYawHead;
                    this.prevRotationYaw = livingMount.rotationYawHead;
                    float radius = mount.width;
                    float angle = (0.01745329251F * (livingMount.renderYawOffset + passengerIndex * 90F));
                    double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                    double extraZ = radius * MathHelper.cos(angle);
                    this.setPosition(mount.posX + extraX, Math.max(mount.posY + livingMount.getEyeHeight() * 0.25F, mount.posY), mount.posZ + extraZ);
                    if (!mount.isEntityAlive() || mount instanceof EntityPlayer && ((EntityPlayer) mount).capabilities.isCreativeMode) {
                        this.dismountRidingEntity();
                    }
                    this.setPhagePitch(0F);
                    if (!world.isRemote && attachTime > 15) {
                        EntityLivingBase target = livingMount;
                        float dmg = 1F;
                        if (target.getHealth() > target.getMaxHealth() * 0.2F) {
                            dmg = 6F;
                        }
                        if ((target.getHealth() < 1.5D || mount.attackEntityFrom(DamageSource.causeMobDamage(this), dmg))) {
                            dismountCooldown = 100;
                            if (mount instanceof EntityEnderman) {
                                this.setMissingEye(false);
                                this.playSound(SoundEvents.ENTITY_ENDERMEN_DEATH, this.getSoundVolume(), this.getSoundPitch());
                                this.heal(5);
                                ((EntityEnderman) mount).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 400));
                                this.fleeAfterStealTime = 400;
                                this.setFlying(true);
                                this.angryEnderman = (EntityEnderman) mount;
                            } else {
                                if (rand.nextInt(3) == 0) {
                                    if (!this.isMissingEye()) {
                                        if (target.getActivePotionEffect(AMEffectRegistry.ENDER_FLU) == null) {
                                            target.addPotionEffect(new PotionEffect(AMEffectRegistry.ENDER_FLU, 12000));
                                        } else {
                                            PotionEffect inst = target.getActivePotionEffect(AMEffectRegistry.ENDER_FLU);
                                            int duration = 12000;
                                            int level = 0;
                                            if (inst != null) {
                                                duration = inst.getDuration();
                                                level = inst.getAmplifier();
                                            }
                                            target.removePotionEffect(AMEffectRegistry.ENDER_FLU);
                                            target.addPotionEffect(new PotionEffect(AMEffectRegistry.ENDER_FLU, duration, Math.min(level + 1, 4)));
                                        }
                                        this.heal(5);
                                        this.playSound(SoundEvents.ENTITY_ITEM_BREAK, this.getSoundVolume(), this.getSoundPitch());
                                        this.setMissingEye(true);
                                    }
                                    if (!world.isRemote) {
                                        this.setAttackTarget(null);
                                        this.setRevengeTarget(null);
                                        this.getNavigator().clearPath();
                                    }
                                }
                            }
                        }
                        if (livingMount.getHealth() <= 0 || this.fleeAfterStealTime > 0 || this.isMissingEye() && !(mount instanceof EntityEnderman) || !this.isMissingEye() && mount instanceof EntityEnderman) {
                            this.dismountRidingEntity();
                            this.setAttackTarget(null);
                            dismountCooldown = 100;
                            AlexsMobs.sendMSGToAll(new MessageMosquitoDismount(this.getEntityId(), mount.getEntityId()));
                            this.setFlying(true);
                        }
                    }
                }

            }
        }

    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    public void onSpawnFromEffect() {
        prevEnderiophageScale = 0.2F;
        this.setPhageScale(0.2F);
    }

    public void setSkinForDimension(){
        if(isInNether()){
            this.setVariant(2);
        }else if(isInOverworld()){
            this.setVariant(1);
        }else{
            this.setVariant(0);
        }
    }
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ENDERIOPHAGE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ENDERIOPHAGE_HURT;
    }

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block state) {
        this.playSound(AMSoundRegistry.ENDERIOPHAGE_WALK, 0.4F, 1.0F);
    }

    protected float determineNextStepDistance() {
        return this.distanceWalkedOnStepModified + 0.3F;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevEnderiophageScale = this.getPhageScale();
        float extraMotionSlow = 1.0F;
        float extraMotionSlowY = 1.0F;
        if (slowDownTicks > 0) {
            slowDownTicks--;
            extraMotionSlow = 0.33F;
            extraMotionSlowY = 0.1F;
        }
        if (dismountCooldown > 0) {
            dismountCooldown--;
        }
        if (squishCooldown > 0) {
            squishCooldown--;
        }
        if (!world.isRemote) {
            if (!this.isRiding() && attachTime != 0) {
                attachTime = 0;
            }
            if (fleeAfterStealTime > 0) {
                if (angryEnderman != null) {
                    Vec3d vec = this.getBlockInViewAway(angryEnderman.getPositionVector(), 10);
                    if (fleeAfterStealTime < 5) {
                        angryEnderman.setAttackTarget(null);
                        angryEnderman.setRevengeTarget(null);
                        angryEnderman = null;
                    }
                    if (vec != null) {
                        this.setFlying(true);
                        this.getMoveHelper().setMoveTo(vec.x, vec.y, vec.z, 1.3F);
                    }
                }
                fleeAfterStealTime--;
            }
        }
        this.renderYawOffset = this.rotationYaw;
        this.rotationYawHead = this.rotationYaw;
        this.setPhagePitch(-90F);
        if (this.isEntityAlive() && this.isFlying() && randomMotionSpeed > 0.75F && (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ) > 0.02D) {
            if (world.isRemote) {
                float pitch = -this.getPhagePitch() / 90F;
                float radius = this.width * 0.2F * -pitch;
                float angle = (0.01745329251F * this.rotationYaw);
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraY = 0.2F - (1 - pitch) * 0.15F;
                double extraZ = radius * MathHelper.cos(angle);
                double motX = extraX * 8 + rand.nextGaussian() * 0.05F;
                double motY = -0.1F;
                double motZ = extraZ + rand.nextGaussian() * 0.05F;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.DNA, this.posX + extraX, this.posY + extraY, this.posZ + extraZ, motX, motY, motZ);
            }
        }
        prevPhagePitch = this.getPhagePitch();
        prevFlyProgress = flyProgress;
        if (isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        this.lastTentacleAngle = this.tentacleAngle;
        this.phageRotation += this.rotationVelocity;
        if ((double) this.phageRotation > (Math.PI * 2D)) {
            if (this.world.isRemote) {
                this.phageRotation = ((float) Math.PI * 2F);
            } else {
                this.phageRotation = (float) ((double) this.phageRotation - (Math.PI * 2D));
                if (this.rand.nextInt(10) == 0) {
                    this.rotationVelocity = 1.0F / (this.rand.nextFloat() + 1.0F) * 0.2F;
                }
                this.world.setEntityState(this, (byte) 19);
            }
        }
        if (this.phageRotation < (float) Math.PI) {
            float f = this.phageRotation / (float) Math.PI;
            this.tentacleAngle = MathHelper.sin(f * f * (float) Math.PI) * 4.275F;
            if ((double) f > 0.75D) {
                if (squishCooldown == 0 && this.isFlying()) {
                    squishCooldown = 20;
                    this.playSound(AMSoundRegistry.ENDERIOPHAGE_SQUISH, 3F, this.getSoundPitch());
                }
                this.randomMotionSpeed = 1.0F;
            } else {
                randomMotionSpeed = 0.01F;
            }
        }
        if (!this.world.isRemote) {
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (this.isFlying()) {
                this.motionX *= this.randomMotionSpeed * extraMotionSlow;
                this.motionY *= this.randomMotionSpeed * extraMotionSlowY;
                this.motionZ *= this.randomMotionSpeed * extraMotionSlow;
                timeFlying++;
                if (this.onGround && timeFlying > 100) {
                    this.setFlying(false);
                }
            } else {
                timeFlying = 0;
            }
            if (this.isMissingEye() && this.getAttackTarget() != null) {
                if (!(this.getAttackTarget() instanceof EntityEnderman)) {
                    this.setAttackTarget(null);
                }
            }
        }
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
        if (this.isFlying()) {
            float phageDist = -(float) ((Math.abs(this.motionX) + Math.abs(this.motionZ)) * 6F);
            this.incrementPhagePitch(phageDist * 1);
            this.setPhagePitch(MathHelper.clamp(this.getPhagePitch(), -90, 10));
            float plateau = 2;
            if (this.getPhagePitch() > plateau) {
                this.decrementPhagePitch(phageDist * Math.abs(this.getPhagePitch()) / 90);
            }
            if (this.getPhagePitch() < -plateau) {
                this.incrementPhagePitch(phageDist * Math.abs(this.getPhagePitch()) / 90);
            }
            if (this.getPhagePitch() > 2F) {
                this.decrementPhagePitch(1);
            } else if (this.getPhagePitch() < -2) {
                this.incrementPhagePitch(1);
            }
            if (this.collidedHorizontally) {
                this.motionY += 0.2F;
            }
        } else {
            if (this.getPhagePitch() > 0F) {
                float decrease = Math.min(2, this.getPhagePitch());
                this.decrementPhagePitch(decrease);
            }
            if (this.getPhagePitch() < 0F) {
                float decrease = Math.min(2, -this.getPhagePitch());
                this.incrementPhagePitch(decrease);
            }
        }
        if (this.getPhageScale() < 1F) {
            this.setPhageScale(this.getPhageScale() + 0.05F);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setBoolean("MissingEye", this.isMissingEye());
        compound.setInteger("Variant", this.getVariant());
        compound.setInteger("SlowDownTicks", slowDownTicks);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.setMissingEye(compound.getBoolean("MissingEye"));
        this.setVariant(compound.getInteger("Variant"));
        this.slowDownTicks = compound.getInteger("SlowDownTicks");
    }

    public boolean isMissingEye() {
        return this.dataManager.get(MISSING_EYE);
    }

    public void setMissingEye(boolean missingEye) {
        this.dataManager.set(MISSING_EYE, missingEye);
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
    }

    public float getPhagePitch() {
        return dataManager.get(PHAGE_PITCH).floatValue();
    }

    public void setPhagePitch(float pitch) {
        dataManager.set(PHAGE_PITCH, pitch);
    }

    public void incrementPhagePitch(float pitch) {
        dataManager.set(PHAGE_PITCH, getPhagePitch() + pitch);
    }

    public void decrementPhagePitch(float pitch) {
        dataManager.set(PHAGE_PITCH, getPhagePitch() - pitch);
    }

    @Override
    public float getEyeHeight() {
        return 1.8F;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 19) {
            this.phageRotation = (float) ((double) this.phageRotation - (Math.PI * 2D));
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 1 && world.isAirBlock(position)) {
            position = position.down();
        }
        return world.getBlockState(position).getMaterial().isLiquid() || position.getY() < 1;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24) - radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getPhageGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 6 + this.getRNG().nextInt(10);
        BlockPos newPos = ground.up(distFromGround > 8 || fleeAfterStealTime > 0 ? flightHeight : this.getRNG().nextInt(6) + 5);
        Vec3d center = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(center) && this.getDistanceSq(center.x, center.y, center.z) > 1) {
            return center;
        }
        return null;
    }

    private BlockPos getPhageGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() > 1 && world.isAirBlock(position)) {
            position = position.down();
        }
        if (position.getY() < 2) {
            return position.up(60 + rand.nextInt(5));
        }

        return position;
    }

    public Vec3d getBlockGrounding(Vec3d fleePos) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, (int) posY, fleePos.z + extraZ);
        BlockPos ground = this.getPhageGround(radialPos);
        if (ground.getY() == 0) {
            return new Vec3d(ground.getX() + 0.5D, ground.getY() + 50 + rand.nextInt(20), ground.getZ() + 0.5D);
        } else {
            ground = this.getPosition();
            while (ground.getY() > 1 && world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        Vec3d center = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(center)) {
            return center;
        }
        return null;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity instanceof EntityEnderman) {
                amount = (amount + 1.0F) * 0.35F;
                angryEnderman = (EntityEnderman) entity;
            }
            return super.attackEntityFrom(source, amount);
        }
    }


    private class AIWalkIdle extends EntityAIBase {
        protected final EntityEnderiophage phage;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;

        public AIWalkIdle() {
            this.setMutexBits(1);
            this.phage = EntityEnderiophage.this;
        }

        @Override
        public boolean shouldExecute() {
            if (this.phage.isBeingRidden() || (phage.getAttackTarget() != null && phage.getAttackTarget().isEntityAlive()) || this.phage.isRiding()) {
                return false;
            } else {
                if (this.phage.getRNG().nextInt(30) != 0 && !phage.isFlying() && phage.fleeAfterStealTime == 0) {
                    return false;
                }
                if (this.phage.onGround) {
                    this.flightTarget = phage.getRNG().nextInt(12) == 0;
                } else {
                    this.flightTarget = phage.getRNG().nextInt(5) > 0 && phage.timeFlying < 100;
                }
                if (phage.fleeAfterStealTime > 0) {
                    this.flightTarget = true;
                }
                Vec3d lvt_1_1_ = this.getPosition();
                if (lvt_1_1_ == null) {
                    return false;
                } else {
                    this.x = lvt_1_1_.x;
                    this.y = lvt_1_1_.y;
                    this.z = lvt_1_1_.z;
                    return true;
                }
            }
        }

        @Override
        public void updateTask() {
            if (flightTarget) {
                phage.getMoveHelper().setMoveTo(x, y, z, fleeAfterStealTime == 0 ? 1.3F : 1F);
            } else {
                this.phage.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, fleeAfterStealTime == 0 ? 1.3F : 1F);
            }
            if (!flightTarget && isFlying() && phage.onGround) {
                phage.setFlying(false);
            }
            if (isFlying() && phage.onGround && phage.timeFlying > 100 && phage.fleeAfterStealTime == 0) {
                phage.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = phage.getPositionVector();
            if (phage.isOverWaterOrVoid()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (phage.timeFlying < 50 || fleeAfterStealTime > 0 || phage.isOverWaterOrVoid()) {
                    return phage.getBlockInViewAway(vector3d, 0);
                } else {
                    return phage.getBlockGrounding(vector3d);
                }
            } else {
                return RandomPositionGenerator.findRandomTarget(this.phage, 10, 7);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (flightTarget) {
                return phage.isFlying() && phage.getDistanceSq(x, y, z) > 2F;
            } else {
                return (!this.phage.getNavigator().noPath()) && !this.phage.isBeingRidden();
            }
        }

        @Override
        public void startExecuting() {
            if (flightTarget) {
                phage.setFlying(true);
                phage.getMoveHelper().setMoveTo(x, y, z, fleeAfterStealTime == 0 ? 1.3F : 1F);
            } else {
                this.phage.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        @Override
        public void resetTask() {
            this.phage.getNavigator().clearPath();
            super.resetTask();
        }
    }

    public class FlyTowardsTarget extends EntityAIBase {
        private final EntityEnderiophage parentEntity;

        public FlyTowardsTarget(EntityEnderiophage phage) {
            this.parentEntity = phage;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return !parentEntity.isRiding() && parentEntity.getAttackTarget() != null && !isBittenByPhage(parentEntity.getAttackTarget()) && parentEntity.fleeAfterStealTime == 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return parentEntity.getAttackTarget() != null && !isBittenByPhage(parentEntity.getAttackTarget()) && !parentEntity.collidedHorizontally && !parentEntity.isRiding() && parentEntity.isFlying() && parentEntity.getMoveHelper().isUpdating() && parentEntity.fleeAfterStealTime == 0 && (parentEntity.getAttackTarget() instanceof EntityEnderman || !parentEntity.isMissingEye());
        }

        public boolean isBittenByPhage(Entity entity) {
            int phageCount = 0;
            for (Entity e : entity.getPassengers()) {
                if (e instanceof EntityEnderiophage) {
                    phageCount++;
                }
            }
            return phageCount > 3;
        }

        @Override
        public void resetTask() {
        }

        @Override
        public void updateTask() {
            if (parentEntity.getAttackTarget() != null) {
                EntityLivingBase target = parentEntity.getAttackTarget();
                float width = target.width + parentEntity.width + 2;
                boolean isWithinReach = parentEntity.getDistanceSq(target) < width * width;
                if (parentEntity.isFlying() || isWithinReach) {
                    this.parentEntity.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, isWithinReach ? 1.6D : 1.0D);
                } else {
                    this.parentEntity.getNavigator().tryMoveToXYZ(target.posX, target.posY, target.posZ, 1.2D);
                }
                if (target.posY > this.parentEntity.posY + 1.2F) {
                    parentEntity.setFlying(true);
                }
                if (parentEntity.dismountCooldown == 0 && parentEntity.getEntityBoundingBox().grow(0.3D, 0.3D, 0.3D).intersects(target.getEntityBoundingBox()) && !isBittenByPhage(target)) {
                    parentEntity.startRiding(target, true);
                    if (!parentEntity.world.isRemote) {
                        AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(parentEntity.getEntityId(), target.getEntityId()));
                    }
                }
            }
        }
    }

}
