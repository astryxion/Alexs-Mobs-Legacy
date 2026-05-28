package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAISwimBottom;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class EntityTriops extends EntityCreature implements ISemiAquatic, ITargetsDroppedItems {

    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityTriops.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> TRIOPS_SCALE = EntityDataManager.createKey(EntityTriops.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> BABY_AGE = EntityDataManager.createKey(EntityTriops.class, DataSerializers.VARINT);
    public float prevOnLandProgress;
    public float onLandProgress;
    public float prevSwimRot;
    public float swimRot;
    public boolean fedCarrot = false;
    public int breedCooldown = 0;
    public float tail1Yaw;
    public float prevTail1Yaw;
    public float tail2Yaw;
    public float prevTail2Yaw;
    public float moveDistance;
    private EntityTriops breedWith;

    public EntityTriops(World world) {
        super(world);
        this.setSize(0.6F, 0.35F);
        this.moveHelper = new AquaticMoveController(this, 1.0F, 15F);
        this.tail1Yaw = this.rotationYaw;
        this.prevTail1Yaw = this.rotationYaw;
        this.tail2Yaw = this.rotationYaw;
        this.prevTail2Yaw = this.rotationYaw;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(4.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(TRIOPS_SCALE, 1.0F);
        this.dataManager.register(BABY_AGE, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new BreedGoal());
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(3, new AnimalAISwimBottom(this, 1.0F, 7));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, 10));
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 5;
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
            moveDistance += MathHelper.sqrt(strafe * strafe + forward * forward);
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Override
    protected boolean canDespawn() {
        return !this.isBaby() && !this.isFromBucket() && !this.fedCarrot && super.canDespawn();
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.triopsSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER)
                && super.getCanSpawnHere();
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    private void updateTriopsAir() {
        if (this.isEntityAlive() && !this.isInWater()) {
            this.setAir(this.getAir() - 1);
            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, this.rand.nextInt(2) == 0 ? 1.0F : 0.0F);
            }
        } else {
            this.setAir(2000);
        }
    }

    public int getBabyAge() {
        return this.dataManager.get(BABY_AGE);
    }

    public void setBabyAge(int babyAge) {
        this.dataManager.set(BABY_AGE, babyAge);
    }

    public float getTriopsScale() {
        return this.dataManager.get(TRIOPS_SCALE);
    }

    public void setTriopsScale(float scale) {
        this.dataManager.set(TRIOPS_SCALE, scale);
        if (!this.world.isRemote) {
            this.setSize(0.6F * scale, 0.35F * scale);
        }
    }

    public boolean isBaby() {
        return this.getBabyAge() < 0;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("FedCarrot", this.fedCarrot);
        compound.setInteger("BreedCooldown", this.breedCooldown);
        compound.setFloat("TriopsScale", this.getTriopsScale());
        compound.setInteger("BabyAge", this.getBabyAge());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.fedCarrot = compound.getBoolean("FedCarrot");
        this.breedCooldown = compound.getInteger("BreedCooldown");
        this.setTriopsScale(compound.getFloat("TriopsScale"));
        this.setBabyAge(compound.getInteger("BabyAge"));
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, net.minecraft.entity.IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setTriopsScale(0.9F + this.rand.nextFloat() * 0.2F);
        return livingdata;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.updateTriopsAir();
        this.prevOnLandProgress = onLandProgress;
        this.prevSwimRot = swimRot;
        this.prevTail1Yaw = tail1Yaw;
        this.prevTail2Yaw = tail2Yaw;
        final boolean onLand = !this.isInWater() && this.onGround;
        this.rotationPitch = (float) -((float) this.motionY * 2.2F * (180F / (float) Math.PI));
        if (onLand && onLandProgress < 5F) {
            onLandProgress++;
        }
        if (!onLand && onLandProgress > 0F) {
            onLandProgress--;
        }
        if (breedCooldown > 0) {
            breedCooldown--;
        }
        tail1Yaw = approachDegrees(this.tail1Yaw, this.renderYawOffset, 7);
        tail2Yaw = approachDegrees(this.tail2Yaw, this.tail1Yaw, 7);
        if (onLandProgress == 0) {
            float f = (float) (20 * Math.sin(this.limbSwing) * this.limbSwingAmount);
            swimRot = approachDegrees(this.swimRot, f, 2);
        }
        if (this.getBabyAge() < 0) {
            this.setBabyAge(this.getBabyAge() + 1);
        }
    }

    private static float approachDegrees(float from, float to, float max) {
        float f = MathHelper.wrapDegrees(to - from);
        if (f > max) {
            f = max;
        }
        if (f < -max) {
            f = -max;
        }
        return from + f;
    }

    @Override
    public EnumCreatureAttribute getCreatureAttribute() {
        return EnumCreatureAttribute.ARTHROPOD;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 67) {
            for (int i = 0; i < 5; i++) {
                this.world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                        this.posX + (this.rand.nextFloat() - 0.5F) * this.width,
                        this.posY + this.height * 0.8F,
                        this.posZ + (this.rand.nextFloat() - 0.5F) * this.width,
                        0.0D, 0.0D, 0.0D);
            }
        } else if (id == 68) {
            this.world.spawnParticle(EnumParticleTypes.HEART, this.posX, this.posY + this.height * 0.8F, this.posZ, 0.0D, 0.0D, 0.0D);
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return (AMTagRegistry.itemInTag(AMTagRegistry.TRIOPS_BREEDABLES, stack.getItem()) || stack.getItem() == AMItemRegistry.MOSQUITO_LARVA) && !fedCarrot;
    }

    @Override
    public void onGetItem(EntityItem e) {
        ItemStack stack = e.getItem();
        if (stack.getItem() instanceof ItemFood && !fedCarrot) {
            this.playSound(SoundEvents.ENTITY_PLAYER_BURP, this.getSoundPitch(), this.getSoundVolume());
            this.heal(5.0F);
            if (!this.world.isRemote) {
                if (breedCooldown == 0) {
                    this.fedCarrot = true;
                    this.world.setEntityState(this, (byte) 67);
                }
            }
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (canTargetItem(itemstack) && !this.fedCarrot) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.playSound(SoundEvents.ENTITY_PLAYER_BURP, this.getSoundPitch(), this.getSoundVolume());
            this.heal(5.0F);
            if (AMTagRegistry.itemInTag(AMTagRegistry.TRIOPS_BREEDABLES, itemstack.getItem())) {
                if (!this.world.isRemote && breedCooldown == 0) {
                    this.world.setEntityState(this, (byte) 67);
                }
                this.fedCarrot = true;
            }
            return true;
        }
        ItemStack bucket = this.getFishBucket();
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            this.setBucketData(bucket);
            if (!this.world.isRemote) {
                this.setDead();
            }
            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, bucket);
            } else if (!player.inventory.addItemStackToInventory(bucket)) {
                player.dropItem(bucket, false);
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.TRIOPS_BUCKET);
        if (this.hasCustomName()) {
            stack.setStackDisplayName(this.getCustomNameTag());
        }
        return stack;
    }

    protected void setBucketData(ItemStack bucket) {
        if (this.hasCustomName()) {
            bucket.setStackDisplayName(this.getCustomNameTag());
        }
        NBTTagCompound tag = bucket.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            bucket.setTagCompound(tag);
        }
        NBTTagCompound platTag = new NBTTagCompound();
        this.writeEntityToNBT(platTag);
        tag.setTag("TriopsTag", platTag);
    }

    public boolean isSearchingForMate() {
        return this.isEntityAlive() && this.isInWater() && this.fedCarrot && this.breedCooldown <= 0;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.TRIOPS_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.TRIOPS_HURT;
    }

    @Override
    public boolean shouldEnterWater() {
        return !this.isInWater();
    }

    @Override
    public boolean shouldLeaveWater() {
        return false;
    }

    @Override
    public boolean shouldStopMoving() {
        return false;
    }

    @Override
    public int getWaterSearchRange() {
        return 14;
    }

    private class BreedGoal extends EntityAIBase {
        private final Predicate<Entity> validBreedPartner;
        private EntityTriops breedPartner;
        private int executionCooldown = 50;

        BreedGoal() {
            this.setMutexBits(1);
            this.validBreedPartner = entity -> {
                if (!(entity instanceof EntityTriops)) {
                    return false;
                }
                EntityTriops other = (EntityTriops) entity;
                return other.getEntityId() != EntityTriops.this.getEntityId() && other.isSearchingForMate();
            };
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityTriops.this.isInWater() || !EntityTriops.this.fedCarrot || EntityTriops.this.breedCooldown > 0 || EntityTriops.this.breedWith != null) {
                return false;
            }
            if (executionCooldown > 0) {
                executionCooldown--;
                return false;
            }
            executionCooldown = 50 + EntityTriops.this.rand.nextInt(50);
            List<EntityTriops> list = EntityTriops.this.world.getEntitiesWithinAABB(EntityTriops.class, EntityTriops.this.getEntityBoundingBox().grow(10, 8, 10), validBreedPartner::test);
            list.sort(Comparator.comparingDouble(EntityTriops.this::getDistanceSq));
            if (!list.isEmpty()) {
                breedPartner = list.get(0);
                breedPartner.breedWith = EntityTriops.this;
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return breedPartner != null && EntityTriops.this.breedWith == null
                    && breedPartner.isSearchingForMate() && EntityTriops.this.isSearchingForMate();
        }

        @Override
        public void resetTask() {
            EntityTriops.this.fedCarrot = false;
            EntityTriops.this.breedCooldown = 1200 + EntityTriops.this.rand.nextInt(3600);
            if (breedPartner != null) {
                breedPartner.breedWith = null;
                breedPartner.fedCarrot = false;
                breedPartner.breedCooldown = 1200 + breedPartner.rand.nextInt(3600);
            }
            breedPartner = null;
        }

        private void spawnOffspring() {
            int count = 2 + EntityTriops.this.rand.nextInt(2);
            for (int i = 0; i < count; i++) {
                EntityTriops baby = (EntityTriops) AMEntityRegistry.TRIOPS.newInstance(EntityTriops.this.world);
                if (baby != null) {
                    baby.setBabyAge(-12000);
                    baby.setLocationAndAngles(
                            EntityTriops.this.posX + (EntityTriops.this.rand.nextDouble() - 0.5D) * 2.0D,
                            EntityTriops.this.posY,
                            EntityTriops.this.posZ + (EntityTriops.this.rand.nextDouble() - 0.5D) * 2.0D,
                            0.0F,
                            0.0F);
                    EntityTriops.this.world.spawnEntity(baby);
                }
            }
        }

        @Override
        public void updateTask() {
            EntityTriops.this.getNavigator().tryMoveToEntityLiving(breedPartner, 1.0D);
            breedPartner.getNavigator().tryMoveToEntityLiving(EntityTriops.this, 1.0D);
            if (EntityTriops.this.getDistance(breedPartner) < 1.2F) {
                EntityTriops.this.world.setEntityState(EntityTriops.this, (byte) 68);
                spawnOffspring();
                resetTask();
            }
        }
    }
}
