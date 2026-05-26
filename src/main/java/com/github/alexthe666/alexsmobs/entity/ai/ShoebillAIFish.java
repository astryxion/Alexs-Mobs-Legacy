package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityShoebill;
import net.minecraft.block.material.Material;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.List;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code ShoebillAIFish}.
 */
public class ShoebillAIFish extends EntityAIBase {

    private final EntityShoebill bird;
    private BlockPos waterPos = null;
    private BlockPos targetPos = null;
    private final EnumFacing[] HORIZONTALS = new EnumFacing[]{EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};
    private int idleTime = 0;
    private int navigateTime = 0;

    public ShoebillAIFish(EntityShoebill bird) {
        this.setMutexBits(3);
        this.bird = bird;
    }

    @Override
    public void resetTask() {
        targetPos = null;
        waterPos = null;
        idleTime = 0;
        navigateTime = 0;
        this.bird.getNavigator().clearPath();
    }

    @Override
    public void updateTask() {
        if (targetPos != null && waterPos != null) {
            double dist = bird.getDistanceSq(waterPos.getX() + 0.5D, waterPos.getY(), waterPos.getZ() + 0.5D);
            if (dist <= 1F) {
                navigateTime = 0;
                double d0 = waterPos.getX() + 0.5D - bird.posX;
                double d2 = waterPos.getZ() + 0.5D - bird.posZ;
                float yaw = (float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F;
                bird.rotationYaw = yaw;
                bird.rotationYawHead = yaw;
                bird.renderYawOffset = yaw;
                bird.getNavigator().clearPath();
                idleTime++;
                if (idleTime > 25) {
                    bird.setAnimation(EntityShoebill.ANIMATION_FISH);
                }
                if (idleTime > 45 && bird.getAnimation() == EntityShoebill.ANIMATION_FISH) {
                    this.bird.playSound(SoundEvents.ENTITY_GENERIC_SPLASH, 0.7F, 0.5F + bird.getRNG().nextFloat());
                    this.bird.resetFishingCooldown();
                    this.spawnFishingLoot();
                    this.resetTask();
                }
            } else {
                navigateTime++;
                bird.getNavigator().tryMoveToXYZ(waterPos.getX(), waterPos.getY(), waterPos.getZ(), 1.2D);
            }
            if (navigateTime > 3600) {
                this.resetTask();
            }
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return targetPos != null && bird.fishingCooldown == 0 && bird.revengeCooldown == 0 && !bird.isFlying();
    }

    public void spawnFishingLoot() {
        double luck = 0D + bird.luckLevel * 0.5F;
        if (this.bird.world instanceof WorldServer) {
            WorldServer sw = (WorldServer) this.bird.world;
            LootTable loottable = sw.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING);
            LootContext.Builder builder = new LootContext.Builder(sw).withLuck((float) luck);
            List<ItemStack> result = loottable.generateLootForPools(this.bird.getRNG(), builder.build());
            for (ItemStack itemstack : result) {
                EntityItem item = new EntityItem(this.bird.world, this.bird.posX + 0.5F, this.bird.posY, this.bird.posZ, itemstack);
                if (!this.bird.world.isRemote) {
                    this.bird.world.spawnEntity(item);
                }
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        if (!bird.isFlying() && bird.fishingCooldown == 0 && bird.getRNG().nextInt(30) == 0) {
            if (bird.isInWater()) {
                waterPos = bird.getPosition();
                targetPos = waterPos;
                return true;
            } else {
                waterPos = generateTarget();
                if (waterPos != null) {
                    targetPos = getLandPos(waterPos);
                    return targetPos != null;
                }
            }
        }
        return false;
    }

    public BlockPos generateTarget() {
        BlockPos blockpos = null;
        Random random = new Random();
        int range = 32;
        for (int i = 0; i < 15; i++) {
            BlockPos blockpos1 = this.bird.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
            while (this.bird.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                blockpos1 = blockpos1.down();
            }
            if (isConnectedToLand(blockpos1)) {
                blockpos = blockpos1;
            }
        }
        return blockpos;
    }

    public boolean isConnectedToLand(BlockPos pos) {
        if (this.bird.world.getBlockState(pos).getMaterial() == Material.WATER) {
            for (EnumFacing dir : HORIZONTALS) {
                BlockPos offsetPos = pos.offset(dir);
                if (this.bird.world.getBlockState(offsetPos).getMaterial() != Material.WATER
                        && this.bird.world.getBlockState(offsetPos.up()).getMaterial() != Material.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    public BlockPos getLandPos(BlockPos pos) {
        if (this.bird.world.getBlockState(pos).getMaterial() == Material.WATER) {
            for (EnumFacing dir : HORIZONTALS) {
                BlockPos offsetPos = pos.offset(dir);
                if (this.bird.world.getBlockState(offsetPos).getMaterial() != Material.WATER
                        && this.bird.world.getBlockState(offsetPos.up()).getMaterial() != Material.WATER) {
                    return offsetPos;
                }
            }
        }
        return null;
    }
}
