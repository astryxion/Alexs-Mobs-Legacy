package com.github.alexthe666.alexsmobs.misc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.12.2 ingredient factory for 1.16-style {@code {"tag": "namespace:path"}} recipe entries.
 */
public class AMTagIngredientFactory implements IIngredientFactory {

    @Override
    public Ingredient parse(JsonContext context, JsonObject json) {
        if (!json.has("tag")) {
            throw new JsonSyntaxException("alexsmobs:tag ingredient requires a \"tag\" field");
        }
        String tag = JsonUtils.getString(json, "tag");
        List<ItemStack> stacks = resolveTagStacks(tag);
        if (stacks.isEmpty()) {
            throw new JsonSyntaxException("Tag \"" + tag + "\" resolved to no items on 1.12.2");
        }
        return Ingredient.fromStacks(stacks.toArray(new ItemStack[0]));
    }

    private static List<ItemStack> resolveTagStacks(String tag) {
        List<ItemStack> stacks = new ArrayList<>();
        if (tag.startsWith("forge:")) {
            String ore = forgeTagToOreDict(tag);
            if (ore != null) {
                for (ItemStack stack : OreDictionary.getOres(ore)) {
                    if (!stack.isEmpty()) {
                        stacks.add(stack);
                    }
                }
            }
            if (stacks.isEmpty()) {
                addForgeFallback(stacks, tag);
            }
            return stacks;
        }
        if (tag.startsWith("minecraft:")) {
            addMinecraftTagFallback(stacks, tag);
            return stacks;
        }
        if (tag.startsWith("alexsmobs:")) {
            ResourceLocation tagId = new ResourceLocation(tag);
            if (!AMTagRegistry.ITEM_TAG_SETS.containsKey(tagId)) {
                AMTagRegistry.loadItemTagSetFor(tagId);
            }
            for (Item item : AMTagRegistry.ITEM_TAG_SETS.getOrDefault(tagId, java.util.Collections.emptySet())) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }

    private static String forgeTagToOreDict(String tag) {
        switch (tag) {
            case "forge:ingots/iron": return "ingotIron";
            case "forge:ingots/gold": return "ingotGold";
            case "forge:ingots/netherite": return "ingotNetherite";
            case "forge:nuggets/iron": return "nuggetIron";
            case "forge:string": return "string";
            case "forge:rods/wooden": return "stickWood";
            case "forge:dyes/green": return "dyeGreen";
            default:
                if (tag.startsWith("forge:ingots/")) {
                    return "ingot" + capitalize(tag.substring("forge:ingots/".length()));
                }
                if (tag.startsWith("forge:nuggets/")) {
                    return "nugget" + capitalize(tag.substring("forge:nuggets/".length()));
                }
                if (tag.startsWith("forge:dyes/")) {
                    return "dye" + capitalize(tag.substring("forge:dyes/".length()));
                }
                return null;
        }
    }

    private static void addForgeFallback(List<ItemStack> stacks, String tag) {
        if ("forge:ingots/netherite".equals(tag)) {
            stacks.add(new ItemStack(Items.DIAMOND));
            stacks.add(new ItemStack(Blocks.DIAMOND_BLOCK));
            return;
        }
        if ("forge:rods/wooden".equals(tag)) {
            stacks.add(new ItemStack(Items.STICK));
        }
    }

    private static void addMinecraftTagFallback(List<ItemStack> stacks, String tag) {
        switch (tag) {
            case "minecraft:planks":
                stacks.add(new ItemStack(Blocks.PLANKS));
                break;
            case "minecraft:sand":
                stacks.add(new ItemStack(Blocks.SAND));
                stacks.add(new ItemStack(Blocks.SAND, 1, 1));
                break;
            default:
                break;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static ItemStack parseItemStackResult(JsonElement result) {
        if (result == null || result.isJsonNull()) {
            throw new JsonSyntaxException("Missing smelting result");
        }
        if (result.isJsonPrimitive()) {
            return stackFromItemName(result.getAsString(), 1);
        }
        return CraftingHelper.getItemStack(result.getAsJsonObject(), new JsonContext("minecraft"));
    }

    public static ItemStack parseItemIngredient(JsonElement ingredient) {
        if (ingredient == null || ingredient.isJsonNull()) {
            throw new JsonSyntaxException("Missing smelting ingredient");
        }
        if (ingredient.isJsonPrimitive()) {
            return stackFromItemName(ingredient.getAsString(), 1);
        }
        JsonObject obj = ingredient.getAsJsonObject();
        if (obj.has("tag")) {
            Ingredient ing = new AMTagIngredientFactory().parse(new JsonContext("alexsmobs"), obj);
            ItemStack[] stacks = ing.getMatchingStacks();
            if (stacks.length == 0) {
                throw new JsonSyntaxException("Tag ingredient resolved to nothing for smelting");
            }
            return stacks[0];
        }
        return CraftingHelper.getItemStackBasic(obj, new JsonContext("minecraft"));
    }

    private static ItemStack stackFromItemName(String itemName, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + itemName + "'");
        }
        return new ItemStack(item, count);
    }
}
