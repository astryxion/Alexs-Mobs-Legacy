package com.github.alexthe666.alexsmobs.client.particle;

import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemDimensionalCarver;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleInvertDig extends Particle {

    private final Entity creator;

    public ParticleInvertDig(World world, double x, double y, double z, double creatorId) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.INVERT_DIG_SPRITES, 0, ItemDimensionalCarver.MAX_TIME);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
        this.particleScale = 0.1F;
        this.setAlphaF(1.0F);
        this.particleMaxAge = ItemDimensionalCarver.MAX_TIME;
        this.creator = world.getEntityByID((int) creatorId);
    }

    @Override
    public int getBrightnessForRender(float partialTicks) {
        return 240;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        boolean live = false;
        this.particleScale = 0.1F + Math.min((this.particleAge / (float) this.particleMaxAge), 0.5F) * 0.5F;
        if (this.particleAge++ >= this.particleMaxAge || this.creator == null) {
            this.setExpired();
        } else if (this.creator instanceof EntityPlayer) {
            ItemStack item = ((EntityPlayer) this.creator).getActiveItemStack();
            if (item.getItem() == AMItemRegistry.DIMENSIONAL_CARVER) {
                this.particleAge = MathHelper.clamp(this.particleMaxAge - ((EntityPlayer) this.creator).getItemInUseCount(), 0, this.particleMaxAge);
                live = true;
            }
        }
        if (!live) {
            this.setExpired();
        }
        TextureAtlasSprite sprite = AMParticleRegistry.spriteByAge(AMParticleRegistry.INVERT_DIG_SPRITES, this.particleAge, this.particleMaxAge);
        if (sprite != null) {
            this.particleTexture = sprite;
        }
    }
}
