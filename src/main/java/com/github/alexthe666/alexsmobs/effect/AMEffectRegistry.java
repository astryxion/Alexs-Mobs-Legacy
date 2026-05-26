package com.github.alexthe666.alexsmobs.effect;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMEffectRegistry {
    public static final Potion KNOCKBACK_RESISTANCE = new EffectKnockbackResistance();
    public static final Potion LAVA_VISION = new EffectLavaVision();
    public static final Potion SUNBIRD_BLESSING = new EffectSunbird(false);
    public static final Potion SUNBIRD_CURSE = new EffectSunbird(true);
    public static final Potion POISON_RESISTANCE = new EffectPoisonResistance();
    public static final Potion OILED = new EffectOiled();
    public static final Potion ORCAS_MIGHT = new EffectOrcaMight();
    public static final Potion BUG_PHEROMONES = new EffectBugPheromones();
    public static final Potion SOULSTEAL = new EffectSoulsteal();
    public static final Potion CLINGING = new EffectClinging();
    public static final Potion ENDER_FLU = new EffectEnderFlu();
    public static final Potion FEAR = new EffectFear();
    public static final Potion TIGERS_BLESSING = new EffectTigersBlessing();
    public static final Potion DEBILITATING_STING = new EffectDebilitatingSting();
    public static final Potion FLEET_FOOTED = new EffectFleetFooted();
    public static final Potion EXSANGUINATION = new EffectExsanguination();
    public static final PotionType KNOCKBACK_RESISTANCE_POTION = new PotionType(new PotionEffect(KNOCKBACK_RESISTANCE, 3600)).setRegistryName(AlexsMobs.MODID, "knockback_resistance");
    public static final PotionType LONG_KNOCKBACK_RESISTANCE_POTION = new PotionType(new PotionEffect(KNOCKBACK_RESISTANCE, 9600)).setRegistryName(AlexsMobs.MODID, "long_knockback_resistance");
    public static final PotionType STRONG_KNOCKBACK_RESISTANCE_POTION = new PotionType(new PotionEffect(KNOCKBACK_RESISTANCE, 1800, 1)).setRegistryName(AlexsMobs.MODID, "strong_knockback_resistance");
    public static final PotionType LAVA_VISION_POTION = new PotionType(new PotionEffect(LAVA_VISION, 3600)).setRegistryName(AlexsMobs.MODID, "lava_vision");
    public static final PotionType LONG_LAVA_VISION_POTION = new PotionType(new PotionEffect(LAVA_VISION, 9600)).setRegistryName(AlexsMobs.MODID, "long_lava_vision");
    public static final PotionType SPEED_III_POTION = new PotionType(new PotionEffect(MobEffects.SPEED, 2200, 2)).setRegistryName(AlexsMobs.MODID, "speed_iii");
    public static final PotionType POISON_RESISTANCE_POTION = new PotionType(new PotionEffect(POISON_RESISTANCE, 3600)).setRegistryName(AlexsMobs.MODID, "poison_resistance");
    public static final PotionType LONG_POISON_RESISTANCE_POTION = new PotionType(new PotionEffect(POISON_RESISTANCE, 9600)).setRegistryName(AlexsMobs.MODID, "long_poison_resistance");
    public static final PotionType BUG_PHEROMONES_POTION = new PotionType(new PotionEffect(BUG_PHEROMONES, 3600)).setRegistryName(AlexsMobs.MODID, "bug_pheromones");
    public static final PotionType LONG_BUG_PHEROMONES_POTION = new PotionType(new PotionEffect(BUG_PHEROMONES, 9600)).setRegistryName(AlexsMobs.MODID, "long_bug_pheromones");
    public static final PotionType SOULSTEAL_POTION = new PotionType(new PotionEffect(SOULSTEAL, 3600)).setRegistryName(AlexsMobs.MODID, "soulsteal");
    public static final PotionType LONG_SOULSTEAL_POTION = new PotionType(new PotionEffect(SOULSTEAL, 9600)).setRegistryName(AlexsMobs.MODID, "long_soulsteal");
    public static final PotionType STRONG_SOULSTEAL_POTION = new PotionType(new PotionEffect(SOULSTEAL, 1800, 1)).setRegistryName(AlexsMobs.MODID, "strong_soulsteal");
    public static final PotionType CLINGING_POTION = new PotionType(new PotionEffect(CLINGING, 3600)).setRegistryName(AlexsMobs.MODID, "clinging");
    public static final PotionType LONG_CLINGING_POTION = new PotionType(new PotionEffect(CLINGING, 9600)).setRegistryName(AlexsMobs.MODID, "long_clinging");

    @SubscribeEvent
    public static void registerPotionEffects(RegistryEvent.Register<Potion> event) {
        try {
            for (Field f : AMEffectRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Potion && obj.getClass().getPackage().equals(AMEffectRegistry.class.getPackage())) {
                    event.getRegistry().register((Potion) obj);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void registerPotionTypes(RegistryEvent.Register<PotionType> event) {
        try {
            for (Field f : AMEffectRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof PotionType) {
                    event.getRegistry().register((PotionType) obj);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        AMEffectRegistry.onInitItems();
    }

    public static ItemStack createPotion(PotionType potionType) {
        return PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), potionType);
    }

    public static void onInitItems() {
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.STRENGTH)), Ingredient.fromItem(AMItemRegistry.BEAR_FUR), createPotion(KNOCKBACK_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(KNOCKBACK_RESISTANCE_POTION)), Ingredient.fromItem(Items.REDSTONE), createPotion(LONG_KNOCKBACK_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(KNOCKBACK_RESISTANCE_POTION)), Ingredient.fromItem(Items.GLOWSTONE_DUST), createPotion(STRONG_KNOCKBACK_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromItem(AMItemRegistry.LAVA_BOTTLE), Ingredient.fromItem(AMItemRegistry.BONE_SERPENT_TOOTH), createPotion(LAVA_VISION_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(LAVA_VISION_POTION)), Ingredient.fromItem(Items.REDSTONE), createPotion(LONG_LAVA_VISION_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.POISON)), Ingredient.fromItem(AMItemRegistry.RATTLESNAKE_RATTLE), new ItemStack(AMItemRegistry.POISON_BOTTLE)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromItem(AMItemRegistry.POISON_BOTTLE), Ingredient.fromItem(AMItemRegistry.CENTIPEDE_LEG), createPotion(POISON_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromItem(AMItemRegistry.KOMODO_SPIT_BOTTLE), Ingredient.fromItem(AMItemRegistry.CENTIPEDE_LEG), createPotion(POISON_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(POISON_RESISTANCE_POTION)), Ingredient.fromItem(AMItemRegistry.KOMODO_SPIT), createPotion(LONG_POISON_RESISTANCE_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.STRONG_SWIFTNESS)), Ingredient.fromItem(AMItemRegistry.GAZELLE_HORN), createPotion(SPEED_III_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.AWKWARD)), Ingredient.fromItem(AMItemRegistry.COCKROACH_WING), createPotion(BUG_PHEROMONES_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(BUG_PHEROMONES_POTION)), Ingredient.fromItem(Items.REDSTONE), createPotion(LONG_BUG_PHEROMONES_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.AWKWARD)), Ingredient.fromItem(AMItemRegistry.SOUL_HEART), createPotion(SOULSTEAL_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(SOULSTEAL_POTION)), Ingredient.fromItem(Items.REDSTONE), createPotion(LONG_SOULSTEAL_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(SOULSTEAL_POTION)), Ingredient.fromItem(Items.GLOWSTONE_DUST), createPotion(STRONG_SOULSTEAL_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(PotionTypes.AWKWARD)), Ingredient.fromItem(AMItemRegistry.DROPBEAR_CLAW), createPotion(CLINGING_POTION)));
        BrewingRecipeRegistry.addRecipe(new ProperBrewingRecipe(Ingredient.fromStacks(createPotion(CLINGING_POTION)), Ingredient.fromItem(Items.REDSTONE), createPotion(LONG_CLINGING_POTION)));
    }
}
