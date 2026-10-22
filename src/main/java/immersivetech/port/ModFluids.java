package immersivetech.port;

import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, ImmersiveTech.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(ForgeRegistries.FLUIDS, ImmersiveTech.MOD_ID);

    public static final RegistryObject<FluidType> DISTILLED_WATER_TYPE =
            FLUID_TYPES.register("dist_water", () -> createFluidType(
                    FluidType.Properties.create().density(1000).viscosity(1000),
                    new ResourceLocation(ImmersiveTech.MOD_ID, "block/fluid/dist_water_still"),
                    new ResourceLocation(ImmersiveTech.MOD_ID, "block/fluid/dist_water_flow")));
    public static final RegistryObject<FluidType> STEAM_TYPE =
            FLUID_TYPES.register("steam", () -> createFluidType(
                    FluidType.Properties.create().density(-100).viscosity(500).temperature(373),
                    new ResourceLocation(ImmersiveTech.MOD_ID, "block/fluid/steam_still"),
                    new ResourceLocation(ImmersiveTech.MOD_ID, "block/fluid/steam_flow")));

    private static FluidType createFluidType(FluidType.Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return new FluidType(properties) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override
                    public ResourceLocation getStillTexture() {
                        return stillTexture;
                    }

                    @Override
                    public ResourceLocation getFlowingTexture() {
                        return flowingTexture;
                    }
                });
            }
        };
    }

    public static final RegistryObject<FlowingFluid> DISTILLED_WATER =
            FLUIDS.register("dist_water", () -> new ForgeFlowingFluid.Source(distilledWaterProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_DISTILLED_WATER =
            FLUIDS.register("flowing_dist_water", () -> new ForgeFlowingFluid.Flowing(distilledWaterProperties()));
    public static final RegistryObject<FlowingFluid> STEAM =
            FLUIDS.register("steam", () -> new ForgeFlowingFluid.Source(steamProperties()));
    public static final RegistryObject<FlowingFluid> FLOWING_STEAM =
            FLUIDS.register("flowing_steam", () -> new ForgeFlowingFluid.Flowing(steamProperties()));

    public static final RegistryObject<LiquidBlock> DISTILLED_WATER_BLOCK = ModRegistries.BLOCKS.register(
            "fluid_dist_water", () -> new LiquidBlock(DISTILLED_WATER,
                    Block.Properties.copy(Blocks.WATER)));
    public static final RegistryObject<LiquidBlock> STEAM_BLOCK = ModRegistries.BLOCKS.register(
            "fluid_steam", () -> new LiquidBlock(STEAM,
                    Block.Properties.copy(Blocks.WATER).noOcclusion()));

    public static final RegistryObject<Item> DISTILLED_WATER_BUCKET = ModRegistries.ITEMS.register(
            "dist_water_bucket", () -> new BucketItem(DISTILLED_WATER,
                    new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> STEAM_BUCKET = ModRegistries.ITEMS.register(
            "steam_bucket", () -> new BucketItem(STEAM,
                    new Item.Properties().stacksTo(1)));

    private static ForgeFlowingFluid.Properties distilledWaterProperties() {
        return new ForgeFlowingFluid.Properties(DISTILLED_WATER_TYPE, DISTILLED_WATER, FLOWING_DISTILLED_WATER)
                    .block(DISTILLED_WATER_BLOCK)
                    .bucket(DISTILLED_WATER_BUCKET)
                    .levelDecreasePerBlock(1)
                    .slopeFindDistance(4);
    }

    private static ForgeFlowingFluid.Properties steamProperties() {
        return new ForgeFlowingFluid.Properties(STEAM_TYPE, STEAM, FLOWING_STEAM)
                    .block(STEAM_BLOCK)
                    .bucket(STEAM_BUCKET)
                    .levelDecreasePerBlock(1)
                    .slopeFindDistance(4);
    }

    private ModFluids() {
    }

    public static void register() {
        var bus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
    }
}
