package at.roteklaue.portabletunes.client;

import at.roteklaue.portabletunes.PortableTunes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(
        value = PortableTunes.MODID,
        dist = Dist.CLIENT
)
public class PortableTunesClient {
    public PortableTunesClient(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                ConfigurationScreen::new
        );
    }
}
