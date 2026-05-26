package com.github.alexthe666.citadel.server.entity.collision;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.World;

public class CitadelVoxelShapeSpliterator extends Spliterators.AbstractSpliterator<AxisAlignedBB> {
   private final Iterator<AxisAlignedBB> iterator;

   public CitadelVoxelShapeSpliterator(World reader, @Nullable Entity entity, AxisAlignedBB aabb) {
      super(Long.MAX_VALUE, 1280);
      List<AxisAlignedBB> boxes = reader.getCollisionBoxes(entity, aabb);
      this.iterator = boxes.iterator();
   }

   public static boolean func_234877_a_(WorldBorder p_234877_0_, AxisAlignedBB p_234877_1_) {
      double d0 = Math.floor(p_234877_0_.minX());
      double d1 = Math.floor(p_234877_0_.minZ());
      double d2 = Math.ceil(p_234877_0_.maxX());
      double d3 = Math.ceil(p_234877_0_.maxZ());
      return p_234877_1_.minX > d0 && p_234877_1_.minX < d2 && p_234877_1_.minZ > d1 && p_234877_1_.minZ < d3 && p_234877_1_.maxX > d0 && p_234877_1_.maxX < d2 && p_234877_1_.maxZ > d1 && p_234877_1_.maxZ < d3;
   }

   public boolean tryAdvance(Consumer<? super AxisAlignedBB> p_tryAdvance_1_) {
      if (this.iterator.hasNext()) {
         p_tryAdvance_1_.accept((AxisAlignedBB)this.iterator.next());
         return true;
      }

      return false;
   }
}
