package com.github.alexthe666.citadel.client.patreon;

import com.github.alexthe666.citadel.ClientProxy;
import com.github.alexthe666.citadel.client.CitadelPatreonRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.model.ModelRenderer;

public class SpaceStationPatreonRenderer extends CitadelPatreonRenderer {
   private ResourceLocation texture;

   public SpaceStationPatreonRenderer(ResourceLocation texture) {
      this.texture = texture;
   }

   public void render(float partialTick, EntityLivingBase entity, float distanceIn, float rotateSpeed, float rotateHeight) {
      float tick = (float)entity.ticksExisted + partialTick;
      float bob = (float)(Math.sin((double)(tick * 0.1F)) * (double)1.0F * (double)0.05F - (double)0.05F);
      float scale = 0.4F;
      float rotation = MathHelper.wrapDegrees(tick * rotateSpeed % 360.0F);
      GlStateManager.pushMatrix();
      GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
      Vec3d look = entity.getLook(partialTick);
      GlStateManager.translate(0.0F, (double)(entity.getEyeHeight() + bob + (rotateHeight - 1.0F)), (double)((float)look.z * distanceIn));
      GlStateManager.pushMatrix();
      GlStateManager.rotate(75.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(scale, scale, scale);
      GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(rotation * 10.0F, 0.0F, 1.0F, 0.0F);
      Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
      ClientProxy.CITADEL_MODEL.resetToDefaultPose();

      for(ModelRenderer renderer : ClientProxy.CITADEL_MODEL.func_225601_a_()) {
         renderer.render(0.0625F);
      }

      GlStateManager.popMatrix();
      GlStateManager.popMatrix();
   }
}
