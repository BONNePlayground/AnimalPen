//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.mixin.accessors;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;


@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsAccessor
{
    @Accessor("guiRenderState")
    GuiRenderState getGuiRenderState();
}
