package com.github.alexthe666.alexsmobs.compat.jei;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemAMInternal;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;

/**
 * Hides internal / broken items from JEI.
 */
@JEIPlugin
public class AMJEIPlugin implements IModPlugin {

    private static final String CITADEL_MODID = "citadel";

    @Override
    public void register(mezz.jei.api.IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        IIngredientRegistry ingredients = registry.getIngredientRegistry();

        blacklist.addIngredientToBlacklist(new ItemStack(AMItemRegistry.TAB_ICON));

        for (Item item : ForgeRegistries.ITEMS.getValuesCollection()) {
            ResourceLocation name = item.getRegistryName();
            if (name == null) {
                continue;
            }
            String domain = name.getResourceDomain();
            if (AlexsMobs.MODID.equals(domain)) {
                if (item instanceof ItemAMInternal) {
                    blacklist.addIngredientToBlacklist(new ItemStack(item));
                }
            }
        }

        Collection<ItemStack> allItems = ingredients.getAllIngredients(VanillaTypes.ITEM);
        for (ItemStack stack : allItems) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            Item item = stack.getItem();
            ResourceLocation name = item.getRegistryName();
            if (name != null && CITADEL_MODID.equals(name.getResourceDomain())) {
                blacklist.addIngredientToBlacklist(stack.copy());
            }
        }
    }
}
