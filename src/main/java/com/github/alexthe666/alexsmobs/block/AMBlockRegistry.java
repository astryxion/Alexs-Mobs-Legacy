package com.github.alexthe666.alexsmobs.block;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMBlockRegistry {

    public static final Block BANANA_PEEL = new BlockBananaPeel();
    public static final Block HUMMINGBIRD_FEEDER = new BlockHummingbirdFeeder();
    public static final Block GUSTMAKER = new BlockGustmaker();
    public static final Block LEAFCUTTER_ANTHILL = new BlockLeafcutterAnthill();
    public static final Block LEAFCUTTER_ANT_CHAMBER = new BlockLeafcutterAntChamber();
    public static final Block CAPSID = new BlockCapsid();
    public static final Block VOID_WORM_BEAK = new BlockVoidWormBeak();
    public static final Block BISON_CARPET = new BlockBisonCarpet();
    public static final Block BISON_FUR_BLOCK = new BlockBisonFurBlock();
    public static final Block SKUNK_SPRAY = new BlockSkunkSpray();
    public static final Block BANANA_SLUG_SLIME_BLOCK = new BlockBananaSlugSlime();
    public static final Block CRYSTALIZED_BANANA_SLUG_MUCUS = new BlockCrystalizedMucus();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        try {
            for (Field f : AMBlockRegistry.class.getDeclaredFields()) {
                Object obj = f.get(null);
                if (obj instanceof Block) {
                    Block block = (Block) obj;
                    AlexsMobs.applyUnlocalizedNameFromRegistry(block);
                    event.getRegistry().register(block);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
