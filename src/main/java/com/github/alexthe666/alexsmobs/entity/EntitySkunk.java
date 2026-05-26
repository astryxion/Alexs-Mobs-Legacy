package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.block.AMBlockRegistry;
import com.github.alexthe666.alexsmobs.block.BlockSkunkSpray;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.AnimalAIWanderRanged;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.pathfinding.Path;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class EntitySkunk extends EntityAnimal {

    public float prevSprayProgress;
    public float sprayProgress;
    private int harassedTime;
    private int sprayCooldown;
    private Vec3d sprayAt;
    private static final DataParameter<Integer> SPRAY_TIME = EntityDataManager.createKey(EntitySkunk.class, DataSerializers.VARINT);
    private static final DataParameter<Float> SPRAY_YAW = EntityDataManager.createKey(EntitySkunk.class, DataSerializers.FLOAT);

    public EntitySkunk(World world) {
        super(world);
        this.setSize(0.85F, 0.65F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SPRAY_YAW, 0F);
        this.dataManager.register(SPRAY_TIME, 0);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new SprayGoal());
        this.tasks.addTask(1, new EntityAIPanic(this, 1.5D) {
            @Override
            public void updateTask() {
                super.updateTask();
                EntitySkunk.this.harassedTime += 10;
            }
        });
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.APPLE, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return AMTagRegistry.itemInTag(AMTagRegistry.SKUNK_BREEDABLES, stack.getItem());
            }
        });
        this.tasks.addTask(3, new SkunkAvoidGoal());
        this.tasks.addTask(4, new AnimalAIWanderRanged(this, 60, 1.0D, 10, 7));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.skunkSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return AMTagRegistry.itemInTag(AMTagRegistry.SKUNK_BREEDABLES, stack.getItem());
    }

    public float getSprayYaw() {
        return this.dataManager.get(SPRAY_YAW);
    }

    public void setSprayYaw(float yaw) {
        this.dataManager.set(SPRAY_YAW, yaw);
    }

    public int getSprayTime() {
        return this.dataManager.get(SPRAY_TIME);
    }

    public void setSprayTime(int time) {
        this.dataManager.set(SPRAY_TIME, time);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SKUNK_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SKUNK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SKUNK_HURT;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.prevSprayProgress = sprayProgress;
        if (this.getSprayTime() > 0) {
            if (this.sprayProgress < 5F) {
                this.sprayProgress++;
            }
            this.setSprayTime(this.getSprayTime() - 1);
            if (this.getSprayTime() == 0) {
                spawnLingeringCloud();
            } else if (this.getSprayTime() % 6 == 0) {
                this.playSound(AMSoundRegistry.SKUNK_SPRAY, this.getSoundVolume(), this.getSoundPitch());
            }
            this.renderYawOffset = this.rotationYaw;
            this.rotationYaw = approachRotation(this.getSprayYaw(), this.rotationYaw + 10, 15F);
        }
        if (this.getSprayTime() <= 0 && this.sprayProgress > 0F) {
            this.sprayProgress--;
        }
        if (!this.world.isRemote) {
            if (harassedTime > 200 && sprayCooldown == 0 && !this.isChild()) {
                harassedTime = 0;
                sprayCooldown = 200 + rand.nextInt(200);
                this.setSprayTime(60 + rand.nextInt(60));
            }
            if (harassedTime > 0) {
                harassedTime--;
            }
            if (sprayCooldown > 0) {
                sprayCooldown--;
            }
            Entity lastHurt = this.getRevengeTarget();
            if (lastHurt != null) {
                this.sprayAt = lastHurt.getPositionVector();
            }
        }
    }

    private void spawnLingeringCloud() {
        Collection<PotionEffect> collection = this.getActivePotionEffects();
        if (!collection.isEmpty()) {
            final float fartDistance = 2.5F;
            Vec3d modelBack = rotateLocalOffset(0, 0.4F, -fartDistance);
            Vec3d fartAt = this.getPositionVector().add(modelBack);
            EntityAreaEffectCloud areaeffectcloud = new EntityAreaEffectCloud(this.world, fartAt.x, fartAt.y, fartAt.z);
            areaeffectcloud.setRadius(2.5F);
            areaeffectcloud.setRadiusOnUse(-0.25F);
            areaeffectcloud.setWaitTime(20);
            areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float) areaeffectcloud.getDuration());
            for (PotionEffect effect : collection) {
                areaeffectcloud.addEffect(new PotionEffect(effect));
            }
            this.world.spawnEntity(areaeffectcloud);
        }
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == 48) {
            Vec3d modelBack = rotateLocalOffset(0, 0.4F, -0.4F);
            Vec3d particleFrom = this.getPositionVector().add(modelBack);
            final float scale = rand.nextFloat() * 0.5F + 1F;
            Vec3d particleTo = modelBack.scale(scale);
            for (int i = 0; i < 3; ++i) {
                final double d0 = this.rand.nextGaussian() * 0.1D;
                final double d1 = this.rand.nextGaussian() * 0.1D;
                final double d2 = this.rand.nextGaussian() * 0.1D;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.SMELLY, particleFrom.x, particleFrom.y, particleFrom.z, particleTo.x + d0, particleTo.y - 0.4F + d1, particleTo.z + d2);
            }
        } else {
            super.handleStatusUpdate(id);
        }
    }

    private float approachRotation(float current, float target, float max) {
        float f = MathHelper.wrapDegrees(target - current);
        if (f > max) {
            f = max;
        }
        if (f < -max) {
            f = -max;
        }
        return MathHelper.wrapDegrees(current + f);
    }

    private Vec3d rotateLocalOffset(double x, double y, double z) {
        float pitch = -this.rotationPitch * 0.017453292F;
        float yaw = -this.rotationYaw * 0.017453292F;
        float cosP = MathHelper.cos(pitch);
        float sinP = MathHelper.sin(pitch);
        double y1 = y * cosP - z * sinP;
        double z1 = y * sinP + z * cosP;
        float cosY = MathHelper.cos(yaw);
        float sinY = MathHelper.sin(yaw);
        double x2 = x * cosY + z1 * sinY;
        double z2 = z1 * cosY - x * sinY;
        return new Vec3d(x2, y1, z2);
    }

    @Nullable
    @Override
    public EntitySkunk createChild(EntityAgeable ageable) {
        return new EntitySkunk(this.world);
    }

    private class SprayGoal extends EntityAIBase {
        private int actualSprayTime = 0;

        SprayGoal() {
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            return EntitySkunk.this.getSprayTime() > 0;
        }

        @Override
        public void resetTask() {
            actualSprayTime = 0;
        }

        @Override
        public void updateTask() {
            EntitySkunk.this.getNavigator().clearPath();
            Vec3d target = getSprayAt();
            final double d0 = EntitySkunk.this.posX - target.x;
            final double d2 = EntitySkunk.this.posZ - target.z;
            final float f = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
            EntitySkunk.this.setSprayYaw(f);
            if (EntitySkunk.this.sprayProgress >= 5F) {
                if (!EntitySkunk.this.world.isRemote) {
                    EntitySkunk.this.world.setEntityState(EntitySkunk.this, (byte) 48);
                }
                if (actualSprayTime > 10 && EntitySkunk.this.rand.nextInt(2) == 0) {
                    Vec3d skunkPos = new Vec3d(EntitySkunk.this.posX, EntitySkunk.this.posY + EntitySkunk.this.getEyeHeight(), EntitySkunk.this.posZ);
                    final float xAdd = EntitySkunk.this.rand.nextFloat() * 20 - 10;
                    final float yAdd = EntitySkunk.this.rand.nextFloat() * 20 - 10;
                    final float maxSprayDist = 5F;
                    Vec3d modelBack = rotateSprayVector(0, 0F, -maxSprayDist, xAdd - EntitySkunk.this.rotationPitch, yAdd - EntitySkunk.this.rotationYaw);
                    Vec3d end = skunkPos.add(modelBack);
                    RayTraceResult hitResult = EntitySkunk.this.world.rayTraceBlocks(skunkPos, end, false, true, false);
                    if (hitResult != null && hitResult.typeOfHit != RayTraceResult.Type.MISS) {
                        BlockPos pos;
                        EnumFacing dir;
                        if (hitResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                            pos = hitResult.getBlockPos().offset(hitResult.sideHit);
                            dir = hitResult.sideHit.getOpposite();
                        } else {
                            pos = new BlockPos(hitResult.hitVec);
                            dir = EnumFacing.UP;
                        }
                        net.minecraft.block.state.IBlockState currentState = EntitySkunk.this.world.getBlockState(pos);
                        net.minecraft.block.state.IBlockState sprayState = BlockSkunkSpray.getStateForPlacement(currentState, EntitySkunk.this.world, pos, dir);
                        if ((currentState.getMaterial().isReplaceable() || currentState.getBlock().isAir(currentState, EntitySkunk.this.world, pos) || currentState.getBlock() == AMBlockRegistry.SKUNK_SPRAY)
                                && sprayState != null && sprayState.getBlock() == AMBlockRegistry.SKUNK_SPRAY) {
                            BlockSkunkSpray.applyPlacementState(EntitySkunk.this.world, pos, sprayState);
                        }
                        double sprayDist = hitResult.hitVec.subtract(skunkPos).lengthVector() / maxSprayDist;
                        AxisAlignedBB poisonBox = new AxisAlignedBB(skunkPos, skunkPos.add(modelBack.scale(sprayDist)).add(new Vec3d(0, 1.5D, 0))).grow(1F);
                        Collection<PotionEffect> collection = EntitySkunk.this.getActivePotionEffects();
                        for (EntityLivingBase entity : EntitySkunk.this.world.getEntitiesWithinAABB(EntityLivingBase.class, poisonBox)) {
                            if (!(entity instanceof EntitySkunk)) {
                                entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 300));
                                if (entity instanceof EntityPlayerMP) {
                                    AMAdvancementTriggerRegistry.SKUNK_SPRAY.trigger((EntityPlayerMP) entity);
                                }
                                for (PotionEffect effect : collection) {
                                    entity.addPotionEffect(new PotionEffect(effect));
                                }
                            }
                        }
                    }
                }
                actualSprayTime++;
            }
        }

        private Vec3d rotateSprayVector(double x, double y, double z, float pitchAdd, float yawAdd) {
            float pitch = -(EntitySkunk.this.rotationPitch + pitchAdd) * 0.017453292F;
            float yaw = -(EntitySkunk.this.rotationYaw + yawAdd) * 0.017453292F;
            float cosP = MathHelper.cos(pitch);
            float sinP = MathHelper.sin(pitch);
            double y1 = y * cosP - z * sinP;
            double z1 = y * sinP + z * cosP;
            float cosY = MathHelper.cos(yaw);
            float sinY = MathHelper.sin(yaw);
            double x2 = x * cosY + z1 * sinY;
            double z2 = z1 * cosY - x * sinY;
            return new Vec3d(x2, y1, z2);
        }

        private Vec3d getSprayAt() {
            Entity last = EntitySkunk.this.getRevengeTarget();
            if (EntitySkunk.this.sprayAt != null) {
                return EntitySkunk.this.sprayAt;
            } else if (last != null) {
                return last.getPositionVector();
            } else {
                return EntitySkunk.this.getPositionVector().add(EntitySkunk.this.rotateLocalOffset(0, 0.4F, -1));
            }
        }
    }

    private class SkunkAvoidGoal extends EntityAIBase {
        private final Predicate<EntityLivingBase> fearPredicate = AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.SKUNK_FEARS);
        private EntityLivingBase avoidTarget;
        private Path fleePath;
        private double fleeSpeed;

        SkunkAvoidGoal() {
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (EntitySkunk.this.getSprayTime() > 0) {
                return false;
            }
            List<EntityLivingBase> list = EntitySkunk.this.world.getEntitiesWithinAABB(EntityLivingBase.class,
                    EntitySkunk.this.getEntityBoundingBox().grow(10.0D), entity -> {
                        if (entity == null || !entity.isEntityAlive()) {
                            return false;
                        }
                        if (entity instanceof EntityPlayer) {
                            EntityPlayer player = (EntityPlayer) entity;
                            return !player.capabilities.isCreativeMode && !player.isSpectator();
                        }
                        return fearPredicate.apply(entity);
                    });
            if (list.isEmpty()) {
                return false;
            }
            list.sort(Comparator.comparingDouble(EntitySkunk.this::getDistanceSq));
            this.avoidTarget = list.get(0);
            Vec3d vec = RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntitySkunk.this, 16, 7,
                    new Vec3d(this.avoidTarget.posX, this.avoidTarget.posY, this.avoidTarget.posZ));
            if (vec == null) {
                return false;
            }
            BlockPos fleePos = new BlockPos(vec.x, vec.y, vec.z);
            if (!EntitySkunk.this.world.isBlockLoaded(fleePos)) {
                return false;
            }
            this.fleePath = EntitySkunk.this.getNavigator().getPathToPos(fleePos);
            if (this.fleePath == null) {
                return false;
            }
            this.fleeSpeed = EntitySkunk.this.getDistance(this.avoidTarget) > 7.0F ? 1.3D : 1.1D;
            return true;
        }

        @Override
        public void startExecuting() {
            EntitySkunk.this.getNavigator().setPath(this.fleePath, this.fleeSpeed);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.avoidTarget != null && this.avoidTarget.isEntityAlive() && EntitySkunk.this.getSprayTime() <= 0
                    && EntitySkunk.this.getDistance(this.avoidTarget) < 16.0F && !EntitySkunk.this.getNavigator().noPath();
        }

        @Override
        public void resetTask() {
            this.avoidTarget = null;
            this.fleePath = null;
            EntitySkunk.this.getNavigator().clearPath();
        }

        @Override
        public void updateTask() {
            if (this.avoidTarget != null) {
                EntitySkunk.this.sprayAt = this.avoidTarget.getPositionVector();
                EntitySkunk.this.harassedTime += 4;
            }
        }
    }
}
