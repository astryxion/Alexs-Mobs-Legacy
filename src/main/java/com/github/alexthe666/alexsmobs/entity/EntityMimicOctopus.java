package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class EntityMimicOctopus extends EntityTameable implements ISemiAquatic, IFollower {

    private static final DataParameter<Boolean> STOP_CHANGE = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> UPGRADED = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> MIMIC_ORDINAL = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PREV_MIMIC_ORDINAL = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MOISTNESS = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> COMMAND = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MIMICKED_BLOCK = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PREV_MIMICKED_BLOCK = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SITTING = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> LAST_SCARED_MOB_ID = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> UPGRADED_LASER_ENTITY_ID = EntityDataManager.createKey(EntityMimicOctopus.class, DataSerializers.VARINT);
    public MimicState localMimicState = MimicState.OVERLAY;
    public float transProgress = 0F;
    public float prevTransProgress = 0F;
    public float colorShiftProgress = 0F;
    public float prevColorShiftProgress = 0F;
    public float groundProgress = 5F;
    public float prevGroundProgress = 0F;
    public float sitProgress = 0F;
    public float prevSitProgress = 0F;
    private boolean isLandNavigator;
    private int moistureAttackTime = 0;
    private int camoCooldown = 120 + rand.nextInt(1200);
    private int mimicCooldown = 0;
    private int stopMimicCooldown = -1;
    private int fishFeedings;
    private int mimicreamFeedings;
    private int exclaimTime = 0;
    private IBlockState localMimic;
    private EntityLivingBase laserTargetEntity;
    private int guardianLaserTime;

    public EntityMimicOctopus(World worldIn) {
        super(worldIn);
        this.setSize(0.7F, 0.4F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ARMOR);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    public static boolean canMimicOctopusSpawn(World worldIn, BlockPos pos) {
        BlockPos downPos = pos;
        while (downPos.getY() > 1 && worldIn.getBlockState(downPos).getMaterial() == Material.WATER) {
            downPos = downPos.down();
        }
        boolean spawnBlock = AMTagRegistry.blockInTag(AMTagRegistry.MIMIC_OCTOPUS_SPAWNS, worldIn.getBlockState(downPos).getBlock());
        return spawnBlock && downPos.getY() < worldIn.getSeaLevel() + 1;
    }

    private static boolean isPufferfishEntity(Entity entity) {
        String id = EntityList.getEntityString(entity);
        return id != null && id.toLowerCase().contains("pufferfish");
    }

    public static MimicState getStateForItem(ItemStack stack) {
        if (AMTagRegistry.isClownfish(stack) || (stack.getItem() == Items.FISH && stack.getMetadata() == 2)) {
            return null;
        }
        if (AMTagRegistry.itemInTag(AMTagRegistry.MIMIC_OCTOPUS_CREEPER_ITEMS, stack.getItem())) {
            return MimicState.CREEPER;
        }
        if (AMTagRegistry.itemInTag(AMTagRegistry.MIMIC_OCTOPUS_GUARDIAN_ITEMS, stack.getItem())) {
            return MimicState.GUARDIAN;
        }
        if (AMTagRegistry.isPufferfish(stack)
                || AMTagRegistry.itemInTag(AMTagRegistry.MIMIC_OCTOPUS_PUFFERFISH_ITEMS, stack.getItem())
                && stack.getItem() != Items.FISH) {
            return MimicState.PUFFERFISH;
        }
        return null;
    }

    public float getRenderScale() {
        return this.isChild() ? 0.5F : 1.0F;
    }

    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.MIMIC_OCTOPUS_IDLE;
    }

    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MIMIC_OCTOPUS_HURT;
    }

    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MIMIC_OCTOPUS_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.MIMIC_OCTOPUS;
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.mimicOctopusSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficultyIn, @Nullable IEntityLivingData spawnDataIn) {
        this.dataManager.set(PREV_MIMIC_ORDINAL, 0);
        this.setMimickedBlock(null);
        this.setMimicState(MimicState.OVERLAY);
        return super.onInitialSpawn(difficultyIn, spawnDataIn);
    }

    public void readAdditional(NBTTagCompound compound) {
        this.dataManager.set(MIMIC_ORDINAL, compound.getInteger("MimicState"));
        this.setUpgraded(compound.getBoolean("Upgraded"));
        this.setSitting(compound.getBoolean("Sitting"));
        this.setStopChange(compound.getBoolean("StopChange"));
        this.setCommand(compound.getInteger("OctoCommand"));
        this.setMoistness(compound.getInteger("Moistness"));
        this.setFromBucket(compound.getBoolean("FromBucket"));
        if (compound.hasKey("MimickedBlockState", 10)) {
            IBlockState blockstate = net.minecraft.nbt.NBTUtil.readBlockState(compound.getCompoundTag("MimickedBlockState"));
            if (blockstate.getBlock() == Blocks.AIR) {
                blockstate = null;
            }
            this.setMimickedBlock(blockstate);
        }
        this.camoCooldown = compound.getInteger("CamoCooldown");
        this.mimicCooldown = compound.getInteger("MimicCooldown");
        this.stopMimicCooldown = compound.getInteger("StopMimicCooldown");
        this.fishFeedings = compound.getInteger("FishFeedings");
        this.mimicreamFeedings = compound.getInteger("MimicreamFeedings");
    }

    public void writeAdditional(NBTTagCompound compound) {
        compound.setInteger("MimicState", this.getMimicState().ordinal());
        compound.setBoolean("Upgraded", this.isUpgraded());
        compound.setBoolean("Sitting", this.isSitting());
        compound.setInteger("OctoCommand", this.getCommand());
        compound.setInteger("Moistness", this.getMoistness());
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("StopChange", this.isStopChange());
        IBlockState blockstate = this.getMimickedBlock();
        if (blockstate != null) {
            NBTTagCompound blockTag = new NBTTagCompound();
            net.minecraft.nbt.NBTUtil.writeBlockState(blockTag, blockstate);
            compound.setTag("MimickedBlockState", blockTag);
        }
        compound.setInteger("CamoCooldown", this.camoCooldown);
        compound.setInteger("MimicCooldown", this.mimicCooldown);
        compound.setInteger("StopMimicCooldown", this.stopMimicCooldown);
        compound.setInteger("FishFeedings", this.fishFeedings);
        compound.setInteger("MimicreamFeedings", this.mimicreamFeedings);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.writeAdditional(compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.readAdditional(compound);
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.MIMIC_OCTOPUS_BUCKET);
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeAdditional(platTag);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag("MimicOctopusData", platTag);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return super.getJumpUpwardsMotion() * (this.isInWater() ? 1.3F : 1F);
    }

    @Override
    public boolean shouldFollow() {
        return this.getCommand() == 1;
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

    public boolean isPushedByWater() {
        return false;
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new AIAttack());
        this.tasks.addTask(1, new EntityAISit(this));
        this.tasks.addTask(2, new FollowOwner(this, 1.3D, 4.0F, 2.0F, false));
        this.tasks.addTask(3, new AnimalAIFindWater(this));
        this.tasks.addTask(3, new AnimalAILeaveWater(this));
        this.tasks.addTask(4, new EntityAITempt(this, 1.0D, Items.FISH, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                Item item = stack.getItem();
                return item == Items.FISH || item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL;
            }

            @Override
            public void updateTask() {
                EntityMimicOctopus.this.setMimickedBlock(null);
                super.updateTask();
                EntityMimicOctopus.this.camoCooldown = 40;
                EntityMimicOctopus.this.stopMimicCooldown = 40;
            }
        });
        this.tasks.addTask(5, new AIFlee());
        this.tasks.addTask(7, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(8, new AIMimicNearbyMobs());
        this.tasks.addTask(9, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(10, new AISwim());
        this.tasks.addTask(11, new EntityAILookIdle(this));
        this.tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true) {
            @Override
            public boolean shouldExecute() {
                return EntityMimicOctopus.this.isTamed() && super.shouldExecute();
            }
        });
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.isClownfish(stack) || (stack.getItem() == Items.FISH && stack.getMetadata() == 2);
    }

    @Override
    public boolean canMateWith(net.minecraft.entity.passive.EntityAnimal otherAnimal) {
        return otherAnimal != this && otherAnimal.getClass() == this.getClass() && this.isInLove() && otherAnimal.isInLove();
    }

    public boolean isActiveCamo() {
        return this.getMimicState() == MimicState.OVERLAY && this.getMimickedBlock() != null;
    }

    public double getVisibilityMultiplier(@Nullable Entity lookingEntity) {
        return isActiveCamo() ? 0.1D : 1.0D;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        Item item = itemstack.getItem();
        if (isBreedingItem(itemstack)) {
            super.processInteract(player, hand);
            return true;
        }
        MimicState readState = getStateForItem(itemstack);
        if (super.processInteract(player, hand)) {
            return true;
        }
        if (readState != null && this.isTamed()) {
            if (mimicCooldown == 0) {
                this.setMimicState(readState);
                mimicCooldown = 20;
                stopMimicCooldown = isUpgraded() ? 120 : 1200;
                camoCooldown = stopMimicCooldown;
                this.setMimickedBlock(null);
            }
            return true;
        }
        if (isTamed() && item == Items.DYE && itemstack.getMetadata() == EnumDyeColor.BLACK.getDyeDamage()) {
            this.setStopChange(!this.isStopChange());
            if (this.isStopChange()) {
                this.makeEatingParticles(itemstack);
            } else {
                this.world.setEntityState(this, (byte) 6);
                this.mimicEnvironment();
            }
            return true;
        }
        if (!isTamed() && (item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL)) {
            this.consumeItemFromStack(player, itemstack);
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
            fishFeedings++;
            if (this.getMimicState() == MimicState.OVERLAY && this.getMimickedBlock() == null) {
                if (fishFeedings > 5 && getRNG().nextInt(2) == 0 || fishFeedings > 8) {
                    this.setTamedBy(player);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.world.setEntityState(this, (byte) 6);
                }
            }
            return true;
        }
        if (isTamed() && (item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL)) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.consumeItemFromStack(player, itemstack);
                this.playSound(SoundEvents.ENTITY_GENERIC_EAT, this.getSoundVolume(), this.getSoundPitch());
                this.heal(5);
                return true;
            }
            return false;
        }
        if (this.isTamed() && itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            ItemStack itemstack1 = this.getFishBucket();
            if (!this.world.isRemote) {
                            }

            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }

            this.setDead();
            return true;
        }
        if (this.isTamed() && item == Items.SLIME_BALL && this.getMoistness() < 24000) {
            this.setMoistness(48000);
            this.makeEatingParticles(itemstack);
            this.consumeItemFromStack(player, itemstack);
            return true;
        }
        if (this.isTamed() && !this.isUpgraded() && item == AMItemRegistry.MIMICREAM) {
            mimicreamFeedings++;
            if (mimicreamFeedings > 5 || mimicreamFeedings > 2 && rand.nextInt(2) == 0) {
                this.world.setEntityState(this, (byte) 46);
                this.setUpgraded(true);
                this.setMimicState(MimicState.MIMICUBE);
                this.setStopChange(false);
                this.setMimickedBlock(null);
                this.stopMimicCooldown = 40;
            }
            this.makeEatingParticles(itemstack);
            this.consumeItemFromStack(player, itemstack);
            return true;
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
            } else if (!isBreedingItem(itemstack)) {
                this.setCommand(this.getCommand() + 1);
                if (this.getCommand() == 3) {
                    this.setCommand(0);
                }
                player.sendStatusMessage(new TextComponentTranslation("entity.alexsmobs.all.command_" + this.getCommand(), this.getName()), true);
                boolean sit = this.getCommand() == 2;
                if (sit) {
                    this.setSitting(true);
                    return true;
                } else {
                    this.setSitting(false);
                    return true;
                }
            }
        }
        return false;
    }

    protected void consumeItemFromStack(EntityPlayer player, ItemStack stack) {
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
    }

    public int getCommand() {
        return this.dataManager.get(COMMAND).intValue();
    }

    public void setCommand(int command) {
        this.dataManager.set(COMMAND, Integer.valueOf(command));
    }

    private void makeEatingParticles(ItemStack item) {
        for (int i = 0; i < 6 + rand.nextInt(3); i++) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F), this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F, d0, d1, d2, Item.getIdFromItem(item.getItem()), item.getMetadata());
        }
    }

    public void func_233629_a_(EntityLivingBase p_233629_1_, boolean p_233629_2_) {
        p_233629_1_.prevLimbSwingAmount = p_233629_1_.limbSwingAmount;
        double d0 = p_233629_1_.posX - p_233629_1_.prevPosX;
        double d1 = p_233629_1_.posY - p_233629_1_.prevPosY;
        double d2 = p_233629_1_.posZ - p_233629_1_.prevPosZ;
        float f = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * (groundProgress < 2.5F ? 4.0F : 8.0F);
        if (f > 1.0F) {
            f = 1.0F;
        }

        p_233629_1_.limbSwingAmount += (f - p_233629_1_.limbSwingAmount) * 0.4F;
        p_233629_1_.limbSwing += p_233629_1_.limbSwingAmount;
    }

    public boolean canBreatheUnderwater() {
        return true;
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AnimalSwimMoveControllerSink(this, 1.3F, 1);
            this.navigator = new SemiAquaticPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.updateAirSupply();
        if (localMimic != this.getPrevMimickedBlock()) {
            localMimic = this.getPrevMimickedBlock();
            colorShiftProgress = 0.0F;
        }
        if (localMimicState != this.getPrevMimicState()) {
            localMimicState = this.getPrevMimicState();
            transProgress = 0.0F;
        }
        if (this.isInWater() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!this.isInWater() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        BlockPos pos = new BlockPos(this.posX, this.posY + (double) this.getEyeHeight() - 1.0D, this.posZ);
        boolean ground = world.getBlockState(pos).isSideSolid(world, pos, EnumFacing.UP) && this.getMimicState() != MimicState.GUARDIAN || !this.isInWater() || this.isSitting();
        this.prevTransProgress = transProgress;
        this.prevColorShiftProgress = colorShiftProgress;
        this.prevGroundProgress = groundProgress;
        this.prevSitProgress = sitProgress;
        if (this.getPrevMimicState() != this.getMimicState() && transProgress < 5.0F) {
            transProgress += 0.25F;
        }
        if (this.getPrevMimicState() == this.getMimicState() && transProgress > 0F) {
            transProgress -= 0.25F;
        }
        if (getPrevMimickedBlock() != this.getMimickedBlock() && colorShiftProgress < 5.0F) {
            colorShiftProgress += 0.25F;
        }
        if (getPrevMimickedBlock() == this.getMimickedBlock() && colorShiftProgress > 0F) {
            colorShiftProgress -= 0.25F;
        }
        if (ground && groundProgress < 5F) {
            groundProgress += 0.5F;
        }
        if (!ground && groundProgress > 0F) {
            groundProgress -= 0.5F;
        }
        if (isSitting() && sitProgress < 5F) {
            sitProgress += 0.5F;
        }
        if (!isSitting() && sitProgress > 0F) {
            sitProgress -= 0.5F;
        }
        if (this.isInWater()) {
            float f2 = (float) -((float) this.motionY * 3.0D * (180F / (float) Math.PI));
            this.rotationPitch = f2;
        }
        if (camoCooldown > 0) {
            camoCooldown--;
        }
        if (mimicCooldown > 0) {
            mimicCooldown--;
        }
        if (stopMimicCooldown > 0) {
            stopMimicCooldown--;
        }
        if (this.isAIDisabled()) {
            this.setAir(300);
        } else {
            if (this.isInWater() || this.world.isRainingAt(this.getPosition()) || this.getHeldItemMainhand().getItem() == Items.WATER_BUCKET) {
                this.setMoistness(60000);
            } else {
                this.setMoistness(this.getMoistness() - 1);
                if (this.getMoistness() <= 0 && moistureAttackTime-- <= 0) {
                    this.setSitting(false);
                    this.attackEntityFrom(DamageSource.GENERIC, rand.nextInt(2) == 0 ? 1.0F : 0.0F);
                    moistureAttackTime = 20;
                }
            }
        }
        if (camoCooldown <= 0 && rand.nextInt(300) == 0) {
            mimicEnvironment();
            camoCooldown = this.getRNG().nextInt(2200) + 200;
        }
        if ((this.getMimicState() != MimicState.OVERLAY || this.getMimickedBlock() != null) && stopMimicCooldown == 0 && !this.isStopChange()) {
            this.setMimicState(MimicState.OVERLAY);
            this.setMimickedBlock(null);
            stopMimicCooldown = -1;
        }
        if (world.isRemote && exclaimTime > 0) {
            exclaimTime--;
            if (exclaimTime == 0) {
                Entity e = world.getEntityByID(this.dataManager.get(LAST_SCARED_MOB_ID));
                if (e != null && transProgress >= 5.0F) {
                    double d2 = this.rand.nextGaussian() * 0.1D;
                    double d0 = this.rand.nextGaussian() * 0.1D;
                    double d1 = this.rand.nextGaussian() * 0.1D;
                    AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.SHOCKED, e.posX, e.posY + e.getEyeHeight() + e.height * 0.15F + (double) (this.rand.nextFloat() * e.height * 0.15F), e.posZ, d0, d1, d2);
                }
            }
        }

        if (this.hasGuardianLaser()) {
            if (this.guardianLaserTime < 30) {
                ++this.guardianLaserTime;
            }
            EntityLivingBase EntityLivingBase = this.getGuardianLaser();
            if (EntityLivingBase != null && this.isInWater()) {
                this.getLookHelper().setLookPositionWithEntity(EntityLivingBase, 90.0F, 90.0F);
                double d5 = this.getLaserAttackAnimationScale(0.0F);
                double d0 = EntityLivingBase.posX - this.posX;
                double d1 = this.getPosYHeight(EntityLivingBase, 0.5D) - (this.posY + (double) this.getEyeHeight());
                double d2 = EntityLivingBase.posZ - this.posZ;
                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                d0 = d0 / d3;
                d1 = d1 / d3;
                d2 = d2 / d3;
                double d4 = this.rand.nextDouble();
                while (d4 < d3) {
                    d4 += 1.8D - d5 + this.rand.nextDouble() * (1.7D - d5);
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX + d0 * d4, this.posY + (double) this.getEyeHeight() + d1 * d4, this.posZ + d2 * d4, 0.0D, 0.0D, 0.0D);
                }
                if (guardianLaserTime == 30) {
                    EntityLivingBase.attackEntityFrom(DamageSource.causeMobDamage(this), 5);
                    guardianLaserTime = 0;
                    this.dataManager.set(UPGRADED_LASER_ENTITY_ID, -1);
                }
            }
        }
        if (!world.isRemote && ticksExisted % 40 == 0) {
            this.heal(2);
        }
    /*if(!world.isRemote){
            if(ticksExisted % 80 == 0){
                mimicEnvironment();
            }else if(ticksExisted % 40 == 0){
                this.setMimicState(MimicState.OVERLAY);
                this.setMimickedBlock(null);
            }
        }*/
    }

    public float getLaserAttackAnimationScale(float p_175477_1_) {
        return ((float) this.guardianLaserTime + p_175477_1_) / 30F;
    }

    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 68) {
            if (exclaimTime == 0) {
                exclaimTime = 20;
            }
        } else if (id == 69) {
            this.creeperExplode();
        } else {
            super.handleStatusUpdate(id);
        }
    }

    public void mimicEnvironment() {
        if (!this.isStopChange()) {
            BlockPos down = getPositionDown();
            if (!world.isAirBlock(down)) {
                this.setMimicState(MimicState.OVERLAY);
                this.setMimickedBlock(world.getBlockState(down));
            }
            stopMimicCooldown = this.getRNG().nextInt(2200);
        }
    }

    private double getPosYHeight(EntityLivingBase entity, double heightScale) {
        return entity.posY + (double) entity.height * heightScale;
    }

    private void updateAirSupply() {
        if (this.isEntityAlive() && !this.isInWater()) {
            int air = this.getAir() - 1;
            this.setAir(air);
            if (air == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAir(1200);
        }
    }

    public int getMoistness() {
        return this.dataManager.get(MOISTNESS);
    }

    public void setMoistness(int p_211137_1_) {
        this.dataManager.set(MOISTNESS, p_211137_1_);
    }

    private BlockPos getPositionDown() {
        BlockPos pos = new BlockPos(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        while (pos.getY() > 1 && (world.isAirBlock(pos) || world.getBlockState(pos).getMaterial() == Material.WATER)) {
            pos = pos.down();
        }
        return pos;
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
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    public boolean isSitting() {
        return this.dataManager.get(SITTING).booleanValue();
    }

    public void setSitting(boolean sit) {
        this.dataManager.set(SITTING, Boolean.valueOf(sit));
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET).booleanValue();
    }

    public void setFromBucket(boolean sit) {
        this.dataManager.set(FROM_BUCKET, Boolean.valueOf(sit));
    }

    public boolean isUpgraded() {
        return this.dataManager.get(UPGRADED).booleanValue();
    }

    public void setUpgraded(boolean upgraded) {
        this.dataManager.set(UPGRADED, Boolean.valueOf(upgraded));
    }

    public boolean isStopChange() {
        return this.dataManager.get(STOP_CHANGE).booleanValue();
    }

    public void setStopChange(boolean sit) {
        this.dataManager.set(STOP_CHANGE, Boolean.valueOf(sit));
    }

    public boolean hasGuardianLaser() {
        return this.dataManager.get(UPGRADED_LASER_ENTITY_ID) != -1 && this.isUpgraded() && this.isInWater();
    }

    @Nullable
    public EntityLivingBase getGuardianLaser() {
        if (!this.hasGuardianLaser()) {
            return null;
        } else if (this.world.isRemote) {
            if (this.laserTargetEntity != null) {
                return this.laserTargetEntity;
            } else {
                Entity lvt_1_1_ = this.world.getEntityByID(this.dataManager.get(UPGRADED_LASER_ENTITY_ID));
                if (lvt_1_1_ instanceof EntityLivingBase) {
                    this.laserTargetEntity = (EntityLivingBase) lvt_1_1_;
                    return this.laserTargetEntity;
                } else {
                    return null;
                }
            }
        } else {
            return this.getAttackTarget();
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntityMimicOctopus child = new EntityMimicOctopus(this.world);
        UUID ownerId = this.getOwnerId();
        if (ownerId != null) {
            child.setOwnerId(ownerId);
            child.setTamed(true);
        }
        return child;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isTamed() && !this.isFromBucket() && super.canDespawn();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MIMIC_ORDINAL, 0);
        this.dataManager.register(PREV_MIMIC_ORDINAL, -1);
        this.dataManager.register(MOISTNESS, 60000);
        this.dataManager.register(MIMICKED_BLOCK, -1);
        this.dataManager.register(PREV_MIMICKED_BLOCK, -1);
        this.dataManager.register(SITTING, false);
        this.dataManager.register(COMMAND, 0);
        this.dataManager.register(LAST_SCARED_MOB_ID, -1);
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(UPGRADED, false);
        this.dataManager.register(STOP_CHANGE, false);
        this.dataManager.register(UPGRADED_LASER_ENTITY_ID, -1);
    }

    public MimicState getMimicState() {
        return MimicState.values()[MathHelper.clamp(dataManager.get(MIMIC_ORDINAL), 0, 4)];
    }

    public void setMimicState(MimicState state) {
        if (getMimicState() != state) {
            this.dataManager.set(PREV_MIMIC_ORDINAL, this.dataManager.get(MIMIC_ORDINAL));
        }
        this.dataManager.set(MIMIC_ORDINAL, state.ordinal());
    }

    public MimicState getPrevMimicState() {
        if (dataManager.get(PREV_MIMIC_ORDINAL) == -1) {
            return null;
        }
        return MimicState.values()[MathHelper.clamp(dataManager.get(PREV_MIMIC_ORDINAL), 0, 4)];
    }

    @Nullable
    public IBlockState getMimickedBlock() {
        int id = this.dataManager.get(MIMICKED_BLOCK);
        return id == -1 ? null : Block.getStateById(id);
    }

    public void setMimickedBlock(@Nullable IBlockState state) {
        if (getMimickedBlock() != state) {
            IBlockState prev = getMimickedBlock();
            this.dataManager.set(PREV_MIMICKED_BLOCK, prev == null ? -1 : Block.getStateId(prev));
        }
        this.dataManager.set(MIMICKED_BLOCK, state == null ? -1 : Block.getStateId(state));
    }

    @Nullable
    public IBlockState getPrevMimickedBlock() {
        int id = this.dataManager.get(PREV_MIMICKED_BLOCK);
        return id == -1 ? null : Block.getStateById(id);
    }

    @Override
    public boolean shouldEnterWater() {
        return !this.isSitting() && (this.getAttackTarget() == null || this.getAttackTarget().isInWater());
    }

    @Override
    public boolean shouldLeaveWater() {
        return this.getAttackTarget() != null && !this.getAttackTarget().isInWater();
    }

    @Override
    public boolean shouldStopMoving() {
        return isSitting();
    }

    @Override
    public int getWaterSearchRange() {
        return 16;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    public Vec3d getBlockInViewAway(Vec3d fleePos, float radiusAdd) {
        float radius = 0.75F * (0.7F * 6) * -3 - this.getRNG().nextInt(24) - radiusAdd;
        float neg = this.getRNG().nextBoolean() ? 1 : -1;
        float renderYawOffset = this.renderYawOffset;
        float angle = (0.01745329251F * renderYawOffset) + 3.15F + (this.getRNG().nextFloat() * neg);
        double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
        double extraZ = radius * MathHelper.cos(angle);
        BlockPos radialPos = new BlockPos(fleePos.x + extraX, 0, fleePos.z + extraZ);
        BlockPos ground = getOctopusGround(radialPos);

        return ground != null ? new Vec3d((double) ground.getX() + 0.5D, (double) ground.getY(), (double) ground.getZ() + 0.5D) : null;
    }

    private BlockPos getOctopusGround(BlockPos in) {
        BlockPos position = new BlockPos(in.getX(), (int) this.posY, in.getZ());
        while (position.getY() > 2 && world.getBlockState(position).getMaterial() == Material.WATER) {
            position = position.down();
        }
        return position;
    }

    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if (UPGRADED_LASER_ENTITY_ID.equals(key)) {
            this.guardianLaserTime = 0;
            this.laserTargetEntity = null;
        }

    }

    private void creeperExplode() {
        if (!this.world.isRemote) {
            this.world.newExplosion(this, this.posX, this.posY, this.posZ, 1.0F + rand.nextFloat(), false, false);
        }
    }

    public enum MimicState {
        OVERLAY,
        CREEPER,
        GUARDIAN,
        PUFFERFISH,
        MIMICUBE
    }

    private class AISwim extends SemiAquaticAIRandomSwimming {

        public AISwim() {
            super(EntityMimicOctopus.this, 1, 35);
        }

        @Nullable
        @Override
        protected Vec3d findSurfaceTarget(EntityCreature creature, int i, int i1) {
            if (creature.getRNG().nextInt(5) == 0) {
                return super.findSurfaceTarget(creature, i, i1);
            } else {
                BlockPos downPos = creature.getPosition();
                while (creature.world.getBlockState(downPos).getMaterial() == Material.WATER || creature.world.getBlockState(downPos).getMaterial() == Material.LAVA) {
                    downPos = downPos.down();
                }
                if (world.getBlockState(downPos).isFullBlock() && world.getBlockState(downPos).getBlock() != Blocks.MAGMA) {
                    return new Vec3d((double) downPos.getX() + 0.5D, (double) downPos.getY(), (double) downPos.getZ() + 0.5D);
                }
            }
            return null;
        }

    }

    private class AIFlee extends EntityAIBase {
        protected final EntitySorter theNearestAttackableTargetSorter;
        protected final Predicate<? super Entity> targetEntitySelector;
        protected int executionChance = 8;
        protected boolean mustUpdate;
        private Entity targetEntity;
        private Vec3d flightTarget = null;
        private int cooldown = 0;
        private final Predicate<EntityLivingBase> fearPredicate = AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.MIMIC_OCTOPUS_FEARS);

        AIFlee() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new EntitySorter(EntityMimicOctopus.this);
            this.targetEntitySelector = new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity e) {
                    if (e == null || !e.isEntityAlive()) {
                        return false;
                    }
                    if (e instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) e;
                        return !player.capabilities.isCreativeMode && !(player instanceof EntityPlayerMP && ((EntityPlayerMP) player).isSpectator());
                    }
                    return e instanceof EntityLivingBase && fearPredicate.apply((EntityLivingBase) e);
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntityMimicOctopus.this.isBeingRidden() || EntityMimicOctopus.this.isBeingRidden() || EntityMimicOctopus.this.isTamed()) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityMimicOctopus.this.world.getTotalWorldTime() % 10;
                if (EntityMimicOctopus.this.ticksExisted >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityMimicOctopus.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntityMimicOctopus.this.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            if (list.isEmpty()) {
                return false;
            } else {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
                this.targetEntity = list.get(0);
                this.mustUpdate = false;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetEntity != null && !EntityMimicOctopus.this.isTamed() && EntityMimicOctopus.this.getDistance(targetEntity) < 20;
        }

        public void resetTask() {
            flightTarget = null;
            this.targetEntity = null;
            EntityMimicOctopus.this.setMimicState(MimicState.OVERLAY);
            EntityMimicOctopus.this.setMimickedBlock(null);
        }

        @Override
        public void updateTask() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (!EntityMimicOctopus.this.isActiveCamo()) {
                EntityMimicOctopus.this.mimicEnvironment();
            }
            if (flightTarget != null) {
                EntityMimicOctopus.this.getNavigator().tryMoveToXYZ(flightTarget.x, flightTarget.y, flightTarget.z, 1.2D);
                if (cooldown == 0 && EntityMimicOctopus.this.isTargetBlocked(flightTarget)) {
                    cooldown = 30;
                    flightTarget = null;
                }
            }

            if (targetEntity != null) {
                if (flightTarget == null || EntityMimicOctopus.this.getDistanceSq(flightTarget.x, flightTarget.y, flightTarget.z) < 6.0D) {
                    Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityMimicOctopus.this, 16, 7, new Vec3d(targetEntity.posX, targetEntity.posY, targetEntity.posZ));
                    if (vec != null) {
                        flightTarget = vec;
                    }
                }
                if (EntityMimicOctopus.this.getDistance(targetEntity) > 20.0F) {
                    this.resetTask();
                }
            }
        }

        protected double getTargetDistance() {
            return 10;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            return EntityMimicOctopus.this.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
        }
    }

    public class EntitySorter implements Comparator<Entity> {
        private final Entity theEntity;

        public EntitySorter(Entity theEntityIn) {
            this.theEntity = theEntityIn;
        }

        public int compare(Entity p_compare_1_, Entity p_compare_2_) {
            double d0 = this.theEntity.getDistanceSq(p_compare_1_);
            double d1 = this.theEntity.getDistanceSq(p_compare_2_);
            return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
        }
    }

    public class FollowOwner extends EntityAIBase {
        private final EntityMimicOctopus tameable;
        private final net.minecraft.world.IBlockAccess world;
        private final double followSpeed;
        private final float maxDist;
        private final float minDist;
        private final boolean teleportToLeaves;
        private EntityLivingBase owner;
        private int timeToRecalcPath;
        private float oldWaterCost;

        public FollowOwner(EntityMimicOctopus p_i225711_1_, double p_i225711_2_, float p_i225711_4_, float p_i225711_5_, boolean p_i225711_6_) {
            this.tameable = p_i225711_1_;
            this.world = p_i225711_1_.world;
            this.followSpeed = p_i225711_2_;
            this.minDist = p_i225711_4_;
            this.maxDist = p_i225711_5_;
            this.teleportToLeaves = p_i225711_6_;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            EntityLivingBase lvt_1_1_ = this.tameable.getOwner();
            if (lvt_1_1_ == null) {
                return false;
            } else if (lvt_1_1_ instanceof EntityPlayer && (((EntityPlayer) lvt_1_1_).capabilities.isCreativeMode || lvt_1_1_ instanceof EntityPlayerMP && ((EntityPlayerMP) lvt_1_1_).isSpectator())) {
                return false;
            } else if (this.tameable.isSitting() || tameable.getCommand() != 1) {
                return false;
            } else if (this.tameable.getDistanceSq(lvt_1_1_) < (double) (this.minDist * this.minDist)) {
                return false;
            } else if (this.tameable.getAttackTarget() != null && this.tameable.getAttackTarget().isEntityAlive()) {
                return false;
            } else {
                this.owner = lvt_1_1_;
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

        public void startExecuting() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.tameable.getPathPriority(PathNodeType.WATER);
            this.tameable.setPathPriority(PathNodeType.WATER, 0.0F);
        }

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
            BlockPos lvt_1_1_ = this.owner.getPosition();

            for (int lvt_2_1_ = 0; lvt_2_1_ < 10; ++lvt_2_1_) {
                int lvt_3_1_ = this.getRandomNumber(-3, 3);
                int lvt_4_1_ = this.getRandomNumber(-1, 1);
                int lvt_5_1_ = this.getRandomNumber(-3, 3);
                boolean lvt_6_1_ = this.tryToTeleportToLocation(lvt_1_1_.getX() + lvt_3_1_, lvt_1_1_.getY() + lvt_4_1_, lvt_1_1_.getZ() + lvt_5_1_);
                if (lvt_6_1_) {
                    return;
                }
            }

        }

        private boolean tryToTeleportToLocation(int p_226328_1_, int p_226328_2_, int p_226328_3_) {
            if (Math.abs((double) p_226328_1_ - this.owner.posX) < 2.0D && Math.abs((double) p_226328_3_ - this.owner.posZ) < 2.0D) {
                return false;
            } else if (!this.isTeleportFriendlyBlock(new BlockPos(p_226328_1_, p_226328_2_, p_226328_3_))) {
                return false;
            } else {
                this.tameable.setPosition((double) p_226328_1_ + 0.5D, (double) p_226328_2_, (double) p_226328_3_ + 0.5D);
                this.tameable.getNavigator().clearPath();
                return true;
            }
        }

        private boolean isTeleportFriendlyBlock(BlockPos p_226329_1_) {
            PathNodeType lvt_2_1_ = new WalkNodeProcessor().getPathNodeType(this.world, p_226329_1_.getX(), p_226329_1_.getY(), p_226329_1_.getZ());
            Material mat = this.world.getBlockState(p_226329_1_).getMaterial();
            if (mat == Material.WATER || mat != Material.WATER && this.world.getBlockState(p_226329_1_.down()).getMaterial() == Material.WATER) {
                return true;
            }
            if (lvt_2_1_ != PathNodeType.WALKABLE || tameable.getMoistness() < 2000) {
                return false;
            } else {
                IBlockState lvt_3_1_ = this.world.getBlockState(p_226329_1_.down());
                if (!this.teleportToLeaves && lvt_3_1_.getBlock() instanceof BlockLeaves) {
                    return false;
                } else {
                    BlockPos lvt_4_1_ = p_226329_1_.subtract(this.tameable.getPosition());
                    return this.tameable.world.getCollisionBoxes(this.tameable, this.tameable.getEntityBoundingBox().offset(lvt_4_1_.getX(), lvt_4_1_.getY(), lvt_4_1_.getZ())).isEmpty();
                }
            }
        }

        private int getRandomNumber(int p_226327_1_, int p_226327_2_) {
            return this.tameable.getRNG().nextInt(p_226327_2_ - p_226327_1_ + 1) + p_226327_1_;
        }
    }

    private class AIMimicNearbyMobs extends EntityAIBase {
        protected final EntitySorter theNearestAttackableTargetSorter;
        protected final Predicate<? super Entity> targetEntitySelector;
        protected int executionChance = 30;
        protected boolean mustUpdate;
        private Entity targetEntity;
        private int cooldown = 0;

        AIMimicNearbyMobs() {
            this.setMutexBits(1);
            this.theNearestAttackableTargetSorter = new EntitySorter(EntityMimicOctopus.this);
            this.targetEntitySelector = new Predicate<Entity>() {
                @Override
                public boolean apply(@Nullable Entity e) {
                    return e != null && e.isEntityAlive() && (e instanceof EntityCreeper || e instanceof EntityGuardian || isPufferfishEntity(e));
                }
            };
        }

        @Override
        public boolean shouldExecute() {
            if (EntityMimicOctopus.this.isBeingRidden() || EntityMimicOctopus.this.isBeingRidden() || EntityMimicOctopus.this.getMimicState() != MimicState.OVERLAY || mimicCooldown > 0) {
                return false;
            }
            if (!this.mustUpdate) {
                long worldTime = EntityMimicOctopus.this.world.getTotalWorldTime() % 10;
                if (EntityMimicOctopus.this.ticksExisted >= 100 && worldTime != 0) {
                    return false;
                }
                if (EntityMimicOctopus.this.getRNG().nextInt(this.executionChance) != 0 && worldTime != 0) {
                    return false;
                }
            }
            List<Entity> list = EntityMimicOctopus.this.world.getEntitiesWithinAABB(Entity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            if (list.isEmpty()) {
                return false;
            } else {
                Collections.sort(list, this.theNearestAttackableTargetSorter);
                this.targetEntity = list.get(0);
                this.mustUpdate = false;
                return true;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            return targetEntity != null && EntityMimicOctopus.this.getDistance(targetEntity) < 10 && EntityMimicOctopus.this.getMimicState() == MimicState.OVERLAY;
        }

        @Override
        public void resetTask() {
            EntityMimicOctopus.this.getNavigator().clearPath();
            this.targetEntity = null;
        }

        @Override
        public void updateTask() {
            if (cooldown > 0) {
                cooldown--;
            }
            if (targetEntity != null) {
                EntityMimicOctopus.this.getNavigator().tryMoveToEntityLiving((EntityLivingBase) targetEntity, 1.2D);
                if (EntityMimicOctopus.this.getDistance(targetEntity) > 20.0F) {
                    this.resetTask();
                    EntityMimicOctopus.this.setMimicState(MimicState.OVERLAY);
                    EntityMimicOctopus.this.setMimickedBlock(null);
                } else if (EntityMimicOctopus.this.getDistance(targetEntity) < 5.0F && EntityMimicOctopus.this.canEntityBeSeen(targetEntity)) {
                    int i = 1200;
                    EntityMimicOctopus.this.stopMimicCooldown = i;
                    EntityMimicOctopus.this.camoCooldown = i + 40;
                    EntityMimicOctopus.this.mimicCooldown = 40;
                    if (targetEntity instanceof EntityCreeper) {
                        EntityMimicOctopus.this.setMimicState(MimicState.CREEPER);
                    } else if (targetEntity instanceof EntityGuardian) {
                        EntityMimicOctopus.this.setMimicState(MimicState.GUARDIAN);
                    } else if (isPufferfishEntity(targetEntity)) {
                        EntityMimicOctopus.this.setMimicState(MimicState.PUFFERFISH);
                    } else {
                        EntityMimicOctopus.this.setMimicState(MimicState.OVERLAY);
                        EntityMimicOctopus.this.setMimickedBlock(null);
                    }
                    resetTask();
                }

            }
        }

        protected double getTargetDistance() {
            return 10;
        }

        protected AxisAlignedBB getTargetableArea(double targetDistance) {
            return EntityMimicOctopus.this.getEntityBoundingBox().grow(targetDistance, targetDistance, targetDistance);
        }
    }

    private class AIAttack extends EntityAIBase {
        private int executionCooldown = 0;
        private int scareMobTime = 0;
        private Vec3d fleePosition = null;

        public AIAttack() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (executionCooldown > 0) {
                EntityMimicOctopus.this.dataManager.set(UPGRADED_LASER_ENTITY_ID, -1);
                executionCooldown--;
            }
            if (EntityMimicOctopus.this.isStopChange() && EntityMimicOctopus.this.getMimicState() == MimicState.OVERLAY) {
                return false;
            }
            return executionCooldown == 0 && EntityMimicOctopus.this.isTamed() && EntityMimicOctopus.this.getAttackTarget() != null && EntityMimicOctopus.this.getAttackTarget().isEntityAlive();
        }

        @Override
        public void resetTask() {
            fleePosition = null;
            scareMobTime = 0;
            executionCooldown = 100 + rand.nextInt(200);
            if (EntityMimicOctopus.this.isUpgraded()) {
                executionCooldown = 30;
            } else {
                EntityMimicOctopus.this.setRevengeTarget(null);
                EntityMimicOctopus.this.setAttackTarget(null);
            }
            if (EntityMimicOctopus.this.stopMimicCooldown <= 0) {
                EntityMimicOctopus.this.mimicEnvironment();
            }
            EntityMimicOctopus.this.dataManager.set(UPGRADED_LASER_ENTITY_ID, -1);
        }

        public Vec3d generateFleePosition(EntityLivingBase fleer) {
            for (int i = 0; i < 15; i++) {
                BlockPos pos = fleer.getPosition().add(rand.nextInt(32) - 16, rand.nextInt(16), rand.nextInt(32) - 16);
                while (fleer.world.isAirBlock(pos) && pos.getY() > 1) {
                    pos = pos.down();
                }
                if (fleer instanceof EntityCreature) {
                    if (((EntityCreature) fleer).getBlockPathWeight(pos) >= 0.0F) {
                        return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D);
                    }
                } else {
                    return new Vec3d((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D);
                }
            }
            return null;
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = EntityMimicOctopus.this.getAttackTarget();
            if (target != null) {
                if (scareMobTime > 0) {
                    if (fleePosition == null || target.getDistanceSq(fleePosition.x, fleePosition.y, fleePosition.z) < (double) (target.width * target.width * 2.0F)) {
                        fleePosition = generateFleePosition(target);
                    }
                    if (target instanceof EntityCreature) {
                        if (fleePosition != null) {
                            ((EntityCreature) target).getNavigator().tryMoveToXYZ(fleePosition.x, fleePosition.y, fleePosition.z, 1.5D);
                            ((EntityCreature) target).getMoveHelper().setMoveTo(fleePosition.x, fleePosition.y, fleePosition.z, 1.5D);
                            ((EntityCreature) target).setAttackTarget(null);
                        }
                    }
                    camoCooldown = Math.max(camoCooldown, 20);
                    stopMimicCooldown = Math.max(stopMimicCooldown, 20);
                    scareMobTime--;
                    if (scareMobTime == 0) {
                        resetTask();
                        return;
                    }
                }
                double dist = EntityMimicOctopus.this.getDistance(target);
                boolean move = true;
                if (dist < 7F && EntityMimicOctopus.this.canEntityBeSeen(target) && EntityMimicOctopus.this.getMimicState() == MimicState.GUARDIAN && EntityMimicOctopus.this.isUpgraded()) {
                    EntityMimicOctopus.this.dataManager.set(UPGRADED_LASER_ENTITY_ID, target.getEntityId());
                    move = false;
                }
                if (dist < 3) {
                    EntityMimicOctopus.this.dataManager.set(LAST_SCARED_MOB_ID, target.getEntityId());
                    if (move) {
                        move = EntityMimicOctopus.this.isUpgraded() && dist > 2;
                    }
                    EntityMimicOctopus.this.getNavigator().clearPath();
                    if (!EntityMimicOctopus.this.isStopChange()) {
                        EntityMimicOctopus.this.setMimickedBlock(null);
                        MimicState prev = EntityMimicOctopus.this.getMimicState();
                        if (EntityMimicOctopus.this.isInWater()) {
                            if (prev != MimicState.GUARDIAN && prev != MimicState.PUFFERFISH) {
                                if (rand.nextBoolean()) {
                                    EntityMimicOctopus.this.setMimicState(MimicState.GUARDIAN);
                                } else {
                                    EntityMimicOctopus.this.setMimicState(MimicState.PUFFERFISH);
                                }
                            }
                        } else {
                            EntityMimicOctopus.this.setMimicState(MimicState.CREEPER);
                        }
                    }
                    if (EntityMimicOctopus.this.getMimicState() != MimicState.OVERLAY) {
                        EntityMimicOctopus.this.mimicCooldown = 40;
                        EntityMimicOctopus.this.stopMimicCooldown = Math.max(EntityMimicOctopus.this.stopMimicCooldown, 60);
                    }
                    if (EntityMimicOctopus.this.isUpgraded() && EntityMimicOctopus.this.transProgress >= 5.0F) {
                        if (EntityMimicOctopus.this.getMimicState() == MimicState.PUFFERFISH) {
                            if (EntityMimicOctopus.this.getEntityBoundingBox().grow(2.0D, 1.3D, 2.0D).intersects(target.getEntityBoundingBox())) {
                                target.attackEntityFrom(DamageSource.causeMobDamage(EntityMimicOctopus.this), 4.0F);
                                target.addPotionEffect(new PotionEffect(MobEffects.POISON, 400, 2));
                            }
                        }
                        if (EntityMimicOctopus.this.getMimicState() == MimicState.GUARDIAN) {
                            if (EntityMimicOctopus.this.getEntityBoundingBox().grow(1.0D, 1.0D, 1.0D).intersects(target.getEntityBoundingBox())) {
                                target.attackEntityFrom(DamageSource.causeMobDamage(EntityMimicOctopus.this), 1.0F);
                            }
                            EntityMimicOctopus.this.dataManager.set(UPGRADED_LASER_ENTITY_ID, target.getEntityId());
                        }
                        if (EntityMimicOctopus.this.getMimicState() == MimicState.CREEPER) {
                            EntityMimicOctopus.this.creeperExplode();
                            EntityMimicOctopus.this.world.setEntityState(EntityMimicOctopus.this, (byte) 69);
                            executionCooldown = 300;
                        }
                    }
                    if (scareMobTime == 0) {
                        EntityMimicOctopus.this.world.setEntityState(EntityMimicOctopus.this, (byte) 68);
                        scareMobTime = 60 + rand.nextInt(60);
                    }
                }
                if (move) {
                    EntityMimicOctopus.this.getLookHelper().setLookPositionWithEntity(target, 30, 30);
                    EntityMimicOctopus.this.getNavigator().tryMoveToEntityLiving(target, 1.2D);
                }
            }
        }
    }
}
