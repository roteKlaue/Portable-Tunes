package at.roteklaue.portabletunes.client.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.IdentityHashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)

public class ToastUtil {
    private static final Map<Toast, PortableToast> ACTIVE_TOASTS =
            new IdentityHashMap<>();

    public static void show(Toast toast1) {
        if (ACTIVE_TOASTS.containsKey(toast1)) return;

        Minecraft minecraft = Minecraft.getInstance();

        PortableToast toast = new PortableToast(toast1.icon, toast1.title, toast1.description, toast1.hasProgressBar);

        ACTIVE_TOASTS.put(toast1, toast);
        minecraft.getToasts().addToast(toast);
    }

    public static void updateProgress(Toast toast1, float progress) {
        PortableToast toast = ACTIVE_TOASTS.get(toast1);
        if (toast == null) return;

        toast.updateProgress(Mth.clamp(progress, 0.0F, 1.0F));
    }

    public static void hide(Toast toast1) {
        PortableToast toast = ACTIVE_TOASTS.remove(toast1);
        if (toast == null) return;

        toast.hide();
    }

    public static boolean isVisible(Toast toast) {
        return ACTIVE_TOASTS.containsKey(toast);
    }

    public record Toast(ItemStack icon, Component title, Component description, boolean hasProgressBar) {
        public Toast(ItemStack icon, Component title, Component description) {
            this(icon, title, description, false);
        }
    }
}
