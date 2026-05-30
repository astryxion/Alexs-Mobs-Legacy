package com.github.alexthe666.alexsmobs.client;

import com.github.alexthe666.alexsmobs.client.render.layer.LayerRainbow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = com.github.alexthe666.alexsmobs.AlexsMobs.MODID, value = Side.CLIENT)
public final class ClientLayerRegistry {

    private static final Set<RenderLivingBase<?>> REGISTERED = Collections.newSetFromMap(new IdentityHashMap<>());

    private ClientLayerRegistry() {
    }

    public static void registerRainbowLayers() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.getRenderManager() == null) {
            return;
        }
        Map<String, RenderPlayer> skinMap = mc.getRenderManager().getSkinMap();
        if (skinMap != null) {
            for (RenderPlayer renderPlayer : skinMap.values()) {
                addRainbowLayer(renderPlayer);
            }
        }
        for (Render<?> renderer : mc.getRenderManager().entityRenderMap.values()) {
            if (renderer instanceof RenderLivingBase && !(renderer instanceof RenderPlayer)) {
                addRainbowLayer((RenderLivingBase<?>) renderer);
            }
        }
    }

    private static void addRainbowLayer(RenderLivingBase<?> renderer) {
        if (REGISTERED.add(renderer)) {
            renderer.addLayer(new LayerRainbow(renderer));
        }
    }

    /** Mod entity renderers are created lazily; attach rainbow layers as they appear. */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            registerRainbowLayers();
        }
    }
}
