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
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public final class AlternatorBlockEntity extends BlockEntity implements FormableMultiblock {
    private final EnergyStorage energy = new EnergyStorage(ModConfig.ALTERNATOR_STORAGE.get(),
            ModConfig.ALTERNATOR_FLUX_PER_TICK.get());
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private boolean formed;

    public AlternatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.ALTERNATOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    public static void tick(Level level, BlockPos pos, BlockState state, AlternatorBlockEntity alternator) {
        if (level.isClientSide || !alternator.formed) return;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor == null) continue;
            neighbor.getCapability(ModCapabilities.MECHANICAL_ENERGY, direction.getOpposite()).ifPresent(mechanical -> {
                int produced = mechanical.power() / ModConfig.ALTERNATOR_FLUX_MODIFIER.get();
                if (produced > 0) {
                    alternator.energy.receiveEnergy(Math.min(produced,
                            ModConfig.ALTERNATOR_FLUX_PER_TICK.get()), false);
                    alternator.setChanged();
                }
            });
        }
    }

    public void displayStatus(net.minecraft.world.entity.player.Player player) {
        String status = String.format("Formed: %s | Energy: %d/%d FE",
                formed, energy.getEnergyStored(), energy.getMaxEnergyStored());
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(status), true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energy.serializeNBT());
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.deserializeNBT(tag.get("energy"));
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY) return energyCapability.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }
}
