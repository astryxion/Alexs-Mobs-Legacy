package com.github.alexthe668.alexsmobs.item;

import com.github.alexthe668.citadel.server.item.CustomArmorMaterial;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.SoundEvent;

public class AMArmorMaterial extends CustomArmorMaterial {

    protected static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private int maxDamageFactor;

    public AMArmorMaterial(String name, int durability, int[] damageReduction, int encantability, SoundEvent sound, float toughness) {
        super(name, durability, damageReduction, encantability, sound, toughness, 0);
        this.maxDamageFactor = durability;
    }

    public AMArmorMaterial(String name, int durability, int[] damageReduction, int encantability, SoundEvent sound, float toughness, float knockbackResist) {
        super(name, durability, damageReduction, encantability, sound, toughness, knockbackResist);
        this.maxDamageFactor = durability;
    }

    public int getDurability(EntityEquipmentSlot slotIn) {
        return MAX_DAMAGE_ARRAY[typeOrdinal(slotIn)] * this.maxDamageFactor;
    }

    /**
     * 1.20 indexes this array by helmet, chest, legs, boots.
     * 1.12 {@code getIndex()} is boots, legs, chest, helmet.
     */
    static int typeOrdinal(EntityEquipmentSlot slotIn) {
        switch (slotIn) {
            case HEAD:
                return 0;
            case CHEST:
                return 1;
            case LEGS:
                return 2;
            case FEET:
                return 3;
            default:
                return 0;
        }
    }
}
