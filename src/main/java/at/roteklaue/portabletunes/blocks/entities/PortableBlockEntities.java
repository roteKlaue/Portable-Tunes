package at.roteklaue.portabletunes.blocks.entities;

import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.blocks.PortableBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PortableBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, PortableTunes.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TapeDeckBlockEntity>> TAPE_DECK =
            registerBlockEntity("tape_deck", TapeDeckBlockEntity::new, PortableBlocks.TAPE_DECK);

    public static <T extends BlockEntity, B extends Block>
    DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(
            String name,
            BlockEntityType.BlockEntitySupplier<T> supplier,
            DeferredBlock<B> block
    ) {
        return ENTITY_TYPES.register(
                name.toLowerCase(),
                () -> BlockEntityType.Builder.of(supplier, block.get()).build(null)
        );
    }

    public static void register(IEventBus bus) {
        ENTITY_TYPES.register(bus);
    }
}
