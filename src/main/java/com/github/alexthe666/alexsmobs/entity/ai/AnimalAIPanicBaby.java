package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.passive.EntityAnimal;

/**
 * Forge 1.12.2 port of 1.16 {@code AnimalAIPanicBaby}.
 */
public class AnimalAIPanicBaby extends EntityAIPanic {

    private final EntityAnimal animal;

    public AnimalAIPanicBaby(EntityAnimal creatureIn, double speed) {
        super(creatureIn, speed);
        this.animal = creatureIn;
    }

    @Override
    public boolean shouldExecute() {
        return animal.isChild() && super.shouldExecute();
    }
}
