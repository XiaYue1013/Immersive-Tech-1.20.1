package immersivetech.port;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiblockDefinitionTest {

    @Test
    void testDistillerDimensionsAndOmissions() {
        MultiblockDefinition def = ModMultiblocks.DISTILLER;
        assertEquals(3, def.width());
        assertEquals(3, def.height());
        assertEquals(3, def.length());
        assertEquals("distiller", def.id());

        // Total 3*3*3 = 27 blocks.
        // Omissions in ModMultiblocks.distiller():
        // (y == 1 && x == 1 && z == 0) -> 1 block
        // (y == 2 && z == 0) -> 3 blocks (x=0,1,2)
        // Total omitted: 4 blocks -> 23 required offsets
        assertEquals(23, def.requiredOffsets().size());

        assertFalse(def.requiredOffsets().contains(new BlockPos(1, 1, 0)));
        assertFalse(def.requiredOffsets().contains(new BlockPos(0, 2, 0)));
        assertFalse(def.requiredOffsets().contains(new BlockPos(1, 2, 0)));
        assertFalse(def.requiredOffsets().contains(new BlockPos(2, 2, 0)));

        assertTrue(def.requiredOffsets().contains(new BlockPos(0, 0, 0)));
        assertTrue(def.requiredOffsets().contains(new BlockPos(1, 1, 1)));
    }

    @Test
    void testSolarTowerDimensionsAndOmissions() {
        MultiblockDefinition def = ModMultiblocks.SOLAR_TOWER;
        assertEquals(3, def.width());
        assertEquals(7, def.height());
        assertEquals(3, def.length());
        assertEquals("solar_tower", def.id());

        // Total 3*7*3 = 63 blocks.
        // Omissions: y == 3 && ((z == 1 && (x == 0 || x == 2)) || (x == 1 && (z == 0 || z == 2))) -> 4 blocks omitted
        // 63 - 4 = 59 blocks
        assertEquals(59, def.requiredOffsets().size());
    }

    @Test
    void testSolarReflectorDimensions() {
        MultiblockDefinition def = ModMultiblocks.SOLAR_REFLECTOR;
        assertEquals(3, def.width());
        assertEquals(1, def.height());
        assertEquals(5, def.length());
        assertEquals("solar_reflector", def.id());
        assertEquals(15, def.requiredOffsets().size());
    }

    @Test
    void testSteamTurbineDimensions() {
        MultiblockDefinition def = ModMultiblocks.STEAM_TURBINE;
        assertEquals(3, def.width());
        assertEquals(4, def.height());
        assertEquals(10, def.length());
        assertEquals("steam_turbine", def.id());
        assertTrue(def.requiredOffsets().size() > 0);
    }

    @Test
    void testBoilerDimensionsAndOmissions() {
        MultiblockDefinition def = ModMultiblocks.BOILER;
        assertEquals(5, def.width());
        assertEquals(3, def.height());
        assertEquals(3, def.length());
        assertEquals("boiler", def.id());
        // 5*3*3 = 45 blocks minus 1 omission (y == 2 && z == 0 && x == 4) = 44
        assertEquals(44, def.requiredOffsets().size());
        assertFalse(def.requiredOffsets().contains(new BlockPos(4, 2, 0)));
    }

    @Test
    void testAlternatorDimensionsAndOmissions() {
        MultiblockDefinition def = ModMultiblocks.ALTERNATOR;
        assertEquals(3, def.width());
        assertEquals(3, def.height());
        assertEquals(4, def.length());
        assertEquals("alternator", def.id());
        // 3*3*4 = 36 blocks minus 1 omission (y == 2 && z == 1, for x=1: wait: y==2 && z==1 for all 3 x values? In code: y==2 && z==1 is true for 3 blocks)
        // 36 - 3 = 33
        assertEquals(33, def.requiredOffsets().size());
    }

    @Test
    void testAdvancedCokeOvenDimensions() {
        MultiblockDefinition def = ModMultiblocks.COKE_OVEN_ADVANCED;
        assertEquals(3, def.width());
        assertEquals(4, def.height());
        assertEquals(3, def.length());
        assertEquals("coke_oven_advanced", def.id());
        // 3*3*3 = 27 + top center block = 28
        assertEquals(28, def.requiredOffsets().size());
        assertTrue(def.requiredOffsets().contains(new BlockPos(1, 3, 1)));
    }

    @Test
    void testCoordinateTransformation() {
        BlockPos origin = new BlockPos(100, 64, 100);
        BlockPos offset = new BlockPos(1, 2, 3);

        // NORTH: relative unchanged
        BlockPos north = MultiblockDefinition.transform(origin, offset, Direction.NORTH, false);
        assertEquals(new BlockPos(101, 66, 103), north);

        // SOUTH: -x, y, -z
        BlockPos south = MultiblockDefinition.transform(origin, offset, Direction.SOUTH, false);
        assertEquals(new BlockPos(99, 66, 97), south);

        // EAST: -z, y, x
        BlockPos east = MultiblockDefinition.transform(origin, offset, Direction.EAST, false);
        assertEquals(new BlockPos(97, 66, 101), east);

        // WEST: z, y, -x
        BlockPos west = MultiblockDefinition.transform(origin, offset, Direction.WEST, false);
        assertEquals(new BlockPos(103, 66, 99), west);
    }
}
