package com.github.alexthe666.alexsmobs.entity;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * Tipped-arrow behavior (potion from stack) with shark-tooth item as pickup and extra aquatic damage like 1.16.
 */
public class EntitySharkToothArrow extends EntityTippedArrow {

    public EntitySharkToothArrow(World worldIn) {
        super(worldIn);
    }

    public EntitySharkToothArrow(World worldIn, double x, double y, double z) {
        super(worldIn);
        this.setPosition(x, y, z);
    }

    public EntitySharkToothArrow(World worldIn, EntityLivingBase shooter) {
        super(worldIn, shooter);
        if (shooter instanceof EntityPlayer) {
            this.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
        }
    }

    @Override
    public ItemStack getArrowStack() {
        return new ItemStack(AMItemRegistry.SHARK_TOOTH_ARROW);
    }

    /**
     * Same threshold and durability math as vanilla shield-break interaction; uses 1.12 durability API.
     */
    protected void damageShield(EntityPlayer player, float damage) {
        if (damage < 3.0F) {
            return;
        }
        ItemStack activeStack = player.getActiveItemStack();
        if (!player.isHandActive() || !(activeStack.getItem() instanceof ItemShield)) {
            return;
        }
        ItemStack copyBeforeUse = activeStack.copy();
        int i = 1 + MathHelper.floor(damage);
        int newDmg = activeStack.getItemDamage() + i;
        if (newDmg >= activeStack.getMaxDamage()) {
            activeStack.setCount(0);
        } else {
            activeStack.setItemDamage(newDmg);
        }
        if (activeStack.isEmpty()) {
            EnumHand hand = player.getActiveHand();
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
            EntityEquipmentSlot slot = hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND;
            player.setItemStackToSlot(slot, ItemStack.EMPTY);
            player.resetActiveHand();
            player.world.playSound(null, player.posX, player.posY, player.posZ,
                    net.minecraft.init.SoundEvents.ITEM_SHIELD_BREAK, SoundCategory.PLAYERS, 0.8F, 0.8F + player.world.rand.nextFloat() * 0.4F);
        }
    }

    @Override
    protected void onHit(RayTraceResult result) {
        if (result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) result.entityHit;
            if (living instanceof EntityPlayer) {
                this.damageShield((EntityPlayer) living, (float) this.getDamage());
            }
            Entity shooterEntity = this.shootingEntity;
            boolean waterMob = living.canBreatheUnderwater()
                    || (living instanceof EntityZombie && living.isInWater())
                    || (living.getCreatureAttribute() != EnumCreatureAttribute.UNDEAD && living.isInsideOfMaterial(net.minecraft.block.material.Material.WATER));
            if (waterMob) {
                DamageSource damagesource;
                if (shooterEntity == null) {
                    damagesource = DamageSource.causeArrowDamage(this, this);
                } else {
                    damagesource = DamageSource.causeArrowDamage(this, shooterEntity);
                }
                living.attackEntityFrom(damagesource, 7);
            }
        }
        super.onHit(result);
    }

    @Override
    public boolean isInWater() {
        return false;
    }
}
