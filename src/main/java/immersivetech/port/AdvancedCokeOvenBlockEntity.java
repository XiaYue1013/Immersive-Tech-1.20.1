package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

public final class AdvancedCokeOvenBlockEntity extends BlockEntity implements FormableMultiblock, net.minecraft.world.MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final FluidTank creosote = new FluidTank(24_000, AdvancedCokeOvenBlockEntity::isCreosote);
    private final LazyOptional<ItemStackHandler> inputCapability = LazyOptional.of(() -> inventory);
    private final LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> creosote);
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private final Container containerView = new Container() {
        @Override public int getContainerSize() { return inventory.getSlots(); }
        @Override public boolean isEmpty() {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (!inventory.getStackInSlot(i).isEmpty()) return false;
            }
            return true;
        }
        @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
        @Override public ItemStack removeItem(int slot, int amount) {
            ItemStack extracted = inventory.extractItem(slot, amount, false);
            setChanged();
            return extracted;
        }
        @Override public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
            setChanged();
            return stack;
        }
        @Override public void setItem(int slot, ItemStack stack) {
            inventory.setStackInSlot(slot, stack);
            setChanged();
        }
        @Override public void setChanged() { AdvancedCokeOvenBlockEntity.this.setChanged(); }
        @Override public boolean stillValid(Player player) {
            return level != null && level.getBlockEntity(worldPosition) == AdvancedCokeOvenBlockEntity.this
                    && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64.0;
        }
        @Override public void clearContent() {
            for (int i = 0; i < inventory.getSlots(); i++) inventory.setStackInSlot(i, ItemStack.EMPTY);
            setChanged();
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> creosote.getFluidAmount();
                case 1 -> progress;
                case 2 -> processMax;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            if (index == 1) progress = value;
            else if (index == 2) processMax = value;
        }
        @Override
        public int getCount() { return 3; }
    };
    private int progress;
    private int processMax;
    private boolean active;
    private boolean formed;

    public AdvancedCokeOvenBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.ADVANCED_COKE_OVEN_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.immersivetech.coke_oven_advanced");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AdvancedCokeOvenMenu(id, inv, containerView, dataAccess);
    }

    private static boolean isCreosote(FluidStack stack) {
        return stack.getFluid().builtInRegistryHolder().key().location().toString()
                .equals("immersiveengineering:creosote");
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedCokeOvenBlockEntity oven) {
        if (!oven.formed) return;

        // Bucket handling for Creosote (slot 2 -> slot 3)
        ItemStack emptyBucket = oven.inventory.getStackInSlot(2);
        if (!emptyBucket.isEmpty() && emptyBucket.is(Items.BUCKET) && oven.creosote.getFluidAmount() >= 1000) {
            var creosoteBucketItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("immersiveengineering", "creosote_bucket"));
            if (creosoteBucketItem != null && creosoteBucketItem != Items.AIR) {
                ItemStack outputBucket = new ItemStack(creosoteBucketItem);
                ItemStack inSlot3 = oven.inventory.getStackInSlot(3);
                if (inSlot3.isEmpty()) {
                    oven.creosote.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    emptyBucket.shrink(1);
                    oven.inventory.setStackInSlot(3, outputBucket);
                    oven.setChanged();
                } else if (ItemStack.isSameItemSameTags(inSlot3, outputBucket) && inSlot3.getCount() < inSlot3.getMaxStackSize()) {
                    oven.creosote.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                    emptyBucket.shrink(1);
                    inSlot3.grow(1);
                    oven.setChanged();
                }
            }
        }

        AdvancedCokeOvenRecipe recipe = oven.findRecipe(level);
        if (recipe == null) {
            oven.progress = 0;
            oven.processMax = 0;
            oven.active = false;
            return;
        }
        if (oven.progress == 0) {
            oven.processMax = recipe.time();
        }
        var creosoteFluid = ForgeRegistries.FLUIDS.getValue(
                new ResourceLocation("immersiveengineering", "creosote"));
        if (recipe.creosoteAmount() > 0
                && (creosoteFluid == null
                || oven.creosote.getFluidAmount() + recipe.creosoteAmount() > oven.creosote.getCapacity())) {
            oven.active = false;
            return;
        }
        int speed = 1 + oven.connectedPreheaters();
        oven.active = true;
        oven.progress += speed;
        if (oven.progress >= oven.processMax) {
            oven.progress = 0;
            oven.processMax = 0;
            oven.inventory.extractItem(0, 1, false);
            oven.inventory.insertItem(1, recipe.output().copy(), false);
            if (recipe.creosoteAmount() > 0) {
                oven.creosote.fill(new FluidStack(creosoteFluid, recipe.creosoteAmount()),
                        IFluidHandler.FluidAction.EXECUTE);
            }
            oven.active = false;
            oven.setChanged();
        }
    }

    private AdvancedCokeOvenRecipe findRecipe(Level level) {
        Container view = new Container() {
            @Override public int getContainerSize() { return 1; }
            @Override public boolean isEmpty() { return inventory.getStackInSlot(0).isEmpty(); }
            @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(0); }
            @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
            @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
            @Override public void setItem(int slot, ItemStack stack) {}
            @Override public void setChanged() {}
            @Override public boolean stillValid(Player player) { return true; }
            @Override public void clearContent() {}
        };
        for (AdvancedCokeOvenRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.ADVANCED_COKE_OVEN_TYPE.get())) {
            if (recipe.matches(view, level)
                    && canAcceptOutput(recipe.output())) {
                return recipe;
            }
        }
        return null;
    }

    private boolean canAcceptOutput(ItemStack output) {
        ItemStack existing = inventory.getStackInSlot(1);
        return existing.isEmpty()
                || (ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + .5,
                        worldPosition.getY() + .5, worldPosition.getZ() + .5, stack.copy()));
            }
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private int connectedPreheaters() {
        int result = 0;
        Direction facing = getBlockState().getValue(AdvancedCokeOvenBlock.FACING);
        for (Direction side : new Direction[] {facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(side, 2));
            if (be instanceof CokeOvenPreheaterBlockEntity preheater
                    && preheater.getBlockState().getValue(CokeOvenPreheaterBlock.FACING) == side.getOpposite()) {
                result += preheater.doSpeedup();
            }
        }
        return result;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", inventory.serializeNBT());
        tag.put("creosote", creosote.writeToNBT(new CompoundTag()));
        tag.putInt("progress", progress);
        tag.putInt("process_max", processMax);
        tag.putBoolean("active", active);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        creosote.readFromNBT(tag.getCompound("creosote"));
        progress = tag.getInt("progress");
        processMax = tag.getInt("process_max");
        active = tag.getBoolean("active");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return inputCapability.cast();
        if (capability == ForgeCapabilities.FLUID_HANDLER) return fluidCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCapability.invalidate();
        fluidCapability.invalidate();
    }

    public boolean isActive() { return active; }
    public int progress() { return progress; }
    public int processMax() { return processMax; }
    public ItemStack insertInput(ItemStack stack) {
        return inventory.insertItem(0, stack, false);
    }
    public ItemStack extractOutput() {
        return inventory.extractItem(1, inventory.getStackInSlot(1).getCount(), false);
    }
}
