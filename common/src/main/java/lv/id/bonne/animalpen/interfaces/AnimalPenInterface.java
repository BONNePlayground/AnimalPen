//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interfaces;


import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Unique;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;


/**
 * This interface is used to inject custom code into animal objects.
 */
public interface AnimalPenInterface
{
    /**
     * The method that tricks animal pen.
     * @return {@code true} if any change was done, {@code false} otherwise
     */
    boolean animalPenTick(BlockEntity blockEntity);


    /**
     * This method is used to save additional information in animal object.
     * @param tag The tag that will be populated with information.
     */
    void animalPenSaveTag(ValueOutput tag);


    /**
     * This method is used to load additional information into animal object.
     * @param tag The tag that will be read for information.
     */
    void animalPenLoadTag(ValueInput tag);


    /**
     * This method processes player interaction with the animal in pen.
     * @param player The player who performs action.
     * @param hand The hand with which it was done.
     * @param position The tile entity position.
     * @return {@code true} if interaction was successful, {@code false} otherwise
     */
    boolean animalPenInteract(Player player, InteractionHand hand, BlockPos position);


    /**
     * This method processes dispenser interaction with animals in pen.
     * @param level the server level
     * @param itemStack the item stack
     * @param position the position of pen
     * @return ItemStack that should be added in dispenser.
     */
    ItemStack animalPenInteract(ServerLevel level, ItemStack itemStack, BlockPos position);


    /**
     * This method returns the description lines that will be displayed above tile entity.
     * @param tick tile entity tick counter.
     * @param shortLine indicates if text should be short or long.
     * @return List of pairs that contains display icon and text next to it
     */
    List<Pair<ItemStack[], Component>> animalPenGetLines(int tick, boolean shortLine);


    /**
     * This method updates animal count by given number
     * @param number How much change will be applied.
     * @return {@code true} if change was successful, {@code false} otherwise
     */
    boolean animalPenUpdateCount(long number);


    /**
     * This method returns how many animals are in pen.
     * @return The number of animals in pen
     */
    long animalPenGetCount();


    /**
     * This method returns the list of food items that animal eats.
     * @return Array of food items.
     */
    ItemStack[] getFood();


    /**
     * This method returns the redstone signal that indicates available actions.
     * @return redstone signal based on bit value:
     *    - 1 - animal count > 0
     *    - 2 - can feed animal
     *    - 4 - can perform first interaction
     *    - 8 - can perform second interaction
     *    and other values combined using `|`
     */
    int getRedStoneSignal();


    /**
     * This method triggers item usage for statistic and advancement.
     * @param entity The entity that is targeted.
     * @param player The player object.
     * @param itemStack The used item stack.
     * @param amount The amount of used items.
     */
    static void triggerItemUse(Entity entity, ServerPlayer player, ItemStack itemStack, int amount)
    {
        if (AnimalPen.config().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(player,
                itemStack,
                entity);
        }

        if (AnimalPen.config().isIncreaseStatistics())
        {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()), amount);
        }
    }
}
