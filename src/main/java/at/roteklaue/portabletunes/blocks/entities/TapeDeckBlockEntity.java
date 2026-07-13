package at.roteklaue.portabletunes.blocks.entities;

import at.roteklaue.portabletunes.client.menus.TapeDeckMenu;
import at.roteklaue.portabletunes.items.PortableItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TapeDeckBlockEntity
        extends NamedContainerProvider<TapeDeckBlockEntity> {
    public static final int INPUT_DISC_SLOT = 0;
    public static final int OUTPUT_DISC_SLOT = 1;
    public static final int CASSETTE_SLOT = 2;
    public static final int INVENTORY_SIZE = 3;
    private static final String INVENTORY_TAG = "TapeDeckInventory";

    private static final int MAX_PROGRESS = 100;
    private int progress;
    private boolean processing;

    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();

            if (level == null || level.isClientSide()) return;

            BlockState state = getBlockState();
            level.sendBlockUpdated(
                    worldPosition,
                    state,
                    state,
                    Block.UPDATE_CLIENTS
            );
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return switch (slot) {
                case INPUT_DISC_SLOT -> canPlaceInInputSlot(stack);
                case OUTPUT_DISC_SLOT -> canPlaceInOutputSlot(stack);
                case CASSETTE_SLOT -> canPlaceInCassetteSlot(stack);
                default -> false;
            };
        }

        private boolean canPlaceInCassetteSlot(ItemStack stack) {
            return stack.is(PortableItems.CASSETTE);
        }
        private boolean canPlaceInOutputSlot(ItemStack stack) {
            return canPlaceInInputSlot(stack) || stack.is(PortableItems.BLANK_DISC);
        }
        private boolean canPlaceInInputSlot(ItemStack stack) {
            return stack.has(DataComponents.JUKEBOX_PLAYABLE);
        }
    };

    public TapeDeckBlockEntity(
            BlockPos blockPos,
            BlockState blockState
    ) {
        super(
                PortableBlockEntities.TAPE_DECK.get(),
                blockPos,
                blockState,
                "tape_deck",
                TapeDeckMenu::new,
                TapeDeckBlockEntity.class
        );
        processing = false;
    }

    public static void tick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            TapeDeckBlockEntity blockEntity
    ) {
        if (level.isClientSide()) {
            return;
        }

        // blockEntity.serverTick();
    }

    // private void serverTick() {
    //     if (!canProcess()) {
    //         resetProgress();
    //         return;
    //     }
//
    //     progress++;
//
    //     if (progress < maxProgress) {
    //         return;
    //     }
//
    //     processItems();
    //     resetProgress();
    // }

    @Override
    protected void saveAdditional(
            @Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putBoolean("Processing", processing);
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }

        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.extractItem(
                    slot,
                    inventory.getSlotLimit(slot),
                    false
            );

            if (stack.isEmpty()) {
                continue;
            }

            Containers.dropItemStack(
                    level,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    stack
            );
        }
    }

    @Override
    protected void loadAdditional(
            @Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registries
    ) {
        super.loadAdditional(tag, registries);

        if (tag.contains(INVENTORY_TAG)) {
            inventory.deserializeNBT(
                    registries,
                    tag.getCompound(INVENTORY_TAG)
            );
        }

        progress = tag.getInt("Progress");
        processing = tag.getBoolean("Processing");
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag(
            @Nonnull HolderLookup.Provider registries
    ) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
