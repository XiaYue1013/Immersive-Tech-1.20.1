package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public final class DistillerBlockEntity extends BlockEntity implements FormableMultiblock, net.minecraft.world.MenuProvider {
    private final FluidTank input = new FluidTank(ModConfig.TANK_CAPACITY.get(), stack ->
            stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER);
    private final FluidTank output = new FluidTank(ModConfig.TANK_CAPACITY.get(), stack ->
            stack.getFluid() == ModFluids.DISTILLED_WATER.get());
    private final EnergyStorage energy = new EnergyStorage(16_000, 160);
    private final LazyOptional<IFluidHandler> inputCapability = LazyOptional.of(() -> input);
    private final LazyOptional<IFluidHandler> outputCapability = LazyOptional.of(() -> output);
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private final net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            DistillerBlockEntity.this.setChanged();
        }
    };
    private final net.minecraft.world.inventory.ContainerData dataAccess = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> input.getFluidAmount();
                case 1 -> output.getFluidAmount();
                case 2 -> energy.getEnergyStored();
                case 3 -> energy.getMaxEnergyStored();
                case 4 -> progress;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            if (index == 4) progress = value;
        }
        @Override
        public int getCount() { return 5; }
    };
    private int progress;
    private int saltCount;
    private boolean formed;

    public DistillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.DISTILLER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.immersivetech.metal_multiblock");
    }

    @Nullable
    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, Player player) {
        return new DistillerMenu(id, inv, container, dataAccess);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity machine) {
        if (level.isClientSide) return;

        // Container bucket operations
        ItemStack inBucket = machine.container.getItem(0);
        if (!inBucket.isEmpty() && inBucket.getItem() instanceof BucketItem bucket && bucket.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
            if (machine.input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                ItemStack outBucket = machine.container.getItem(1);
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                if (outBucket.isEmpty()) {
                    machine.input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                    inBucket.shrink(1);
                    machine.container.setItem(1, emptyBucket);
                    machine.setChanged();
                } else if (ItemStack.isSameItemSameTags(outBucket, emptyBucket) && outBucket.getCount() < outBucket.getMaxStackSize()) {
                    machine.input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
                    inBucket.shrink(1);
                    outBucket.grow(1);
                    machine.setChanged();
                }
            }
        }
        ItemStack emptyOut = machine.container.getItem(2);
        if (!emptyOut.isEmpty() && emptyOut.is(net.minecraft.world.item.Items.BUCKET) && machine.output.getFluidAmount() >= 1000) {
            ItemStack distilledBucket = new ItemStack(ModFluids.DISTILLED_WATER_BUCKET.get());
            ItemStack fullOut = machine.container.getItem(3);
            if (fullOut.isEmpty()) {
                machine.output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                emptyOut.shrink(1);
                machine.container.setItem(3, distilledBucket);
                machine.setChanged();
            } else if (ItemStack.isSameItemSameTags(fullOut, distilledBucket) && fullOut.getCount() < fullOut.getMaxStackSize()) {
                machine.output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                emptyOut.shrink(1);
                fullOut.grow(1);
                machine.setChanged();
            }
        }

        DistillerRecipe recipe = machine.findRecipe(level);
        int inputAmount = recipe == null ? 0 : recipe.inputAmount();
        int outputAmount = recipe == null ? 0 : recipe.outputAmount();
        int energyCost = recipe == null ? 0 : recipe.energy();
        int processTime = recipe == null ? 1 : recipe.time();
        if (recipe != null && recipe.getId().equals(new net.minecraft.resources.ResourceLocation(
                ImmersiveTech.MOD_ID, "distiller_water"))) {
            inputAmount = ModConfig.DISTILLER_INPUT.get();
            outputAmount = ModConfig.DISTILLER_OUTPUT.get();
            energyCost = ModConfig.DISTILLER_ENERGY.get();
            processTime = ModConfig.DISTILLER_TIME.get();
        }
        if (!machine.formed || recipe == null
                || machine.input.getFluidAmount() < inputAmount
                || machine.output.getFluidAmount() + outputAmount > machine.output.getCapacity()
                || machine.energy.getEnergyStored() < energyCost) {
            return;
        }
        machine.progress++;
        if (machine.progress >= processTime) {
            machine.progress = 0;
            machine.energy.extractEnergy(energyCost, false);
            machine.input.drain(inputAmount, IFluidHandler.FluidAction.EXECUTE);
            machine.output.fill(new FluidStack(recipe.output(), outputAmount),
                    IFluidHandler.FluidAction.EXECUTE);
            if (level.random.nextFloat() < recipe.saltChance()) {
                machine.saltCount++;
            }
            machine.setChanged();
        }
    }

    @Nullable
    private DistillerRecipe findRecipe(Level level) {
        FluidStack contents = input.getFluid();
        if (contents.isEmpty()) return null;
        for (DistillerRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.DISTILLER_TYPE.get())) {
            if (recipe.input() == contents.getFluid()) return recipe;
        }
        return null;
    }

    public void tryHandleItem(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BucketItem bucket && bucket.getFluid() == net.minecraft.world.level.material.Fluids.WATER
                && input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                IFluidHandler.FluidAction.SIMULATE) == 1000) {
            input.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000),
                    IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(net.minecraft.world.item.Items.BUCKET));
            }
            setChanged();
            return;
        }
        if (stack.is(net.minecraft.world.item.Items.BUCKET) && output.getFluidAmount() >= 1000) {
            output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack distilledBucket = new ItemStack(ModFluids.DISTILLED_WATER_BUCKET.get());
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, distilledBucket);
                } else if (!player.getInventory().add(distilledBucket)) {
                    player.drop(distilledBucket, false);
                }
            }
            setChanged();
            return;
        }
        if (saltCount > 0 && player.getInventory().add(new ItemStack(ModRegistries.MATERIAL_SALT.get(), saltCount))) {
            saltCount = 0;
            setChanged();
            return;
        }
        if (!player.isShiftKeyDown() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, this, worldPosition);
            return;
        }
        String status = String.format("Formed: %s | Water: %d/%d mB | Distilled: %d/%d mB | Energy: %d/%d FE",
                formed, input.getFluidAmount(), input.getCapacity(),
                output.getFluidAmount(), output.getCapacity(),
                energy.getEnergyStored(), energy.getMaxEnergyStored());
        player.displayClientMessage(Component.literal(status), true);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        dropFluid(input);
        dropFluid(output);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .5,
                        worldPosition.getZ() + .5, stack));
            }
        }
        if (saltCount > 0) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .5,
                    worldPosition.getZ() + .5, new ItemStack(ModRegistries.MATERIAL_SALT.get(), saltCount)));
        }
    }

    private void dropFluid(FluidTank tank) {
        if (tank.getFluidAmount() > 0 && level != null) {
            tank.setFluid(FluidStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("input", input.writeToNBT(new CompoundTag()));
        tag.put("output", output.writeToNBT(new CompoundTag()));
        tag.put("energy", energy.serializeNBT());
        tag.put("container", container.createTag());
        tag.putInt("progress", progress);
        tag.putInt("salt_count", saltCount);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        input.readFromNBT(tag.getCompound("input"));
        output.readFromNBT(tag.getCompound("output"));
        energy.deserializeNBT(tag.get("energy"));
        if (tag.contains("container")) {
            container.fromTag(tag.getList("container", 10));
        }
        progress = tag.getInt("progress");
        saltCount = tag.getInt("salt_count");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null || side == getBlockState().getValue(DistillerBlock.FACING).getCounterClockWise()) {
                return inputCapability.cast();
            }
            if (side == getBlockState().getValue(DistillerBlock.FACING).getClockWise()) {
                return outputCapability.cast();
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCapability.invalidate();
        outputCapability.invalidate();
        energyCapability.invalidate();
    }
}
