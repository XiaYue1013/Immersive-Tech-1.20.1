package immersivetech.port;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SolarReflectorBlockEntity extends BlockEntity implements FormableMultiblock {
    private final MultiblockPartTracker partTracker = new MultiblockPartTracker();
    private boolean sunlit;
    private boolean formed;

    public SolarReflectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.SOLAR_REFLECTOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override public boolean isFormed() { return formed; }
    @Override public void setFormed(boolean formed) { this.formed = formed; setChanged(); }
    @Override public MultiblockPartTracker getPartTracker() { return partTracker; }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarReflectorBlockEntity reflector) {
        if (level.isClientSide) return;
        boolean next = reflector.formed && level.isDay() && level.canSeeSky(pos.above());
        if (next != reflector.sunlit) {
            reflector.sunlit = next;
            reflector.setChanged();
        }
    }

    public boolean isSunlit() {
        return formed && sunlit;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("sunlit", sunlit);
        tag.putBoolean("formed", formed);
        partTracker.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        sunlit = tag.getBoolean("sunlit");
        formed = tag.getBoolean("formed");
        partTracker.load(tag);
    }
}
