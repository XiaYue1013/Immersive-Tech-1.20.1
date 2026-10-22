package immersivetech.port;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(ImmersiveTech.MOD_ID)
public final class ImmersiveTech {
    public static final String MOD_ID = "immersivetech";

    public ImmersiveTech() {
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,
                immersivetech.port.ModConfig.SPEC);
        ModFluids.register();
        ModRegistries.register();
        ModNetwork.register();
        ModRecipes.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
