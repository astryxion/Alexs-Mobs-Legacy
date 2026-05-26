package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.EntityStraddleboard;
import com.google.common.base.Predicate;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Port of 1.16's straddleboard item: raycast placement, passenger interference check,
 * dye color in {@code display.color}, and enchanting exclusions (unbreaking, mending).
 */
public class ItemStraddleboard extends Item {

    private static final Predicate<Entity> PLACEMENT_ENTITY_PREDICATE = new Predicate<Entity>() {
        @Override
        public boolean apply(@Nullable Entity entity) {
            if (entity == null) {
                return false;
            }
            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSpectator()) {
                return false;
            }
            return entity.canBeCollidedWith();
        }
    };

    public ItemStraddleboard() {
        this.setMaxDamage(220);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setMaxStackSize(1);
    }

    /**
     * Matches dyeable item semantics ({@code display.color} present).
     */
    public boolean hasColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("display", 10) && tag.getCompoundTag("display").hasKey("color", 3);
    }

    public int getColor(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("display", 10)) {
            NBTTagCompound display = tag.getCompoundTag("display");
            if (display.hasKey("color", 3)) {
                return display.getInteger("color");
            }
        }
        return 0XADC3D7;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (!super.canApplyAtEnchantingTable(stack, enchantment)) {
            return false;
        }
        Enchantment unbreaking = Enchantment.getEnchantmentByID(34);
        Enchantment mending = Enchantment.getEnchantmentByID(70);
        return enchantment != unbreaking && enchantment != mending;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        RayTraceResult raytraceresult = playerIn.rayTrace(5.0D, 1.0F);
        if (raytraceresult == null || raytraceresult.typeOfHit == RayTraceResult.Type.MISS) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
        } else {
            Vec3d vecLook = playerIn.getLook(1.0F);
            AxisAlignedBB searchBox = playerIn.getEntityBoundingBox().expand(vecLook.x * 5.0D, vecLook.y * 5.0D, vecLook.z * 5.0D).grow(1.0D);
            List<Entity> list = worldIn.getEntitiesInAABBexcluding(playerIn, searchBox, PLACEMENT_ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3d eyePos = playerIn.getPositionEyes(1.0F);

                for (Entity entity : list) {
                    AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
                    if (axisalignedbb.contains(eyePos)) {
                        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
                    }
                }
            }

            if (raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                EntityStraddleboard boatentity = new EntityStraddleboard(worldIn, raytraceresult.hitVec.x, raytraceresult.hitVec.y, raytraceresult.hitVec.z);
                boatentity.setDefaultColor(!this.hasColor(itemstack));
                boatentity.setItemStack(itemstack.copy());
                boatentity.setColor(this.getColor(itemstack));
                boatentity.rotationYaw = playerIn.rotationYaw;
                if (!worldIn.collidesWithAnyBlock(boatentity.getEntityBoundingBox().grow(-0.1D))) {
                    return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
                } else {
                    if (!worldIn.isRemote) {
                        worldIn.spawnEntity(boatentity);
                        if (!playerIn.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }
                    }

                    playerIn.addStat(StatList.getObjectUseStats(this));
                    return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
                }
            } else {
                return new ActionResult<ItemStack>(EnumActionResult.PASS, itemstack);
            }
        }
    }
}
