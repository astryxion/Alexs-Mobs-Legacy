package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.misc.AMPointOfInterestRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class EntitySunbird extends EntityAnimal {

    public static final Predicate<EntityLivingBase> SCORCH_PRED = AMEntityRegistry.buildPredicateFromTag(AMTagRegistry.SUNBIRD_SCORCH_TARGETS);
    public float birdPitch = 0;
    public float prevBirdPitch = 0;
    private int beaconSearchCooldown = 50;
    private BlockPos beaconPos = null;
    private boolean orbitClockwise = false;
    private float beaconOrbitAngle = 0.0F;

    public EntitySunbird(World worldIn) {
        super(worldIn);
        this.moveHelper = new MoveHelperController(this);
        this.orbitClockwise = new Random().nextBoolean();
        this.setSize(1.2F, 1.0F);
    }

    public static boolean canSunbirdSpawn(World worldIn, BlockPos pos, Random random) {
        return true;
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.sunbirdSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SUNBIRD_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SUNBIRD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SUNBIRD_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(3, new RandomFlyGoal(this));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 32.0F));
        this.tasks.addTask(5, new EntityAILookIdle(this));
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    public boolean isFlying() {
        return true;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        boolean prev = super.attackEntityFrom(source, amount);
        if (prev && source.getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase hurter = (EntityLivingBase) source.getTrueSource();
            if (hurter.isPotionActive(AMEffectRegistry.SUNBIRD_BLESSING)) {
                hurter.removePotionEffect(AMEffectRegistry.SUNBIRD_BLESSING);
            }
            hurter.addPotionEffect(new PotionEffect(AMEffectRegistry.SUNBIRD_CURSE, 600, 0));
        }
        return prev;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isInWater()) {
            this.moveRelative(0.02F, strafe, forward, 0.98F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.8D;
        } else if (this.isInLava()) {
            this.moveRelative(0.02F, strafe, forward, 0.98F);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.5D;
            this.motionY *= 0.5D;
            this.motionZ *= 0.5D;
        } else {
            this.updateLimbSwing(true);
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.91D;
            this.motionY *= 0.91D;
            this.motionZ *= 0.91D;
        }
        this.updateLimbSwing(false);
    }

    private void updateLimbSwing(boolean active) {
        if (active) {
            double d0 = this.posX - this.prevPosX;
            double d1 = this.posY - this.prevPosY;
            double d2 = this.posZ - this.prevPosZ;
            float f = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2) * 4.0F;
            if (f > 1.0F) {
                f = 1.0F;
            }
            this.limbSwingAmount += (f - this.limbSwingAmount) * 0.4F;
            this.limbSwing += this.limbSwingAmount;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.setNoGravity(true);
        if (!this.world.isRemote && this.getAttackTarget() == null) {
            double horizontalMotion = this.motionX * this.motionX + this.motionZ * this.motionZ;
            if (horizontalMotion > 1.0E-4D) {
                this.rotationYaw = -((float) MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float) Math.PI);
                this.renderYawOffset = this.rotationYaw;
            }
        }
        this.prevBirdPitch = this.birdPitch;
        float f2 = (float) -((float) this.motionY * (double) (180F / (float) Math.PI));
        this.birdPitch = f2;

        if (world.isRemote) {
            float radius = 0.35F + rand.nextFloat() * 1.85F;
            float angle = (0.01745329251F * ((rand.nextBoolean() ? -85F : 85F) + this.renderYawOffset));
            float angleMotion = (0.01745329251F * this.renderYawOffset);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            double extraXMotion = -0.2F * MathHelper.sin((float) (Math.PI + angleMotion));
            double extraZMotion = -0.2F * MathHelper.cos(angleMotion);
            double yRandom = 0.2F + rand.nextFloat() * 0.3F;
            this.world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, this.posX + extraX, this.posY + yRandom, this.posZ + extraZ, extraXMotion, 0D, extraZMotion);
        } else {
            if (this.ticksExisted % 100 == 0) {
                List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, this.getScorchArea(), SCORCH_PRED);
                for (EntityLivingBase e : list) {
                    e.setFire(4);
                    if (isPhantomEntity(e)) {
                        e.addPotionEffect(new PotionEffect(AMEffectRegistry.SUNBIRD_CURSE, 200, 0));
                    }
                }
                List<EntityPlayer> playerList = this.world.getEntitiesWithinAABB(EntityPlayer.class, this.getScorchArea(), Predicates.alwaysTrue());
                for (EntityPlayer e : playerList) {
                    if (!e.isPotionActive(AMEffectRegistry.SUNBIRD_BLESSING) && !e.isPotionActive(AMEffectRegistry.SUNBIRD_CURSE)) {
                        e.addPotionEffect(new PotionEffect(AMEffectRegistry.SUNBIRD_BLESSING, 600, 0));
                    }
                }
            }
            if (beaconSearchCooldown > 0) {
                beaconSearchCooldown--;
            }
            if (beaconSearchCooldown <= 0) {
                beaconSearchCooldown = 100 + rand.nextInt(200);
                BlockPos closest = AMPointOfInterestRegistry.findClosest(world, this.getPosition(), 64, AMPointOfInterestRegistry::matchesBeacon);
                if (closest != null && isValidBeacon(closest)) {
                    beaconPos = closest;
                }
                if (beaconPos != null && !isValidBeacon(beaconPos) && ticksExisted > 40) {
                    this.beaconPos = null;
                }
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        BlockPos blockpos = this.beaconPos;
        if (blockpos != null) {
            compound.setInteger("BeaconPosX", blockpos.getX());
            compound.setInteger("BeaconPosY", blockpos.getY());
            compound.setInteger("BeaconPosZ", blockpos.getZ());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("BeaconPosX")) {
            int i = compound.getInteger("BeaconPosX");
            int j = compound.getInteger("BeaconPosY");
            int k = compound.getInteger("BeaconPosZ");
            this.beaconPos = new BlockPos(i, j, k);
        } else {
            this.beaconPos = null;
        }
    }

    private AxisAlignedBB getScorchArea() {
        return this.getEntityBoundingBox().grow(15, 32, 15);
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    public boolean isTargetBlocked(Vec3d target) {
        Vec3d start = new Vec3d(this.posX, this.getPositionEyes(1.0F).y, this.posZ);
        RayTraceResult result = this.world.rayTraceBlocks(start, target, false, true, false);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private static boolean isPhantomEntity(Entity e) {
        String id = EntityList.getEntityString(e);
        return id != null && id.toLowerCase().contains("phantom");
    }

    private boolean isValidBeacon(BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        return te instanceof TileEntityBeacon && ((TileEntityBeacon) te).getLevels() > 0;
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntitySunbird parentEntity;

        MoveHelperController(EntitySunbird sunbird) {
            super(sunbird);
            this.parentEntity = sunbird;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - parentEntity.posX;
                double d1 = this.posY - parentEntity.posY;
                double d2 = this.posZ - parentEntity.posZ;
                double lenSq = d0 * d0 + d1 * d1 + d2 * d2;
                if (lenSq < 0.09D) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    double len = Math.sqrt(lenSq);
                    double scale = this.speed * 0.05D / len;
                    parentEntity.motionX += d0 * scale;
                    parentEntity.motionY += d1 * scale;
                    parentEntity.motionZ += d2 * scale;
                    if (parentEntity.getAttackTarget() != null) {
                        EntityLivingBase target = parentEntity.getAttackTarget();
                        double d2t = target.posX - parentEntity.posX;
                        double d1t = target.posZ - parentEntity.posZ;
                        parentEntity.rotationYaw = -((float) MathHelper.atan2(d2t, d1t)) * (180F / (float) Math.PI);
                        parentEntity.renderYawOffset = parentEntity.rotationYaw;
                    }
                }
            }
        }
    }

    static class RandomFlyGoal extends EntityAIBase {
        private final EntitySunbird parentEntity;
        private BlockPos target = null;

        RandomFlyGoal(EntitySunbird sunbird) {
            this.parentEntity = sunbird;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            EntityMoveHelper movementcontroller = this.parentEntity.getMoveHelper();
            if (!movementcontroller.isUpdating() || target == null) {
                if (parentEntity.beaconPos != null) {
                    target = getBlockInViewBeacon(parentEntity.beaconPos, 5 + parentEntity.rand.nextInt(1));
                } else {
                    target = getBlockInViewSunbird();
                }
                if (target != null) {
                    this.parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, parentEntity.beaconPos != null ? 0.8D : 1.0D);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return target != null && parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > 2.4D
                    && !parentEntity.collidedHorizontally;
        }

        @Override
        public void resetTask() {
            target = null;
        }

        @Override
        public void updateTask() {
            if (target == null) {
                if (parentEntity.beaconPos != null) {
                    target = getBlockInViewBeacon(parentEntity.beaconPos, 5 + parentEntity.rand.nextInt(1));
                } else {
                    target = getBlockInViewSunbird();
                }
            }
            if (parentEntity.beaconPos != null && parentEntity.rand.nextInt(100) == 0) {
                parentEntity.orbitClockwise = parentEntity.rand.nextBoolean();
            }
            if (target != null) {
                this.parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, parentEntity.beaconPos != null ? 0.8D : 1.0D);
                if (parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) < 2.5F) {
                    target = null;
                }
            }
        }

        private BlockPos getBlockInViewBeacon(BlockPos orbitPos, float gatheringCircleDist) {
            float angle = parentEntity.beaconOrbitAngle * 0.01745329251F;
            double extraX = gatheringCircleDist * MathHelper.sin(angle);
            double extraZ = gatheringCircleDist * MathHelper.cos(angle);
            if (orbitPos != null) {
                BlockPos pos = new BlockPos(orbitPos.getX() + extraX, orbitPos.getY() + parentEntity.rand.nextInt(2) + 2, orbitPos.getZ() + extraZ);
                if (parentEntity.world.isAirBlock(pos)) {
                    parentEntity.beaconOrbitAngle += parentEntity.orbitClockwise ? -9.0F : 9.0F;
                    return pos;
                }
            }
            return null;
        }

        public BlockPos getBlockInViewSunbird() {
            float radius = 0.75F * (0.7F * 6) * -3 - parentEntity.getRNG().nextInt(24);
            float neg = parentEntity.getRNG().nextBoolean() ? 1 : -1;
            float renderYawOffset = parentEntity.renderYawOffset;
            float angle = (0.01745329251F * renderYawOffset) + 3.15F + (parentEntity.getRNG().nextFloat() * neg);
            double extraX = radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = radius * MathHelper.cos(angle);
            BlockPos radialPos = new BlockPos(parentEntity.posX + extraX, 0, parentEntity.posZ + extraZ);
            BlockPos ground = parentEntity.world.getTopSolidOrLiquidBlock(radialPos);
            int distFromGround = (int) parentEntity.posY - ground.getY();
            int flightHeight = Math.max(ground.getY(), 230 + parentEntity.getRNG().nextInt(40)) - ground.getY();
            BlockPos newPos = radialPos.up(distFromGround > 16 ? flightHeight : (int) parentEntity.posY + parentEntity.getRNG().nextInt(16) + 1);
            if (!parentEntity.isTargetBlocked(new Vec3d(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D))
                    && parentEntity.getDistanceSq(newPos.getX() + 0.5D, newPos.getY() + 0.5D, newPos.getZ() + 0.5D) > 6) {
                return newPos;
            }
            return null;
        }
    }
}
