package com.github.alexthe666.alexsmobs.event;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.effect.AMEffectRegistry;
import com.github.alexthe666.alexsmobs.effect.EffectClinging;
import com.github.alexthe666.alexsmobs.entity.*;
import com.github.alexthe666.alexsmobs.entity.util.VineLassoUtil;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.item.ItemFalconryGlove;
import com.github.alexthe666.alexsmobs.item.ItemTarantulaHawkElytra;
import com.github.alexthe666.alexsmobs.message.MessageSwingArm;
import com.github.alexthe666.alexsmobs.misc.AMAdvancementTriggerRegistry;
import com.github.alexthe666.alexsmobs.world.AMWorldData;
import com.github.alexthe666.alexsmobs.world.BeachedCachalotWhaleSpawner;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EntitySelectors;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.items.ItemHandlerHelper;

import java.lang.reflect.Field;
import java.util.*;

@Mod.EventBusSubscriber(modid = AlexsMobs.MODID)
public class ServerEvents {

    /**
     * 1.12 {@link EntityLiving#getLootTable()} always returns null; death drops require {@code deathLootTable}
     * to be set. Entities that override {@code getLootTable()} for stateful drops are left alone.
     */
    private static final Field DEATH_LOOT_TABLE_FIELD = ReflectionHelper.findField(
            EntityLiving.class, "deathLootTable", "field_184659_bA");

    private static final UUID SAND_SPEED_MODIFIER = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF28E");
    private static final UUID SNEAK_SPEED_MODIFIER = UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF28F");
    private static final AttributeModifier SAND_SPEED_BONUS = new AttributeModifier(SAND_SPEED_MODIFIER, "roadrunner speed bonus", 0.1D, 0);
    private static final AttributeModifier SNEAK_SPEED_BONUS = new AttributeModifier(SNEAK_SPEED_MODIFIER, "frontier cap speed bonus", 0.1D, 0);
    private static final Map<WorldServer, BeachedCachalotWhaleSpawner> BEACHED_CACHALOT_WHALE_SPAWNER_MAP = new HashMap<>();
    public static final List<PendingDimensionTeleport> teleportPlayers = new ArrayList<>();

    public static final class PendingDimensionTeleport {
        public final EntityPlayerMP player;
        public final int dimensionId;
        public final BlockPos destination;

        public PendingDimensionTeleport(EntityPlayerMP player, int dimensionId, BlockPos destination) {
            this.player = player;
            this.dimensionId = dimensionId;
            this.destination = destination;
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.WorldTickEvent tick) {
        if (!tick.world.isRemote && tick.world instanceof WorldServer) {
            WorldServer serverWorld = (WorldServer) tick.world;
            if (BEACHED_CACHALOT_WHALE_SPAWNER_MAP.get(serverWorld) == null) {
                BEACHED_CACHALOT_WHALE_SPAWNER_MAP.put(serverWorld, new BeachedCachalotWhaleSpawner(serverWorld));
            }
            BeachedCachalotWhaleSpawner spawner = BEACHED_CACHALOT_WHALE_SPAWNER_MAP.get(serverWorld);
            spawner.tick();
            if (serverWorld.provider.getDimension() == 0) {
                AMWorldData worldData = AMWorldData.get(serverWorld);
                if (worldData != null) {
                    worldData.tick();
                }
            }
        }
        if (!tick.world.isRemote && tick.world instanceof WorldServer && tick.phase == TickEvent.Phase.END) {
            WorldServer serverWorld = (WorldServer) tick.world;
            for (PendingDimensionTeleport trip : teleportPlayers) {
                if (trip.player.mcServer != null) {
                    trip.player.mcServer.getPlayerList().transferPlayerToDimension(trip.player, trip.dimensionId, new EntityVoidPortal.BlockPosTeleporter(trip.destination));
                }
            }
            teleportPlayers.clear();
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof WorldServer) {
            BEACHED_CACHALOT_WHALE_SPAWNER_MAP.remove(event.getWorld());
        }
    }

