package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityElephant;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.pathfinding.Path;

public class ElephantAIVillagerRide extends EntityAIBase {

    private final EntityElephant elephant;
    private EntityVillager villager;
    private final double speed;

    public ElephantAIVillagerRide(EntityElephant dragon, double speed) {
        elephant = dragon;
        this.speed = speed;
    }

    @Override
    public boolean shouldExecute() {
        if (elephant.getControllingVillager() != null) {
            villager = elephant.getControllingVillager();
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
    }

    @Override
    public void updateTask() {
        Path path = this.villager.getNavigator().getPath();
        if (path != null) {
            this.elephant.getNavigator().setPath(path, speed);
        }
    }
}
