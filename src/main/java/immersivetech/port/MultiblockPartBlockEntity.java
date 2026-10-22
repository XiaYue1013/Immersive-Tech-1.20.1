package immersivetech.port;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public final class MultiblockPartBlockEntity extends BlockEntity {
    private BlockPos masterPos = BlockPos.ZERO;
    private BlockState originalState = Blocks.AIR.defaultBlockState();

    public MultiblockPartBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistries.MULTIBLOCK_PART_BLOCK_ENTITY.get(), pos, state);
    }

    public void setMaster(BlockPos masterPos, BlockState originalState) {
        this.masterPos = masterPos;
        this.originalState = originalState;
        setChanged();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public BlockState getOriginalState() {
        return originalState;
    }

    public ItemStack getOriginalStack() {
        return new ItemStack(originalState.getBlock().asItem());
    }

    @Nullable
    public BlockEntity getMaster() {
        return level != null && !masterPos.equals(BlockPos.ZERO) ? level.getBlockEntity(masterPos) : null;
    }

    public InteractionResult forwardUse(Player player, InteractionHand hand, BlockHitResult hit) {
        if (level == null || masterPos.equals(BlockPos.ZERO)) return InteractionResult.PASS;
        BlockState masterState = level.getBlockState(masterPos);
        return masterState.use(level, player, hand, hit.withPosition(masterPos));
    }

    public void disassembleStructure() {
        if (level == null || level.isClientSide || masterPos.equals(BlockPos.ZERO)) return;
        BlockEntity masterBe = level.getBlockEntity(masterPos);
        if (masterBe instanceof FormableMultiblock machine) {
            machine.disassemble(level);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("masterPos", masterPos.asLong());
        tag.put("originalState", NbtUtils.writeBlockState(originalState));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        masterPos = BlockPos.of(tag.getLong("masterPos"));
        if (tag.contains("originalState")) {
            originalState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("originalState"));
        }
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        BlockEntity master = getMaster();
        if (master != null) {
            return master.getCapability(capability, side);
        }
        return super.getCapability(capability, side);
    }
}
