package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityCapuchinMonkey extends EntityTameable implements IAnimatedEntity, IFollower, ITargetsDroppedItems {

    public static final Animation ANIMATION_THROW = Animation.create(12);
    public static final Animation ANIMATION_HEADTILT = Animation.create(15);
    public static final Animation ANIMATION_SCRATCH = Animation.create(20);

    protected static final DataParameter<Boolean> DART = EntityDataManager.createKey(EntityCapuchinMonkey.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityCapuchinMonkey.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityCapuchinMonkey.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityCapuchinMonkey.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DART_TARGET = EntityDataManager.createKey(EntityCapuchinMonkey.class, DataSerializers.VARINT);
    public float prevSitProgress;
    public float sitProgress;
    public boolean forcedSit = false;
    public boolean attackDecision = false;
    private int animationTick;
    private Animation currentAnimation;
    private int sittingTime = 0;
    private int maxSitTime = 75;
    private int rideCooldown = 0;

    public EntityCapuchinMonkey(World worldIn) {
        super(worldIn);
        this.setPathPriority(PathNodeType.OPEN, 0.0F);
        this.setSize(0.65F, 0.75F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.capuchinMonkeySpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 8;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 4.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAISit(this));
        this.tasks.addTask(3, new CapuchinAIMelee(this, 1.0D, true));
        this.tasks.addTask(3, new CapuchinAIRangedAttack(this, 1.0D, 20, 15.0F));
        this.tasks.addTask(4, new EntityAITempt(this, 1.1D, Items.AIR, true) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return isTameableFood(stack);
            }

            @Override
            public void updateTask() {
                super.updateTask();
                EntityPlayer player = EntityCapuchinMonkey.this.world.getClosestPlayerToEntity(EntityCapuchinMonkey.this, 10.0D);
                if (player != null && EntityCapuchinMonkey.this.getDistanceSq(player) < 6.25D && EntityCapuchinMonkey.this.getRNG().nextInt(14) == 0) {
                    EntityCapuchinMonkey.this.setAnimation(ANIMATION_HEADTILT);
                }
            }
        });
        this.tasks.addTask(6, new TameableAIFollowOwner(this, 1.0D, 10.0F, 2.0F, false));
        this.tasks.addTask(7, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(8, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(4, new EntityAIHurtByTarget(this, true, EntityTossedItem.class));
        this.targetTasks.addTask(5, new CapuchinAITargetBalloons(this, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.CAPUCHIN_MONKEY_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.CAPUCHIN_MONKEY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.CAPUCHIN_MONKEY_HURT;
    }

    @Override
    public boolean isOnSameTeam(Entity entityIn) {
        if (this.isTamed()) {
            EntityLivingBase owner = this.getOwner();
            if (entityIn == owner) {
                return true;
            }
            if (entityIn instanceof EntityTameable) {
                EntityTameable other = (EntityTameable) entityIn;
                return other.isTamed() && other.getOwnerId() != null && other.getOwnerId().equals(this.getOwnerId());
            }
            if (owner != null) {
                return owner.isOnSameTeam(entityIn);
            }
        }
        return super.isOnSameTeam(entityIn);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("MonkeySitting", this.isSitting());
        compound.setBoolean("HasDart", this.hasDart());
        compound.setBoolean("ForcedToSit", this.forcedSit);
        compound.setInteger("Command", this.getCommand());
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("MonkeySitting"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.setCommand(compound.getInteger("Command"));
        this.setDart(compound.getBoolean("HasDart"));
        this.setVariant(compound.getInteger("Variant"));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevSitProgress = this.sitProgress;
        if (this.isSitting() && sitProgress < 10) {
            sitProgress += 1;
        }
        if (!this.isSitting() && sitProgress > 0) {
            sitProgress -= 1;
        }
        if (isSitting() && !forcedSit && ++sittingTime > maxSitTime) {
            this.setSitting(false);
            sittingTime = 0;
            maxSitTime = 75 + rand.nextInt(50);
        }
        if (!world.isRemote && this.getAnimation() == NO_ANIMATION && !this.isSitting() && this.getCommand() != 1 && rand.nextInt(1500) == 0) {
            maxSitTime = 300 + rand.nextInt(250);
            this.setSitting(true);
        }
        this.stepHeight = 2.0F;
        if (!forcedSit && this.isSitting() && (this.getDartTarget() != null || this.getCommand() == 1)) {
            this.setSitting(false);
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.getAnimation() == ANIMATION_SCRATCH && this.getAnimationTick() == 10) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * 0.3F;
            this.motionZ += MathHelper.cos(f1) * 0.3F;
            this.getAttackTarget().knockBack(this, 1.0F, this.getAttackTarget().posX - this.posX, this.getAttackTarget().posZ - this.posZ);
            this.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(this),
                    (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            this.setAttackDecision(this.getAttackTarget());
        }
        if (!world.isRemote && this.getDartTarget() != null && this.getDartTarget().isEntityAlive()
                && this.getAnimation() == ANIMATION_THROW && this.getAnimationTick() == 5) {
            Entity dartTarget = this.getDartTarget();
            double d0 = dartTarget.posX + dartTarget.motionX - this.posX;
            double d1 = dartTarget.posY + dartTarget.getEyeHeight() - 1.1D - this.posY;
            double d2 = dartTarget.posZ + dartTarget.motionZ - this.posZ;
            float f = MathHelper.sqrt(d0 * d0 + d2 * d2);
            EntityTossedItem tossedItem = new EntityTossedItem(this.world, this);
            tossedItem.setDart(this.hasDart());
            tossedItem.rotationPitch -= -20.0F;
            tossedItem.shoot(d0, d1 + (double) (f * 0.2F), d2, hasDart() ? 1.15F : 0.75F, 8.0F);
            if (!this.isSilent()) {
                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0F, 0.8F + this.rand.nextFloat() * 0.4F);
            }
            this.world.spawnEntity(tossedItem);
            this.setAttackDecision(this.getDartTarget());
        }
        if (rideCooldown > 0) {
            rideCooldown--;
        }
        if (!world.isRemote && getAnimation() == NO_ANIMATION && this.getRNG().nextInt(300) == 0) {
            setAnimation(ANIMATION_HEADTILT);
        }
        if (!world.isRemote && this.isSitting()) {
            this.getNavigator().clearPath();
        }
        AMEntityRegistry.updateAnimations(this);
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_SCRATCH);
        }
        return true;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting()) {
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
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        if (hasDart()) {
            this.dropItem(AMItemRegistry.ANCIENT_DART, 1);
        }
    }

    @Override
    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if (this.isRiding() && !entity.isEntityAlive()) {
            this.dismountRidingEntity();
        } else if (isTamed() && entity instanceof EntityLivingBase && isOwner((EntityLivingBase) entity)) {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onUpdate();
            if (this.isRiding()) {
                Entity mount = this.getRidingEntity();
                if (mount instanceof EntityPlayer) {
                    this.renderYawOffset = ((EntityLivingBase) mount).renderYawOffset;
                    this.rotationYaw = mount.rotationYaw;
                    this.rotationYawHead = ((EntityLivingBase) mount).rotationYawHead;
                    this.prevRotationYaw = ((EntityLivingBase) mount).rotationYawHead;
                    float radius = 0F;
                    float angle = (0.01745329251F * (((EntityLivingBase) mount).renderYawOffset - 180F));
                    double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
                    double extraZ = radius * MathHelper.cos(angle);
                    this.setPosition(mount.posX + extraX, Math.max(mount.posY + mount.height + 0.1, mount.posY), mount.posZ + extraZ);
                    attackDecision = true;
                    if (!mount.isEntityAlive() || rideCooldown == 0 && mount.isSneaking()) {
                        this.dismountRidingEntity();
                        attackDecision = false;
                    }
                }
            }
        } else {
            super.updateRidden();
        }
    }

    public void setAttackDecision(Entity target) {
        if (target instanceof EntityMob || this.hasDart()) {
            attackDecision = true;
        } else {
            attackDecision = !attackDecision;
        }
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND);
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, command);
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING);
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, sit);
    }

    public boolean hasDartTarget() {
        return this.dataManager.get(DART_TARGET) != -1 && this.hasDart();
    }

    public void setDartTarget(@Nullable Entity entity) {
        this.dataManager.set(DART_TARGET, entity == null ? -1 : entity.getEntityId());
        if (entity instanceof EntityLivingBase) {
            this.setAttackTarget((EntityLivingBase) entity);
        }
    }

    @Nullable
    public Entity getDartTarget() {
        if (!this.hasDartTarget()) {
            return this.getAttackTarget();
        } else {
            Entity entity = this.world.getEntityByID(this.dataManager.get(DART_TARGET));
            if (entity == null || !entity.isEntityAlive()) {
                return this.getAttackTarget();
            } else {
                return entity;
            }
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(DART_TARGET, -1);
        this.dataManager.register(SITTING, false);
        this.dataManager.register(DART, false);
        this.dataManager.register(VARIANT, 0);
    }

    public boolean hasDart() {
        return this.dataManager.get(DART);
    }

    public void setDart(boolean dart) {
        this.dataManager.set(DART, dart);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public static boolean isTameableFood(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.CAPUCHIN_MONKEY_TAMEABLES, stack.getItem());
    }

    public static boolean isCapuchinFood(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.CAPUCHIN_MONKEY_BREEDABLES, stack.getItem())
                || AMTagRegistry.itemInTag(AMTagRegistry.CAPUCHIN_MONKEY_FOODSTUFFS, stack.getItem());
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        int i;
        if (livingdata instanceof CapuchinGroupData) {
            i = ((CapuchinGroupData) livingdata).variant;
        } else {
            i = this.rand.nextInt(4);
            livingdata = new CapuchinGroupData(i);
        }
        this.setVariant(i);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Nullable
    @Override
    public EntityCapuchinMonkey createChild(EntityAgeable ageable) {
        EntityCapuchinMonkey child = new EntityCapuchinMonkey(this.world);
        child.setVariant(this.getVariant());
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            child.setOwnerId(ownerId);
            child.setTamed(true);
        }
        return child;
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
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick) {
        animationTick = tick;
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    public static boolean isBanana(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.BANANAS, stack.getItem());
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (isTameableFood(itemstack)) {
            if (!isTamed()) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                if (getRNG().nextInt(5) == 0) {
                    this.setTamedBy(player);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.world.setEntityState(this, (byte) 6);
                }
                return true;
            }
            if (isTamed() && isCapuchinFood(itemstack) && !isBreedingItem(itemstack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
                this.heal(5.0F);
                return true;
            }
        }
        if (isTamed() && isOwner(player) && !isBreedingItem(itemstack) && !isTameableFood(itemstack) && !isCapuchinFood(itemstack)) {
            if (!this.hasDart() && item == AMItemRegistry.ANCIENT_DART) {
                this.setDart(true);
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                return true;
            }
            if (this.hasDart() && item == Items.SHEARS) {
                this.setDart(false);
                itemstack.damageItem(1, player);
                return true;
            }
            if (player.isSneaking() && player.getPassengers().isEmpty()) {
                this.startRiding(player, true);
                rideCooldown = 20;
                return true;
            } else {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                if (sit) {
                    this.forcedSit = true;
                    this.setSitting(true);
                    return true;
                } else {
                    this.forcedSit = false;
                    this.setSitting(false);
                    return true;
                }
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_THROW, ANIMATION_SCRATCH};
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return isCapuchinFood(stack) || isTameableFood(stack);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.CAPUCHIN_MONKEY_BREEDABLES, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.heal(5.0F);
        this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
        if (isBanana(e.getItem())) {
            if (getRNG().nextInt(4) == 0) {
                this.entityDropItem(new ItemStack(AMBlockRegistry.BANANA_PEEL), 0.0F);
            }
        }
        String throwerName = e.getThrower();
        if (AMTagRegistry.itemInTag(AMTagRegistry.CAPUCHIN_MONKEY_TAMEABLES, e.getItem().getItem()) && throwerName != null && !this.isTamed()) {
            if (getRNG().nextInt(5) == 0) {
                this.setTamed(true);
                if (!this.world.isRemote) {
                    EntityPlayer thrower = this.world.getPlayerEntityByName(throwerName);
                    if (thrower != null) {
                        this.setOwnerId(thrower.getUniqueID());
                    }
                }
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
        }
    }

    public static class CapuchinGroupData implements net.minecraft.entity.IEntityLivingData {
        public final int variant;

        public CapuchinGroupData(int variant) {
            this.variant = variant;
        }
    }
}
