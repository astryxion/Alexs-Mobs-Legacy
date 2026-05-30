package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.Random;

public class EntityWarpedMosco extends EntityMob implements IAnimatedEntity {

    public static final Animation ANIMATION_PUNCH_R = Animation.create(25);
    public static final Animation ANIMATION_PUNCH_L = Animation.create(25);
    public static final Animation ANIMATION_SLAM = Animation.create(35);
    public static final Animation ANIMATION_SUCK = Animation.create(60);
    public static final Animation ANIMATION_SPIT = Animation.create(60);
    private static final Animation[] ANIMATIONS = {
            ANIMATION_PUNCH_L, ANIMATION_PUNCH_R, ANIMATION_SLAM, ANIMATION_SUCK, ANIMATION_SPIT
    };
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityWarpedMosco.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAND_SIDE = EntityDataManager.createKey(EntityWarpedMosco.class, DataSerializers.BOOLEAN);
    public float flyLeftProgress;
    public float prevLeftFlyProgress;
    public float flyRightProgress;
    public float prevFlyRightProgress;
    private int animationTick;
    private Animation currentAnimation;
    private boolean isLandNavigator;
    int timeFlying;
    private int loopSoundTick = 0;

    public EntityWarpedMosco(World world) {
        super(world);
        this.experienceValue = 30;
        this.setSize(1.2F, 1.8F);
        this.switchNavigator(false);
    }

