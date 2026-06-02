package lv.id.bonne.animalpen.client.screens;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.network.packets.UpdateConfigurationData;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


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
            public void renderButton(PoseStack poseStack, int i, int j, float f)
            {
                super.renderButton(poseStack, i, j, f);

                VariantsConfigScreen.this.font.draw(poseStack,
                    MINIMAL_ANIMAL_COUNT,
                    this.x,
                    this.y - font.lineHeight - LABEL_PADDING_Y,
                    4210752);

                if (this.isHovered)
                {
                    VariantsConfigScreen.this.renderTooltip(poseStack,
                        VariantsConfigScreen.this.font.split(MINIMAL_ANIMAL_COUNT_TOOLTIP, TOOLTIP_MAX_WIDTH),
                        this.x + TOOLTIP_OFFSET_X,
                        this.y + this.height + TOOLTIP_OFFSET_Y);
                }
            }
        };
        this.minimalAnimalCountField.setValue(String.valueOf(this.minimalAnimalCount));
        this.minimalAnimalCountField.setFilter(this::isValidNumber);
        this.minimalAnimalCountField.setMaxLength(3);
        this.minimalAnimalCountField.setEditable(canEdit);
        this.addRenderableWidget(this.minimalAnimalCountField);

        // Protection Checkbox (built-in inline label)
        this.protectionCheckbox = new Checkbox(this.leftPos + X_OFFSET_STANDARD,
            this.topPos + Y_OFFSET_CHECKBOX,
            COMPONENT_WIDTH_FULL,
            COMPONENT_HEIGHT,
            PROTECTION,
            this.enableProtection,
            false)
        {
            @Override
            public void renderButton(PoseStack poseStack, int i, int j, float f)
            {
                super.renderButton(poseStack, i, j, f);

                VariantsConfigScreen.this.font.draw(poseStack,
                    PROTECTION,
                    this.x + 24,
                    this.y + (this.height - 8) / 2,
                    4210752);

                if (this.isHovered)
                {
                    VariantsConfigScreen.this.renderTooltip(poseStack,
                        VariantsConfigScreen.this.font.split(PROTECTION_TOOLTIP, TOOLTIP_MAX_WIDTH),
                        this.x + TOOLTIP_OFFSET_X,
                        this.y + this.height + TOOLTIP_OFFSET_Y);
                }
            }
        };
        this.protectionCheckbox.active = canEdit;
        this.addRenderableWidget(this.protectionCheckbox);

        // Save Button
        this.addRenderableWidget(new Button(this.leftPos + X_OFFSET_SAVE_BTN,
            this.topPos + Y_OFFSET_BUTTONS,
            COMPONENT_WIDTH_HALF,
            COMPONENT_HEIGHT,
            SAVE,
            this::onSave));

        // Cancel Button
        this.addRenderableWidget(new Button(this.leftPos + X_OFFSET_CANCEL_BTN,
            this.topPos + Y_OFFSET_BUTTONS,
            COMPONENT_WIDTH_HALF,
            COMPONENT_HEIGHT,
            CANCEL,
            this::onCancel));

        this.addRenderableWidget(this.displayAnimalAmountSlider);
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        // Draw screen title
        this.font.draw(poseStack,
            this.title,
            (float) (this.width / 2 - this.font.width(this.title) / 2),
            (float) (this.topPos + Y_OFFSET_TITLE),
            4210752);
    }


    @Override
    public void renderBackground(PoseStack poseStack)
    {
        super.renderBackground(poseStack);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
        NetworkManager.sendToServer(UpdateConfigurationData.ID,
            UpdateConfigurationData.encode(this.parent.getPosition(),
                this.displayAnimalAmount,
                this.minimalAnimalCount,
                this.enableProtection ? this.minecraft.player.getUUID() : null));
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
        public void renderButton(PoseStack poseStack, int i, int j, float f)
        {
            super.renderButton(poseStack, i, j, f);

            Component size = Component.translatable(
                "gui.animal_pen.variant_selection_screen.configure.entity_size",
                this.getIntValue(),
                VariantsConfigScreen.this.maxAnimalAmount);

            VariantsConfigScreen.this.font.draw(poseStack,
                size,
                this.x,
                this.y - font.lineHeight - LABEL_PADDING_Y,
                4210752);

            if (this.isHovered)
            {
                Component component = this.isActive() ? ENTITY_SIZE_TOOLTIP :
                    Component.literal("").
                        append(ENTITY_SIZE_TOOLTIP).
                        append("\n").
                        append(ENTITY_SIZE_DISABLED_TOOLTIP);

                VariantsConfigScreen.this.renderTooltip(poseStack,
                    VariantsConfigScreen.this.font.split(component, TOOLTIP_MAX_WIDTH),
                    this.x + TOOLTIP_OFFSET_X,
                    this.y + this.height + TOOLTIP_OFFSET_Y);
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

    private Checkbox protectionCheckbox;

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

    private static final ResourceLocation TEXTURE = AnimalPen.resourceOf("textures/gui/config_screen.png");
}