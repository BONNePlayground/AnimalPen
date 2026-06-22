//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.events;


import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.client.screens.VariantScreenSelection;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;


public class CommonClientEvents
{
    public static InteractionResult onRightClickBlock(Player player, Level level, BlockPos blockPos)
    {
        if (Minecraft.getInstance().player != player)
        {
            // fixes local host issues.
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(blockPos) instanceof AbstractAnimalPenBlockEntity blockEntity))
        {
            return InteractionResult.PASS;
        }

        if (!player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() || player.isCrouching())
        {
            return InteractionResult.PASS;
        }

        if (blockEntity.getStoredAnimal().isEmpty())
        {
            return InteractionResult.PASS;
        }

        if (Services.PLATFORM.isFake(player))
        {
            return InteractionResult.PASS;
        }

        Minecraft.getInstance().execute(() ->
            Minecraft.getInstance().setScreenAndShow(new VariantScreenSelection(blockPos)));

        return InteractionResult.SUCCESS;
    }
}
