package at.roteklaue.portabletunes;

import at.roteklaue.portabletunes.client.event.ClientConfigChangedEvent;
import at.roteklaue.portabletunes.items.data.CassetteContents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Objects;

public final class Config {
    private static final ModConfigSpec.Builder SERVER_CONFIG_BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue OUTPUT_DISC_DAMAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue OUTPUT_CASSETTE_DAMAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue INPUT_DISC_DAMAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue INPUT_DISC_INTERRUPTION_DAMAGE_CHANCE;
    public static final ModConfigSpec.IntValue TRANSCRIPTION_TIME;
    public static final ModConfigSpec.IntValue MIN_MIXTAPE_LENGTH;
    public static final ModConfigSpec.IntValue MAX_MIXTAPE_LENGTH;

    static {
        SERVER_CONFIG_BUILDER.push("tape_deck");

        OUTPUT_DISC_DAMAGE_CHANCE = SERVER_CONFIG_BUILDER
                .comment(
                        "Chance that the destination music disc breaks after being written.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.output_disc_damage_chance")
                .defineInRange("outputDiscDamageChance", 0.05, 0.0, 1.0);

        OUTPUT_CASSETTE_DAMAGE_CHANCE = SERVER_CONFIG_BUILDER
                .comment(
                        "Chance that the destination cassette breaks after being recorded.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.output_cassette_damage_chance")
                .defineInRange("outputCassetteDamageChance", 0.05, 0.0, 1.0);

        INPUT_DISC_DAMAGE_CHANCE = SERVER_CONFIG_BUILDER
                .comment(
                        "Chance that the source music disc breaks after being copied.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.input_disc_damage_chance")
                .defineInRange("inputDiscDamageChance", 0.025, 0.0, 1.0);

        INPUT_DISC_INTERRUPTION_DAMAGE_CHANCE = SERVER_CONFIG_BUILDER
                .comment(
                        "Chance that the source music disc breaks if the copy process is interrupted.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.input_disc_interruption_damage_chance")
                .defineInRange("inputDiscInterruptionDamageChance", 0.5, 0.0, 1.0);

        TRANSCRIPTION_TIME = SERVER_CONFIG_BUILDER
                .comment("The duration in seconds of how long the transcription process takes.")
                .translation("config.portable_tunes.transcription_time")
                .defineInRange("transcriptionTime", 5, 1, Integer.MAX_VALUE);

        SERVER_CONFIG_BUILDER.pop();

        SERVER_CONFIG_BUILDER.push("mixtape");

        MIN_MIXTAPE_LENGTH = SERVER_CONFIG_BUILDER
                .comment("The minimum number of tracks required for a mixtape.")
                .translation("config.portable_tunes.min_mixtape_length")
                .defineInRange("minMixtapeLength", 1, 1, CassetteContents.TECHNICAL_MAX_SONGS);

        MAX_MIXTAPE_LENGTH = SERVER_CONFIG_BUILDER
                .comment("The maximum number of tracks allowed on a mixtape.")
                .translation("config.portable_tunes.max_mixtape_length")
                .defineInRange("maxMixtapeLength", 8, 1, CassetteContents.TECHNICAL_MAX_SONGS);

        SERVER_CONFIG_BUILDER.pop();
    }

    public static final ModConfigSpec SERVER_SPEC = SERVER_CONFIG_BUILDER.build();

    public static int getMinimumMixtapeLength() {
        return Math.max(0, Math.min(MIN_MIXTAPE_LENGTH.get(), MAX_MIXTAPE_LENGTH.get()));
    }

    public static int getMaximumMixtapeLength() {
        return Math.max(MIN_MIXTAPE_LENGTH.get(), MAX_MIXTAPE_LENGTH.get());
    }

    @EventBusSubscriber(modid = PortableTunes.MODID, value = Dist.CLIENT)
    public static class ClientConfig {
        private static final ModConfigSpec.Builder CLIENT_CONFIG_BUILDER =
                new ModConfigSpec.Builder();

        public static final ModConfigSpec.BooleanValue SHOW_SONG_CHANGE_NOTIFICATION;

        static {
            CLIENT_CONFIG_BUILDER.push("cassette_player");

            SHOW_SONG_CHANGE_NOTIFICATION = CLIENT_CONFIG_BUILDER
                    .comment("Whether to display a notification when the current song changes.")
                    .translation("config.portable_tunes.show_song_change_notification")
                    .define("showSongChangeNotification", true);

            CLIENT_CONFIG_BUILDER.pop();
        }

        @SubscribeEvent
        public static void onConfigReloading(ModConfigEvent.Reloading event) {
            if (!Objects.equals(event.getConfig().getSpec(), CLIENT_CONFIG_BUILDER)) return;
            applyClientConfig();
        }

        private static void applyClientConfig() {
            NeoForge.EVENT_BUS.post(new ClientConfigChangedEvent());
        }

        @SubscribeEvent
        public static void onConfigLoading(ModConfigEvent.Loading event) {
            if (!Objects.equals(event.getConfig().getSpec(), CLIENT_CONFIG_BUILDER)) return;
            applyClientConfig();
        }

        public static final ModConfigSpec CLIENT_SPEC = CLIENT_CONFIG_BUILDER.build();
    }
}
