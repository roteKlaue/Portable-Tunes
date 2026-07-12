package at.roteklaue.portabletunes;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = PortableTunes.MODID)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER =
            new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue OUTPUT_DISC_DAMAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue OUTPUT_CASSETTE_DAMAGE_CHANCE;
    public static final ModConfigSpec.DoubleValue INPUT_DISC_DAMAGE_CHANCE;
    public static final ModConfigSpec.IntValue MIN_MIXTAPE_LENGTH;
    public static final ModConfigSpec.IntValue MAX_MIXTAPE_LENGTH;

    static {
        BUILDER.push("tape_deck");

        OUTPUT_DISC_DAMAGE_CHANCE = BUILDER
                .comment(
                        "Chance that the destination music disc breaks while being written.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.output_disc_damage_chance")
                .defineInRange("outputDiscDamageChance", 0.1, 0.0, 1.0);

        OUTPUT_CASSETTE_DAMAGE_CHANCE = BUILDER
                .comment(
                        "Chance that the destination cassette breaks while being recorded.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.output_cassette_damage_chance")
                .defineInRange("outputCassetteDamageChance", 0.1, 0.0, 1.0);

        INPUT_DISC_DAMAGE_CHANCE = BUILDER
                .comment(
                        "Chance that the source music disc breaks while being copied.",
                        "0.0 means it never breaks; 1.0 means it always breaks."
                )
                .translation("config.portable_tunes.input_disc_damage_chance")
                .defineInRange("inputDiscDamageChance", 0.05, 0.0, 1.0);

        BUILDER.pop();

        BUILDER.push("mixtape");

        MIN_MIXTAPE_LENGTH = BUILDER
                .comment("The minimum number of tracks required for a mixtape.")
                .translation("config.portable_tunes.min_mixtape_length")
                .defineInRange("minMixtapeLength", 1, 1, Integer.MAX_VALUE);

        MAX_MIXTAPE_LENGTH = BUILDER
                .comment("The maximum number of tracks allowed on a mixtape.")
                .translation("config.portable_tunes.max_mixtape_length")
                .defineInRange("maxMixtapeLength", 8, 1, Integer.MAX_VALUE);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
