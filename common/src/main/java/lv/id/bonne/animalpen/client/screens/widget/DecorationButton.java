//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.widget;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        if (!this.visible) return;

        // Render background slot container
        int backgroundColor = this.isHoveredOrFocused() ? 0xFF444444 : 0xFF222222;
        int borderColor = 0xFF8B8B8B;
        fill(poseStack, this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
        fill(poseStack, this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, backgroundColor);

        int centerX = this.getX() + this.width / 2;
        int centerY = this.getY() + this.height / 2 + 2;

        float scale = Math.min(this.width, this.height) * 0.45F;

        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();

        poseStack.pushPose();

        poseStack.translate(centerX, centerY, 100.0F);
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

//        Quaternionf lightRotation = Axis.XP.rotationDegrees(30.0F);
//        lightRotation.mul(Axis.YP.rotationDegrees(225.0F));
//        Matrix4f lightMatrix = new Matrix4f(lightRotation);
//        Lighting.setupLevel(lightMatrix);

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        blockRenderer.renderSingleBlock(
            blockState,
            poseStack,
            bufferSource,
            LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY
        );

        bufferSource.endBatch();
        poseStack.popPose();

        Lighting.setupForFlatItems();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }
}