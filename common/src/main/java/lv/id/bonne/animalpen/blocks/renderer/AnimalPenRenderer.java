//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;


public class AnimalPenRenderer implements BlockEntityRenderer<AnimalPenTileEntity>
{
    public AnimalPenRenderer()
    {
    }


    @Override
    public void render(AnimalPenTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        Animal animal = tileEntity.getStoredAnimal();

        if (animal == null)
        {
            // Not an entity.
            return;
        }

        this.renderAnimal(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        this.renderCounter(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        this.renderTextLines(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
    }


    private void renderAnimal(Animal animal,
        AnimalPenTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        // Freeze entity rotation
        animal.yBodyRot = 0.0f;
        animal.setYRot(0.0f);
        animal.yHeadRot = 0.0f;
        animal.yHeadRotO = 0.0f;

        // Stop animations
        animal.tickCount = 0;

        // Position the player model inside the block
        poseStack.pushPose();
        poseStack.translate(0.5, (2/16f), 0.5);
        poseStack.scale(0.6f, 0.6f, 0.6f);

        poseStack.mulPose(Vector3f.YP.rotationDegrees(
            tileEntity.getBlockState().getValue(AnimalPenBlock.FACING).toYRot()));

        // Use the player renderer to render the player
        Minecraft.getInstance().getEntityRenderDispatcher().
            getRenderer(animal).
            render(animal, 0.0f, Minecraft.getInstance().getFrameTime(), poseStack, buffer, combinedLight);

        CompoundTag cloneTag = new CompoundTag();
        animal.save(cloneTag);

        tileEntity.getDeathTicker().forEach(tick ->
        {
            EntityType.create(cloneTag, tileEntity.getLevel()).
                map(entity -> (Animal) entity).
                ifPresent(deadAnimal ->
                {
                    deadAnimal.setPose(Pose.DYING);
                    deadAnimal.deathTime = tick;

                    Minecraft.getInstance().getEntityRenderDispatcher().
                        getRenderer(deadAnimal).
                        render(deadAnimal, 0.0f, Minecraft.getInstance().getFrameTime(), poseStack, buffer, combinedLight);

                    deadAnimal.discard();
                });
        });

        poseStack.popPose();
    }


    private void renderCounter(Animal animal,
        AnimalPenTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        long count = ((AnimalPenInterface) animal).animalPenGetCount();

        poseStack.pushPose();

        // Move to block face
        poseStack.translate(0.5D, 2 / 16D, 0.5D);
        // Rotate to face north
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(180));

        poseStack.mulPose(Vector3f.YP.rotationDegrees(
            tileEntity.getBlockState().getValue(AnimalPenBlock.FACING).toYRot()));

        poseStack.translate(0, 0, -0.5D - 1 / 16D);

        // Scale for pixel-perfect rendering
        poseStack.scale(0.01F, 0.01F, 0.01F);

        String text = String.valueOf(count);
        int textWidth = Minecraft.getInstance().font.width(text);

        // Center text on panel
        float x = -textWidth / 2.0F;
        float y = -4.0F; // Vertical center adjustment

        Minecraft.getInstance().font.drawInBatch(text, x, y, 0xFFFFFF,
            false, poseStack.last().pose(), buffer, true,
            0, combinedLight);

        poseStack.popPose();
    }


    private void renderTextLines(Animal animal,
        AnimalPenTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        // Get your list of components
        List<Pair<ItemStack, Component>> textList =
            ((AnimalPenInterface) animal).animalPenGetLines(tileEntity.getTickCounter());

        if (textList.isEmpty()) return;

        double totalHeight = 1.5 + 0.25 * (textList.size() - 1);
        double maxWidth = 0;

        for (Pair<ItemStack, Component> pair : textList)
        {
            maxWidth = Math.max(0, this.font.width(pair.getRight()));
        }

        maxWidth += 4;

        for (int i = 0; i < textList.size(); i++)
        {
            poseStack.pushPose();

            // Move to the center of the block and above it
            poseStack.translate(0.5, totalHeight - 0.25 * i, 0.5);

            // Render text
            poseStack.pushPose();
            poseStack.scale(-0.025f, -0.03f, 0.025f);
            poseStack.translate(-maxWidth / 2, -6, 0);
            Minecraft.getInstance().font.draw(poseStack, textList.get(i).getRight(), 8, 0, 0xFFFFFF);
            poseStack.popPose();

            // Render Item Stack
            poseStack.pushPose();
            poseStack.scale(0.5f, 0.5f, 0.5f);
            poseStack.translate(maxWidth / 2 * 0.05, 0, 0);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                textList.get(i).getLeft(),
                ItemTransforms.TransformType.GROUND,
                combinedLight,
                combinedOverlay,
                poseStack,
                buffer,
                0
            );
            poseStack.popPose();

            poseStack.popPose();
        }
    }


    final Minecraft minecraft = Minecraft.getInstance();

    final Font font = this.minecraft.font;
}
