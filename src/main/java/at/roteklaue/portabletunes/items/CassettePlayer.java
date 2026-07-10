package at.roteklaue.portabletunes.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class CassettePlayer extends Item {
    public CassettePlayer() {
        super(new Item.Properties()
                .stacksTo(1));
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack,
                              @Nonnull Level level,
                              @Nonnull Entity entity,
                              int itemSlot,
                              boolean isSelected) {
        if (level instanceof ServerLevel serverlevel) {

        }
    }
}
