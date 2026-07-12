package at.roteklaue.portabletunes.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nonnull;
import java.util.List;

public class StaticTooltipItem extends Item {
    private final List<String> tooltips;
    private final ChatFormatting formatting;

    public StaticTooltipItem(
            Properties properties,
            List<String> tooltips
    ) {
        this(properties, tooltips, ChatFormatting.GRAY);
    }

    public StaticTooltipItem(
            Properties properties,
            List<String> tooltips,
            ChatFormatting formatting
    ) {
        super(properties);
        this.tooltips = List.copyOf(tooltips);
        this.formatting = formatting;
    }

    @Override
    public void appendHoverText(
            @Nonnull ItemStack stack,
            @Nonnull Item.TooltipContext context,
            @Nonnull List<Component> tooltipComponents,
            @Nonnull TooltipFlag tooltipFlag
    ) {
        for (String key : tooltips) {
            tooltipComponents.add(
                    Component.translatable(key)
                            .withStyle(formatting)
            );
        }
    }
}