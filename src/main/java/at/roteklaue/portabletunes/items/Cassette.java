package at.roteklaue.portabletunes.items;

import at.roteklaue.portabletunes.Config;
import at.roteklaue.portabletunes.items.data.CassetteContents;
import at.roteklaue.portabletunes.items.data.PortableDataComponents;
import at.roteklaue.portabletunes.items.data.PortableWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class Cassette extends Item {
    public Cassette() {
        super(new Item.Properties()
                .stacksTo(16)
                .component(PortableDataComponents.CASSETTE_CONTENTS.get(),
                        CassetteContents.EMPTY));
    }

    private static CassetteContents getContents(ItemStack stack) {
        return stack.getOrDefault(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                CassetteContents.EMPTY
        );
    }

    public static boolean isEmpty(ItemStack stack) {
        return getContents(stack).isEmpty();
    }

    @Nonnull
    public static List<JukeboxPlayable> getSongs(@Nonnull ItemStack stack) {
        return getContents(stack).songs();
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
        CassetteContents contents = getContents(stack);

        if (contents.isFull()) return false;
        CassetteContents updatedContents = contents.add(song.withTooltip(true));
        stack.set(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                updatedContents
        );

        stack.remove(PortableDataComponents.MIXTAPE_DATA.get());
        return true;
    }

    public static boolean removeSong(
            @Nonnull ItemStack stack,
            int index
    ) {
        CassetteContents contents = getContents(stack);
        if (index < 0 || index >= contents.size()) return false;

        CassetteContents updatedContents = contents.remove(index);

        stack.set(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                updatedContents
        );

        stack.remove(PortableDataComponents.MIXTAPE_DATA.get());
        return true;
    }

    public static boolean saveMixtape(
            @Nonnull ServerLevel level,
            @Nonnull ItemStack stack,
            @Nonnull String name,
            @Nonnull String playerUUID,
            @Nonnull String playerName
    ) {
        var songKeys = getSongKeys(stack);
        if (songKeys.isEmpty()) {
            stack.remove(PortableDataComponents.MIXTAPE_DATA.get());
            return false;
        }

        name = name.trim();
        if (name.isEmpty()) return false;

        var worldData = PortableWorldData.get(level.getServer());
        var mixtapeData = worldData.getMixtape(songKeys);
        if (mixtapeData != null) return false;

        worldData.putMixtape(songKeys, playerUUID, playerName, name);
        mixtapeData = worldData.getMixtape(songKeys);
        if (mixtapeData == null) return false;

        stack.set(
                PortableDataComponents.MIXTAPE_DATA.get(),
                mixtapeData
        );

        return true;
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        if (Cassette.isEmpty(stack))
            return Component.translatable("item.portable_tunes.cassette.blank");

        var mixtapeData = stack.get(PortableDataComponents.MIXTAPE_DATA.get());
        if (mixtapeData == null || mixtapeData.mixtapeName() == null || mixtapeData.mixtapeName().isBlank())
            return super.getName(stack);

        return Component.translatable("item.portable_tunes.cassette.named_mixtape", mixtapeData.mixtapeName());
    }

    @Override
    public void inventoryTick(
            @Nonnull ItemStack stack,
            @Nonnull Level level,
            @Nonnull Entity entity,
            int itemSlot,
            boolean isSelected
    ) {
        if (!(level instanceof ServerLevel serverLevel))
            return;

        if ((level.getGameTime() + itemSlot) % 20 != 0) return;
        updateMixtapeData(serverLevel, stack);
    }

    private static void updateMixtapeData(
            ServerLevel level,
            ItemStack stack
    ) {
        var songKeys = getSongKeys(stack);

        if (songKeys.isEmpty()) {
            stack.remove(PortableDataComponents.MIXTAPE_DATA.get());
            return;
        }

        var worldData = PortableWorldData.get(level.getServer());
        var mixtapeData = worldData.getMixtape(songKeys);

        if (mixtapeData == null) {
            stack.remove(PortableDataComponents.MIXTAPE_DATA.get());
            return;
        }

        var currentData = stack.get(PortableDataComponents.MIXTAPE_DATA.get());

        if (mixtapeData.equals(currentData)) return;
        stack.set(
                PortableDataComponents.MIXTAPE_DATA.get(),
                mixtapeData
        );
    }

    @Nonnull
    private static List<String> getSongKeys(@Nonnull ItemStack stack) {
        return Cassette.getSongs(stack)
                .stream()
                .map(playable -> playable.song().key().location().toString())
                .toList();
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nonnull Item.TooltipContext context,
                                @Nonnull List<Component> tooltipComponents,
                                @Nonnull TooltipFlag tooltipFlag) {
        if (Cassette.isEmpty(stack)) {
            tooltipComponents.add(Component.translatable("item.portable_tunes.cassette.empty", Config.getMaximumMixtapeLength())
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        var mixtapeData = stack.get(PortableDataComponents.MIXTAPE_DATA.get());
        if (mixtapeData != null) {
            tooltipComponents.add(Component.translatable("item.portable_tunes.cassette.mixtape_by", mixtapeData.cachedPlayerName())
                    .withStyle(ChatFormatting.BLUE));
        }
        var songs = getSongs(stack);
        songs.forEach(song -> song.addToTooltip(context, tooltipComponents::add, tooltipFlag));
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return stack.has(PortableDataComponents.MIXTAPE_DATA.get());
    }
}
