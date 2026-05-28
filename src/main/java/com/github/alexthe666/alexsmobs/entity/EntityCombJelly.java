package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityCombJelly extends EntityCreature {

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityCombJelly.class, DataSerializers.VARINT);
    private static final DataParameter<Float> JELLYPITCH = EntityDataManager.createKey(EntityCombJelly.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.createKey(EntityCombJelly.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> JELLY_SCALE = EntityDataManager.createKey(EntityCombJelly.class, DataSerializers.FLOAT);
    public float prevOnLandProgress;
    public float onLandProgress;
    private BlockPos moveTarget;
    public float prevjellyPitch = 0;
    public float spin;
    public float prevSpin;

    public EntityCombJelly(World world) {
        super(world);
        this.setSize(0.9F, 0.9F);
        this.setNoGravity(true);
    }

    @Override
    protected void initEntityAI() {
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
    }

    public static boolean canCombJellySpawn(World world, BlockPos pos, java.util.Random rand) {
        if (world.getBlockState(pos).getMaterial() != Material.WATER || world.getBlockState(pos.up()).getMaterial() != Material.WATER) {
            return false;
        }
        float time = world.getCelestialAngle(1.0F);
        int light = world.getLight(pos);
        return light <= 4 && time > 0.27F && time <= 0.8F;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.combJellySpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 2;
    }

    @Override
    protected boolean canDespawn() {
        return !this.isFromBucket() && !this.hasCustomName() && super.canDespawn();
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(JELLYPITCH, 0F);
        this.dataManager.register(FROM_BUCKET, false);
        this.dataManager.register(JELLY_SCALE, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.COMB_JELLY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.COMB_JELLY_HURT;
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public float getJellyPitch() {
        return MathHelper.clamp(this.dataManager.get(JELLYPITCH), -90, 90);
    }

    public void setJellyPitch(float pitch) {
        this.dataManager.set(JELLYPITCH, MathHelper.clamp(pitch, -90, 90));
    }

    public float getJellyScale() {
        return this.dataManager.get(JELLY_SCALE);
    }

    public void setJellyScale(float scale) {
        this.dataManager.set(JELLY_SCALE, scale);
    }

    public boolean isFromBucket() {
        return this.dataManager.get(FROM_BUCKET);
    }

    public void setFromBucket(boolean fromBucket) {
        this.dataManager.set(FROM_BUCKET, fromBucket);
    }

    protected ItemStack getFishBucket() {
        ItemStack stack = new ItemStack(AMItemRegistry.COMB_JELLY_BUCKET);
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
        tag.setFloat("BucketScale", this.getJellyScale());
        tag.setInteger("BucketVariantTag", this.getVariant());
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.getItem() == Items.WATER_BUCKET && this.isEntityAlive()) {
            this.playSound(SoundEvents.ITEM_BUCKET_FILL, 1.0F, 1.0F);
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            ItemStack itemstack1 = this.getFishBucket();
            this.setBucketData(itemstack1);
            if (itemstack.isEmpty()) {
                player.setHeldItem(hand, itemstack1);
            } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                player.dropItem(itemstack1, false);
            }
            this.setDead();
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevOnLandProgress = onLandProgress;
        this.prevjellyPitch = this.getJellyPitch();
        this.prevSpin = this.spin;
        if (!this.isInWater() && onLandProgress < 5F) {
            onLandProgress++;
        }
        if (this.isInWater() && onLandProgress > 0F) {
            onLandProgress--;
        }

        if (!this.world.isRemote) {
            if (this.isInWater()) {
                this.setNoGravity(true);
                if (moveTarget == null || this.rand.nextInt(120) == 0 || this.getDistanceSq(moveTarget.getX() + 0.5F, moveTarget.getY() + 0.5F, moveTarget.getZ() + 0.5F) < 5
                        || this.ticksExisted % 10 == 0 && !canBlockPosBeSeen(moveTarget)) {
                    BlockPos randPos = this.getPosition().add(this.rand.nextInt(10) - 5, this.rand.nextInt(6) - 3, this.rand.nextInt(10) - 5);
                    if (this.world.getBlockState(randPos).getMaterial() == Material.WATER && this.world.getBlockState(randPos.up()).getMaterial() == Material.WATER) {
                        moveTarget = randPos;
                    }
                }
                if (this.getFluidHeight() < this.height) {
                    moveTarget = null;
                    this.motionY -= 0.02D;
                }
                if (moveTarget != null) {
                    final double d0 = moveTarget.getX() + 0.5F - this.posX;
                    final double d1 = moveTarget.getY() + 0.5F - this.posY;
                    final double d2 = moveTarget.getZ() + 0.5F - this.posZ;
                    final double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    final float f = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
                    this.rotationYaw = rotlerp(this.rotationYaw, f, 1);
                    this.renderYawOffset = this.rotationYaw;
                    final float movSpeed = 0.004F;
                    if (d3 > 0.001D) {
                        this.motionX += (d0 / d3) * movSpeed;
                        this.motionY += (d1 / d3) * movSpeed;
                        this.motionZ += (d2 / d3) * movSpeed;
                    }
                }
                final float dist = (float) ((Math.abs(this.motionX) + Math.abs(this.motionZ)) * 30);
                this.incrementJellyPitch(dist);
                if (this.collidedHorizontally) {
                    this.motionY += 0.2D;
                }
                if (this.getJellyPitch() > 0F) {
                    float decrease = Math.min(0.5F, this.getJellyPitch());
                    this.decrementJellyPitch(decrease);
                }
                if (this.getJellyPitch() < 0F) {
                    float decrease = Math.min(0.5F, -this.getJellyPitch());
                    this.incrementJellyPitch(decrease);
                }
            } else {
                this.setNoGravity(false);
            }
        }
    }

    private float getFluidHeight() {
        if (!this.isInWater()) {
            return 0;
        }
        float height = 0;
        BlockPos pos = this.getPosition();
        for (int y = pos.getY(); y < pos.getY() + 4; y++) {
            if (this.world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getMaterial() == Material.WATER) {
                height += 1;
            } else {
                break;
            }
        }
        return height;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("FromBucket", this.isFromBucket());
        compound.setFloat("JellyScale", this.getJellyScale());
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setJellyScale(compound.getFloat("JellyScale"));
        this.setVariant(compound.getInteger("Variant"));
    }

    public boolean canBlockPosBeSeen(BlockPos pos) {
        final double x = pos.getX() + 0.5F;
        final double y = pos.getY() + 0.5F;
        final double z = pos.getZ() + 0.5F;
        final RayTraceResult result = this.world.rayTraceBlocks(this.getPositionEyes(1.0F), new Vec3d(x, y, z), false, true, false);
        if (result == null) {
            return true;
        }
        final double dist = result.hitVec.squareDistanceTo(x, y, z);
        return dist <= 1.0D || result.typeOfHit == RayTraceResult.Type.MISS;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!this.world.isRemote && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.6D;
            this.motionZ *= 0.9D;
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.setVariant(this.rand.nextInt(3));
        this.setJellyScale(0.8F + this.rand.nextFloat() * 0.4F);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    public void incrementJellyPitch(float pitch) {
        this.dataManager.set(JELLYPITCH, this.getJellyPitch() + pitch);
    }

    public void decrementJellyPitch(float pitch) {
        this.dataManager.set(JELLYPITCH, this.getJellyPitch() - pitch);
    }

    protected float rotlerp(float current, float target, float maxChange) {
        float f = MathHelper.wrapDegrees(target - current);
        if (f > maxChange) {
            f = maxChange;
        }
        if (f < -maxChange) {
            f = -maxChange;
        }
        float f1 = current + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }
        return f1;
    }

    @Override
    public boolean isNotColliding() {
        return this.world.checkNoEntityCollision(this.getEntityBoundingBox(), this);
    }
}
