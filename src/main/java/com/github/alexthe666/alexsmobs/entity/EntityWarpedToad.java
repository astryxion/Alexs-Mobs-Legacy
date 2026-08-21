package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.UUID;

public class EntityWarpedToad extends EntityTameable implements ITargetsDroppedItems, IFollower, ISemiAquatic {

    private static final DataParameter<Float> TONGUE_LENGTH = EntityDataManager.createKey(EntityWarpedToad.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> TONGUE_OUT = EntityDataManager.createKey(EntityWarpedToad.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityWarpedToad.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityWarpedToad.class, DataSerializers.VARINT);
    public float blinkProgress;
    public float prevBlinkProgress;
    public float attackProgress;
    public float prevAttackProgress;
    public float sitProgress;
    public float prevSitProgress;
    public float swimProgress;
    public float prevSwimProgress;
    private boolean isLandNavigator;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int currentMoveTypeDuration;
    private int swimTimer = -100;

    public EntityWarpedToad(World world) {
        super(world);
        this.setSize(0.9F, 1.4F);
        this.isImmuneToFire = true;
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.LAVA, 0.0F);
        switchNavigator(false);
    }

    public boolean isBased() {
        String s = this.getName();
        return s != null && s.toLowerCase().contains("pepe");
    }

    public static boolean canWarpedToadSpawn(World world, BlockPos pos) {
        BlockPos blockpos = pos.down();
        Material mat = world.getBlockState(blockpos).getMaterial();
        return mat == Material.LAVA || world.getBlockState(blockpos).isFullCube();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.WARPED_TOAD_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.WARPED_TOAD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.WARPED_TOAD_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.WARPED_TOAD;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.warpedToadSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
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
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("MonkeySitting", this.isSitting());
        compound.setInteger("Command", this.getCommand());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("MonkeySitting"));
        this.setCommand(compound.getInteger("Command"));
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISit(this));
        this.tasks.addTask(1, new EntityWarpedToad.TongueAttack(this));
        this.tasks.addTask(2, new EntityWarpedToad.FollowOwner(this, 1.3D, 4.0F, 2.0F, false));
        this.tasks.addTask(3, new AnimalAIFindWaterLava(this));
        this.tasks.addTask(3, new AnimalAILeaveWaterLava(this));
        this.tasks.addTask(3, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, Items.ROTTEN_FLESH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
            }
        });
        this.tasks.addTask(5, new WarpedToadAIRandomSwimming(this, 1.0D, 7));
        this.tasks.addTask(6, new AnimalAIWanderRanged(this, 60, 1.0D, 5, 4));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntityLivingBase.class, 50, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.WARPED_TOAD_TARGETS)));
        this.targetTasks.addTask(5, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && (this.isInWater() || this.isInLava())) {
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
    protected float getJumpUpwardsMotion() {
        return 0.5F;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void jump() {
        super.jump();
        double d0 = this.moveHelper.getSpeed();
        if (d0 > 0.0D) {
            double d1 = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            if (d1 < 0.01D) {
            }
        }

        if (!this.world.isRemote) {
            this.world.setEntityState(this, (byte) 1);
        }
    }

    public void setMovementSpeed(double newSpeed) {
        this.setAIMoveSpeed((float) newSpeed);
        this.moveHelper.setMoveTo(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ(), newSpeed);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);
    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (this.currentMoveTypeDuration > 0) {
            --this.currentMoveTypeDuration;
        }

        if (this.onGround && !this.isSitting()) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            if (this.currentMoveTypeDuration == 0) {
                EntityLivingBase livingentity = this.getAttackTarget();
                if (livingentity != null && this.getDistanceSq(livingentity) < 16.0D) {
                    this.calculateRotationYaw(livingentity.posX, livingentity.posZ);
                    this.moveHelper.setMoveTo(livingentity.posX, livingentity.posY, livingentity.posZ, this.moveHelper.getSpeed());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }
            if (this.jumpHelper instanceof EntityWarpedToad.JumpHelperController) {
                EntityWarpedToad.JumpHelperController rabbitController = (EntityWarpedToad.JumpHelperController) this.jumpHelper;
                if (!rabbitController.getIsJumping()) {
                    if (this.moveHelper.isUpdating() && this.currentMoveTypeDuration == 0) {
                        Path path = this.getNavigator().getPath();
                        Vec3d vector3d = new Vec3d(this.moveHelper.getX(), this.moveHelper.getY(), this.moveHelper.getZ());
                        if (path != null && !path.isFinished()) {
                            vector3d = path.getPosition(this);
                        }

                        this.calculateRotationYaw(vector3d.x, vector3d.z);
                        this.startJumping();
                    }
                } else if (!rabbitController.canJump()) {
                    this.enableJumpControl();
                }
            }
        } else if (this.isSitting()) {
            this.setJumping(false);
            this.checkLandingDelay();
        }

        this.wasOnGround = this.onGround;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == AMItemRegistry.MOSQUITO_LARVA && isTamed();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (isBreedingItem(itemstack)) {
            return super.processInteract(player, hand);
        }
        if (!isTamed() && item == AMItemRegistry.MOSQUITO_LARVA) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            if (getRNG().nextInt(3) == 0) {
                this.setTamedBy(player);
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
            return true;
        }
        if (isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, item)) {
            if (this.getHealth() < this.getMaxHealth()) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.heal(5);
                return true;
            }
            return false;
        }
        if (isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            this.setCommand(this.getCommand() + 1);
            if (this.getCommand() == 3) {
                this.setCommand(0);
            }
            player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
            boolean sit = this.getCommand() == 2;
            if (sit) {
                this.setSitting(true);
                return true;
            } else {
                this.setSitting(false);
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase livingentity = this.getOwner();
            if (entityIn == livingentity) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(livingentity);
            }
            if (livingentity != null) {
                return livingentity.isOnSameTeam(entityIn);
            }
        }

        return super.isOnSameTeam(entityIn);
    }

    public boolean canSpawnRunningEffectParticles() {
        return false;
    }

    private void calculateRotationYaw(double x, double z) {
        this.rotationYaw = (float) (MathHelper.atan2(z - this.posZ, x - this.posX) * (double) (180F / (float) Math.PI)) - 90.0F;
    }

    private void enableJumpControl() {
        if (jumpHelper instanceof EntityWarpedToad.JumpHelperController) {
            ((EntityWarpedToad.JumpHelperController) this.jumpHelper).setCanJump(true);
        }
    }

    private void disableJumpControl() {
        if (jumpHelper instanceof EntityWarpedToad.JumpHelperController) {
            ((EntityWarpedToad.JumpHelperController) this.jumpHelper).setCanJump(false);
        }
    }

    private void updateMoveTypeDuration() {
        if (this.moveHelper.getSpeed() < 2.2D) {
            this.currentMoveTypeDuration = 10;
        } else {
            this.currentMoveTypeDuration = 1;
        }
    }

    private void checkLandingDelay() {
        this.updateMoveTypeDuration();
        this.disableJumpControl();
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isChild() && this.getEyeHeight() > this.height) {
            this.setSize(this.width, this.height);
        }
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
        if (!world.isRemote) {
            if (isInWater() || isInLava()) {
                if (swimTimer < 0) {
                    swimTimer = 0;
                }
                swimTimer++;
            } else {
                if (swimTimer > 0) {
                    swimTimer = 0;
                }
                swimTimer--;
            }
        }
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.jumpHelper = new EntityWarpedToad.JumpHelperController(this);
            this.moveHelper = new EntityWarpedToad.MoveHelperController(this);
            this.navigator = createNavigator(this.world);
            this.isLandNavigator = true;
        } else {
            this.jumpHelper = new EntityJumpHelper(this);
            this.moveHelper = new AquaticMoveController(this, 1.2F);
            this.navigator = new BoneSerpentPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateGround(this, worldIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(TONGUE_LENGTH, 1F);
        this.dataManager.register(TONGUE_OUT, false);
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(SITTING, false);
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        prevBlinkProgress = blinkProgress;
        prevAttackProgress = attackProgress;
        prevSitProgress = sitProgress;
        prevSwimProgress = swimProgress;
        this.stepHeight = 1;

        boolean isTechnicalBlinking = this.ticksExisted % 50 > 42;
        if (isTechnicalBlinking && blinkProgress < 5F) {
            blinkProgress++;
        }
        if (!isTechnicalBlinking && blinkProgress > 0F) {
            blinkProgress--;
        }
        if (isTongueOut() && attackProgress < 5F) {
            attackProgress++;
        }
        EntityLivingBase entityIn = this.getAttackTarget();
        if (entityIn != null && attackProgress > 0) {
            if (isTongueOut()) {
                double d0 = entityIn.posX - this.posX;
                double d2 = entityIn.posZ - this.posZ;
                double d1 = entityIn.getPositionEyes(1.0F).y - this.getPositionEyes(1.0F).y;
                double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
                float f = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                float f1 = (float) (-(MathHelper.atan2(d1, d3) * (double) (180F / (float) Math.PI)));
                this.rotationPitch = f1;
                this.rotationYaw = f;
                this.renderYawOffset = this.rotationYaw;
                this.rotationYawHead = this.rotationYaw;
            } else {
                if (entityIn instanceof EntityCrimsonMosquito) {
                    ((EntityCrimsonMosquito) entityIn).setShrink(true);
                }
                this.rotationPitch = 0;
                float radius = attackProgress * 0.2F * 1.2F * (getTongueLength() - getTongueLength() * 0.4F);
                float angle = (0.01745329251F * this.renderYawOffset);
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraZ = radius * MathHelper.cos(angle);
                double yHelp = entityIn.height;
                Vec3d minus = new Vec3d(this.posX + extraX - this.getAttackTarget().posX, this.getEyeHeight() - yHelp - this.getAttackTarget().posY, this.posZ + extraZ - this.getAttackTarget().posZ);
                this.getAttackTarget().motionX = minus.x;
                this.getAttackTarget().motionY = minus.y;
                this.getAttackTarget().motionZ = minus.z;
                if (attackProgress == 0.5F) {
                    float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                    if (entityIn instanceof EntityCrimsonMosquito) {
                        damage = Float.MAX_VALUE;
                    }
                    entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                }
            }

            if (attackProgress == 5 && (entityIn.height < 0.89D || entityIn instanceof EntityCrimsonMosquito) && entityIn.getRidingEntity() != this) {
            }
        }
        if (!world.isRemote && isTongueOut() && attackProgress == 5F) {
            setTongueOut(false);
            attackProgress = 4F;
        }
        if (!isTongueOut() && attackProgress > 0F) {
            attackProgress -= 0.5F;
        }
        if (isSitting() && sitProgress < 5F) {
            sitProgress++;
        }
        if (!isSitting() && sitProgress > 0F) {
            sitProgress--;
        }
        if (shouldSwim() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!shouldSwim() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (shouldSwim() && swimProgress < 5F) {
            swimProgress++;
        }
        if (!shouldSwim() && swimProgress > 0F) {
            swimProgress--;
        }
    }

    public boolean shouldSwim() {
        return isInWater() || isInLava();
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.heal(5);
    }

    public boolean isBlinking() {
        return blinkProgress > 1 || blinkProgress < -1 || attackProgress > 1;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityWarpedToad child = new EntityWarpedToad(this.world);
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            child.setOwnerId(ownerId);
            child.setTamed(true);
        }
        return child;
    }

    public float getTongueLength() {
        return dataManager.get(TONGUE_LENGTH);
    }

    public void setTongueLength(float length) {
        dataManager.set(TONGUE_LENGTH, length);
    }

    public float getJumpCompletion(float partialTicks) {
        return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + partialTicks) / (float) this.jumpDuration;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 1) {
            this.spawnRunningParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public boolean hasJumper() {
        return jumpHelper instanceof EntityWarpedToad.JumpHelperController;
    }

    @Override
    public boolean shouldEnterWater() {
        return swimTimer < -200 && !isSitting() && this.getCommand() != 1;
    }

    @Override
    public boolean shouldLeaveWater() {
        return swimTimer > 600 && !isSitting() && this.getCommand() != 1;
    }

    @Override
    public boolean shouldStopMoving() {
        return isSitting();
    }

    private boolean isTongueOut() {
        return this.dataManager.get(TONGUE_OUT);
    }

    private void setTongueOut(boolean out) {
        this.dataManager.set(TONGUE_OUT, out);
    }

    @Override
    public int getWaterSearchRange() {
        return 8;
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntityWarpedToad warpedToad;
        private double nextJumpSpeed;

        MoveHelperController(EntityWarpedToad warpedToad) {
            super(warpedToad);
            this.warpedToad = warpedToad;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.warpedToad.hasJumper() && this.warpedToad.onGround && !this.warpedToad.isJumping && !((EntityWarpedToad.JumpHelperController) this.warpedToad.jumpHelper).getIsJumping()) {
                this.warpedToad.setMovementSpeed(0.0D);
            } else if (this.isUpdating()) {
                this.warpedToad.setMovementSpeed(this.nextJumpSpeed);
            }

            super.onUpdateMoveHelper();
        }

        @Override
        public void setMoveTo(double x, double y, double z, double speedIn) {
            if (this.warpedToad.isInWater()) {
                speedIn = 1.5D;
            }

            super.setMoveTo(x, y, z, speedIn);
            if (speedIn > 0.0D) {
                this.nextJumpSpeed = speedIn;
            }
        }
    }

    public class JumpHelperController extends EntityJumpHelper {
        private final EntityWarpedToad toad;
        private boolean canJump;

        public JumpHelperController(EntityWarpedToad toad) {
            super(toad);
            this.toad = toad;
        }

        public boolean getIsJumping() {
            return this.isJumping;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJumpIn) {
            this.canJump = canJumpIn;
        }

        @Override
        public void doJump() {
            if (this.isJumping) {
                this.toad.startJumping();
                this.isJumping = false;
            }
        }
    }

    public class TongueAttack extends EntityAIBase {
        private final EntityWarpedToad parentEntity;
        private int spitCooldown = 0;

        public TongueAttack(EntityWarpedToad toad) {
            this.parentEntity = toad;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return parentEntity.getAttackTarget() != null && !parentEntity.isBeingRidden();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return parentEntity.getAttackTarget() != null && !parentEntity.isBeingRidden();
        }

        @Override
        public void resetTask() {
            spitCooldown = 20;
            parentEntity.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (spitCooldown > 0) {
                spitCooldown--;
            }
            Entity entityIn = parentEntity.getAttackTarget();
            if (entityIn != null) {
                double dist = parentEntity.getDistance(entityIn);
                if (dist < 8 && this.parentEntity.canEntityBeSeen(entityIn)) {
                    if (!parentEntity.isTongueOut() && parentEntity.attackProgress == 0 && spitCooldown == 0) {
                        this.parentEntity.setTongueLength((float) Math.max(1F, dist + 2F));
                        spitCooldown = 10;
                        this.parentEntity.setTongueOut(true);
                    }
                }
                this.parentEntity.getNavigator().tryMoveToEntityLiving((EntityLivingBase) entityIn, 1.4F);
            }
        }
    }

    public class FollowOwner extends EntityAIBase {
        private final EntityWarpedToad tameable;
        private final IBlockAccess world;
        private final double followSpeed;
        private final float maxDist;
        private final float minDist;
        private final boolean teleportToLeaves;
        private EntityLivingBase owner;
        private int timeToRecalcPath;
        private float oldWaterCost;

        public FollowOwner(EntityWarpedToad p_i225711_1_, double p_i225711_2_, float p_i225711_4_, float p_i225711_5_, boolean p_i225711_6_) {
            this.tameable = p_i225711_1_;
            this.world = p_i225711_1_.world;
            this.followSpeed = p_i225711_2_;
            this.minDist = p_i225711_4_;
            this.maxDist = p_i225711_5_;
            this.teleportToLeaves = p_i225711_6_;
            this.setMutexBits(3);
            if (!(p_i225711_1_.getNavigator() instanceof PathNavigateGround) && !(p_i225711_1_.getNavigator() instanceof PathNavigateSwimmer)) {
                throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
            }
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase lvt_1_1_ = this.tameable.getOwner();
            if (lvt_1_1_ == null) {
                return false;
            } else if (lvt_1_1_ instanceof EntityPlayerMP && ((EntityPlayerMP) lvt_1_1_).isSpectator()) {
                return false;
            } else if (this.tameable.isSitting() || tameable.getCommand() != 1) {
                return false;
            } else if (this.tameable.getDistanceSq(lvt_1_1_) < (double) (this.minDist * this.minDist)) {
                return false;
            } else {
                this.owner = lvt_1_1_;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (this.tameable.getNavigator().noPath()) {
                return false;
            } else if (this.tameable.isSitting() || tameable.getCommand() != 1) {
                return false;
            } else {
                return this.tameable.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
            }
        }

        @Override
        public void startExecuting() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.tameable.getPathPriority(PathNodeType.WATER);
            this.tameable.setPathPriority(PathNodeType.WATER, 0.0F);
        }

        @Override
        public void resetTask() {
            this.owner = null;
            this.tameable.getNavigator().clearPath();
            this.tameable.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
        }

        @Override
        public void updateTask() {
            this.tameable.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.tameable.getVerticalFaceSpeed());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!this.tameable.getLeashed() && !this.tameable.isRiding()) {
                    if (this.tameable.getDistanceSq(this.owner) >= 144.0D) {
                        this.tryToTeleportNearEntity();
                    } else {
                        this.tameable.getNavigator().tryMoveToEntityLiving(this.owner, this.followSpeed);
                    }
                }
            }
        }

        private void tryToTeleportNearEntity() {
            BlockPos lvt_1_1_ = this.owner.getPosition();

            for (int lvt_2_1_ = 0; lvt_2_1_ < 10; ++lvt_2_1_) {
                int lvt_3_1_ = this.getRandomNumber(-3, 3);
                int lvt_4_1_ = this.getRandomNumber(-1, 1);
                int lvt_5_1_ = this.getRandomNumber(-3, 3);
                boolean lvt_6_1_ = this.tryToTeleportToLocation(lvt_1_1_.getX() + lvt_3_1_, lvt_1_1_.getY() + lvt_4_1_, lvt_1_1_.getZ() + lvt_5_1_);
                if (lvt_6_1_) {
                    return;
                }
            }
        }

        private boolean tryToTeleportToLocation(int p_226328_1_, int p_226328_2_, int p_226328_3_) {
            if (Math.abs((double) p_226328_1_ - this.owner.posX) < 2.0D && Math.abs((double) p_226328_3_ - this.owner.posZ) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(p_226328_1_, p_226328_2_, p_226328_3_))) {
                return false;
            } else {
                this.tameable.setPosition(p_226328_1_ + 0.5D, p_226328_2_, p_226328_3_ + 0.5D);
                this.tameable.getNavigator().clearPath();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos p_226329_1_) {
            PathNodeType lvt_2_1_ = new WalkNodeProcessor().getPathNodeType(this.world, p_226329_1_.getX(), p_226329_1_.getY(), p_226329_1_.getZ());
            if (lvt_2_1_ != PathNodeType.WALKABLE) {
                return false;
            } else {
                IBlockState lvt_3_1_ = this.world.getBlockState(p_226329_1_.down());
                if (!this.teleportToLeaves && lvt_3_1_.getBlock() instanceof BlockLeaves) {
                    return false;
                } else {
                    BlockPos offset = p_226329_1_.subtract(this.tameable.getPosition());
                    return this.tameable.world.getCollisionBoxes(this.tameable, this.tameable.getEntityBoundingBox().offset(offset.getX(), offset.getY(), offset.getZ())).isEmpty();
                }
            }
        }

        private int getRandomNumber(int p_226327_1_, int p_226327_2_) {
            return this.tameable.getRNG().nextInt(p_226327_2_ - p_226327_1_ + 1) + p_226327_1_;
        }
    }
}
