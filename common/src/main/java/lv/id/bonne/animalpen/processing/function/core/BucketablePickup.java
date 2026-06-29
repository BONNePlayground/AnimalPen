//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.platform.Services;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Bucketable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;


/**
 * This function allows to pick up any animal that has Bucketable interface.
 */
public class BucketablePickup implements EntityFunction
{
    @Override
    public boolean interactPlayer(ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

        if (storedMobData == null)
        {
            AnimalPen.LOGGER.error("FAILED to use player bucket as data is missing.");
            return false;
        }

        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (!(mob instanceof Bucketable bucketable))
        {
            return false;
        }

        ItemStack bucketItem = bucketable.getBucketItemStack();
        bucketable.saveToBucketTag(bucketItem);

        player.level().playSound(null,
            blockPos,
            bucketable.getPickupSound(),
            SoundSource.NEUTRAL,
            1.0F,
            Mth.randomBetween(player.level().getRandom(), 0.8F, 1.2F));

        player.setItemInHand(interactionHand,
            ItemUtils.createFilledResult(itemInHand, player, bucketItem, false));

        long animalCount = storedMobData.animalCount() - 1;
        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount, storedMobData.properties(), storedMobData.cooldowns()));

        if (AnimalPen.config().isTriggerAdvancements())
        {
            // Trigger bucket filling
            CriteriaTriggers.FILLED_BUCKET.trigger(player, bucketItem);
        }

        return true;
    }


    @Override
    public boolean interactDispenser(ServerLevel serverLevel,
        Container dispenserInventory,
        int index,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());

        if (storedMobData == null)
        {
            AnimalPen.LOGGER.error("FAILED to use dispenser bucket as data is missing.");
            return false;
        }

        if (!(mob instanceof Bucketable bucketable))
        {
            return false;
        }

        ItemStack bucketItem = bucketable.getBucketItemStack();
        bucketable.saveToBucketTag(bucketItem);

        serverLevel.playSound(null,
            blockPos,
            bucketable.getPickupSound(),
            SoundSource.NEUTRAL,
            1.0F,
            Mth.randomBetween(serverLevel.getRandom(), 0.8F, 1.2F));

        // Remove item from container
        dispenserInventory.setItem(index, ItemStack.EMPTY);

        if (dispenserInventory.canPlaceItem(index, bucketItem))
        {
            // Try to place it back into dispenser.
            dispenserInventory.setItem(index, bucketItem);
        }
        else
        {
            // Transfer into container bellow or drop
            Services.ITEM_TRANSFER.insertBellowOrDrop(serverLevel, bucketItem, blockPos, blockPos);
        }

        long animalCount = storedMobData.animalCount() - 1;
        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount, storedMobData.properties(), storedMobData.cooldowns()));

        return true;
    }
}
