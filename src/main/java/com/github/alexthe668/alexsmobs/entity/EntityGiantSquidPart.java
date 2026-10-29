package com.github.alexthe668.alexsmobs.entity;

import com.github.alexthe668.alexsmobs.AlexsMobs;
import com.github.alexthe668.alexsmobs.message.MessageHurtMultipart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
/**
 * Hitbox part for the giant squid; Forge 1.12 has no {@code PartEntity}, so this is a lightweight
 * {@link Entity} positioned each tick by {@link EntityGiantSquid}.
 */
public class EntityGiantSquidPart extends Entity implements IHurtableMultipart {

    private static final net.minecraft.network.datasync.DataParameter<Integer> PARENT_ID = net.minecraft.network.datasync.EntityDataManager.createKey(EntityGiantSquidPart.class, net.minecraft.network.datasync.DataSerializers.VARINT);
    private static final net.minecraft.network.datasync.DataParameter<Integer> PART_INDEX = net.minecraft.network.datasync.EntityDataManager.createKey(EntityGiantSquidPart.class, net.minecraft.network.datasync.DataSerializers.VARINT);

    @Nullable
    private EntityGiantSquid squid;
    public float scale = 1F;
    private final float baseWidth;
    private final float baseHeight;
    private boolean collisionOnly = false;
    private int partIndex = -1;

  /**
     * Required for {@link net.minecraftforge.fml.common.registry.EntityEntry} / factory registration.
     */
    public EntityGiantSquidPart(World world) {
        super(world);
        this.baseWidth = 1.0F;
        this.baseHeight = 1.0F;
        this.setSize(1.0F, 1.0F);
        this.setInvisible(true);
    }

    public EntityGiantSquidPart(EntityGiantSquid parent, float sizeX, float sizeY) {
        super(parent.world);
        this.squid = parent;
        this.baseWidth = sizeX;
        this.baseHeight = sizeY;
        this.setSize(sizeX, sizeY);
        this.setInvisible(true);
    }

    public EntityGiantSquidPart(EntityGiantSquid parent, float sizeX, float sizeY, boolean collisionOnly) {
        this(parent, sizeX, sizeY);
        this.collisionOnly = collisionOnly;
    }

    @Nullable
    public EntityGiantSquid getSquid() {
        return this.squid;
    }

    protected void entityInit() {
        this.dataManager.register(PARENT_ID, -1);
        this.dataManager.register(PART_INDEX, -1);
    }

    public void bindToParent(EntityGiantSquid parent, int index, boolean collisionOnly) {
        this.squid = parent;
        this.partIndex = index;
        this.collisionOnly = collisionOnly;
        if (parent != null) {
            this.dataManager.set(PARENT_ID, parent.getEntityId());
            this.dataManager.set(PART_INDEX, index);
        }
    }

    private static final float[] PART_SIZES = new float[]{0.9F, 1.2F, 0.45F, 2.9F, 0.9F, 1.0F, 1.2F, 1.2F, 1.2F, 1.2F};

    @Override
    public void onUpdate() {
        this.setNoGravity(true);
        this.noClip = true;
        int index = this.dataManager.get(PART_INDEX);
        if (index < 0) {
            index = this.partIndex;
        }
        if (index >= 0 && index < PART_SIZES.length && (this.width != PART_SIZES[index] || this.height != PART_SIZES[index])) {
            this.setSize(PART_SIZES[index], PART_SIZES[index]);
        }
        this.collisionOnly = index == 3;
        super.onUpdate();
        EntityGiantSquid parent = resolveParent();
        if (parent != null && index >= 0) {
            parent.positionPart(this, index);
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
        }
    }

    @Nullable
    private EntityGiantSquid resolveParent() {
        if (this.squid != null && this.squid.isEntityAlive()) {
            return this.squid;
        }
        int id = this.dataManager.get(PARENT_ID);
        if (id >= 0) {
            net.minecraft.entity.Entity entity = this.world.getEntityByID(id);
            if (entity instanceof EntityGiantSquid) {
                this.squid = (EntityGiantSquid) entity;
                return this.squid;
            }
        }
        return this.squid;
    }

    @Override
    public boolean isInvisible() {
        return true;
    }

    protected void collideWithNearbyEntities() {
    }

    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        return this.squid != null && this.squid.applyInteractionFromPart(player, hand);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !collisionOnly;
    }

    protected void collideWithEntity(Entity entityIn) {
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    public boolean isPickable() {
        return !collisionOnly;
    }

    @Nullable
    public ItemStack getPickedResult(net.minecraft.util.math.RayTraceResult target) {
        Entity parent = this.squid;
        return parent != null ? parent.getPickedResult(target) : ItemStack.EMPTY;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.world.isRemote && this.squid != null && !this.squid.isEntityInvulnerable(source) && !collisionOnly) {
            AlexsMobs.sendMSGToServer(new MessageHurtMultipart(this.getEntityId(), this.squid.getEntityId(), amount));
        }
        return !collisionOnly && !this.isEntityInvulnerable(source) && this.squid != null && this.squid.attackEntityPartFrom(this, source, amount);
    }

    @Override
    public boolean isEntityEqual(Entity entityIn) {
        return this == entityIn || this.squid == entityIn;
    }

    public void recalculateSize() {
        this.setSize(this.baseWidth * this.scale, this.baseHeight * this.scale);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
    }

    @Override
    public void onAttackedFromServer(EntityLivingBase parent, float damage) {
    }
}
