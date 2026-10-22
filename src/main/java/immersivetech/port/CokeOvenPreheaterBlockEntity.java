package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public final class CokeOvenPreheaterBlockEntity extends BlockEntity {
    private final EnergyStorage energy = new EnergyStorage(8_000, 128);
    private final LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> energy);
    private boolean active;

    public CokeOvenPreheaterBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.PREHEATER_BLOCK_ENTITY.get(), pos, state);
    }

    /** Consumes the audited 32 FE/t cost when a connected oven requests a speedup. */
    public int doSpeedup() {
        boolean powered = energy.extractEnergy(32, true) == 32;
        if (powered) energy.extractEnergy(32, false);
        if (active != powered) {
            active = powered;
            setChanged();
        }
        return powered ? 1 : 0;
    }

    public boolean isActive() { return active; }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("energy", energy.serializeNBT());
        tag.putBoolean("active", active);
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        energy.deserializeNBT(tag.get("energy"));
        active = tag.getBoolean("active");
    }
    @Nonnull @Override public <T> LazyOptional<T> getCapability(
            Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY
                && (side == null || side == Direction.UP)) return energyCapability.cast();
        return super.getCapability(capability, side);
    }
    @Override public void invalidateCaps() {
        super.invalidateCaps();
        energyCapability.invalidate();
    }
}
