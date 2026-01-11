package lv.id.bonne.animalpen.client.screens;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.blocks.entities.AbstractAnimalPenBlockEntity;
import lv.id.bonne.animalpen.mixin.accessors.EntityAccessor;
import lv.id.bonne.animalpen.network.packets.RemoveDisplayAnimalData;
import lv.id.bonne.animalpen.network.packets.UpdateDisplayAnimalData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;


/**
 * This menu allows to change which entity variant is on display for player.
 */
public class VariantScreenSelection extends Screen
{
    public VariantScreenSelection(BlockPos position)
    {
        super(TITLE);
        this.position = position;

        this.imageWidth = 176;
        this.imageHeight = 136;

        this.cooldownWidth = 149;
    }


    @Override
    protected void init()
    {
        super.init();
        this.clearWidgets();

        if (this.isCooldownOpened)
        {
            this.leftPos = (this.width - this.imageWidth + this.cooldownWidth) / 2;
        }
        else
        {
            this.leftPos = (this.width - this.imageWidth) / 2;
        }

        this.topPos = (this.height - this.imageHeight) / 2;
        this.buttons.clear();

        this.bodyTopPos = this.topPos + 18;
        int buttonPos = this.leftPos + 24;

        int buttonWidth = 46;
        int buttonHeight = 20;

        // collect variants

        if (this.minecraft == null || this.minecraft.level == null)
        {
            return;
        }

        BlockEntity blockEntity = this.minecraft.level.getBlockEntity(this.position);

        if (!(blockEntity instanceof AbstractAnimalPenBlockEntity animalPenEntity))
        {
            return;
        }

        this.blockEntityInterface = animalPenEntity;

        List<CompoundTag> entityList = this.blockEntityInterface.getEntityVariants();

        if (entityList == null)
        {
            // Avoid null-pointer
            entityList = Collections.emptyList();
        }

        // initialize variant buttons.
        for (int i = 0; i < entityList.size(); i++)
        {
            int y = this.bodyTopPos + (i * buttonHeight);
            final int index = i;

            this.buttons.add(this.addWidget(Button.builder(Component.translatable(BUTTON_TEXT, (index + 1)),
                    button -> handleVariantButton(button, index)).
                pos(buttonPos, y).
                size(buttonWidth, buttonHeight).
                build()));
        }

        // Delete variant button
        this.deleteButton = this.addWidget(Button.builder(Component.empty(),
                this::handleDeleteButton).
            pos(this.leftPos + 157, this.topPos + 111).
            size(11, 14).
            build());

        this.deleteButton.active = this.blockEntityInterface.getOwner().
            map(uuid -> uuid.equals(this.minecraft.player.getUUID())).
            orElse(true);

        // Apply variant button
        this.applyButton = this.addWidget(Button.builder(Component.empty(),
                this::handleApplyButton).
            pos(this.leftPos + 73, this.topPos + 111).
            size(14, 14).
            build());

        this.applyButton.active = false;

        this.sliderBarPos = this.leftPos + 89;

        // Apply variant button
        this.configureButton = this.addWidget(Button.builder(Component.empty(),
            this::handleConfigureButton).
            pos(this.leftPos + 160, this.topPos + 5).
            size(10, 10).
            build());

        // Create display entity.

        CompoundTag defaultAnimal = new CompoundTag();
        this.blockEntityInterface.getStoredAnimal().ifPresent(entity -> entity.save(defaultAnimal));

        EntityType.create(defaultAnimal, this.minecraft.level, EntitySpawnReason.TRIGGERED).
            map(entity -> (LivingEntity) entity).
            ifPresent(entity ->
            {
                this.displayEntity = entity;

                float width = this.displayEntity.getBbWidth();
                float height = this.displayEntity.getBbHeight();

                float entitySize = Math.max(1F, Math.max(width, height));

                this.entityScale = 60F / entitySize * 0.8F;
                this.entityOffset = Math.max(height, entitySize) * 0.5F;
            });

        // Create cooldown menu renderer
        this.cooldownButton = this.addWidget(Button.builder(Component.empty(),
                this::handleCooldownButton).
            pos(this.leftPos - 12, this.topPos + (this.imageHeight - 18) / 2).
            size(11, 18).
            build());
    }


