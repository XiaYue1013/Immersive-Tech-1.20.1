package immersivetech.port;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class FormationHammerItem extends Item {
    public FormationHammerItem(Properties properties) {
        super(properties.durability(256));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockPos origin = context.getClickedPos();
        if (level.getBlockEntity(origin) instanceof MultiblockPartBlockEntity part && !part.getMasterPos().equals(BlockPos.ZERO)) {
            origin = part.getMasterPos();
        }
        BlockState state = level.getBlockState(origin);
        Player player = context.getPlayer();

        if (player != null && player.isShiftKeyDown()) {
            if (level.getBlockEntity(origin) instanceof FormableMultiblock machine && machine.isFormed()) {
                machine.disassemble(level);
                player.displayClientMessage(Component.translatable("chat.immersivetech.multiblock_disassembled"), true);
                level.levelEvent(2001, origin, Block.getId(state));
                context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        var definition = ModMultiblocks.forTrigger(state);
        if (definition != null) {
            if (level.getBlockEntity(origin) instanceof FormableMultiblock machine) {
                if (machine.isFormed()) {
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("chat.immersivetech.multiblock_already_formed"), true);
                    }
                    return InteractionResult.PASS;
                }

                Direction clickDirection = context.getHorizontalDirection();
                Direction matchedDirection = null;
                if (definition.isComplete(level, origin, clickDirection, false)) {
                    matchedDirection = clickDirection;
                } else {
                    for (Direction dir : Direction.Plane.HORIZONTAL) {
                        if (dir != clickDirection && definition.isComplete(level, origin, dir, false)) {
                            matchedDirection = dir;
                            break;
                        }
                    }
                }

                if (matchedDirection != null) {
                    List<BlockPos> parts = new ArrayList<>();
                    for (BlockPos offset : definition.requiredOffsets()) {
                        BlockPos pos = MultiblockDefinition.transform(origin, offset, matchedDirection, false);
                        if (!pos.equals(origin)) {
                            BlockState oldState = level.getBlockState(pos);
                            level.setBlock(pos, ModRegistries.MULTIBLOCK_PART.get().defaultBlockState(), 3);
                            if (level.getBlockEntity(pos) instanceof MultiblockPartBlockEntity part) {
                                part.setMaster(origin, oldState);
                            }
                            parts.add(pos);
                        }
                    }
                    machine.setFormed(true);
                    if (machine.getPartTracker() != null) {
                        machine.getPartTracker().setPositions(parts);
                    }
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("chat.immersivetech.multiblock_formed"), true);
                    }
                    level.levelEvent(2001, origin, Block.getId(state));
                    if (player != null) {
                        context.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                    }
                    return InteractionResult.CONSUME;
                } else {
                    if (player != null) {
                        player.displayClientMessage(Component.translatable("chat.immersivetech.multiblock_incomplete"), true);
                    }
                    return InteractionResult.PASS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
