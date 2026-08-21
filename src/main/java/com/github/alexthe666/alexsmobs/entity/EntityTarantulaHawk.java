package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.DirectPathNavigator;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.FlyingAIFollowOwner;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.message.MessageTarantulaHawkSting;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIMate;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import java.util.Random;

public class EntityTarantulaHawk extends EntityTameable implements IFollower {

    public static final int STING_DURATION = 2400;
    private static final float GROUND_WIDTH = 0.9F;
    private static final float GROUND_HEIGHT = 0.9F;
    private static final float FLIGHT_WIDTH = 0.9F;
    private static final float FLIGHT_HEIGHT = 1.5F;
    private static final DataParameter<Float> FLY_ANGLE = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> NETHER = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DRAGGING = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FLYING = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DIGGING = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SCARED = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ATTACK_TICK = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> ANGRY = EntityDataManager.createKey(EntityTarantulaHawk.class, DataSerializers.BOOLEAN);
    public float prevFlyAngle;
    public float prevSitProgress;
    public float sitProgress;
    public float prevDragProgress;
    public float dragProgress;
    public float prevFlyProgress;
    public float flyProgress;
    public float prevAttackProgress;
    public float attackProgress;
    public float prevDigProgress;
    public float digProgress;
    private boolean isLandNavigator;
    private boolean flightSize = false;
    private int timeFlying = 0;
    private boolean bredBuryFlag = false;
    private int spiderFeedings = 0;
    private int dragTime = 0;

