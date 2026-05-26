package com.github.alexthe666.citadel.client.model;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class AdvancedEntityModel<T extends Entity> extends ModelBase {
   private float movementScale = 1.0F;
   private final Map<String, TextureOffset> modelTextureMap = Maps.newHashMap();

   public void updateDefaultPose() {
      this.getAllParts().forEach((modelRenderer) -> modelRenderer.updateDefaultPose());
   }

   protected void setTextureOffset(String partName, int x, int y) {
      this.modelTextureMap.put(partName, new TextureOffset(x, y));
   }

   public TextureOffset getTextureOffset(String partName) {
      return (TextureOffset)this.modelTextureMap.get(partName);
   }

   public void resetToDefaultPose() {
      this.getAllParts().forEach((modelRenderer) -> modelRenderer.resetToDefaultPose());
   }

   public void faceTarget(float yaw, float pitch, float rotationDivisor, AdvancedModelBox... boxes) {
      float actualRotationDivisor = rotationDivisor * (float)boxes.length;
      float yawAmount = yaw / (180F / (float)Math.PI) / actualRotationDivisor;
      float pitchAmount = pitch / (180F / (float)Math.PI) / actualRotationDivisor;

      for(AdvancedModelBox box : boxes) {
         box.rotateAngleY += yawAmount;
         box.rotateAngleX += pitchAmount;
      }

   }

   public void chainSwing(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);

      for(int index = 0; index < boxes.length; ++index) {
         boxes[index].rotateAngleY += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }

   }

   public void chainWave(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);

      for(int index = 0; index < boxes.length; ++index) {
         boxes[index].rotateAngleX += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }

   }

   public void chainFlap(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing, float swingAmount) {
      float offset = this.calculateChainOffset(rootOffset, boxes);

      for(int index = 0; index < boxes.length; ++index) {
         boxes[index].rotateAngleZ += this.calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
      }

   }

   private float calculateChainRotation(float speed, float degree, float swing, float swingAmount, float offset, int boxIndex) {
      return MathHelper.cos(swing * speed * this.movementScale + offset * (float)boxIndex) * swingAmount * degree * this.movementScale;
   }

   private float calculateChainOffset(double rootOffset, AdvancedModelBox... boxes) {
      return (float)(rootOffset * Math.PI / (double)(2 * boxes.length));
   }

   public float getMovementScale() {
      return this.movementScale;
   }

   public void setMovementScale(float movementScale) {
      this.movementScale = movementScale;
   }

   public void walk(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float walk, float walkAmount) {
      box.walk(speed, degree, invert, offset, weight, walk, walkAmount);
   }

   public void flap(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float flap, float flapAmount) {
      box.flap(speed, degree, invert, offset, weight, flap, flapAmount);
   }

   public void swing(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight, float swing, float swingAmount) {
      box.swing(speed, degree, invert, offset, weight, swing, swingAmount);
   }

   public void bob(AdvancedModelBox box, float speed, float degree, boolean bounce, float f, float f1) {
      box.bob(speed, degree, bounce, f, f1);
   }

   public float moveBox(float speed, float degree, boolean bounce, float f, float f1) {
      return bounce ? -MathHelper.abs(MathHelper.sin(f * speed) * f1 * degree) : MathHelper.sin(f * speed) * f1 * degree - f1 * degree;
   }

   public void setRotateAngle(AdvancedModelBox model, float x, float y, float z) {
      model.rotateAngleX = x;
      model.rotateAngleY = y;
      model.rotateAngleZ = z;
   }

   public void rotate(ModelAnimator animator, AdvancedModelBox model, float x, float y, float z) {
      animator.rotate(model, (float)Math.toRadians((double)x), (float)Math.toRadians((double)y), (float)Math.toRadians((double)z));
   }

   public void rotateMinus(ModelAnimator animator, AdvancedModelBox model, float x, float y, float z) {
      animator.rotate(model, (float)Math.toRadians((double)x) - model.defaultRotationX, (float)Math.toRadians((double)y) - model.defaultRotationY, (float)Math.toRadians((double)z) - model.defaultRotationZ);
   }

   public void progressRotation(AdvancedModelBox model, float progress, float rotX, float rotY, float rotZ, float divisor) {
      model.rotateAngleX += progress * (rotX - model.defaultRotationX) / divisor;
      model.rotateAngleY += progress * (rotY - model.defaultRotationY) / divisor;
      model.rotateAngleZ += progress * (rotZ - model.defaultRotationZ) / divisor;
   }

   public void progressRotationPrev(AdvancedModelBox model, float progress, float rotX, float rotY, float rotZ, float divisor) {
      model.rotateAngleX += progress * rotX / divisor;
      model.rotateAngleY += progress * rotY / divisor;
      model.rotateAngleZ += progress * rotZ / divisor;
   }

   public void progressPosition(AdvancedModelBox model, float progress, float x, float y, float z, float divisor) {
      model.rotationPointX += progress * (x - model.defaultPositionX) / divisor;
      model.rotationPointY += progress * (y - model.defaultPositionY) / divisor;
      model.rotationPointZ += progress * (z - model.defaultPositionZ) / divisor;
   }

   public void progressPositionPrev(AdvancedModelBox model, float progress, float x, float y, float z, float divisor) {
      model.rotationPointX += progress * x / divisor;
      model.rotationPointY += progress * y / divisor;
      model.rotationPointZ += progress * z / divisor;
   }

   public abstract Iterable<AdvancedModelBox> getAllParts();
}
