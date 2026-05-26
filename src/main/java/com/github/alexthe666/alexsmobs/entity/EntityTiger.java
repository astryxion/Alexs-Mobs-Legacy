package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.entity.ai.MovementControllerCustomCollisions;
import com.github.alexthe666.alexsmobs.entity.ai.ResetAngerGoal;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class EntityTiger extends EntityAnimal implements ICustomCollisions, IAnimatedEntity, IAngerable, ITargetsDroppedItems {

    public static final Animation ANIMATION_PAW_R = Animation.create(15);
    public static final Animation ANIMATION_PAW_L = Animation.create(15);
    public static final Animation ANIMATION_TAIL_FLICK = Animation.create(45);
    public static final Animation ANIMATION_LEAP = Animation.create(20);
    private static final DataParameter<Boolean> WHITE = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> RUNNING = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLEEPING = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STEALTH_MODE = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HOLDING = EntityDataManager.createKey(EntityTiger.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ANGER_TIME = EntityDataManager.createKey(EntityTiger.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LAST_SCARED_MOB_ID = EntityDataManager.createKey(EntityTiger.class, DataSerializers.VARINT);
    private static final Predicate<EntityLivingBase> NO_BLESSING_EFFECT = mob -> !mob.isPotionActive(AMEffectRegistry.TIGERS_BLESSING);
    public float prevSitProgress;
    public float sitProgress;
    public float prevSleepProgress;
    public float sleepProgress;
    public float prevHoldProgress;
    public float holdProgress;
    public float prevStealthProgress;
    public float stealthProgress;
    private int animationTick;
    private Animation currentAnimation;
    private boolean hasSpedUp = false;
    private UUID lastHurtBy;
    private int sittingTime;
    private int maxSitTime;
    private int holdTime = 0;
    private int prevScaredMobId = -1;
    private boolean dontSitFlag = false;
    private long lastPlayerAttackGameTime = -1000000L;

    public EntityTiger(World worldIn) {
        super(worldIn);
        this.setSize(1.45F, 1.2F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.moveHelper = new MovementControllerCustomCollisions(this);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.tigerSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public boolean isNotColliding() {
        return !this.world.containsAnyLiquid(this.getEntityBoundingBox());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(86.0D);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        IBlockState below = this.world.getBlockState(pos.down());
        IBlockState at = this.world.getBlockState(pos);
        return below.getMaterial() != Material.WATER && at.getMaterial() == Material.WATER ? 0.0F : super.getBlockPathWeight(pos);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("TigerSitting", this.isSitting());
        compound.setBoolean("TigerSleeping", this.isSleeping());
        compound.setBoolean("White", this.isWhite());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("TigerSitting"));
        this.setSleeping(compound.getBoolean("TigerSleeping"));
        this.setWhite(compound.getBoolean("White"));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(WHITE, Boolean.valueOf(false));
        this.dataManager.register(RUNNING, Boolean.valueOf(false));
        this.dataManager.register(SITTING, Boolean.valueOf(false));
        this.dataManager.register(STEALTH_MODE, Boolean.valueOf(false));
        this.dataManager.register(HOLDING, Boolean.valueOf(false));
        this.dataManager.register(SLEEPING, Boolean.valueOf(false));
        this.dataManager.register(ANGER_TIME, 0);
        this.dataManager.register(LAST_SCARED_MOB_ID, -1);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(3, new AIMelee());
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 60, 1.0D, 14, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 25.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, 10));
        this.targetTasks.addTask(2, new AngerGoal(this));
        this.targetTasks.addTask(3, new AttackPlayerGoal());
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class, 10, false, false, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.TIGER_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return !EntityTiger.this.isChild() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(5, new ResetAngerGoal(this, true));
    }

    protected SoundEvent getAmbientSound() {
        return isStealth() ? super.getAmbientSound() : getAngerTime() > 0 ? AMSoundRegistry.TIGER_ANGRY : AMSoundRegistry.TIGER_IDLE;
    }

    public int getTalkInterval() {
        return getAngerTime() > 0 ? 40 : 80;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TIGER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TIGER_HURT;
    }



    protected float getWaterSlowDown() {
        return 0.99F;
    }

    public boolean shouldMove() {
        return !isSitting() && !isSleeping() && !this.isHolding();
    }

    public double getVisibilityMultiplier(@Nullable Entity lookingEntity) {
        if (this.isStealth()) {
            return 0.2D;
        }
        return 1.0D;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.TIGER_BREEDABLES, stack.getItem());
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        boolean flag = super.attackEntityAsMob(entityIn);
        if (flag && entityIn instanceof EntityLivingBase && !((EntityLivingBase) entityIn).isEntityAlive()) {
            this.heal(5);
        }
        return flag;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.shouldMove()) {
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
    protected PathNavigateGround createNavigator(World worldIn) {
        return new Navigator(this, worldIn);
    }

    public boolean isWhite() {
        return this.dataManager.get(WHITE).booleanValue();
    }

    public void setWhite(boolean white) {
        this.dataManager.set(WHITE, Boolean.valueOf(white));
    }

    public boolean isRunning() {
        return this.dataManager.get(RUNNING).booleanValue();
    }

    public void setRunning(boolean running) {
        this.dataManager.set(RUNNING, Boolean.valueOf(running));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean bar) {
        this.dataManager.set(SITTING, Boolean.valueOf(bar));
    }

    public boolean isStealth() {
        return this.dataManager.get(STEALTH_MODE).booleanValue();
    }

    public void setStealth(boolean bar) {
        this.dataManager.set(STEALTH_MODE, Boolean.valueOf(bar));
    }

    public boolean isHolding() {
        return this.dataManager.get(HOLDING).booleanValue();
    }

    public void setHolding(boolean running) {
        this.dataManager.set(HOLDING, Boolean.valueOf(running));
    }

    public boolean isSleeping() {
        return this.dataManager.get(SLEEPING).booleanValue();
    }

    public void setSleeping(boolean sleeping) {
        this.dataManager.set(SLEEPING, Boolean.valueOf(sleeping));
    }

    public int getAngerTime() {
        return this.dataManager.get(ANGER_TIME);
    }

    public void setAngerTime(int time) {
        this.dataManager.set(ANGER_TIME, time);
    }

    public UUID getAngerTarget() {
        return this.lastHurtBy;
    }

    public void setAngerTarget(@Nullable UUID target) {
        this.lastHurtBy = target;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(40 + this.rand.nextInt(41));
    }

    @Override
    public void resetTargets() {
        this.setAngerTime(0);
        this.setAngerTarget(null);
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
    }

    @Override
    public boolean wasHurtByPlayerRecently() {
        return world.getTotalWorldTime() - this.lastPlayerAttackGameTime <= 100L;
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        if (!this.world.isRemote) {
            tickIAngerable();
        }
    }

    private void tickIAngerable() {
        UUID id = this.getAngerTarget();
        if (this.getAngerTime() > 0) {
            this.setAngerTime(this.getAngerTime() - 1);
        }
        if (id != null) {
            Entity e = null;
            if (this.world instanceof WorldServer) {
                e = ((WorldServer) this.world).getEntityFromUuid(id);
            }
            if (e instanceof EntityLivingBase) {
                if (this.getAngerTime() > 0 && this.getAttackTarget() != e) {
                    this.setAttackTarget((EntityLivingBase) e);
                }
            } else if (this.getAngerTime() <= 0) {
                this.setAngerTarget(null);
                this.setAttackTarget(null);
            }
        } else if (this.getAngerTime() <= 0) {
            this.setAttackTarget(null);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevSitProgress = sitProgress;
        prevSleepProgress = sleepProgress;
        prevHoldProgress = holdProgress;
        prevStealthProgress = stealthProgress;
        if (isSitting() && sitProgress < 5F) {
            sitProgress++;
        }
        if (!isSitting() && sitProgress > 0F) {
            sitProgress--;
        }
        if (isSleeping() && sleepProgress < 5F) {
            sleepProgress++;
        }
        if (!isSleeping() && sleepProgress > 0F) {
            sleepProgress--;
        }
        if (isHolding() && holdProgress < 5F) {
            holdProgress++;
        }
        if (!isHolding() && holdProgress > 0F) {
            holdProgress--;
        }
        if (isStealth() && stealthProgress < 10F) {
            stealthProgress += 0.25F;
        }
        if (!isStealth() && stealthProgress > 0F) {
            stealthProgress--;
        }
        if (!world.isRemote) {
            if (isRunning() && !hasSpedUp) {
                hasSpedUp = true;
                stepHeight = 1F;
                this.setSprinting(true);
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
            }
            if (!isRunning() && hasSpedUp) {
                hasSpedUp = false;
                this.stepHeight = 0.6F;
                this.setSprinting(false);
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
            }
            if ((isSitting() || isSleeping()) && (++sittingTime > maxSitTime || this.getAttackTarget() != null || this.isInLove() || dontSitFlag || this.isInWater())) {
                this.setSitting(false);
                this.setSleeping(false);
                sittingTime = 0;
                maxSitTime = 100 + rand.nextInt(50);
            }
            if (this.getAttackTarget() == null && !dontSitFlag && this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.03D && this.getAnimation() == IAnimatedEntity.NO_ANIMATION && !this.isSleeping() && !this.isSitting() && !this.isInWater() && rand.nextInt(100) == 0) {
                sittingTime = 0;
                if (this.getRNG().nextBoolean()) {
                    maxSitTime = 100 + rand.nextInt(550);
                    this.setSitting(true);
                    this.setSleeping(false);
                } else {
                    maxSitTime = 200 + rand.nextInt(550);
                    this.setSitting(false);
                    this.setSleeping(true);
                }
            }
            if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 0.03D && this.getAnimation() == IAnimatedEntity.NO_ANIMATION && !this.isSleeping() && !this.isSitting() && rand.nextInt(100) == 0) {
                this.setAnimation(ANIMATION_TAIL_FLICK);
            }
        }
        if (this.isHolding()) {
            this.setSprinting(false);
            this.setRunning(false);
            if (!world.isRemote && this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive()) {
                this.rotationPitch = 0;
                float radius = 1.0F + this.getAttackTarget().width * 0.5F;
                float angle = (0.01745329251F * this.renderYawOffset);
                double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                double extraZ = radius * MathHelper.cos(angle);
                double extraY = -0.5F;
                EntityLivingBase target = this.getAttackTarget();
                target.motionX = this.posX + extraX - target.posX;
                target.motionY = this.posY + extraY - target.posY;
                target.motionZ = this.posZ + extraZ - target.posZ;
                if (holdTime % 20 == 0) {
                    this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), 5 + this.getRNG().nextInt(2));
                }
            }
            holdTime++;
            if (holdTime > 100) {
                holdTime = 0;
                this.setHolding(false);
            }
        } else {
            holdTime = 0;
        }
        if (prevScaredMobId != this.dataManager.get(LAST_SCARED_MOB_ID) && world.isRemote) {
            Entity e = world.getEntityByID(this.dataManager.get(LAST_SCARED_MOB_ID).intValue());
            if (e != null) {
                double d2 = this.rand.nextGaussian() * 0.1D;
                double d0 = this.rand.nextGaussian() * 0.1D;
                double d1 = this.rand.nextGaussian() * 0.1D;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.SHOCKED, e.posX, e.posY + e.getEyeHeight() + e.height * 0.15F + (double) (this.rand.nextFloat() * e.height * 0.15F), e.posZ, d0, d1, d2);
            }
        }
        if(this.getAttackTarget() != null && this.getAttackTarget().isPotionActive(AMEffectRegistry.TIGERS_BLESSING)){
            this.setAttackTarget(null);
            this.setRevengeTarget(null);
        }
        prevScaredMobId = this.dataManager.get(LAST_SCARED_MOB_ID);
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            if (source.getTrueSource() instanceof EntityPlayer) {
                this.lastPlayerAttackGameTime = world.getTotalWorldTime();
            }
            if (source.getTrueSource() != null) {
                if (source.getTrueSource() instanceof EntityLivingBase) {
                    EntityLivingBase hurter = (EntityLivingBase) source.getTrueSource();
                    if (hurter.isPotionActive(AMEffectRegistry.TIGERS_BLESSING)) {
                        hurter.removePotionEffect(AMEffectRegistry.TIGERS_BLESSING);
                    }
                }
            }
            return prev;
        }
        return prev;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public BlockPos getLightPosition() {
        BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
        if (!world.getBlockState(pos).isOpaqueCube()) {
            return pos.up();
        }
        return pos;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        boolean whiteOther = ageable instanceof EntityTiger && ((EntityTiger) ageable).isWhite();
        EntityTiger baby = (EntityTiger) AMEntityRegistry.TIGER.newInstance(this.world);
        double whiteChance = 0.1D;
        if (this.isWhite() && whiteOther) {
            whiteChance = 0.8D;
        }
        if (this.isWhite() != whiteOther) {
            whiteChance = 0.4D;
        }
        baby.setWhite(rand.nextDouble() < whiteChance);
        return baby;
    }

    public Vec3d getAllowedMovement(Vec3d vec) {
        return com.github.alexthe666.citadel.server.entity.collision.ICustomCollisions.getAllowedMovementForEntity(this, vec);
    }

    @Override
    public boolean canPassThrough(BlockPos pos, IBlockState blockstate, AxisAlignedBB collision) {
        return blockstate.getBlock() instanceof BlockLeaves;
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_PAW_R, ANIMATION_PAW_L, ANIMATION_LEAP, ANIMATION_TAIL_FLICK};
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    public void applyEntityCollision(Entity entityIn) {
        if (!this.isHolding() || entityIn != this.getAttackTarget()) {
            super.applyEntityCollision(entityIn);
        }
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {
        if (!this.isHolding() || entityIn != this.getAttackTarget()) {
            super.collideWithEntity(entityIn);
        }
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemFood && ((ItemFood) item).isWolfsFavoriteMeat() && item != Items.ROTTEN_FLESH;
    }

    public double getMaxDistToItem() {
        return 3.0D;
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.dontSitFlag = false;
        ItemStack stack = e.getItem();
        Item item = stack.getItem();
        if (item instanceof ItemFood && ((ItemFood) item).isWolfsFavoriteMeat() && item != Items.ROTTEN_FLESH) {
            this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
            this.heal(5);
            EntityPlayer throwerPlayer = e.getThrower() != null ? world.getPlayerEntityByName(e.getThrower()) : null;
            if (throwerPlayer != null && rand.nextFloat() < getChanceForEffect(stack)) {
                EntityPlayer player = throwerPlayer;
                player.addPotionEffect(new PotionEffect(AMEffectRegistry.TIGERS_BLESSING, 12000));
                this.setAttackTarget(null);
                this.setRevengeTarget(null);
            }
        }
    }

    public void onFindTarget(EntityItem e) {
        this.dontSitFlag = true;
        this.setSitting(false);
        this.setSleeping(false);
    }

    public double getChanceForEffect(ItemStack stack) {
        if (stack.getItem() == Items.PORKCHOP || stack.getItem() == Items.COOKED_PORKCHOP) {
            return 0.4F;
        }
        if (stack.getItem() == Items.CHICKEN || stack.getItem() == Items.COOKED_CHICKEN) {
            return 0.3F;
        }
        return 0.1F;
    }

    protected void jump() {
        if (!this.isSleeping() && !this.isSitting()) {
            super.jump();
        }
    }

    static class TigerNodeProcessor extends WalkNodeProcessor {

        private TigerNodeProcessor() {
        }

        private PathNodeType getFloorNodeTypeFor(IBlockAccess access, int x, int y, int z) {
            PathNodeType pathnodetype = getNodes(access, x, y, z);
            if (pathnodetype == PathNodeType.OPEN && y >= 1) {
                PathNodeType pathnodetype1 = getNodes(access, x, y - 1, z);
                pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
                if (pathnodetype1 == PathNodeType.DAMAGE_FIRE) {
                    pathnodetype = PathNodeType.DAMAGE_FIRE;
                }
                if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
                    pathnodetype = PathNodeType.DAMAGE_CACTUS;
                }
                if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
                    pathnodetype = PathNodeType.DAMAGE_OTHER;
                }
            }
            if (pathnodetype == PathNodeType.WALKABLE) {
                pathnodetype = this.checkNeighborBlocks(access, x, y, z, pathnodetype);
            }
            return pathnodetype;
        }

        private PathNodeType getNodes(IBlockAccess access, int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState blockstate = access.getBlockState(pos);
            PathNodeType type = blockstate.getBlock().getAiPathNodeType(blockstate, access, pos, null);
            if (type != null) {
                return type;
            }
            if (blockstate.getMaterial() == Material.AIR) {
                return PathNodeType.OPEN;
            }
            if (blockstate.getBlock() instanceof BlockLeaves) {
                return PathNodeType.OPEN;
            }
            return this.getPathNodeTypeRaw(access, x, y, z);
        }

        @Override
        public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z) {
            return getFloorNodeTypeFor(blockaccessIn, x, y, z);
        }

        @Override
        public PathNodeType getPathNodeType(IBlockAccess world, int x, int y, int z, EntityLiving entitylivingIn, int xSize, int ySize, int zSize, boolean canBreakDoors, boolean canEnterDoors) {
            PathNodeType type = getFloorNodeTypeFor(world, x, y, z);
            if (type == PathNodeType.OPEN || world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof BlockLeaves) {
                return PathNodeType.OPEN;
            }
            return super.getPathNodeType(world, x, y, z, entitylivingIn, xSize, ySize, zSize, canBreakDoors, canEnterDoors);
        }
    }

    class Navigator extends GroundPathNavigatorWide {

        Navigator(EntityLiving mob, World world) {
            super(mob, world, 1.2F);
        }

        @Override
        protected PathFinder getPathFinder() {
            this.nodeProcessor = new TigerNodeProcessor();
            return new PathFinder(this.nodeProcessor);
        }

        @Override
        protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
            int i = MathHelper.floor(posVec31.x);
            int j = MathHelper.floor(posVec31.z);
            double d0 = posVec32.x - posVec31.x;
            double d1 = posVec32.z - posVec31.z;
            double d2 = d0 * d0 + d1 * d1;
            if (d2 < 1.0E-8D) {
                return false;
            } else {
                double d3 = 1.0D / Math.sqrt(d2);
                d0 = d0 * d3;
                d1 = d1 * d3;
                sizeX = sizeX + 2;
                sizeZ = sizeZ + 2;
                if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
                    return false;
                } else {
                    sizeX = sizeX - 2;
                    sizeZ = sizeZ - 2;
                    double d4 = 1.0D / Math.abs(d0);
                    double d5 = 1.0D / Math.abs(d1);
                    double d6 = (double) i - posVec31.x;
                    double d7 = (double) j - posVec31.z;
                    if (d0 >= 0.0D) {
                        ++d6;
                    }

                    if (d1 >= 0.0D) {
                        ++d7;
                    }

                    d6 = d6 / d0;
                    d7 = d7 / d1;
                    int k = d0 < 0.0D ? -1 : 1;
                    int l = d1 < 0.0D ? -1 : 1;
                    int i1 = MathHelper.floor(posVec32.x);
                    int j1 = MathHelper.floor(posVec32.z);
                    int k1 = i1 - i;
                    int l1 = j1 - j;

                    while (k1 * k > 0 || l1 * l > 0) {
                        if (d6 < d7) {
                            d6 += d4;
                            i += k;
                            k1 = i1 - i;
                        } else {
                            d7 += d5;
                            j += l;
                            l1 = j1 - j;
                        }

                        if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
                            return false;
                        }
                    }

                    return true;
                }
            }
        }

        private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d p_179692_7_, double p_179692_8_, double p_179692_10_) {
            for (BlockPos blockpos : BlockPos.getAllInBox(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
                double d0 = (double) blockpos.getX() + 0.5D - p_179692_7_.x;
                double d1 = (double) blockpos.getZ() + 0.5D - p_179692_7_.z;
                if (!(d0 * p_179692_8_ + d1 * p_179692_10_ < 0.0D) && !this.world.getBlockState(blockpos).getBlock().isPassable(this.world, blockpos) || EntityTiger.this.canPassThrough(blockpos, world.getBlockState(blockpos), null)) {
                    return false;
                }
            }

            return true;
        }

        private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d vec31, double p_179683_8_, double p_179683_10_) {
            int i = x - sizeX / 2;
            int j = z - sizeZ / 2;
            if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, p_179683_8_, p_179683_10_)) {
                return false;
            } else {
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (int k = i; k < i + sizeX; ++k) {
                    for (int l = j; l < j + sizeZ; ++l) {
                        double d0 = (double) k + 0.5D - vec31.x;
                        double d1 = (double) l + 0.5D - vec31.z;
                        if (!(d0 * p_179683_8_ + d1 * p_179683_10_ < 0.0D)) {
                            PathNodeType pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y - 1, l);
                            mutable.setPos(k, y - 1, l);
                            if (!this.isPathNodeTypeWalkable(pathnodetype) || EntityTiger.this.canPassThrough(mutable, world.getBlockState(mutable), null)) {
                                return false;
                            }

                            pathnodetype = this.nodeProcessor.getPathNodeType(this.world, k, y, l);
                            float f = this.entity.getPathPriority(pathnodetype);
                            if (f < 0.0F || f >= 8.0F) {
                                return false;
                            }

                            if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE || pathnodetype == PathNodeType.DAMAGE_OTHER) {
                                return false;
                            }
                        }
                    }
                }

                return true;
            }
        }

        private boolean isPathNodeTypeWalkable(PathNodeType p_230287_1_) {
            if (p_230287_1_ == PathNodeType.WATER) {
                return false;
            } else if (p_230287_1_ == PathNodeType.LAVA) {
                return false;
            } else {
                return p_230287_1_ != PathNodeType.OPEN;
            }
        }
    }

    private class AIMelee extends EntityAIBase {
        private final EntityTiger tiger;
        private int jumpAttemptCooldown = 0;

        AIMelee() {
            this.tiger = EntityTiger.this;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return tiger.getAttackTarget() != null && tiger.getAttackTarget().isEntityAlive();
        }

        @Override
        public void updateTask() {
            if (jumpAttemptCooldown > 0) {
                jumpAttemptCooldown--;
            }
            EntityLivingBase target = tiger.getAttackTarget();
            if (target != null && target.isEntityAlive()) {
                double dist = tiger.getDistance(target);
                if (tiger.getRevengeTarget() != null && tiger.getRevengeTarget().isEntityAlive() && dist < 10) {
                    tiger.setStealth(false);
                } else {
                    if (dist > 20) {
                        tiger.setRunning(false);
                        tiger.setStealth(true);
                    }
                }
                if (dist <= 20) {
                    tiger.setStealth(false);
                    tiger.setRunning(true);
                    if (tiger.dataManager.get(LAST_SCARED_MOB_ID) != target.getEntityId()) {
                        tiger.dataManager.set(LAST_SCARED_MOB_ID, target.getEntityId());
                        target.addPotionEffect(new PotionEffect(AMEffectRegistry.FEAR, 100, 0, true, false));
                    }
                }
                if (dist < 12 && tiger.getAnimation() == IAnimatedEntity.NO_ANIMATION && tiger.onGround && jumpAttemptCooldown == 0 && !tiger.isHolding()) {
                    tiger.setAnimation(ANIMATION_LEAP);
                    jumpAttemptCooldown = 70;
                }
                if ((jumpAttemptCooldown > 0 || tiger.isInWater()) && !tiger.isHolding() && tiger.getAnimation() == IAnimatedEntity.NO_ANIMATION && dist < 4 + target.width) {
                    tiger.setAnimation(tiger.getRNG().nextBoolean() ? ANIMATION_PAW_L : ANIMATION_PAW_R);
                }
                if (dist < 4 + target.width && (tiger.getAnimation() == ANIMATION_PAW_L || tiger.getAnimation() == ANIMATION_PAW_R) && tiger.getAnimationTick() == 8) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(tiger), 7 + tiger.getRNG().nextInt(5));
                }
                if (tiger.getAnimation() == ANIMATION_LEAP) {
                    tiger.getNavigator().clearPath();
                    tiger.rotationYaw = -((float) MathHelper.atan2(target.posX - tiger.posX, target.posZ - tiger.posZ)) * (180F / (float) Math.PI);
                    tiger.renderYawOffset = tiger.rotationYaw;

                    if (tiger.getAnimationTick() == 5 && tiger.onGround) {
                        double dx = target.posX - this.tiger.posX;
                        double dz = target.posZ - this.tiger.posZ;
                        double len = dx * dx + dz * dz;
                        if (len > 1.0E-7D) {
                            len = Math.sqrt(len);
                            double scale = Math.min(dist, 15) * 0.2F / len;
                            dx *= scale;
                            dz *= scale;
                        }
                        this.tiger.motionX = dx;
                        this.tiger.motionY = 0.3D + 0.1D * MathHelper.clamp((float) (target.getPositionEyes(1.0F).y - this.tiger.posY), 0, 2);
                        this.tiger.motionZ = dz;
                    }
                    if (dist < target.width + 3 && tiger.getAnimationTick() >= 15) {
                        target.attackEntityFrom(DamageSource.causeMobDamage(tiger), 2);
                        tiger.setRunning(false);
                        tiger.setStealth(false);
                        tiger.setHolding(true);
                    }
                } else {
                    if (tiger.isHolding()) {
                        tiger.getNavigator().clearPath();
                    } else {
                        try{
                            tiger.getNavigator().tryMoveToEntityLiving(target, tiger.isStealth() ? 0.75F : 1.0F);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void resetTask() {
            tiger.setStealth(false);
            tiger.setRunning(false);
            tiger.setHolding(false);
        }
    }

    class AttackPlayerGoal extends EntityAINearestAttackableTarget<EntityPlayer> {

        AttackPlayerGoal() {
            super(EntityTiger.this, EntityPlayer.class, 100, false, true, NO_BLESSING_EFFECT);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityTiger.this.isChild()) {
                return false;
            } else {
                return super.shouldExecute();
            }
        }

        protected double getTargetDistance() {
            return 4.0D;
        }
    }

    class AngerGoal extends EntityAIHurtByTarget {
        AngerGoal(EntityTiger tigerIn) {
            super(tigerIn, true);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return EntityTiger.this.isAngry() && super.shouldContinueExecuting();
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            if (EntityTiger.this.isChild()) {
                this.alertOthers();
                this.resetTask();
            }
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
            if (!EntityTiger.this.isChild()) {
                super.setEntityAttackTarget(creature, target);
            }
        }
    }
}
