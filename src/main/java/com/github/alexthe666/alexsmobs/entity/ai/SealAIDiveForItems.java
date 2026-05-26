package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntitySeal;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
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
 * Forge 1.12.2 port of 1.16 {@code SealAIDiveForItems}.
 */
public class SealAIDiveForItems extends EntityAIBase {

    public static final ResourceLocation SEAL_REWARD = new ResourceLocation("alexsmobs", "gameplay/seal_reward");

    private final EntitySeal seal;
    private EntityPlayer thrower;
    private BlockPos digPos;
    private boolean returnToPlayer = false;
    private int digTime = 0;

    public SealAIDiveForItems(EntitySeal seal) {
        this.seal = seal;
        this.setMutexBits(3);
    }

    private static List<ItemStack> getItemStacks(EntitySeal seal) {
        if (!(seal.world instanceof WorldServer)) {
            return java.util.Collections.emptyList();
        }
        WorldServer sw = (WorldServer) seal.world;
        LootTable loottable = sw.getLootTableManager().getLootTableFromLocation(SEAL_REWARD);
        return loottable.generateLootForPools(seal.getRNG(), new LootContext.Builder(sw).withLootedEntity(seal).build());
    }

    @Override
    public boolean shouldExecute() {
        if (seal.feederUUID == null || seal.world.getPlayerEntityByUUID(seal.feederUUID) == null || seal.revengeCooldown > 0) {
            return false;
        }
        thrower = seal.world.getPlayerEntityByUUID(seal.feederUUID);
        digPos = genDigPos();
        return thrower != null && digPos != null;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return seal.getAttackTarget() == null && seal.revengeCooldown == 0 && seal.getRevengeTarget() == null
                && thrower != null && seal.feederUUID != null && digPos != null
                && seal.world.getBlockState(digPos.up()).getMaterial() == Material.WATER;
    }

    @Override
    public void updateTask() {
        seal.setBasking(false);
        if (returnToPlayer) {
            seal.getNavigator().tryMoveToEntityLiving(thrower, 1D);
            if (seal.getDistance(thrower) < 2D) {
                ItemStack stack = seal.getHeldItemMainhand().copy();
                seal.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                EntityItem item = seal.entityDropItem(stack, 0.0F);
                if (item != null) {
                    double d0 = thrower.posX - this.seal.posX;
                    double d1 = thrower.getPositionEyes(1.0F).y - this.seal.getPositionEyes(1.0F).y;
                    double d2 = thrower.posZ - this.seal.posZ;
                    double lvt_7_1_ = MathHelper.sqrt(d0 * d0 + d2 * d2);
                    float pitch = (float) (-(MathHelper.atan2(d1, lvt_7_1_) * 57.2957763671875D));
                    float yaw = (float) (MathHelper.atan2(d2, d0) * (double) (180F / (float) Math.PI)) - 90.0F;
                    float f8 = MathHelper.sin(pitch * ((float) Math.PI / 180F));
                    float f2 = MathHelper.cos(pitch * ((float) Math.PI / 180F));
                    float f3 = MathHelper.sin(yaw * ((float) Math.PI / 180F));
                    float f4 = MathHelper.cos(yaw * ((float) Math.PI / 180F));
                    float f5 = seal.getRNG().nextFloat() * ((float) Math.PI * 2F);
                    float f6 = 0.02F * seal.getRNG().nextFloat();
                    item.motionX = (double) (-f3 * f2 * 0.5F) + Math.cos(f5) * (double) f6;
                    item.motionY = -f8 * 0.2F + 0.1F + (seal.getRNG().nextFloat() - seal.getRNG().nextFloat()) * 0.1F;
                    item.motionZ = (double) (f4 * f2 * 0.5F) + Math.sin(f5) * (double) f6;
                }
                seal.feederUUID = null;
                resetTask();
            }
        } else {
            double dist = seal.getDistanceSq(digPos.getX() + 0.5D, digPos.getY(), digPos.getZ() + 0.5D);
            double d0 = digPos.getX() + 0.5 - this.seal.posX;
            double d2 = digPos.getZ() + 0.5 - this.seal.posZ;
            float f = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) - 90.0F;

            if (dist < 2) {
                seal.getNavigator().clearPath();
                digTime++;
                if (digTime % 5 == 0) {
                    SoundEvent sound = seal.world.getBlockState(digPos).getBlock().getSoundType().getHitSound();
                    seal.playSound(sound, 1, 0.5F + seal.getRNG().nextFloat() * 0.5F);
                }
                if (digTime >= 100) {
                    List<ItemStack> lootList = getItemStacks(seal);
                    if (lootList.size() > 0) {
                        ItemStack copy = lootList.remove(0).copy();
                        this.seal.setHeldItem(EnumHand.MAIN_HAND, copy);
                        for (ItemStack stack : lootList) {
                            this.seal.entityDropItem(stack.copy(), 0.0F);
                        }
                        this.returnToPlayer = true;
                    }
                    seal.setDigging(false);
                    digTime = 0;
                } else {
                    seal.setDigging(true);
                }
            } else {
                seal.setDigging(false);
                seal.getNavigator().tryMoveToXYZ(digPos.getX(), digPos.getY(), digPos.getZ(), 1);
                seal.rotationYaw = f;
            }
        }
    }

    @Override
    public void resetTask() {
        seal.setDigging(false);
        digPos = null;
        thrower = null;
        digTime = 0;
        returnToPlayer = false;
        seal.fishFeedings = 0;
        if (!seal.getHeldItemMainhand().isEmpty()) {
            seal.entityDropItem(seal.getHeldItemMainhand().copy(), 0.0F);
            seal.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private BlockPos genSeafloorPos(BlockPos parent) {
        World world = seal.world;
        Random random = new Random();
        int range = 15;
        for (int i = 0; i < 15; i++) {
            BlockPos seafloor = parent.add(random.nextInt(range) - range / 2, 0, random.nextInt(range) - range / 2);
            while (world.getBlockState(seafloor).getMaterial() == Material.WATER && seafloor.getY() > 1) {
                seafloor = seafloor.down();
            }
            IBlockState state = world.getBlockState(seafloor);
            if (AMTagRegistry.blockInTag(AMTagRegistry.SEAL_DIGABLES, state.getBlock())) {
                return seafloor;
            }
        }
        return null;
    }

    private BlockPos genDigPos() {
        Random random = new Random();
        int range = 15;
        if (seal.isInWater()) {
            return genSeafloorPos(this.seal.getPosition());
        } else {
            for (int i = 0; i < 15; i++) {
                BlockPos blockpos1 = this.seal.getPosition().add(random.nextInt(range) - range / 2, 3, random.nextInt(range) - range / 2);
                while (this.seal.world.isAirBlock(blockpos1) && blockpos1.getY() > 1) {
                    blockpos1 = blockpos1.down();
                }
                if (this.seal.world.getBlockState(blockpos1).getMaterial() == Material.WATER) {
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
