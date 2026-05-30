package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIFindWater;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIRandomSwimming;
import com.github.alexthe666.alexsmobs.entity.ai.AquaticMoveController;
import com.github.alexthe666.alexsmobs.entity.ai.SwimmerJumpPathNavigator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.EnumSet;
import java.util.List;

public class EntityFlyingFish extends EntityAnimal implements ISemiAquatic {

    private static final DataParameter<Boolean> GLIDING = EntityDataManager.createKey(EntityFlyingFish.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityFlyingFish.class, DataSerializers.VARINT);

    public float prevOnLandProgress;
    public float onLandProgress;
    public float prevFlyProgress;
    public float flyProgress;
    public int glideIn = rand.nextInt(75) + 50;

    public EntityFlyingFish(World world) {
        super(world);
        this.setSize(0.6F, 0.35F);
        this.moveHelper = new AquaticMoveController(this, 1.0F, 15F);
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.FLYING_FISH;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(GLIDING, false);
        this.dataManager.register(VARIANT, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new AnimalAIFindWater(this));
        this.tasks.addTask(2, new GlideGoal());
        this.tasks.addTask(3, new EntityAIPanic(this, 1.0D));
        this.tasks.addTask(4, new AnimalAIRandomSwimming(this, 1.0D, 12, 5));
    }

    @Override
    protected PathNavigate createNavigator(World worldIn) {
        return new SwimmerJumpPathNavigator(this, worldIn);
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return 3;
    }

