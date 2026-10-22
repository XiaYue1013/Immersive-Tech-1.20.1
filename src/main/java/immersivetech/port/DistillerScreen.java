package immersivetech.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DistillerScreen extends AbstractContainerScreen<DistillerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/gui/gui_distiller.png");

    public DistillerScreen(DistillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Energy bar at (x + 158, y + 22), width 7, height 46
        int maxEnergy = this.menu.getMaxEnergy();
        if (maxEnergy > 0) {
            int energy = this.menu.getEnergy();
            int h = (int) (46 * Math.min(1.0, (double) energy / maxEnergy));
            if (h > 0) {
                graphics.fillGradient(x + 158, y + 22 + (46 - h), x + 165, y + 68, 0xFFB51500, 0xFF600B00);
            }
        }

        // Water tank at (x + 58, y + 21), width 16, height 47
        int water = this.menu.getWaterAmount();
        int waterH = (int) (47 * Math.min(1.0, water / 24000.0));
        if (waterH > 0) {
            graphics.fill(x + 58, y + 21 + (47 - waterH), x + 58 + 16, y + 21 + 47, 0x883F76E4);
        }

        // Distilled water tank at (x + 112, y + 21), width 16, height 47
        int dist = this.menu.getDistilledAmount();
        int distH = (int) (47 * Math.min(1.0, dist / 24000.0));
        if (distH > 0) {
            graphics.fill(x + 112, y + 21 + (47 - distH), x + 112 + 16, y + 21 + 47, 0x886B8EFF);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 157 && mouseX <= x + 165 && mouseY >= y + 21 && mouseY <= y + 68) {
            graphics.renderTooltip(this.font,
                    Component.literal(this.menu.getEnergy() + " / " + this.menu.getMaxEnergy() + " FE"), mouseX, mouseY);
        }
        if (mouseX >= x + 58 && mouseX <= x + 74 && mouseY >= y + 21 && mouseY <= y + 68) {
            graphics.renderTooltip(this.font,
                    Component.literal("Water: " + this.menu.getWaterAmount() + " / 24000 mB"), mouseX, mouseY);
        }
        if (mouseX >= x + 112 && mouseX <= x + 128 && mouseY >= y + 21 && mouseY <= y + 68) {
            graphics.renderTooltip(this.font,
                    Component.literal("Distilled Water: " + this.menu.getDistilledAmount() + " / 24000 mB"), mouseX, mouseY);
        }
    }
}
