package at.roteklaue.portabletunes.client.audio;

import at.roteklaue.portabletunes.Config;
import at.roteklaue.portabletunes.PortableTunes;
import at.roteklaue.portabletunes.client.event.ClientConfigChangedEvent;
import at.roteklaue.portabletunes.client.toast.ToastUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = PortableTunes.MODID, value = Dist.CLIENT)
public class AudioManager {
    private static final int SOUND_START_TIMEOUT_TICKS = 20;

    private static final Map<UUID, ActivePlayback> ACTIVE_PLAYBACKS =
            new LinkedHashMap<>();

    @Nonnull
    public static Optional<PlaybackHandle> play(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();

        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;

        if (level == null) return Optional.empty();

        var songHolder = JukeboxSong.fromStack(level.registryAccess(), stack);
        if (songHolder.isEmpty()) return Optional.empty();

        var song = songHolder.get().value();
        int lengthInTicks = song.lengthInTicks();

        if (lengthInTicks <= 0) return Optional.empty();

        SoundInstance sound = AudioUtil.playNonPositionalSound(song.soundEvent().value(), 1.0F, 1.0F);

        var toast = new ToastUtil.Toast(
                stack.copyWithCount(1),
                Component.translatable("toast.portable_tunes.now_playing"),
                song.description(),
                true
        );

        UUID id = UUID.randomUUID();
        var handle = new PlaybackHandle(id);

        ACTIVE_PLAYBACKS.put(id, new ActivePlayback(handle, sound, toast, lengthInTicks));

        if (Config.ClientConfig.SHOW_SONG_CHANGE_NOTIFICATION.get()) ToastUtil.show(toast);
        ToastUtil.updateProgress(toast, 0.0F);

        return Optional.of(handle);
    }

    public static void stop(@Nonnull PlaybackHandle handle) {
        ActivePlayback playback = ACTIVE_PLAYBACKS.remove(handle.id());
        if (playback == null) return;

        closePlayback(playback);
    }

    public static void stopAll() {
        var soundManager = Minecraft.getInstance().getSoundManager();

        for (ActivePlayback playback : ACTIVE_PLAYBACKS.values()) {
            soundManager.stop(playback.sound);
            ToastUtil.hide(playback.toast);
        }

        ACTIVE_PLAYBACKS.clear();
    }

    public static boolean isPlaying(@Nonnull PlaybackHandle handle) {
        return ACTIVE_PLAYBACKS.containsKey(handle.id());
    }

    public static int getActivePlaybackCount() {
        return ACTIVE_PLAYBACKS.size();
    }

    @Nonnull
    public static Optional<Float> getProgress(@Nonnull PlaybackHandle handle) {
        ActivePlayback playback = ACTIVE_PLAYBACKS.get(handle.id());
        if (playback == null) return Optional.empty();

        return Optional.of(playback.getProgress());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ACTIVE_PLAYBACKS.isEmpty()) return;

        var minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            stopAll();
            return;
        }

        if (minecraft.isPaused()) return;

        var soundManager = minecraft.getSoundManager();
        var iterator = ACTIVE_PLAYBACKS.values().iterator();

        while (iterator.hasNext()) {
            ActivePlayback playback = iterator.next();

            playback.elapsedTicks++;

            float progress = playback.getProgress();

            ToastUtil.updateProgress(playback.toast, progress);

            boolean soundActive = soundManager.isActive(playback.sound);
            if (soundActive) playback.soundStarted = true;

            if (playback.elapsedTicks >= playback.lengthInTicks) {
                ToastUtil.updateProgress(playback.toast, 1.0F);

                closePlayback(playback);
                iterator.remove();
                continue;
            }

            if (soundActive) continue;
            if (!playback.soundStarted && playback.elapsedTicks <= SOUND_START_TIMEOUT_TICKS) continue;

            closePlayback(playback);
            iterator.remove();
        }
    }

    private static void closePlayback(@Nonnull ActivePlayback playback) {
        Minecraft.getInstance()
                .getSoundManager()
                .stop(playback.sound);

        ToastUtil.hide(playback.toast);
    }

    public record PlaybackHandle(UUID id) {
        public void stop() {
            AudioManager.stop(this);
        }

        public Optional<Float> getProgress() {
            return AudioManager.getProgress(this);
        }

        public boolean isPlaying() {
            return AudioManager.isPlaying(this);
        }
    }

    private static final class ActivePlayback {
        private final PlaybackHandle handle;
        private final SoundInstance sound;
        private final ToastUtil.Toast toast;
        private final int lengthInTicks;

        private int elapsedTicks;
        private boolean soundStarted;

        private ActivePlayback(PlaybackHandle handle, SoundInstance sound, ToastUtil.Toast toast, int lengthInTicks) {
            this.handle = handle;
            this.sound = sound;
            this.toast = toast;
            this.lengthInTicks = lengthInTicks;
        }

        private float getProgress() {
            return Math.min((float) elapsedTicks / lengthInTicks, 1.0F);
        }
    }

    @SubscribeEvent
    public static void onClientConfigChanged(ClientConfigChangedEvent event) {
        boolean show = Config.ClientConfig.SHOW_SONG_CHANGE_NOTIFICATION.get();
        if (show) return;
        for (ActivePlayback playback : ACTIVE_PLAYBACKS.values()) {
            ToastUtil.hide(playback.toast);
        }
    }
}
