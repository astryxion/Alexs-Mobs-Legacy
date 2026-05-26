package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.world.World;

public class BoneSerpentPathNavigator extends PathNavigateSwimmer {

    public BoneSerpentPathNavigator(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected PathFinder getPathFinder() {
        BoneSerpentNodeProcessor processor = new BoneSerpentNodeProcessor();
        processor.init(this.world, this.entity);
        return new PathFinder(processor);
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }

    public void setCanSwim(boolean canSwim) {
    }
}
