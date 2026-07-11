package at.roteklaue.portabletunes.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class TapeDeck extends Block {
    public TapeDeck() {
        super(BlockBehaviour.Properties.of()
                .ignitedByLava()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.IGNORE));
    }
}
