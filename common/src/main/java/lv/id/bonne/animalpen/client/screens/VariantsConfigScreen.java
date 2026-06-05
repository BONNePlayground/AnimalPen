package lv.id.bonne.animalpen.client.screens;


import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.network.packets.UpdateConfigurationData;
import lv.id.bonne.animalpen.platform.Services;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;


public class VariantsConfigScreen extends Screen
{
    public VariantsConfigScreen(VariantScreenSelection parent)
    {
        super(TITLE);
        this.parent = parent;

        this.maxAnimalAmount = this.parent.blockEntityInterface.getAnimalCount();
        this.displayAnimalAmount = this.parent.blockEntityInterface.getAnimalDisplaySize();

        if (!this.parent.blockEntityInterface.canGrowEntity())
        {
            this.displayAnimalAmount = this.maxAnimalAmount;
        }

        this.minimalAnimalCount = this.parent.blockEntityInterface.getProtectedAmount();
        this.enableProtection = this.parent.blockEntityInterface.getOwner().isPresent();

        this.imageWidth = 220;
        this.imageHeight = 176;
    }


    @Override
    protected void init()
    {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        boolean canEdit = this.parent.blockEntityInterface.getOwner().isEmpty() ||
            this.parent.blockEntityInterface.getOwner().
                map(owner -> owner.equals(this.minecraft.player.getUUID())).
                orElse(false);

        // Display Animal Amount Slider (with floating label above)
        this.displayAnimalAmountSlider = new DisplayAnimalAmountSlider(this.leftPos + X_OFFSET_STANDARD,
            this.topPos + Y_OFFSET_SLIDER,
            COMPONENT_WIDTH_FULL,
            COMPONENT_HEIGHT,
            (int) this.displayAnimalAmount,
            (int) this.maxAnimalAmount);
        this.displayAnimalAmountSlider.active = canEdit;

        if (!this.parent.blockEntityInterface.canGrowEntity())
        {
            this.displayAnimalAmountSlider.active = false;
        }

        // Minimal Animal Count Input Field (with floating label above)
        this.minimalAnimalCountField = new EditBox(this.font,
            this.leftPos + X_OFFSET_STANDARD,
            this.topPos + Y_OFFSET_EDIT_BOX,
            COMPONENT_WIDTH_FULL,
            COMPONENT_HEIGHT,
            MINIMAL_ANIMAL_COUNT)
        {
            @Override
            public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int i, int j, float f)
            {
                super.extractWidgetRenderState(graphics, i, j, f);

                graphics.text(VariantsConfigScreen.this.font,
                    MINIMAL_ANIMAL_COUNT,
                    this.getX(),
                    this.getY() - font.lineHeight - LABEL_PADDING_Y,
                    CommonColors.DARK_GRAY,
                    false);

                if (this.isHovered)
                {
                    graphics.setTooltipForNextFrame(VariantsConfigScreen.this.font,
                        VariantsConfigScreen.this.font.split(MINIMAL_ANIMAL_COUNT_TOOLTIP, TOOLTIP_MAX_WIDTH),
                        this.getX() + TOOLTIP_OFFSET_X,
                        this.getY() + this.height + TOOLTIP_OFFSET_Y);
                }
            }

            private boolean isValidText(@NotNull String text) {
                return text.chars().allMatch((c) -> Character.isDigit(c) || c == 45);
            }

            @Override
            public void insertText(@NotNull String input) {
                String before = this.getValue();
                super.insertText(input);
                if (!this.isValidText(this.getValue())) {
                    this.setValue(before);
                }

            }
        };
        this.minimalAnimalCountField.setValue(String.valueOf(this.minimalAnimalCount));
        this.minimalAnimalCountField.setMaxLength(3);
        this.minimalAnimalCountField.setEditable(canEdit);
        this.addRenderableWidget(this.minimalAnimalCountField);

        // Protection Checkbox (built-in inline label)
        this.protectionCheckbox = new CustomCheckBox(this.leftPos + X_OFFSET_STANDARD,
            this.topPos + Y_OFFSET_CHECKBOX,
            COMPONENT_WIDTH_FULL,
            COMPONENT_HEIGHT,
            PROTECTION,
            this.font,
            this.enableProtection);

        this.protectionCheckbox.active = canEdit;
        this.addRenderableWidget(this.protectionCheckbox);

