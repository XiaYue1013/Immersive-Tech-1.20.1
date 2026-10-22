package immersivetech.port;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class MultiblockPartTracker {
    private final List<BlockPos> partPositions = new ArrayList<>();

    public List<BlockPos> getPositions() {
        return partPositions;
    }

    public void setPositions(List<BlockPos> positions) {
        partPositions.clear();
        partPositions.addAll(positions);
    }

    public void disassemble(Level level) {
        for (BlockPos pos : partPositions) {
            if (level.getBlockState(pos).is(ModRegistries.MULTIBLOCK_PART.get())) {
                if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity part) {
                    BlockState orig = part.getOriginalState();
                    level.setBlock(pos, orig, 3);
                }
            }
        }
        partPositions.clear();
    }

    public void save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (BlockPos p : partPositions) {
            list.add(LongTag.valueOf(p.asLong()));
        }
        tag.put("partPositions", list);
    }

    public void load(CompoundTag tag) {
        partPositions.clear();
        if (tag.contains("partPositions", Tag.TAG_LIST)) {
            ListTag list = tag.getList("partPositions", Tag.TAG_LONG);
            for (int i = 0; i < list.size(); i++) {
                partPositions.add(BlockPos.of(((LongTag) list.get(i)).getAsLong()));
            }
        }
    }
}
