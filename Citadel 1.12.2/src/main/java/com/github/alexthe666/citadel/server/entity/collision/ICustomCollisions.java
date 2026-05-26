package com.github.alexthe666.citadel.server.entity.collision;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICustomCollisions {
   static Vec3d getAllowedMovementForEntity(Entity entity, Vec3d vec) {
      AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox();
      java.util.List<AxisAlignedBB> list = entity.world.getCollisionBoxes(entity, axisalignedbb.expand(vec.x, vec.y, vec.z));
      double x = vec.x;
      double y = vec.y;
      double z = vec.z;

      for(AxisAlignedBB aabb : list) {
         y = aabb.calculateYOffset(axisalignedbb, y);
      }

      axisalignedbb = axisalignedbb.offset(0.0D, y, 0.0D);

      for(AxisAlignedBB aabb : list) {
         x = aabb.calculateXOffset(axisalignedbb, x);
      }

      axisalignedbb = axisalignedbb.offset(x, 0.0D, 0.0D);

      for(AxisAlignedBB aabb : list) {
         z = aabb.calculateZOffset(axisalignedbb, z);
      }

      Vec3d vector3d = new Vec3d(x, y, z);
      boolean flag = vec.x != vector3d.x;
      boolean flag1 = vec.y != vector3d.y;
      boolean flag2 = vec.z != vector3d.z;
      boolean flag3 = entity.onGround || flag1 && vec.y < 0.0D;
      if (entity.stepHeight > 0.0F && flag3 && (flag || flag2)) {
         Vec3d vector3d1 = ((ICustomCollisions)entity).collideBoundingBoxHeuristicallyPassable(entity, new Vec3d(vec.x, entity.stepHeight, vec.z), axisalignedbb, entity.world);
         Vec3d vector3d2 = ((ICustomCollisions)entity).collideBoundingBoxHeuristicallyPassable(entity, new Vec3d(0.0D, entity.stepHeight, 0.0D), axisalignedbb.offset(vec.x, 0.0D, vec.z), entity.world);
         if (vector3d2.y < (double)entity.stepHeight) {
            Vec3d vector3d3 = ((ICustomCollisions)entity).collideBoundingBoxHeuristicallyPassable(entity, new Vec3d(vec.x, 0.0D, vec.z), axisalignedbb.offset(vector3d2.x, vector3d2.y, vector3d2.z), entity.world).add(vector3d2);
            if (vector3d3.lengthSquared() > vector3d1.lengthSquared()) {
               vector3d1 = vector3d3;
            }
         }

         if (vector3d1.lengthSquared() > vector3d.lengthSquared()) {
            Vec3d post = ((ICustomCollisions)entity).collideBoundingBoxHeuristicallyPassable(entity, new Vec3d(0.0D, -vector3d1.y + vec.y, 0.0D), axisalignedbb.offset(vector3d1.x, vector3d1.y, vector3d1.z), entity.world);
            return vector3d1.add(post);
         }
      }

      return vector3d;
   }

   boolean canPassThrough(BlockPos var1, IBlockState var2, AxisAlignedBB var3);

   default Vec3d collideBoundingBoxHeuristicallyPassable(@Nullable Entity entity, Vec3d vec, AxisAlignedBB collisionBox, World world) {
      boolean flag = vec.x == 0.0D;
      boolean flag1 = vec.y == 0.0D;
      boolean flag2 = vec.z == 0.0D;
      if (flag && flag1 || flag && flag2 || flag1 && flag2) {
         return vec;
      } else {
         java.util.List<AxisAlignedBB> boxes = world.getCollisionBoxes(entity, collisionBox.expand(vec.x, vec.y, vec.z));
         double x = vec.x;
         double y = vec.y;
         double z = vec.z;

         for(AxisAlignedBB aabb : boxes) {
            y = aabb.calculateYOffset(collisionBox, y);
         }

         AxisAlignedBB moved = collisionBox.offset(0.0D, y, 0.0D);

         for(AxisAlignedBB aabb : boxes) {
            x = aabb.calculateXOffset(moved, x);
         }

         moved = moved.offset(x, 0.0D, 0.0D);

         for(AxisAlignedBB aabb : boxes) {
            z = aabb.calculateZOffset(moved, z);
         }

         return new Vec3d(x, y, z);
      }
   }
}
