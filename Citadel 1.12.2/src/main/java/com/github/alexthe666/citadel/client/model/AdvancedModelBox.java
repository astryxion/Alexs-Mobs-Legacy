package com.github.alexthe666.citadel.client.model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class AdvancedModelBox extends ModelRenderer {
   public float defaultRotationX;
   public float defaultRotationY;
   public float defaultRotationZ;
   public float defaultOffsetX;
   public float defaultOffsetY;
   public float defaultOffsetZ;
   public float defaultPositionX;
   public float defaultPositionY;
   public float defaultPositionZ;
   public float scaleX;
   public float scaleY;
   public float scaleZ;
   public int field_78803_o;
   public int field_78813_p;
   public boolean scaleChildren;
   private AdvancedEntityModel model;
   private AdvancedModelBox parent;
   private int displayList;
   private boolean compiled;
   public ObjectList<TabulaModelRenderUtils.ModelBox> field_78804_l;
   public ObjectList<ModelRenderer> field_78805_m;
   private float textureWidth;
   private float textureHeight;
   public float offsetX;
   public float offsetY;
   public float offsetZ;
   public String boxName;

   public AdvancedModelBox(AdvancedEntityModel model, String name) {
      super(model);
      this.scaleX = 1.0F;
      this.scaleY = 1.0F;
      this.scaleZ = 1.0F;
      this.boxName = "";
      this.textureWidth = (float)model.textureWidth;
      this.textureHeight = (float)model.textureHeight;
      this.model = model;
      this.field_78804_l = new ObjectArrayList();
      this.field_78805_m = new ObjectArrayList();
      this.boxName = name;
   }

   public AdvancedModelBox(AdvancedEntityModel model) {
      this(model, (String)null);
      this.textureWidth = (float)model.textureWidth;
      this.textureHeight = (float)model.textureHeight;
      this.field_78804_l = new ObjectArrayList();
      this.field_78805_m = new ObjectArrayList();
   }

   public AdvancedModelBox(AdvancedEntityModel model, int textureOffsetX, int textureOffsetY) {
      this(model);
      this.textureWidth = (float)model.textureWidth;
      this.textureHeight = (float)model.textureHeight;
      this.setTextureOffset(textureOffsetX, textureOffsetY);
      this.field_78804_l = new ObjectArrayList();
      this.field_78805_m = new ObjectArrayList();
   }

   public ModelRenderer func_78787_b(int p_78787_1_, int p_78787_2_) {
      this.textureWidth = (float)p_78787_1_;
      this.textureHeight = (float)p_78787_2_;
      return this;
   }

   public ModelRenderer func_217178_a(String p_217178_1_, float p_217178_2_, float p_217178_3_, float p_217178_4_, int p_217178_5_, int p_217178_6_, int p_217178_7_, float p_217178_8_, int p_217178_9_, int p_217178_10_) {
      this.setTextureOffset(p_217178_9_, p_217178_10_);
      this.addBox(this.field_78803_o, this.field_78813_p, p_217178_2_, p_217178_3_, p_217178_4_, (float)p_217178_5_, (float)p_217178_6_, (float)p_217178_7_, p_217178_8_, p_217178_8_, p_217178_8_, this.mirror, false);
      return this;
   }

   public ModelRenderer func_228300_a_(float p_228300_1_, float p_228300_2_, float p_228300_3_, float p_228300_4_, float p_228300_5_, float p_228300_6_) {
      this.addBox(this.field_78803_o, this.field_78813_p, p_228300_1_, p_228300_2_, p_228300_3_, p_228300_4_, p_228300_5_, p_228300_6_, 0.0F, 0.0F, 0.0F, this.mirror, false);
      return this;
   }

   public ModelRenderer func_228304_a_(float p_228304_1_, float p_228304_2_, float p_228304_3_, float p_228304_4_, float p_228304_5_, float p_228304_6_, boolean p_228304_7_) {
      this.addBox(this.field_78803_o, this.field_78813_p, p_228304_1_, p_228304_2_, p_228304_3_, p_228304_4_, p_228304_5_, p_228304_6_, 0.0F, 0.0F, 0.0F, p_228304_7_, false);
      return this;
   }

   public void func_228301_a_(float p_228301_1_, float p_228301_2_, float p_228301_3_, float p_228301_4_, float p_228301_5_, float p_228301_6_, float p_228301_7_) {
      this.addBox(this.field_78803_o, this.field_78813_p, p_228301_1_, p_228301_2_, p_228301_3_, p_228301_4_, p_228301_5_, p_228301_6_, p_228301_7_, p_228301_7_, p_228301_7_, this.mirror, false);
   }

   public void func_228302_a_(float p_228302_1_, float p_228302_2_, float p_228302_3_, float p_228302_4_, float p_228302_5_, float p_228302_6_, float p_228302_7_, float p_228302_8_, float p_228302_9_) {
      this.addBox(this.field_78803_o, this.field_78813_p, p_228302_1_, p_228302_2_, p_228302_3_, p_228302_4_, p_228302_5_, p_228302_6_, p_228302_7_, p_228302_8_, p_228302_9_, this.mirror, false);
   }

   public void func_228303_a_(float p_228303_1_, float p_228303_2_, float p_228303_3_, float p_228303_4_, float p_228303_5_, float p_228303_6_, float p_228303_7_, boolean p_228303_8_) {
      this.addBox(this.field_78803_o, this.field_78813_p, p_228303_1_, p_228303_2_, p_228303_3_, p_228303_4_, p_228303_5_, p_228303_6_, p_228303_7_, p_228303_7_, p_228303_7_, p_228303_8_, false);
   }

   private void addBox(int p_228305_1_, int p_228305_2_, float p_228305_3_, float p_228305_4_, float p_228305_5_, float p_228305_6_, float p_228305_7_, float p_228305_8_, float p_228305_9_, float p_228305_10_, float p_228305_11_, boolean p_228305_12_, boolean p_228305_13_) {
      this.field_78804_l.add(new TabulaModelRenderUtils.ModelBox(p_228305_1_, p_228305_2_, p_228305_3_, p_228305_4_, p_228305_5_, p_228305_6_, p_228305_7_, p_228305_8_, p_228305_9_, p_228305_10_, p_228305_11_, p_228305_12_, this.textureWidth, this.textureHeight));
   }

   public void setShouldScaleChildren(boolean scaleChildren) {
      this.scaleChildren = scaleChildren;
   }

   public void setScale(float scaleX, float scaleY, float scaleZ) {
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
   }

   public void setScaleX(float scaleX) {
      this.scaleX = scaleX;
   }

   public void setScaleY(float scaleY) {
      this.scaleY = scaleY;
   }

   public void setScaleZ(float scaleZ) {
      this.scaleZ = scaleZ;
   }

   public void updateDefaultPose() {
      this.defaultRotationX = this.rotateAngleX;
      this.defaultRotationY = this.rotateAngleY;
      this.defaultRotationZ = this.rotateAngleZ;
      this.defaultPositionX = this.rotationPointX;
      this.defaultPositionY = this.rotationPointY;
      this.defaultPositionZ = this.rotationPointZ;
   }

   public void resetToDefaultPose() {
      this.rotateAngleX = this.defaultRotationX;
      this.rotateAngleY = this.defaultRotationY;
      this.rotateAngleZ = this.defaultRotationZ;
      this.rotationPointX = this.defaultPositionX;
      this.rotationPointY = this.defaultPositionY;
      this.rotationPointZ = this.defaultPositionZ;
   }

   @Override
   public void addChild(ModelRenderer child) {
      super.addChild(child);
      this.field_78805_m.add(child);
      if (child instanceof AdvancedModelBox) {
         AdvancedModelBox advancedChild = (AdvancedModelBox)child;
         advancedChild.setParent(this);
      }

   }

   public AdvancedModelBox getParent() {
      return this.parent;
   }

   public void setParent(AdvancedModelBox parent) {
      this.parent = parent;
   }

   public void parentedPostRender(float scale) {
      if (this.parent != null) {
         this.parent.parentedPostRender(scale);
      }

   }

   public void renderWithParents(float scale) {
      if (this.parent != null) {
         this.parent.renderWithParents(scale);
      }

   }

   public void func_78785_a(float scale) {
      if (this.showModel && (!this.field_78804_l.isEmpty() || !this.field_78805_m.isEmpty())) {
         GlStateManager.pushMatrix();
         GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
         if (this.rotateAngleZ != 0.0F) {
            GlStateManager.rotate(this.rotateAngleZ * 57.295776F, 0.0F, 0.0F, 1.0F);
         }

         if (this.rotateAngleY != 0.0F) {
            GlStateManager.rotate(this.rotateAngleY * 57.295776F, 0.0F, 1.0F, 0.0F);
         }

         if (this.rotateAngleX != 0.0F) {
            GlStateManager.rotate(this.rotateAngleX * 57.295776F, 1.0F, 0.0F, 0.0F);
         }

         GlStateManager.scale(this.scaleX, this.scaleY, this.scaleZ);
         this.doRender(scale);
         ObjectListIterator var9 = this.field_78805_m.iterator();
         if (!this.scaleChildren) {
            GlStateManager.scale(1.0F / Math.max(this.scaleX, 1.0E-4F), 1.0F / Math.max(this.scaleY, 1.0E-4F), 1.0F / Math.max(this.scaleZ, 1.0E-4F));
         }

         while(var9.hasNext()) {
            ModelRenderer lvt_10_1_ = (ModelRenderer)var9.next();
            lvt_10_1_.render(scale);
         }

         GlStateManager.popMatrix();
      }

   }

   private void doRender(float scale) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      ObjectListIterator var11 = this.field_78804_l.iterator();

      while(var11.hasNext()) {
         TabulaModelRenderUtils.ModelBox lvt_12_1_ = (TabulaModelRenderUtils.ModelBox)var11.next();

         for(TabulaModelRenderUtils.TexturedQuad lvt_16_1_ : lvt_12_1_.quads) {
            float lvt_18_1_ = (float)lvt_16_1_.normal.x;
            float lvt_19_1_ = (float)lvt_16_1_.normal.y;
            float lvt_20_1_ = (float)lvt_16_1_.normal.z;
            bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.OLDMODEL_POSITION_TEX_NORMAL);

            for(int lvt_21_1_ = 0; lvt_21_1_ < 4; ++lvt_21_1_) {
               TabulaModelRenderUtils.PositionTextureVertex lvt_22_1_ = lvt_16_1_.vertexPositions[lvt_21_1_];
               double lvt_23_1_ = lvt_22_1_.position.x * (double)scale;
               double lvt_24_1_ = lvt_22_1_.position.y * (double)scale;
               double lvt_25_1_ = lvt_22_1_.position.z * (double)scale;
               bufferbuilder.pos(lvt_23_1_, lvt_24_1_, lvt_25_1_).tex((double)lvt_22_1_.textureU, (double)lvt_22_1_.textureV).normal(lvt_18_1_, lvt_19_1_, lvt_20_1_).endVertex();
            }

            tessellator.draw();
         }
      }

   }

   public AdvancedEntityModel getModel() {
      return this.model;
   }

   private float calculateRotation(float speed, float degree, boolean invert, float offset, float weight, float f, float f1) {
      float movementScale = this.model.getMovementScale();
      float rotation = MathHelper.cos(f * speed * movementScale + offset) * degree * movementScale * f1 + weight * f1;
      return invert ? -rotation : rotation;
   }

   public void walk(float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
      this.rotateAngleX += this.calculateRotation(speed, degree, invert, offset, weight, walk, walkAmount);
   }

   public void flap(float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
      this.rotateAngleZ += this.calculateRotation(speed, degree, invert, offset, weight, flap, flapAmount);
   }

   public void swing(float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
      this.rotateAngleY += this.calculateRotation(speed, degree, invert, offset, weight, swing, swingAmount);
   }

   public void bob(float speed, float degree, boolean bounce, float f, float f1) {
      float movementScale = this.model.getMovementScale();
      degree *= movementScale;
      speed *= movementScale;
      float bob = (float)(Math.sin((double)(f * speed)) * (double)f1 * (double)degree - (double)(f1 * degree));
      if (bounce) {
         bob = (float)(-Math.abs(Math.sin((double)(f * speed)) * (double)f1 * (double)degree));
      }

      this.rotationPointY += bob;
   }

   public AdvancedModelBox setTextureOffset(int textureOffsetX, int textureOffsetY) {
      this.field_78803_o = textureOffsetX;
      this.field_78813_p = textureOffsetY;
      return this;
   }

   public void transitionTo(AdvancedModelBox to, float timer, float maxTime) {
      this.rotateAngleX += (to.rotateAngleX - this.rotateAngleX) / maxTime * timer;
      this.rotateAngleY += (to.rotateAngleY - this.rotateAngleY) / maxTime * timer;
      this.rotateAngleZ += (to.rotateAngleZ - this.rotateAngleZ) / maxTime * timer;
      this.rotationPointX += (to.rotationPointX - this.rotationPointX) / maxTime * timer;
      this.rotationPointY += (to.rotationPointY - this.rotationPointY) / maxTime * timer;
      this.rotationPointZ += (to.rotationPointZ - this.rotationPointZ) / maxTime * timer;
      this.offsetX += (to.offsetX - this.offsetX) / maxTime * timer;
      this.offsetY += (to.offsetY - this.offsetY) / maxTime * timer;
      this.offsetZ += (to.offsetZ - this.offsetZ) / maxTime * timer;
   }
}