    /**
     * This returns position of block that relates to current screen.
     *
     * @return block position.
     */
    public BlockPos getPosition()
    {
        return this.position;
    }


    /**
     * This inits that update will be triggered after 5 ticks.
     */
    public void update()
    {
        this.needsUpdate = 5;
    }


    @Override
    public void tick()
    {
        super.tick();

        if (this.displayEntity != null)
        {
            this.displayEntity.tickCount++;
        }

        if (!(this.minecraft.level.getBlockEntity(this.position) instanceof AbstractAnimalPenBlockEntity) ||
            this.blockEntityInterface.getStoredAnimal().isEmpty())
        {
            // close screen
            this.minecraft.setScreen(null);
            return;
        }

        if (this.needsUpdate > 0 && --this.needsUpdate == 0)
        {
            this.init();
        }
    }


    @Override
    public boolean isPauseScreen()
    {
        return false;
    }


// ---------------------------------------------------------------------
// Section: Renderer
// ---------------------------------------------------------------------


    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        this.updateButtonPositions();

        super.render(graphics, mouseX, mouseY, partialTicks);

        // Render title of the menu.
        graphics.drawString(this.font,
            this.title,
            this.leftPos + 88 - this.font.width(this.title) / 2,
            this.topPos + 3 + 7 - this.font.lineHeight / 2,
            4210752,
            false);

        this.renderVariantButtons(graphics, mouseX, mouseY, partialTicks);
        this.renderOtherButtons(graphics, mouseX, mouseY);
        this.renderScrollBar(graphics, mouseX, mouseY);
        this.renderTextBar(graphics, mouseX, mouseY, partialTicks);
        this.renderEntity(graphics, partialTicks);
        this.renderCooldown(graphics, mouseX, mouseY, partialTicks);

