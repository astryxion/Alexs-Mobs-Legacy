package com.github.alexthe666.alexsmobs.tileentity;

import com.github.alexthe666.alexsmobs.block.BlockSkunkSpray;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntitySkunkSpray extends TileEntity {

    private int faceMask;
    private int age;
    private boolean waterlogged;

    public boolean hasFace(EnumFacing face) {
        return (faceMask & (1 << face.getIndex())) != 0;
    }

    public void addFace(EnumFacing face) {
        faceMask |= 1 << face.getIndex();
        markDirty();
    }

    public void setFace(EnumFacing face, boolean value) {
        if (value) {
            faceMask |= 1 << face.getIndex();
        } else {
            faceMask &= ~(1 << face.getIndex());
        }
        markDirty();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        markDirty();
    }

    public boolean isWaterlogged() {
        return waterlogged;
    }

    public void setWaterlogged(boolean waterlogged) {
        this.waterlogged = waterlogged;
        markDirty();
    }

    public IBlockState toBlockState(IBlockState base) {
        IBlockState state = base;
        for (EnumFacing facing : EnumFacing.values()) {
            state = state.withProperty(BlockSkunkSpray.getFaceProperty(facing), hasFace(facing));
        }
        return state.withProperty(BlockSkunkSpray.AGE, age).withProperty(BlockSkunkSpray.WATERLOGGED, waterlogged);
    }

    public void readFromBlockState(IBlockState state) {
        faceMask = 0;
        for (EnumFacing facing : EnumFacing.values()) {
            if (BlockSkunkSpray.hasFace(state, facing)) {
                addFace(facing);
            }
        }
        age = state.getValue(BlockSkunkSpray.AGE);
        waterlogged = state.getValue(BlockSkunkSpray.WATERLOGGED);
        markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("FaceMask", faceMask);
        compound.setInteger("Age", age);
        compound.setBoolean("Waterlogged", waterlogged);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        faceMask = compound.getInteger("FaceMask");
        age = compound.getInteger("Age");
        waterlogged = compound.getBoolean("Waterlogged");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }
}
