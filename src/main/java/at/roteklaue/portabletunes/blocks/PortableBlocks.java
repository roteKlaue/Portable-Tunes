package at.roteklaue.portabletunes.blocks;

import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.items.PortableItems;
import at.roteklaue.portabletunes.items.PortableTabs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class PortableBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PortableTunes.MODID);

    public static final DeferredBlock<TapeDeck> TAPE_DECK = registerBlock("tape_deck", TapeDeck::new,
            List.of(PortableTabs.PORTABLE_TUNES.getId(), CreativeModeTabs.BUILDING_BLOCKS.location()));

    public static <T extends Block> DeferredBlock<T> registerBlock(String identifier, Supplier<T> supplier) {
        return registerBlock(identifier, supplier, List.of());
    }

    public static <T extends Block> DeferredBlock<T> registerBlock(String identifier, Supplier<T> supplier, List<ResourceLocation> creativeTabs) {
        var block = BLOCKS.register(identifier, supplier);
        registerBlockItem(identifier, block, creativeTabs);
        return block;
    }

    public static @NotNull DeferredItem<BlockItem> registerBlockItem(@NotNull String identifier,
                                                                     @NotNull DeferredBlock<?> block,
                                                                     @NotNull List<ResourceLocation> creativeTabs) {
        return PortableItems.registerItem(identifier, () -> new BlockItem(block.get(), new Item.Properties()), creativeTabs);
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
