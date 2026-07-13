package at.roteklaue.portabletunes.client.menus;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class PortableMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, PortableTunes.MODID);

    public static final Supplier<MenuType<TapeDeckMenu>> TAPE_DECK =
            MENUS.register("tape_deck", () -> new MenuType<>(
                    TapeDeckMenu::new,
                    FeatureFlags.DEFAULT_FLAGS
            ));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    @EventBusSubscriber(modid = PortableTunes.MODID, value = Dist.CLIENT)
    public static class PortableScreens {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(PortableMenus.TAPE_DECK.get(), TapeDeckScreen::new);
        }
    }
}
