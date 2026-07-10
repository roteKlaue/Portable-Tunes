package at.roteklaue.portabletunes.items;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortableTabs {
    static Map<ResourceLocation, List<Holder<Item>>> MAPPED_ITEMS = new HashMap<>();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PortableTunes.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PORTABLE_TUNES = CREATIVE_MODE_TABS.register("portable_tunes", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.portable_tunes"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> PortableItems.CASSETTE.get().getDefaultInstance())
                    .build()
    );

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        MAPPED_ITEMS
                .getOrDefault(event.getTabKey().location(), List.of())
                .forEach(holder -> event.accept(holder.value()));
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
