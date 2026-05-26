package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelAMElytra;
import com.github.alexthe666.alexsmobs.item.ItemTarantulaHawkElytra;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Mirrors vanilla {@link net.minecraft.client.renderer.entity.layers.LayerElytra} for {@link ItemTarantulaHawkElytra}.
 */
@SideOnly(Side.CLIENT)
public class LayerTarantulaHawkElytra implements LayerRenderer<EntityLivingBase> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/armor/tarantula_hawk_elytra.png");
    private final ModelAMElytra model = new ModelAMElytra();

    @Override
    public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack chest = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ItemTarantulaHawkElytra)) {
            return;
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        if (entity.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }
        this.model.withAnimations(entity);
        this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