        this.renderTooltips(graphics, mouseX, mouseY, partialTicks);
    }


    /**
     * Renders the main background image.
     *
     * @param graphics The pose stack
     */
    @Override
    public void renderMenuBackground(GuiGraphics graphics)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int offsetX = this.leftPos;
        int offsetY = this.topPos;

        graphics.blit(RenderType::guiTextured, TEXTURE, offsetX, offsetY, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }


    /**
     * This method renders all variant selection buttons.
     *
     * @param graphics The pose stack
     * @param mouseX Cursor X location
     * @param mouseY Cursor Y location
     * @param partialTicks Partial Ticks
     */
    private void renderVariantButtons(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        // Enable scissor test to restrict rendering area
        graphics.enableScissor(10, this.bodyTopPos, this.width - 10, this.bodyTopPos + this.buttonAreaHeight);

        // Render only visible buttons

        for (int i = 0; i < this.buttons.size(); i++)
        {
            Button button = this.buttons.get(i);
            button.render(graphics, mouseX, mouseY, partialTicks);

            // If this is the selected button, render a green border around it
            if (i == this.selectedButton)
            {
                this.renderButtonBorder(graphics, button, 0x8000FF00);
            }
        }

        graphics.disableScissor();
    }


    /**
     * Renders a colored border around a button
     */
    private void renderButtonBorder(GuiGraphics graphics, Button button, int color)
    {
        int width = button.getWidth();
        int height = button.getHeight();

        // Draw top line
        graphics.fill(button.getX(), button.getY(), button.getX() + width, button.getY() + 1, color);
        graphics.fill(button.getX(), button.getY() + height - 1, button.getX() + width, button.getY() + height, color);
        graphics.fill(button.getX(), button.getY(), button.getX() + 1, button.getY() + height, color);
        graphics.fill(button.getX() + width - 1, button.getY(), button.getX() + width, button.getY() + height, color);
    }


    /**
     * This method renders icons in spot where delete and apply button should be.
     *
     * @param graphics The pose stack.
     * @param mouseX Cursor X location
     * @param mouseY Cursor Y location
     */
    private void renderOtherButtons(@NotNull GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (this.blockEntityInterface.getOwner().
            map(uuid -> uuid.equals(this.minecraft.player.getUUID())).
            orElse(true))
        {
            // Render icon instead of delete button.
            graphics.blit(RenderType::guiTextured,
                TEXTURE,
                this.deleteButton.getX() + 1,
                this.deleteButton.getY() + 1,
                176 + (this.selectedButton != -1 ? 0 : 9),
                39,
                9,
                12,
            256,
            256);

            // Render icon instead of apply button.
            graphics.blit(RenderType::guiTextured,
                TEXTURE,
                this.applyButton.getX() + 1,
                this.applyButton.getY() + 1,
                176 + (this.selectedButton != -1 ? 0 : 12),
                51,
                12,
                12,
            256,
            256);
        }

        // Render configure icon.
        graphics.blit(RenderType::guiTextured,
            TEXTURE,
            this.configureButton.getX(),
            this.configureButton.getY(),
            176,
            63,
            10,
            10,
            256,
            256);
    }


    /**
     * This method renders the scrollbar thumb in proper position.
     *
     * @param graphics The pose stack.
     * @param mouseX Cursor X location
     * @param mouseY Cursor Y location
     */
    private void renderScrollBar(@NotNull GuiGraphics graphics, int mouseX, int mouseY)
    {
        int scrollThumbHeight = 15;
        int scrollPosition;

        if (this.needsScrollBars())
        {
            // Calculate scrollbar thumb position, accounting for the thumb's height
            // This ensures that when fully scrolled, the bottom of the thumb aligns with bottom of track
            scrollPosition = this.bodyTopPos +
                (int) ((this.buttonAreaHeight - scrollThumbHeight) * this.currentVariantScroll);
        }
        else
        {
            // Move scrollbar thumb to the top.
            scrollPosition = this.bodyTopPos;
        }

        graphics.blit(RenderType::guiTextured,
            TEXTURE,
            this.leftPos + 9,
            scrollPosition,
            176 + (this.needsScrollBars() ? 0 : 12),
            0,
            12,
            scrollThumbHeight,
            256,
            256);
    }


    /**
     * This method renders the entity size bar in proper position.
     *
     * @param graphics The pose stack
     * @param mouseX Cursor X location
     * @param mouseY Cursor Y location
     * @param partialTicks Partial Ticks
     */
    private void renderTextBar(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        Component text = Component.translatable(FIXED_TEXT,
            this.blockEntityInterface.getAnimalCount());

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        float textWidth = this.font.width(text);
        float scale = Math.min(1f, this.sliderAreaWidth / textWidth);

        poseStack.translate(this.sliderBarPos,
            this.topPos + 112 + 7 - this.font.lineHeight * scale / 2f,
            0);
        poseStack.scale(scale, scale, 1.0f);

        graphics.drawString(this.font,
            text,
            scale < 1 ? 0 : (int) (this.sliderAreaWidth - textWidth) / 2,
            0,
            0xffffff);

        poseStack.popPose();
    }


    /**
     * This method renders the entity in proper position.
     *
     * @param graphics The pose stack.
     */
    private void renderEntity(@NotNull GuiGraphics graphics, float partialTicks)
    {
//        this.enableScissor(
//            this.leftPos + 73, this.bodyTopPos,
//            this.leftPos + 73 + 96, this.bodyTopPos + 92
//        );

        // 73 till black box and 48 till the box middle
        float x = this.leftPos + 73 + 48;
        // 46 till black box middle
        float y = this.bodyTopPos + 46;

        this.displayEntity.yBodyRot = 0.0f;
        this.displayEntity.setYRot(0.0f);
        this.displayEntity.yHeadRot = 0.0f;
        this.displayEntity.yHeadRotO = 0.0f;

        if (this.displayEntity instanceof WaterAnimal animal)
        {
            animal.setPose(Pose.SWIMMING);
            animal.setSwimming(true);
            ((EntityAccessor) animal).setWasTouchingWater(true);
            y -= 10;
        }

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 50);
        poseStack.scale(this.entityScale, this.entityScale, this.entityScale);
        poseStack.translate(0, this.entityOffset, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotationDegrees(this.entityRotation));
        EntityRenderDispatcher erd = Minecraft.getInstance().getEntityRenderDispatcher();
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        erd.setRenderShadow(false);
        erd.render(this.displayEntity, 0, 0, 0, partialTicks, poseStack, immediate, 0xF000F0);
        erd.setRenderShadow(true);
        immediate.endBatch();
        poseStack.popPose();

