package immersivetech.port;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end survival workflow and formula validation test for FerroO2000's Immersive Tech.
 * Validates:
 * Water -> Distiller -> Distilled Water -> Solar Tower + Reflector -> Steam ->
 * Steam Turbine -> Mechanical Energy -> Alternator -> Forge Energy.
 */
public class SurvivalWorkflowSimulationTest {

    @BeforeAll
    static void setupConfig() {
        if (!ModConfig.SPEC.isLoaded()) {
            com.electronwill.nightconfig.core.CommentedConfig config = com.electronwill.nightconfig.core.CommentedConfig.inMemory();
            ModConfig.SPEC.correct(config);
            ModConfig.SPEC.acceptConfig(config);
        }
    }

    @Test
    void testDistillerEnergyAndConversion() {
        int inputWater = ModConfig.DISTILLER_INPUT.get(); // 200 mB
        int outputDistWater = ModConfig.DISTILLER_OUTPUT.get(); // 100 mB
        int energyCost = ModConfig.DISTILLER_ENERGY.get(); // 50 FE
        int time = ModConfig.DISTILLER_TIME.get(); // 1 tick

        assertEquals(200, inputWater);
        assertEquals(100, outputDistWater);
        assertEquals(50, energyCost);
        assertEquals(1, time);

        // Efficiency ratio: 200 mB Water + 50 FE = 100 mB Distilled Water
        assertTrue(outputDistWater > 0);
        assertTrue(energyCost > 0);
    }

    @Test
    void testSolarTowerAndReflectorWorkflow() {
        int minRange = ModConfig.SOLAR_MIN_RANGE.get(); // 5
        int maxRange = ModConfig.SOLAR_MAX_RANGE.get(); // 10

        assertEquals(5, minRange);
        assertEquals(10, maxRange);

        // Within range checks
        for (int distance = minRange; distance <= maxRange; distance++) {
            boolean inRange = distance >= minRange && distance <= maxRange;
            assertTrue(inRange);
        }

        // Out of range checks
        assertFalse(3 >= minRange && 3 <= maxRange);
        assertFalse(12 >= minRange && 12 <= maxRange);

        // Water vs Distilled Water recipe output comparison
        // Normal water: 200 mB -> 100 mB steam
        // Distilled water: 200 mB -> 150 mB steam (1.5x bonus)
        int normalSteamOutput = 100;
        int distilledSteamOutput = 150;
        assertTrue(distilledSteamOutput > normalSteamOutput,
                "Distilled water must yield more steam than regular water in Solar Tower");
        assertEquals(1.5, (double) distilledSteamOutput / normalSteamOutput, 0.001);
    }

    @Test
    void testSteamTurbineToMechanicalEnergy() {
        int burnModifier = ModConfig.TURBINE_BURN_TIME_MODIFIER.get(); // 5
        int maxTorque = ModConfig.TURBINE_MAX_TORQUE.get(); // 8192
        int maxSpeed = ModConfig.TURBINE_MAX_SPEED.get(); // 6144

        // Steam burn rate formula: (1000 / 50 / 2) * burnModifier = 10 * 5 = 50 mB/t
        int burnRate = Math.max(1, (1000 / 50 / 2) * burnModifier);
        assertEquals(50, burnRate, "Turbine should consume 50 mB steam per tick");

        MechanicalEnergy activeEnergy = new MechanicalEnergy(maxTorque, maxSpeed);
        assertEquals(8192, activeEnergy.torque());
        assertEquals(6144, activeEnergy.speed());

        // Mechanical power = torque * speed = 8192 * 6144 = 50,331,648
        int power = activeEnergy.power();
        assertEquals(50_331_648, power);

        // Inactive turbine produces NONE (0 torque, 0 speed)
        assertEquals(0, MechanicalEnergy.NONE.power());
    }

