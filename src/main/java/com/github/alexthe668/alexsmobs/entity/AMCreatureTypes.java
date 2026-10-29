package com.github.alexthe668.alexsmobs.entity;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.util.EnumHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * 1.12 only has {@link EnumCreatureType#WATER_CREATURE} (cap 5). Modern Alex's Mobs puts small
 * fish on {@code WATER_AMBIENT} (cap 20) so lobsters and pupfish do not fill the whale cap.
 * The marker type is unused by vanilla mobs; Alex's Mobs entities opt in via {@code isCreatureType}.
 * <p>
 * {@code Biome.getSpawnableList} switches on the enum. That switch table is built the first time
 * it runs and is only as long as the enum was then. Adding this type afterwards makes ordinal 4
 * throw {@link ArrayIndexOutOfBoundsException}. {@link #prepareSpawnLists()} grows that table so
 * the new type falls through to Forge's extra-type list.
 */
public final class AMCreatureTypes {

    public static final EnumCreatureType WATER_AMBIENT = EnumHelper.addCreatureType(
            "AM_WATER_AMBIENT",
            Marker.class,
            20,
            Material.WATER,
            true,
            false);

    private static boolean prepared;

    private AMCreatureTypes() {
    }

    public static void prepareSpawnLists() {
        if (prepared) {
            return;
        }
        prepared = true;
        int needed = EnumCreatureType.values().length;
        widenSwitchMaps(Biome.class, needed);
        for (Class<?> inner : Biome.class.getDeclaredClasses()) {
            widenSwitchMaps(inner, needed);
        }
    }

    private static void widenSwitchMaps(Class<?> clazz, int needed) {
        Field[] fields;
        try {
            fields = clazz.getDeclaredFields();
        } catch (Throwable ignored) {
            return;
        }
        for (Field field : fields) {
            if (field.getType() != int[].class || !Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                int[] current = (int[]) field.get(null);
                if (current == null || current.length >= needed || !looksLikeEnumSwitch(current)) {
                    continue;
                }
                int[] grown = new int[needed];
                System.arraycopy(current, 0, grown, 0, current.length);
                field.set(null, grown);
                AlexsMobs.LOGGER.info("Widened biome creature-type switch in {} so small water mobs can spawn", clazz.getName());
            } catch (Throwable error) {
                AlexsMobs.LOGGER.error("Could not widen biome creature-type switch in {}", clazz.getName(), error);
            }
        }
    }

    private static boolean looksLikeEnumSwitch(int[] values) {
        for (int value : values) {
            if (value < 0 || value > 8) {
                return false;
            }
        }
        return true;
    }

    private interface Marker extends IAnimals {
    }
}
