package at.roteklaue.portabletunes.client.renderers;

import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.blocks.entities.TapeDeckBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;

public record TapeDeckBlockEntityRenderer(ItemRenderer itemRenderer)
        implements BlockEntityRenderer<TapeDeckBlockEntity> {

    private static final float ITEM_SCALE = 0.4F;
    private static final double ITEM_SPACING = 0.35D;
    private static final double ITEM_HEIGHT = 1.25D;

    public TapeDeckBlockEntityRenderer(
            BlockEntityRendererProvider.Context itemRenderer
    ) {
        this(itemRenderer.getItemRenderer());
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
        IItemHandler itemHandler = blockEntity.getInventory();

        int slotCount = itemHandler.getSlots();

        for (int slot = 0; slot < slotCount; slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);

            if (stack.isEmpty()) continue;

            renderStack(
                    blockEntity,
                    stack,
                    slot,
                    slotCount,
                    poseStack,
                    bufferSource,
                    packedLight,
                    packedOverlay
            );
        }
    }

    private void renderStack(
            TapeDeckBlockEntity blockEntity,
            ItemStack stack,
            int slot,
            int slotCount,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();

        double totalWidth = (slotCount - 1) * ITEM_SPACING;
        double xOffset = slot * ITEM_SPACING - totalWidth / 2.0D;

        poseStack.translate(
                0.5D + xOffset,
                ITEM_HEIGHT,
                0.5D
        );

        if (blockEntity.getLevel() != null) {
            float rotation = (
                    blockEntity.getLevel().getGameTime()
            ) * 2.0F;

            poseStack.mulPose(
                    Axis.YP.rotationDegrees(rotation)
            );
        }

        poseStack.scale(
                ITEM_SCALE,
                ITEM_SCALE,
                ITEM_SCALE
        );

        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                blockEntity.getBlockPos().hashCode() + slot
        );

        poseStack.popPose();
    }
}
