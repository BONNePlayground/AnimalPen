//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.config;


import org.jetbrains.annotations.Nullable;

import javax.swing.text.html.parser.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;


public class AnimalPenConfiguration
{
    public static int getEntityCooldown(ResourceLocation entity, Item item, long amount)
    {
        return 60 * 20;
    }


    public static int getDropLimits(@Nullable ResourceLocation resourceLocation, Item egg, long animalAmount)
    {
        return 0;
    }
}
