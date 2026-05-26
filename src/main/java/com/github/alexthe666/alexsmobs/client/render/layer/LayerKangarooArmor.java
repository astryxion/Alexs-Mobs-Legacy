package com.github.alexthe666.alexsmobs.client.render.layer;

import com.github.alexthe666.alexsmobs.client.model.ModelKangaroo;
import com.github.alexthe666.alexsmobs.client.render.RenderKangaroo;
import com.github.alexthe666.alexsmobs.entity.EntityKangaroo;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class LayerKangarooArmor implements LayerRenderer<EntityKangaroo> {

    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_RES_MAP = Maps.newHashMap();
    private final ModelBiped defaultBipedModel = new ModelBiped(1.0F);
    private final RenderKangaroo renderer;

    public LayerKangarooArmor(RenderKangaroo render) {
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
    public void doRenderLayer(EntityKangaroo roo, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.pushMatrix();
        if (roo.isRoger()) {
            ItemStack haloStack = new ItemStack(AMItemRegistry.HALO);
            GlStateManager.pushMatrix();
            translateToHead(scale);
            float f = 0.1F * (float) Math.sin((roo.ticksExisted + partialTicks) * 0.1F) + (roo.isChild() ? 0.2F : 0.0F);
            GlStateManager.translate(0.0F, -0.75F - f, -0.2F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(1.3F, 1.3F, 1.3F);
            Minecraft.getMinecraft().getRenderItem().renderItem(haloStack, ItemCameraTransforms.TransformType.GROUND);
            GlStateManager.popMatrix();
        }
        if (!roo.isChild()) {
            {
                GlStateManager.pushMatrix();
                ItemStack itemstack = roo.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
                if (itemstack.getItem() instanceof ItemArmor) {
                    ItemArmor armoritem = (ItemArmor) itemstack.getItem();
                    if (armoritem.getEquipmentSlot() == EntityEquipmentSlot.HEAD) {
                        ModelBiped a = defaultBipedModel;
                        a = getArmorModelHook(roo, itemstack, EntityEquipmentSlot.HEAD, a);
                        boolean notAVanillaModel = a != defaultBipedModel;
                        this.setModelSlotVisible(a, EntityEquipmentSlot.HEAD);
                        translateToHead(scale);
                        GlStateManager.translate(0.0F, 0.015F, -0.05F);
                        if (itemstack.getItem() == AMItemRegistry.FEDORA) {
                            GlStateManager.translate(0.0F, 0.05F, 0.0F);
                        }
                        GlStateManager.scale(0.7F, 0.7F, 0.7F);
                        boolean flag1 = itemstack.hasEffect();
                        int clampedLight = 0;
                        if (armoritem.hasOverlay(itemstack)) {
                            int i = armoritem.getColor(itemstack);
                            float cr = (float) (i >> 16 & 255) / 255.0F;
                            float cg = (float) (i >> 8 & 255) / 255.0F;
                            float cb = (float) (i & 255) / 255.0F;
                            renderHelmet(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, cr, cg, cb, getArmorResource(roo, itemstack, EntityEquipmentSlot.HEAD, null), notAVanillaModel);
                            renderHelmet(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(roo, itemstack, EntityEquipmentSlot.HEAD, "overlay"), notAVanillaModel);
                        } else {
                            renderHelmet(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(roo, itemstack, EntityEquipmentSlot.HEAD, null), notAVanillaModel);
                        }
                    }
                } else if (!itemstack.isEmpty()) {
                    translateToHead(scale);
                    GlStateManager.translate(0.0F, -0.2F, -0.1F);
                    GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.scale(1.0F, 1.0F, 1.0F);
                    Minecraft.getMinecraft().getRenderItem().renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
                }
                GlStateManager.popMatrix();
            }
            {
                GlStateManager.pushMatrix();
                ItemStack itemstack = roo.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (itemstack.getItem() instanceof ItemArmor) {
                    ItemArmor armoritem = (ItemArmor) itemstack.getItem();
                    if (armoritem.getEquipmentSlot() == EntityEquipmentSlot.CHEST) {
                        ModelBiped a = defaultBipedModel;
                        a = getArmorModelHook(roo, itemstack, EntityEquipmentSlot.CHEST, a);
                        boolean notAVanillaModel = a != defaultBipedModel;
                        this.setModelSlotVisible(a, EntityEquipmentSlot.CHEST);
                        translateToChest(scale);
                        GlStateManager.translate(0.0F, 0.25F, 0.0F);
                        GlStateManager.scale(1.0F, 1.0F, 1.0F);
                        boolean flag1 = itemstack.hasEffect();
                        if (armoritem.hasOverlay(itemstack)) {
                            int i = armoritem.getColor(itemstack);
                            float cr = (float) (i >> 16 & 255) / 255.0F;
                            float cg = (float) (i >> 8 & 255) / 255.0F;
                            float cb = (float) (i & 255) / 255.0F;
                            renderChestplate(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, cr, cg, cb, getArmorResource(roo, itemstack, EntityEquipmentSlot.CHEST, null), notAVanillaModel);
                            renderChestplate(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(roo, itemstack, EntityEquipmentSlot.CHEST, "overlay"), notAVanillaModel);
                        } else {
                            renderChestplate(roo, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, flag1, a, 1.0F, 1.0F, 1.0F, getArmorResource(roo, itemstack, EntityEquipmentSlot.CHEST, null), notAVanillaModel);
                        }
                    }
                }
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
    }

    private void translateToHead(float scale) {
        translateToChest(scale);
        this.renderer.getMainModel();
        ModelKangaroo model = (ModelKangaroo) this.renderer.getMainModel();
        model.neck.postRender(scale);
        model.head.postRender(scale);
    }

    private void translateToChest(float scale) {
        ModelKangaroo model = (ModelKangaroo) this.renderer.getMainModel();
        model.root.postRender(scale);
        model.body.postRender(scale);
        model.chest.postRender(scale);
    }

    private void syncKangarooPose(EntityKangaroo entity, ModelKangaroo km, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        km.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    private void renderChestplate(EntityKangaroo entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean glintIn, ModelBiped modelIn, float red, float green, float blue, ResourceLocation armorResource, boolean notAVanillaModel) {
        ModelKangaroo km = (ModelKangaroo) this.renderer.getMainModel();
        syncKangarooPose(entity, km, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.renderer.bindTexture(armorResource);
        float sitProgress = entity.prevSitProgress + (entity.sitProgress - entity.prevSitProgress) * Minecraft.getMinecraft().getRenderPartialTicks();
        modelIn.bipedBody.rotateAngleX = 90.0F * 0.017453292F;
        modelIn.bipedBody.rotateAngleY = 0.0F;
        modelIn.bipedBody.rotateAngleZ = 0.0F;
        modelIn.bipedBody.rotationPointX = 0.0F;
        modelIn.bipedBody.rotationPointY = 0.25F;
        modelIn.bipedBody.rotationPointZ = -7.6F;
        copyModelAngles(km.arm_right, modelIn.bipedRightArm);
        copyModelAngles(km.arm_left, modelIn.bipedLeftArm);
        modelIn.bipedLeftArm.rotationPointY = km.arm_left.rotationPointY - 4.0F + sitProgress * 0.25F;
        modelIn.bipedRightArm.rotationPointY = km.arm_right.rotationPointY - 4.0F + sitProgress * 0.25F;
        modelIn.bipedLeftArm.rotationPointZ = km.arm_left.rotationPointZ - 0.5F;
        modelIn.bipedRightArm.rotationPointZ = km.arm_right.rotationPointZ - 0.5F;
        GlStateManager.color(red, green, blue, 1.0F);
        modelIn.bipedBody.showModel = false;
        modelIn.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        modelIn.bipedBody.showModel = true;
        modelIn.bipedRightArm.showModel = false;
        modelIn.bipedLeftArm.showModel = false;
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.1F, 1.65F, 1.1F);
        modelIn.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
        modelIn.bipedRightArm.showModel = true;
        modelIn.bipedLeftArm.showModel = true;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderHelmet(EntityKangaroo entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean glintIn, ModelBiped modelIn, float red, float green, float blue, ResourceLocation armorResource, boolean notAVanillaModel) {
        ModelKangaroo km = (ModelKangaroo) this.renderer.getMainModel();
        syncKangarooPose(entity, km, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.renderer.bindTexture(armorResource);
        modelIn.bipedHead.rotateAngleX = 0.0F;
        modelIn.bipedHead.rotateAngleY = 0.0F;
        modelIn.bipedHead.rotateAngleZ = 0.0F;
        modelIn.bipedHeadwear.rotateAngleX = 0.0F;
        modelIn.bipedHeadwear.rotateAngleY = 0.0F;
        modelIn.bipedHeadwear.rotateAngleZ = 0.0F;
        modelIn.bipedHead.rotationPointX = 0.0F;
        modelIn.bipedHead.rotationPointY = 0.0F;
        modelIn.bipedHead.rotationPointZ = 0.0F;
        modelIn.bipedHeadwear.rotationPointX = 0.0F;
        modelIn.bipedHeadwear.rotationPointY = 0.0F;
        modelIn.bipedHeadwear.rotationPointZ = 0.0F;
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
