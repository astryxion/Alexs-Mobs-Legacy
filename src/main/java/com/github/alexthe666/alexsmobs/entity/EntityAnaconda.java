package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.entity.util.AnacondaPartIndex;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;
import java.util.Random;
import java.util.UUID;

public class EntityAnaconda extends EntityAnimal implements ISemiAquatic {

    private static final float STARTING_ANGLE = 0.0174532925F;
    private static final DataParameter<Optional<UUID>> CHILD_UUID = EntityDataManager.createKey(EntityAnaconda.class, DataSerializers.OPTIONAL_UNIQUE_ID);
    private static final DataParameter<Integer> CHILD_ID = EntityDataManager.createKey(EntityAnaconda.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> STRANGLING = EntityDataManager.createKey(EntityAnaconda.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> YELLOW = EntityDataManager.createKey(EntityAnaconda.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SHEDTIME = EntityDataManager.createKey(EntityAnaconda.class, DataSerializers.VARINT);
    public final float[] ringBuffer = new float[64];
    public int ringBufferIndex = -1;
    private EntityAnacondaPart[] parts;
    private float prevStrangleProgress = 0F;
    private float strangleProgress = 0F;
    private int strangleTimer = 0;
    private int shedCooldown = 0;
    private int feedings = 0;
    private boolean isLandNavigator;
    private int swimTimer = -1000;
    private int passiveFor = 0;

    public EntityAnaconda(World world) {
        super(world);
        this.setSize(0.8F, 0.8F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(true);
    }

    public static boolean canAnacondaSpawn(IBlockAccess worldIn, BlockPos pos) {
        if (!(worldIn instanceof World)) {
            return false;
        }
        World world = (World) worldIn;
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.ANACONDA_SPAWNS, worldIn.getBlockState(pos.down()).getBlock());
        return spawnBlock && pos.getY() < world.getSeaLevel() + 4;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.anacondaSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AnimalSwimMoveControllerSink(this, 1.3F, 1F);
            this.navigator = new SemiAquaticPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15D);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ANACONDA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ANACONDA_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        if (!isChild()) {
            this.playSound(AMSoundRegistry.ANACONDA_SLITHER, 1.0F, 1.0F);
        } else {
            super.playStepSound(pos, blockIn);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(2, new AIMelee());
        this.tasks.addTask(3, new AnimalAIFindWater(this));
        this.tasks.addTask(3, new AnimalAILeaveWater(this));
        this.tasks.addTask(4, new EntityAITempt(this, 1.25D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.ANACONDA_FOODSTUFFS, stack.getItem());
            }
        });
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 60, 1.0D, 14, 7));
        this.tasks.addTask(8, new SemiAquaticAIRandomSwimming(this, 1.5D, 7));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 25F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        Predicate<EntityLivingBase> anacondaTargetPredicate = e -> {
            if (!e.isEntityAlive() || !AMTagRegistry.entityMatchesEntityTypeTag(AMTagRegistry.ANACONDA_TARGETS, e)) {
                return false;
            }
            if (passiveFor > 0 && e instanceof EntityPlayer) {
                EntityLivingBase last = EntityAnaconda.this.getRevengeTarget();
                return last != null && last.getUniqueID().equals(e.getUniqueID());
            }
            return true;
        };
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class, 200, false, false, anacondaTargetPredicate));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, 110, false, true, null) {
            @Override
            public boolean shouldExecute() {
                return !isChild() && passiveFor == 0 && !world.getDifficulty().equals(EnumDifficulty.PEACEFUL) && !EntityAnaconda.this.isInLove() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
    }

    @Override
    public float getEyeHeight() {
        return this.isChild() ? 0.15F : 0.3F;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (isBreedingItem(itemstack)) {
            this.setAttackTarget(null);
            this.passiveFor = 3600 + rand.nextInt(3600);
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (this.getChildId() != null) {
            compound.setUniqueId("ChildUUID", this.getChildId());
        }
        compound.setInteger("Feedings", feedings);
        compound.setInteger("ShedTime", getSheddingTime());
        compound.setBoolean("Yellow", isYellow());
        compound.setInteger("ShedCooldown", shedCooldown);
        compound.setInteger("PassiveFor", passiveFor);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasUniqueId("ChildUUID")) {
            this.setChildId(compound.getUniqueId("ChildUUID"));
        }
        feedings = compound.getInteger("Feedings");
        this.setSheddingTime(compound.getInteger("ShedTime"));
        this.setYellow(compound.getBoolean("Yellow"));
        shedCooldown = compound.getInteger("ShedCooldown");
        passiveFor = compound.getInteger("PassiveFor");
    }

    public void pushEntities() {
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().grow(0.2D, 0.0D, 0.2D));
        for (Entity entity : entities) {
            if (!(entity instanceof EntityAnacondaPart) && entity.canBePushed()) {
                entity.applyEntityCollision(this);
            }
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        super.applyEntityCollision(entityIn);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CHILD_UUID, Optional.absent());
        this.dataManager.register(CHILD_ID, -1);
        this.dataManager.register(STRANGLING, false);
        this.dataManager.register(YELLOW, false);
        this.dataManager.register(SHEDTIME, 0);
    }

    @Nullable
    public UUID getChildId() {
        return this.dataManager.get(CHILD_UUID).orNull();
    }

    public void setChildId(@Nullable UUID uniqueId) {
        this.dataManager.set(CHILD_UUID, Optional.fromNullable(uniqueId));
    }

    public int getSheddingTime() {
        return this.dataManager.get(SHEDTIME);
    }

    public void setSheddingTime(int shedtime) {
        this.dataManager.set(SHEDTIME, shedtime);
    }

    public boolean isStrangling() {
        return this.dataManager.get(STRANGLING);
    }

    public void setStrangling(boolean running) {
        this.dataManager.set(STRANGLING, running);
    }

    public boolean isYellow() {
        return this.dataManager.get(YELLOW);
    }

    public void setYellow(boolean yellow) {
        this.dataManager.set(YELLOW, yellow);
    }

    @Override
    public int getVerticalFaceSpeed() {
        return 1;
    }

    @Override
    public int getHorizontalFaceSpeed() {
        return 3;
    }

    @Nullable
    public Entity getChild() {
        UUID id = getChildId();
        if (id != null && !world.isRemote && world instanceof WorldServer) {
            return ((WorldServer) world).getEntityFromUuid(id);
        }
        return null;
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
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.passiveFor > 0) {
            passiveFor--;
        }
        if (this.isInWater()) {
            if (this.isLandNavigator) {
                switchNavigator(false);
            }
        } else {
            if (!this.isLandNavigator) {
                switchNavigator(true);
            }
        }

        this.prevStrangleProgress = strangleProgress;
        if (this.isStrangling()) {
            if (strangleProgress < 5F) {
                strangleProgress++;
            }
        } else {
            if (strangleProgress > 0F) {
                strangleProgress--;
            }
        }

        this.renderYawOffset = this.rotationYaw;
        this.rotationYawHead = MathHelper.clamp(this.rotationYawHead, this.renderYawOffset - 70, this.renderYawOffset + 70);

        if (this.isStrangling()) {
            if (!world.isRemote && this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
                this.rotationPitch = 0;
                EntityLivingBase target = this.getAttackTarget();
                float radius = target.width * -0.5F;
                float angle = (STARTING_ANGLE * (target.renderYawOffset - 45F));
                double extraX = radius * MathHelper.sin((float) Math.PI + angle);
                double extraZ = radius * MathHelper.cos(angle);
                Vec3d targetVec = new Vec3d(extraX + target.posX, target.posY + target.getEyeHeight(), extraZ + target.posZ);
                Vec3d moveVec = targetVec.subtract(this.getPositionVector()).scale(1F);
                this.motionX = moveVec.x;
                this.motionY = moveVec.y;
                this.motionZ = moveVec.z;
                if (!target.onGround) {
                    target.motionY = -0.08F;
                    target.motionX = 0;
                    target.motionZ = 0;
                } else {
                    target.motionX = 0;
                    target.motionY = 0;
                    target.motionZ = 0;
                }
                if (strangleTimer >= 40 && strangleTimer % 20 == 0) {
                    double health = MathHelper.clamp(target.getMaxHealth(), 4, 50);
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) Math.max(4F, 0.25F * health));
                }
                if (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive()) {
                    strangleTimer = 0;
                    this.setStrangling(false);
                }
            }
            this.fallDistance = 0;
            strangleTimer++;
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
        }
        if (this.ringBufferIndex < 0) {
            for (int i = 0; i < this.ringBuffer.length; ++i) {
                this.ringBuffer[i] = this.rotationYaw;
            }
        }
        this.ringBufferIndex++;
        if (this.ringBufferIndex == this.ringBuffer.length) {
            this.ringBufferIndex = 0;
        }
        this.ringBuffer[this.ringBufferIndex] = this.rotationYaw;

        if (!this.world.isRemote) {
            final int segments = 7;
            Entity child = getChild();
            if (child == null) {
                EntityLivingBase partParent = this;
                parts = new EntityAnacondaPart[segments];
                AnacondaPartIndex partIndex = AnacondaPartIndex.HEAD;
                Vec3d prevPos = this.getPositionVector();
                for (int i = 0; i < segments; i++) {
                    float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                    float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                    EntityAnacondaPart part = new EntityAnacondaPart(this.world, partParent);
                    part.setParent(partParent);
                    part.copyDataFrom(this);
                    part.setBodyIndex(i);
                    part.setPartType(AnacondaPartIndex.sizeAt(1 + i));
                    if (partParent == this) {
                        this.setChildId(part.getUniqueID());
                        this.dataManager.set(CHILD_ID, part.getEntityId());
                    }
                    if (partParent instanceof EntityAnacondaPart) {
                        ((EntityAnacondaPart) partParent).setChildId(part.getUniqueID());
                    }
                    part.setPosition(part.tickMultipartPosition(this.getEntityId(), partIndex, prevPos, this.rotationPitch, prevReqRot, reqRot, false).x,
                            part.tickMultipartPosition(this.getEntityId(), partIndex, prevPos, this.rotationPitch, prevReqRot, reqRot, false).y,
                            part.tickMultipartPosition(this.getEntityId(), partIndex, prevPos, this.rotationPitch, prevReqRot, reqRot, false).z);
                    partParent = part;
                    world.spawnEntity(part);
                    parts[i] = part;
                    partIndex = part.getPartType();
                    prevPos = part.getPositionVector();
                }
            }
            if (shouldReplaceParts() && this.getChild() instanceof EntityAnacondaPart) {
                parts = new EntityAnacondaPart[segments];
                parts[0] = (EntityAnacondaPart) this.getChild();
                this.dataManager.set(CHILD_ID, parts[0].getEntityId());
                int i = 1;
                while (i < parts.length && parts[i - 1].getChild() instanceof EntityAnacondaPart) {
                    parts[i] = (EntityAnacondaPart) parts[i - 1].getChild();
                    i++;
                }
            }
            AnacondaPartIndex partIndex = AnacondaPartIndex.HEAD;
            Vec3d prev = this.getPositionVector();
            float xRot = this.rotationPitch;
            for (int i = 0; i < segments; i++) {
                if (this.parts != null && this.parts[i] != null) {
                    float prevReqRot = calcPartRotation(i) + getYawForPart(i);
                    float reqRot = calcPartRotation(i + 1) + getYawForPart(i);
                    parts[i].setStrangleProgress(this.strangleProgress);
                    parts[i].copyDataFrom(this);
                    prev = parts[i].tickMultipartPosition(this.getEntityId(), partIndex, prev, xRot, prevReqRot, reqRot, true);
                    partIndex = parts[i].getPartType();
                    xRot = parts[i].rotationPitch;
                }
            }

            if (isInWater()) {
                swimTimer = Math.max(swimTimer + 1, 0);
            } else {
                swimTimer = Math.min(swimTimer - 1, 0);
            }
        }
        if (shedCooldown > 0) {
            shedCooldown--;
        }
        if (this.getSheddingTime() > 0) {
            this.setSheddingTime(this.getSheddingTime() - 1);
            if (this.getSheddingTime() == 0) {
                this.spawnItemAtOffset(new ItemStack(AMItemRegistry.SHED_SNAKE_SKIN), 1 + rand.nextFloat(), 0.2F);
                shedCooldown = 1000 + rand.nextInt(2000);
            }
        }
        if (!this.world.isRemote) {
            this.pushEntities();
        }
    }

    private boolean shouldReplaceParts() {
        if (parts == null || parts[0] == null) {
            return true;
        }

        for (int i = 0; i < 7; i++) {
            if (parts[i] == null) {
                return true;
            }
        }

        return false;
    }

    private float getYawForPart(int i) {
        return this.getRingBuffer(4 + i * 2, 1.0F);
    }

    public float getRingBuffer(int bufferOffset, float partialTicks) {
        if (!this.isEntityAlive()) {
            partialTicks = 0.0F;
        }

        partialTicks = 1.0F - partialTicks;
        int i = this.ringBufferIndex - bufferOffset & 63;
        int j = this.ringBufferIndex - bufferOffset - 1 & 63;
        float d0 = this.ringBuffer[i];
        float d1 = this.ringBuffer[j] - d0;
        return MathHelper.wrapDegrees(d0 + d1 * partialTicks);
    }

    public float getScale() {
        return this.isChild() ? 0.75F : 1.0F;
    }

    @Override
    public boolean canBePushed() {
        return !this.isStrangling();
    }

    public boolean shouldMove() {
        return !this.isStrangling();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.ANACONDA_FOODSTUFFS, stack.getItem());
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.shouldMove()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            super.travel(0, 0, 0);
            return;
        }
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

    public float getStrangleProgress(float partialTick) {
        return this.prevStrangleProgress + (this.strangleProgress - this.prevStrangleProgress) * partialTick;
    }

    private float calcPartRotation(int i) {
        float f = 1 - (this.strangleProgress * 0.2F);
        float strangleIntensity = (float) (MathHelper.clamp(strangleTimer * 3, 0, 100F) * (1.0F + 0.2F * Math.sin(0.15F * strangleTimer)));
        return (float) (40 * -Math.sin(this.distanceWalkedOnStepModified * 3 - (i))) * f + this.strangleProgress * 0.2F * i * strangleIntensity;
    }

    @Nullable
    public EntityItem spawnItemAtOffset(ItemStack stack, float f, float f1) {
        if (stack.isEmpty()) {
            return null;
        } else if (this.world.isRemote) {
            return null;
        } else {
            Vec3d vec = new Vec3d(0, 0, f).rotateYaw(-f * ((float) Math.PI / 180F));
            EntityItem itementity = new EntityItem(this.world, this.posX + vec.x, this.posY + (double) f1, this.posZ + vec.z, stack);
            itementity.setDefaultPickupDelay();
            this.world.spawnEntity(itementity);
            return itementity;
        }
    }

    @Override
    public boolean shouldEnterWater() {
        return this.getAttackTarget() == null && !shouldLeaveWater() && swimTimer <= -1000;
    }

    @Override
    public boolean shouldLeaveWater() {
        if (!this.getPassengers().isEmpty()) {
            return false;
        }

        if (this.getAttackTarget() != null && !this.getAttackTarget().isInWater()) {
            return true;
        }

        return swimTimer > 600 || this.isShedding();
    }

    @Override
    public boolean shouldStopMoving() {
        return !this.shouldMove();
    }

    @Override
    public int getWaterSearchRange() {
        return 12;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityAnaconda anaconda = new EntityAnaconda(this.world);
        anaconda.setYellow(this.isYellow());
        return anaconda;
    }

    @Override
    public void onKillEntity(EntityLivingBase entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = entity;
            try {
                ReflectionHelper.findField(net.minecraft.entity.EntityLiving.class, "deathLootTable", "field_184659_bA")
                        .set(living, null);
            } catch (Exception ignored) {
            }

            if (this.getChild() instanceof EntityAnacondaPart) {
                ((EntityAnacondaPart) this.getChild()).setSwell(5);
            }
        }
        super.onKillEntity(entity);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    public void feed() {
        this.heal(10);
        this.feedings++;
        if (feedings >= 3 && feedings % 3 == 0 && shedCooldown <= 0) {
            this.setSheddingTime(this.getRNG().nextInt(500) + 500);
        }
    }

    public boolean isShedding() {
        return this.getSheddingTime() > 0;
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setYellow(rand.nextBoolean());
        return super.onInitialSpawn(difficulty, livingdata);
    }

    private class AIMelee extends EntityAIBase {
        private final EntityAnaconda snake;
        private int jumpAttemptCooldown = 0;

        AIMelee() {
            this.setMutexBits(3);
            snake = EntityAnaconda.this;
        }

        @Override
        public boolean shouldExecute() {
            return snake.getAttackTarget() != null && snake.getAttackTarget().isEntityAlive();
        }

        @Override
        public void updateTask() {
            if (jumpAttemptCooldown > 0) {
                jumpAttemptCooldown--;
            }

            EntityLivingBase target = snake.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
                if (jumpAttemptCooldown == 0 && snake.getDistance(target) < 1 + target.width && !snake.isStrangling()) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(snake), 4);
                    snake.setStrangling(target.width <= 2.0F && !(target instanceof EntityAnaconda));
                    snake.playSound(AMSoundRegistry.ANACONDA_ATTACK, snake.getSoundVolume(), snake.getSoundPitch());
                    jumpAttemptCooldown = 5 + rand.nextInt(5);
                }
                if (snake.isStrangling()) {
                    snake.getNavigator().clearPath();
                } else {
                    try {
                        snake.getNavigator().tryMoveToEntityLiving(target, 1.3D);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void resetTask() {
            snake.setStrangling(false);
        }
    }
}
