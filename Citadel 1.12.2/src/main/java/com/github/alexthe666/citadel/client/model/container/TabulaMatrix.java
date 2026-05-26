package com.github.alexthe666.citadel.client.model.container;

import java.util.Stack;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TabulaMatrix {
   public Stack<Matrix4f> matrixStack = new Stack();

   public TabulaMatrix() {
      Matrix4f matrix = new Matrix4f();
      matrix.setIdentity();
      this.matrixStack.push(matrix);
   }

   public void push() {
      this.matrixStack.push(new Matrix4f((Matrix4f)this.matrixStack.peek()));
   }

   public void pop() {
      if (this.matrixStack.size() < 2) {
         try {
            throw new Exception("Stack Underflow for tabula matrix!!!");
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      this.matrixStack.pop();
   }

   public void translate(float x, float y, float z) {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      Matrix4f translation = new Matrix4f();
      translation.setIdentity();
      translation.setTranslation(new Vector3f(x, y, z));
      matrix.mul(translation);
   }

   public void translate(double x, double y, double z) {
      this.translate((float)x, (float)y, (float)z);
   }

   public void rotate(double angle, double x, double y, double z) {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      Matrix4f rotation = new Matrix4f();
      rotation.setIdentity();
      rotation.set(new AxisAngle4f((float)x, (float)y, (float)z, (float)Math.toRadians(angle)));
      matrix.mul(rotation);
   }

   public void rotate(float angle, float x, float y, float z) {
      this.rotate(angle, x, y, z);
   }

   public void rotate(Matrix4f qaut) {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      Matrix4f rotation = new Matrix4f();
      rotation.set(qaut);
      matrix.mul(rotation);
   }

   public void scale(float x, float y, float z) {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      Matrix4f scale = new Matrix4f();
      scale.setIdentity();
      scale.m00 = x;
      scale.m11 = y;
      scale.m22 = z;
      matrix.mul(scale);
   }

   public void scale(double x, double y, double z) {
      this.scale((float)x, (float)y, (float)z);
   }

   public void transform(Vector3f point) {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      matrix.transform(point);
   }

   public Vector3f getTranslation() {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      Vector3f translation = new Vector3f(matrix.m03, matrix.m13, matrix.m23);
      return translation;
   }

   public Matrix4f getRotation() {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      return new Matrix4f(matrix);
   }

   public Vector3f getScale() {
      Matrix4f matrix = (Matrix4f)this.matrixStack.peek();
      return new Vector3f(1.0F, 1.0F, 1.0F);
   }

   public void multiply(TabulaMatrix matrix) {
      ((Matrix4f)this.matrixStack.peek()).mul((Matrix4f)matrix.matrixStack.peek());
   }

   public void multiply(Matrix4f matrix) {
      ((Matrix4f)this.matrixStack.peek()).mul(matrix);
   }

   public void add(TabulaMatrix matrix) {
      ((Matrix4f)this.matrixStack.peek()).add((Matrix4f)matrix.matrixStack.peek());
   }

   public void add(Matrix4f matrix) {
      ((Matrix4f)this.matrixStack.peek()).add(new Matrix4f(matrix));
   }

   public void invert() {
      ((Matrix4f)this.matrixStack.peek()).invert();
   }
}
