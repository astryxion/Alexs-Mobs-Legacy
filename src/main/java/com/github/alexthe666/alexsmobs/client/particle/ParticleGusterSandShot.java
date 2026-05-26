package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleGusterSandShot extends Particle {

    public ParticleGusterSandShot(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int variant) {
        super(world, x, y, z, motionX, motionY, motionZ);
        int color = ParticleGusterSandSpin.selectColor(variant, this.rand);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        this.setRBGColorF(r, g, b);
        TextureAtlasSprite sprite = AMParticleRegistry.randomSprite(AMParticleRegistry.GENERIC_0_7, this.rand);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleScale *= 0.6F + this.rand.nextFloat() * 1.4F;
        this.particleMaxAge = 10 + this.rand.nextInt(15);
        this.particleGravity = 0.5F;
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
            this.motionY -= 0.004D + 0.04D * (double) this.particleGravity;
        }
    }
}
