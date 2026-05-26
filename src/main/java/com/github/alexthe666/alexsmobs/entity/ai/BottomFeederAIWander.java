package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.ISemiAquatic;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BottomFeederAIWander extends EntityAIWander {
    private final EntityCreature creature;
    private int waterChance = 0;
    private int landChance = 0;
    private int range = 5;

    public BottomFeederAIWander(EntityCreature creature, double speed, int waterChance, int landChance) {
        super(creature, speed, waterChance);
        this.creature = creature;
        this.waterChance = waterChance;
        this.landChance = landChance;
    }

    public BottomFeederAIWander(EntityCreature creature, double speed, int waterChance, int landChance, int range) {
        super(creature, speed, waterChance);
        this.creature = creature;
        this.waterChance = waterChance;
        this.landChance = landChance;
        this.range = range;
    }

    @Override
    public boolean shouldExecute() {
        if (creature instanceof ISemiAquatic && ((ISemiAquatic) creature).shouldStopMoving()) {
            return false;
        }
        this.setExecutionChance(creature.isInWater() ? waterChance : landChance);
        return super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        if (creature instanceof ISemiAquatic && ((ISemiAquatic) creature).shouldStopMoving()) {
            return false;
        }
        return super.shouldContinueExecuting();
    }

    @Override
    @Nullable
    protected Vec3d getPosition() {
        if (this.creature.isInWater()) {
            BlockPos blockpos = null;
            Random random = new Random();
            for (int i = 0; i < 15; i++) {
                BlockPos blockpos1 = this.creature.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
                while (isAirOrWaterBlock(creature.world, blockpos1) && blockpos1.getY() > 1) {
                    blockpos1 = blockpos1.down();
                }
                if (isBottomOfSeafloor(creature.world, blockpos1.up())) {
                    blockpos = blockpos1;
                }
            }

            return blockpos != null ? new Vec3d(blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D) : null;
        } else {
            return super.getPosition();
        }
    }

    private static boolean isAirOrWaterBlock(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return state.getMaterial() == Material.AIR || state.getMaterial() == Material.WATER;
    }

    private boolean isBottomOfSeafloor(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        IBlockState below = world.getBlockState(pos.down());
        return state.getMaterial() == Material.WATER && below.getMaterial() != Material.WATER && below.isOpaqueCube();
    }
}
