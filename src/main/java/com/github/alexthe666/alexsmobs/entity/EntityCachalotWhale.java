package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * Forge 1.12.2 port of the cachalot whale (1.16 used {@code AnimalEntity}, {@code EntityType}, multipart API, etc.).
 * Hit parts are separate {@link EntityCachalotPart} entities spawned into the world like {@link EntityBoneSerpent} segments.
 */
public class EntityCachalotWhale extends EntityAnimal implements ISemiAquatic {

    private static final DataParameter<Boolean> CHARGING = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> BEACHED = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ALBINO = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DESPAWN_BEACH = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GRABBING = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HOLDING_SQUID_LEFT = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> CAUGHT_ID = EntityDataManager.createKey(EntityCachalotWhale.class, DataSerializers.VARINT);

    public final double[][] ringBuffer = new double[64][3];
    public final EntityCachalotPart headPart;
    public final EntityCachalotPart bodyFrontPart;
    public final EntityCachalotPart bodyPart;
    public final EntityCachalotPart tail1Part;
    public final EntityCachalotPart tail2Part;
    public final EntityCachalotPart tail3Part;
    public final EntityCachalotPart[] whaleParts;
    public int ringBufferIndex = -1;
    public float prevChargingProgress;
    public float chargeProgress;
    public float prevSleepProgress;
    public float sleepProgress;
    public float prevBeachedProgress;
    public float beachedProgress;
    public float prevGrabProgress;
    public float grabProgress;
    public int grabTime;
    private boolean receivedEcho = false;
    private boolean waitForEchoFlag = true;
    private int echoTimer = 0;
    private boolean prevEyesInWater = false;
    private int spoutTimer = 0;
    private int chargeCooldown = 0;
    private float whaleSpeedMod = 1F;
    private int rewardTime = 0;
    private EntityPlayer rewardPlayer;
    private int blockBreakCounter;
    private int despawnDelay = 47999;
    private int ambergrisDrops = 0;
    private int echoSoundCooldown = 0;
    private boolean whalePartsSpawned;

    public EntityCachalotWhale(World world) {
        super(world);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setSize(9.0F, 4.0F);
        this.moveHelper = new AnimalSwimMoveControllerSink(this, 1.0F, 1.0F, 3.0F);
        this.headPart = new EntityCachalotPart(this, 3.0F, 3.5F);
        this.bodyFrontPart = new EntityCachalotPart(this, 4.0F, 4.0F);
        this.bodyPart = new EntityCachalotPart(this, 5.0F, 4.0F);
        this.tail1Part = new EntityCachalotPart(this, 4.0F, 3.0F);
        this.tail2Part = new EntityCachalotPart(this, 3.0F, 2.0F);
        this.tail3Part = new EntityCachalotPart(this, 3.0F, 0.7F);
        this.whaleParts = new EntityCachalotPart[]{this.headPart, this.bodyFrontPart, this.bodyPart, this.tail1Part, this.tail2Part, this.tail3Part};
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.2D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(30.0D);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isSleeping() && !this.isCharging() && !this.isDespawnBeach() && !this.isAlbino();
    }

    private boolean canDespawnBeachLogic() {
        return this.isDespawnBeach();
    }

