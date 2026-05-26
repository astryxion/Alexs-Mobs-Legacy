package com.github.alexthe666.alexsmobs.tileentity;

import com.github.alexthe666.alexsmobs.AlexsMobs;
import com.github.alexthe666.alexsmobs.block.BlockCapsid;
import com.github.alexthe666.alexsmobs.entity.AMEntityRegistry;
import com.github.alexthe666.alexsmobs.entity.EntityEnderiophage;
import com.github.alexthe666.alexsmobs.item.AMItemRegistry;
import com.github.alexthe666.alexsmobs.message.MessageUpdateCapsid;
import net.minecraft.block.BlockEndRod;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import javax.annotation.Nullable;

/**
 * 1.12 port of the 1.16 capsid tile: same transfer animation, end-rod enderiophage spawn, larva/disc transforms.
 */
public class TileEntityCapsid extends TileEntity implements ITickable, ISidedInventory {

    private static final int[] slotsTop = new int[]{0};

    public int ticksExisted;
    public float prevFloatUpProgress;
    public float floatUpProgress;
    public float prevYawSwitchProgress;
    public float yawSwitchProgress;
    public boolean vibrating = false;

    private final IItemHandler[] handlers = new IItemHandler[]{
            new SidedInvWrapper(this, EnumFacing.UP),
            new SidedInvWrapper(this, EnumFacing.DOWN)
    };

    private float yawTarget = 0;
    private int transformProg = 0;
    private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

    public TileEntityCapsid() {
    }

