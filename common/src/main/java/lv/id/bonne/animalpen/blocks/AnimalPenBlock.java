package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

import dev.architectury.hooks.level.entity.PlayerHooks;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class AnimalPenBlock extends HorizontalDirectionalBlock implements EntityBlock
{
    public AnimalPenBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }


    @Override
    @NotNull
    protected MapCodec<? extends HorizontalDirectionalBlock> codec()
    {
        return AnimalPenBlock.CODEC;
    }


// ---------------------------------------------------------------------
// Section: Interaction
// ---------------------------------------------------------------------


    @Override
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
            AnimalPen.sendDebug("Blocked by external forces");
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
            if (!(level.getBlockEntity(blockPos) instanceof AnimalPenTileEntity entity))
            {
                return InteractionResult.FAIL;
            }

            if (PlayerHooks.isFake(player) && itemInHand.is(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS))
            {
                this.attack(blockState, level, blockPos, player);
                return InteractionResult.SUCCESS;
            }
            else if (entity.interactWithPen(player, interactionHand))
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
        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (weapon.is(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS) &&
            !level.isClientSide() &&
            level.getBlockEntity(blockPos) instanceof AnimalPenTileEntity entity)
        {
            if (player.getCooldowns().isOnCooldown(weapon))
            {
                // item is on cooldown. Prevent attack
                return;
            }

            entity.attackThePen(player, level);

            if (PlayerHooks.isFake(player))
            {
                // Fake players do not need cooldowns
                return;
            }

            int cooldown = AnimalPen.config().getAttackCooldown();

            if (cooldown > 0)
            {
                player.getCooldowns().addCooldown(weapon, cooldown);
            }

            return;
        }

        super.attack(blockState, level, blockPos, player);
    }


    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos)
    {
        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (weapon.is(AnimalPenTags.ANIMAL_PEN_ATTACK_TOOLS))
        {
            // Do not damage break block with tools you kill entity.
            return 0f;
        }

        return super.getDestroyProgress(blockState, player, blockGetter, blockPos);
    }


// ---------------------------------------------------------------------
// Section: Redstone related
// ---------------------------------------------------------------------


    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }


    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos, Direction direction)
    {
        BlockEntity tile = world.getBlockEntity(pos);

        if (tile instanceof AnimalPenTileEntity animalPen)
        {
            return animalPen.getRedStoneSignal();
        }

        return 0;
    }


// ---------------------------------------------------------------------
// Section: Placement related
// ---------------------------------------------------------------------


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
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity)
    {
        return EntityBlock.super.getListener(level, blockEntity);
    }


    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType)
    {
        return false;
    }

    public static final MapCodec<AnimalPenBlock> CODEC = simpleCodec(AnimalPenBlock::new);

    private final VoxelShape SHAPE = Shapes.or(
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 4.0, 0.0, 3.0, 8.0, 3.0),
        Block.box(0.0, 4.0, 13.0, 3.0, 8.0, 16.0),
        Block.box(13.0, 4.0, 0.0, 16.0, 8.0, 3.0),
        Block.box(13.0, 4.0, 13.0, 16.0, 8.0, 16.0),
        Block.box(3.0, 5.0, 0.0, 13.0, 7.0, 2.0),
        Block.box(3.0, 5.0, 14.0, 13.0, 7.0, 16.0),
        Block.box(0.0, 5.0, 3.0, 2.0, 7.0, 13.0),
        Block.box(14.0, 5.0, 3.0, 16.0, 7.0, 13.0));
}