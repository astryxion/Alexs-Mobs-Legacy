package com.github.alexthe666.alexsmobs.tileentity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Forge 1.12.2 tile registration (no {@code TileEntityType}); call {@link #register()} from {@link AlexsMobs} preInit.
 */
public class AMTileEntityRegistry {

    private AMTileEntityRegistry() {
    }

    public static void register() {
        GameRegistry.registerTileEntity(TileEntityLeafcutterAnthill.class, new ResourceLocation(AlexsMobs.MODID, "leafcutter_anthill"));
        GameRegistry.registerTileEntity(TileEntityCapsid.class, new ResourceLocation(AlexsMobs.MODID, "capsid"));
        GameRegistry.registerTileEntity(TileEntityVoidWormBeak.class, new ResourceLocation(AlexsMobs.MODID, "void_worm_beak"));
        GameRegistry.registerTileEntity(TileEntitySkunkSpray.class, new ResourceLocation(AlexsMobs.MODID, "skunk_spray"));
    }
}
