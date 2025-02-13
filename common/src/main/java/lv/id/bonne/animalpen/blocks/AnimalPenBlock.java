package lv.id.bonne.animalpen.blocks;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AnimalPenBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public AnimalPenBlock(Properties properties, WoodType woodType)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
        this.type = woodType;
    }


// ---------------------------------------------------------------------
// Section: Interaction
// ---------------------------------------------------------------------


    @Override
    @NotNull
    public InteractionResult use(BlockState blockState,
        Level level,
        BlockPos blockPos,
        Player player,
        InteractionHand interactionHand,
        BlockHitResult blockHitResult)
    {
        InteractionResult result = super.use(blockState, level, blockPos, player, interactionHand, blockHitResult);

        if (result == InteractionResult.FAIL || interactionHand != InteractionHand.MAIN_HAND)
        {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.is(AnimalPensItemRegistry.ANIMAL_CAGE.get()))
        {
            if (level.getBlockEntity(blockPos) instanceof AnimalPenTileEntity entity &&
                entity.processContainer(player, interactionHand))
            {
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.FAIL;
            }
        }
        else
        {
            if (level.getBlockEntity(blockPos) instanceof AnimalPenTileEntity entity &&
                entity.interactWithPen(player, interactionHand))
            {
                return InteractionResult.SUCCESS;
            }
            else
            {
                return InteractionResult.FAIL;
            }
        }
    }


    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player)
    {
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof AnimalPenTileEntity entity)
        {
            entity.attackThePen(player, level);
            return;
        }

        super.attack(blockState, level, blockPos, player);
    }


// ---------------------------------------------------------------------
// Section: Placement related
// ---------------------------------------------------------------------


    /**
     * This method drops all items from container when block is broken.
     *
     * @param state The BlockState.
     * @param level Level where block is broken.
     * @param pos Position of broken block.
     * @param newState New block state.
     * @param isMoving Boolean if block is moving.
     */
    @Override
    public void onRemove(BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        BlockState newState,
        boolean isMoving)
    {
        if (!state.is(newState.getBlock()))
        {
            BlockEntity tile = level.getBlockEntity(pos);

            if (tile instanceof AnimalPenTileEntity entity)
            {
                for (int i = 0; i < entity.getInventory().getContainerSize(); i++)
                {
                    Containers.dropItemStack(level,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        entity.getInventory().getItem(i));
                }

                entity.getInventory().clearContent();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }


    /**
     * Create block state definition
     *
     * @param builder The definition builder.
     */
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
    }


    /**
     * This method allows to rotate block opposite to player.
     *
     * @param context The placement context.
     * @return The new block state.
     */
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        return Objects.requireNonNull(super.getStateForPlacement(context)).
            setValue(FACING, context.getHorizontalDirection().getOpposite());
    }


    /**
     * This method returns the shape of current table.
     *
     * @param state The block state.
     * @param level The level where block is located.
     * @param pos The position of the block.
     * @param context The collision content.
     * @return The VoxelShape of current table.
     */
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState state,
        @NotNull BlockGetter level,
        @NotNull BlockPos pos,
        @NotNull CollisionContext context)
    {
        return SHAPE;
    }


    /**
     * Returns the wood type.
     * @return WoodType.
     */
    public WoodType getType()
    {
        return this.type;
    }


    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get().create(blockPos, blockState);
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<T> type)
    {
        return createTickerHelper(type,
            AnimalPenTileEntityRegistry.ANIMAL_PEN_TILE_ENTITY.get(),
            (world, pos, blockState, tileEntity) -> tileEntity.tick());
    }


    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> type,
        BlockEntityType<E> expectedType,
        BlockEntityTicker<? super E> ticker)
    {
        return type == expectedType ? (BlockEntityTicker<A>) ticker : null;
    }


    @Override
    @Nullable
    public <T extends BlockEntity> GameEventListener getListener(Level level, T blockEntity)
    {
        return EntityBlock.super.getListener(level, blockEntity);
    }


    /**
     * This method indicates if entities can path find over this block.
     *
     * @param state The block state.
     * @param level Level where block is located.
     * @param pos Position of the block.
     * @param type The path finder type.
     * @return {@code false} always
     */
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    /**
     * The type of wood that is used for animal pen block.
     */
    private final WoodType type;

    private final VoxelShape SHAPE = Shapes.or(
        Block.box(1.0, 10.0, 1.0, 15.0, 12.0, 15.0),
        Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0));
}