        // Save Button
        this.addRenderableWidget(Button.builder(SAVE, this::onSave).
            pos(this.leftPos + X_OFFSET_SAVE_BTN, this.topPos + Y_OFFSET_BUTTONS).
            size(COMPONENT_WIDTH_HALF, COMPONENT_HEIGHT).
            build());

        // Cancel Button
        this.addRenderableWidget(Button.builder(CANCEL, this::onCancel).
            pos(this.leftPos + X_OFFSET_CANCEL_BTN, this.topPos + Y_OFFSET_BUTTONS).
            size(COMPONENT_WIDTH_HALF, COMPONENT_HEIGHT).
            build());

        this.addRenderableWidget(this.displayAnimalAmountSlider);
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Draw title
        graphics.text(this.font,
            this.title,
            (this.width / 2 - this.font.width(this.title) / 2),
            (this.topPos + Y_OFFSET_TITLE),
            CommonColors.DARK_GRAY,
            false);
    }


    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int i, int j, float f)
    {
        super.extractBackground(graphics, i, j, f);

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }


    private void onSave(Button button)
    {
        // Save configuration values
        this.displayAnimalAmount = this.displayAnimalAmountSlider.getIntValue();

        try
        {
            int inputValue = Integer.parseInt(this.minimalAnimalCountField.getValue());
            if (inputValue < 0)
            {
                this.minimalAnimalCount = 0;
                this.minimalAnimalCountField.setValue("0");
            }
            else
            {
                this.minimalAnimalCount = inputValue;
            }
        }
        catch (NumberFormatException e)
        {
            this.minimalAnimalCountField.setValue(String.valueOf(this.minimalAnimalCount));
        }

        this.enableProtection = this.protectionCheckbox.selected();

        if (this.parent.blockEntityInterface.getOwner().isEmpty() ||
            this.parent.blockEntityInterface.getOwner().
                map(owner -> owner.equals(this.minecraft.player.getUUID())).
                orElse(false))
        {
            this.saveConfiguration();
        }

        // Close the screen
        this.minecraft.setScreen(this.parent);
    }


    private void onCancel(Button button)
    {
        // Close without saving
        this.minecraft.setScreen(this.parent);
    }


    private void saveConfiguration()
    {
        Services.NETWORK.sendToServer(new UpdateConfigurationData(this.parent.getPosition(),
            this.displayAnimalAmount,
            this.minimalAnimalCount,
            Optional.ofNullable(this.enableProtection ? this.minecraft.player.getUUID() : null)));
    }


    @Override
    public boolean isPauseScreen()
    {
        return false;
    }


    @Override
    public void onClose()
    {
        this.minecraft.setScreen(this.parent);
    }


    // Input validation for number field
    private boolean isValidNumber(String input)
    {
        if (input.isEmpty())
        {
            return true; // Allow empty input for editing
        }

        try
        {
            int value = Integer.parseInt(input);
            return value >= 0; // Allow positive numbers (will be validated to minimum 0 on save)
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }


    // Custom slider for display animal amount
    private class DisplayAnimalAmountSlider extends AbstractSliderButton
    {
        public DisplayAnimalAmountSlider(int x, int y, int width, int height, int initialValue, int maxValue)
        {
            super(x, y, width, height, Component.empty(), (double) (initialValue - 1) / Math.max(1, maxValue - 1));
            this.maxValue = maxValue;
            this.updateMessage();
        }


        @Override
        public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int i, int j, float f)
        {
            super.extractWidgetRenderState(graphics, i, j, f);

            Component size = Component.translatable(
                "gui.animal_pen.variant_selection_screen.configure.entity_size",
                this.getIntValue(),
                VariantsConfigScreen.this.maxAnimalAmount);

            graphics.text(VariantsConfigScreen.this.font,
                size,
                this.getX(),
                this.getY() - font.lineHeight - LABEL_PADDING_Y,
                CommonColors.DARK_GRAY,
                false);

            if (this.isHovered)
            {
                Component component = this.isActive() ? ENTITY_SIZE_TOOLTIP :
                    Component.literal("").
                        append(ENTITY_SIZE_TOOLTIP).
                        append("\n").
                        append(ENTITY_SIZE_DISABLED_TOOLTIP);

                graphics.setTooltipForNextFrame(VariantsConfigScreen.this.font,
                    VariantsConfigScreen.this.font.split(component, TOOLTIP_MAX_WIDTH),
                    this.getX() + TOOLTIP_OFFSET_X,
                    this.getY() + this.height + TOOLTIP_OFFSET_Y);
            }
        }


        @Override
        protected void updateMessage()
        {
            this.setMessage(Component.literal(String.valueOf(getIntValue())));
        }


        @Override
        protected void applyValue()
        {
            // Value is automatically applied through getIntValue()
        }


        public int getIntValue()
        {
            if (this.maxValue == 1)
            {
                return 1;
            }
            int minValue = 1;
            return minValue + (int) ((this.maxValue - minValue) * this.value);
        }


        private final int maxValue;
    }


    private static class CustomCheckBox extends AbstractButton
    {
        private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE =
            Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");

        private static final Identifier CHECKBOX_SELECTED_SPRITE =
            Identifier.withDefaultNamespace("widget/checkbox_selected");

        private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE =
            Identifier.withDefaultNamespace("widget/checkbox_highlighted");

        private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");

        private boolean selected;

        CustomCheckBox(int x, int y, int width, int height, Component component, Font font, boolean selected)
        {
            super(x, y, width, height, component);
            this.selected = selected;
        }


        public boolean selected()
        {
            return this.selected;
        }


        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput)
        {
            narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
            if (this.active)
            {
                if (this.isFocused())
                {
                    narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.focused"));
                }
                else
                {
                    narrationElementOutput.add(NarratedElementType.USAGE,
                        Component.translatable("narration.checkbox.usage.hovered"));
                }
            }
        }


        @Override
        public void onPress(InputWithModifiers inputWithModifiers)
        {
            this.selected = !this.selected;
        }


        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int i, int j, float f)
        {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            Identifier resourceLocation;
            if (this.selected)
            {
                resourceLocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
            }
            else
            {
                resourceLocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
            }

            int k = 15;
            Objects.requireNonNull(font);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), k, k);

            guiGraphics.text(font,
                PROTECTION,
                this.getX() + k + 4,
                this.getY() + (this.height - font.lineHeight) / 2,
                CommonColors.DARK_GRAY,
                false);

            if (this.isHovered)
            {
                guiGraphics.setTooltipForNextFrame(font,
                    font.split(PROTECTION_TOOLTIP, TOOLTIP_MAX_WIDTH),
                    this.getX() + TOOLTIP_OFFSET_X,
                    this.getY() + this.height + TOOLTIP_OFFSET_Y);
            }
        }
    }


    private final int imageWidth;

    private final int imageHeight;

    private final VariantScreenSelection parent;

    private final long maxAnimalAmount;

    private int leftPos;

    private int topPos;

    private long displayAnimalAmount;

    private long minimalAnimalCount;

    private boolean enableProtection;

    private DisplayAnimalAmountSlider displayAnimalAmountSlider;

    private EditBox minimalAnimalCountField;

    private CustomCheckBox protectionCheckbox;

    public static final Component TITLE =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.title");

    public static final Component MINIMAL_ANIMAL_COUNT =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.minimal_animal_count");

    public static final Component MINIMAL_ANIMAL_COUNT_TOOLTIP =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.minimal_animal_count.tooltip");

    public static final Component PROTECTION =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.enable_protection");

    public static final Component PROTECTION_TOOLTIP =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.enable_protection.tooltip");

    public static final Component ENTITY_SIZE_TOOLTIP =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.entity_size.tooltip");

    public static final Component ENTITY_SIZE_DISABLED_TOOLTIP =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.entity_size.disabled");

    public static final Component SAVE =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.save");

    public static final Component CANCEL =
        Component.translatable("gui.animal_pen.variant_selection_screen.configure.cancel");

    private static final int COMPONENT_HEIGHT = 20;

    private static final int COMPONENT_WIDTH_FULL = 200;

    private static final int COMPONENT_WIDTH_HALF = 98;

    private static final int X_OFFSET_STANDARD = 10;

    private static final int X_OFFSET_SAVE_BTN = 10;

    private static final int X_OFFSET_CANCEL_BTN = 112;

    private static final int Y_OFFSET_TITLE = 12;

    private static final int Y_OFFSET_SLIDER = 42;

    private static final int Y_OFFSET_EDIT_BOX = 82;

    private static final int Y_OFFSET_CHECKBOX = 114;

    private static final int Y_OFFSET_BUTTONS = 144;

    private static final int LABEL_PADDING_Y = 2;

    private static final int TOOLTIP_MAX_WIDTH = 198;

    private static final int TOOLTIP_OFFSET_X = -8;

    private static final int TOOLTIP_OFFSET_Y = 16;

    private static final Identifier TEXTURE = AnimalPen.resourceOf("textures/gui/config_screen.png");
}