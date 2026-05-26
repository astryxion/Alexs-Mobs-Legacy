package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBison;
import com.github.alexthe666.alexsmobs.client.model.ModelBisonBaby;
import com.github.alexthe666.alexsmobs.client.render.layer.LayerBisonSnow;
import com.github.alexthe666.alexsmobs.entity.EntityBison;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBison extends RenderLiving<EntityBison> {
    private static final ResourceLocation TEXTURE_BABY = new ResourceLocation("alexsmobs:textures/entity/bison_baby.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/bison.png");
    private static final ResourceLocation TEXTURE_SHEARED = new ResourceLocation("alexsmobs:textures/entity/bison_sheared.png");
    private static final ModelBison MODEL_BISON = new ModelBison();
    private static final ModelBisonBaby MODEL_BABY = new ModelBisonBaby();

    public RenderBison(RenderManager renderManagerIn) {
        super(renderManagerIn, MODEL_BISON, 0.8F);
        this.addLayer(new LayerBisonSnow(this));
    }

    @Override
    protected void preRenderCallback(EntityBison entitylivingbaseIn, float partialTickTime) {
        if (entitylivingbaseIn.isChild()) {
            this.mainModel = MODEL_BABY;
        } else {
            this.mainModel = MODEL_BISON;
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBison entity) {
        if (entity.isChild()) {
            return TEXTURE_BABY;
        }
        return entity.isSheared() ? TEXTURE_SHEARED : TEXTURE;
    }
}
