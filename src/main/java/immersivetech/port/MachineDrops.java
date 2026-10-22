package immersivetech.port;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.templates.FluidTank;

final class MachineDrops {
    private MachineDrops() {}

    static void dropBuckets(Level level, BlockPos pos, FluidTank tank, Item bucket) {
        int count = tank.getFluidAmount() / 1000;
        for (int i = 0; i < count; i++) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + .5, pos.getY() + .5,
                    pos.getZ() + .5, new ItemStack(bucket)));
        }
        tank.setFluid(net.minecraftforge.fluids.FluidStack.EMPTY);
    }
}
