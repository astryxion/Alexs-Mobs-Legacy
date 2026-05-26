package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSimpleHeart extends Particle {

    public ParticleSimpleHeart(World world, double x, double y, double z) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        if (AMParticleRegistry.SHOCKED_SPRITE != null) {
            this.particleTexture = AMParticleRegistry.SHOCKED_SPRITE;
        }
        this.motionX *= 0.01D;
        this.motionY *= 0.01D;
        this.motionZ *= 0.01D;
        this.motionY += 0.1D;
        this.particleScale *= 2.0F;
        this.particleMaxAge = 32;
    }

    public float getScale(float scaleFactor) {
        return this.particleScale * MathHelper.clamp(((float) this.particleAge + scaleFactor) / (float) this.particleMaxAge * 16.0F, 0.0F, 1.0F);
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
            if (this.posY == this.prevPosY) {
                this.motionX *= 1.1D;
                this.motionZ *= 1.1D;
            }
            this.motionX *= 0.86D;
            this.motionY *= 0.86D;
            this.motionZ *= 0.86D;
            if (this.onGround) {
                this.motionX *= 0.7D;
                this.motionZ *= 0.7D;
            }
        }
    }
}
