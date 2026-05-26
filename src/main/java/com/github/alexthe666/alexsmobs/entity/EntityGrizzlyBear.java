package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.GrizzlyBearAIBeehive;
import com.github.alexthe666.alexsmobs.entity.ai.GrizzlyBearAIFleeBees;
import com.github.alexthe666.alexsmobs.entity.ai.ResetAngerGoal;
import com.github.alexthe666.alexsmobs.entity.ai.TameableAIRide;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityGrizzlyBear extends EntityTameable implements IAngerable, IAnimatedEntity, ITargetsDroppedItems {

    public static final Animation ANIMATION_MAUL = Animation.create(20);
    public static final Animation ANIMATION_SNIFF = Animation.create(12);
    public static final Animation ANIMATION_SWIPE_R = Animation.create(15);
    public static final Animation ANIMATION_SWIPE_L = Animation.create(20);

    private static final float NORMAL_WIDTH = 1.4F;
    private static final float NORMAL_HEIGHT = 1.0F;
    private static final float STAND_WIDTH = 1.7F;
    private static final float STAND_HEIGHT = 2.75F;

    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityGrizzlyBear.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityGrizzlyBear.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HONEYED = EntityDataManager.createKey(EntityGrizzlyBear.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> EATING = EntityDataManager.createKey(EntityGrizzlyBear.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SNOWY = EntityDataManager.createKey(EntityGrizzlyBear.class, DataSerializers.BOOLEAN);

    public float prevStandProgress;
    public float prevSitProgress;
    public float standProgress;
    public float sitProgress;
    public int maxStandTime = 75;
    public boolean forcedSit = false;
    private int animationTick;
    private Animation currentAnimation;
    private int standingTime = 0;
    private int sittingTime = 0;
    private int maxSitTime = 75;
    private int eatingTime = 0;
    private int angerTime;
    private UUID angerTarget;
    private int warningSoundTicks;
    private int honeyedTime;
    @Nullable
    private String salmonThrowerName = null;
    public int timeUntilNextFur = this.rand.nextInt(24000) + 24000;
    private boolean recalcSize = false;
    private int snowTimer = 0;
    private boolean permSnow = false;
    private long lastPlayerAttackGameTime = -1000000L;

    public EntityGrizzlyBear(World worldIn) {
        super(worldIn);
        this.setSize(NORMAL_WIDTH, NORMAL_HEIGHT);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.6D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.grizzlyBearSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
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
            boolean ok = super.attackEntityFrom(source, amount);
            if (ok && source.getTrueSource() instanceof EntityPlayer) {
                this.lastPlayerAttackGameTime = world.getTotalWorldTime();
            }
            return ok;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.GRIZZLY_BEAR_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GRIZZLY_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GRIZZLY_BEAR_DIE;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            float sitAdd = -0.065F * this.sitProgress;
            float standAdd = -0.07F * this.standProgress;
            float radius = standAdd + sitAdd;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    public double getMountedYOffset() {
        float f = Math.min(0.25F, this.limbSwingAmount);
        float f1 = this.limbSwing;
        float sitAdd = 0.01F * this.sitProgress;
        float standAdd = 0.07F * this.standProgress;
        return (double) this.height - 0.3D + (double) (0.12F * MathHelper.cos(f1 * 0.7F) * 0.7F * f) + sitAdd + standAdd;
    }

    @Override
    protected float getWaterSlowDown() {
        return isBeingRidden() ? 0.9F : 0.98F;
    }

    @Override
    public void func_230258_H__() {
        this.setAngerTime(20 + this.rand.nextInt(20));
    }

    @Override
    public int getAngerTime() {
        return this.angerTime;
    }

    @Override
    public void setAngerTime(int time) {
        this.angerTime = time;
    }

    @Override
    public UUID getAngerTarget() {
        return this.angerTarget;
    }

    @Override
    public void setAngerTarget(@Nullable UUID target) {
        this.angerTarget = target;
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
    public boolean isEntityInvulnerable(DamageSource source) {
        return "sting".equals(source.getDamageType()) || source == DamageSource.IN_WALL || super.isEntityInvulnerable(source);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new TameableAIRide(this, 1D));
        this.tasks.addTask(2, new MeleeAttackGoal());
        this.tasks.addTask(2, new PanicGoal());
        this.tasks.addTask(4, new GrizzlyBearAITempt(this, 1.1D));
        this.tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
        this.tasks.addTask(4, new GrizzlyBearAIBeehive(this));
        this.tasks.addTask(5, new GrizzlyBearAIFleeBees(this, 14, 1D, 1D));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWander(this, 0.75D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new HurtByTargetGoal());
        this.targetTasks.addTask(4, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(5, new AttackPlayerGoal());
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 10, true, false, new Predicate<EntityPlayer>() {
            @Override
            public boolean apply(@Nullable EntityPlayer player) {
                return canAngerTarget(player);
            }
        }));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityRabbit>(this, EntityRabbit.class, 10, true, true, null));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityWolf>(this, EntityWolf.class, 10, true, true, null));
        this.targetTasks.addTask(7, new ResetAngerGoal(this, false));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Honeyed", this.isHoneyed());
        compound.setBoolean("Snowy", this.isSnowy());
        compound.setBoolean("Standing", this.isStanding());
        compound.setBoolean("BearSitting", this.isSitting());
        compound.setBoolean("ForcedToSit", this.forcedSit);
        compound.setBoolean("SnowPerm", this.permSnow);
        compound.setInteger("FurTime", this.timeUntilNextFur);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setHoneyed(compound.getBoolean("Honeyed"));
        this.setSnowy(compound.getBoolean("Snowy"));
        this.setStanding(compound.getBoolean("Standing"));
        this.setSitting(compound.getBoolean("BearSitting"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.permSnow = compound.getBoolean("SnowPerm");
        this.timeUntilNextFur = compound.getInteger("FurTime");
        this.updateSize();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isTamed() && stack.getItem() == Items.FISH;
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof EntityPlayer) {
                return passenger;
            }
        }
        return null;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Item.getItemFromBlock(Blocks.SNOW_LAYER) && !this.isSnowy() && !world.isRemote) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.permSnow = true;
            this.setSnowy(true);
            this.playSound(SoundEvents.BLOCK_SNOW_PLACE, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        if (item instanceof ItemTool && ((ItemTool) item).getToolClasses(itemstack).contains("shovel") && this.isSnowy() && !world.isRemote) {
            this.permSnow = false;
            if (!player.capabilities.isCreativeMode) {
                itemstack.damageItem(1, player);
            }
            this.setSnowy(false);
            this.playSound(SoundEvents.BLOCK_SNOW_BREAK, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }

        if (isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
            if (!player.isSneaking() && !this.isChild()) {
                player.startRiding(this);
                return true;
            } else {
                if (this.isSitting()) {
                    this.forcedSit = false;
                    this.setSitting(false);
                    return true;
                } else {
                    this.forcedSit = true;
                    this.setSitting(true);
                    return true;
                }
            }
        }
        return super.processInteract(player, hand);
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
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isChild() || this.getEyeHeight() > this.height) {
            this.updateSize();
        }
        if (!isStanding() && this.height >= STAND_HEIGHT - 0.01F) {
            this.updateSize();
        }
        this.prevStandProgress = this.standProgress;
        this.prevSitProgress = this.sitProgress;
        if (this.isSitting() && sitProgress < 10) {
            sitProgress += 1;
        }
        if (!this.isSitting() && sitProgress > 0) {
            sitProgress -= 1;
        }
        if (this.isStanding() && standProgress < 10) {
            standProgress += 1;
        }
        if (!this.isStanding() && standProgress > 0) {
            standProgress -= 1;
        }
        if (!this.getHeldItemMainhand().isEmpty() && this.canTargetItem(this.getHeldItemMainhand())) {
            this.setEating(true);
            this.setSitting(true);
            this.setStanding(false);
        }
        if (recalcSize) {
            recalcSize = false;
            this.updateSize();
        }
        if (isEating() && !this.canTargetItem(this.getHeldItemMainhand())) {
            this.setEating(false);
            eatingTime = 0;
            if (!forcedSit) {
                this.setSitting(true);
            }
        }
        if (isEating()) {
            eatingTime++;
            for (int i = 0; i < 3; i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                ItemStack held = this.getHeldItemMainhand();
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(held.getItem()), held.getMetadata());
            }
            if (eatingTime % 5 == 0) {
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            }
            if (eatingTime > 100) {
                ItemStack stack = this.getHeldItemMainhand();
                if (!stack.isEmpty()) {
                    if (AMTagRegistry.itemInTag(AMTagRegistry.GRIZZLY_HONEY, stack.getItem())) {
                        this.setHoneyed(true);
                        this.heal(10);
                        this.honeyedTime = 700;
                    } else {
                        this.heal(4);
                    }
                    if (stack.getItem() == Items.FISH && !this.isTamed() && this.salmonThrowerName != null) {
                        if (getRNG().nextFloat() < 0.3F) {
                            this.setTamed(true);
                            EntityPlayer player = world.getMinecraftServer().getPlayerList().getPlayerByUsername(salmonThrowerName);
                            if (player != null) {
                                this.setOwnerId(player.getUniqueID());
                            }
                            if (player instanceof EntityPlayerMP) {
                                CriteriaTriggers.TAME_ANIMAL.trigger((EntityPlayerMP) player, this);
                            }
                            this.world.setEntityState(this, (byte) 7);
                        } else {
                            this.world.setEntityState(this, (byte) 6);
                        }
                    }
                    if (stack.getItem().hasContainerItem()) {
                        this.entityDropItem(new ItemStack(stack.getItem().getContainerItem()), 0.0F);
                    }
                    stack.shrink(1);
                }
                eatingTime = 0;
            }
        }
        if (isStanding() && ++standingTime > maxStandTime) {
            this.setStanding(false);
            standingTime = 0;
            maxStandTime = 75 + rand.nextInt(50);
        }
        if (isSitting() && !forcedSit && ++sittingTime > maxSitTime) {
            this.setSitting(false);
            sittingTime = 0;
            maxSitTime = 75 + rand.nextInt(50);
        }
        if (!world.isRemote && this.getAnimation() == NO_ANIMATION && !this.isStanding() && !this.isSitting() && rand.nextInt(1500) == 0) {
            maxSitTime = 300 + rand.nextInt(250);
            this.setSitting(true);
        }
        if (!forcedSit && this.isSitting() && (this.getAttackTarget() != null || this.isStanding()) && !this.isEating()) {
            this.setSitting(false);
        }
        if (this.getAnimation() == NO_ANIMATION && rand.nextInt(isStanding() ? 350 : 2500) == 0) {
            this.setAnimation(ANIMATION_SNIFF);
        }
        if (this.isSitting()) {
            this.getNavigator().clearPath();
        }
        EntityLivingBase attackTarget = this.getAttackTarget();
        if (this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer rider = (EntityPlayer) this.getControllingPassenger();
            if (rider.getLastAttackedEntity() != null && this.getDistance(rider.getLastAttackedEntity()) < this.width + 3F && !this.isOnSameTeam(rider.getLastAttackedEntity())) {
                UUID preyUUID = rider.getLastAttackedEntity().getUniqueID();
                if (!this.getUniqueID().equals(preyUUID)) {
                    attackTarget = rider.getLastAttackedEntity();
                    if (getAnimation() == NO_ANIMATION || getAnimation() == ANIMATION_SNIFF) {
                        EntityGrizzlyBear.this.setAnimation(rand.nextBoolean() ? ANIMATION_MAUL : rand.nextBoolean() ? ANIMATION_SWIPE_L : ANIMATION_SWIPE_R);
                    }
                }
            }
        }
        if (attackTarget != null) {
            if (!world.isRemote) {
                this.setSprinting(true);
            }
            if (getDistance(attackTarget) < attackTarget.width + this.width + 2) {
                if (this.getAnimation() == ANIMATION_MAUL && this.getAnimationTick() % 5 == 0 && this.getAnimationTick() > 3) {
                    attackEntityAsMob(attackTarget);
                }
                if ((this.getAnimation() == ANIMATION_SWIPE_L) && this.getAnimationTick() == 7) {
                    attackEntityAsMob(attackTarget);
                    float rot = rotationYaw + 90;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                }
                if ((this.getAnimation() == ANIMATION_SWIPE_R) && this.getAnimationTick() == 7) {
                    attackEntityAsMob(attackTarget);
                    float rot = rotationYaw - 90;
                    attackTarget.knockBack(this, 0.5F, MathHelper.sin(rot * ((float) Math.PI / 180F)), -MathHelper.cos(rot * ((float) Math.PI / 180F)));
                }
            }
        } else {
            if (!world.isRemote) {
                this.setSprinting(false);
            }
        }
        if (!world.isRemote && isHoneyed() && --honeyedTime <= 0) {
            this.setHoneyed(false);
            honeyedTime = 0;
        }
        if (this.forcedSit && !this.isBeingRidden() && this.isTamed()) {
            this.setSitting(true);
        }
        if (this.isBeingRidden() && this.isSitting()) {
            this.setSitting(false);
        }
        if (!this.world.isRemote && this.isEntityAlive() && isTamed() && !this.isChild() && --this.timeUntilNextFur <= 0) {
            this.entityDropItem(new ItemStack(AMItemRegistry.BEAR_FUR), 0.0F);
            this.timeUntilNextFur = this.rand.nextInt(24000) + 24000;
        }
        if (snowTimer > 0) {
            snowTimer--;
        }
        if (snowTimer == 0 && !world.isRemote) {
            snowTimer = 200 + rand.nextInt(400);
            if (this.isSnowy()) {
                if (!permSnow) {
                    if (this.isBurning() || this.isInWater() || !isSnowingAt(world, this.getPosition().up())) {
                        this.setSnowy(false);
                    }
                }
            } else {
                if (isSnowingAt(world, this.getPosition())) {
                    this.setSnowy(true);
                }
            }
        }
        AMEntityRegistry.updateAnimations(this);
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

    public static boolean isSnowingAt(World world, BlockPos position) {
        if (!world.isRaining()) {
            return false;
        } else if (!world.canSeeSky(position)) {
            return false;
        } else if (world.getPrecipitationHeight(position).getY() > position.getY()) {
            return false;
        } else {
            Biome biome = world.getBiome(position);
            return biome.getEnableSnow() || biome.getTemperature(position) < 0.15F;
        }
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

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STANDING, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(HONEYED, Boolean.FALSE);
        this.dataManager.register(SNOWY, Boolean.FALSE);
        this.dataManager.register(EATING, Boolean.FALSE);
    }

    public boolean isEating() {
        return this.dataManager.get(EATING).booleanValue();
    }

    public void setEating(boolean eating) {
        this.dataManager.set(EATING, Boolean.valueOf(eating));
    }

    public boolean isHoneyed() {
        return this.dataManager.get(HONEYED).booleanValue();
    }

    public void setHoneyed(boolean honeyed) {
        this.dataManager.set(HONEYED, Boolean.valueOf(honeyed));
    }

    public boolean isSnowy() {
        return this.dataManager.get(SNOWY).booleanValue();
    }

    public void setSnowy(boolean snowy) {
        this.dataManager.set(SNOWY, Boolean.valueOf(snowy));
    }

    public boolean isStanding() {
        return this.dataManager.get(STANDING).booleanValue();
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, Boolean.valueOf(standing));
        this.recalcSize = true;
    }

    private void updateSize() {
        float scale = isChild() ? 0.5F : 1.0F;
        if (isStanding()) {
            this.setSize(STAND_WIDTH * scale, STAND_HEIGHT * scale);
        } else {
            this.setSize(NORMAL_WIDTH * scale, NORMAL_HEIGHT * scale);
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityGrizzlyBear) AMEntityRegistry.GRIZZLY_BEAR.newInstance(this.world);
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
        if (animation == ANIMATION_MAUL) {
            maxStandTime = 21;
            this.setStanding(true);
        }
        if (animation == ANIMATION_SWIPE_R || animation == ANIMATION_SWIPE_L) {
            maxStandTime = 2 + rand.nextInt(5);
            this.setStanding(true);
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_MAUL, ANIMATION_SNIFF, ANIMATION_SWIPE_R, ANIMATION_SWIPE_L};
    }

    public boolean shouldMove() {
        return !isSitting();
    }

    private void playWarningSound() {
    }

    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.GRIZZLY_FOODSTUFFS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem targetEntity) {
        ItemStack duplicate = targetEntity.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItemMainhand().isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
        }
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
        if (targetEntity.getItem().getItem() == Items.FISH && this.isHoneyed()) {
            salmonThrowerName = targetEntity.getThrower();
        } else {
            salmonThrowerName = null;
        }
    }

    private boolean canAngerTarget(@Nullable EntityLivingBase target) {
        if (target == null || !this.isAngry()) {
            return false;
        }
        UUID id = this.getAngerTarget();
        return id == null || id.equals(target.getUniqueID());
    }

    class GrizzlyBearAITempt extends EntityAITempt {

        GrizzlyBearAITempt(EntityGrizzlyBear bear, double speed) {
            super(bear, speed, Items.FISH, false);
        }

        @Override
        protected boolean isTempting(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            Item item = stack.getItem();
            if (item == Items.FISH || item == Items.COOKED_FISH) {
                return true;
            }
            return AMTagRegistry.itemInTag(AMTagRegistry.GRIZZLY_HONEY, item);
        }

        @Override
        public boolean shouldExecute() {
            return (!(EntityGrizzlyBear.this instanceof EntityTameable) || EntityGrizzlyBear.this.isTamed()) && super.shouldExecute();
        }
    }

    class HurtByTargetGoal extends EntityAIHurtByTarget {

        HurtByTargetGoal() {
            super(EntityGrizzlyBear.this, true);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            if (EntityGrizzlyBear.this.isChild()) {
                this.alertOthers();
                this.resetTask();
            }
        }

        @Override
        protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase target) {
            if (creature instanceof EntityGrizzlyBear && !creature.isChild()) {
                super.setEntityAttackTarget(creature, target);
            }
        }
    }

    class MeleeAttackGoal extends EntityAIAttackMelee {

        MeleeAttackGoal() {
            super(EntityGrizzlyBear.this, 1.25D, true);
        }

        @Override
        protected void checkAndPerformAttack(EntityLivingBase enemy, double distToEnemySqr) {
            double d0 = this.getAttackReachSqr(enemy);
            if (distToEnemySqr <= d0) {
                if (getAnimation() == NO_ANIMATION || getAnimation() == ANIMATION_SNIFF) {
                    EntityGrizzlyBear.this.setAnimation(rand.nextBoolean() ? ANIMATION_MAUL : rand.nextBoolean() ? ANIMATION_SWIPE_L : ANIMATION_SWIPE_R);
                }
            } else if (distToEnemySqr <= d0 * 2.0D) {
                if (EntityGrizzlyBear.this.isSwingInProgress) {
                    EntityGrizzlyBear.this.swingProgressInt = 0;
                }
                if (EntityGrizzlyBear.this.swingProgressInt <= 10) {
                    EntityGrizzlyBear.this.playWarningSound();
                }
            } else {
                EntityGrizzlyBear.this.swingProgressInt = 0;
            }
        }

        @Override
        public void resetTask() {
            EntityGrizzlyBear.this.setStanding(false);
            super.resetTask();
        }

        @Override
        protected double getAttackReachSqr(EntityLivingBase attackTarget) {
            return 3.0F + attackTarget.width;
        }
    }

    class AttackPlayerGoal extends EntityAINearestAttackableTarget<EntityPlayer> {

        AttackPlayerGoal() {
            super(EntityGrizzlyBear.this, EntityPlayer.class, 3, true, true, null);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityGrizzlyBear.this.isChild() || EntityGrizzlyBear.this.isHoneyed()) {
                return false;
            } else {
                return super.shouldExecute();
            }
        }

        @Override
        protected double getTargetDistance() {
            return 3.0D;
        }
    }

    class PanicGoal extends EntityAIPanic {

        PanicGoal() {
            super(EntityGrizzlyBear.this, 2.0D);
        }

        @Override
        public boolean shouldExecute() {
            return (EntityGrizzlyBear.this.isChild() || EntityGrizzlyBear.this.isBurning()) && super.shouldExecute();
        }
    }
}
