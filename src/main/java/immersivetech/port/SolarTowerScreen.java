package immersivetech.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SolarTowerScreen extends AbstractContainerScreen<SolarTowerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/gui/gui_solar_tower.png");

    public SolarTowerScreen(SolarTowerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Reflector directional indicators (u=198, v=31, w=10, h=10 in gui_solar_tower.png)
        if (this.menu.hasReflector(0)) { // North
            graphics.blit(TEXTURE, x + 32, y + 24, 198, 31, 10, 10);
        }
        if (this.menu.hasReflector(1)) { // West
            graphics.blit(TEXTURE, x + 16, y + 40, 198, 31, 10, 10);
        }
        if (this.menu.hasReflector(2)) { // South
            graphics.blit(TEXTURE, x + 32, y + 56, 198, 31, 10, 10);
        }
        if (this.menu.hasReflector(3)) { // East
            graphics.blit(TEXTURE, x + 48, y + 40, 198, 31, 10, 10);
        }

        // Water tank at (x + 102, y + 21), width 16, height 47 (capacity 32000)
        int water = this.menu.getWaterAmount();
        int waterH = (int) (47 * Math.min(1.0, water / 32000.0));
        if (waterH > 0) {
            graphics.fill(x + 102, y + 21 + (47 - waterH), x + 102 + 16, y + 21 + 47, 0x883F76E4);
        }

        // Steam tank at (x + 126, y + 21), width 16, height 47 (capacity 32000)
        int steam = this.menu.getSteamAmount();
        int steamH = (int) (47 * Math.min(1.0, steam / 32000.0));
        if (steamH > 0) {
            graphics.fill(x + 126, y + 21 + (47 - steamH), x + 126 + 16, y + 21 + 47, 0x88E0E0E0);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 102 && mouseX <= x + 118 && mouseY >= y + 21 && mouseY <= y + 68) {
            graphics.renderTooltip(this.font,
                    Component.literal("Water: " + this.menu.getWaterAmount() + " / 32000 mB"), mouseX, mouseY);
        }
        if (mouseX >= x + 126 && mouseX <= x + 142 && mouseY >= y + 21 && mouseY <= y + 68) {
            graphics.renderTooltip(this.font,
                    Component.literal("Steam: " + this.menu.getSteamAmount() + " / 32000 mB"), mouseX, mouseY);
        }
        if (mouseX >= x + 16 && mouseX <= x + 58 && mouseY >= y + 24 && mouseY <= y + 66) {
            graphics.renderTooltip(this.font,
                    Component.literal("Active Reflectors: " + this.menu.getReflectorCount()), mouseX, mouseY);
        }
    }
}
