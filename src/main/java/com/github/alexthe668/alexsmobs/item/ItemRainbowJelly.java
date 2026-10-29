package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.entity.util.RainbowUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class ItemRainbowJelly extends ItemFood {

    public ItemRainbowJelly() {
        super(1, 0.2F, false);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:rainbow_jelly");
        this.setAlwaysEdible();
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand) {
        int type = RainbowUtil.getRainbowTypeFromStack(stack);
        if (RainbowUtil.getRainbowType(target) == type) {
            return false;
        }
        RainbowUtil.setRainbowType(target, type);
        for (int j = 0; j < 6 + playerIn.getRNG().nextInt(3); j++) {
            double d2 = playerIn.getRNG().nextGaussian() * 0.02D;
            double d0 = playerIn.getRNG().nextGaussian() * 0.02D;
            double d1 = playerIn.getRNG().nextGaussian() * 0.02D;
            target.world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
                    target.posX + (double) (playerIn.getRNG().nextFloat() * target.width) - (double) target.width * 0.5F,
                    target.posY + target.height * 0.5F + (double) (playerIn.getRNG().nextFloat() * target.height * 0.5F),
                    target.posZ + (double) (playerIn.getRNG().nextFloat() * target.width) - (double) target.width * 0.5F,
                    d0, d1, d2,
                    net.minecraft.item.Item.getIdFromItem(stack.getItem()), stack.getMetadata());
        }
        target.world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_SMALL_SLIME_SQUISH, target.getSoundCategory(), 1.0F, 1.0F);
        if (!playerIn.capabilities.isCreativeMode) {
            stack.shrink(1);
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.capabilities.isCreativeMode || player.canEat(true)) {
            player.setActiveHand(hand);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving) {
        RainbowUtil.setRainbowType(entityLiving, RainbowUtil.getRainbowTypeFromStack(stack));
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return super.hasEffect(stack) || RainbowUtil.getRainbowTypeFromStack(stack) > 1;
    }

    public enum RainbowType {
        RAINBOW, TRANS, NONBI, BI, ACE, WEEZER, BRAZIL;

        public static RainbowType getFromString(String name) {
            if (name.contains("nonbi") || name.contains("non-bi")) {
                return NONBI;
            } else if (name.contains("trans")) {
                return TRANS;
            } else if (name.contains("bi")) {
                return BI;
            } else if (name.contains("asexual") || name.contains("ace")) {
                return ACE;
            } else if (name.contains("weezer")) {
                return WEEZER;
            } else if (name.contains("brazil")) {
                return BRAZIL;
            }
            return RAINBOW;
        }
    }
}
