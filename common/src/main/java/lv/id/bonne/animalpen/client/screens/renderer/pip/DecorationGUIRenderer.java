//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.client.screens.renderer.pip;


import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import lv.id.bonne.animalpen.client.screens.renderer.state.DecorationGUIRenderState;
import lv.id.bonne.animalpen.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
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
        FeatureRenderDispatcher dispatcher =
            Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher(); // check name
        SubmitNodeStorage storage = dispatcher.getSubmitNodeStorage();

        pose.pushPose();

        pose.translate(0.0F, -1.0F, 0.0F);

        pose.mulPose(Axis.XP.rotationDegrees(renderState.rotationX()));
        pose.mulPose(Axis.YP.rotationDegrees(renderState.rotationY()));
        pose.mulPose(Axis.ZP.rotationDegrees(renderState.rotationZ()));

        pose.translate(-0.5F, -0.5F, -0.5F);

        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        ((MinecraftAccessor) Minecraft.getInstance()).getBlockModelResolver().
            update(modelState,
                renderState.blockState(),
                BlockDisplayContext.create());

        this.modelState.submit(pose, storage, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);

        dispatcher.renderAllFeatures();

        pose.popPose();
    }

    @Override
    protected boolean textureIsReadyToBlit(DecorationGUIRenderState renderState)
    {
        return false;
    }


    private final BlockModelRenderState modelState = new BlockModelRenderState();
}
