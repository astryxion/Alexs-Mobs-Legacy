package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticlePlatypus extends Particle {

    public ParticlePlatypus(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.PLATYPUS_SENSE_SPRITES, 0, 5);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleScale *= 0.2F + this.rand.nextFloat() * 0.6F;
        this.particleMaxAge = 3 + this.rand.nextInt(2);
        this.particleGravity = 0.0F;
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        int light = super.getBrightnessForRender(partialTicks);
        int sky = light >> 16 & 255;
        return 240 | sky << 16;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.8D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.8D;
            double d0 = this.posX - this.prevPosX;
            double d2 = this.posZ - this.prevPosZ;
            this.particleAngle = (float) (MathHelper.atan2(d2, d0) * 57.2957763671875D) - 180.0F;
            this.prevParticleAngle = this.particleAngle;
            TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.PLATYPUS_SENSE_SPRITES, this.particleAge, this.particleMaxAge);
            if (sprite != null) {
                this.particleTexture = sprite;
            }
        }
    }
}