    private void tryDespawn() {
        if (this.canDespawnBeachLogic()) {
            this.despawnDelay = this.despawnDelay - 1;
            if (this.despawnDelay <= 0) {
                this.clearLeashed(true, false);
                this.setDead();
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.CACHALOT_WHALE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CACHALOT_WHALE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CACHALOT_WHALE_HURT;
    }

    public void scaleParts() {
        for (EntityCachalotPart parts : whaleParts) {
            float prev = parts.scale;
            parts.scale = this.isChild() ? 0.5F : 1F;
            if (prev != parts.scale) {
                parts.recalculateSize();
            }
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @Override
    public void collideWithNearbyEntities() {
    }

    public boolean applyInteractionFromPart(EntityPlayer player, EnumHand hand) {
        return this.processInteract(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (this.isBreedingItem(stack)) {
            if (!this.world.isRemote) {
                if (this.getGrowingAge() == 0 && !this.isInLove()) {
                    this.consumeItemFromStack(player, stack);
                    this.setInLove(player);
                } else if (this.isChild()) {
                    this.consumeItemFromStack(player, stack);
                    this.ageUp((int) ((float) (-this.getGrowingAge() / 20) * 0.1F), true);
                }
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Albino", this.isAlbino());
        compound.setBoolean("Beached", this.isBeached());
        compound.setBoolean("BeachedDespawnFlag", this.isDespawnBeach());
        compound.setInteger("DespawnDelay", this.despawnDelay);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setAlbino(compound.getBoolean("Albino"));
        this.setBeached(compound.getBoolean("Beached"));
        this.setDespawnBeach(compound.getBoolean("BeachedDespawnFlag"));
        if (compound.hasKey("DespawnDelay", 99)) {
            this.despawnDelay = compound.getInteger("DespawnDelay");
        }
        this.whalePartsSpawned = false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHARGING, Boolean.FALSE);
        this.dataManager.register(SLEEPING, Boolean.FALSE);
        this.dataManager.register(BEACHED, Boolean.FALSE);
        this.dataManager.register(ALBINO, Boolean.FALSE);
        this.dataManager.register(DESPAWN_BEACH, Boolean.FALSE);
        this.dataManager.register(GRABBING, Boolean.FALSE);
        this.dataManager.register(HOLDING_SQUID_LEFT, Boolean.FALSE);
        this.dataManager.register(CAUGHT_ID, -1);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AIBreathe());
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new AnimalAIMate(this, 1.0D));
        this.tasks.addTask(3, new AnimalAIFollowParentRanged(this, 1.1D, 32, 10));
        this.tasks.addTask(4, new AnimalAIRandomSwimming(this, 0.6D, 10, 24, true) {
            @Override
            public boolean shouldExecute() {
                return !EntityCachalotWhale.this.isSleeping() && !EntityCachalotWhale.this.isBeached() && super.shouldExecute();
            }
        });
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 20.0F));
        this.tasks.addTask(7, new CachalotAIFollowBoat(this));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityLivingBase.class, 30, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.CACHALOT_WHALE_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return !EntityCachalotWhale.this.isSleeping() && !EntityCachalotWhale.this.isBeached() && super.shouldExecute();
            }
        });
    }

    @Override
    public void onLivingUpdate() {
        int airBefore = this.getAir();
        super.onLivingUpdate();

        if (!this.world.isRemote && !this.whalePartsSpawned) {
            for (EntityCachalotPart part : this.whaleParts) {
                if (!part.isEntityAlive()) {
                    part.setPosition(this.posX, this.posY, this.posZ);
                }
                this.world.spawnEntity(part);
            }
            this.whalePartsSpawned = true;
        }

        if (!this.world.isRemote) {
            if (!this.isInsideOfMaterial(Material.WATER) && this.prevEyesInWater && spoutTimer <= 0 && airBefore < this.getMaxAir() / 2) {
                spoutTimer = 20 + this.rand.nextInt(10);
            }
        }
        this.prevEyesInWater = this.isInsideOfMaterial(Material.WATER);

        this.scaleParts();
        if (echoSoundCooldown > 0) {
            echoSoundCooldown--;
        }
        if (this.isSleeping()) {
            this.getNavigator().clearPath();
            this.rotationPitch = -90;
            this.whaleSpeedMod = 0;
            if (this.isInsideOfMaterial(Material.WATER) && this.getAir() < 200) {
                this.motionY += 0.06D;
            } else {
                BlockPos waterPos = new BlockPos(this.posX, this.posY, this.posZ);
                while (this.world.getBlockState(waterPos).getMaterial() == Material.WATER && waterPos.getY() < 255) {
                    waterPos = waterPos.up();
                }
                if (waterPos.getY() - this.posY < (this.isChild() ? 7 : 12)) {
                    this.motionY -= 0.06D;
                }
                if (this.rand.nextInt(100) == 0) {
                    this.motionY += this.rand.nextGaussian() * 0.06D;
                }
            }
        } else {
            if (this.whaleSpeedMod == 0) {
                this.whaleSpeedMod = 1;
            }
        }
        float rPitch = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        if (this.isGrabbing()) {
            this.rotationPitch = 0;
        } else {
            this.rotationPitch = MathHelper.clamp(rPitch, -90, 90);
        }
        if (this.onGround && !this.isInWater()) {
            this.setBeached(true);
            this.rotationPitch = 0;
            this.setSleeping(false);
        }
        if (this.isBeached()) {
            this.whaleSpeedMod = 0;
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
            if (this.isInsideOfMaterial(Material.WATER)) {
                EntityPlayer entity = this.world.getClosestPlayerToEntity(this, 50.0D);
                if (this.getRevengeTarget() != entity) {
                    rewardTime = 15;
                    rewardPlayer = entity;
                }
                this.despawnDelay = 47999;
                this.setBeached(false);
            }
        }
        if (!this.isBeached() && rewardTime > 0) {
            float dif = 0;
            if (rewardPlayer != null) {
                double d0 = rewardPlayer.posX - this.posX;
                double d1 = rewardPlayer.posY + (double) rewardPlayer.getEyeHeight() - (this.posY + (double) this.getEyeHeight());
                double d2 = rewardPlayer.posZ - this.posZ;
                double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
                float targetYaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                float targetPitch = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
                this.rotationYaw += MathHelper.clamp(targetYaw - this.rotationYaw, -2, 2);
                this.rotationPitch += MathHelper.clamp(targetPitch - this.rotationPitch, -2, 2);
                this.renderYawOffset = this.rotationYaw;
                dif = Math.abs(MathHelper.wrapDegrees(targetYaw) - MathHelper.wrapDegrees(this.rotationYaw));
            }
            if (dif < 5) {
                if (rewardTime % 5 == 0 && ambergrisDrops < 2 + this.rand.nextInt(1) && this.isDespawnBeach()) {
                    ambergrisDrops++;
                    if (!this.world.isRemote) {
                        Vec3d vec = this.getMouthVec();
                        EntityItem itementity = new EntityItem(this.world, vec.x, vec.y, vec.z, new ItemStack(AMItemRegistry.AMBERGRIS));
                        itementity.setDefaultPickupDelay();
                        this.world.spawnEntity(itementity);
                    }
                }
                this.rewardTime--;
            }
            if (rewardTime <= 2) {
                this.setDespawnBeach(false);
                this.setCharging(false);
                this.whaleSpeedMod = 1F;
            } else {
                this.setCharging(true);
                this.whaleSpeedMod = 0.2F;
            }
        }

        this.prevChargingProgress = this.chargeProgress;
        this.prevSleepProgress = this.sleepProgress;
        this.prevBeachedProgress = this.beachedProgress;
        this.prevGrabProgress = this.grabProgress;
        if (this.isGrabbing()) {
            if (this.grabProgress < 10F) {
                this.grabProgress++;
            }
            this.grabTime++;
        } else {
            if (this.grabProgress > 0F) {
                this.grabProgress--;
            }
            this.grabTime = 0;
        }
        if (this.isCharging() && this.chargeProgress < 10F) {
            this.chargeProgress++;
        }
        if (!this.isCharging() && this.chargeProgress > 0F) {
            this.chargeProgress--;
        }
        if (this.isSleeping() && this.sleepProgress < 10F) {
            this.sleepProgress++;
        }
        if (!this.isSleeping() && this.sleepProgress > 0F) {
            this.sleepProgress--;
        }
        if (this.isBeached() && this.beachedProgress < 10F) {
            this.beachedProgress++;
        }
        if (!this.isBeached() && this.beachedProgress > 0F) {
            this.beachedProgress--;
        }
        this.rotationYawHead = this.rotationYaw;
        this.renderYawOffset = this.rotationYaw;

        if (!this.isAIDisabled()) {
            if (this.ringBufferIndex < 0) {
                for (int i = 0; i < this.ringBuffer.length; ++i) {
                    this.ringBuffer[i][0] = this.rotationYaw;
                    this.ringBuffer[i][1] = this.posY;
                    this.ringBuffer[i][2] = 0.0D;
                }
            }
            this.ringBufferIndex++;
            if (this.ringBufferIndex == this.ringBuffer.length) {
                this.ringBufferIndex = 0;
            }
            this.ringBuffer[this.ringBufferIndex][0] = this.rotationYaw;
            this.ringBuffer[this.ringBufferIndex][1] = this.posY;
            Vec3d[] avector3d = new Vec3d[this.whaleParts.length];

            for (int j = 0; j < this.whaleParts.length; ++j) {
                this.whaleParts[j].collideWithNearbyEntities();
                avector3d[j] = new Vec3d(this.whaleParts[j].posX, this.whaleParts[j].posY, this.whaleParts[j].posZ);
            }
            float f4 = MathHelper.sin(this.rotationYaw * ((float) Math.PI / 180F) - 0 * 0.01F);
            float f19 = MathHelper.cos(this.rotationYaw * ((float) Math.PI / 180F) - 0 * 0.01F);
            float f15 = (float) (this.getMovementOffsets(5, 1.0F)[1] - this.getMovementOffsets(10, 1.0F)[1]) * 10.0F * ((float) Math.PI / 180F);
            float f16 = MathHelper.cos(f15);
            float f2 = MathHelper.sin(f15);
            float f17 = this.rotationYaw * ((float) Math.PI / 180F);
            float pitch = this.rotationPitch * ((float) Math.PI / 180F);
            float f3 = MathHelper.sin(f17) * (1 - Math.abs(this.rotationPitch / 90F));
            float f18 = MathHelper.cos(f17) * (1 - Math.abs(this.rotationPitch / 90F));

            this.setPartPosition(this.bodyPart, f3 * 0.5F, -pitch * 0.5F, -f18 * 0.5F);
            this.setPartPosition(this.bodyFrontPart, (f3) * -3.5F, -pitch * 3F, (f18) * 3.5F);
            this.setPartPosition(this.headPart, f3 * -7F, -pitch * 5F, -f18 * -7F);
            double[] adouble = this.getMovementOffsets(5, 1.0F);

            for (int k = 0; k < 3; ++k) {
                EntityCachalotPart enderdragonpartentity = null;
                if (k == 0) {
                    enderdragonpartentity = this.tail1Part;
                }
                if (k == 1) {
                    enderdragonpartentity = this.tail2Part;
                }
                if (k == 2) {
                    enderdragonpartentity = this.tail3Part;
                }

                double[] adouble1 = this.getMovementOffsets(15 + k * 5, 1.0F);
                float f7 = this.rotationYaw * ((float) Math.PI / 180F) + (float) MathHelper.wrapDegrees(adouble1[0] - adouble[0]) * ((float) Math.PI / 180F);
                float f20 = MathHelper.sin(f7) * (1 - Math.abs(this.rotationPitch / 90F));
                float f21 = MathHelper.cos(f7) * (1 - Math.abs(this.rotationPitch / 90F));
                float f22 = -3.6F;
                float f23 = (float) (k + 1) * f22 - 2F;
                this.setPartPosition(enderdragonpartentity, -(f3 * 0.5F + f20 * f23) * f16, pitch * 1.5F * (k + 1), (f18 * 0.5F + f21 * f23) * f16);
            }

            for (int l = 0; l < this.whaleParts.length; ++l) {
                this.whaleParts[l].prevPosX = avector3d[l].x;
                this.whaleParts[l].prevPosY = avector3d[l].y;
                this.whaleParts[l].prevPosZ = avector3d[l].z;
                this.whaleParts[l].lastTickPosX = avector3d[l].x;
                this.whaleParts[l].lastTickPosY = avector3d[l].y;
                this.whaleParts[l].lastTickPosZ = avector3d[l].z;
            }
        }
        if (!this.world.isRemote) {
            EntityLivingBase target = this.getAttackTarget();
            if (target == null || !target.isEntityAlive()) {
                this.setGrabbing(false);
                whaleSpeedMod = this.isSleeping() ? 0 : 1;
                this.setCharging(false);
                this.setCaughtSquidId(-1);
            } else if (!this.isBeached() && !this.isSleeping() && this.rewardTime <= 0) {
                if (this.isGrabbing() && target.isEntityAlive()) {
                    this.setCaughtSquidId(target.getEntityId());
                    whaleSpeedMod = 0.1F;
                    float scale = this.isChild() ? 0.5F : 1F;
                    float offsetAngle = -(float) Math.cos(this.grabTime * 0.3F) * 0.1F * this.grabProgress;
                    float renderYaw = (float) this.getMovementOffsets(0, 1.0F)[0];
                    Vec3d extraVec = new Vec3d(0, 0, -3F).rotatePitch(-this.rotationPitch * ((float) Math.PI / 180F)).rotateYaw(-renderYaw * ((float) Math.PI / 180F));
                    Vec3d backOfHead = this.headPart.getPositionVector().add(extraVec);
                    Vec3d swingVec = new Vec3d(this.isHoldingSquidLeft() ? 1.4F : -1.4F, -0.1, 3F).rotatePitch(-this.rotationPitch * ((float) Math.PI / 180F)).rotateYaw(-renderYaw * ((float) Math.PI / 180F)).rotateYaw(offsetAngle);
                    Vec3d mouth = backOfHead.add(swingVec.scale(scale));
                    target.setPosition(mouth.x, mouth.y, mouth.z);
                    if (this.isHoldingSquidLeft()) {
                        target.rotationYaw = this.renderYawOffset + 90 - (float) Math.toDegrees(offsetAngle);
                    } else {
                        target.rotationYaw = this.renderYawOffset - 90 - (float) Math.toDegrees(offsetAngle);
                    }
                    if (target instanceof EntityGiantSquid) {
                        if (((EntityGiantSquid) target).tickCaptured(this)) {
                            this.setGrabbing(false);
                            Vec3d release = this.getSquidReleasePos();
                            target.setPosition(release.x, release.y, release.z);
                        }
                    }
                    if (this.grabTime % 20 == 0 && this.grabTime > 30) {
                        target.attackEntityFrom(DamageSource.causeMobDamage(this), 4 + this.rand.nextInt(4));
                    }
                    if (this.grabTime > 300) {
                        this.setGrabbing(false);
                        Vec3d release = this.getSquidReleasePos();
                        target.setPosition(release.x, release.y, release.z);
                    }
                } else {
                    this.setCaughtSquidId(-1);
                    this.getLookHelper().setLookPositionWithEntity(target, 360, 360);
                    this.waitForEchoFlag = this.getRevengeTarget() == null || !this.getRevengeTarget().isEntityEqual(target);
                    if (target instanceof EntityPlayer || !target.isInWater()) {
                        this.waitForEchoFlag = false;
                    }
                    if (this.waitForEchoFlag && !this.receivedEcho) {
                        this.setCharging(false);
                        whaleSpeedMod = 0.25F;
                        if (this.echoTimer % 10 == 0) {
                            if (this.echoTimer % 40 == 0) {
                                this.playSound(AMSoundRegistry.CACHALOT_WHALE_CLICK, this.getSoundVolume(), this.getSoundPitch());
                            }
                            EntityCachalotEcho echo = new EntityCachalotEcho(this.world, this);
                            float radius = this.headPart.width * 0.5F;
                            float angle = (0.01745329251F * this.renderYawOffset);
                            double extraX = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.sin((float) (Math.PI + angle)) + (this.rand.nextFloat() - 0.5F) + this.motionX * 2F;
                            double extraZ = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.cos(angle) + (this.rand.nextFloat() - 0.5F) + this.motionZ * 2F;
                            double x = this.headPart.posX + extraX;
                            double y = this.headPart.posY + (double) this.headPart.height * 0.5D;
                            double z = this.headPart.posZ + extraZ;
                            double d0 = target.posX - x;
                            double d1 = target.getEntityBoundingBox().minY + 0.1D * (target.getEntityBoundingBox().maxY - target.getEntityBoundingBox().minY) - y;
                            double d2 = target.posZ - z;
                            echo.setPosition(x, y, z);
                            echo.shoot(d0, d1, d2, 1F, 0.0F);
                            this.world.spawnEntity(echo);
                        }
                        this.echoTimer++;
                    }
                    if (!this.waitForEchoFlag || this.receivedEcho) {
                        double d0 = target.posX - this.posX;
                        double d1 = target.posY + (double) target.getEyeHeight() - (this.posY + (double) this.getEyeHeight());
                        double d2 = target.posZ - this.posZ;
                        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
                        float targetYaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                        float targetPitch = (float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI)));
                        this.rotationPitch += MathHelper.clamp(targetPitch - this.rotationPitch, -2, 2);
                        if (d0 * d0 + d2 * d2 >= 4) {
                            this.rotationYaw += MathHelper.clamp(targetYaw - this.rotationYaw, -2, 2);
                            this.renderYawOffset = this.rotationYaw;
                        }
                        float dif = Math.abs(MathHelper.wrapDegrees(targetYaw) - MathHelper.wrapDegrees(this.rotationYaw));
                        if (this.chargeCooldown <= 0 && dif < 4) {
                            this.setCharging(true);
                            whaleSpeedMod = 1.2F;
                            if (d0 * d0 + d2 * d2 < 4) {
                                this.rotationYaw = this.prevRotationYaw;
                                this.renderYawOffset = this.prevRotationYaw;
                                this.motionX *= 0.8D;
                                this.motionZ *= 0.8D;
                            } else {
                                if (this.isInWater() && target.isInWater()) {
                                    double mx = this.motionX;
                                    double my = this.motionY;
                                    double mz = this.motionZ;
                                    double vx = target.posX - this.posX;
                                    double vy = target.posY - this.posY;
                                    double vz = target.posZ - this.posZ;
                                    double lenSq = vx * vx + vy * vy + vz * vz;
                                    if (lenSq > 1.0E-7D) {
                                        double len = MathHelper.sqrt(lenSq);
                                        this.motionX = vx / len * 0.5D + mx * 0.8D;
                                        this.motionY = vy / len * 0.5D + my * 0.8D;
                                        this.motionZ = vz / len * 0.5D + mz * 0.8D;
                                    }
                                }
                                this.getMoveHelper().setMoveTo(target.posX, target.posY, target.posZ, 1.0D);
                            }
                            if (this.isCharging()) {
                                if (this.getDistance(target) < (double) this.width && this.chargeProgress > 4) {
                                    if (target instanceof EntityGiantSquid && !this.isChild()) {
                                        this.setGrabbing(true);
                                        this.setHoldingSquidLeft(this.rand.nextBoolean());
                                    } else {
                                        target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                                    }
                                    this.setCharging(false);
                                    if (target.getRidingEntity() instanceof EntityBoat) {
                                        EntityBoat boat = (EntityBoat) target.getRidingEntity();
                                        int plankMeta = boat.getBoatType().ordinal();
                                        for (int i = 0; i < 3; ++i) {
                                            this.entityDropItem(new ItemStack(Blocks.PLANKS, 1, plankMeta), 0.0F);
                                        }
                                        for (int j = 0; j < 2; ++j) {
                                            this.entityDropItem(new ItemStack(Items.STICK), 0.0F);
                                        }
                                        target.dismountRidingEntity();
                                        boat.attackEntityFrom(DamageSource.causeMobDamage(this), 1000.0F);
                                        boat.setDead();
                                    }
                                    this.chargeCooldown = target instanceof EntityPlayer ? 30 : 100;
                                    if (this.rand.nextInt(10) == 0) {
                                        if (!this.world.isRemote) {
                                            Vec3d vec = this.getMouthVec();
                                            EntityItem itementity = new EntityItem(this.world, vec.x, vec.y, vec.z, new ItemStack(AMItemRegistry.CACHALOT_WHALE_TOOTH));
                                            itementity.setDefaultPickupDelay();
                                            this.world.spawnEntity(itementity);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (this.chargeCooldown > 0) {
                this.chargeCooldown--;
            }
            if (this.spoutTimer > 0) {
                this.world.setEntityState(this, (byte) 67);
                this.spoutTimer--;
                this.rotationPitch = 0;
                this.motionX = 0;
                this.motionY = 0;
                this.motionZ = 0;
            }
            if (this.isSleepTime() && !this.isSleeping() && this.isInWater() && this.getAttackTarget() == null) {
                this.setSleeping(true);
            }
            if (this.isSleeping() && (!this.isSleepTime() || this.getAttackTarget() != null)) {
                this.setSleeping(false);
            }
        }

        if (this.isEntityAlive() && this.isCharging()) {
            AxisAlignedBB headBox = this.headPart.getEntityBoundingBox().grow(1.0D);
            List<EntityLivingBase> entities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, headBox);
            for (Entity entity : entities) {
                if (!this.isOnSameTeam(entity) && !(entity instanceof EntityCachalotPart) && entity != this) {
                    this.launch(entity, true);
                }
            }
        }
        if (this.isInWater() && !this.isInsideOfMaterial(Material.WATER) && this.getAir() > 140) {
            this.motionY -= 0.06D;
        }
        if (!this.world.isRemote) {
            this.tryDespawn();
        }
    }

    private void launch(Entity e, boolean huge) {
        if (e.onGround || e.isInWater()) {
            double d0 = e.posX - this.posX;
            double d1 = e.posZ - this.posZ;
            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
            float f = huge ? 2F : 0.5F;
            e.addVelocity(d0 / d2 * f, huge ? 0.5D : 0.2F, d1 / d2 * f);
        }
    }

    private boolean isSleepTime() {
        long time = this.world.getWorldTime();
        return time > 18000L && time < 22812L && this.isInWater();
    }

    public Vec3d getReturnEchoVector() {
        float radius = this.headPart.width * 0.5F;
        float angle = (0.01745329251F * this.renderYawOffset);
        double extraX = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.sin((float) (Math.PI + angle)) + (this.rand.nextFloat() - 0.5F) + this.motionX * 2F;
        double extraZ = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.cos(angle) + (this.rand.nextFloat() - 0.5F) + this.motionZ * 2F;
        double x = this.headPart.posX + extraX;
        double y = this.headPart.posY + (double) this.headPart.height * 0.5D;
        double z = this.headPart.posZ + extraZ;
        return new Vec3d(x, y, z);
    }

    public Vec3d getMouthVec() {
        float radius = this.headPart.width * 0.5F;
        float angle = (0.01745329251F * this.renderYawOffset);
        double extraX = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.sin((float) (Math.PI + angle)) + (this.rand.nextFloat() - 0.5F) + this.motionX * 2F;
        double extraZ = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.cos(angle) + (this.rand.nextFloat() - 0.5F) + this.motionZ * 2F;
        double x = this.headPart.posX + extraX;
        double y = this.headPart.posY + 0.25D;
        double z = this.headPart.posZ + extraZ;
        return new Vec3d(x, y, z);
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        EntityLivingBase prev = this.getAttackTarget();
        if (prev != entitylivingbaseIn && entitylivingbaseIn != null) {
            this.receivedEcho = false;
        }
        super.setAttackTarget(entitylivingbaseIn);
    }

    public double[] getMovementOffsets(int p_70974_1_, float partialTicks) {
        if (!this.isEntityAlive()) {
            partialTicks = 0.0F;
        }

        partialTicks = 1.0F - partialTicks;
        int i = this.ringBufferIndex - p_70974_1_ & 63;
        int j = this.ringBufferIndex - p_70974_1_ - 1 & 63;
        double[] adouble = new double[3];
        double d0 = this.ringBuffer[i][0];
        double d1 = this.ringBuffer[j][0] - d0;
        adouble[0] = d0 + d1 * (double) partialTicks;
        d0 = this.ringBuffer[i][1];
        d1 = this.ringBuffer[j][1] - d0;
        adouble[1] = d0 + d1 * (double) partialTicks;
        adouble[2] = this.ringBuffer[i][2] + (this.ringBuffer[j][2] - this.ringBuffer[i][2]) * (double) partialTicks;
        return adouble;
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
    }

    private void setPartPosition(EntityCachalotPart part, double offsetX, double offsetY, double offsetZ) {
        part.setPosition(this.posX + offsetX * part.scale, this.posY + offsetY * part.scale, this.posZ + offsetZ * part.scale);
    }

    @Override
    @Nullable
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityCachalotWhale whale = (EntityCachalotWhale) AMEntityRegistry.CACHALOT_WHALE.newInstance(this.world);
        whale.setAlbino(this.isAlbino());
        return whale;
    }

    public boolean attackEntityPartFrom(EntityCachalotPart entityCachalotPart, DamageSource source, float amount) {
        return this.attackEntityFrom(source, amount);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData spawnDataIn) {
        this.setAir(this.getMaxAir());
        this.rotationPitch = 0.0F;
        IEntityLivingData data = super.onInitialSpawn(difficulty, spawnDataIn);
        this.setAlbino(this.rand.nextInt(100) == 0);
        return data;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.UNDEFINED;
    }

    public int getMaxAir() {
        return 4000;
    }

    public boolean hasCaughtSquid() {
        return this.dataManager.get(CAUGHT_ID) != -1;
    }

    private void setCaughtSquidId(int i) {
        this.dataManager.set(CAUGHT_ID, i);
    }

    @Nullable
    public Entity getCaughtSquid() {
        if (!this.hasCaughtSquid()) {
            return null;
        }
        return this.world.getEntityByID(this.dataManager.get(CAUGHT_ID));
    }

    private Vec3d getSquidReleasePos() {
        Vec3d mouth = this.getMouthVec();
        float yaw = this.renderYawOffset * ((float) Math.PI / 180F);
        return mouth.addVector(-MathHelper.sin(yaw) * 3.0D, 1.0D, MathHelper.cos(yaw) * 3.0D);
    }

    public boolean isGrabbing() {
        return this.dataManager.get(GRABBING);
    }

    public void setGrabbing(boolean grabbing) {
        this.dataManager.set(GRABBING, grabbing);
    }

    public boolean isHoldingSquidLeft() {
        return this.dataManager.get(HOLDING_SQUID_LEFT);
    }

    public void setHoldingSquidLeft(boolean left) {
        this.dataManager.set(HOLDING_SQUID_LEFT, left);
    }

    public boolean isCharging() {
        return this.dataManager.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.dataManager.set(CHARGING, charging);
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean charging) {
        this.dataManager.set(SLEEPING, charging);
    }

    public boolean isBeached() {
        return this.dataManager.get(BEACHED);
    }

    public void setBeached(boolean charging) {
        this.dataManager.set(BEACHED, charging);
    }

    public boolean isAlbino() {
        return this.dataManager.get(ALBINO);
    }

    public void setAlbino(boolean albino) {
        boolean prev = this.isAlbino();
        if (!prev && albino) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(230.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(45.0D);
            this.setHealth(160.0F);
        } else {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(160.0D);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(30.0D);
        }
        this.dataManager.set(ALBINO, albino);
    }

    public boolean isDespawnBeach() {
        return this.dataManager.get(DESPAWN_BEACH);
    }

    public void setDespawnBeach(boolean despawn) {
        this.dataManager.set(DESPAWN_BEACH, despawn);
    }

    @Override
    protected float getSoundVolume() {
        return this.isSilent() ? 0 : (float) AMConfig.cachalotVolume;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void updateAITasks() {
        super.updateAITasks();
        this.breakBlock();
    }

    public void breakBlock() {
        if (this.blockBreakCounter > 0) {
            --this.blockBreakCounter;
            return;
        }
        boolean flag = false;
        net.minecraft.util.ResourceLocation breakablesTag = this.isCharging() && this.getAttackTarget() != null ? AMTagRegistry.CACHALOT_WHALE_BREAKABLES : AMTagRegistry.ORCA_BREAKABLES;
        AxisAlignedBB box = this.getEntityBoundingBox();
        if (!this.world.isRemote && this.blockBreakCounter == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.world, this)) {
            for (int a = (int) Math.round(box.minX); a <= (int) Math.round(box.maxX); a++) {
                for (int b = (int) Math.round(box.minY) - 1; (b <= (int) Math.round(box.maxY) + 1) && (b <= 127); b++) {
                    for (int c = (int) Math.round(box.minZ); c <= (int) Math.round(box.maxZ); c++) {
                        BlockPos pos = new BlockPos(a, b, c);
                        IBlockState state = this.world.getBlockState(pos);
                        Block block = state.getBlock();
                        if (this.world.isAirBlock(pos)) {
                            continue;
                        }
                        AxisAlignedBB col = state.getCollisionBoundingBox(this.world, pos);
                        if (col == null || col == Block.NULL_AABB) {
                            continue;
                        }
                        if (!AMTagRegistry.blockInTag(breakablesTag, block)) {
                            continue;
                        }
                        if (state.getMaterial() == Material.WATER) {
                            continue;
                        }
                        if (block != Blocks.AIR) {
                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            flag = true;
                            this.world.destroyBlock(pos, true);
                            if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.FROSTED_ICE) {
                                this.world.setBlockState(pos, Blocks.WATER.getDefaultState());
                            }
                        }
                    }
                }
            }
        }
        if (flag) {
            blockBreakCounter = this.isCharging() && this.getAttackTarget() != null ? 2 : 20;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 67) {
            this.spawnSpoutParticles();
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private void spawnSpoutParticles() {
        if (this.isEntityAlive()) {
            for (int j = 0; j < 5 + this.rand.nextInt(4); ++j) {
                float radius = this.headPart.width * 0.5F;
                float angle = (0.01745329251F * this.renderYawOffset);
                double extraX = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.sin((float) (Math.PI + angle)) + (this.rand.nextFloat() - 0.5F) + this.motionX * 2F;
                double extraZ = (radius * (1F + this.rand.nextFloat() * 0.13F)) * MathHelper.cos(angle) + (this.rand.nextFloat() - 0.5F) + this.motionZ * 2F;
                double motX = this.rand.nextGaussian();
                double motZ = this.rand.nextGaussian();
                this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.headPart.posX + extraX, this.headPart.posY + (double) this.headPart.height, this.headPart.posZ + extraZ, motX * 0.1D + this.motionX, 2.0D, motZ * 0.1D + this.motionZ);
            }
        }
    }

    public void recieveEcho() {
        this.receivedEcho = true;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.cachalotWhaleSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 1;
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    public boolean shouldEnterWater() {
        return this.onGround && !this.isInWater();
    }

    @Override
    public boolean shouldLeaveWater() {
        return false;
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isSleeping() || this.isBeached();
    }

    @Override
    public int getWaterSearchRange() {
        return 32;
    }

    @Override
    public void setDead() {
        if (!this.world.isRemote) {
            for (EntityCachalotPart part : this.whaleParts) {
                if (part.isEntityAlive()) {
                    part.setDead();
                }
            }
        }
        super.setDead();
    }

    /**
     * Vanilla 1.16 dolphin-style boat interest; 1.12 has no {@code FollowBoatGoal}.
     */
    private static class CachalotAIFollowBoat extends net.minecraft.entity.ai.EntityAIBase {
        private final EntityCachalotWhale whale;
        private EntityBoat boat;
        private int delay;

        CachalotAIFollowBoat(EntityCachalotWhale whale) {
            this.whale = whale;
        }

        @Override
        public boolean shouldExecute() {
            if (!whale.isInWater()) {
                return false;
            }
            List<EntityBoat> list = whale.world.getEntitiesWithinAABB(EntityBoat.class, whale.getEntityBoundingBox().grow(24.0D));
            EntityBoat closest = null;
            double best = Double.MAX_VALUE;
            for (EntityBoat b : list) {
                double d = whale.getDistanceSq(b);
                if (d < best) {
                    best = d;
                    closest = b;
                }
            }
            this.boat = closest;
            return this.boat != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.boat != null && this.boat.isEntityAlive() && whale.getDistanceSq(this.boat) > 4.0D;
        }

        @Override
        public void resetTask() {
            this.boat = null;
        }

        @Override
        public void updateTask() {
            if (this.boat == null) {
                return;
            }
            if (--this.delay <= 0) {
                this.delay = 10;
                this.whale.getNavigator().tryMoveToEntityLiving(this.boat, 1.0D);
            }
        }
    }

    private class AIBreathe extends net.minecraft.entity.ai.EntityAIBase {

        AIBreathe() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return EntityCachalotWhale.this.getAir() < 140;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void startExecuting() {
            this.navigate();
        }

        private void navigate() {
            Iterator<BlockPos> it = BlockPos.getAllInBox(
                    new BlockPos(MathHelper.floor(EntityCachalotWhale.this.posX - 1.0D), MathHelper.floor(EntityCachalotWhale.this.posY), MathHelper.floor(EntityCachalotWhale.this.posZ - 1.0D)),
                    new BlockPos(MathHelper.floor(EntityCachalotWhale.this.posX + 1.0D), MathHelper.floor(EntityCachalotWhale.this.posY + 8.0D), MathHelper.floor(EntityCachalotWhale.this.posZ + 1.0D))
            ).iterator();
            BlockPos targetPos = null;
            while (it.hasNext()) {
                BlockPos pos = it.next();
                if (this.canBreatheAt(EntityCachalotWhale.this.world, pos)) {
                    targetPos = pos.down((int) (EntityCachalotWhale.this.height * 0.25D));
                    break;
                }
            }

            if (targetPos == null) {
                targetPos = new BlockPos(EntityCachalotWhale.this.posX, EntityCachalotWhale.this.posY + 4.0D, EntityCachalotWhale.this.posZ);
            }
            if (EntityCachalotWhale.this.isInsideOfMaterial(Material.WATER)) {
                EntityCachalotWhale.this.motionY += 0.05D;
            }

            EntityCachalotWhale.this.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 0.7D);
        }

        @Override
        public void updateTask() {
            this.navigate();
        }

        private boolean canBreatheAt(World world, BlockPos pos) {
            return world.isAirBlock(pos);
        }
    }
}
