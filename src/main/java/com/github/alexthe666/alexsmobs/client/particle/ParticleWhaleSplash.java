package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.ParticleRain;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleWhaleSplash extends ParticleRain {

    public ParticleWhaleSplash(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        super(world, x, y, z);
        TextureAtlasSprite sprite = AMParticleRegistry.randomSprite(AMParticleRegistry.SPLASH_0_3, this.rand);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleGravity = 0.04F;
        this.motionY = 1.0D;
        this.particleMaxAge = (int) (16.0D / (Math.random() * 0.4D + 0.1D));
        this.particleScale = 0.2F * (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (this.motionY < 0.0D) {
            if (Math.abs(this.motionX) < 0.23D) {
                this.motionX *= 1.2D;
            }
            if (Math.abs(this.motionZ) < 0.23D) {
                this.motionZ *= 1.2D;
            }
        }
    }
}