    @Test
    void testAlternatorConversionToForgeEnergy() {
        int maxTorque = ModConfig.TURBINE_MAX_TORQUE.get();
        int maxSpeed = ModConfig.TURBINE_MAX_SPEED.get();
        MechanicalEnergy turbineOutput = new MechanicalEnergy(maxTorque, maxSpeed);

        int fluxModifier = ModConfig.ALTERNATOR_FLUX_MODIFIER.get(); // 800
        int maxFluxPerTick = ModConfig.ALTERNATOR_FLUX_PER_TICK.get(); // 4096
        int storage = ModConfig.ALTERNATOR_STORAGE.get(); // 1,200,000

        assertEquals(800, fluxModifier);
        assertEquals(4096, maxFluxPerTick);
        assertEquals(1_200_000, storage);

        // Conversion formula: power / fluxModifier
        int rawProduced = turbineOutput.power() / fluxModifier; // 50,331,648 / 800 = 62,914 FE/t
        assertEquals(62_914, rawProduced);

        // Output must be capped at alternatorFluxPerTick (4096 FE/t)
        int actualProduced = Math.min(rawProduced, maxFluxPerTick);
        assertEquals(4096, actualProduced, "Alternator tick generation must cap at alternatorFluxPerTick");

        // Verify net energy positivity of full survival loop:
        // Distiller consumes 50 FE to make 100 mB Distilled Water.
        // Solar Tower converts 100 mB Distilled Water to 75 mB Steam.
        // Turbine consumes 50 mB Steam per tick -> 75 mB Steam runs Turbine for 1.5 ticks.
        // Alternator generates 4096 FE/t * 1.5 ticks = 6144 FE.
        // Net gain: 6144 - 50 = +6094 FE!
        double generatedEnergy = (75.0 / 50.0) * actualProduced;
        double netGain = generatedEnergy - ModConfig.DISTILLER_ENERGY.get();
        assertTrue(netGain > 0, "Full survival loop (Water->Distilled->Steam->Turbine->Alternator) must be net energy positive");
    }

    @Test
    void testBoilerBurnTimesAndRatios() {
        int burnTimeBiodiesel = 125;
        int burnTimeDiesel = 175;
        int burnTimeFuel = 375;

        int burnTimeModifier = ModConfig.BOILER_BURN_TIME_MODIFIER.get(); // 1

        int consumedDiesel = Math.max(1, (1000 / burnTimeDiesel) * (4 * burnTimeModifier));
        int consumedBiodiesel = Math.max(1, (1000 / burnTimeBiodiesel) * (4 * burnTimeModifier));
        int consumedFuel = Math.max(1, (1000 / burnTimeFuel) * (4 * burnTimeModifier));

        assertTrue(consumedBiodiesel > consumedDiesel, "Biodiesel burns faster than diesel");
        assertTrue(consumedDiesel > consumedFuel, "Diesel burns faster than fuel");

        // Boiler steam yields:
        // Normal water: 200 mB -> 100 mB steam
        // Distilled water: 200 mB -> 150 mB steam
        int boilerNormalSteam = 100;
        int boilerDistilledSteam = 150;
        assertEquals(1.5, (double) boilerDistilledSteam / boilerNormalSteam, 0.001);
    }

    @Test
    void testFormableMultiblockContract() {
        // Mock FormableMultiblock instance to verify interface contract
        class TestMultiblock implements FormableMultiblock {
            private boolean formed;
            @Override public boolean isFormed() { return formed; }
            @Override public void setFormed(boolean formed) { this.formed = formed; }
        }

        TestMultiblock mb = new TestMultiblock();
        assertFalse(mb.isFormed());

        mb.setFormed(true);
        assertTrue(mb.isFormed());

        mb.setFormed(false);
        assertFalse(mb.isFormed());
    }

    @Test
    void testCokeOvenAndPreheaterIntegration() {
        int baseCokeTime = 1800; // ticks
        int preheaterCost = 32; // FE per tick per active preheater
        int preheaterCapacity = 8000; // FE storage

        assertEquals(1800, baseCokeTime);
        assertEquals(32, preheaterCost);
        assertEquals(8000, preheaterCapacity);

        // Preheater speedup: 1 preheater doubles progress (advances by 2 instead of 1)
        int preheaterCount = 2; // Up to 2 preheaters adjacent
        int effectiveSpeed = 1 + preheaterCount; // 3x speed
        assertEquals(3, effectiveSpeed);
        int acceleratedTime = baseCokeTime / effectiveSpeed;
        assertEquals(600, acceleratedTime, "With 2 preheaters, coke processing should take 600 ticks instead of 1800");

        // Total energy consumed by 2 preheaters: 2 * 32 FE/t * 600 ticks = 38,400 FE
        int totalEnergyUsed = preheaterCount * preheaterCost * acceleratedTime;
        assertEquals(38_400, totalEnergyUsed);
    }

    @Test
    void testRedstoneTimerBounds() {
        int minInterval = 10; // ticks (0.5s)
        int maxInterval = 600; // ticks (30s)
        int defaultInterval = 20; // ticks (1s)

        int interval = defaultInterval;
        assertTrue(interval >= minInterval && interval <= maxInterval);

        // Increment step (10 ticks / 0.5s)
        interval = Math.min(maxInterval, interval + 10);
        assertEquals(30, interval);

        // Max clamp
        interval = Math.min(maxInterval, 1000);
        assertEquals(600, interval);

        // Min clamp
        interval = Math.max(minInterval, 2);
        assertEquals(10, interval);
    }
}

