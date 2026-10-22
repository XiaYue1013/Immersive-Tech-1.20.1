package immersivetech.port;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public final class AdvancedCokeOvenBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AdvancedCokeOvenBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, net.minecraft.core.Direction.NORTH));
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
        return new AdvancedCokeOvenBlockEntity(pos, state);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AdvancedCokeOvenBlockEntity oven) {
            oven.dropContents();
            oven.disassemble(level);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof AdvancedCokeOvenBlockEntity oven)
                || !oven.isFormed()) return InteractionResult.PASS;

        if (!player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, oven, pos);
            return InteractionResult.CONSUME;
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.isEmpty()) {
            ItemStack remainder = oven.insertInput(held.copyWithCount(1));
            if (remainder.isEmpty() && !player.getAbilities().instabuild) held.shrink(1);
        } else {
            ItemStack output = oven.extractOutput();
            if (!output.isEmpty() && !player.getInventory().add(output)) {
                player.drop(output, false);
            }
        }
        return InteractionResult.CONSUME;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModRegistries.ADVANCED_COKE_OVEN_BLOCK_ENTITY.get()
                ? (level.isClientSide ? null : (l, p, s, be) ->
                        AdvancedCokeOvenBlockEntity.tick(l, p, s, (AdvancedCokeOvenBlockEntity) be))
                : null;
    }
}
