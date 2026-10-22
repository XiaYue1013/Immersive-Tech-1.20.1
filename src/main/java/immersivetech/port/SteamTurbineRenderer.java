package immersivetech.port;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class SteamTurbineRenderer implements BlockEntityRenderer<SteamTurbineBlockEntity> {
    private static final ResourceLocation ROTOR_TEXTURE =
            new ResourceLocation(ImmersiveTech.MOD_ID, "textures/block/metal_device_rotor.png");

    public SteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SteamTurbineBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.isFormed()) return;

        Direction facing = Direction.NORTH;
        if (blockEntity.getBlockState().hasProperty(SteamTurbineBlock.FACING)) {
            facing = blockEntity.getBlockState().getValue(SteamTurbineBlock.FACING);
        }

        float rotation = blockEntity.getAnimation().getRotation(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            default -> {}
        }

        // Rotate along local Z axis
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(ROTOR_TEXTURE));
        renderRotorShaft(poseStack, consumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderRotorShaft(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        float radius = 0.4f;
        float length = 4.0f;
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 45.0f));
            drawQuad(poseStack, consumer, -0.05f, -radius, 0.05f, radius, -length / 2, length / 2, light, overlay);
            poseStack.popPose();
        }
    }

    private void drawQuad(PoseStack poseStack, VertexConsumer consumer,
            float x1, float y1, float x2, float y2, float z1, float z2, int light, int overlay) {
        var pose = poseStack.last().pose();
        var normal = poseStack.last().normal();
        consumer.vertex(pose, x1, y1, z1).color(255, 255, 255, 255).uv(0, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(pose, x2, y1, z1).color(255, 255, 255, 255).uv(1, 0).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(pose, x2, y2, z2).color(255, 255, 255, 255).uv(1, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
        consumer.vertex(pose, x1, y2, z2).color(255, 255, 255, 255).uv(0, 1).overlayCoords(overlay).uv2(light).normal(normal, 0, 1, 0).endVertex();
    }
}
