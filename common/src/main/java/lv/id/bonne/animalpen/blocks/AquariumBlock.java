package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AquariumBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public AquariumBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().
            setValue(FACING, Direction.NORTH).
            setValue(FILLED, false));
    }


    @Override
    @NotNull
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return AquariumBlock.CODEC;
    }


// ---------------------------------------------------------------------
// Section: PP
// ---------------------------------------------------------------------


    @Override
    protected boolean propagatesSkylightDown(BlockState blockState)
    {
        return true;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }


// ---------------------------------------------------------------------
// Section: Interaction
// ---------------------------------------------------------------------


    @Override
    @NotNull
    protected InteractionResult useItemOn(ItemStack itemStack,
        BlockState blockState,
        Level level,
        BlockPos blockPos,
        Player player,
        InteractionHand interactionHand,
        BlockHitResult blockHitResult)
    {
        InteractionResult result = super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);

        if (result == InteractionResult.FAIL || interactionHand != InteractionHand.MAIN_HAND)
        {
            return InteractionResult.SUCCESS;
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.is(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()))
        {
            if (level.getBlockEntity(blockPos) instanceof AquariumTileEntity entity &&
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
            if (level.getBlockEntity(blockPos) instanceof AquariumTileEntity entity &&
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
        if (!level.isClientSide() && level.getBlockEntity(blockPos) instanceof AquariumTileEntity entity)
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

            if (tile instanceof AquariumTileEntity entity)
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
        builder.add(FACING).add(FILLED);
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
            setValue(FACING, context.getHorizontalDirection().getOpposite()).
            setValue(FILLED, false);
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


    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get().create(blockPos, blockState);
    }


    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<T> type)
    {
        return createTickerHelper(type,
            AnimalPenTileEntityRegistry.AQUARIUM_TILE_ENTITY.get(),
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
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity)
    {
        return EntityBlock.super.getListener(level, blockEntity);
    }


    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType)
    {
        return false;
    }

    public static final MapCodec<AquariumBlock> CODEC = simpleCodec(AquariumBlock::new);

    private final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 23.0, 16.0);

    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
}