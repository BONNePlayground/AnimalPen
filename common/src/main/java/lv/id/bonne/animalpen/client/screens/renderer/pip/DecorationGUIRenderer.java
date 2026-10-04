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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;


public class DecorationGUIRenderer extends PictureInPictureRenderer<DecorationGUIRenderState>
{
    public DecorationGUIRenderer()
    {
        super();
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
    protected void renderToTexture(DecorationGUIRenderState renderState, PoseStack pose, SubmitNodeCollector storage)
    {
        pose.pushPose();

        pose.translate(0.0F, -1.0F, 0.0F);

        pose.mulPose(Axis.XP.rotationDegrees(renderState.rotationX()));
        pose.mulPose(Axis.YP.rotationDegrees(renderState.rotationY()));
        pose.mulPose(Axis.ZP.rotationDegrees(renderState.rotationZ()));

        pose.translate(-0.5F, -0.5F, -0.5F);

        Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_3D);

        this.modelState.clear();

        Minecraft.getInstance().getModelManager().getBlockModelSet().get(renderState.blockState()).
            update(this.modelState, renderState.blockState(), BlockDisplayContext.create(), 42L);

        this.modelState.blockLightCoords = renderState.blockState().emissiveRendering() ?
            15728880 :
            LightCoordsUtil.pack(renderState.blockState().getLightEmission(), 15);

        this.modelState.submit(pose, storage, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);

        pose.popPose();
    }

    @Override
    protected boolean textureIsReadyToBlit(DecorationGUIRenderState renderState)
    {
        return false;
    }


    private final BlockModelRenderState modelState = new BlockModelRenderState();
}
