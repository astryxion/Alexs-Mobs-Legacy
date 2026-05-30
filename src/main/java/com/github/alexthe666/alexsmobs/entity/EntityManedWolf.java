package com.github.alexthe666.alexsmobs.entity;
import com.github.alexthe666.alexsmobs.misc.AMLootTables;

import com.github.alexthe666.alexsmobs.client.particle.AMParticleRegistry;
import com.github.alexthe666.alexsmobs.config.AMConfig;
import com.github.alexthe666.alexsmobs.entity.ai.CreatureAITargetItems;
import com.github.alexthe666.alexsmobs.entity.util.Maths;
import com.github.alexthe666.alexsmobs.misc.AMPointOfInterestRegistry;
import com.github.alexthe666.alexsmobs.misc.AMSoundRegistry;
import com.github.alexthe666.alexsmobs.misc.AMTagRegistry;
import com.github.alexthe666.alexsmobs.tileentity.TileEntityLeafcutterAnthill;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import java.util.List;

public class EntityManedWolf extends EntityAnimal implements ITargetsDroppedItems {

    private static final DataParameter<Float> EAR_PITCH = EntityDataManager.createKey(EntityManedWolf.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> EAR_YAW = EntityDataManager.createKey(EntityManedWolf.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> DANCING = EntityDataManager.createKey(EntityManedWolf.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> SHAKING_TIME = EntityDataManager.createKey(EntityManedWolf.class, DataSerializers.VARINT);

    public float prevEarPitch;
    public float prevEarYaw;
    public float prevDanceProgress;
    public float danceProgress;
    public float prevShakeProgress;
    public float shakeProgress;

    private int earCooldown = 0;
    private float targetPitch;
    private float targetYaw;
    private boolean isJukeboxing;
    private BlockPos jukeboxPosition;
    private BlockPos nearestAnthill;

    public EntityManedWolf(World world) {
        super(world);
        this.setSize(0.7F, 0.85F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0D);
        AMEntityRegistry.registerAttributeIfAbsent(this, SharedMonsterAttributes.ATTACK_DAMAGE);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
    }

    @Override
    public boolean getCanSpawnHere() {
        return AMEntityRegistry.rollSpawn(AMConfig.manedWolfSpawnRolls, this.getRNG(), AMEntityRegistry.AMSpawnReason.OTHER) && super.getCanSpawnHere();
    }

    @Override
    protected void initEntityAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.5D));
        this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.AIR, false) {
            @Override
            protected boolean isTempting(ItemStack stack) {
                return EntityManedWolf.isManedWolfFood(stack) && !AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_STENCH_FOODS, stack.getItem());
            }
        });
        this.tasks.addTask(4, new EntityAIWander(this, 1.0D, 60));
        this.tasks.addTask(5, new EntityAIFollowParent(this, 1.0D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new CreatureAITargetItems(this, false, 30));
    }

    public static boolean isManedWolfFood(ItemStack stack) {
        Item item = stack.getItem();
        return AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_BREEDABLES, item) || AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_STENCH_FOODS, item);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(EAR_PITCH, 0F);
        this.dataManager.register(EAR_YAW, 0F);
        this.dataManager.register(SHAKING_TIME, 0);
        this.dataManager.register(DANCING, false);
    }

    public float getEarYaw() {
        return this.dataManager.get(EAR_YAW);
    }

    public void setEarYaw(float yaw) {
        this.dataManager.set(EAR_YAW, yaw);
    }

    public float getEarPitch() {
        return this.dataManager.get(EAR_PITCH);
    }

    public void setEarPitch(float pitch) {
        this.dataManager.set(EAR_PITCH, pitch);
    }

    public boolean isDancing() {
        return this.dataManager.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        this.dataManager.set(DANCING, dancing);
        this.isJukeboxing = dancing;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSoundRegistry.MANED_WOLF_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return AMSoundRegistry.MANED_WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSoundRegistry.MANED_WOLF_HURT;
    }
    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return AMLootTables.MANED_WOLF;
    }

    private void attractAnimals() {
        if (this.getShakingTime() % 5 == 0) {
            List<EntityAnimal> list = this.world.getEntitiesWithinAABB(EntityAnimal.class, this.getEntityBoundingBox().grow(16, 8, 16));
            for (EntityAnimal e : list) {
                if (!(e instanceof EntityManedWolf) && !(e instanceof EntityTameable && ((EntityTameable) e).isSitting())) {
                    e.setRevengeTarget(null);
                    e.setLastAttackedEntity(null);
                    Vec3d vec = net.minecraft.entity.ai.RandomPositionGenerator.findRandomTargetBlockAwayFrom(e, 20, 7, new Vec3d(this.posX, this.posY, this.posZ));
                    BlockPos blockpos = vec == null ? null : new BlockPos(vec);
                    if (blockpos != null) {
                        e.getNavigator().tryMoveToXYZ(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 1.5D);
                    }
                }
            }
        }
    }

    private void pollinateAnthill() {
        if (nearestAnthill != null) {
            TileEntity te = this.world.getTileEntity(nearestAnthill);
            if (te instanceof TileEntityLeafcutterAnthill) {
                if (this.getShakingTime() % 5 == 0) {
                    this.getNavigator().tryMoveToXYZ(nearestAnthill.getX() + 0.5F, nearestAnthill.getY() + 1F, nearestAnthill.getZ() + 0.5F, 1.0D);
                }
                if (nearestAnthill.distanceSq(this.posX, this.posY, this.posZ) < 36 && this.getShakingTime() % 20 == 0) {
                    ((TileEntityLeafcutterAnthill) te).growFungus();
                }
            }
        }
    }

    private void findAnthill() {
        if (nearestAnthill == null || !(this.world.getTileEntity(nearestAnthill) instanceof TileEntityLeafcutterAnthill)) {
            List<BlockPos> listOfHives = AMPointOfInterestRegistry.findAll(this.world, this.getPosition(), 10, AMPointOfInterestRegistry::matchesLeafcutterAnthill);
            BlockPos nearest = null;
            for (BlockPos pos : listOfHives) {
                if (nearest == null || pos.distanceSq(this.getPosition()) < nearest.distanceSq(this.getPosition())) {
                    nearest = pos;
                }
            }
            nearestAnthill = nearest;
        }
    }

    public boolean isShaking() {
        return this.getShakingTime() > 0;
    }

    public int getShakingTime() {
        return this.dataManager.get(SHAKING_TIME);
    }

    public void setShakingTime(int shaking) {
        this.dataManager.set(SHAKING_TIME, shaking);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack itemstack = player.getHeldItem(hand);
        boolean type = super.processInteract(player, hand);
        if (AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_STENCH_FOODS, itemstack.getItem()) && !this.isShaking() && this.getHeldItemMainhand().isEmpty()) {
            if (!player.capabilities.isCreativeMode) {
                itemstack.shrink(1);
            }
            eatItemEffect(itemstack);
            this.setShakingTime(100 + this.rand.nextInt(30));
            return true;
        }
        return type;
    }

    private void eatItemEffect(ItemStack heldItemMainhand) {
        for (int i = 0; i < 2 + this.rand.nextInt(2); i++) {
            double d2 = this.rand.nextGaussian() * 0.02D;
            double d0 = this.rand.nextGaussian() * 0.02D;
            double d1 = this.rand.nextGaussian() * 0.02D;
            float radius = this.width * 0.65F;
            float angle = Maths.STARTING_ANGLE * this.renderYawOffset;
            double extraX = radius * MathHelper.sin((float) Math.PI + angle);
            double extraZ = radius * MathHelper.cos(angle);
            if (heldItemMainhand.getItem() instanceof ItemBlock) {
                Block block = ((ItemBlock) heldItemMainhand.getItem()).getBlock();
                this.world.spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX + extraX, this.posY + this.height * 0.6F, this.posZ + extraZ, d0, d1, d2, Block.getStateId(block.getDefaultState()));
            } else {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX + extraX, this.posY + this.height * 0.6F, this.posZ + extraZ, d0, d1, d2, Item.getIdFromItem(heldItemMainhand.getItem()));
            }
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.prevEarPitch = this.getEarPitch();
        this.prevEarYaw = this.getEarYaw();
        this.prevDanceProgress = this.danceProgress;
        this.prevShakeProgress = this.shakeProgress;
        if (!this.world.isRemote) {
            updateEars();
        }
        boolean dance = this.isDancing();
        if (this.jukeboxPosition == null || this.jukeboxPosition.distanceSq(this.posX, this.posY, this.posZ) > 15.0D * 15.0D || this.world.getBlockState(this.jukeboxPosition).getBlock() != Blocks.JUKEBOX) {
            this.isJukeboxing = false;
            this.setDancing(false);
            this.jukeboxPosition = null;
        }
        if (dance && this.danceProgress < 5F) {
            this.danceProgress++;
        }
        if (!dance && this.danceProgress > 0F) {
            this.danceProgress--;
        }
        if (this.isShaking() && this.shakeProgress < 5F) {
            this.shakeProgress++;
        }
        if (!this.isShaking() && this.shakeProgress > 0F) {
            this.shakeProgress--;
        }
        if (this.isShaking()) {
            this.setShakingTime(this.getShakingTime() - 1);
            if (this.world.isRemote) {
                double d0 = this.rand.nextGaussian() * 0.02D;
                double d1 = 0.05F + this.rand.nextGaussian() * 0.02D;
                double d2 = this.rand.nextGaussian() * 0.02D;
                AMParticleRegistry.spawnParticle(this.world, AMParticleRegistry.SMELLY, this.posX + (this.rand.nextFloat() - 0.5F) * 0.7F, this.posY + this.height * 0.6F, this.posZ + (this.rand.nextFloat() - 0.5F) * 0.7F, d0, d1, d2);
            } else {
                attractAnimals();
                findAnthill();
                if (this.nearestAnthill != null) {
                    pollinateAnthill();
                }
            }
        }
    }

    private void updateEars() {
        final float pitchDist = Math.abs(this.targetPitch - this.getEarPitch());
        final float yawDist = Math.abs(this.targetYaw - this.getEarYaw());
        if (this.earCooldown <= 0 && this.rand.nextInt(30) == 0 && pitchDist <= 0.1F && yawDist <= 0.1F) {
            this.targetPitch = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            this.targetYaw = MathHelper.clamp(this.rand.nextFloat() * 60F - 30, -30, 30);
            this.earCooldown = 8 + this.rand.nextInt(15);
        }
        if (pitchDist > 0.1F) {
            if (this.getEarPitch() < this.targetPitch) {
                this.setEarPitch(this.getEarPitch() + Math.min(pitchDist, 4F));
            }
            if (this.getEarPitch() > this.targetPitch) {
                this.setEarPitch(this.getEarPitch() - Math.min(pitchDist, 4F));
            }
        }
        if (yawDist > 0.1F) {
            if (this.getEarYaw() < this.targetYaw) {
                this.setEarYaw(this.getEarYaw() + Math.min(yawDist, 4F));
            }
            if (this.getEarYaw() > this.targetYaw) {
                this.setEarYaw(this.getEarYaw() - Math.min(yawDist, 4F));
            }
        }
        if (this.earCooldown > 0) {
            this.earCooldown--;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return !AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_STENCH_FOODS, stack.getItem()) && isManedWolfFood(stack);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.isDancing() || this.danceProgress > 0) {
            if (this.getNavigator().getPath() != null) {
                this.getNavigator().clearPath();
            }
            strafe = 0.0F;
            vertical = 0.0F;
            forward = 0.0F;
        }
        super.travel(strafe, vertical, forward);
    }

    @Override
    public boolean canTargetItem(ItemStack stack) {
        return isManedWolfFood(stack) && !this.isShaking();
    }

    @Override
    public void onGetItem(EntityItem e) {
        eatItemEffect(e.getItem());
        if (AMTagRegistry.itemInTag(AMTagRegistry.MANED_WOLF_STENCH_FOODS, e.getItem().getItem())) {
            this.setShakingTime(100 + this.rand.nextInt(30));
        }
    }

    @Nullable
    @Override
    public EntityAgeable createChild(EntityAgeable ageable) {
        return (EntityManedWolf) AMEntityRegistry.MANED_WOLF.newInstance(this.world);
    }

    @SideOnly(Side.CLIENT)
    public void setPartying(BlockPos pos, boolean isPartying) {
        this.jukeboxPosition = pos;
        this.isJukeboxing = isPartying;
        this.setDancing(isPartying);
    }

    public boolean isEnder() {
        String s = TextFormatting.getTextWithoutFormattingCodes(this.getName());
        return s != null && (s.toLowerCase().contains("plummet") || s.toLowerCase().contains("ender"));
    }
}
