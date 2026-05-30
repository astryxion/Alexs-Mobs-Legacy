package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;

/**
 * Direct motion toward water when swim pathfinding cannot run on land (e.g. {@link SwimmerJumpPathNavigator}).
 */
public final class FindWaterSteering {

    private FindWaterSteering() {
    }

    public static void steerToward(EntityLiving entity, BlockPos targetPos) {
        double dx = targetPos.getX() + 0.5D - entity.posX;
        double dz = targetPos.getZ() + 0.5D - entity.posZ;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len > 0.01D) {
            entity.motionX += (dx / len) * 0.08D;
            entity.motionZ += (dz / len) * 0.08D;
        }
        if (targetPos.getY() > entity.posY + 0.5D) {
            entity.motionY = Math.max(entity.motionY, 0.35D);
        }
    }
}
