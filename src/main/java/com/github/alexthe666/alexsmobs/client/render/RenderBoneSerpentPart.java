package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBoneSerpentBody;
import com.github.alexthe666.alexsmobs.client.model.ModelBoneSerpentTail;
import com.github.alexthe666.alexsmobs.entity.EntityBoneSerpentPart;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBoneSerpentPart extends RenderLivingBase<EntityBoneSerpentPart> {

    private static final ResourceLocation TEXTURE_BODY = new ResourceLocation("alexsmobs:textures/entity/bone_serpent_mid.png");
    private static final ResourceLocation TEXTURE_TAIL = new ResourceLocation("alexsmobs:textures/entity/bone_serpent_tail.png");
    private final ModelBoneSerpentBody bodyModel = new ModelBoneSerpentBody();
    private final ModelBoneSerpentTail tailModel = new ModelBoneSerpentTail();

    public RenderBoneSerpentPart(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBoneSerpentBody(), 0.3F);
    }

    @Override
    protected boolean canRenderName(EntityBoneSerpentPart entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    @Override
    protected void preRenderCallback(EntityBoneSerpentPart entitylivingbaseIn, float partialTickTime) {
        this.mainModel = entitylivingbaseIn.isTail() ? this.tailModel : this.bodyModel;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoneSerpentPart entity) {
        return entity.isTail() ? TEXTURE_TAIL : TEXTURE_BODY;
    }
}
