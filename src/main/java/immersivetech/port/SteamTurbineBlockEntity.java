package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public final class SteamTurbineBlockEntity extends BlockEntity implements FormableMultiblock {
    private final FluidTank steam = new FluidTank(24_000,
            stack -> stack.getFluid() == ModFluids.STEAM.get());
    private final LazyOptional<IFluidHandler> steamCapability = LazyOptional.of(() -> steam);
    private final LazyOptional<MechanicalEnergy> mechanicalCapability =
            LazyOptional.of(this::energy);
    private final MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private boolean active;
    private boolean formed;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.STEAM_TURBINE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    public MechanicalEnergyAnimation getAnimation() { return animation; }

    public void clientTick() {
        animation.update(active);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity turbine) {
        if (level.isClientSide || !turbine.formed) return;
        int burn = Math.max(1, (1000 / 50 / 2) * ModConfig.TURBINE_BURN_TIME_MODIFIER.get());
        boolean next = turbine.steam.getFluidAmount() >= burn;
        if (next) turbine.steam.drain(burn, IFluidHandler.FluidAction.EXECUTE);
        if (next != turbine.active) {
            turbine.active = next;
            turbine.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public MechanicalEnergy energy() {
        return active ? new MechanicalEnergy(ModConfig.TURBINE_MAX_TORQUE.get(),
                ModConfig.TURBINE_MAX_SPEED.get()) : MechanicalEnergy.NONE;
    }

    public void tryHandleItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ModFluids.STEAM_BUCKET.get())
                && steam.fill(new FluidStack(ModFluids.STEAM.get(), 1000), IFluidHandler.FluidAction.SIMULATE) == 1000) {
            steam.fill(new FluidStack(ModFluids.STEAM.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BUCKET));
            }
            setChanged();
            return;
        }
        String status = String.format("Formed: %s | Steam: %d/%d mB | Active: %s",
                formed, steam.getFluidAmount(), steam.getCapacity(), active);
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(status), true);
    }

    public void dropContents() {
        if (level == null || level.isClientSide) return;
        MachineDrops.dropBuckets(level, worldPosition, steam, ModFluids.STEAM_BUCKET.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("steam", steam.writeToNBT(new CompoundTag()));
        tag.putBoolean("active", active);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        steam.readFromNBT(tag.getCompound("steam"));
        active = tag.getBoolean("active");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putBoolean("active", active);
        tag.putBoolean("formed", formed);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.active = tag.getBoolean("active");
            this.formed = tag.getBoolean("formed");
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) return steamCapability.cast();
        if (capability == ModCapabilities.MECHANICAL_ENERGY) {
            return mechanicalCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        steamCapability.invalidate();
        mechanicalCapability.invalidate();
    }
}
