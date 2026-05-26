package com.github.alexthe666.citadel.server.entity.collision;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class CustomCollisionsNavigator extends PathNavigateGround {
   private NodeProcessor nodeProcessorCustom;

   public CustomCollisionsNavigator(EntityLiving mob, World world) {
      super(mob, world);
   }

   protected PathFinder getPathFinder() {
      this.nodeProcessorCustom = new CustomCollisionsNodeProcessor();
      return new PathFinder(this.nodeProcessorCustom);
   }

   public NodeProcessor getNodeProcessor() {
      return this.nodeProcessorCustom;
   }

   protected boolean isDirectPathBetweenPoints(Vec3d posVec31, Vec3d posVec32, int sizeX, int sizeY, int sizeZ) {
      int i = MathHelper.floor(posVec31.x);
      int j = MathHelper.floor(posVec31.z);
      double d0 = posVec32.x - posVec31.x;
      double d1 = posVec32.z - posVec31.z;
      double d2 = d0 * d0 + d1 * d1;
      if (d2 < 1.0E-8) {
         return false;
      } else {
         double d3 = (double)1.0F / Math.sqrt(d2);
         d0 *= d3;
         d1 *= d3;
         sizeX += 2;
         sizeZ += 2;
         if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
            return false;
         } else {
            sizeX -= 2;
            sizeZ -= 2;
            double d4 = (double)1.0F / Math.abs(d0);
            double d5 = (double)1.0F / Math.abs(d1);
            double d6 = (double)i - posVec31.x;
            double d7 = (double)j - posVec31.z;
            if (d0 >= (double)0.0F) {
               ++d6;
            }

            if (d1 >= (double)0.0F) {
               ++d7;
            }

            d6 /= d0;
            d7 /= d1;
            int k = d0 < (double)0.0F ? -1 : 1;
            int l = d1 < (double)0.0F ? -1 : 1;
            int i1 = MathHelper.floor(posVec32.x);
            int j1 = MathHelper.floor(posVec32.z);
            int k1 = i1 - i;
            int l1 = j1 - j;

            while(k1 * k > 0 || l1 * l > 0) {
               if (d6 < d7) {
                  d6 += d4;
                  i += k;
                  k1 = i1 - i;
               } else {
                  d7 += d5;
                  j += l;
                  l1 = j1 - j;
               }

               if (!this.isSafeToStandAt(i, MathHelper.floor(posVec31.y), j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d p_179692_7_, double p_179692_8_, double p_179692_10_) {
      for(BlockPos blockpos : BlockPos.getAllInBox(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
         double d0 = (double)blockpos.getX() + 0.5D - p_179692_7_.x;
         double d1 = (double)blockpos.getZ() + 0.5D - p_179692_7_.z;
         if ((!(d0 * p_179692_8_ + d1 * p_179692_10_ < 0.0D) && this.world.getBlockState(blockpos).getMaterial().blocksMovement()) || ((ICustomCollisions)this.entity).canPassThrough(blockpos, this.world.getBlockState(blockpos), null)) {
            return false;
         }
      }

      return true;
   }

   private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3d vec31, double p_179683_8_, double p_179683_10_) {
      int i = x - sizeX / 2;
      int j = z - sizeZ / 2;
      if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, p_179683_8_, p_179683_10_)) {
         return false;
      } else {
         BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

         for(int k = i; k < i + sizeX; ++k) {
            for(int l = j; l < j + sizeZ; ++l) {
               double d0 = (double)k + 0.5D - vec31.x;
               double d1 = (double)l + 0.5D - vec31.z;
               if (!(d0 * p_179683_8_ + d1 * p_179683_10_ < (double)0.0F)) {
                  PathNodeType pathnodetype = this.nodeProcessorCustom.getPathNodeType(this.world, k, y - 1, l, this.entity, sizeX, sizeY, sizeZ, true, true);
                  mutable.setPos(k, y - 1, l);
                  if (!this.func_230287_a_(pathnodetype) || ((ICustomCollisions)this.entity).canPassThrough(mutable, this.world.getBlockState(mutable), null)) {
                     return false;
                  }

                  pathnodetype = this.nodeProcessorCustom.getPathNodeType(this.world, k, y, l, this.entity, sizeX, sizeY, sizeZ, true, true);
                  float f = this.entity.getPathPriority(pathnodetype);
                  if (f < 0.0F || f >= 8.0F) {
                     return false;
                  }

                  if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE || pathnodetype == PathNodeType.DAMAGE_OTHER) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   protected boolean func_230287_a_(PathNodeType p_230287_1_) {
      if (p_230287_1_ == PathNodeType.WATER) {
         return false;
      } else if (p_230287_1_ == PathNodeType.LAVA) {
         return false;
      } else {
         return p_230287_1_ != PathNodeType.OPEN;
      }
   }
}
