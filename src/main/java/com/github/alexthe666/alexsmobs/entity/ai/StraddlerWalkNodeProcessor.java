package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Ground pathfinding that treats lava like a valid floor node (1.16 straddler / strider-style walking on lava).
 */
public class StraddlerWalkNodeProcessor extends WalkNodeProcessor {

    @Override
    public PathNodeType getPathNodeType(IBlockAccess worldIn, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = worldIn.getBlockState(pos);
        if (state.getMaterial() == Material.LAVA) {
            return PathNodeType.LAVA;
        }
        return super.getPathNodeType(worldIn, x, y, z);
    }
}
