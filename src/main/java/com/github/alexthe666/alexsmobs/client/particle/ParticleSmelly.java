package com.github.alexthe666.alexsmobs.client.particle;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.12 port of 1.20 {@link com.github.alexthe666.alexsmobs.client.particle.ParticleSmelly}.
 */
@SideOnly(Side.CLIENT)
public class ParticleSmelly extends Particle {

    private final TextureAtlasSprite[] sprites;

    public ParticleSmelly(World world, double x, double y, double z, double motionX, double motionY, double motionZ, TextureAtlasSprite[] sprites) {
        super(world, x, y, z, motionX, motionY, motionZ);
        this.sprites = sprites;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.particleScale *= 0.7F + this.rand.nextFloat() * 0.6F;
        this.particleMaxAge = 15 + this.rand.nextInt(15);
        this.particleGravity = -0.1F;
        this.setSpriteFromAge();
    }

    private void setSpriteFromAge() {
        if (sprites != null && sprites.length > 0) {
            this.particleTexture = AMParticleRegistry.spriteByAge(sprites, this.particleAge, this.particleMaxAge);
        }
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            this.motionX += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            this.motionY += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            this.motionZ += (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.setSpriteFromAge();
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements net.minecraft.client.particle.IParticleFactory {
        @Override
        public Particle createParticle(int particleID, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... parameters) {
            return new ParticleSmelly(world, x, y, z, xSpeed, ySpeed, zSpeed, AMParticleRegistry.SMELLY_SPRITES);
        }
    }
}
