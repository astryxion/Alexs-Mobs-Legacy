package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.item.Item;
import net.minecraft.init.Items;

import java.util.HashSet;
import java.util.Set;

/**
 * Same item union as 1.16 merged ingredient: rotten flesh, fish oil,
 * plus all items from {@link AMTagRegistry#BALD_EAGLE_TAMEABLES}.
 */
public class EagleTemptMerged extends EntityAITempt {

    public EagleTemptMerged(EntityBaldEagle eagle) {
        super(eagle, 1.1D, true, mergedItems());
    }

    private static Set<Item> mergedItems() {
        Set<Item> set = new HashSet<>();
        set.add(Items.ROTTEN_FLESH);
        set.add(AMItemRegistry.FISH_OIL);
        Set<Item> tag = AMTagRegistry.ITEM_TAG_SETS.get(AMTagRegistry.BALD_EAGLE_TAMEABLES);
        if (tag != null) {
            set.addAll(tag);
        }
        return set;
    }
}
