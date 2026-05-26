package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelMimicube;
import com.github.alexthe666.alexsmobs.client.render.RenderMimicube;
import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import com.google.common.collect.Maps;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class LayerMimicubeHelmet implements LayerRenderer<EntityMimicube> {

    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();
    private final ModelBiped defaultBipedModel = new ModelBiped(1.0F);
    private final RenderMimicube renderer;

    public LayerMimicubeHelmet(RenderMimicube render) {
        this.renderer = render;
    }

    public static ResourceLocation getArmorResource(net.minecraft.entity.Entity entity, ItemStack stack, EntityEquipmentSlot slot, @Nullable String type) {
        ItemArmor item = (ItemArmor) stack.getItem();
        String texture = item.getArmorMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, 1, type == null ? "" : String.format("_%s", type));

        s1 = net.minecraftforge.client.ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
        ResourceLocation resourcelocation = ARMOR_TEXTURE_RES_MAP.get(s1);

        if (resourcelocation == null) {
            resourcelocation = new ResourceLocation(s1);
            ARMOR_TEXTURE_RES_MAP.put(s1, resourcelocation);
        }

        return resourcelocation;
    }

    @Override
    public void doRenderLayer(EntityMimicube cube, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.pushMatrix();
        ItemStack itemstack = cube.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        float helmetSwap = (cube.prevHelmetSwapProgress + (cube.helmetSwapProgress - cube.prevHelmetSwapProgress) * partialTicks) * 0.2F;
        if (itemstack.getItem() instanceof ItemArmor) {
            ItemArmor armoritem = (ItemArmor) itemstack.getItem();
            if (armoritem.getEquipmentSlot() == EntityEquipmentSlot.HEAD) {
                ModelBiped a = defaultBipedModel;
                a = getArmorModelHook(cube, itemstack, EntityEquipmentSlot.HEAD, a);
                boolean notAVanillaModel = a != defaultBipedModel;

                this.setModelSlotVisible(a, EntityEquipmentSlot.HEAD);
                ModelMimicube mimicModel = (ModelMimicube) this.renderer.getMainModel();
                mimicModel.root.postRender(scale);
                mimicModel.innerbody.postRender(scale);
                GlStateManager.translate(0.0F, notAVanillaModel ? 0.25F : -0.75F, 0.0F);
                GlStateManager.scale(1.0F + 0.3F * (1.0F - helmetSwap), 1.0F + 0.3F * (1.0F - helmetSwap), 1.0F + 0.3F * (1.0F - helmetSwap));
                boolean flag1 = itemstack.hasEffect();
                if (helmetSwap > 0.0F) {
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 0.0F, 0.0F);
                }
                GlStateManager.rotate(360.0F * helmetSwap, 0.0F, 1.0F, 0.0F);
                if (armoritem.hasOverlay(itemstack)) {
                    int i = armoritem.getColor(itemstack);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float f1 = (float) (i >> 8 & 255) / 255.0F;
                    float f2 = (float) (i & 255) / 255.0F;
                    renderArmor(cube, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, f, f1, f2, getArmorResource(cube, itemstack, EntityEquipmentSlot.HEAD, null), notAVanillaModel);
                    renderArmor(cube, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(cube, itemstack, EntityEquipmentSlot.HEAD, "overlay"), notAVanillaModel);
                } else {
                    renderArmor(cube, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(cube, itemstack, EntityEquipmentSlot.HEAD, null), notAVanillaModel);
                }
            }
        }
        GlStateManager.popMatrix();
    }

    private void renderArmor(EntityMimicube entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean glintIn, ModelBiped modelIn, float red, float green, float blue, ResourceLocation armorResource, boolean notAVanillaModel) {
        this.renderer.bindTexture(armorResource);
        if (notAVanillaModel) {
            ModelMimicube mimicModel = (ModelMimicube) this.renderer.getMainModel();
            modelIn.bipedBody.rotationPointY = 0.0F;
            modelIn.bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);
            modelIn.bipedHeadwear.rotationPointY = 0.0F;
            copyModelAngles(mimicModel.body, modelIn.bipedHead);
            copyModelAngles(mimicModel.body, modelIn.bipedHeadwear);
            copyModelAngles(mimicModel.body, modelIn.bipedBody);
        }
        GlStateManager.color(red, green, blue, 1.0F);
        modelIn.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected void setModelSlotVisible(ModelBiped model, EntityEquipmentSlot slotIn) {
        this.setModelVisible(model);
        switch (slotIn) {
            case HEAD:
                model.bipedHead.showModel = true;
                model.bipedHeadwear.showModel = true;
                break;
            case CHEST:
                model.bipedBody.showModel = true;
                model.bipedRightArm.showModel = true;
                model.bipedLeftArm.showModel = true;
                break;
            case LEGS:
                model.bipedBody.showModel = true;
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                break;
            case FEET:
                model.bipedRightLeg.showModel = true;
                model.bipedLeftLeg.showModel = true;
                break;
            default:
                break;
        }
    }

    protected void setModelVisible(ModelBiped model) {
        model.setVisible(false);
    }

    protected ModelBiped getArmorModelHook(EntityLivingBase entity, ItemStack itemStack, EntityEquipmentSlot slot, ModelBiped model) {
        return net.minecraftforge.client.ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    public static void copyModelAngles(ModelRenderer from, ModelRenderer to) {
        to.rotateAngleX = from.rotateAngleX;
        to.rotateAngleY = from.rotateAngleY;
        to.rotateAngleZ = from.rotateAngleZ;
    }

}
