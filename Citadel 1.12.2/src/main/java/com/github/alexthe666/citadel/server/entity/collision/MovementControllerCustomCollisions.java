package com.github.alexthe666.citadel.server.entity.collision;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class MovementControllerCustomCollisions extends EntityMoveHelper {
   private final EntityLiving mob;

   public MovementControllerCustomCollisions(EntityLiving mob) {
      super(mob);
      this.mob = mob;
   }

   public void onUpdateMoveHelper() {
      if (this.action == EntityMoveHelper.Action.STRAFE) {
         float f = (float)this.mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
         float f1 = (float)this.speed * f;
         float f2 = this.moveForward;
         float f3 = this.moveStrafe;
         float f4 = MathHelper.sqrt(f2 * f2 + f3 * f3);
         if (f4 < 1.0F) {
            f4 = 1.0F;
         }

         f4 = f1 / f4;
         f2 *= f4;
         f3 *= f4;
         float f5 = MathHelper.sin(this.mob.rotationYaw * 0.017453292F);
         float f6 = MathHelper.cos(this.mob.rotationYaw * 0.017453292F);
         float f7 = f2 * f6 - f3 * f5;
         float f8 = f3 * f6 + f2 * f5;
         if (!this.func_234024_b_(f7, f8)) {
            this.moveForward = 1.0F;
            this.moveStrafe = 0.0F;
         }

         this.mob.setAIMoveSpeed(f1);
         this.mob.setMoveForward(this.moveForward);
         this.mob.setMoveStrafing(this.moveStrafe);
         this.action = EntityMoveHelper.Action.WAIT;
      } else if (this.action == EntityMoveHelper.Action.MOVE_TO) {
         this.action = EntityMoveHelper.Action.WAIT;
         double d0 = this.posX - this.mob.posX;
         double d1 = this.posZ - this.mob.posZ;
         double d2 = this.posY - this.mob.posY;
         double d3 = d0 * d0 + d2 * d2 + d1 * d1;
         if (d3 < 2.5000003E-7D) {
            this.mob.setMoveForward(0.0F);
            return;
         }

         float f9 = (float)(MathHelper.atan2(d1, d0) * 57.2957763671875D) - 90.0F;
         this.mob.rotationYaw = this.limitAngle(this.mob.rotationYaw, f9, 90.0F);
         this.mob.setAIMoveSpeed((float)(this.speed * this.mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
         BlockPos blockpos = new BlockPos(this.mob);
         IBlockState blockstate = this.mob.world.getBlockState(blockpos);
         Block block = blockstate.getBlock();
         AxisAlignedBB voxelshape = blockstate.getCollisionBoundingBox(this.mob.world, blockpos);
         if ((!(this.mob instanceof ICustomCollisions) || !((ICustomCollisions)this.mob).canPassThrough(blockpos, blockstate, voxelshape)) && (d2 > (double)this.mob.stepHeight && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, this.mob.height) || voxelshape != Block.NULL_AABB && this.mob.posY < voxelshape.maxY + (double)blockpos.getY() && block != Blocks.TRAPDOOR && block != Blocks.IRON_TRAPDOOR)) {
            this.mob.getJumpHelper().setJumping();
            this.action = EntityMoveHelper.Action.JUMPING;
         }
      } else if (this.action == EntityMoveHelper.Action.JUMPING) {
         this.mob.setAIMoveSpeed((float)(this.speed * this.mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
         if (this.mob.onGround) {
            this.action = EntityMoveHelper.Action.WAIT;
         }
      } else {
         this.mob.setMoveForward(0.0F);
      }

   }

   private boolean func_234024_b_(float p_234024_1_, float p_234024_2_) {
      PathNavigate pathnavigator = this.mob.getNavigator();
      if (pathnavigator != null) {
         NodeProcessor nodeprocessor = pathnavigator instanceof CustomCollisionsNavigator ? ((CustomCollisionsNavigator)pathnavigator).getNodeProcessor() : null;
         if (nodeprocessor != null && nodeprocessor.getPathNodeType(this.mob.world, MathHelper.floor(this.mob.posX + (double)p_234024_1_), MathHelper.floor(this.mob.posY), MathHelper.floor(this.mob.posZ + (double)p_234024_2_)) != PathNodeType.WALKABLE) {
            return false;
         }
      }

      return true;
   }
}
