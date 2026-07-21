package at.roteklaue.portabletunes.blocks;

import at.roteklaue.portabletunes.blocks.entities.PortableBlockEntities;
import at.roteklaue.portabletunes.blocks.entities.TapeDeckBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class TapeDeckBlock extends BaseEntityBlock implements EntityBlock {
    public static final MapCodec<TapeDeckBlock> CODEC =
            simpleCodec(TapeDeckBlock::new);

    public static final DirectionProperty FACING =
            HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<TapeDeckPart> PART =
            EnumProperty.create("part", TapeDeckPart.class);

    private TapeDeckBlock(Properties properties) {
        super(properties);

        registerDefaultState(stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PART, TapeDeckPart.LEFT));
    }

    public TapeDeckBlock() {
        this(Properties.of()
                .ignitedByLava()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD)
                .pushReaction(PushReaction.IGNORE));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@Nonnull BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();

        BlockPos leftPos = context.getClickedPos();
        BlockPos rightPos = leftPos.relative(getRightDirection(facing));

        if (!context.getLevel().getWorldBorder().isWithinBounds(rightPos)) {
            return null;
        }

        if (!context.getLevel()
                .getBlockState(rightPos)
                .canBeReplaced(context)) {
            return null;
        }

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PART, TapeDeckPart.LEFT);
    }

    @Override
    public void setPlacedBy(
            @Nonnull Level level,
            @Nonnull BlockPos blockPos,
            @Nonnull BlockState state,
            @Nullable LivingEntity placer,
            @Nonnull ItemStack stack
    ) {
        super.setPlacedBy(level, blockPos, state, placer, stack);

        if (level.isClientSide()) return;

        Direction facing = state.getValue(FACING);
        BlockPos rightPos = blockPos.relative(getRightDirection(facing));

        BlockState rightState = state.setValue(PART, TapeDeckPart.RIGHT);
        level.setBlock(rightPos, rightState, Block.UPDATE_ALL);
    }


    @Override
    protected void onRemove(
            @Nonnull BlockState oldState,
            @Nonnull Level level,
            @Nonnull BlockPos blockPos,
            @Nonnull BlockState newState,
            boolean movedByPiston
    ) {
        if (oldState.is(newState.getBlock())) {
            super.onRemove(oldState, level, blockPos, newState, movedByPiston);
            return;
        }

        if (!level.isClientSide()
                && oldState.getValue(PART) == TapeDeckPart.LEFT) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            if (blockEntity instanceof TapeDeckBlockEntity tapeDeck) {
                tapeDeck.dropContents();
            }
        }

        BlockPos otherPos = getOtherPos(oldState, blockPos);
        BlockState otherState = level.getBlockState(otherPos);

        if (isMatchingOtherHalf(oldState, otherState)) {
            level.removeBlock(otherPos, false);
        }

        super.onRemove(oldState, level, blockPos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos blockPos, @Nonnull BlockState blockState) {
        if (blockState.getValue(PART) != TapeDeckPart.LEFT) return null;
        return new TapeDeckBlockEntity(blockPos, blockState);
    }

    @Nullable
     @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level level,
            @Nonnull BlockState blockState,
            @Nonnull BlockEntityType<T> blockEntityType
    ) {
        if (level.isClientSide()) return null;
        if (blockState.getValue(PART) != TapeDeckPart.LEFT) return null;

        return createTickerHelper(blockEntityType, PortableBlockEntities.TAPE_DECK.get(), TapeDeckBlockEntity::tick);
    }

    @Override
    @Nonnull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @Nonnull
    protected RenderShape getRenderShape(@Nonnull BlockState blockState) {
        return RenderShape.MODEL;
    }

    private int getSidedSlot(TapeDeckPart half) {
        return switch (half) {
            case LEFT -> 0;
            case RIGHT -> 1;
        };
    }

    @Override
    @Nonnull
    protected InteractionResult useWithoutItem(
            @Nonnull BlockState state,
            @Nonnull Level level,
            @Nonnull BlockPos blockPos,
            @Nonnull Player player,
            @Nonnull BlockHitResult hitResult
    ) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos mainPos = getMainPos(state, blockPos);
        BlockEntity blockEntity = level.getBlockEntity(mainPos);

        if (!(blockEntity instanceof TapeDeckBlockEntity tapeDeck)) return InteractionResult.PASS;

        ItemStack heldStack = player.getMainHandItem();
        if (heldStack.isEmpty()) heldStack = player.getOffhandItem();

        int slotToCheck = getSidedSlot(state.getValue(PART));
        IItemHandler handler = tapeDeck.getInventory();
        if (!heldStack.isEmpty()
                && handler.getStackInSlot(slotToCheck).isEmpty()
                && handler.isItemValid(slotToCheck, heldStack)) {
            ItemStack stackToInsert = heldStack.copyWithCount(1);
            ItemStack remainder = handler.insertItem(slotToCheck, stackToInsert, false);

            if (remainder.isEmpty()) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }

                tapeDeck.setChanged();
                return InteractionResult.CONSUME;
            }
        }

        if (heldStack.isEmpty() && player.isCrouching()) {
            int slot = getSidedSlot(state.getValue(PART));
            ItemStack extractedStack = handler.extractItem(slot, 1, false);

            if (!extractedStack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(extractedStack);
                tapeDeck.setChanged();
                return InteractionResult.CONSUME;
            }
        }

        player.openMenu(tapeDeck);
        return InteractionResult.CONSUME;
    }

    @Nonnull
    public static BlockPos getMainPos(@Nonnull BlockState state, @Nonnull BlockPos blockPos) {
        if (state.getValue(PART) == TapeDeckPart.LEFT) return blockPos;

        Direction facing = state.getValue(FACING);
        return blockPos.relative(getLeftDirection(facing));
    }

    @Nonnull
    private static BlockPos getOtherPos(@Nonnull BlockState state, @Nonnull BlockPos blockPos) {
        return blockPos.relative(getOtherDirection(state));
    }

    @Nonnull
    private static Direction getOtherDirection(@Nonnull BlockState state) {
        Direction facing = state.getValue(FACING);
        TapeDeckPart part = state.getValue(PART);

        if (part == TapeDeckPart.LEFT) return getRightDirection(facing);
        return getLeftDirection(facing);
    }

    @Nonnull
    private static Direction getRightDirection(@Nonnull Direction facing) {
        return facing.getClockWise();
    }

    @Nonnull
    private static Direction getLeftDirection(@Nonnull Direction facing) {
        return facing.getCounterClockWise();
    }

    private static boolean isMatchingOtherHalf(@Nonnull BlockState state, @Nonnull BlockState otherState) {
        if (!otherState.is(state.getBlock())) return false;

        if (state.getValue(FACING) != otherState.getValue(FACING)) return false;
        return state.getValue(PART) != otherState.getValue(PART);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }
}
