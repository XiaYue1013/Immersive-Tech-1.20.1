package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public final class BoilerBlockEntity extends BlockEntity implements FormableMultiblock, net.minecraft.world.MenuProvider {
    private final FluidTank fuel = new FluidTank(12_000, BoilerBlockEntity::isFuel);
    private final FluidTank water = new FluidTank(24_000, stack ->
            stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER
                    || stack.getFluid() == ModFluids.DISTILLED_WATER.get());
    private final FluidTank steam = new FluidTank(24_000,
            stack -> stack.getFluid() == ModFluids.STEAM.get());
    private final LazyOptional<IFluidHandler> fuelCap = LazyOptional.of(() -> fuel);
    private final LazyOptional<IFluidHandler> waterCap = LazyOptional.of(() -> water);
    private final LazyOptional<IFluidHandler> steamCap = LazyOptional.of(() -> steam);
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private final net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(6) {
        @Override
        public void setChanged() {
            super.setChanged();
            BoilerBlockEntity.this.setChanged();
        }
    };
    private final net.minecraft.world.inventory.ContainerData dataAccess = new net.minecraft.world.inventory.ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuel.getFluidAmount();
                case 1 -> water.getFluidAmount();
                case 2 -> steam.getFluidAmount();
                case 3 -> progress;
                case 4 -> 100;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            if (index == 3) progress = value;
        }
        @Override
        public int getCount() { return 5; }
    };
    private int progress;
    private boolean formed;

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.BOILER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.immersivetech.boiler");
    }

    @Nullable
    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
        return new BoilerMenu(id, inv, container, dataAccess);
    }

    private static boolean isFuel(FluidStack stack) {
        String name = stack.getFluid().builtInRegistryHolder().key().location().getPath();
        return name.equals("diesel") || name.equals("biodiesel") || name.equals("fuel");
    }

    private static int burnTime(FluidStack stack) {
        String name = stack.getFluid().builtInRegistryHolder().key().location().getPath();
        return name.equals("biodiesel") ? 125 : name.equals("diesel") ? 175 : name.equals("fuel") ? 375 : 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BoilerBlockEntity boiler) {
        if (level.isClientSide) return;

        // Container bucket operations for fuel (slot 0 -> slot 1)
        ItemStack fuelIn = boiler.container.getItem(0);
        if (!fuelIn.isEmpty() && fuelIn.getItem() instanceof net.minecraft.world.item.BucketItem bucket) {
            FluidStack fStack = new FluidStack(bucket.getFluid(), 1000);
            if (isFuel(fStack) && boiler.fuel.fill(fStack, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                ItemStack fuelOut = boiler.container.getItem(1);
                if (fuelOut.isEmpty()) {
                    boiler.fuel.fill(fStack, IFluidHandler.FluidAction.EXECUTE);
                    fuelIn.shrink(1);
                    boiler.container.setItem(1, emptyBucket);
                    boiler.setChanged();
                } else if (ItemStack.isSameItemSameTags(fuelOut, emptyBucket) && fuelOut.getCount() < fuelOut.getMaxStackSize()) {
                    boiler.fuel.fill(fStack, IFluidHandler.FluidAction.EXECUTE);
                    fuelIn.shrink(1);
                    fuelOut.grow(1);
                    boiler.setChanged();
                }
            }
        }
        // Container bucket operations for water (slot 2 -> slot 3)
        ItemStack waterIn = boiler.container.getItem(2);
        if (!waterIn.isEmpty() && waterIn.getItem() instanceof net.minecraft.world.item.BucketItem bucket
                && (bucket.getFluid() == net.minecraft.world.level.material.Fluids.WATER || bucket.getFluid() == ModFluids.DISTILLED_WATER.get())) {
            FluidStack wStack = new FluidStack(bucket.getFluid(), 1000);
            if (boiler.water.fill(wStack, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                ItemStack emptyBucket = new ItemStack(net.minecraft.world.item.Items.BUCKET);
                ItemStack waterOut = boiler.container.getItem(3);
                if (waterOut.isEmpty()) {
                    boiler.water.fill(wStack, IFluidHandler.FluidAction.EXECUTE);
                    waterIn.shrink(1);
                    boiler.container.setItem(3, emptyBucket);
                    boiler.setChanged();
                } else if (ItemStack.isSameItemSameTags(waterOut, emptyBucket) && waterOut.getCount() < waterOut.getMaxStackSize()) {
                    boiler.water.fill(wStack, IFluidHandler.FluidAction.EXECUTE);
                    waterIn.shrink(1);
                    waterOut.grow(1);
                    boiler.setChanged();
                }
            }
        }
        // Container bucket operations for steam (slot 4 -> slot 5)
        ItemStack steamIn = boiler.container.getItem(4);
        if (!steamIn.isEmpty() && steamIn.is(net.minecraft.world.item.Items.BUCKET) && boiler.steam.getFluidAmount() >= 1000) {
            ItemStack steamBucket = new ItemStack(ModFluids.STEAM_BUCKET.get());
            ItemStack steamOut = boiler.container.getItem(5);
            if (steamOut.isEmpty()) {
                boiler.steam.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                steamIn.shrink(1);
                boiler.container.setItem(5, steamBucket);
                boiler.setChanged();
            } else if (ItemStack.isSameItemSameTags(steamOut, steamBucket) && steamOut.getCount() < steamOut.getMaxStackSize()) {
                boiler.steam.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                steamIn.shrink(1);
                steamOut.grow(1);
                boiler.setChanged();
            }
        }

        if (!boiler.formed || boiler.fuel.getFluid().isEmpty() || boiler.water.getFluid().isEmpty()) return;
        int burn = burnTime(boiler.fuel.getFluid());
        int consumed = Math.max(1, (1000 / Math.max(1, burn))
                * (4 * ModConfig.BOILER_BURN_TIME_MODIFIER.get()));
        BoilerRecipe recipe = boiler.findRecipe(level);
        if (recipe == null || boiler.fuel.getFluidAmount() < consumed
                || boiler.water.getFluidAmount() < recipe.inputAmount()
                || boiler.steam.getFluidAmount() + recipe.outputAmount() > boiler.steam.getCapacity()) return;
        boiler.progress++;
        if (boiler.progress >= recipe.time() * ModConfig.BOILER_TIME_MODIFIER.get()) {
            boiler.progress = 0;
            boiler.fuel.drain(consumed, IFluidHandler.FluidAction.EXECUTE);
            boiler.water.drain(recipe.inputAmount(), IFluidHandler.FluidAction.EXECUTE);
            boiler.steam.fill(new FluidStack(ModFluids.STEAM.get(), recipe.outputAmount()),
                    IFluidHandler.FluidAction.EXECUTE);
            boiler.setChanged();
        }
    }

    private BoilerRecipe findRecipe(Level level) {
        FluidStack contents = water.getFluid();
        if (contents.isEmpty()) return null;
        for (BoilerRecipe recipe : level.getRecipeManager().getAllRecipesFor(ModRecipes.BOILER_TYPE.get())) {
            if (recipe.input() == contents.getFluid()) return recipe;
        }
        return null;
    }

    public void tryHandleItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof net.minecraft.world.item.BucketItem bucket) {
            net.minecraft.world.level.material.Fluid fluid = bucket.getFluid();
            if ((fluid == net.minecraft.world.level.material.Fluids.WATER || fluid == ModFluids.DISTILLED_WATER.get())
                    && water.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                water.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
                }
                setChanged();
                return;
            }
            if (isFuel(new FluidStack(fluid, 1000))
                    && fuel.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                fuel.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
                }
                setChanged();
                return;
            }
        }
        if (stack.is(net.minecraft.world.item.Items.BUCKET) && steam.getFluidAmount() >= 1000) {
            steam.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                net.minecraft.world.item.ItemStack steamBucket = new net.minecraft.world.item.ItemStack(ModFluids.STEAM_BUCKET.get());
                if (stack.isEmpty()) {
                    player.setItemInHand(hand, steamBucket);
                } else if (!player.getInventory().add(steamBucket)) {
                    player.drop(steamBucket, false);
                }
            }
            setChanged();
            return;
        }
        if (!player.isShiftKeyDown() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, this, worldPosition);
            return;
        }
        String status = String.format("Formed: %s | Fuel: %d/%d mB | Water: %d/%d mB | Steam: %d/%d mB",
                formed, fuel.getFluidAmount(), fuel.getCapacity(),
                water.getFluidAmount(), water.getCapacity(),
                steam.getFluidAmount(), steam.getCapacity());
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(status), true);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        MachineDrops.dropBuckets(level, worldPosition, water,
                water.getFluid().getFluid() == ModFluids.DISTILLED_WATER.get()
                        ? ModFluids.DISTILLED_WATER_BUCKET.get() : net.minecraft.world.item.Items.WATER_BUCKET);
        MachineDrops.dropBuckets(level, worldPosition, steam, ModFluids.STEAM_BUCKET.get());
        fuel.setFluid(FluidStack.EMPTY);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .5,
                        worldPosition.getZ() + .5, stack));
            }
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("fuel", fuel.writeToNBT(new CompoundTag()));
        tag.put("water", water.writeToNBT(new CompoundTag()));
        tag.put("steam", steam.writeToNBT(new CompoundTag()));
        tag.put("container", container.createTag());
        tag.putInt("progress", progress);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        fuel.readFromNBT(tag.getCompound("fuel"));
        water.readFromNBT(tag.getCompound("water"));
        steam.readFromNBT(tag.getCompound("steam"));
        if (tag.contains("container")) {
            container.fromTag(tag.getList("container", 10));
        }
        progress = tag.getInt("progress");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }
    @Nonnull @Override public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == Direction.DOWN) return steamCap.cast();
            if (side == Direction.UP) return fuelCap.cast();
            return waterCap.cast();
        }
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        fuelCap.invalidate(); waterCap.invalidate(); steamCap.invalidate();
    }
}
