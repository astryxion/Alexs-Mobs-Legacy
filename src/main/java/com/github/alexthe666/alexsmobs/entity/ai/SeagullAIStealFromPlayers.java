package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.EntitySeagull;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class SeagullAIStealFromPlayers extends EntityAIBase {

    private final EntitySeagull seagull;
    private Vec3d fleeVec = null;
    private EntityPlayer target;
    private int fleeTime = 0;

    public SeagullAIStealFromPlayers(EntitySeagull entitySeagull) {
        this.setMutexBits(3);
        this.seagull = entitySeagull;
    }

    @Override
    public boolean shouldExecute() {
        long worldTime = this.seagull.world.getTotalWorldTime() % 10;
        if (this.seagull.getIdleTime() >= 100 && worldTime != 0 || seagull.isSitting() || !AMConfig.seagullStealing) {
            return false;
        }
        if (this.seagull.getRNG().nextInt(12) != 0 && worldTime != 0 || seagull.stealCooldown > 0) {
            return false;
        }
        if (this.seagull.getHeldItemMainhand().isEmpty()) {
            EntityPlayer valid = getClosestValidPlayer();
            if (valid != null) {
                target = valid;
                return true;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.seagull.aiItemFlag = true;
    }

    @Override
    public void resetTask() {
        this.seagull.aiItemFlag = false;
        target = null;
        fleeVec = null;
        fleeTime = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return target != null && !target.capabilities.isCreativeMode && (seagull.getHeldItemMainhand().isEmpty() || fleeTime > 0);
    }

    @Override
    public void updateTask() {
        seagull.setFlying(true);
        seagull.getMoveHelper().setMoveTo(target.posX, target.posY + target.getEyeHeight(), target.posZ, 1.2F);
        if (seagull.getDistance(target) < 2F && seagull.getHeldItemMainhand().isEmpty()) {
            if (hasFoods(target)) {
                ItemStack foodStack = getFoodItemFrom(target);
                if (!foodStack.isEmpty()) {
                    ItemStack copy = foodStack.copy();
                    foodStack.shrink(1);
                    copy.setCount(1);
                    seagull.peck();
                    seagull.setHeldItem(EnumHand.MAIN_HAND, copy);
                    fleeTime = 60;
                    seagull.stealCooldown = 1500 + seagull.getRNG().nextInt(1500);
                    if (target instanceof EntityPlayerMP) {
                        AMAdvancementTriggerRegistry.SEAGULL_STEAL.trigger((EntityPlayerMP) target);
                    }
                } else {
                    resetTask();
                }
            } else {
                resetTask();
            }
        }
        if (fleeTime > 0) {
            if (fleeVec == null) {
                fleeVec = seagull.getBlockInViewAway(target.getPositionVector(), 4);
            }
            if (fleeVec != null) {
                seagull.setFlying(true);
                seagull.getMoveHelper().setMoveTo(fleeVec.x, fleeVec.y, fleeVec.z, 1.2F);
                if (seagull.getDistanceSq(fleeVec.x, fleeVec.y, fleeVec.z) < 5) {
                    fleeVec = seagull.getBlockInViewAway(fleeVec, 4);
                }
            }
            fleeTime--;
        }
    }

    private EntityPlayer getClosestValidPlayer() {
        List<EntityPlayer> list = seagull.world.getEntitiesWithinAABB(EntityPlayer.class, seagull.getEntityBoundingBox().grow(10, 25, 10), EntitySelectors.CAN_AI_TARGET);
        EntityPlayer closest = null;
        if (!list.isEmpty()) {
            for (EntityPlayer player : list) {
                if ((closest == null || closest.getDistance(seagull) > player.getDistance(seagull)) && hasFoods(player)) {
                    closest = player;
                }
            }
        }
        return closest;
    }

    private boolean hasFoods(EntityPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stackIn = player.inventory.mainInventory.get(i);
            if (isFood(stackIn) && !isBlacklisted(stackIn)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlacklisted(ItemStack stack) {
        for (String str : AMConfig.seagullStealingBlacklist) {
            if (stack.getItem().getRegistryName().toString().equals(str)) {
                return true;
            }
        }
        return false;
    }

    private ItemStack getFoodItemFrom(EntityPlayer player) {
        List<ItemStack> foods = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            ItemStack stackIn = player.inventory.mainInventory.get(i);
            if (isFood(stackIn) && !isBlacklisted(stackIn)) {
                foods.add(stackIn);
            }
        }
        if (!foods.isEmpty()) {
            return foods.get(foods.size() <= 1 ? 0 : seagull.getRNG().nextInt(foods.size()));
        }
        return ItemStack.EMPTY;
    }

    private boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFood;
    }
}
