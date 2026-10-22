package immersivetech.port;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public final class SolarReflectorBlock extends BaseEntityBlock {
    public SolarReflectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarReflectorBlockEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SolarReflectorBlockEntity reflector) {
            reflector.disassemble(level);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SolarReflectorBlockEntity reflector) {
            String status = String.format("Formed: %s | Sunlit: %s", reflector.isFormed(), reflector.isSunlit());
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(status), true);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.SOLAR_REFLECTOR_BLOCK_ENTITY.get()
                ? (lvl, pos, blockState, blockEntity) -> SolarReflectorBlockEntity.tick(lvl,
                pos, blockState, (SolarReflectorBlockEntity) blockEntity)
                : null;
    }
}
