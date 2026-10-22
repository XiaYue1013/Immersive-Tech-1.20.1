package immersivetech.port;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AdvancedCokeOvenMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public AdvancedCokeOvenMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(4), new SimpleContainerData(3));
    }

    public AdvancedCokeOvenMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModRegistries.ADVANCED_COKE_OVEN_MENU.get(), containerId);
        checkContainerSize(container, 4);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;

        // Container slots
        this.addSlot(new Slot(container, 0, 30, 35));
        this.addSlot(new Slot(container, 1, 85, 35));
        this.addSlot(new Slot(container, 2, 152, 17));
        this.addSlot(new Slot(container, 3, 152, 53));

        // Player Inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        this.addDataSlots(data);
    }

    public int getCreosoteAmount() { return data.get(0); }
    public int getProgress() { return data.get(1); }
    public int getMaxProgress() { return data.get(2); }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            itemstack = stackInSlot.copy();
            if (index < 4) {
                if (!this.moveItemStackTo(stackInSlot, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 4, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
