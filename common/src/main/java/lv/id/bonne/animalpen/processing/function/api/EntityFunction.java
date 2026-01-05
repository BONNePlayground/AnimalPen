package lv.id.bonne.animalpen.processing.function.api;


import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpen.interaction.value.Value;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * This interface is used to define functions that are triggered by player/dispenser/tickevent
 */
public interface EntityFunction
{
    /**
     * This method manages interaction made by player
     *
     * @param player The player object
     * @param interactionHand The hand player interacted
     * @param itemConsumed The consumed item
     * @param amount The amount of consumed item
     * @param mob The mob that was interacted with
     * @param mobNBT The nbt that contains data for entity
     * @param blockPos The block position
     * @param dataKey The input key for function
     * @param dataValue The input object for function
     * @return {@code true} if interaction changed data and requires update, {@code false} otherwise
     */
    default boolean interactPlayer(ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return this.processFunction(player.getLevel(), mob, mobNBT, blockPos, dataKey, dataValue);
    }


    /**
     * This method manages interaction made by dispenser
     *
     * @param serverLevel The server level object
     * @param dispenserInventory The inventory of dispenser
     * @param itemConsumed The consumed item
     * @param amount The amount of consumed item
     * @param mob The mob that was interacted with
     * @param mobNBT The nbt that contains data for entity
     * @param blockPos The block position
     * @param dataKey The input key for function
     * @param dataValue The input object for function
     * @return {@code true} if interaction changed data and requires update, {@code false} otherwise
     */
    default boolean interactDispenser(ServerLevel serverLevel,
        Container dispenserInventory,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return this.processFunction(serverLevel, mob, mobNBT, blockPos, dataKey, dataValue);
    }


    /**
     * This method manages interaction made by tick event. Usually used at the end of cooldown.
     *
     * @param serverLevel The server level object
     * @param mob The mob that was interacted with
     * @param mobNBT The nbt that contains data for entity
     * @param blockPos The block position
     * @param dataKey The input key for function
     * @param dataValue The input object for function
     * @return {@code true} if interaction changed data and requires update, {@code false} otherwise
     */
    default boolean processFunction(ServerLevel serverLevel,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        @Nullable String dataKey,
        @Nullable Value dataValue)
    {
        return false;
    }


    /**
     * Interface for player-only interaction.
     */
    @FunctionalInterface
    interface PlayerEntityFunction extends EntityFunction
    {
        /**
         * @param player The player object
         * @param interactionHand The hand player interacted
         * @param itemConsumed The consumed item
         * @param amount The amount of consumed item
         * @param mob The mob that was interacted with
         * @param mobNBT The nbt that contains data for entity
         * @param blockPos The block position
         * @param dataKey The input key for function
         * @param dataValue The input object for function
         * @return {@code true} if interaction changed data and requires update, {@code false} otherwise
         */
        @Override
        boolean interactPlayer(ServerPlayer player,
            InteractionHand interactionHand,
            ItemStack itemConsumed,
            int amount,
            Mob mob,
            CompoundTag mobNBT,
            BlockPos blockPos,
            @Nullable String dataKey,
            @Nullable Value dataValue);
    }


    /**
     * Interface for tick-only interaction.
     */
    @FunctionalInterface
    interface ProcessEntityFunction extends EntityFunction
    {
        /**
         * @param serverLevel The server level object
         * @param mob The mob that was interacted with
         * @param mobNBT The nbt that contains data for entity
         * @param blockPos The block position
         * @param dataKey The input key for function
         * @param dataValue The input object for function
         * @return {@code true} if interaction changed data and requires update, {@code false} otherwise
         */
        @Override
        boolean processFunction(ServerLevel serverLevel,
            Mob mob,
            CompoundTag mobNBT,
            BlockPos blockPos,
            @Nullable String dataKey,
            @Nullable Value dataValue);
    }
}