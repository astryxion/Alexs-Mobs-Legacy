package com.github.alexthe668.alexsmobs.entity.util;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nullable;

/**
 * Citadel's living-entity mixin is not applied on 1.12.2, so rainbow jelly cannot use {@code CitadelEntityData}.
 */
public class RainbowCapability {

    @CapabilityInject(RainbowData.class)
    public static Capability<RainbowData> CAPABILITY = null;

    public static final ResourceLocation ID = new ResourceLocation(AlexsMobs.MODID, "rainbow");

    public static void register() {
        CapabilityManager.INSTANCE.register(RainbowData.class, new Capability.IStorage<RainbowData>() {
            @Override
            public NBTBase writeNBT(Capability<RainbowData> capability, RainbowData instance, EnumFacing side) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("RainbowType", instance.type);
                return tag;
            }

            @Override
            public void readNBT(Capability<RainbowData> capability, RainbowData instance, EnumFacing side, NBTBase nbt) {
                if (nbt instanceof NBTTagCompound) {
                    instance.type = ((NBTTagCompound) nbt).getInteger("RainbowType");
                }
            }
        }, RainbowData::new);
    }

    @Nullable
    public static RainbowData get(EntityLivingBase entity) {
        if (CAPABILITY == null) {
            return null;
        }
        return entity.getCapability(CAPABILITY, null);
    }

    public static class RainbowData {
        private int type;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public static class Provider implements ICapabilitySerializable<NBTTagCompound> {
        private final RainbowData data = new RainbowData();

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY;
        }

        @Override
        @Nullable
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAPABILITY ? CAPABILITY.cast(this.data) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return (NBTTagCompound) CAPABILITY.getStorage().writeNBT(CAPABILITY, this.data, null);
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            CAPABILITY.getStorage().readNBT(CAPABILITY, this.data, null, nbt);
        }
    }

    public static void attach(net.minecraftforge.event.AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(ID, new Provider());
        }
    }
}
