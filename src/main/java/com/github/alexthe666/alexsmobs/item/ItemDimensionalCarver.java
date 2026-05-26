package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityVoidPortal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemDimensionalCarver extends Item {

    public static final int MAX_TIME = 200;

    public ItemDimensionalCarver() {
        setCreativeTab(AlexsMobs.TAB);
        setMaxDamage(4);
        setMaxStackSize(1);
    }

    protected static RayTraceResult rayTracePortal(World worldIn, EntityPlayer player) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        Vec3d vector3d = player.getPositionEyes(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = 1.5F;
        Vec3d vector3d1 = vector3d.addVector(f6 * d0, f5 * d0, f7 * d0);
        return worldIn.rayTraceBlocks(vector3d, vector3d1, true, false, false);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (itemstack.getItemDamage() >= itemstack.getMaxDamage() - 1) {
            return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
        playerIn.setActiveHand(handIn);
        RayTraceResult raytraceresult = rayTracePortal(worldIn, playerIn);
        EnumFacing dir = playerIn.getHorizontalFacing();
        double x;
        double y;
        double z;
        if (raytraceresult != null && raytraceresult.hitVec != null) {
            x = raytraceresult.hitVec.x - dir.getFrontOffsetX() * 0.1F;
            y = raytraceresult.hitVec.y - dir.getFrontOffsetY() * 0.1F;
            z = raytraceresult.hitVec.z - dir.getFrontOffsetZ() * 0.1F;
        } else {
            Vec3d look = playerIn.getLook(1.0F);
            x = playerIn.posX + look.x * 1.5D;
            y = playerIn.posY + playerIn.getEyeHeight() + look.y * 1.5D;
            z = playerIn.posZ + look.z * 1.5D;
        }
        if (itemstack.getTagCompound() != null && itemstack.getTagCompound().getBoolean("HASBLOCK")) {
            x = itemstack.getTagCompound().getDouble("BLOCKX");
            y = itemstack.getTagCompound().getDouble("BLOCKY");
            z = itemstack.getTagCompound().getDouble("BLOCKZ");
        } else {
            if (itemstack.getTagCompound() == null) {
                itemstack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            }
            itemstack.getTagCompound().setBoolean("HASBLOCK", true);
            itemstack.getTagCompound().setDouble("BLOCKX", x);
            itemstack.getTagCompound().setDouble("BLOCKY", y);
            itemstack.getTagCompound().setDouble("BLOCKZ", z);
        }
        if (worldIn.isRemote) {
            AMParticleRegistry.spawnParticle(worldIn, AMParticleRegistry.INVERT_DIG, x, y, z, playerIn.getEntityId(), 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return MAX_TIME;
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return 100F;
    }

    @Override
    public void onUsingTick(ItemStack itemstack, EntityLivingBase player, int count) {
        player.swingArm(player.getActiveHand());
        if (count % 5 == 0) {
            player.playSound(SoundEvents.BLOCK_ANVIL_HIT, 1, 0.5F + itemRand.nextFloat());
        }
        boolean flag = false;
        if (itemstack.getTagCompound() != null && itemstack.getTagCompound().getBoolean("HASBLOCK")) {
            double x = itemstack.getTagCompound().getDouble("BLOCKX");
            double y = itemstack.getTagCompound().getDouble("BLOCKY");
            double z = itemstack.getTagCompound().getDouble("BLOCKZ");
            if (itemRand.nextFloat() < 0.2F && player.world.isRemote) {
                AMParticleRegistry.spawnParticle(player.world, AMParticleRegistry.WORM_PORTAL, x + itemRand.nextGaussian() * 0.1F, y + itemRand.nextGaussian() * 0.1F, z + itemRand.nextGaussian() * 0.1F, itemRand.nextGaussian() * 0.1F, -0.1F, itemRand.nextGaussian() * 0.1F);
            }
            if (player.getDistanceSq(x, y, z) > 9) {
                flag = true;
                if (player instanceof EntityPlayer) {
                    ((EntityPlayer) player).getCooldownTracker().setCooldown(this, 40);
                }
            }
            if (count == 1 && !player.world.isRemote) {
                player.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1, 0.5F);
                EntityVoidPortal portal = (EntityVoidPortal) AMEntityRegistry.VOID_PORTAL.newInstance(player.world);
                portal.setPosition(x, y, z);
                EnumFacing dir = player.getHorizontalFacing().getOpposite();
                if (dir == EnumFacing.UP) {
                    dir = EnumFacing.DOWN;
                }
                portal.setAttachmentFacing(dir);
                portal.setLifespan(1200);
                BlockPos respawnPosition = player instanceof EntityPlayer ? ((EntityPlayer) player).getBedLocation() : null;
                if (respawnPosition == null) {
                    respawnPosition = player.world.getSpawnPoint();
                }
                player.world.spawnEntity(portal);
                portal.setDestination(respawnPosition.up(2));
                itemstack.damageItem(1, player);
                flag = true;
                if (player instanceof EntityPlayer) {
                    ((EntityPlayer) player).getCooldownTracker().setCooldown(this, 200);
                }
            }
        }
        if (flag) {
            player.resetActiveHand();
            if (itemstack.getTagCompound() != null) {
                itemstack.getTagCompound().setBoolean("HASBLOCK", false);
                itemstack.getTagCompound().setDouble("BLOCKX", 0);
                itemstack.getTagCompound().setDouble("BLOCKY", 0);
                itemstack.getTagCompound().setDouble("BLOCKZ", 0);
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (stack.getTagCompound() != null) {
            stack.getTagCompound().setBoolean("HASBLOCK", false);
            stack.getTagCompound().setDouble("BLOCKX", 0);
            stack.getTagCompound().setDouble("BLOCKY", 0);
            stack.getTagCompound().setDouble("BLOCKZ", 0);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.areItemStacksEqual(oldStack, newStack);
    }
}
