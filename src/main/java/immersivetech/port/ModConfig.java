package immersivetech.port;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue DISTILLER_ENERGY;
    public static final ForgeConfigSpec.IntValue DISTILLER_TIME;
    public static final ForgeConfigSpec.IntValue DISTILLER_INPUT;
    public static final ForgeConfigSpec.IntValue DISTILLER_OUTPUT;
    public static final ForgeConfigSpec.IntValue TANK_CAPACITY;
    public static final ForgeConfigSpec.IntValue SOLAR_MIN_RANGE;
    public static final ForgeConfigSpec.IntValue SOLAR_MAX_RANGE;
    public static final ForgeConfigSpec.IntValue SOLAR_TIME_MODIFIER;
    public static final ForgeConfigSpec.IntValue BOILER_TIME_MODIFIER;
    public static final ForgeConfigSpec.IntValue BOILER_BURN_TIME_MODIFIER;
    public static final ForgeConfigSpec.IntValue TURBINE_BURN_TIME_MODIFIER;
    public static final ForgeConfigSpec.IntValue TURBINE_MAX_TORQUE;
    public static final ForgeConfigSpec.IntValue TURBINE_MAX_SPEED;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_STORAGE;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_FLUX_MODIFIER;
    public static final ForgeConfigSpec.IntValue ALTERNATOR_FLUX_PER_TICK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("machines");
        DISTILLER_ENERGY = builder.comment("Legacy Distiller energy cost per operation.")
                .defineInRange("distillerEnergy", 50, 0, Integer.MAX_VALUE);
        DISTILLER_TIME = builder.comment("Legacy Distiller processing time in ticks.")
                .defineInRange("distillerTime", 1, 1, Integer.MAX_VALUE);
        DISTILLER_INPUT = builder.defineInRange("distillerInput", 200, 1, 24_000);
        DISTILLER_OUTPUT = builder.defineInRange("distillerOutput", 100, 1, 24_000);
        TANK_CAPACITY = builder.defineInRange("tankCapacity", 24_000, 1, Integer.MAX_VALUE);
        SOLAR_MIN_RANGE = builder.defineInRange("solarTowerMinRange", 5, 1, 64);
        SOLAR_MAX_RANGE = builder.defineInRange("solarTowerMaxRange", 10, 1, 128);
        SOLAR_TIME_MODIFIER = builder.defineInRange("solarTowerTimeModifier", 1, 1, Integer.MAX_VALUE);
        BOILER_TIME_MODIFIER = builder.defineInRange("boilerTimeModifier", 1, 1, Integer.MAX_VALUE);
        BOILER_BURN_TIME_MODIFIER = builder.defineInRange("boilerBurnTimeModifier", 1, 1, Integer.MAX_VALUE);
        TURBINE_BURN_TIME_MODIFIER = builder.defineInRange("steamTurbineBurnTimeModifier", 5, 1, 128);
        TURBINE_MAX_TORQUE = builder.defineInRange("steamTurbineMaxTorque", 8192, 0, Integer.MAX_VALUE);
        TURBINE_MAX_SPEED = builder.defineInRange("steamTurbineMaxSpeed", 6144, 0, Integer.MAX_VALUE);
        ALTERNATOR_STORAGE = builder.defineInRange("alternatorEnergyStorage", 1_200_000, 1, Integer.MAX_VALUE);
        ALTERNATOR_FLUX_MODIFIER = builder.defineInRange("alternatorFluxModifier", 800, 1, Integer.MAX_VALUE);
        ALTERNATOR_FLUX_PER_TICK = builder.defineInRange("alternatorFluxPerTick", 4096, 1, Integer.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private ModConfig() {
    }
}
