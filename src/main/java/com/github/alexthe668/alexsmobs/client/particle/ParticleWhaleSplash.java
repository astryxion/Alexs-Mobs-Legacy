package com.github.alexthe668.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.20 uses vanilla splash_0..3 on the particle sheet. 1.12 stores those on particles.png at indices 19-22.
 * Do not extend ParticleRain: Forge already assigns a texture, and ParticleRain then calls setParticleTextureIndex, which crashes.
 */
@SideOnly(Side.CLIENT)
public class ParticleWhaleSplash extends Particle {

    public ParticleWhaleSplash(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.particleTexture = null;
        this.setParticleTextureIndex(19 + this.rand.nextInt(4));
        this.particleGravity = 0.04F;
        this.motionX = motionX;
        this.motionY = 1.0D;
        this.motionZ = motionZ;
        this.particleMaxAge = (int) (16.0D / (Math.random() * 0.4D + 0.1D));
        this.particleScale = 0.2F * (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    @Override
    public int getFXLayer() {
        return 0;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= (double) this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else if (this.motionY < 0.0D) {
            if (Math.abs(this.motionX) < 0.23D) {
                this.motionX *= 1.2D;
            }
            if (Math.abs(this.motionZ) < 0.23D) {
                this.motionZ *= 1.2D;
            }
        }
    }
}
