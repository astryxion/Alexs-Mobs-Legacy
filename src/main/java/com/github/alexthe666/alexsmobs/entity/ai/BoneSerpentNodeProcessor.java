package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.SwimNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Lava + water pathfinding: extends vanilla swim processor and marks lava as traversable.
 */
public class BoneSerpentNodeProcessor extends SwimNodeProcessor {

    @Override
    public PathNodeType getPathNodeType(IBlockAccess worldIn, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = worldIn.getBlockState(pos);
        Material mat = state.getMaterial();
        if (mat == Material.LAVA) {
            return PathNodeType.LAVA;
        }
        if (mat == Material.WATER) {
            return PathNodeType.WATER;
        }
        PathNodeType base = super.getPathNodeType(worldIn, x, y, z);
        if (base == PathNodeType.OPEN && worldIn.getBlockState(pos.down()).getMaterial() == Material.LAVA) {
            return PathNodeType.OPEN;
        }
        return base;
    }
}