//        this.disableScissor();
    }


    /**
     * This method renders the cooldown menu and button.
     *
     * @param graphics The pose stack
     * @param mouseX Cursor X location
     * @param mouseY Cursor Y location
     * @param partialTicks Partial Ticks
     */
    private void renderCooldown(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        RenderSystem.setShaderTexture(0, COOLDOWN_TEXTURE);

        graphics.blit(RenderType::guiTextured,
            COOLDOWN_TEXTURE,
            this.leftPos - 12,
            this.topPos + (this.imageHeight - 18) / 2,
            149 + (this.isCooldownOpened ? 0 : 11),
            (this.cooldownButton.isMouseOver(mouseX, mouseY) ? 18 : 1),
            11,
            18,
            256,
            256);

        if (this.isCooldownOpened)
        {
            graphics.blit(RenderType::guiTextured,
                COOLDOWN_TEXTURE,
                this.leftPos - 12 - this.cooldownWidth,
                this.topPos,
                0,
                1,
                this.cooldownWidth,
                this.imageHeight + 1,
                256,
                256);

            List<Pair<ItemStack[], Component>> textList = this.blockEntityInterface.getCooldownLines(false);

            if (!textList.isEmpty())
            {
                int leftOffset = this.leftPos - this.cooldownWidth;
                int top = this.topPos + this.font.lineHeight;

                for (int i = 0; i < textList.size(); i++)
                {
                    this.renderTextLine(graphics, textList.get(i), leftOffset, top + i * 16, mouseX, mouseY);
                }
            }
        }
    }


    /**
     * This method renders text component and inserts icons in their correct spots.
     *
     * @param graphics The pose stack.
     * @param componentPair The pair that contains icons and text
     * @param leftOffset Offset from left side.
     * @param y The offset from top side.
     * @param mouseX The mouse X location.
     * @param mouseY The mouse Y location.
     */
    private void renderTextLine(@NotNull GuiGraphics graphics,
        Pair<ItemStack[], Component> componentPair,
        int leftOffset,
        int y,
        int mouseX,
        int mouseY)
    {
        Component text = componentPair.getRight();
        ItemStack first = componentPair.getLeft().length > 0 ? componentPair.getLeft()[0] : null;
        ItemStack second = componentPair.getLeft().length > 1 ? componentPair.getLeft()[1] : null;

        // Track positions of rendered items for tooltip detection
        List<Pair<ItemStack, Rect2i>> itemPositions = new ArrayList<>();

        // A bit of hacky way to compact drawing, as usually lang $s is separated with spaced.
        int whiteSpace = this.font.width(" ");
        boolean isFirst = true;

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
                    leftOffset -= whiteSpace;
                }

                // Render the first item
                graphics.renderItem(first, leftOffset, y);
                itemPositions.add(Pair.of(first, new Rect2i(leftOffset, y, 16, 16)));
                leftOffset += 16 - whiteSpace;
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
                    leftOffset -= whiteSpace;
                }

                // Render the second item (if available)
                graphics.renderItem(second, leftOffset, y);
                itemPositions.add(Pair.of(second, new Rect2i(leftOffset, y, 16, 16)));
                leftOffset += 16 - whiteSpace;
            }
            else
            {
                // Render regular text
                graphics.drawString(this.font,
                    part,
                    leftOffset,
                    y + this.font.lineHeight / 2 + 2,
                    0xffffff);

                leftOffset += this.font.width(part);
            }

            isFirst = false;
        }

        // Handle tooltips for all item positions
        for (Pair<ItemStack, Rect2i> itemPos : itemPositions)
        {
            Rect2i rect = itemPos.getRight();

            if (mouseX >= rect.getX() &&
                mouseX <= rect.getX() + rect.getWidth() &&
                mouseY >= rect.getY() &&
                mouseY <= rect.getY() + rect.getHeight())
            {
                graphics.renderTooltip(this.font, itemPos.getLeft(), mouseX, mouseY);
                break;
            }
        }
    }


    private void renderTooltips(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
    {
        // Render tooltips
        if (this.applyButton.isMouseOver(mouseX, mouseY))
        {
            graphics.renderTooltip(this.font, APPLY, mouseX, mouseY);
        }

        if (this.deleteButton.isMouseOver(mouseX, mouseY))
        {
            graphics.renderTooltip(this.font, this.selectedButton != -1 ? DELETE : SELECT_TO_DELETE, mouseX, mouseY);
        }

        if (this.cooldownButton.isMouseOver(mouseX, mouseY))
        {
            graphics.renderTooltip(this.font, this.isCooldownOpened ? COOLDOWN_CLOSE : COOLDOWN_OPEN, mouseX, mouseY);
        }

        if (this.configureButton.isMouseOver(mouseX, mouseY))
        {
            graphics.renderTooltip(this.font, CONFIGURE, mouseX, mouseY);
        }
    }


// ---------------------------------------------------------------------
// Section: Actions
// ---------------------------------------------------------------------


    /**
     * This method indicates if scroll bar is necessary to be enabled or not.
     *
     * @return {@code true} if scroll bar is needed, {@code false} otherwise.
     */
    private boolean needsScrollBars()
    {
        // button height and 106 would be better but this works.
        return this.buttons.size() > 5;
    }


    /**
     * This method handles the deletion of the pose from the list.
     *
     * @param button The deletion button.
     */
    private void handleDeleteButton(Button button)
    {
        if (this.selectedButton == -1)
        {
            return;
        }

        // Remove entity from list.
        NetworkManager.sendToServer(new RemoveDisplayAnimalData(this.position, this.selectedButton));

        // Update data
        this.selectedButton = -1;
        this.applyButton.active = false;

        // Reinit the gui
        this.init();
    }


    /**
     * This method handles the applying of the pose to the animal pen.
     *
     * @param button The apply button.
     */
    private void handleApplyButton(Button button)
    {
        if (this.selectedButton == -1)
        {
            return;
        }

        // Send message to server
        List<CompoundTag> entityVariants = this.blockEntityInterface.getEntityVariants();

        if (this.selectedButton >= entityVariants.size())
        {
            return;
        }

        NetworkManager.sendToServer(new UpdateDisplayAnimalData(this.position,
                entityVariants.get(this.selectedButton)));
        // Update current client gui.
        this.displayEntity.load(entityVariants.get(this.selectedButton));

        this.selectedButton = -1;
    }


    /**
     * This method handles entity pose selection from the menu.
     *
     * @param button The button of pose.
     * @param index The index of pose.
     */
    private void handleVariantButton(Button button, int index)
    {
        if (button.getY() + button.getHeight() < this.bodyTopPos ||
            button.getY() > this.bodyTopPos + this.buttonAreaHeight)
        {
            return;
        }

        if (this.selectedButton == index)
        {
            this.selectedButton = -1;
            this.applyButton.active = false;
        }
        else
        {
            this.selectedButton = index;
            this.applyButton.active = !this.buttons.isEmpty() &&
                this.blockEntityInterface.getOwner().
                    map(uuid -> uuid.equals(this.minecraft.player.getUUID())).
                    orElse(true);
        }

        CompoundTag tag;

        if (this.selectedButton != -1)
        {
            tag = (CompoundTag) this.blockEntityInterface.getEntityVariants().get(index);
        }
        else
        {
            tag = new CompoundTag();
            this.blockEntityInterface.getStoredAnimal().ifPresent(entity -> entity.save(tag));
        }

        this.displayEntity.load(tag);
        this.currentXOnEntity = 0;
    }


    /**
     * This method handles the configure button that opens new menu.
     *
     * @param button The configure button.
     */
    private void handleConfigureButton(Button button)
    {
        this.minecraft.setScreen(new VariantsConfigScreen(this));
    }


    private void handleCooldownButton(Button button)
    {
        this.isCooldownOpened = !this.isCooldownOpened;
        this.init();
    }


// ---------------------------------------------------------------------
// Section: Mouse actions
// ---------------------------------------------------------------------


    /**
     * Indicate if player clicks on scroll bar
     *
     * @param mouseX cursor x location
     * @param mouseY cursor y location
     * @return {@code true} if scrollbar is clicked, {@code false} otherwise.
     */
    private boolean scrollBarClicked(double mouseX, double mouseY)
    {
        int scrollLeft = this.leftPos + 9;
        int scrollRight = scrollLeft + 12;
        int scrollBottom = this.bodyTopPos + this.buttonAreaHeight;

        return mouseX >= (double) scrollLeft &&
            mouseY >= (double) this.bodyTopPos &&
            mouseX < (double) scrollRight &&
            mouseY < (double) scrollBottom;
    }


    /**
     * Indicate if player clicks on entity area
     *
     * @param mouseX cursor x location
     * @param mouseY cursor y location
     * @return {@code true} if entity area is clicked, {@code false} otherwise.
     */
    private boolean entityAreaClicked(double mouseX, double mouseY)
    {
        int left = this.leftPos + 73;
        int top = this.bodyTopPos;
        int right = left + 96;
        int bottom = top + 92;

        return mouseX >= left && mouseX < right && mouseY >= top && mouseY < bottom;
    }


    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.scrollBarClicked(mouseX, mouseY))
        {
            this.isScrollingVariants = true;
            this.mouseDragged(mouseX, mouseY, button, 0, 0);
            return true;
        }
        else if (this.entityAreaClicked(mouseX, mouseY))
        {
            this.currentXOnEntity = (int) mouseX;
            this.isSelectingEntity = true;
            return true;
        }
        else
        {
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }


    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.isSelectingEntity = false;
        this.isScrollingVariants = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double var, double amount)
    {
        if (!this.needsScrollBars())
        {
            return false;
        }
        else
        {
            // Adjust scroll with a consistent step size
            this.currentVariantScroll = (float) ((double) this.currentVariantScroll - amount / 10.0);
            this.currentVariantScroll = Mth.clamp(this.currentVariantScroll, 0.0F, 1.0F);
            this.updateButtonPositions();
            return true;
        }
    }


    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY)
    {
        if (this.isScrollingVariants && this.needsScrollBars())
        {
            int scrollBottom = this.bodyTopPos + this.buttonAreaHeight;

            // Calculate normalized scroll position (0.0 to 1.0)
            this.currentVariantScroll = ((float) mouseY - (float) this.bodyTopPos) /
                ((float) (scrollBottom - this.bodyTopPos));
            this.currentVariantScroll = Mth.clamp(this.currentVariantScroll, 0.0F, 1.0F);
            this.updateButtonPositions();
            return true;
        }
        else if (this.isSelectingEntity)
        {
            int deltaX = (int) mouseX - this.currentXOnEntity;
            this.entityRotation -= deltaX;

            // Keep rotation in 0-360 range
            while (this.entityRotation < 0)
            {
                this.entityRotation += 360;
            }
            while (this.entityRotation >= 360)
            {
                this.entityRotation -= 360;
            }

            this.currentXOnEntity = (int) mouseX;
            return true;
        }
        else
        {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if ((keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) &&
            this.buttons.size() > 1)
        {
            int button;

            if (keyCode == GLFW.GLFW_KEY_DOWN)
            {
                button = Math.min(this.selectedButton + 1, this.buttons.size() - 1);
            }
            else
            {
                button = Math.max(this.selectedButton - 1, 0);
            }

            if (button != this.selectedButton)
            {
                this.ensureButtonVisible(button);
                this.handleVariantButton(this.buttons.get(button), button);
            }

            return true;
        }
        else if (keyCode == GLFW.GLFW_KEY_ENTER)
        {
            if (this.selectedButton >= 0 &&
                this.selectedButton < this.buttons.size() &&
                this.applyButton.isActive())
            {
                this.handleApplyButton(null);
                return true;
            }
        }
        else if (keyCode == GLFW.GLFW_KEY_DELETE)
        {
            if (this.selectedButton >= 0 && this.selectedButton < this.buttons.size())
            {
                this.handleDeleteButton(null);
                return true;
            }
        }

        // Handle other key presses with the parent implementation
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        return super.keyReleased(keyCode, scanCode, modifiers);
    }


    /**
     * This method updates Y position for all buttons.
     */
    private void updateButtonPositions()
    {
        int buttonHeight = 20;

        // Calculate total content height
        int totalButtonsHeight = this.buttons.size() * buttonHeight;

        // Calculate maximum possible scroll offset
        int maxScrollPixels = Math.max(0, totalButtonsHeight - this.buttonAreaHeight);

        // Convert normalized scroll (0.0-1.0) to pixel offset
        int scrollOffset = (int) (maxScrollPixels * this.currentVariantScroll);

        // Update button positions based on scroll offset
        for (int i = 0; i < this.buttons.size(); i++)
        {
            Button button = this.buttons.get(i);
            button.setY(this.bodyTopPos + (i * buttonHeight) - scrollOffset);
        }
    }


    /**
     * This method ensures that selected button is on the screen.
     */
    private void ensureButtonVisible(int buttonIndex)
    {
        if (buttonIndex < 0 || buttonIndex >= this.buttons.size())
        {
            return;
        }

        Button targetButton = this.buttons.get(buttonIndex);
        int buttonTop = targetButton.getY();
        int buttonBottom = buttonTop + targetButton.getHeight();
        float areaDifference = this.buttons.size() * targetButton.getHeight() - this.buttonAreaHeight;

        if (buttonTop < this.bodyTopPos)
        {
            float newScrollPos = (this.bodyTopPos - buttonTop) / areaDifference;
            this.currentVariantScroll = Mth.clamp(this.currentVariantScroll - newScrollPos, 0.0f, 1.0f);
            this.updateButtonPositions();
        }
        else if (buttonBottom > this.bodyTopPos + this.buttonAreaHeight)
        {
            float newScrollPos = (buttonBottom - this.bodyTopPos - buttonAreaHeight) / areaDifference;
            this.currentVariantScroll = Mth.clamp(this.currentVariantScroll + newScrollPos, 0.0f, 1.0f);
            this.updateButtonPositions();
        }
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    /**
     * The position of tile entity.
     */
    private final BlockPos position;

    /**
     * The texture image width.
     */
    private final int imageWidth;

    /**
     * The cooldown image width.
     */
    private final int cooldownWidth;

    /**
     * The texture image height.
     */
    private final int imageHeight;

    /**
     * The list of variant buttons for menu.
     */
    private final List<Button> buttons = new ArrayList<>();

    /**
     * The height of variant selection button area
     */
    private final int buttonAreaHeight = 106;

    /**
     * The width of slider area for size
     */
    private final int sliderAreaWidth = 64;

    /**
     * This variable stores current animal pen.
     */
    protected AbstractAnimalPenBlockEntity blockEntityInterface;

    /**
     * The left position of the menu.
     */
    private int leftPos;

    /**
     * The top position of menu.
     */
    private int topPos;

    /**
     * The position of main body message from top.
     */
    private int bodyTopPos;

    /**
     * The position of size slider
     */
    private int sliderBarPos;

    /**
     * This float stores current scroll value.
     */
    private float currentVariantScroll;

    /**
     * This boolean indicates if player is scrolling through variants
     */
    private boolean isScrollingVariants;

    /**
     * This boolean indicates if player opened cooldown view.
     */
    private boolean isCooldownOpened;

    /**
     * The selected button index.
     */
    private int selectedButton = -1;

    /**
     * Button that allows to delete variant.
     */
    private Button deleteButton;

    /**
     * Button that indicates that player wants to apply selected variant.
     */
    private Button applyButton;

    /**
     * Button that allows to configure current pen.
     */
    private Button configureButton;

    /**
     * Button that indicates that player wants to cooldown menu.
     */
    private Button cooldownButton;

    /**
     * The entity that is rendered in menu.
     */
    private LivingEntity displayEntity;

    /**
     * The scale of the entity.
     */
    private float entityScale;

    /**
     * The offset of entity.
     */
    private float entityOffset;

    /**
     * The rotation of entity
     */
    private float entityRotation = -45f;

    /**
     * This variable stores X location of mouse when it was clicked on entity panel.
     */
    private int currentXOnEntity;

    /**
     * This boolean indicates if player is selecting entity
     */
    private boolean isSelectingEntity;

    /**
     * This boolean indicates if player screen requires update.
     */
    private int needsUpdate;

    /**
     * The title of menu
     */
    private static final Component TITLE =
        Component.translatable("gui.animal_pen.variant_selection_screen");

    /**
     * The APPLY of button tooltip
     */
    private static final Component APPLY =
        Component.translatable("gui.animal_pen.variant_selection_screen.apply_tooltip");

    /**
     * The DELETE of button tooltip
     */
    private static final Component DELETE =
        Component.translatable("gui.animal_pen.variant_selection_screen.delete_tooltip");

    /**
     * The DELETE of button tooltip
     */
    private static final Component SELECT_TO_DELETE =
        Component.translatable("gui.animal_pen.variant_selection_screen.select_to_delete_tooltip");

    /**
     * The COOLDOWN of button tooltip
     */
    private static final Component COOLDOWN_OPEN =
        Component.translatable("gui.animal_pen.variant_selection_screen.cooldown_open_tooltip");

    /**
     * The COOLDOWN of button tooltip
     */
    private static final Component COOLDOWN_CLOSE =
        Component.translatable("gui.animal_pen.variant_selection_screen.cooldown_close_tooltip");

    /**
     * The configure button tooltip
     */
    private static final Component CONFIGURE =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure_tooltip");

    /**
     * The button text location
     */
    private static final String BUTTON_TEXT = "gui.animal_pen.variant_selection_screen.select_variant";

    /**
     * The fixed size text location
     */
    private static final String FIXED_TEXT = "gui.animal_pen.variant_selection_screen.fixed_size";

    /**
     * The texture of menu
     */
    private static final ResourceLocation TEXTURE =
        AnimalPen.resourceOf("textures/gui/animal_selection.png");

    /**
     * The texture of cooldown
     */
    private static final ResourceLocation COOLDOWN_TEXTURE =
        AnimalPen.resourceOf("textures/gui/cooldown_area.png");
}

