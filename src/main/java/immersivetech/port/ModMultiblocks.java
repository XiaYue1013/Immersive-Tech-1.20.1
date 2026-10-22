package immersivetech.port;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ModMultiblocks {
    public static final MultiblockDefinition DISTILLER = distiller();
    public static final MultiblockDefinition SOLAR_TOWER = solarTower();
    public static final MultiblockDefinition SOLAR_REFLECTOR = filled("solar_reflector", 3, 1, 5);
    public static final MultiblockDefinition STEAM_TURBINE = steamTurbine();
    public static final MultiblockDefinition BOILER = boiler();
    public static final MultiblockDefinition ALTERNATOR = alternator();
    public static final MultiblockDefinition COKE_OVEN_ADVANCED = advancedCokeOven();

    private static MultiblockDefinition filled(String id, int width, int height, int length) {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            for (int z = 0; z < length; z++) offsets.add(new BlockPos(x, y, z));
        return new MultiblockDefinition(id, width, height, length, List.copyOf(offsets));
    }

    private static MultiblockDefinition steamTurbine() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int h = 0; h < 4; h++) {
            for (int l = 0; l < 10; l++) {
                for (int w = 0; w < 3; w++) {
                    boolean omitted = (h == 0 && (l == 4 || l == 9))
                            || (h == 2 && l == 1)
                            || (h == 2 && l == 0 && w == 2)
                            || ((l == 4 || l == 9) && (w == 0 || w == 2))
                            || (h == 2 && w == 1 && (l == 4 || l == 9))
                            || (h == 3 && (w == 0 || w == 2))
                            || (h == 3 && w == 1 && (l < 3 || l > 7));
                    if (!omitted) offsets.add(new BlockPos(w, h, l));
                }

            }
        }
        return new MultiblockDefinition("steam_turbine", 3, 4, 10, List.copyOf(offsets));
    }

    private static MultiblockDefinition distiller() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 3; x++)
            if (!((y == 1 && x == 1 && z == 0) || (y == 2 && z == 0)))
                offsets.add(new BlockPos(x, y, z));
        return new MultiblockDefinition("distiller", 3, 3, 3, List.copyOf(offsets));
    }

    private static MultiblockDefinition solarTower() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int y = 0; y < 7; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 3; x++)
            if (!(y == 3 && ((z == 1 && (x == 0 || x == 2)) || (x == 1 && (z == 0 || z == 2)))))
                offsets.add(new BlockPos(x, y, z));
        return new MultiblockDefinition("solar_tower", 3, 7, 3, List.copyOf(offsets));
    }

    private static MultiblockDefinition boiler() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int y = 0; y < 3; y++) for (int z = 0; z < 3; z++) for (int x = 0; x < 5; x++)
            if (!(y == 2 && z == 0 && x == 4)) offsets.add(new BlockPos(x, y, z));
        return new MultiblockDefinition("boiler", 5, 3, 3, List.copyOf(offsets));
    }

    private static MultiblockDefinition alternator() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int y = 0; y < 3; y++) for (int z = 0; z < 4; z++) for (int x = 0; x < 3; x++)
            if (!(y == 2 && z == 1)) offsets.add(new BlockPos(x, y, z));
        return new MultiblockDefinition("alternator", 3, 3, 4, List.copyOf(offsets));
    }

    private static MultiblockDefinition advancedCokeOven() {
        List<BlockPos> offsets = new java.util.ArrayList<>();
        for (int h = 0; h < 4; h++) {
            for (int l = 0; l < 3; l++) {
                for (int w = 0; w < 3; w++) {
                    if (h < 3 || (w == 1 && l == 1)) offsets.add(new BlockPos(w, h, l));
                }
            }
        }
        return new MultiblockDefinition("coke_oven_advanced", 3, 4, 3, List.copyOf(offsets));
    }

    public static MultiblockDefinition forTrigger(BlockState state) {
        if (state.is(ModRegistries.SOLAR_TOWER.get())) return SOLAR_TOWER;
        if (state.is(ModRegistries.SOLAR_REFLECTOR.get())) return SOLAR_REFLECTOR;
        if (state.is(ModRegistries.STEAM_TURBINE.get())) return STEAM_TURBINE;
        if (state.is(ModRegistries.BOILER.get())) return BOILER;
        if (state.is(ModRegistries.ALTERNATOR.get())) return ALTERNATOR;
        if (state.is(ModRegistries.ADVANCED_COKE_OVEN.get())) return COKE_OVEN_ADVANCED;
        if (state.is(ModRegistries.TIMER.get())) return null;
        if (state.is(ModRegistries.METAL_MULTIBLOCK.get())) return DISTILLER;
        return null;
    }
    private ModMultiblocks() {}
}
