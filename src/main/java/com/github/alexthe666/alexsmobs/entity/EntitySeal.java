package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.*;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EntitySeal extends EntityAnimal implements ISemiAquatic, IHerdPanic, ITargetsDroppedItems {

    private static final DataParameter<Float> SWIM_ANGLE = EntityDataManager.createKey(EntitySeal.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> BASKING = EntityDataManager.createKey(EntitySeal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DIGGING = EntityDataManager.createKey(EntitySeal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> ARCTIC = EntityDataManager.createKey(EntitySeal.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntitySeal.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> BOB_TICKS = EntityDataManager.createKey(EntitySeal.class, DataSerializers.VARINT);
    public float prevSwimAngle;
    public float prevBaskProgress;
    public float baskProgress;
    public float prevDigProgress;
    public float digProgress;
    public float prevBobbingProgress;
    public float bobbingProgress;
    public int revengeCooldown = 0;
    public UUID feederUUID = null;
    public int fishFeedings = 0;
    private int baskingTimer = 0;
    private int swimTimer = -1000;
    private boolean isLandNavigator;

    public EntitySeal(World worldIn) {
        super(worldIn);
        this.setSize(1.1F, 0.8F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, 0.0F);
        switchNavigator(false);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.sealSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SEAL_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SEAL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SEAL_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.18D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new SealAIBask(this));
        this.tasks.addTask(1, new BreatheAirGoal(this));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new AnimalAIFindWater(this));
        this.tasks.addTask(3, new AnimalAILeaveWater(this));
        this.tasks.addTask(4, new AnimalAIHerdPanic(this, 1.6D));
        this.tasks.addTask(5, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(6, new SealAIDiveForItems(this));
        this.tasks.addTask(7, new SemiAquaticAIRandomSwimming(this, 1.0D, 7));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(9, new EntityAIAvoidEntity(this, EntityOrca.class, 20.0F, 1.3D, 1.0D));
        this.tasks.addTask(10, new SealAITempt(this, 1.1D));
        this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityFlyingFish.class, 55, true, true, null));
        this.targetTasks.addTask(2, new CreatureAITargetItems(this, false));
    }

    private void switchNavigator(boolean onLand) {
        if (onLand) {
            this.moveHelper = new EntityMoveHelper(this);
            this.navigator = new GroundPathNavigatorWide(this, world);
            this.isLandNavigator = true;
        } else {
            this.moveHelper = new AquaticMoveController(this, 1.5F);
            this.navigator = new SemiAquaticPathNavigator(this, world);
            this.isLandNavigator = false;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev) {
            double range = 15;
            int fleeTime = 100 + getRNG().nextInt(150);
            this.revengeCooldown = fleeTime;
            List<EntitySeal> list = this.world.getEntitiesWithinAABB(EntitySeal.class, this.getEntityBoundingBox().grow(range, range / 2, range));
            for (EntitySeal gaz : list) {
                gaz.revengeCooldown = fleeTime;
                gaz.setBasking(false);
            }
            this.setBasking(false);
        }
        return prev;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SWIM_ANGLE, 0F);
        this.dataManager.register(BASKING, Boolean.FALSE);
        this.dataManager.register(DIGGING, Boolean.FALSE);
        this.dataManager.register(ARCTIC, Boolean.FALSE);
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(BOB_TICKS, 0);
    }

    public float getSwimAngle() {
        return this.dataManager.get(SWIM_ANGLE);
    }

    public void setSwimAngle(float progress) {
        this.dataManager.set(SWIM_ANGLE, progress);
    }

    @Override
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
        prevBaskProgress = baskProgress;
        prevDigProgress = digProgress;
        prevBobbingProgress = bobbingProgress;
        prevSwimAngle = this.getSwimAngle();
        boolean dig = isDigging() && isInWater();
        float f2 = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        if (isInWater()) {
            this.rotationPitch = f2 * 2.5F;
        }

        if (isInWater() && this.isLandNavigator) {
            switchNavigator(false);
        }
        if (!isInWater() && !this.isLandNavigator) {
            switchNavigator(true);
        }
        if (isBasking() && baskProgress < 5F) {
            baskProgress++;
        }
        if (!isBasking() && baskProgress > 0F) {
            baskProgress--;
        }
        if (dig && digProgress < 5F) {
            digProgress++;
        }
        if (!dig && digProgress > 0F) {
            digProgress--;
        }
        if (dig && world.getBlockState(this.getPositionUnderneath()).isSideSolid(world, this.getPositionUnderneath(), EnumFacing.UP)) {
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
        if (!this.world.isRemote) {
            if (isBasking()) {
                if (this.getRevengeTarget() != null || isInLove() || revengeCooldown > 0 || this.isInWater() || this.getAttackTarget() != null || baskingTimer > 1000 && this.getRNG().nextInt(100) == 0) {
                    this.setBasking(false);
                }
            } else {
                if (this.getAttackTarget() == null && !isInLove() && this.getRevengeTarget() == null && revengeCooldown == 0 && !isBasking() && baskingTimer == 0 && this.getRNG().nextInt(15) == 0) {
                    if (!isInWater()) {
                        this.setBasking(true);
                    }
                }
            }
            if (revengeCooldown > 0) {
                revengeCooldown--;
            }
            if (revengeCooldown == 0 && this.getRevengeTarget() != null) {
                this.setRevengeTarget(null);
            }
            float threshold = 0.05F;
            if (isInWater() && this.prevRotationYaw - this.rotationYaw > threshold) {
                this.setSwimAngle(this.getSwimAngle() + 2);
            } else if (isInWater() && this.prevRotationYaw - this.rotationYaw < -threshold) {
                this.setSwimAngle(this.getSwimAngle() - 2);
            } else if (this.getSwimAngle() > 0) {
                this.setSwimAngle(Math.max(this.getSwimAngle() - 10, 0));
            } else if (this.getSwimAngle() < 0) {
                this.setSwimAngle(Math.min(this.getSwimAngle() + 10, 0));
            }
            this.setSwimAngle(MathHelper.clamp(this.getSwimAngle(), -70, 70));
            if (isBasking()) {
                baskingTimer++;
            } else {
                baskingTimer = 0;
            }
            if (isInWater()) {
                swimTimer++;
            } else {
                swimTimer--;
            }
        }
        int bob = this.dataManager.get(BOB_TICKS);
        if (bob > 0) {
            bob--;
            if (this.bobbingProgress < 5F) {
                this.bobbingProgress++;
            }
            this.dataManager.set(BOB_TICKS, bob);
        } else {
            if (this.bobbingProgress > 0F) {
                this.bobbingProgress--;
            }
            if (!this.world.isRemote && this.rand.nextInt(300) == 0 && !this.isInWater() && this.revengeCooldown == 0) {
                bob = 20 + this.rand.nextInt(20);
                this.dataManager.set(BOB_TICKS, bob);
            }
        }
    }

    protected BlockPos getPositionUnderneath() {
        return new BlockPos(this.posX, this.posY - 1.0D, this.posZ);
    }

    public boolean isBasking() {
        return this.dataManager.get(BASKING);
    }

    public void setBasking(boolean basking) {
        this.dataManager.set(BASKING, basking);
    }

    public boolean isDigging() {
        return this.dataManager.get(DIGGING);
    }

    public void setDigging(boolean digging) {
        this.dataManager.set(DIGGING, digging);
    }

    public boolean isArctic() {
        return this.dataManager.get(ARCTIC);
    }

    public void setArctic(boolean arctic) {
        this.dataManager.set(ARCTIC, arctic);
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isTearsEasterEgg() {
        String s = net.minecraft.util.text.TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && s.toLowerCase().contains("he was");
    }

    public int getMaxAir() {
        return 4800;
    }

    protected int determineNextAir(int currentAir) {
        return this.getMaxAir();
    }

    public int getVerticalFaceSpeed() {
        return 1;
    }

    public int getHorizontalFaceSpeed() {
        return 1;
    }

    @Override
    @Nullable
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable net.minecraft.entity.IEntityLivingData livingdata) {
        this.setArctic(this.isBiomeArctic(this.world, this.getPosition()));
        int i;
        if (livingdata instanceof SealGroupData) {
            i = ((SealGroupData) livingdata).variant;
        } else {
            i = this.rand.nextInt(2);
            livingdata = new SealGroupData(i);
        }
        this.setVariant(i);
        this.setAir(this.getMaxAir());
        this.rotationPitch = 0.0F;
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("Arctic", this.isArctic());
        compound.setBoolean("Basking", this.isBasking());
        compound.setInteger("BaskingTimer", this.baskingTimer);
        compound.setInteger("SwimTimer", this.swimTimer);
        compound.setInteger("FishFeedings", this.fishFeedings);
        compound.setInteger("Variant", this.getVariant());
        if (feederUUID != null) {
            compound.setUniqueId("FeederUUID", feederUUID);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setArctic(compound.getBoolean("Arctic"));
        this.setBasking(compound.getBoolean("Basking"));
        this.baskingTimer = compound.getInteger("BaskingTimer");
        this.swimTimer = compound.getInteger("SwimTimer");
        this.fishFeedings = compound.getInteger("FishFeedings");
        if (compound.hasUniqueId("FeederUUID")) {
            this.feederUUID = compound.getUniqueId("FeederUUID");
        }
        this.setVariant(compound.getInteger("Variant"));
    }

    private boolean isBiomeArctic(World worldIn, BlockPos position) {
        Biome biome = worldIn.getBiome(position);
        return biome.getEnableSnow();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(net.minecraft.entity.MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
            if (this.isDigging()) {
                this.motionY -= 0.02D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.SEAL_BREEDABLES, stack.getItem());
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        EntitySeal seal = (EntitySeal) AMEntityRegistry.SEAL.newInstance(this.world);
        seal.setArctic(this.isBiomeArctic(this.world, this.getPosition()));
        return seal;
    }

    @Override
    public boolean shouldEnterWater() {
        return !shouldLeaveWater() && swimTimer <= -1000;
    }

    @Override
    public boolean shouldLeaveWater() {
        if (!this.getPassengers().isEmpty()) {
            return false;
        }
        if (this.getAttackTarget() != null && !this.getAttackTarget().isInWater()) {
            return true;
        }
        return swimTimer > 600;
    }

    @Override
    public boolean shouldStopMoving() {
        return isBasking();
    }

    @Override
    public int getWaterSearchRange() {
        return 32;
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.SEAL_OFFERINGS, stack.getItem())
                || AMTagRegistry.itemInTag(AMTagRegistry.SEAL_BREEDABLES, stack.getItem());
    }

    @Override
    public void onGetItem(EntityItem e) {
        if (AMTagRegistry.itemInTag(AMTagRegistry.SEAL_OFFERINGS, e.getItem().getItem())) {
            fishFeedings++;
            this.playSound(SoundEvents.ENTITY_CAT_PURREOW, this.getSoundVolume(), this.getSoundPitch());
            UUID throwerUUID = resolveThrowerUuid(e);
            if (fishFeedings >= 3) {
                if (throwerUUID != null) {
                    feederUUID = throwerUUID;
                }
                fishFeedings = 0;
            }
        } else {
            feederUUID = null;
        }
        this.heal(10);
    }

    @Override
    public void onPanic() {
    }

    @Override
    public boolean canPanic() {
        return !isBasking();
    }

    private static UUID resolveThrowerUuid(EntityItem item) {
        String throwerName = item.getThrower();
        if (throwerName == null) {
            return null;
        }
        EntityPlayer player = item.world.getPlayerEntityByName(throwerName);
        return player != null ? player.getUniqueID() : null;
    }

    private static class SealAITempt extends EntityAITempt {

        SealAITempt(EntitySeal seal, double speed) {
            super(seal, speed, Items.FISH, false);
        }

        @Override
        protected boolean isTempting(ItemStack stack) {
            return AMTagRegistry.itemInTag(AMTagRegistry.SEAL_BREEDABLES, stack.getItem())
                    || AMTagRegistry.itemInTag(AMTagRegistry.SEAL_OFFERINGS, stack.getItem());
        }
    }

    public static class SealGroupData implements net.minecraft.entity.IEntityLivingData {
        public final int variant;

        public SealGroupData(int variant) {
            this.variant = variant;
        }
    }
}
