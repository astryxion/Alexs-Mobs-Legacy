package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityLaviathan;
import com.github.alexthe666.alexsmobs.entity.IHerdPanic;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.List;

public class AnimalAIHerdPanic extends EntityAIBase {
    protected final EntityCreature creature;
    protected final double speed;
    protected final Predicate<? super EntityCreature> targetEntitySelector;
    protected double randPosX;
    protected double randPosY;
    protected double randPosZ;
    protected boolean running;

    public AnimalAIHerdPanic(EntityCreature creature, double speedIn) {
        this.creature = creature;
        this.speed = speedIn;
        this.setMutexBits(1);
        this.targetEntitySelector = new Predicate<EntityCreature>() {
            @Override
            public boolean apply(@Nullable EntityCreature animal) {
                if (animal instanceof IHerdPanic && animal.getClass() == creature.getClass()) {
                    return ((IHerdPanic) animal).canPanic();
                }
                return false;
            }
        };
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.getRevengeTarget() == null && !this.creature.isBurning()) {
            return false;
        } else {
            if (this.creature.isBurning()) {
                BlockPos blockpos = this.getRandPos(this.creature.world, this.creature, 5, 4);
                if (blockpos != null) {
                    this.randPosX = blockpos.getX();
                    this.randPosY = blockpos.getY();
                    this.randPosZ = blockpos.getZ();
                    return true;
                }
            }
            if (this.creature.getRevengeTarget() != null && this.creature instanceof IHerdPanic && ((IHerdPanic) this.creature).canPanic()) {
                List<EntityCreature> list = this.creature.world.getEntitiesWithinAABB(this.creature.getClass(), this.getTargetableArea(), this.targetEntitySelector);
                for (EntityCreature creatureEntity : list) {
                    creatureEntity.setRevengeTarget(this.creature.getRevengeTarget());
                }
                return this.findRandomPositionFrom(this.creature.getRevengeTarget());
            }
            return this.findRandomPosition();
        }
    }

    private boolean findRandomPositionFrom(EntityLivingBase revengeTarget) {
        Vec3d vector3d;
        if (this.creature instanceof EntityLaviathan) {
            vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 32, 16, new Vec3d(revengeTarget.posX, revengeTarget.posY, revengeTarget.posZ));
        } else {
            vector3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(this.creature, 16, 7, new Vec3d(revengeTarget.posX, revengeTarget.posY, revengeTarget.posZ));
        }
        if (vector3d == null) {
            return false;
        } else {
            this.randPosX = vector3d.x;
            this.randPosY = vector3d.y;
            this.randPosZ = vector3d.z;
            return true;
        }
    }

    protected AxisAlignedBB getTargetableArea() {
        double searchRadius = 15;
        return new AxisAlignedBB(-searchRadius, -searchRadius, -searchRadius, searchRadius, searchRadius, searchRadius)
                .offset(this.creature.posX + 0.5D, this.creature.posY + 0.5D, this.creature.posZ + 0.5D);
    }

    protected boolean findRandomPosition() {
        Vec3d vector3d = RandomPositionGenerator.findRandomTarget(this.creature, 5, 4);
        if (vector3d == null) {
            return false;
        } else {
            this.randPosX = vector3d.x;
            this.randPosY = vector3d.y;
            this.randPosZ = vector3d.z;
            return true;
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    @Override
    public void startExecuting() {
        if (this.creature instanceof IHerdPanic) {
            ((IHerdPanic) this.creature).onPanic();
        }
        this.creature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY, this.randPosZ, this.speed);
        this.running = true;
    }

    @Override
    public void resetTask() {
        this.running = false;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    @Nullable
    protected BlockPos getRandPos(IBlockAccess worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
        BlockPos blockpos = entityIn.getPosition();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();
        float f = (float) (horizontalRange * horizontalRange * verticalRange * 2);
        BlockPos blockpos1 = null;
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();

        for (int l = i - horizontalRange; l <= i + horizontalRange; ++l) {
            for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1) {
                for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1) {
                    blockpos$mutable.setPos(l, i1, j1);
                    if (worldIn.getBlockState(blockpos$mutable).getMaterial() == Material.WATER) {
                        float f1 = (float) ((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));
                        if (f1 < f) {
                            f = f1;
                            blockpos1 = new BlockPos(blockpos$mutable);
                        }
                    }
                }
            }
        }

        return blockpos1;
    }
}
