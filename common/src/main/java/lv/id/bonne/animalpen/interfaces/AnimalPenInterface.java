//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interfaces;


import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public interface AnimalPenInterface
{
    boolean animalPenTick();
    void animalPenSaveTag(CompoundTag tag);
    void animalPenLoadTag(CompoundTag tag);
    boolean animalPenInteract(Player player, InteractionHand hand, BlockPos position);
    List<Pair<ItemStack, String>> animalPenGetLines();
    boolean animalPenUpdateCount(long number);
    long animalPenGetCount();
}
