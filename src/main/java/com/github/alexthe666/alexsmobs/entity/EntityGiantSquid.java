package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

public class EntityGiantSquid extends EntityCreature {

    private static final DataParameter<Float> SQUID_PITCH = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DEPRESSURIZATION = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> OVERRIDE_BODYROT = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GRABBING = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> CAPTURED = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BLUE = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> GRAB_ENTITY = EntityDataManager.createKey(EntityGiantSquid.class, DataSerializers.VARINT);

    public final EntityGiantSquidPart mantlePart1;
    public final EntityGiantSquidPart mantlePart2;
    public final EntityGiantSquidPart mantlePart3;
    public final EntityGiantSquidPart tentaclesPart1;
    public final EntityGiantSquidPart tentaclesPart2;
    public final EntityGiantSquidPart tentaclesPart3;
    public final EntityGiantSquidPart tentaclesPart4;
    public final EntityGiantSquidPart tentaclesPart5;
    public final EntityGiantSquidPart tentaclesPart6;
    public final EntityGiantSquidPart mantleCollisionPart;
    public final EntityGiantSquidPart[] allParts;
    public final float[][] ringBuffer = new float[64][2];
    public int ringBufferIndex = -1;
    public float prevSquidPitch;
    public float prevDepressurization;
    public float grabProgress;
    public float prevGrabProgress;
    public float dryProgress;
    public float prevDryProgress;
    public float capturedProgress;
    public float prevCapturedProgress;
    public int humTick = 0;
    private int holdTime;
    private int resetCapturedStateIn;
    private boolean squidPartsSpawned;

    public EntityGiantSquid(World world) {
        super(world);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setSize(0.9F, 1.2F);
        this.mantlePart1 = new EntityGiantSquidPart(this, 0.9F, 0.9F);
        this.mantlePart2 = new EntityGiantSquidPart(this, 1.2F, 1.2F);
        this.mantlePart3 = new EntityGiantSquidPart(this, 0.45F, 0.45F);
        this.tentaclesPart1 = new EntityGiantSquidPart(this, 0.9F, 0.9F);
        this.tentaclesPart2 = new EntityGiantSquidPart(this, 1F, 1F);
        this.tentaclesPart3 = new EntityGiantSquidPart(this, 1.2F, 1.2F);
        this.tentaclesPart4 = new EntityGiantSquidPart(this, 1.2F, 1.2F);
        this.tentaclesPart5 = new EntityGiantSquidPart(this, 1.2F, 1.2F);
        this.tentaclesPart6 = new EntityGiantSquidPart(this, 1.2F, 1.2F);
        this.mantleCollisionPart = new EntityGiantSquidPart(this, 2.9F, 2.9F, true);
        this.allParts = new EntityGiantSquidPart[]{this.mantlePart1, this.mantlePart2, this.mantlePart3, this.mantleCollisionPart, this.tentaclesPart1, this.tentaclesPart2, this.tentaclesPart3, this.tentaclesPart4, this.tentaclesPart5, this.tentaclesPart6};
        this.moveHelper = new AquaticMoveController(this, 1.2F, 5.0F);
    }

