package immersivetech.port;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class ModCapabilities {
    public static final Capability<MechanicalEnergy> MECHANICAL_ENERGY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ModCapabilities() {
    }
}
