package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHerdPanic;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.GeladaAIGroom;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class EntityGeladaMonkey extends EntityAnimal implements IAnimatedEntity, IHerdPanic {

    public static final Animation ANIMATION_SWIPE_R = Animation.create(13);
    public static final Animation ANIMATION_SWIPE_L = Animation.create(13);
    public static final Animation ANIMATION_GROOM = Animation.create(35);
    public static final Animation ANIMATION_CHEST = Animation.create(35);

    private static final DataParameter<Boolean> LEADER = EntityDataManager.createKey(EntityGeladaMonkey.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityGeladaMonkey.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.createKey(EntityGeladaMonkey.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> GRASS_TIME = EntityDataManager.createKey(EntityGeladaMonkey.class, DataSerializers.VARINT);

    public float prevSitProgress;
    public float sitProgress;
    public boolean isGrooming = false;
    public int groomerID = -1;

    private int animationTick;
    private Animation currentAnimation;
    private int sittingTime;
    private int maxSitTime;
    private int leaderFightTime;
    private EntityAIHurtByTarget hurtByTargetGoal = null;
    private EntityAINearestTarget3D leaderFightGoal = null;
    private int revengeCooldown = 0;
    private boolean hasSpedUp = false;

    public EntityGeladaMonkey(World world) {
        super(world);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setSize(0.85F, 1.1F);
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos pos = new BlockPos(this);
        return pos.getY() >= AMConfig.geladaMonkeySpawnHeight
                && AMEntityRegistry.rollSpawn(AMConfig.geladaMonkeySpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.GELADA_MONKEY_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GELADA_MONKEY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GELADA_MONKEY_HURT;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.5D, true) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && EntityGeladaMonkey.this.revengeCooldown <= 0;
            }

            @Override
            public boolean shouldContinueExecuting() {
                return super.shouldContinueExecuting() && EntityGeladaMonkey.this.revengeCooldown <= 0;
            }
        });
        this.tasks.addTask(2, new AIClearGrass());
        this.tasks.addTask(3, new AnimalAIHerdPanic(this, 1.5D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAITempt(this, 1.0D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.GELADA_MONKEY_BREEDABLES, stack.getItem())
                        || AMTagRegistry.itemInTag(AMTagRegistry.GELADA_MONKEY_LAND_CLEARING_FOODS, stack.getItem());
            }
        });
        this.tasks.addTask(7, new GeladaAIGroom(this));
        this.tasks.addTask(8, new EntityAIWander(this, 1.0D, 120));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, hurtByTargetGoal = new EntityAIHurtByTarget(this, true, EntityGeladaMonkey.class));
        this.targetTasks.addTask(2, leaderFightGoal = new EntityAINearestTarget3D(this, EntityGeladaMonkey.class, 70, false, true, entity -> {
            return EntityGeladaMonkey.this.isLeader() && EntityGeladaMonkey.this.leaderFightTime == 0
                    && entity instanceof EntityGeladaMonkey && ((EntityGeladaMonkey) entity).isLeader() && ((EntityGeladaMonkey) entity).leaderFightTime == 0;
        }));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Leader", this.isLeader());
        compound.setInteger("GrassTime", this.getClearGrassTime());
        compound.setInteger("FightTime", this.leaderFightTime);
        compound.setBoolean("MonkeySitting", this.isSitting());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setLeader(compound.getBoolean("Leader"));
        this.setClearGrassTime(compound.getInteger("GrassTime"));
        this.setSitting(compound.getBoolean("MonkeySitting"));
        this.leaderFightTime = compound.getInteger("FightTime");
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.GELADA_MONKEY_BREEDABLES, stack.getItem());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(LEADER, false);
        this.dataManager.register(SITTING, false);
        this.dataManager.register(HAS_TARGET, false);
        this.dataManager.register(GRASS_TIME, 0);
    }

    public boolean isLeader() {
        return this.dataManager.get(LEADER) && !this.isChild();
    }

    public void setLeader(boolean leader) {
        this.dataManager.set(LEADER, leader);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isAggro() {
        return this.dataManager.get(HAS_TARGET);
    }

    public void setAggro(boolean sit) {
        this.dataManager.set(HAS_TARGET, sit);
    }

    public int getClearGrassTime() {
        return this.dataManager.get(GRASS_TIME);
    }

    public void setClearGrassTime(int i) {
        this.dataManager.set(GRASS_TIME, i);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevSitProgress = this.sitProgress;
        if (this.isSitting()) {
            if (this.sitProgress < 5F) {
                this.sitProgress++;
            }
        } else if (this.sitProgress > 0F) {
            this.sitProgress--;
        }
        if (!this.world.isRemote) {
            if (this.isSitting() && ++this.sittingTime > this.maxSitTime) {
                this.setSitting(false);
                this.sittingTime = 0;
                this.maxSitTime = 75 + this.rand.nextInt(50);
            }
            if (this.motionX * this.motionX + this.motionZ * this.motionZ < 0.03D && this.getAnimation() == NO_ANIMATION && !this.isSitting() && this.rand.nextInt(500) == 0) {
                this.sittingTime = 0;
                this.maxSitTime = 200 + this.rand.nextInt(550);
                this.setSitting(true);
            }
            if (this.isSitting() && (this.getAttackTarget() != null || this.isInLove())) {
                this.setSitting(false);
            }
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && (this.getAnimation() == ANIMATION_SWIPE_L || this.getAnimation() == ANIMATION_SWIPE_R) && this.getAnimationTick() == 7 && this.canEntityBeSeen(target) && this.getDistance(target) < this.height + target.height + 1) {
                target.knockBack(this, 0.4F, target.posX - this.posX, target.posZ - this.posZ);
                float dmg = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                if (this.isLeader() && target instanceof EntityGeladaMonkey) {
                    EntityGeladaMonkey monkey = (EntityGeladaMonkey) target;
                    if (monkey.isLeader()) {
                        monkey.setAttackTarget(this);
                        monkey.leaderFightTime = this.leaderFightTime;
                        dmg = 0;
                    }
                }
                target.attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
            }
            if (target != null && target.isEntityAlive()) {
                this.setAggro(true);
                if (this.isLeader() && target instanceof EntityGeladaMonkey) {
                    EntityGeladaMonkey monkey = (EntityGeladaMonkey) target;
                    if (monkey.isLeader()) {
                        this.leaderFightTime++;
                    }
                    if (Math.max(this.leaderFightTime, monkey.leaderFightTime) >= 250) {
                        this.resetAttackAI();
                        monkey.resetAttackAI();
                    }
                    if (this.leaderFightTime < 10 && this.rand.nextInt(5) == 0 && this.getAnimation() == NO_ANIMATION) {
                        this.setAnimation(ANIMATION_CHEST);
                    }
                }
            } else {
                this.setAggro(false);
            }
            if (this.leaderFightTime < 0) {
                this.leaderFightTime++;
            }
        }
        if (this.isAggro()) {
            if (!this.hasSpedUp) {
                this.hasSpedUp = true;
                this.setSprinting(true);
                this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.31D);
            }
        } else if (this.hasSpedUp) {
            this.hasSpedUp = false;
            this.setSprinting(false);
            this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        }
        if (this.getClearGrassTime() > 0) {
            this.setClearGrassTime(this.getClearGrassTime() - 1);
        }
        if (this.getClearGrassTime() < 0) {
            this.setClearGrassTime(this.getClearGrassTime() + 1);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    private void resetAttackAI() {
        this.leaderFightTime = -500 - this.rand.nextInt(2000);
        this.setAttackTarget(null);
        this.setRevengeTarget(null);
        if (this.leaderFightGoal != null) {
            this.leaderFightGoal.resetTask();
        }
        if (this.hurtByTargetGoal != null) {
            this.hurtByTargetGoal.resetTask();
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            attackAnimation();
        }
        return true;
    }

    public float getGeladaScale() {
        return this.isChild() ? 0.5F : this.isLeader() ? 1.15F : 1.0F;
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
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
        return new Animation[]{ANIMATION_SWIPE_R, ANIMATION_SWIPE_L, ANIMATION_GROOM, ANIMATION_CHEST};
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            Entity direct = source.getTrueSource();
            if (direct instanceof EntityGeladaMonkey) {
                this.revengeCooldown = 100 + this.getRNG().nextInt(5);
                this.revengeCooldown = 10 + this.getRNG().nextInt(30);
            }
        }
        return prev;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean type = super.processInteract(player, hand);
        if (AMTagRegistry.itemInTag(AMTagRegistry.GELADA_MONKEY_LAND_CLEARING_FOODS, itemstack.getItem()) && this.getClearGrassTime() == 0) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.eatGrassWithBuddies(3 + this.rand.nextInt(2));
            return true;
        }
        return type;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable mob) {
        EntityGeladaMonkey baby = (EntityGeladaMonkey) AMEntityRegistry.GELADA_MONKEY.newInstance(this.world);
        baby.setLeader(this.rand.nextInt(2) == 0);
        return baby;
    }

    public void eatGrassWithBuddies(int otherMonkies) {
        int i = 300 + this.rand.nextInt(300);
        this.setClearGrassTime(i);
        int monky = 0;
        for (EntityGeladaMonkey entity : this.world.getEntitiesWithinAABB(EntityGeladaMonkey.class, this.getEntityBoundingBox().grow(15F))) {
            if (monky < otherMonkies && entity.getEntityId() != this.getEntityId() && !entity.shouldStopBeingGroomed()) {
                monky++;
                entity.setClearGrassTime(i);
            }
        }
    }

    @Override
    public void onPanic() {
    }

    @Override
    public boolean canPanic() {
        return this.getRevengeTarget() instanceof EntityGeladaMonkey && this.rand.nextInt(3) == 0;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting() || this.getAnimation() == ANIMATION_CHEST) {
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
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            int others = 0;
            for (EntityGeladaMonkey other : this.world.getEntitiesWithinAABB(EntityGeladaMonkey.class, this.getEntityBoundingBox().grow(2.0D, 1.0D, 2.0D))) {
                if (other != this) {
                    others++;
                }
            }
            if (others == 0 || (others > 4 && this.rand.nextInt(2) == 0)) {
                this.setLeader(true);
            } else {
                this.setLeader(this.rand.nextInt(4) == 0);
            }
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public boolean canBeGroomed() {
        return this.groomerID == -1;
    }

    public boolean shouldStopBeingGroomed() {
        return this.getAttackTarget() != null && this.getAttackTarget().isEntityAlive() || this.isInLove() || this.revengeCooldown > 0;
    }

    private void attackAnimation() {
        this.setAnimation(this.rand.nextBoolean() ? ANIMATION_SWIPE_L : ANIMATION_SWIPE_R);
    }

    private class AIClearGrass extends net.minecraft.entity.ai.EntityAIBase {

        private BlockPos target;

        public AIClearGrass() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityGeladaMonkey.this.getClearGrassTime() > 0) {
                target = generateTarget();
                return target != null;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && AMTagRegistry.blockInTag(AMTagRegistry.GELADA_MONKEY_GRASS, EntityGeladaMonkey.this.world.getBlockState(target).getBlock());
        }

        @Override
        public void updateTask() {
            EntityGeladaMonkey.this.setSitting(false);
            EntityGeladaMonkey.this.getNavigator().tryMoveToXYZ(target.getX() + 0.5F, target.getY() + 0.5F, target.getZ() + 0.5F, 1.4D);
            if (EntityGeladaMonkey.this.getDistanceSq(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5) < 3.4F) {
                if (EntityGeladaMonkey.this.getAnimation() == NO_ANIMATION) {
                    EntityGeladaMonkey.this.attackAnimation();
                } else if (EntityGeladaMonkey.this.getAnimationTick() > 7) {
                    EntityGeladaMonkey.this.world.destroyBlock(target, true);
                }
            }
        }

        @Nullable
        public BlockPos generateTarget() {
            BlockPos blockpos = null;
            Random random = new Random();
            int range = 7;
            for (int i = 0; i < 15; i++) {
                BlockPos blockpos1 = EntityGeladaMonkey.this.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
                while (EntityGeladaMonkey.this.world.isAirBlock(blockpos1) && blockpos1.getY() > 0) {
                    blockpos1 = blockpos1.down();
                }
                IBlockState state = EntityGeladaMonkey.this.world.getBlockState(blockpos1);
                if (AMTagRegistry.blockInTag(AMTagRegistry.GELADA_MONKEY_GRASS, state.getBlock())) {
                    blockpos = blockpos1;
                }
            }
            return blockpos;
        }
    }
}
