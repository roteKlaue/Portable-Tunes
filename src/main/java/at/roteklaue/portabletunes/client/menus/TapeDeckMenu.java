package at.roteklaue.portabletunes.client.menus;

import at.roteklaue.portabletunes.blocks.PortableBlocks;
import at.roteklaue.portabletunes.blocks.entities.TapeDeckBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TapeDeckMenu extends AbstractContainerMenu {
    private static final int TAPE_DECK_SLOT_COUNT = 3;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
    private static final int HOTBAR_SLOT_COUNT = 9;

    private static final int TAPE_DECK_SLOT_START = 0;
    private static final int TAPE_DECK_SLOT_END =
            TAPE_DECK_SLOT_START + TAPE_DECK_SLOT_COUNT;

    private static final int PLAYER_INVENTORY_SLOT_START =
            TAPE_DECK_SLOT_END;
    private static final int PLAYER_INVENTORY_SLOT_END =
            PLAYER_INVENTORY_SLOT_START + PLAYER_INVENTORY_SLOT_COUNT;

    private static final int HOTBAR_SLOT_START =
            PLAYER_INVENTORY_SLOT_END;
    private static final int HOTBAR_SLOT_END =
            HOTBAR_SLOT_START + HOTBAR_SLOT_COUNT;

    private final ContainerLevelAccess access;

    // Client constructor.
    public TapeDeckMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new ItemStackHandler(TAPE_DECK_SLOT_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public TapeDeckMenu(
            int containerId,
            Inventory playerInventory,
            TapeDeckBlockEntity blockEntity
    ) {
        this(
                containerId,
                playerInventory,
                blockEntity.getInventory(),
                ContainerLevelAccess.create(
                        Objects.requireNonNull(blockEntity.getLevel()),
                        blockEntity.getBlockPos()
                )
        );
    }

    private TapeDeckMenu(
            int containerId,
            Inventory playerInventory,
            IItemHandler tapeDeckInventory,
            ContainerLevelAccess access
    ) {
        super(PortableMenus.TAPE_DECK.get(), containerId);

        checkInventorySize(tapeDeckInventory, TAPE_DECK_SLOT_COUNT);

        this.access = access;

        addTapeDeckSlots(tapeDeckInventory);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private void addTapeDeckSlots(IItemHandler inventory) {
        addSlot(new SlotItemHandler(
                inventory,
                TapeDeckBlockEntity.INPUT_DISC_SLOT,
                44,
                25
        ));

        addSlot(new SlotItemHandler(
                inventory,
                TapeDeckBlockEntity.OUTPUT_DISC_SLOT,
                116,
                25
        ));

        addSlot(new SlotItemHandler(
                inventory,
                TapeDeckBlockEntity.CASSETTE_SLOT,
                80,
                45
        ));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventoryIndex = column + row * 9 + 9;

                addSlot(new Slot(
                        inventory,
                        inventoryIndex,
                        8 + column * 18,
                        84 + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(
                    inventory,
                    column,
                    8 + column * 18,
                    142
            ));
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return stillValid(
                access,
                player,
                PortableBlocks.TAPE_DECK.get()
        );
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) return ItemStack.EMPTY;

        Slot sourceSlot = slots.get(slotIndex);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();
        if (!moveStackFromSlot(slotIndex, sourceStack)) return ItemStack.EMPTY;

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) return ItemStack.EMPTY;

        sourceSlot.onTake(player, sourceStack);
        return originalStack;
    }

    private boolean moveStackFromSlot(int slotIndex, ItemStack stack) {
        if (isTapeDeckSlot(slotIndex)) {
            return moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_SLOT_START,
                    HOTBAR_SLOT_END,
                    true
            );
        }

        if (isPlayerInventorySlot(slotIndex)) {
            if (moveItemToTapeDeck(stack)) return true;

            return moveItemStackTo(
                    stack,
                    HOTBAR_SLOT_START,
                    HOTBAR_SLOT_END,
                    false
            );
        }

        if (isHotbarSlot(slotIndex)) {
            if (moveItemToTapeDeck(stack)) return true;

            return moveItemStackTo(
                    stack,
                    PLAYER_INVENTORY_SLOT_START,
                    PLAYER_INVENTORY_SLOT_END,
                    false
            );
        }

        return false;
    }

    private boolean moveItemToTapeDeck(ItemStack stack) {
        return moveItemStackTo(
                stack,
                TapeDeckBlockEntity.INPUT_DISC_SLOT,
                TAPE_DECK_SLOT_END,
                false
        );
    }

    private boolean isTapeDeckSlot(int slotIndex) {
        return slotIndex >= TAPE_DECK_SLOT_START
                && slotIndex < TAPE_DECK_SLOT_END;
    }

    private boolean isPlayerInventorySlot(int slotIndex) {
        return slotIndex >= PLAYER_INVENTORY_SLOT_START
                && slotIndex < PLAYER_INVENTORY_SLOT_END;
    }

    private boolean isHotbarSlot(int slotIndex) {
        return slotIndex >= HOTBAR_SLOT_START
                && slotIndex < HOTBAR_SLOT_END;
    }

    private static void checkInventorySize(IItemHandler inventory, int expectedSize) {
        if (inventory.getSlots() == expectedSize) return;

        throw new IllegalArgumentException(
                "Expected an inventory with "
                        + expectedSize
                        + " slots, but received "
                        + inventory.getSlots()
        );
    }
}
