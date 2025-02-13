//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.interfaces;


import org.apache.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public interface AnimalPenInterface
{
    boolean animalPenTick();
    void animalPenSaveTag(CompoundTag tag);
    void animalPenLoadTag(CompoundTag tag);
    boolean animalPenInteract(Player player, InteractionHand hand, BlockPos position);
    List<Pair<ItemStack, Component>> animalPenGetLines(int tick);
    boolean animalPenUpdateCount(long number);
    long animalPenGetCount();
    List<ItemStack> getFood();
}
