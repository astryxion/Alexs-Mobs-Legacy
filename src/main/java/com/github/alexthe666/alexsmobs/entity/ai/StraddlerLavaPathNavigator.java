package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.world.World;

public class StraddlerLavaPathNavigator extends PathNavigateGround {

    public StraddlerLavaPathNavigator(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
        this.nodeProcessor = new StraddlerWalkNodeProcessor();
        this.nodeProcessor.init(this.world, this.entity);
    }

    @Override
    protected PathFinder getPathFinder() {
        if (this.nodeProcessor == null) {
            this.nodeProcessor = new StraddlerWalkNodeProcessor();
            this.nodeProcessor.init(this.world, this.entity);
        }
        return new PathFinder(this.nodeProcessor);
    }
}
