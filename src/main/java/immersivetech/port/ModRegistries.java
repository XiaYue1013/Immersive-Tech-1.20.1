package immersivetech.port;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ModRegistries {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, ImmersiveTech.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ImmersiveTech.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ImmersiveTech.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ImmersiveTech.MOD_ID);

    public static final RegistryObject<Block> MULTIBLOCK_PART = BLOCKS.register("multiblock_part",
            () -> new MultiblockPartBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<BlockEntityType<MultiblockPartBlockEntity>> MULTIBLOCK_PART_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("multiblock_part",
                    () -> BlockEntityType.Builder.of(MultiblockPartBlockEntity::new, MULTIBLOCK_PART.get()).build(null));

    public static final RegistryObject<net.minecraft.world.inventory.MenuType<DistillerMenu>> DISTILLER_MENU =
            MENUS.register("distiller", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(DistillerMenu::new));
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<BoilerMenu>> BOILER_MENU =
            MENUS.register("boiler", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(BoilerMenu::new));
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<SolarTowerMenu>> SOLAR_TOWER_MENU =
            MENUS.register("solar_tower", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(SolarTowerMenu::new));
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<AdvancedCokeOvenMenu>> ADVANCED_COKE_OVEN_MENU =
            MENUS.register("coke_oven_advanced", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(AdvancedCokeOvenMenu::new));
    public static final RegistryObject<net.minecraft.world.inventory.MenuType<TimerMenu>> TIMER_MENU =
            MENUS.register("connectors_timer", () -> net.minecraftforge.common.extensions.IForgeMenuType.create(TimerMenu::new));

    public static final RegistryObject<Block> METAL_MULTIBLOCK = BLOCKS.register("metal_multiblock",
            () -> new DistillerBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> SOLAR_TOWER = BLOCKS.register("solar_tower",
            () -> new SolarTowerBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> SOLAR_REFLECTOR = BLOCKS.register("solar_reflector",
            () -> new SolarReflectorBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> STEAM_TURBINE = BLOCKS.register("steam_turbine",
            () -> new SteamTurbineBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> ALTERNATOR = BLOCKS.register("alternator",
            () -> new AlternatorBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> BOILER = BLOCKS.register("boiler",
            () -> new BoilerBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> ADVANCED_COKE_OVEN = BLOCKS.register("coke_oven_advanced",
            () -> new AdvancedCokeOvenBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.STONE)));
    public static final RegistryObject<Block> COKE_OVEN_PREHEATER = BLOCKS.register("coke_oven_preheater",
            () -> new CokeOvenPreheaterBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> TIMER = BLOCKS.register("connectors_timer",
            () -> new TimerBlock(BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
    public static final RegistryObject<Block> STONE_MULTIBLOCK = block("stone_multiblock");
    public static final RegistryObject<Block> CONNECTORS = block("connectors");
    public static final RegistryObject<Block> METAL_DEVICE = block("metal_device");
    public static final RegistryObject<Block> STONE_DECORATION = block("stone_decoration");
    public static final RegistryObject<Item> MATERIAL_SALT =
            ITEMS.register("material", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FORMATION_HAMMER =
            ITEMS.register("formation_hammer", () -> new FormationHammerItem(new Item.Properties()));
    public static final RegistryObject<Item> DISTILLER_ITEM = blockItem("metal_multiblock", METAL_MULTIBLOCK);
    public static final RegistryObject<Item> SOLAR_TOWER_ITEM = blockItem("solar_tower", SOLAR_TOWER);
    public static final RegistryObject<Item> SOLAR_REFLECTOR_ITEM = blockItem("solar_reflector", SOLAR_REFLECTOR);
    public static final RegistryObject<Item> STEAM_TURBINE_ITEM = blockItem("steam_turbine", STEAM_TURBINE);
    public static final RegistryObject<Item> ALTERNATOR_ITEM = blockItem("alternator", ALTERNATOR);
    public static final RegistryObject<Item> BOILER_ITEM = blockItem("boiler", BOILER);
    public static final RegistryObject<Item> ADVANCED_COKE_OVEN_ITEM =
            blockItem("coke_oven_advanced", ADVANCED_COKE_OVEN);
    public static final RegistryObject<Item> COKE_OVEN_PREHEATER_ITEM =
            blockItem("coke_oven_preheater", COKE_OVEN_PREHEATER);
    public static final RegistryObject<Item> TIMER_ITEM = blockItem("connectors_timer", TIMER);
    public static final RegistryObject<BlockEntityType<DistillerBlockEntity>> DISTILLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("distiller",
                    () -> BlockEntityType.Builder.of(DistillerBlockEntity::new, METAL_MULTIBLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarTowerBlockEntity>> SOLAR_TOWER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("solar_tower",
                    () -> BlockEntityType.Builder.of(SolarTowerBlockEntity::new, SOLAR_TOWER.get()).build(null));
    public static final RegistryObject<BlockEntityType<SolarReflectorBlockEntity>> SOLAR_REFLECTOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("solar_reflector",
                    () -> BlockEntityType.Builder.of(SolarReflectorBlockEntity::new, SOLAR_REFLECTOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("steam_turbine",
                    () -> BlockEntityType.Builder.of(SteamTurbineBlockEntity::new, STEAM_TURBINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<AlternatorBlockEntity>> ALTERNATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("alternator",
                    () -> BlockEntityType.Builder.of(AlternatorBlockEntity::new, ALTERNATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<BoilerBlockEntity>> BOILER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("boiler",
                    () -> BlockEntityType.Builder.of(BoilerBlockEntity::new, BOILER.get()).build(null));
    public static final RegistryObject<BlockEntityType<AdvancedCokeOvenBlockEntity>> ADVANCED_COKE_OVEN_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("coke_oven_advanced",
                    () -> BlockEntityType.Builder.of(AdvancedCokeOvenBlockEntity::new,
                            ADVANCED_COKE_OVEN.get()).build(null));
    public static final RegistryObject<BlockEntityType<CokeOvenPreheaterBlockEntity>> PREHEATER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("coke_oven_preheater",
                    () -> BlockEntityType.Builder.of(CokeOvenPreheaterBlockEntity::new,
                            COKE_OVEN_PREHEATER.get()).build(null));
    public static final RegistryObject<BlockEntityType<TimerBlockEntity>> TIMER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("connectors_timer",
                    () -> BlockEntityType.Builder.of(TimerBlockEntity::new, TIMER.get()).build(null));

    private ModRegistries() {
    }

    private static RegistryObject<Block> block(String id) {
        RegistryObject<Block> registered = BLOCKS.register(id, () -> new Block(
                BlockBehaviour.Properties.of().strength(3.0F).sound(SoundType.METAL)));
        ITEMS.register(id, () -> new net.minecraft.world.item.BlockItem(
                registered.get(), new Item.Properties()));
        return registered;
    }

    private static RegistryObject<Item> blockItem(String id, RegistryObject<Block> block) {
        return ITEMS.register(id, () -> new net.minecraft.world.item.BlockItem(
                block.get(), new Item.Properties()));
    }

    public static void register() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        bus.addListener(ModRegistries::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(MATERIAL_SALT);
            event.accept(FORMATION_HAMMER);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(DISTILLER_ITEM);
            event.accept(SOLAR_TOWER_ITEM);
            event.accept(SOLAR_REFLECTOR_ITEM);
            event.accept(STEAM_TURBINE_ITEM);
            event.accept(ALTERNATOR_ITEM);
            event.accept(BOILER_ITEM);
            event.accept(ADVANCED_COKE_OVEN_ITEM);
            event.accept(COKE_OVEN_PREHEATER_ITEM);
            event.accept(TIMER_ITEM);
            event.accept(STONE_DECORATION);
            event.accept(STONE_MULTIBLOCK);
            event.accept(CONNECTORS);
            event.accept(METAL_DEVICE);
        } else if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(TIMER_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FORMATION_HAMMER);
            event.accept(ModFluids.DISTILLED_WATER_BUCKET);
            event.accept(ModFluids.STEAM_BUCKET);
        }
    }
}
