package at.roteklaue.portabletunes.items.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortableWorldData extends SavedData {
    private static final String FILE_NAME = "portable_tunes";

    private final Map<List<String>, MixTapeData> mixTapes = new HashMap<>();

    public static PortableWorldData create() {
        return new PortableWorldData();
    }

    public int clearMixTapes() {
        if (this.mixTapes.isEmpty()) {
            return 0;
        }

        var removedCount = this.mixTapes.size();

        this.mixTapes.clear();
        this.setDirty();

        return removedCount;
    }

    public static PortableWorldData load(
            CompoundTag tag,
            HolderLookup.Provider lookupProvider
    ) {
        var data = create();
        var entries = tag.getList("Entries", Tag.TAG_COMPOUND);

        for (var element : entries) {
            if (!(element instanceof CompoundTag entryTag)) {
                continue;
            }

            var keyTag = entryTag.getList("Key", Tag.TAG_STRING);
            var key = new ArrayList<String>();

            for (var keyElement : keyTag) {
                key.add(keyElement.getAsString());
            }

            var mixTapeData = new MixTapeData(
                    entryTag.getString("PlayerUUID"),
                    entryTag.getString("CachedPlayerName"),
                    entryTag.getString("MixtapeName")
            );

            data.mixTapes.put(List.copyOf(key), mixTapeData);
        }

        return data;
    }

    public static PortableWorldData get(MinecraftServer server) {
        return server.overworld()
                .getDataStorage()
                .computeIfAbsent(
                        new Factory<>(
                                PortableWorldData::create,
                                PortableWorldData::load
                        ),
                        FILE_NAME
                );
    }

    @Override
    @Nonnull
    public CompoundTag save(
            @Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider lookupProvider
    ) {
        var entries = new ListTag();

        for (var entry : this.mixTapes.entrySet()) {
            var entryTag = new CompoundTag();
            var keyList = new ListTag();

            for (var value : entry.getKey()) {
                keyList.add(StringTag.valueOf(value));
            }

            entryTag.put("Key", keyList);
            entryTag.putString(
                    "PlayerUUID",
                    entry.getValue().playerUUID()
            );
            entryTag.putString(
                    "CachedPlayerName",
                    entry.getValue().cachedPlayerName()
            );
            entryTag.putString(
                    "MixtapeName",
                    entry.getValue().mixtapeName()
            );

            entries.add(entryTag);
        }

        tag.put("Entries", entries);

        return tag;
    }

    public void putMixTape(
            List<String> songs,
            String playerUUID,
            String playerName,
            String mixtapeName
    ) {
        var key = List.copyOf(songs);
        var value = new MixTapeData(playerUUID, playerName, mixtapeName);

        if (value.equals(this.mixTapes.get(key))) {
            return;
        }

        this.mixTapes.put(key, value);
        this.setDirty();
    }

    public MixTapeData getMixTape(List<String> songs) {
        return this.mixTapes.get(songs);
    }

    public boolean containsMixTape(List<String> songs) {
        return this.mixTapes.containsKey(songs);
    }

    public void removeMixTape(List<String> songs) {
        if (this.mixTapes.remove(songs) == null) {
            return;
        }

        this.setDirty();
    }

    public Map<List<String>, MixTapeData> getMixTapes() {
        return Map.copyOf(this.mixTapes);
    }

    public record MixTapeData(
            String playerUUID,
            String cachedPlayerName,
            String mixtapeName
    ) {
        public static final Codec<MixTapeData> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        Codec.STRING
                                .fieldOf("player_uuid")
                                .forGetter(MixTapeData::playerUUID),
                        Codec.STRING
                                .fieldOf("cached_player_name")
                                .forGetter(MixTapeData::cachedPlayerName),
                        Codec.STRING
                                .fieldOf("mixtape_name")
                                .forGetter(MixTapeData::mixtapeName)
                ).apply(instance, MixTapeData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MixTapeData>
                STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                MixTapeData::playerUUID,
                ByteBufCodecs.STRING_UTF8,
                MixTapeData::cachedPlayerName,
                ByteBufCodecs.STRING_UTF8,
                MixTapeData::mixtapeName,
                MixTapeData::new
        );
    }
}
