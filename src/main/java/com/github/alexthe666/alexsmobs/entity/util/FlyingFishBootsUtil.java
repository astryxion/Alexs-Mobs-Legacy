package com.github.alexthe666.alexsmobs.entity.util;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 1.20.1 flying-fish boot leap/glide. Boost ticks are stored in side-local maps because
 * Citadel's {@code LivingEntityMixin} is not injected on 1.12.2, so {@code CitadelEntityData}
 * does not persist on players.
 */
public class FlyingFishBootsUtil {

    private static final int MIN_BOOST_TIME = 35;
    private static final Field IS_JUMPING = ReflectionHelper.findField(EntityLivingBase.class, "isJumping", "field_70703_bu");
    private static final Map<UUID, Integer> SERVER_BOOST = new HashMap<UUID, Integer>();
    private static final Map<UUID, Integer> CLIENT_BOOST = new HashMap<UUID, Integer>();

    public static void setBoostTicks(EntityLivingBase entity, int ticks) {
        Map<UUID, Integer> boosts = boostsFor(entity);
        UUID id = entity.getUniqueID();
        if (ticks <= 0) {
            boosts.remove(id);
        } else {
            boosts.put(id, ticks);
        }
    }

    public static int getBoostTicks(EntityLivingBase entity) {
        Integer ticks = boostsFor(entity).get(entity.getUniqueID());
        return ticks == null ? 0 : ticks;
    }

    public static boolean isWearing(EntityLivingBase entity) {
        return entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == AMItemRegistry.FLYING_FISH_BOOTS;
    }

    public static void tickFlyingFishBoots(EntityLivingBase fishy) {
        int boostTime = getBoostTicks(fishy);
        if (boostTime <= 15 && inWater(fishy) && !fishy.onGround) {
            boolean creativeFly = fishy instanceof EntityPlayer && ((EntityPlayer) fishy).capabilities.isFlying;
            if (nearWaterSurface(fishy) && isHoldingJump(fishy) && !creativeFly) {
                boostTime = MIN_BOOST_TIME;
                float yaw = -fishy.rotationYawHead * ((float) Math.PI / 180F);
                float pitch = -fishy.rotationPitch * ((float) Math.PI / 180F);
                double dist = 0.5D + fishy.getRNG().nextFloat() * 1.2D;
                Vec3d forward = new Vec3d(0.0D, 0.0D, dist);
                forward = rotatePitch(forward, pitch);
                forward = rotateYaw(forward, yaw);
                fishy.motionX += forward.x;
                fishy.motionY = 0.3D + fishy.getRNG().nextFloat() * 0.3D;
                fishy.motionZ += forward.z;
                fishy.rotationYaw = fishy.rotationYawHead;
                fishy.velocityChanged = true;
            }
        }
        if (boostTime > 0) {
            if (!inWater(fishy) && !fishy.onGround) {
                if (fishy.motionY < 0.0D) {
                    fishy.motionY *= 0.75D;
                    fishy.velocityChanged = true;
                }
            }
            setBoostTicks(fishy, boostTime - 1);
        }
    }

    private static Map<UUID, Integer> boostsFor(EntityLivingBase entity) {
        return entity.world.isRemote ? CLIENT_BOOST : SERVER_BOOST;
    }

    /**
     * 1.20 uses {@code living.jumping}. Water jump only adds 0.04 motionY in 1.12, so a
     * motionY threshold never fires. Read the jump flag, and on the client also check
     * the actual jump key so the local player launches.
     */
    private static boolean isHoldingJump(EntityLivingBase entity) {
        if (entity.world.isRemote && entity instanceof EntityPlayer) {
            if (AlexsMobs.PROXY.isPlayerJumping((EntityPlayer) entity)) {
                return true;
            }
        }
        try {
            return IS_JUMPING.getBoolean(entity);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static boolean inWater(EntityLivingBase entity) {
        if (entity.isInWater()) {
            return true;
        }
        BlockPos feet = new BlockPos(entity.posX, entity.posY, entity.posZ);
        return entity.world.getBlockState(feet).getMaterial() == Material.WATER;
    }

    /** 1.20: {@code getFluidHeight(WATER) < 0.4F} — only launch when barely submerged. */
    private static boolean nearWaterSurface(EntityLivingBase entity) {
        BlockPos upper = new BlockPos(entity.posX, entity.posY + 0.4D, entity.posZ);
        return entity.world.getBlockState(upper).getMaterial() != Material.WATER;
    }

    private static Vec3d rotateYaw(Vec3d vec, float yaw) {
        float cos = MathHelper.cos(yaw);
        float sin = MathHelper.sin(yaw);
        return new Vec3d(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static Vec3d rotatePitch(Vec3d vec, float pitch) {
        float cos = MathHelper.cos(pitch);
        float sin = MathHelper.sin(pitch);
        return new Vec3d(vec.x, vec.y * cos + vec.z * sin, vec.z * cos - vec.y * sin);
    }
}
