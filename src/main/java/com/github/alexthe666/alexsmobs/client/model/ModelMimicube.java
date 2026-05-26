package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.entity.Entity;

import com.github.alexthe666.alexsmobs.entity.EntityMimicube;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;

public class ModelMimicube extends AdvancedEntityModel<EntityMimicube> {
	public final AdvancedModelBox root;
	public final AdvancedModelBox body;
	public final AdvancedModelBox innerbody;
	public final AdvancedModelBox mouth;
	public final AdvancedModelBox eye_left;
	public final AdvancedModelBox eye_right;

	public ModelMimicube() {
		textureWidth = 64;
		textureHeight = 64;

		root = new AdvancedModelBox(this);
		root.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		body = new AdvancedModelBox(this);
		body.setRotationPoint(0.0F, 0.0F, 0.0F);
		root.addChild(body);
		body.setTextureOffset(0, 0).func_228303_a_(-8.0F, -14.0F, -8.0F, 16.0F, 14.0F, 16.0F, 0.0F, false);

		innerbody = new AdvancedModelBox(this);
		innerbody.setRotationPoint(0.0F, -7.0F, 0.0F);
		root.addChild(innerbody);
		innerbody.setTextureOffset(0, 31).func_228303_a_(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		mouth = new AdvancedModelBox(this);
		mouth.setRotationPoint(2.0F, 4.0F, -5.0F);
		innerbody.addChild(mouth);
		mouth.setTextureOffset(0, 12).func_228303_a_(-2.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, 0.0F, false);

		eye_left = new AdvancedModelBox(this);
		eye_left.setRotationPoint(3.5F, -1.5F, -4.0F);
		innerbody.addChild(eye_left);
		setRotationAngle(eye_left, 0.0F, 0.0F, 0.3054F);
		eye_left.setTextureOffset(0, 6).func_228303_a_(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, 0.0F, false);

		eye_right = new AdvancedModelBox(this);
		eye_right.setRotationPoint(-3.5F, -0.5F, -4.0F);
		innerbody.addChild(eye_right);
		setRotationAngle(eye_right, 0.0F, 0.0F, -0.3927F);
		eye_right.setTextureOffset(0, 0).func_228303_a_(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, 0.0F, false);
		this.updateDefaultPose();
	}
    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
            part.render(scale);
        }
    }


		public Iterable<ModelRenderer> getParts() {
		return ImmutableList.of(root);
	}

	@Override
	public Iterable<AdvancedModelBox> getAllParts() {
		return ImmutableList.of(root, body, innerbody, eye_left, eye_right, mouth);
	}

	@Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityMimicube) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityMimicube entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.resetToDefaultPose();
		float idleSpeed = 0.1F;
		float idleDegree = 1F;
		this.bob(innerbody, idleDegree, idleSpeed, false, limbSwing, limbSwingAmount);
		this.flap(innerbody, idleSpeed * 1.3F, idleDegree * 0.05F, false, 2F, 0F, ageInTicks, 1);
		float partial = Minecraft.getMinecraft().getRenderPartialTicks();
		float lvt_6_1_ = entity.prevSquishFactor + (entity.squishFactor - entity.prevSquishFactor) * partial;
		float lvt_7_1_ = 1.0F / (lvt_6_1_ + 1.0F);
		float squishScale = 1.0F / lvt_7_1_;
		this.innerbody.rotationPointY += lvt_6_1_ * -5F;
		this.body.setScale(1F, squishScale, 1F);
	}


	public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
		AdvancedModelBox.rotateAngleX = x;
		AdvancedModelBox.rotateAngleY = y;
		AdvancedModelBox.rotateAngleZ = z;
	}
}