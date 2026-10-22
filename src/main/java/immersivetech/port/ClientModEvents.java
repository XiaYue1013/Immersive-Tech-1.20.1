package immersivetech.port;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ImmersiveTech.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModRegistries.DISTILLER_MENU.get(), DistillerScreen::new);
            MenuScreens.register(ModRegistries.BOILER_MENU.get(), BoilerScreen::new);
            MenuScreens.register(ModRegistries.SOLAR_TOWER_MENU.get(), SolarTowerScreen::new);
            MenuScreens.register(ModRegistries.ADVANCED_COKE_OVEN_MENU.get(), AdvancedCokeOvenScreen::new);
            MenuScreens.register(ModRegistries.TIMER_MENU.get(), TimerScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistries.STEAM_TURBINE_BLOCK_ENTITY.get(), SteamTurbineRenderer::new);
    }

    private ClientModEvents() {}
}