    private static Animation getRandomAttack(Random rand) {
        switch (rand.nextInt(4)) {
            case 0:
                return ANIMATION_PUNCH_L;
            case 1:
                return ANIMATION_PUNCH_R;
            case 2:
                return ANIMATION_SLAM;
            case 3:
                return ANIMATION_SUCK;
        }
        return ANIMATION_SUCK;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.WARPED_MOSCO_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.WARPED_MOSCO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.WARPED_MOSCO_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.WARPED_MOSCO;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(0, new AttackGoal());
        this.tasks.addTask(4, new AIWalkIdle());
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityCrimsonMosquito.class, EntityWarpedMosco.class));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityLivingBase.class, 50, false, true,
                AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.CRIMSON_MOSQUITO_TARGETS)));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.7F, false);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(HAND_SIDE, Boolean.TRUE);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        setDashRight(flying != this.isFlying() ? rand.nextBoolean() : this.isDashRight());
        this.dataManager.set(FLYING, flying);
    }

    public boolean isDashRight() {
        return this.dataManager.get(HAND_SIDE);
    }

    public void setDashRight(boolean right) {
        this.dataManager.set(HAND_SIDE, right);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevFlyRightProgress = flyRightProgress;
        prevLeftFlyProgress = flyLeftProgress;
        if (this.isFlying() && isDashRight() && flyRightProgress < 5F) {
            flyRightProgress++;
        }
        if ((!this.isFlying() || !isDashRight()) && flyRightProgress > 0F) {
            flyRightProgress--;
        }
        if (this.isFlying() && !isDashRight() && flyLeftProgress < 5F) {
            flyLeftProgress++;
        }
        if ((!this.isFlying() || isDashRight()) && flyLeftProgress > 0F) {
            flyLeftProgress--;
        }
        if (!world.isRemote) {
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
        }
        if (isFlying()) {
            if (loopSoundTick == 0) {
                this.playSound(AMSoundRegistry.MOSQUITO_LOOP, this.getSoundVolume(), this.getSoundPitch() * 0.3F);
            }
            loopSoundTick++;
            if (loopSoundTick > 100) {
                loopSoundTick = 0;
            }
        }
        if (isFlying()) {
            timeFlying++;
            this.setNoGravity(true);
            if (this.isRiding() || this.isBeingRidden()) {
                this.setFlying(false);
            }
        } else {
            timeFlying = 0;
            this.setNoGravity(false);
        }
        if (this.collidedHorizontally && ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
            boolean flag = false;
            AxisAlignedBB axisalignedbb = this.getEntityBoundingBox().grow(0.2D);
            for (BlockPos blockpos : BlockPos.getAllInBox(
                    MathHelper.floor(axisalignedbb.minX), MathHelper.floor(axisalignedbb.minY), MathHelper.floor(axisalignedbb.minZ),
                    MathHelper.floor(axisalignedbb.maxX), MathHelper.floor(axisalignedbb.maxY), MathHelper.floor(axisalignedbb.maxZ))) {
                Block block = this.world.getBlockState(blockpos).getBlock();
                if (AMTagRegistry.blockInTag(AMTagRegistry.WARPED_MOSCO_BREAKABLES, block)) {
                    flag = this.world.destroyBlock(blockpos, true) || flag;
                }
            }
            if (!flag && this.onGround) {
                this.jump();
            }
        }

        EntityLivingBase target = this.getAttackTarget();
        if (!this.world.isRemote && target != null && this.isEntityAlive()) {
            if (this.getAnimation() == ANIMATION_SUCK && this.getAnimationTick() == 3 && this.getDistance(target) < 4.7F) {
                target.startRiding(this, true);
            }
            if (this.getAnimation() == ANIMATION_SLAM) {
                if (this.getAnimationTick() == 19) {
                    for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(5.0D), null)) {
                        if (!this.isOnSameTeam(entity) && !(entity instanceof EntityWarpedMosco) && entity != this) {
                            entity.attackEntityFrom(DamageSource.causeMobDamage(this), 10.0F + rand.nextFloat() * 8.0F);
                            launch(entity, true);
                        }
                    }
                }
            }
            if ((this.getAnimation() == ANIMATION_PUNCH_R || this.getAnimation() == ANIMATION_PUNCH_L) && this.getAnimationTick() == 13) {
                if (this.getDistance(target) < 4.7F) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    knockbackRidiculous(target, 0.9F);
                }
            }
        }
        if (this.getAnimation() == ANIMATION_SLAM && this.getAnimationTick() == 19) {
            spawnGroundEffects();
        }

        AMEntityRegistry.updateAnimations(this);
    }

    public void spawnGroundEffects() {
        float radius = 2.3F;
        for (int i = 0; i < 4; i++) {
            for (int i1 = 0; i1 < 20 + rand.nextInt(12); i1++) {
                double motionX = getRNG().nextGaussian() * 0.07D;
                double motionY = getRNG().nextGaussian() * 0.07D;
                double motionZ = getRNG().nextGaussian() * 0.07D;
                float angle = (0.01745329251F * this.renderYawOffset) + i1;
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraY = 0.8F;
                double extraZ = radius * MathHelper.cos(angle);
                BlockPos ground = getMoscoGround(new BlockPos(MathHelper.floor(this.posX + extraX), MathHelper.floor(this.posY + extraY) - 1, MathHelper.floor(this.posZ + extraZ)));
                IBlockState blockState = this.world.getBlockState(ground);
                if (blockState.getMaterial() != Material.AIR) {
                    if (world.isRemote) {
                        world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + extraX, ground.getY() + extraY, this.posZ + extraZ, motionX, motionY, motionZ, Block.getStateId(blockState));
                    }
                }
            }
        }
    }

    private void launch(Entity e, boolean huge) {
        if (e.onGround) {
            double d0 = e.posX - this.posX;
            double d1 = e.posZ - this.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            float f = huge ? 2F : 0.5F;
            e.motionX += d0 / d2 * f;
            e.motionY += huge ? 0.5D : 0.2F;
            e.motionZ += d1 / d2 * f;
        }
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        if (this.currentAnimation != animation) {
            this.currentAnimation = animation;
            this.animationTick = 0;
        }
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
    public Animation[] getAnimations() {
        return ANIMATIONS;
    }

    private BlockPos getMoscoGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() > 2 && world.isAirBlock(position)) {
            position = position.down();
        }
        return position;
    }

    @Nullable
    public Vec3d getBlockGrounding(Vec3d fleePos) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24);
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, (int) this.posY, fleePos.z + extraZ);
        BlockPos ground = this.getMoscoGround(radialPos);
        if (ground.getY() == 0) {
            return new Vec3d(this.posX, this.posY, this.posZ);
        } else {
            ground = this.getPosition();
            while (ground.getY() > 2 && world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        Vec3d vec = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(vec)) {
            return vec;
        }
        return null;
    }

    @Nullable
    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24) - radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getMoscoGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 4 + this.getRNG().nextInt(10);
        BlockPos newPos = ground.up(distFromGround > 8 ? flightHeight : this.getRNG().nextInt(6) + 1);
        Vec3d vec = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(vec) && this.getDistanceSq(vec.x, vec.y, vec.z) > 1) {
            return vec;
        }
        return null;
    }

    public void knockbackRidiculous(EntityLivingBase target, float power) {
        target.knockBack(this, power, this.posX - target.posX, this.posZ - target.posZ);
        float knockbackResist = (float) MathHelper.clamp((1.0D - target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()), 0, 1);
        target.motionY += knockbackResist * power * 0.45F;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private boolean isOverLiquid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 2 && world.isAirBlock(position)) {
            position = position.down();
        }
        return this.world.getBlockState(position).getMaterial().isLiquid();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if ((this.getAnimation() == ANIMATION_SUCK || this.getAnimation() == ANIMATION_SLAM) && this.getAnimationTick() > 8) {
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
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if (this.getPassengers().contains(passenger)) {
            int tick = 5;
            if (this.getAnimation() == ANIMATION_SUCK) {
                tick = this.getAnimationTick();
            } else {
                passenger.dismountRidingEntity();
            }
            float radius = 2F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            double extraY = tick < 10 ? 0 : 0.15F * MathHelper.clamp(tick - 10, 0, 15);
            passenger.setPosition(this.posX + extraX, this.posY + extraY + 0.1F, this.posZ + extraZ);
            if ((tick - 10) % 4 == 0) {
                this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 100, 1));
                passenger.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
        }
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
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.warpedMoscoSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    private void spit(EntityLivingBase target) {
        if (this.getAnimation() != ANIMATION_SPIT) {
            return;
        }
        this.getLookHelper().setLookPositionWithEntity(target, 100.0F, 100.0F);
        this.renderYawOffset = rotationYawHead;
        for (int i = 0; i < 2 + rand.nextInt(2); i++) {
            EntityHemolymph hemolymph = new EntityHemolymph(this.world, this);
            double d0 = target.posX - this.posX;
            double d1 = target.posY + target.height * 0.3333333333333333D - hemolymph.posY;
            double d2 = target.posZ - this.posZ;
            float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
            hemolymph.shoot(d0, d1 + (double) f, d2, 1.5F, 5.0F);
            if (!this.isSilent()) {
                this.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            }
            this.world.spawnEntity(hemolymph);
        }
    }

    private class AIWalkIdle extends EntityAIBase {
        protected final EntityWarpedMosco mosco;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;

        AIWalkIdle() {
            this.setMutexBits(1);
            this.mosco = EntityWarpedMosco.this;
        }

        @Override
        public boolean shouldExecute() {
            if (this.mosco.isBeingRidden() || (mosco.getAttackTarget() != null && mosco.getAttackTarget().isEntityAlive()) || this.mosco.isRiding()) {
                return false;
            } else {
                if (this.mosco.getRNG().nextInt(30) != 0 && !mosco.isFlying()) {
                    return false;
                }
                if (this.mosco.onGround) {
                    this.flightTarget = rand.nextInt(8) == 0;
                } else {
                    this.flightTarget = rand.nextInt(5) > 0 && mosco.timeFlying < 200;
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
                mosco.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.mosco.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
            if (!flightTarget && isFlying() && mosco.onGround) {
                mosco.setFlying(false);
            }
            if (isFlying() && mosco.onGround && mosco.timeFlying > 10) {
                mosco.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = new Vec3d(mosco.posX, mosco.posY, mosco.posZ);

            if (mosco.isOverLiquid()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (mosco.timeFlying < 50 || mosco.isOverLiquid()) {
                    return mosco.getBlockInViewAway(vector3d, 0);
                } else {
                    return mosco.getBlockGrounding(vector3d);
                }
            } else {
                return RandomPositionGenerator.findRandomTarget(this.mosco, 20, 7);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (flightTarget) {
                return mosco.isFlying() && mosco.getDistanceSq(x, y, z) > 20F && !mosco.collidedHorizontally;
            } else {
                return (!this.mosco.getNavigator().noPath()) && !this.mosco.isBeingRidden();
            }
        }

        @Override
        public void startExecuting() {
            if (flightTarget) {
                mosco.setFlying(true);
                mosco.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.mosco.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        @Override
        public void resetTask() {
            this.mosco.getNavigator().clearPath();
        }
    }

    private class AttackGoal extends EntityAIBase {
        private int upTicks = 0;
        private int dashCooldown = 0;
        private boolean ranged = false;
        private BlockPos farTarget = null;

        AttackGoal() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return EntityWarpedMosco.this.getAttackTarget() != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return shouldExecute();
        }

        @Override
        public void updateTask() {
            if (dashCooldown > 0) {
                dashCooldown--;
            }
            if (EntityWarpedMosco.this.getAttackTarget() != null) {
                EntityLivingBase target = EntityWarpedMosco.this.getAttackTarget();
                ranged = EntityWarpedMosco.this.shouldRangeAttack(target);
                Vec3d targetVec = new Vec3d(target.posX, target.posY + target.height * 0.6F, target.posZ);
                float dist = EntityWarpedMosco.this.getDistance(target);
                boolean useFlyMode = ranged || (dist > 12F && !EntityWarpedMosco.this.isTargetBlocked(targetVec));
                if (!ranged && dist < 4.5F && EntityWarpedMosco.this.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    useFlyMode = false;
                    EntityWarpedMosco.this.setFlying(false);
                } else if (useFlyMode || EntityWarpedMosco.this.isFlying()) {
                    useFlyMode = true;
                }
                if (useFlyMode) {
                    float speedRush = 5F;
                    upTicks++;
                    EntityWarpedMosco.this.setFlying(true);
                    if (ranged) {
                        if (farTarget == null || EntityWarpedMosco.this.getDistanceSq(farTarget.getX() + 0.5D, farTarget.getY() + 0.5D, farTarget.getZ() + 0.5D) < 9) {
                            farTarget = this.getAvoidTarget(target);
                        }
                        if (farTarget != null) {
                            EntityWarpedMosco.this.getMoveHelper().setMoveTo(farTarget.getX(), farTarget.getY() + target.getEyeHeight() * 0.6F, farTarget.getZ(), 3D);
                        }
                        if (EntityWarpedMosco.this.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                            EntityWarpedMosco.this.setAnimation(ANIMATION_SPIT);
                        }
                        if (upTicks % 30 == 0) {
                            EntityWarpedMosco.this.heal(1.0F);
                        }
                        int tick = EntityWarpedMosco.this.getAnimationTick();
                        if (tick == 10 || tick == 20 || tick == 30 || tick == 40) {
                            EntityWarpedMosco.this.spit(target);
                        }
                    } else {
                        if (upTicks > 20 || EntityWarpedMosco.this.getDistance(target) < 6) {
                            EntityWarpedMosco.this.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight() * 0.6F, target.posZ, speedRush);
                        } else {
                            EntityWarpedMosco.this.getMoveHelper().setMoveTo(EntityWarpedMosco.this.posX, EntityWarpedMosco.this.posY + 3, EntityWarpedMosco.this.posZ, 0.5F);
                        }
                    }
                } else {
                    EntityWarpedMosco.this.getNavigator().tryMoveToEntityLiving(EntityWarpedMosco.this.getAttackTarget(), 1.25D);
                }
                if (EntityWarpedMosco.this.isFlying()) {
                    if (EntityWarpedMosco.this.getDistance(target) < 4.3F) {
                        if (dashCooldown == 0 || target.onGround || target.isInLava() || target.isInWater()) {
                            target.attackEntityFrom(DamageSource.causeMobDamage(EntityWarpedMosco.this), 5F);
                            EntityWarpedMosco.this.knockbackRidiculous(target, 1.0F);
                            dashCooldown = 30;
                        }
                        float groundHeight = EntityWarpedMosco.this.getMoscoGround(EntityWarpedMosco.this.getPosition()).getY();
                        if (Math.abs(EntityWarpedMosco.this.posY - groundHeight) < 3.0F && !EntityWarpedMosco.this.isOverLiquid()) {
                            EntityWarpedMosco.this.timeFlying += 300;
                            EntityWarpedMosco.this.setFlying(false);
                        }
                    }
                } else {
                    if (EntityWarpedMosco.this.getDistance(target) < 4F && EntityWarpedMosco.this.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                        Animation animation = getRandomAttack(rand);
                        if (animation == ANIMATION_SUCK && target.isRiding()) {
                            animation = ANIMATION_SLAM;
                        }
                        EntityWarpedMosco.this.setAnimation(animation);
                    }
                }
            }
        }

        public BlockPos getAvoidTarget(EntityLivingBase target) {
            float radius = 10 + EntityWarpedMosco.this.getRNG().nextInt(8);
            float angle = (0.01745329251F * (target.rotationYawHead + 90F + EntityWarpedMosco.this.getRNG().nextInt(180)));
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(target.posX + extraX, target.posY + 1, target.posZ + extraZ);
            BlockPos ground = radialPos;
            if (EntityWarpedMosco.this.getDistanceSq(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D) > 30) {
                Vec3d groundVec = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
                if (!EntityWarpedMosco.this.isTargetBlocked(groundVec) && EntityWarpedMosco.this.getDistanceSq(groundVec.x, groundVec.y, groundVec.z) > 6) {
                    return ground;
                }
            }
            return EntityWarpedMosco.this.getPosition();
        }

        @Override
        public void resetTask() {
            upTicks = 0;
            dashCooldown = 0;
            ranged = false;
            farTarget = null;
        }
    }

    private boolean shouldRangeAttack(EntityLivingBase target) {
        if (this.getHealth() < Math.floor(this.getMaxHealth() * 0.25F)) {
            return true;
        }
        return this.getHealth() < this.getMaxHealth() * 0.5F && this.getDistance(target) > 10;
    }
}
