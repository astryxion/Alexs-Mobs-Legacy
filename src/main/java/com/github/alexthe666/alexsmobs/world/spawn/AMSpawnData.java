package com.github.alexthe666.alexsmobs.world.spawn;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * Per-mob natural spawn specification (Mo Creatures {@code MoCEntityData} analogue).
 * Weights and group sizes follow 1.16/1.20 {@code AMWorldRegistry}; biomes use {@link BiomeFilter}.
 */
public final class AMSpawnData {

    public final Class<? extends EntityLiving> entityClass;
    public final BiomeFilter biomeFilter;
    public final EnumCreatureType creatureType;
    public final IntSupplier weight;
    public final int minGroup;
    public final int maxGroup;
    public final int[] dimensions;
    private final BooleanSupplier registerCondition;

    public AMSpawnData(Class<? extends EntityLiving> entityClass, BiomeFilter biomeFilter,
                       EnumCreatureType creatureType, IntSupplier weight, int minGroup, int maxGroup,
                       int[] dimensions, BooleanSupplier registerCondition) {
        this.entityClass = entityClass;
        this.biomeFilter = biomeFilter;
        this.creatureType = creatureType;
        this.weight = weight;
        this.minGroup = minGroup;
        this.maxGroup = maxGroup;
        this.dimensions = dimensions;
        this.registerCondition = registerCondition;
    }

    public AMSpawnData(Class<? extends EntityLiving> entityClass, BiomeFilter biomeFilter,
                       EnumCreatureType creatureType, IntSupplier weight, int minGroup, int maxGroup,
                       int[] dimensions) {
        this(entityClass, biomeFilter, creatureType, weight, minGroup, maxGroup, dimensions, () -> true);
    }

    public boolean shouldRegister() {
        return weight.getAsInt() > 0 && registerCondition.getAsBoolean();
    }

    @FunctionalInterface
    public interface BiomeFilter {
        boolean test(Biome biome);
    }
}
