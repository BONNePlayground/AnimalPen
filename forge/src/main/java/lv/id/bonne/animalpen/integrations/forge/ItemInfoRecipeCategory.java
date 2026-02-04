package lv.id.bonne.animalpen.integrations.forge;


import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.integrations.forge.ItemInfoRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public class ItemInfoRecipeCategory implements IRecipeCategory<ItemInfoRecipe>
{
    public static final RecipeType<ItemInfoRecipe> RECIPE_TYPE =
        RecipeType.create(AnimalPen.MOD_ID, "item_info", ItemInfoRecipe.class);

    private final IDrawable background;

    private final IDrawable icon;


    public ItemInfoRecipeCategory(IGuiHelper guiHelper)
    {
        this.background = guiHelper.createBlankDrawable(160, 125);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(Items.WRITABLE_BOOK)); // Or your custom icon
    }


    @Override
    @NotNull
    public RecipeType<ItemInfoRecipe> getRecipeType()
    {
        return RECIPE_TYPE;
    }


    @Override
    @NotNull
    public Component getTitle()
    {
        return new TranslatableComponent("jei.animal_pen.item_info");
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
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // Main item on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1).addItemStack(recipe.item());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStack(recipe.item());

        int maxWidth = 135;
        List<FormattedCharSequence> lines = font.split(recipe.description(), maxWidth);
        int textHeight = lines.size() * (font.lineHeight + 1);
        int startY = 5 + textHeight;

        // Spawn eggs grid
        List<ItemStack> eggs = recipe.spawnEggs();
        int x = 1;
        int y = startY;

        for (ItemStack egg : eggs)
        {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x, y).addItemStack(egg);

            x += 18;
            if (x > 160)
            {
                x = 1;
                y += 18;
            }
        }
    }


    @Override
    public void draw(ItemInfoRecipe recipe, IRecipeSlotsView recipeSlotsView,
        PoseStack poseStack, double mouseX, double mouseY)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        Component description = recipe.description();

        // Simple word wrapping
        int maxWidth = 135;
        int yPos = 5;
        int xPos = 25;

        List<FormattedCharSequence> lines = font.split(description, maxWidth);

        for (FormattedCharSequence line : lines)
        {
            font.draw(poseStack, line, xPos, yPos, 0xFF404040);
            yPos += font.lineHeight + 1;
        }
    }


    @Override
    public ResourceLocation getUid()
    {
        return getRecipeType().getUid();
    }


    @Override
    public Class<? extends ItemInfoRecipe> getRecipeClass()
    {
        return getRecipeType().getRecipeClass();
    }
}