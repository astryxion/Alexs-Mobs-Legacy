package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * 1.12 {@link EntityAIWander} with configurable horizontal/vertical search like 1.16 {@code RandomWalkingGoal}.
 */
public class AnimalAIWanderRanged extends EntityAIWander {

    protected final float probability;
    protected final int xzRange;
    protected final int yRange;

    public AnimalAIWanderRanged(EntityCreature creature, int chance, double speedIn, int xzRange, int yRange) {
        this(creature, chance, speedIn, 0.001F, xzRange, yRange);
    }

    public AnimalAIWanderRanged(EntityCreature creature, int chance, double speedIn, float probabilityIn, int xzRange, int yRange) {
        super(creature, speedIn, chance);
        this.probability = probabilityIn;
        this.xzRange = xzRange;
        this.yRange = yRange;
    }

    @Override
    public boolean shouldExecute() {
        if (this.entity.isBeingRidden() && !(this.entity instanceof EntityKangaroo)) {
            return false;
        } else {
            if (!this.mustUpdate) {
                if (this.entity.getIdleTime() >= 100) {
                    return false;
                }

                if (this.entity.getRNG().nextInt(this.executionChance) != 0) {
                    return false;
                }
            }

            Vec3d dest = this.pickDest();
            if (dest == null) {
                return false;
            } else {
                this.x = dest.x;
                this.y = dest.y;
                this.z = dest.z;
                this.mustUpdate = false;
                return true;
            }
        }
    }

    @Nullable
    protected Vec3d pickDest() {
        if (this.entity.isInWater()) {
            Vec3d vector3d = RandomPositionGenerator.getLandPos(this.entity, xzRange, yRange);
            return vector3d == null ? super.getPosition() : vector3d;
        } else {
            return this.entity.getRNG().nextFloat() >= this.probability ? RandomPositionGenerator.getLandPos(this.entity, xzRange, yRange) : super.getPosition();
        }
    }
}
