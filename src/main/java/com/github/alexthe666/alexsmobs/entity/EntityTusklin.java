package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIHurtByTargetNotBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIMeleeNearby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIPanicBaby;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.entity.ai.EntityAINearestTarget3D;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.entity.ai.TusklinAIRide;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;

public class EntityTusklin extends EntityAnimal implements IAnimatedEntity {

    public static final Animation ANIMATION_RUT = Animation.create(26);
    public static final Animation ANIMATION_GORE_L = Animation.create(25);
    public static final Animation ANIMATION_GORE_R = Animation.create(25);
    public static final Animation ANIMATION_FLING = Animation.create(15);
    public static final Animation ANIMATION_BUCK = Animation.create(15);

    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(EntityTusklin.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> PASSIVETICKS = EntityDataManager.createKey(EntityTusklin.class, DataSerializers.VARINT);

    private int animationTick;
    private Animation currentAnimation;
    private int ridingTime = 0;
    private int entityToLaunchId = -1;
    private int conversionTime = 0;

    public EntityTusklin(World worldIn) {
        super(worldIn);
        this.setSize(2.2F, 1.9F);
        this.stepHeight = 1.1F;
    }

    public static boolean canTusklinSpawn(World worldIn, BlockPos pos) {
        if (worldIn.getLight(pos) <= 8) {
            return false;
        }
        Block block = worldIn.getBlockState(pos.down()).getBlock();
        return block.isFullBlock(worldIn.getBlockState(pos.down()))
                || AMTagRegistry.blockInTag(AMTagRegistry.TUSKLIN_SPAWNS, block);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.tusklinSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(9.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.TUSKLIN_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TUSKLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TUSKLIN_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.TUSKLIN;
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    public boolean isInNether() {
        return this.dimension == 1 && !this.isAIDisabled();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new AnimalAIMeleeNearby(this, 15, 1.25D));
        this.tasks.addTask(3, new TusklinAIRide(this, 2.0D));
        this.tasks.addTask(4, new AnimalAIPanicBaby(this, 1.25D));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 120, 0.6F, 14, 7));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new AnimalAIHurtByTargetNotBaby(this, true));
        this.targetTasks.addTask(2, new EntityAINearestTarget3D(this, EntityPlayer.class, 100, true, false, new Predicate<EntityLivingBase>() {
            @Override
            public boolean apply(@Nullable EntityLivingBase target) {
                if (!(target instanceof EntityPlayer)) {
                    return false;
                }
                return EntityTusklin.this.canAngerTarget((EntityPlayer) target);
            }
        }));
    }

    public boolean canAngerTarget(@Nullable EntityPlayer player) {
        if (player == null) {
            return false;
        }
        if (this.getPassiveTicks() > 0) {
            return false;
        }
        if (isMushroom(player.getHeldItemMainhand()) || isMushroom(player.getHeldItemOffhand())) {
            return false;
        }
        return this.getRevengeTarget() == null || !this.getRevengeTarget().equals(player) || this.canAttackClass(EntityPlayer.class);
    }

    @Nullable
    @Override
    public Entity getControllingPassenger() {
        if (this.isSaddled()) {
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof EntityPlayer) {
                    return passenger;
                }
            }
        }
        return null;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            final int anim = this.rand.nextInt(3);
            switch (anim) {
                case 0:
                    this.setAnimation(ANIMATION_FLING);
                    break;
                case 1:
                    this.setAnimation(ANIMATION_GORE_L);
                    break;
                default:
                    this.setAnimation(ANIMATION_GORE_R);
                    break;
            }
        }
        return true;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            float radius = 0.4F;
            if (this.getAnimation() == ANIMATION_GORE_L || this.getAnimation() == ANIMATION_GORE_R) {
                if (this.getAnimationTick() <= 4) {
                    radius -= this.getAnimationTick() * 0.1F;
                } else {
                    radius -= -0.4F + Math.min(this.getAnimationTick() - 4, 4) * 0.1F;
                }
            }
            if (this.getAnimation() == ANIMATION_BUCK) {
                if (this.getAnimationTick() < 5) {
                    radius -= this.getAnimationTick() * 0.1F;
                } else if (this.getAnimationTick() < 10) {
                    radius -= 0.4F - (this.getAnimationTick() - 5) * 0.1F;
                }
            }
            final float angle = (0.01745329251F * this.renderYawOffset);
            final double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            final double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    public double getMountedYOffset() {
        final float f = Math.min(0.25F, this.limbSwingAmount);
        final float f1 = this.limbSwing;
        float f2 = 0;
        if (this.getAnimation() == ANIMATION_FLING) {
            if (this.getAnimationTick() <= 3F) {
                f2 = this.getAnimationTick() * -0.1F;
            } else {
                f2 = -0.3F + MathHelper.clamp(this.getAnimationTick() - 3, 0, 3) * 0.1F;
            }
        }
        if (this.getAnimation() == ANIMATION_BUCK) {
            if (this.getAnimationTick() < 5) {
                f2 = (this.getAnimationTick() * 0.2F) * 0.8F;
            } else if (this.getAnimationTick() < 10) {
                f2 = (0.8F - (this.getAnimationTick() - 5) * 0.2F) * 0.8F;
            }
        }
        return (double) this.height - 0.3D + (double) (Math.abs(MathHelper.sin(f1 * 0.7F) * f * 0.0625F * 1.6F)) + f2;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (item == Items.SADDLE && !this.isSaddled() && !this.isChild()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setSaddled(true);
            return true;
        }
        if (item == AMItemRegistry.PIGSHOES && this.getShoeStack().isEmpty() && !this.isChild()) {
            this.setShoeStack(itemstack.copy());
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            return true;
        }
        if (isMushroom(itemstack) && (this.getPassiveTicks() <= 0 || this.getHealth() < this.getMaxHealth())) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.heal(6);
            this.setPassiveTicks(this.getPassiveTicks() + 1200);
            return true;
        }
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (!isBreedingItem(itemstack)) {
            if (!player.isSneaking() && !this.isChild() && this.isSaddled() && this.getAnimation() != ANIMATION_BUCK) {
                player.startRiding(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.TUSKLIN_BREEDABLES, stack.getItem());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SADDLED, Boolean.FALSE);
        this.dataManager.register(PASSIVETICKS, 0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        if (!this.getShoeStack().isEmpty()) {
            compound.setTag("ShoeItem", this.getShoeStack().writeToNBT(new NBTTagCompound()));
        }
        compound.setInteger("PassiveTicks", this.getPassiveTicks());
        compound.setBoolean("Saddle", this.isSaddled());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSaddled(compound.getBoolean("Saddle"));
        this.setPassiveTicks(compound.getInteger("PassiveTicks"));
        if (compound.hasKey("ShoeItem", 10)) {
            NBTTagCompound shoeTag = compound.getCompoundTag("ShoeItem");
            ItemStack itemstack = new ItemStack(shoeTag);
            if (!itemstack.isEmpty()) {
                this.setShoeStack(itemstack);
            }
        }
    }

    public boolean isMushroom(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.TUSKLIN_FOODSTUFFS, stack.getItem());
    }

    public int getPassiveTicks() {
        return this.dataManager.get(PASSIVETICKS);
    }

    private void setPassiveTicks(int passiveTicks) {
        this.dataManager.set(PASSIVETICKS, passiveTicks);
    }

    public boolean isSaddled() {
        return this.dataManager.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.dataManager.set(SADDLED, saddled);
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        if (this.isSaddled()) {
            if (!this.world.isRemote) {
                this.entityDropItem(new ItemStack(Items.SADDLE), 0.0F);
            }
        }
        if (!this.getShoeStack().isEmpty()) {
            if (!this.world.isRemote) {
                this.entityDropItem(this.getShoeStack().copy(), 0.0F);
            }
        }
        this.setSaddled(false);
        this.setShoeStack(ItemStack.EMPTY);
    }

    public ItemStack getShoeStack() {
        return this.getItemStackFromSlot(EntityEquipmentSlot.FEET);
    }

    public void setShoeStack(ItemStack shoe) {
        this.setItemStackToSlot(EntityEquipmentSlot.FEET, shoe);
        if (!this.world.isRemote && this.world instanceof net.minecraft.world.WorldServer) {
            ((net.minecraft.world.WorldServer) this.world).getEntityTracker().sendToTrackingAndSelf(
                    this, new net.minecraft.network.play.server.SPacketEntityEquipment(this.getEntityId(), EntityEquipmentSlot.FEET, shoe));
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (isInNether()) {
            conversionTime++;
            if (conversionTime > 300 && !this.world.isRemote) {
                EntityPigZombie pigZombie = new EntityPigZombie(this.world);
                pigZombie.setPosition(this.posX, this.posY, this.posZ);
                pigZombie.rotationYaw = this.rotationYaw;
                pigZombie.rotationYawHead = this.rotationYawHead;
                pigZombie.renderYawOffset = this.renderYawOffset;
                pigZombie.addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.NAUSEA, 200, 0));
                this.dropEquipment(false, 0);
                this.world.spawnEntity(pigZombie);
                this.setDead();
            }
        }
        if (entityToLaunchId != -1 && this.isEntityAlive()) {
            Entity launch = this.world.getEntityByID(entityToLaunchId);
            this.removePassengers();
            entityToLaunchId = -1;
            if (launch != null && !launch.isRiding()) {
                if (launch instanceof EntityLivingBase) {
                    launch.setPosition(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
                    float rot = 180F + this.rotationYaw;
                    double resist = 0.0D;
                    if (((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) != null) {
                        resist = ((EntityLivingBase) launch).getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue();
                    }
                    float strength = (float) (getLaunchStrength() * (1.0D - resist));
                    float x = MathHelper.sin(rot * 0.017453292F);
                    float z = -MathHelper.cos(rot * 0.017453292F);
                    if (!(strength <= 0.0D)) {
                        launch.isAirBorne = true;
                        launch.motionX += x * strength;
                        launch.motionY = strength;
                        launch.motionZ += z * strength;
                    }
                }
            }
        }
        if (this.getAnimation() == ANIMATION_BUCK && this.getAnimationTick() >= 5) {
            Entity passenger = this.getControllingPassenger();
            if (passenger instanceof EntityLivingBase) {
                entityToLaunchId = passenger.getEntityId();
            }
        }
        if (!this.world.isRemote) {
            if (this.isBeingRidden()) {
                ridingTime++;
                if (ridingTime >= this.getMaxRidingTime() && this.getAnimation() != ANIMATION_BUCK) {
                    this.setAnimation(ANIMATION_BUCK);
                }
            } else {
                ridingTime = 0;
            }
            if (this.isEntityAlive() && ridingTime > 0 && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.1D) {
                for (Entity entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(1.0D))) {
                    if (!(entity instanceof EntityTusklin) && !entity.isRiding()) {
                        entity.attackEntityFrom(DamageSource.causeMobDamage(this), 4F + rand.nextFloat() * 3.0F);
                        if (entity.onGround) {
                            double d0 = entity.posX - this.posX;
                            double d1 = entity.posZ - this.posZ;
                            double d2 = Math.max(d0 * d0 + d1 * d1, 0.001D);
                            float f = 0.5F;
                            entity.addVelocity(d0 / d2 * f, f, d1 / d2 * f);
                        }
                    }
                }
                this.stepHeight = 2F;
            } else {
                this.stepHeight = 1.1F;
            }
            EntityLivingBase target = this.getAttackTarget();
            if (target != null && this.canEntityBeSeen(target) && this.getDistance(target) < target.width + this.width + 1.8F) {
                if (this.getAnimation() == ANIMATION_FLING && this.getAnimationTick() == 6) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    knockbackTarget(target, 0.9F, 0F);
                }
                if ((this.getAnimation() == ANIMATION_GORE_L) && this.getAnimationTick() == 6) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    knockbackTarget(target, 0.5F, -90F);
                }
                if ((this.getAnimation() == ANIMATION_GORE_R) && this.getAnimationTick() == 6) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
                    knockbackTarget(target, 0.5F, 90F);
                }
            }
        }
        if (this.getAnimation() == ANIMATION_RUT && this.getAnimationTick() == 23) {
            if (this.world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS && rand.nextInt(3) == 0) {
                if (this.isChild()) {
                    if (this.world.isAirBlock(this.getPosition()) && rand.nextInt(3) == 0) {
                        this.world.setBlockState(this.getPosition(), Blocks.BROWN_MUSHROOM.getDefaultState(), 2);
                        this.playSound(SoundEvents.BLOCK_GRASS_PLACE, this.getSoundVolume(), this.getSoundPitch());
                    }
                }
                this.world.playEvent(2001, this.getPosition().down(), Block.getStateId(Blocks.GRASS.getDefaultState()));
                this.world.setBlockState(this.getPosition().down(), Blocks.DIRT.getDefaultState(), 2);
                this.heal(5);
            }
        }
        if (!this.world.isRemote && this.getAnimation() == NO_ANIMATION && rand.nextInt(isChild() ? 140 : 70) == 0
                && (this.getRevengeTarget() == null || this.getDistance(this.getRevengeTarget()) > 30)) {
            if (this.world.getBlockState(this.getPosition().down()).getBlock() == Blocks.GRASS && rand.nextInt(3) == 0) {
                this.setAnimation(ANIMATION_RUT);
            }
        }
        if (this.getPassiveTicks() > 0 && !this.world.isRemote) {
            this.setPassiveTicks(this.getPassiveTicks() - 1);
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isBeingRidden() && this.getControllingPassenger() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) this.getControllingPassenger();
            this.rotationYaw = player.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.renderYawOffset = this.rotationYaw;
            this.rotationPitch = player.rotationPitch * 0.25F;
            this.rotationYawHead = this.rotationYaw;
            this.stepHeight = 1.0F;
            this.getNavigator().clearPath();
            this.setAttackTarget(null);
            this.setSprinting(true);
            super.travel(0.0F, vertical, 1.0F);
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() != null;
    }

    private float getLaunchStrength() {
        return this.getShoeStack().getItem() == AMItemRegistry.PIGSHOES ? 0.4F : 0.9F;
    }

    private int getMaxRidingTime() {
        return this.getShoeStack().getItem() == AMItemRegistry.PIGSHOES ? 160 : 60;
    }

    private void knockbackTarget(EntityLivingBase entity, float strength, float angle) {
        float rot = rotationYaw + angle;
        if (entity != null) {
            entity.knockBack(this, strength, MathHelper.sin(rot * 0.017453292F), -MathHelper.cos(rot * 0.017453292F));
        }
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

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        if (livingdata == null && this.rand.nextFloat() < 0.34F) {
            this.setGrowingAge(-24000);
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_RUT, ANIMATION_GORE_L, ANIMATION_GORE_R, ANIMATION_FLING, ANIMATION_BUCK};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return new EntityTusklin(this.world);
    }
}
