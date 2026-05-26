package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWormPortal extends Particle {

    public ParticleWormPortal(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        TextureAtlasSprite sprite = AMParticleRegistry.WORM_PORTAL_SPRITE;
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleScale = 0.35F;
        this.particleMaxAge = 10 + this.rand.nextInt(12);
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
            this.prevParticleAngle = this.particleAngle;
            this.motionX *= 0.8D;
            this.motionY *= 0.8D;
            this.motionZ *= 0.8D;
            this.particleAngle += 0.25F;
            TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(new TextureAtlasSprite[]{AMParticleRegistry.WORM_PORTAL_SPRITE}, this.particleAge, this.particleMaxAge);
            if (sprite != null) {
                this.particleTexture = sprite;
            }
            this.particleScale = 0.35F * (1.0F - (this.particleAge / (float) this.particleMaxAge));
            this.setAlphaF(1.0F - (this.particleAge / (float) this.particleMaxAge));
        }
    }
}
