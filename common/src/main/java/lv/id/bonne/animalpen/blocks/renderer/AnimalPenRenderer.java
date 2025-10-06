//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.blocks.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.AnimalPenBlock;
import lv.id.bonne.animalpen.blocks.entities.AnimalPenTileEntity;
import lv.id.bonne.animalpen.blocks.renderer.state.AnimalPenRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;


public class AnimalPenRenderer implements BlockEntityRenderer<AnimalPenTileEntity, AnimalPenRenderState>
{
    public AnimalPenRenderer(BlockEntityRendererProvider.Context context)
    {
        this.entityRenderer = context.entityRenderer();
        this.itemModelResolver = context.itemModelResolver();
        this.font = context.font();
    }


    @Override
    @NotNull
    public AnimalPenRenderState createRenderState()
    {
        return new AnimalPenRenderState();
    }


    @Override
    public void extractRenderState(AnimalPenTileEntity blockEntity,
        AnimalPenRenderState renderState,
        float partialTick,
        Vec3 position,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay)
    {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, position, crumblingOverlay);
        renderState.facing = blockEntity.getBlockState().getValue(AnimalPenBlock.FACING);
        renderState.animalDisplaySize = blockEntity.getAnimalDisplaySize();
        renderState.animalCount = blockEntity.getAnimalCount();
        renderState.cooldownLines = blockEntity.getCooldownLines(true);
        renderState.level = blockEntity.getLevel();

        Animal animal = blockEntity.getStoredAnimal().orElse(null);

