package com.github.alexthe666.alexsmobs.client.render;

import com.github.alexthe666.alexsmobs.client.model.ModelPotoo;
import com.github.alexthe666.alexsmobs.entity.EntityPotoo;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderPotoo extends RenderLiving<EntityPotoo> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexsmobs:textures/entity/potoo.png");

    public RenderPotoo(RenderManager renderManagerIn) {
        super(renderManagerIn, new ModelPotoo(), 0.35F);
    }

    @Override
    public boolean shouldRender(EntityPotoo bird, ICamera camera, double camX, double camY, double camZ) {
        if (bird.isRiding() && bird.getRidingEntity() instanceof EntityPlayer
                && Minecraft.getMinecraft().player == bird.getRidingEntity()
                && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
            return false;
        }
        return super.shouldRender(bird, camera, camX, camY, camZ);
    }

    @Override
    protected void preRenderCallback(EntityPotoo bird, float partialTickTime) {
        if (bird.isRiding() && bird.getRidingEntity() instanceof EntityPlayer) {
            EntityPlayer mount = (EntityPlayer) bird.getRidingEntity();
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
            if (playerRender instanceof RenderLivingBase) {
                RenderLivingBase<?> livingR = (RenderLivingBase<?>) playerRender;
                if (livingR.getMainModel() instanceof ModelBiped) {
                    ModelBiped biped = (ModelBiped) livingR.getMainModel();
                    if (leftHand) {
                        GlStateManager.translate(-0.3F, -0.7F, 0.5F);
                        biped.bipedLeftArm.postRender(0.0625F);
                        GlStateManager.translate(-0.1F, 0.6F, -0.1F);
                        GlStateManager.rotate(55.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(70.0F, 0.0F, 1.0F, 0.0F);
                    } else {
                        GlStateManager.translate(0.3F, -0.7F, 0.5F);
                        biped.bipedRightArm.postRender(0.0625F);
                        GlStateManager.translate(0.1F, 0.6F, -0.1F);
                        GlStateManager.rotate(55.0F, 1.0F, 0.0F, 0.0F);
                        GlStateManager.rotate(-70.0F, 0.0F, 1.0F, 0.0F);
                    }
                }
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityPotoo entity) {
        return TEXTURE;
    }
}
