package at.roteklaue.portabletunes.client.commands;

import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.client.audio.AudioManager;
import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@EventBusSubscriber(modid = PortableTunes.MODID, value = Dist.CLIENT)
public class PortableClientCommands {
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("show-toast")
                        .executes(context -> {

                            // var toast = new ToastUtil.Toast(
                            //         ResourceLocation.fromNamespaceAndPath(PortableTunes.MODID, "item/cassette"),
                            //         Component.literal("Portable Tunes"),
                            //         Component.literal("Playing cassette..."),
                            //         true
                            // );
//
                            // ToastUtil.show(toast);
                            // ToastUtil.updateProgress(toast, 0.5F);

                            return Command.SINGLE_SUCCESS;
                        })

        );

        event.getDispatcher().register(Commands.literal("play-music-test")
                .executes(context -> {
            var minecraft = Minecraft.getInstance();
            var player = minecraft.player;

            if (player == null) {
                context.getSource().sendFailure(Component.literal("No client player is available."));
                return 0;
            }

            var heldStack = player.getMainHandItem();
            if (heldStack.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Hold a music disc in your main hand."));
                return 0;
            }

            var playable = heldStack.get(DataComponents.JUKEBOX_PLAYABLE);
            if (playable == null) {
                context.getSource().sendFailure(Component.literal("The held item is not playable."));
                return 0;
            }



                    AudioManager.play(heldStack);

            context.getSource().sendSuccess(
                    () -> Component.literal("Playing " + heldStack.getHoverName().getString()),
                    false
            );

            return Command.SINGLE_SUCCESS;
        }));
    }
}
