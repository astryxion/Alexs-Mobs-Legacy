package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityPlatypus;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

import java.util.List;
import java.util.Random;

/**
 * Forge 1.12.2 port of 1.16 {@code PlatypusAIDigForItems}.
 */
public class PlatypusAIDigForItems extends EntityAIBase {

    public static final ResourceLocation PLATYPUS_REWARD = new ResourceLocation("alexsmobs", "gameplay/platypus_reward");
    public static final ResourceLocation PLATYPUS_REWARD_CHARGED = new ResourceLocation("alexsmobs", "gameplay/platypus_supercharged_reward");

    private final EntityPlatypus platypus;
    private BlockPos digPos;
    private int generatePosCooldown = 0;
    private int digTime = 0;
    private int maxDroppedItems = 3;

    public PlatypusAIDigForItems(EntityPlatypus platypus) {
        this.platypus = platypus;
        this.setMutexBits(3);
    }

    private static List<ItemStack> getItemStacks(EntityPlatypus platypus) {
        if (!(platypus.world instanceof WorldServer)) {
            return java.util.Collections.emptyList();
        }
        WorldServer sw = (WorldServer) platypus.world;
        LootTable loottable = sw.getLootTableManager().getLootTableFromLocation(platypus.superCharged ? PLATYPUS_REWARD_CHARGED : PLATYPUS_REWARD);
        return loottable.generateLootForPools(platypus.getRNG(), new LootContext.Builder(sw).withLootedEntity(platypus).build());
    }

    @Override
    public boolean shouldExecute() {
        if (!platypus.isSensing()) {
            return false;
        }
        if (generatePosCooldown == 0) {
            generatePosCooldown = 20 + platypus.getRNG().nextInt(20);
            digPos = genDigPos();
            maxDroppedItems = 2 + platypus.getRNG().nextInt(5);
            return digPos != null;
        } else {
            generatePosCooldown--;
            return false;
        }
    }

    @Override
    public boolean shouldContinueExecuting() {
        return platypus.getAttackTarget() == null && platypus.isSensing() && platypus.getRevengeTarget() == null
                && digPos != null && platypus.world.getBlockState(digPos).getBlock() == Blocks.CLAY
                && platypus.world.getBlockState(digPos.up()).getMaterial() == Material.WATER;
    }

    @Override
    public void updateTask() {
        double dist = platypus.getDistanceSq(digPos.getX() + 0.5D, digPos.getY(), digPos.getZ() + 0.5D);
        double d0 = digPos.getX() + 0.5 - this.platypus.posX;
        double d2 = digPos.getZ() + 0.5 - this.platypus.posZ;
        float f = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) - 90.0F;
        if (dist < 2) {
            platypus.motionY -= 0.01D;
            platypus.getNavigator().clearPath();
            digTime++;
            if (digTime % 5 == 0) {
                SoundEvent sound = platypus.world.getBlockState(digPos).getBlock().getSoundType().getHitSound();
                platypus.playSound(sound, 1, 0.5F + platypus.getRNG().nextFloat() * 0.5F);
            }
            int itemDivis = (int) Math.floor(100F / maxDroppedItems);
            if (digTime % itemDivis == 0) {
                List<ItemStack> lootList = getItemStacks(platypus);
                if (lootList.size() > 0) {
                    for (ItemStack stack : lootList) {
                        EntityItem e = this.platypus.entityDropItem(stack.copy(), 0.0F);
                        e.isAirBorne = true;
                        e.motionX *= 0.2D;
                        e.motionY *= 0.2D;
                        e.motionZ *= 0.2D;
                    }
                }
            }
            if (digTime >= 100) {
                platypus.setSensing(false);
                platypus.setDigging(false);
                digTime = 0;
            } else {
                platypus.setDigging(true);
            }
        } else {
            platypus.setDigging(false);
            platypus.getNavigator().tryMoveToXYZ(digPos.getX(), digPos.getY() + 1, digPos.getZ(), 1);
            platypus.rotationYaw = f;
        }
    }

    @Override
    public void resetTask() {
        generatePosCooldown = 0;
        platypus.setSensing(false);
        platypus.setDigging(false);
        digPos = null;
        digTime = 0;
    }

    private BlockPos genSeafloorPos(BlockPos parent) {
        World world = platypus.world;
        Random random = new Random();
        int range = 15;
        for (int i = 0; i < 15; i++) {
            BlockPos seafloor = parent.add(random.nextInt(range) - range / 2, 0, random.nextInt(range) - range / 2);
            while (world.getBlockState(seafloor).getMaterial() == Material.WATER && seafloor.getY() > 1) {
                seafloor = seafloor.down();
            }
            IBlockState state = world.getBlockState(seafloor);
            if (state.getBlock() == Blocks.CLAY) {
                return seafloor;
            }
        }
        return null;
    }

    private BlockPos genDigPos() {
        Random random = new Random();
        int range = 15;
        if (platypus.isInWater()) {
            return genSeafloorPos(this.platypus.getPosition());
        } else {
            for (int i = 0; i < 15; i++) {
                BlockPos blockpos1 = this.platypus.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
                while (this.platypus.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                    blockpos1 = blockpos1.down();
                }
                if (this.platypus.world.getBlockState(blockpos1).getMaterial() == Material.WATER) {
                    BlockPos pos3 = genSeafloorPos(blockpos1);
                    if (pos3 != null) {
                        return pos3;
                    }
                }
            }
        }
        return null;
    }
}
