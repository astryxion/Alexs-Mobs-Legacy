package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIRideParent;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIChargeLooker;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIFollowCaravan;
import com.github.alexthe666.alexsmobs.entity.ai.GorillaAIForageLeaves;
import com.github.alexthe666.alexsmobs.entity.ai.GroundPathNavigatorWide;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.citadel.animation.Animation;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntityGorilla extends EntityTameable implements IAnimatedEntity, ITargetsDroppedItems {

    public static final Animation ANIMATION_BREAKBLOCK_R = Animation.create(20);
    public static final Animation ANIMATION_BREAKBLOCK_L = Animation.create(20);
    public static final Animation ANIMATION_POUNDCHEST = Animation.create(40);
    public static final Animation ANIMATION_ATTACK = Animation.create(20);

    private static final float NORMAL_WIDTH = 1.15F;
    private static final float NORMAL_HEIGHT = 1.35F;
    private static final float SILVERBACK_WIDTH = 1.35F;
    private static final float SILVERBACK_HEIGHT = 1.95F;

    private static final DataParameter<Boolean> SILVERBACK = EntityDataManager.createKey(EntityGorilla.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> STANDING = EntityDataManager.createKey(EntityGorilla.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityGorilla.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> EATING = EntityDataManager.createKey(EntityGorilla.class, DataSerializers.BOOLEAN);

    public int maxStandTime = 75;
    public float prevStandProgress;
    public float prevSitProgress;
    public float standProgress;
    public float sitProgress;
    public boolean forcedSit = false;
    private int animationTick;
    private Animation currentAnimation;
    private int standingTime = 0;
    private int eatingTime;
    @Nullable
    private EntityGorilla caravanHead;
    @Nullable
    private EntityGorilla caravanTail;
    private int sittingTime = 0;
    private int maxSitTime = 75;
    @Nullable
    private UUID bananaThrowerID = null;
    private boolean hasSilverbackAttributes = false;
    public int poundChestCooldown = 0;

    public EntityGorilla(World worldIn) {
        super(worldIn);
        this.setSize(NORMAL_WIDTH, NORMAL_HEIGHT);
        this.stepHeight = 1.1F;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.WALKABLE, 0.0F);
    }

    public static boolean isTameableFood(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.GORILLA_TAMEABLES, stack.getItem());
    }

    public static boolean isBanana(ItemStack stack) {
        return isTameableFood(stack);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        BlockPos down = new BlockPos(this).down();
        net.minecraft.block.state.IBlockState below = this.world.getBlockState(down);
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.GORILLA_SPAWNS, below.getBlock()) || below.getBlock() == Blocks.AIR;
        return spawnBlock && this.world.getLight(new BlockPos(this)) > 8
                && AMEntityRegistry.rollSpawn(AMConfig.gorillaSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new GroundPathNavigatorWide(this, worldIn);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return isTamed() && AMTagRegistry.itemInTag(AMTagRegistry.GORILLA_BREEDABLES, stack.getItem());
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
            this.setSitting(false);
            if (entity != null && this.isTamed() && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow)) {
                amount = (amount + 1.0F) / 2.0F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.2D, true));
        this.tasks.addTask(2, new GorillaAIFollowCaravan(this, 0.8D));
        this.tasks.addTask(3, new GorillaAIChargeLooker(this, 1.6D));
        this.tasks.addTask(4, new EntityAITempt(this, 1.1D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return EntityGorilla.isTameableFood(stack);
            }

            @Override
            public boolean shouldExecute() {
                return EntityGorilla.this.isTamed() && super.shouldExecute();
            }
        });
        this.tasks.addTask(4, new AnimalAIRideParent(this, 1.25D));
        this.tasks.addTask(5, new GorillaAIForageLeaves(this));
        this.tasks.addTask(5, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(6, new AIWalkIdle(this, 0.8D));
        this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.GORILLA_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.GORILLA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.GORILLA_HURT;
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (this.getAnimation() == NO_ANIMATION) {
            this.setAnimation(ANIMATION_ATTACK);
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

    @Nullable
    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        if (!this.world.isRemote) {
            int others = 0;
            for (EntityGorilla other : this.world.getEntitiesWithinAABB(EntityGorilla.class, this.getEntityBoundingBox().grow(2.0D, 1.0D, 2.0D))) {
                if (other != this) {
                    others++;
                }
            }
            if (others == 0) {
                this.setSilverback(true);
            } else {
                this.setSilverback(this.rand.nextBoolean());
            }
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Nullable
    public EntityGorilla getNearestSilverback(World world, double dist) {
        List<EntityGorilla> list = world.getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(dist, dist / 2, dist));
        if (list.isEmpty()) {
            return null;
        }
        EntityGorilla gorilla = null;
        double d0 = Double.MAX_VALUE;
        for (EntityGorilla gorrila2 : list) {
            if (gorrila2.isSilverback()) {
                double d1 = this.getDistanceSq(gorrila2);
                if (d1 <= d0) {
                    d0 = d1;
                    gorilla = gorrila2;
                }
            }
        }
        return gorilla;
    }

    @Override
    public void updatePassenger(Entity passenger) {
        if (this.getPassengers().contains(passenger)) {
            this.setSitting(false);
            if (passenger instanceof EntityGorilla) {
                EntityGorilla babyGorilla = (EntityGorilla) passenger;
                babyGorilla.setStanding(this.isStanding());
                babyGorilla.setSitting(this.isSitting());
                babyGorilla.renderYawOffset = this.renderYawOffset;
                babyGorilla.rotationYaw = this.renderYawOffset;
                babyGorilla.rotationYawHead = this.renderYawOffset;
            }
            float sitAdd = -0.03F * this.sitProgress;
            float standAdd = -0.03F * this.standProgress;
            float radius = standAdd + sitAdd;
            float angle = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            passenger.setPosition(this.posX + extraX, this.posY + this.getMountedYOffset() + passenger.getYOffset(), this.posZ + extraZ);
        }
    }

    @Override
    public boolean canBeSteered() {
        return false;
    }

    public double getMountedYOffset() {
        return (double) this.height * 0.65F * getGorillaScale() * (isSilverback() ? 0.75F : 1.0F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SILVERBACK, Boolean.FALSE);
        this.dataManager.register(STANDING, Boolean.FALSE);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(EATING, Boolean.FALSE);
    }

    public boolean isSilverback() {
        return this.dataManager.get(SILVERBACK).booleanValue();
    }

    public void setSilverback(boolean silver) {
        this.dataManager.set(SILVERBACK, Boolean.valueOf(silver));
    }

    public boolean isStanding() {
        return this.dataManager.get(STANDING).booleanValue();
    }

    public void setStanding(boolean standing) {
        this.dataManager.set(STANDING, Boolean.valueOf(standing));
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    public boolean isEating() {
        return this.dataManager.get(EATING).booleanValue();
    }

    public void setEating(boolean eating) {
        this.dataManager.set(EATING, Boolean.valueOf(eating));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Silverback", this.isSilverback());
        compound.setBoolean("Standing", this.isStanding());
        compound.setBoolean("GorillaSitting", this.isSitting());
        compound.setBoolean("ForcedToSit", this.forcedSit);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSilverback(compound.getBoolean("Silverback"));
        this.setStanding(compound.getBoolean("Standing"));
        this.setSitting(compound.getBoolean("GorillaSitting"));
        this.forcedSit = compound.getBoolean("ForcedToSit");
        this.updateSize();
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.NAME_TAG) {
            return super.processInteract(player, hand);
        }
        if (isTamed() && isTameableFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
            this.heal(5);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            return true;
        }
        if (isTamed() && isOwner(player) && !isBreedingItem(itemstack)) {
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
        return super.processInteract(player, hand);
    }

    @Override
    public Animation getAnimation() {
        return currentAnimation;
    }

    @Override
    public void setAnimation(Animation animation) {
        currentAnimation = animation;
        if (animation == ANIMATION_POUNDCHEST) {
            this.maxStandTime = 45;
            this.setStanding(true);
        }
        if (animation == ANIMATION_ATTACK) {
            this.maxStandTime = 10;
            this.setStanding(true);
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!this.getHeldItemMainhand().isEmpty() && this.canTargetItem(this.getHeldItemMainhand())) {
            this.setEating(true);
            this.setSitting(true);
            this.setStanding(false);
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
            if (!isHeldItemLeaves(this.getHeldItemMainhand())) {
                for (int i = 0; i < 3; i++) {
                    double d2 = this.rand.nextGaussian() * 0.02D;
                    double d0 = this.rand.nextGaussian() * 0.02D;
                    double d1 = this.rand.nextGaussian() * 0.02D;
                    ItemStack held = this.getHeldItemMainhand();
                    this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(held.getItem()), held.getMetadata());
                }
            }
            if (eatingTime % 5 == 0) {
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            }
            if (eatingTime > 100) {
                ItemStack stack = this.getHeldItemMainhand();
                if (!stack.isEmpty()) {
                    this.heal(4);
                    if (isTameableFood(stack) && bananaThrowerID != null) {
                        if (getRNG().nextFloat() < 0.3F) {
                            this.setTamed(true);
                            this.setOwnerId(bananaThrowerID);
                            EntityPlayer player = world.getMinecraftServer().getPlayerList().getPlayerByUUID(bananaThrowerID);
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
        prevSitProgress = sitProgress;
        prevStandProgress = standProgress;
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
        if (this.isRiding() && this.getRidingEntity() instanceof EntityGorilla) {
            if (!this.isChild()) {
                this.dismountRidingEntity();
            } else {
                EntityGorilla mount = (EntityGorilla) this.getRidingEntity();
                this.rotationYaw = mount.renderYawOffset;
                this.rotationYawHead = mount.renderYawOffset;
                this.renderYawOffset = mount.renderYawOffset;
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
        if (!forcedSit && this.isSitting() && (this.getAttackTarget() != null || this.isStanding()) && !this.isEating()) {
            this.setSitting(false);
        }
        if (!world.isRemote && this.getAnimation() == NO_ANIMATION && !this.isStanding() && !this.isSitting() && rand.nextInt(1500) == 0) {
            maxSitTime = 300 + rand.nextInt(250);
            this.setSitting(true);
        }
        if (this.forcedSit && !this.isBeingRidden() && this.isTamed()) {
            this.setSitting(true);
        }
        if (sitProgress == 0 && poundChestCooldown <= 0 && this.isSilverback() && this.rand.nextInt(800) == 0 && this.getAnimation() == NO_ANIMATION && !this.isSitting() && !this.isAIDisabled() && this.getHeldItemMainhand().isEmpty()) {
            this.setAnimation(ANIMATION_POUNDCHEST);
        }
        if (!world.isRemote && this.getAttackTarget() != null && this.getAnimation() == ANIMATION_ATTACK && this.getAnimationTick() == 10) {
            float f1 = this.rotationYaw * ((float) Math.PI / 180F);
            this.motionX += -MathHelper.sin(f1) * 0.02F;
            this.motionZ += MathHelper.cos(f1) * 0.02F;
            EntityLivingBase target = this.getAttackTarget();
            target.knockBack(this, 1F, target.posX - this.posX, target.posZ - this.posZ);
            target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue());
        }
        if (isSilverback() && !isChild() && !hasSilverbackAttributes) {
            hasSilverbackAttributes = true;
            this.updateSize();
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50F);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(10F);
            this.heal(50F);
        }
        if (!isSilverback() && !isChild() && hasSilverbackAttributes) {
            hasSilverbackAttributes = false;
            this.updateSize();
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30F);
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(8F);
            this.heal(30F);
        }
        if (poundChestCooldown > 0) {
            poundChestCooldown--;
        }
        AMEntityRegistry.updateAnimations(this);
    }

    private boolean isHeldItemLeaves(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Block block = Block.getBlockFromItem(stack.getItem());
        return block instanceof BlockLeaves;
    }

    private void updateSize() {
        float scale = getGorillaScale();
        if (isSilverback() && !isChild()) {
            this.setSize(SILVERBACK_WIDTH * scale, SILVERBACK_HEIGHT * scale);
        } else {
            this.setSize(NORMAL_WIDTH * scale, NORMAL_HEIGHT * scale);
        }
    }

    @Override
    public int getAnimationTick() {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int i) {
        animationTick = i;
    }

    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.GORILLA_FOODSTUFFS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem targetEntity) {
        ItemStack duplicate = targetEntity.getItem().copy();
        duplicate.setCount(1);
        if (!this.getHeldItemMainhand().isEmpty() && !this.world.isRemote) {
            this.entityDropItem(this.getHeldItemMainhand(), 0.0F);
        }
        this.setHeldItem(EnumHand.MAIN_HAND, duplicate);
        if (EntityGorilla.isTameableFood(targetEntity.getItem()) && !this.isTamed()) {
            String throwerName = targetEntity.getThrower();
            if (throwerName != null && this.world.getMinecraftServer() != null) {
                EntityPlayer player = this.world.getMinecraftServer().getPlayerList().getPlayerByUsername(throwerName);
                if (player != null) {
                    bananaThrowerID = player.getUniqueID();
                }
            }
        }
    }

    @Override
    public Animation[] getAnimations() {
        return new Animation[]{ANIMATION_BREAKBLOCK_R, ANIMATION_BREAKBLOCK_L, ANIMATION_POUNDCHEST, ANIMATION_ATTACK};
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityGorilla) AMEntityRegistry.GORILLA.newInstance(this.world);
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }
        this.caravanHead = null;
    }

    public void joinCaravan(EntityGorilla caravanHeadIn) {
        this.caravanHead = caravanHeadIn;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTrail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public EntityGorilla getCaravanHead() {
        return this.caravanHead;
    }

    public float getGorillaScale() {
        return isChild() ? 0.5F : isSilverback() ? 1.3F : 1.0F;
    }

    public boolean isDonkeyKong() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && (s.toLowerCase().contains("donkey") && s.toLowerCase().contains("kong") || s.toLowerCase().equals("dk"));
    }

    public boolean isFunkyKong() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && (s.toLowerCase().contains("funky") && s.toLowerCase().contains("kong"));
    }

    class AIWalkIdle extends EntityAIBase {

        private final double speed;
        private int executionChance;

        AIWalkIdle(EntityGorilla gorilla, double speed) {
            this.speed = speed;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            this.executionChance = EntityGorilla.this.isSilverback() ? 10 : 120;
            if (EntityGorilla.this.isSitting() || EntityGorilla.this.getAttackTarget() != null) {
                return false;
            }
            return EntityGorilla.this.getNavigator().noPath() && EntityGorilla.this.getRNG().nextInt(this.executionChance) == 0;
        }

        @Override
        public void startExecuting() {
            int range = EntityGorilla.this.isSilverback() ? 25 : 10;
            Vec3d vec = RandomPositionGenerator.findRandomTarget(EntityGorilla.this, range, 7);
            if (vec != null) {
                EntityGorilla.this.getNavigator().tryMoveToXYZ(vec.x, vec.y, vec.z, speed);
            }
        }
    }
}