    public static boolean canGiantSquidSpawn(World world, BlockPos pos) {
        return world.getBlockState(pos).getMaterial() == Material.WATER && world.getBlockState(pos.up()).getMaterial() == Material.WATER;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.giantSquidSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    private void doInitialPosing(World world) {
        BlockPos down = this.getPosition();
        while (world.getBlockState(down).getMaterial() == Material.WATER && down.getY() > 1) {
            down = down.down();
        }
        this.setPosition(down.getX() + 0.5D, down.getY() + 3 + this.rand.nextInt(3), down.getZ() + 0.5D);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GIANT_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GIANT_SQUID_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(38.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SQUID_PITCH, 0F);
        this.dataManager.register(OVERRIDE_BODYROT, false);
        this.dataManager.register(DEPRESSURIZATION, 0F);
        this.dataManager.register(GRABBING, false);
        this.dataManager.register(CAPTURED, false);
        this.dataManager.register(BLUE, false);
        this.dataManager.register(GRAB_ENTITY, -1);
    }

    @Nullable
    public Entity getGrabbedEntity() {
        if (!this.world.isRemote || this.dataManager.get(GRAB_ENTITY) == -1) {
            return this.getAttackTarget();
        } else {
            return this.world.getEntityByID(this.dataManager.get(GRAB_ENTITY));
        }
    }

    public boolean applyInteractionFromPart(EntityPlayer player, EnumHand hand) {
        return this.processInteract(player, hand);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        return super.processInteract(player, hand);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(1, new AIAvoidWhales());
        this.tasks.addTask(2, new AIMelee());
        this.tasks.addTask(3, new AIDeepwaterSwimming());
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, EntityCachalotWhale.class));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityGuardian.class, 20, true, true, null) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute();
            }
        });
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityLivingBase.class, 70, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.GIANT_SQUID_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return !EntityGiantSquid.this.isInWater() && !EntityGiantSquid.this.isCaptured() && super.shouldExecute();
            }
        });
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.isAIDisabled()) {
            if (this.ringBufferIndex < 0) {
                for (int i = 0; i < this.ringBuffer.length; ++i) {
                    this.ringBuffer[i][0] = 180 + this.rotationYaw;
                    this.ringBuffer[i][1] = this.getSquidPitch();
                }
            }
            this.ringBufferIndex++;
            if (this.ringBufferIndex == this.ringBuffer.length) {
                this.ringBufferIndex = 0;
            }
            this.ringBuffer[this.ringBufferIndex][0] = this.renderYawOffset;
            this.ringBuffer[this.ringBufferIndex][1] = this.getSquidPitch();
        }
    }

    @Override
    public void onUpdate() {
        if (!this.world.isRemote && !this.squidPartsSpawned) {
            for (EntityGiantSquidPart part : this.allParts) {
                if (!part.isEntityAlive()) {
                    part.setPosition(this.posX, this.posY, this.posZ);
                }
                this.world.spawnEntity(part);
            }
            this.squidPartsSpawned = true;
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
            this.fallDistance = 0;
        }
        this.setNoGravity(this.isInWater());
        super.onUpdate();
        if (this.ticksExisted % 100 == 0) {
            this.heal(2.0F);
        }
        float f = MathHelper.wrapDegrees(180 + this.rotationYaw);
        this.renderYawOffset = rotlerp(this.renderYawOffset, f, 180);
        prevSquidPitch = getSquidPitch();
        prevDepressurization = getDepressurization();
        prevDryProgress = dryProgress;
        prevGrabProgress = grabProgress;
        prevCapturedProgress = capturedProgress;
        if (!this.isInWater() && dryProgress < 5F) {
            dryProgress++;
        }
        if (this.isInWater() && dryProgress > 0F) {
            dryProgress--;
        }

        if (this.isGrabbing()) {
            if (grabProgress < 5F) {
                grabProgress += 0.25F;
            }
        } else {
            if (grabProgress > 0F) {
                grabProgress -= 0.25F;
            }
        }

        if (this.isCaptured()) {
            if (capturedProgress < 5F) {
                capturedProgress += 0.5F;
            }
        } else {
            if (capturedProgress > 0F) {
                capturedProgress -= 0.5F;
            }
        }

        if (this.isGrabbing()) {
            Entity target = getGrabbedEntity();
            if (!this.world.isRemote && target != null) {
                this.dataManager.set(GRAB_ENTITY, target.getEntityId());
                if (holdTime % 20 == 0 && holdTime > 30) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), 3 + this.rand.nextInt(5));
                }
            }
            if (target != null && target.isEntityAlive()) {
                this.setSquidPitch(0);
                float invert = 1F - grabProgress * 0.2F;
                Vec3d extraVec = new Vec3d(0, 0, 2F + invert * 7F).rotatePitch(-this.getSquidPitch() * ((float) Math.PI / 180F)).rotateYaw(-this.renderYawOffset * ((float) Math.PI / 180F));
                Vec3d minus = new Vec3d(this.posX + extraVec.x - target.posX, this.posY + extraVec.y - target.posY, this.posZ + extraVec.z - target.posZ);
                target.motionX = minus.x;
                target.motionY = minus.y;
                target.motionZ = minus.z;
            }
            holdTime++;
            if (holdTime > 1000) {
                holdTime = 0;
                this.setGrabbing(false);
            }
        } else {
            holdTime = 0;
        }

        if (!this.isAIDisabled()) {
            Vec3d[] avector3d = new Vec3d[this.allParts.length];
            for (int j = 0; j < this.allParts.length; ++j) {
                this.allParts[j].collideWithNearbyEntities();
                avector3d[j] = new Vec3d(this.allParts[j].posX, this.allParts[j].posY, this.allParts[j].posZ);
            }
            final float pitch = this.getSquidPitch() * ((float) Math.PI / 180F) * 0.8F;
            this.mantleCollisionPart.setPosition(this.posX, this.posY - ((this.mantleCollisionPart.height - this.getEyeHeight()) * 0.5F) * (1F - dryProgress * 0.2F), this.posZ);
            this.setPartPositionFromBuffer(this.mantlePart1, pitch, 0.9F, 0);
            this.setPartPositionFromBuffer(this.mantlePart2, pitch, 1.6F, 0);
            this.setPartPositionFromBuffer(this.mantlePart3, pitch, 2.45F, 0);
            this.setPartPositionFromBuffer(this.tentaclesPart1, pitch, -0.8F, 0);
            this.setPartPositionFromBuffer(this.tentaclesPart2, pitch, -1.5F, 0);
            this.setPartPositionFromBuffer(this.tentaclesPart3, pitch, -2.3F, 5);
            this.setPartPositionFromBuffer(this.tentaclesPart4, pitch, -3.4F, 10);
            this.setPartPositionFromBuffer(this.tentaclesPart5, pitch, -5.4F, 15);
            this.setPartPositionFromBuffer(this.tentaclesPart6, pitch, -7.4F, 20);
            if (this.isInWater()) {
                if (this.mantleCollisionPart.scale != 1F) {
                    this.mantleCollisionPart.scale = 1F;
                    this.mantleCollisionPart.recalculateSize();
                }
            } else {
                if (this.mantleCollisionPart.scale != 0.25F) {
                    this.mantleCollisionPart.scale = 0.25F;
                    this.mantleCollisionPart.recalculateSize();
                }
            }
            for (int l = 0; l < this.allParts.length; ++l) {
                this.allParts[l].prevPosX = avector3d[l].x;
                this.allParts[l].prevPosY = avector3d[l].y;
                this.allParts[l].prevPosZ = avector3d[l].z;
                this.allParts[l].lastTickPosX = avector3d[l].x;
                this.allParts[l].lastTickPosY = avector3d[l].y;
                this.allParts[l].lastTickPosZ = avector3d[l].z;
            }
            this.setNoGravity(this.isInWater());
        }
        if (!this.world.isRemote) {
            if (this.getSquidPitch() > 0F) {
                float decrease = Math.min(2F, this.getSquidPitch());
                this.decrementSquidPitch(decrease);
            }
            if (this.getSquidPitch() < 0F) {
                float decrease = Math.min(2F, -this.getSquidPitch());
                this.incrementSquidPitch(decrease);
            }
            if (this.isInWater()) {
                float dist = (float) this.motionY * 45;
                if (this.dataManager.get(OVERRIDE_BODYROT)) {
                    this.decrementSquidPitch(dist);
                } else {
                    this.incrementSquidPitch(dist);
                }
            }
            if (!this.onGround && this.getFluidHeight() < this.height) {
                this.motionY -= 0.1F;
            }
            float pressure = getDepressureLevel();
            if (this.getDepressurization() < pressure) {
                this.setDepressurization(this.getDepressurization() + 0.1F);
            }
            if (this.getDepressurization() > pressure) {
                this.setDepressurization(this.getDepressurization() - 0.1F);
            }
        }
        if (this.isHumming()) {
            if (humTick % 20 == 0) {
                this.playSound(AMSoundRegistry.GIANT_SQUID_GAMES, this.getSoundVolume(), 1);
                humTick = 0;
            }
            humTick++;
        }
        if (!this.world.isRemote) {
            if (resetCapturedStateIn > 0) {
                resetCapturedStateIn--;
            } else {
                this.setCaptured(false);
            }
        }
    }

    @Override
    public void setDead() {
        for (EntityGiantSquidPart part : this.allParts) {
            part.setDead();
        }
        super.setDead();
    }

    private float getFluidHeight() {
        if (!this.isInWater()) {
            return 0;
        }
        BlockPos surface = this.getPosition();
        while (this.world.getBlockState(surface).getMaterial() == Material.WATER) {
            surface = surface.up();
        }
        return MathHelper.clamp(surface.getY() - (float) this.posY, 0, this.height);
    }

    private boolean isHumming() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        Calendar calendar = Calendar.getInstance();
        boolean aprilFools = calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) == 1;
        return s != null && s.toLowerCase().contains("squid games!!") || aprilFools;
    }

    public float getRingBuffer(int bufferOffset, float partialTicks, boolean pitch) {
        int i = (this.ringBufferIndex - bufferOffset) & 63;
        int j = (this.ringBufferIndex - bufferOffset - 1) & 63;
        int k = pitch ? 1 : 0;
        float prevBuffer = this.ringBuffer[j][k];
        float buffer = this.ringBuffer[i][k];
        float end = prevBuffer + (buffer - prevBuffer) * partialTicks;
        return rotlerp(prevBuffer, end, 10);
    }

    private void setPartPosition(EntityGiantSquidPart part, double offsetX, double offsetY, double offsetZ, float offsetScale) {
        part.setPosition(this.posX + offsetX * offsetScale * part.scale, this.posY + offsetY * offsetScale * part.scale, this.posZ + offsetZ * offsetScale * part.scale);
    }

    private void setPartPositionFromBuffer(EntityGiantSquidPart part, float pitch, float offsetScale, int ringBufferOffset) {
        float f2 = MathHelper.sin(getRingBuffer(ringBufferOffset, 1.0F, false) * ((float) Math.PI / 180F)) * (1 - Math.abs((this.getSquidPitch()) / 90F));
        float f3 = MathHelper.cos(getRingBuffer(ringBufferOffset, 1.0F, false) * ((float) Math.PI / 180F)) * (1 - Math.abs((this.getSquidPitch()) / 90F));
        setPartPosition(part, f2, pitch, -f3, offsetScale);
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 1;
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 3;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.isAIDisabled() && this.isInWater()) {
            if (this.dataManager.get(OVERRIDE_BODYROT)) {
                forward = -forward;
            }
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            double d = this.getAttackTarget() == null ? 0.6D : 0.9D;
            this.motionX *= 0.9D;
            this.motionY *= d;
            this.motionZ *= 0.9D;
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }


    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setBlue(compound.getBoolean("Blue"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Blue", this.isBlue());
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    public float getDepressurization() {
        return MathHelper.clamp(this.dataManager.get(DEPRESSURIZATION), 0, 1F);
    }

    public void setDepressurization(float depressurization) {
        this.dataManager.set(DEPRESSURIZATION, depressurization);
    }

    public float getSquidPitch() {
        return MathHelper.clamp(this.dataManager.get(SQUID_PITCH), -90, 90);
    }

    public void setSquidPitch(float pitch) {
        this.dataManager.set(SQUID_PITCH, pitch);
    }

    public void incrementSquidPitch(float pitch) {
        this.dataManager.set(SQUID_PITCH, this.getSquidPitch() + pitch);
    }

    public void decrementSquidPitch(float pitch) {
        this.dataManager.set(SQUID_PITCH, this.getSquidPitch() - pitch);
    }

    public boolean isGrabbing() {
        return this.dataManager.get(GRABBING);
    }

    public void setGrabbing(boolean running) {
        this.dataManager.set(GRABBING, running);
    }

    public boolean isCaptured() {
        return this.dataManager.get(CAPTURED);
    }

    public void setCaptured(boolean running) {
        this.dataManager.set(CAPTURED, running);
    }

    public boolean isBlue() {
        return this.dataManager.get(BLUE);
    }

    public void setBlue(boolean t) {
        this.dataManager.set(BLUE, t);
    }

    @Override
    public void applyEntityCollision(Entity entity) {
        if (!this.isCaptured()) {
            super.applyEntityCollision(entity);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.isEntityAlive();
    }

    public boolean attackEntityPartFrom(EntityGiantSquidPart part, DamageSource source, float amount) {
        return this.attackEntityFrom(source, amount);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    public void directPitch(double d0, double d1, double d2, double d3) {
        boolean shift = this.dataManager.get(OVERRIDE_BODYROT);
        float add = shift ? 90.0F : -90.0F;
        float f = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) + add;
        this.rotationYaw = this.rotlerp(this.rotationYaw, f, shift ? 10 : 5);
    }

    public float getViewPitch(float partialTick) {
        return prevSquidPitch + (getSquidPitch() - prevSquidPitch) * partialTick;
    }

    public float getViewYaw(float partialTick) {
        return partialTick == 1.0F ? this.renderYawOffset : this.prevRenderYawOffset + (this.renderYawOffset - this.prevRenderYawOffset) * partialTick;
    }

    protected float rotlerp(float in, float target, float maxShift) {
        float f = MathHelper.wrapDegrees(target - in);
        if (f > maxShift) {
            f = maxShift;
        }

        if (f < -maxShift) {
            f = -maxShift;
        }

        float f1 = in + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }

    private float getDepressureLevel() {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        int waterLevelAbove = 0;
        while (waterLevelAbove < 10) {
            IBlockState blockstate = this.world.getBlockState(blockpos$mutable.setPos(this.posX, this.posY + waterLevelAbove, this.posZ));
            if (blockstate.getMaterial() != Material.WATER && !blockstate.isFullCube()) {
                break;
            } else {
                waterLevelAbove++;
            }
        }
        return 1F - (waterLevelAbove / 10F);
    }

    private boolean canFitAt(BlockPos pos) {
        return true;
    }

    public boolean tickCaptured(EntityCachalotWhale whale) {
        resetCapturedStateIn = 25;
        if (this.rand.nextInt(13) == 0) {
            spawnInk();
            whale.attackEntityFrom(DamageSource.causeMobDamage(this), 4 + this.rand.nextInt(4));
            if (this.rand.nextFloat() <= 0.3F) {
                this.setCaptured(false);
                if (this.rand.nextFloat() < 0.2F) {
                    this.entityDropItem(new ItemStack(AMItemRegistry.LOST_TENTACLE), 0.0F);
                }
                return true;
            }
        }
        this.setCaptured(true);
        this.setSquidPitch(0);
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float f) {
        if (super.attackEntityFrom(src, f) && this.getRevengeTarget() != null && !this.isCaptured() && this.rand.nextBoolean()) {
            this.spawnInk();
            return true;
        } else {
            return false;
        }
    }

    private void spawnInk() {
        this.playSound(SoundEvents.ENTITY_SQUID_HURT, this.getSoundVolume(), 0.5F * this.getSoundPitch());
        if (!this.world.isRemote) {
            Vec3d inkDirection = new Vec3d(0, 0, 1.2F).rotatePitch(-this.getSquidPitch() * ((float) Math.PI / 180F)).rotateYaw(-this.renderYawOffset * ((float) Math.PI / 180F));
            Vec3d vec3 = this.getPositionVector().add(inkDirection);
            for (int i = 0; i < 30; ++i) {
                Vec3d vec32 = inkDirection.add(new Vec3d(
                        this.rand.nextFloat() - 0.5F,
                        this.rand.nextFloat() - 0.5F,
                        this.rand.nextFloat() - 0.5F)).scale(0.8D + (double) (this.rand.nextFloat() * 2.0F));
                ((net.minecraft.world.WorldServer) this.world).spawnParticle(
                        EnumParticleTypes.SPELL_MOB,
                        vec3.x, vec3.y + 0.5D, vec3.z,
                        0, vec32.x, vec32.y, vec32.z, 0.1D);
            }
        }
    }

    private class AIAvoidWhales extends EntityAIBase {

        private EntityCachalotWhale whale;
        private int runDelay;

        public AIAvoidWhales() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityGiantSquid.this.isInWater() && !EntityGiantSquid.this.collidedHorizontally && !EntityGiantSquid.this.isCaptured() && runDelay-- <= 0) {
                EntityCachalotWhale closest = null;
                float dist = 50;
                List<EntityCachalotWhale> whales = EntityGiantSquid.this.world.getEntitiesWithinAABB(EntityCachalotWhale.class, EntityGiantSquid.this.getEntityBoundingBox().grow(dist));
                for (EntityCachalotWhale dude : whales) {
                    if (closest == null || dude.getDistance(EntityGiantSquid.this) < closest.getDistance(EntityGiantSquid.this)) {
                        closest = dude;
                    }
                }
                if (closest != null) {
                    whale = closest;
                    return true;
                }
                runDelay = 50 + EntityGiantSquid.this.rand.nextInt(50);
            }

            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return whale != null && whale.isEntityAlive() && !EntityGiantSquid.this.collidedHorizontally && EntityGiantSquid.this.getDistance(whale) < 60;
        }

        @Override
        public void updateTask() {
            if (whale != null && whale.isEntityAlive()) {
                double dist = EntityGiantSquid.this.getDistance(whale);
                Vec3d vec = EntityGiantSquid.this.getPositionVector().subtract(whale.getPositionVector()).normalize();
                Vec3d vec2 = EntityGiantSquid.this.getPositionVector().add(vec.scale(12 + EntityGiantSquid.this.rand.nextInt(5)));
                EntityGiantSquid.this.getNavigator().tryMoveToXYZ(vec2.x, vec2.y, vec2.z, dist < 20 ? 1.9D : 1.3D);
            }
        }

        @Override
        public void resetTask() {
            whale = null;
        }
    }

    private class AIDeepwaterSwimming extends EntityAIBase {

        private BlockPos moveTo;

        public AIDeepwaterSwimming() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityGiantSquid.this.isBeingRidden() || EntityGiantSquid.this.getAttackTarget() != null && !EntityGiantSquid.this.isGrabbing() || !EntityGiantSquid.this.isInWater() && !EntityGiantSquid.this.isInLava()) {
                return false;
            } else {
                if (EntityGiantSquid.this.getNavigator().noPath() || EntityGiantSquid.this.getRNG().nextInt(30) == 0) {
                    BlockPos found = findTargetPos();
                    if (found != null) {
                        moveTo = found;
                        return true;
                    }
                }
                return false;
            }
        }

        private BlockPos findTargetPos() {
            for (int i = 0; i < 15; i++) {
                BlockPos pos = EntityGiantSquid.this.getPosition().add(
                        EntityGiantSquid.this.rand.nextInt(16) - 8,
                        EntityGiantSquid.this.rand.nextInt(32) - 16,
                        EntityGiantSquid.this.rand.nextInt(16) - 8);
                if (EntityGiantSquid.this.world.getBlockState(pos).getMaterial() == Material.WATER && EntityGiantSquid.this.canFitAt(pos)) {
                    return getDeeperTarget(pos);
                }
            }
            return null;
        }

        private BlockPos getDeeperTarget(BlockPos waterAtPos) {
            BlockPos surface = new BlockPos(waterAtPos);
            BlockPos seafloor = new BlockPos(waterAtPos);
            while (EntityGiantSquid.this.world.getBlockState(surface).getMaterial() == Material.WATER && surface.getY() < 255) {
                surface = surface.up();
            }
            while (EntityGiantSquid.this.world.getBlockState(seafloor).getMaterial() == Material.WATER && seafloor.getY() > 0) {
                seafloor = seafloor.down();
            }
            int distance = surface.getY() - seafloor.getY();
            if (distance < 10) {
                return waterAtPos;
            } else {
                int i = (int) (distance * 0.4);
                return seafloor.up(1 + EntityGiantSquid.this.rand.nextInt(i));
            }
        }

        @Override
        public void startExecuting() {
            EntityGiantSquid.this.getNavigator().tryMoveToXYZ(moveTo.getX() + 0.5D, moveTo.getY() + 0.5D, moveTo.getZ() + 0.5D, 1.0D);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }
    }

    private class AIMelee extends EntityAIBase {

        @Override
        public boolean shouldExecute() {
            return EntityGiantSquid.this.isInWater() && EntityGiantSquid.this.getAttackTarget() != null && EntityGiantSquid.this.getAttackTarget().isEntityAlive();
        }

        @Override
        public void updateTask() {
            EntityGiantSquid squid = EntityGiantSquid.this;
            EntityLivingBase target = EntityGiantSquid.this.getAttackTarget();
            double dist = squid.getDistance(target);
            if (squid.canEntityBeSeen(target) && dist < 7.0D) {
                squid.setGrabbing(true);
            } else {
                Vec3d moveBodyTo = target.getPositionVector();
                squid.getNavigator().tryMoveToXYZ(moveBodyTo.x, moveBodyTo.y, moveBodyTo.z, 1.0D);
            }
            if (dist < 14.0D) {
                squid.dataManager.set(OVERRIDE_BODYROT, true);
            } else {
                squid.dataManager.set(OVERRIDE_BODYROT, false);
            }
        }

        @Override
        public void resetTask() {
            EntityGiantSquid.this.dataManager.set(OVERRIDE_BODYROT, false);
            EntityGiantSquid.this.setGrabbing(false);
        }
    }
}
