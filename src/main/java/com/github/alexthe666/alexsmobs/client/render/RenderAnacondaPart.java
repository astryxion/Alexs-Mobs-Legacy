package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelAnaconda;
import com.github.alexthe666.alexsmobs.entity.EntityAnacondaPart;
import com.github.alexthe666.alexsmobs.entity.util.AnacondaPartIndex;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderAnacondaPart extends RenderLivingBase<EntityAnacondaPart> {
    private final ModelAnaconda neckModel = new ModelAnaconda(AnacondaPartIndex.NECK);
    private final ModelAnaconda bodyModel = new ModelAnaconda(AnacondaPartIndex.BODY);
    private final ModelAnaconda tailModel = new ModelAnaconda(AnacondaPartIndex.TAIL);

    public RenderAnacondaPart(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelAnaconda(AnacondaPartIndex.NECK), 0.3F);
    }

    @Override
    protected void applyRotations(EntityAnacondaPart entity, float ageInTicks, float rotationYaw, float partialTicks) {
        float newYaw = entity.rotationYawHead;
        GlStateManager.rotate(180.0F - newYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entity.rotationPitch, 1.0F, 0.0F, 0.0F);

        if (entity.deathTime > 0) {
            float f = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if (f > 1.0F) {
                f = 1.0F;
            }

            GlStateManager.rotate(f * this.getDeathMaxRotation(entity), 0.0F, 0.0F, 1.0F);
        } else if (entity.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
            if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
                GlStateManager.translate(0.0D, entity.height + 0.1F, 0.0D);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

    @Override
    protected boolean canRenderName(EntityAnacondaPart entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    @Override
    protected void preRenderCallback(EntityAnacondaPart entitylivingbaseIn, float partialTickTime) {
        this.mainModel = getModelForType(entitylivingbaseIn.getPartType());
        GlStateManager.scale(entitylivingbaseIn.getScale(), entitylivingbaseIn.getScale(), entitylivingbaseIn.getScale());
    }

    private AdvancedEntityModel getModelForType(AnacondaPartIndex partType) {
        switch (partType) {
            case BODY:
                return bodyModel;
            case NECK:
                return neckModel;
            case TAIL:
                return tailModel;
            default:
                return bodyModel;
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityAnacondaPart entity) {
        return RenderAnaconda.getAnacondaTexture(entity.isYellow(), entity.isShedding());
    }
}
