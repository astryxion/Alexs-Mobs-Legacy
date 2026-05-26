package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelBaldEagle;
import com.github.alexthe666.alexsmobs.entity.EntityBaldEagle;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBaldEagle extends RenderLiving<EntityBaldEagle> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/bald_eagle.png");
    private static final ResourceLocation TEXTURE_CAP = new ResourceLocation("alexsmobs:textures/entity/bald_eagle_hood.png");

    public RenderBaldEagle(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelBaldEagle(), 0.3F);
        this.addLayer(new CapLayer(this));
    }

    @Override
    public boolean shouldRender(EntityBaldEagle baldEagle, ICamera camera, double camX, double camY, double camZ) {
        if (baldEagle.isRiding() && baldEagle.getRidingEntity() instanceof EntityPlayer
                && Minecraft.getMinecraft().player == baldEagle.getRidingEntity()
                && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            return false;
        }
        return super.shouldRender(baldEagle, camera, camX, camY, camZ);
    }

    @Override
    protected void preRenderCallback(EntityBaldEagle eagle, float partialTickTime) {
        if (eagle.isRiding() && eagle.getRidingEntity() != null && eagle.getRidingEntity() instanceof EntityPlayer) {
            EntityPlayer mount = (EntityPlayer) eagle.getRidingEntity();
            boolean leftHand = false;
            ItemStack main = mount.getHeldItem(EnumHand.MAIN_HAND);
            ItemStack off = mount.getHeldItem(EnumHand.OFF_HAND);
            if (main.getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                leftHand = mount.getPrimaryHand() == EnumHandSide.LEFT;
            } else if (off.getItem() == AMItemRegistry.FALCONRY_GLOVE) {
                leftHand = mount.getPrimaryHand() != EnumHandSide.LEFT;
            }
            net.minecraft.client.renderer.entity.Render<?> playerRender = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(mount);
            if (Minecraft.getMinecraft().player == mount && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                return;
            }
            if (playerRender instanceof net.minecraft.client.renderer.entity.RenderLivingBase) {
                net.minecraft.client.renderer.entity.RenderLivingBase<?> livingR = (net.minecraft.client.renderer.entity.RenderLivingBase<?>) playerRender;
                if (livingR.getMainModel() instanceof ModelBiped) {
                    ModelBiped biped = (ModelBiped) livingR.getMainModel();
                    GlStateManager.pushMatrix();
                    if (leftHand) {
                        GlStateManager.translate(-0.3F, -0.7F, 0.5F);
                        biped.bipedLeftArm.postRender(0.0625F);
                        GlStateManager.translate(-0.2F, 0.5F, -0.18F);
                        GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(70.0F, 0.0F, 1.0F, 0.0F);
                    } else {
                        GlStateManager.translate(0.3F, -0.7F, 0.5F);
                        biped.bipedRightArm.postRender(0.0625F);
                        GlStateManager.translate(0.2F, 0.5F, -0.18F);
                        GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(-70.0F, 0.0F, 1.0F, 0.0F);
                    }
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBaldEagle entity) {
        return TEXTURE;
    }

    private static class CapLayer implements LayerRenderer<EntityBaldEagle> {
        private final RenderBaldEagle renderer;

        CapLayer(RenderBaldEagle parent) {
            this.renderer = parent;
        }

        @Override
        public void doRenderLayer(
                EntityBaldEagle entity,
                float limbSwing,
                float limbSwingAmount,
                float partialTicks,
                float ageInTicks,
                float netHeadYaw,
                float headPitch,
                float scale) {
            if (!entity.hasCap()) {
                return;
            }
            this.renderer.bindTexture(TEXTURE_CAP);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            ((ModelBaldEagle) this.renderer.getMainModel()).render(
                    entity,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    netHeadYaw,
                    headPitch,
                    scale);
            GlStateManager.disableBlend();
            this.renderer.bindTexture(this.renderer.getEntityTexture(entity));
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }
    }
}
