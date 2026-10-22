package immersivetech.port;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public record MultiblockDefinition(String id, int width, int height, int length,
        List<BlockPos> requiredOffsets) {
    public boolean contains(BlockPos origin, BlockPos target, Direction facing, boolean mirrored) {
        BlockPos relative = target.subtract(origin);
        BlockPos local = switch (facing) {
            case NORTH -> relative;
            case SOUTH -> new BlockPos(-relative.getX(), relative.getY(), -relative.getZ());
            case EAST -> new BlockPos(relative.getZ(), relative.getY(), -relative.getX());
            case WEST -> new BlockPos(-relative.getZ(), relative.getY(), relative.getX());
            default -> relative;
        };
        if (mirrored) local = new BlockPos(-local.getX(), local.getY(), local.getZ());
        return local.getX() >= 0 && local.getX() < width
                && local.getY() >= 0 && local.getY() < height
                && local.getZ() >= 0 && local.getZ() < length;
    }

    public boolean isComplete(Level level, BlockPos origin, Direction facing, boolean mirrored) {
        return requiredOffsets.stream().allMatch(offset -> {
            BlockPos transformed = transform(origin, offset, facing, mirrored);
            return !level.isEmptyBlock(transformed);
        });
    }

    public static BlockPos transform(BlockPos origin, BlockPos offset, Direction facing, boolean mirrored) {
        BlockPos local = mirrored ? new BlockPos(-offset.getX(), offset.getY(), offset.getZ()) : offset;
        return origin.offset(switch (facing) {
            case NORTH -> local;
            case SOUTH -> new BlockPos(-local.getX(), local.getY(), -local.getZ());
            case EAST -> new BlockPos(-local.getZ(), local.getY(), local.getX());
            case WEST -> new BlockPos(local.getZ(), local.getY(), -local.getX());
            default -> local;
        });
    }
}
