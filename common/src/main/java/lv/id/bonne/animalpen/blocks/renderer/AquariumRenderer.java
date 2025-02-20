//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AquariumTileEntity;
import lv.id.bonne.animalpen.interfaces.AnimalPenInterface;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;


public class AquariumRenderer implements BlockEntityRenderer<AquariumTileEntity>
{
    @Override
    public void render(AquariumTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        Mob animal = tileEntity.getStoredAnimal();

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

            EntityType.create(cloneTag, tileEntity.getLevel(), EntitySpawnReason.TRIGGERED).
                map(entity -> (Mob) entity).
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
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
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


    private void renderAnimal(Mob animal,
        AquariumTileEntity tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        // Freeze entity rotation
        animal.yBodyRot = 0f;
        animal.setYRot(0f);
        animal.yHeadRot = 0f;
        animal.yHeadRotO = 0f;

        animal.setPose(Pose.SWIMMING);
        animal.setSwimming(true);
        ((EntityAccessor) animal).setWasTouchingWater(true);

        // Stop animations
        animal.tickCount = tileEntity.getTickCounter();

        poseStack.pushPose();
        poseStack.translate(0.00, 12/16f, 0);
        poseStack.scale(0.33f, 0.33f, 0.33f);

        if (AnimalPen.CONFIG_MANAGER.getConfiguration().isGrowWaterAnimals())
        {
            float scale = 1 + 0.33f * (((AnimalPenInterface) animal).animalPenGetCount() / 1000f);
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        this.minecraft.getEntityRenderDispatcher().
            render(animal, 0.0f, 0.0f, 0.0f, this.minecraft.getFrameTimeNs(), poseStack, buffer, combinedLight);

        CompoundTag cloneTag = new CompoundTag();
        animal.save(cloneTag);

        tileEntity.getDeathTicker().forEach(tick ->
        {
            if (this.dyingAnimal != null)
            {
                this.dyingAnimal.deathTime = tick;

                this.minecraft.getEntityRenderDispatcher().
                    render(this.dyingAnimal, 0.0f, 0.0f, 0.0f, this.minecraft.getFrameTimeNs(), poseStack, buffer, combinedLight);
            }
        });

        poseStack.popPose();
    }


    private void renderCounter(Mob animal,
        AquariumTileEntity tileEntity,
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
        Component text = Component.translatable("display.animal_pen.count", count);
        poseStack.translate(-this.font.width(text) / 2D, 0, 0);

        this.font.drawInBatch(
            text,                    // The text component
            0, 0,                 // X, Y position in the matrix
            0xFFFFFF,                // Color (white)
            false,                   // Drop shadow
            poseStack.last().pose(), // Transformation matrix
            buffer,                  // Buffer source from method parameters
            Font.DisplayMode.NORMAL, // Display mode (NORMAL or SEE_THROUGH)
            0,                       // Packed overlay
            combinedLight            // Lighting conditions
        );

        poseStack.popPose();
    }


    private void renderTextLines(Mob animal,
        AquariumTileEntity tileEntity,
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

        double totalHeight = 1.75 + 0.25 * (textList.size() - 1);
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

            this.font.drawInBatch(
                textList.get(i).getRight(),  // The text component
                8, 0,                     // X, Y position in the matrix
                0xFFFFFF,                    // Color (white)
                false,                       // Drop shadow
                poseStack.last().pose(),     // Transformation matrix
                buffer,                      // Buffer source from method parameters
                Font.DisplayMode.NORMAL,     // Display mode (NORMAL or SEE_THROUGH)
                0,                           // Packed overlay
                combinedLight                // Lighting conditions
            );

            poseStack.popPose();

            // Render Item Stack
            poseStack.pushPose();
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.translate(maxWidth / 2 * 0.05, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            this.minecraft.getItemRenderer().renderStatic(
                textList.get(i).getLeft(),
                ItemDisplayContext.GROUND,
                combinedLight,
                combinedOverlay,
                poseStack,
                buffer,
                tileEntity.getLevel(),
                0
            );
            poseStack.popPose();
            poseStack.popPose();
        }

        poseStack.popPose();
    }


    @Override
    public boolean shouldRenderOffScreen(AquariumTileEntity blockEntity)
    {
        return !blockEntity.getInventory().isEmpty();
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
    private Mob dyingAnimal;
}
