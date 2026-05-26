package com.github.alexthe666.citadel.server.entity.collision;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CustomCollisionsNodeProcessor extends WalkNodeProcessor {
   public static PathNodeType func_237231_a_(IBlockAccess p_237231_0_, BlockPos.MutableBlockPos p_237231_1_) {
      int i = p_237231_1_.getX();
      int j = p_237231_1_.getY();
      int k = p_237231_1_.getZ();
      PathNodeType pathnodetype = getNodes(p_237231_0_, p_237231_1_);
      if (pathnodetype == PathNodeType.OPEN && j >= 1) {
         PathNodeType pathnodetype1 = getNodes(p_237231_0_, p_237231_1_.setPos(i, j - 1, k));
         pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         if (pathnodetype1 == PathNodeType.DAMAGE_FIRE) {
            pathnodetype = PathNodeType.DAMAGE_FIRE;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
            pathnodetype = PathNodeType.DAMAGE_CACTUS;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
            pathnodetype = PathNodeType.DAMAGE_OTHER;
         }

      }

      return pathnodetype;
   }

   protected static PathNodeType getNodes(IBlockAccess p_237238_0_, BlockPos p_237238_1_) {
      IBlockState blockstate = p_237238_0_.getBlockState(p_237238_1_);
      Block block = blockstate.getBlock();
      Material material = blockstate.getMaterial();
      if (material == Material.AIR) {
         return PathNodeType.OPEN;
      } else if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
         return PathNodeType.WATER;
      } else if (block == Blocks.LAVA || block == Blocks.FLOWING_LAVA) {
         return PathNodeType.LAVA;
      } else if (block == Blocks.FIRE) {
         return PathNodeType.DAMAGE_FIRE;
      } else {
         return block == Blocks.CACTUS ? PathNodeType.DAMAGE_CACTUS : PathNodeType.BLOCKED;
      }
   }

   public PathNodeType getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z) {
      return func_237231_a_(blockaccessIn, new BlockPos.MutableBlockPos(x, y, z));
   }

   protected PathNodeType getPathNodeTypeRaw(IBlockAccess world, int x, int y, int z) {
      BlockPos pos = new BlockPos(x, y, z);
      IBlockState state = world.getBlockState(pos);
      return ((ICustomCollisions)this.entity).canPassThrough(pos, state, null) ? PathNodeType.OPEN : super.getPathNodeTypeRaw(world, x, y, z);
   }
}
