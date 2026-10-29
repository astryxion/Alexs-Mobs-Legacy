package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.alexsmobs.entity.util.TerrapinTypes;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Shows the two parent terrapin variants stored on a silk-touched egg.
 */
public class ItemTerrapinEgg extends ItemBlock {

    public ItemTerrapinEgg(net.minecraft.block.Block block) {
        super(block);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey("BlockEntityTag", 10)) {
            return;
        }
        NBTTagCompound blockTag = tag.getCompoundTag("BlockEntityTag");
        if (!blockTag.hasKey("Parent1Data", 10) || !blockTag.hasKey("Parent2Data", 10)) {
            return;
        }
        TerrapinTypes parent1 = typeFrom(blockTag.getCompoundTag("Parent1Data"));
        TerrapinTypes parent2 = typeFrom(blockTag.getCompoundTag("Parent2Data"));
        String s1 = I18n.translateToLocal(parent1.getTranslationName());
        String s2 = I18n.translateToLocal(parent2.getTranslationName());
        tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("tile.alexsmobs.terrapin_egg.desc", s1, s2));
    }

    private static TerrapinTypes typeFrom(NBTTagCompound tag) {
        int index = MathHelper.clamp(tag.getInteger("TerrapinType"), 0, TerrapinTypes.values().length - 1);
        return TerrapinTypes.values()[index];
    }
}