    @Override
    public void update() {
        prevFloatUpProgress = floatUpProgress;
        prevYawSwitchProgress = yawSwitchProgress;
        ticksExisted++;
        vibrating = false;
        if (!this.getStackInSlot(0).isEmpty()) {
            TileEntity up = world.getTileEntity(this.pos.up());
            if (up instanceof IInventory) {
                if (floatUpProgress >= 1) {
                    if (up.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP)) {
                        IItemHandler handler = up.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
                        if (handler != null) {
                            if (ItemHandlerHelper.insertItem(handler, this.getStackInSlot(0), true).isEmpty()) {
                                ItemHandlerHelper.insertItem(handler, this.getStackInSlot(0).copy(), false);
                                this.setInventorySlotContents(0, ItemStack.EMPTY);
                            }
                        }
                    }
                    yawTarget = 0F;
                    yawSwitchProgress = 0F;
                } else {
                    if (up instanceof TileEntityCapsid) {
                        yawTarget = MathHelper.wrapDegrees(((TileEntityCapsid) up).getBlockAngle() - this.getBlockAngle());
                    } else {
                        yawTarget = 0F;
                    }
                    if (yawTarget < yawSwitchProgress) {
                        yawSwitchProgress += yawTarget * 0.1F;
                    } else if (yawTarget > yawSwitchProgress) {
                        yawSwitchProgress += yawTarget * 0.1F;
                    }
                    floatUpProgress += 0.05F;
                }
            } else {
                floatUpProgress = 0F;
            }
            IBlockState downState = world.getBlockState(this.getPos().down());
            if (this.getStackInSlot(0).getItem() == Items.ENDER_EYE && downState.getBlock() == Blocks.END_ROD
                    && downState.getValue(BlockEndRod.FACING).getAxis() == EnumFacing.Axis.Y) {
                vibrating = true;
                if (transformProg > 20) {
                    this.setInventorySlotContents(0, ItemStack.EMPTY);
                    this.world.destroyBlock(this.getPos(), false);
                    this.world.destroyBlock(this.getPos().down(), false);
                    Entity spawned = AMEntityRegistry.ENDERIOPHAGE.newInstance(world);
                    if (spawned instanceof EntityEnderiophage) {
                        EntityEnderiophage phage = (EntityEnderiophage) spawned;
                        phage.setPosition(this.getPos().getX() + 0.5D, this.getPos().getY() - 1.0D, this.getPos().getZ() + 0.5D);
                        phage.setVariant(0);
                        if (!world.isRemote) {
                            world.spawnEntity(phage);
                        }
                    }
                }
            }
            if (this.getStackInSlot(0).getItem() == AMItemRegistry.MOSQUITO_LARVA && world.getBlockState(this.getPos().up()).getBlock() != world.getBlockState(this.getPos()).getBlock()) {
                vibrating = true;
                if (transformProg > 60) {
                    ItemStack current = this.getStackInSlot(0).copy();
                    current.shrink(1);
                    if (!current.isEmpty()) {
                        EntityItem entityItem = new EntityItem(this.world, this.getPos().getX() + 0.5F, this.getPos().getY() + 0.5F, this.getPos().getZ() + 0.5F, current);
                        if (!world.isRemote) {
                            world.spawnEntity(entityItem);
                        }
                    }
                    this.setInventorySlotContents(0, new ItemStack(AMItemRegistry.MYSTERIOUS_WORM));
                }
            }
            if (isMusicDiscForCapsid(this.getStackInSlot(0)) && world.getBlockState(this.getPos().up()).getBlock() != world.getBlockState(this.getPos()).getBlock()) {
                vibrating = true;
                if (transformProg > 120) {
                    ItemStack current = this.getStackInSlot(0).copy();
                    current.shrink(1);
                    if (!current.isEmpty()) {
                        EntityItem entityItem = new EntityItem(this.world, this.getPos().getX() + 0.5F, this.getPos().getY() + 0.5F, this.getPos().getZ() + 0.5F, current);
                        if (!world.isRemote) {
                            world.spawnEntity(entityItem);
                        }
                    }
                    this.setInventorySlotContents(0, new ItemStack(AMItemRegistry.MUSIC_DISC_DAZE));
                }
            }
        } else {
            floatUpProgress = 0F;
        }
        if (!vibrating) {
            transformProg = 0;
        } else {
            transformProg++;
        }
    }

    /**
     * 1.12 equivalent of 1.16 {@code ItemTags.MUSIC_DISCS} membership (vanilla {@link ItemRecord} + Alex's Mobs discs once registered).
     */
    private static boolean isMusicDiscForCapsid(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == AMItemRegistry.MUSIC_DISC_DAZE) {
            return false;
        }
        if (item instanceof ItemRecord) {
            return true;
        }
        return item == AMItemRegistry.MUSIC_DISC_THIME;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public net.minecraft.util.math.AxisAlignedBB getRenderBoundingBox() {
        BlockPos max = this.pos.add(1, 2, 1);
        return new net.minecraft.util.math.AxisAlignedBB(
                (double) this.pos.getX(), (double) this.pos.getY(), (double) this.pos.getZ(),
                (double) max.getX(), (double) max.getY(), (double) max.getZ());
    }

    @Override
    public int getSizeInventory() {
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getSizeInventory(); i++) {
            if (!this.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.stacks.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (!this.stacks.get(index).isEmpty()) {
            ItemStack itemstack;

            if (this.stacks.get(index).getCount() <= count) {
                itemstack = this.stacks.get(index);
                this.stacks.set(index, ItemStack.EMPTY);
                return itemstack;
            } else {
                itemstack = this.stacks.get(index).splitStack(count);

                if (this.stacks.get(index).isEmpty()) {
                    this.stacks.set(index, ItemStack.EMPTY);
                }

                return itemstack;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack getStackInSlotOnClosing(int index) {
        if (!this.stacks.get(index).isEmpty()) {
            ItemStack itemstack = this.stacks.get(index);
            this.stacks.set(index, itemstack);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.stacks.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }
        this.markDirty();
        if (this.world != null && !this.world.isRemote) {
            AlexsMobs.sendMSGToAll(new MessageUpdateCapsid(this.getPos().toLong(), this.stacks.get(0)));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.stacks = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, this.stacks);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, this.stacks);
        return compound;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.stacks.size(); ++i) {
            this.stacks.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return slotsTop;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack slotStack = this.stacks.get(index);
        if (slotStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.stacks.set(index, ItemStack.EMPTY);
            return slotStack;
        }
    }

    @Override
    public String getName() {
        return "alexsmobs.capsid";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation("block.alexsmobs.capsid");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        this.readFromNBT(tag);
    }

    public float getBlockAngle() {
        IBlockState state = this.world != null ? this.world.getBlockState(this.pos) : null;
        if (state != null && state.getBlock() instanceof BlockCapsid) {
            EnumFacing dir = state.getValue(BlockCapsid.FACING);
            return dir.getHorizontalAngle();
        }
        return 0.0F;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (!this.isInvalid() && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (!this.isInvalid() && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.DOWN) {
                return (T) this.handlers[0];
            } else {
                return (T) this.handlers[1];
            }
        }
        return super.getCapability(capability, facing);
    }
}
