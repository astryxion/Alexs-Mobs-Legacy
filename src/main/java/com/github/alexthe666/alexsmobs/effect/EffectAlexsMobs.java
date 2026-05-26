package com.github.alexthe666.alexsmobs.effect;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * 1.12 custom {@link Potion} icons: draw sprites from {@code textures/mob_effect/<registry_path>.png}.
 */
public class EffectAlexsMobs extends Potion {

    private final ResourceLocation iconTexture;

    public EffectAlexsMobs(boolean isBadEffect, int liquidColor, String registryPath) {
        super(isBadEffect, liquidColor);
        this.setRegistryName(AlexsMobs.MODID, registryPath);
        this.iconTexture = new ResourceLocation(AlexsMobs.MODID, "textures/mob_effect/" + registryPath + ".png");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasStatusIcon() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) {
        drawEffectIcon(this.iconTexture, x + 6, y + 7, z, 1.0F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) {
        drawEffectIcon(this.iconTexture, x + 3, y + 3, z, alpha);
    }

    @SideOnly(Side.CLIENT)
    public static void drawEffectIcon(ResourceLocation iconTexture, int x, int y, float z, float alpha) {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            mc.getResourceManager().getResource(iconTexture);
        } catch (IOException e) {
            return;
        }
        mc.getTextureManager().bindTexture(iconTexture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        int width = 18;
        int height = 18;
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        buf.pos(x, y + height, z).tex(0.0D, 1.0D).endVertex();
        buf.pos(x + width, y + height, z).tex(1.0D, 1.0D).endVertex();
        buf.pos(x + width, y, z).tex(1.0D, 0.0D).endVertex();
        buf.pos(x, y, z).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
