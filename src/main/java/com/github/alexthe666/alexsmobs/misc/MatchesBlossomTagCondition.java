package com.github.alexthe666.alexsmobs.misc;

import net.minecraft.block.Block;

/**
 * 1.16 implemented {@code ILootCondition} for loot modifiers. On 1.12, block checks use {@link AMTagRegistry#BLOCKS_DROPPING_ACACIA_BLOSSOMS}.
 */
public final class MatchesBlossomTagCondition {

    private MatchesBlossomTagCondition() {
    }

    public static boolean matchesBlock(Block block) {
        return block != null && AMTagRegistry.BLOCKS_DROPPING_ACACIA_BLOSSOMS.contains(block);
    }
}
