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
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;


public abstract class AbstractAnimalPenRenderer<T extends AbstractAnimalPenBlockEntity>
    implements BlockEntityRenderer<T>
{
    @Override
    public void render(T tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay,
        Vec3 location)
    {
        Mob animal = tileEntity.getStoredAnimal().orElse(null);

        if (animal == null)
        {
            return;
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

        this.renderAnimal(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        this.renderCounter(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);

        if (this.minecraft.player != null && this.minecraft.player.isCrouching() ||
            !AnimalPen.config().isShowCooldownsOnCrouch())
        {
            this.renderTextLines(animal, tileEntity, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
        }

        poseStack.popPose();
    }


    private void initializeDyingAnimal(Mob animal, T tileEntity)
    {
        if (this.dyingAnimal == null || this.dyingAnimal.getType() != animal.getType())
        {
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
    }


    private void renderAnimal(Mob animal,
        T tileEntity,
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

        // Allow subclasses to set additional pose properties
        this.configureAnimalPose(animal, tileEntity);

        boolean update = animal.tickCount != tileEntity.getTickCounter();

        // Stop animations
        animal.tickCount = tileEntity.getTickCounter();

        poseStack.pushPose();

        // Get vertical offset and animal size from subclass
        poseStack.translate(0, this.getAnimalVerticalOffset(), 0);

        float animalSize = this.getAnimalSize();
        poseStack.scale(animalSize, animalSize, animalSize);

        if (this.shouldGrowAnimals())
        {
            float scale = 1 + animalSize *
                tileEntity.getAnimalDisplaySize() *
                AnimalPen.config().getGrowthMultiplier();
            poseStack.scale(scale, scale, scale);
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        this.minecraft.getEntityRenderDispatcher().
            render(animal, 0.0f, 0.0f, 0.0f, partialTicks, poseStack, buffer, combinedLight);

        if (!tileEntity.getDeathTicker().isEmpty())
        {
            this.initializeDyingAnimal(animal, tileEntity);
        }

        tileEntity.getDeathTicker().forEach(tick ->
        {
            if (this.dyingAnimal != null)
            {
                this.dyingAnimal.deathTime = tick;
                this.dyingAnimal.tickCount = animal.tickCount;

                if (update && animal instanceof Squid squid && this.dyingAnimal instanceof Squid dead)
                {
                    dead.tentacleMovement = squid.tentacleMovement;
                    dead.oldTentacleMovement = squid.oldTentacleMovement;
                    dead.xBodyRot = squid.xBodyRot;
                    dead.xBodyRotO = squid.xBodyRotO;
                    dead.oldTentacleAngle = squid.oldTentacleAngle;
                    dead.tentacleAngle = squid.tentacleAngle;
                }

                this.minecraft.getEntityRenderDispatcher().
                    render(this.dyingAnimal, 0.0f, 0.0f, 0.0f, partialTicks, poseStack, buffer, combinedLight);
            }
        });

        poseStack.popPose();
    }


    private void renderCounter(Mob animal,
        T tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        long count = tileEntity.getAnimalCount();

        poseStack.pushPose();

        // Move to block face 7 at the end because 1/16 is a "sign" in front
        poseStack.translate(0, 2 / 16f, -0.51f);

        // Create text
        Component text = Component.translatable("display.animal_pen.count", count);
        int textWidth = this.font.width(text);

        float maxWidth = 30f;
        float scale = Math.min(1.0f, maxWidth / textWidth) * 0.015f;

        // Apply scaling
        poseStack.scale(-scale, -scale, 0F);
        poseStack.translate(-textWidth / 2D, -this.font.lineHeight / 2f, 0);

        // Render text
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
        T tileEntity,
        float partialTicks,
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
    {
        // Get your list of components
        List<Pair<ItemStack[], Component>> textList = tileEntity.getCooldownLines(true);

        if (textList.isEmpty())
        {
            return;
        }

        BlockPos blockPos = tileEntity.getBlockPos();
        Vec3 playerPos = this.minecraft.player.position();

        // Determine the player's relative position to the block
        Vec3 toPlayer = new Vec3(playerPos.x() - blockPos.getX(), 0, playerPos.z() - blockPos.getZ());
        Direction facing = tileEntity.getBlockState().getValue(AnimalPenBlock.FACING);

        // Get the facing direction as a vector
        Vec3 facingVec = Vec3.atLowerCornerOf(facing.getUnitVec3i());

        if (toPlayer.dot(facingVec) < 0)
        {
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
        }

        double totalHeight = this.getTextStartHeight() + this.getTextLineSpacing() * (textList.size() - 1);
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
            this.renderTextLine(textList.get(i), poseStack, buffer, combinedLight, combinedOverlay);

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
        @NotNull PoseStack poseStack,
        @NotNull MultiBufferSource buffer,
        int combinedLight,
        int combinedOverlay)
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
                this.minecraft.getItemRenderer().renderStatic(
                    first,
                    ItemDisplayContext.GROUND,
                    combinedLight,
                    combinedOverlay,
                    poseStack,
                    buffer,
                    Minecraft.getInstance().level,
                    0
                );
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
                this.minecraft.getItemRenderer().renderStatic(
                    second,
                    ItemDisplayContext.GROUND,
                    combinedLight,
                    combinedOverlay,
                    poseStack,
                    buffer,
                    Minecraft.getInstance().level,
                    0
                );
                poseStack.popPose();

                leftOffset = 8 - whiteSpace;
            }
            else
            {
                // Render regular text
                poseStack.pushPose();

                this.font.drawInBatch(
                    part,  // The text component
                    0, -6,                     // X, Y position in the matrix
                    0xFFFFFF,                    // Color (white)
                    false,                       // Drop shadow
                    poseStack.last().pose(),     // Transformation matrix
                    buffer,                      // Buffer source from method parameters
                    Font.DisplayMode.NORMAL,     // Display mode (NORMAL or SEE_THROUGH)
                    0,                           // Packed overlay
                    combinedLight                // Lighting conditions
                );

                poseStack.popPose();

                leftOffset = this.font.width(part);
            }

            isFirst = false;
        }
    }


    /**
     * The offset of animal in the renderer.
     */
    protected abstract float getAnimalVerticalOffset();


    protected abstract float getAnimalSize();


    protected abstract boolean shouldGrowAnimals();


    protected abstract double getTextStartHeight();


    /**
     * The pose configuration for animals.
     */
    protected void configureAnimalPose(Mob animal, T tileEntity)
    {
    }


    /**
     * The text line spacing value
     */
    protected double getTextLineSpacing()
    {
        return 0.125;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    protected final Minecraft minecraft = Minecraft.getInstance();

    protected final Font font = this.minecraft.font;

    protected Mob dyingAnimal;
}