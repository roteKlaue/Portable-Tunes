package at.roteklaue.portabletunes.client.toast;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PortableToast implements Toast {
    // private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/tutorial");
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/advancement");

    private static final int PROGRESS_BAR_WIDTH = 154;
    private static final int PROGRESS_BAR_X = 3;
    private static final int PROGRESS_BAR_Y = 28;

    private final ItemStack icon;
    private final Component title;

    @Nullable
    private final Component message;

    private final boolean progressable;

    private Visibility visibility = Visibility.SHOW;

    private long lastProgressTime;
    private float lastProgress;
    private float progress;

    public PortableToast(ItemStack icon, Component title, @Nullable Component message, boolean progressable) {
        this.icon = icon.copy();
        this.title = title;
        this.message = message;
        this.progressable = progressable;
    }

    @Override
    @NotNull
    public Visibility render(GuiGraphics guiGraphics, @NotNull ToastComponent toastComponent, long timeSinceVisible) {
        guiGraphics.blitSprite(
                BACKGROUND_SPRITE,
                0,
                0,
                width(),
                height()
        );

        renderIcon(guiGraphics);

        if (message == null) {
            guiGraphics.drawString(
                    toastComponent.getMinecraft().font,
                    title,
                    30,
                    12,
                    16746751 | 0xFF000000,
                    false
            );
        } else {
            guiGraphics.drawString(
                    toastComponent.getMinecraft().font,
                    title,
                    30,
                    7,
                    16746751 | 0xFF000000,
                    false
            );

            guiGraphics.drawString(
                    toastComponent.getMinecraft().font,
                    message,
                    30,
                    18,
                    -1,
                    false
            );
        }

        renderProgressBar(guiGraphics, timeSinceVisible);

        return visibility;
    }

    private static final float ICON_SCALE = 1.25F;

    private void renderIcon(GuiGraphics guiGraphics) {
        if (icon.isEmpty()) return;

        var poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate(6.0F, 6.0F, 0.0F);
        poseStack.scale(ICON_SCALE, ICON_SCALE, 1.0F);

        guiGraphics.renderItem(icon, 0, 0);

        poseStack.popPose();
    }

    private void renderProgressBar(GuiGraphics guiGraphics, long timeSinceVisible) {
        if (!progressable) return;

        // guiGraphics.fill(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_X + PROGRESS_BAR_WIDTH, PROGRESS_BAR_Y + 1, -1);
        guiGraphics.fill(PROGRESS_BAR_X, PROGRESS_BAR_Y, PROGRESS_BAR_X + PROGRESS_BAR_WIDTH, PROGRESS_BAR_Y + 1, -1);

        float displayedProgress = Mth.clampedLerp(lastProgress, progress, (float) (timeSinceVisible - lastProgressTime) / 100.0F);
        int progressColor = progress >= lastProgress
                ? -16755456
                : -11206656;

        guiGraphics.fill(
                PROGRESS_BAR_X,
                PROGRESS_BAR_Y,
                (int) (PROGRESS_BAR_X
                        + PROGRESS_BAR_WIDTH * displayedProgress),
                PROGRESS_BAR_Y + 1,
                progressColor
        );

        lastProgress = displayedProgress;
        lastProgressTime = timeSinceVisible;
    }

    public void updateProgress(float progress) {
        this.progress = Mth.clamp(progress, 0.0F, 1.0F);
    }

    public void hide() {
        visibility = Visibility.HIDE;
    }
}
