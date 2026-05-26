package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EntityAlligatorSnappingTurtle extends EntityAnimal implements ISemiAquatic, IShearable {

    public static final Predicate<EntityLivingBase> TARGET_PRED = new Predicate<EntityLivingBase>() {
        @Override
        public boolean apply(@Nullable EntityLivingBase animal) {
            return animal != null
                    && !(animal instanceof EntityAlligatorSnappingTurtle)
                    && EntitySelectors.CAN_AI_TARGET.apply(animal)
                    && !(animal instanceof EntityArmorStand)
                    && animal.isEntityAlive();
        }
    };

    private static final DataParameter<Byte> CLIMBING = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> MOSS = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> WAITING = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ATTACK_TARGET_FLAG = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> LUNGE_FLAG = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> TURTLE_SCALE = EntityDataManager.createKey(EntityAlligatorSnappingTurtle.class, DataSerializers.FLOAT);

    public float openMouthProgress;
    public float prevOpenMouthProgress;
    public float attackProgress;
    public float prevAttackProgress;
    public int chaseTime = 0;
    private int biteTick = 0;
    private int waitTime = 0;
    private int timeUntilWait = 0;
    private int mossTime = 0;

    public EntityAlligatorSnappingTurtle(World worldIn) {
        super(worldIn);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, 0.0F);
        this.setPathPriority(net.minecraft.pathfinding.PathNodeType.WATER, 0.0F);
        this.setSize(1.95F, 0.65F);
        this.stepHeight = 1.0F;
    }

    /**
     * World spawn check aligned with {@link AMEntityRegistry} predicate + height vs sea level (1.16 behavior).
     */
    public static boolean canTurtleSpawn(World worldIn, BlockPos pos) {
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.ALLIGATOR_SNAPPING_TURTLE_SPAWNS, worldIn.getBlockState(pos.down()).getBlock());
        return spawnBlock && pos.getY() < worldIn.getSeaLevel() + 4;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.7D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.ALLIGATOR_SNAPPING_TURTLE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.ALLIGATOR_SNAPPING_TURTLE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.ALLIGATOR_SNAPPING_TURTLE_HURT;
    }

    public float getRenderScale() {
        return this.isChild() ? 0.3F : 1.0F;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.3D, false));
        this.tasks.addTask(2, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new AnimalAILeaveWater(this));
        this.tasks.addTask(3, new BottomFeederAIWander(this, 1.0D, 120, 150, 10));
        this.tasks.addTask(3, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(5, new EntityAILookIdle(this));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true) {
            @Override
            public boolean shouldContinueExecuting() {
                return chaseTime >= 0 && super.shouldContinueExecuting();
            }
        });
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityLivingBase.class, 2, false, true, TARGET_PRED));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == Items.FISH && stack.getMetadata() == 0;
    }

    @Override
    public boolean isOnLadder() {
        return this.isBesideClimbableBlock();
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CLIMBING, (byte) 0);
        this.dataManager.register(MOSS, 0);
        this.dataManager.register(TURTLE_SCALE, 1F);
        this.dataManager.register(WAITING, false);
        this.dataManager.register(ATTACK_TARGET_FLAG, false);
        this.dataManager.register(LUNGE_FLAG, false);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevOpenMouthProgress = openMouthProgress;
        prevAttackProgress = attackProgress;
        boolean attack = this.dataManager.get(LUNGE_FLAG);
        boolean open = this.isWaiting() || this.dataManager.get(ATTACK_TARGET_FLAG) && !attack;
        if (attack && attackProgress < 5) {
            attackProgress++;
        }
        if (!attack && attackProgress > 0) {
            attackProgress--;
        }
        if (open && openMouthProgress < 5) {
            openMouthProgress++;
        }
        if (!open && openMouthProgress > 0) {
            openMouthProgress--;
        }
        if (this.attackProgress == 4 && this.isEntityAlive() && this.getAttackTarget() != null && this.canEntityBeSeen(this.getAttackTarget()) && this.getDistance(this.getAttackTarget()) < 2.3D) {
            float dmg = this.isChild() ? 1F : (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
            this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this), dmg);
        }
        if (this.attackProgress > 4) {
            biteTick = 5;
        }
        if (biteTick > 0) {
            biteTick--;
        }
        if (chaseTime < 0) {
            chaseTime++;
        }
        if (!this.world.isRemote) {
            this.setBesideClimbableBlock(this.collidedHorizontally && this.isInWater());
            if (this.isWaiting()) {
                waitTime++;
                timeUntilWait = 1500;
                if (waitTime > 1500 || this.getAttackTarget() != null) {
                    this.setWaiting(false);
                }
            } else {
                timeUntilWait--;
                waitTime = 0;
            }
            if ((this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive()) && timeUntilWait <= 0 && this.isInWater()) {
                this.setWaiting(true);
            }
            if (this.getAttackTarget() != null && biteTick == 0) {
                this.setWaiting(false);
                chaseTime++;
                this.dataManager.set(ATTACK_TARGET_FLAG, true);
                this.getLookHelper().setLookPositionWithEntity(this.getAttackTarget(), 360.0F, 40.0F);
                this.renderYawOffset = this.rotationYaw;
                if (this.canEntityBeSeen(this.getAttackTarget()) && this.getDistance(this.getAttackTarget()) < 2.3D && openMouthProgress > 4) {
                    this.dataManager.set(LUNGE_FLAG, true);
                }
                double lim = this.getAttackTarget() instanceof EntityPlayer ? 5D : 10D;
                if (this.getDistanceSq(this.getAttackTarget()) > lim * lim && chaseTime > 40) {
                    chaseTime = -50;
                    this.setAttackTarget(null);
                    this.setRevengeTarget(null);
                    this.attackingPlayer = null;
                }
            } else {
                this.dataManager.set(ATTACK_TARGET_FLAG, false);
                this.dataManager.set(LUNGE_FLAG, false);
            }
            mossTime++;
            if (this.isInWater() && mossTime > 12000) {
                mossTime = 0;
                this.setMoss(Math.min(10, this.getMoss() + 1));
            }
        }
    }

    @Nullable
    @Override
    public EntityLivingBase getAttackTarget() {
        return this.chaseTime < 0 ? null : super.getAttackTarget();
    }

    @Override
    public void setAttackTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (this.chaseTime >= 0) {
            super.setAttackTarget(entitylivingbaseIn);
        } else {
            super.setAttackTarget(null);
        }
    }

    @Nullable
    @Override
    public EntityLivingBase getRevengeTarget() {
        return this.chaseTime < 0 ? null : super.getRevengeTarget();
    }

    @Override
    public void setRevengeTarget(@Nullable EntityLivingBase entitylivingbaseIn) {
        if (this.chaseTime >= 0) {
            super.setRevengeTarget(entitylivingbaseIn);
        } else {
            super.setRevengeTarget(null);
        }
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        this.setMoss(rand.nextInt(6));
        this.setTurtleScale(0.8F + rand.nextFloat() * 0.2F);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public float getTurtleScale() {
        return this.dataManager.get(TURTLE_SCALE);
    }

    public void setTurtleScale(float scale) {
        this.dataManager.set(TURTLE_SCALE, scale);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new SemiAquaticPathNavigator(this, worldIn) {
            @Override
            public boolean canEntityStandOnPos(BlockPos pos) {
                return !this.world.getBlockState(pos).getMaterial().isLiquid();
            }
        };
    }

    public boolean isWaiting() {
        return this.dataManager.get(WAITING);
    }

    public void setWaiting(boolean sit) {
        this.dataManager.set(WAITING, sit);
    }

    public int getMoss() {
        return this.dataManager.get(MOSS);
    }

    public void setMoss(int moss) {
        this.dataManager.set(MOSS, moss);
    }

    protected void updateAir(int air) {
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Waiting", this.isWaiting());
        compound.setInteger("MossLevel", this.getMoss());
        compound.setFloat("TurtleScale", this.getTurtleScale());
        compound.setInteger("MossTime", this.mossTime);
        compound.setInteger("WaitTime", this.waitTime);
        compound.setInteger("WaitTime2", this.timeUntilWait);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setWaiting(compound.getBoolean("Waiting"));
        this.setMoss(compound.getInteger("MossLevel"));
        this.setTurtleScale(compound.getFloat("TurtleScale"));
        this.mossTime = compound.getInteger("MossTime");
        this.waitTime = compound.getInteger("WaitTime");
        this.timeUntilWait = compound.getInteger("WaitTime2");
    }

    @Override
    public boolean shouldEnterWater() {
        return true;
    }

    @Override
    public boolean shouldLeaveWater() {
        return false;
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isWaiting();
    }

    @Override
    public int getWaterSearchRange() {
        return 10;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        boolean dryBelow = this.world.getBlockState(pos.down()).getMaterial() != Material.WATER;
        boolean waterHere = this.world.getBlockState(pos).getMaterial() == Material.WATER;
        return dryBelow && waterHere ? 10.0F : super.getBlockPathWeight(pos);
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

    public boolean isNotColliding(World worldIn) {
        return worldIn.checkNoEntityCollision(this.getEntityBoundingBox(), this)
                && worldIn.getCollisionBoxes(this, this.getEntityBoundingBox()).isEmpty();
    }

    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityAgeable) AMEntityRegistry.ALLIGATOR_SNAPPING_TURTLE.newInstance(this.world);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            if (this.isJumping) {
                this.motionY += 0.72D;
            } else {
                this.motionX *= 0.4D;
                this.motionY = this.motionY * 0.4D - 0.08D;
                this.motionZ *= 0.4D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    private boolean canShearForLoot() {
        return this.isEntityAlive() && this.getMoss() > 0;
    }

    @Override
    public boolean isShearable(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos) {
        return canShearForLoot();
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        World w = this.world;
        w.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS, 1.0F, 1.0F);
        if (!w.isRemote) {
            int mossLevel = this.getMoss();
            this.setMoss(0);
            if (rand.nextFloat() < mossLevel * 0.05F) {
                return Lists.newArrayList(new ItemStack(AMItemRegistry.SPIKED_SCUTE));
            }
            return Lists.newArrayList(new ItemStack(net.minecraft.item.Item.getItemFromBlock(Blocks.WATERLILY)));
        }
        return Collections.emptyList();
    }
}
