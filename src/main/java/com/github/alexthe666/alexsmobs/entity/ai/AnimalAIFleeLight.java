package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class AnimalAIFleeLight extends EntityAIBase {
    protected final EntityCreature creature;
    private double shelterX;
    private double shelterY;
    private double shelterZ;
    private final double movementSpeed;
    private final World world;
    private int executeChance = 50;

    public AnimalAIFleeLight(EntityCreature creature, double movementSpeed) {
        this.creature = creature;
        this.movementSpeed = movementSpeed;
        this.world = creature.world;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute() {
        if (this.creature.getAttackTarget() != null || this.creature.getRNG().nextInt(executeChance) != 0) {
            return false;
        } else if (this.world.getLight(this.creature.getPosition()) < 10) {
            return false;
        } else {
            return this.isPossibleShelter();
        }
    }

    protected boolean isPossibleShelter() {
        Vec3d shelter = this.findPossibleShelter();
        if (shelter == null) {
            return false;
        } else {
            this.shelterX = shelter.x;
            this.shelterY = shelter.y;
            this.shelterZ = shelter.z;
            return true;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return !this.creature.getNavigator().noPath();
    }

    @Override
    public void startExecuting() {
        this.creature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
    }

    @Nullable
    protected Vec3d findPossibleShelter() {
        Random random = this.creature.getRNG();
        BlockPos origin = this.creature.getPosition();

        for (int i = 0; i < 10; ++i) {
            BlockPos pos = origin.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (this.creature.world.getLight(pos) < 10) {
                return new Vec3d(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
            }
        }

        return null;
    }
}
