package at.roteklaue.portabletunes.commands;

import at.roteklaue.portabletunes.items.Cassette;
import at.roteklaue.portabletunes.items.data.PortableWorldData;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class PortableCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("portabletunes")
                        .then(Commands.literal("add-song")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var source = context.getSource();
                                    var player = source.getPlayerOrException();
                                    var cassetteStack = player.getMainHandItem();
                                    var musicDiscStack = player.getOffhandItem();

                                    if (!(cassetteStack.getItem() instanceof Cassette)) {
                                        source.sendFailure(
                                                Component.translatable(
                                                        "commands.portable_tunes.hold_cassette"
                                                )
                                        );
                                        return 0;
                                    }

                                    var jukeboxPlayable = musicDiscStack.get(DataComponents.JUKEBOX_PLAYABLE);

                                    if (jukeboxPlayable == null) {
                                        source.sendFailure(
                                                Component.translatable(
                                                        "commands.portable_tunes.not_playable"
                                                )
                                        );
                                        return 0;
                                    }

                                    var songHolder = jukeboxPlayable.song()
                                            .unwrap(source.registryAccess())
                                            .orElse(null);

                                    if (songHolder == null) {
                                        source.sendFailure(
                                                Component.translatable(
                                                        "commands.portable_tunes.unknown_song"
                                                )
                                        );
                                        return 0;
                                    }

                                    if (!Cassette.addSong(cassetteStack, jukeboxPlayable)) {
                                        source.sendFailure(
                                                Component.translatable(
                                                        "commands.portable_tunes.cassette_full"
                                                )
                                        );
                                        return 0;
                                    }

                                    var songDescription = songHolder.value()
                                            .description()
                                            .copy();

                                    source.sendSuccess(
                                            () -> Component.translatable(
                                                    "commands.portable_tunes.add_song.success",
                                                    songDescription
                                            ),
                                            false
                                    );

                                    return 1;
                                }))
                        .then(
                                Commands.literal("save-mixtape")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                Commands.argument(
                                                        "name",
                                                        StringArgumentType.greedyString()
                                                ).executes(context -> {
                                                    var source = context.getSource();
                                                    var player = source.getPlayerOrException();
                                                    var stack = player.getMainHandItem();

                                                    if (!(stack.getItem() instanceof Cassette)) {
                                                        source.sendFailure(Component.translatable("commands.portable_tunes.hold_cassette"));
                                                        return 0;
                                                    }

                                                    var songs = Cassette.getSongs(stack);
                                                    if (songs.isEmpty()) {
                                                        source.sendFailure(Component.translatable("commands.portable_tunes.save_mixtape.no_songs"));
                                                        return 0;
                                                    }

                                                    var name = StringArgumentType.getString(
                                                            context,
                                                            "name"
                                                    );

                                                    boolean success = Cassette.saveMixtape(source.getLevel(), stack, name, player.getStringUUID(), player.getGameProfile().getName());
                                                    if (!success) {
                                                        source.sendFailure(Component.translatable("commands.portable_tunes.save_mixtape.already_mixtape_with_these_songs"));
                                                        return 0;
                                                    }

                                                    source.sendSuccess(() -> Component.translatable("commands.portable_tunes.save_mixtape.success",name,songs.size()), false);

                                                    return 1;
                                                })
                                        )
                        )
                        .then(Commands.literal("list-mixtapes")
                                .executes(context -> {
                                    var source = context.getSource();
                                    var worldData = PortableWorldData.get(
                                            source.getServer()
                                    );
                                    var mixTapes = worldData.getMixtapes();

                                    if (mixTapes.isEmpty()) {
                                        source.sendSuccess(
                                                () -> Component.translatable(
                                                        "commands.portable_tunes.list_mixtapes.empty"
                                                ),
                                                false
                                        );

                                        return 1;
                                    }

                                    source.sendSuccess(
                                            () -> Component.translatable(
                                                    "commands.portable_tunes.list_mixtapes.header",
                                                    mixTapes.size()
                                            ).withStyle(ChatFormatting.GOLD),
                                            false
                                    );

                                    var index = 1;

                                    for (var entry : mixTapes.entrySet()) {
                                        var mixTapeData = entry.getValue();
                                        var songCount = entry.getKey().size();

                                        var message = Component.literal(
                                                        index + ". "
                                                )
                                                .withStyle(ChatFormatting.GRAY)
                                                .append(
                                                        Component.literal(
                                                                mixTapeData.mixtapeName()
                                                        ).withStyle(
                                                                ChatFormatting.AQUA
                                                        )
                                                )
                                                .append(
                                                        Component.translatable(
                                                                "commands.portable_tunes.list_mixtapes.entry",
                                                                mixTapeData.cachedPlayerName(),
                                                                songCount
                                                        ).withStyle(
                                                                ChatFormatting.GRAY
                                                        )
                                                );

                                        source.sendSuccess(() -> message, false);
                                        index++;
                                    }

                                    return mixTapes.size();
                                })
                        )
                        .then(Commands.literal("clear-mixtapes")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    var source = context.getSource();
                                    var worldData = PortableWorldData.get(
                                            source.getServer()
                                    );
                                    var removedCount = worldData.clearMixtapes();

                                    if (removedCount == 0) {
                                        source.sendSuccess(
                                                () -> Component.translatable("commands.portable_tunes.clear_mixtapes.empty"),
                                                false
                                        );

                                        return 1;
                                    }

                                    source.sendSuccess(
                                            () -> Component.translatable(
                                                    "commands.portable_tunes.clear_mixtapes.success",
                                                    removedCount
                                            ),
                                            true
                                    );

                                    return removedCount;
                                })
                        )
        );
    }
}
