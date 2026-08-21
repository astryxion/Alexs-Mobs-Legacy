package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoMountPlayer;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.FlightMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingAIFollowOwner;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.effect.EffectSlowFall;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.PotionEffect;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

/**
 * 1.12.2 port of 1.20 {@link com.github.alexthe666.alexsmobs.entity.EntitySugarGlider}.
 * Tame/breed tags still list 1.13+ ids; {@link AMTagRegistry} maps sweet berries → apple and honeycomb → golden apple.
 */
public class EntitySugarGlider extends EntityTameable implements IFollower {

    private static final DataParameter<Byte> CLIMBING = EntityDataManager.createKey(EntitySugarGlider.class, DataSerializers.BYTE);
    private static final DataParameter<Byte> ATTACHED_FACE = EntityDataManager.createKey(EntitySugarGlider.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> GLIDING = EntityDataManager.createKey(EntitySugarGlider.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntitySugarGlider.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntitySugarGlider.class, DataSerializers.BOOLEAN);

    private static final EnumFacing[] WALL_FACES = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    public float prevGlideProgress;
    public float glideProgress;
    public float prevSitProgress;
    public float sitProgress;
    public float forageProgress;
    public float prevForageProgress;
    public float attachChangeProgress;
    public float prevAttachChangeProgress;
    public EnumFacing prevAttachDir = EnumFacing.DOWN;

    private boolean isGlidingNavigator;
    private boolean stopClimbing;
    private int rideCooldown;
    private int detachCooldown;
    private boolean sneakDismountArmed;

    public EntitySugarGlider(World world) {
        super(world);
        this.setSize(0.45F, 0.4F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.stepHeight = 1.0F;
        switchNavigator(true);
    }

    public static boolean canSugarGliderSpawnAt(World world, BlockPos pos) {
        return world.getLightFromNeighbors(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, this.aiSit);
        this.tasks.addTask(2, new FlyingAIFollowOwner(this, 1.0D, 5.0F, 2.0F, true));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.APPLE, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.SUGAR_GLIDER_BREEDABLES, stack.getItem())
                        || AMTagRegistry.itemInTag(AMTagRegistry.SUGAR_GLIDER_TAMEABLES, stack.getItem());
            }

            @Override
            public void startExecuting() {
                super.startExecuting();
                EntitySugarGlider.this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            }
        });
        this.tasks.addTask(4, new EntityAIMate(this, 0.8D) {
            @Override
            public void startExecuting() {
                super.startExecuting();
                EntitySugarGlider.this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            }
        });
        this.tasks.addTask(5, new GlideGoal());
        this.tasks.addTask(6, new EntityAIPanic(this, 1.2D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 100, 1.0D, 10, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!canSugarGliderSpawnAt(this.world, this.getPosition())) {
            return false;
        }
        if (!this.world.isAirBlock(this.getPosition())) {
            return false;
        }
        IBlockState below = this.world.getBlockState(this.getPosition().down());
        Block block = below.getBlock();
        boolean validPerch = block instanceof BlockLeaves || block == Blocks.LOG || block == Blocks.LOG2 || block == Blocks.GRASS;
        return validPerch && AMEntityRegistry.rollSpawn(AMConfig.sugarGliderSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CLIMBING, (byte) 0);
        this.dataManager.register(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
        this.dataManager.register(GLIDING, Boolean.FALSE);
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(SITTING, Boolean.FALSE);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateClimber(this, worldIn) {
            @Override
            protected boolean canNavigate() {
                return super.canNavigate() || EntitySugarGlider.this.isBesideClimbableBlock() || EntitySugarGlider.this.collidedVertically;
            }
        };
    }

    private void switchNavigator(boolean onGround) {
        if (onGround) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new PathNavigateClimber(this, this.world) {
                @Override
                protected boolean canNavigate() {
                    return super.canNavigate() || EntitySugarGlider.this.isBesideClimbableBlock() || EntitySugarGlider.this.collidedVertically;
                }
            };
            this.isGlidingNavigator = false;
        } else {
            this.moveHelper = new FlightMoveController(this, 0.6F, false);
            this.navigator = new DirectPathNavigator(this, this.world);
            this.isGlidingNavigator = true;
        }
    }

    public static boolean isMountedSlowFallPartner(EntityPlayer player) {
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof EntitySugarGlider) {
                EntitySugarGlider glider = (EntitySugarGlider) passenger;
                return glider.isRiding() && glider.isTamed() && glider.isOwner(player);
            }
        }
        return false;
    }

    public static void applySlowFallMotion(EntityLivingBase entity) {
        if (!entity.onGround) {
            entity.fallDistance = 0.0F;
            if (entity.motionY < EffectSlowFall.TERMINAL_VELOCITY) {
                entity.motionY = EffectSlowFall.TERMINAL_VELOCITY;
            }
        }
    }

    @Override
    public boolean hasNoGravity() {
        if (this.isRiding() || this.isGliding()) {
            return true;
        }
        return this.getAttachmentFacing() != EnumFacing.DOWN;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevGlideProgress = this.glideProgress;
        this.prevSitProgress = this.sitProgress;
        this.prevForageProgress = this.forageProgress;
        this.prevAttachChangeProgress = this.attachChangeProgress;

        if (this.glideProgress < 5F && this.isGliding()) {
            this.glideProgress = Math.min(5F, this.glideProgress + 2.5F);
        }
        if (this.glideProgress > 0F && !this.isGliding()) {
            this.glideProgress = Math.max(0F, this.glideProgress - 2.5F);
        }

        boolean sitVisual = this.isSitting() && !this.isInWater() && this.onGround;
        if (this.sitProgress < 5F && sitVisual) {
            this.sitProgress += 1F;
        }
        if (this.sitProgress > 0F && !sitVisual) {
            this.sitProgress -= 1F;
        }

        if (this.isSitting()) {
            this.setGliding(false);
            if (!this.world.isRemote) {
                this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            }
        }

        if (this.isRiding()) {
            this.attachChangeProgress = 0.0F;
            this.prevAttachDir = EnumFacing.DOWN;
        } else if (this.attachChangeProgress > 0F) {
            this.attachChangeProgress = Math.max(0F, this.attachChangeProgress - 0.25F);
        }

        if (this.isGliding()) {
            if (this.shouldStopGliding()) {
                this.setGliding(false);
            } else {
                this.motionX *= 0.99D;
                this.motionY *= 0.5D;
                this.motionZ *= 0.99D;
                this.fallDistance = 0.0F;
                if (this.motionY < -0.2D) {
                    this.motionY = -0.2D;
                }
            }
        }

        double motionYBefore = this.motionY;
        if (!this.world.isRemote) {
            this.setBesideClimbableBlock(this.collidedHorizontally);
            this.updateAttachmentFacing();
        }

        boolean dampFall = false;
        if (this.getAttachmentFacing() != EnumFacing.DOWN) {
            if (this.getAttachmentFacing() == EnumFacing.UP) {
                this.motionY += 0.1D;
            } else if (!this.collidedHorizontally) {
                Vec3d vec = new Vec3d(this.getAttachmentFacing().getDirectionVec());
                this.motionX += vec.x * 0.1D;
                this.motionY += vec.y * 0.1D;
                this.motionZ += vec.z * 0.1D;
            }
            if (!this.onGround && motionYBefore < 0.0D) {
                this.motionY *= 0.5D;
                dampFall = true;
            }
        }

        if (this.getAttachmentFacing() != EnumFacing.DOWN && !this.isGliding()) {
            this.motionX *= 0.6D;
            this.motionY *= 0.4D;
            this.motionZ *= 0.6D;
            if (this.collidedHorizontally) {
                this.motionY = 0.0D;
            }
        }

        if (!dampFall && this.isOnLadder()) {
            this.motionY *= 0.4D;
        }

        if (!this.isRiding()) {
            if (this.prevAttachDir != this.getAttachmentFacing()) {
                this.attachChangeProgress = 1F;
                this.prevAttachDir = this.getAttachmentFacing();
            }
        }

        if (!this.world.isRemote) {
            if ((this.getAttachmentFacing() == EnumFacing.UP || this.isGliding()) && !this.isGlidingNavigator) {
                this.switchNavigator(false);
            }
            if (this.getAttachmentFacing() != EnumFacing.UP && !this.isGliding() && this.isGlidingNavigator) {
                this.switchNavigator(true);
            }
        }

        if (this.detachCooldown > 0) {
            this.detachCooldown--;
        }
        if (this.rideCooldown > 0) {
            this.rideCooldown--;
        }

        if (!this.world.isRemote) {
            this.nudgeOffWall();
        }
    }

    private void updateAttachmentFacing() {
        if (this.isSitting() || this.isInWater() || this.isInLava() || this.isGliding() || this.isRiding() || this.onGround) {
            this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
            return;
        }
        EnumFacing current = this.getAttachmentFacing();
        if (current != EnumFacing.DOWN && this.collidedHorizontally) {
            BlockPos wallPos = new BlockPos(this.posX, MathHelper.floor(this.posY), this.posZ).offset(current);
            if (this.world.isSideSolid(wallPos, current.getOpposite(), false)) {
                return;
            }
        }
        EnumFacing closestDirection = EnumFacing.DOWN;
        double closestDistance = 100.0D;
        BlockPos mobPos = new BlockPos(this.posX, MathHelper.floor(this.posY), this.posZ);
        Vec3d self = new Vec3d(this.posX, this.posY + this.height * 0.5D, this.posZ);
        for (EnumFacing dir : WALL_FACES) {
            BlockPos offsetPos = mobPos.offset(dir);
            Vec3d offset = new Vec3d(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D);
            if (closestDistance > self.distanceTo(offset) && this.world.isSideSolid(offsetPos, dir.getOpposite(), false)) {
                closestDistance = self.distanceTo(offset);
                closestDirection = dir;
            }
        }
        this.dataManager.set(ATTACHED_FACE, (byte) (closestDistance > this.width * 0.5F + 0.7F ? EnumFacing.DOWN.getIndex() : closestDirection.getIndex()));
    }

    /** Keeps the mob on the exterior face of the wall it is attached to. */
    private void nudgeOffWall() {
        if (this.onGround || this.isRiding() || this.isGliding() || this.isSitting()) {
            return;
        }
        EnumFacing face = this.getAttachmentFacing();
        if (face.getAxis() == EnumFacing.Axis.Y) {
            return;
        }
        BlockPos wallPos = new BlockPos(this.posX, MathHelper.floor(this.posY), this.posZ).offset(face);
        if (!this.world.isSideSolid(wallPos, face.getOpposite(), false)) {
            return;
        }
        EnumFacing out = face.getOpposite();
        double standOff = 0.5D + this.width / 2.0D + 0.2D;
        double targetX = wallPos.getX() + 0.5D + out.getFrontOffsetX() * standOff;
        double targetZ = wallPos.getZ() + 0.5D + out.getFrontOffsetZ() * standOff;
        this.setPosition(targetX, this.posY, targetZ);
        this.motionY = 0.0D;
        float yaw = (float) (MathHelper.atan2(out.getFrontOffsetZ(), out.getFrontOffsetX()) * (180D / Math.PI)) - 90F;
        this.rotationYaw = yaw;
        this.renderYawOffset = yaw;
        this.rotationYawHead = yaw;
        this.prevRotationYaw = yaw;
        this.prevRenderYawOffset = yaw;
        this.prevRotationYawHead = yaw;
    }

    @Override
    public void dismountRidingEntity() {
        Entity mount = this.getRidingEntity();
        super.dismountRidingEntity();
        if (!this.world.isRemote && mount instanceof EntityPlayer) {
            ((EntityPlayer) mount).removePotionEffect(AMEffectRegistry.SLOW_FALL);
        }
    }

    @Override
    public void applyEntityCollision(Entity entityIn) {
        if (this.isRiding() && entityIn == this.getRidingEntity()) {
            return;
        }
        super.applyEntityCollision(entityIn);
    }

    @Override
    public boolean pushOutOfBlocks(double x, double y, double z) {
        return !this.isRiding() && super.pushOutOfBlocks(x, y, z);
    }

    @Override
    public void updateRidden() {
        Entity mount = this.getRidingEntity();
        if (!this.isRiding() || mount == null) {
            super.updateRidden();
            return;
        }
        if (!mount.isEntityAlive()) {
            this.dismountRidingEntity();
            return;
        }
        if (this.isTamed() && mount instanceof EntityLivingBase && this.isOwner((EntityLivingBase) mount) && mount instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) mount;
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onLivingUpdate();
            if (!this.world.isRemote) {
                player.addPotionEffect(new PotionEffect(AMEffectRegistry.SLOW_FALL, 100, 0, true, false));
            }
            this.rotationYaw = player.rotationYaw;
            this.prevRotationYaw = player.prevRotationYaw;
            this.renderYawOffset = player.renderYawOffset;
            this.prevRenderYawOffset = player.prevRenderYawOffset;
            this.rotationYawHead = player.rotationYawHead;
            this.prevRotationYawHead = player.prevRotationYawHead;
            this.rotationPitch = 0.0F;
            this.prevRotationPitch = 0.0F;
            double extraY = player.height + 0.12D;
            this.setPosition(player.posX, player.posY + extraY, player.posZ);
            if (!player.isEntityAlive()) {
                this.dismountRidingEntity();
                this.rideCooldown = 20;
                this.sneakDismountArmed = false;
            } else if (!player.isSneaking()) {
                this.sneakDismountArmed = true;
            } else if (this.sneakDismountArmed && this.rideCooldown <= 0) {
                this.dismountRidingEntity();
                this.rideCooldown = 20;
                this.sneakDismountArmed = false;
            }
            return;
        } else {
            super.updateRidden();
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isRiding()) {
            return;
        }
        if (this.isSitting()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        if (this.isInWater() && this.motionY > 0.0D) {
            this.motionY *= 0.5D;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean isOnLadder() {
        return this.isBesideClimbableBlock() && !this.isGliding() && !this.stopClimbing && !this.isSitting();
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

    public EnumFacing getAttachmentFacing() {
        return facingFromIndex(this.dataManager.get(ATTACHED_FACE));
    }

    public boolean isGliding() {
        return this.dataManager.get(GLIDING);
    }

    public void setGliding(boolean gliding) {
        if (gliding && this.isChild()) {
            return;
        }
        this.dataManager.set(GLIDING, gliding);
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
        if (command != 1) {
            this.setGliding(false);
        }
    }

    @Override
    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    @Override
    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
        if (this.aiSit != null) {
            this.aiSit.setSitting(sit);
        }
        if (sit) {
            this.setGliding(false);
            this.getNavigator().clearPath();
        }
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    @Override
    public void followEntity(EntityTameable tameable, EntityLivingBase owner, double followSpeed) {
        if (this.getDistance(owner) < 5.0F || this.isChild()) {
            this.setGliding(!this.onGround);
            this.getNavigator().tryMoveToEntityLiving(owner, followSpeed);
        } else {
            Vec3d fly = new Vec3d(0, this.onGround ? 0.4D : 0, 0);
            double f = this.onGround ? 0.9D : 0.5D;
            Vec3d toOwner = owner.getPositionEyes(1.0F).subtract(this.posX, this.posY, this.posZ).normalize().scale(f);
            fly = fly.add(toOwner);
            this.motionX = fly.x;
            this.motionY = fly.y;
            this.motionZ = fly.z;
            double d0 = MathHelper.sqrt(fly.x * fly.x + fly.z * fly.z);
            this.rotationPitch = (float) (-MathHelper.atan2(fly.y, d0) * (180D / Math.PI));
            this.rotationYaw = (float) (MathHelper.atan2(fly.z, fly.x) * (180D / Math.PI)) - 90F;
            this.setGliding(true);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.SUGAR_GLIDER_BREEDABLES, stack.getItem())
                || stack.getItem() == Items.APPLE;
    }

    /**
     * 1.12 {@code EntityTameable} blocks wild breeding. 1.20 sugar gliders breed without being tamed.
     */
    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        return otherAnimal != this && otherAnimal.getClass() == this.getClass() && this.isInLove() && otherAnimal.isInLove();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (!this.isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.SUGAR_GLIDER_TAMEABLES, itemstack.getItem())) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_PARROT_EAT, this.getSoundVolume(), this.getSoundPitch());
            if (this.getRNG().nextInt(2) == 0) {
                this.setTamedBy(player);
                this.setOwnerId(player.getUniqueID());
                if (player instanceof EntityPlayerMP) {
                    CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) player, this);
                }
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
            return true;
        }
        if (this.isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.INSECT_ITEMS, itemstack.getItem()) && this.getHealth() < this.getMaxHealth()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_PARROT_EAT, this.getSoundVolume(), this.getSoundPitch());
            this.heal(5.0F);
            return true;
        }
        boolean handled = super.processInteract(player, hand);
        if (!handled && this.isTamed() && this.isOwner(player) && hand == EnumHand.MAIN_HAND && itemstack.isEmpty()) {
            if (player.isSneaking()) {
                if (!this.isRiding() && player.getPassengers().isEmpty()) {
                    if (!this.world.isRemote) {
                        this.setSitting(false);
                        this.setGliding(false);
                        this.startRiding(player, true);
                        this.rideCooldown = 40;
                        this.sneakDismountArmed = false;
                        AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(this.getEntityId(), player.getEntityId()));
                    }
                    return true;
                }
                if (this.isRiding()) {
                    this.dismountRidingEntity();
                    this.rideCooldown = 20;
                    return true;
                }
            } else {
                if (this.isRiding()) {
                    this.dismountRidingEntity();
                    this.rideCooldown = 20;
                }
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                this.setSitting(sit);
                if (!sit) {
                    this.getNavigator().clearPath();
                }
                return true;
            }
        }
        return handled;
    }

    @Override
    public boolean canBeSteered() {
        return false;
    }

    @Override
    public double getMountedYOffset() {
        return 0.0D;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setByte("AttachFace", this.dataManager.get(ATTACHED_FACE));
        compound.setInteger("SugarGliderCommand", this.getCommand());
        compound.setBoolean("SugarGliderSitting", this.isSitting());
        compound.setBoolean("Gliding", this.isGliding());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("AttachFace")) {
            this.dataManager.set(ATTACHED_FACE, compound.getByte("AttachFace"));
        }
        this.setCommand(compound.getInteger("SugarGliderCommand"));
        this.setSitting(compound.getBoolean("SugarGliderSitting"));
        this.setGliding(compound.getBoolean("Gliding"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SUGAR_GLIDER_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SUGAR_GLIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SUGAR_GLIDER_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.SUGAR_GLIDER;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntitySugarGlider) AMEntityRegistry.SUGAR_GLIDER.newInstance(this.world);
    }

    private boolean shouldStopGliding() {
        return this.onGround || this.getAttachmentFacing() != EnumFacing.DOWN;
    }

    private boolean canSeeBlock(BlockPos destinationBlock) {
        Vec3d start = new Vec3d(this.posX, this.getEntityBoundingBox().minY + this.getEyeHeight(), this.posZ);
        Vec3d blockVec = new Vec3d(destinationBlock.getX() + 0.5D, destinationBlock.getY() + 0.5D, destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(start, blockVec, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().equals(destinationBlock);
    }

    private static EnumFacing facingFromIndex(byte b) {
        EnumFacing[] values = EnumFacing.values();
        int i = b & 7;
        return i >= 0 && i < values.length ? values[i] : EnumFacing.DOWN;
    }

    private class GlideGoal extends EntityAIBase {
        private boolean climbing;
        private int climbTime;
        private int leapSearchCooldown;
        private int climbTimeout;
        private BlockPos climb;
        private BlockPos glide;
        private boolean itsOver;
        private int airtime;
        private EnumFacing climbOffset = EnumFacing.UP;

        private GlideGoal() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntitySugarGlider.this.isChild() || EntitySugarGlider.this.isSitting() || EntitySugarGlider.this.getRNG().nextInt(45) != 0) {
                return false;
            }
            if (EntitySugarGlider.this.getAttachmentFacing() != EnumFacing.DOWN) {
                this.climb = EntitySugarGlider.this.getPosition().offset(EntitySugarGlider.this.getAttachmentFacing());
            } else {
                this.climb = findClimbPos();
            }
            return this.climb != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.climb != null && !this.itsOver && this.climbTimeout < 30
                    && (!this.climbing || !EntitySugarGlider.this.world.isAirBlock(this.climb))
                    && !EntitySugarGlider.this.isSitting();
        }

        @Override
        public void startExecuting() {
            this.climbTimeout = 0;
            this.leapSearchCooldown = 0;
            this.airtime = 0;
            this.climbing = true;
            this.climbTime = 0;
            EntitySugarGlider.this.getNavigator().clearPath();
        }

        @Override
        public void resetTask() {
            this.climbTimeout = 0;
            this.climb = null;
            this.glide = null;
            this.itsOver = false;
            EntitySugarGlider.this.stopClimbing = false;
            EntitySugarGlider.this.setGliding(false);
            EntitySugarGlider.this.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (this.leapSearchCooldown > 0) {
                this.leapSearchCooldown--;
            }
            if (this.climbing && this.climb != null) {
                float inDir = EntitySugarGlider.this.getAttachmentFacing() == EnumFacing.DOWN && EntitySugarGlider.this.posY > this.climb.getY() + 0.3F
                        ? 0.5F + EntitySugarGlider.this.width * 0.5F : 0.5F;
                Vec3d faceVec = new Vec3d(this.climbOffset.getDirectionVec());
                Vec3d offset = new Vec3d(this.climb.getX() + 0.5D + faceVec.x * inDir,
                        this.climb.getY() + 0.5D + faceVec.y * inDir,
                        this.climb.getZ() + 0.5D + faceVec.z * inDir);
                double d0 = this.climb.getX() + 0.5D - EntitySugarGlider.this.posX;
                double d2 = this.climb.getZ() + 0.5D - EntitySugarGlider.this.posZ;
                double xzDistSqr = d0 * d0 + d2 * d2;
                if (EntitySugarGlider.this.posY > offset.y - 0.3D - EntitySugarGlider.this.height) {
                    EntitySugarGlider.this.stopClimbing = true;
                }
                if (xzDistSqr < 3.0D && EntitySugarGlider.this.getAttachmentFacing() != EnumFacing.DOWN) {
                    Vec3d push = new Vec3d(d0, 0, d2).normalize().scale(0.1D);
                    EntitySugarGlider.this.motionX += push.x;
                    EntitySugarGlider.this.motionZ += push.z;
                } else {
                    EntitySugarGlider.this.getNavigator().tryMoveToXYZ(offset.x, offset.y, offset.z, 1.0D);
                }
                if (EntitySugarGlider.this.getAttachmentFacing() == EnumFacing.DOWN) {
                    this.climbTimeout++;
                    this.climbTime = 0;
                } else {
                    this.climbTimeout = 0;
                    this.climbTime++;
                    if (this.climbTime > 40 && this.leapSearchCooldown == 0) {
                        BlockPos leapTo = findLeapPos(false);
                        this.leapSearchCooldown = 5 + EntitySugarGlider.this.getRNG().nextInt(10);
                        if (leapTo != null) {
                            EntitySugarGlider.this.stopClimbing = false;
                            EntitySugarGlider.this.setGliding(true);
                            EntitySugarGlider.this.getNavigator().clearPath();
                            EntitySugarGlider.this.dataManager.set(ATTACHED_FACE, (byte) EnumFacing.DOWN.getIndex());
                            this.glide = leapTo;
                            this.climbing = false;
                        }
                    }
                }
            } else if (this.glide != null) {
                EntitySugarGlider.this.stopClimbing = false;
                EntitySugarGlider.this.setGliding(true);
                if (this.airtime > 5 && (EntitySugarGlider.this.collidedHorizontally || EntitySugarGlider.this.onGround
                        || EntitySugarGlider.this.getDistanceSq(this.glide.getX() + 0.5D, this.glide.getY() + 0.5D, this.glide.getZ() + 0.5D) < 1.21D)) {
                    EntitySugarGlider.this.setGliding(false);
                    EntitySugarGlider.this.detachCooldown = 20 + EntitySugarGlider.this.getRNG().nextInt(80);
                    this.itsOver = true;
                }
                Vec3d fly = new Vec3d(this.glide.getX() + 0.5D - EntitySugarGlider.this.posX,
                        this.glide.getY() + 0.5D - EntitySugarGlider.this.posY,
                        this.glide.getZ() + 0.5D - EntitySugarGlider.this.posZ).normalize().scale(0.3D);
                EntitySugarGlider.this.motionX = fly.x;
                EntitySugarGlider.this.motionY = fly.y;
                EntitySugarGlider.this.motionZ = fly.z;
                double horiz = MathHelper.sqrt(fly.x * fly.x + fly.z * fly.z);
                EntitySugarGlider.this.rotationPitch = (float) (-MathHelper.atan2(fly.y, horiz) * (180D / Math.PI));
                EntitySugarGlider.this.rotationYaw = (float) (MathHelper.atan2(fly.z, fly.x) * (180D / Math.PI)) - 90F;
                this.airtime++;
            }
        }

        @Nullable
        private BlockPos findClimbPos() {
            BlockPos mobPos = EntitySugarGlider.this.getPosition();
            for (int i = 0; i < 15; i++) {
                BlockPos offset = mobPos.add(EntitySugarGlider.this.getRNG().nextInt(16) - 8, EntitySugarGlider.this.getRNG().nextInt(4) + 1, EntitySugarGlider.this.getRNG().nextInt(16) - 8);
                double d0 = offset.getX() + 0.5D - EntitySugarGlider.this.posX;
                double d2 = offset.getZ() + 0.5D - EntitySugarGlider.this.posZ;
                double xzDistSqr = d0 * d0 + d2 * d2;
                Vec3d blockVec = new Vec3d(offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D);
                RayTraceResult result = EntitySugarGlider.this.world.rayTraceBlocks(EntitySugarGlider.this.getPositionEyes(1.0F), blockVec, false, true, false);
                if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && xzDistSqr > 4.0D
                        && result.sideHit.getAxis() != EnumFacing.Axis.Y
                        && getDistanceOffGround(result.getBlockPos().offset(result.sideHit)) > 3
                        && isPositionEasilyClimbable(result.getBlockPos())) {
                    this.climbOffset = result.sideHit;
                    return result.getBlockPos();
                }
            }
            return null;
        }

        @Nullable
        private BlockPos findLeapPos(boolean leavesOnly) {
            BlockPos mobPos = EntitySugarGlider.this.getPosition().offset(this.climbOffset.getOpposite());
            for (int i = 0; i < 15; i++) {
                BlockPos offset = mobPos.add(EntitySugarGlider.this.getRNG().nextInt(32) - 16, -1 - EntitySugarGlider.this.getRNG().nextInt(4), EntitySugarGlider.this.getRNG().nextInt(32) - 16);
                Vec3d blockVec = new Vec3d(offset.getX() + 0.5D, offset.getY() + 0.5D, offset.getZ() + 0.5D);
                RayTraceResult result = EntitySugarGlider.this.world.rayTraceBlocks(EntitySugarGlider.this.getPositionEyes(1.0F), blockVec, false, true, false);
                if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().distanceSq(mobPos) > 4) {
                    if (leavesOnly) {
                        Block block = EntitySugarGlider.this.world.getBlockState(result.getBlockPos()).getBlock();
                        if (!(block instanceof BlockLeaves)) {
                            continue;
                        }
                    }
                    return result.getBlockPos();
                }
            }
            return null;
        }

        private int getDistanceOffGround(BlockPos pos) {
            int dist = 0;
            BlockPos check = pos;
            while (check.getY() > 0 && EntitySugarGlider.this.world.isAirBlock(check)) {
                check = check.down();
                dist++;
            }
            return dist;
        }

        private boolean isPositionEasilyClimbable(BlockPos pos) {
            BlockPos check = pos.down();
            while (check.getY() > EntitySugarGlider.this.getPosition().getY() && !EntitySugarGlider.this.world.isAirBlock(check)) {
                check = check.down();
            }
            return check.getY() <= EntitySugarGlider.this.getPosition().getY();
        }
    }
}
