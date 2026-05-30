package com.github.alexthe666.alexsmobs.item;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.entity.util.RainbowUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemRainbowJelly extends ItemFood {

    public ItemRainbowJelly() {
        super(1, 0.2F, false);
        this.setCreativeTab(AlexsMobs.TAB);
        this.setRegistryName("alexsmobs:rainbow_jelly");
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
