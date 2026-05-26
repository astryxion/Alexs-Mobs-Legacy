package com.github.alexthe666.alexsmobs.entity.ai;

import net.minecraft.entity.ai.EntityAIBase;

/**
 * 1.16 {@code net.minecraft.entity.ai.goal.JumpGoal} equivalent for dolphin-style breach / jump attacks.
 */
public abstract class JumpGoal extends EntityAIBase {

    public JumpGoal() {
        this.setMutexBits(1);
    }

    /**
     * Mirrors Yarn {@code isPreemptible}: when false, this goal is not interruptible by other goals.
     */
    public boolean isPreemptible() {
        return true;
    }

    @Override
    public boolean isInterruptible() {
        return this.isPreemptible();
    }
}
