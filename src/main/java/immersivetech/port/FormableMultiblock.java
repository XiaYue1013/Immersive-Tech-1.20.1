package immersivetech.port;

import net.minecraft.world.level.Level;

public interface FormableMultiblock {
    boolean isFormed();
    void setFormed(boolean formed);

    default MultiblockPartTracker getPartTracker() {
        return null;
    }

    default void disassemble(Level level) {
        setFormed(false);
        MultiblockPartTracker tracker = getPartTracker();
        if (tracker != null && level != null && !level.isClientSide) {
            tracker.disassemble(level);
        }
    }
}
