package at.roteklaue.portabletunes.blocks.entities;

import at.roteklaue.portabletunes.Config;
import at.roteklaue.portabletunes.client.menus.TapeDeckMenu;
import at.roteklaue.portabletunes.items.Cassette;
import at.roteklaue.portabletunes.items.PortableItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
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
    private double progress;
    private boolean processing;
    private static Double PROGRESS_PER_TICK = null;
    private static Double INPUT_DAMAGE_CHANCE = null;
    private static Double OUTPUT_DAMAGE_CHANCE = null;
    private static Double CASSETTE_DAMAGE_CHANCE = null;

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

    public TapeDeckBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(
                PortableBlockEntities.TAPE_DECK.get(),
                blockPos,
                blockState,
                "tape_deck",
                TapeDeckMenu::new,
                TapeDeckBlockEntity.class
        );
        processing = false;
        if (PROGRESS_PER_TICK == null) PROGRESS_PER_TICK = MAX_PROGRESS / 20.0 /* ticks */ / Config.TRANSCRIPTION_TIME.get();
        if (INPUT_DAMAGE_CHANCE == null) INPUT_DAMAGE_CHANCE = Config.INPUT_DISC_DAMAGE_CHANCE.get();
        if (OUTPUT_DAMAGE_CHANCE == null) OUTPUT_DAMAGE_CHANCE = Config.OUTPUT_DISC_DAMAGE_CHANCE.get();
        if (CASSETTE_DAMAGE_CHANCE == null) CASSETTE_DAMAGE_CHANCE = Config.OUTPUT_CASSETTE_DAMAGE_CHANCE.get();
    }

    public static void tick(
            Level level,
            BlockPos blockPos,
            BlockState blockState,
            TapeDeckBlockEntity blockEntity
    ) {
        if (level.isClientSide()) return;
        blockEntity.serverTick();
    }

    public boolean isProcessing() {
        return processing;
    }

    private void serverTick() {
        if (!processing || level == null) return;

        progress += PROGRESS_PER_TICK;
        if (progress < MAX_PROGRESS) return;

        ItemStack inputDiscStack = inventory.getStackInSlot(INPUT_DISC_SLOT);
        ItemStack outputDiscStack = inventory.getStackInSlot(OUTPUT_DISC_SLOT);
        ItemStack cassetteStack = inventory.getStackInSlot(CASSETTE_SLOT);

        if (!inputDiscStack.isEmpty() && !outputDiscStack.isEmpty()) {
            inventory.setStackInSlot(OUTPUT_DISC_SLOT, inputDiscStack.copyWithCount(1));
        }

        if (cassetteStack.is(PortableItems.CASSETTE)) {
            var playable = inputDiscStack.get(DataComponents.JUKEBOX_PLAYABLE);

            if (playable != null) {
                Cassette.addSong(cassetteStack, playable);
            }
        }

        damageSlot(INPUT_DISC_SLOT, INPUT_DAMAGE_CHANCE, PortableItems.BROKEN_DISC.get());
        damageSlot(OUTPUT_DISC_SLOT, OUTPUT_DAMAGE_CHANCE, PortableItems.BROKEN_DISC.get());
        damageSlot(CASSETTE_SLOT, CASSETTE_DAMAGE_CHANCE, PortableItems.BROKEN_CASSETTE.get());

        processing = false;
        progress = 0;
        setChanged();
    }

    private void damageSlot(int slot, double damageChance, Item brokenItem) {
        if (inventory.getStackInSlot(slot).isEmpty()) return;
        if (level == null) return;
        if (level.getRandom().nextDouble() >= damageChance) return;
        inventory.setStackInSlot(slot, new ItemStack(brokenItem));
    }

    @Override
    protected void saveAdditional(
            @Nonnull CompoundTag tag,
            @Nonnull HolderLookup.Provider registries
    ) {
        super.saveAdditional(tag, registries);

        tag.put(INVENTORY_TAG, inventory.serializeNBT(registries));
        tag.putDouble("Progress", progress);
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

        progress = tag.getDouble("Progress");
        processing = tag.getBoolean("Processing");
    }

    public IItemHandler getInventory() {
        return inventory;
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag(@Nonnull HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
