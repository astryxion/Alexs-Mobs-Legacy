package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EagleTemptFish;
import com.github.alexthe666.alexsmobs.entity.ai.EagleTemptMerged;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingAIFollowOwner;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoMountPlayer;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumHand;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EntityBaldEagle extends EntityTameable implements IFollower {

    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> TACKLING = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_CAP = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LAUNCHED = EntityDataManager.createKey(EntityBaldEagle.class, DataSerializers.BOOLEAN);
    /** Tempt priority 6: rotten flesh, fish oil, and {@link AMTagRegistry#BALD_EAGLE_TAMEABLES} (see {@link EagleTemptMerged}). */
    public float prevAttackProgress;
    public float attackProgress;
    public float prevFlyProgress;
    public float flyProgress;
    public float prevTackleProgress;
    public float tackleProgress;
    public float prevSwoopProgress;
    public float swoopProgress;
    public float prevFlapAmount;
    public float flapAmount;
    public float birdPitch = 0;
    public float prevBirdPitch = 0;
    public float prevSitProgress;
    public float sitProgress;
    private boolean isLandNavigator;
    private int timeFlying;
    private BlockPos orbitPos = null;
    private double orbitDist = 5D;
    private boolean orbitClockwise = false;
    private int passengerTimer = 0;
    private int launchTime = 0;
    private int lastPlayerControlTime = 0;
    private int returnControlTime = 0;
    private int tackleCapCooldown = 0;
    private boolean controlledFlag = false;
    private int chunkLoadCooldown;
    private int stillTicksCounter = 0;
    private int rideCooldown = 0;

    public EntityBaldEagle(World worldIn) {
        super(worldIn);
        this.setSize(0.75F, 1.2F);
        this.setPathPriority(PathNodeType.LAVA, -1.0F);
        this.experienceValue = 4;
        switchNavigator(true);
    }

    public static boolean canEagleSpawn(World world, BlockPos pos) {
        return world.getLightFromNeighbors(pos) > 8;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(0, new EntityAISwimming(this) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && (EntityBaldEagle.this.getAir() < 30 || EntityBaldEagle.this.getAttackTarget() == null || !EntityBaldEagle.this.getAttackTarget().isInWater() && EntityBaldEagle.this.posY > EntityBaldEagle.this.getAttackTarget().posY);
            }
        });
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new FlyingAIFollowOwner(this, 1.0D, 25.0F, 2.0F, false));
        this.tasks.addTask(3, new AITackle());
        this.tasks.addTask(4, new AILandOnGlove());
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EagleTemptMerged(this));
        this.tasks.addTask(7, new EagleTemptFish(this, 1.1D, false));
        this.tasks.addTask(8, new AIWanderIdle());
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F) {
            @Override
            public boolean shouldExecute() {
                return EntityBaldEagle.this.returnControlTime == 0 && super.shouldExecute();
            }
        });
        this.tasks.addTask(10, new EntityAILookIdle(this) {
            @Override
            public boolean shouldExecute() {
                return EntityBaldEagle.this.returnControlTime == 0 && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntityLivingBase.class, 5, true, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.BALD_EAGLE_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityBaldEagle.this.isLaunched() && EntityBaldEagle.this.getCommand() == 0;
            }
        });
    }

    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.BALD_EAGLE_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.BALD_EAGLE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.BALD_EAGLE_HURT;
    }


    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.baldEagleSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase EntityLivingBase = this.getOwner();
            if (entityIn == EntityLivingBase) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(EntityLivingBase);
            }
            if (EntityLivingBase != null) {
                return EntityLivingBase.isOnSameTeam(entityIn);
            }
        }

        return super.isOnSameTeam(entityIn);
    }

    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.ROTTEN_FLESH;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new EntityBaldEagle.MoveHelper(this);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("BirdSitting", this.isSitting());
        compound.setBoolean("Launched", this.isLaunched());
        compound.setBoolean("HasCap", this.hasCap());
        compound.setInteger("EagleCommand", this.getCommand());
        compound.setInteger("LaunchTime", this.launchTime);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("BirdSitting"));
        this.setLaunched(compound.getBoolean("Launched"));
        this.setCap(compound.getBoolean("HasCap"));
        this.setCommand(compound.getInteger("EagleCommand"));
        this.launchTime = compound.getInteger("LaunchTime");
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.shouldHoodedReturn() && this.hasCap() && this.isTamed() && !this.isRiding()) {
            super.travel(0.0F, 0.0F, 0.0F);
            return;
        }
        super.travel(strafe, vertical, forward);
    }

    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.dataManager.get(ATTACK_TICK) == 0 && this.attackProgress == 0 && entityIn.isEntityAlive() && this.getDistance(entityIn) < entityIn.width + 5) {
            this.dataManager.set(ATTACK_TICK, 5);
        }
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLYING, false);
        this.dataManager.register(HAS_CAP, false);
        this.dataManager.register(TACKLING, false);
        this.dataManager.register(LAUNCHED, false);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(COMMAND, Integer.valueOf(0));
        this.dataManager.register(SITTING, Boolean.valueOf(false));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND).intValue();
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, Integer.valueOf(command));
    }

    public boolean isLaunched() {
        return this.dataManager.get(LAUNCHED);
    }

    public void setLaunched(boolean flying) {
        this.dataManager.set(LAUNCHED, flying);
    }

    public boolean isFlying() {
        return this.dataManager.get(FLYING);
    }

    public void setFlying(boolean flying) {
        if(flying && this.isChild()){
            flying = false;
        }
        this.dataManager.set(FLYING, flying);
    }

    public boolean hasCap() {
        return this.dataManager.get(HAS_CAP);
    }

    public void setCap(boolean cap) {
        this.dataManager.set(HAS_CAP, cap);
    }

    public boolean isTackling() {
        return this.dataManager.get(TACKLING);
    }

    public void setTackling(boolean tackling) {
        this.dataManager.set(TACKLING, tackling);
    }

    public void followEntity(EntityTameable tameable, EntityLivingBase owner, double followSpeed) {
        if (this.getDistance(owner) > 15) {
            this.setFlying(true);
            this.getMoveHelper().setMoveTo(owner.posX, owner.posY + owner.height, owner.posZ, followSpeed);
        } else {
            if (this.isFlying() && !this.isOverWaterOrVoid()) {
                BlockPos vec = this.getCrowGround(this.getPosition());
                if (vec != null) {
                    this.getMoveHelper().setMoveTo(vec.getX(), vec.getY(), vec.getZ(), followSpeed);
                }
                if (this.onGround) {
                    this.setFlying(false);
                }
            } else {
                this.getNavigator().tryMoveToEntityLiving(owner, followSpeed);
            }
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (this.isHealFishItem(item) && this.getHealth() < this.getMaxHealth()) {
            this.heal(10);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.world.setEntityState(this, (byte) 7);
            return true;
        }
        if (!this.isTamed() && isBaldEagleTameableItem(item)) {
            if (itemstack.getItem().hasContainerItem()) {
                this.entityDropItem(new ItemStack(itemstack.getItem().getContainerItem()), 0.0F);
            }
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            if (rand.nextBoolean()) {
                this.world.setEntityState(this, (byte) 7);
                this.setTamedBy(player);
                this.setCommand(1);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
            return true;
        }
        if (this.isTamed() && !this.isBreedingItem(itemstack)) {
            if (!this.isChild() && item == AMItemRegistry.FALCONRY_HOOD) {
                if (!this.hasCap()) {
                    this.setCap(true);
                    if (!player.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }
                    this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, this.getSoundVolume(), this.getSoundPitch());
                    return true;
                }
            } else if (item == Items.SHEARS && this.hasCap()) {
                this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
                if (!this.world.isRemote && player instanceof EntityPlayerMP) {
                    itemstack.attemptDamageItem(1, this.rand, (EntityPlayerMP) player);
                }
                this.entityDropItem(new ItemStack(AMItemRegistry.FALCONRY_HOOD), 0.0F);
                this.setCap(false);
                return true;
            } else if (!this.isChild() && this.getRidingEagles(player) <= 0
                    &&                     (player.getHeldItem(EnumHand.MAIN_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE
                    || player.getHeldItem(EnumHand.OFF_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE)) {
                this.rideCooldown = 30;
                this.setLaunched(false);
                this.removePassengers();
                this.startRiding(player, true);
                if (!this.world.isRemote) {
                    AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(this.getEntityId(), player.getEntityId()));
                }
                return true;
            } else {
                this.setCommand((this.getCommand() + 1) % 3);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                if (sit) {
                    this.setSitting(true);
                } else {
                    this.setSitting(false);
                }
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    private static boolean isHealFishItem(Item item) {
        return item == Items.FISH || item == Items.COOKED_FISH;
    }

    private static boolean isBaldEagleTameableItem(Item item) {
        Set<Item> set = AMTagRegistry.ITEM_TAG_SETS.get(AMTagRegistry.BALD_EAGLE_TAMEABLES);
        return set != null && set.contains(item);
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1 && !isLaunched();
    }

    public int getRidingEagles(EntityLivingBase player) {
        int crowCount = 0;
        for (Entity e : player.getPassengers()) {
            if (e instanceof EntityBaldEagle) {
                crowCount++;
            }
        }
        return crowCount;
    }

    @Override
    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && (!entity.isEntityAlive() || !this.isEntityAlive())) {
            this.dismountRidingEntity();
        } else if (this.isTamed() && entity instanceof EntityLivingBase && this.isOwner((EntityLivingBase) entity)) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.eagleTickLogic();
            if (this.isRiding()) {
                Entity mount = this.getRidingEntity();
                if (mount instanceof EntityPlayer) {
                    float yawAdd = 0;
                    if (((EntityPlayer) mount).getHeldItem(EnumHand.MAIN_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                        yawAdd = ((EntityPlayer) mount).getPrimaryHand() == EnumHandSide.LEFT ? 135.0F : -135.0F;
                    } else if (((EntityPlayer) mount).getHeldItem(EnumHand.OFF_HAND).getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                        yawAdd = ((EntityPlayer) mount).getPrimaryHand() == EnumHandSide.LEFT ? -135.0F : 135.0F;
                    } else {
                        this.setCommand(2);
                        this.setSitting(true);
                        this.dismountRidingEntity();
                        this.copyLocationAndAnglesFrom(mount);
                    }
                    float birdYaw = yawAdd * 0.5F;
                    this.renderYawOffset = MathHelper.wrapDegrees(((EntityLivingBase) mount).renderYawOffset + birdYaw);
                    this.rotationYaw = MathHelper.wrapDegrees(((EntityLivingBase) mount).rotationYaw + birdYaw);
                    this.rotationYawHead = MathHelper.wrapDegrees(((EntityLivingBase) mount).rotationYawHead + birdYaw);
                    float radius = 0.6F;
                    float angle = (0.01745329251F * (((EntityLivingBase) mount).renderYawOffset - 180.0F + yawAdd));
                    double extraX = (double) radius * MathHelper.sin((float) (Math.PI + (double) angle));
                    double extraZ = (double) radius * MathHelper.cos(angle);
                    this.setPosition(mount.posX + extraX, Math.max(mount.posY + (double) mount.height * 0.45D, mount.posY), mount.posZ + extraZ);
                }
                if (!mount.isEntityAlive()) {
                    this.dismountRidingEntity();
                }
            }
        } else {
            super.updateRidden();
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!(this.isRiding() && this.isTamed() && this.getRidingEntity() instanceof EntityLivingBase && this.isOwner((EntityLivingBase) this.getRidingEntity()))) {
            this.eagleTickLogic();
        }
    }

    /** Eagle animation / flight state (1.16 {@code tick()} body after super). When falconry-mounted on owner, runs from {@link #updateRidden()} only. */
    private void eagleTickLogic() {
        this.prevAttackProgress = attackProgress;
        this.prevBirdPitch = birdPitch;
        this.prevTackleProgress = tackleProgress;
        this.prevFlyProgress = flyProgress;
        this.prevFlapAmount = flapAmount;
        this.prevSwoopProgress = swoopProgress;
        this.prevSitProgress = sitProgress;
        float yMot = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        this.birdPitch = yMot;
        if (isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (isTackling() && tackleProgress < 5F) {
            tackleProgress++;
        }
        if (!isTackling() && tackleProgress > 0F) {
            tackleProgress--;
        }
        boolean sit = isSitting() || this.isRiding();
        if (sit && sitProgress < 5F) {
            sitProgress++;
        }
        if (!sit && sitProgress > 0F) {
            sitProgress--;
        }
        if (this.isLaunched()) {
            launchTime++;
        } else {
            launchTime = 0;
        }
        if (lastPlayerControlTime > 0) {
            lastPlayerControlTime--;
        }
        if (lastPlayerControlTime <= 0) {
            controlledFlag = false;
        }
        if (yMot < 0.1F) {
            flapAmount = Math.min(-yMot * 0.2F, 1F);
            if (swoopProgress > 0) {
                swoopProgress--;
            }
        } else {
            if (flapAmount > 0.0F) {
                flapAmount -= Math.min(flapAmount, 0.1F);
            } else {
                flapAmount = 0;
            }
            if (swoopProgress < yMot * 0.2F) {
                swoopProgress = Math.min(yMot * 0.2F, swoopProgress + 1);
            }
        }
        if (this.isTackling()) {
            flapAmount = Math.min(2, flapAmount + 0.2F);
        }
        if (!world.isRemote) {
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (this.isTackling() && !this.isBeingRidden() && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive()) && tackleCapCooldown == 0) {
                this.setTackling(false);
            }
            if (isFlying()) {
                timeFlying++;
                this.setNoGravity(true);
                if (this.isSitting() || this.isRiding() || this.isInLove()) {
                    if(!isLaunched()){
                        this.setFlying(false);
                    }
                }
                if (this.getAttackTarget() != null && this.getAttackTarget().posY < this.posX && !this.isBeingRidden()) {
                    this.motionX *= 1.0D;
                    this.motionY *= 0.9D;
                    this.motionZ *= 1.0D;
                }
            } else {
                timeFlying = 0;
                this.setNoGravity(false);
            }
            if (this.isInWater() && this.isBeingRidden()) {
                this.motionY += 0.1D;
            }
            if(this.isSitting() && !this.isLaunched()){
                this.motionY += -0.1D;
            }
            if (this.getAttackTarget() != null && this.isInWater()) {
                timeFlying = 0;
                this.setFlying(true);
            }
            if (isFlying() && this.onGround && !this.isInWater() && this.timeFlying > 30) {
                this.setFlying(false);
            }
        }
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            if (this.dataManager.get(ATTACK_TICK) == 2 && this.getAttackTarget() != null && this.getDistance(this.getAttackTarget()) < this.getAttackTarget().width + 2D) {
                this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 2);
            }
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
        if (this.isRiding()) {
            this.setFlying(false);
            this.setTackling(false);
        }
        if (rideCooldown > 0) {
            rideCooldown--;
        }
        if (returnControlTime > 0) {
            returnControlTime--;
        }
        if (tackleCapCooldown > 0) {
            tackleCapCooldown--;
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityAgeable) AMEntityRegistry.BALD_EAGLE.newInstance(this.world);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    private static Vec3d vecCenter(BlockPos p) {
        return new Vec3d((double) p.getX() + 0.5D, (double) p.getY() + 0.5D, (double) p.getZ() + 0.5D);
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24) - radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getCrowGround(radialPos);
        int distFromGround = (int) this.posY - ground.getY();
        int flightHeight = 7 + this.getRNG().nextInt(10);
        BlockPos newPos = ground.up(distFromGround > 8 ? flightHeight : this.getRNG().nextInt(7) + 4);
        Vec3d centerNew = vecCenter(newPos);
        if (!this.isTargetBlocked(centerNew) && this.getPositionVector().squareDistanceTo(centerNew) > 1.0D) {
            return centerNew;
        }
        return null;
    }

    private BlockPos getCrowGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), MathHelper.floor(this.posY), in.getZ());
        while (position.getY() < 256 && this.world.getBlockState(position).getMaterial() == Material.WATER) {
            position = position.up();
        }
        while (position.getY() > 2 && this.world.isAirBlock(position)) {
            position = position.down();
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
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, this.posY, fleePos.z + extraZ);
        BlockPos ground = this.getCrowGround(radialPos);
        if (ground.getY() == 0) {
            return this.getPositionVector();
        } else {
            ground = this.getPosition();
            while (ground.getY() > 2 && this.world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        if (!this.isTargetBlocked(vecCenter(ground.up()))) {
            return vecCenter(ground);
        }
        return null;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        RayTraceResult res = this.world.rayTraceBlocks(start, target, false, true, false);
        return res != null && res.typeOfHit == RayTraceResult.Type.BLOCK;
    }

    private Vec3d getOrbitVec(Vec3d fleeVector, float gatheringCircleDist) {
        float angle = (0.01745329251F * (float) this.orbitDist * (orbitClockwise ? -ticksExisted : ticksExisted));
        double extraX = gatheringCircleDist * MathHelper.sin((angle));
        double extraZ = gatheringCircleDist * MathHelper.cos(angle);
        if (this.orbitPos != null) {
            Vec3d at = new Vec3d(orbitPos.getX() + extraX, orbitPos.getY() + rand.nextInt(2) - 2, orbitPos.getZ() + extraZ);
            if (this.world.isAirBlock(new BlockPos(at))) {
                return at;
            }
        }
        return null;
    }

    private boolean isOverWaterOrVoid() {
        BlockPos position = this.getPosition();
        while (position.getY() > 0 && this.world.isAirBlock(position)) {
            position = position.down();
        }
        return this.world.getBlockState(position).getMaterial() == Material.WATER || position.getY() <= 0;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (passenger.getRidingEntity() == this) {
            float radius = 0.3F;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.rotationYaw = this.renderYawOffset + 90F;
            if (passenger instanceof EntityLivingBase) {
                EntityLivingBase living = (EntityLivingBase) passenger;
                living.renderYawOffset = this.renderYawOffset + 90F;
            }
            double extraY = 0;
            if (passenger instanceof EntitySquid && !passenger.isInWater()) {
                extraY = 0.1F;
            }
            passenger.setPosition(this.posX + extraX, this.posY - 0.3F + extraY + passenger.height * 0.3F, this.posZ + extraZ);
            passengerTimer++;
            if (this.isEntityAlive() && passengerTimer > 0 && passengerTimer % 40 == 0) {
                passenger.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
            }
        }
    }

    public boolean canBeRiddenInWater(Entity rider) {
        return true;
    }

    public boolean shouldHoodedReturn() {
        if (this.getOwner() != null) {
            if (!this.getOwner().isEntityAlive() || this.getOwner().isSneaking() ) {
                return true;
            }
        }
        return !this.isEntityAlive() || launchTime > 12000 || this.inPortal || this.portalCounter > 0;
    }

    @Override
    public void setDead() {
        if (this.lastPlayerControlTime == 0 && !this.isRiding()) {
            super.setDead();
        }
    }

    public void directFromPlayer(float rotationYaw, float rotationPitch, boolean loadChunk, Entity over) {
        EntityLivingBase owner = this.getOwner();
        if (owner != null && this.getDistance(owner) > 150) {
            returnControlTime = 100;
        }
        if (Math.abs(this.prevPosX - this.posX) > 0.1F || Math.abs(this.prevPosY - this.posY) > 0.1F || Math.abs(this.prevPosZ - this.posZ) > 0.1F) {
            stillTicksCounter = 0;
        } else {
            stillTicksCounter++;
        }
        int stillTPthreshold = AMConfig.falconryTeleportsBack ? 200 : 6000;
        this.setSitting(false);
        this.setLaunched(true);
        if ((returnControlTime > 0 && AMConfig.falconryTeleportsBack || stillTicksCounter > stillTPthreshold && owner != null && this.getDistance(owner) > 30)) {
            this.copyLocationAndAnglesFrom(owner);
            returnControlTime = 0;
            stillTicksCounter = 0;
            launchTime = Math.max(launchTime, 12000);
        }
        if (!this.world.isRemote) {
            if (returnControlTime > 0 && owner != null) {
                this.getLookHelper().setLookPositionWithEntity(owner, 30.0F, 30.0F);
            } else {
                this.renderYawOffset = rotationYaw;
                this.rotationYaw = rotationYaw;
                this.rotationYawHead = rotationYaw;
                this.rotationPitch = rotationPitch;
            }
            if (rotationPitch < 10 && this.onGround) {
                this.setFlying(true);
            }
            float yawOffset = rotationYaw + 90.0F;
            float rad = 3F;
            float speed = 1.2F;
            if (returnControlTime > 0 && owner != null) {
                this.getMoveHelper().setMoveTo(owner.posX, owner.posY + 10.0D, owner.posZ, (double) speed);
            } else {
                this.getMoveHelper().setMoveTo(this.posX + (double) rad * 1.5D * Math.cos((double) yawOffset * (Math.PI / 180.0D)), this.posY - (double) rad * Math.sin((double) rotationPitch * (Math.PI / 180.0D)), this.posZ + (double) rad * Math.sin((double) yawOffset * (Math.PI / 180.0D)), (double) speed);
            }
            if (loadChunk) {
                this.loadChunkOnServer(this.getPosition());
            }
            this.setRevengeTarget(null);
            this.setAttackTarget(null);
            if (over == null) {
                List<Entity> list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().grow(3.0D), EntitySelectors.CAN_AI_TARGET);
                Entity closest = null;
                for (Entity e : list) {
                    if (closest == null || this.getDistance(e) < this.getDistance(closest)) {
                        closest = e;
                    }
                }
                over = closest;
            }
        }
        if (over != null && !this.isOnSameTeam(over) && over != owner && this.canFalconryAttack(over)) {
            if (tackleCapCooldown == 0 && this.getDistance(over) <= over.width + 4.0D) {
                this.setTackling(true);
                if (this.getDistance(over) <= over.width + 2.0D) {
                    double motLen = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    float speedDamage = (float) Math.ceil(MathHelper.clamp(motLen + 0.2D, 0.0D, 1.2D) * 3.333D);
                    over.attackEntityFrom(DamageSource.causeMobDamage(this), (float) (5.0D + (double) speedDamage + (double) this.rand.nextInt(2)));
                    tackleCapCooldown = 22;
                }
            }
        }
        this.lastPlayerControlTime = 10;
        this.controlledFlag = true;
    }

    private boolean canFalconryAttack(Entity over) {
        return !(over instanceof EntityItem) && (!(over instanceof EntityLivingBase) || !this.isOwner((EntityLivingBase)over));
    }

    @Override
    public void onKillEntity(EntityLivingBase entity) {
        if (this.isLaunched() && this.hasCap() && this.isTamed() && this.getOwner() != null) {
            if (this.getOwner() instanceof EntityPlayerMP && this.getDistanceSq(this.getOwner()) >= 10000.0D) {
                AMAdvancementTriggerRegistry.BALD_EAGLE_CHALLENGE.trigger((EntityPlayerMP) this.getOwner());
            }
        }
        super.onKillEntity(entity);
    }


    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow) && this.isLaunched()) {
                amount = (amount + 1.0F) / 4.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    public void loadChunkOnServer(BlockPos center) {
        if (!this.world.isRemote && this.world instanceof WorldServer) {
            WorldServer ws = (WorldServer) this.world;
            int cx = center.getX() >> 4;
            int cz = center.getZ() >> 4;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    ws.getChunkProvider().loadChunk(cx + i, cz + j);
                }
            }
        }
    }

    class MoveHelper extends EntityMoveHelper {
        private final EntityBaldEagle parentEntity;

        public MoveHelper(EntityBaldEagle bird) {
            super(bird);
            this.parentEntity = bird;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == EntityMoveHelper.Action.MOVE_TO) {
                Vec3d delta = new Vec3d(this.posX - parentEntity.posX, this.posY - parentEntity.posY, this.posZ - parentEntity.posZ);
                double d5 = delta.lengthVector();
                if (d5 < 0.3D) {
                    this.action = EntityMoveHelper.Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    Vec3d scaled = delta.scale(this.speed * 0.05D / d5);
                    parentEntity.motionX += scaled.x;
                    parentEntity.motionY += scaled.y;
                    parentEntity.motionZ += scaled.z;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                }
            }
        }

        private boolean func_220673_a(Vec3d p_220673_1_, int p_220673_2_) {
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();

            for (int i = 1; i < p_220673_2_; ++i) {
                axisalignedbb = axisalignedbb.offset(p_220673_1_);
                if (!this.parentEntity.world.collidesWithAnyBlock(axisalignedbb)) {
                    return false;
                }
            }

            return true;
        }
    }

    private class AIWanderIdle extends EntityAIBase {
        protected final EntityBaldEagle eagle;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;
        private int orbitResetCooldown = 0;
        private int maxOrbitTime = 360;
        private int orbitTime = 0;

        public AIWanderIdle() {
            this.setMutexBits(1);
            this.eagle = EntityBaldEagle.this;
        }

        @Override
        public boolean shouldExecute() {
            if (orbitResetCooldown < 0) {
                orbitResetCooldown++;
            }
            if ((eagle.getAttackTarget() != null && eagle.getAttackTarget().isEntityAlive() && !this.eagle.isBeingRidden()) || this.eagle.isRiding() || this.eagle.isSitting() || eagle.controlledFlag) {
                return false;
            }
            if (this.eagle.getRNG().nextInt(15) != 0 && !eagle.isFlying()) {
                return false;
            }
            if (this.eagle.isChild()) {
                this.flightTarget = false;
            } else if (this.eagle.isInWater()) {
                this.flightTarget = true;
            } else if (this.eagle.hasCap()) {
                this.flightTarget = false;
            } else if (this.eagle.onGround) {
                this.flightTarget = rand.nextBoolean();
            } else {
                if (orbitResetCooldown == 0 && rand.nextInt(6) == 0) {
                    orbitResetCooldown = 400;
                    eagle.orbitPos = eagle.getPosition();
                    eagle.orbitDist = 4 + rand.nextInt(5);
                    eagle.orbitClockwise = rand.nextBoolean();
                    orbitTime = 0;
                    maxOrbitTime = (int) (360.0F + 360.0F * rand.nextFloat());
                }
                this.flightTarget = eagle.isBeingRidden() || rand.nextInt(7) > 0 && eagle.timeFlying < 700;
            }
            Vec3d pick = this.pickWanderVec();
            if (pick == null) {
                return false;
            }
            this.x = pick.x;
            this.y = pick.y;
            this.z = pick.z;
            return true;
        }

        @Override
        public void updateTask() {
            if (orbitResetCooldown > 0) {
                orbitResetCooldown--;
            }
            if (orbitResetCooldown < 0) {
                orbitResetCooldown++;
            }
            if (orbitResetCooldown > 0 && eagle.orbitPos != null) {
                if (orbitTime < maxOrbitTime && !eagle.isInWater()) {
                    orbitTime++;
                } else {
                    orbitTime = 0;
                    eagle.orbitPos = null;
                    orbitResetCooldown = -400 - rand.nextInt(400);
                }
            }
            if (eagle.collidedHorizontally && !eagle.onGround) {
                resetTask();
            }
            if (flightTarget) {
                eagle.getMoveHelper().setMoveTo(x, y, z, 1.0D);
            } else {
                if (eagle.isFlying() && !eagle.onGround) {
                    if (!eagle.isInWater()) {
                        eagle.motionX *= 1.2D;
                        eagle.motionY *= 0.6D;
                        eagle.motionZ *= 1.2D;
                    }
                } else {
                    this.eagle.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1.0D);
                }
            }
            if (!flightTarget && this.eagle.isFlying() && eagle.onGround) {
                eagle.setFlying(false);
                orbitTime = 0;
                eagle.orbitPos = null;
                orbitResetCooldown = -400 - rand.nextInt(400);
            }
            BlockPos under = eagle.getPosition().down();
            if (this.eagle.isFlying() && (!eagle.world.isAirBlock(under) || eagle.onGround) && !eagle.isInWater() && eagle.timeFlying > 30) {
                eagle.setFlying(false);
                orbitTime = 0;
                eagle.orbitPos = null;
                orbitResetCooldown = -400 - rand.nextInt(400);
            }
        }

        @Nullable
        private Vec3d pickWanderVec() {
            Vec3d wanderFrom = eagle.getPositionVector();
            if (eagle.isTamed() && eagle.getCommand() == 1 && eagle.getOwner() != null) {
                wanderFrom = eagle.getOwner().getPositionVector();
                eagle.orbitPos = eagle.getOwner().getPosition();
            }
            if (orbitResetCooldown > 0 && eagle.orbitPos != null) {
                return eagle.getOrbitVec(wanderFrom, 4 + rand.nextInt(2));
            }
            if (eagle.isBeingRidden() || eagle.isOverWaterOrVoid()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (eagle.timeFlying < 500 || eagle.isBeingRidden() || eagle.isOverWaterOrVoid()) {
                    return eagle.getBlockInViewAway(wanderFrom, 0);
                }
                return eagle.getBlockGrounding(wanderFrom);
            }
            return RandomPositionGenerator.findRandomTarget(this.eagle, 10, 7);
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (eagle.isSitting()) {
                return false;
            }
            if (flightTarget) {
                return eagle.isFlying() && eagle.getDistanceSq(x, y, z) > 2.0D;
            }
            return (!this.eagle.getNavigator().noPath()) && !this.eagle.isBeingRidden();
        }

        @Override
        public void startExecuting() {
            if (flightTarget) {
                eagle.setFlying(true);
                eagle.getMoveHelper().setMoveTo(x, y, z, 1.0D);
            } else {
                this.eagle.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1.0D);
            }
        }

        @Override
        public void resetTask() {
            this.eagle.getNavigator().clearPath();
        }
    }

    private class AITackle extends EntityAIBase {
        protected EntityBaldEagle eagle;
        private int circleTime;
        private int maxCircleTime = 10;

        public AITackle() {
            this.eagle = EntityBaldEagle.this;
        }

        @Override
        public boolean shouldExecute() {
            return eagle.getAttackTarget() != null && !eagle.controlledFlag && !eagle.isBeingRidden();
        }

        @Override
        public void startExecuting() {
            eagle.orbitPos = null;
        }

        @Override
        public void resetTask() {
            circleTime = 0;
            maxCircleTime = 60 + rand.nextInt(60);
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = eagle.getAttackTarget();
            boolean smallPrey = (target != null && target.height < 1.0F && target.width < 0.7F && !(target instanceof EntityBaldEagle))
                    || target instanceof EntitySquid;
            if (eagle.orbitPos != null && circleTime < maxCircleTime) {
                circleTime++;
                eagle.setTackling(false);
                eagle.setFlying(true);
                if (target != null) {
                    int i = 0;
                    int up = 2 + eagle.getRNG().nextInt(4);
                    eagle.orbitPos = target.getPosition().up((int) target.height);
                    while (eagle.world.isAirBlock(eagle.orbitPos) && i < up) {
                        i++;
                        eagle.orbitPos = eagle.orbitPos.up();
                    }
                }
                Vec3d vec = eagle.getOrbitVec(Vec3d.ZERO, 4 + rand.nextInt(2));
                if (vec != null) {
                    eagle.getMoveHelper().setMoveTo(vec.x, vec.y, vec.z, 1.2D);
                }
            } else if (target != null) {
                if (eagle.isFlying() || eagle.isInWater()) {
                    double d0 = eagle.posX - target.posX;
                    double d2 = eagle.posZ - target.posZ;
                    double xzDist = Math.sqrt(d0 * d0 + d2 * d2);
                    double yAddition = target.height;
                    if (xzDist > 15.0D) {
                        yAddition = 3.0D;
                    }
                    eagle.setTackling(true);
                    eagle.getMoveHelper().setMoveTo(target.posX, target.posY + yAddition, target.posZ, eagle.isInWater() ? 1.3D : 1.0D);
                } else {
                    this.eagle.getNavigator().tryMoveToEntityLiving(target, 1.0D);
                }
                if (eagle.getDistance(target) < target.width + 2.5F) {
                    if (eagle.isTackling()) {
                        if (smallPrey) {
                            eagle.setFlying(true);
                            eagle.timeFlying = 0;
                            float radius = 0.3F;
                            float angle = 0.01745329251F * eagle.renderYawOffset;
                            double extraX = (double) radius * MathHelper.sin((float) (Math.PI + (double) angle));
                            double extraZ = (double) radius * MathHelper.cos(angle);
                            target.rotationYaw = eagle.renderYawOffset + 90.0F;
                            if (target instanceof EntityLivingBase) {
                                EntityLivingBase living = (EntityLivingBase) target;
                                living.renderYawOffset = eagle.renderYawOffset + 90.0F;
                            }
                            target.setPosition(eagle.posX + extraX, eagle.posY - 0.4F + (double) target.height * 0.45D, eagle.posZ + extraZ);
                            target.startRiding(eagle, true);
                        } else {
                            target.attackEntityFrom(DamageSource.causeMobDamage(eagle), 5.0F);
                            eagle.setFlying(false);
                            eagle.orbitPos = target.getPosition().up(2);
                            circleTime = 0;
                            maxCircleTime = 60 + rand.nextInt(60);
                        }
                    } else {
                        eagle.attackEntityAsMob(target);
                    }
                } else if (eagle.getDistance(target) > 12.0D || target.isInWater()) {
                    eagle.setFlying(true);
                }
            }
            if (eagle.isLaunched()) {
                eagle.setFlying(true);
            }
        }
    }

    private class AILandOnGlove extends EntityAIBase {
        protected EntityBaldEagle eagle;
        private int seperateTime = 0;

        public AILandOnGlove() {
            this.eagle = EntityBaldEagle.this;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            return eagle.isLaunched() && !eagle.controlledFlag && eagle.isTamed() && !eagle.isRiding() && !eagle.isBeingRidden()
                    && (eagle.getAttackTarget() == null || !eagle.getAttackTarget().isEntityAlive());
        }

        @Override
        public void updateTask() {
            double m2 = eagle.motionX * eagle.motionX + eagle.motionY * eagle.motionY + eagle.motionZ * eagle.motionZ;
            if (m2 < 0.03D) {
                seperateTime++;
            }
            EntityLivingBase owner = eagle.getOwner();
            if (owner != null) {
                if (seperateTime > 200) {
                    seperateTime = 0;
                    eagle.copyLocationAndAnglesFrom(owner);
                }
                eagle.setFlying(true);
                double d0 = eagle.posX - owner.posX;
                double d2 = eagle.posZ - owner.posZ;
                double xzDist = Math.sqrt(d0 * d0 + d2 * d2);
                double yAdd = xzDist > 14.0D ? 5.0D : 0.0D;
                eagle.getMoveHelper().setMoveTo(owner.posX, owner.posY + yAdd + (double) owner.getEyeHeight(), owner.posZ, 1.0D);

                if (this.eagle.getDistance(owner) < owner.width + 1.4D) {
                    this.eagle.setLaunched(false);
                    if (this.eagle.getRidingEagles(owner) <= 0) {
                        this.eagle.startRiding(owner);
                        if (!eagle.world.isRemote) {
                            AlexsMobs.sendMSGToAll(new MessageMosquitoMountPlayer(eagle.getEntityId(), owner.getEntityId()));
                        }
                    } else {
                        this.eagle.setCommand(2);
                        this.eagle.setSitting(true);
                    }
                }
            }
        }

        @Override
        public void resetTask() {
            seperateTime = 0;
        }
    }
}
