package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleBirdSong extends Particle {

    public ParticleBirdSong(World world, double x, double y, double z, double motionX, double motionY, double motionZ, TextureAtlasSprite sprite) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.particleTexture = sprite;
        this.particleGravity = 0.0F;
        this.particleScale = 0.15F + this.rand.nextFloat() * 0.2F;
        this.particleMaxAge = 20 + this.rand.nextInt(20);
        this.particleRed = 0.294F;
        this.particleGreen = 0.584F;
        this.particleBlue = 1.0F;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY = Math.sin(this.particleAge * 0.3F) * 0.3F;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            this.move(this.motionX, this.motionY, this.motionZ);
        }
        float subAlpha = 1F;
        if (this.particleAge > 5) {
            subAlpha = 1 - (float) (this.particleAge - 5) / (float) (this.particleMaxAge - 5);
        }
        this.motionX *= 0.99D;
        this.motionY *= 0.99D;
        this.motionZ *= 0.99D;
        this.particleAlpha = subAlpha;
        this.particleAngle += (float) Math.toRadians(Math.sin(this.particleAge * 0.01F) * 5);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        int i = super.getBrightnessForRender(partialTick);
        int j = i >> 16 & 255;
        return 240 | j << 16;
    }
}
