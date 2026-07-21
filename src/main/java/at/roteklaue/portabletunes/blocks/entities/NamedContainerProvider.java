package at.roteklaue.portabletunes.blocks.entities;

import at.roteklaue.portabletunes.PortableTunes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class NamedContainerProvider<T extends NamedContainerProvider<T>>
        extends BlockEntity implements MenuProvider {
    @Nullable
    protected Component customName;

    private final String translationKey;
    private final ContainerFactory<T> containerFactory;
    private final Class<T> blockEntityClass;

    protected NamedContainerProvider(
            BlockEntityType<T> blockEntityType,
            BlockPos blockPos,
            BlockState blockState,
            String blockName,
            ContainerFactory<T> containerFactory,
            Class<T> blockEntityClass
    ) {
        this(blockEntityType, blockPos, blockState, containerFactory, blockEntityClass, "container." + PortableTunes.MODID + "." + blockName);
    }

    protected NamedContainerProvider(
            BlockEntityType<T> blockEntityType,
            BlockPos blockPos,
            BlockState blockState,
            ContainerFactory<T> containerFactory,
            Class<T> blockEntityClass,
            String translationKey
    ) {
        super(blockEntityType, blockPos, blockState);

        this.translationKey = translationKey;
        this.containerFactory = containerFactory;
        this.blockEntityClass = blockEntityClass;
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (!tag.contains("CustomName", Tag.TAG_STRING)) {
            customName = null;
            return;
        }

        customName = Component.Serializer.fromJson(
                tag.getString("CustomName"),
                registries
        );
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!hasCustomName()) return;
        tag.putString(
                "CustomName",
                Component.Serializer.toJson(customName, registries)
        );
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(@Nullable Component customName) {
        this.customName = customName;
        setChanged();
    }

    @Nullable
    public Component getCustomName() {
        return customName;
    }

    @Override
    @Nonnull
    public Component getDisplayName() {
        if (customName != null) return customName;
        return Component.translatable(translationKey);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId,
                                            @Nonnull Inventory playerInventory,
                                            @Nonnull Player player) {
        if (!blockEntityClass.isInstance(this)) return null;

        return containerFactory.create(
                containerId,
                playerInventory,
                blockEntityClass.cast(this)
        );
    }

    @FunctionalInterface
    public interface ContainerFactory<T extends BlockEntity> {
        AbstractContainerMenu create(
                int containerId,
                Inventory playerInventory,
                T blockEntity
        );
    }
}
