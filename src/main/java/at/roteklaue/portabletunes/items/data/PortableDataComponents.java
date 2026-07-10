package at.roteklaue.portabletunes.items.data;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PortableDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(
                    Registries.DATA_COMPONENT_TYPE,
                    PortableTunes.MODID
            );

    public static final DeferredHolder<
                DataComponentType<?>,
                DataComponentType<CassetteContents>
                > CASSETTE_CONTENTS = COMPONENTS.registerComponentType(
            "cassette_contents",
            builder -> builder
                    .persistent(CassetteContents.CODEC)
                    .networkSynchronized(CassetteContents.STREAM_CODEC)
    );

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
