package lv.id.bonne.animalpen.blocks;


import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;

import dev.architectury.hooks.level.entity.PlayerHooks;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPenTileEntityRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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
            AnimalPen.sendDebug("Blocked by external forces");
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
            if (!(level.getBlockEntity(blockPos) instanceof AquariumTileEntity entity))
            {
                return InteractionResult.FAIL;
            }

            if (PlayerHooks.isFake(player) && itemInHand.is(AnimalPenTags.AQUARIUM_ATTACK_TOOLS))
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

        if (weapon.is(AnimalPenTags.AQUARIUM_ATTACK_TOOLS) &&
            !level.isClientSide() &&
            level.getBlockEntity(blockPos) instanceof AquariumTileEntity entity)
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

        if (weapon.is(AnimalPenTags.AQUARIUM_ATTACK_TOOLS))
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
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos)
    {
        BlockEntity tile = world.getBlockEntity(pos);

        if (tile instanceof AquariumTileEntity aquarium)
        {
            return aquarium.getRedStoneSignal();
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