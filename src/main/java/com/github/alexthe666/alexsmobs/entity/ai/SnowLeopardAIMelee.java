package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySnowLeopard;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class SnowLeopardAIMelee extends EntityAIBase {

    private final EntitySnowLeopard leopard;
    private EntityLivingBase target;
    private boolean secondPartOfLeap = false;
    private Vec3d leapPos = null;
    private boolean stalk = false;

    public SnowLeopardAIMelee(EntitySnowLeopard snowLeopard) {
        this.leopard = snowLeopard;
        this.setMutexBits(3);
    }

    @Nullable
    private static BlockPos func_226343_a_(Random p_226343_0_, int p_226343_1_, int p_226343_2_, int p_226343_3_, @Nullable Vec3d p_226343_4_, double p_226343_5_) {
        if (p_226343_4_ != null && p_226343_5_ < 3.141592653589793D) {
            double lvt_7_2_ = MathHelper.atan2(p_226343_4_.z, p_226343_4_.x) - 1.5707963705062866D;
            double lvt_9_2_ = lvt_7_2_ + (double) (2.0F * p_226343_0_.nextFloat() - 1.0F) * p_226343_5_;
            double lvt_11_1_ = Math.sqrt(p_226343_0_.nextDouble()) * (double) MathHelper.SQRT_2 * (double) p_226343_1_;
            double lvt_13_1_ = -lvt_11_1_ * Math.sin(lvt_9_2_);
            double lvt_15_1_ = lvt_11_1_ * Math.cos(lvt_9_2_);
            if (Math.abs(lvt_13_1_) <= (double) p_226343_1_ && Math.abs(lvt_15_1_) <= (double) p_226343_1_) {
                int lvt_17_1_ = p_226343_0_.nextInt(2 * p_226343_2_ + 1) - p_226343_2_ + p_226343_3_;
                return new BlockPos(lvt_13_1_, lvt_17_1_, lvt_15_1_);
            } else {
                return null;
            }
        } else {
            int lvt_7_1_ = p_226343_0_.nextInt(2 * p_226343_1_ + 1) - p_226343_1_;
            int lvt_8_1_ = p_226343_0_.nextInt(2 * p_226343_2_ + 1) - p_226343_2_ + p_226343_3_;
            int lvt_9_1_ = p_226343_0_.nextInt(2 * p_226343_1_ + 1) - p_226343_1_;
            return new BlockPos(lvt_7_1_, lvt_8_1_, lvt_9_1_);
        }
    }

    static BlockPos func_226342_a_(BlockPos p_226342_0_, int p_226342_1_, int p_226342_2_, Predicate<BlockPos> p_226342_3_) {
        if (p_226342_1_ < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + p_226342_1_ + ", expected >= 0");
        } else if (!p_226342_3_.test(p_226342_0_)) {
            return p_226342_0_;
        } else {
            BlockPos lvt_4_1_;
            for (lvt_4_1_ = p_226342_0_.up(); lvt_4_1_.getY() < p_226342_2_ && p_226342_3_.test(lvt_4_1_); lvt_4_1_ = lvt_4_1_.up()) {
            }

            BlockPos lvt_5_1_;
            BlockPos lvt_6_1_;
            for (lvt_5_1_ = lvt_4_1_; lvt_5_1_.getY() < p_226342_2_ && lvt_5_1_.getY() - lvt_4_1_.getY() < p_226342_1_; lvt_5_1_ = lvt_6_1_) {
                lvt_6_1_ = lvt_5_1_.up();
                if (p_226342_3_.test(lvt_6_1_)) {
                    break;
                }
            }

            return lvt_5_1_;
        }
    }

    @Override
    public boolean shouldExecute() {
        return leopard.getAttackTarget() != null && !leopard.isSleeping() && !leopard.isSitting() && (leopard.getAttackTarget().isEntityAlive() || leopard.getAttackTarget() instanceof EntityPlayer) && !leopard.isChild();
    }

    @Override
    public void startExecuting() {
        target = leopard.getAttackTarget();
        if (target instanceof EntityPlayer && leopard.getRevengeTarget() != null && leopard.getRevengeTarget() == target) {
            stalk = this.leopard.getDistance(target) > 10F;
        } else {
            stalk = this.leopard.getDistance(target) > 4F;
        }
        secondPartOfLeap = false;
    }

    @Override
    public void resetTask() {
        secondPartOfLeap = false;
        stalk = false;
        leapPos = null;
        this.leopard.setTackling(false);
        this.leopard.setSlSneaking(false);
    }

    @Override
    public void updateTask() {
        if (stalk) {
            if (secondPartOfLeap) {
                leopard.faceEntity(target, 180F, 10F);
                leopard.getLookHelper().setLookPositionWithEntity(target, 180F, 10F);
                leopard.renderYawOffset = leopard.rotationYaw;
                if (leopard.onGround) {
                    this.leopard.setSlSneaking(false);
                    this.leopard.setTackling(true);
                    Vec3d vector3d = new Vec3d(this.leopard.motionX, this.leopard.motionY, this.leopard.motionZ);
                    Vec3d vector3d1 = new Vec3d(this.target.posX - this.leopard.posX, 0.0D, this.target.posZ - this.leopard.posZ);
                    if (vector3d1.lengthSquared() > 1.0E-7D) {
                        vector3d1 = vector3d1.normalize().scale(0.9D).add(vector3d.scale(0.8D));
                    }
                    this.leopard.motionX = vector3d1.x;
                    this.leopard.motionY = vector3d1.y + 0.6F;
                    this.leopard.motionZ = vector3d1.z;
                }
                if (this.leopard.getDistance(target) < 3F && this.leopard.canEntityBeSeen(target)) {
                    target.attackEntityFrom(DamageSource.causeMobDamage(leopard),
                            (float) (leopard.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * 2.5F));
                    this.stalk = false;
                    this.secondPartOfLeap = false;
                }
            } else {
                if (leapPos == null || target.getDistanceSq(leapPos.x, leapPos.y, leapPos.z) > 250) {
                    Vec3d vector3d1 = calculateFarPoint(50);
                    if (vector3d1 != null) {
                        leapPos = vector3d1;
                    }
                } else {
                    this.leopard.setSlSneaking(true);
                    this.leopard.getNavigator().tryMoveToXYZ(leapPos.x, leapPos.y, leapPos.z, 1D);
                    if (this.leopard.getDistanceSq(leapPos.x, leapPos.y, leapPos.z) < 9) {
                        if (this.leopard.canEntityBeSeen(target)) {
                            secondPartOfLeap = true;
                            this.leopard.getNavigator().clearPath();
                        }
                    }
                }
            }
        } else {
            this.leopard.setSlSneaking(false);
            this.leopard.getNavigator().tryMoveToEntityLiving(target, 1D);
            if (this.leopard.getDistance(target) < 3F) {
                if (leopard.getAnimation() == IAnimatedEntity.NO_ANIMATION) {
                    leopard.setAnimation(leopard.getRNG().nextBoolean() ? EntitySnowLeopard.ANIMATION_ATTACK_R : EntitySnowLeopard.ANIMATION_ATTACK_L);
                } else if (this.leopard.getAnimationTick() == 5) {
                    leopard.attackEntityAsMob(target);
                }
            }
        }
    }

    private Vec3d calculateFarPoint(double dist) {
        Vec3d highest = null;
        for (int i = 0; i < 10; i++) {
            Vec3d vector3d1 = calculateVantagePoint(target, 8, 3, 1, new Vec3d(target.posX - leopard.posX, target.posY - leopard.posY, target.posZ - leopard.posZ), false, 1.5707963705062866D, leopard::getBlockPathWeight, false, 0, 0, true);
            if (vector3d1 != null && target.getDistanceSq(vector3d1.x, vector3d1.y, vector3d1.z) > dist && (highest == null || highest.y < vector3d1.y)) {
                highest = vector3d1;
            }
        }
        return highest;
    }

    @Nullable
    private Vec3d calculateVantagePoint(EntityLivingBase creature, int xz, int y, int p_226339_3_, @Nullable Vec3d p_226339_4_, boolean p_226339_5_, double p_226339_6_, ToDoubleFunction<BlockPos> p_226339_8_, boolean p_226339_9_, int p_226339_10_, int p_226339_11_, boolean p_226339_12_) {
        PathNavigate lvt_13_1_ = leopard.getNavigator();
        Random lvt_14_1_ = creature.getRNG();
        boolean lvt_15_2_;
        if (leopard.hasHome()) {
            lvt_15_2_ = leopard.isWithinHomeDistanceFromPosition(creature.getPosition());
        } else {
            lvt_15_2_ = false;
        }

        boolean lvt_16_1_ = false;
        double lvt_17_1_ = -1.0D / 0.0;
        BlockPos lvt_19_1_ = creature.getPosition();
        WalkNodeProcessor walkNodeProcessor = new WalkNodeProcessor();

        for (int lvt_20_1_ = 0; lvt_20_1_ < 10; ++lvt_20_1_) {
            BlockPos lvt_21_1_ = func_226343_a_(lvt_14_1_, xz, y, p_226339_3_, p_226339_4_, p_226339_6_);
            if (lvt_21_1_ != null) {
                int lvt_22_1_ = lvt_21_1_.getX();
                int lvt_23_1_ = lvt_21_1_.getY();
                int lvt_24_1_ = lvt_21_1_.getZ();
                BlockPos lvt_25_2_;
                if (leopard.hasHome() && xz > 1) {
                    lvt_25_2_ = leopard.getHomePosition();
                    if (creature.posX > (double) lvt_25_2_.getX()) {
                        lvt_22_1_ -= lvt_14_1_.nextInt(xz / 2);
                    } else {
                        lvt_22_1_ += lvt_14_1_.nextInt(xz / 2);
                    }

                    if (creature.posZ > (double) lvt_25_2_.getZ()) {
                        lvt_24_1_ -= lvt_14_1_.nextInt(xz / 2);
                    } else {
                        lvt_24_1_ += lvt_14_1_.nextInt(xz / 2);
                    }
                }

                lvt_25_2_ = new BlockPos(MathHelper.floor((double) lvt_22_1_ + creature.posX), MathHelper.floor((double) lvt_23_1_ + creature.posY), MathHelper.floor((double) lvt_24_1_ + creature.posZ));
                if (lvt_25_2_.getY() >= 0 && lvt_25_2_.getY() <= creature.world.getHeight() && (!lvt_15_2_ || leopard.isWithinHomeDistanceFromPosition(lvt_25_2_)) && (!p_226339_12_ || lvt_13_1_.canEntityStandOnPos(lvt_25_2_))) {
                    if (p_226339_9_) {
                        lvt_25_2_ = func_226342_a_(lvt_25_2_, lvt_14_1_.nextInt(p_226339_10_ + 1) + p_226339_11_, creature.world.getHeight(), (p_226341_1_) -> creature.world.getBlockState(p_226341_1_).getMaterial().isSolid());
                    }

                    if (p_226339_5_ || creature.world.getBlockState(lvt_25_2_).getMaterial() != Material.WATER) {
                        PathNodeType lvt_26_1_ = walkNodeProcessor.getPathNodeType(creature.world, lvt_25_2_.getX(), lvt_25_2_.getY(), lvt_25_2_.getZ());
                        if (leopard.getPathPriority(lvt_26_1_) == 0.0F) {
                            double lvt_27_1_ = p_226339_8_.applyAsDouble(lvt_25_2_);
                            if (lvt_27_1_ > lvt_17_1_) {
                                lvt_17_1_ = lvt_27_1_;
                                lvt_19_1_ = lvt_25_2_;
                                lvt_16_1_ = true;
                            }
                        }
                    }
                }
            }
        }

        if (lvt_16_1_) {
            return new Vec3d(lvt_19_1_.getX() + 0.5D, lvt_19_1_.getY(), lvt_19_1_.getZ() + 0.5D);
        } else {
            return null;
        }
    }
}
