package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BucketItem;
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

public final class SolarTowerBlockEntity extends BlockEntity implements FormableMultiblock, net.minecraft.world.MenuProvider {
    private final FluidTank input = new FluidTank(32_000, stack ->
            stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER
                    || stack.getFluid() == ModFluids.DISTILLED_WATER.get());
    private final FluidTank output = new FluidTank(32_000,
            stack -> stack.getFluid() == ModFluids.STEAM.get());
    private final LazyOptional<IFluidHandler> inputCapability = LazyOptional.of(() -> input);
    private final LazyOptional<IFluidHandler> outputCapability = LazyOptional.of(() -> output);
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private final net.minecraft.world.SimpleContainer container = new net.minecraft.world.SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            SolarTowerBlockEntity.this.setChanged();
        }
    };
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> input.getFluidAmount();
                case 1 -> output.getFluidAmount();
                case 2 -> reflectorCount;
                case 3 -> reflectorFlags;
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
    private int reflectorCount;
    private int reflectorFlags;
    private boolean formed;

    public SolarTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SOLAR_TOWER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.immersivetech.solar_tower");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new SolarTowerMenu(id, inv, container, dataAccess);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarTowerBlockEntity tower) {
        if (level.isClientSide) return;

        // Container bucket operations for water (slot 0 -> slot 1)
        ItemStack waterIn = tower.container.getItem(0);
        if (!waterIn.isEmpty() && waterIn.getItem() instanceof BucketItem bucket
                && (bucket.getFluid() == net.minecraft.world.level.material.Fluids.WATER || bucket.getFluid() == ModFluids.DISTILLED_WATER.get())) {
            FluidStack wStack = new FluidStack(bucket.getFluid(), 1000);
            if (tower.input.fill(wStack, IFluidHandler.FluidAction.SIMULATE) == 1000) {
                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                ItemStack waterOut = tower.container.getItem(1);
                if (waterOut.isEmpty()) {
                    tower.input.fill(wStack, IFluidHandler.FluidAction.EXECUTE);
                    waterIn.shrink(1);
                    tower.container.setItem(1, emptyBucket);
                    tower.setChanged();
                } else if (ItemStack.isSameItemSameTags(waterOut, emptyBucket) && waterOut.getCount() < waterOut.getMaxStackSize()) {
                    tower.input.fill(wStack, IFluidHandler.FluidAction.EXECUTE);
                    waterIn.shrink(1);
                    waterOut.grow(1);
                    tower.setChanged();
                }
            }
        }
        // Container bucket operations for steam (slot 2 -> slot 3)
        ItemStack steamIn = tower.container.getItem(2);
        if (!steamIn.isEmpty() && steamIn.is(Items.BUCKET) && tower.output.getFluidAmount() >= 1000) {
            ItemStack steamBucket = new ItemStack(ModFluids.STEAM_BUCKET.get());
            ItemStack steamOut = tower.container.getItem(3);
            if (steamOut.isEmpty()) {
                tower.output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                steamIn.shrink(1);
                tower.container.setItem(3, steamBucket);
                tower.setChanged();
            } else if (ItemStack.isSameItemSameTags(steamOut, steamBucket) && steamOut.getCount() < steamOut.getMaxStackSize()) {
                tower.output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                steamIn.shrink(1);
                steamOut.grow(1);
                tower.setChanged();
            }
        }

        if (!tower.formed || !level.isDay() || !level.canSeeSky(pos.above())) return;
        tower.reflectorCount = tower.countReflectors(level);
        if (tower.reflectorCount == 0 || tower.input.getFluid().isEmpty()) return;
        SolarTowerRecipe recipe = tower.findRecipe(level);
        if (recipe == null || tower.input.getFluidAmount() < recipe.inputAmount()
                || tower.output.getFluidAmount() + recipe.outputAmount() > tower.output.getCapacity()) return;
        tower.progress += Math.max(1, tower.reflectorCount);
        if (tower.progress >= recipe.time() * ModConfig.SOLAR_TIME_MODIFIER.get()) {
            tower.progress = 0;
            tower.input.drain(recipe.inputAmount(), IFluidHandler.FluidAction.EXECUTE);
            tower.output.fill(new FluidStack(ModFluids.STEAM.get(), recipe.outputAmount()),
                    IFluidHandler.FluidAction.EXECUTE);
            tower.setChanged();
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        MachineDrops.dropBuckets(level, worldPosition, input,
                input.getFluid().getFluid() == ModFluids.DISTILLED_WATER.get()
                        ? ModFluids.DISTILLED_WATER_BUCKET.get() : Items.WATER_BUCKET);
        MachineDrops.dropBuckets(level, worldPosition, output, ModFluids.STEAM_BUCKET.get());
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + .5, worldPosition.getY() + .5,
                        worldPosition.getZ() + .5, stack));
            }
        }
    }

    private SolarTowerRecipe findRecipe(Level level) {
        FluidStack contents = input.getFluid();
        if (contents.isEmpty()) return null;
        for (SolarTowerRecipe recipe : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.SOLAR_TOWER_TYPE.get())) {
            if (recipe.input() == contents.getFluid()) return recipe;
        }
        return null;
    }

    private int countReflectors(Level level) {
        int count = 0;
        int flags = 0;
        Direction[] directions = new Direction[] {Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST};
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            for (int distance = ModConfig.SOLAR_MIN_RANGE.get();
                    distance <= ModConfig.SOLAR_MAX_RANGE.get(); distance++) {
                BlockPos candidate = worldPosition.relative(direction, distance);
                if (!level.isEmptyBlock(candidate)) {
                    if (level.getBlockEntity(candidate) instanceof SolarReflectorBlockEntity reflector
                            && reflector.isSunlit()) {
                        count++;
                        flags |= (1 << i);
                    }
                    break;
                }
            }
        }
        this.reflectorFlags = flags;
        return count;
    }

    public void tryHandleItem(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BucketItem bucket) {
            net.minecraft.world.level.material.Fluid fluid = bucket.getFluid();
            if ((fluid == net.minecraft.world.level.material.Fluids.WATER || fluid == ModFluids.DISTILLED_WATER.get())
                    && input.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
                input.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                }
                setChanged();
                return;
            }
        }
        if (stack.is(Items.BUCKET) && output.getFluidAmount() >= 1000) {
            output.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ItemStack steamBucket = new ItemStack(ModFluids.STEAM_BUCKET.get());
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
        String status = String.format("Formed: %s | Water: %d/%d mB | Steam: %d/%d mB | Reflectors: %d",
                formed, input.getFluidAmount(), input.getCapacity(),
                output.getFluidAmount(), output.getCapacity(), reflectorCount);
        player.displayClientMessage(Component.literal(status), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("input", input.writeToNBT(new CompoundTag()));
        tag.put("output", output.writeToNBT(new CompoundTag()));
        tag.put("container", container.createTag());
        tag.putInt("progress", progress);
        tag.putInt("reflectors", reflectorCount);
        tag.putInt("reflector_flags", reflectorFlags);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        input.readFromNBT(tag.getCompound("input"));
        output.readFromNBT(tag.getCompound("output"));
        if (tag.contains("container")) {
            container.fromTag(tag.getList("container", 10));
        }
        progress = tag.getInt("progress");
        reflectorCount = tag.getInt("reflectors");
        reflectorFlags = tag.getInt("reflector_flags");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null || side == Direction.WEST) return inputCapability.cast();
            if (side == Direction.EAST) return outputCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCapability.invalidate();
        outputCapability.invalidate();
    }
}
