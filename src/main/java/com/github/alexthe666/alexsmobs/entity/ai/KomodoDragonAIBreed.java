package com.github.alexthe666.alexsmobs.entity.ai;

import com.github.alexthe666.alexsmobs.entity.EntityKomodoDragon;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;

/**
 * Forge 1.12.2 port of 1.16 {@code KomodoDragonAIBreed}: vanilla mate + parthenogenic reproduction when in love alone.
 */
public class KomodoDragonAIBreed extends AnimalAIMate {

    private final EntityKomodoDragon komodo;
    private boolean withPartner;
    private int selfBreedTime;

    public KomodoDragonAIBreed(EntityKomodoDragon komodo, double speed) {
        super(komodo, speed);
        this.komodo = komodo;
    }

    @Override
    public boolean shouldExecute() {
        withPartner = super.shouldExecute();
        return withPartner || komodo.isInLove();
    }

    @Override
    public boolean shouldContinueExecuting() {
        return withPartner ? super.shouldContinueExecuting() : selfBreedTime < 60 && komodo.isInLove();
    }

    @Override
    public void resetTask() {
        super.resetTask();
        selfBreedTime = 0;
        withPartner = false;
    }

    @Override
    public void updateTask() {
        if (withPartner) {
            super.updateTask();
        } else {
            this.getAnimal().getNavigator().clearPath();
            ++this.selfBreedTime;
            if (this.selfBreedTime >= 60) {
                this.spawnParthenogenicBaby();
                this.selfBreedTime = 0;
                this.komodo.resetInLove();
            }
        }
    }

    @Override
    protected void spawnBaby() {
        for (int i = 0; i < 2 + this.getAnimal().getRNG().nextInt(2); i++) {
            EntityAgeable child = this.komodo.createChild(this.getAnimal());
            if (child != null) {
                child.setGrowingAge(-24000);
                child.setLocationAndAngles(
                        this.getAnimal().posX,
                        this.getAnimal().posY,
                        this.getAnimal().posZ,
                        0.0F,
                        0.0F);
                this.getAnimal().world.spawnEntity(child);
            }
        }
        this.komodo.slaughterCooldown = 200;
        this.getAnimal().setGrowingAge(6000);
        if (this.getTargetMate() != null) {
            this.getTargetMate().setGrowingAge(6000);
        }
        this.getAnimal().resetInLove();
        if (this.getTargetMate() != null) {
            this.getTargetMate().resetInLove();
        }
    }

    private void spawnParthenogenicBaby() {
        for (int i = 0; i < 2 + this.komodo.getRNG().nextInt(2); i++) {
            EntityAgeable child = this.komodo.createChild(this.komodo);
            if (child != null) {
                child.setGrowingAge(-24000);
                child.setLocationAndAngles(
                        this.komodo.posX,
                        this.komodo.posY,
                        this.komodo.posZ,
                        0.0F,
                        0.0F);
                this.komodo.world.spawnEntity(child);
            }
        }
        this.komodo.slaughterCooldown = 200;
    }
}
