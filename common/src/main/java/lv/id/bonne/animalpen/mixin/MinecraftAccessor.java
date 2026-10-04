//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin;


import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.block.BlockModelResolver;


@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.Minecraft.class)
public interface MinecraftAccessor
{
    @Accessor
    BlockModelResolver getBlockModelResolver();
}
