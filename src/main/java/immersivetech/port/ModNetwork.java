package immersivetech.port;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ImmersiveTech.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, TimerAdjustPacket.class,
                TimerAdjustPacket::encode,
                TimerAdjustPacket::decode,
                TimerAdjustPacket::handle);
    }

    public static final class TimerAdjustPacket {
        private final BlockPos pos;
        private final boolean increase;

        public TimerAdjustPacket(BlockPos pos, boolean increase) {
            this.pos = pos;
            this.increase = increase;
        }

        public static void encode(TimerAdjustPacket msg, FriendlyByteBuf buf) {
            buf.writeBlockPos(msg.pos);
            buf.writeBoolean(msg.increase);
        }

        public static TimerAdjustPacket decode(FriendlyByteBuf buf) {
            return new TimerAdjustPacket(buf.readBlockPos(), buf.readBoolean());
        }

        public static void handle(TimerAdjustPacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player != null && player.level().isLoaded(msg.pos)
                        && player.distanceToSqr(msg.pos.getX() + .5, msg.pos.getY() + .5, msg.pos.getZ() + .5) <= 64) {
                    if (player.level().getBlockEntity(msg.pos) instanceof TimerBlockEntity timer) {
                        if (msg.increase) timer.increase();
                        else timer.decrease();
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
