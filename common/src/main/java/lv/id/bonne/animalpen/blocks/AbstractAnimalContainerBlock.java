package lv.id.bonne.animalpen.blocks;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.architectury.hooks.level.entity.PlayerHooks;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;


public abstract class AbstractAnimalContainerBlock<T extends AbstractAnimalPenBlockEntity>
        extends HorizontalDirectionalBlock implements EntityBlock
{

    protected AbstractAnimalContainerBlock(Properties props)
    {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }


// ---------------------------------------------------------------------
// Section: Abstract methods
// ---------------------------------------------------------------------


    protected abstract TagKey<Item> getAttackToolTag();


    protected abstract Item getContainerItem();


    protected abstract BlockEntityType<T> getTileType();


// ---------------------------------------------------------------------
// Section: Interaction
// ---------------------------------------------------------------------


    @Override
    @NotNull
    protected InteractionResult useItemOn(ItemStack itemStack,
        BlockState blockState, Level level, BlockPos blockPos,
        Player player, InteractionHand interactionHand, BlockHitResult blockHitResult)
    {
        InteractionResult result = super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);

        if (result == InteractionResult.FAIL || interactionHand != InteractionHand.MAIN_HAND)
        {
            AnimalPen.sendDebug("Blocked by external forces");
            return InteractionResult.SUCCESS;
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.is(this.getContainerItem()))
        {
            if (level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity entity &&
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
            if (!(level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity entity))
            {
                return InteractionResult.FAIL;
            }

            if (PlayerHooks.isFake(player) && itemInHand.is(this.getAttackToolTag()))
            {
                this.attack(blockState, level, blockPos, player);
                return InteractionResult.SUCCESS;
            }
            else if (entity.interactWithPen(player, interactionHand))
            {
                return InteractionResult.SUCCESS_SERVER;
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

        if (weapon.is(this.getAttackToolTag()) &&
            !level.isClientSide() &&
            level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity entity)
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


// ---------------------------------------------------------------------
// Section: Block Related Methods
// ---------------------------------------------------------------------


    @Override
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos)
    {
        ItemStack weapon = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (weapon.is(this.getAttackToolTag()))
        {
            // Do not damage break block with tools you kill entity.
            return 0f;
        }

        return super.getDestroyProgress(blockState, player, blockGetter, blockPos);
    }


    @Override
    public boolean hasAnalogOutputSignal(BlockState state)
    {
        return true;
    }


    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos)
    {
        BlockEntity be = level.getBlockEntity(pos);

        return be instanceof AbstractAnimalPenBlockEntity entity ?
            entity.getRedStoneSignal() : 0;
    }


    /**
     * This method indicates if entities can path find over this block.
     * @return {@code false} always
     */
    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType)
    {
        return false;
    }


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

            if (tile instanceof AbstractAnimalPenBlockEntity entity)
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


// ---------------------------------------------------------------------
// Section: Block Tile Entity related methods
// ---------------------------------------------------------------------


    @Override
    @Nullable
    public <E extends BlockEntity> GameEventListener getListener(ServerLevel level, E blockEntity)
    {
        return EntityBlock.super.getListener(level, blockEntity);
    }


    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return this.getTileType().create(blockPos, blockState);
    }


    @Override
    public <E extends BlockEntity> BlockEntityTicker<E> getTicker(@NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<E> type)
    {
        return createTickerHelper(type,
            this.getTileType(),
            (world, pos, blockState, tileEntity) -> tileEntity.tick());
    }


    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
        BlockEntityType<A> type,
        BlockEntityType<E> expectedType,
        BlockEntityTicker<? super E> ticker)
    {
        return type == expectedType ? (BlockEntityTicker<A>) ticker : null;
    }
}