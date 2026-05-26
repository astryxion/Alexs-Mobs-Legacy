package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * 1.16 used {@code GlobalLootModifier}; on 1.12 the same math runs from {@link com.github.alexthe666.alexsmobs.CommonProxy#onHarvestDrops}.
 */
public final class BananaLootModifier {

    private BananaLootModifier() {
    }

    public static boolean shouldProcessForBlock(Block block) {
        return AMConfig.bananasDropFromLeaves && AMTagRegistry.BLOCKS_DROPPING_BANANAS.contains(block);
    }

    /**
     * Preserves 1.16 {@code doApply} behavior (tool checks, fortune scaling, RNG).
     */
    public static void applyToHarvestDrops(List<ItemStack> generatedLoot, World world, EntityPlayer harvester,
            ItemStack ctxTool, boolean silkTouchingFromWorld, int fortuneLevelFromEvent) {
        if (!AMConfig.bananasDropFromLeaves) {
            return;
        }
        Random random = world.rand;
        int silkTouch = ctxTool.isEmpty() ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, ctxTool);
        boolean shears = !ctxTool.isEmpty() && (ctxTool.getItem() instanceof ItemShears || ctxTool.getItem() == Items.SHEARS);
        if (silkTouchingFromWorld || silkTouch > 0 || shears) {
            return;
        }
        int bonusLevel = ctxTool.isEmpty() ? fortuneLevelFromEvent : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, ctxTool);
        int bananaStep = (int) Math.min(AMConfig.bananaChance * 0.1F, 0);
        int bananaRarity = AMConfig.bananaChance - (bonusLevel * bananaStep);
        if (bananaRarity < 1 || random.nextInt(bananaRarity) == 0) {
            generatedLoot.add(new ItemStack(AMItemRegistry.BANANA));
        }
    }
}
