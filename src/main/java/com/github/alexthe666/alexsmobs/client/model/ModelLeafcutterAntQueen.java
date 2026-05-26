package com.github.alexthe666.alexsmobs.client.model;// Made with Blockbench 3.8.3
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports


import com.github.alexthe666.alexsmobs.entity.EntityLeafcutterAnt;
import com.github.alexthe666.alexsmobs.entity.EntityOrca;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.ModelAnimator;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelLeafcutterAntQueen extends AdvancedEntityModel<EntityLeafcutterAnt> {
	private final AdvancedModelBox root;
	private final AdvancedModelBox body;
	private final AdvancedModelBox legront_left;
	private final AdvancedModelBox legront_right;
	private final AdvancedModelBox legmid_left;
	private final AdvancedModelBox legmid_right;
	private final AdvancedModelBox legback_left;
	private final AdvancedModelBox legback_right;
	private final AdvancedModelBox abdomen;
	private final AdvancedModelBox head;
	private final AdvancedModelBox antenna_left;
	private final AdvancedModelBox antenna_right;
	private final AdvancedModelBox fangs;
	private ModelAnimator animator;

	public ModelLeafcutterAntQueen() {
		textureWidth = 128;
		textureHeight = 128;

		root = new AdvancedModelBox(this);
		root.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		body = new AdvancedModelBox(this);
		body.setRotationPoint(0.0F, -10.0F, 0.0F);
		root.addChild(body);
		body.setTextureOffset(0, 25).func_228303_a_(-3.5F, -5.0F, -5.0F, 7.0F, 9.0F, 11.0F, 0.0F, false);

		legront_left = new AdvancedModelBox(this);
		legront_left.setRotationPoint(3.5F, 4.0F, -4.0F);
		body.addChild(legront_left);
		setRotationAngle(legront_left, 0.0F, 0.3927F, 0.0F);
		legront_left.setTextureOffset(38, 46).func_228303_a_(0.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, false);

		legront_right = new AdvancedModelBox(this);
		legront_right.setRotationPoint(-3.5F, 4.0F, -4.0F);
		body.addChild(legront_right);
		setRotationAngle(legront_right, 0.0F, -0.3927F, 0.0F);
		legront_right.setTextureOffset(38, 46).func_228303_a_(-9.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, true);

		legmid_left = new AdvancedModelBox(this);
		legmid_left.setRotationPoint(3.5F, 4.0F, 0.0F);
		body.addChild(legmid_left);
		legmid_left.setTextureOffset(19, 46).func_228303_a_(0.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, false);

		legmid_right = new AdvancedModelBox(this);
		legmid_right.setRotationPoint(-3.5F, 4.0F, 0.0F);
		body.addChild(legmid_right);
		legmid_right.setTextureOffset(19, 46).func_228303_a_(-9.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, true);

		legback_left = new AdvancedModelBox(this);
		legback_left.setRotationPoint(3.5F, 4.0F, 4.0F);
		body.addChild(legback_left);
		setRotationAngle(legback_left, 0.0F, -0.3927F, 0.0F);
		legback_left.setTextureOffset(0, 46).func_228303_a_(0.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, false);

		legback_right = new AdvancedModelBox(this);
		legback_right.setRotationPoint(-3.5F, 4.0F, 4.0F);
		body.addChild(legback_right);
		setRotationAngle(legback_right, 0.0F, 0.3927F, 0.0F);
		legback_right.setTextureOffset(0, 46).func_228303_a_(-9.0F, -2.0F, 0.0F, 9.0F, 8.0F, 0.0F, 0.0F, true);

		abdomen = new AdvancedModelBox(this);
		abdomen.setRotationPoint(0.0F, -1.0F, 6.0F);
		body.addChild(abdomen);
		abdomen.setTextureOffset(0, 0).func_228303_a_(-4.5F, -2.0F, 0.0F, 9.0F, 9.0F, 15.0F, 0.0F, false);

		head = new AdvancedModelBox(this);
		head.setRotationPoint(0.0F, 0.0F, -5.0F);
		body.addChild(head);
		head.setTextureOffset(37, 25).func_228303_a_(-4.5F, -4.0F, -7.0F, 9.0F, 7.0F, 7.0F, 0.0F, false);

		antenna_left = new AdvancedModelBox(this);
		antenna_left.setRotationPoint(0.5F, -4.0F, -7.0F);
		head.addChild(antenna_left);
		setRotationAngle(antenna_left, 0.0F, -0.5672F, 0.3054F);
		antenna_left.setTextureOffset(34, 0).func_228303_a_(0.0F, 0.0F, -12.0F, 9.0F, 0.0F, 12.0F, 0.0F, false);

		antenna_right = new AdvancedModelBox(this);
		antenna_right.setRotationPoint(-0.5F, -4.0F, -7.0F);
		head.addChild(antenna_right);
		setRotationAngle(antenna_right, 0.0F, 0.5672F, -0.3054F);
		antenna_right.setTextureOffset(34, 0).func_228303_a_(-9.0F, 0.0F, -12.0F, 9.0F, 0.0F, 12.0F, 0.0F, true);

		fangs = new AdvancedModelBox(this);
		fangs.setRotationPoint(0.0F, 2.0F, -7.0F);
		head.addChild(fangs);
		fangs.setTextureOffset(37, 40).func_228303_a_(-4.5F, -1.0F, -3.0F, 9.0F, 2.0F, 3.0F, 0.0F, false);
		this.updateDefaultPose();
		animator = ModelAnimator.create();
	}

		public Iterable<ModelRenderer> getParts() {
		return ImmutableList.of(root);
	}

	@Override
	public Iterable<AdvancedModelBox> getAllParts() {
		return ImmutableList.of(legback_left, legback_right, root, body, legront_left, legront_right, legmid_left, legmid_right, abdomen, head, antenna_left, antenna_right, fangs);
	}

	public void animate(IAnimatedEntity entity, float f, float f1, float f2, float f3, float f4) {
		this.resetToDefaultPose();
		animator.update(entity);
		animator.setAnimation(EntityLeafcutterAnt.ANIMATION_BITE);
		animator.startKeyframe(5);
		animator.move(body, 0, 0, -5);
		animator.move(fangs, 0, 0, 1);
		animator.rotate(head, (float)Math.toRadians(-25), 0, 0);
		animator.rotate(abdomen, (float)Math.toRadians(5), 0, 0);
		animator.rotate(antenna_left, (float)Math.toRadians(-25), (float)Math.toRadians(-25), 0);
		animator.rotate(antenna_right, (float)Math.toRadians(-25), (float)Math.toRadians(25), 0);
		animator.endKeyframe();
		animator.startKeyframe(5);
		animator.move(fangs, 0, 0, -0);
		animator.move(body, 0, 0, 2);
		animator.rotate(head, (float)Math.toRadians(25), 0, 0);
		animator.endKeyframe();
		animator.resetKeyframe(3);

	}

	@Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityLeafcutterAnt) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityLeafcutterAnt entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		animate(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float idleSpeed = 0.25F;
		float idleDegree = 0.25F;
		float walkSpeed = 1F;
		float walkDegree = 1F;
		this.swing(antenna_left, idleSpeed, idleDegree, true, 1, 0.1F, ageInTicks, 1);
		this.swing(antenna_right, idleSpeed, idleDegree, false, 1, 0.1F, ageInTicks, 1);
		this.walk(antenna_left, idleSpeed, idleDegree * 0.25F, false, -1, -0.05F, ageInTicks, 1);
		this.walk(antenna_right, idleSpeed, idleDegree * 0.25F, false, -1, -0.05F, ageInTicks, 1);
		this.swing(legback_right, walkSpeed, walkDegree * 1.2F, false, 0, 0.2F, limbSwing, limbSwingAmount);
		this.flap(legback_right, walkSpeed, walkDegree * 0.8F, false, -1.5F, 0.4F, limbSwing, limbSwingAmount);
		this.swing(legront_right, walkSpeed, walkDegree, false, 0, -0.3F, limbSwing, limbSwingAmount);
		this.flap(legront_right, walkSpeed, walkDegree * 0.8F, false, -1.5F, 0.4F, limbSwing, limbSwingAmount);
		this.swing(legmid_left, walkSpeed, walkDegree, false, 0, 0F, limbSwing, limbSwingAmount);
		this.flap(legmid_left, walkSpeed, walkDegree * 0.8F, false, -1.5F, -0.4F, limbSwing, limbSwingAmount);
		this.bob(body, walkSpeed * 2F, walkDegree * -0.6F, false, limbSwing, limbSwingAmount);
		float offsetleft = 2F;
		this.swing(legback_left, walkSpeed, -walkDegree * 1.2F, false, offsetleft, -0.2F, limbSwing, limbSwingAmount);
		this.flap(legback_left, walkSpeed, walkDegree * 0.8F, false, offsetleft-1.5F, -0.4F, limbSwing, limbSwingAmount);
		this.swing(legront_left, walkSpeed, -walkDegree, false, offsetleft, 0.3F, limbSwing, limbSwingAmount);
		this.flap(legront_left, walkSpeed, walkDegree * 0.8F, false, offsetleft + 1.5F, -0.4F, limbSwing, limbSwingAmount);
		this.swing(legmid_right, walkSpeed, -walkDegree, false, offsetleft,  0, limbSwing, limbSwingAmount);
		this.flap(legmid_right, walkSpeed, walkDegree * 0.8F, false, offsetleft-1.5F, 0.4F, limbSwing, limbSwingAmount);
		this.swing(abdomen, walkSpeed, walkDegree * 0.4F, false, 3, 0, limbSwing, limbSwingAmount);
		this.faceTarget(netHeadYaw, headPitch, 1.2F, head);

	}

	    @Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            this.head.setScale(1, 1, 1);
            this.head.setShouldScaleChildren(true);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.5F, 0.5F, 0.5F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0F * scale, 1.5F * scale, 0.125F * scale);
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

	public void setRotationAngle(AdvancedModelBox AdvancedModelBox, float x, float y, float z) {
		AdvancedModelBox.rotateAngleX = x;
		AdvancedModelBox.rotateAngleY = y;
		AdvancedModelBox.rotateAngleZ = z;
	}
}