package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleHemolymph extends Particle {

    private static final int[] POSSIBLE_COLORS = {0X70FFF8, 0X3BFFD0, 0X08DED9};

    public ParticleHemolymph(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z, motionX, motionY, motionZ);
        if (AMParticleRegistry.HEMOLYMPH_SPRITE != null) {
            this.particleTexture = AMParticleRegistry.HEMOLYMPH_SPRITE;
        }
        int color = POSSIBLE_COLORS[this.rand.nextInt(POSSIBLE_COLORS.length)];
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        this.setRBGColorF(r, g, b);
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
