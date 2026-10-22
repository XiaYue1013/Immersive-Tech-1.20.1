package immersivetech.port;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InteractiveFeaturesTest {

    @Test
    void testMechanicalEnergyAnimationSpinAndFade() {
        MechanicalEnergyAnimation anim = new MechanicalEnergyAnimation();
        assertEquals(0f, anim.getAnimationRotation());
        assertEquals(0f, anim.getAnimationStep());

        // Update when active: base speed is 18.0 deg/tick
        anim.update(true);
        assertEquals(18.0f, anim.getAnimationStep(), 0.001f);
        assertEquals(18.0f, anim.getAnimationRotation(), 0.001f);

        // Interpolation with partial tick 0.5: 18 + 18 * 0.5 = 27 degrees
        assertEquals(27.0f, anim.getRotation(0.5f), 0.001f);

        // Update for multiple ticks
        for (int i = 0; i < 9; i++) {
            anim.update(true);
        }
        // Total 10 ticks = 180 degrees
        assertEquals(180.0f, anim.getAnimationRotation(), 0.001f);

        // 360 wrap around
        for (int i = 0; i < 10; i++) {
            anim.update(true);
        }
        assertEquals(0.0f, anim.getAnimationRotation(), 0.001f);

        // Test fade out
        anim.triggerInactive();
        assertEquals(160, anim.getAnimationFadeOut());
        anim.update(false);
        assertTrue(anim.getAnimationStep() > 0);
        assertTrue(anim.getAnimationFadeOut() < 160);
    }

    @Test
    void testMechanicalEnergyAnimationNbt() {
        MechanicalEnergyAnimation anim1 = new MechanicalEnergyAnimation(40, 80, 123.4f, 12.5f);
        CompoundTag tag = new CompoundTag();
        anim1.save(tag);

        MechanicalEnergyAnimation anim2 = new MechanicalEnergyAnimation();
        anim2.load(tag);

        assertEquals(40, anim2.getAnimationFadeIn());
        assertEquals(80, anim2.getAnimationFadeOut());
        assertEquals(123.4f, anim2.getAnimationRotation(), 0.001f);
        assertEquals(12.5f, anim2.getAnimationStep(), 0.001f);
    }

    @Test
    void testMultiblockPartTrackerPositionsAndNbt() {
        MultiblockPartTracker tracker = new MultiblockPartTracker();
        assertTrue(tracker.getPositions().isEmpty());

        BlockPos p1 = new BlockPos(10, 64, -20);
        BlockPos p2 = new BlockPos(11, 64, -20);
        BlockPos p3 = new BlockPos(12, 65, -19);

        tracker.setPositions(java.util.List.of(p1, p2, p3));
        assertEquals(3, tracker.getPositions().size());
        assertTrue(tracker.getPositions().contains(p1));
        assertTrue(tracker.getPositions().contains(p2));
        assertTrue(tracker.getPositions().contains(p3));

        CompoundTag tag = new CompoundTag();
        tracker.save(tag);

        MultiblockPartTracker loaded = new MultiblockPartTracker();
        loaded.load(tag);
        assertEquals(3, loaded.getPositions().size());
        assertEquals(p1, loaded.getPositions().get(0));
        assertEquals(p2, loaded.getPositions().get(1));
        assertEquals(p3, loaded.getPositions().get(2));
    }
}
