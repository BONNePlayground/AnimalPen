//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Animal;


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
}
