package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.world.World;

/**
 * Semi-aquatic navigation: vanilla {@link PathNavigateSwimmer} but pathfinding is allowed on land too (1.16 parity).
 */
public class SemiAquaticPathNavigator extends PathNavigateSwimmer {

    public SemiAquaticPathNavigator(EntityLiving entitylivingIn, World worldIn) {
        super(entitylivingIn, worldIn);
    }

    @Override
    protected boolean canNavigate() {
        return true;
    }

    public void setCanSwim(boolean canSwim) {
    }
}
