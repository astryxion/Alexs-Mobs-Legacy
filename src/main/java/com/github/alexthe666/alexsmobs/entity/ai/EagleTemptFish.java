package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.item.Item;
import net.minecraft.init.Items;

import java.util.HashSet;
import java.util.Set;

/** 1.16 {@code ItemTags.FISHES}: raw/cooked fish in 1.12. */
public class EagleTemptFish extends EntityAITempt {

    public EagleTemptFish(EntityBaldEagle eagle, double speedIn, boolean scaredByPlayerMovementIn) {
        super(eagle, speedIn, scaredByPlayerMovementIn, fishItems());
    }

    private static Set<Item> fishItems() {
        Set<Item> set = new HashSet<>();
        set.add(Items.FISH);
        set.add(Items.COOKED_FISH);
        return set;
    }
}
