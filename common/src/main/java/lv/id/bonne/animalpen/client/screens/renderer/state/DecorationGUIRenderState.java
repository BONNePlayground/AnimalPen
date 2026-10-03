//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.renderer.state;


import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;


public record DecorationGUIRenderState(
    BlockState blockState,
    int x0,
    int y0,
    int x1,
    int y1,
    float scale,
    float rotationX,
    float rotationY,
    float rotationZ,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public DecorationGUIRenderState(
        BlockState blockState,
        int x,
        int y,
        int width,
        int height,
        float scale,
        float rotationX,
        float rotationY,
        float rotationZ,
        @Nullable ScreenRectangle scissorArea)
    {
        this(blockState,
            x,
            y,
            x + width,
            y + height,
            scale,
            rotationX,
            rotationY,
            rotationZ,
            scissorArea,
            PictureInPictureRenderState.getBounds(
                x,
                y,
                x + width,
                y + height,
                scissorArea
            )
        );
    }
}
