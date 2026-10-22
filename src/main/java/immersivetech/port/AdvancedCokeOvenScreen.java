package immersivetech.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedCokeOvenScreen extends AbstractContainerScreen<AdvancedCokeOvenMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/gui/gui_coke_oven_advanced.png");

    public AdvancedCokeOvenScreen(AdvancedCokeOvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Progress bar at (x + 59, y + 37)
        int maxProgress = this.menu.getMaxProgress();
        if (maxProgress > 0) {
            int progress = this.menu.getProgress();
            int h = (int) (12 * Math.min(1.0, (double) progress / maxProgress));
            if (h > 0) {
                // Progress flame/arrow
                graphics.fill(x + 59, y + 37 + (12 - h), x + 68, y + 37 + 12, 0xFFFF6A00);
            }
        }

        // Creosote tank at (x + 129, y + 20), width 16, height 47 (capacity 24000)
        int creosote = this.menu.getCreosoteAmount();
        int creosoteH = (int) (47 * Math.min(1.0, creosote / 24000.0));
        if (creosoteH > 0) {
            graphics.fill(x + 129, y + 20 + (47 - creosoteH), x + 129 + 16, y + 20 + 47, 0x884D3A20); // dark brown creosote
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 129 && mouseX <= x + 145 && mouseY >= y + 20 && mouseY <= y + 67) {
            graphics.renderTooltip(this.font,
                    Component.literal("Creosote: " + this.menu.getCreosoteAmount() + " / 24000 mB"), mouseX, mouseY);
        }
    }
}
