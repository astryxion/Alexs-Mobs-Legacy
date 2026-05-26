package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleDna extends Particle {

    public ParticleDna(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.DNA_SPRITES, 0, 15);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleScale *= 1.5F + this.rand.nextFloat() * 0.6F;
        this.particleMaxAge = 15 + this.rand.nextInt(15);
        this.particleGravity = 0.1F;
        int color = 15916745;
        this.setRBGColorF((float) (color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F);
        this.setAlphaF(0.0F);
        this.particleAngle = (float) (Math.PI * 0.5F * this.rand.nextFloat());
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
            this.motionX += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            this.motionY += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            this.motionZ += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            float subAlpha = 1.0F;
            if (this.particleAge > 5) {
                subAlpha = 1.0F - (float) (this.particleAge - 5) / (float) (this.particleMaxAge - 5);
            }
            this.setAlphaF(subAlpha);
            TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.DNA_SPRITES, this.particleAge, this.particleMaxAge);
            if (sprite != null) {
                this.particleTexture = sprite;
            }
            this.particleAngle += (float) Math.random() * ((float) Math.PI * 0.3F * subAlpha);
        }
    }
}
