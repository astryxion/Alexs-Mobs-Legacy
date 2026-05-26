package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import com.google.common.collect.ImmutableSet;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityPlatypus extends EntityAnimal implements ISemiAquatic, ITargetsDroppedItems {

    private static final DataParameter<Boolean> SENSING = EntityDataManager.createKey(EntityPlatypus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SENSING_VISUAL = EntityDataManager.createKey(EntityPlatypus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DIGGING = EntityDataManager.createKey(EntityPlatypus.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> FEDORA = EntityDataManager.createKey(EntityPlatypus.class, DataSerializers.BOOLEAN);
    public float prevInWaterProgress;
    public float inWaterProgress;
    public float prevDigProgress;
    public float digProgress;
    public boolean superCharged = false;
    private boolean isLandNavigator;
    private int swimTimer = -1000;

    public EntityPlatypus(World world) {
        super(world);
        this.setSize(0.8F, 0.5F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(false);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.platypusSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == AMItemRegistry.LOBSTER_TAIL || item == AMItemRegistry.COOKED_LOBSTER_TAIL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.PLATYPUS_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.PLATYPUS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.PLATYPUS_HURT;
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.PLATYPUS_BUCKET);
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeAdditional(platTag);
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setTag("PlatypusData", platTag);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getName());
        }
        return stack;
    }

    protected void setBucketData(ItemStack bucket) {
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeAdditional(platTag);
        if (!bucket.hasTagCompound()) {
            bucket.setTagCompound(new NBTTagCompound());
        }
        bucket.getTagCompound().setTag("PlatypusData", platTag);
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getName());
        }
    }

    /** Bucket NBT hook used by {@link com.github.alexthe666.alexsmobs.item.ItemModFishBucket}. */
    public void readAdditional(NBTTagCompound compound) {
        this.setFedora(compound.getBoolean("Fedora"));
        this.setSensing(compound.getBoolean("Sensing"));
    }

    /** Bucket NBT hook used by {@link com.github.alexthe666.alexsmobs.item.ItemModFishBucket}. */
    public void writeAdditional(NBTTagCompound compound) {
        compound.setBoolean("Fedora", this.hasFedora());
        compound.setBoolean("Sensing", this.isSensing());
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean redstone = itemstack.getItem() == Items.REDSTONE || itemstack.getItem() == Item.getItemFromBlock(Blocks.REDSTONE_BLOCK);
        if (itemstack.getItem() == AMItemRegistry.FEDORA && !this.hasFedora()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setFedora(true);
            return true;
        }
        if (redstone && !this.isSensing()) {
            superCharged = itemstack.getItem() == Item.getItemFromBlock(Blocks.REDSTONE_BLOCK);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setSensing(true);
            return true;
        }
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!this.world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    itemstack.shrink(1);
                }
                ItemStack bucket = this.getFishBucket();
                this.setBucketData(bucket);
                                ItemStack handStack = player.getHeldItem(hand);
                if (handStack.isEmpty()) {
                    player.setHeldItem(hand, bucket);
                } else if (!player.inventory.addItemStackToInventory(bucket)) {
                    player.dropItem(bucket, false);
                }
                this.setDead();
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new BreatheAirGoal(this));
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(1, new AnimalAILeaveWater(this));
        this.tasks.addTask(2, new EntityAIMate(this, 0.8D));
        this.tasks.addTask(3, new EntityAIPanic(this, 1.1D));
        this.tasks.addTask(3, new PlatypusRedstoneTempt(this, 1.0D));
        this.tasks.addTask(5, new PlatypusFoodTempt(this, 1.1D));
        this.tasks.addTask(5, new PlatypusAIDigForItems(this));
        this.tasks.addTask(6, new SemiAquaticAIRandomSwimming(this, 1.0D, 30));
        this.tasks.addTask(7, new AnimalAIWanderRanged(this, 60, 1.0D, 14, 7));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.targetTasks.addTask(1, new PlatypusTargetItems(this));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getImmediateSource() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) source.getImmediateSource();
            entity.addPotionEffect(new PotionEffect(MobEffects.POISON, 100));
        }
        return prev;
    }

    public boolean isPerry() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().contains("perry");
    }

    public int getMaxAir() {
        return 4800;
    }

    protected int determineNextAir(int currentAir) {
        return this.getMaxAir();
    }

    public void spawnGroundEffects() {
        float radius = 0.3F;
        for (int i1 = 0; i1 < 3; i1++) {
            double motionX = getRNG().nextGaussian() * 0.07D;
            double motionY = getRNG().nextGaussian() * 0.07D;
            double motionZ = getRNG().nextGaussian() * 0.07D;
            float angle = (0.01745329251F * this.renderYawOffset) + i1;
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraY = 0.8F;
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos ground = this.getPositionUnderneath();
            IBlockState blockState = this.world.getBlockState(ground);
            if (blockState.getMaterial() != Material.AIR && blockState.getMaterial() != Material.WATER) {
                if (world.isRemote) {
                    world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + extraX, ground.getY() + extraY, this.posZ + extraZ, motionX, motionY, motionZ, Block.getStateId(blockState));
                }
            }
        }
    }

    protected BlockPos getPositionUnderneath() {
        return new BlockPos(this.posX, this.posY - 1.0D, this.posZ);
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        this.setAir(this.getMaxAir());
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DIGGING, Boolean.FALSE);
        this.dataManager.register(SENSING, Boolean.FALSE);
        this.dataManager.register(SENSING_VISUAL, Boolean.FALSE);
        this.dataManager.register(FEDORA, Boolean.FALSE);
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int looting) {
        super.dropEquipment(wasRecentlyHit, looting);
        if (this.hasFedora()) {
            this.entityDropItem(new ItemStack(AMItemRegistry.FEDORA), 0.0F);
        }
    }

    public boolean isSensing() {
        return this.dataManager.get(SENSING);
    }

    public void setSensing(boolean sensing) {
        this.dataManager.set(SENSING, sensing);
    }

    public boolean isSensingVisual() {
        return this.dataManager.get(SENSING_VISUAL);
    }

    public void setSensingVisual(boolean sensing) {
        this.dataManager.set(SENSING_VISUAL, sensing);
    }

    public boolean hasFedora() {
        return this.dataManager.get(FEDORA);
    }

    public void setFedora(boolean fedora) {
        this.dataManager.set(FEDORA, fedora);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Fedora", this.hasFedora());
        compound.setBoolean("Sensing", this.isSensing());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFedora(compound.getBoolean("Fedora"));
        this.setSensing(compound.getBoolean("Sensing"));
    }

    public void onUpdate() {
        int i = this.getAir();
        super.onUpdate();
        this.updateAir(i);
    }

    protected void updateAir(int air) {
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        prevInWaterProgress = inWaterProgress;
        prevDigProgress = digProgress;
        boolean dig = isDigging() && isInWater();
        if (dig && digProgress < 5F) {
            digProgress++;
        }
        if (!dig && digProgress > 0F) {
            digProgress--;
        }
        if (this.isInWater() && inWaterProgress < 5F) {
            inWaterProgress++;
        }
        if (!this.isInWater() && inWaterProgress > 0F) {
            inWaterProgress--;
        }
        if (this.isInWater() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!this.isInWater() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (this.onGround && isDigging()) {
            spawnGroundEffects();
        }
        if (inWaterProgress > 0) {
            this.stepHeight = 1;
        } else {
            this.stepHeight = 0.6F;
        }
        if (!world.isRemote) {
            if (isInWater()) {
                swimTimer++;
            } else {
                swimTimer--;
            }
        }
        if (this.isEntityAlive() && (this.isSensing() || this.isSensingVisual())) {
            for (int j = 0; j < 2; ++j) {
                float radius = this.width * 0.65F;
                float angle = (0.01745329251F * this.renderYawOffset);
                double extraX = (radius * (1.5F + rand.nextFloat() * 0.3F)) * MathHelper.sin((float) (Math.PI + angle)) + (rand.nextFloat() - 0.5F) + this.motionX * 2F;
                double extraZ = (radius * (1.5F + rand.nextFloat() * 0.3F)) * MathHelper.cos(angle) + (rand.nextFloat() - 0.5F) + this.motionZ * 2F;
                double actualX = radius * MathHelper.sin((float) (Math.PI + angle));
                double actualZ = radius * MathHelper.cos(angle);
                double motX = actualX - extraX;
                double motZ = actualZ - extraZ;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.PLATYPUS_SENSE, this.posX + extraX, this.height * 0.3F + this.posY, this.posZ + extraZ, motX * 0.1F, 0, motZ * 0.1F);
            }
        }
    }

    public boolean isDigging() {
        return this.dataManager.get(DIGGING);
    }

    public void setDigging(boolean digging) {
        this.dataManager.set(DIGGING, digging);
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AnimalSwimMoveControllerSink(this, 1.2F, 1.6F);
            this.navigator = new SemiAquaticPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public boolean shouldEnterWater() {
        return this.getRevengeTarget() != null || swimTimer <= -1000 || this.isSensing();
    }

    @Override
    public boolean shouldLeaveWater() {
        return swimTimer > 600 && !this.isSensing();
    }

    @Override
    public boolean shouldStopMoving() {
        return this.isDigging();
    }

    @Override
    public int getWaterSearchRange() {
        return 10;
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityPlatypus) AMEntityRegistry.PLATYPUS.newInstance(this.world);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return !this.isSensing() && AMTagRegistry.itemInTag(AMTagRegistry.PLATYPUS_FOODSTUFFS, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
        if (e.getItem().getItem() == Items.REDSTONE || e.getItem().getItem() == Item.getItemFromBlock(Blocks.REDSTONE_BLOCK)) {
            superCharged = e.getItem().getItem() == Item.getItemFromBlock(Blocks.REDSTONE_BLOCK);
            this.setSensing(true);
        } else {
            this.heal(6);
        }
    }

    private class PlatypusRedstoneTempt extends EntityAITempt {

        private final EntityPlatypus platypus;

        PlatypusRedstoneTempt(EntityPlatypus platypus, double speed) {
            super(platypus, speed, false, ImmutableSet.of(Items.REDSTONE, Item.getItemFromBlock(Blocks.REDSTONE_BLOCK)));
            this.platypus = platypus;
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            this.platypus.setSensingVisual(true);
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !this.platypus.isSensing();
        }

        @Override
        public void resetTask() {
            super.resetTask();
            this.platypus.setSensingVisual(false);
        }
    }

    private class PlatypusFoodTempt extends EntityAITempt {

        private final EntityPlatypus platypus;

        PlatypusFoodTempt(EntityPlatypus platypus, double speed) {
            super(platypus, speed, Items.FISH, false);
            this.platypus = platypus;
        }

        @Override
        protected boolean isTempting(ItemStack stack) {
            return AMTagRegistry.itemInTag(AMTagRegistry.PLATYPUS_FOODSTUFFS, stack.getItem());
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !this.platypus.isSensing();
        }
    }

    private class PlatypusTargetItems extends CreatureAITargetItems {

        private final EntityPlatypus platypus;

        PlatypusTargetItems(EntityPlatypus platypus) {
            super(platypus, false, false, 40, 15);
            this.platypus = platypus;
        }

        @Override
        public boolean shouldExecute() {
            return super.shouldExecute() && !this.platypus.isSensing();
        }

        @Override
        public boolean shouldContinueExecuting() {
            return super.shouldContinueExecuting() && !this.platypus.isSensing();
        }
    }
}
