package at.roteklaue.portabletunes.client.renderers;

import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.blocks.entities.PortableBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = PortableTunes.MODID, value = Dist.CLIENT)
public class PortableBlockEntityRenderers {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                PortableBlockEntities.TAPE_DECK.get(),
                TapeDeckBlockEntityRenderer::new
        );
    }
}
