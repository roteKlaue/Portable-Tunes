package at.roteklaue.portabletunes.compat.amendments;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class OptionalRecordRenderer {
    private static final boolean AMENDMENTS_LOADED =
            ModList.get().isLoaded("amendments");

    public static boolean renderWithAmendments(
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        if (!AMENDMENTS_LOADED) return false;

        AmendmentsBridge.render(
                stack,
                poseStack,
                bufferSource,
                packedLight
        );

        return true;
    }

    private static final class AmendmentsBridge {
        private static void render(
                ItemStack stack,
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                int packedLight
        ) {
            at.roteklaue.portabletunes.compat.amendments
                    .AmendmentsRecordRenderer.render(
                            stack,
                            poseStack,
                            bufferSource,
                            packedLight
                    );
        }
    }
}
