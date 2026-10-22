package immersivetech.port;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class TimerBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MIN_TARGET = 10;
    private static final int MAX_TARGET = 600;
    private int target = 40;
    private int ticks;
    private int output;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? target : 0;
        }
        @Override
        public void set(int index, int value) {
            if (index == 0) target = value;
        }
        @Override
        public int getCount() { return 1; }
    };

    public TimerBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.TIMER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.immersivetech.timer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TimerMenu(id, inv, dataAccess, worldPosition);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TimerBlockEntity timer) {
        if (level.isClientSide) return;
        var facing = state.getValue(TimerBlock.FACING);
        if (level.hasSignal(pos.relative(facing.getOpposite()), facing)) {
            if (timer.output != 0) { timer.output = 0; timer.setChanged(); }
            return;
        }
        if (++timer.ticks >= timer.target) {
            timer.ticks = 0;
            timer.output = 1;
            timer.setChanged();
            level.updateNeighborsAt(pos, state.getBlock());
        } else if (timer.output != 0) {
            timer.output = 0;
            timer.setChanged();
            level.updateNeighborsAt(pos, state.getBlock());
        }
    }

    public void increase() {
        target = Math.min(MAX_TARGET, target < 100 ? target + 10 : target < 200 ? target + 20 : target + 40);
        ticks = 0; setChanged();
    }

    public void decrease() {
        target = Math.max(MIN_TARGET, target <= 100 ? target - 10 : target <= 200 ? target - 20 : target - 40);
        ticks = 0; setChanged();
    }

    public int output() { return output; }
    public int target() { return target; }
    public boolean isOutputSide(net.minecraft.core.Direction side) {
        return side == getBlockState().getValue(TimerBlock.FACING);
    }

    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("target", target); tag.putInt("ticks", ticks); tag.putInt("output", output);
    }

    @Override public void load(CompoundTag tag) {
        super.load(tag);
        target = Math.max(MIN_TARGET, Math.min(MAX_TARGET, tag.getInt("target")));
        ticks = tag.getInt("ticks"); output = tag.getInt("output");
    }
}
