package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingEntityAITempt;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoMountPlayer;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;

public class EntityPotoo extends EntityAnimal implements IFalconry {

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PERCHING = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Optional<BlockPos>> PERCH_POS = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Byte> PERCH_DIRECTION = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> MOUTH_TICK = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TEMP_BRIGHTNESS = EntityDataManager.createKey(EntityPotoo.class, DataSerializers.VARINT);

    public final float[] ringBuffer = new float[64];
    public float prevFlyProgress;
    public float flyProgress;
    public float mouthProgress;
    public float prevMouthProgress;
    public float prevPerchProgress;
    public float perchProgress;
    public int ringBufferIndex = -1;
    private int lastScreamTimestamp;
    private int perchCooldown = 100;
    private boolean isLandNavigator;
    private int timeFlying;

    public EntityPotoo(World world) {
        super(world);
        this.setSize(0.6F, 0.8F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.FENCE, -1.0F);
        Arrays.fill(this.ringBuffer, 15F);
        switchNavigator(true);
    }

    public static boolean canPotooSpawnAt(World world, BlockPos pos) {
        return world.getLightFromNeighbors(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, false);
        this.dataManager.register(PERCHING, false);
        this.dataManager.register(PERCH_POS, Optional.absent());
        this.dataManager.register(PERCH_DIRECTION, (byte) EnumFacing.NORTH.getIndex());
        this.dataManager.register(SLEEPING, false);
        this.dataManager.register(MOUTH_TICK, 0);
        this.dataManager.register(TEMP_BRIGHTNESS, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new FlyingEntityAITempt(this, 1.0D, false, Collections.<net.minecraft.item.Item>emptySet()) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return EntityPotoo.this.isBreedingItem(stack);
            }
        });
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(4, new AIPerch());
        this.tasks.addTask(5, new AIMelee());
        this.tasks.addTask(6, new AIFlyIdle());
        this.targetTasks.addTask(1, new EntityAINearestTarget3D(this, EntityFly.class, 100, true, true, null));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new net.minecraft.entity.ai.EntityMoveHelper(this);
            this.navigator = new PathNavigateGround(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.6F, false, true);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!canPotooSpawnAt(this.world, this.getPosition())) {
            return false;
        }
        if (!this.world.isAirBlock(this.getPosition())) {
            return false;
        }
        IBlockState below = this.world.getBlockState(this.getPosition().down());
        Block block = below.getBlock();
        boolean validPerch = block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2
                || AMTagRegistry.blockInTag(AMTagRegistry.POTOO_PERCHES, block);
        return validPerch && AMEntityRegistry.rollSpawn(AMConfig.potooSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.POTOO_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.POTOO_HURT;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.POTOO;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityPotoo) AMEntityRegistry.POTOO.newInstance(this.world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return !stack.isEmpty() && AMTagRegistry.itemInTag(AMTagRegistry.POTOO_BREEDABLES, stack.getItem());
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, sleeping);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.isHeldOnGlove()) {
            this.potooTickLogic();
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isHeldOnGlove()) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            return;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    protected boolean isMovementBlocked() {
        return super.isMovementBlocked() || this.isHeldOnGlove();
    }

    @Override
    public boolean canBePushed() {
        return !this.isHeldOnGlove() && super.canBePushed();
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (this.isHeldOnGlove() && this.getRidingEntity() == entityIn) {
            return;
        }
        super.applyEntityCollision(entityIn);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    private boolean isHeldOnGlove() {
        return this.isRiding() && this.getRidingEntity() instanceof EntityPlayer;
    }

    @Override
    public boolean canBeCollidedWith() {
        return super.canBeCollidedWith() && !this.isHeldOnGlove();
    }

    @Override
    public void dismountRidingEntity() {
        super.dismountRidingEntity();
        this.noClip = false;
    }

    @Override
    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && (entity == null || !entity.isEntityAlive() || !this.isEntityAlive())) {
            this.dismountRidingEntity();
        } else if (entity instanceof EntityLivingBase) {
            this.noClip = true;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.setFlying(false);
            this.setSleeping(false);
            this.setPerching(false);
            this.getNavigator().clearPath();
            this.potooTickLogic();
            if (this.isRiding()) {
                Entity mount = this.getRidingEntity();
                if (mount instanceof EntityPlayer) {
                    float yawAdd = 0;
                    if (((EntityPlayer) mount).getHeldItem(EnumHand.MAIN_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                        yawAdd = ((EntityPlayer) mount).getPrimaryHand() == EnumHandSide.LEFT ? 135.0F : -135.0F;
                    } else if (((EntityPlayer) mount).getHeldItem(EnumHand.OFF_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                        yawAdd = ((EntityPlayer) mount).getPrimaryHand() == EnumHandSide.LEFT ? -135.0F : 135.0F;
                    } else {
                        this.dismountRidingEntity();
                        this.copyLocationAndAnglesFrom(mount);
                        return;
                    }
                    float birdYaw = yawAdd * 0.5F;
                    this.renderYawOffset = MathHelper.wrapDegrees(((EntityLivingBase) mount).renderYawOffset + birdYaw);
                    this.rotationYaw = MathHelper.wrapDegrees(((EntityLivingBase) mount).rotationYaw + birdYaw);
                    this.rotationYawHead = MathHelper.wrapDegrees(((EntityLivingBase) mount).rotationYawHead + birdYaw);
                    this.prevRotationYaw = this.rotationYaw;
                    this.prevRotationYawHead = this.rotationYawHead;
                    this.prevRenderYawOffset = this.renderYawOffset;
                    float radius = 0.6F;
                    float angle = (Maths.STARTING_ANGLE * (((EntityLivingBase) mount).renderYawOffset - 180.0F + yawAdd));
                    double extraX = (double) radius * MathHelper.sin((float) (Math.PI + (double) angle));
                    double extraZ = (double) radius * MathHelper.cos(angle);
                    double newX = mount.posX + extraX;
                    double newY = Math.max(mount.posY + (double) mount.height * 0.45D, mount.posY);
                    double newZ = mount.posZ + extraZ;
                    this.setPosition(newX, newY, newZ);
                    this.motionX = 0.0D;
                    this.motionY = 0.0D;
                    this.motionZ = 0.0D;
                }
                if (mount != null && !mount.isEntityAlive()) {
                    this.dismountRidingEntity();
                }
            }
        } else {
            super.updateRidden();
        }
    }

    private void potooTickLogic() {
        this.prevPerchProgress = perchProgress;
        this.prevMouthProgress = mouthProgress;
        this.prevFlyProgress = flyProgress;

        if (this.isFlying()) {
            if (flyProgress < 5F) {
                flyProgress++;
            }
        } else if (flyProgress > 0F) {
            flyProgress--;
        }

        if (this.isPerching()) {
            if (perchProgress < 5F) {
                perchProgress++;
            }
        } else if (perchProgress > 0F) {
            perchProgress--;
        }

        if (this.ringBufferIndex < 0) {
            Arrays.fill(this.ringBuffer, 15F);
        }
        this.ringBufferIndex++;
        if (this.ringBufferIndex == this.ringBuffer.length) {
            this.ringBufferIndex = 0;
        }
        this.ringBuffer[this.ringBufferIndex] = this.dataManager.get(TEMP_BRIGHTNESS);
        if (perchCooldown > 0) {
            perchCooldown--;
        }
        if (!this.world.isRemote) {
            this.dataManager.set(TEMP_BRIGHTNESS, this.world.getLightFromNeighbors(this.getPosition()));
            if (!this.isHeldOnGlove()) {
                if (isFlying()) {
                    if (this.isLandNavigator) {
                        switchNavigator(false);
                    }
                } else if (!this.isLandNavigator) {
                    switchNavigator(true);
                }

                if (this.isFlying()) {
                    if (!this.onGround) {
                        if (!this.isInWater()) {
                            this.motionY *= 0.6D;
                        }
                    } else if (timeFlying > 20) {
                        this.setFlying(false);
                    }
                    this.timeFlying++;
                } else {
                    this.timeFlying = 0;
                }
                if (this.isPerching() && !this.isBeingRidden()) {
                    EntityLivingBase target = this.getAttackTarget();
                    this.setSleeping(this.world.isDaytime() && (target == null || !target.isEntityAlive()));
                } else if (isSleeping()) {
                    this.setSleeping(false);
                }
                if (isPerching() && this.getPerchPos() != null) {
                    Vec3d perchCenter = new Vec3d(this.getPerchPos().getX() + 0.5D, this.getPerchPos().getY() + 0.5D, this.getPerchPos().getZ() + 0.5D);
                    if (!AMTagRegistry.blockInTag(AMTagRegistry.POTOO_PERCHES, this.world.getBlockState(this.getPerchPos()).getBlock())
                            || this.getDistanceSq(perchCenter.x, perchCenter.y, perchCenter.z) > 2.25D) {
                        this.setPerching(false);
                    } else {
                        slideTowardsPerch();
                    }
                }
            }
        }
        if (this.dataManager.get(MOUTH_TICK) > 0) {
            this.dataManager.set(MOUTH_TICK, this.dataManager.get(MOUTH_TICK) - 1);
            if (mouthProgress < 5F) {
                mouthProgress++;
            }
        } else if (mouthProgress > 0F) {
            mouthProgress--;
        }
        EntityLivingBase screamTarget = this.getAttackTarget();
        if (!isSleeping() && (screamTarget == null || !screamTarget.isEntityAlive())) {
            int j = this.ticksExisted - lastScreamTimestamp;
            if (getEyeScale(10, 1.0F) == 0F) {
                if (j > 40) {
                    this.openMouth(30);
                    this.playPotooCall();
                }
            } else if (getEyeScale(10, 1.0F) < 7F) {
                if (j > 300 && j % 300 == 0 && this.rand.nextInt(4) == 0) {
                    this.openMouth(30);
                    this.playPotooCall();
                }
            }
        }
    }

    @Override
    public float getHandOffset() {
        return 1.0F;
    }

    private void playPotooCall() {
        if (this.world.isRemote) {
            return;
        }
        Entity src = this.getRidingEntity() instanceof EntityPlayer ? this.getRidingEntity() : this;
        this.world.playSound(null, src.posX, src.posY, src.posZ, AMSoundRegistry.POTOO_CALL, SoundCategory.NEUTRAL, 1.0F, this.getSoundPitch());
    }

    @Override
    public void onLaunch(EntityPlayer player, Entity pointedEntity) {
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getImmediateSource() instanceof EntityLivingBase) {
            this.setPerching(false);
        }
        return prev;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    public void openMouth(int duration) {
        this.dataManager.set(MOUTH_TICK, duration);
        lastScreamTimestamp = this.ticksExisted;
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if (flying && isChild()) {
            return;
        }
        this.dataManager.set(FLYING, flying);
    }

    public BlockPos getPerchPos() {
        return this.dataManager.get(PERCH_POS).orNull();
    }

    public void setPerchPos(BlockPos pos) {
        this.dataManager.set(PERCH_POS, Optional.fromNullable(pos));
    }

    public EnumFacing getPerchDirection() {
        return EnumFacing.getFront(this.dataManager.get(PERCH_DIRECTION) & 7);
    }

    public void setPerchDirection(EnumFacing direction) {
        this.dataManager.set(PERCH_DIRECTION, (byte) direction.getIndex());
    }

    public boolean isPerching() {
        return this.dataManager.get(PERCHING);
    }

    public void setPerching(boolean perching) {
        this.dataManager.set(PERCHING, perching);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Flying", this.isFlying());
        compound.setBoolean("Perching", this.isPerching());
        compound.setInteger("PerchDir", this.getPerchDirection().getIndex());
        if (this.getPerchPos() != null) {
            compound.setInteger("PerchX", this.getPerchPos().getX());
            compound.setInteger("PerchY", this.getPerchPos().getY());
            compound.setInteger("PerchZ", this.getPerchPos().getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFlying(compound.getBoolean("Flying"));
        this.setPerching(compound.getBoolean("Perching"));
        this.setPerchDirection(EnumFacing.getFront(compound.getInteger("PerchDir")));
        if (compound.hasKey("PerchX") && compound.hasKey("PerchY") && compound.hasKey("PerchZ")) {
            this.setPerchPos(new BlockPos(compound.getInteger("PerchX"), compound.getInteger("PerchY"), compound.getInteger("PerchZ")));
        }
    }

    public boolean isValidPerchFromSide(BlockPos pos, EnumFacing direction) {
        IBlockState state = world.getBlockState(pos);
        if (!AMTagRegistry.blockInTag(AMTagRegistry.POTOO_PERCHES, state.getBlock())) {
            return false;
        }
        BlockPos above = pos.up();
        IBlockState aboveState = world.getBlockState(above);
        if (aboveState.isFullCube() && !world.isAirBlock(above)) {
            return false;
        }
        BlockPos offset = pos.offset(direction);
        IBlockState offsetState = world.getBlockState(offset);
        boolean offsetBlocked = (offsetState.isFullCube() || AMTagRegistry.blockInTag(AMTagRegistry.POTOO_PERCHES, offsetState.getBlock())) && !world.isAirBlock(offset);
        return !offsetBlocked;
    }

    public float getEyeScale(int bufferOffset, float partialTicks) {
        int i = (this.ringBufferIndex - bufferOffset) & 63;
        int j = (this.ringBufferIndex - bufferOffset - 1) & 63;
        float prevBuffer = this.ringBuffer[j];
        float buffer = this.ringBuffer[i];
        return prevBuffer + (buffer - prevBuffer) * partialTicks;
    }

    private void slideTowardsPerch() {
        if (this.getPerchPos() == null) {
            return;
        }
        Vec3d block = new Vec3d(this.getPerchPos().getX() + 0.5D, this.getPerchPos().getY() + 1.0D, this.getPerchPos().getZ() + 0.5D);
        Vec3d look = block.subtract(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
        Vec3d onBlock = block.addVector(this.getPerchDirection().getFrontOffsetX() * 0.35F, 0.0D, this.getPerchDirection().getFrontOffsetZ() * 0.35F);
        Vec3d diff = onBlock.subtract(new Vec3d(this.posX, this.posY, this.posZ));
        float f = (float) diff.lengthVector();
        float f1 = f > 1F ? 0.25F : f * 0.1F;
        Vec3d sub = diff.normalize().scale(f1);
        float f2 = -(float) (MathHelper.atan2(look.x, look.z) * (180D / Math.PI));
        this.rotationYaw = f2;
        this.rotationYawHead = f2;
        this.renderYawOffset = f2;
        this.motionX += sub.x;
        this.motionY += sub.y;
        this.motionZ += sub.z;
    }

    private BlockPos getBlockBelow() {
        return new BlockPos(this.posX, this.getEntityBoundingBox().minY - 0.5000001D, this.posZ);
    }

    public BlockPos getPotooGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() < 256 && world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.up();
        }
        while (position.getY() > 0 && !world.getBlockState(position).getMaterial().isSolid() && !world.getBlockState(position).getMaterial().isLiquid()) {
            position = position.down();
        }
        return position;
    }

    public Vec3d getBlockGrounding(Vec3d fleePos) {
        final float radius = 10 + this.getRNG().nextInt(15);
        final float neg = this.getRNG().nextBoolean() ? 1 : -1;
        final float renderYawOffset = this.renderYawOffset;
        final float angle = (Maths.STARTING_ANGLE * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        final double extraX = radius * MathHelper.sin((float) Math.PI + angle);
        final double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, this.posY, fleePos.z + extraZ);
        BlockPos ground = this.getPotooGround(radialPos);
        if (ground.getY() <= 0) {
            return null;
        } else {
            ground = this.getPosition();
            while (ground.getY() > 0 && !world.getBlockState(ground).getMaterial().isSolid()) {
                ground = ground.down();
            }
        }
        Vec3d above = new Vec3d(ground.getX() + 0.5D, ground.getY() + 1.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(above)) {
            return new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        }
        return null;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        boolean handled = super.processInteract(player, hand);
        if (!this.isChild() && getRidingFalcons(player) <= 0
                && (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE
                || player.getHeldItem(EnumHand.OFF_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE)) {
            this.removePassengers();
            this.setFlying(false);
            this.setPerching(false);
            this.setSleeping(false);
            this.getNavigator().clearPath();
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.noClip = true;
            this.startRiding(player, true);
            if (!this.world.isRemote) {
                AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(this.getEntityId(), player.getEntityId()));
            }
            return true;
        }
        return handled;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        final float radius = 5 + radiusAdd + this.getRNG().nextInt(5);
        final float neg = this.getRNG().nextBoolean() ? 1 : -1;
        final float renderYawOffset = this.renderYawOffset;
        final float angle = (Maths.STARTING_ANGLE * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        final double extraX = radius * MathHelper.sin((float) Math.PI + angle);
        final double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getPotooGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 5 + this.getRNG().nextInt(5);
        int j = this.getRNG().nextInt(5) + 5;
        BlockPos newPos = ground.up(distFromGround > 5 ? flightHeight : j);
        if (world.getBlockState(ground).getBlock() instanceof BlockLeaves) {
            newPos = ground.up(1 + this.getRNG().nextInt(3));
        }
        Vec3d center = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(center) && this.getDistanceSq(center.x, center.y, center.z) > 1.0D) {
            return center;
        }
        return null;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
        this.fallDistance = 0.0F;
    }

    @Override
    public boolean canTrample(World world, Block block, BlockPos pos, float fallDistance) {
        return false;
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 0 && world.isAirBlock(position)) {
            position = position.down();
        }
        return world.getBlockState(position).getMaterial().isLiquid() || world.getBlockState(position).getBlock() == Blocks.VINE || position.getY() <= 0;
    }

    private class AIFlyIdle extends EntityAIBase {
        protected double x;
        protected double y;
        protected double z;

        public AIFlyIdle() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityPotoo.this.isBeingRidden() || EntityPotoo.this.isPerching() || EntityPotoo.this.isRiding()
                    || (EntityPotoo.this.getAttackTarget() != null && EntityPotoo.this.getAttackTarget().isEntityAlive())) {
                return false;
            }
            if (EntityPotoo.this.getRNG().nextInt(45) != 0 && !EntityPotoo.this.isFlying()) {
                return false;
            }
            Vec3d dest = this.getPosition();
            if (dest == null) {
                return false;
            }
            this.x = dest.x;
            this.y = dest.y;
            this.z = dest.z;
            return true;
        }

        @Override
        public void updateTask() {
            EntityPotoo.this.getMoveHelper().setMoveTo(this.x, this.y, this.z, 1.0D);
            if (isFlying() && EntityPotoo.this.onGround && EntityPotoo.this.timeFlying > 10) {
                EntityPotoo.this.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = EntityPotoo.this.getPositionVector();
            if (EntityPotoo.this.timeFlying < 200 || EntityPotoo.this.isOverWaterOrVoid()) {
                return EntityPotoo.this.getBlockInViewAway(vector3d, 0);
            }
            return EntityPotoo.this.getBlockGrounding(vector3d);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return EntityPotoo.this.isFlying() && EntityPotoo.this.getDistanceSq(x, y, z) > 5.0D;
        }

        @Override
        public void startExecuting() {
            EntityPotoo.this.setFlying(true);
            EntityPotoo.this.getMoveHelper().setMoveTo(this.x, this.y, this.z, 1.0D);
        }

        @Override
        public void resetTask() {
            EntityPotoo.this.getNavigator().clearPath();
            x = 0;
            y = 0;
            z = 0;
        }
    }

    private class AIPerch extends EntityAIBase {
        private BlockPos perch = null;
        private EnumFacing perchDirection = null;
        private int perchingTime = 0;
        private int runCooldown = 0;
        private int pathRecalcTime = 0;

        public AIPerch() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityPotoo.this.getAttackTarget() != null && EntityPotoo.this.getAttackTarget().isEntityAlive()) {
                return false;
            }
            if (runCooldown > 0) {
                runCooldown--;
                return false;
            }
            if (!EntityPotoo.this.isPerching() && EntityPotoo.this.perchCooldown == 0 && EntityPotoo.this.rand.nextInt(35) == 0) {
                this.perchingTime = 0;
                if (EntityPotoo.this.getPerchPos() != null && EntityPotoo.this.isValidPerchFromSide(EntityPotoo.this.getPerchPos(), EntityPotoo.this.getPerchDirection())) {
                    perch = EntityPotoo.this.getPerchPos();
                    perchDirection = EntityPotoo.this.getPerchDirection();
                } else {
                    findPerch();
                }
                runCooldown = 120 + EntityPotoo.this.getRNG().nextInt(140);
                return perch != null && perchDirection != null;
            }
            return false;
        }

        private void findPerch() {
            EnumFacing[] horiz = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
            BlockPos below = EntityPotoo.this.getBlockBelow();
            if (isValidPerchFromSide(below, EntityPotoo.this.getHorizontalFacing())) {
                perch = below;
                perchDirection = EntityPotoo.this.getHorizontalFacing();
                return;
            }
            for (EnumFacing dir : horiz) {
                if (isValidPerchFromSide(below, dir)) {
                    perch = below;
                    perchDirection = dir;
                    return;
                }
            }
            int range = 14;
            for (int i = 0; i < 15; i++) {
                BlockPos blockpos1 = EntityPotoo.this.getPosition().add(EntityPotoo.this.rand.nextInt(range) - range / 2, 3, EntityPotoo.this.rand.nextInt(range) - range / 2);
                if (!EntityPotoo.this.world.isBlockLoaded(blockpos1)) {
                    continue;
                }
                while (EntityPotoo.this.world.isAirBlock(blockpos1) && blockpos1.getY() > 0) {
                    blockpos1 = blockpos1.down();
                }
                EnumFacing dir = EnumFacing.getHorizontal(EntityPotoo.this.rand.nextInt(4));
                if (isValidPerchFromSide(blockpos1, dir)) {
                    perch = blockpos1;
                    perchDirection = dir;
                    break;
                }
            }
        }

        @Override
        public void startExecuting() {
            pathRecalcTime = 0;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return (perchingTime < 300 || EntityPotoo.this.world.isDaytime())
                    && (EntityPotoo.this.getAttackTarget() == null || !EntityPotoo.this.getAttackTarget().isEntityAlive())
                    && !EntityPotoo.this.isRiding();
        }

        @Override
        public void updateTask() {
            if (EntityPotoo.this.isPerching()) {
                perchingTime++;
                EntityPotoo.this.getNavigator().clearPath();
                Vec3d block = new Vec3d(EntityPotoo.this.getPerchPos().getX() + 0.5D, EntityPotoo.this.getPerchPos().getY() + 1.0D, EntityPotoo.this.getPerchPos().getZ() + 0.5D);
                Vec3d onBlock = block.addVector(EntityPotoo.this.getPerchDirection().getFrontOffsetX() * 0.35F, 0.0D, EntityPotoo.this.getPerchDirection().getFrontOffsetZ() * 0.35F);
                final double dist = EntityPotoo.this.getDistanceSq(onBlock.x, onBlock.y, onBlock.z);
                Vec3d dirVec = block.subtract(new Vec3d(EntityPotoo.this.posX, EntityPotoo.this.posY, EntityPotoo.this.posZ));
                if (perchingTime > 10 && (dist > 2.3D || !EntityPotoo.this.isValidPerchFromSide(EntityPotoo.this.getPerchPos(), EntityPotoo.this.getPerchDirection()))) {
                    EntityPotoo.this.setPerching(false);
                } else if (dist > 1.0D) {
                    EntityPotoo.this.slideTowardsPerch();
                    if (EntityPotoo.this.getPerchPos().getY() + 1.2F > EntityPotoo.this.getEntityBoundingBox().minY) {
                        EntityPotoo.this.motionY += 0.2D;
                    }
                    final float f = -(float) (MathHelper.atan2(dirVec.x, dirVec.z) * (180D / Math.PI));
                    EntityPotoo.this.rotationYaw = f;
                    EntityPotoo.this.rotationYawHead = f;
                    EntityPotoo.this.renderYawOffset = f;
                }
            } else if (perch != null) {
                Vec3d perchCenter = new Vec3d(perch.getX() + 0.5D, perch.getY() + 0.5D, perch.getZ() + 0.5D);
                if (EntityPotoo.this.getDistanceSq(perchCenter.x, perchCenter.y, perchCenter.z) > 100.0D) {
                    EntityPotoo.this.setFlying(true);
                }
                final double distX = perch.getX() + 0.5F - EntityPotoo.this.posX;
                final double distZ = perch.getZ() + 0.5F - EntityPotoo.this.posZ;
                if (distX * distX + distZ * distZ < 1.0D || !EntityPotoo.this.isFlying()) {
                    if (pathRecalcTime <= 0) {
                        pathRecalcTime = EntityPotoo.this.getRNG().nextInt(30) + 30;
                        EntityPotoo.this.getNavigator().tryMoveToXYZ(perch.getX() + 0.5D, perch.getY() + 1.5D, perch.getZ() + 0.5D, 1.0D);
                    }
                    if (EntityPotoo.this.getNavigator().noPath()) {
                        EntityPotoo.this.getMoveHelper().setMoveTo(perch.getX() + 0.5D, perch.getY() + 1.5D, perch.getZ() + 0.5D, 1.0D);
                    }
                } else if (pathRecalcTime <= 0) {
                    pathRecalcTime = EntityPotoo.this.getRNG().nextInt(30) + 30;
                    EntityPotoo.this.getNavigator().tryMoveToXYZ(perch.getX() + 0.5D, perch.getY() + 2.5D, perch.getZ() + 0.5D, 1.0D);
                }
                if (EntityPotoo.this.getBlockBelow().equals(perch)) {
                    EntityPotoo.this.motionX = 0.0D;
                    EntityPotoo.this.motionY = 0.0D;
                    EntityPotoo.this.motionZ = 0.0D;
                    EntityPotoo.this.setPerching(true);
                    EntityPotoo.this.setFlying(false);
                    EntityPotoo.this.setPerchPos(perch);
                    EntityPotoo.this.setPerchDirection(perchDirection);
                    EntityPotoo.this.getNavigator().clearPath();
                    perch = null;
                } else {
                    EntityPotoo.this.setPerching(false);
                }
            }
            if (pathRecalcTime > 0) {
                pathRecalcTime--;
            }
        }

        @Override
        public void resetTask() {
            EntityPotoo.this.setPerching(false);
            EntityPotoo.this.perchCooldown = 120 + EntityPotoo.this.rand.nextInt(1200);
            this.perch = null;
            this.perchDirection = null;
        }
    }

    private class AIMelee extends EntityAIBase {
        private int biteCooldown = 0;

        public AIMelee() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return !EntityPotoo.this.isSleeping() && !EntityPotoo.this.isRiding()
                    && EntityPotoo.this.getAttackTarget() != null && EntityPotoo.this.getAttackTarget().isEntityAlive();
        }

        @Override
        public void updateTask() {
            if (biteCooldown > 0) {
                biteCooldown--;
            }
            EntityLivingBase entity = EntityPotoo.this.getAttackTarget();
            if (entity != null) {
                EntityPotoo.this.setFlying(true);
                EntityPotoo.this.setPerching(false);
                EntityPotoo.this.getMoveHelper().setMoveTo(entity.posX, entity.posY + entity.height * 0.5D, entity.posZ, 1.5D);
                if (EntityPotoo.this.getDistance(entity) < 1.4F) {
                    if (biteCooldown == 0) {
                        EntityPotoo.this.openMouth(7);
                        biteCooldown = 10;
                    }
                    if (EntityPotoo.this.mouthProgress >= 4.5F) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(EntityPotoo.this), 2);
                        if (entity.width <= 0.5F) {
                            entity.setDead();
                        }
                    }
                }
            }
        }
    }
}
