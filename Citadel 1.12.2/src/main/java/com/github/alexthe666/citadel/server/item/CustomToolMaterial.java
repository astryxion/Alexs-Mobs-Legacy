package com.github.alexthe666.citadel.server.item;

import net.minecraft.item.crafting.Ingredient;

public class CustomToolMaterial {
   private String name;
   private int harvestLevel;
   private int durability;
   private float damage;
   private float speed;
   private int enchantability;
   private Ingredient ingredient = null;

   public CustomToolMaterial(String name, int harvestLevel, int durability, float damage, float speed, int enchantability) {
      this.name = name;
      this.harvestLevel = harvestLevel;
      this.durability = durability;
      this.damage = damage;
      this.speed = speed;
      this.enchantability = enchantability;
   }

   public String getName() {
      return this.name;
   }

   public int getMaxUses() {
      return this.durability;
   }

   public float getEfficiency() {
      return this.speed;
   }

   public float getAttackDamage() {
      return this.damage;
   }

   public int getHarvestLevel() {
      return this.harvestLevel;
   }

   public int getEnchantability() {
      return this.enchantability;
   }

   public Ingredient getRepairMaterial() {
      return this.ingredient == null ? Ingredient.EMPTY : this.ingredient;
   }

   public void setRepairMaterial(Ingredient ingredient) {
      this.ingredient = ingredient;
   }
}
