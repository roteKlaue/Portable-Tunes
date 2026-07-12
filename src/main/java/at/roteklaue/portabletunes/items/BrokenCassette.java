package at.roteklaue.portabletunes.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class BrokenCassette extends StaticTooltipItem {
    private static final int REPAIR_DURATION = 5 * 20;

    public BrokenCassette() {
        super(new Properties(), List.of("item.portable_tunes.broken_cassette.desc", "item.portable_tunes.restore_state"));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level,
                                                  @Nonnull Player player,
                                                  @Nonnull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }


    @Override
    public int getUseDuration(@Nonnull ItemStack stack,
                              @Nonnull LivingEntity entity) {
        return REPAIR_DURATION;
    }

    @Override
    @Nonnull
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    @Nonnull
    public ItemStack finishUsingItem(@Nonnull ItemStack stack,
                                     @Nonnull Level level,
                                     @Nonnull LivingEntity entity) {
        if (level.isClientSide()) return stack;

        level.playSound(
                null,
                entity.blockPosition(),
                SoundEvents.WOOL_PLACE,
                SoundSource.PLAYERS,
                0.8F,
                1.2F
        );

        if (entity instanceof Player player) {
            stack.shrink(1);

            ItemStack repaired = new ItemStack(PortableItems.CASSETTE.get());

            if (!player.getInventory().add(repaired)) {
                player.drop(repaired, false);
            }
        }

        return stack;
    }
}
