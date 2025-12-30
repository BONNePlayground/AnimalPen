package lv.id.bonne.animalpen.client.screens;


import com.mojang.blaze3d.vertex.PoseStack;

import dev.architectury.networking.NetworkManager;
import lv.id.bonne.animalpen.network.packets.UpdateConfigurationData;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;


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
    }


    @Override
    protected void init()
    {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        boolean canEdit = this.parent.blockEntityInterface.getOwner().isEmpty() ||
            this.parent.blockEntityInterface.getOwner().
                map(owner -> owner.equals(this.minecraft.player.getUUID())).
                orElse(false);

        // Display Animal Amount Slider (1 to maxAnimalAmount)
        this.displayAnimalAmountSlider = new DisplayAnimalAmountSlider(centerX - 100, startY, 200, 20,
            (int) this.displayAnimalAmount, (int) this.maxAnimalAmount);
        this.addRenderableWidget(this.displayAnimalAmountSlider);

        this.displayAnimalAmountSlider.active = canEdit;

        if (!this.parent.blockEntityInterface.canGrowEntity())
        {
            this.displayAnimalAmountSlider.active = false;
        }

        // Minimal Animal Count Input Field
        this.minimalAnimalCountField =
            new EditBox(this.font, centerX - 100, startY + 40, 200, 20, MINIMAL_ANIMAL_COUNT);
        this.minimalAnimalCountField.setValue(String.valueOf(this.minimalAnimalCount));
        this.minimalAnimalCountField.setFilter(this::isValidNumber);
        this.minimalAnimalCountField.setMaxLength(3);
        this.addRenderableWidget(this.minimalAnimalCountField);

        this.minimalAnimalCountField.setEditable(canEdit);

        // Protection Checkbox
        this.protectionCheckbox = new Checkbox(centerX - 100, startY + 80, 200, 20, PROTECTION, this.enableProtection);
        this.addRenderableWidget(this.protectionCheckbox);

        this.protectionCheckbox.active = canEdit;

        // Save Button
        this.addRenderableWidget(new Button(centerX - 102, startY + 120, 100, 20, SAVE, this::onSave));

        // Cancel Button
        this.addRenderableWidget(new Button(centerX + 2, startY + 120, 100, 20, CANCEL, this::onCancel));
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(poseStack);

        // Draw title
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Draw labels
        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        TranslatableComponent size = new TranslatableComponent(
            "gui.animal_pen.variant_selection_screen.configure.entity_size",
            this.displayAnimalAmountSlider.getIntValue(),
            this.maxAnimalAmount);

        drawString(poseStack, this.font, size,
            centerX - 100, startY - 12, 0xFFFFFF);

        drawString(poseStack, this.font, MINIMAL_ANIMAL_COUNT,
            centerX - 100, startY + 28, 0xFFFFFF);

        super.render(poseStack, mouseX, mouseY, partialTick);
    }


    private void onSave(Button button)
    {
        // Save configuration values
        this.displayAnimalAmount = this.displayAnimalAmountSlider.getIntValue();

        // Parse and validate minimal animal count
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
            // If parsing fails, use current value
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
    private static class DisplayAnimalAmountSlider extends AbstractSliderButton
    {
        public DisplayAnimalAmountSlider(int x, int y, int width, int height, int initialValue, int maxValue)
        {
            super(x, y, width, height, TextComponent.EMPTY,
                (double) (initialValue - 1) / Math.max(1, maxValue - 1));
            this.maxValue = maxValue;
            this.updateMessage();
        }


        @Override
        protected void updateMessage()
        {
            this.setMessage(new TextComponent(String.valueOf(getIntValue())));
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


    private final VariantScreenSelection parent;

    private final long maxAnimalAmount;

    private long displayAnimalAmount;

    private long minimalAnimalCount;

    private boolean enableProtection;

    private DisplayAnimalAmountSlider displayAnimalAmountSlider;

    private EditBox minimalAnimalCountField;

    private Checkbox protectionCheckbox;

    public static final TranslatableComponent TITLE = new TranslatableComponent(
        "gui.animal_pen.variant_selection_screen.configure.title");

    public static final TranslatableComponent MINIMAL_ANIMAL_COUNT = new TranslatableComponent(
        "gui.animal_pen.variant_selection_screen.configure.minimal_animal_count");

    public static final TranslatableComponent PROTECTION = new TranslatableComponent(
        "gui.animal_pen.variant_selection_screen.configure.enable_protection");

    public static final TranslatableComponent SAVE = new TranslatableComponent(
        "gui.animal_pen.variant_selection_screen.configure.save");

    public static final TranslatableComponent CANCEL = new TranslatableComponent(
        "gui.animal_pen.variant_selection_screen.configure.cancel");
}