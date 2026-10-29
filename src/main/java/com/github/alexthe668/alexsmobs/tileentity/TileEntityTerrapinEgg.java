package com.github.alexthe668.alexsmobs.tileentity;

import com.github.alexthe668.alexsmobs.entity.EntityTerrapin;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.Random;

/**
 * Stores the two parent terrapins so hatchlings inherit shell, skin, and color.
 */
public class TileEntityTerrapinEgg extends TileEntity {

    public EntityTerrapin.ParentData parent1;
    public EntityTerrapin.ParentData parent2;

    public void addAttributesToOffspring(EntityTerrapin baby, Random random) {
        if (this.parent1 != null && this.parent2 != null) {
            EntityTerrapin.ParentData.addAttributesToOffspring(baby, this.parent1, this.parent2, random);
        }
    }

    public void readParents(NBTTagCompound compound) {
        if (compound.hasKey("Parent1Data", 10)) {
            this.parent1 = EntityTerrapin.ParentData.readFromNBT(compound.getCompoundTag("Parent1Data"));
        }
        if (compound.hasKey("Parent2Data", 10)) {
            this.parent2 = EntityTerrapin.ParentData.readFromNBT(compound.getCompoundTag("Parent2Data"));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.readParents(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.parent1 != null) {
            NBTTagCompound tag = new NBTTagCompound();
            this.parent1.writeToNBT(tag);
            compound.setTag("Parent1Data", tag);
        }
        if (this.parent2 != null) {
            NBTTagCompound tag = new NBTTagCompound();
            this.parent2.writeToNBT(tag);
            compound.setTag("Parent2Data", tag);
        }
        return compound;
    }
}
