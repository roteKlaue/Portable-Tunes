package at.roteklaue.portabletunes.compat.amendments;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.mehvahdjukaar.amendments.AmendmentsClient;
import net.mehvahdjukaar.moonlight.api.client.util.VertexUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.ItemStack;

public final class AmendmentsRecordRenderer {
    public static void render(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (stack.isEmpty()) return;

        Material material = AmendmentsClient.getRecordMaterial(stack.getItem());
        VertexConsumer vertexConsumer = material.buffer(
                bufferSource,
                RenderType::entityCutout
        );

        int lu = packedLight & '\uffff';
        int lv = packedLight >> 16 & '\uffff';

        VertexUtil.addQuad(
                vertexConsumer,
                poseStack,
                -0.5F,
                -0.5F,
                0.5F,
                0.5F,
                lu,
                lv
        );
    }
}
