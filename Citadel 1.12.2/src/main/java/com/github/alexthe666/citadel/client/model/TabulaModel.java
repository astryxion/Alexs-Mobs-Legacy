package com.github.alexthe666.citadel.client.model;

import com.github.alexthe666.citadel.client.model.container.TabulaCubeContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaCubeGroupContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaModelContainer;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TabulaModel extends AdvancedEntityModel {
   protected Map<String, AdvancedModelBox> cubes;
   protected List<AdvancedModelBox> rootBoxes;
   protected ITabulaModelAnimator tabulaAnimator;
   public ModelAnimator llibAnimator;
   protected Map<String, AdvancedModelBox> identifierMap;
   protected double[] scale;

   public TabulaModel(TabulaModelContainer container, ITabulaModelAnimator tabulaAnimator) {
      this.cubes = new HashMap();
      this.rootBoxes = new ArrayList();
      this.identifierMap = new HashMap();
      this.textureWidth = container.getTextureWidth();
      this.textureHeight = container.getTextureHeight();
      this.tabulaAnimator = tabulaAnimator;

      for(TabulaCubeContainer cube : container.getCubes()) {
         this.parseCube(cube, (AdvancedModelBox)null);
      }

      container.getCubeGroups().forEach(this::parseCubeGroup);
      this.updateDefaultPose();
      this.scale = container.getScale();
      this.llibAnimator = ModelAnimator.create();
   }

   public TabulaModel(TabulaModelContainer container) {
      this(container, (ITabulaModelAnimator)null);
   }

   private void parseCubeGroup(TabulaCubeGroupContainer container) {
      for(TabulaCubeContainer cube : container.getCubes()) {
         this.parseCube(cube, (AdvancedModelBox)null);
      }

      container.getCubeGroups().forEach(this::parseCubeGroup);
   }

   private void parseCube(TabulaCubeContainer cube, AdvancedModelBox parent) {
      AdvancedModelBox box = this.createBox(cube);
      this.cubes.put(cube.getName(), box);
      this.identifierMap.put(cube.getIdentifier(), box);
      if (parent != null) {
         parent.addChild(box);
      } else {
         this.rootBoxes.add(box);
      }

      for(TabulaCubeContainer child : cube.getChildren()) {
         this.parseCube(child, box);
      }

   }

   private AdvancedModelBox createBox(TabulaCubeContainer cube) {
      int[] textureOffset = cube.getTextureOffset();
      double[] position = cube.getPosition();
      double[] rotation = cube.getRotation();
      double[] offset = cube.getOffset();
      int[] dimensions = cube.getDimensions();
      float scaleIn = 0.0F;
      AdvancedModelBox box = new AdvancedModelBox(this, cube.getName());
      box.setTextureOffset(textureOffset[0], textureOffset[1]);
      box.mirror = cube.isTextureMirrorEnabled();
      box.setRotationPoint((float)position[0], (float)position[1], (float)position[2]);
      box.func_228301_a_((float)offset[0], (float)offset[1], (float)offset[2], (float)dimensions[0], (float)dimensions[1], (float)dimensions[2], scaleIn);
      box.rotateAngleX = (float)Math.toRadians(rotation[0]);
      box.rotateAngleY = (float)Math.toRadians(rotation[1]);
      box.rotateAngleZ = (float)Math.toRadians(rotation[2]);
      return box;
   }

   public void func_225597_a_(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw, float rotationPitch) {
      if (this.tabulaAnimator != null) {
         this.tabulaAnimator.setRotationAngles(this, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, 1.0F);
      }

   }

   public AdvancedModelBox getCube(String name) {
      return (AdvancedModelBox)this.cubes.get(name);
   }

   public AdvancedModelBox getCubeByIdentifier(String identifier) {
      return (AdvancedModelBox)this.identifierMap.get(identifier);
   }

   public Map<String, AdvancedModelBox> getCubes() {
      return this.cubes;
   }

   public Iterable<ModelRenderer> func_225601_a_() {
      return ImmutableList.copyOf(this.rootBoxes);
   }

   public Iterable<AdvancedModelBox> getAllParts() {
      return ImmutableList.copyOf(this.cubes.values());
   }
}
