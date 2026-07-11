package at.roteklaue.portabletunes.items;

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

    public static boolean isEmpty(ItemStack stack) {
        CassetteContents cassetteContents = stack.get(PortableDataComponents.CASSETTE_CONTENTS.get());
        if (null == cassetteContents) {
            return true;
        }
        return cassetteContents.isEmpty();
    }

    @Nonnull
    public static List<JukeboxPlayable> getSongs(@Nonnull ItemStack stack) {
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

        if (contents.isFull()) return false;

        CassetteContents updatedContents = contents.add(song);
        stack.set(
                PortableDataComponents.CASSETTE_CONTENTS.get(),
                updatedContents
        );

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

        return true;
    }

    public static boolean saveMixtape(ServerLevel level, ItemStack stack, String name, String playerUUID, String playerName) {
        var songKeys = getSongKeys(stack);
        if (songKeys.isEmpty()) {
            stack.remove(PortableDataComponents.MIX_TAPE_DATA.get());
            return false;
        }

        var worldData = PortableWorldData.get(level.getServer());
        var mixTapeData = worldData.getMixTape(songKeys);
        if (mixTapeData != null) {
            return false;
        }

        worldData.putMixTape(songKeys, playerUUID, playerName, name);
        return true;
    }

    @Nonnull
    public Component getName(@Nonnull ItemStack stack) {
        if (Cassette.isEmpty(stack))
            return Component.translatable("item.portable_tunes.cassette.blank");

        var mixTapeData = stack.get(PortableDataComponents.MIX_TAPE_DATA.get());
        if (mixTapeData == null || mixTapeData.mixtapeName() == null || mixTapeData.mixtapeName().isBlank()) {
            return super.getName(stack);
        }

        return Component.translatable("item.portable_tunes.cassette.named_mixtape", mixTapeData.mixtapeName());
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

        var songKeys = getSongKeys(stack);
        if (songKeys.isEmpty()) {
            stack.remove(PortableDataComponents.MIX_TAPE_DATA.get());
            return;
        }

        var worldData = PortableWorldData.get(serverLevel.getServer());
        var mixTapeData = worldData.getMixTape(songKeys);
        if (mixTapeData == null) {
            stack.remove(PortableDataComponents.MIX_TAPE_DATA.get());
            return;
        }

        var currentData = stack.get(PortableDataComponents.MIX_TAPE_DATA.get());
        if (mixTapeData.equals(currentData)) return;

        stack.set(PortableDataComponents.MIX_TAPE_DATA.get(), mixTapeData);
    }

    @Nonnull
    private static List<String> getSongKeys(@Nonnull ItemStack stack) {
        return Cassette.getSongs(stack)
                .stream()
                .map(p -> p.song().key().location().toString())
                .toList();
    }


    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nonnull Item.TooltipContext context,
                                @Nonnull List<Component> tooltipComponents,
                                @Nonnull TooltipFlag tooltipFlag) {
        if (Cassette.isEmpty(stack)) return;
        var mixTapeData = stack.get(PortableDataComponents.MIX_TAPE_DATA.get());
        if (mixTapeData != null) {
            tooltipComponents.add(Component.translatable("item.portable_tunes.cassette.mixtape_by", mixTapeData.cachedPlayerName())
                    .withStyle(ChatFormatting.BLUE));
        }
        var songs = getSongs(stack);
        songs.forEach(e -> e.addToTooltip(context, tooltipComponents::add, tooltipFlag));
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        var mixTapeData = stack.get(PortableDataComponents.MIX_TAPE_DATA.get());
        return mixTapeData != null;
    }
}
