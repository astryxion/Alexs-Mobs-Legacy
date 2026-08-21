package com.github.alexthe666.alexsmobs.client.model;// Made with Blockbench 3.7.4
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


import com.github.alexthe666.alexsmobs.entity.EntityKomodoDragon;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelKomodoDragon extends AdvancedEntityModel<EntityKomodoDragon> {
	private final AdvancedModelBox root;
	private final AdvancedModelBox body;
	private final AdvancedModelBox tail1;
	private final AdvancedModelBox tail2;
	private final AdvancedModelBox tail3;
	private final AdvancedModelBox legfront_left;
	private final AdvancedModelBox legfront_right;
	private final AdvancedModelBox legback_left;
	private final AdvancedModelBox legback_right;
	private final AdvancedModelBox neck;
	private final AdvancedModelBox head;
	private final AdvancedModelBox tongue;

	public ModelKomodoDragon() {
		textureWidth = 64;
		textureHeight = 64;
		root = new AdvancedModelBox(this);
		root.setRotationPoint(0.0F, 24.0F, 0.0F);
		body = new AdvancedModelBox(this);
		body.setRotationPoint(0.0F, -7.0F, 0.0F);
		root.addChild(body);
		body.setTextureOffset(0, 0).func_228303_a_(-4.0F, -3.5F, -10.0F, 8.0F, 6.0F, 18.0F, 0.0F, false);

		tail1 = new AdvancedModelBox(this);
		tail1.setRotationPoint(0.0F, -1.0F, 8.0F);
		body.addChild(tail1);
		setRotationAngle(tail1, -0.5672F, 0.0F, 0.0F);
		tail1.setTextureOffset(0, 25).func_228303_a_(-2.5F, -2.0F, -1.0F, 5.0F, 5.0F, 10.0F, 0.0F, false);

		tail2 = new AdvancedModelBox(this);
		tail2.setRotationPoint(0.0F, 0.5F, 9.0F);
		tail1.addChild(tail2);
		setRotationAngle(tail2, 0.4363F, 0.0F, 0.0F);
		tail2.setTextureOffset(20, 30).func_228303_a_(-1.5F, -2.0F, 0.0F, 3.0F, 4.0F, 11.0F, 0.0F, false);

		tail3 = new AdvancedModelBox(this);
		tail3.setRotationPoint(0.0F, 0.0F, 11.0F);
		tail2.addChild(tail3);
		setRotationAngle(tail3, 0.1745F, 0.0F, 0.0F);
		tail3.setTextureOffset(35, 0).func_228303_a_(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 10.0F, 0.0F, false);

		legfront_left = new AdvancedModelBox(this);
		legfront_left.setRotationPoint(3.5F, 0.5F, -7.5F);
		body.addChild(legfront_left);
		setRotationAngle(legfront_left, -0.3054F, 0.0F, -0.48F);
		legfront_left.setTextureOffset(21, 46).func_228303_a_(-1.5F, -1.5F, -1.5F, 3.0F, 9.0F, 3.0F, 0.0F, false);

		legfront_right = new AdvancedModelBox(this);
		legfront_right.setRotationPoint(-3.5F, 0.5F, -7.5F);
		body.addChild(legfront_right);
		setRotationAngle(legfront_right, -0.3054F, 0.0F, 0.48F);
		legfront_right.setTextureOffset(21, 46).func_228303_a_(-1.5F, -1.5F, -1.5F, 3.0F, 9.0F, 3.0F, 0.0F, true);

		legback_left = new AdvancedModelBox(this);
		legback_left.setRotationPoint(3.5F, 0.5F, 6.6F);
		body.addChild(legback_left);
		setRotationAngle(legback_left, 0.2182F, 0.0F, -0.5672F);
		legback_left.setTextureOffset(0, 0).func_228303_a_(-1.5F, -1.5F, -2.0F, 3.0F, 9.0F, 4.0F, 0.0F, false);

		legback_right = new AdvancedModelBox(this);
		legback_right.setRotationPoint(-3.5F, 0.5F, 6.6F);
		body.addChild(legback_right);
		setRotationAngle(legback_right, 0.2182F, 0.0F, 0.5672F);
		legback_right.setTextureOffset(0, 0).func_228303_a_(-1.5F, -1.5F, -2.0F, 3.0F, 9.0F, 4.0F, 0.0F, true);

		neck = new AdvancedModelBox(this);
		neck.setRotationPoint(0.0F, -0.5F, -10.0F);
		body.addChild(neck);
		setRotationAngle(neck, 0.1309F, 0.0F, 0.0F);
		neck.setTextureOffset(0, 41).func_228303_a_(-2.5F, -2.5F, -5.0F, 5.0F, 5.0F, 5.0F, 0.0F, false);

		head = new AdvancedModelBox(this);
		head.setRotationPoint(0.0F, 0.0F, -5.0F);
		neck.addChild(head);
		setRotationAngle(head, -0.1745F, 0.0F, 0.0F);
		head.setTextureOffset(38, 25).func_228303_a_(-2.0F, -1.9F, -6.0F, 4.0F, 4.0F, 6.0F, 0.0F, false);

		tongue = new AdvancedModelBox(this);
		tongue.setRotationPoint(0.0F, 0.5564F, -6.001F);
		head.addChild(tongue);
		tongue.setTextureOffset(44, 44).func_228303_a_(-0.5F, 0.0F, -5.0F, 1.0F, 0.0F, 5.0F, 0.0F, false);
		this.updateDefaultPose();
	}

	@Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityKomodoDragon) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityKomodoDragon entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.resetToDefaultPose();
		float idleSpeed = 0.7F;
		float idleDegree = 0.7F;
		float walkSpeed = 1F;
		float walkDegree = 0.7F;
		AdvancedModelBox[] tailBoxes = new AdvancedModelBox[]{tail1, tail2, tail3};
		this.bob(body, walkSpeed * 0.5F, walkDegree, true, limbSwing, limbSwingAmount);
		this.walk(legback_right, walkSpeed, walkDegree, false, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(legback_right, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(legback_left, walkSpeed, walkDegree, true, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(legback_left, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(legfront_left, walkSpeed, walkDegree, false, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(legfront_left, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(legfront_right, walkSpeed, walkDegree, true, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(legfront_right, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.swing(body, walkSpeed, walkDegree * 0.2F, false, 2F, 0F, limbSwing, limbSwingAmount);
		this.swing(neck, walkSpeed, walkDegree * 0.2F, true, 2F, 0F, limbSwing, limbSwingAmount);
		this.chainSwing(tailBoxes, walkSpeed, walkDegree * 0.7F, -1.5F, limbSwing, limbSwingAmount);
		float toungeF = (float) Math.min(Math.sin(ageInTicks * 0.3F), 0) * 6F;
		float toungeMinus = (float) Math.max(Math.sin(ageInTicks * 0.3F), 0);
		this.walk(tongue, idleSpeed * 2F, idleDegree, false, 0F, 0F, ageInTicks,  toungeMinus);
		this.tongue.rotationPointZ -= toungeF;
		this.faceTarget(netHeadYaw, headPitch, 2, neck, head);
	}

	    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            this.head.setScale(1, 1, 1);
            this.head.setShouldScaleChildren(true);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.35F, 0.35F, 0.35F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0F, 2.75F, 0.125F);
            for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
            net.minecraft.client.renderer.GlStateManager.popMatrix();
            this.head.setScale(1.0F, 1.0F, 1.0F);
            this.head.setShouldScaleChildren(false);
        } else {
            for (net.minecraft.client.model.ModelRenderer part : this.getParts()) {
                part.render(scale);
            }
        }
    }

		public Iterable<ModelRenderer> getParts() {
		return ImmutableList.of(root);
	}

	@Override
	public Iterable<AdvancedModelBox> getAllParts() {
		return ImmutableList.of(root, body, tail3, tail2, tail1, legback_left, legback_right, legfront_left, legfront_right, neck, head, tongue);
	}

	public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
		AdvancedModelBox.rotateAngleX = x;
		AdvancedModelBox.rotateAngleY = y;
		AdvancedModelBox.rotateAngleZ = z;
	}
}