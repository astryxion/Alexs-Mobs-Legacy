package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Dolphin-style swim navigator with jump-ahead waypoint skipping (1.16 {@code SwimmerJumpPathNavigator} parity).
 */
public class SwimmerJumpPathNavigator extends PathNavigateSwimmer {

    public SwimmerJumpPathNavigator(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder getPathFinder() {
        SwimNodeProcessor processor = new SwimNodeProcessor();
        processor.init(this.world, this.entity);
        return new PathFinder(processor);
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }

    @Override
    protected void pathFollow() {
        Path path = this.currentPath;
        if (path != null) {
            Vec3d entityPos = this.getEntityPosition();
            float f = this.entity.width;
            float f1 = f > 0.75F ? f * 0.75F : 0.75F - f / 2.0F;
            if (Math.abs(this.entity.motionX) > 0.2D || Math.abs(this.entity.motionZ) > 0.2D) {
                double horiz = Math.sqrt(this.entity.motionX * this.entity.motionX + this.entity.motionZ * this.entity.motionZ);
                f1 = (float) ((double) f1 * horiz * 6.0D);
            }

            PathPoint target = path.getPathPointFromIndex(path.getCurrentPathIndex());
            Vec3d waypoint = new Vec3d(target.x + 0.5D, target.y, target.z + 0.5D);
            if (Math.abs(this.entity.posX - waypoint.x) < (double) f1 && Math.abs(this.entity.posZ - waypoint.z) < (double) f1 && Math.abs(this.entity.posY - waypoint.y) < (double) (f1 * 2.0F)) {
                path.incrementPathIndex();
            }

            for (int j = Math.min(path.getCurrentPathIndex() + 6, path.getCurrentPathLength() - 1); j > path.getCurrentPathIndex(); --j) {
                PathPoint pp = path.getPathPointFromIndex(j);
                Vec3d vec = new Vec3d(pp.x + 0.5D, pp.y, pp.z + 0.5D);
                if (!(vec.squareDistanceTo(entityPos) > 36.0D) && this.isDirectPathBetweenPoints(entityPos, vec)) {
                    path.setCurrentPathIndex(j);
                    break;
                }
            }

            this.checkForStuck(entityPos);
        }
    }

    protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32) {
        Vec3d end = new Vec3d(posVec32.x, posVec32.y + (double) this.entity.height * 0.5D, posVec32.z);
        RayTraceResult result = this.world.rayTraceBlocks(posVec31, end, false, true, false);
        return result == null || result.typeOfHit == RayTraceResult.Type.MISS;
    }

    public void setCanSwim(boolean canSwim) {
    }
}
