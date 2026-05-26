package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * 1.12 replacement for 1.16 {@code SpawnEggItem}; mirrors vanilla {@link net.minecraft.item.ItemMonsterPlacer}
 * spawn ordering (Forge {@code doSpecialSpawn}, {@code EntityLiving#onInitialSpawn}, then {@code World#spawnEntity}).
 */
public class ItemAMSpawnEgg extends Item {

    private final EntityEntry entityEntry;
    private final int primaryColor;
    private final int secondaryColor;

    public ItemAMSpawnEgg(EntityEntry entityEntry, int primaryColor, int secondaryColor) {
        this.entityEntry = entityEntry;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.setMaxStackSize(64);
        this.setCreativeTab(AlexsMobs.TAB);
    }

    public EntityEntry getEntry() {
        return entityEntry;
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return renderPass == 0 ? this.primaryColor : this.secondaryColor;
    }

    @Override
    @Nonnull
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return EnumActionResult.FAIL;
        }
        Entity entity = this.entityEntry.newInstance(world);
        if (entity == null) {
            return EnumActionResult.FAIL;
        }
        BlockPos spawnPos = pos.offset(facing);
        double x = spawnPos.getX() + 0.5D;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5D;
        entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            living.rotationYawHead = living.rotationYaw;
            living.renderYawOffset = living.rotationYaw;
            if (ForgeEventFactory.doSpecialSpawn(living, world, (float) x, (float) y, (float) z, null)) {
                entity.setDead();
                return EnumActionResult.FAIL;
            }
            living.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(living)), (IEntityLivingData) null);
            if (living instanceof EntityAgeable) {
                ((EntityAgeable) living).setGrowingAge(0);
            }
        }
        if (!world.spawnEntity(entity)) {
            entity.setDead();
            return EnumActionResult.FAIL;
        }
        if (entity instanceof EntityLiving) {
            ((EntityLiving) entity).playLivingSound();
        }
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        player.swingArm(hand);
        return EnumActionResult.SUCCESS;
    }
}
