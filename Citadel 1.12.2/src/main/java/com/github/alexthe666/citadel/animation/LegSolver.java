package com.github.alexthe666.citadel.animation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LegSolver {
   public final Leg[] legs;

   public LegSolver(Leg... legs) {
      this.legs = legs;
   }

   public final void update(EntityLivingBase entity, float scale) {
      this.update(entity, entity.renderYawOffset, scale);
   }

   public final void update(EntityLivingBase entity, float yaw, float scale) {
      double sideTheta = (double)yaw / (180D / Math.PI);
      double sideX = Math.cos(sideTheta) * (double)scale;
      double sideZ = Math.sin(sideTheta) * (double)scale;
      double forwardTheta = sideTheta + (Math.PI / 2D);
      double forwardX = Math.cos(forwardTheta) * (double)scale;
      double forwardZ = Math.sin(forwardTheta) * (double)scale;

      for(Leg leg : this.legs) {
         leg.update(entity, sideX, sideZ, forwardX, forwardZ, scale);
      }

   }

   public static final class Leg {
      public final float forward;
      public final float side;
      private final float range;
      private float height;
      private float prevHeight;
      private boolean isWing;

      public Leg(float forward, float side, float range, boolean isWing) {
         this.forward = forward;
         this.side = side;
         this.range = range;
         this.isWing = isWing;
      }

      public final float getHeight(float delta) {
         return this.prevHeight + (this.height - this.prevHeight) * delta;
      }

      public void update(EntityLivingBase entity, double sideX, double sideZ, double forwardX, double forwardZ, float scale) {
         this.prevHeight = this.height;
         double posY = entity.posY;
         float settledHeight = this.settle(entity, entity.posX + sideX * (double)this.side + forwardX * (double)this.forward, posY, entity.posZ + sideZ * (double)this.side + forwardZ * (double)this.forward, this.height);
         this.height = MathHelper.clamp(settledHeight, -this.range * scale, this.range * scale);
      }

      private float settle(EntityLivingBase entity, double x, double y, double z, float height) {
         BlockPos pos = new BlockPos(x, y + 0.001, z);
         float dist = this.getDistance(entity.world, pos);
         if ((double)(1.0F - dist) < 0.001) {
            dist = this.getDistance(entity.world, pos.down()) + (float)y % 1.0F;
         } else {
            dist = (float)((double)dist - ((double)1.0F - y % (double)1.0F));
         }

         if (entity.onGround && height <= dist) {
            return height == dist ? height : Math.min(height + this.getFallSpeed(), dist);
         } else {
            return height > 0.0F ? Math.max(height - this.getRiseSpeed(), dist) : height;
         }
      }

      private float getDistance(World world, BlockPos pos) {
         IBlockState state = world.getBlockState(pos);
         AxisAlignedBB aabb = state.getBoundingBox(world, pos).offset(pos).offset(0.0D, (double)EnumFacing.UP.getFrontOffsetY(), 0.0D);
         return aabb == null ? 1.0F : 1.0F - Math.min((float)aabb.maxY, 1.0F);
      }

      protected float getFallSpeed() {
         return 0.25F;
      }

      protected float getRiseSpeed() {
         return 0.25F;
      }
   }
}
