package at.roteklaue.portabletunes.items;

import at.roteklaue.portabletunes.items.data.CassetteContents;
import at.roteklaue.portabletunes.items.data.PortableDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;

import java.util.List;

public class Cassette extends Item {
    public Cassette() {
        super(new Item.Properties()
                .component(
                        PortableDataComponents.CASSETTE_CONTENTS.get(),
                        CassetteContents.EMPTY
                ));
    }

    public static List<JukeboxPlayable> getSongs(ItemStack stack) {
        CassetteContents contents = stack.getOrDefault(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                CassetteContents.EMPTY
        );

        return contents.songs();
    }

    public static boolean addSong(
            ItemStack stack,
            ResourceKey<JukeboxSong> song
    ) {
        return addSong(
                stack,
                new JukeboxPlayable(
                        new EitherHolder<>(song),
                        true
                )
        );
    }

    public static boolean addSong(
            ItemStack stack,
            JukeboxPlayable song
    ) {
        CassetteContents contents = stack.getOrDefault(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                CassetteContents.EMPTY
        );

        if (contents.isFull()) {
            return false;
        }

        CassetteContents updatedContents = contents.add(song);

        stack.set(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                updatedContents
        );

        /*
         * Make the first recorded song the currently playable song.
         */
        if (contents.isEmpty()) {
            stack.set(DataComponents.JUKEBOX_PLAYABLE, song);
        }

        return true;
    }

    public static boolean selectSong(
            ItemStack stack,
            int index
    ) {
        CassetteContents contents = stack.getOrDefault(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                CassetteContents.EMPTY
        );

        JukeboxPlayable song = contents.get(index);

        if (song == null) {
            return false;
        }

        stack.set(DataComponents.JUKEBOX_PLAYABLE, song);
        return true;
    }

    public static boolean removeSong(
            ItemStack stack,
            int index
    ) {
        CassetteContents contents = stack.getOrDefault(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                CassetteContents.EMPTY
        );

        if (index < 0 || index >= contents.size()) {
            return false;
        }

        CassetteContents updatedContents = contents.remove(index);

        stack.set(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                updatedContents
        );

        if (updatedContents.isEmpty()) {
            stack.remove(DataComponents.JUKEBOX_PLAYABLE);
            return true;
        }

        stack.set(
                DataComponents.JUKEBOX_PLAYABLE,
                updatedContents.songs().getFirst()
        );

        return true;
    }
}
