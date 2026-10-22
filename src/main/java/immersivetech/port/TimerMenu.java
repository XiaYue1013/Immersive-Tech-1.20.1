package immersivetech.port;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TimerMenu extends AbstractContainerMenu {
    private final ContainerData data;
    private final BlockPos pos;

    public TimerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainerData(1), extraData.readBlockPos());
    }

    public TimerMenu(int containerId, Inventory playerInventory, ContainerData data, BlockPos pos) {
        super(ModRegistries.TIMER_MENU.get(), containerId);
        checkContainerDataCount(data, 1);
        this.data = data;
        this.pos = pos;

        // Player Inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
            }
        }
        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 143));
        }

        this.addDataSlots(data);
    }

    public int getIntervalTicks() { return data.get(0); }
    public BlockPos getPos() { return pos; }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) <= 64;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
