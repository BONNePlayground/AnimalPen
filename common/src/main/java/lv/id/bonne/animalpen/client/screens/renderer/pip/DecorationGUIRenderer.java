//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.renderer.pip;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import lv.id.bonne.animalpen.client.screens.renderer.state.DecorationGUIRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;


public class DecorationGUIRenderer extends PictureInPictureRenderer<DecorationGUIRenderState>
{
    public DecorationGUIRenderer(MultiBufferSource.BufferSource bufferSource)
    {
        super(bufferSource);
    }

    @Override
    public Class<DecorationGUIRenderState> getRenderStateClass()
    {
        return DecorationGUIRenderState.class;
    }

    @Override
    protected String getTextureLabel()
    {
        return "animal_pen:decoration_gui";
    }

    @Override
    protected void renderToTexture(DecorationGUIRenderState renderState, PoseStack pose)
    {
        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

        // Vanilla already did translate(center) and scale(guiScale * state.scale()).
        pose.pushPose();

        pose.translate(0.0F, -1.0F, 0.0F);

        pose.mulPose(Axis.XP.rotationDegrees(renderState.rotationX()));
        pose.mulPose(Axis.YP.rotationDegrees(renderState.rotationY()));
        pose.mulPose(Axis.ZP.rotationDegrees(renderState.rotationZ()));

        pose.translate(-0.5F, -0.5F, -0.5F);

        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        blockRenderer.renderSingleBlock(
            renderState.blockState(),
            pose,
            this.bufferSource,
            LightTexture.FULL_BLOCK,
            OverlayTexture.NO_OVERLAY);

        pose.popPose();
    }

    @Override
    protected boolean textureIsReadyToBlit(DecorationGUIRenderState renderState)
    {
        return false;
    }
}
