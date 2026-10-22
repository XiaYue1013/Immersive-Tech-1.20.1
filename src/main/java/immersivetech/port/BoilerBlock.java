package immersivetech.port;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BoilerBlock extends BaseEntityBlock {
    public BoilerBlock(Properties properties) { super(properties); }
    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilerBlockEntity(pos, state);
    }
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BoilerBlockEntity boiler) {
            boiler.tryHandleItem(player, hand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BoilerBlockEntity boiler) {
            boiler.dropContents();
            boiler.disassemble(level);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
    @Override @Nullable public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.BOILER_BLOCK_ENTITY.get()
                ? (lvl, pos, blockState, blockEntity) -> BoilerBlockEntity.tick(lvl, pos, blockState,
                (BoilerBlockEntity) blockEntity) : null;
    }
}
