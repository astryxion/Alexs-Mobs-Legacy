package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleTeethGlint extends Particle {

    private final float initScale;

    public ParticleTeethGlint(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        if (AMParticleRegistry.TEETH_GLINT_SPRITE != null) {
            this.particleTexture = AMParticleRegistry.TEETH_GLINT_SPRITE;
        }
        float shade = this.rand.nextFloat() * 0.1F + 0.2F;
        this.setRBGColorF(shade, shade, shade);
        this.setSize(0.02F, 0.02F);
        this.particleScale = initScale = 0.1F * (this.rand.nextFloat() * 0.3F + 0.5F);
        this.motionX *= 0.019999999552965164D;
        this.motionY *= 0.019999999552965164D;
        this.motionZ *= 0.019999999552965164D;
        this.particleMaxAge = 3 + this.rand.nextInt(5);
        this.setAlphaF(1.0F - (this.particleAge / (float) this.particleMaxAge) * 0.5F);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        int light = super.getBrightnessForRender(partialTicks);
        int sky = light >> 16 & 255;
        return 240 | sky << 16;
    }

    @Override
    public void onUpdate() {
        this.prevParticleAngle = this.particleAngle;
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleMaxAge-- <= 0) {
            this.setExpired();
        } else {
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.99D;
            this.motionY *= 0.99D;
            this.motionZ *= 0.99D;
        }
        this.particleAngle += 0.25F * Math.sin(this.particleAge * 2);
        this.particleScale = initScale * (1.0F - (this.particleAge / (float) this.particleMaxAge) * 0.5F);
    }
}
