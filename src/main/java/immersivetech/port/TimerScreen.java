package immersivetech.port;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TimerScreen extends AbstractContainerScreen<TimerMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/gui/gui_timer.png");

    public TimerScreen(TimerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        // "+" button at (x + 39, y + 35)
        this.addRenderableWidget(Button.builder(Component.literal("+"), btn -> {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.TimerAdjustPacket(this.menu.getPos(), true));
        }).bounds(x + 39, y + 35, 16, 16).build());

        // "-" button at (x + 120, y + 35)
        this.addRenderableWidget(Button.builder(Component.literal("-"), btn -> {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.TimerAdjustPacket(this.menu.getPos(), false));
        }).bounds(x + 120, y + 35, 16, 16).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        float seconds = this.menu.getIntervalTicks() / 20.0f;
        String text = String.format("%.1f Sec.", seconds);
        graphics.drawString(this.font, text, x + 65, y + 39, 0xFFFFFF, true);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
