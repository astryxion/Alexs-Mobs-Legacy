package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class LaviathanAIRandomSwimming extends LavaAndWaterAIRandomSwimming {

    public LaviathanAIRandomSwimming(EntityCreature creature, double speed, int chance) {
        super(creature, speed, chance);
    }

    @Nullable
    @Override
    protected Vec3d getPosition() {
        BlockPos pos = this.mob.getPosition().add(
                this.mob.getRNG().nextInt(32) - 16,
                this.mob.getRNG().nextInt(10) - 5,
                this.mob.getRNG().nextInt(32) - 16);

        for (int i = 0; pos != null && this.mob.world.getBlockState(pos).getMaterial() == Material.AIR && i++ < 10; pos = this.mob.getPosition().add(
                this.mob.getRNG().nextInt(32) - 16,
                this.mob.getRNG().nextInt(10) - 5,
                this.mob.getRNG().nextInt(32) - 16)) {
        }
        if (this.mob.world.getBlockState(pos).getMaterial() != Material.WATER && this.mob.world.getBlockState(pos).getMaterial() != Material.LAVA) {
            return null;
        }
        if (mob.getRNG().nextInt(3) == 0) {
            while (this.mob.world.getBlockState(pos).getMaterial() == Material.WATER || this.mob.world.getBlockState(pos).getMaterial() == Material.LAVA) {
                pos = pos.up();
            }
            pos = pos.down();
        }
        return new Vec3d(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
    }
}
