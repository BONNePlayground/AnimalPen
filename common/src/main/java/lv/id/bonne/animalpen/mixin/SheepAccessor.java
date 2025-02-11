//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;


@Mixin(Sheep.class)
public interface SheepAccessor
{
    @Accessor("ITEM_BY_DYE")
    public Map<DyeColor, ItemLike> getITEM_BY_DYE();
}
