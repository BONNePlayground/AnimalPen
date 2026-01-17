//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin.accessors;


import com.google.common.collect.BiMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;


@Mixin(HoneycombItem.class)
public interface HoneycombItemAccessor
{
    @Accessor("WAXABLES")
    @Final
    @Mutable
    static void animal_pen$setWaxables(Supplier<BiMap<Block, Block>> value)
    {
        throw new AssertionError();
    }


    @Accessor("WAX_OFF_BY_BLOCK")
    @Final
    @Mutable
    static void animal_pen$setWaxOffByBlock(Supplier<BiMap<Block, Block>> value)
    {
        throw new AssertionError();
    }
}
