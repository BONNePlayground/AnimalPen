package lv.id.bonne.animalpen.integration.jei.category;


import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import lv.id.bonne.animalpen.integration.jei.recipe.ItemInfoRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;


public abstract class AbstractRecipeCategory implements IRecipeCategory<ItemInfoRecipe>
{
    public AbstractRecipeCategory(IGuiHelper guiHelper, ItemStack itemStack)
    {
        this.background = guiHelper.createBlankDrawable(BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
    }


    @Override
    @NotNull
    public IDrawable getBackground()
    {
        return this.background;
    }


    @Override
    @NotNull
    public IDrawable getIcon()
    {
        return this.icon;
    }


    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ItemInfoRecipe recipe, IFocusGroup focuses)
    {
        // Animal Cage (or Bird Catcher) — the tool the player uses
        builder.addSlot(RecipeIngredientRole.INPUT, TOOL_SLOT_X, TOOL_SLOT_Y)
            .addItemStack(recipe.item());

        // Spawn egg — what the player gets
        builder.addSlot(RecipeIngredientRole.OUTPUT, EGG_SLOT_X, EGG_SLOT_Y)
            .addItemStack(recipe.spawnEgg());
    }


    @Override
    public void draw(ItemInfoRecipe recipe, IRecipeSlotsView recipeSlotsView,
        PoseStack poseStack, double mouseX, double mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // "Right-click with Animal Cage to capture"
        Component description = recipe.description();

        List<FormattedCharSequence> lines = font.split(description, TEXT_MAX_WIDTH);
        int yPos = TEXT_Y;

        for (FormattedCharSequence line : lines)
        {
            font.draw(poseStack, line, TEXT_X, yPos, 0xFF404040);
            yPos += font.lineHeight + 1;
        }
    }

    private final IDrawable background;

    private final IDrawable icon;

    // Slot positions
    private static final int TOOL_SLOT_X = 1;

    private static final int TOOL_SLOT_Y = 5;

    private static final int EGG_SLOT_X = 135;

    private static final int EGG_SLOT_Y = 5;

    private static final int BACKGROUND_WIDTH = 160;

    private static final int BACKGROUND_HEIGHT = 40;

    private static final int TEXT_X = 25;

    private static final int TEXT_Y = 5;

    private static final int TEXT_MAX_WIDTH = 130;
}