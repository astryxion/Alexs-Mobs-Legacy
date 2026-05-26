package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.entity.EntitySquidGrapple;
import com.github.alexthe666.alexsmobs.entity.util.SquidGrappleUtil;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSquidGrapple extends Item {

    public ItemSquidGrapple() {
        this.setMaxDamage(256);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase livingEntityIn, int timeLeft) {
        if (livingEntityIn instanceof EntityPlayer && ((EntityPlayer) livingEntityIn).isElytraFlying()) {
            return;
        }
        livingEntityIn.playSound(AMSoundRegistry.GIANT_SQUID_TENTACLE, 1.0F, 1.0F + (livingEntityIn.getRNG().nextFloat() - livingEntityIn.getRNG().nextFloat()) * 0.2F);
        if (!worldIn.isRemote) {
            boolean left = false;
            if (livingEntityIn.getActiveHand() == EnumHand.OFF_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.RIGHT
                    || livingEntityIn.getActiveHand() == EnumHand.MAIN_HAND && livingEntityIn.getPrimaryHand() == EnumHandSide.LEFT) {
                left = true;
            }
            int power = this.getMaxItemUseDuration(stack) - timeLeft;
            EntitySquidGrapple hook = new EntitySquidGrapple(worldIn, livingEntityIn, !left);
            Vec3d vector3d = livingEntityIn.getLook(1.0F);
            hook.shoot(vector3d.x, vector3d.y, vector3d.z, getPowerForTime(power) * 3, 1);
            hook.rotationPitch = livingEntityIn.rotationPitch;
            hook.rotationYaw = livingEntityIn.rotationYaw;
            worldIn.spawnEntity(hook);
            stack.damageItem(1, livingEntityIn);
            SquidGrappleUtil.onFireHook(livingEntityIn, hook.getUniqueID());
        }
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == AMItemRegistry.LOST_TENTACLE;
    }

    public static float getPowerForTime(int p) {
        float f = (float) p / 20.0F;
        f = (f * f + f + f * 2.0F) / 4.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("item.alexsmobs.squid_grapple.desc"));
    }
}
