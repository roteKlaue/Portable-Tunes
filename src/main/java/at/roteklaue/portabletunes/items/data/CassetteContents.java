package at.roteklaue.portabletunes.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.JukeboxPlayable;

import java.util.ArrayList;
import java.util.List;

public record CassetteContents(List<JukeboxPlayable> songs) {
    public static final int MAX_SONGS = 8;

    public static final CassetteContents EMPTY =
            new CassetteContents(List.of());

    public static final Codec<CassetteContents> CODEC =
            JukeboxPlayable.CODEC
                    .listOf()
                    .validate(CassetteContents::validateSongs)
                    .xmap(CassetteContents::new, CassetteContents::songs);

    public static final StreamCodec<RegistryFriendlyByteBuf, CassetteContents> STREAM_CODEC =
            JukeboxPlayable.STREAM_CODEC
                    .apply(ByteBufCodecs.list(MAX_SONGS))
                    .map(CassetteContents::new, CassetteContents::songs);

    public CassetteContents {
        songs = List.copyOf(songs);

        if (songs.size() > MAX_SONGS) {
            throw new IllegalArgumentException(
                    "A cassette cannot contain more than " + MAX_SONGS + " songs"
            );
        }
    }

    private static DataResult<List<JukeboxPlayable>> validateSongs(
            List<JukeboxPlayable> songs
    ) {
        if (songs.size() <= MAX_SONGS) {
            return DataResult.success(songs);
        }

        return DataResult.error(() ->
                "A cassette cannot contain more than " + MAX_SONGS + " songs"
        );
    }

    public boolean isFull() {
        return songs.size() >= MAX_SONGS;
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public int size() {
        return songs.size();
    }

    public CassetteContents add(JukeboxPlayable song) {
        if (isFull()) {
            return this;
        }

        List<JukeboxPlayable> updatedSongs = new ArrayList<>(songs);
        updatedSongs.add(song);

        return new CassetteContents(updatedSongs);
    }

    public CassetteContents remove(int index) {
        if (index < 0 || index >= songs.size()) {
            return this;
        }

        List<JukeboxPlayable> updatedSongs = new ArrayList<>(songs);
        updatedSongs.remove(index);

        return new CassetteContents(updatedSongs);
    }

    public JukeboxPlayable get(int index) {
        if (index < 0 || index >= songs.size()) {
            return null;
        }

        return songs.get(index);
    }
}