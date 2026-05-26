package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelAncientDart extends ModelBase {
	private final ModelRenderer root;
	private final ModelRenderer main;
	private final ModelRenderer feathers;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;

	public ModelAncientDart() {
		textureWidth = 32;
		textureHeight = 32;

		root = new ModelRenderer(this);
		root.setRotationPoint(0.0F, 0.0F, 0.0F);


		main = new ModelRenderer(this);
		main.setRotationPoint(0.0F, -1.0F, 0.0F);
		root.addChild(main);
		main.setTextureOffset(11, 0).addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.0F);
		main.setTextureOffset(0, 0).addBox(-0.5F, -0.5F, -5.0F, 1, 1, 4, 0.0F);

		feathers = new ModelRenderer(this);
		feathers.setRotationPoint(0.0F, 1.0F, 1.0F);
		main.addChild(feathers);


		cube_r1 = new ModelRenderer(this);
		cube_r1.setRotationPoint(0.0F, -1.0F, 0.5F);
		feathers.addChild(cube_r1);
		setRotationAngle(cube_r1, 0.0F, 0.0F, 0.7854F);
		cube_r1.setTextureOffset(0, 6).addBox(0.0F, -1.5F, -0.5F, 0, 3, 3, 0.0F);

		cube_r2 = new ModelRenderer(this);
		cube_r2.setRotationPoint(0.0F, -1.0F, 0.5F);
		feathers.addChild(cube_r2);
		setRotationAngle(cube_r2, 0.0F, 0.0F, -0.7854F);
		cube_r2.setTextureOffset(7, 6).addBox(0.0F, -1.5F, -0.5F, 0, 3, 3, 0.0F);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		GlStateManager.pushMatrix();
		root.render(scale);
		GlStateManager.popMatrix();
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}
