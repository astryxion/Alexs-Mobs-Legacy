package com.github.alexthe666.alexsmobs.entity.util;

import com.github.alexthe666.alexsmobs.entity.EntitySquidGrapple;
import com.github.alexthe666.citadel.server.entity.CitadelEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.UUID;

public class SquidGrappleUtil {

    private static final String HOOK_1 = "SquidGrappleHook1AlexsMobs";
    private static final String HOOK_2 = "SquidGrappleHook2AlexsMobs";
    private static final String HOOK_3 = "SquidGrappleHook3AlexsMobs";
    private static final String HOOK_4 = "SquidGrappleHook4AlexsMobs";
    private static final String LAST_REPLACED_HOOK = "LastSquidGrappleHookAlexsMobs";

    public static int onFireHook(EntityLivingBase entity, UUID newHookUUID) {
        NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        int index = getFirstAvailableHookIndex(entity);
        String indexStr = getHookStrFromIndex(index);
        if (tag.hasKey(indexStr)) {
            EntitySquidGrapple hook = getHookEntity(entity.world, tag.getUniqueId(indexStr));
            if (hook != null && hook.isEntityAlive()) {
                hook.setWithdrawing(true);
            }
        }
        tag.setUniqueId(indexStr, newHookUUID);
        CitadelEntityData.setCitadelTag(entity, tag);
        return index;
    }

    public static int getFirstAvailableHookIndex(EntityLivingBase entity) {
        int nulls = getAnyNullHooks(entity);
        if (nulls != -1) {
            return nulls;
        }
        int i = getHookCount(entity);
        if (i < 4) {
            return i;
        } else {
            NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
            int j = tag.getInteger(LAST_REPLACED_HOOK);
            tag.setInteger(LAST_REPLACED_HOOK, (j + 1) % 4);
            CitadelEntityData.setCitadelTag(entity, tag);
            return j;
        }
    }

    public static String getHookStrFromIndex(int i) {
        switch (i) {
            case 0:
                return HOOK_1;
            case 1:
                return HOOK_2;
            case 2:
                return HOOK_3;
            case 3:
                return HOOK_4;
        }
        return HOOK_1;
    }

    public static int getAnyNullHooks(EntityLivingBase entity) {
        NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        if (!tag.hasKey(HOOK_1) || getHookEntity(entity.world, tag.getUniqueId(HOOK_1)) == null) {
            return 0;
        }
        if (!tag.hasKey(HOOK_2) || getHookEntity(entity.world, tag.getUniqueId(HOOK_2)) == null) {
            return 1;
        }
        if (!tag.hasKey(HOOK_3) || getHookEntity(entity.world, tag.getUniqueId(HOOK_3)) == null) {
            return 2;
        }
        if (!tag.hasKey(HOOK_4) || getHookEntity(entity.world, tag.getUniqueId(HOOK_4)) == null) {
            return 3;
        }
        return -1;
    }

    public static int getHookCount(EntityLivingBase entity) {
        NBTTagCompound tag = CitadelEntityData.getOrCreateCitadelTag(entity);
        int count = 0;
        if (tag.hasKey(HOOK_1) && getHookEntity(entity.world, tag.getUniqueId(HOOK_1)) != null) {
            count++;
        }
        if (tag.hasKey(HOOK_2) && getHookEntity(entity.world, tag.getUniqueId(HOOK_2)) != null) {
            count++;
        }
        if (tag.hasKey(HOOK_3) && getHookEntity(entity.world, tag.getUniqueId(HOOK_3)) != null) {
            count++;
        }
        if (tag.hasKey(HOOK_4) && getHookEntity(entity.world, tag.getUniqueId(HOOK_4)) != null) {
            count++;
        }
        return count;
    }

    public static EntitySquidGrapple getHookEntity(World level, UUID id) {
        if (id != null && !level.isRemote && level instanceof WorldServer) {
            Entity e = ((WorldServer) level).getEntityFromUuid(id);
            return e instanceof EntitySquidGrapple ? (EntitySquidGrapple) e : null;
        }
        return null;
    }
}
