//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.items.component.StoredMobData;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.registries.AnimalPenDataComponentRegistry;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.block.Block;


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
        if (!componentHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
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

        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
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
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        ItemStack componentHolder,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        if (!componentHolder.has(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get()))
        {
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

        Block.popResource(serverLevel, blockPos, bucketItem);

        StoredMobData storedMobData = componentHolder.get(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get());
        long animalCount = storedMobData.animalCount() - 1;
        componentHolder.set(AnimalPenDataComponentRegistry.MOB_DATA_COMPONENT.get(),
            StoredMobData.of(animalCount, storedMobData.properties(), storedMobData.cooldowns()));

        return true;
    }
}
