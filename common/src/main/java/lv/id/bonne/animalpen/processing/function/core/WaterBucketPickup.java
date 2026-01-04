//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.processing.function.core;


import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.value.Value;
import lv.id.bonne.animalpen.processing.function.api.EntityFunction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;


/**
 * This function allows to pick up any animal that has Bucketable interface.
 */
public class WaterBucketPickup implements EntityFunction
{
    @Override
    public boolean interactPlayer(ServerPlayer player,
        InteractionHand interactionHand,
        ItemStack itemConsumed,
        int amount,
        Mob mob,
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);
        ItemStack itemInHand = player.getItemInHand(interactionHand);

        if (itemInHand.getItem() != Items.WATER_BUCKET || !(mob instanceof Bucketable bucketable))
        {
            return false;
        }

        ItemStack bucketItem = bucketable.getBucketItemStack();
        bucketable.saveToBucketTag(bucketItem);

        player.getLevel().playSound(null,
            blockPos,
            bucketable.getPickupSound(),
            SoundSource.NEUTRAL,
            1.0F,
            Mth.randomBetween(player.getLevel().getRandom(), 0.8F, 1.2F));

        player.setItemInHand(interactionHand,
            ItemUtils.createFilledResult(itemInHand, player, bucketItem, false));

        int animalCount = animalData.getInt(AnimalPenCompoundTags.TAG_AMOUNT);
        animalData.putInt(AnimalPenCompoundTags.TAG_AMOUNT, animalCount - 1);

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
        CompoundTag mobNBT,
        BlockPos blockPos,
        String dataKey,
        Value dataValue)
    {
        CompoundTag animalData = mobNBT.getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA);

        if (!itemConsumed.is(Items.WATER_BUCKET) || !(mob instanceof Bucketable bucketable))
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

        int animalCount = animalData.getInt(AnimalPenCompoundTags.TAG_AMOUNT);
        animalData.putInt(AnimalPenCompoundTags.TAG_AMOUNT, animalCount - 1);

        return true;
    }
}
