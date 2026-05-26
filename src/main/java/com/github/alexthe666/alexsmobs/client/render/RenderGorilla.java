package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelGorilla;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerGorillaItem;
import com.github.alexthe666.alexsmobs.entity.EntityGorilla;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderGorilla extends RenderLiving<EntityGorilla> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/gorilla.png");
    private static final ResourceLocation TEXTURE_SILVERBACK = new ResourceLocation("alexsmobs:textures/entity/gorilla_silverback.png");
    private static final ResourceLocation TEXTURE_DK = new ResourceLocation("alexsmobs:textures/entity/gorilla_dk.png");
    private static final ResourceLocation TEXTURE_FUNKY = new ResourceLocation("alexsmobs:textures/entity/gorilla_funky.png");

    public RenderGorilla(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelGorilla(), 0.7F);
        this.addLayer(new LayerGorillaItem(this));
    }

    @Override
    protected void preRenderCallback(EntityGorilla entitylivingbaseIn, float partialTickTime) {
        float s = entitylivingbaseIn.getGorillaScale();
        GlStateManager.scale(s, s, s);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityGorilla entity) {
        if (entity.isFunkyKong()) {
            return TEXTURE_FUNKY;
        }
        if (entity.isDonkeyKong()) {
            return TEXTURE_DK;
        }
        if (entity.isSilverback()) {
            return TEXTURE_SILVERBACK;
        }
        return TEXTURE;
    }
}
