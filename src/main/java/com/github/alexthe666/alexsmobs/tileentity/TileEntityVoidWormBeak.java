package com.github.alexthe666.alexsmobs.tileentity;

import com.github.alexthe666.alexsmobs.block.BlockVoidWormBeak;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityVoidWormBeak extends TileEntity implements ITickable {

    private float chompProgress;
    private float prevChompProgress;
    public int ticksExisted;

    public TileEntityVoidWormBeak() {
    }

    @Override
    public void update() {
        prevChompProgress = chompProgress;
        boolean powered = false;
        if (this.world != null && this.pos != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            if (state.getBlock() instanceof BlockVoidWormBeak) {
                powered = state.getValue(BlockVoidWormBeak.POWERED);
            }
        }
        if (powered && chompProgress < 5F) {
            chompProgress++;
        }
        if (!powered && chompProgress > 0F) {
            chompProgress--;
        }
        if (chompProgress >= 5F && this.world != null && !this.world.isRemote && ticksExisted % 5 == 0) {
            float i = this.pos.getX() + 0.5F;
            float j = this.pos.getY() + 0.5F;
            float k = this.pos.getZ() + 0.5F;
            float d0 = 0.5F;
            for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB((double) i - d0, (double) j - d0, (double) k - d0, (double) i + d0, (double) j + d0, (double) k + d0))) {
                entity.attackEntityFrom(DamageSource.FALLING_BLOCK, 5);
            }
        }
        ticksExisted++;
    }

    public float getChompProgress(float partialTick) {
        return prevChompProgress + (chompProgress - prevChompProgress) * partialTick;
    }
}
