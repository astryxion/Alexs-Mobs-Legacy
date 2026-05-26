package com.github.alexthe666.alexsmobs.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@SideOnly(Side.CLIENT)
public class ParticleGusterSandSpin extends Particle {

    private static final int[] POSSIBLE_COLORS = {0XF3C389, 0XEFB578, 0XF8D49A, 0XFFE6AD, 0XFFFFCD};
    private static final int[] POSSIBLE_COLORS_RED = {0XD57136, 0XC66127, 0XBC5A21, 0XB2541B, 0XA74B11};
    private static final int[] POSSIBLE_COLORS_SOUL = {0X534238, 0X4E3D33, 0X3D2E25, 0X49372C, 0X2B201B};

    private final float targetX;
    private final float targetY;
    private final float targetZ;

    public ParticleGusterSandSpin(World world, double x, double y, double z, double motionX, double motionY, double motionZ, int variant) {
        super(world, x, y, z, 0, 0, 0);
        int color = selectColor(variant, this.rand);
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        this.setRBGColorF(r, g, b);
        TextureAtlasSprite sprite = AMParticleRegistry.randomSprite(AMParticleRegistry.GENERIC_0_7, this.rand);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.targetX = (float) motionX;
        this.targetY = (float) motionY;
        this.targetZ = (float) motionZ;
        this.particleScale *= 0.4F + this.rand.nextFloat() * 1.4F;
        this.particleMaxAge = 15 + this.rand.nextInt(15);
    }

    public static int selectColor(int variant, Random rand) {
        if (variant == 2) {
            return POSSIBLE_COLORS_SOUL[rand.nextInt(POSSIBLE_COLORS_SOUL.length)];
        } else if (variant == 1) {
            return POSSIBLE_COLORS_RED[rand.nextInt(POSSIBLE_COLORS_RED.length)];
        }
        return POSSIBLE_COLORS[rand.nextInt(POSSIBLE_COLORS.length)];
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            float radius = 2.0F;
            float angle = this.particleAge * 2.0F;
            double extraX = this.targetX + (double) radius * MathHelper.sin((float) (Math.PI + angle));
            double extraZ = this.targetZ + (double) radius * MathHelper.cos(angle);
            double d2 = extraX - this.posX;
            double d3 = this.targetY - this.posY;
            double d4 = extraZ - this.posZ;
            float speed = 0.02F;
            this.motionX += d2 * (double) speed;
            this.motionY += d3 * (double) speed;
            this.motionZ += d4 * (double) speed;
            this.move(this.motionX, this.motionY, this.motionZ);
        }
    }
}
