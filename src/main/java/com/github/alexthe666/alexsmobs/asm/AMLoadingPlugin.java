package com.github.alexthe666.alexsmobs.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

/**
 * Applies HEI/JEI access wideners at runtime in ForgeGradle 3 dev, where dependency ATs are not
 * always applied to the deobfuscated game jar before HEI loads {@code TextureMap#initMissingImage}.
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
