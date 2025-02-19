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

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

        if (this.dyingAnimal == null || this.dyingAnimal.getType() != animal.getType())
        {
            // Set as null.
            this.dyingAnimal = null;

            CompoundTag cloneTag = new CompoundTag();
            animal.save(cloneTag);

            EntityType.create(cloneTag, tileEntity.getLevel()).
                map(entity -> (Animal) entity).
                ifPresent(clone ->
                {
                    this.dyingAnimal = clone;
                    this.dyingAnimal.setPose(Pose.DYING);

                    // Freeze entity rotation
                    this.dyingAnimal.yBodyRot = 0.0f;
                    this.dyingAnimal.setYRot(0.0f);
                    this.dyingAnimal.yHeadRot = 0.0f;
                    this.dyingAnimal.yHeadRotO = 0.0f;

                    // Stop animations
                    this.dyingAnimal.tickCount = 0;
                    this.dyingAnimal.deathTime = 0;
                });
        }

        Direction facing = tileEntity.getBlockState().getValue(AnimalPenBlock.FACING);

        poseStack.pushPose();

        poseStack.translate(0.5, 0, 0.5);

        // Apply rotation based on facing direction
        switch (facing)
        {
            case SOUTH -> poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Vector3f.YP.rotationDegrees(270));
        }

        // Optional: offset from the face of the block
        poseStack.translate(0, 0, 0);

        this.renderAnimal(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        this.renderCounter(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);

        if (this.minecraft.player != null && this.minecraft.player.isCrouching())
        {
            this.renderTextLines(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        }

        poseStack.popPose();
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

        poseStack.pushPose();
        poseStack.translate(0, (4/16f), 0);
        poseStack.scale(0.33f, 0.33f, 0.33f);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().isGrowAnimals())
        {
            float scale = 1 + 0.33f * (((AnimalPenInterface) animal).animalPenGetCount() / 1000f);
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Vector3f.YP.rotationDegrees(180));

        this.minecraft.getEntityRenderDispatcher().
            getRenderer(animal).
            render(animal, 0.0f, this.minecraft.getFrameTime(), poseStack, buffer, combinedLight);

        CompoundTag cloneTag = new CompoundTag();
        animal.save(cloneTag);

        tileEntity.getDeathTicker().forEach(tick ->
        {
            if (this.dyingAnimal != null)
            {
                this.dyingAnimal.deathTime = tick;

                this.minecraft.getEntityRenderDispatcher().
                    getRenderer(this.dyingAnimal).
                    render(this.dyingAnimal, 0.0f, this.minecraft.getFrameTime(), poseStack, buffer, combinedLight);
            }
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

        // Move to block face 7 at the end because 1/16 is a "sign" in front
        poseStack.translate(0, 3/16f, -0.51f);

        // Scale for pixel-perfect rendering
        poseStack.scale(-0.015f, -0.015f, 0F);

        // Render text
        MutableComponent text = Component.translatable("display.animal_pen.count", count);
        poseStack.translate(-this.font.width(text) / 2D, 0, 0);
        this.font.draw(poseStack, text, 0, 0, 0xFFFFFF);

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

        if (textList.isEmpty())
        {
            return;
        }

        double totalHeight = 1.5 + 0.125 * (textList.size() - 1);
        double maxWidth = 0;

        for (Pair<ItemStack, Component> pair : textList)
        {
            maxWidth = Math.max(0, this.font.width(pair.getRight()));
        }

        maxWidth += 4;

        poseStack.pushPose();
        poseStack.translate(0, totalHeight, 0);

        for (int i = 0; i < textList.size(); i++)
        {
            poseStack.pushPose();

            // Move to the center of the block and above it
            poseStack.translate(0.0, -0.125 * i, 0.00);

            // Render text
            poseStack.pushPose();
            poseStack.scale(-0.0125f, -0.0125f, -0.0125f);
            poseStack.translate(-maxWidth / 2, -6, 0);
            this.font.draw(poseStack, textList.get(i).getRight(), 8, 0, 0xFFFFFF);
            poseStack.popPose();

            // Render Item Stack
            poseStack.pushPose();
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.translate(maxWidth / 2 * 0.05, 0, 0);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            this.minecraft.getItemRenderer().renderStatic(
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

        poseStack.popPose();
    }


    /**
     * The minecraft instance.
     */
    private final Minecraft minecraft = Minecraft.getInstance();

    /**
     * The font instance.
     */
    private final Font font = this.minecraft.font;

    /**
     * Dying animal instance.
     */
    private Animal dyingAnimal;
}
