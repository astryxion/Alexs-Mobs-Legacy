package com.github.alexthe666.alexsmobs.client.model;

import net.minecraft.entity.Entity;

import com.github.alexthe666.alexsmobs.entity.EntityStradpole;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;

public class ModelStradpole extends AdvancedEntityModel<EntityStradpole> {
	private final AdvancedModelBox root;
	private final AdvancedModelBox body;
	private final AdvancedModelBox hair_left;
	private final AdvancedModelBox hair_right;
	private final AdvancedModelBox tail;

	public ModelStradpole() {
		textureWidth = 64;
		textureHeight = 64;

		root = new AdvancedModelBox(this);
		root.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		body = new AdvancedModelBox(this);
		body.setRotationPoint(0.0F, -4.0F, 0.0F);
		root.addChild(body);
		body.setTextureOffset(0, 0).func_228303_a_(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		hair_left = new AdvancedModelBox(this);
		hair_left.setRotationPoint(4.0F, -4.0F, 0.0F);
		body.addChild(hair_left);
		setRotationAngle(hair_left, 0.0F, 0.0F, 1.1345F);
		hair_left.setTextureOffset(0, 17).func_228303_a_(0.0F, 0.0F, -3.0F, 9.0F, 0.0F, 8.0F, 0.0F, false);

		hair_right = new AdvancedModelBox(this);
		hair_right.setRotationPoint(-4.0F, -4.0F, 0.0F);
		body.addChild(hair_right);
		setRotationAngle(hair_right, 0.0F, 0.0F, -1.1345F);
		hair_right.setTextureOffset(0, 17).func_228303_a_(-9.0F, 0.0F, -3.0F, 9.0F, 0.0F, 8.0F, 0.0F, true);

		tail = new AdvancedModelBox(this);
		tail.setRotationPoint(0.0F, 0.0F, 4.0F);
		body.addChild(tail);
		tail.setTextureOffset(24, 24).func_228303_a_(0.0F, -4.0F, 0.0F, 0.0F, 8.0F, 14.0F, 0.0F, false);
		this.updateDefaultPose();
	}

	@Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityStradpole) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityStradpole entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.resetToDefaultPose();
		float walkSpeed = 1F;
		float walkDegree = 0.4F;
		float idleSpeed = 0.1F;
		float idleDegree = 0.25F;
		this.flap(hair_right, idleSpeed, idleDegree, true, 1, 0F, ageInTicks, 1);
		this.flap(hair_left, idleSpeed, idleDegree, false, 1, 0F, ageInTicks, 1);
		this.flap(body, walkSpeed, walkDegree * 0.2F, true, 0, 0F, limbSwing, limbSwingAmount);
		this.swing(body, walkSpeed, walkDegree * 0.4F, true, 2, 0F, limbSwing, limbSwingAmount);
		this.swing(tail, walkSpeed * 1.4F, walkDegree * 2F, false, 2, 0F, limbSwing, limbSwingAmount);
		this.faceTarget(netHeadYaw, headPitch, 1.2F, body);
		float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
		float birdPitch = entity.prevSwimPitch + (entity.swimPitch - entity.prevSwimPitch) * partialTick;
		this.body.rotateAngleX += birdPitch * ((float)Math.PI / 180F);

	}

	/** Used when rendering a stradpole prop on the straddler (not a real {@link EntityStradpole}). */
	public void setRotationAnglesStraddlerLayer(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.resetToDefaultPose();
		float walkSpeed = 1F;
		float walkDegree = 0.4F;
		float idleSpeed = 0.1F;
		float idleDegree = 0.25F;
		this.flap(hair_right, idleSpeed, idleDegree, true, 1, 0F, ageInTicks, 1);
		this.flap(hair_left, idleSpeed, idleDegree, false, 1, 0F, ageInTicks, 1);
		this.flap(body, walkSpeed, walkDegree * 0.2F, true, 0, 0F, limbSwing, limbSwingAmount);
		this.swing(body, walkSpeed, walkDegree * 0.4F, true, 2, 0F, limbSwing, limbSwingAmount);
		this.swing(tail, walkSpeed * 1.4F, walkDegree * 2F, false, 2, 0F, limbSwing, limbSwingAmount);
		this.faceTarget(netHeadYaw, headPitch, 1.2F, body);
	}

	public void renderStraddlerLayer(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAnglesStraddlerLayer(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		this.root.render(scale);
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
		return ImmutableList.of(root, tail, body, hair_left, hair_right);
	}

	public void setRotationAngle(AdvancedModelBox advancedModelBox, float x, float y, float z) {
		advancedModelBox.rotateAngleX = x;
		advancedModelBox.rotateAngleY = y;
		advancedModelBox.rotateAngleZ = z;
	}
}