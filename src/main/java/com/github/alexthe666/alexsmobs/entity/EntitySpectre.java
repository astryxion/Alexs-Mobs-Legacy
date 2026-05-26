package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class EntitySpectre extends EntityAnimal {

    private static final DataParameter<Integer> CARDINAL_ORDINAL = EntityDataManager.createKey(EntitySpectre.class, DataSerializers.VARINT);
    public float birdPitch = 0;
    public float prevBirdPitch = 0;
    public Vec3d lurePos = null;

    public EntitySpectre(World world) {
        super(world);
        this.moveHelper = new MoveHelperController(this);
        this.setSize(1.4F, 1.2F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.spectreSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    public static boolean canSpectreSpawn(World worldIn, BlockPos pos, Random random) {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.SPECTRE_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.SPECTRE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.SPECTRE_HURT;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(64.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(1.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(CARDINAL_ORDINAL, EnumFacing.NORTH.getHorizontalIndex());
    }

    public int getCardinalInt() {
        return this.dataManager.get(CARDINAL_ORDINAL);
    }

    public void setCardinalInt(int command) {
        this.dataManager.set(CARDINAL_ORDINAL, command);
    }

    public EnumFacing getCardinalDirection() {
        return EnumFacing.getHorizontal(getCardinalInt());
    }

    public void setCardinalDirection(EnumFacing dir) {
        setCardinalInt(dir.getHorizontalIndex());
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(1, new TemptHeartGoal(this, 1.0D));
        this.tasks.addTask(2, new FlyGoal(this));
    }

    @Override
    public boolean isEntityInvulnerable(DamageSource source) {
        return (!source.isMagicDamage() && source != DamageSource.OUT_OF_WORLD) || super.isEntityInvulnerable(source);
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        this.rotationPitch = 0.0F;
        this.randomizeDirection();
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public float getBrightness() {
        return 1.0F;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected void updateFallState(double y, boolean onGroundIn, net.minecraft.block.state.IBlockState state, BlockPos pos) {
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.rotationYaw = -((float) MathHelper.atan2(this.motionX, this.motionZ)) * (180F / (float) Math.PI);
        this.renderYawOffset = this.rotationYaw;
        this.prevBirdPitch = this.birdPitch;
        this.noClip = true;
        this.setNoGravity(true);
        float f2 = (float) -((float) this.motionY * 0.5F * (double) (180F / (float) Math.PI));
        this.birdPitch = f2;
        if (this.getLeashHolder() != null && !(this.getLeashHolder() instanceof EntityLeashKnot)) {
            Entity entity = this.getLeashHolder();
            float f = this.getDistance(entity);
            if (f > 10) {
                double d0 = (this.posX - entity.posX) / (double) f;
                double d1 = (this.posY - entity.posY) / (double) f;
                double d2 = (this.posZ - entity.posZ) / (double) f;
                entity.motionX += Math.copySign(d0 * d0 * 0.4D, d0);
                entity.motionY += Math.copySign(d1 * d1 * 0.4D, d1);
                entity.motionZ += Math.copySign(d2 * d2 * 0.4D, d2);
            }
            entity.fallDistance = 0.0F;
            if (entity.motionY < 0.0D) {
                entity.motionY *= 0.7D;
            }
            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSneaking()) {
                this.clearLeashed(true, true);
            }
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return null;
    }

    @Override
    protected void updateLeashedState() {
        if (this.getLeashHolder() != null) {
            if (this.getLeashHolder().isRiding() || this.getLeashHolder() instanceof EntityLeashKnot) {
                super.updateLeashedState();
                return;
            }
            float f = this.getDistance(this.getLeashHolder());
            if (f > 30) {
                double lvt_3_1_ = (this.getLeashHolder().posX - this.posX) / (double) f;
                double lvt_5_1_ = (this.getLeashHolder().posY - this.posY) / (double) f;
                double lvt_7_1_ = (this.getLeashHolder().posZ - this.posZ) / (double) f;
                this.motionX += Math.copySign(lvt_3_1_ * lvt_3_1_ * 0.4D, lvt_3_1_);
                this.motionY += Math.copySign(lvt_5_1_ * lvt_5_1_ * 0.4D, lvt_5_1_);
                this.motionZ += Math.copySign(lvt_7_1_ * lvt_7_1_ * 0.4D, lvt_7_1_);
            }
        }
        if (ReflectionHelper.getPrivateValue(net.minecraft.entity.EntityLiving.class, this, "leashNBTTag", "field_110170_bx") != null) {
            try {
                ReflectionHelper.findMethod(net.minecraft.entity.EntityLiving.class, "recreateLeash", "func_110165_bF").invoke(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (this.getLeashHolder() != null) {
            if (!this.isEntityAlive() || !this.getLeashHolder().isEntityAlive()) {
                this.clearLeashed(true, true);
            }
        }
    }

    private void randomizeDirection() {
        this.setCardinalInt(2 + this.rand.nextInt(3));
    }

    private int getSurfaceY(BlockPos pos) {
        return this.world.getHeight(pos).getY();
    }

    static class MoveHelperController extends EntityMoveHelper {
        private final EntitySpectre parentEntity;

        MoveHelperController(EntitySpectre spectre) {
            super(spectre);
            this.parentEntity = spectre;
        }

        @Override
        public void onUpdateMoveHelper() {
            if (this.action == Action.MOVE_TO) {
                double d0 = this.posX - parentEntity.posX;
                double d1 = this.posY - parentEntity.posY;
                double d2 = this.posZ - parentEntity.posZ;
                double d5 = d0 * d0 + d1 * d1 + d2 * d2;
                if (d5 < 0.09D) {
                    this.action = Action.WAIT;
                    parentEntity.motionX *= 0.5D;
                    parentEntity.motionY *= 0.5D;
                    parentEntity.motionZ *= 0.5D;
                } else {
                    double scale = this.speed * 0.05D / Math.sqrt(d5);
                    parentEntity.motionX += d0 * scale;
                    parentEntity.motionY += d1 * scale;
                    parentEntity.motionZ += d2 * scale;
                    parentEntity.rotationYaw = -((float) MathHelper.atan2(parentEntity.motionX, parentEntity.motionZ)) * (180F / (float) Math.PI);
                    parentEntity.renderYawOffset = parentEntity.rotationYaw;
                }
            }
        }

        private boolean func_220673_a(Vec3d offset, int steps) {
            AxisAlignedBB axisalignedbb = this.parentEntity.getEntityBoundingBox();
            for (int i = 1; i < steps; ++i) {
                axisalignedbb = axisalignedbb.offset(offset.x, offset.y, offset.z);
                if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, axisalignedbb).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
    }

    private class FlyGoal extends EntityAIBase {
        private final EntitySpectre parentEntity;
        boolean island = false;
        float circlingTime = 0;
        float circleDistance = 14;
        boolean clockwise = false;
        private BlockPos target = null;
        private int islandCheckTime = 20;

        FlyGoal(EntitySpectre spectre) {
            this.parentEntity = spectre;
            this.setMutexBits(1);
        }

        @Override
        public boolean shouldExecute() {
            if (parentEntity.lurePos != null) {
                return false;
            }
            EntityMoveHelper movementcontroller = parentEntity.getMoveHelper();
            clockwise = parentEntity.getRNG().nextBoolean();
            circleDistance = 5 + parentEntity.getRNG().nextInt(10);
            if (!movementcontroller.isUpdating() || target == null) {
                target = island ? getIslandPos(parentEntity.getPosition()) : getBlockFromDirection();
                if (target != null) {
                    parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return parentEntity.lurePos == null;
        }

        @Override
        public void resetTask() {
            island = false;
            islandCheckTime = 0;
            circleDistance = 5 + parentEntity.getRNG().nextInt(10);
            circlingTime = 0;
            clockwise = parentEntity.getRNG().nextBoolean();
            target = null;
        }

        @Override
        public void updateTask() {
            if (islandCheckTime-- <= 0) {
                islandCheckTime = 20;
                if (circlingTime == 0) {
                    island = parentEntity.getSurfaceY(parentEntity.getPosition()) > 2;
                    if (island) {
                        parentEntity.randomizeDirection();
                    }
                }
            }
            if (island) {
                circlingTime++;
                if (circlingTime > 100) {
                    island = false;
                    islandCheckTime = 1200;
                }
            } else if (circlingTime > 0) {
                circlingTime--;
            }
            if (target == null) {
                target = island ? getIslandPos(parentEntity.getPosition()) : getBlockFromDirection();
            }
            if (!island) {
                parentEntity.rotationYaw = parentEntity.getCardinalDirection().getHorizontalAngle();
            }
            if (target != null) {
                parentEntity.getMoveHelper().setMoveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
                if (parentEntity.getDistanceSq(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) < 5.5F) {
                    target = null;
                }
            }
        }

        public BlockPos getBlockFromDirection() {
            float radius = 15;
            BlockPos forwards = parentEntity.getPosition().offset(parentEntity.getCardinalDirection(), (int) Math.ceil(radius));
            int height;
            if (parentEntity.getSurfaceY(forwards) < 15) {
                height = 70 + parentEntity.getRNG().nextInt(2);
            } else {
                height = parentEntity.getSurfaceY(forwards) + 10 + parentEntity.getRNG().nextInt(10);
            }
            return new BlockPos(forwards.getX(), height, forwards.getZ());
        }

        public BlockPos getIslandPos(BlockPos orbit) {
            float angle = (0.01745329251F * 3 * (clockwise ? -circlingTime : circlingTime));
            double extraX = circleDistance * MathHelper.sin(angle);
            double extraZ = circleDistance * MathHelper.cos(angle);
            int height = parentEntity.getSurfaceY(orbit);
            if (height < 3) {
                island = false;
                return getBlockFromDirection();
            }
            return new BlockPos(orbit.getX() + extraX, Math.min(height + 10, orbit.getY() + parentEntity.getRNG().nextInt(3) - parentEntity.getRNG().nextInt(1)), orbit.getZ() + extraZ);
        }
    }

    class TemptHeartGoal extends EntityAIBase {
        protected final EntitySpectre creature;
        private final double speed;
        protected EntityPlayer closestPlayer;
        private int delayTemptCounter;

        TemptHeartGoal(EntitySpectre creature, double speed) {
            this.creature = creature;
            this.speed = speed;
            this.setMutexBits(3);
        }

        @Override
        public boolean shouldExecute() {
            if (this.delayTemptCounter > 0) {
                --this.delayTemptCounter;
                return false;
            } else {
                this.closestPlayer = this.creature.world.getClosestPlayerToEntity(this.creature, 64.0D);
                if (this.closestPlayer == null || this.creature.getLeashHolder() == closestPlayer) {
                    return false;
                } else {
                    return this.isTempting(this.closestPlayer.getHeldItemMainhand()) || this.isTempting(this.closestPlayer.getHeldItemOffhand());
                }
            }
        }

        protected boolean isTempting(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() == AMItemRegistry.SOUL_HEART;
        }

        @Override
        public boolean shouldContinueExecuting() {
            return this.shouldExecute();
        }

        @Override
        public void startExecuting() {
            creature.lurePos = new Vec3d(closestPlayer.posX, closestPlayer.posY, closestPlayer.posZ);
        }

        @Override
        public void resetTask() {
            this.closestPlayer = null;
            this.delayTemptCounter = 100;
            creature.lurePos = null;
        }

        @Override
        public void updateTask() {
            this.creature.getLookHelper().setLookPositionWithEntity(this.closestPlayer, (float) (this.creature.getHorizontalFaceSpeed() + 20), (float) this.creature.getVerticalFaceSpeed());
            if (this.creature.getDistanceSq(this.closestPlayer) < 6.25D) {
                this.creature.getNavigator().clearPath();
            } else {
                this.creature.getMoveHelper().setMoveTo(this.closestPlayer.posX, this.closestPlayer.posY + this.closestPlayer.getEyeHeight(), this.closestPlayer.posZ, this.speed);
            }
        }
    }
}