    protected static RayTraceResult rayTrace(World worldIn, EntityPlayer player, boolean stopOnLiquid) {
        float f = player.rotationPitch;
        float f1 = player.rotationYaw;
        Vec3d vector3d = player.getPositionEyes(1.0F);
        float f2 = MathHelper.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f3 = MathHelper.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
        float f4 = -MathHelper.cos(-f * ((float) Math.PI / 180F));
        float f5 = MathHelper.sin(-f * ((float) Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
        Vec3d vector3d1 = vector3d.addVector(f6 * d0, f5 * d0, f7 * d0);
        return worldIn.rayTraceBlocks(vector3d, vector3d1, stopOnLiquid, false, false);
    }

    @SubscribeEvent
    public static void onItemUseLast(LivingEntityUseItemEvent.Finish event) {
        if (event.getItem().getItem() == Items.CHORUS_FRUIT && new Random().nextInt(3) == 0 && event.getEntityLiving().isPotionActive(AMEffectRegistry.ENDER_FLU)) {
            event.getEntityLiving().removePotionEffect(AMEffectRegistry.ENDER_FLU);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (AMConfig.giveBookOnStartup) {
            NBTTagCompound playerData = event.player.getEntityData();
            NBTTagCompound data = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            if (data != null && !data.getBoolean("alexsmobs_has_book")) {
                ItemHandlerHelper.giveItemToPlayer(event.player, new ItemStack(AMItemRegistry.ANIMAL_DICTIONARY));
                data.setBoolean("alexsmobs_has_book", true);
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, data);
            }
        }
    }

    @SubscribeEvent
    public void onProjectileHit(ProjectileImpactEvent event) {
        RayTraceResult result = event.getRayTraceResult();
        if (result != null && result.entityHit instanceof EntityEmu && !event.getEntity().world.isRemote) {
            EntityEmu emu = (EntityEmu) result.entityHit;
            if (event.getEntity() instanceof EntityArrow) {
                ((EntityArrow) event.getEntity()).setIsCritical(false);
            }
            if ((emu.getAnimation() == EntityEmu.ANIMATION_DODGE_RIGHT || emu.getAnimation() == EntityEmu.ANIMATION_DODGE_LEFT) && emu.getAnimationTick() < 7) {
                event.setCanceled(true);
            }
            if (emu.getAnimation() != EntityEmu.ANIMATION_DODGE_RIGHT && emu.getAnimation() != EntityEmu.ANIMATION_DODGE_LEFT) {
                boolean left = true;
                Vec3d arrowPos = new Vec3d(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
                Vec3d look = emu.getLookVec();
                Vec3d rightVector = rotateYawVec(look, 0.5F * (float) Math.PI).addVector(emu.posX, emu.posY, emu.posZ);
                Vec3d leftVector = rotateYawVec(look, -0.5F * (float) Math.PI).addVector(emu.posX, emu.posY, emu.posZ);
                if (arrowPos.squareDistanceTo(rightVector) < arrowPos.squareDistanceTo(leftVector)) {
                    left = false;
                } else if (arrowPos.squareDistanceTo(rightVector) > arrowPos.squareDistanceTo(leftVector)) {
                    left = true;
                } else {
                    left = emu.getRNG().nextBoolean();
                }
                Vec3d vector3d2 = rotateYawVec(new Vec3d(event.getEntity().motionX, event.getEntity().motionY, event.getEntity().motionZ), (left ? -0.5F : 0.5F) * (float) Math.PI).normalize();
                emu.setAnimation(left ? EntityEmu.ANIMATION_DODGE_LEFT : EntityEmu.ANIMATION_DODGE_RIGHT);
                emu.isAirBorne = true;
                if (!emu.collidedHorizontally) {
                    emu.move(net.minecraft.entity.MoverType.SELF, vector3d2.x * 0.25F, 0.1F, vector3d2.z * 0.25F);
                }
                if (!event.getEntity().world.isRemote) {
                    EntityPlayerMP shooterMp = null;
                    if (event.getEntity() instanceof EntityArrow) {
                        Entity thrower = ((EntityArrow) event.getEntity()).shootingEntity;
                        if (thrower instanceof EntityPlayerMP) {
                            shooterMp = (EntityPlayerMP) thrower;
                        }
                    }
                    if (event.getEntity() instanceof EntityThrowable) {
                        Entity thrower = ((EntityThrowable) event.getEntity()).getThrower();
                        if (thrower instanceof EntityPlayerMP) {
                            shooterMp = (EntityPlayerMP) thrower;
                        }
                    }
                    if (shooterMp != null) {
                        AMAdvancementTriggerRegistry.EMU_DODGE.trigger(shooterMp);
                    }
                }
                emu.motionX += vector3d2.x * 0.5F;
                emu.motionY += 0.32F;
                emu.motionZ += vector3d2.z * 0.5F;
                event.setCanceled(true);
            }
        }
    }

    private static Vec3d rotateYawVec(Vec3d vec, float yaw) {
        double cos = MathHelper.cos(yaw);
        double sin = MathHelper.sin(yaw);
        return new Vec3d(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    @SubscribeEvent
    public void onEntityDespawnAttempt(LivingSpawnEvent.AllowDespawn event) {
        if (event.getEntityLiving().isPotionActive(AMEffectRegistry.DEBILITATING_STING)
                && event.getEntityLiving().getActivePotionEffect(AMEffectRegistry.DEBILITATING_STING) != null
                && event.getEntityLiving().getActivePotionEffect(AMEffectRegistry.DEBILITATING_STING).getAmplifier() > 0) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onLootLevelEvent(LootingLevelEvent event) {
        DamageSource src = event.getDamageSource();
        if (src != null) {
            Entity dmgSrc = src.getTrueSource();
            if (dmgSrc instanceof EntitySnowLeopard) {
                event.setLootingLevel(event.getLootingLevel() + 2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        ItemFalconryGlove.onLeftClick(event.getEntityPlayer(), event.getEntityPlayer().getHeldItemOffhand());
        ItemFalconryGlove.onLeftClick(event.getEntityPlayer(), event.getEntityPlayer().getHeldItemMainhand());
        if (event.getWorld().isRemote) {
            AlexsMobs.sendMSGToServer(new MessageSwingArm());
        }
    }

    @SubscribeEvent
    public void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getItem() == Items.WHEAT && event.getEntityPlayer().getRidingEntity() instanceof EntityElephant) {
            if (((EntityElephant) event.getEntityPlayer().getRidingEntity()).triggerCharge(event.getItemStack())) {
                event.getEntityPlayer().swingArm(event.getHand());
                if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                    event.getItemStack().shrink(1);
                }
            }
        }
        if (event.getItemStack().getItem() == Items.GLASS_BOTTLE && AMConfig.lavaBottleEnabled) {
            RayTraceResult raytraceresult = rayTrace(event.getWorld(), event.getEntityPlayer(), true);
            if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
                BlockPos blockpos = raytraceresult.getBlockPos();
                if (event.getWorld().isBlockModifiable(event.getEntityPlayer(), blockpos)) {
                    if (event.getWorld().getBlockState(blockpos).getMaterial() == Material.LAVA) {
                        event.getWorld().playSound(null, event.getEntityPlayer().posX, event.getEntityPlayer().posY, event.getEntityPlayer().posZ, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                        event.getEntityPlayer().addStat(StatList.getObjectUseStats(Items.GLASS_BOTTLE));
                        event.getEntityPlayer().setFire(6);
                        if (!event.getEntityPlayer().inventory.addItemStackToInventory(new ItemStack(AMItemRegistry.LAVA_BOTTLE))) {
                            event.getEntityPlayer().dropItem(new ItemStack(AMItemRegistry.LAVA_BOTTLE), false);
                        }
                        event.getEntityPlayer().swingArm(event.getHand());
                        if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                            event.getItemStack().shrink(1);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof EntityLivingBase && !(event.getTarget() instanceof EntityPlayer)) {
            EntityLivingBase living = (EntityLivingBase) event.getTarget();
            if (!event.getEntityPlayer().isSneaking() && VineLassoUtil.hasLassoData(living)) {
                if (!event.getWorld().isRemote) {
                    EntityItem lassoItem = new EntityItem(event.getWorld(), living.posX, living.posY, living.posZ, new ItemStack(AMItemRegistry.VINE_LASSO));
                    lassoItem.setDefaultPickupDelay();
                    event.getWorld().spawnEntity(lassoItem);
                }
                VineLassoUtil.lassoTo(null, living);
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
                return;
            }
        }
        if (event.getTarget() instanceof EntityLivingBase && !(event.getTarget() instanceof EntityPlayer) && !(event.getTarget() instanceof EntityEndergrade) && ((EntityLivingBase) event.getTarget()).isPotionActive(AMEffectRegistry.ENDER_FLU)) {
            if (event.getItemStack().getItem() == Items.CHORUS_FRUIT) {
                if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                    event.getItemStack().shrink(1);
                }
                event.getTarget().playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0F, 0.5F + event.getEntityPlayer().getRNG().nextFloat());
                if (event.getEntityPlayer().getRNG().nextFloat() < 0.4F) {
                    ((EntityLivingBase) event.getTarget()).removePotionEffect(AMEffectRegistry.ENDER_FLU);
                    event.getItemStack().onItemUseFinish(event.getWorld(), (EntityLivingBase) event.getTarget());
                }
                event.setCanceled(true);
                event.setCancellationResult(EnumActionResult.SUCCESS);
            }
        }
    }

    @SubscribeEvent
    public void onEntityDrops(LivingDropsEvent event) {
        if (VineLassoUtil.hasLassoData(event.getEntityLiving())) {
            VineLassoUtil.lassoTo(null, event.getEntityLiving());
            event.getDrops().add(new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, new ItemStack(AMItemRegistry.VINE_LASSO)));
        }
    }

    @SubscribeEvent
    public void onStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof EntitySquid && !event.getEntity().world.isRemote) {
            event.setCanceled(true);
            EntityGiantSquid squid = new EntityGiantSquid(event.getEntity().world);
            squid.setLocationAndAngles(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ, event.getEntity().rotationYaw, event.getEntity().rotationPitch);
            if (event.getEntity().hasCustomName()) {
                squid.setCustomNameTag(event.getEntity().getCustomNameTag());
                squid.setAlwaysRenderNameTag(event.getEntity().getAlwaysRenderNameTag());
            }
            squid.setBlue(true);
            squid.enablePersistence();
            event.getEntity().world.spawnEntity(squid);
            event.getEntity().setDead();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(LivingSpawnEvent.SpecialSpawn event) {
        try {
            if (event.getEntity() instanceof EntitySpider && AMConfig.spidersAttackFlies) {
                EntitySpider spider = (EntitySpider) event.getEntity();
                spider.targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(spider, EntityFly.class, true, true));
            }
            if (event.getEntity() instanceof EntityWolf && AMConfig.wolvesAttackMoose) {
                EntityWolf wolf = (EntityWolf) event.getEntity();
                wolf.targetTasks.addTask(6, new EntityAINearestAttackableTarget<EntityMoose>(wolf, EntityMoose.class, true, true) {
                    @Override
                    protected boolean isSuitableTarget(EntityLivingBase entity, boolean includeInvincibles) {
                        return !wolf.isTamed() && super.isSuitableTarget(entity, includeInvincibles);
                    }
                });
            }
            if (AMConfig.polarBearsAttackSeals && event.getEntity() instanceof EntityCreature && "Polar Bear".equals(EntityList.getEntityString(event.getEntity()))) {
                EntityCreature bear = (EntityCreature) event.getEntity();
                bear.targetTasks.addTask(6, new EntityAINearestAttackableTarget<>(bear, EntitySeal.class, true, true));
            }
            if (event.getEntity() instanceof EntityCreeper) {
                EntityCreeper creeper = (EntityCreeper) event.getEntity();
                creeper.tasks.addTask(3, new EntityAIAvoidEntity(creeper, EntitySnowLeopard.class, 6.0F, 1.0D, 1.2D));
                creeper.tasks.addTask(3, new EntityAIAvoidEntity(creeper, EntityTiger.class, 6.0F, 1.0D, 1.2D));
            }
            if (AMConfig.catsAndFoxesAttackJerboas && event.getEntity() instanceof net.minecraft.entity.passive.EntityOcelot) {
                net.minecraft.entity.passive.EntityOcelot ocelot = (net.minecraft.entity.passive.EntityOcelot) event.getEntity();
                ocelot.targetTasks.addTask(6, new EntityAINearestAttackableTarget<>(ocelot, EntityJerboa.class, true, true));
            }
        } catch (Exception e) {
            AlexsMobs.LOGGER.warn("Tried to add unique behaviors to vanilla mobs and encountered an error");
        }
    }

    @SubscribeEvent
    public void onPlayerAttackEntityEvent(AttackEntityEvent event) {
        if (event.getEntityPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItemRegistry.MOOSE_HEADGEAR && event.getTarget() instanceof EntityLivingBase) {
            float f1 = 2;
            ((EntityLivingBase) event.getTarget()).knockBack(event.getEntityPlayer(), f1 * 0.5F, MathHelper.sin(event.getEntityPlayer().rotationYaw * ((float) Math.PI / 180F)), -MathHelper.cos(event.getEntityPlayer().rotationYaw * ((float) Math.PI / 180F)));
        }
        if (event.getEntityPlayer().isPotionActive(AMEffectRegistry.TIGERS_BLESSING) && event.getTarget() instanceof EntityLivingBase && !event.getEntityPlayer().isOnSameTeam(event.getTarget()) && !(event.getTarget() instanceof EntityTiger)) {
            AxisAlignedBB bb = new AxisAlignedBB(event.getEntityPlayer().posX - 32, event.getEntityPlayer().posY - 32, event.getEntityPlayer().posZ - 32, event.getEntityPlayer().posX + 32, event.getEntityPlayer().posY + 32, event.getEntityPlayer().posZ + 32);
            List<EntityTiger> tigers = event.getEntityPlayer().world.getEntitiesWithinAABB(EntityTiger.class, bb, EntitySelectors.IS_ALIVE);
            for (EntityTiger tiger : tigers) {
                if (!tiger.isChild()) {
                    tiger.setAttackTarget((EntityLivingBase) event.getTarget());
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingDamageEvent(LivingDamageEvent event) {
        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
            if (event.getAmount() > 0 && attacker.isPotionActive(AMEffectRegistry.SOULSTEAL) && attacker.getActivePotionEffect(AMEffectRegistry.SOULSTEAL) != null) {
                int level = attacker.getActivePotionEffect(AMEffectRegistry.SOULSTEAL).getAmplifier() + 1;
                Random rand = new Random();
                if (attacker.getHealth() < attacker.getMaxHealth() && rand.nextFloat() < (0.25F + (level * 0.25F))) {
                    attacker.heal(Math.min(event.getAmount() / 2F * level, 2 + 2 * level));
                }
            }
        }
        if (event.getEntityLiving() instanceof EntityPlayer && event.getSource().getTrueSource() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.getSource().getTrueSource();
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (attacker instanceof EntityMimicOctopus && ((EntityMimicOctopus) attacker).isOwner(player)) {
                event.setCanceled(true);
                return;
            }
            if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItemRegistry.SPIKED_TURTLE_SHELL) {
                float f1 = 1F;
                if (attacker.getDistance(player) < attacker.width + player.width + 0.5F) {
                    attacker.attackEntityFrom(DamageSource.causeThornsDamage(player), 1F);
                    attacker.knockBack(player, f1 * 0.5F, MathHelper.sin((attacker.rotationYaw + 180) * ((float) Math.PI / 180F)), -MathHelper.cos((attacker.rotationYaw + 180) * ((float) Math.PI / 180F)));
                }
            }
        }
        if (!event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty() && event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == AMItemRegistry.EMU_LEGGINGS) {
            if (event.getSource().isProjectile() && event.getEntityLiving().getRNG().nextFloat() < AMConfig.emuPantsDodgeChance) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onLivingSetTargetEvent(LivingSetAttackTargetEvent event) {
        if (event.getTarget() != null && event.getEntityLiving() instanceof EntityMob) {
            if (event.getEntityLiving().getCreatureAttribute() == net.minecraft.entity.EnumCreatureAttribute.ARTHROPOD) {
                if (event.getTarget().isPotionActive(AMEffectRegistry.BUG_PHEROMONES) && event.getEntityLiving().getRevengeTarget() != event.getTarget()) {
                    ((EntityMob) event.getEntityLiving()).setAttackTarget(null);
                }
            }
        }
    }

    /** After the player tick (and vanilla {@code updatePassenger}) so mosquitoes stay latched during knockback. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void repositionBloodDrinkingMosquitos(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.getPassengers().isEmpty()) {
            return;
        }
        for (Entity passenger : player.getPassengers()) {
            if (passenger instanceof EntityCrimsonMosquito) {
                ((EntityCrimsonMosquito) passenger).repositionOnMount(player);
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            if (player.isPotionActive(AMEffectRegistry.CLINGING) && EffectClinging.isUpsideDown(player)) {
                player.eyeHeight = Math.max(0.1F, player.height - player.getEyeHeight());
            } else if (player.eyeHeight < player.height * 0.4F) {
                player.eyeHeight = player.getEyeHeight();
            }
            if (player.getEyeHeight() < player.height * 0.5D) {
                AxisAlignedBB aabb = player.getEntityBoundingBox();
                player.setEntityBoundingBox(new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.minY + player.height, aabb.maxZ));
            }
            IAttributeInstance modifiableattributeinstance = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            if (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == AMItemRegistry.ROADDRUNNER_BOOTS || modifiableattributeinstance.hasModifier(SAND_SPEED_BONUS)) {
                boolean sand = isSandBlock(player.world.getBlockState(getDownPos(player.getPosition(), player.world)).getBlock());
                if (sand && !modifiableattributeinstance.hasModifier(SAND_SPEED_BONUS)) {
                    modifiableattributeinstance.applyModifier(SAND_SPEED_BONUS);
                }
                if (player.ticksExisted % 25 == 0 && (player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() != AMItemRegistry.ROADDRUNNER_BOOTS || !sand) && modifiableattributeinstance.hasModifier(SAND_SPEED_BONUS)) {
                    modifiableattributeinstance.removeModifier(SAND_SPEED_BONUS);
                }
            }
            if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItemRegistry.FRONTIER_CAP || modifiableattributeinstance.hasModifier(SNEAK_SPEED_BONUS)) {
                if (player.isSneaking() && !modifiableattributeinstance.hasModifier(SNEAK_SPEED_BONUS)) {
                    modifiableattributeinstance.applyModifier(SNEAK_SPEED_BONUS);
                }
                if ((!player.isSneaking() || player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() != AMItemRegistry.FRONTIER_CAP) && modifiableattributeinstance.hasModifier(SNEAK_SPEED_BONUS)) {
                    modifiableattributeinstance.removeModifier(SNEAK_SPEED_BONUS);
                }
            }
            if (player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItemRegistry.SPIKED_TURTLE_SHELL) {
                if (!player.isInsideOfMaterial(Material.WATER)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 210, 0, false, false));
                }
            }
        }

        if (event.getEntityLiving().isElytraFlying()
                && event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() instanceof ItemTarantulaHawkElytra) {
            ItemStack elytraStack = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST);
            if (!event.getEntityLiving().world.isRemote
                    && (event.getEntityLiving().getTicksElytraFlying() + 1) % 20 == 0
                    && ItemTarantulaHawkElytra.isUsable(elytraStack)) {
                elytraStack.damageItem(1, event.getEntityLiving());
            }
        }

        if (event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == AMItemRegistry.CENTIPEDE_LEGGINGS) {
            if (event.getEntityLiving().collidedHorizontally && !event.getEntityLiving().isInWater()) {
                event.getEntityLiving().fallDistance = 0.0F;
                double d0 = MathHelper.clamp(event.getEntityLiving().motionX, -0.15F, 0.15F);
                double d1 = MathHelper.clamp(event.getEntityLiving().motionZ, -0.15F, 0.15F);
                double d2 = 0.1D;
                event.getEntityLiving().motionX = d0;
                event.getEntityLiving().motionZ = d1;
                event.getEntityLiving().motionY = d2;
            }
        }

        if (VineLassoUtil.hasLassoData(event.getEntityLiving())) {
            VineLassoUtil.tickLasso(event.getEntityLiving());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) {
            return;
        }
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof ItemTarantulaHawkElytra) {
            ItemTarantulaHawkElytra.handleElytraFlightTick(player, chest);
        } else {
            ItemTarantulaHawkElytra.clearGlidingState(player);
        }
    }

    private static boolean isSandBlock(Block block) {
        return block == Blocks.SAND || block == Blocks.SANDSTONE;
    }

    private BlockPos getDownPos(BlockPos entered, World world) {
        int i = 0;
        while (world.isAirBlock(entered) && i < 3) {
            entered = entered.down();
            i++;
        }
        return entered;
    }

    @SubscribeEvent
    public void onFOVUpdate(EntityViewRenderEvent.FOVModifier event) {
        if (event.getEntity() instanceof EntityLivingBase && ((EntityLivingBase) event.getEntity()).isPotionActive(AMEffectRegistry.FEAR)) {
            event.setFOV(1.0F);
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!event.getEntityLiving().getActiveItemStack().isEmpty() && event.getSource() != null && event.getSource().getTrueSource() != null) {
            if (event.getEntityLiving().getActiveItemStack().getItem() == AMItemRegistry.SHIELD_OF_THE_DEEP) {
                Entity attacker = event.getSource().getTrueSource();
                if (attacker instanceof EntityLivingBase) {
                    boolean flag = false;
                    if (attacker.getDistance(event.getEntityLiving()) <= 4 && !((EntityLivingBase) attacker).isPotionActive(AMEffectRegistry.EXSANGUINATION)) {
                        ((EntityLivingBase) attacker).addPotionEffect(new PotionEffect(AMEffectRegistry.EXSANGUINATION, 60, 2));
                        flag = true;
                    }
                    if (event.getEntityLiving().isInWater()) {
                        event.getEntityLiving().setAir(Math.min(300, event.getEntityLiving().getAir() + 150));
                        flag = true;
                    }
                    if (flag) {
                        event.getEntityLiving().getActiveItemStack().damageItem(1, event.getEntityLiving());
                    }
                }
            }
        }
    }

    private static boolean declaresLootTableOverride(Class<?> clazz) {
        Class<?> check = clazz;
        while (check != null && EntityLiving.class.isAssignableFrom(check)) {
            if (check != EntityLiving.class) {
                try {
                    check.getDeclaredMethod("getLootTable");
                    return true;
                } catch (NoSuchMethodException ignored) {
                }
            }
            check = check.getSuperclass();
        }
        return false;
    }

    @SubscribeEvent
    public static void assignEntityLootTable(EntityEvent.EntityConstructing event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityLiving)) {
            return;
        }
        EntityLiving living = (EntityLiving) entity;
        if (declaresLootTableOverride(living.getClass())) {
            return;
        }
        ResourceLocation key = EntityList.getKey(living);
        if (key == null || !AlexsMobs.MODID.equals(key.getResourceDomain())) {
            return;
        }
        try {
            if (DEATH_LOOT_TABLE_FIELD.get(living) != null) {
                return;
            }
            DEATH_LOOT_TABLE_FIELD.set(living, new ResourceLocation(key.getResourceDomain(), "entities/" + key.getResourcePath()));
        } catch (IllegalAccessException e) {
            AlexsMobs.LOGGER.warn("Failed to assign loot table for {}", key, e);
        }
    }

    @SubscribeEvent
    public void onChestGenerated(LootTableLoadEvent event) {
        if (event.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE)) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                pool.addEntry(new LootEntryItem(AMItemRegistry.ANCIENT_DART, 1, 40, new LootFunction[0], new LootCondition[0], "alexsmobs_ancient_dart"));
            }
        }
        if (event.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE_DISPENSER)) {
            LootPool pool = event.getTable().getPool("main");
            if (pool != null) {
                pool.addEntry(new LootEntryItem(AMItemRegistry.ANCIENT_DART, 3, 20, new LootFunction[0], new LootCondition[0], "alexsmobs_ancient_dart_dispenser"));
            }
        }
    }
}
