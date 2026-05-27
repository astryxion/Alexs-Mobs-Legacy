package com.github.alexthe666.alexsmobs.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Applies HEI/JEI {@code jei_at.cfg} access wideners at runtime for ForgeGradle 3 dev.
 */
public class JeiAccessTransformer implements IClassTransformer {

    private static final Map<String, Target> TARGETS = new HashMap<>();

    static {
        TARGETS.put("net.minecraft.client.renderer.texture.TextureMap", new Target(
                set("field_94252_e", "mapUploadedSprites",
                        "field_94258_i", "listAnimatedSprites",
                        "field_94249_f", "missingImage",
                        "field_94254_c", "basePath",
                        "field_110574_e", "mapRegisteredSprites"),
                set("func_110569_e", "initMissingImage")
        ));
        TARGETS.put("net.minecraft.client.renderer.RenderItem", new Target(
                Collections.<String>emptySet(),
                set("func_191965_a", "renderModel", "func_191961_a")
        ));
        TARGETS.put("net.minecraft.client.gui.recipebook.GuiRecipeBook", new Target(
                set("field_191904_o", "width",
                        "field_191905_p", "height",
                        "field_191903_n", "xOffset",
                        "field_193018_j", "recipeTabs"),
                Collections.<String>emptySet()
        ));
        TARGETS.put("net.minecraft.item.crafting.Ingredient", new Target(
                set("field_193371_b", "matchingStacks"),
                Collections.<String>emptySet()
        ));
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        Target target = TARGETS.get(transformedName);
        if (target == null) {
            return basicClass;
        }
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        boolean changed = false;

        for (FieldNode field : classNode.fields) {
            if (target.fields.contains(field.name)) {
                int access = field.access;
                if ((access & Opcodes.ACC_PUBLIC) == 0) {
                    field.access = (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
                    changed = true;
                }
            }
        }

        for (MethodNode method : classNode.methods) {
            if (target.methods.contains(method.name)) {
                int access = method.access;
                if ((access & Opcodes.ACC_PUBLIC) == 0) {
                    method.access = (access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED)) | Opcodes.ACC_PUBLIC;
                    changed = true;
                }
            }
        }

        if (!changed) {
            return basicClass;
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static Set<String> set(String... names) {
        return new HashSet<>(Arrays.asList(names));
    }

    private static final class Target {
        final Set<String> fields;
        final Set<String> methods;

        Target(Set<String> fields, Set<String> methods) {
            this.fields = fields;
            this.methods = methods;
        }
    }
}
