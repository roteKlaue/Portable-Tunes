package at.roteklaue.portabletunes.client.renderers;

import at.roteklaue.portabletunes.blocks.TapeDeckBlock;
import at.roteklaue.portabletunes.blocks.entities.TapeDeckBlockEntity;
import at.roteklaue.portabletunes.compat.amendments.OptionalRecordRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

public record TapeDeckBlockEntityRenderer(ItemRenderer itemRenderer)
        implements BlockEntityRenderer<TapeDeckBlockEntity> {
    private static final float PIXEL = 1.0F / 16.0F;

    private static final SlotRenderData[] SLOT_RENDER_DATA = {
            new SlotRenderData(
                    SlotType.Disc,
                    0.4D,
                    15D * PIXEL,
                    0.4D,
                    -90.0F,
                    0.0F,
                    0.0F,
                    0.60F
            ),

            new SlotRenderData(
                    SlotType.Disc,
                    1.6D,
                    15D * PIXEL,
                    0.4D,
                    -90.0F,
                    0.0F,
                    0.0F,
                    0.60F
            ),

            new SlotRenderData(
                    SlotType.Cassette,
                    1.00D,
                    13.5D * PIXEL,
                    0.75D,
                    -90.0F,
                    180.0F,
                    0.0f,
                    0.40F
            )
    };

    public TapeDeckBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this(context.getItemRenderer());
    }

    @Override
    public void render(
            TapeDeckBlockEntity blockEntity,
            float partialTick,
            @Nonnull PoseStack poseStack,
            @Nonnull MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        IItemHandler inventory = blockEntity.getInventory();
        Direction facing = blockEntity
                .getBlockState()
                .getValue(TapeDeckBlock.FACING);

        poseStack.pushPose();

        rotateForFacing(poseStack, facing);

        int slotCount = Math.min(inventory.getSlots(), SLOT_RENDER_DATA.length);

        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            renderStack(
                    blockEntity,
                    stack,
                    slot,
                    SLOT_RENDER_DATA[slot],
                    poseStack,
                    bufferSource,
                    packedOverlay
            );
        }

        poseStack.popPose();
    }

    private static void rotateForFacing(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-1D, 0.0D, -1D);
    }

    private void renderStack(
            TapeDeckBlockEntity blockEntity,
            ItemStack stack,
            int slot,
            SlotRenderData renderData,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedOverlay
    ) {
        poseStack.pushPose();

        poseStack.translate(renderData.x(), renderData.y(), renderData.z());
        poseStack.mulPose(Axis.XP.rotationDegrees(renderData.xRotation()));
        poseStack.mulPose(Axis.YP.rotationDegrees(renderData.yRotation()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(renderData.zRotation()));

        float scale = renderData.scale();
        poseStack.scale(scale, scale, scale);

        boolean renderedByAmendments = false;

        if (renderData.type() == SlotType.Disc && blockEntity.getLevel() != null) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(0D, 0.0D, -.02D);
            int upLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above(1));
            renderedByAmendments = OptionalRecordRenderer.renderWithAmendments(
                    stack,
                    poseStack,
                    bufferSource,
                    upLight
            );
        }

        if (!renderedByAmendments) {
            int itemLight = getTapeDeckLight(blockEntity);
            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    itemLight,
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos().hashCode() + slot
            );
        }

        poseStack.popPose();
    }

    private static int getTapeDeckLight(TapeDeckBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) return LightTexture.FULL_BRIGHT;

        BlockPos origin = blockEntity.getBlockPos();
        int combinedLight = LevelRenderer.getLightColor(level, origin);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int nearbyLight = LevelRenderer.getLightColor(
                    level,
                    origin.relative(direction)
            );

            combinedLight = combineLight(combinedLight, nearbyLight);
        }

        int aboveLight = LevelRenderer.getLightColor(level, origin.above());
        return combineLight(combinedLight, aboveLight);
    }

    private static int combineLight(int first, int second) {
        int blockLight = Math.max(LightTexture.block(first), LightTexture.block(second));
        int skyLight = Math.max(LightTexture.sky(first), LightTexture.sky(second));

        return LightTexture.pack(blockLight, skyLight);
    }

    private record SlotRenderData(
            SlotType type,
            double x,
            double y,
            double z,
            float xRotation,
            float yRotation,
            float zRotation,
            float scale
    ) {}

    private enum SlotType {
        Disc,
        Cassette
    }
}
