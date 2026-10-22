package immersivetech.port;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public final class SteamTurbineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SteamTurbineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamTurbineBlockEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SteamTurbineBlockEntity turbine) {
            turbine.dropContents();
            turbine.disassemble(level);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SteamTurbineBlockEntity turbine) {
            turbine.tryHandleItem(player, hand);
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.STEAM_TURBINE_BLOCK_ENTITY.get()
                ? (lvl, p, s, be) -> {
                    if (be instanceof SteamTurbineBlockEntity turbine) {
                        if (lvl.isClientSide) {
                            turbine.clientTick();
                        } else {
                            SteamTurbineBlockEntity.tick(lvl, p, s, turbine);
                        }
                    }
                }
                : null;
    }
}
