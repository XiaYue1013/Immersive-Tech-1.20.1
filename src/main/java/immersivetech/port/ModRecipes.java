package immersivetech.port;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ImmersiveTech.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ImmersiveTech.MOD_ID);
    public static final RegistryObject<RecipeSerializer<DistillerRecipe>> DISTILLER_SERIALIZER =
            SERIALIZERS.register("distiller", () -> DistillerRecipe.SERIALIZER);
    public static final RegistryObject<RecipeType<DistillerRecipe>> DISTILLER_TYPE =
            TYPES.register("distiller", () -> DistillerRecipe.TYPE);
    public static final RegistryObject<RecipeSerializer<SolarTowerRecipe>> SOLAR_TOWER_SERIALIZER =
            SERIALIZERS.register("solar_tower", () -> SolarTowerRecipe.SERIALIZER);
    public static final RegistryObject<RecipeType<SolarTowerRecipe>> SOLAR_TOWER_TYPE =
            TYPES.register("solar_tower", () -> SolarTowerRecipe.TYPE);
    public static final RegistryObject<RecipeSerializer<BoilerRecipe>> BOILER_SERIALIZER =
            SERIALIZERS.register("boiler", () -> BoilerRecipe.SERIALIZER);
    public static final RegistryObject<RecipeType<BoilerRecipe>> BOILER_TYPE =
            TYPES.register("boiler", () -> BoilerRecipe.TYPE);
    public static final RegistryObject<RecipeSerializer<AdvancedCokeOvenRecipe>> ADVANCED_COKE_OVEN_SERIALIZER =
            SERIALIZERS.register("coke_oven_advanced", () -> AdvancedCokeOvenRecipe.SERIALIZER);
    public static final RegistryObject<RecipeType<AdvancedCokeOvenRecipe>> ADVANCED_COKE_OVEN_TYPE =
            TYPES.register("coke_oven_advanced", () -> AdvancedCokeOvenRecipe.TYPE);

    private ModRecipes() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
        TYPES.register(bus);
    }
}