        if (animal != null)
        {
            // Freeze entity rotation
            animal.yBodyRot = 0.0f;
            animal.setYRot(0.0f);
            animal.yHeadRot = 0.0f;
            animal.yHeadRotO = 0.0f;

            // Stop animations
            animal.tickCount = blockEntity.getTickCounter();

            renderState.displayEntity = this.entityRenderer.extractEntity(animal, partialTick);
            renderState.displayEntity.lightCoords = renderState.lightCoords;

            try (ProblemReporter.ScopedCollector reported =
                     new ProblemReporter.ScopedCollector(animal.problemPath(), AnimalPen.LOGGER))
            {
                TagValueOutput tagOutput = TagValueOutput.createWithContext(reported, animal.registryAccess());
                animal.save(tagOutput);
                ValueInput tagInput = TagValueInput.create(reported,
                    animal.registryAccess(),
                    tagOutput.buildResult());

                // Summon entity per death on client and remove it once render state is created.
                blockEntity.getDeathTicker().forEach(tick ->
                    EntityType.create(animal.getType(), tagInput, animal.level(), EntitySpawnReason.TRIGGERED).
                        ifPresent(entity ->
                        {
                            LivingEntity death = (LivingEntity) entity;

                            death.setPose(Pose.DYING);
                            death.deathTime = tick;

                            EntityRenderState entityRenderState =
                                this.entityRenderer.extractEntity(death, partialTick);
                            entityRenderState.lightCoords = renderState.lightCoords;

                            renderState.dyingEntity.add(entityRenderState);
                            death.remove(Entity.RemovalReason.DISCARDED);
                        }));
            }
        }
    }


    @Override
    public void submit(AnimalPenRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState cameraRenderState)
    {
        if (renderState.displayEntity == null)
        {
            // Not an entity.
            return;
        }

        Direction facing = renderState.facing;

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

        this.renderAnimal(renderState, poseStack, submitNodeCollector, cameraRenderState);
        this.renderCounter(renderState, poseStack, submitNodeCollector, cameraRenderState);

        if (this.minecraft.player != null && this.minecraft.player.isCrouching())
        {
            this.renderTextLines(renderState, poseStack, submitNodeCollector, cameraRenderState);
        }

        poseStack.popPose();
    }


    private void renderAnimal(AnimalPenRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState cameraRenderState)
    {
        poseStack.pushPose();
        poseStack.translate(0, (4 / 16f), 0);

        float animalSize = AnimalPen.config().getAnimalSize();

        poseStack.scale(animalSize, animalSize, animalSize);

        if (AnimalPen.config().isGrowAnimals())
        {
            float scale = 1 + animalSize * renderState.animalDisplaySize * AnimalPen.config().getGrowthMultiplier();
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        submitNodeCollector.order(1);
        this.entityRenderer.submit(renderState.displayEntity,
            cameraRenderState,
            0.0f,
            0.0f,
            0.0f,
            poseStack,
            submitNodeCollector);

        renderState.dyingEntity.forEach(state ->
            this.entityRenderer.submit(state,
                cameraRenderState,
                0.0f,
                0.0f,
                0.0f,
                poseStack,
                submitNodeCollector));

        poseStack.popPose();
    }


    private void renderCounter(AnimalPenRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState cameraRenderState)
    {
        long count = renderState.animalCount;

        poseStack.pushPose();

        // Move to block face 7 at the end because 1/16 is a "sign" in front
        poseStack.translate(0, 2 / 16f, -0.51f);

        // Create text
        MutableComponent text = Component.translatable("display.animal_pen.count", count);
        int textWidth = this.font.width(text);

        float maxWidth = 30f;
        float scale = Math.min(1.0f, maxWidth / textWidth) * 0.015f;

        // Apply scaling
        poseStack.scale(-scale, -scale, 0F);
        poseStack.translate(-textWidth / 2D, -this.font.lineHeight / 2f, 0);

        submitNodeCollector.order(2).submitText(poseStack,
            0F,
            0F,
            text.getVisualOrderText(),
            false,
            Font.DisplayMode.NORMAL,
            renderState.lightCoords,
            -1,
            0,
            0);

        poseStack.popPose();
    }


    private void renderTextLines(AnimalPenRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState cameraRenderState)
    {
        // Get your list of components
        List<Pair<ItemStack[], Component>> textList = renderState.cooldownLines;

        if (textList.isEmpty())
        {
            return;
        }

        BlockPos blockPos = renderState.blockPos;
        Vec3 playerPos = cameraRenderState.pos;

        // Determine the player's relative position to the block
        Vec3 toPlayer = new Vec3(playerPos.x() - blockPos.getX(), 0, playerPos.z() - blockPos.getZ());
        Direction facing = renderState.facing;

        // Get the facing direction as a vector
        Vec3 facingVec = Vec3.atLowerCornerOf(facing.getUnitVec3i());

        if (toPlayer.dot(facingVec) < 0)
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }

        double totalHeight = 1.5 + 0.125 * (textList.size() - 1);
        double maxWidth = 0;

        for (Pair<ItemStack[], Component> pair : textList)
        {
            maxWidth = Math.max(maxWidth, this.calculateMaxWidth(pair));
        }

        poseStack.pushPose();
        poseStack.translate(0, totalHeight, 0);

        maxWidth = -maxWidth / 2;

        for (int i = 0; i < textList.size(); i++)
        {
            poseStack.pushPose();

            // Move to the center of the block and above it
            poseStack.translate(0.0, -0.125 * i, 0.00);
            poseStack.scale(-0.0125F, -0.0125F, 0.0125F);

            // apply offset
            poseStack.translate(maxWidth, 0, 0);
            this.renderTextLine(textList.get(i), renderState, poseStack, submitNodeCollector, cameraRenderState);

            poseStack.popPose();
        }

        poseStack.popPose();
    }


    private double calculateMaxWidth(Pair<ItemStack[], Component> pair)
    {
        Component text = pair.getRight();
        ItemStack first = pair.getLeft().length > 0 ? pair.getLeft()[0] : null;
        ItemStack second = pair.getLeft().length > 1 ? pair.getLeft()[1] : null;

        // A bit of hacky way to compact drawing, as usually lang $s is separated with spaced.
        int whiteSpace = this.font.width(" ");
        boolean isFirst = true;

        double width = 0;

        // Process each text part
        for (Component part : text.toFlatList(Style.EMPTY))
        {
            String content = part.getString();

            if (content.equals("\uE000"))
            {
                if (first == null)
                {
                    // Skip rendering as icon is missing.
                    continue;
                }

                if (!isFirst)
                {
                    // move closer to previous part to overlap white space.
                    width -= whiteSpace;
                }

                // Render the first item
                width += 16 - whiteSpace;
            }
            else if (content.equals("\uE001"))
            {
                if (second == null)
                {
                    // Skip rendering as icon is missing.
                    continue;
                }

                if (!isFirst)
                {
                    // move closer to previous part to overlap white space.
                    width -= whiteSpace;
                }

                // Render the second item (if available)
                width += 16 - whiteSpace;
            }
            else
            {
                // Render regular text
                width += this.font.width(part);
            }

            isFirst = false;
        }

        return width;
    }


    private void renderTextLine(Pair<ItemStack[], Component> componentPair,
        AnimalPenRenderState renderState,
        @NotNull PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState cameraRenderState)
    {
        Component text = componentPair.getRight();
        ItemStack first = componentPair.getLeft().length > 0 ? componentPair.getLeft()[0] : null;
        ItemStack second = componentPair.getLeft().length > 1 ? componentPair.getLeft()[1] : null;

        // A bit of hacky way to compact drawing, as usually lang $s is separated with spaced.
        int whiteSpace = this.font.width(" ");
        boolean isFirst = true;

        int leftOffset = 0;

        // Process each text part
        for (Component part : text.toFlatList(Style.EMPTY))
        {
            // apply offset
            poseStack.translate(leftOffset, 0, 0);
            String content = part.getString();

            if (content.equals("\uE000"))
            {
                if (first == null)
                {
                    leftOffset = isFirst ? 0 : -whiteSpace;
                    // Skip rendering as icon is missing.
                    continue;
                }

                // Render the first item
                poseStack.pushPose();
                // image is 20x smaller and flipped than text
                poseStack.scale(-20f, -20f, 20f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                // I know ths is incorrect, but I cannot be bothered to create list at the extraction
                // to render it now...
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();

                this.itemModelResolver.updateForTopItem(itemStackRenderState,
                    first,
                    ItemDisplayContext.GROUND,
                    renderState.level,
                    null,
                    0);

                itemStackRenderState.submit(poseStack,
                    submitNodeCollector,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);

                poseStack.popPose();

                leftOffset = 8 - whiteSpace;
            }
            else if (content.equals("\uE001"))
            {
                if (second == null)
                {
                    leftOffset = isFirst ? 0 : -whiteSpace;
                    // Skip rendering as icon is missing.
                    continue;
                }

                // Render the second item
                poseStack.pushPose();
                // image is 20x smaller and flipped than text
                poseStack.scale(-20f, -20f, 20f);
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

                // I know ths is incorrect, but I cannot be bothered to create list at the extraction
                // to render it now...
                ItemStackRenderState itemStackRenderState = new ItemStackRenderState();

                this.itemModelResolver.updateForTopItem(itemStackRenderState,
                    second,
                    ItemDisplayContext.GROUND,
                    renderState.level,
                    null,
                    0);

                itemStackRenderState.submit(poseStack,
                    submitNodeCollector,
                    renderState.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0);

                poseStack.popPose();

                leftOffset = 8 - whiteSpace;
            }
            else
            {
                // Render regular text
                poseStack.pushPose();

                submitNodeCollector.submitText(poseStack,
                    0F,
                    -6,
                    part.getVisualOrderText(),
                    false,
                    Font.DisplayMode.NORMAL,
                    renderState.lightCoords,
                    -1,
                    0,
                    0);

                poseStack.popPose();

                leftOffset = this.font.width(part);
            }

            isFirst = false;
        }
    }


    @Override
    public boolean shouldRenderOffScreen()
    {
        return AnimalPen.config().isGrowAnimals();
    }


    @Override
    public int getViewDistance()
    {
        return BlockEntityRenderer.super.getViewDistance();
    }


    @Override
    public boolean shouldRender(AnimalPenTileEntity blockEntity, Vec3 vec3)
    {
        return AnimalPen.config().isGrowAnimals() || BlockEntityRenderer.super.shouldRender(blockEntity, vec3);
    }


    /**
     * Instance of entity renderer
     */
    private final EntityRenderDispatcher entityRenderer;

    /**
     * Instance of item model  renderer
     */
    private final ItemModelResolver itemModelResolver;

    /**
     * The font instance.
     */
    private final Font font;

    /**
     * The minecraft instance.
     */
    private final Minecraft minecraft = Minecraft.getInstance();
}
