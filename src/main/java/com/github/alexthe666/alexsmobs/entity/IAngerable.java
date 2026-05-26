package com.github.alexthe666.alexsmobs.entity;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * 1.16 {@code IAngerable} surface, for Forge 1.12.2 anger mechanics shared by bees/bears/tigers/ants.
 */
public interface IAngerable {

    int getAngerTime();

    void setAngerTime(int time);

    @Nullable
    UUID getAngerTarget();

    void setAngerTarget(@Nullable UUID uuid);

    default boolean isAngry() {
        return getAngerTime() > 0;
    }

    /**
     * Mirrors 1.16 {@code IAngerable#resetTargets} when local players/revenge targets are cleared.
     */
    void resetTargets();

    /**
     * 1.16 {@code IAngerable#func_230258_H__}: set a random anger duration from the mob's configured range.
     */
    void func_230258_H__();

    /**
     * Used by {@link com.github.alexthe666.alexsmobs.entity.ai.ResetAngerGoal}: vanilla checks
     * {@code world.getGameTime() - lastPlayerAttackTick <= 100L}.
     */
    boolean wasHurtByPlayerRecently();
}
