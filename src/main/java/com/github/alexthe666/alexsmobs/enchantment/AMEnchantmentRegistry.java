package com.github.alexthe666.alexsmobs.enchantment;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegistryEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class AMEnchantmentRegistry {

    public static Enchantment STRADDLE_JUMP = new StraddleJumpEnchantment(Enchantment.Rarity.COMMON).setRegistryName("alexsmobs:straddle_jump");
    public static Enchantment STRADDLE_LAVAWAX = new StraddleEnchantment(Enchantment.Rarity.UNCOMMON).setRegistryName("alexsmobs:lavawax");
    public static Enchantment STRADDLE_SERPENTFRIEND = new StraddleEnchantment(Enchantment.Rarity.RARE).setRegistryName("alexsmobs:serpentfriend");
    public static Enchantment STRADDLE_BOARDRETURN = new StraddleEnchantment(Enchantment.Rarity.UNCOMMON).setRegistryName("alexsmobs:board_return");

    @SubscribeEvent
    public static void registerEnchantments(final RegistryEvent.Register<Enchantment> event) {
        if (AMConfig.straddleboardEnchants) {
            try {
                for (Field f : AMEnchantmentRegistry.class.getDeclaredFields()) {
                    Object obj = f.get(null);
                    if (obj instanceof Enchantment) {
                        Enchantment enchantment = (Enchantment) obj;
                        if (enchantment.getRegistryName() != null) {
                            enchantment.setName(enchantment.getRegistryName().getResourcePath());
                        }
                        event.getRegistry().register(enchantment);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
