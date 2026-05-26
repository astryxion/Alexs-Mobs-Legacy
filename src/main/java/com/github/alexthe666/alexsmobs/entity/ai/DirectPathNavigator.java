package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

/**
 * Lightweight 1.12 navigator: forwards movement to {@link net.minecraft.entity.ai.EntityMoveHelper}.
 */
public class DirectPathNavigator extends PathNavigateGround {

    private final EntityLiving mob;

    public DirectPathNavigator(EntityLiving mob, World world) {
        super(mob, world);
        this.mob = mob;
    }

    @Override
    public void onUpdateNavigation() {
        ++this.totalTicks;
    }

    public boolean tryMoveToXYZ(double x, double y, double z, double speedIn) {
        mob.getMoveHelper().setMoveTo(x, y, z, speedIn);
        return true;
    }

    public boolean tryMoveToEntityLiving(Entity entityIn, double speedIn) {
        mob.getMoveHelper().setMoveTo(entityIn.posX, entityIn.posY, entityIn.posZ, speedIn);
        return true;
    }
}
