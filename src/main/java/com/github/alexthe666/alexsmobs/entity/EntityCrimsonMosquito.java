package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoDismount;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoMountPlayer;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntityCrimsonMosquito extends EntityMob {

    public static final ResourceLocation FULL_LOOT = new ResourceLocation("alexsmobs", "entities/crimson_mosquito_full");
    public static final ResourceLocation FROM_FLY_LOOT = new ResourceLocation("alexsmobs", "entities/crimson_mosquito_fly");
    public static final ResourceLocation FROM_FLY_FULL_LOOT = new ResourceLocation("alexsmobs", "entities/crimson_mosquito_fly_full");
    private static final float GROUND_WIDTH = 0.9F;
    private static final float GROUND_HEIGHT = 0.9F;
    private static final float FLIGHT_WIDTH = 1.2F;
    private static final float FLIGHT_HEIGHT = 1.8F;
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SHOOTING = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> BLOOD_LEVEL = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SHRINKING = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_FLY = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> MOSQUITO_SCALE = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> SICK = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LURING_LAVIATHAN = EntityDataManager.createKey(EntityCrimsonMosquito.class, DataSerializers.VARINT);
    public float prevFlyProgress;
    public float flyProgress;
    public float prevShootProgress;
    public float shootProgress;
    public int shootingTicks;
    public int randomWingFlapTick = 0;
    private int flightTicks = 0;
    private int sickTicks = 0;
    private boolean prevFlying = false;
    private int loopSoundTick = 0;
    int drinkTime = 0;
    public float prevMosquitoScale = 1F;

    public EntityCrimsonMosquito(World worldIn) {
        super(worldIn);
        this.moveHelper = new MoveHelperController(this);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, -1.0F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.LAVA, 0.0F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.DANGER_FIRE, 0.0F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.DAMAGE_FIRE, 0.0F);
        this.setSize(GROUND_WIDTH, GROUND_HEIGHT);
    }

    public void onSpawnFromFly() {
        prevMosquitoScale = 0.2F;
        this.setShrink(false);
        this.setMosquitoScale(0.2F);
        this.setFromFly(true);
        for (int j = 0; j < 4; ++j) {
            this.world.spawnParticle(EnumParticleTypes.SPELL_MOB,
                    this.posX + this.rand.nextDouble() / 2.0D, this.posY + (double) this.height * 0.5D, this.posZ + this.rand.nextDouble() / 2.0D,
                    this.rand.nextDouble() * 0.5F + 0.5F, 0, 0.0D);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MOSQUITO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MOSQUITO_DIE;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.crimsonMosquitoSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        if (this.getBloodLevel() > 0) {
            return this.isFromFly() ? FROM_FLY_FULL_LOOT : FULL_LOOT;
        }
        return this.isFromFly() ? FROM_FLY_LOOT : super.getLootTable();
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(2, new FlyTowardsTarget(this));
        this.tasks.addTask(2, new FlyAwayFromTarget(this));
        this.tasks.addTask(3, new RandomFlyGoal(this));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityCrimsonMosquito.class, EntityWarpedMosco.class));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityLivingBase.class, 50, false, true,
                AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.CRIMSON_MOSQUITO_TARGETS)));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("FlightTicks", this.flightTicks);
        compound.setInteger("SickTicks", this.sickTicks);
        compound.setFloat("MosquitoScale", this.getMosquitoScale());
        compound.setBoolean("Flying", this.isFlying());
        compound.setBoolean("Shrinking", this.isShrinking());
        compound.setBoolean("IsFromFly", this.isFromFly());
        compound.setBoolean("Sick", this.isSick());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.flightTicks = compound.getInteger("FlightTicks");
        this.sickTicks = compound.getInteger("SickTicks");
        this.setMosquitoScale(compound.getFloat("MosquitoScale"));
        this.setFlying(compound.getBoolean("Flying"));
        this.setShrink(compound.getBoolean("Shrinking"));
        this.setFromFly(compound.getBoolean("IsFromFly"));
        this.setSick(compound.getBoolean("Sick"));
    }

    private void spit(EntityLivingBase target) {
        if (this.isSick()) {
            return;
        }
        EntityMosquitoSpit spit = new EntityMosquitoSpit(this.world, this);
        double d0 = target.posX - this.posX;
        double d1 = target.posY + target.height * 0.3333333333333333D - spit.posY;
        double d2 = target.posZ - this.posZ;
        float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
        spit.shoot(d0, d1 + (double) f, d2, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0F, 1.0F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
        }
        if (this.getBloodLevel() > 0) {
            this.setBloodLevel(this.getBloodLevel() - 1);
        }
        this.world.spawnEntity(spit);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.FALL || source == DamageSource.DROWN || source == DamageSource.IN_WALL
                || source == DamageSource.FALLING_BLOCK || source == DamageSource.LAVA || source.isFireDamage()
                || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() != null && this.getLowestRidingEntity() == source.getTrueSource().getLowestRidingEntity()) {
            return super.attackEntityFrom(source, amount * 0.333F);
        }
        if (flightTicks < 0) {
            flightTicks = 0;
        }
        return super.attackEntityFrom(source, amount);
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
            if (this.isRiding()) {
                Entity mount = this.getRidingEntity();
                if (mount instanceof EntityLivingBase) {
                    EntityLivingBase livingMount = (EntityLivingBase) mount;
                    this.renderYawOffset = livingMount.renderYawOffset;
                    this.rotationYaw = livingMount.rotationYaw;
                    this.rotationYawHead = livingMount.rotationYawHead;
                    this.prevRotationYaw = livingMount.rotationYawHead;
                    float radius = 1F;
                    float angle = (0.01745329251F * livingMount.renderYawOffset);
                    double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                    double extraZ = radius * MathHelper.cos(angle);
                    this.setPosition(mount.posX + extraX, Math.max(mount.posY + livingMount.getEyeHeight() * 0.25F, mount.posY), mount.posZ + extraZ);
                    if (!mount.isEntityAlive() || mount instanceof EntityPlayer && ((EntityPlayer) mount).capabilities.isCreativeMode) {
                        this.dismountRidingEntity();
                    }
                    if (drinkTime % 20 == 0 && !world.isRemote && this.isEntityAlive()) {
                        boolean mungus = AMConfig.warpedMoscoTransformation && mount instanceof EntityMungus && ((EntityMungus) mount).isWarpedMoscoReady();
                        boolean sick = this.isNonMungusWarpedTrigger(mount);
                        if (mount.attackEntityFrom(DamageSource.causeMobDamage(this), mungus ? 7F : 2.0F)) {
                            if (mungus) {
                                ((EntityMungus) mount).disableExplosion();
                            }
                            if (sick || mungus) {
                                if (!this.isSick() && !world.isRemote) {
                                    for (EntityPlayerMP playerMp : this.world.getEntitiesWithinAABB(EntityPlayerMP.class, this.getEntityBoundingBox().grow(40.0D, 25.0D, 40.0D))) {
                                        AMAdvancementTriggerRegistry.MOSQUITO_SICK.trigger(playerMp);
                                    }
                                }
                                this.setSick(true);
                                this.setFlying(false);
                                flightTicks = -150 - rand.nextInt(200);
                            }
                            this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, this.getSoundVolume(), this.getSoundPitch());
                            this.setBloodLevel(this.getBloodLevel() + 1);
                            if (this.getBloodLevel() > 3) {
                                this.dismountRidingEntity();
                                AlexsMobs.sendMSGToAll(new MessageMosquitoDismount(this.getEntityId(), mount.getEntityId()));
                                this.setFlying(false);
                                this.flightTicks = -15;
                            }
                        }
                    }
                    if (drinkTime > 81 && !world.isRemote) {
                        drinkTime = -20 - rand.nextInt(20);
                        this.dismountRidingEntity();
                        AlexsMobs.sendMSGToAll(new MessageMosquitoDismount(this.getEntityId(), mount.getEntityId()));
                        this.setFlying(false);
                        this.flightTicks = -15;
                    }
                }
            }
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(SHOOTING, Boolean.FALSE);
        this.dataManager.register(SICK, Boolean.FALSE);
        this.dataManager.register(BLOOD_LEVEL, 0);
        this.dataManager.register(SHRINKING, Boolean.FALSE);
        this.dataManager.register(FROM_FLY, Boolean.FALSE);
        this.dataManager.register(MOSQUITO_SCALE, 1F);
        this.dataManager.register(LURING_LAVIATHAN, -1);
    }

    public boolean hasLuringLaviathan() {
        return this.dataManager.get(LURING_LAVIATHAN) != -1;
    }

    public int getLuringLaviathan() {
        return this.dataManager.get(LURING_LAVIATHAN);
    }

    public void setLuringLaviathan(int lure) {
        this.dataManager.set(LURING_LAVIATHAN, lure);
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        this.dataManager.set(FLYING, flying);
    }

    public void setupShooting() {
        this.dataManager.set(SHOOTING, true);
        this.shootingTicks = 5;
    }

    public int getBloodLevel() {
        return Math.min(this.dataManager.get(BLOOD_LEVEL), 4);
    }

    public void setBloodLevel(int bloodLevel) {
        this.dataManager.set(BLOOD_LEVEL, bloodLevel);
    }

    public boolean isShrinking() {
        return this.dataManager.get(SHRINKING);
    }

    public boolean isFromFly() {
        return this.dataManager.get(FROM_FLY);
    }

    public void setShrink(boolean shrink) {
        this.dataManager.set(SHRINKING, shrink);
    }

    public void setFromFly(boolean fromFly) {
        this.dataManager.set(FROM_FLY, fromFly);
    }

    public float getMosquitoScale() {
        return this.dataManager.get(MOSQUITO_SCALE);
    }

    public void setMosquitoScale(float scale) {
        this.dataManager.set(MOSQUITO_SCALE, scale);
    }

    public boolean isSick() {
        return this.dataManager.get(SICK);
    }

    public void setSick(boolean sick) {
        this.dataManager.set(SICK, sick);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        boolean shooting = this.dataManager.get(SHOOTING);
        if (prevFlying != this.isFlying()) {
            if (this.isFlying()) {
                this.setSize(FLIGHT_WIDTH, FLIGHT_HEIGHT);
            } else {
                this.setSize(GROUND_WIDTH, GROUND_HEIGHT);
            }
        }
        if (shooting && shootProgress < 5) {
            shootProgress += 1;
        }
        if (!shooting && shootProgress > 0) {
            shootProgress -= 1;
        }
        if (this.isFlying() && flyProgress < 5) {
            flyProgress += 1;
        }
        if (!this.isFlying() && flyProgress > 0) {
            flyProgress -= 1;
        }
        if (!world.isRemote && this.isRiding()) {
            this.setFlying(false);
        }
        if (!world.isRemote) {
            this.setNoGravity(this.isFlying());
        }
        if (hasLuringLaviathan()) {
            this.setAttackTarget(null);
            this.setRevengeTarget(null);
            Entity entity = this.world.getEntityByID(this.getLuringLaviathan());
            if (entity instanceof EntityLaviathan && ((EntityLaviathan) entity).isChilling()) {
                Vec3d vec = ((EntityLaviathan) entity).getLureMosquitoPos();
                this.setFlying(true);
                this.getLookHelper().setLookPositionWithEntity(entity, 10.0F, 10.0F);
                this.getMoveHelper().setMoveTo(vec.x, vec.y, vec.z, 0.7D);
            } else {
                this.setLuringLaviathan(-1);
            }
        }
        if (this.flyProgress == 0 && rand.nextInt(200) == 0) {
            randomWingFlapTick = 5 + rand.nextInt(15);
        }
        if (randomWingFlapTick > 0) {
            randomWingFlapTick--;
        }
        if (!world.isRemote && this.onGround && !this.isFlying() && (flightTicks >= 0 && rand.nextInt(5) == 0 || this.getAttackTarget() != null)) {
            this.setFlying(true);
            this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.motionY += 0.5D;
            this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
            this.onGround = false;
            this.isAirBorne = true;
        }
        if (flightTicks < 0) {
            flightTicks++;
        }
        if (isFlying() && !world.isRemote) {
            flightTicks++;
            if (flightTicks > 200 && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive())) {
                BlockPos above = this.getGroundPosition(this.getPosition().up());
                if (!this.world.isAirBlock(above)) {
                    this.motionY += -0.2D;
                    if (this.onGround) {
                        this.setFlying(false);
                        flightTicks = -150 - rand.nextInt(200);
                    }
                }
            }
        }
        prevMosquitoScale = this.getMosquitoScale();
        if (isShrinking()) {
            if (this.getMosquitoScale() > 0.4F) {
                this.setMosquitoScale(this.getMosquitoScale() - 0.1F);
            }
        } else {
            if (this.getMosquitoScale() < 1F && !this.isSick()) {
                this.setMosquitoScale(this.getMosquitoScale() + 0.05F);
            }
        }
        if (!world.isRemote && shootingTicks > 0) {
            shootingTicks--;
            if (shootingTicks == 0) {
                if (this.getAttackTarget() != null && this.getBloodLevel() > 0) {
                    this.spit(this.getAttackTarget());
                }
                this.dataManager.set(SHOOTING, false);
            }
        }
        if (isFlying()) {
            if (loopSoundTick == 0) {
                this.playSound(AMSoundRegistry.MOSQUITO_LOOP, this.getSoundVolume(), this.getSoundPitch());
            }
            loopSoundTick++;
            if (loopSoundTick > 100) {
                loopSoundTick = 0;
            }
        }
        if (isRiding() || drinkTime < 0) {
            if (isRiding() && drinkTime < 0) {
                drinkTime = 0;
            }
            drinkTime++;
        }
        prevFlyProgress = flyProgress;
        prevShootProgress = shootProgress;
        prevFlying = this.isFlying();
        if (this.isSick()) {
            sickTicks++;
            if (this.getAttackTarget() != null && !this.isRiding()) {
                this.setAttackTarget(null);
            }
            if (sickTicks > 100) {
                this.setShrink(false);
                this.setMosquitoScale(this.getMosquitoScale() + 0.015F);
                if (sickTicks > 160) {
                    EntityWarpedMosco mosco = (EntityWarpedMosco) AMEntityRegistry.WARPED_MOSCO.newInstance(world);
                    mosco.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                    if (!world.isRemote) {
                        mosco.onInitialSpawn(this.world.getDifficultyForLocation(this.getPosition()), (net.minecraft.entity.IEntityLivingData) null);
                    }
                    if (!world.isRemote) {
                        this.world.setEntityState(this, (byte) 79);
                        world.spawnEntity(mosco);
                    }
                    this.setDead();
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 79) {
            for (int i = 0; i < 27; ++i) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,
                        this.posX + (this.rand.nextDouble() - 0.5D) * 1.6D, this.posY + rand.nextFloat() * 3.4F, this.posZ + (this.rand.nextDouble() - 0.5D) * 1.6D,
                        d0, d1, d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.onGround && !this.isFlying()) {
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
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == AMItemRegistry.WARPED_MIXTURE && !this.isSick()) {
            if (item.hasContainerItem()) {
                ItemStack container = new ItemStack(item.getContainerItem());
                this.entityDropItem(container, 0.0F);
            }
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setSick(true);
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private BlockPos getGroundPosition(BlockPos radialPos) {
        while (radialPos.getY() > 1 && world.isAirBlock(radialPos)) {
            radialPos = radialPos.down();
        }
        return radialPos;
    }

    public boolean isNonMungusWarpedTrigger(Entity entity) {
        ResourceLocation name = AMTagRegistry.registrationNameForEntity(entity);
        return name != null && !AMConfig.warpedMoscoMobTriggers.isEmpty() && AMConfig.warpedMoscoMobTriggers.contains(name.toString());
    }

    static class RandomFlyGoal extends EntityAIBase {
        private final EntityCrimsonMosquito parentEntity;
        private BlockPos target = null;

        RandomFlyGoal(EntityCrimsonMosquito mosquito) {
            this.parentEntity = mosquito;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!parentEntity.isFlying() || parentEntity.getAttackTarget() != null) {
                return false;
            }
            if (!parentEntity.getMoveHelper().isUpdating() || target == null) {
                target = getBlockInViewMosquito();
                if (target != null) {
                    parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && parentEntity.isFlying() && parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > 2.4D
                    && parentEntity.getMoveHelper().isUpdating() && !parentEntity.collidedHorizontally;
        }

        @Override
        public void resetTask() {
            target = null;
        }

        @Override
        public void updateTask() {
            if (target == null) {
                target = getBlockInViewMosquito();
            }
            if (target != null) {
                parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                if (parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) < 2.5F) {
                    target = null;
                }
            }
        }

        @Nullable
        BlockPos getBlockInViewMosquito() {
            float radius = 1 + parentEntity.getRNG().nextInt(5);
            float neg = parentEntity.getRNG().nextBoolean() ? 1 : -1;
            float renderYawOffset = parentEntity.renderYawOffset;
            float angle = (0.01745329251F * renderYawOffset) + 3.15F + (parentEntity.getRNG().nextFloat() * neg);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(parentEntity.posX + extraX, parentEntity.posY + 2, parentEntity.posZ + extraZ);
            BlockPos ground = parentEntity.getGroundPosition(radialPos);
            int up = parentEntity.isSick() ? 2 : 6;
            BlockPos newPos = ground.up(1 + parentEntity.getRNG().nextInt(up));
            Vec3d targetVec = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
            if (!parentEntity.isTargetBlocked(targetVec) && parentEntity.getDistanceSq(targetVec.x, targetVec.y, targetVec.z) > 6) {
                return newPos;
            }
            return null;
        }
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityCrimsonMosquito parentEntity;

        MoveHelperController(EntityCrimsonMosquito mosquito) {
            super(mosquito);
            this.parentEntity = mosquito;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.speed >= 1.0D && parentEntity.isSick()) {
                this.speed = 0.35D;
            }
            if (parentEntity.isFlying()) {
                if (this.action == Action.MOVE_TO) {
                    double d0 = this.posX - parentEntity.posX;
                    double d1 = this.posY - parentEntity.posY;
                    double d2 = this.posZ - parentEntity.posZ;
                    double len = d0 * d0 + d1 * d1 + d2 * d2;
                    if (len < parentEntity.getEntityBoundingBox().getAverageEdgeLength()) {
                        this.action = Action.WAIT;
                        parentEntity.motionX *= 0.5D;
                        parentEntity.motionY *= 0.5D;
                        parentEntity.motionZ *= 0.5D;
                    } else {
                        double scale = this.speed * 0.05D / Math.sqrt(len);
                        parentEntity.motionX += d0 * scale;
                        parentEntity.motionY += d1 * scale;
                        parentEntity.motionZ += d2 * scale;
                        if (parentEntity.getAttackTarget() == null) {
                            parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                            parentEntity.renderYawOffset = parentEntity.rotationYaw;
                        } else {
                            double tx = parentEntity.getAttackTarget().posX - parentEntity.posX;
                            double tz = parentEntity.getAttackTarget().posZ - parentEntity.posZ;
                            parentEntity.rotationYaw = -((float) MathHelper.atan2(tx, tz)) * (180F / (float) Math.PI);
                            parentEntity.renderYawOffset = parentEntity.rotationYaw;
                        }
                    }
                }
            } else {
                this.action = Action.WAIT;
                parentEntity.setAIMoveSpeed(0.0F);
            }
        }
    }

    public class FlyTowardsTarget extends EntityAIBase {
        private final EntityCrimsonMosquito parentEntity;

        public FlyTowardsTarget(EntityCrimsonMosquito mosquito) {
            this.parentEntity = mosquito;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!parentEntity.isFlying() || parentEntity.getBloodLevel() > 0 || parentEntity.drinkTime < 0) {
                return false;
            }
            return !parentEntity.isRiding() && parentEntity.getAttackTarget() != null && !isBittenByMosquito(parentEntity.getAttackTarget());
        }

        @Override
        public boolean shouldContinueExecuting() {
            return parentEntity.drinkTime >= 0 && parentEntity.getAttackTarget() != null && !isBittenByMosquito(parentEntity.getAttackTarget())
                    && !parentEntity.collidedHorizontally && parentEntity.getBloodLevel() == 0 && parentEntity.isFlying() && parentEntity.getMoveHelper().isUpdating();
        }

        public boolean isBittenByMosquito(Entity entity) {
            for (Entity e : entity.getPassengers()) {
                if (e instanceof EntityCrimsonMosquito) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void resetTask() {
        }

        @Override
        public void updateTask() {
            if (parentEntity.getAttackTarget() != null) {
                EntityLivingBase target = parentEntity.getAttackTarget();
                parentEntity.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D);
                if (parentEntity.getEntityBoundingBox().grow(0.3F, 0.3F, 0.3F).intersects(target.getEntityBoundingBox()) && !isBittenByMosquito(target) && parentEntity.drinkTime == 0) {
                    parentEntity.startRiding(target, true);
                    if (!parentEntity.world.isRemote) {
                        AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(parentEntity.getEntityId(), target.getEntityId()));
                    }
                }
            }
        }
    }

    public class FlyAwayFromTarget extends EntityAIBase {
        private final EntityCrimsonMosquito parentEntity;
        private int spitCooldown = 0;
        private BlockPos shootPos = null;

        public FlyAwayFromTarget(EntityCrimsonMosquito mosquito) {
            this.parentEntity = mosquito;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!parentEntity.isFlying() || parentEntity.getBloodLevel() <= 0 && parentEntity.drinkTime >= 0) {
                return false;
            }
            if (!parentEntity.isRiding() && parentEntity.getAttackTarget() != null) {
                shootPos = getBlockInTargetsViewMosquito(parentEntity.getAttackTarget());
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return parentEntity.getAttackTarget() != null && (parentEntity.getBloodLevel() > 0 || parentEntity.drinkTime < 0) && parentEntity.isFlying() && !parentEntity.collidedHorizontally;
        }

        @Override
        public void resetTask() {
            spitCooldown = 20;
        }

        @Override
        public void updateTask() {
            if (spitCooldown > 0) {
                spitCooldown--;
            }
            if (parentEntity.getAttackTarget() != null) {
                if (shootPos == null) {
                    shootPos = getBlockInTargetsViewMosquito(parentEntity.getAttackTarget());
                } else {
                    parentEntity.getMoveHelper().setMoveTo(shootPos.getX() + 0.5D, shootPos.getY() + 0.5D, shootPos.getZ() + 0.5D, 1.0D);
                    parentEntity.getLookHelper().setLookPositionWithEntity(parentEntity.getAttackTarget(), 30.0F, 30.0F);
                    if (parentEntity.getDistanceSq(shootPos.getX() + 0.5D, shootPos.getY() + 0.5D, shootPos.getZ() + 0.5D) < 2.5F) {
                        if (spitCooldown == 0 && parentEntity.getBloodLevel() > 0) {
                            parentEntity.setupShooting();
                            spitCooldown = 20;
                        }
                        shootPos = null;
                    }
                }
            }
        }

        public BlockPos getBlockInTargetsViewMosquito(EntityLivingBase target) {
            float radius = 4 + parentEntity.getRNG().nextInt(5);
            float angle = (0.01745329251F * (target.rotationYawHead + 90F + parentEntity.getRNG().nextInt(180)));
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(target.posX + extraX, target.posY + 1, target.posZ + extraZ);
            BlockPos ground = radialPos;
            if (parentEntity.getDistanceSq(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D) > 30) {
                Vec3d groundVec = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
                if (!parentEntity.isTargetBlocked(groundVec) && parentEntity.getDistanceSq(groundVec.x, groundVec.y, groundVec.z) > 6) {
                    return ground;
                }
            }
            return parentEntity.getPosition();
        }
    }
}
