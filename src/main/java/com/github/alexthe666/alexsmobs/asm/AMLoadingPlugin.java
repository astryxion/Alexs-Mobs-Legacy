package com.github.alexthe666.alexsmobs.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Ensures {@link net.minecraft.client.renderer.texture.TextureMap#initMissingImage()} is accessible to JEI in
 * ForgeGradle 3 dev runs, where dependency access transformers are not always applied at runtime.
 */
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("AlexsMobsJeiCompat")
@IFMLLoadingPlugin.SortingIndex(1000)
public class AMLoadingPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{JeiAccessTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
