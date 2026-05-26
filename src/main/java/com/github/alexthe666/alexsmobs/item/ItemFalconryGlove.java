package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.message.MessageMosquitoDismount;
import com.github.alexthe666.alexsmobs.message.MessageSyncEntityPos;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.List;
import com.google.common.base.Optional;

public class ItemFalconryGlove extends Item {

    public ItemFalconryGlove() {
        setMaxStackSize(1);
        setCreativeTab(AlexsMobs.TAB);
    }

    public static void onLeftClick(EntityPlayer playerIn, ItemStack stack) {
        if (stack.getItem() == AMItemRegistry.FALCONRY_GLOVE) {
            float dist = 128;
            Vec3d eyePos = playerIn.getPositionEyes(1.0F);
            Vec3d look = playerIn.getLook(1.0F);
            Vec3d endPos = eyePos.addVector(look.x * dist, look.y * dist, look.z * dist);
            double d1 = dist;
            Entity pointedEntity = null;
            List<Entity> list = playerIn.world.getEntitiesInAABBexcluding(playerIn,
                    playerIn.getEntityBoundingBox().expand(look.x * dist, look.y * dist, look.z * dist).grow(1.0D, 1.0D, 1.0D),
                    new Predicate<Entity>() {
                        public boolean apply(@Nullable Entity entity) {
                            return entity != null && entity.canBeCollidedWith() && (entity instanceof EntityPlayer || (entity instanceof EntityLivingBase));
                        }
                    });
            double d2 = d1;
            for (int j = 0; j < list.size(); ++j) {
                Entity entity1 = list.get(j);
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow(entity1.getCollisionBorderSize());
                RayTraceResult trace = axisalignedbb.calculateIntercept(eyePos, endPos);
                Optional<Vec3d> optional = trace == null || trace.hitVec == null ? Optional.absent() : Optional.of(trace.hitVec);

                if (axisalignedbb.contains(eyePos)) {
                    if (d2 >= 0.0D) {
                        d2 = 0.0D;
                    }
                } else if (optional.isPresent()) {
                    double d3 = eyePos.distanceTo(optional.get());

                    if (d3 < d2 || d2 == 0.0D) {
                        if (entity1.getLowestRidingEntity() == playerIn.getLowestRidingEntity() && !playerIn.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity1;
                            }
                        } else {
                            pointedEntity = entity1;
                            d2 = d3;
                        }
                    }
                }
            }

            if (!playerIn.getPassengers().isEmpty()) {
                for (Entity entity : playerIn.getPassengers()) {
                    if (entity instanceof EntityBaldEagle) {
                        EntityBaldEagle eagle = (EntityBaldEagle) entity;
                        eagle.setLaunched(true);
                        eagle.dismountRidingEntity();
                        eagle.setSitting(false);
                        eagle.setCommand(0);
                        eagle.setLocationAndAngles(playerIn.posX, playerIn.posY + (double) playerIn.getEyeHeight(), playerIn.posZ, eagle.rotationYaw, eagle.rotationPitch);
                        if (eagle.world.isRemote) {
                            AlexsMobs.sendMSGToServer(new MessageSyncEntityPos(eagle.getEntityId(), playerIn.posX, playerIn.posY + (double) playerIn.getEyeHeight(), playerIn.posZ));
                        } else {
                            AlexsMobs.sendMSGToAll(new MessageSyncEntityPos(eagle.getEntityId(), playerIn.posX, playerIn.posY + (double) playerIn.getEyeHeight(), playerIn.posZ));
                        }
                        if (eagle.hasCap()) {
                            eagle.setFlying(true);
                            eagle.getMoveHelper().setMoveTo(eagle.posX, eagle.posY, eagle.posZ, 0.1F);
                            if (eagle.world.isRemote) {
                                AlexsMobs.sendMSGToServer(new MessageMosquitoDismount(eagle.getEntityId(), playerIn.getEntityId()));
                            }
                            AlexsMobs.PROXY.setRenderViewEntity(eagle);
                        } else {
                            eagle.getNavigator().clearPath();
                            eagle.getMoveHelper().setMoveTo(eagle.posX, eagle.posY, eagle.posZ, 0.1F);
                            if (pointedEntity != null && !eagle.isOnSameTeam(pointedEntity)) {
                                eagle.setFlying(true);
                                if (pointedEntity instanceof EntityLivingBase) {
                                    eagle.setAttackTarget((EntityLivingBase) pointedEntity);
                                }
                            } else {
                                eagle.setFlying(false);
                                eagle.setCommand(2);
                                eagle.setSitting(true);
                            }
                        }
                    }
                }
            }
        }
    }

}
