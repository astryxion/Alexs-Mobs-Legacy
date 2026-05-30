package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Semi-aquatic navigation (1.16 {@code WalkAndSwimNodeProcessor} parity).
 * On land uses {@link WalkNodeProcessor}; in water uses swim pathfinding.
 * Using {@link net.minecraft.pathfinding.SwimNodeProcessor} on land freezes the game when seeking water.
 */
public class SemiAquaticPathNavigator extends PathNavigateSwimmer {

    private final PathFinder landPathFinder;
    private final WalkNodeProcessor landNodeProcessor;

    public SemiAquaticPathNavigator(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
        this.landNodeProcessor = new WalkNodeProcessor();
        this.landNodeProcessor.setCanEnterDoors(true);
        this.landPathFinder = new PathFinder(this.landNodeProcessor);
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }

    @Override
    @Nullable
    public Path getPathToPos(BlockPos pos) {
        if (this.entity.isInWater() || this.entity.isInLava()) {
            return super.getPathToPos(pos);
        }
        float range = this.getPathSearchRange();
        this.world.profiler.startSection("pathfind");
        BlockPos origin = new BlockPos(this.entity);
        int i = (int) (range + 8.0F);
        ChunkCache chunkcache = new ChunkCache(this.world, origin.add(-i, -i, -i), origin.add(i, i, i), 0);
        Path path = this.landPathFinder.findPath(chunkcache, this.entity, pos, range);
        this.world.profiler.endSection();
        return path;
    }

    @Override
    @Nullable
    public Path getPathToEntityLiving(Entity entityIn) {
        if (this.entity.isInWater() || this.entity.isInLava()) {
            return super.getPathToEntityLiving(entityIn);
        }
        return this.getPathToPos(new BlockPos(entityIn));
    }

    public void setCanSwim(boolean canSwim) {
    }
}
