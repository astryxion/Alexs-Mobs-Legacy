package com.github.alexthe666.citadel.client;

import com.github.alexthe666.citadel.Citadel;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CitadelItemstackRenderer extends TileEntityItemStackRenderer {
   public void func_192838_a(ItemStack stack, float partialTicks) {
      float ticksExisted = (float)Minecraft.getSystemTime() / 50.0F + partialTicks;
      if (stack.getItem() == Citadel.FANCY_ITEM) {
         Random random = new Random();
         boolean animateAnyways = false;
         ItemStack toRender = null;
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayItem")) {
            String id = stack.getTagCompound().getString("DisplayItem");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null) {
               toRender = new ItemStack(item);
            }

            if (stack.getTagCompound().hasKey("DisplayItemNBT")) {
               try {
                  toRender.setTagCompound(stack.getTagCompound().getCompoundTag("DisplayItemNBT"));
               } catch (Exception var16) {
                  toRender = new ItemStack(Items.APPLE);
               }
            }
         }

         if (toRender == null) {
            animateAnyways = true;
            toRender = new ItemStack(Items.APPLE);
         }

         GlStateManager.pushMatrix();
         GlStateManager.translate(0.5F, 0.5F, 0.5F);
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayShake") && stack.getTagCompound().getBoolean("DisplayShake")) {
            GlStateManager.translate((random.nextFloat() - 0.5F) * 0.1F, (random.nextFloat() - 0.5F) * 0.1F, (random.nextFloat() - 0.5F) * 0.1F);
         }

         if (animateAnyways || stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayBob") && stack.getTagCompound().getBoolean("DisplayBob")) {
            GlStateManager.translate(0.0F, 0.05F + 0.1F * MathHelper.sin(0.3F * ticksExisted), 0.0F);
         }

         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplaySpin") && stack.getTagCompound().getBoolean("DisplaySpin")) {
            GlStateManager.rotate(6.0F * ticksExisted, 0.0F, 1.0F, 0.0F);
         }

         if (animateAnyways || stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayZoom") && stack.getTagCompound().getBoolean("DisplayZoom")) {
            float scale = (float)((double)1.0F + (double)0.15F * (Math.sin((double)(ticksExisted * 0.3F)) + (double)1.0F));
            GlStateManager.scale(scale, scale, scale);
         }

         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayScale") && stack.getTagCompound().getFloat("DisplayScale") != 1.0F) {
            float scale = stack.getTagCompound().getFloat("DisplayScale");
            GlStateManager.scale(scale, scale, scale);
         }

         Minecraft.getMinecraft().getRenderItem().renderItem(toRender, ItemCameraTransforms.TransformType.NONE);
         GlStateManager.popMatrix();
      }

      if (stack.getItem() == Citadel.EFFECT_ITEM) {
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         GlStateManager.enableAlpha();
         GlStateManager.enableDepth();
         Potion effect;
         if (stack.hasTagCompound() && stack.getTagCompound().hasKey("DisplayEffect")) {
            String id = stack.getTagCompound().getString("DisplayEffect");
            effect = ForgeRegistries.POTIONS.getValue(new ResourceLocation(id));
         } else {
            int size = ForgeRegistries.POTIONS.getValuesCollection().size();
            int time = (int)(Minecraft.getSystemTime() / 500L);
            effect = (Potion)ForgeRegistries.POTIONS.getValuesCollection().toArray()[time % size];
            if (effect == null) {
               effect = MobEffects.SPEED;
            }
         }

         if (effect == null) {
            effect = MobEffects.SPEED;
         }

         TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:items/potion_overlay");
         GlStateManager.pushMatrix();
         GlStateManager.translate(0.0F, 0.0F, 0.5F);
         Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         int br = 255;
         bufferbuilder.pos(1.0D, 1.0D, 0.0D).tex((double)sprite.getMaxU(), (double)sprite.getMinV()).color(br, br, br, 255).endVertex();
         bufferbuilder.pos(0.0D, 1.0D, 0.0D).tex((double)sprite.getMinU(), (double)sprite.getMinV()).color(br, br, br, 255).endVertex();
         bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex((double)sprite.getMinU(), (double)sprite.getMaxV()).color(br, br, br, 255).endVertex();
         bufferbuilder.pos(1.0D, 0.0D, 0.0D).tex((double)sprite.getMaxU(), (double)sprite.getMaxV()).color(br, br, br, 255).endVertex();
         tessellator.draw();
         GlStateManager.popMatrix();
      }

   }
}
