package at.roteklaue.portabletunes.client.menus;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class TapeDeckScreen
        extends AbstractContainerScreen<TapeDeckMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(PortableTunes.MODID, "textures/gui/container/tape_deck_background.png");

    public TapeDeckScreen(TapeDeckMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

    }
}
