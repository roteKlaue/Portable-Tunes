package at.roteklaue.portabletunes.items;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PortableItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PortableTunes.MODID);

    public static final DeferredItem<Cassette> CASSETTE = registerItem("cassette", Cassette::new,
            List.of(PortableTabs.PORTABLE_TUNES.getId(), CreativeModeTabs.INGREDIENTS.location()));
    public static final DeferredItem<CassettePlayer> CASSETTE_PLAYER = registerItem("cassette_player", CassettePlayer::new,
            List.of(PortableTabs.PORTABLE_TUNES.getId()));
    public static final DeferredItem<Item> BLANK_DISC = registerItem("blank_disc", () -> new Item(new Item.Properties()),
            List.of(PortableTabs.PORTABLE_TUNES.getId(), CreativeModeTabs.INGREDIENTS.location()));

    public static<T extends Item> DeferredItem<T> registerItem(String identifier, Supplier<T> itemSupplier, List<ResourceLocation> creativeTabs) {
        var item = ITEMS.register(identifier, itemSupplier);
        creativeTabs.forEach(t -> PortableTabs.ITEMS_BY_TAB.computeIfAbsent(t, ignored -> new ArrayList<>()).add(item));
        return item;
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
