package at.roteklaue.portabletunes.items.data;

import at.roteklaue.portabletunes.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.JukeboxPlayable;

import java.util.ArrayList;
import java.util.List;

public record CassetteContents(List<JukeboxPlayable> songs) {
    public static final int TECHNICAL_MAX_SONGS = 64;

    public static final CassetteContents EMPTY =
            new CassetteContents(List.of());

    public static final Codec<CassetteContents> CODEC =
            JukeboxPlayable.CODEC
                    .listOf()
                    .validate(CassetteContents::validateSongs)
                    .xmap(CassetteContents::new, CassetteContents::songs);

    public static final StreamCodec<RegistryFriendlyByteBuf, CassetteContents> STREAM_CODEC =
            JukeboxPlayable.STREAM_CODEC
                    .apply(ByteBufCodecs.list(TECHNICAL_MAX_SONGS))
                    .map(CassetteContents::new, CassetteContents::songs);

    public CassetteContents {
        songs = List.copyOf(songs);

        if (songs.size() > TECHNICAL_MAX_SONGS) {
            throw new IllegalArgumentException("A cassette cannot contain more than " + TECHNICAL_MAX_SONGS + " songs");
        }
    }

    private static DataResult<List<JukeboxPlayable>> validateSongs(List<JukeboxPlayable> songs) {
        if (songs.size() <= Math.min(TECHNICAL_MAX_SONGS, Config.getMaximumMixtapeLength())) {
            return DataResult.success(songs);
        }

        return DataResult.error(() ->
                "A cassette cannot contain more than " + Math.min(TECHNICAL_MAX_SONGS, Config.getMaximumMixtapeLength()) + " songs"
        );
    }

    public boolean isFull() {
        return songs.size() >= Math.min(TECHNICAL_MAX_SONGS, Config.getMaximumMixtapeLength());
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public int size() {
        return songs.size();
    }

    public CassetteContents add(JukeboxPlayable song) {
        if (isFull()) return this;

        List<JukeboxPlayable> updatedSongs = new ArrayList<>(songs);
        updatedSongs.add(song);

        return new CassetteContents(updatedSongs);
    }

    public CassetteContents remove(int index) {
        if (index < 0 || index >= songs.size()) return this;

        List<JukeboxPlayable> updatedSongs = new ArrayList<>(songs);
        updatedSongs.remove(index);

        return new CassetteContents(updatedSongs);
    }

    public JukeboxPlayable get(int index) {
        if (index < 0 || index >= songs.size()) return null;
        return songs.get(index);
    }
}
