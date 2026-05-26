package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Wider horizontal reach for ground waypoints ({@code width * distancemodifier} instead of vanilla {@code width * 0.75F}).
 */
public class GroundPathNavigatorWide extends PathNavigateGround {
    private float distancemodifier = 0.75F;

    public GroundPathNavigatorWide(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    public GroundPathNavigatorWide(EntityLiving entitylivingIn, World worldIn, float distancemodifier) {
        super(entitylivingIn, worldIn);
        this.distancemodifier = distancemodifier;
    }

    @Override
    protected void pathFollow() {
        Vec3d entityPos = this.getEntityPosition();
        this.maxDistanceToWaypoint = this.entity.width * this.distancemodifier;
        Path path = this.currentPath;
        if (path != null) {
            PathPoint pp = path.getPathPointFromIndex(path.getCurrentPathIndex());
            double d0 = Math.abs(this.entity.posX - ((double) pp.x + 0.5D));
            double d1 = Math.abs(this.entity.posY - (double) pp.y);
            double d2 = Math.abs(this.entity.posZ - ((double) pp.z + 0.5D));
            boolean flag = d0 < (double) this.maxDistanceToWaypoint && d2 < (double) this.maxDistanceToWaypoint && d1 < 1.0D;
            PathPoint cur = path.getPathPointFromIndex(path.getCurrentPathIndex());
            if (flag || this.entity.getPathPriority(cur.nodeType) >= 0.0F && this.func_234112_b_(entityPos)) {
                path.incrementPathIndex();
            }
        }

        this.checkForStuck(entityPos);
    }

    private boolean func_234112_b_(Vec3d currentPosition) {
        Path path = this.currentPath;
        if (path == null || path.getCurrentPathIndex() + 1 >= path.getCurrentPathLength()) {
            return false;
        } else {
            PathPoint cur = path.getPathPointFromIndex(path.getCurrentPathIndex());
            Vec3d vec3d = new Vec3d((double) cur.x + 0.5D, (double) cur.y, (double) cur.z + 0.5D);
            if (currentPosition.squareDistanceTo(vec3d) >= 4.0D) {
                return false;
            } else {
                PathPoint next = path.getPathPointFromIndex(path.getCurrentPathIndex() + 1);
                Vec3d vec3d1 = new Vec3d((double) next.x + 0.5D, (double) next.y, (double) next.z + 0.5D).subtract(vec3d);
                Vec3d vec3d2 = currentPosition.subtract(vec3d);
                return vec3d1.x * vec3d2.x + vec3d1.y * vec3d2.y + vec3d1.z * vec3d2.z > 0.0D;
            }
        }
    }
}