    public boolean canSpawnMoreAnimals() {
        return false;
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
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.flyingFishSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER);
    }

    @Override
    public boolean isNotColliding() {
        return AMEntityRegistry.aquaticNoEntityCollision(this);
    }

    @Override
    protected boolean canDespawn() {
        return !this.hasCustomName() && super.canDespawn();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getTrueSource() != null) {
            double range = 15;
            this.glideIn = 0;
            List<EntityFlyingFish> list = this.world.getEntitiesWithinAABB(EntityFlyingFish.class, this.getEntityBoundingBox().grow(range, range / 2, range));
            for (EntityFlyingFish fsh : list) {
                fsh.glideIn = 0;
            }
        }
        return prev;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevOnLandProgress = onLandProgress;
        this.prevFlyProgress = flyProgress;
        boolean onLand = !this.isInWater() && this.onGround;
        if (onLand && onLandProgress < 5F) {
            onLandProgress++;
        }
        if (!onLand && onLandProgress > 0F) {
            onLandProgress--;
        }

        if (isGliding()) {
            if (flyProgress < 5F) {
                flyProgress++;
            }
            if (!this.isInWater() && this.motionY < 0.0D) {
                this.motionY *= 0.5D;
            }
        } else if (flyProgress > 0F) {
            flyProgress--;
        }

        if (glideIn > 0) {
            glideIn--;
        }
        this.renderYawOffset = this.rotationYaw;
        float f2 = (float) -(this.motionY * 3F * ((float) Math.PI / 180F));
        if (this.isGliding()) {
            f2 = -f2;
        }
        this.rotationPitch = rotlerp(this.rotationPitch, f2, 9);
        if (!isInWater() && this.isEntityAlive()) {
            if (this.onGround && this.rand.nextFloat() < 0.05F) {
                this.motionX += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                this.motionY = 0.5D;
                this.motionZ += (this.rand.nextFloat() * 2.0F - 1.0F) * 0.2F;
                this.rotationYaw = this.rand.nextFloat() * 360.0F;
                this.playSound(SoundEvents.ENTITY_GUARDIAN_FLOP, this.getSoundVolume(), this.getSoundPitch());
            }
        }
        this.updateFlyingFishAir();
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    private void updateFlyingFishAir() {
        if (this.isEntityAlive() && !this.isInWater()) {
            this.setAir(this.getAir() - 1);
            if (this.getAir() == -20) {
                this.setAir(0);
                this.attackEntityFrom(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAir(300);
        }
    }

    protected float rotlerp(float current, float target, float maxChange) {
        float f = MathHelper.wrapDegrees(target - current);
        if (f > maxChange) {
            f = maxChange;
        }
        if (f < -maxChange) {
            f = -maxChange;
        }
        return current + f;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isServerWorld() && this.isInWater()) {
            this.moveRelative(strafe, vertical, forward, this.getAIMoveSpeed());
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9D;
            this.motionY *= 0.6D;
            this.motionZ *= 0.9D;
            if (this.getAttackTarget() == null) {
                this.motionY -= 0.005D;
            }
        } else {
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isGliding() {
        return this.dataManager.get(GLIDING);
    }

    public void setGliding(boolean flying) {
        this.dataManager.set(GLIDING, flying);
    }

    private boolean canSeeBlock(BlockPos destinationBlock) {
        Vec3d vector3d = new Vec3d(this.posX, this.posY + (double) this.getEyeHeight(), this.posZ);
        Vec3d blockVec = new Vec3d((double) destinationBlock.getX() + 0.5D, (double) destinationBlock.getY() + 0.5D, (double) destinationBlock.getZ() + 0.5D);
        RayTraceResult result = this.world.rayTraceBlocks(vector3d, blockVec, false, true, false);
        return result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.getBlockPos().equals(destinationBlock);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Variant", this.getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setVariant(compound.getInteger("Variant"));
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        int i;
        if (livingdata instanceof FlyingFishGroupData) {
            i = ((FlyingFishGroupData) livingdata).variant;
        } else {
            i = this.rand.nextInt(3);
            livingdata = new FlyingFishGroupData(i);
        }
        this.setVariant(i);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Nullable
    @Override
    public EntityFlyingFish createChild(net.minecraft.entity.EntityAgeable ageable) {
        return (EntityFlyingFish) AMEntityRegistry.FLYING_FISH.newInstance(this.world);
    }

    @Override
    public boolean isBreedingItem(net.minecraft.item.ItemStack stack) {
        return false;
    }

    public static class FlyingFishGroupData implements IEntityLivingData {
        public final int variant;

        FlyingFishGroupData(int variant) {
            this.variant = variant;
        }
    }

    private class GlideGoal extends EntityAIBase {
        private BlockPos surface;
        private BlockPos glide;

        private GlideGoal() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (!EntityFlyingFish.this.isInWater()) {
                return false;
            } else if (EntityFlyingFish.this.glideIn == 0 || EntityFlyingFish.this.getRNG().nextInt(80) == 0) {
                BlockPos found = findSurfacePos();
                if (found != null) {
                    BlockPos glideTo = findGlideToPos(EntityFlyingFish.this.getPosition(), found);
                    if (glideTo != null) {
                        surface = found;
                        glide = glideTo;
                        EntityFlyingFish.this.glideIn = 0;
                        return true;
                    }
                }
            }
            return false;
        }

        private BlockPos findSurfacePos() {
            BlockPos fishPos = EntityFlyingFish.this.getPosition();
            for (int i = 0; i < 15; i++) {
                BlockPos offset = fishPos.add(EntityFlyingFish.this.rand.nextInt(16) - 8, 0, EntityFlyingFish.this.rand.nextInt(16) - 8);
                while (EntityFlyingFish.this.world.getBlockState(offset).getMaterial() == Material.WATER && offset.getY() < 256) {
                    offset = offset.up();
                }
                if (EntityFlyingFish.this.world.getBlockState(offset).getMaterial() != Material.WATER
                        && EntityFlyingFish.this.world.getBlockState(offset.down()).getMaterial() == Material.WATER
                        && EntityFlyingFish.this.canSeeBlock(offset)) {
                    return offset;
                }
            }
            return null;
        }

        private BlockPos findGlideToPos(BlockPos fishPos, BlockPos surfacePos) {
            Vec3d sub = new Vec3d(surfacePos.getX() - fishPos.getX(), 0, surfacePos.getZ() - fishPos.getZ()).normalize();
            double scale = EntityFlyingFish.this.rand.nextDouble() * 8 + 1;
            while (scale > 2) {
                Vec3d scaled = sub.scale(scale);
                BlockPos at = surfacePos.add((int) scaled.x, 0, (int) scaled.z);
                if (EntityFlyingFish.this.world.getBlockState(at).getMaterial() != Material.WATER
                        && EntityFlyingFish.this.world.getBlockState(at.down()).getMaterial() == Material.WATER
                        && EntityFlyingFish.this.canSeeBlock(at)) {
                    return at;
                }
                scale -= 1;
            }
            return null;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return surface != null && glide != null && (!EntityFlyingFish.this.onGround || EntityFlyingFish.this.isInWater());
        }

        @Override
        public void resetTask() {
            surface = null;
            glide = null;
            EntityFlyingFish.this.glideIn = EntityFlyingFish.this.rand.nextInt(75) + 150;
            EntityFlyingFish.this.setGliding(false);
        }

        @Override
        public void updateTask() {
            if (EntityFlyingFish.this.isInWater() && EntityFlyingFish.this.getDistanceSq(surface.getX() + 0.5D, surface.getY() + 1D, surface.getZ() + 0.5D) > 3F) {
                EntityFlyingFish.this.getNavigator().tryMoveToXYZ(surface.getX() + 0.5F, surface.getY() + 1F, surface.getZ() + 0.5F, 1.2D);
                if (EntityFlyingFish.this.isGliding()) {
                    resetTask();
                }
            } else {
                EntityFlyingFish.this.getNavigator().clearPath();
                Vec3d face = new Vec3d(glide.getX() + 0.5D, glide.getY() + 0.5D, glide.getZ() + 0.5D)
                        .subtract(new Vec3d(surface.getX() + 0.5D, surface.getY() + 0.5D, surface.getZ() + 0.5D));
                if (face.lengthVector() < 0.2F) {
                    float yaw = EntityFlyingFish.this.rotationYaw * ((float) Math.PI / 180F);
                    float pitch = EntityFlyingFish.this.rotationPitch * ((float) Math.PI / 180F);
                    face = new Vec3d(-MathHelper.sin(yaw) * MathHelper.cos(pitch), -MathHelper.sin(pitch), MathHelper.cos(yaw) * MathHelper.cos(pitch));
                }
                Vec3d target = face.normalize().scale(0.1D);
                double y = 0;
                if (!EntityFlyingFish.this.isGliding()) {
                    y = 0.4F + EntityFlyingFish.this.rand.nextFloat() * 0.2F;
                } else if (EntityFlyingFish.this.isGliding() && EntityFlyingFish.this.isInWater()) {
                    resetTask();
                    return;
                }
                EntityFlyingFish.this.motionX += target.x;
                EntityFlyingFish.this.motionY += y;
                EntityFlyingFish.this.motionZ += target.z;
                double d0 = MathHelper.sqrt(EntityFlyingFish.this.motionX * EntityFlyingFish.this.motionX + EntityFlyingFish.this.motionZ * EntityFlyingFish.this.motionZ);
                EntityFlyingFish.this.rotationPitch = (float) (-MathHelper.atan2(EntityFlyingFish.this.motionY, d0) * (180D / Math.PI));
                EntityFlyingFish.this.rotationYaw = (float) (MathHelper.atan2(EntityFlyingFish.this.motionZ, EntityFlyingFish.this.motionX) * (180D / Math.PI)) - 90F;
                EntityFlyingFish.this.setGliding(true);
            }
        }
    }

    @Override
    public int getWaterSearchRange() {
        return 14;
    }
}
