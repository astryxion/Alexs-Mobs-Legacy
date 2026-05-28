package com.github.alexthe666.alexsmobs.world.spawn;

import net.minecraft.entity.EnumCreatureType;

import java.util.HashMap;
import java.util.Map;

/**
 * 1.16 / 1.20 spawn weight and group sizes for natural spawns.
 * 1.12 has no {@code WATER_AMBIENT}; small fish use {@link EnumCreatureType#WATER_CREATURE} with tighter caps.
 */
public final class AMSpawnReference {

    public static final class Spec {
        public final int minGroup;
        public final int maxGroup;
        public final EnumCreatureType creatureType;

        Spec(int minGroup, int maxGroup, EnumCreatureType creatureType) {
            this.minGroup = minGroup;
            this.maxGroup = maxGroup;
            this.creatureType = creatureType;
        }
    }

    private static final Map<String, Spec> BY_CONFIG_NAME = new HashMap<>();

    static {
        water("orca", 3, 4);
        water("hammerhead_shark", 2, 3);
        water("lobster", 2, 3);
        water("blobfish", 1, 2);
        water("mantis_shrimp", 1, 3);
        water("stradpole", 1, 1);
        water("cachalot_whale", 1, 2);
        water("frilled_shark", 1, 1);
        water("mimic_octopus", 1, 2);
        water("giant_squid", 1, 2);
        water("triops", 1, 3);
        water("flying_fish", 2, 3);
        water("comb_jelly", 1, 2);
        water("devils_hole_pupfish", 2, 4);
        water("catfish", 1, 2);
        water("terrapin", 1, 2);
    }

    private AMSpawnReference() {
    }

    private static void water(String configName, int min, int max) {
        BY_CONFIG_NAME.put(configName, new Spec(min, max, EnumCreatureType.WATER_CREATURE));
    }

    public static Spec forMob(String configName, AMFaunaSpawnProfile fallback) {
        Spec spec = BY_CONFIG_NAME.get(configName);
        if (spec != null) {
            return spec;
        }
        return new Spec(fallback.minGroup, fallback.maxGroup, fallback.creatureType);
    }

    public static int cappedMinGroup(Spec spec) {
        return Math.max(1, Math.min(spec.minGroup, cappedMaxGroup(spec)));
    }

    public static int cappedMaxGroup(Spec spec) {
        int max = spec.maxGroup;
        if (spec.creatureType == EnumCreatureType.AMBIENT) {
            return Math.min(max, 4);
        }
        if (spec.creatureType == EnumCreatureType.WATER_CREATURE) {
            return Math.min(max, 3);
        }
        if (spec.creatureType == EnumCreatureType.MONSTER) {
            return Math.min(max, 2);
        }
        return Math.min(max, 4);
    }
}
