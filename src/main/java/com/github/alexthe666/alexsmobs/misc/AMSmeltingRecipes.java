package com.github.alexthe666.alexsmobs.misc;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads {@code minecraft:smelting} recipe JSON from {@code assets/alexsmobs/recipes/} (1.16 {@code data/} path).
 */
public final class AMSmeltingRecipes {

    private AMSmeltingRecipes() {
    }

    public static void register() {
        ModContainer mod = Loader.instance().getIndexedModList().get(AlexsMobs.MODID);
        if (mod == null) {
            return;
        }
        String base = "assets/" + AlexsMobs.MODID + "/recipes";
        if (mod.getSource().isFile()) {
            loadFromJar(mod.getSource(), base);
        } else if (mod.getSource().isDirectory()) {
            loadFromDirectory(mod.getSource().toPath().resolve(base.replace('/', java.io.File.separatorChar)));
        }
    }

    private static void loadFromDirectory(Path recipesDir) {
        if (!Files.isDirectory(recipesDir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(recipesDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .filter(p -> !p.getFileName().toString().startsWith("_"))
                    .forEach(AMSmeltingRecipes::loadSmeltingFile);
        } catch (IOException e) {
            AlexsMobs.LOGGER.error("Failed to walk smelting recipe directory", e);
        }
    }

    private static void loadFromJar(java.io.File jarFile, String basePath) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName().replace('\\', '/');
                if (!name.startsWith(basePath + "/") || !name.endsWith(".json") || name.contains("/_")) {
                    continue;
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    parseSmeltingJson(in);
                } catch (Exception e) {
                    AlexsMobs.LOGGER.error("Failed to load smelting recipe {}", name, e);
                }
            }
        } catch (IOException e) {
            AlexsMobs.LOGGER.error("Failed to read smelting recipes from jar", e);
        }
    }

    private static void loadSmeltingFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            parseSmeltingJson(reader);
        } catch (Exception e) {
            AlexsMobs.LOGGER.error("Failed to load smelting recipe {}", path, e);
        }
    }

    private static void parseSmeltingJson(InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            parseSmeltingJson(reader);
        }
    }

    private static void parseSmeltingJson(BufferedReader reader) {
        JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
        if (!"minecraft:smelting".equals(JsonUtils.getString(json, "type", ""))) {
            return;
        }
        ItemStack input = AMTagIngredientFactory.parseItemIngredient(json.get("ingredient"));
        ItemStack output = AMTagIngredientFactory.parseItemStackResult(json.get("result"));
        float xp = JsonUtils.getFloat(json, "experience", 0.15F);
        FurnaceRecipes.instance().addSmeltingRecipe(input, output, xp);
    }
}