    public EntityTarantulaHawk(World worldIn) {
        super(worldIn);
        this.setSize(GROUND_WIDTH, GROUND_HEIGHT);
        switchNavigator(false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.tarantulaHawkSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && AMEntityRegistry.canLandSpawnWithoutGrass(this);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (this.world.provider.getDimension() == -1) {
            this.setNether(true);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new FlyingAIFollowOwner(this, 1.0D, 10.0F, 2.0F, false));
        this.tasks.addTask(3, new AIFleeRoadrunners());
        this.tasks.addTask(4, new AIMelee());
        this.tasks.addTask(5, new AIBury());
        this.tasks.addTask(6, new HawkMate(this, 1.0D));
        this.tasks.addTask(7, new EntityAITempt(this, 1.1D, Items.SPIDER_EYE, false));
        this.tasks.addTask(7, new EntityAITempt(this, 1.1D, Items.FERMENTED_SPIDER_EYE, false));
        this.tasks.addTask(8, new AIWalkIdle());
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new AnimalAIHurtByTargetNotBaby(this));
        this.targetTasks.addTask(4, new EntityAINearestTarget3D(this, EntitySpider.class, 15, true, true, null) {
            @Override
            public boolean shouldExecute() {
                return super.shouldExecute() && !EntityTarantulaHawk.this.isChild() && !EntityTarantulaHawk.this.isSitting();
            }
        });
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TARANTULA_HAWK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TARANTULA_HAWK_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.TARANTULA_HAWK;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new HawkFlightMoveHelper(this);
            this.navigator = new DirectPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FLY_ANGLE, 0F);
        this.dataManager.register(NETHER, Boolean.FALSE);
        this.dataManager.register(FLYING, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(DRAGGING, Boolean.FALSE);
        this.dataManager.register(DIGGING, Boolean.FALSE);
        this.dataManager.register(SCARED, Boolean.FALSE);
        this.dataManager.register(ANGRY, Boolean.FALSE);
        this.dataManager.register(ATTACK_TICK, 0);
        this.dataManager.register(COMMAND, 0);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof EntityLivingBase
                && ((EntityLivingBase) source.getTrueSource()).getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD
                && ((EntityLivingBase) source.getTrueSource()).isPotionActive(AMEffectRegistry.DEBILITATING_STING)) {
            return false;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("HawkSitting", this.isSitting());
        compound.setBoolean("Nether", this.isNether());
        compound.setBoolean("Digging", this.isDigging());
        compound.setBoolean("Flying", this.isFlying());
        compound.setInteger("Command", this.getCommand());
        compound.setInteger("SpiderFeedings", this.spiderFeedings);
        compound.setBoolean("BreedFlag", this.bredBuryFlag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("HawkSitting"));
        this.setNether(compound.getBoolean("Nether"));
        this.setDigging(compound.getBoolean("Digging"));
        this.setFlying(compound.getBoolean("Flying"));
        this.setCommand(compound.getInteger("Command"));
        this.spiderFeedings = compound.getInteger("SpiderFeedings");
        this.bredBuryFlag = compound.getBoolean("BreedFlag");
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase owner = this.getOwner();
            if (entityIn == owner) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                return ((EntityTameable) entityIn).isOwner(owner);
            }
            if (owner != null) {
                return owner.isOnSameTeam(entityIn);
            }
        }
        return super.isOnSameTeam(entityIn);
    }

    public float getFlyAngle() {
        return this.dataManager.get(FLY_ANGLE);
    }

    public void setFlyAngle(float progress) {
        this.dataManager.set(FLY_ANGLE, progress);
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

    public boolean isNether() {
        return this.dataManager.get(NETHER);
    }

    public void setNether(boolean nether) {
        this.dataManager.set(NETHER, nether);
    }

    public boolean isScared() {
        return this.dataManager.get(SCARED);
    }

    public void setScared(boolean scared) {
        this.dataManager.set(SCARED, scared);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean isDragging() {
        return this.dataManager.get(DRAGGING);
    }

    public void setDragging(boolean dragging) {
        this.dataManager.set(DRAGGING, dragging);
    }

    public boolean isDigging() {
        return this.dataManager.get(DIGGING);
    }

    public void setDigging(boolean digging) {
        this.dataManager.set(DIGGING, digging);
    }

    private void updateFlightSize() {
        if (flightSize && !isFlying()) {
            this.setSize(GROUND_WIDTH, GROUND_HEIGHT);
            flightSize = false;
        }
        if (!flightSize && isFlying() && !isChild()) {
            this.setSize(FLIGHT_WIDTH, FLIGHT_HEIGHT);
            flightSize = true;
        }
    }

    @Override
    public void onLivingUpdate() {
        this.isImmuneToFire = isNether() || AMConfig.fireproofTarantulaHawk;
        prevFlyAngle = this.getFlyAngle();
        super.onLivingUpdate();
        updateFlightSize();
        prevAttackProgress = attackProgress;
        prevFlyProgress = flyProgress;
        prevSitProgress = sitProgress;
        prevDragProgress = dragProgress;
        prevDigProgress = digProgress;
        if (this.isFlying() && flyProgress < 5F) {
            flyProgress++;
        }
        if (!this.isFlying() && flyProgress > 0F) {
            flyProgress--;
        }
        if (this.isSitting() && sitProgress < 5F) {
            sitProgress++;
        }
        if (!this.isSitting() && sitProgress > 0F) {
            sitProgress--;
        }
        if (this.isDragging() && dragProgress < 5F) {
            dragProgress++;
        }
        if (!this.isDragging() && dragProgress > 0F) {
            dragProgress--;
        }
        if (this.isDigging() && digProgress < 5F) {
            digProgress++;
        }
        if (!this.isDigging() && digProgress > 0F) {
            digProgress--;
        }
        float threshold = 0.015F;
        if (isFlying() && this.prevRotationYaw - this.rotationYaw > threshold) {
            this.setFlyAngle(this.getFlyAngle() + 5);
        } else if (isFlying() && this.prevRotationYaw - this.rotationYaw < -threshold) {
            this.setFlyAngle(this.getFlyAngle() - 5);
        } else if (this.getFlyAngle() > 0) {
            this.setFlyAngle(Math.max(this.getFlyAngle() - 4, 0));
        } else if (this.getFlyAngle() < 0) {
            this.setFlyAngle(Math.min(this.getFlyAngle() + 4, 0));
        }
        this.setFlyAngle(MathHelper.clamp(this.getFlyAngle(), -30, 30));
        if (!world.isRemote) {
            if (isFlying() && this.isLandNavigator) {
                switchNavigator(false);
            }
            if (!isFlying() && !this.isLandNavigator) {
                switchNavigator(true);
            }
            if (isFlying()) {
                if (timeFlying % 25 == 0) {
                    this.playSound(AMSoundRegistry.TARANTULA_HAWK_WING, this.getSoundVolume(), this.getSoundPitch());
                }
                timeFlying++;
                this.setNoGravity(true);
                if (this.isSitting() || this.isBeingRidden() || this.isInLove()) {
                    this.setFlying(false);
                }
            } else {
                timeFlying = 0;
                this.setNoGravity(false);
            }
            if (this.getAttackTarget() != null && this.getAttackTarget() instanceof EntityPlayer && !this.isTamed()) {
                this.dataManager.set(ANGRY, Boolean.TRUE);
            } else {
                this.dataManager.set(ANGRY, Boolean.FALSE);
            }
        }
        if (this.dataManager.get(ATTACK_TICK) > 0) {
            this.dataManager.set(ATTACK_TICK, this.dataManager.get(ATTACK_TICK) - 1);
            if (attackProgress < 5F) {
                attackProgress++;
            }
        } else {
            if (attackProgress > 0F) {
                attackProgress--;
            }
        }
        if (isDigging() && world.getBlockState(this.getPositionUnderneath()).isSideSolid(world, this.getPositionUnderneath(), EnumFacing.UP)) {
            BlockPos posit = this.getPositionUnderneath();
            IBlockState understate = world.getBlockState(posit);
            for (int i = 0; i < 4 + rand.nextInt(2); i++) {
                double particleX = posit.getX() + rand.nextFloat();
                double particleY = posit.getY() + 1F;
                double particleZ = posit.getZ() + rand.nextFloat();
                double motX = this.rand.nextGaussian() * 0.02D;
                double motY = 0.1F + rand.nextFloat() * 0.2F;
                double motZ = this.rand.nextGaussian() * 0.02D;
                world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, particleX, particleY, particleZ, motX, motY, motZ, Block.getStateId(understate));
            }
        }
        if (this.ticksExisted > 0 && ticksExisted % 300 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1);
        }
        if (!world.isRemote && this.isDragging() && this.getPassengers().isEmpty() && !this.isDigging()) {
            dragTime++;
            if (dragTime > 5000) {
                dragTime = 0;
                for (Entity e : this.getPassengers()) {
                    e.attackEntityFrom(DamageSource.causeMobDamage(this), 10);
                }
                this.removePassengers();
                this.setDragging(false);
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Items.NAME_TAG) {
            return super.processInteract(player, hand);
        }
        // Wild: 15-25 spider eyes (same as 1.16/1.20). Fermented eyes are breeding-only once tamed.
        if (!isTamed() && item == Items.SPIDER_EYE) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            if (!this.world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                spiderFeedings++;
                if (spiderFeedings >= 15 && getRNG().nextInt(6) == 0 || spiderFeedings > 25) {
                    this.setTamedBy(player);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.world.setEntityState(this, (byte) 6);
                }
            }
            return true;
        }
        if (isTamed() && isBreedingItem(itemstack)) {
            if (this.isChild()) {
                return super.processInteract(player, hand);
            }
            if (!this.world.isRemote && this.getGrowingAge() == 0 && !this.isInLove()) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.setInLove(player);
                this.releaseForHunt();
            }
            return true;
        }
        if (isTamed() && isFlowerItem(item)) {
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
        if (isTamed() && isOwner(player)) {
            if (player.isSneaking()) {
                if (this.getHeldItemMainhand().isEmpty()) {
                    ItemStack cop = itemstack.copy();
                    cop.setCount(1);
                    this.setHeldItem(EnumHand.MAIN_HAND, cop);
                    if (!player.capabilities.isCreativeMode) {
                        itemstack.shrink(1);
                    }
                    return true;
                } else {
                    this.entityDropItem(this.getHeldItemMainhand().copy(), 0.0F);
                    this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                    return true;
                }
            } else {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                this.setSitting(sit);
                if (this.aiSit != null) {
                    this.aiSit.setSitting(sit);
                }
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isTamed() && stack.getItem() == Items.FERMENTED_SPIDER_EYE;
    }

    @Override
    public boolean canMateWith(EntityAnimal otherAnimal) {
        if (otherAnimal == this || !(otherAnimal instanceof EntityTarantulaHawk)) {
            return false;
        }
        EntityTarantulaHawk other = (EntityTarantulaHawk) otherAnimal;
        return this.isTamed() && other.isTamed() && this.isInLove() && other.isInLove();
    }

    private void releaseForHunt() {
        this.setSitting(false);
        if (this.aiSit != null) {
            this.aiSit.setSitting(false);
        }
        this.setCommand(0);
        this.setFlying(false);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.CACTUS || super.isEntityInvulnerable(source);
    }

    static class HawkMate extends AnimalAIMate {
        private final EntityTarantulaHawk hawk;

        HawkMate(EntityTarantulaHawk hawk, double speed) {
            super(hawk, speed);
            this.hawk = hawk;
        }

        @Override
        protected void spawnBaby() {
            EntityAnimal partner = this.getTargetMate();
            if (partner != null) {
                hawk.onBreedComplete(partner);
            }
        }
    }

    private void onBreedComplete(EntityAnimal partner) {
        bredBuryFlag = true;
        this.releaseForHunt();
        if (partner instanceof EntityTarantulaHawk) {
            EntityTarantulaHawk other = (EntityTarantulaHawk) partner;
            other.bredBuryFlag = true;
            other.releaseForHunt();
        }
        EntityPlayerMP breeder = this.getLoveCause();
        if (breeder == null && partner.getLoveCause() != null) {
            breeder = partner.getLoveCause();
        }
        if (breeder != null) {
            breeder.addStat(StatList.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(breeder, this, partner, this);
        }
        this.setGrowingAge(6000);
        partner.setGrowingAge(6000);
        this.resetInLove();
        partner.resetInLove();
        this.world.setEntityState(this, (byte) 7);
        this.world.setEntityState(this, (byte) 18);
        if (this.world.getGameRules().getBoolean("doMobLoot")) {
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, this.getRNG().nextInt(7) + 1));
        }
    }

    @Override
    public void followEntity(EntityTameable tameable, EntityLivingBase owner, double followSpeed) {
        if (this.getDistance(owner) > 5) {
            this.setFlying(true);
            this.getMoveHelper().setMoveTo(owner.posX, owner.posY + owner.height, owner.posZ, followSpeed);
        } else {
            if (this.onGround) {
                this.setFlying(false);
            }
            if (this.isFlying() && !this.isOverWater()) {
                BlockPos vec = this.getCrowGround(this.getPosition());
                if (vec != null) {
                    this.getMoveHelper().setMoveTo(vec.getX(), vec.getY(), vec.getZ(), followSpeed);
                }
            } else {
                this.getNavigator().tryMoveToEntityLiving(owner, followSpeed);
            }
        }
    }

    @Override
    public void updatePassenger(Entity passenger) {
        this.rotationPitch = 0;
        float radius = 1.0F + passenger.width * 0.5F;
        float angle = (0.01745329251F * (this.renderYawOffset - 180));
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        passenger.setPosition(this.posX + extraX, this.posY, this.posZ + extraZ);
    }

    public boolean isOverWater() {
        BlockPos position = this.getPosition();
        while (position.getY() > 0 && world.isAirBlock(position)) {
            position = position.down();
        }
        return world.getBlockState(position).getMaterial() == Material.WATER || position.getY() <= 0;
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
        int flightHeight = 4 + this.getRNG().nextInt(10);
        BlockPos newPos = ground.up(distFromGround > 8 ? flightHeight : this.getRNG().nextInt(6) + 1);
        Vec3d centerPos = new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D);
        if (!this.isTargetBlocked(centerPos) && this.getDistanceSq(centerPos.x, centerPos.y, centerPos.z) > 1.0D) {
            return centerPos;
        }
        return null;
    }

    private BlockPos getCrowGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() > 2 && world.isAirBlock(position)) {
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
            while (ground.getY() > 2 && world.isAirBlock(ground)) {
                ground = ground.down();
            }
        }
        Vec3d centerGround = new Vec3d(ground.getX() + 0.5D, ground.getY() + 0.5D, ground.getZ() + 0.5D);
        if (!this.isTargetBlocked(centerGround)) {
            return centerGround;
        }
        return null;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private Vec3d getOrbitVec(Vec3d vector3d, float gatheringCircleDist, boolean orbitClockwise) {
        float angle = (0.01745329251F * 2 * (orbitClockwise ? -ticksExisted : ticksExisted));
        double extraX = gatheringCircleDist * MathHelper.sin(angle);
        double extraZ = gatheringCircleDist * MathHelper.cos(angle);
        if (vector3d != null) {
            Vec3d pos = new Vec3d(vector3d.x + extraX, vector3d.y + rand.nextInt(2) + 4, vector3d.z + extraZ);
            if (this.world.isAirBlock(new BlockPos(pos))) {
                return pos;
            }
        }
        return null;
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
    }

    private BlockPos genSandPos(BlockPos parent) {
        Random random = new Random();
        int range = 24;
        for (int i = 0; i < 15; i++) {
            BlockPos sandAir = parent.add(random.nextInt(range) - range / 2, -5, random.nextInt(range) - range / 2);
            while (!world.isAirBlock(sandAir) && sandAir.getY() < 255) {
                sandAir = sandAir.up();
            }
            if (world.getBlockState(sandAir.down()).getBlock() == Blocks.SAND) {
                return sandAir.down();
            }
        }
        return null;
    }

    @Override
    public boolean shouldFollow() {
        return getCommand() == 1 && !this.bredBuryFlag && !this.isDragging() && !this.isDigging()
                && (this.getAttackTarget() == null || !this.getAttackTarget().isEntityAlive());
    }

    public boolean isAngry() {
        return dataManager.get(ANGRY);
    }

    protected BlockPos getPositionUnderneath() {
        return new BlockPos(this.posX, this.posY - 1.0D, this.posZ);
    }

    private static boolean isFlowerItem(Item item) {
        Block block = Block.getBlockFromItem(item);
        return block instanceof BlockFlower;
    }

    class HawkFlightMoveHelper extends EntityMoveHelper {
        private final EntityTarantulaHawk parentEntity;

        HawkFlightMoveHelper(EntityTarantulaHawk hawk) {
            super(hawk);
            this.parentEntity = hawk;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.isUpdating()) {
                double tx = this.getX() - parentEntity.posX;
                double ty = this.getY() - parentEntity.posY;
                double tz = this.getZ() - parentEntity.posZ;
                double d0 = MathHelper.sqrt(tx * tx + ty * ty + tz * tz);
                double width = parentEntity.getEntityBoundingBox().getAverageEdgeLength();
                if (d0 < width) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    float angle = (0.01745329251F * (parentEntity.renderYawOffset + 90));
                    float radius = (float) Math.sin(parentEntity.ticksExisted * 0.2F) * 2;
                    double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                    double extraZ = radius * MathHelper.cos(angle);
                    double sp = this.getSpeed() * 0.05D / d0;
                    Vec3d strafPlus = new Vec3d(extraX, 0, extraZ).scale(0.003D * Math.min(d0, 100));
                    parentEntity.motionX += strafPlus.x;
                    parentEntity.motionZ += strafPlus.z;
                    parentEntity.motionX += tx * sp;
                    parentEntity.motionY += ty * sp;
                    parentEntity.motionZ += tz * sp;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(tx, tz)) * (180F / (float) Math.PI);
                    if (!EntityTarantulaHawk.this.isDragging()) {
                        parentEntity.renderYawOffset = parentEntity.rotationYaw;
                    }
                }
            }
        }
    }

    private class AIMelee extends EntityAIBase {
        private final EntityTarantulaHawk hawk;
        private int orbitCooldown = 0;
        private boolean clockwise = false;
        private Vec3d orbitVec = null;
        private BlockPos sandPos = null;

        AIMelee() {
            this.hawk = EntityTarantulaHawk.this;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return hawk.getAttackTarget() != null && !hawk.isSitting() && !hawk.isScared() && hawk.getAttackTarget().isEntityAlive()
                    && !hawk.isDragging() && !hawk.isDigging() && !hawk.getAttackTarget().noClip && !hawk.getAttackTarget().isRiding();
        }

        @Override
        public void startExecuting() {
            hawk.setDragging(false);
            clockwise = hawk.getRNG().nextBoolean();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = hawk.getAttackTarget();
            boolean paralized = target != null && target.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD
                    && !target.noClip && target.isPotionActive(AMEffectRegistry.DEBILITATING_STING);
            PotionEffect sting = target == null ? null : target.getActivePotionEffect(AMEffectRegistry.DEBILITATING_STING);
            boolean paralizedWithChild = paralized && sting != null && sting.getAmplifier() > 0;
            if (sandPos == null || world.getBlockState(sandPos).getBlock() != Blocks.SAND) {
                sandPos = hawk.genSandPos(target.getPosition());
            }
            if (orbitCooldown > 0) {
                orbitCooldown--;
                hawk.setFlying(true);
                if (target != null) {
                    if (orbitVec == null || hawk.getDistanceSq(orbitVec.x, orbitVec.y, orbitVec.z) < 4F || !hawk.getMoveHelper().isUpdating()) {
                        orbitVec = hawk.getOrbitVec(new Vec3d(target.posX, target.posY + target.height, target.posZ), 10 + hawk.getRNG().nextInt(2), false);
                        if (orbitVec != null) {
                            hawk.getMoveHelper().setMoveTo(orbitVec.x, orbitVec.y, orbitVec.z, 1F);
                        }
                    }
                }
            } else if (((paralized && !hawk.isTamed()) || (paralizedWithChild && hawk.bredBuryFlag)) && sandPos != null) {
                if (hawk.onGround) {
                    hawk.setFlying(false);
                    hawk.getNavigator().tryMoveToEntityLiving(target, 1);
                } else {
                    Vec3d vector3d = hawk.getBlockGrounding(hawk.getPositionVector());
                    if (vector3d != null && hawk.isFlying()) {
                        hawk.getMoveHelper().setMoveTo(vector3d.x, vector3d.y, vector3d.z, 1F);
                    }
                }
                if (hawk.getDistance(target) < target.width + 1.5F && !target.isRiding()) {
                    hawk.setDragging(true);
                    hawk.setFlying(false);
                    target.startRiding(hawk, true);
                }
            } else {
                if (target != null && !paralizedWithChild) {
                    double dist = hawk.getDistance(target);
                    if (dist < 10 && !hawk.isFlying()) {
                        if (hawk.onGround) {
                            hawk.setFlying(false);
                        }
                        hawk.getNavigator().tryMoveToEntityLiving(target, 1);
                    } else {
                        hawk.setFlying(true);
                        hawk.getMoveHelper().setMoveTo(target.posX, target.getPositionEyes(1.0F).y, target.posZ, 1F);
                    }
                    if (dist < target.width + 2.5F) {
                        if (hawk.dataManager.get(ATTACK_TICK) == 0 && hawk.attackProgress == 0) {
                            hawk.dataManager.set(ATTACK_TICK, 7);
                        }
                        if (hawk.attackProgress >= 5F) {
                            hawk.attackEntityAsMob(target);
                            if (hawk.bredBuryFlag) {
                                if (target.getHealth() <= 1.0F) {
                                    target.heal(5);
                                }
                            }
                            int duration = target.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD ? EntityTarantulaHawk.STING_DURATION : 600;
                            int amp = hawk.bredBuryFlag ? 1 : 0;
                            target.addPotionEffect(new PotionEffect(AMEffectRegistry.DEBILITATING_STING, duration, amp));
                            if (!hawk.world.isRemote && target.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD) {
                                AlexsMobs.sendMSGToAll(new MessageTarantulaHawkSting(hawk.getEntityId(), target.getEntityId()));
                            }
                            orbitCooldown = target.getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD ? 200 + hawk.getRNG().nextInt(200) : 10 + hawk.getRNG().nextInt(20);
                        }
                    }
                }
            }
        }

        @Override
        public void resetTask() {
            orbitCooldown = 0;
            clockwise = hawk.getRNG().nextBoolean();
            orbitVec = null;
            if (hawk.getPassengers().isEmpty()) {
                hawk.setAttackTarget(null);
            }
        }
    }

    private class AIWalkIdle extends EntityAIBase {
        protected final EntityTarantulaHawk hawk;
        protected double x;
        protected double y;
        protected double z;
        private boolean flightTarget = false;

        AIWalkIdle() {
            this.setMutexBits(1);
            this.hawk = EntityTarantulaHawk.this;
        }

        @Override
        public boolean shouldExecute() {
            if (this.hawk.isBeingRidden() || hawk.isScared() || hawk.isDragging() || EntityTarantulaHawk.this.getCommand() == 1
                    || (hawk.getAttackTarget() != null && hawk.getAttackTarget().isEntityAlive()) || this.hawk.isBeingRidden() || this.hawk.isSitting()) {
                return false;
            } else {
                if (this.hawk.getRNG().nextInt(30) != 0 && !hawk.isFlying()) {
                    return false;
                }
                if (this.hawk.onGround) {
                    this.flightTarget = hawk.getRNG().nextBoolean();
                } else {
                    this.flightTarget = hawk.getRNG().nextInt(5) > 0 && hawk.timeFlying < 200;
                }
                Vec3d lvt_1_1_ = this.getPosition();
                if (lvt_1_1_ == null) {
                    return false;
                } else {
                    this.x = lvt_1_1_.x;
                    this.y = lvt_1_1_.y;
                    this.z = lvt_1_1_.z;
                    return true;
                }
            }
        }

        @Override
        public void updateTask() {
            if (flightTarget) {
                hawk.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.hawk.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
            if (!flightTarget && isFlying() && hawk.onGround) {
                hawk.setFlying(false);
            }
            if (isFlying() && hawk.onGround && hawk.timeFlying > 10) {
                hawk.setFlying(false);
            }
        }

        @Nullable
        protected Vec3d getPosition() {
            Vec3d vector3d = hawk.getPositionVector();
            if (hawk.isOverWater()) {
                flightTarget = true;
            }
            if (flightTarget) {
                if (hawk.timeFlying < 50 || hawk.isOverWater()) {
                    return hawk.getBlockInViewAway(vector3d, 0);
                } else {
                    return hawk.getBlockGrounding(vector3d);
                }
            } else {
                return RandomPositionGenerator.findRandomTarget(this.hawk, 10, 7);
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (hawk.isSitting() || EntityTarantulaHawk.this.getCommand() == 1) {
                return false;
            }
            if (flightTarget) {
                return hawk.isFlying() && hawk.getDistanceSq(x, y, z) > 2F;
            } else {
                return (!this.hawk.getNavigator().noPath()) && !this.hawk.isBeingRidden();
            }
        }

        @Override
        public void startExecuting() {
            if (flightTarget) {
                hawk.setFlying(true);
                hawk.getMoveHelper().setMoveTo(x, y, z, 1F);
            } else {
                this.hawk.getNavigator().tryMoveToXYZ(this.x, this.y, this.z, 1F);
            }
        }

        @Override
        public void resetTask() {
            this.hawk.getNavigator().clearPath();
        }
    }

    private class AIBury extends EntityAIBase {
        private final EntityTarantulaHawk hawk;
        private BlockPos buryPos = null;
        private int digTime = 0;
        private double stageX;
        private double stageY;
        private double stageZ;

        AIBury() {
            this.hawk = EntityTarantulaHawk.this;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (hawk.isDragging() && hawk.getAttackTarget() != null) {
                BlockPos pos = hawk.genSandPos(hawk.getPosition());
                if (pos != null) {
                    buryPos = pos;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return hawk.isDragging() && digTime < 200 && hawk.getAttackTarget() != null && buryPos != null
                    && world.getBlockState(buryPos).getBlock() == Blocks.SAND;
        }

        @Override
        public void startExecuting() {
            digTime = 0;
            stageX = hawk.posX;
            stageY = hawk.posY;
            stageZ = hawk.posZ;
        }

        @Override
        public void resetTask() {
            digTime = 0;
            hawk.setDigging(false);
            hawk.setDragging(false);
            hawk.setAttackTarget(null);
            hawk.setRevengeTarget(null);
            if (hawk.bredBuryFlag) {
                hawk.bredBuryFlag = false;
            }
        }

        @Override
        public void updateTask() {
            hawk.setFlying(false);
            hawk.setDragging(true);
            EntityLivingBase target = hawk.getAttackTarget();
            if (hawk.getDistanceSq(buryPos.getX() + 0.5D, buryPos.getY(), buryPos.getZ() + 0.5D) < 9) {
                if (!hawk.isDigging()) {
                    hawk.setDigging(true);
                    stageX = target.posX;
                    stageY = target.posY;
                    stageZ = target.posZ;
                }
            }
            if (hawk.isDigging()) {
                target.noClip = true;
                digTime++;
                hawk.removePassengers();
                target.setPosition(stageX, stageY - Math.min(3, digTime * 0.05F), stageZ);
                hawk.getNavigator().tryMoveToXYZ(stageX, stageY, stageZ, 0.85F);
            } else {
                hawk.getNavigator().tryMoveToXYZ(buryPos.getX(), buryPos.getY(), buryPos.getZ(), 0.5F);
            }
        }
    }

    private class AIFleeRoadrunners extends EntityAIBase {
        private int searchCooldown = 0;
        private EntityLivingBase fear = null;
        private Vec3d fearVec = null;

        @Override
        public boolean shouldExecute() {
            if (searchCooldown <= 0) {
                searchCooldown = 100 + EntityTarantulaHawk.this.rand.nextInt(100);
                List<EntityRoadrunner> list = EntityTarantulaHawk.this.world.getEntitiesWithinAABB(EntityRoadrunner.class,
                        EntityTarantulaHawk.this.getEntityBoundingBox().grow(15, 32, 15));
                for (EntityRoadrunner roadrunner : list) {
                    if (fear == null || EntityTarantulaHawk.this.getDistance(fear) > EntityTarantulaHawk.this.getDistance(roadrunner)) {
                        fear = roadrunner;
                    }
                }
            } else {
                searchCooldown--;
            }
            return EntityTarantulaHawk.this.isEntityAlive() && fear != null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return fear != null && fear.isEntityAlive() && EntityTarantulaHawk.this.getDistance(fear) < 32F;
        }

        @Override
        public void startExecuting() {
            EntityTarantulaHawk.this.setScared(true);
        }

        @Override
        public void updateTask() {
            if (fear != null) {
                if (fearVec == null || EntityTarantulaHawk.this.getDistanceSq(fearVec.x, fearVec.y, fearVec.z) < 4) {
                    fearVec = EntityTarantulaHawk.this.getBlockInViewAway(fearVec == null ? fear.getPositionVector() : fearVec, 12);
                }
                if (fearVec != null) {
                    EntityTarantulaHawk.this.setFlying(true);
                    EntityTarantulaHawk.this.getMoveHelper().setMoveTo(fearVec.x, fearVec.y, fearVec.z, 1.1F);
                }
            }
        }

        @Override
        public void resetTask() {
            EntityTarantulaHawk.this.setScared(false);
            fear = null;
            fearVec = null;
        }
    }
}
