//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.widget;


import lv.id.bonne.animalpen.client.screens.renderer.state.DecorationGUIRenderState;
import lv.id.bonne.animalpen.mixin.accessors.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;


public class DecorationButton extends Button
{
    private final BlockState blockState;


    public DecorationButton(int x, int y, int width, int height, BlockState blockState, OnPress onPress)
    {
        super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        this.blockState = blockState;
    }


    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible) return;

        // Render background slot container
        int backgroundColor = this.isHoveredOrFocused() ? 0xFF444444 : 0xFF222222;
        int borderColor = 0xFF8B8B8B;
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
        graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, backgroundColor);

        int centerX = this.getX() + this.width / 2;
        int centerY = this.getY() + this.height / 2 + 2;

        float scale = Math.min(this.width, this.height) * 0.45F;

        GuiGraphicsAccessor graphicsAccessor = (GuiGraphicsAccessor) graphics;

        graphicsAccessor.getGuiRenderState().addPicturesInPictureState(
            new DecorationGUIRenderState(
                this.blockState,
                centerX - this.width / 2,
                centerY - this.height / 2,
                this.width,
                this.height,
                scale,
                0.0F,
                45.0F,
                180.0F,
                null
            )
        );
    }
}