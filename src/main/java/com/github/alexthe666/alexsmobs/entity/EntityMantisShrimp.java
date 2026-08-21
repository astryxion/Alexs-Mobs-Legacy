package com.github.alexthe666.alexsmobs.entity;

import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class EntityMantisShrimp extends EntityTameable implements ISemiAquatic, IFollower {

    private static final DataParameter<Float> RIGHT_EYE_PITCH = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> RIGHT_EYE_YAW = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LEFT_EYE_PITCH = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> LEFT_EYE_YAW = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> PUNCH_TICK = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MOISTNESS = EntityDataManager.createKey(EntityMantisShrimp.class, DataSerializers.VARINT);
    public float prevRightPitch;
    public float prevRightYaw;
    public float prevLeftPitch;
    public float prevLeftYaw;
    public float prevInWaterProgress;
    public float inWaterProgress;
    public float prevPunchProgress;
    public float punchProgress;
    private int leftLookCooldown = 0;
    private int rightLookCooldown = 0;
    private float targetRightPitch;
    private float targetRightYaw;
    private float targetLeftPitch;
    private float targetLeftYaw;
    private boolean isLandNavigator;
    private int fishFeedings;
    private int moistureAttackTime = 0;

    public EntityMantisShrimp(World world) {
        super(world);
        this.setSize(1.1F, 0.9F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(false);
        this.stepHeight = 1.0F;
    }

    public static boolean canMantisShrimpSpawn(World worldIn, BlockPos pos) {
        BlockPos downPos = pos;
        while (downPos.getY() > 1 && worldIn.getBlockState(downPos).getMaterial() == Material.WATER) {
            downPos = downPos.down();
        }
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.MANTIS_SHRIMP_SPAWNS, worldIn.getBlockState(downPos).getBlock());
        return spawnBlock && downPos.getY() < worldIn.getSeaLevel() + 1;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MANTIS_SHRIMP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MANTIS_SHRIMP_HURT;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else {
            Entity entity = source.getTrueSource();
            if (entity instanceof EntityShulker || entity instanceof EntityShulkerBullet) {
                amount = (amount + 1.0F) * 0.33F;
            }
            return super.attackEntityFrom(source, amount);
        }
    }

    @Override
    public void onKillEntity(EntityLivingBase entity) {
        if (entity instanceof EntityShulker && !this.world.isRemote) {
            clearDeathLoot(entity);
            this.entityDropItem(new ItemStack(Items.SHULKER_SHELL), 0.0F);
        }
        super.onKillEntity(entity);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.mantisShrimpSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.1D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed();
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new MantisShrimpAIFryRice(this));
        this.tasks.addTask(0, new MantisShrimpAIBreakBlocks(this));
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new FollowOwner(this, 1.3D, 4.0F, 2.0F, false));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, false));
        this.tasks.addTask(4, new AnimalAIFindWater(this));
        this.tasks.addTask(4, new AnimalAILeaveWater(this));
        this.tasks.addTask(5, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(6, new EntityAITempt(this, 1.0D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                Item item = stack.getItem();
                return item == Items.FISH || item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL;
            }
        });
        this.tasks.addTask(7, new SemiAquaticAIRandomSwimming(this, 1.0D, 30));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAINearestTarget3D(this, EntityLivingBase.class, 120, false, true, AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.MANTIS_SHRIMP_TARGETS)) {
            @Override
            public boolean shouldExecute() {
                return EntityMantisShrimp.this.getCommand() != 3 && !EntityMantisShrimp.this.isSitting() && super.shouldExecute();
            }
        });
        this.targetTasks.addTask(4, new EntityAIHurtByTarget(this, true));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, this.world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AnimalSwimMoveControllerSink(this, 1.0F, 1.0F);
            this.navigator = new SemiAquaticPathNavigator(this, this.world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isSitting()) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            super.travel(0.0F, 0.0F, 0.0F);
            return;
        }
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return source == DamageSource.DROWN || source == DamageSource.IN_WALL || source == DamageSource.FALLING_BLOCK || super.isEntityInvulnerable(source);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(RIGHT_EYE_PITCH, 0F);
        this.dataManager.register(RIGHT_EYE_YAW, 0F);
        this.dataManager.register(LEFT_EYE_PITCH, 0F);
        this.dataManager.register(LEFT_EYE_YAW, 0F);
        this.dataManager.register(PUNCH_TICK, 0);
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(SITTING, Boolean.FALSE);
        this.dataManager.register(MOISTNESS, 60000);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Item item = stack.getItem();
        return isTamed() && (item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        this.punch();
        return true;
    }

    public void punch() {
        this.dataManager.set(PUNCH_TICK, 4);
    }

    public float getEyeYaw(boolean left) {
        return this.dataManager.get(left ? LEFT_EYE_YAW : RIGHT_EYE_YAW);
    }

    public float getEyePitch(boolean left) {
        return this.dataManager.get(left ? LEFT_EYE_PITCH : RIGHT_EYE_PITCH);
    }

    public void setEyePitch(boolean left, float pitch) {
        this.dataManager.set(left ? LEFT_EYE_PITCH : RIGHT_EYE_PITCH, pitch);
    }

    public void setEyeYaw(boolean left, float yaw) {
        this.dataManager.set(left ? LEFT_EYE_YAW : RIGHT_EYE_YAW, yaw);
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

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public int getMoistness() {
        return this.dataManager.get(MOISTNESS);
    }

    public void setMoistness(int moistness) {
        this.dataManager.set(MOISTNESS, moistness);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (this.isAIDisabled()) {
            this.setAir(300);
        } else {
            if (this.isInWater() || this.world.isRainingAt(this.getPosition()) || this.getHeldItemMainhand().getItem() == Items.WATER_BUCKET) {
                this.setMoistness(60000);
            } else {
                this.setMoistness(this.getMoistness() - 1);
                if (this.getMoistness() <= 0 && moistureAttackTime-- <= 0) {
                    this.setCommand(0);
                    this.setSitting(false);
                    this.attackEntityFrom(DamageSource.STARVE, this.rand.nextInt(2) == 0 ? 1.0F : 0.0F);
                    moistureAttackTime = 20;
                }
            }
        }
        if (this.isPotionActive(MobEffects.LEVITATION)) {
            this.motionY *= 0.5D;
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (!isTamed() && (item == Items.FISH || item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL)) {
            consumeItemFromStack(player, itemstack);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            fishFeedings++;
            if (fishFeedings > 10 && this.getRNG().nextInt(6) == 0 || fishFeedings > 30) {
                this.setTamedBy(player);
                this.world.setEntityState(this, (byte) 7);
            } else {
                this.world.setEntityState(this, (byte) 6);
            }
            return true;
        }
        if (isTamed() && (item == Items.FISH || item == Items.COOKED_FISH)) {
            if (this.getHealth() < this.getMaxHealth()) {
                consumeItemFromStack(player, itemstack);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.heal(5.0F);
                return true;
            }
            return false;
        }
        if (isTamed() && isOwner(player)) {
            if (player.isSneaking() || AMTagRegistry.itemInTag(AMTagRegistry.SHRIMP_RICE_FRYABLES, item)) {
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
            } else if (!isBreedingItem(itemstack)) {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 4) {
                    this.setCommand(0);
                }
                if (this.getCommand() == 3) {
                    player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.mantis_shrimp.command_3", this.getName()), true);
                } else {
                    player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                }
                boolean sit = this.getCommand() == 2;
                this.setSitting(sit);
                return true;
            }
        }
        return false;
    }

    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("MantisShrimpSitting", this.isSitting());
        compound.setInteger("Command", this.getCommand());
        compound.setInteger("Moisture", this.getMoistness());
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setSitting(compound.getBoolean("MantisShrimpSitting"));
        this.setCommand(compound.getInteger("Command"));
        this.setVariant(compound.getInteger("Variant"));
        this.setMoistness(compound.getInteger("Moisture"));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.isChild() && this.getEyeHeight() > this.height) {
            this.setSize(this.width, this.height);
        }
        this.prevLeftPitch = this.getEyePitch(true);
        this.prevRightPitch = this.getEyePitch(false);
        this.prevLeftYaw = this.getEyeYaw(true);
        this.prevRightYaw = this.getEyeYaw(false);
        this.prevInWaterProgress = this.inWaterProgress;
        this.prevPunchProgress = this.punchProgress;
        updateEyes();
        if (this.isSitting() && this.getNavigator().noPath()) {
            this.getNavigator().clearPath();
        }
        if (this.isInWater() && this.inWaterProgress < 5F) {
            this.inWaterProgress++;
        }
        if (!this.isInWater() && this.inWaterProgress > 0F) {
            this.inWaterProgress--;
        }
        if (this.isInWater() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!this.isInWater() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (this.dataManager.get(PUNCH_TICK) > 0) {
            EntityLivingBase target = this.getAttackTarget();
            if (this.dataManager.get(PUNCH_TICK) == 2 && target != null && this.getDistance(target) < 2.8D) {
                if (isAquaticPrey(target) && !this.isTamed()) {
                    clearDeathLoot(target);
                }
                target.knockBack(this, 1.7F, this.posX - target.posX, this.posZ - target.posZ);
                float knockbackResist = (float) MathHelper.clamp(1.0D - target.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue(), 0.0D, 1.0D);
                target.motionY += knockbackResist * 0.8F;
                if (!target.isInWater()) {
                    target.setFire(2);
                }
                target.attackEntityFrom(DamageSource.causeMobDamage(this), (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
            }
            if (this.punchProgress == 1) {
                this.playSound(AMSoundRegistry.MANTIS_SHRIMP_SNAP, this.getSoundPitch(), this.getSoundVolume());
            }
            if (this.punchProgress == 2 && this.world.isRemote && this.isInWater()) {
                for (int i = 0; i < 10 + this.rand.nextInt(8); i++) {
                    double d2 = this.rand.nextGaussian() * 0.6D;
                    double d0 = this.rand.nextGaussian() * 0.2D;
                    double d1 = this.rand.nextGaussian() * 0.6D;
                    float radius = this.width * 0.85F;
                    float angle = (0.01745329251F * this.renderYawOffset);
                    double extraX = radius * MathHelper.sin((float) (Math.PI + angle)) + this.rand.nextFloat() * 0.5F - 0.25F;
                    double extraZ = radius * MathHelper.cos(angle) + this.rand.nextFloat() * 0.5F - 0.25F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + extraX, this.posY + this.height * 0.3F + this.rand.nextFloat() * 0.15F, this.posZ + extraZ, d0, d1, d2);
                }
            }
            if (this.punchProgress < 2F) {
                this.punchProgress++;
            }
            this.dataManager.set(PUNCH_TICK, this.dataManager.get(PUNCH_TICK) - 1);
        } else {
            if (this.punchProgress > 0F) {
                this.punchProgress -= 0.25F;
            }
        }
    }

    private static boolean isAquaticPrey(EntityLivingBase entity) {
        return entity instanceof EntityWaterMob && !(entity instanceof EntityGuardian);
    }

    private static void clearDeathLoot(EntityLivingBase entity) {
        if (entity instanceof net.minecraft.entity.EntityLiving) {
            net.minecraft.entity.EntityLiving living = (net.minecraft.entity.EntityLiving) entity;
            try {
                ReflectionHelper.findField(net.minecraft.entity.EntityLiving.class, "deathLootTable", "field_184659_bA")
                        .set(living, null);
            } catch (Exception e) {
                AlexsMobs.LOGGER.warn("Could not clear death loot for {}", entity, e);
            }
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

    private void updateEyes() {
        float leftPitchDist = Math.abs(this.getEyePitch(true) - targetLeftPitch);
        float rightPitchDist = Math.abs(this.getEyePitch(false) - targetRightPitch);
        float leftYawDist = Math.abs(this.getEyeYaw(true) - targetLeftYaw);
        float rightYawDist = Math.abs(this.getEyeYaw(false) - targetRightYaw);
        if (rightLookCooldown == 0 && this.rand.nextInt(20) == 0 && rightPitchDist < 0.5F && rightYawDist < 0.5F) {
            targetRightPitch = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            targetRightYaw = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            rightLookCooldown = 3 + this.rand.nextInt(15);
        }
        if (leftLookCooldown == 0 && this.rand.nextInt(20) == 0 && leftPitchDist < 0.5F && leftYawDist < 0.5F) {
            targetLeftPitch = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            targetLeftYaw = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            leftLookCooldown = 3 + this.rand.nextInt(15);
        }
        if (this.getEyePitch(true) < this.targetLeftPitch && leftPitchDist > 0.5F) {
            this.setEyePitch(true, this.getEyePitch(true) + Math.min(leftPitchDist, 4F));
        }
        if (this.getEyePitch(true) > this.targetLeftPitch && leftPitchDist > 0.5F) {
            this.setEyePitch(true, this.getEyePitch(true) - Math.min(leftPitchDist, 4F));
        }
        if (this.getEyePitch(false) < this.targetRightPitch && rightPitchDist > 0.5F) {
            this.setEyePitch(false, this.getEyePitch(false) + Math.min(rightPitchDist, 4F));
        }
        if (this.getEyePitch(false) > this.targetRightPitch && rightPitchDist > 0.5F) {
            this.setEyePitch(false, this.getEyePitch(false) - Math.min(rightPitchDist, 4F));
        }
        if (this.getEyeYaw(true) < this.targetLeftYaw && leftYawDist > 0.5F) {
            this.setEyeYaw(true, this.getEyeYaw(true) + Math.min(leftYawDist, 4F));
        }
        if (this.getEyeYaw(true) > this.targetLeftYaw && leftYawDist > 0.5F) {
            this.setEyeYaw(true, this.getEyeYaw(true) - Math.min(leftYawDist, 4F));
        }
        if (this.getEyeYaw(false) < this.targetRightYaw && rightYawDist > 0.5F) {
            this.setEyeYaw(false, this.getEyeYaw(false) + Math.min(rightYawDist, 4F));
        }
        if (this.getEyeYaw(false) > this.targetRightYaw && rightYawDist > 0.5F) {
            this.setEyeYaw(false, this.getEyeYaw(false) - Math.min(rightYawDist, 4F));
        }
        if (rightLookCooldown > 0) {
            rightLookCooldown--;
        }
        if (leftLookCooldown > 0) {
            leftLookCooldown--;
        }
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setVariant(this.getRNG().nextInt(3));
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Nullable
    @Override
    public EntityMantisShrimp createChild(EntityAgeable ageable) {
        EntityMantisShrimp shrimp = new EntityMantisShrimp(this.world);
        shrimp.setVariant(this.getRNG().nextInt(3));
        return shrimp;
    }

    @Override
    public boolean shouldEnterWater() {
        return (this.getHeldItemMainhand().isEmpty() || this.getHeldItemMainhand().getItem() != Items.WATER_BUCKET) && !this.isSitting();
    }

    @Override
    public boolean shouldLeaveWater() {
        return this.getHeldItemMainhand().getItem() == Items.WATER_BUCKET;
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isSitting();
    }

    @Override
    public int getWaterSearchRange() {
        return 16;
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    protected void updateAir(int air) {
    }

    public class FollowOwner extends EntityAIBase {
        private final EntityMantisShrimp tameable;
        private final net.minecraft.world.IBlockAccess world;
        private final double followSpeed;
        private final float maxDist;
        private final float minDist;
        private final boolean teleportToLeaves;
        private EntityLivingBase owner;
        private int timeToRecalcPath;
        private float oldWaterCost;

        public FollowOwner(EntityMantisShrimp tameable, double followSpeed, float minDist, float maxDist, boolean teleportToLeaves) {
            this.tameable = tameable;
            this.world = tameable.world;
            this.followSpeed = followSpeed;
            this.minDist = minDist;
            this.maxDist = maxDist;
            this.teleportToLeaves = teleportToLeaves;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase ownerEntity = this.tameable.getOwner();
            if (ownerEntity == null) {
                return false;
            } else if (ownerEntity instanceof EntityPlayer && (((EntityPlayer) ownerEntity).capabilities.isCreativeMode || ownerEntity instanceof EntityPlayerMP && ((EntityPlayerMP) ownerEntity).isSpectator())) {
                return false;
            } else if (this.tameable.isSitting() || tameable.getCommand() != 1) {
                return false;
            } else if (this.tameable.getDistanceSq(ownerEntity) < (double) (this.minDist * this.minDist)) {
                return false;
            } else if (this.tameable.getAttackTarget() != null && this.tameable.getAttackTarget().isEntityAlive()) {
                return false;
            } else {
                this.owner = ownerEntity;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (this.tameable.getNavigator().noPath()) {
                return false;
            } else if (this.tameable.isSitting() || tameable.getCommand() != 1) {
                return false;
            } else if (this.tameable.getAttackTarget() != null && this.tameable.getAttackTarget().isEntityAlive()) {
                return false;
            } else {
                return this.tameable.getDistanceSq(this.owner) > (double) (this.maxDist * this.maxDist);
            }
        }

        @Override
        public void startExecuting() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.tameable.getPathPriority(PathNodeType.WATER);
            this.tameable.setPathPriority(PathNodeType.WATER, 0.0F);
        }

        @Override
        public void resetTask() {
            this.owner = null;
            this.tameable.getNavigator().clearPath();
            this.tameable.setPathPriority(PathNodeType.WATER, this.oldWaterCost);
        }

        @Override
        public void updateTask() {
            this.tameable.getLookHelper().setLookPositionWithEntity(this.owner, 10.0F, (float) this.tameable.getVerticalFaceSpeed());
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;
                if (!this.tameable.getLeashed() && !this.tameable.isRiding()) {
                    if (this.tameable.getDistanceSq(this.owner) >= 144.0D) {
                        this.tryToTeleportNearEntity();
                    } else {
                        this.tameable.getNavigator().tryMoveToEntityLiving(this.owner, this.followSpeed);
                    }
                }
            }
        }

        private void tryToTeleportNearEntity() {
            BlockPos origin = this.owner.getPosition();
            for (int i = 0; i < 10; ++i) {
                int x = this.getRandomNumber(-3, 3);
                int y = this.getRandomNumber(-1, 1);
                int z = this.getRandomNumber(-3, 3);
                if (this.tryToTeleportToLocation(origin.getX() + x, origin.getY() + y, origin.getZ() + z)) {
                    return;
                }
            }
        }

        private boolean tryToTeleportToLocation(int x, int y, int z) {
            if (Math.abs((double) x - this.owner.posX) < 2.0D && Math.abs((double) z - this.owner.posZ) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(x, y, z))) {
                return false;
            } else {
                this.tameable.setPosition((double) x + 0.5D, (double) y, (double) z + 0.5D);
                this.tameable.getNavigator().clearPath();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos pos) {
            PathNodeType nodeType = new WalkNodeProcessor().getPathNodeType(this.world, pos.getX(), pos.getY(), pos.getZ());
            Material mat = this.world.getBlockState(pos).getMaterial();
            if (mat == Material.WATER || (mat != Material.WATER && this.world.getBlockState(pos.down()).getMaterial() == Material.WATER)) {
                return true;
            }
            if (nodeType != PathNodeType.WALKABLE || tameable.getMoistness() < 2000) {
                return false;
            } else {
                IBlockState below = this.world.getBlockState(pos.down());
                if (!this.teleportToLeaves && below.getBlock() instanceof BlockLeaves) {
                    return false;
                } else {
                    BlockPos offset = pos.subtract(this.tameable.getPosition());
                    return this.tameable.world.getCollisionBoxes(this.tameable, this.tameable.getEntityBoundingBox().offset(offset.getX(), offset.getY(), offset.getZ())).isEmpty();
                }
            }
        }

        private int getRandomNumber(int min, int max) {
            return this.tameable.getRNG().nextInt(max - min + 1) + min;
        }
    }
}
