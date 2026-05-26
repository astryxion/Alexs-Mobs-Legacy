package com.github.alexthe666.alexsmobs.client.model;
import com.github.alexthe666.alexsmobs.entity.EntityTasmanianDevil;
import com.github.alexthe666.citadel.animation.IAnimatedEntity;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.model.AdvancedModelBox;
import com.github.alexthe666.citadel.client.model.ModelAnimator;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTasmanianDevil extends AdvancedEntityModel<EntityTasmanianDevil> {
	private final AdvancedModelBox root;
	private final AdvancedModelBox body;
	private final AdvancedModelBox tail;
	private final AdvancedModelBox head;
	private final AdvancedModelBox ear_left;
	private final AdvancedModelBox ear_right;
	private final AdvancedModelBox upper_jaw;
	private final AdvancedModelBox lower_jaw;
	private final AdvancedModelBox arm_left;
	private final AdvancedModelBox arm_right;
	private final AdvancedModelBox leg_left;
	private final AdvancedModelBox leg_right;
	private ModelAnimator animator;

	public ModelTasmanianDevil() {
		textureWidth = 64;
		textureHeight = 64;

		root = new AdvancedModelBox(this);
		root.setRotationPoint(0.0F, 24.0F, 0.0F);
		

		body = new AdvancedModelBox(this);
		body.setRotationPoint(0.0F, -5.5F, 0.0F);
		root.addChild(body);
		body.setTextureOffset(0, 0).func_228303_a_(-2.5F, -2.5F, -6.0F, 5.0F, 5.0F, 12.0F, 0.0F, false);

		tail = new AdvancedModelBox(this);
		tail.setRotationPoint(0.0F, -0.5F, 6.0F);
		body.addChild(tail);
		tail.setTextureOffset(0, 18).func_228303_a_(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 10.0F, 0.0F, false);

		head = new AdvancedModelBox(this);
		head.setRotationPoint(-0.5F, -1.5F, -6.0F);
		body.addChild(head);
		head.setTextureOffset(15, 18).func_228303_a_(-1.5F, -2.0F, -4.0F, 4.0F, 4.0F, 4.0F, 0.0F, false);

		ear_left = new AdvancedModelBox(this);
		ear_left.setRotationPoint(2.5F, -1.0F, -1.0F);
		head.addChild(ear_left);
		ear_left.setTextureOffset(2, 9).func_228303_a_(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 0.0F, 0.0F, false);

		ear_right = new AdvancedModelBox(this);
		ear_right.setRotationPoint(-1.5F, -2.0F, -1.0F);
		head.addChild(ear_right);
		ear_right.setTextureOffset(2, 9).func_228303_a_(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F, 0.0F, true);

		upper_jaw = new AdvancedModelBox(this);
		upper_jaw.setRotationPoint(0.5F, 0.0F, -4.0F);
		head.addChild(upper_jaw);
		upper_jaw.setTextureOffset(23, 0).func_228303_a_(-1.5F, -1.0F, -3.0F, 3.0F, 2.0F, 3.0F, 0.0F, false);

		lower_jaw = new AdvancedModelBox(this);
		lower_jaw.setRotationPoint(0.5F, 1.0F, -4.0F);
		head.addChild(lower_jaw);
		lower_jaw.setTextureOffset(23, 6).func_228303_a_(-1.5F, 0.0F, -3.0F, 3.0F, 1.0F, 3.0F, 0.0F, false);

		arm_left = new AdvancedModelBox(this);
		arm_left.setRotationPoint(1.4F, 1.5F, -4.0F);
		body.addChild(arm_left);
		arm_left.setTextureOffset(0, 18).func_228303_a_(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);

		arm_right = new AdvancedModelBox(this);
		arm_right.setRotationPoint(-1.4F, 1.5F, -4.0F);
		body.addChild(arm_right);
		arm_right.setTextureOffset(0, 18).func_228303_a_(-1.0F, -1.0F, -1.0F, 2.0F, 5.0F, 2.0F, 0.0F, true);

		leg_left = new AdvancedModelBox(this);
		leg_left.setRotationPoint(1.4F, 2.5F, 4.5F);
		body.addChild(leg_left);
		leg_left.setTextureOffset(0, 0).func_228303_a_(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, 0.0F, false);

		leg_right = new AdvancedModelBox(this);
		leg_right.setRotationPoint(-1.4F, 2.5F, 4.5F);
		body.addChild(leg_right);
		leg_right.setTextureOffset(0, 0).func_228303_a_(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);
		this.updateDefaultPose();
		animator = ModelAnimator.create();
	}

	@Override
	public Iterable<AdvancedModelBox> getAllParts() {
		return ImmutableList.of(root, body, head, ear_right, ear_left, lower_jaw, upper_jaw, tail, arm_left, arm_right, leg_left, leg_right);
	}

	public void animate(IAnimatedEntity entity, float f, float f1, float f2, float f3, float f4) {
		this.resetToDefaultPose();
		animator.update(entity);
		animator.setAnimation(EntityTasmanianDevil.ANIMATION_ATTACK);
		animator.startKeyframe(3);
		animator.rotate(head, (float)Math.toRadians(10F), 0, 0);
		animator.move(lower_jaw, 0, 0, 0.5F);
		animator.rotate(lower_jaw, (float)Math.toRadians(15F), 0, 0);
		animator.move(head, 0, -1, 1.5F);
		animator.endKeyframe();
		animator.startKeyframe(2);
		animator.move(lower_jaw, 0, 0, 1F);
		animator.rotate(head, (float)Math.toRadians(-15F), 0, 0);
		animator.rotate(upper_jaw, (float)Math.toRadians(-5F), 0, 0);
		animator.rotate(lower_jaw, (float)Math.toRadians(65F), 0, 0);
		animator.endKeyframe();
		animator.resetKeyframe(3);
		animator.endKeyframe();
		animator.setAnimation(EntityTasmanianDevil.ANIMATION_HOWL);
		animator.startKeyframe(10);
		animator.move(lower_jaw, 0, 0, 1F);
		animator.rotate(upper_jaw, (float)Math.toRadians(-5F), 0, 0);
		animator.rotate(lower_jaw, (float)Math.toRadians(45F), 0, 0);
		animator.rotate(head, (float)Math.toRadians(-45F),  (float)Math.toRadians(45F), 0);
		animator.endKeyframe();
		animator.startKeyframe(20);
		animator.move(lower_jaw, 0, 0, 1F);
		animator.rotate(upper_jaw, (float)Math.toRadians(-5F), 0, 0);
		animator.rotate(lower_jaw, (float)Math.toRadians(45F), 0, 0);
		animator.rotate(head, (float)Math.toRadians(-45F),  (float)Math.toRadians(-45F), 0);
		animator.endKeyframe();
		animator.setStaticKeyframe(5);
		animator.resetKeyframe(5);
	}

	@Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, Entity entityIn) {
        setRotationAngles((EntityTasmanianDevil) entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    public void setRotationAngles(EntityTasmanianDevil entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		animate(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float walkSpeed = 1F;
		float walkDegree = 0.5F;
		float idleSpeed = 0.1F;
		float idleDegree = 0.1F;
		float stillProgress = 1F - limbSwingAmount;
		float partialTick = Minecraft.getMinecraft().getRenderPartialTicks();
		float baskProgress0 = entity.prevBaskProgress + (entity.baskProgress - entity.prevBaskProgress) * partialTick;
		float sitProgress = entity.prevSitProgress + (entity.sitProgress - entity.prevSitProgress) * partialTick;
		float baskProgress = Math.max(0, baskProgress0 - sitProgress);
		progressRotationPrev(tail, stillProgress, (float)Math.toRadians(-23), 0, 0, 1F);
		progressRotationPrev(body, sitProgress, (float)Math.toRadians(-25), 0, 0, 5F);
		progressRotationPrev(arm_right, sitProgress, (float)Math.toRadians(25), 0, 0, 5F);
		progressRotationPrev(arm_left, sitProgress, (float)Math.toRadians(25), 0, 0, 5F);
		progressRotationPrev(tail, sitProgress, (float)Math.toRadians(40), 0, 0, 5F);
		progressRotationPrev(head, sitProgress, (float)Math.toRadians(25), 0, 0, 5F);
		progressRotationPrev(leg_right, sitProgress, (float)Math.toRadians(-40),  (float)Math.toRadians(40), 0, 5F);
		progressRotationPrev(leg_left, sitProgress, (float)Math.toRadians(-40),  (float)Math.toRadians(-40), 0, 5F);
		progressPositionPrev(arm_right, sitProgress, 0, 1.5F, 1.2F, 5F);
		progressPositionPrev(arm_left, sitProgress, 0, 1.5F, 1.2F, 5F);
		progressPositionPrev(leg_left, sitProgress, 1, -1.5F, 0F, 5F);
		progressPositionPrev(leg_right, sitProgress, -1, -1.5F, 0F, 5F);
		progressRotationPrev(arm_right, baskProgress, (float)Math.toRadians(-80),  (float)Math.toRadians(40), 0, 5F);
		progressRotationPrev(arm_left, baskProgress, (float)Math.toRadians(-80),  (float)Math.toRadians(-40), 0, 5F);
		progressRotationPrev(leg_right, baskProgress, (float)Math.toRadians(80),  (float)Math.toRadians(-20), 0, 5F);
		progressRotationPrev(leg_left, baskProgress, (float)Math.toRadians(80),  (float)Math.toRadians(20), 0, 5F);
		progressRotationPrev(tail, baskProgress, (float)Math.toRadians(10), 0, 0, 5F);
		progressRotationPrev(head, baskProgress, (float)Math.toRadians(10), 0, 0, 5F);
		progressRotationPrev(ear_right, baskProgress, 0,  (float)Math.toRadians(40), 0, 5F);
		progressRotationPrev(ear_left, baskProgress, 0,  (float)Math.toRadians(-40), 0, 5F);
		progressPositionPrev(body, baskProgress, 0, 3F, 0F, 5F);
		progressPositionPrev(head, baskProgress, 0, 1.2F, 0F, 5F);
		progressPositionPrev(arm_right, baskProgress, 0, -0.2F, -1.2F, 5F);
		progressPositionPrev(arm_left, baskProgress, 0, -0.2F, -1.2F, 5F);
		progressPositionPrev(leg_left, baskProgress, 1, -1.5F, 0F, 5F);
		progressPositionPrev(leg_right, baskProgress, -1, -1.5F, 0F, 5F);

		this.walk(arm_right, walkSpeed, walkDegree * 1.1F, true, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(arm_right, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(arm_left, walkSpeed, walkDegree * 1.1F, false, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(arm_left, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(leg_right, walkSpeed, walkDegree * 1.1F, false, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(leg_right, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.walk(leg_left, walkSpeed, walkDegree * 1.1F, true, 0F, 0F, limbSwing, limbSwingAmount);
		this.bob(leg_left, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.swing(body, walkSpeed, walkDegree * 0.6F, true, 1F, 0F, limbSwing, limbSwingAmount);
		this.swing(tail, walkSpeed, walkDegree * 0.6F, false, 1F, 0F, limbSwing, limbSwingAmount);
		this.swing(head, walkSpeed, walkDegree * 0.6F, false, 1F, 0F, limbSwing, limbSwingAmount);
		this.walk(body, walkSpeed, walkDegree * 0.05F, true, 0F, 0F, limbSwing, limbSwingAmount);
		this.walk(tail, walkSpeed, walkDegree * 0.6F, true, -1F, 0F, limbSwing, limbSwingAmount);
		this.bob(body, walkSpeed, walkDegree, false, limbSwing, limbSwingAmount);
		this.bob(head, walkSpeed, walkDegree * 0.6F, false, limbSwing, limbSwingAmount);
		this.swing(ear_right, walkSpeed, walkDegree * 0.6F, false, -1F, 0.3F, limbSwing, limbSwingAmount);
		this.swing(ear_left, walkSpeed, walkDegree * 0.6F, true, -1F, 0.3F, limbSwing, limbSwingAmount);
		this.swing(tail, idleSpeed, idleDegree * 0.9F, false, 1F, 0F, ageInTicks, 1);
		this.faceTarget(netHeadYaw, headPitch, 1, head);

	}

	@Override
    public void render(net.minecraft.entity.Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (this.isChild) {
            this.head.setScale(1, 1, 1);
            this.head.setShouldScaleChildren(true);
            net.minecraft.client.renderer.GlStateManager.pushMatrix();
            net.minecraft.client.renderer.GlStateManager.scale(0.5F, 0.5F, 0.5F);
            net.minecraft.client.renderer.GlStateManager.translate(0.0F * scale, 1.5F * scale, 0.0F * scale);
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

	public void setRotationAngle(AdvancedModelBox advancedModelBox, float x, float y, float z) {
		advancedModelBox.rotateAngleX = x;
		advancedModelBox.rotateAngleY = y;
		advancedModelBox.rotateAngleZ = z;
	}
}