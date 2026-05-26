package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAISwimBottom;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityBlobfish extends EntityCreature implements ISemiAquatic {

    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityBlobfish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> BLOBFISH_SCALE = EntityDataManager.createKey(EntityBlobfish.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> DEPRESSURIZED = EntityDataManager.createKey(EntityBlobfish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> SLIMED = EntityDataManager.createKey(EntityBlobfish.class, DataSerializers.BOOLEAN);
    public float squishFactor;
    public float prevSquishFactor;
    public float squishAmount;
    private boolean wasOnGround;

    public EntityBlobfish(World world) {
        super(world);
        this.setSize(0.6F, 0.45F);
        this.moveHelper = new AquaticMoveController(this, 1.0F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.blobfishSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new PathNavigateSwimmer(this, worldIn);
    }

    private void updateBlobfishAir() {
        if (this.isEntityAlive() && !this.isInWater() && !this.isSlimed()) {
            this.setAir(this.getAir() - 1);
            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, this.rand.nextInt(2) == 0 ? 1.0F : 0.0F);
            }
        } else {
            this.setAir(300);
        }
    }

    @Override
    public float getEyeHeight() {
        return this.height * 0.65F;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName() && super.canDespawn();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 4;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(BLOBFISH_SCALE, 1.0F);
        this.dataManager.register(DEPRESSURIZED, false);
        this.dataManager.register(SLIMED, false);
    }

    private void applyBlobfishScaleToBoundingBox() {
        float sc = this.getBlobfishScale();
        this.setSize(0.6F * sc, 0.45F * sc);
    }

    private boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setBoolean("Depressurized", this.isDepressurized());
        compound.setBoolean("Slimed", this.isSlimed());
        compound.setFloat("BlobfishScale", this.getBlobfishScale());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setDepressurized(compound.getBoolean("Depressurized"));
        this.setSlimed(compound.getBoolean("Slimed"));
        this.setBlobfishScale(compound.getFloat("BlobfishScale"));
        this.applyBlobfishScaleToBoundingBox();
    }

    private boolean hasClearance() {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(0, 0, 0);
        for (int l1 = 0; l1 < 10; ++l1) {
            mutable.setPos(MathHelper.floor(this.posX), MathHelper.floor(this.posY) + l1, MathHelper.floor(this.posZ));
            if (this.world.getBlockState(mutable).getMaterial() != Material.WATER) {
                return false;
            }
        }
        return true;
    }

    public float getBlobfishScale() {
        return this.dataManager.get(BLOBFISH_SCALE);
    }

    public void setBlobfishScale(float scale) {
        this.dataManager.set(BLOBFISH_SCALE, scale);
        if (!this.world.isRemote) {
            this.applyBlobfishScaleToBoundingBox();
        }
    }

    public boolean isDepressurized() {
        return this.dataManager.get(DEPRESSURIZED);
    }

    public void setDepressurized(boolean depressurized) {
        this.dataManager.set(DEPRESSURIZED, depressurized);
    }

    public boolean isSlimed() {
        return this.dataManager.get(SLIMED);
    }

    public void setSlimed(boolean slimed) {
        this.dataManager.set(SLIMED, slimed);
    }

    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(3, new AnimalAISwimBottom(this, 1.0D, 7));
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

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.world != null && !this.world.isRemote && this.isInWater()) {
            float moveSpeed = this.getAIMoveSpeed();
            this.moveRelative(strafe, vertical, forward, 0.02F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.9D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY += -0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() == Items.SLIME_BALL && this.isEntityAlive() && !this.isSlimed()) {
            this.setSlimed(true);
            for (int i = 0; i < 6 + this.rand.nextInt(3); i++) {
                double d2 = this.rand.nextGaussian() * 0.02D;
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = this.rand.nextGaussian() * 0.02D;
                this.world.spawnParticle(
                        EnumParticleTypes.ITEM_CRACK,
                        this.posX + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F,
                        this.posY + this.height * 0.5F + (double) (this.rand.nextFloat() * this.height * 0.5F),
                        this.posZ + (double) (this.rand.nextFloat() * this.width) - (double) this.width * 0.5F,
                        d0,
                        d1,
                        d2,
                        net.minecraft.item.Item.getIdFromItem(Items.SLIME_BALL));
            }
            if (!this.world.isRemote && !player.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
            return true;
        }
        if (stack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!this.world.isRemote) {
                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }
                ItemStack handStack = player.getHeldItem(hand);
                ItemStack bucket = this.getFishBucket();
                this.setBucketData(bucket);
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

    protected void playStepSound(BlockPos pos, net.minecraft.block.Block blockIn) {
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.BLOBFISH_BUCKET);
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
        tag.setFloat("BucketScale", this.getBlobfishScale());
        tag.setBoolean("Slimed", this.isSlimed());
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setBlobfishScale(0.75F + this.rand.nextFloat() * 0.5F);
        if (!this.world.isRemote) {
            this.applyBlobfishScaleToBoundingBox();
        }
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public void onLivingUpdate() {
        this.updateBlobfishAir();
        super.onLivingUpdate();
        this.prevSquishFactor = this.squishFactor;
        this.squishFactor += (this.squishAmount - this.squishFactor) * 0.5F;

        float f2 = (float) -((float) this.motionY * 2.2F * (180F / (float) Math.PI));
        this.rotationPitch = f2;
        if (!this.isInWater()) {
            if (this.onGround && !this.wasOnGround) {
                this.squishAmount = -0.35F;
            } else if (!this.onGround && this.wasOnGround) {
                this.squishAmount = 2.0F;
            }
        }
        this.wasOnGround = this.onGround;

        this.alterSquishAmount();
        boolean clear = this.hasClearance();
        if (this.isDepressurized() && clear) {
            this.setDepressurized(false);
        }
        if (!this.isDepressurized() && !clear) {
            this.setDepressurized(true);
        }
    }

    protected void alterSquishAmount() {
        this.squishAmount *= 0.6F;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SQUID_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_SQUID_HURT;
    }

}
