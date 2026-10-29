package com.github.alexthe668.alexsmobs.effect;

/**
 * Makes crimson mosquitoes avoid the affected mob. The stew applies this for 24000 ticks.
 */
public class EffectMosquitoRepellent extends EffectAlexsMobs {

    public EffectMosquitoRepellent() {
        super(false, 0XCC7E70, "mosquito_repellent");
    }

    @Override
    public String getName() {
        return "alexsmobs.potion.mosquito_repellent";
    }
}
