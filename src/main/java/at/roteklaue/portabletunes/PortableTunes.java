package at.roteklaue.portabletunes;

import at.roteklaue.portabletunes.blocks.PortableBlocks;
import at.roteklaue.portabletunes.blocks.entities.PortableBlockEntities;
import at.roteklaue.portabletunes.client.menus.PortableMenus;
import at.roteklaue.portabletunes.commands.PortableCommands;
import at.roteklaue.portabletunes.items.PortableItems;
import at.roteklaue.portabletunes.items.PortableTabs;
import at.roteklaue.portabletunes.items.data.PortableDataComponents;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(PortableTunes.MODID)
public class PortableTunes {
    public static final String MODID = "portable_tunes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public PortableTunes(IEventBus modEventBus, ModContainer modContainer) {
        PortableDataComponents.register(modEventBus);
        PortableItems.register(modEventBus);
        PortableBlocks.register(modEventBus);
        PortableTabs.register(modEventBus);
        PortableBlockEntities.register(modEventBus);
        PortableMenus.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(PortableCommands::register);
        modEventBus.addListener(PortableTabs::addCreative);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.ClientConfig.CLIENT_SPEC);
    }
}
