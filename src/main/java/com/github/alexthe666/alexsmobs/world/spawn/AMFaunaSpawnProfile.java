package com.github.alexthe666.alexsmobs.world.spawn;

import net.minecraft.entity.EnumCreatureType;

/**
 * Spawn weight / group sizes modeled on Familiar Fauna 1.12.2 defaults:
 * butterfly 1/2-4 AMBIENT, dragonfly 1/1-2 AMBIENT, snail/pixie 1/1-1 AMBIENT,
 * deer 8/2-4 CREATURE, turkey 10/3-4 CREATURE.
 * <p>
 * Aquatic mobs use the same FF tiers on {@link net.minecraft.entity.EnumCreatureType#WATER_CREATURE}
 * (FF has no water mobs; squid is vanilla weight 6 — our large whales use pixie/snail, not deer).
 */
public enum AMFaunaSpawnProfile {
    AMBIENT_BUTTERFLY(1, 2, 4, EnumCreatureType.AMBIENT),
    AMBIENT_DRAGONFLY(1, 1, 2, EnumCreatureType.AMBIENT),
    AMBIENT_SNAIL(1, 1, 1, EnumCreatureType.AMBIENT),
    CREATURE_DEER(8, 2, 4, EnumCreatureType.CREATURE),
    CREATURE_TURKEY(10, 3, 4, EnumCreatureType.CREATURE),
    CREATURE_PIXIE(1, 1, 1, EnumCreatureType.CREATURE),
    /** Hostile spawns — FF has no nether mobs; deer-tier weight, smaller groups. */
    MONSTER_DEER(8, 1, 2, EnumCreatureType.MONSTER),
    MONSTER_PIXIE(1, 1, 1, EnumCreatureType.MONSTER),
    /** Small / medium aquatic — FF snail or dragonfly tier, never deer/turkey density. */
    WATER_SNAIL(1, 1, 1, EnumCreatureType.WATER_CREATURE),
    WATER_DRAGONFLY(1, 1, 2, EnumCreatureType.WATER_CREATURE),
    WATER_PIXIE(1, 1, 1, EnumCreatureType.WATER_CREATURE),
    AMBIENT_JERBOA(8, 1, 2, EnumCreatureType.AMBIENT),
    AMBIENT_RAIN_FROG(8, 1, 2, EnumCreatureType.AMBIENT),
    /** Shallow pool invertebrates — same density tier as FF dragonfly, not deer-sized packs. */
    WATER_TRIOPS(1, 1, 2, EnumCreatureType.WATER_CREATURE);

    public final int weight;
    public final int minGroup;
    public final int maxGroup;
    public final EnumCreatureType creatureType;

    AMFaunaSpawnProfile(int weight, int minGroup, int maxGroup, EnumCreatureType creatureType) {
        this.weight = weight;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
        this.creatureType = creatureType;
    }
}
