package at.roteklaue.portabletunes.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class AudioUtil {
    @Nonnull
    public static SoundInstance playNonPositionalSound(SoundEvent soundEvent, float volume, float pitch) {
        var minecraft = Minecraft.getInstance();

        var sound = new SimpleSoundInstance(
                soundEvent.getLocation(),
                SoundSource.RECORDS,
                volume,
                pitch,
                minecraft.level != null
                        ? minecraft.level.getRandom()
                        : RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true
        );

        Minecraft.getInstance()
                .getSoundManager()
                .play(sound);

        return sound;
    }

    @Nonnull
    public static Optional<SoundEvent> getSoundEvent(@Nonnull JukeboxPlayable playable, @Nonnull HolderLookup.Provider registries) {
        return playable.song()
                .unwrap(registries)
                .map(songHolder -> songHolder.value().soundEvent().value());
    }

    @Nonnull
    public static Optional<SoundEvent> getClientSoundEvent(@Nonnull JukeboxPlayable playable) {
        var level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();

        return getSoundEvent(playable, level.registryAccess());
    }

    @Nonnull
    public static Optional<Integer> getLengthInTicks(@Nonnull JukeboxPlayable playable, @Nonnull HolderLookup.Provider registries) {
        return playable.song()
                .unwrap(registries)
                .map(Holder::value)
                .map(JukeboxSong::lengthInTicks);
    }
}
