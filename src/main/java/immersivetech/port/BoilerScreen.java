package immersivetech.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BoilerScreen extends AbstractContainerScreen<BoilerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/gui/gui_boiler.png");

    public BoilerScreen(BoilerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Fuel tank at (x + 13, y + 20), width 16, height 47 (capacity 12000)
        int fuel = this.menu.getFuelAmount();
        int fuelH = (int) (47 * Math.min(1.0, fuel / 12000.0));
        if (fuelH > 0) {
            graphics.fill(x + 13, y + 20 + (47 - fuelH), x + 13 + 16, y + 20 + 47, 0x88D48E28); // diesel/biodiesel orange
        }

        // Water tank at (x + 100, y + 20), width 16, height 47 (capacity 24000)
        int water = this.menu.getWaterAmount();
        int waterH = (int) (47 * Math.min(1.0, water / 24000.0));
        if (waterH > 0) {
            graphics.fill(x + 100, y + 20 + (47 - waterH), x + 100 + 16, y + 20 + 47, 0x883F76E4); // water blue
        }

        // Steam tank at (x + 123, y + 20), width 16, height 47 (capacity 24000)
        int steam = this.menu.getSteamAmount();
        int steamH = (int) (47 * Math.min(1.0, steam / 24000.0));
        if (steamH > 0) {
            graphics.fill(x + 123, y + 20 + (47 - steamH), x + 123 + 16, y + 20 + 47, 0x88E0E0E0); // steam white
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        int x = this.leftPos;
        int y = this.topPos;
        if (mouseX >= x + 13 && mouseX <= x + 29 && mouseY >= y + 20 && mouseY <= y + 67) {
            graphics.renderTooltip(this.font,
                    Component.literal("Fuel: " + this.menu.getFuelAmount() + " / 12000 mB"), mouseX, mouseY);
        }
        if (mouseX >= x + 100 && mouseX <= x + 116 && mouseY >= y + 20 && mouseY <= y + 67) {
            graphics.renderTooltip(this.font,
                    Component.literal("Water: " + this.menu.getWaterAmount() + " / 24000 mB"), mouseX, mouseY);
        }
        if (mouseX >= x + 123 && mouseX <= x + 139 && mouseY >= y + 20 && mouseY <= y + 67) {
            graphics.renderTooltip(this.font,
                    Component.literal("Steam: " + this.menu.getSteamAmount() + " / 24000 mB"), mouseX, mouseY);
        }
    }
}
